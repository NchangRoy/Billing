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

    /**
     * "A document is waiting for you" notification for the login-based client
     * portal (distinct from sendQuotation's one-off token-link email, which
     * needs no login at all). No PDF attachment — the client views/prints the
     * document once they're inside the portal.
     */
    public Mono<Void> sendPortalDocumentNotification(String recipientEmail, String recipientName,
                                                       String documentType, String documentRef, String loginUrl) {
        return Mono.fromCallable(() -> {
            log.info("Sending portal notification for {} {} to {}", documentType, documentRef, recipientEmail);

            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("documentType", documentType);
            context.setVariable("documentRef", documentRef);
            context.setVariable("loginUrl", loginUrl);

            String emailContent = templateEngine.process("portal-document-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setSubject(documentType + " " + documentRef + " — a document is ready for your review");
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Portal notification sent for {} {}", documentType, documentRef);
            return true;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Notifies a seller they've been granted a permission (owner/editor/viewer)
     * on a document via the "Share" action.
     */
    public Mono<Void> sendDocPermissionInviteNotification(String recipientEmail, String recipientName,
                                                            String sharedByName, String permissionLevel,
                                                            String docLabel, String loginUrl) {
        return Mono.fromCallable(() -> {
            log.info("Sending doc-permission invite ({}) for {} to {}", permissionLevel, docLabel, recipientEmail);

            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("sharedByName", sharedByName);
            context.setVariable("permissionLevel", permissionLevel);
            context.setVariable("docLabel", docLabel);
            context.setVariable("loginUrl", loginUrl);

            String emailContent = templateEngine.process("doc-permission-invite", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setSubject("You've been invited as " + permissionLevel + " on " + docLabel);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Doc-permission invite sent for {}", docLabel);
            return true;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}