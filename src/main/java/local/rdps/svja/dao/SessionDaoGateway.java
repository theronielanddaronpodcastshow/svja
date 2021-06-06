package local.rdps.svja.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This gateway provides access to methods that allow reading and writing session data, as well as creating a new
 * session.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionDaoGateway {
	/**
	 * <p>
	 * This method updates the last accessed time if a session ID is provided. If none is provided, a new session is
	 * created and returned.
	 * </p>
	 *
	 * @param sessionId
	 *            Either empty/null if a new session is desired <em>or</em> or an existing session's ID
	 * @return The session's {@link SessionsRecord}, which contains all of the data from the database, or {@code null}
	 *         if a session of the provided ID could not be found or, in the case of a {@code null} sessionId, a new
	 *         session could not be created
	 */
	public static SessionsRecord createNewSessionOrUpdateLastAccessed(final @Nullable String sessionId) {
		return SessionDao.createNewSessionOrUpdateLastAccessed(sessionId);
	}

	/**
	 * <p>
	 * This method deletes the given session if it exists in the database.
	 * </p>
	 *
	 * @param sessionId
	 *            The ID of the session to be deleted
	 * @throws ApplicationException
	 */
	public static void deleteSession(final @NotNull String sessionId) throws ApplicationException {
		if (!ValidationUtils.isAlphaNumericId(sessionId))
			throw new IllegalParameterException("The session ID cannot be empty or null: " + sessionId);

		SessionDao.deleteSession(sessionId);
	}

	/**
	 * <p>
	 * This method returns the session tied to the given session ID. If no session exists, {@code null} is returned.
	 * </p>
	 *
	 * @param sessionId
	 *            The ID of the session to be returned
	 * @return The session's {@link SessionsRecord}, which contains all of the data from the database, or {@code null}
	 *         if a session of the provided ID could not be found
	 * @throws IllegalParameterException
	 *             If any of the parameters are illegal
	 */
	public static @Nullable SessionsRecord getSession(final @NotNull String sessionId)
			throws IllegalParameterException {
		if (!ValidationUtils.isAlphaNumericId(sessionId))
			throw new IllegalParameterException("The session ID cannot be empty or null: " + sessionId);

		return SessionDao.getSession(sessionId);
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private SessionDaoGateway() {
		// Do nothing
	}
}
