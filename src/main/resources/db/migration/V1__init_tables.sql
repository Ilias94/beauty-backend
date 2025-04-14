CREATE TABLE `address` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `apartment_number` varchar(255) DEFAULT NULL,
                           `city` varchar(255) DEFAULT NULL,
                           `district` varchar(255) DEFAULT NULL,
                           `postal_code` varchar(255) DEFAULT NULL,
                           `street` varchar(255) DEFAULT NULL,
                           `street_number` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`id`)
);
CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `label` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UK_qa4k6p9mhd8aa4xc39uygxwut` (`label`)
);
CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `email` varchar(255) DEFAULT NULL,
                         `first_name` varchar(255) DEFAULT NULL,
                         `last_name` varchar(255) DEFAULT NULL,
                         `password` varchar(255) DEFAULT NULL,
                         `file_name` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `idx_email` (`email`)
);
CREATE TABLE `role` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `name` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`)
);
CREATE TABLE `user_role` (
                             `user_id` bigint NOT NULL,
                             `role_id` bigint NOT NULL,
                             KEY `FKa68196081fvovjhkek5m97n3y` (`role_id`),
                             KEY `abcd_idx` (`user_id`),
                             CONSTRAINT `abcd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                             CONSTRAINT `FKa68196081fvovjhkek5m97n3y` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
);
CREATE TABLE `course` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `description` varchar(255) DEFAULT NULL,
                          `end_date` datetime(6) DEFAULT NULL,
                          `max_participants` int NOT NULL,
                          `start_date` datetime(6) DEFAULT NULL,
                          `title` varchar(255) DEFAULT NULL,
                          `created_by_user_id` bigint DEFAULT NULL,
                          `address_id` bigint DEFAULT NULL,
                          `category_id` bigint DEFAULT NULL,
                          `rating` double DEFAULT NULL,
                          `price` decimal(38,2) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `UK_lhorsx1uj1sm6y4k4wuhl776y` (`address_id`),
                          KEY `FKkyes7515s3ypoovxrput029bh` (`category_id`),
                          KEY `FKk4nb7twiay2lliswx2ohe00kl` (`created_by_user_id`),
                          CONSTRAINT `FK7rlccxfog2pq067suash0njoc` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`),
                          CONSTRAINT `FKk4nb7twiay2lliswx2ohe00kl` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`),
                          CONSTRAINT `FKkyes7515s3ypoovxrput029bh` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
);
CREATE TABLE `comment` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `content` varchar(255) DEFAULT NULL,
                           `created_date` datetime(6) DEFAULT NULL,
                           `last_modified_date` datetime(6) DEFAULT NULL,
                           `course_id` bigint DEFAULT NULL,
                           `answer_id` bigint DEFAULT NULL,
                           `author_id` bigint DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_sr56od3koverm1nbbyiccj13o` (`answer_id`),
                           KEY `FKdsub2q6m6519rpas8b075fr7m` (`course_id`),
                           KEY `FKir20vhrx08eh4itgpbfxip0s1` (`author_id`),
                           CONSTRAINT `FKdsub2q6m6519rpas8b075fr7m` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
                           CONSTRAINT `FKir20vhrx08eh4itgpbfxip0s1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
                           CONSTRAINT `FKk2u1rd366m8wvf2c2wu1wc6ir` FOREIGN KEY (`answer_id`) REFERENCES `comment` (`id`)
);
CREATE TABLE `course_participants` (
                                       `user_id` bigint NOT NULL,
                                       `course_id` bigint NOT NULL,
                                       PRIMARY KEY (`user_id`,`course_id`),
                                       KEY `FK8dvnj6ef9lwhi2xo4orhev83i` (`course_id`),
                                       CONSTRAINT `FK4hkimtg6ss3iph7b0yt2g2pm3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                                       CONSTRAINT `FK8dvnj6ef9lwhi2xo4orhev83i` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
);
CREATE TABLE `rating` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `created_date` datetime(6) DEFAULT NULL,
                          `last_modified_date` datetime(6) DEFAULT NULL,
                          `value` float NOT NULL,
                          `author_id` bigint DEFAULT NULL,
                          `course_id` bigint DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          KEY `FKrbuqjo7wyi9w281uaeupnk26m` (`course_id`),
                          KEY `FKo3vn2o4r03bwc3krhl2x47es4` (`author_id`),
                          CONSTRAINT `FKo3vn2o4r03bwc3krhl2x47es4` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
                          CONSTRAINT `FKrbuqjo7wyi9w281uaeupnk26m` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
);
CREATE TABLE `revinfo` (
                           `rev` int NOT NULL AUTO_INCREMENT,
                           `revtstmp` bigint DEFAULT NULL,
                           PRIMARY KEY (`rev`)
);
CREATE TABLE `role_aud` (
                            `id` bigint NOT NULL,
                            `rev` int NOT NULL,
                            `revtype` tinyint DEFAULT NULL,
                            `name` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`rev`,`id`),
                            CONSTRAINT `FKrks7qtsmup3w81fdp0d6omfk7` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
);
CREATE TABLE `template` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `body` text NOT NULL,
                            `name` varchar(255) NOT NULL,
                            `subject` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `idx_template_name` (`name`)
);
CREATE TABLE `user_aud` (
                            `id` bigint NOT NULL,
                            `rev` int NOT NULL,
                            `revtype` tinyint DEFAULT NULL,
                            `email` varchar(255) DEFAULT NULL,
                            `first_name` varchar(255) DEFAULT NULL,
                            `last_name` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`rev`,`id`),
                            CONSTRAINT `FK89ntto9kobwahrwxbne2nqcnr` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
);
CREATE TABLE `user_role_aud` (
                                 `rev` int NOT NULL,
                                 `user_id` bigint NOT NULL,
                                 `role_id` bigint NOT NULL,
                                 `revtype` tinyint DEFAULT NULL,
                                 PRIMARY KEY (`rev`,`user_id`,`role_id`),
                                 CONSTRAINT `FK2ax4xks5sy1yh2a2gxdndkcmc` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
);
CREATE TABLE `users_aud` (
                             `id` bigint NOT NULL,
                             `rev` int NOT NULL,
                             `revtype` tinyint DEFAULT NULL,
                             `email` varchar(255) DEFAULT NULL,
                             `first_name` varchar(255) DEFAULT NULL,
                             `last_name` varchar(255) DEFAULT NULL,
                             `file_name` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`rev`,`id`),
                             CONSTRAINT `FKc4vk4tui2la36415jpgm9leoq` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
);