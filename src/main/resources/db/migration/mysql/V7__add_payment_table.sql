CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    user_id BIGINT,
    course_id BIGINT,
    status VARCHAR(50),

    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_payment_course
        FOREIGN KEY (course_id) REFERENCES course(id)
);
