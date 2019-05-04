drop table image_links;
drop table exception_log;
drop table general_log;
drop table article_upload_history;
drop table articles;
drop table pages;
drop table newspapers;
drop table countries;
drop table languages;

CREATE TABLE `countries`
(
    `name`        varchar(255) NOT NULL,
    `countryCode` varchar(255) NOT NULL,
    `timeZone`    varchar(255) NOT NULL,
    `created`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `modified`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `languages`
(
    `id`       varchar(255) NOT NULL,
    `name`     varchar(255) NOT NULL,
    UNIQUE KEY `language_name_unique_key` (`name`),
    `created`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `modified` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# Table, Create Table
CREATE TABLE `newspapers`
(
    `id`          varchar(255) NOT NULL,
    `active`      bit(1)       NOT NULL default true,
    `name`        varchar(255) NOT NULL,
    `countryName` varchar(255) NOT NULL,
    `languageId`  varchar(255) NOT NULL,
    `created`     DATETIME              DEFAULT CURRENT_TIMESTAMP,
    `modified`    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `newspapers_name_countryName_unique_key` (`countryName`, `name`),
    KEY `newspapers_countryName_key` (`countryName`),
    KEY `newspapers_languageId_key` (`languageId`),
    CONSTRAINT `newspaper_countryName_fk` FOREIGN KEY (`countryName`) REFERENCES `countries` (`name`),
    CONSTRAINT `newspaper_languageId_fk` FOREIGN KEY (`languageId`) REFERENCES `languages` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# Table, Create Table
CREATE TABLE `pages`
(
    `id`           varchar(255) NOT NULL,
    `parentPageId` varchar(255) NOT NULL,
    `name`         varchar(255) NOT NULL,
    `newsPaperId`  varchar(255) NOT NULL,
    `hasChild`     bit(1)       NOT NULL,
    `hasData`      bit(1)       NOT NULL,
    `topLevelPage` bit(1)       NOT NULL,
    `active`       bit(1)       NOT NULL default true,
    `created`      DATETIME              DEFAULT CURRENT_TIMESTAMP,
    `modified`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `pages_name_newsPaperId_parentPageId_unique_key` (`newsPaperId`, `name`, `parentPageId`),
    KEY `pages_newsPaperId_key` (`newsPaperId`),
    CONSTRAINT `pages_newsPaperId_fkey_constraint` FOREIGN KEY (`newsPaperId`) REFERENCES `newspapers` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `articles`
(
    `id`               varchar(255) NOT NULL,
    `publicationTime`  datetime     NOT NULL,
    `title`            varchar(255) NOT NULL,
    `articleText`      text         NOT NULL,
    `previewImageLink` text                  DEFAULT NULL,
    `pageId`           varchar(255) NOT NULL,
    `upOnFirebaseDb`   bit(1)       NOT NULL default false,
    `upOnFireStore`    bit(1)       NOT NULL default false,
    `upOnMongoRest`    bit(1)       NOT NULL default false,
    `created`          DATETIME              DEFAULT CURRENT_TIMESTAMP,
    `modified`         DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `articles_pageId_key` (`pageId`),
    CONSTRAINT `articles_pageId_fkey_constraint` FOREIGN KEY (`pageId`) REFERENCES `pages` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `image_links`
(
    `articleId` varchar(255) NOT NULL,
    `link`      text         NOT NULL,
    `caption`   text     DEFAULT NULL,
    `created`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `FKtarkqvk2kgymilolrr4g2x3ae` (`articleId`),
    CONSTRAINT `FKtarkqvk2kgymilolrr4g2x3ae` FOREIGN KEY (`articleId`) REFERENCES `articles` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

#id	pageId	pageNumber	creation_time	article_count
create table `general_log`
(
    `id`         int(11) NOT NULL auto_increment,
    `logMessage` text     DEFAULT NULL,
    `created`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) Engine = MyISAM
  DEFAULT CHARSET = utf8mb4;

create table article_upload_history
(
    `id`           int(11)      NOT NULL auto_increment,
    `uploadTarget` varchar(255) NOT NULL,
    `logMessage`   text     DEFAULT NULL,
    `created`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `article_upload_history_articleId_uploadTarget_unique_key` (`articleId`, `uploadTarget`),
    KEY `article_upload_history_articleId_key` (`articleId`),
    CONSTRAINT `article_upload_history_articleId_fk` FOREIGN KEY (`articleId`) REFERENCES `articles` (`id`),
    PRIMARY KEY (`id`)
) Engine = InnoDB
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

CREATE TABLE `news_server_data_coordinator`.`settings_update_log`
(
    `id`         INT(11)  NOT NULL AUTO_INCREMENT,
    `updateTime` datetime NOT NULL,
    `logMessage` text     NULL,
    PRIMARY KEY (`id`)
) Engine = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`settings_upload_log`
(
    `id`         INT(11)  NOT NULL AUTO_INCREMENT,
    `uploadTime` datetime NOT NULL,
    `uploadTarget` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) Engine = InnoDB
  DEFAULT CHARSET = utf8mb4;