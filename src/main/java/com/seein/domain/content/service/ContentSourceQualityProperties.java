package com.seein.domain.content.service;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 콘텐츠 소스 신뢰도 점수표
 */
@Getter
@Component
@ConfigurationProperties(prefix = "content.source-quality")
public class ContentSourceQualityProperties {

    private List<String> trustedDomains = new ArrayList<>(List.of(
            "bbc.com",
            "reuters.com",
            "apnews.com",
            "npr.org",
            "nhk.or.jp",
            "japantimes.co.jp",
            "asahi.com",
            "mainichi.jp",
            "xinhuanet.com",
            "chinadaily.com.cn",
            "people.com.cn"
    ));

    private List<String> blockedDomains = new ArrayList<>(List.of(
            "facebook.com",
            "instagram.com",
            "x.com",
            "twitter.com",
            "youtube.com",
            "tiktok.com",
            "pinterest.com"
    ));

    /**
     * 신뢰 도메인 설정
     */
    public void setTrustedDomains(List<String> trustedDomains) {
        this.trustedDomains = normalizeDomains(trustedDomains);
    }

    /**
     * 차단 도메인 설정
     */
    public void setBlockedDomains(List<String> blockedDomains) {
        this.blockedDomains = normalizeDomains(blockedDomains);
    }

    public boolean isTrusted(String host) {
        return matchesAny(host, trustedDomains);
    }

    public boolean isBlocked(String host) {
        return matchesAny(host, blockedDomains);
    }

    private List<String> normalizeDomains(List<String> domains) {
        if (domains == null) {
            return new ArrayList<>();
        }
        return domains.stream()
                .map(this::normalizeHost)
                .filter(domain -> !domain.isBlank())
                .distinct()
                .toList();
    }

    private boolean matchesAny(String host, List<String> domains) {
        String normalizedHost = normalizeHost(host);
        if (normalizedHost.isBlank()) {
            return false;
        }

        return domains.stream()
                .map(this::normalizeHost)
                .anyMatch(domain -> normalizedHost.equals(domain) || normalizedHost.endsWith("." + domain));
    }

    private String normalizeHost(String host) {
        if (host == null) {
            return "";
        }
        return host.toLowerCase()
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "")
                .split("/")[0]
                .trim();
    }
}
