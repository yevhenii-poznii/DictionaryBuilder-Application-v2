alter table user_profile
    add constraint uq_public_username unique (public_username);

create index idx_public_username on user_profile (public_username);
