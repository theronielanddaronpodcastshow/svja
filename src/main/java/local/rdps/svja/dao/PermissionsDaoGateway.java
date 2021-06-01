package local.rdps.svja.dao;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.PermissionsVo;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This class serves as the gateway for the {@link PermissionsDao}, giving all other packages access to the methods
 * contained therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class PermissionsDaoGateway {
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
	public static @NotNull PermissionsVo getUserPermissions(@Nullable final UserVo user) throws ApplicationException {
		if (Objects.isNull(user) || ValidationUtils.not(ValidationUtils.isId(user.getId())))
			return new PermissionsVo();

		return PermissionsDao.getUserPermissions(user);
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private PermissionsDaoGateway() {
		// Do nothing
	}
}
