package local.rdps.svja.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.SessionConstants;
import local.rdps.svja.construct.NonceFactoryInterface;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.HijackingException;

/**
 * <p>
 * This class contains a series of methods to help detect, prevent, and thwart or hamper Session Hijacking attempts.
 * These methods can detect a slew of Session Hijacking attacks, such as cookie theft, certain man-in-the-middle
 * attacks, and even certain cross site scripting attacks.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class HijackingUtils {
	private static final Logger logger = LogManager.getLogger();

	private static @NotNull String dumpClientData(final ClientInfo passedClientInfo,
			final ClientInfo storedClientInfo) {
		final StringBuilder data = new StringBuilder(255);

		data.append("\tREMEMBERED CLIENT INFO: ").append(storedClientInfo).append(';').append(System.lineSeparator());
		data.append("\tGIVEN CLIENT INFO:      ").append(passedClientInfo).append(';');

		return data.toString();
	}

	private static @NotNull String dumpNonceData(final @NotNull ServletRequest request) {
		final StringBuilder data = new StringBuilder(255);
		if (!ValidationUtils.isEmpty(request)) {
			final String[] passedInNonce = request.getParameterValues("nonce");

			if (!ValidationUtils.isEmpty((Object[]) passedInNonce)) {
				Arrays.stream(passedInNonce)
						.forEach(passedNonce -> data.append("\tPASSED NONCE:     ").append(passedNonce).append(';'));
			} else {
				data.append("\tPASSED NONCE:     ");
			}
		}

		return data.toString();
	}

	/**
	 * <p>
	 * This method verifies that key client information used to uniquely identify a client hasn't changed. Changes often
	 * indicate that session hijacking is occurring and is considered abnormal behaviour.
	 * </p>
	 *
	 * @param userSession
	 *            The user's session
	 * @param request
	 *            The request
	 * @throws ApplicationException
	 *             If hijacking is detected, a HijackingException is thrown, but if the stored clientinfo is null or not
	 *             ClientInfo, an ApplicationException is thrown
	 */
	public static void verifyClient(final @NotNull Map<String, Object> userSession,
			final @NotNull HttpServletRequest request) throws ApplicationException {
		// Ignore null contexts and requests
		if (ValidationUtils.isEmpty(userSession) || ValidationUtils.isEmpty(request))
			return;

		final ClientInfo requestsClientInfo = new ClientInfo(request);

		if (!userSession.containsKey(SessionConstants.CLIENT_INFO)) {
			if (userSession.containsKey(SessionConstants.AUTHENTICATED_USER_ID))
				throw new HijackingException(
						"The clientInfo is not set BUT a userId is... there may be hijacking going on!"
								+ System.lineSeparator() + HijackingUtils.dumpNonceData(request));
		}

		final ClientInfo storedClientInfo = ConversionUtils.as(ClientInfo.class,
				userSession.get(SessionConstants.CLIENT_INFO));
		if (ValidationUtils.isEmpty(storedClientInfo))
			throw new ApplicationException("The stored client info is null or not of the ClientInfo type: "
					+ userSession.get(SessionConstants.CLIENT_INFO));
		if (!Objects.equals(userSession.get(SessionConstants.CLIENT_INFO), requestsClientInfo))
			throw new HijackingException(
					"The clientInfo that is saved for this session doesn't match the clientInfo that the client sent this request... there may be hijacking going on!"
							+ System.lineSeparator()
							+ HijackingUtils.dumpClientData(requestsClientInfo, storedClientInfo));
	}

	/**
	 * <p>
	 * Verifies that:
	 * </p>
	 * <ul>
	 * <li>The user provided a nonce when we expected</li>
	 * <li>The user didn't provide a nonce when we didn't expect one</li>
	 * <li>The nonce matches what we expected</li>
	 * </ul>
	 *
	 * @param sessionId
	 *            The user's session
	 * @param nonce
	 *            The nonce provided by the user
	 * @param nonceFactory
	 *            The factory responsible for creating and managing nonces
	 * @throws ApplicationException
	 */
	public static void verifyNonce(final @Nullable String sessionId, final @NotNull String nonce,
			final @NotNull NonceFactoryInterface nonceFactory) throws ApplicationException {
		// Ignore null contexts and requests
		if (ValidationUtils.isEmpty(sessionId))
			return;

		// Basic validations
		if (Objects.isNull(sessionId))
			throw new HijackingException("No user session...");

		if (ValidationUtils.isEmpty(nonce))
			throw new HijackingException("No nonce was passed by the client... there may be hijacking going on!"
					+ System.lineSeparator() + nonce);
		nonceFactory.verifyNonce(new Nonce(nonce), sessionId);
	}

	private HijackingUtils() {
	}
}
