CREATE TABLE message (
    id BINARY(16) PRIMARY KEY, -- UNHEX(REPLACE(UUID(),'-','')
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    test boolean default false not null,
    INDEX (created_at)
) ENGINE = RocksDB;
