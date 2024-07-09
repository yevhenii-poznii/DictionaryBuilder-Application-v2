create table token
(
    id             bigserial      not null primary key,
    token          text           not null,
    is_invalidated boolean        not null,
    user_id        uuid           not null,
    created_at     timestamptz(6) not null,
    expires_at     timestamptz(6),
    token_type     varchar(20)    not null
);
