CREATE TABLE `member` (
    `member_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `email` varchar(255) UNIQUE NOT NULL COMMENT '로그인 ID 및 발송 주소',
    `nickname` varchar(255),
    `membership` ENUM('NORMAL', 'PREMIUM') COMMENT '등급',
    `provider` varchar(255) COMMENT 'google/naver',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP COMMENT '탈퇴 시간'
);

CREATE TABLE `learning_subscription` (
    `subscription_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `member_id` int NOT NULL,
    `study_language` varchar(30) NOT NULL COMMENT '학습 언어',
    `explanation_language` varchar(30) NOT NULL COMMENT '해설 언어',
    `learning_style` varchar(30) NOT NULL COMMENT '학습 스타일',
    `difficulty_level` varchar(30) NOT NULL COMMENT '난이도',
    `delivery_time` time NOT NULL COMMENT '매일 발송 시간',
    `is_active` boolean NOT NULL DEFAULT true COMMENT '활성 여부',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `learning_content` (
    `content_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `study_language` varchar(30) NOT NULL COMMENT '학습 언어',
    `explanation_language` varchar(30) NOT NULL COMMENT '해설 언어',
    `learning_style` varchar(30) NOT NULL COMMENT '학습 스타일',
    `difficulty_level` varchar(30) NOT NULL COMMENT '난이도',
    `title` varchar(255) NOT NULL COMMENT '콘텐츠 제목',
    `source_text` text NOT NULL COMMENT '원문 텍스트',
    `expression_one` varchar(255) COMMENT '핵심 표현 1',
    `expression_two` varchar(255) COMMENT '핵심 표현 2',
    `source_link` varchar(2048) COMMENT '원문 링크',
    `content_source_type` varchar(30) COMMENT '콘텐츠 원천 타입',
    `source_name` varchar(120) COMMENT '원천 이름',
    `source_host` varchar(255) COMMENT '원천 호스트',
    `source_page_age` varchar(120) COMMENT '원천 페이지 발행 정보',
    `quality_score` int COMMENT '콘텐츠 품질 점수',
    `raw_snippets` text COMMENT '원천 원문 후보',
    `generation_version` varchar(30) COMMENT '생성 버전',
    `published_date` date NOT NULL COMMENT '발행 기준일',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `delivery_log` (
    `log_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `subscription_id` int NOT NULL COMMENT '학습 구독 ID',
    `content_id` int COMMENT '발송된 학습 콘텐츠 ID',
    `sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '발송 시간',
    `status` ENUM('SUCCESS', 'FAIL') COMMENT '발송 결과',
    `issue_date` date NOT NULL COMMENT '발송 기준일',
    `fail_reason` text COMMENT '실패 시 에러 메시지 저장'
);

CREATE UNIQUE INDEX `learning_subscription_unique_settings`
    ON `learning_subscription` (`member_id`, `study_language`, `explanation_language`, `learning_style`, `difficulty_level`, `delivery_time`);

CREATE UNIQUE INDEX `learning_content_unique_daily_settings`
    ON `learning_content` (`study_language`, `explanation_language`, `learning_style`, `difficulty_level`, `published_date`);

ALTER TABLE `learning_subscription`
    ADD FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `delivery_log`
    ADD FOREIGN KEY (`subscription_id`) REFERENCES `learning_subscription` (`subscription_id`);

ALTER TABLE `delivery_log`
    ADD FOREIGN KEY (`content_id`) REFERENCES `learning_content` (`content_id`);
