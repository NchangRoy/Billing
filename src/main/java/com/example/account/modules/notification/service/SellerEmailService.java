package com.example.account.modules.notification.service;

import com.example.account.modules.notification.dto.SendSellerInvitationEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class SellerEmailService {

    private final SpringTemplateEngine templateEngine;
    private final EmailSenderService emailSenderService;

    @Value("${seller.invite.login-url}")
    private String loginUrl;

    public SellerEmailService(SpringTemplateEngine templateEngine, EmailSenderService emailSenderService) {
        this.templateEngine = templateEngine;
        this.emailSenderService = emailSenderService;
    }

    public Mono<Void> sendInvitation(SendSellerInvitationEmailRequest request) {
        return Mono.fromCallable(() -> {
            Context context = new Context();
            context.setVariable("firstName", request.getFirstName());
            context.setVariable("email", request.getEmail());
            context.setVariable("pin", request.getPin());
            context.setVariable("agency", request.getAgency());
            context.setVariable("role", request.getRole());
            context.setVariable("loginUrl", loginUrl);
            return templateEngine.process("seller-invitation", context);
        }).subscribeOn(Schedulers.boundedElastic())
          .flatMap(html -> emailSenderService.sendEmail(request.getOrganizationId(), request.getEmail(),
                  "Bienvenue sur KSM — vos identifiants de connexion", html))
          .doOnSuccess(v -> log.info("Sent seller invitation email to {}", request.getEmail()));
    }
}
