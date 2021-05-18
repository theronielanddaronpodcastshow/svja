package local.rdps.svja.exception;

import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.ResultConstants;

/**
 * <p>
 * This exception indicates that there is something wrong with the account.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class AccountException extends SecurityException {
	private static final long serialVersionUID = 3030000L;

	/**
	 * Create a new instance
	 */
	public AccountException() {
		super();
	}

	/**
	 * Create a new instance with the passed in message
	 *
	 * @param message
	 */
	public AccountException(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and the exception
	 *
	 * @param message
	 * @param cause
	 */
	public AccountException(final String message, final Throwable cause) {
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
		return ResultConstants.RESULT_ERROR_NOACCOUNT;
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
		return 401;
	}

	/**
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public @NotNull String getExceptionTitle() {
		return "Account Exception";
	}
}
