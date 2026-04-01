package com.seein.domain.delivery.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.entity.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class LearningEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendLearningEmail(Subscription subscription, LearningContent learningContent) throws MessagingException {
        SubscriptionResponse subscriptionResponse = SubscriptionResponse.from(subscription);
        LearningContentCardResponse contentResponse = LearningContentCardResponse.from(learningContent);

        Context context = new Context();
        context.setVariable("subscription", subscriptionResponse);
        context.setVariable("content", contentResponse);
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
        log.info("학습 이메일 발송 성공 - subscriptionId={}, email={}",
                subscription.getSubscriptionId(), subscription.getMember().getEmail());
    }
}
