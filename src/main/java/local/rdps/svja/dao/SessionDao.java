package local.rdps.svja.dao;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.SelectQuery;
import org.jooq.UpdateQuery;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import local.rdps.svja.dao.jooq.tables.Sessions;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class handles reading and writing session data, as well as creating a new session.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionDao {
	/**
	 * The characterset that makes up the possible characters for a session ID
	 */
	private static final char[] CHARACTERS_FOR_SESSION_IDS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	private static final Logger logger = LogManager.getLogger();
	private static final Sessions s = Sessions.SESSIONS;
	/**
	 * The length session ids should be
	 */
	private static final int SESSION_ID_LENGTH = 36;
	/**
	 * A secure PRNG engine
	 */
	private static final SecureRandom SPRNG = new SecureRandom();

	/**
	 * <p>
	 * This method creates a new, randomly generated session ID. Of course, because randomness doesn't mean that there
	 * aren't duplicates, it is the job of the caller to catch instances where duplicates are created and try again.
	 * </p>
	 *
	 * @return A random session ID
	 */
	private static String createNewSessionId() {
		final StringBuilder str = new StringBuilder(SessionDao.SESSION_ID_LENGTH);
		for (int i = 0; i < SessionDao.SESSION_ID_LENGTH; i++) {
			str.append(SessionDao.CHARACTERS_FOR_SESSION_IDS[SessionDao.SPRNG
					.nextInt(SessionDao.CHARACTERS_FOR_SESSION_IDS.length)]);
		}
		return str.toString();
	}

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
		try (final Connection writeConn = DatabaseManager.getConnection(true)) {
			final DSLContext writeContext = DatabaseManager.getBuilder(writeConn);
			// We should be inserting a new session
			if (ValidationUtils.isEmpty(sessionId)) {
				try (final InsertQuery<SessionsRecord> query = writeContext.insertQuery(SessionDao.s)) {
					// Set up the query
					query.setReturning();
					query.addValue(SessionDao.s.ID, SessionDao.createNewSessionId());
					query.addValue(SessionDao.s.LAST_ACCESSED, DSL.currentLocalDateTime());

					// Execute and get the results
					final int rowsWritten = query.execute();
					if (SessionDao.logger.isDebugEnabled()) {
						SessionDao.logger.debug("We wrote {} rows to the database when creating a new session",
								Integer.valueOf(rowsWritten));
					}
					return query.getReturnedRecord();
				} catch (final DataAccessException e) {
					// We weren't unique, so we have to try again
					return SessionDao.createNewSessionOrUpdateLastAccessed(sessionId);
				}
			}

			// We should be updating an existing session
			try (final UpdateQuery<SessionsRecord> query = writeContext.updateQuery(SessionDao.s)) {
				// Set up the query
				query.setReturning();
				query.addValue(SessionDao.s.LAST_ACCESSED, DSL.currentLocalDateTime());
				query.addConditions(SessionDao.s.ID.equal(sessionId));

				// Execute and get the results
				final int rowsWritten = query.execute();
				if (SessionDao.logger.isDebugEnabled()) {
					SessionDao.logger.debug("We wrote {} rows when updating the last_accessed data",
							Integer.valueOf(rowsWritten));
				}
				if (rowsWritten > 1) {
					writeConn.setAutoCommit(false);
					writeConn.rollback();
					throw new ApplicationException("We updated too many groups (" + rowsWritten
							+ " updated, but we only expected 1) so we rolled back: "
							+ writeContext.renderInlined(query));
				}
				return query.getReturnedRecord();
			}
		} catch (final SQLException | ApplicationException e) {
			SessionDao.logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * <p>
	 * This method deletes the given session if it exists in the database.
	 * </p>
	 *
	 * @param sessionId
	 *            The ID of the session to be deleted
	 * @throws ApplicationException
	 * @throws IllegalParameterException
	 *             If any of the parameters are illegal
	 */
	static void deleteSession(final String sessionId) throws ApplicationException {
		try (final Connection writeConn = DatabaseManager.getConnection(true)) {

			final DSLContext writeContext = DatabaseManager.getBuilder(writeConn);
			final DeleteQuery<SessionsRecord> query = writeContext.deleteQuery(SessionDao.s);
			query.addConditions(SessionDao.s.ID.equal(sessionId));

			// Execute the query
			final int rowsDeleted = query.execute();
			if (SessionDao.logger.isDebugEnabled()) {
				SessionDao.logger.debug("We deleted {} rows when deleting the session data",
						Integer.valueOf(rowsDeleted));
			}
			if (rowsDeleted > 1) {
				writeConn.setAutoCommit(false);
				writeConn.rollback();
				throw new ApplicationException("We deleted too many groups (" + rowsDeleted
						+ " deleted, but we only expected 1) so we rolled back: " + writeContext.renderInlined(query));
			}
			if (rowsDeleted == 0)
				throw new IllegalParameterException("Someone is giving us bad session Ids: " + sessionId);
		} catch (final SQLException e) {
			SessionDao.logger.error(e.getMessage(), e);
		}
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
	 */
	static @Nullable SessionsRecord getSession(final String sessionId) {
		try (final Connection readConn = DatabaseManager.getConnection(false)) {
			final DSLContext readContext = DatabaseManager.getBuilder(readConn);
			final SelectQuery<SessionsRecord> query = readContext.selectQuery(SessionDao.s);
			query.addConditions(SessionDao.s.ID.equal(sessionId));

			// Execute the query
			return query.fetchAny();
		} catch (final SQLException | ApplicationException e) {
			SessionDao.logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private SessionDao() {
		// Do nothing
	}
}
