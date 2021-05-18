package local.rdps.svja.exception;

import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * Common exception to deal with bad parameters being passed in to the service layer
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class IllegalParameterException extends SecurityException {
	private static final long serialVersionUID = 3000000L;

	/**
	 * Create a new instance
	 */
	public IllegalParameterException() {
		super();
	}

	/**
	 * Creates a new instance, based on the given exception. Note that this copies the base information - to chain
	 * exceptions use the previous method
	 *
	 * @param ex
	 *            Exception to use for reference
	 */
	public IllegalParameterException(final @NotNull IllegalArgumentException ex) {
		super(ex.getMessage());
	}

	/**
	 * Create a new instance with the passed in message
	 *
	 * @param message
	 */
	public IllegalParameterException(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and uiMessage
	 *
	 * @param message
	 * @param uiMessage
	 * @since 1.0
	 */
	public IllegalParameterException(final String message, final String uiMessage) {
		super(message, uiMessage);
	}

	/**
	 * Create a new instance with the passed in message and the exception
	 *
	 * @param message
	 * @param cause
	 */
	public IllegalParameterException(final String message, final Throwable cause) {
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
		return ResultConstants.RESULT_ERROR_INPUT;
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
		return 400;
	}

	/**
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public @NotNull String getExceptionTitle() {
		return "Illegal Parameter (invalid input) Error";
	}

	/**
	 * Retrieve the initial message that cause this error
	 *
	 * @return initialMessage The message that was first throw without any additional information
	 */
	@Override
	public String getUiMessage() {
		return ValidationUtils.isEmpty(super.getUiMessage()) ? "Illegal Parameter (invalid input) Error"
				: super.getUiMessage();
	}
}
