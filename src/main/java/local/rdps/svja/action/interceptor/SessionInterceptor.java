package local.rdps.svja.action.interceptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsStatics;
import org.jetbrains.annotations.NotNull;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import local.rdps.svja.action.AuthenticationAction;
import local.rdps.svja.action.BaseAction;
import local.rdps.svja.action.FederatedAuthenticationAction;
import local.rdps.svja.blo.SessionBloGateway;
import local.rdps.svja.constant.AuthenticationConstants;
import local.rdps.svja.constant.SessionConstants;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.AuthenticationException;
import local.rdps.svja.exception.HijackingException;
import local.rdps.svja.exception.TimeoutException;
import local.rdps.svja.exception.UnknownSessionException;
import local.rdps.svja.util.ClientInfo;
import local.rdps.svja.util.ConversionUtils;
import local.rdps.svja.util.HijackingUtils;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This interceptor exists solely to ensure that we perform proper session management.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionInterceptor extends BaseInterceptor {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * <p>
	 * This field
	 * </p>
	 */
	private static final long serialVersionUID = -7014524176067079961L;

	/**
	 * Get the session from storage based on the session id saved in the provided cookie
	 *
	 * @param invocation
	 * @return The session map from the database
	 * @throws ApplicationException
	 */
	private static @NotNull Map<String, Object> getSessionFromStorage(final @NotNull ActionInvocation invocation)
			throws ApplicationException {
		final ActionContext context = invocation.getInvocationContext();
		final HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);

		final BaseAction action = ConversionUtils.as(BaseAction.class, invocation.getAction());
		if (Objects.nonNull(action)) {
			// To be safe, set our request/response information
			action.setServletRequest(request);
			action.setServletResponse((HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE));

			final Optional<String> sessionId = action.getSessionId();

			// We have a session, so we need to pull the data
			if (sessionId.isPresent()) {
				final SessionsRecord sessionItem = SessionBloGateway.getSession(sessionId.get());

				if (SessionInterceptor.logger.isDebugEnabled()) {
					SessionInterceptor.logger.debug("We were provided the following session data from the database: {}",
							sessionItem);
				}

				// Check that the session is real and not expired
				if (ValidationUtils.isEmpty(sessionItem)) {
					// Our session couldn't be found
					if (SessionInterceptor.logger.isDebugEnabled()) {
						SessionInterceptor.logger.debug(
								"Making a new session since the one that the user presented ({}) could not be found",
								sessionId.get());
					}

					// TODO
					// Prevent someone from harvesting session IDs
					// AttackProtectionUtils.checkForDenialOfServiceAttacks(
					// AttackProtectionUtils.PROTECTION_TYPE_SESSION_GENERATION_ATTACKS);

					if (action instanceof AuthenticationAction || action instanceof FederatedAuthenticationAction) {
						// If we are the authentication action or the login page, it's fair to say that we don't need to
						// throw an exception and can just hand out a new ID
						action.changeSessionId();
					} else
						// Pitch a fit
						throw new UnknownSessionException("The session (" + sessionId
								+ ") could not be found so we're going to scream");
				} else if (Duration.between(sessionItem.getLastAccessed(), LocalDateTime.now(ZoneOffset.UTC)).abs()
						.compareTo(AuthenticationConstants.SESSION_MAX_DURATION) > 0) {
					// Our session is expired
					if (SessionInterceptor.logger.isDebugEnabled()) {
						final LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
						SessionInterceptor.logger.debug(
								"Deleting the expired session {} with a last access time of {} and a current time of {} and a diff of {}",
								sessionId.get(), sessionItem.getLastAccessed(), time,
								Duration.between(sessionItem.getLastAccessed(), time).abs()
										.minus(AuthenticationConstants.SESSION_MAX_DURATION));
					}

					throw new TimeoutException("The session (" + sessionId + ") is expired by "
							+ Duration.between(sessionItem.getLastAccessed(), LocalDateTime.now(ZoneOffset.UTC)).abs()
									.minus(AuthenticationConstants.SESSION_MAX_DURATION)
							+ " so we're going to scream and change it");
				} else {
					// We're real and not expired
					if (SessionInterceptor.logger.isDebugEnabled()) {
						SessionInterceptor.logger.debug("We have an existing, good session {}", sessionId.get());
					}

					action.setSessionsRecord(sessionItem);
					return SessionBloGateway.getSessionMap(sessionItem);
				}
			}
		}

		if (SessionInterceptor.logger.isDebugEnabled()) {
			SessionInterceptor.logger.debug(
					"Falling through and just using an empty map because we couldn't find a session for the user");
		}
		action.changeSessionId();
		return new HashMap<>(0);
	}

	/**
	 * Place the client info into the provided session if it is missing
	 *
	 * @param invocation
	 * @param session
	 */
	private static void putClientInfoIntoSessionIfMissing(final @NotNull ActionInvocation invocation,
			final Map<? super String, Object> session) {
		if (Objects.isNull(session))
			return;

		if (ValidationUtils.isEmpty(session)
				|| ValidationUtils.not(session.containsKey(AuthenticationConstants.CLIENT_INFO))) {
			final ActionContext context = invocation.getInvocationContext();
			final HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
			final ClientInfo clientInfo = new ClientInfo(request);
			session.put(AuthenticationConstants.CLIENT_INFO, clientInfo);
		}
	}

	/**
	 * This method verifies the session for the given request to protect against session hijacking.
	 *
	 * @param session
	 *            The user's session
	 * @param request
	 *            The request
	 * @throws HijackingException
	 * @throws ApplicationException
	 */
	public static void verifySession(final @NotNull Map<String, Object> session, final @NotNull BaseAction action,
			final @NotNull HttpServletRequest request) throws ApplicationException {
		HijackingUtils.verifyClient(session, request);

		if (ValidationUtils.not(action instanceof AuthenticationAction) && ValidationUtils.not(action instanceof FederatedAuthenticationAction)
				&& ValidationUtils.not(session.containsKey(SessionConstants.AUTHENTICATED_USER_ID))) {
			throw new AuthenticationException("The user needs to authenticate");
		}
	}

	/**
	 * <p>
	 * Perform any final things (like log how long the action took).
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public String svjaIntercept(final ActionInvocation invocation) throws Exception {
		final ActionContext context = invocation.getInvocationContext();
		final Map<String, Object> sessionMap = SessionInterceptor.getSessionFromStorage(invocation);

		// add the client info if it doesn't already exist and we're supposed to be touching the session
		SessionInterceptor.putClientInfoIntoSessionIfMissing(invocation, sessionMap);

		final BaseAction action = ConversionUtils.as(BaseAction.class, invocation.getAction());
		final HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		action.getSessionId().orElseThrow(() -> new AuthenticationException("The user needs to authenticate"));
		SessionInterceptor.verifySession(sessionMap, action, request);

		// add the session into context
		context.setSession(sessionMap);
		return invocation.invoke();
	}
}
