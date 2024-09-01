package com.futuresign.signing.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import io.minio.*;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Service
public class SigningService {

    @Autowired
    private SignFileRepository signFileRepository;
    private final MinioClient minioClient;
    private final String bucketName;

    public SigningService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created bucket: " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    public SignatureResponse requestSignature(SignatureRequest request) throws Exception {
        SignFile signFile = new SignFile();
        signFile.setId(UUID.randomUUID().toString());
        signFile.setUsername(request.getUsername());
        signFile.setFileKey(request.getFilekey());
        signFile.setStatus("pending");

        signFileRepository.save(signFile);

        // Fetch the document from Minio
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(request.getFilekey())
                        .build()
        );

        byte[] documentData = response.readAllBytes();

        // Sign the document
        byte[] signedDocument = signDocument(request.getUsername(), documentData);

        // Upload the signed document back to Minio
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("your-signed-bucket-name")
                        .object("signed_" + request.getFilekey())
                        .stream(new ByteArrayInputStream(signedDocument), signedDocument.length, -1)
                        .build()
        );

        signFile.setStatus("completed");
        signFileRepository.save(signFile);

        return new SignatureResponse("Signature requested successfully", signFile.getId());
    }

    public StatusResponse checkSignatureStatus(String id) throws EntityNotFoundException {
        SignFile signFile = signFileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SignFile not found"));
        return new StatusResponse(signFile.getStatus());
    }

    private byte[] signDocument(String username, byte[] documentData) throws Exception {
        // Load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream("path/to/keystore.p12");
        keystore.load(fis, "keystorePassword".toCharArray());

        // Get the private key
        PrivateKey privateKey = (PrivateKey) keystore.getKey("alias", "keyPassword".toCharArray());

        // Get the certificate
        X509Certificate cert = (X509Certificate) keystore.getCertificate("alias");

        // Create a PDFReader instance
        PdfReader reader = new PdfReader(documentData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, baos, '\0');

        // Create the signature appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason("Official Document");
        appearance.setLocation("Digital Signature");

        // Create the signature
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, null);
        MakeSignature.signDetached(appearance, digest, signature, new Certificate[]{cert}, null, null, null, 0, MakeSignature.CryptoStandard.CMS);

        stamper.close();
        return baos.toByteArray();
    }
}
