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
    `player_a` CHAR(36) NOT NULL,
    `player_b` CHAR(36) NOT NULL,
    `relation` TINYINT NOT NULL,
    `last_changed` bigint(20) NOT NULL,
    PRIMARY KEY (`player_a`, `player_b`),
    KEY `idx_player_a` (`player_a`),
    KEY `idx_player_b` (`player_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `potato_requests` (
    `player_from` CHAR(36) NOT NULL,
    `player_to` CHAR(36) NOT NULL,
    `created_at` bigint(20) NOT NULL,
    PRIMARY KEY (`player_from`, `player_to`),
    KEY `idx_to` (`player_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;