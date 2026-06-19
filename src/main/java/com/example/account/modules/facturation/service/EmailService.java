package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.ExternalRequest.EmailRequest;
import com.example.account.modules.facturation.domain.model.Devis;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine; // Changed to SpringTemplateEngine
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine; // Use the Spring-specific version
    private final PdfGeneratorService pdfGeneratorService;

   public Mono<Void> sendQuotation(Devis devis, EmailRequest emailRequest,String token) {

    return Mono.fromCallable(() -> {

        log.info("Starting email processing for Devis: {}", devis.getNumeroDevis());

        Context context = new Context();
        context.setVariable("quotationRef", devis.getNumeroDevis());
        context.setVariable("clientName", devis.getNomClient());
        context.setVariable("sellerName",emailRequest.getOrganizationRaisonSociale());
        context.setVariable("baseUrl", "http://localhost:3000");
        context.setVariable("token", token);

        // Render Thymeleaf
        String emailContent = templateEngine.process("quotation-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(devis.getEmailClient());
        helper.setSubject("Quotation " + devis.getNumeroDevis());
        helper.setText(emailContent, true);

        log.info("Generating PDF for Devis: {}", devis.getNumeroDevis());

        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(emailRequest.getHtmlContent());

        helper.addAttachment(
                devis.getNumeroDevis() + ".pdf",
                new ByteArrayResource(pdfBytes),
                "application/pdf"
        );

        mailSender.send(message);

        log.info("Email sent successfully for Devis: {}", devis.getNumeroDevis());

        return true;

    }).subscribeOn(Schedulers.boundedElastic())
      .then();
}
}