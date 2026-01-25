CREATE TABLE `member` (
`member_id` int PRIMARY KEY AUTO_INCREMENT,
`email` varchar(255) UNIQUE NOT NULL COMMENT '로그인 ID 및 발송 주소',
`nickname` varchar(255),
`is_active` boolean DEFAULT true COMMENT '탈퇴 여부',
`created_at` timestamp DEFAULT (now()),
`membership` varchar(255) COMMENT 'normal, premium'
);

CREATE TABLE `keyword` (
`keyword_id` int PRIMARY KEY AUTO_INCREMENT,
`keyword` varchar(255) UNIQUE NOT NULL COMMENT '키워드 (예: Java, 주식)',
`created_at` timestamp DEFAULT (now())
);

CREATE TABLE `subscription` (
`subscription_id` int PRIMARY KEY AUTO_INCREMENT,
`member_id` int,
`keyword_id` int,
`is_active` boolean DEFAULT true COMMENT '알림 설정',
`notification_at` varchar(255) COMMENT '발송 설정 시간',
`created_at` timestamp DEFAULT (now())
);

CREATE TABLE `news_card` (
`news_id` int PRIMARY KEY AUTO_INCREMENT,
`keyword_id` int,
`summary_content` text COMMENT 'AI가 요약한 뉴스 본문',
`source_link` varchar(255) COMMENT '뉴스 원문 링크',
`created_date` date NOT NULL COMMENT '파티셔닝 및 조회 기준일',
`created_at` timestamp DEFAULT (now())
);

CREATE TABLE `send_log` (
`log_id` int PRIMARY KEY AUTO_INCREMENT,
`member_id` int,
`sent_at` datetime DEFAULT (now()),
`status` varchar(255) COMMENT 'SUCCESS / FAIL',
`fail_reason` text COMMENT '실패 시 에러 메시지 저장'
);

CREATE UNIQUE INDEX `subscription_index_0` ON `subscription` (`member_id`, `keyword_id`);

CREATE INDEX `news_card_index_1` ON `news_card` (`keyword_id`, `created_date`);

ALTER TABLE `subscription` ADD FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `subscription` ADD FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`keyword_id`);

ALTER TABLE `news_card` ADD FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`keyword_id`);

ALTER TABLE `send_log` ADD FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);
