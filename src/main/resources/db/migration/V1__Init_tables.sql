create table user_vocabulary_application
(
    id        uuid         not null primary key,
    email     varchar(255) not null unique,
    username  varchar(255) not null unique,
    password  text,
    is_active boolean      not null,
    role      varchar(50)  not null check (role IN ('ROLE_USER', 'ROLE_METRICS', 'ROLE_ADMIN'))
);

create table user_profile
(
    user_id         uuid         not null primary key,
    public_username varchar(255) not null,
    public_name     varchar(255),
    profile_picture text         not null,
    created_at      timestamp(6) not null,

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id)
);

create table user_preference
(
    user_id                                uuid        not null primary key,
    profile_visibility                     varchar(20) not null check (profile_visibility IN ('PRIVATE', 'PUBLIC_FOR_FRIEND', 'PUBLIC')),
    right_answers_to_disable_in_repetition integer     not null,
    words_per_page                         integer     not null,
    blur_translation                       boolean     not null,
    page_filter                            varchar(50) not null check (page_filter IN
                                                                       ('BY_ADDED_AT_ASC', 'BY_ADDED_AT_DESC',
                                                                        'ONLY_USE_IN_REPETITION_ASC',
                                                                        'ONLY_USE_IN_REPETITION_DESC',
                                                                        'ONLY_NOT_USE_IN_REPETITION_ASC',
                                                                        'ONLY_NOT_USE_IN_REPETITION_DESC')),

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id)
);

create table dictionary
(
    id              bigserial   not null primary key,
    dictionary_name varchar(50) not null,
    user_profile_id uuid,

    unique (dictionary_name, user_profile_id),
    constraint fk_user_profile_id foreign key (user_profile_id) references user_profile (user_id)
);

create table word
(
    id                    bigserial    not null primary key,
    word                  text         not null,
    use_in_repetition     boolean      not null,
    counter_right_answers integer      not null,
    added_at              timestamp(6) not null,
    edited_at             timestamp(6),
    dictionary_id         bigint,
    word_hint             varchar(255),

    constraint fk_dictionary_id foreign key (dictionary_id) references dictionary (id) on delete cascade
);

create table word_translation
(
    id          bigserial not null primary key,
    translation text      not null,
    word_id     bigint,

    constraint fk_word_id foreign key (word_id) references word on delete cascade
);

create table token
(
    id             bigserial   not null primary key,
    token          text        not null,
    is_invalidated boolean     not null,
    user_id        uuid        not null,
    created_at     timestamptz(6) not null,
    expires_at     timestamptz(6),
    token_type     varchar(20) not null
);
