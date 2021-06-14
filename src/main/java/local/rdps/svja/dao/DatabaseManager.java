package local.rdps.svja.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Attachable;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Scope;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.simpleflatmapper.jooq.JooqMapperFactory;
import org.sqlite.SQLiteConfig;

import local.rdps.svja.constant.SessionConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.CommonUtils;
import local.rdps.svja.util.ConversionUtils;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class handles the tasks for creating connections, etc, for the Java SQL DSL.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class DatabaseManager {
	private static final Configuration CONNECTION_CONFIGURATION = DatabaseManager.getConfiguration();
	private static final @NotNull String DATABASE_URI = "jdbc:sqlite:"
			+ ServletActionContext.getServletContext().getRealPath("WEB-INF/classes/db/svja.db");
	private static final Logger logger = LogManager.getLogger();
	private static final @NotNull Properties READ_CONNECTION_PROPERTIES = DatabaseManager
			.setupConnectionProperties(false);
	private static final @NotNull Properties WRITE_CONNECTION_PROPERTIES = DatabaseManager
			.setupConnectionProperties(true);

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (final ClassNotFoundException e) {
			DatabaseManager.logger.error(e.getMessage(), e);
		}
	}

	private static @NotNull Configuration getConfiguration() {
		final Configuration configuration = new DefaultConfiguration().set(SQLDialect.SQLITE)
				.set(new Settings().withRenderFormatted(Boolean.TRUE))
				.set(JooqMapperFactory.newInstance().ignorePropertyNotFound().useAsm(true).newRecordMapperProvider());
		configuration.set(JooqMapperFactory.newInstance().ignorePropertyNotFound().useAsm(true)
				.newRecordUnmapperProvider(configuration));
		configuration.set(new DefaultExecuteListenerProvider(new DatabaseListener()));

		return configuration;
	}

	private static @NotNull Properties setupConnectionProperties(final boolean writePool) {
		// Set up our properties
		final SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(true);
		config.setReadOnly(ValidationUtils.not(writePool));
		config.enforceForeignKeys(true);
		config.enableShortColumnNames(true);
		return config.toProperties();
	}

	/**
	 * Closes the provided connection.
	 *
	 * @param conn
	 *            The connection to be closed;
	 */
	public static void closeConnection(final @Nullable Connection conn) {
		if (Objects.nonNull(conn)) {
			try {
				conn.close();
			} catch (final @NotNull SQLException e) {
				DatabaseManager.logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns a configuration that can be tied to a record for the given connection. The configuration will be properly
	 * set for SVJA.
	 *
	 * @param conn
	 *            The open connection through which the record will persist or searched for
	 * @return The connection's configuration
	 * @see #getAttachableConfiguration(Scope)
	 */
	public static @NotNull Configuration getAttachableConfiguration(final Connection conn) {
		return DatabaseManager.getBuilder(conn).configuration();
	}

	/**
	 * Returns a configuration from the provided context that can be tied to a record. The configuration will only be
	 * able to connect to the database if the context was built with a connection -or- a connect was tied to the context
	 * after building but before this call.
	 *
	 * @param context
	 *            The context to get the configuration from
	 * @return The context's configuration
	 * @see #getAttachableConfiguration(Connection)
	 */
	public static @NotNull Configuration getAttachableConfiguration(final @NotNull Scope context) {
		return context.configuration();
	}

	/**
	 * Returns a context from which SQL queries are to be built, <strong>but should not be executed</strong>. This
	 * context is set using the proper SQL dialect and ready for use.
	 *
	 * @return A context from which SQL queries can be built <strong>but not run</strong>
	 * @see #getBuilder(Connection)
	 */
	public static @NotNull DSLContext getBuilder() {
		return DSL.using(DatabaseManager.CONNECTION_CONFIGURATION);
	}

	/**
	 * Returns a context from which SQL queries are to be built and executed. This context is set using the proper SQL
	 * dialect and ready for use.
	 *
	 * @param conn
	 *            The connection to tie the context to
	 * @return A context from which SQL queries can be built and run
	 * @see #getBuilder()
	 * @see #getConnection(boolean)
	 */
	public static @NotNull DSLContext getBuilder(final Connection conn) {
		return DSL.using(DatabaseManager.CONNECTION_CONFIGURATION.derive().set(new DefaultConnectionProvider(conn)));
	}

	/**
	 * Returns a connection to the database.
	 *
	 * @param write
	 *            {@code true} iff the connection is to use the write-enabled user, otherwise we use the read-only user
	 * @return A valid, open connection to the database.
	 * @throws ApplicationException
	 * @see #getBuilder(Connection)
	 */
	public static Connection getConnection(final boolean write) throws ApplicationException {
		if (write) {
			try {
				return DriverManager.getConnection(DatabaseManager.DATABASE_URI,
						DatabaseManager.WRITE_CONNECTION_PROPERTIES);
			} catch (final @NotNull SQLException e) {
				throw new ApplicationException("Connection to the WRITE pool failed... ", e);
			}
		}

		try {
			return DriverManager.getConnection(DatabaseManager.DATABASE_URI,
					DatabaseManager.READ_CONNECTION_PROPERTIES);
		} catch (final @NotNull SQLException e) {
			throw new ApplicationException("Connection to the READ pool failed... ", e);
		}
	}

	/**
	 * Gets the current user's UID or null
	 *
	 * @return The uid of the current user.
	 */
	public static @Nullable Long getUid() {
		final Map<String, Object> session = CommonUtils.getSession();
		if (ValidationUtils.isValidSessionMap(session))
			return ConversionUtils.as(Long.class, session.get(SessionConstants.AUTHENTICATED_USER_ID));
		return null;
	}

	/**
	 * This method will attach a configuration that is formed using the connection to the record, after which point the
	 * record will be able to make calls to the database.
	 *
	 * @param record
	 * @param conn
	 * @see #setConfiguration(Attachable, Scope)
	 */
	public static void setConfiguration(final @NotNull Attachable record, final Connection conn) {
		record.attach(DatabaseManager.getBuilder(conn).configuration());
	}

	/**
	 * This method will attach the context's configuration to the record. If the context is tied to a connection prior
	 * to this call, the record will be able to make calls to the database.
	 *
	 * @param record
	 * @param context
	 * @see #setConfiguration(Attachable, Connection)
	 */
	public static void setConfiguration(final @NotNull Attachable record, final @NotNull Scope context) {
		record.attach(context.configuration());
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private DatabaseManager() {
		// Do nothing
	}
}
