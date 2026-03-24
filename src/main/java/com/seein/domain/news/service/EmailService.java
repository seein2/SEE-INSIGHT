package com.seein.domain.news.service;

import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.news.entity.NewsCard;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스
 * JavaMailSender를 사용하여 뉴스 요약 이메일을 HTML 형태로 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 뉴스 요약 이메일 발송
     *
     * @param subscription 구독 정보 (수신자 이메일, 키워드 포함)
     * @param newsCard     발송할 뉴스 카드
     * @throws MessagingException 이메일 발송 실패 시
     */
    public void sendNewsEmail(Subscription subscription, NewsCard newsCard) throws MessagingException {
        String recipientEmail = subscription.getMember().getEmail();
        String keywordName = subscription.getKeyword().getKeyword();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipientEmail);
        helper.setSubject("[SEE-INSIGHT] " + keywordName + " - 오늘의 뉴스 요약");
        helper.setText(buildHtmlContent(keywordName, newsCard), true);

        mailSender.send(message);
        log.info("이메일 발송 성공 - to: {}, keyword: {}", recipientEmail, keywordName);
    }

    /**
     * HTML 이메일 본문 생성
     */
    private String buildHtmlContent(String keywordName, NewsCard newsCard) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='max-width:600px;margin:0 auto;font-family:Arial,sans-serif;'>");

        // 헤더
        sb.append("<div style='background-color:#2563eb;color:white;padding:20px;text-align:center;'>");
        sb.append("<h1 style='margin:0;font-size:20px;'>SEE-INSIGHT</h1>");
        sb.append("<p style='margin:5px 0 0;font-size:14px;'>").append(keywordName).append(" 뉴스 요약</p>");
        sb.append("</div>");

        // 본문
        sb.append("<div style='padding:20px;background-color:#f9fafb;'>");
        sb.append("<div style='background-color:white;padding:20px;border-radius:8px;border:1px solid #e5e7eb;'>");
        sb.append("<p style='color:#374151;line-height:1.6;white-space:pre-wrap;'>");
        sb.append(escapeHtml(newsCard.getSummaryContent()));
        sb.append("</p>");

        // 출처 링크
        if (newsCard.getSourceLink() != null && !newsCard.getSourceLink().isEmpty()) {
            sb.append("<p style='margin-top:16px;'>");
            sb.append("<a href='").append(newsCard.getSourceLink()).append("' ");
            sb.append("style='color:#2563eb;text-decoration:none;font-size:14px;'>원문 보기 →</a>");
            sb.append("</p>");
        }

        sb.append("</div>");
        sb.append("</div>");

        // 푸터
        sb.append("<div style='padding:16px;text-align:center;color:#9ca3af;font-size:12px;'>");
        sb.append("<p>이 메일은 SEE-INSIGHT에서 구독 설정에 따라 자동으로 발송되었습니다.</p>");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * HTML 특수문자 이스케이프
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
