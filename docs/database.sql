CREATE TABLE `member` (
    `member_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `email` varchar(255) UNIQUE NOT NULL COMMENT '로그인 ID 및 발송 주소',
    `nickname` varchar(255),
    `membership` ENUM('NORMAL', 'PREMIUM') COMMENT '등급',
    `provider` varchar(255) COMMENT 'google/naver',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP COMMENT '탈퇴 시간'
);

CREATE TABLE `keyword` (
   `keyword_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `keyword` varchar(255) UNIQUE NOT NULL COMMENT '키워드 (예: Java, 주식)',
   `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `subscription` (
    `subscription_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `member_id` int NOT NULL,
    `keyword_id` int NOT NULL,
    `is_active` boolean DEFAULT true COMMENT '알림설정',
    `notification_time` varchar(255) COMMENT '발송 설정 시간',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `news_card` (
     `news_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
     `keyword_id` int NOT NULL,
     `summary_content` text COMMENT 'AI가 요약한 뉴스 본문',
     `source_link` varchar(2048) COMMENT '뉴스 원문 링크',
     `created_date` date NOT NULL COMMENT '파티셔닝 및 조회 기준일'
);

CREATE TABLE `send_log` (
    `log_id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `subscription_id` int NOT NULL COMMENT '구독(키워드) 구분',
    `sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('SUCCESS', 'FAIL') COMMENT '발송 결과',
    `issue_date` datetime COMMENT '발행 날짜',
    `fail_reason` text COMMENT '실패 시 에러 메시지 저장'
);

CREATE UNIQUE INDEX `subscription_index_0` ON `subscription` (`member_id`, `keyword_id`);

CREATE UNIQUE INDEX `news_card_index_1` ON `news_card` (`created_date`, `keyword_id`);

ALTER TABLE `subscription` ADD FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `subscription` ADD FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`keyword_id`);

ALTER TABLE `news_card` ADD FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`keyword_id`);

ALTER TABLE `send_log` ADD FOREIGN KEY (`subscription_id`) REFERENCES `subscription` (`subscription_id`);
