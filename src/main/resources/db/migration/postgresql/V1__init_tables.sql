-- ADDRESS
CREATE TABLE beautypg.address (
    id BIGSERIAL PRIMARY KEY,
    apartment_number varchar(255),
    city varchar(255),
    district varchar(255),
    postal_code varchar(255),
    street varchar(255),
    street_number varchar(255)
);

-- CATEGORY
CREATE TABLE beautypg.category (
    id BIGSERIAL PRIMARY KEY,
    label varchar(255) NOT NULL,
    CONSTRAINT uk_category_label UNIQUE (label)
);

-- USERS
CREATE TABLE beautypg.users (
    id BIGSERIAL PRIMARY KEY,
    email varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    password varchar(255),
    file_name varchar(255),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ROLE
CREATE TABLE beautypg.role (
    id BIGSERIAL PRIMARY KEY,
    name varchar(255) NOT NULL
);

-- USER_ROLE (JOIN TABLE)
CREATE TABLE beautypg.user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES beautypg.users(id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES beautypg.role(id)
);

-- COURSE
CREATE TABLE beautypg.course (
    id BIGSERIAL PRIMARY KEY,
    description varchar(255),
    end_date timestamp(6),
    max_participants int NOT NULL,
    start_date timestamp(6),
    title varchar(255),
    created_by_user_id bigint,
    address_id bigint UNIQUE,
    category_id bigint,
    rating double precision,
    price decimal(38,2),
    CONSTRAINT fk_course_address
        FOREIGN KEY (address_id) REFERENCES beautypg.address(id),
    CONSTRAINT fk_course_user
        FOREIGN KEY (created_by_user_id) REFERENCES beautypg.users(id),
    CONSTRAINT fk_course_category
        FOREIGN KEY (category_id) REFERENCES beautypg.category(id)
);

-- COMMENT
CREATE TABLE beautypg.comment (
    id BIGSERIAL PRIMARY KEY,
    content varchar(255),
    created_date timestamp(6),
    last_modified_date timestamp(6),
    course_id bigint,
    answer_id bigint UNIQUE,
    author_id bigint,
    CONSTRAINT fk_comment_course
        FOREIGN KEY (course_id) REFERENCES beautypg.course(id),
    CONSTRAINT fk_comment_author
        FOREIGN KEY (author_id) REFERENCES beautypg.users(id),
    CONSTRAINT fk_comment_answer
        FOREIGN KEY (answer_id) REFERENCES beautypg.comment(id)
);

-- COURSE_PARTICIPANTS
CREATE TABLE beautypg.course_participants (
    user_id bigint NOT NULL,
    course_id bigint NOT NULL,
    PRIMARY KEY (user_id, course_id),
    CONSTRAINT fk_cp_user
        FOREIGN KEY (user_id) REFERENCES beautypg.users(id),
    CONSTRAINT fk_cp_course
        FOREIGN KEY (course_id) REFERENCES beautypg.course(id)
);

-- RATING
CREATE TABLE beautypg.rating (
    id BIGSERIAL PRIMARY KEY,
    created_date timestamp(6),
    last_modified_date timestamp(6),
    value float NOT NULL,
    author_id bigint,
    course_id bigint,
    CONSTRAINT fk_rating_user
        FOREIGN KEY (author_id) REFERENCES beautypg.users(id),
    CONSTRAINT fk_rating_course
        FOREIGN KEY (course_id) REFERENCES beautypg.course(id)
);

-- REVINFO (Envers)
CREATE TABLE beautypg.revinfo (
    rev SERIAL PRIMARY KEY,
    revtstmp bigint
);

-- ROLE_AUD
CREATE TABLE beautypg.role_aud (
    id bigint NOT NULL,
    rev int NOT NULL,
    revtype smallint,
    name varchar(255),
    PRIMARY KEY (rev, id),
    CONSTRAINT fk_role_aud_rev
        FOREIGN KEY (rev) REFERENCES beautypg.revinfo(rev)
);

-- TEMPLATE
CREATE TABLE beautypg.template (
    id BIGSERIAL PRIMARY KEY,
    body text NOT NULL,
    name varchar(255) NOT NULL,
    subject varchar(255) NOT NULL,
    CONSTRAINT uk_template_name UNIQUE (name)
);

-- USER_AUD
CREATE TABLE beautypg.user_aud (
    id bigint NOT NULL,
    rev int NOT NULL,
    revtype smallint,
    email varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    PRIMARY KEY (rev, id),
    CONSTRAINT fk_user_aud_rev
        FOREIGN KEY (rev) REFERENCES beautypg.revinfo(rev)
);

-- USER_ROLE_AUD
CREATE TABLE beautypg.user_role_aud (
    rev int NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    revtype smallint,
    PRIMARY KEY (rev, user_id, role_id),
    CONSTRAINT fk_user_role_aud_rev
        FOREIGN KEY (rev) REFERENCES beautypg.revinfo(rev)
);

-- USERS_AUD
CREATE TABLE beautypg.users_aud (
    id bigint NOT NULL,
    rev int NOT NULL,
    revtype smallint,
    email varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    file_name varchar(255),
    PRIMARY KEY (rev, id),
    CONSTRAINT fk_users_aud_rev
        FOREIGN KEY (rev) REFERENCES beautypg.revinfo(rev)
);
