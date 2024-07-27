create table word_addition_goal_report
(
    id      bigserial not null primary key,
    user_id uuid      not null unique,

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id) on delete cascade
);

create table word_addition_goal_report_row
(
    id            bigserial   not null primary key,
    start_period  date,
    end_period    date        not null,
    working_days  integer     not null,
    report_period varchar(50) not null check (report_period IN ('day', 'week', 'month', 'year', 'total')),

    report_id     bigint,

    constraint fk_word_addition_goal_report_id foreign key (report_id) references word_addition_goal_report (id) on delete cascade
);

create table dictionary_word_addition_goal_report
(
    id                         bigserial        not null primary key,
    dictionary_id              bigint           not null,
    goal_completion_percentage double precision not null,
    new_words_goal             integer          not null,
    new_words_actual           integer          not null,

    report_row_id              bigint,

    constraint fk_word_addition_goal_report_row_id foreign key (report_row_id) references word_addition_goal_report_row (id) on delete cascade
);

create index idx_word_addition_goal_report_user_id on word_addition_goal_report (user_id);
create index idx_word_addition_goal_report_row_report_id on word_addition_goal_report_row (report_id);
create index idx_dictionary_word_addition_goal_report_report_row_id on dictionary_word_addition_goal_report (report_row_id);

create table repetition_time_spend_goal_report
(
    id      bigserial not null primary key,
    user_id uuid      not null unique,

    constraint fk_user_id foreign key (user_id) references user_vocabulary_application (id) on delete cascade
);

create table repetition_time_spend_goal_report_row
(
    id            bigserial   not null primary key,
    start_period  date,
    end_period    date        not null,
    working_days  integer     not null,
    report_period varchar(50) not null check (report_period IN ('day', 'week', 'month', 'year', 'total')),

    report_id     bigint,

    constraint fk_repetition_time_spend_goal_report_id foreign key (report_id) references repetition_time_spend_goal_report (id) on delete cascade
);

create table dictionary_repetition_time_spend_goal_report
(
    id                         bigserial        not null primary key,
    dictionary_id              bigint           not null,
    goal_completion_percentage double precision not null,
    repetition_time_goal       varchar(255)     not null,
    time_spent_duration        varchar(255)     not null,
    completed_repetitions      integer          not null,

    report_row_id              bigint,

    constraint fk_repetition_time_spend_goal_report_row_id foreign key (report_row_id) references repetition_time_spend_goal_report_row (id) on delete cascade
);

create index idx_repetition_time_spend_goal_report_user_id on repetition_time_spend_goal_report (user_id);
create index idx_repetition_time_spend_goal_report_row_report_id on repetition_time_spend_goal_report_row (report_id);
create index idx_dictionary_repetition_time_spend_goal_report_report_row_id on dictionary_repetition_time_spend_goal_report (report_row_id);
