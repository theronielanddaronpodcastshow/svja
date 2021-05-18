package local.rdps.svja.exception;

/**
 * <p>
 * This exception signals that the user has insufficient capabilities to perform the requested action.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class UnauthorizedAccessException extends SecurityException {
	private static final long serialVersionUID = 3000000L;

	/**
	 * Create a new instance with the passed in message
	 *
	 * @param message
	 */
	public UnauthorizedAccessException(final String message) {
		super(message);
	}

	/**
	 * Create a new instance with the passed in message and the exception
	 *
	 * @param message
	 * @param cause
	 */
	public UnauthorizedAccessException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * The "pretty" title for the exception, which is displayed in the logs to make it easily identifiable.
	 *
	 * @return A unique, "pretty" title for the exception
	 */
	@Override
	public String getExceptionTitle() {
		return "Unauthorized Access Exception";
	}

	/**
	 * Override the ui message
	 *
	 * @return
	 */
	@Override
	public String getUiMessage() {
		return "Unauthorized Access:  Please request access to this system through the RDPS Access Management System (RAMS).";
	}
}
