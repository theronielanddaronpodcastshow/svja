package local.rdps.svja.action;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.blo.SessionBloGateway;
import local.rdps.svja.blo.UserManagementBloGateway;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.constant.SessionConstants;
import local.rdps.svja.construct.UsernameRegex;
import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.dao.jooq.tables.records.UsersRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.AuthenticationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ConversionUtils;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This class contains the authentication methods that should be used by Struts if using federated services.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class FederatedAuthenticationAction extends RestAction {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 3010002L;
	private UserVo user;
	/**
	 * This string contains the regular expression required to translate the given username into one that we recognise
	 */
	private String usernameTranslationRegex;

	/**
	 * <p>
	 * This method is responsible for authenticating a user using the local database and putting into the database
	 * everything required for the session to work.
	 * </p>
	 *
	 * @return A Struts response indicating whether or not we've succeeded
	 * @throws ApplicationException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 */
	private String authenticateViaLocalDatabase()
			throws ApplicationException, IllegalArgumentException, NoSuchAlgorithmException {
		final UserVo user = ensureAccountIsReady();

		FederatedAuthenticationAction.logger.info("User is authenticated as {} with a User-Agent of {}",
				user.getUsername(), this.request.getHeader("User-Agent"));

		setupSession(user);

		return ResultConstants.RESULT_SUCCESS;
	}

	/**
	 * <p>
	 * This method ensures that the account is available, active, and ready for use, as well as that the contact
	 * information and user information is correct. This information is pulled from the user session.
	 * </p>
	 *
	 * @return A valid, up-to-date {@link UsersRecord} or an empty Optional if the user isn't allowed to authenticate
	 * @throws ApplicationException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 */
	private UserVo ensureAccountIsReady()
			throws ApplicationException, IllegalArgumentException, NoSuchAlgorithmException {
		final UserVo userInput = getUser();
		if (Objects.isNull(userInput))
			throw new IllegalParameterException("The User is empty for some reason");

		final String username = new UsernameRegex(this.usernameTranslationRegex).transform(userInput.getUsername());
		if (FederatedAuthenticationAction.logger.isDebugEnabled()) {
			FederatedAuthenticationAction.logger.debug("The username was transformed from {} to {}",
					userInput.getUsername(), username);
		}

		return UserManagementBloGateway.verifyUserExists(username);
	}

	/**
	 * <p>
	 * Create a new sessionId for the user if a valid session currently exists.
	 * </p>
	 *
	 * @return The new session ID
	 * @throws ApplicationException
	 */
	private Optional<String> rekeyTheUser() throws ApplicationException {
		final SessionsRecord oldSession = getSessionsRecord();
		if (ValidationUtils.isEmpty(oldSession)) {
			final String newSessionId = changeSessionId();
			if (ValidationUtils.not(Objects.equals(oldSession.getId(), newSessionId))) {
				// Port over old session data, if any exists
				if (ValidationUtils.not(ValidationUtils.isEmpty(oldSession.getSessionData()))) {
					getSessionsRecord().setSessionData(oldSession.getSessionData());
					CommonDaoGateway.upsertItem(getSessionsRecord());
				}
				SessionBloGateway.deleteSession(oldSession.getId());
				if (FederatedAuthenticationAction.logger.isDebugEnabled()) {
					FederatedAuthenticationAction.logger.debug(
							"We have copied over the data from the old session and deleted it, having performed the actions necessary to rekey the user");
				}
			}
			return Optional.ofNullable(newSessionId);
		} else if (FederatedAuthenticationAction.logger.isDebugEnabled()) {
			FederatedAuthenticationAction.logger.debug("We couldn't find a session ID to port everything over");
		}
		// No new sessionId
		return Optional.empty();
	}

	/**
	 * <p>
	 * Sets up all user session data for an authenticated user.
	 * </p>
	 *
	 * @param user
	 *            The user to which data will be added
	 * @return A session ID
	 * @throws ApplicationException
	 */
	private Optional<String> setupSession(final UserVo user) throws ApplicationException {
		// Re-key the user
		final Optional<String> newSessionId = rekeyTheUser();

		// set up the user's session
		this.session.put(SessionConstants.AUTHENTICATED_USER_ID, user.getId());

		if (FederatedAuthenticationAction.logger.isDebugEnabled()) {
			FederatedAuthenticationAction.logger.debug("Session Successfully Created.");
		}

		return newSessionId;
	}

	/**
	 * <p>
	 * This method is used to login the user in to the system via the internal login.
	 * </p>
	 *
	 * @return A success or error string
	 * @throws ApplicationException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 * @throws UnsupportedEncodingException
	 */
	public @NotNull String authenticate() throws ApplicationException, IllegalArgumentException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		try {
			// if the user is authenticated, then execution proceeds
			isUserAuthenticated();
			return ResultConstants.RESULT_SUCCESS;
		} catch (final AuthenticationException e) {
			FederatedAuthenticationAction.logger
					.debug("User is not authenticated.  Proceeding to authentication. Exception: {}", e.getMessage());

			return authenticateViaLocalDatabase();
		}
	}

	/**
	 * <p>
	 * This method returns the user's record.
	 * </p>
	 *
	 * @return The user record
	 */
	public @Nullable UserVo getUser() {
		return this.user;
	}

	/**
	 * <p>
	 * No id is set for this action.
	 * </p>
	 *
	 * @return {@code false}
	 */
	@Override
	public boolean isIdSet() {
		return false;
	}

	/**
	 * <p>
	 * Returns if the user has been authenticated or not. If the user is not authenticated, then an
	 * AuthenticationException is thrown.
	 * </p>
	 *
	 * @throws ApplicationException
	 * @throws AuthenticationException
	 *             If the user isn't authenticated
	 */
	public void isUserAuthenticated() throws ApplicationException, AuthenticationException {
		if (ValidationUtils.isEmpty(this.session))
			throw new AuthenticationException("The user session is empty");

		if (ValidationUtils.isEmpty(this.session.get(SessionConstants.AUTHENTICATED_USER_ID)))
			throw new AuthenticationException("No authenticated userId found in the user session." + "\nSessionId: "
					+ getSessionsRecord().getId() + "\nSession: " + this.session);

		final Long userId = ConversionUtils.as(Long.class, this.session.get(SessionConstants.AUTHENTICATED_USER_ID));

		FederatedAuthenticationAction.logger.info("Checking on user {} with a User-Agent of {}", userId,
				this.request.getHeader("User-Agent"));
	}

	/**
	 * <p>
	 * Make sure to keep the user session alive.
	 * </p>
	 *
	 * @return {@link ResultConstants#RESULT_SUCCESS}
	 * @throws ApplicationException
	 */
	public String keepAlive() throws ApplicationException {
		// Update the user session last accessed date. This is automatically done by the session writer
		return ResultConstants.RESULT_SUCCESS;
	}

	/**
	 * <p>
	 * This method sets the user's record.
	 * </p>
	 *
	 * @param user
	 *            The user record
	 */
	public void setUser(final @Nullable UserVo user) {
		this.user = user;
	}

	/**
	 * <p>
	 * This method sets a string that will translate between usernames from a federated service into the SVJA's .
	 * </p>
	 *
	 * @param usernameTranslationRegex
	 *            A perl-style regular expression
	 */
	public void setUsernameTranslationRegex(final String usernameTranslationRegex) {
		this.usernameTranslationRegex = usernameTranslationRegex;
	}

	/**
	 * <p>
	 * Verify that the token and session are valid.
	 * </p>
	 *
	 * @return {@link ResultConstants#RESULT_SUCCESS}
	 * @throws ApplicationException
	 */
	public String verifyAuthentication() throws ApplicationException {
		isUserAuthenticated();
		return ResultConstants.RESULT_SUCCESS;
	}
}
