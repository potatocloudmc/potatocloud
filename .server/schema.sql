CREATE TABLE IF NOT EXISTS `potato_players` (
    `uuid` CHAR(36) NOT NULL,
    `username` CHAR(36) NOT NULL,
    `nickname` CHAR(36) NOT NULL DEFAULT "",
    `playtime` bigint(20) NOT NULL DEFAULT 0,
    `first_seen` bigint(20) NOT NULL,
    `last_seen` bigint(20) NOT NULL,
    PRIMARY KEY (`uuid`),
    KEY (`username`),
    KEY (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `potato_player_settings` (
    `uuid` CHAR(36) NOT NULL, -- players uniqueId
    `id` CHAR(64) NOT NULL, -- setting id
    `value` CHAR(256) NOT NULL, -- value
    `last_updated` bigint(20) NOT NULL, -- last update of that setting
    PRIMARY KEY (`uuid`, `id`),
    KEY `idx_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `potato_player_relations` (
    `uuid_a` CHAR(36) NOT NULL,
    `uuid_b` CHAR(36) NOT NULL,
    `relation` TINYINT NOT NULL,
    `last_changed` bigint(20) NOT NULL,
    PRIMARY KEY (`uuid_a`, `uuid_b`),
    KEY `idx_uuid_a` (`uuid_a`),
    KEY `idx_uuid_b` (`uuid_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `potato_requests` (
    `uuid_from` CHAR(36) NOT NULL,
    `uuid_to` CHAR(36) NOT NULL,
    `created_at` bigint(20) NOT NULL,
    PRIMARY KEY (`uuid_from`, `uuid_to`),
    KEY `idx_to` (`uuid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;