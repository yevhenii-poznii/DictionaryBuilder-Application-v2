create table user_vocabulary_application (
    id uuid not null primary key,
    email varchar(255) not null unique,
    username varchar(255) not null unique,
    password text not null,
    is_active boolean not null
);

create table user_profile (
    user_id uuid not null primary key,
    public_name varchar(50) not null,
    created_at timestamp(6) not null,

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application(id)
);

create table user_preference (
    user_id uuid not null primary key,
    profile_visibility varchar(20) not null check (profile_visibility IN ('PRIVATE', 'PUBLIC_FOR_FRIEND', 'PUBLIC')),
    right_answers_to_disable_in_repetition integer not null,

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application(id)
);

create table dictionary (
    id bigserial not null primary key,
    dictionary_name varchar(50) not null,
    user_profile_id uuid,

    constraint fk_user_profile_id foreign key (user_profile_id) references user_profile(user_id)
);

create table word (
    id bigserial not null primary key,
    word text not null,
    use_in_repetition boolean not null,
    counter_right_answers integer not null,
    added_at timestamp(6) not null,
    edited_at timestamp(6),
    dictionary_id bigint,

    constraint fk_dictionary_id foreign key (dictionary_id) references dictionary(id)
);

create table word_translation (
    id bigserial not null primary key,
    translation text not null,
    word_id bigint,

    constraint fk_word_id foreign key (word_id) references word
);

create table word_hint (
    id bigserial not null,
    hint varchar(255) not null,
    word_id bigint unique,

    constraint fk_word_id foreign key (word_id) references word(id)
);

create table verification_token (
    id bigserial not null primary key,
    verification_token varchar(255) not null,
    user_id uuid not null,
    timestamp timestamp(6) not null
);
