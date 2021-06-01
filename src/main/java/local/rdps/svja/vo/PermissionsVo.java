package local.rdps.svja.vo;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.jooq.tables.GroupPermissions;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This view object stores user permissions.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class PermissionsVo extends ItemVo {
	/**
	 * Whether or not the user should be able to perform administrative actions
	 */
	private Boolean mayAdmin;
	/**
	 * Whether or not the user should be able to write
	 */
	private Boolean mayWrite;
	/**
	 * The {@link UserVo} that these permissions pertain to
	 */
	private UserVo user;

	/**
	 * <p>
	 * This method takes two {@link PermissionsVo} instances and merges the permissions, taking the highest level
	 * permissions from the group and returns a new instance. Please note that the id is never set.
	 * </p>
	 *
	 * @param p1
	 *            The first instance
	 * @param p2
	 *            The second instance
	 * @return A new, merged instance
	 */
	public static PermissionsVo mergePermissions(final @Nullable PermissionsVo p1, final @Nullable PermissionsVo p2) {
		final PermissionsVo mergedPermissions = new PermissionsVo();
		if (Objects.isNull(p1)) {
			if (Objects.isNull(p2))
				return mergedPermissions;
			mergedPermissions.setMayAdmin(p2.mayAdmin);
			mergedPermissions.setMayWrite(p2.mayWrite);
			return mergedPermissions;
		}

		if (Objects.isNull(p2)) {
			mergedPermissions.setMayAdmin(p1.mayAdmin);
			mergedPermissions.setMayWrite(p1.mayWrite);
			return mergedPermissions;
		}

		mergedPermissions.setMayAdmin(p1.getMayAdmin() || p2.getMayAdmin());
		mergedPermissions.setMayWrite(p1.getMayWrite() || p2.getMayWrite());
		return mergedPermissions;
	}

	/**
	 * Default constructor -- does nothing special
	 */
	public PermissionsVo() {
		super(null);
	}

	/**
	 * <p>
	 * This constructor creates a new instance, setting the instance's entity ID to that provided.
	 * </p>
	 *
	 * @param entityId
	 *            The ID of the entity
	 */
	public PermissionsVo(@Nullable final Long entityId) {
		super(entityId);
	}

	/**
	 * <p>
	 * This method returns whether or not the user has permission to administer this application.
	 * </p>
	 *
	 * @return Whether or not the user should be able to perform administrative actions
	 */
	public boolean getMayAdmin() {
		return Objects.nonNull(this.mayAdmin) && this.mayAdmin.booleanValue();
	}

	/**
	 * <p>
	 * This method returns whether or not the user has permission to upsert data.
	 * </p>
	 *
	 * @return Whether or not the user should be able to write data
	 */
	public Boolean getMayWrite() {
		return Objects.nonNull(this.mayWrite) && this.mayWrite.booleanValue();
	}

	/**
	 * <p>
	 * This VO doesn't have a reference table because you should be using the
	 * {@link local.rdps.svja.dao.PermissionsDaoGateway} to work with it, so bugger off.
	 * </p>
	 *
	 * @return {@code null} because you shouldn't be using this VO like that -- use the
	 *         {@link local.rdps.svja.dao.PermissionsDaoGateway}
	 */
	@Override
	public @Nullable GroupPermissions getReferenceTable() {
		return null;
	}

	/**
	 * <p>
	 * This method returns the {@link UserVo} in question. Alternatively, {@link #getId()} will return the user's ID.
	 * </p>
	 *
	 * @return The {@link UserVo} that these permissions pertain to
	 */
	public UserVo getUser() {
		if (Objects.isNull(this.user) && ValidationUtils.isId(super.getId())) {
			final UserVo u = new UserVo();
			u.setId(super.getId());
			try {
				this.user = CommonDaoGateway.getItems(u).stream().findFirst().orElse(null);
			} catch (final ApplicationException e) {
				// Do nothing
			}
		}
		return this.user;
	}

	/**
	 * <p>
	 * This method sets whether or not the user has permission to administer this application.
	 * </p>
	 *
	 * @param mayAdmin
	 *            Whether or not the user should be able to perform administrative actions
	 */
	public void setMayAdmin(final Boolean mayAdmin) {
		this.mayAdmin = mayAdmin;
	}

	/**
	 * <p>
	 * This method sets whether or not the user has permission to upsert data.
	 * </p>
	 *
	 * @param mayWrite
	 *            Whether or not the user should be able to write data
	 */
	public void setMayWrite(final Boolean mayWrite) {
		this.mayWrite = mayWrite;
	}

	/**
	 * <p>
	 * This method sets the {@link UserVo} in question and will update the result of {@link #getId()}. Alternatively, if
	 * you set the id, we will pick up for {@link #getUser()}.
	 * </p>
	 *
	 * @return The {@link UserVo} that these permissions pertain to
	 */
	public void setUser(final UserVo user) {
		if (Objects.nonNull(user)) {
			super.setId(user.getId());
		} else {
			super.setId(null);
		}
		this.user = user;
	}
}
