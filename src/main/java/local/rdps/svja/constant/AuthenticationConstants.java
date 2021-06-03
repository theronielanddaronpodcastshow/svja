package local.rdps.svja.constant;

import java.time.Duration;

/**
 * <p>
 * This class contains various constants tied to authentication/deauthentication and session validation.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class AuthenticationConstants {
	/**
	 * The key name for our client fingerprint in the session map.
	 *
	 * @since 1.0
	 */
	public static final String CLIENT_INFO = "clientInfo";
	/**
	 * The user of the of the anonymous (or unauthenticated) user.
	 *
	 * @since 1.0
	 */
	public static final Long GUEST_USER_ID = Long.valueOf(0);
	/**
	 * The name of the session cookie used by the application.
	 *
	 * @since 1.0
	 */
	public static final String SESSION_COOKIE_NAME = "svjatoken";
	/**
	 * This is the maximum idle-time allowed for a given session before the session is terminated.
	 *
	 * @since 1.0
	 */
	public static final Duration SESSION_MAX_DURATION = Duration.ofMinutes(15L);
	/**
	 * This is the name of the temporary cookie used to hold the URL hit prior to the whole SAML authentication
	 * procedure.
	 *
	 * @since 1.0
	 */
	public static final String TEMPORARY_PRIOR_URL_COOKIE_NAME = "priorUrl";

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 *
	 * @since 1.0
	 */
	private AuthenticationConstants() {
		// Do nothing
	}
}
