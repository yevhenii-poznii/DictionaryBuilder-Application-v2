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

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id) on delete cascade
);

create table user_preference
(
    user_id                                uuid         not null primary key,
    profile_visibility                     varchar(20)  not null check (profile_visibility IN ('PRIVATE', 'PUBLIC_FOR_FRIEND', 'PUBLIC')),
    right_answers_to_disable_in_repetition integer      not null,
    words_per_page                         integer      not null,
    blur_translation                       boolean      not null,
    new_words_per_day_goal                 integer      not null,
    daily_repetition_duration_goal         varchar(255) not null,
    page_filter                            varchar(50)  not null check (page_filter IN
                                                                        ('BY_ADDED_AT_ASC', 'BY_ADDED_AT_DESC',
                                                                         'ONLY_USE_IN_REPETITION_ASC',
                                                                         'ONLY_USE_IN_REPETITION_DESC',
                                                                         'ONLY_NOT_USE_IN_REPETITION_ASC',
                                                                         'ONLY_NOT_USE_IN_REPETITION_DESC')),

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id) on delete cascade
);
