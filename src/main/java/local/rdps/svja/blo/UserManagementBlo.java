package local.rdps.svja.blo;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.jooq.tables.Users;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.AuthenticationException;
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
public class UserManagementBlo {
	/**
	 * <p>
	 * Verify the user exists and password.
	 * </p>
	 *
	 * @param user
	 *            A {@link UserVo} instance that has the username filled in
	 * @return The verified user
	 * @throws ApplicationException
	 */
	static @Nullable UserVo verifyUserLogon(final @NotNull UserVo user) throws ApplicationException {
		final Collection<UserVo> userRecords = CommonDaoGateway.getItems(user,
				Users.USERS.USERNAME.equal(user.getUsername()));

		final UserVo userRecord = userRecords.stream().findFirst()
				.orElseThrow(() -> new AuthenticationException("Unable to find a user with the given data: " + user));
		if (ValidationUtils.isEmpty(userRecord.getId()))
			throw new AuthenticationException("The user found is lacking an id: " + user);
		if (Objects.equals(user.getPassword(), userRecord.getPassword())) {
			userRecord.setLastLoginDate(Instant.now());
			userRecord.setLoginCount(userRecord.getLoginCount() + 1);
			CommonDaoGateway.upsertItem(userRecord);
			return userRecord;
		}

		throw new AuthenticationException("Argh... our passwords didn't match!");
	}
}
