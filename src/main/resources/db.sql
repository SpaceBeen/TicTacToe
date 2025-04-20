<<<<<<< HEAD
drop schema tictactoe cascade;

create schema if not exists tictactoe;

CREATE TABLE if not exists tictactoe.game_field
(
    id    int GENERATED ALWAYS AS IDENTITY,
    field text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT game_field_pkey PRIMARY KEY (id)
);

create TABLE IF NOT EXISTS tictactoe.current_game
(
    id            uuid NOT NULL,
    game_field_id bigint,
    state         int,
=======
CREATE TABLE IF NOT EXISTS tictactoe.current_game
(
    id text COLLATE pg_catalog."default" NOT NULL,
    game_field_id bigint,
>>>>>>> fb4ba35bd9e01822bc84a148f4a2063fced6048e
    CONSTRAINT current_game_pkey PRIMARY KEY (id),
    CONSTRAINT fk_game_field FOREIGN KEY (game_field_id)
        REFERENCES tictactoe.game_field (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tictactoe.users
(
<<<<<<< HEAD
    id              uuid                              NOT NULL primary key,
    login           text COLLATE pg_catalog."default" NOT NULL unique,
    password        text COLLATE pg_catalog."default" NOT NULL,
    rating          int default 0 check (rating >= 0),
    current_game_id uuid references tictactoe.current_game (id)
);

TRUNCATE TABLE tictactoe.game_field cascade;
TRUNCATE TABLE tictactoe.current_game cascade;
TRUNCATE TABLE tictactoe.users cascade;

alter table tictactoe.current_game
    alter column state TYPE varchar,
    add column player_id uuid references tictactoe.users (id);

alter table tictactoe.current_game
    drop column player_id cascade;

-- Обновляем таблицу current_game
ALTER TABLE tictactoe.current_game
    DROP COLUMN IF EXISTS player_id,
    ADD COLUMN IF NOT EXISTS X_player UUID REFERENCES tictactoe.users (id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS O_player UUID REFERENCES tictactoe.users (id) ON DELETE SET NULL;

alter table tictactoe.current_game
add column player_id uuid;
=======
    id uuid NOT NULL,
    login text COLLATE pg_catalog."default" NOT NULL,
    password text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_login_key UNIQUE (login)
);

CREATE TABLE if not exists tictactoe.game_field
(
    id bigint GENERATED ALWAYS AS IDENTITY,
    field text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT game_field_pkey PRIMARY KEY (id)
);
>>>>>>> fb4ba35bd9e01822bc84a148f4a2063fced6048e
