-- Migration V14: Create pt_file_content table for file content storage
-- Maps to ru.pt.files.entity.FileContent

CREATE TABLE IF NOT EXISTS pt_file_content (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    file_body BYTEA NOT NULL
);

drop table if exists pt_file_content;