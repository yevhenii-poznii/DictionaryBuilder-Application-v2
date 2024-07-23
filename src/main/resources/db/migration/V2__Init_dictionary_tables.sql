create table dictionary
(
    id              bigserial   not null primary key,
    dictionary_name varchar(50) not null,
    user_profile_id uuid,

    unique (dictionary_name, user_profile_id),
    constraint fk_user_profile_id foreign key (user_profile_id) references user_profile (user_id) on delete cascade
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
