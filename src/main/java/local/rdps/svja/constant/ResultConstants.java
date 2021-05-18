package local.rdps.svja.constant;

/**
 * <p>
 * This class contains the various error constants used by the application that are tied to/pushed the results
 * listeners.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class ResultConstants {
	public static final String RESULT_ERROR_APINOTFOUND = "error-apiNotFound";
	/**
	 * Authentication failed.
	 */
	public static final String RESULT_ERROR_AUTHFAILED = "error-authFailed";
	/**
	 * The requested item has been deleted and the user cannot view it.
	 */
	public static final String RESULT_ERROR_DELETED = "error-deleted";
	/**
	 * Denial of Service (DoS) conditions are present, so we need struts to handle it using the global action.
	 */
	public static final String RESULT_ERROR_DOS = "error-denialOfService";
	/**
	 * The user cannot edit the requested resource.
	 */
	public static final String RESULT_ERROR_EDIT = "error-edit";
	/**
	 * A generic error has occurred and we are using the global struts action to handle generic or non-specific errors.
	 */
	public static final String RESULT_ERROR_GLOBAL = "error-global";
	/**
	 * There was something wrong with the input provided by the user.
	 */
	public static final String RESULT_ERROR_INPUT = "error-input";
	/**
	 * The user has no access to the requested resource.
	 */
	public static final String RESULT_ERROR_NOACCESS = "error-noAccess";
	/**
	 * The account does not exist.
	 */
	public static final String RESULT_ERROR_NOACCOUNT = "error-noAccount";
	/**
	 * The resource could not be found.
	 */
	public static final String RESULT_ERROR_NOTFOUND = "error-notFound";
	/**
	 * The user needs to reauthenticate, typically because their session has expired.
	 */
	public static final String RESULT_ERROR_REAUTH = "error-reauth";
	public static final String RESULT_ERROR_SERVICE_UNAVAILABLE = "error-service-unavailable";
	/**
	 * The session is authenticated too low and needs to step up.
	 */
	public static final String RESULT_ERROR_STEPUP = "error-step-up-authentication-context";
	/**
	 * The session has timed out and we will need to reauthenticate.
	 */
	public static final String RESULT_ERROR_TIMEOUT = "error-timeout";
	public static final String RESULT_EXCEPTION = "exception";
	/**
	 * We want and need to redirect the user.
	 */
	public static final String RESULT_REDIRECT = "redirect";
	/**
	 * We need to reauthenticate
	 */
	public static final String RESULT_REDIRECT_AUTHENTICATE = "redirect-authenticate";
	public static final String RESULT_SUCCESS = "success";
	/**
	 * There are too many users or requests, so we need struts to handle it using the global action.
	 */
	public static final String RESULT_TOO_MANY_USERS = "error-tooManyUsers";

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private ResultConstants() {
		// Do nothing
	}
}
