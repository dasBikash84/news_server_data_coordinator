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

# Table, Create Table articles
CREATE TABLE `articles`
(
    `id`                       varchar(255) NOT NULL,
    `pageId`                   varchar(255) NOT NULL,
    `title`                    varchar(255) NOT NULL,
    `articleText`              text         NOT NULL,
    `previewImageLink`         text,
    `publicationTime`          datetime     NOT NULL,
    `processedForSearchResult` bit(1)       NOT NULL DEFAULT b'0',
    `upOnFirebaseDb`           bit(1)       NOT NULL DEFAULT b'0',
    `upOnFireStore`            bit(1)       NOT NULL DEFAULT b'0',
    `upOnMongoRest`            bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromFirebaseDb`    bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromFireStore`     bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromMongoRest`     bit(1)       NOT NULL DEFAULT b'0',
    `created`                  datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`                 datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `articles_pageId_key` (`pageId`),
    KEY `articles_pubtime_index` (`publicationTime`),
    KEY `articles_created_index` (`created`),
    KEY `articles_modified_index` (`modified`),
    KEY `articles_title_index` (`title`),
    KEY `articles_upOnFirebaseDb_index` (`upOnFirebaseDb`),
    KEY `articles_upOnFireStore_index` (`upOnFireStore`),
    KEY `articles_upOnMongoRest_index` (`upOnMongoRest`),
    KEY `articles_deletedFromFirebaseDb_index` (`deletedFromFirebaseDb`),
    KEY `articles_deletedFromFireStore_index` (`deletedFromFireStore`),
    KEY `articles_deletedFromMongoRest_index` (`deletedFromMongoRest`),
    KEY `articles_processedForSearchResult_index` (`processedForSearchResult`),
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
    `id`           int(11)                                                    NOT NULL auto_increment,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
#     `uploadTarget` varchar(255) NOT NULL,
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
    `id`           INT(11)                                                    NOT NULL AUTO_INCREMENT,
    `uploadTime`   datetime                                                   NOT NULL,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
#     `uploadTarget` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) Engine = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`article_uploader_status_change_log`
(
    `id`                        INT(11)                                                    NOT NULL AUTO_INCREMENT,
    `status`                    enum ('ON','OFF')                                          NOT NULL,
#     `status` varchar(255) NOT NULL,
    `articleDataUploaderTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
#     `articleDataUploaderTarget` varchar(255) NOT NULL,
    `created`                   datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) Engine = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`article_download_log`
(
    `id`         INT(11) NOT NULL AUTO_INCREMENT,
    `parents`    varchar(255) DEFAULT NULL,
    `logMessage` text    NULL,
    `created`    datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) Engine = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`tokens`
(
    `token`     varchar(255) NOT NULL,
    `expiresOn` datetime     NOT NULL,
    `created`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`token`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`daily_deletion_task_log`
(
    `id`                 INT(6)                                                       NOT NULL AUTO_INCREMENT,
    `uploadTarget`       ENUM ('REAL_TIME_DB', 'FIRE_STORE_DB', 'MONGO_REST_SERVICE') NOT NULL,
    `deletionLogMessage` MEDIUMTEXT                                                   NOT NULL,
    `created`            DATETIME                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `daily_deletion_task_log_created_index` (`created` ASC),
    INDEX `daily_deletion_task_log_uploadTarget_index` (`uploadTarget` ASC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`restricted_search_key_word`
(
    `keyWord` varchar(255) NOT NULL,
    `created` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`keyword`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# Table, Create Table key_word_serach_result,
CREATE TABLE `news_server_data_coordinator`.`key_word_serach_result`
(
    `keyWord`                        varchar(255) NOT NULL,
    `searchResult`                   mediumtext   NOT NULL,
    `modified`                       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `lastUploadedOnFireBaseDb`       datetime              DEFAULT NULL,
    `lastUploadedOnFireStore`        datetime              DEFAULT NULL,
    `lastUploadedOnMongoRestService` datetime              DEFAULT NULL,
    PRIMARY KEY (`keyWord`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


