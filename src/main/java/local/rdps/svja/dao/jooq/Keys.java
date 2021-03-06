/*
 * This file is generated by jOOQ.
 */
package local.rdps.svja.dao.jooq;


import javax.annotation.processing.Generated;

import local.rdps.svja.dao.jooq.tables.Files;
import local.rdps.svja.dao.jooq.tables.GroupPermissions;
import local.rdps.svja.dao.jooq.tables.Groups;
import local.rdps.svja.dao.jooq.tables.ProjectFiles;
import local.rdps.svja.dao.jooq.tables.Projects;
import local.rdps.svja.dao.jooq.tables.Sessions;
import local.rdps.svja.dao.jooq.tables.UserGroups;
import local.rdps.svja.dao.jooq.tables.Users;
import local.rdps.svja.dao.jooq.tables.records.FilesRecord;
import local.rdps.svja.dao.jooq.tables.records.GroupPermissionsRecord;
import local.rdps.svja.dao.jooq.tables.records.GroupsRecord;
import local.rdps.svja.dao.jooq.tables.records.ProjectFilesRecord;
import local.rdps.svja.dao.jooq.tables.records.ProjectsRecord;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.dao.jooq.tables.records.UserGroupsRecord;
import local.rdps.svja.dao.jooq.tables.records.UsersRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * the default schema.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.14.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<FilesRecord> PK_FILES = Internal.createUniqueKey(Files.FILES, DSL.name("pk_files"), new TableField[] { Files.FILES.ID }, true);
    public static final UniqueKey<FilesRecord> SQLITE_AUTOINDEX_FILES_1 = Internal.createUniqueKey(Files.FILES, DSL.name("sqlite_autoindex_files_1"), new TableField[] { Files.FILES.ID }, true);
    public static final UniqueKey<GroupPermissionsRecord> PK_GROUP_PERMISSIONS = Internal.createUniqueKey(GroupPermissions.GROUP_PERMISSIONS, DSL.name("pk_group_permissions"), new TableField[] { GroupPermissions.GROUP_PERMISSIONS.GROUP_ID }, true);
    public static final UniqueKey<GroupsRecord> PK_GROUPS = Internal.createUniqueKey(Groups.GROUPS, DSL.name("pk_groups"), new TableField[] { Groups.GROUPS.ID }, true);
    public static final UniqueKey<GroupsRecord> SQLITE_AUTOINDEX_GROUPS_1 = Internal.createUniqueKey(Groups.GROUPS, DSL.name("sqlite_autoindex_groups_1"), new TableField[] { Groups.GROUPS.ID }, true);
    public static final UniqueKey<GroupsRecord> SQLITE_AUTOINDEX_GROUPS_2 = Internal.createUniqueKey(Groups.GROUPS, DSL.name("sqlite_autoindex_groups_2"), new TableField[] { Groups.GROUPS.NAME }, true);
    public static final UniqueKey<ProjectFilesRecord> PK_PROJECT_FILES = Internal.createUniqueKey(ProjectFiles.PROJECT_FILES, DSL.name("pk_project_files"), new TableField[] { ProjectFiles.PROJECT_FILES.PROJECT_ID, ProjectFiles.PROJECT_FILES.FILE_ID }, true);
    public static final UniqueKey<ProjectsRecord> PK_PROJECTS = Internal.createUniqueKey(Projects.PROJECTS, DSL.name("pk_projects"), new TableField[] { Projects.PROJECTS.ID }, true);
    public static final UniqueKey<ProjectsRecord> SQLITE_AUTOINDEX_PROJECTS_1 = Internal.createUniqueKey(Projects.PROJECTS, DSL.name("sqlite_autoindex_projects_1"), new TableField[] { Projects.PROJECTS.ID }, true);
    public static final UniqueKey<SessionsRecord> PK_SESSIONS = Internal.createUniqueKey(Sessions.SESSIONS, DSL.name("pk_sessions"), new TableField[] { Sessions.SESSIONS.ID }, true);
    public static final UniqueKey<UserGroupsRecord> PK_USER_GROUPS = Internal.createUniqueKey(UserGroups.USER_GROUPS, DSL.name("pk_user_groups"), new TableField[] { UserGroups.USER_GROUPS.USER_ID, UserGroups.USER_GROUPS.GROUP_ID }, true);
    public static final UniqueKey<UsersRecord> PK_USERS = Internal.createUniqueKey(Users.USERS, DSL.name("pk_users"), new TableField[] { Users.USERS.ID }, true);
    public static final UniqueKey<UsersRecord> SQLITE_AUTOINDEX_USERS_1 = Internal.createUniqueKey(Users.USERS, DSL.name("sqlite_autoindex_users_1"), new TableField[] { Users.USERS.ID }, true);
    public static final UniqueKey<UsersRecord> SQLITE_AUTOINDEX_USERS_2 = Internal.createUniqueKey(Users.USERS, DSL.name("sqlite_autoindex_users_2"), new TableField[] { Users.USERS.USERNAME }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<FilesRecord, UsersRecord> FK_FILES_USERS_1 = Internal.createForeignKey(Files.FILES, DSL.name("fk_files_users_1"), new TableField[] { Files.FILES.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<GroupPermissionsRecord, GroupsRecord> FK_GROUP_PERMISSIONS_GROUPS_1 = Internal.createForeignKey(GroupPermissions.GROUP_PERMISSIONS, DSL.name("fk_group_permissions_groups_1"), new TableField[] { GroupPermissions.GROUP_PERMISSIONS.GROUP_ID }, Keys.PK_GROUPS, new TableField[] { Groups.GROUPS.ID }, true);
    public static final ForeignKey<GroupPermissionsRecord, UsersRecord> FK_GROUP_PERMISSIONS_USERS_1 = Internal.createForeignKey(GroupPermissions.GROUP_PERMISSIONS, DSL.name("fk_group_permissions_users_1"), new TableField[] { GroupPermissions.GROUP_PERMISSIONS.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<GroupsRecord, UsersRecord> FK_GROUPS_USERS_1 = Internal.createForeignKey(Groups.GROUPS, DSL.name("fk_groups_users_1"), new TableField[] { Groups.GROUPS.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<ProjectFilesRecord, FilesRecord> FK_PROJECT_FILES_FILES_1 = Internal.createForeignKey(ProjectFiles.PROJECT_FILES, DSL.name("fk_project_files_files_1"), new TableField[] { ProjectFiles.PROJECT_FILES.FILE_ID }, Keys.PK_FILES, new TableField[] { Files.FILES.ID }, true);
    public static final ForeignKey<ProjectFilesRecord, ProjectsRecord> FK_PROJECT_FILES_PROJECTS_1 = Internal.createForeignKey(ProjectFiles.PROJECT_FILES, DSL.name("fk_project_files_projects_1"), new TableField[] { ProjectFiles.PROJECT_FILES.PROJECT_ID }, Keys.PK_PROJECTS, new TableField[] { Projects.PROJECTS.ID }, true);
    public static final ForeignKey<ProjectFilesRecord, UsersRecord> FK_PROJECT_FILES_USERS_1 = Internal.createForeignKey(ProjectFiles.PROJECT_FILES, DSL.name("fk_project_files_users_1"), new TableField[] { ProjectFiles.PROJECT_FILES.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<ProjectsRecord, UsersRecord> FK_PROJECTS_USERS_1 = Internal.createForeignKey(Projects.PROJECTS, DSL.name("fk_projects_users_1"), new TableField[] { Projects.PROJECTS.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<UserGroupsRecord, GroupsRecord> FK_USER_GROUPS_GROUPS_1 = Internal.createForeignKey(UserGroups.USER_GROUPS, DSL.name("fk_user_groups_groups_1"), new TableField[] { UserGroups.USER_GROUPS.GROUP_ID }, Keys.PK_GROUPS, new TableField[] { Groups.GROUPS.ID }, true);
    public static final ForeignKey<UserGroupsRecord, UsersRecord> FK_USER_GROUPS_USERS_1 = Internal.createForeignKey(UserGroups.USER_GROUPS, DSL.name("fk_user_groups_users_1"), new TableField[] { UserGroups.USER_GROUPS.MODIFIED_BY }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
    public static final ForeignKey<UserGroupsRecord, UsersRecord> FK_USER_GROUPS_USERS_2 = Internal.createForeignKey(UserGroups.USER_GROUPS, DSL.name("fk_user_groups_users_2"), new TableField[] { UserGroups.USER_GROUPS.USER_ID }, Keys.PK_USERS, new TableField[] { Users.USERS.ID }, true);
}
