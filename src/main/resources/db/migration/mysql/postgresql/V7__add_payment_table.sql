CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    order_id uuid NOT NULL,
    user_id bigint,
    course_id bigint,
    status varchar(50),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_course FOREIGN KEY (course_id) REFERENCES course(id)
);
