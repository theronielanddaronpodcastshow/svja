package local.rdps.svja.blo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.UpdatableRecord;

import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.SessionDaoGateway;
import local.rdps.svja.dao.jooq.tables.interfaces.ISessions;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.SessionUtils;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class is the primer workhorse for any session request.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class SessionBlo {
	private static final Logger logger = LogManager.getLogger();

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
	static SessionsRecord createNewSessionOrUpdateLastAccessed(final String sessionId) {
		return SessionDaoGateway.createNewSessionOrUpdateLastAccessed(sessionId);
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
	static void deleteSession(final String sessionId) throws ApplicationException {
		SessionDaoGateway.deleteSession(sessionId);
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
	static @Nullable SessionsRecord getSession(final String sessionId) throws IllegalParameterException {
		return SessionDaoGateway.getSession(sessionId);
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
	static @NotNull Map<String, Object> getSessionMap(final ISessions sessionItem) {
		if (ValidationUtils.isEmpty(sessionItem)) {
			if (SessionBlo.logger.isDebugEnabled()) {
				SessionBlo.logger.debug("The session record was empty, so we will return a new, but empty, map");
			}
			return new HashMap<>(0);
		}

		Map<String, Object> sessionMap = ValidationUtils.isEmpty(sessionItem) ? null
				: SessionUtils.createSessionMapFromBytes(sessionItem.getSessionData());

		if (Objects.isNull(sessionMap)) {
			if (SessionBlo.logger.isDebugEnabled()) {
				SessionBlo.logger.debug("The session map was empty, so we will return a new, but empty map");
			}
			sessionMap = new HashMap<>(0);
		}

		return sessionMap;
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
	static void saveSession(final UpdatableRecord<SessionsRecord> session) throws ApplicationException {
		CommonDaoGateway.upsertItem(session);
	}

	/**
	 * This is a constants class, so should never be instantiated.
	 *
	 * @since 1.0
	 */
	private SessionBlo() {
		// Do nothing
	}
}
