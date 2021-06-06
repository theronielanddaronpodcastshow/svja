package local.rdps.svja.blo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This class is responsible for providing access to specific user data in an easy, repeatable fashion.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class UserManagementBloGateway {
	/**
	 * <p>
	 * Verify the user exists and password.
	 * </p>
	 *
	 * @param userVo
	 *            A {@link UserVo} instance that has the username filled in
	 * @return The verified user; if we were given a user id and the user does not exist a {@code null} is returned; if
	 *         we were not given a user id and something goes wrong, such as the user doesn't exist or the password is
	 *         bad, we throw an exception
	 * @throws ApplicationException
	 */
	public static @NotNull UserVo verifyUserLogon(final @Nullable UserVo userVo) throws ApplicationException {
		if (ValidationUtils.isEmpty(userVo))
			throw new IllegalParameterException("The TpUser or session map is bad...");
		if (ValidationUtils.not(ValidationUtils.isUserName(userVo.getUsername()))
				|| ValidationUtils.isEmpty(userVo.getPassword()))
			throw new IllegalParameterException("To authenticate, either we need the username/password");

		return UserManagementBlo.verifyUserLogon(userVo);
	}

}
