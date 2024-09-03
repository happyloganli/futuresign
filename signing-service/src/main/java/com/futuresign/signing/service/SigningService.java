package com.futuresign.signing.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;

import io.minio.*;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.UUID;

@Service
public class SigningService {

    @Autowired
    private SignFileRepository signFileRepository;
    private final MinioClient minioClient;
    private final String bucketName;
    @Value("${keystore.password}")
    private String KEYSTORE_PASSWORD;
    @Value("${key.alias}")
    private String KEY_ALIAS;
    @Value("${keystore.name}")
    private String KEYSTORE_NAME;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${signing.exchange.name}")
    private String SIGNING_EXCHANGE_NAME;
    @Value("${syntax.check.routing.key}")
    private String SYNTAX_CHECK_ROUTING_KEY;

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
        signFile.setFileKey(request.getFileKey());
        signFile.setStatus("pending");

        signFileRepository.save(signFile);

        publishSyntaxCheckEvent(request.getFileKey(), request.getUsername());

        // Fetch the document from Minio
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(request.getFileKey())
                        .build()
        );

        byte[] documentData = response.readAllBytes();

        // Sign the document
        byte[] signedDocument = signDocument(documentData);

        // Upload the signed document back to Minio
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object("signed_" + request.getFileKey() + ".pdf")
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

    private byte[] signDocument(byte[] documentData) throws Exception {

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_NAME)) {
            if (keystoreStream == null) {
                throw new IllegalStateException("Keystore file not found in resources: " + KEYSTORE_NAME);
            }
            keystore.load(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
        }

        // Get the private key and certificate chain
        PrivateKey privateKey = (PrivateKey) keystore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());
        Certificate[] certificateChain = keystore.getCertificateChain(KEY_ALIAS);

        // Create PdfReader and ByteArrayOutputStream
        PdfReader reader = new PdfReader(new ByteArrayInputStream(documentData));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create PdfSigner
        PdfSigner signer = new PdfSigner(reader, outputStream, new StampingProperties());

        // Create signature appearance
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance.setReason("Reason for Signing");
        appearance.setLocation("Signer Location");

        // Create the signature
        IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, null);
        IExternalDigest digest = new BouncyCastleDigest();

        // Sign the document using the detached mode, CMS or CAdES equivalent.
        signer.signDetached(digest, pks, certificateChain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

        return outputStream.toByteArray();
    }

    @RabbitListener(queues = "syntax_check_finished")
    public void consumeSyntaxCheckEvent(SyntaxCheckFinishedEvent event) {
        String fileKey = event.getFileKey();
        String username = event.getUsername();
        String checkResult = event.getResult();

        System.out.println("Syntax check finished: " + fileKey);
        System.out.println("Syntax check finished: " + username);
        System.out.println("Syntax check finished: " + checkResult);
    }

    private void publishSyntaxCheckEvent(String fileKey, String username) {
        SyntaxCheckEvent event = new SyntaxCheckEvent(fileKey, username);
        rabbitTemplate.convertAndSend(SIGNING_EXCHANGE_NAME, SYNTAX_CHECK_ROUTING_KEY, event);
    }

}
