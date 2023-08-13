create table IF NOT EXISTS GENRES
(
    GENRE_ID INTEGER auto_increment,
    NAME     CHARACTER VARYING,
    constraint GENRE_PK
        primary key (GENRE_ID)
);

create table IF NOT EXISTS MPA
(
    MPA_ID INTEGER auto_increment,
    NAME   CHARACTER VARYING,
    constraint "MPA_pk"
        primary key (MPA_ID)
);

create table IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER auto_increment,
    NAME         CHARACTER VARYING not null,
    RELEASE_DATE DATE,
    DURATION     INTEGER           not null,
    DESCRIPTION  CHARACTER VARYING not null,
    MPA_ID       INTEGER,
    constraint FILM_PK
        primary key (FILM_ID),
    constraint FILM_MPA_MPA_ID_FK
        foreign key (MPA_ID) references MPA
            on update cascade on delete cascade
);

create table IF NOT EXISTS FILM_GENRE_CONNECTION
(
    FILM_GENRE_CONNECTION_ID INTEGER auto_increment,
    FILM_ID                  INTEGER,
    GENRE_ID                 INTEGER,
    constraint "FILM_GENRE_CONNECTION_pk"
        primary key (FILM_GENRE_CONNECTION_ID),
    constraint "FILM_GENRE_CONNECTION_FILMS_null_fk"
        foreign key (FILM_ID) references FILMS
            on update cascade on delete cascade,
    constraint "FILM_GENRE_CONNECTION_GENRES_null_fk"
        foreign key (GENRE_ID) references GENRES
            on update cascade on delete cascade
);

create table IF NOT EXISTS USERS
(
    USER_ID  INTEGER auto_increment,
    EMAIL    CHARACTER VARYING not null,
    LOGIN    CHARACTER VARYING not null,
    NAME     CHARACTER VARYING,
    BIRTHDAY DATE,
    constraint USERS_PK
        primary key (USER_ID)
);

create table IF NOT EXISTS FRIEND_REQUEST
(
    FRIEND_REQUEST_ID INTEGER auto_increment,
    USER_ID           INTEGER not null,
    FRIEND_ID         INTEGER,
    constraint FRIENDS_PK
        primary key (FRIEND_REQUEST_ID),
    constraint FRIENDS_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
            on update cascade on delete cascade,
    constraint FRIENDS_USERS_USER_ID_FK_2
        foreign key (FRIEND_ID) references USERS
            on update cascade on delete cascade
);

create table IF NOT EXISTS LIKES
(
    LIKE_ID INTEGER auto_increment,
    FILM_ID INTEGER,
    USER_ID INTEGER,
    constraint PEOPLE_LIKED_PK
        primary key (LIKE_ID),
    constraint PEOPLE_LIKED_FILM_FILM_ID_FK
        foreign key (FILM_ID) references FILMS
            on update cascade on delete cascade,
    constraint PEOPLE_LIKED_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
            on update cascade on delete cascade
);