CREATE TABLE "users"
(
    "id"              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    "username"        TEXT    NOT NULL UNIQUE,
    "password"        TEXT,
    "login_count"     INTEGER NOT NULL DEFAULT 0,
    "last_login_date" TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "users_id_index" ON "users" ("id");

CREATE TABLE "groups"
(
    "id"            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    "name"          TEXT    NOT NULL UNIQUE,
    "description"   TEXT    NOT NULL,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id")
);
CREATE INDEX "groups_id_index" ON "groups" ("id");

CREATE TABLE "user_groups"
(
    "user_id"       INTEGER NOT NULL,
    "group_id"      INTEGER NOT NULL,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("group_id") REFERENCES "groups" ("id"),
    FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id"),
    PRIMARY KEY ("user_id", "group_id")
) WITHOUT ROWID;
CREATE INDEX "user_groups_uid_index" ON "user_groups" ("user_id");
CREATE INDEX "user_groups_gid_index" ON "user_groups" ("group_id");

CREATE TABLE "group_permissions"
(
    "group_id"      INTEGER NOT NULL,
    "can_write"     INTEGER NOT NULL DEFAULT 0,
    "can_admin"     INTEGER NOT NULL DEFAULT 0,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("group_id") REFERENCES "groups" ("id"),
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id"),
    PRIMARY KEY ("group_id")
);
CREATE INDEX "group_permissions_gid_index" ON "group_permissions" ("group_id");

CREATE TABLE "files"
(
    "id"            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    "file_name"     TEXT    NOT NULL,
    "description"   TEXT,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id")
);
CREATE INDEX "files_id_index" ON "files" ("id");

CREATE TABLE "projects"
(
    "id"            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    "title"         TEXT    NOT NULL,
    "description"   TEXT,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id")
);
CREATE INDEX "projects_id_index" ON "projects" ("id");

CREATE TABLE "project_files"
(
    "project_id"    INTEGER NOT NULL,
    "file_id"       INTEGER NOT NULL,
    "modified_by"   INTEGER NOT NULL,
    "modified_date" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY ("file_id") REFERENCES "files" ("id"),
    FOREIGN KEY ("project_id") REFERENCES "projects" ("id")
    FOREIGN KEY ("modified_by") REFERENCES "users" ("id"),
    PRIMARY KEY ("project_id", "file_id")
) WITHOUT ROWID;
CREATE INDEX "project_files_pid_index" ON "project_files" ("project_id");
CREATE INDEX "project_files_fid_index" ON "project_files" ("file_id");

CREATE TABLE "sessions" (
	"id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	"session_data"	TEXT
);
CREATE INDEX "sessions_id_index" ON "sessions" ("id");
