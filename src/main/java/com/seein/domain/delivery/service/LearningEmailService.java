package com.seein.domain.delivery.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.service.LearningContentTemplateFactory;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.entity.LearningSubscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 학습 이메일 발송 서비스
 */
@Slf4j
@Service
public class LearningEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final LearningContentTemplateFactory templateFactory;
    private final String homeUrl;

    public LearningEmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            LearningContentTemplateFactory templateFactory,
            @Value("${app.base-url}") String appBaseUrl
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.templateFactory = templateFactory;
        this.homeUrl = normalizeHomeUrl(appBaseUrl);
    }

    /**
     * 학습 이메일 발송
     */
    public void sendLearningEmail(LearningSubscription subscription, LearningContent learningContent) throws MessagingException {
        SubscriptionResponse subscriptionResponse = SubscriptionResponse.from(subscription);
        LearningContentCardResponse contentResponse = LearningContentCardResponse.from(learningContent, templateFactory);

        Context context = new Context();
        context.setVariable("subscription", subscriptionResponse);
        context.setVariable("content", contentResponse);
        context.setVariable("homeUrl", homeUrl);
        String html = templateEngine.process("email/learning-digest", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(subscription.getMember().getEmail());
        helper.setSubject(String.format(
                "[SEE-INSIGHT] %s %s",
                subscription.getStudyLanguage().getLabel(),
                subscription.getLearningStyle().getLabel()
        ));
        helper.setText(html, true);

        mailSender.send(message);
        log.info("학습 이메일 발송 성공 - subscriptionId={}, email={}", subscription.getSubscriptionId(), subscription.getMember().getEmail());
    }

    /*
     * 이메일 클라이언트는 상대 경로를 안정적으로 처리하지 못하므로 절대 URL을 전달한다.
     */
    private String normalizeHomeUrl(String appBaseUrl) {
        return appBaseUrl.endsWith("/") ? appBaseUrl : appBaseUrl + "/";
    }
}
