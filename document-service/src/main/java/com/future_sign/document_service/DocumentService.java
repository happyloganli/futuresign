package com.future_sign.document_service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

    public Document uploadDocument(MultipartFile file, String username) throws IOException {
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setUsername(username);
        document.setContent(file.getBytes());
        return documentRepository.save(document);
    }

    public byte[] signDocument(Long id, String username) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        PDDocument pdfDocument = PDDocument.load(document.getContent());
        PDPage page = pdfDocument.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, true, true);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(100, 100);
        contentStream.showText("Signed by: " + username);
        contentStream.endText();
        contentStream.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdfDocument.save(baos);
        pdfDocument.close();

        return baos.toByteArray();
    }
}