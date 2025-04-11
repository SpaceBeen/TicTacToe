CREATE TABLE IF NOT EXISTS tictactoe.current_game
(
    id text COLLATE pg_catalog."default" NOT NULL,
    game_field_id bigint,
    CONSTRAINT current_game_pkey PRIMARY KEY (id),
    CONSTRAINT fk_game_field FOREIGN KEY (game_field_id)
        REFERENCES tictactoe.game_field (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tictactoe.users
(
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