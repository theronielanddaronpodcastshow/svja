package local.rdps.svja.exception;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.ResultConstants;

/**
 * <p>
 * This is an exception caused by some kind of hijacking attack. The one who's information has changed, is missing, is
 * present when it shouldn't be, or is otherwise invalid receives this exception. Most often it is caused by some kind
 * of session hijacking, such as cookie theft or click-jacking.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class HijackingException extends RedirectingException {
	private static final Level priority = Level.ERROR;
	private static final long serialVersionUID = 2070000L;
	private static final int statusCode = 403;

	/**
	 * Create a new instance
	 */
	public HijackingException() {
		super();
	}

	/**
	 * Create a new instance with the passed in message
	 *
	 * @param message
	 */
	public HijackingException(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and the exception
	 *
	 * @param message
	 * @param cause
	 */
	public HijackingException(final String message, final Throwable cause) {
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
		return ResultConstants.RESULT_ERROR_REAUTH;
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
		return HijackingException.statusCode;
	}

	/**
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public @NotNull String getExceptionTitle() {
		return "Hijacking Exception";
	}

	/**
	 * This method returns the level at which the logger should log the exception.
	 *
	 * @return The level at which the error should be logged by the Logger
	 */
	@Override
	public Level getLogLevel() {
		return HijackingException.priority;
	}
}
