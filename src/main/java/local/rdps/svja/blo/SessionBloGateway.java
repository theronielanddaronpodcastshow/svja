package local.rdps.svja.blo;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class serves as the gateway for the {@link SessionBlo}, giving all other packages access to the methods
 * contained therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionBloGateway {
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
	public static SessionsRecord createNewSessionOrUpdateLastAccessed(final String sessionId) {
		return SessionBlo.createNewSessionOrUpdateLastAccessed(sessionId);
	}

	/**
	 * <p>
	 * This method deletes the given session if it exists.
	 * </p>
	 *
	 * @param sessionId
	 *            The ID of the session to be deleted
	 * @throws ApplicationException
	 */
	public static void deleteSession(final String sessionId) throws ApplicationException {
		if (ValidationUtils.isEmpty(sessionId))
			throw new IllegalParameterException("The session ID is empty: " + sessionId);

		SessionBlo.deleteSession(sessionId);
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
	public static @Nullable SessionsRecord getSession(final String sessionId) throws IllegalParameterException {
		if (ValidationUtils.isEmpty(sessionId))
			throw new IllegalParameterException("The session ID is empty: " + sessionId);

		return SessionBlo.getSession(sessionId);
	}

	/**
	 * <p>
	 * Get the session map for the provided session.
	 * </p>
	 *
	 * @param sessionItem
	 *            The item from which the map is to be retrieved
	 * @return A valid session map
	 */
	public static @NotNull Map<String, Object> getSessionMap(final SessionsRecord sessionItem) {
		return SessionBlo.getSessionMap(sessionItem);
	}

	/**
	 * <p>
	 * Save session to the database.
	 * </p>
	 *
	 * @param session
	 *            The session to be saved
	 * @throws ApplicationException
	 */
	public static void saveSession(final SessionsRecord session) throws ApplicationException {
		if (ValidationUtils.isEmpty(session))
			throw new IllegalParameterException("The session is empty: " + session);

		SessionBlo.saveSession(session);
	}
}
