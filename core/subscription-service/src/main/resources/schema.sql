CREATE TABLE IF NOT EXISTS subscribers
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT,
    subscriber BIGINT
);

CREATE TABLE IF NOT EXISTS black_list
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    block_user BIGINT
);