package local.rdps.svja.constant;

/**
 * <p>
 * This class contains the session map key constants used by the underlying application and tied to security decisions.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class SessionConstants {
	/**
	 * The authenticated user's numeric, application ID.
	 */
	public static final String AUTHENTICATED_USER_ID = "authUserId";
	/**
	 * The authenticated user's username.
	 */
	public static final String AUTHENTICATED_USER_NAME = "authUserName";
	/**
	 * This indicates the level at which the user is currently authenticated and is used to decide when stepping up is
	 * required.
	 */
	public static final String AUTHENTICATION_LEVEL = "authentationLevel";
	/**
	 * The stored client fingerprint.
	 */
	public static final String CLIENT_INFO = "clientInfo";

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private SessionConstants() {
		// Do nothing
	}
}
