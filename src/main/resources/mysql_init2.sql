CREATE TABLE `countries`
(
    `name`        varchar(255) NOT NULL,
    `countryCode` varchar(255) NOT NULL,
    `timeZone`    varchar(255) NOT NULL,
    `created`     datetime DEFAULT CURRENT_TIMESTAMP,
    `modified`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `languages`
(
    `id`       varchar(255) NOT NULL,
    `name`     varchar(255) NOT NULL,
    `created`  datetime DEFAULT CURRENT_TIMESTAMP,
    `modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `language_name_unique_key` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `newspapers`
(
    `id`          varchar(255) NOT NULL,
    `name`        varchar(255) NOT NULL,
    `countryName` varchar(255) NOT NULL,
    `languageId`  varchar(255) NOT NULL,
    `active`      bit(1)       NOT NULL DEFAULT b'1',
    `created`     datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`    datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `newspapers_name_countryName_unique_key` (`countryName`, `name`),
    KEY `newspapers_countryName_key` (`countryName`),
    KEY `newspapers_languageId_key` (`languageId`),
    CONSTRAINT `newspaper_countryName_fk` FOREIGN KEY (`countryName`) REFERENCES `countries` (`name`),
    CONSTRAINT `newspaper_languageId_fk` FOREIGN KEY (`languageId`) REFERENCES `languages` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `pages`
(
    `id`           varchar(255) NOT NULL,
    `name`         varchar(255) NOT NULL,
    `newsPaperId`  varchar(255) NOT NULL,
    `parentPageId` varchar(255) NOT NULL,
    `hasData`      bit(1)       NOT NULL,
    `hasChild`     bit(1)       NOT NULL,
    `topLevelPage` bit(1)       NOT NULL,
    `active`       bit(1)       NOT NULL DEFAULT b'1',
    `created`      datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `pages_name_newsPaperId_parentPageId_unique_key` (`newsPaperId`, `name`, `parentPageId`),
    KEY `pages_newsPaperId_key` (`newsPaperId`),
    CONSTRAINT `pages_newsPaperId_fkey_constraint` FOREIGN KEY (`newsPaperId`) REFERENCES `newspapers` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `articles`
(
    `id`               varchar(255) NOT NULL,
    `pageId`           varchar(255) NOT NULL,
    `title`            varchar(255) NOT NULL,
    `articleText`      text         NOT NULL,
    `previewImageLink` text,
    `publicationTime`  datetime     NOT NULL,
    `upOnFirebaseDb`   bit(1)       NOT NULL DEFAULT b'0',
    `upOnFireStore`    bit(1)       NOT NULL DEFAULT b'0',
    `upOnMongoRest`    bit(1)       NOT NULL DEFAULT b'0',
    `created`          datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`         datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `articles_pageId_key` (`pageId`),
    CONSTRAINT `articles_pageId_fkey_constraint` FOREIGN KEY (`pageId`) REFERENCES `pages` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `image_links`
(
    `articleId` varchar(255) NOT NULL,
    `link`      text         NOT NULL,
    `caption`   text,
    `created`   datetime DEFAULT CURRENT_TIMESTAMP,
    KEY `FKtarkqvk2kgymilolrr4g2x3ae` (`articleId`),
    CONSTRAINT `FKtarkqvk2kgymilolrr4g2x3ae` FOREIGN KEY (`articleId`) REFERENCES `articles` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `article_download_log`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `parents`    varchar(255) DEFAULT NULL,
    `logMessage` text,
    `created`    datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `article_uploader_status_change_log`
(
    `id`                        int(11)                                                    NOT NULL AUTO_INCREMENT,
    `status`                    enum ('ON','OFF')                                          NOT NULL,
    `articleDataUploaderTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    `created`                   datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `article_upload_log`
(
    `id`           int(11)                                                    NOT NULL AUTO_INCREMENT,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    `logMessage`   text,
    `created`      datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `exception_log`
(
    `id`                       int(11) NOT NULL AUTO_INCREMENT,
    `exceptionClassFullName`   varchar(255) DEFAULT NULL,
    `exceptionClassSimpleName` varchar(255) DEFAULT NULL,
    `exceptionCause`           text,
    `exceptionMessage`         text,
    `stackTrace`               text,
    `created`                  datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `general_log`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `logMessage` text,
    `created`    datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `settings_update_log`
(
    `id`         int(11)  NOT NULL AUTO_INCREMENT,
    `updateTime` datetime NOT NULL,
    `logMessage` text,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `settings_upload_log`
(
    `id`           int(11)                                                    NOT NULL AUTO_INCREMENT,
    `uploadTime`   datetime                                                   NOT NULL,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `tokens`
(
    `token`     varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `expiresOn` datetime                                NOT NULL,
    `created`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`token`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;