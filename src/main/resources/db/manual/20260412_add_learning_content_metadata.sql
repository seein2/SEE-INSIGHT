ALTER TABLE learning_content
    ADD COLUMN content_source_type VARCHAR(30) NULL AFTER source_link,
    ADD COLUMN source_name VARCHAR(120) NULL AFTER content_source_type,
    ADD COLUMN source_host VARCHAR(255) NULL AFTER source_name,
    ADD COLUMN source_page_age VARCHAR(120) NULL AFTER source_host,
    ADD COLUMN quality_score INT NULL AFTER source_page_age,
    ADD COLUMN raw_snippets TEXT NULL AFTER quality_score,
    ADD COLUMN generation_version VARCHAR(30) NULL AFTER raw_snippets;
