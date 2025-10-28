CREATE TABLE `video` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `title` varchar(255) NOT NULL,
                            `path` varchar(255) NOT NULL,
                            `description` varchar(255) NOT NULL,
                            `course_id` bigint NOT NULL,
                            PRIMARY KEY (`id`)
);