package com.pdfconvertor.pdf.controller;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "http://localhost:3000") 
public class ImageToPdfController {

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertImageToPdf(@RequestParam("image") MultipartFile imageFile) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDImageXObject image = PDImageXObject.createFromByteArray(
                document,
                imageFile.getBytes(),
                imageFile.getOriginalFilename()
        );

        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        float scale = Math.min(PDRectangle.A4.getWidth() / imageWidth, PDRectangle.A4.getHeight() / imageHeight);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.drawImage(
                image,
                (PDRectangle.A4.getWidth() - imageWidth * scale) / 2,
                (PDRectangle.A4.getHeight() - imageHeight * scale) / 2,
                imageWidth * scale,
                imageHeight * scale
        );
        contentStream.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=image.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }
    @PostMapping("/word-to-pdf")
    public ResponseEntity<byte[]> convertWordToPdf(@RequestParam("file") MultipartFile wordFile) throws IOException {
        PDDocument pdfDoc = new PDDocument();

        try (XWPFDocument doc = new XWPFDocument(wordFile.getInputStream())) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page);

            PDFont pdfFont = new PDType1Font(FontName.TIMES_ROMAN);
            contentStream.setFont(pdfFont, 12);

            float margin = 50;
            float yPosition = PDRectangle.A4.getHeight() - margin;
            float lineHeight = 15;
            float maxWidth = PDRectangle.A4.getWidth() - 2 * margin;

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    text = text.replace("\r", "").replace("\n", " ");

                    List<String> lines = wrapText(text, pdfFont, 12, maxWidth);

                    for (String line : lines) {
                        if (yPosition < margin) {
                            // Start new page
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            pdfDoc.addPage(page);
                            contentStream = new PDPageContentStream(pdfDoc, page);
                            contentStream.setFont(pdfFont, 12);
                            yPosition = PDRectangle.A4.getHeight() - margin;
                        }

                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(line);
                        contentStream.endText();

                        yPosition -= lineHeight;
                    }
                }
            }
            contentStream.close();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfDoc.save(out);
        pdfDoc.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=word.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float size = font.getStringWidth(testLine) / 1000 * fontSize;
            if (size > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

}

