package local.rdps.svja.exception;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.ErrorConstants;

/**
 * <p>
 * An exception class intended for warnings, rather than to cause a halting exception.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SqlPerformanceWarning extends ApplicationException {
	private static final long serialVersionUID = 1000000L;

	/**
	 * <p>
	 * Create a new instance.
	 * </p>
	 */
	public SqlPerformanceWarning() {
		super();
	}

	/**
	 * <p>
	 * Create a new instance with the passed in message.
	 * </p>
	 *
	 * @param message
	 *            The warning message to log
	 */
	public SqlPerformanceWarning(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and the exception.
	 * </p>
	 *
	 * @param message
	 *            The warning message to log
	 * @param cause
	 *            The exception to wrap
	 */
	public SqlPerformanceWarning(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 * </p>
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public @NotNull String getExceptionTitle() {
		return "SQL Performance Warning";
	}

	/**
	 * <p>
	 * This method returns the level at which the logger should log the exception.
	 * </p>
	 *
	 * @return The level at which the error should be logged by the Logger
	 */
	@Override
	public @NotNull Level getLogLevel() {
		return Level.WARN;
	}

	/**
	 * <p>
	 * This method determines whether or not the stack trace should be logged in the same message as the exception or at
	 * all.
	 * </p>
	 *
	 * @return A byte indicating the level and point at which the stack trace should be logged
	 */
	@Override
	public byte getStackTraceLogDirective() {
		return ErrorConstants.LOG_LIMITED_STACK_TRACE;
	}
}
