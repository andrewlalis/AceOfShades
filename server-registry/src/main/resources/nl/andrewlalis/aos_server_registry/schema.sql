SET MODE MySQL;

CREATE TABLE servers (
    name VARCHAR(64) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(0) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP(0),
    updated_at TIMESTAMP(0) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP(0),
    description VARCHAR(1024),
    location VARCHAR(64),

    max_players INTEGER NOT NULL,
    current_players INTEGER NOT NULL,

    PRIMARY KEY (name, address),
    CHECK (max_players > 0 AND current_players >= 0)
);

CREATE INDEX server_name_idx ON servers(name);
