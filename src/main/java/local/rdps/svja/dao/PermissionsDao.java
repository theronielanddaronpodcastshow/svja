package local.rdps.svja.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import local.rdps.svja.dao.jooq.tables.GroupPermissions;
import local.rdps.svja.dao.jooq.tables.UserGroups;
import local.rdps.svja.dao.jooq.tables.records.GroupPermissionsRecord;
import local.rdps.svja.dao.jooq.tables.records.UserGroupsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.vo.Permissions;
import local.rdps.svja.vo.User;

/**
 * <p>
 * This class serves as the main means of obtaining permissions data, directly interfacing with the database to pull
 * such information as is requested.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class PermissionsDao {
	private static final GroupPermissions gp = GroupPermissions.GROUP_PERMISSIONS;
	private static final Logger logger = LogManager.getLogger();
	private static final Integer TRUE = Integer.valueOf(1);
	private static final UserGroups ug = UserGroups.USER_GROUPS;

	/**
	 * <p>
	 * This method grabs and returns the user's permissions.
	 * </p>
	 *
	 * @param user
	 *            The user for whom we'd like permissions
	 * @return The user's permissions
	 * @throws ApplicationException
	 *             Iff there is an error trying to connect to the database
	 */
	static @NotNull Permissions getUserPermissions(@NotNull final User user) throws ApplicationException {
		try (final Connection readConn = DatabaseManager.getConnection(false)) {
			final DSLContext readContext = DatabaseManager.getBuilder(readConn);
			try (final @NotNull SelectQuery<GroupPermissionsRecord> query = readContext.selectQuery(PermissionsDao.gp);
					final @NotNull SelectQuery<UserGroupsRecord> subQuery = readContext
							.selectQuery(PermissionsDao.ug)) {

				subQuery.addConditions(PermissionsDao.ug.GROUP_ID.equal(PermissionsDao.gp.GROUP_ID));
				subQuery.addConditions(PermissionsDao.ug.USER_ID.equal(Integer.valueOf(user.getId().intValue())));

				query.addConditions(DSL.exists(subQuery));
				final @NotNull Result<GroupPermissionsRecord> results = query.fetch();
				Permissions permissions = new Permissions();
				for (final GroupPermissionsRecord result : results) {
					final Permissions perm = new Permissions();
					perm.setMayAdmin(Boolean.valueOf(PermissionsDao.TRUE.equals(result.getCanAdmin())));
					perm.setMayWrite(Boolean.valueOf(PermissionsDao.TRUE.equals(result.getCanWrite())));
					permissions = Permissions.mergePermissions(permissions, perm);
				}
				permissions.setId(user.getId());
				return permissions;
			}
		} catch (final @NotNull SQLException e) {
			PermissionsDao.logger.error(e.getMessage(), e);

			final Permissions permissions = new Permissions();
			permissions.setId(user.getId());
			return new Permissions();
		}
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private PermissionsDao() {
		// Do nothing
	}
}
