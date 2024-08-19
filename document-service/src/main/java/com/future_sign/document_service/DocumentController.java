package com.future_sign.document_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        String username = authentication.getName();
        return ResponseEntity.ok(documentService.uploadDocument(file, username));
    }

    @GetMapping("/{id}/sign")
    public ResponseEntity<byte[]> signDocument(@PathVariable Long id, Authentication authentication) throws IOException {
        String username = authentication.getName();
        byte[] signedDocument = documentService.signDocument(id, username);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signed_document.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(signedDocument);
    }
}
