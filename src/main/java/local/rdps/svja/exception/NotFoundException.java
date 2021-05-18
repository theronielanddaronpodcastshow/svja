package local.rdps.svja.exception;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.ErrorConstants;
import local.rdps.svja.constant.ResultConstants;

/**
 * <p>
 * Base exception class to handle DAO logic exception.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class NotFoundException extends ApplicationException {
	private static final long serialVersionUID = 1000000L;

	/**
	 * Create a new instance
	 */
	public NotFoundException() {
		super();
	}

	/**
	 * Create a new instance with the passed in message
	 *
	 * @param message
	 */
	public NotFoundException(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and uiMessage
	 *
	 * @param message
	 */
	public NotFoundException(final String message, final String uiMessage) {
		super(message, uiMessage);
	}

	/**
	 * Create a new instance with the passed in message and the exception
	 *
	 * @param message
	 * @param cause
	 */
	public NotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * This method returns the exception's Struts return value String, should the exception not be caught before the
	 * BaseInterceptor. This String is used by Struts to make flow decisions, so should either be a global directive or
	 * a local (action-specific) directive. Please see {@link local.rdps.svja.constant.ResultConstants} for the global
	 * Strings and review the struts configuration file (struts.xml) if you are unsure about the proper value for this
	 * String or the flow that this value will cause the action to take.
	 *
	 * @return A known String value for the Struts action, for which Struts has a path
	 */
	@Override
	public String getExceptionReturnString() {
		return ResultConstants.RESULT_ERROR_NOTFOUND;
	}

	/**
	 * This method returns the exception's HTTP response status code that is sent to the client if the request is not
	 * caught before the BaseInterceptor. Please see {@link "http://en.wikipedia.org/wiki/List_of_HTTP_status_codes"}
	 * for a list of status codes and their meaning.
	 *
	 * @return A valid HTTP response status code
	 */
	@Override
	public int getExceptionStatusCode() {
		return 404;
	}

	/**
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public @NotNull String getExceptionTitle() {
		return "API Not Found Exception";
	}

	/**
	 * This method returns the level at which the logger should log the exception.
	 *
	 * @return The level at which the error should be logged by the Logger
	 */
	@Override
	public Level getLogLevel() {
		return Level.WARN;
	}

	/**
	 * This method determines whether or not the stack trace should be logged in the same message as the exception or at
	 * all.
	 *
	 * @return A byte indicating the level and point at which the stack trace should be logged
	 */
	@Override
	public byte getStackTraceLogDirective() {
		return ErrorConstants.LOG_LIMITED_STACK_TRACE;
	}
}
