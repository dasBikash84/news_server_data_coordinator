DROP DATABASE if exists `news_server_data_coordinator`;

CREATE DATABASE `news_server_data_coordinator`;

CREATE TABLE `news_server_data_coordinator`.`countries`
(
    `name`        varchar(255) NOT NULL,
    `countryCode` varchar(255) NOT NULL,
    `timeZone`    varchar(255) NOT NULL,
    `updated`     bit(1)       NOT NULL DEFAULT b'1',
    `created`     datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`    datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`languages`
(
    `id`       varchar(255) NOT NULL,
    `name`     varchar(255) NOT NULL,
    `updated`  bit(1)       NOT NULL DEFAULT b'1',
    `created`  datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `language_name_unique_key` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`newspapers`
(
    `id`          varchar(255) NOT NULL,
    `name`        varchar(255) NOT NULL,
    `countryName` varchar(255) NOT NULL,
    `languageId`  varchar(255) NOT NULL,
    `active`      bit(1)       NOT NULL DEFAULT b'1',
    `updated`     bit(1)       NOT NULL DEFAULT b'1',
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
CREATE TABLE `news_server_data_coordinator`.`pages`
(
    `id`           varchar(255) NOT NULL,
    `name`         varchar(255) NOT NULL,
    `newsPaperId`  varchar(255) NOT NULL,
    `parentPageId` varchar(255) NOT NULL,
    `hasData`      bit(1)       NOT NULL,
    `hasChild`     bit(1)       NOT NULL,
    `topLevelPage` bit(1)       NOT NULL,
    `active`       bit(1)       NOT NULL DEFAULT b'1',
    `updated`      bit(1)       NOT NULL DEFAULT b'1',
    `created`      datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `pages_name_newsPaperId_parentPageId_unique_key` (`newsPaperId`, `name`, `parentPageId`),
    KEY `pages_newsPaperId_key` (`newsPaperId`),
    CONSTRAINT `pages_newsPaperId_fkey_constraint` FOREIGN KEY (`newsPaperId`) REFERENCES `newspapers` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`page_groups`
(
    `id`      int(11)      NOT NULL AUTO_INCREMENT,
    `name`    varchar(255) NOT NULL,
    `active`  bit(1)   DEFAULT b'1',
    `created` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`page_group_entries`
(
    `id`          int(11)      NOT NULL AUTO_INCREMENT,
    `pageGroupId` int(11)      NOT NULL,
    `pageId`      varchar(255) NOT NULL,
    `created`     datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `page_group_entries_pageGroupId_pageId_unique_key` (`pageGroupId`, `pageId`),
    KEY `page_group_entries_pageId_fk` (`pageId`),
    CONSTRAINT `page_group_entries_pageGroupId_fk` FOREIGN KEY (`pageGroupId`) REFERENCES `page_groups` (`id`),
    CONSTRAINT `page_group_entries_pageId_fk` FOREIGN KEY (`pageId`) REFERENCES `pages` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`articles`
(
    `id`                               varchar(255) NOT NULL,
    `pageId`                           varchar(255) NOT NULL,
    `title`                            varchar(255) NOT NULL,
    `articleText`                      text         NOT NULL,
    `previewImageLink`                 text,
    `publicationTime`                  datetime     NOT NULL,
    `processedForSearchResult` bit(1) NOT NULL DEFAULT b'0',
    `deletedProcessedForSearchResult` bit(1) NOT NULL DEFAULT b'0',
    `upOnFirebaseDb`                   bit(1)       NOT NULL DEFAULT b'0',
    `upOnFireStore`                    bit(1)       NOT NULL DEFAULT b'0',
    `upOnMongoRest`                    bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromFirebaseDb`            bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromFireStore`             bit(1)       NOT NULL DEFAULT b'0',
    `deletedFromMongoRest`             bit(1)       NOT NULL DEFAULT b'0',
    `processedInNewFormatForFirestore` BIT(1)       NOT NULL DEFAULT b'1',
    `created`                          datetime              DEFAULT CURRENT_TIMESTAMP,
    `modified`                         datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `articles_pageId_key` (`pageId`),
    CONSTRAINT `articles_pageId_fkey_constraint` FOREIGN KEY (`pageId`) REFERENCES `pages` (`id`),
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
    KEY `articles_deletedProcessedForSearchResult_index` (`deletedProcessedForSearchResult`),
    KEY `articles_processedInNewFormatForFirestore_index` (`processedInNewFormatForFirestore`),
    KEY `articles_pageId_deletedFromFirebaseDb_key` (`pageId`,`deletedFromFirebaseDb`),
    KEY `articles_pageId_deletedFromFireStore_key` (`pageId`,`deletedFromFireStore`),
    KEY `articles_pageId_deletedFromMongoRest_key` (`pageId`,`deletedFromMongoRest`),
    KEY `articles_pageId_upOnFirebaseDb_deletedFromFirebaseDb_Index` (`pageId`,`upOnFirebaseDb`,`deletedFromFirebaseDb`,`publicationTime`),
    KEY `articles_pageId_upOnMongoRest_deletedFromMongoRest_Index` (`pageId`,`upOnMongoRest`,`deletedFromMongoRest`,`publicationTime`),
    KEY `articles_pageId_upOnFireStore_deletedFromFireStore_Index` (`pageId`,`upOnFireStore`,`deletedFromFireStore`,`publicationTime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`image_links`
(
    `articleId` varchar(255) NOT NULL,
    `link`      text         NOT NULL,
    `caption`   text,
    `created`   datetime DEFAULT CURRENT_TIMESTAMP,
    KEY `FKtarkqvk2kgymilolrr4g2x3ae` (`articleId`),
    CONSTRAINT `FKtarkqvk2kgymilolrr4g2x3ae` FOREIGN KEY (`articleId`) REFERENCES `articles` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`article_download_log`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `parents`    varchar(255) DEFAULT NULL,
    `articleCount` int(11) DEFAULT '0',
    `logMessage` text,
    `created`    datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`article_uploader_status_change_log`
(
    `id`                        int(11)                                                    NOT NULL AUTO_INCREMENT,
    `status`                    enum ('ON','OFF')                                          NOT NULL,
    `articleDataUploaderTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    `created`                   datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`article_upload_log`
(
    `id`           int(11)                                                    NOT NULL AUTO_INCREMENT,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    `articleCount` int(11) DEFAULT '0',
    `logMessage`   text,
    `created`      datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`exception_log`
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
CREATE TABLE `news_server_data_coordinator`.`general_log`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `logMessage` text,
    `created`    datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`settings_update_log`
(
    `id`         int(11)  NOT NULL AUTO_INCREMENT,
    `updateTime` datetime NOT NULL,
    `logMessage` text,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `news_server_data_coordinator`.`settings_upload_log`
(
    `id`           int(11)                                                    NOT NULL AUTO_INCREMENT,
    `uploadTime`   datetime                                                   NOT NULL,
    `uploadTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`tokens`
(
    `token`     varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `expiresOn` datetime                                NOT NULL,
    `created`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`token`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `news_server_data_coordinator`.`rest_activity_log`
(
    `id`                  int(11)      NOT NULL AUTO_INCREMENT,
    `requestURL`          varchar(255) NOT NULL,
    `requestMethod`       varchar(255) NOT NULL,
    `remoteHost`          varchar(255) NOT NULL,
    `methodSignature`     varchar(255) NOT NULL,
    `exceptionClassName`  varchar(255) DEFAULT NULL,
    `timeTakenMs`         int(5)       NOT NULL,
    `returnedEntiryCount` int(3)       DEFAULT 0,
    `acceptHeader`        VARCHAR(45)  DEFAULT NULL,
    `userAgentHeader`     VARCHAR(255) DEFAULT NULL,
    `created`             datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8;

CREATE TABLE `news_server_data_coordinator`.`article_delete_request`
(
    `id`                    int(11)                                                    NOT NULL AUTO_INCREMENT,
    `pageId`                varchar(255)                                               NOT NULL,
    `deleteRequestCount`    int(3)                                                     NOT NULL,
    `articleUploaderTarget` enum ('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE') NOT NULL,
    `served`                bit(1)                                                     NOT NULL DEFAULT b'0',
    `created`               datetime                                                            DEFAULT CURRENT_TIMESTAMP,
    `modified`              datetime                                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `article_delete_request_pageId_fkey_constraint` FOREIGN KEY (`pageId`) REFERENCES `pages` (`id`),
    PRIMARY KEY (`id`)
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

CREATE TABLE `news_server_data_coordinator`.`article_search_result_uploader_log`
(
    `id`         INT(11)       NOT NULL AUTO_INCREMENT,
    `logMessage` VARCHAR(1500) NOT NULL,
    `created`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`news_categories`
(
    `id`       VARCHAR(50)  NOT NULL,
    `name`     VARCHAR(255) NOT NULL,
    `created`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `name_UNIQUE` (`name` ASC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`news_category_entry`
(
    `id`             INT          NOT NULL AUTO_INCREMENT,
    `newsCategoryId` VARCHAR(50)  NOT NULL,
    `pageId`         VARCHAR(255) NOT NULL,
    `updated`        bit(1)       NOT NULL DEFAULT b'1',
    `created`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `newsCategoryId_fk_idx` (`newsCategoryId` ASC),
    INDEX `fk_news_category_entry_pageId_idx` (`pageId` ASC),
    UNIQUE INDEX `uk_newsCategoryId_pageId` (`newsCategoryId` ASC, `pageId` ASC),
    CONSTRAINT `fk_news_category_entry_newsCategoryId`
        FOREIGN KEY (`newsCategoryId`)
            REFERENCES `news_server_parser2`.`news_categories` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_news_category_entry_pageId`
        FOREIGN KEY (`pageId`)
            REFERENCES `news_server_parser2`.`pages` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`firebase_user`
(
    `uid`                    VARCHAR(50)  NOT NULL,
    `providerId`             VARCHAR(255) NOT NULL,
    `disabled`               BIT(1)       NOT NULL DEFAULT b'0',
    `displayName`            VARCHAR(255) NULL,
    `emailId`                VARCHAR(255) NULL,
    `phoneNumber`            VARCHAR(45)  NULL,
    `emailVerified`          BIT(1)       NOT NULL DEFAULT b'0',
    `photoUrl`               VARCHAR(512) NULL,
    `lastSettingsUpdateTime` DATETIME     NULL,
    `created`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`firebase_user_info_synchronizer_log`
(
    `id`              INT(11)    NOT NULL AUTO_INCREMENT,
    `syncedUserCount` INT(11)    NOT NULL DEFAULT 0,
    `newUserCount`    INT(11)    NOT NULL DEFAULT 0,
    `message`         MEDIUMTEXT NOT NULL,
    `created`         DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`fav_page_entry_on_user_settings`
(
    `id`             INT(11)      NOT NULL AUTO_INCREMENT,
    `pageId`         VARCHAR(255) NOT NULL,
    `firebaseUserId` VARCHAR(50)  NOT NULL,
    `subscribed`     BIT(1)       NOT NULL DEFAULT b'0',
    `created`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `fav_page_entry_on_user_settings_fk_pageId_idx` (`pageId` ASC),
    INDEX `fk_fav_page_entry_on_user_settings_firebaseUserId_idx` (`firebaseUserId` ASC),
    CONSTRAINT `fk_fav_page_entry_on_user_settings_pageId`
        FOREIGN KEY (`pageId`)
            REFERENCES `news_server_data_coordinator`.`pages` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_fav_page_entry_on_user_settings_firebaseUserId`
        FOREIGN KEY (`firebaseUserId`)
            REFERENCES `news_server_data_coordinator`.`firebase_user` (`uid`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `news_server_data_coordinator`.`article_notification_generation_log`
(
    `id`           INT(11)      NOT NULL AUTO_INCREMENT,
    `articleId`    VARCHAR(255) NOT NULL,
    `parentPageId` VARCHAR(255) NOT NULL,
    `created`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `fk_article_notification_generation_log_articleId_idx` (`articleId` ASC),
    INDEX `fk_article_notification_generation_log_pageId_idx` (`parentPageId` ASC),
    CONSTRAINT `fk_article_notification_generation_log_articleId`
        FOREIGN KEY (`articleId`)
            REFERENCES `news_server_data_coordinator`.`articles` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_article_notification_generation_log_pageId`
        FOREIGN KEY (`parentPageId`)
            REFERENCES `news_server_data_coordinator`.`pages` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

CREATE INDEX `articles_upOnFirebaseDb_index` ON articles (upOnFirebaseDb);
CREATE INDEX `articles_upOnFireStore_index` ON articles (upOnFireStore);
CREATE INDEX `articles_upOnMongoRest_index` ON articles (upOnMongoRest);

CREATE INDEX `articles_created_index` ON articles (created);
CREATE INDEX `articles_publicationTime_index` ON articles (publicationTime);

drop user if exists 'nsdc_app_user'@'localhost';
drop user if exists 'nsdc_rest_user'@'localhost';

create user 'nsdc_app_user'@'localhost' identified by 'nsdc_app_user';
create user 'nsdc_rest_user'@'localhost' identified by 'nsdc_rest_user';

grant select, insert, update, delete on news_server_data_coordinator.* to 'nsdc_app_user'@'localhost';

grant select on news_server_data_coordinator.* to 'nsdc_rest_user'@'localhost';
grant insert, update on news_server_data_coordinator.tokens to 'nsdc_rest_user'@'localhost';
grant insert on news_server_data_coordinator.article_uploader_status_change_log to 'nsdc_rest_user'@'localhost';
grant insert on news_server_data_coordinator.rest_activity_log to 'nsdc_rest_user'@'localhost';
grant insert on news_server_data_coordinator.article_delete_request to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.general_log to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.exception_log to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.article_upload_log to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.article_download_log to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.settings_update_log to 'nsdc_rest_user'@'localhost';
grant delete on news_server_data_coordinator.settings_upload_log to 'nsdc_rest_user'@'localhost';