package local.rdps.svja.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import local.rdps.svja.action.preresultlistener.CookieWriter;
import local.rdps.svja.action.preresultlistener.SessionWriter;
import local.rdps.svja.blo.SessionBloGateway;
import local.rdps.svja.constant.AuthenticationConstants;
import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.constant.ErrorConstants;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.dao.jooq.tables.records.SessionsRecord;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.exception.NotFoundException;
import local.rdps.svja.util.ValidationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * This class serves as a base form, providing the pieces needed universally across the website.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class BaseAction extends ActionSupport implements ActionInterface {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	/**
	 * A map of cookie name-value pairs.
	 */
	private final Map<String, String> cookies = new HashMap<>(4);
	/**
	 * Whether or not we should create a cookie writer.
	 */
	private boolean createCookieWriter = true;
	/**
	 * Whether or not we should create a session writer.
	 */
	private boolean createSessionWriter = true;
	/**
	 * Whether or not we should log how long the action took to execute
	 */
	private boolean logActionTime = BaseAction.logger.isInfoEnabled();
	/**
	 * The current session
	 */
	private SessionsRecord sessionsRecord;
	private boolean sessionDeleted;
	private boolean sessionIdHasChanged;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Map<String, Object> session;
	/**
	 * Used to track how long an action takes
	 */
	public final Instant startTime = Instant.now();

	/**
	 * Filters potentially harmful parameters
	 *
	 * @param parameterName
	 * @return
	 */
	@Override
	public boolean acceptableParameterName(final @NotNull String parameterName) {
		final boolean allowedParameterName = !parameterName.contains("session")
				&& !Objects.equals("request", parameterName) && !parameterName.contains("dojo")
				&& !parameterName.contains("struts") && !parameterName.contains("application")
				&& !parameterName.contains("servlet") && !parameterName.contains("parameters")
				&& !parameterName.contains("userSession");

		return allowedParameterName;
	}

	/**
	 * <p>
	 * This method sets the session ID to something specific and is used to change the session ID from its current ID,
	 * which may be null, to the newly generated one ID. We only allow <em>changing</em> the session ID once. It is
	 * important to note that this method <em>does not</em> delete the old session, if there was one.
	 * </p>
	 *
	 * @return The most current session ID
	 */
	public String changeSessionId() {
		if (this.sessionIdHasChanged) {
			if (BaseAction.logger.isInfoEnabled()) {
				BaseAction.logger.info(
						"We are trying to change the session id when we already have ({}) and will ignore this request",
						this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME));
			}
		} else {
			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug("We are going to change the session ID");
			}

			this.sessionsRecord = SessionBloGateway.createNewSessionOrUpdateLastAccessed(null);
			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug("The session is changing from {} to {}",
						this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME), this.sessionsRecord.getId());
			}
			this.cookies.put(AuthenticationConstants.SESSION_COOKIE_NAME, this.sessionsRecord.getId().toString());
			this.sessionIdHasChanged = true;
		}

		return this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME);
	}

	/**
	 * <p>
	 * This method removes everything in the cookies map and is largely used to prevent double-writes.
	 * </p>
	 */
	public void clearCookies() {
		this.cookies.clear();
	}

	/**
	 * <p>
	 * This method will request that the current session be deleted. If a new session has been created we will delete it
	 * ourselves since there isn't a means of bubbling it up.
	 * </p>
	 *
	 * @throws ApplicationException
	 */
	public void deleteSessionId() throws ApplicationException {
		if (this.sessionDeleted)
			throw new ApplicationException("We're trying to double-delete the session...");

		if (this.sessionIdHasChanged) {
			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug("We are deleting a newly created session ({})",
						this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME));
			}

			final String sessionId = this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME);
			if (!ValidationUtils.isEmpty(sessionId)) {
				try {
					SessionBloGateway.deleteSession(sessionId);
				} catch (final IllegalParameterException e) {
					BaseAction.logger.error(e.getMessage(), e);
				}
			}
		} else {
			// get the session cookie if it exists
			final Optional<Cookie> optionalSessionId = Objects
					.isNull(this.request.getCookies())
							? Optional.empty()
							: Arrays.stream(this.request.getCookies())
									.filter(cookie -> Objects.equals(AuthenticationConstants.SESSION_COOKIE_NAME,
											cookie.getName()) && !ValidationUtils.isEmpty(cookie.getValue()))
									.findFirst();
			final Optional<String> sessionId = optionalSessionId.map(Cookie::getValue);

			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug("We are requesting that the current session (detected as {}) be deleted",
						sessionId);
			}
			if (sessionId.isPresent()) {
				try {
					SessionBloGateway.deleteSession(sessionId.get());
				} catch (final IllegalParameterException e) {
					BaseAction.logger.error(e.getMessage(), e);
				}
			}

			this.sessionIdHasChanged = true;
		}
		this.sessionDeleted = true;

		this.sessionsRecord = SessionBloGateway.createNewSessionOrUpdateLastAccessed(null);
		this.cookies.put(AuthenticationConstants.SESSION_COOKIE_NAME, this.sessionsRecord.getId().toString());
	}

	/**
	 * Get the current action's name
	 *
	 * @return
	 */
	public String getActionName() {
		return ActionContext.getContext().getName();
	}

	/**
	 * <p>
	 * This method returns the new session ID, if there has been a request for the ID to change. If there has not, an
	 * empty {@link Optional} is returned instead. In cases where the session is to be fully deleted and no new session
	 * created, we return an {@link Optional} that contains a reference to {@link CommonConstants#EMPTY_STRING}.
	 * </p>
	 *
	 * @return An empty {@link Optional} if no change has been requested; an {@link Optional} with a reference to
	 *         {@link CommonConstants#EMPTY_STRING} if the session is to be fully deleted; an {@link Optional} with a
	 *         {@link String} that is the new session ID
	 */
	public Optional<String> getChangedSessionId() {
		if (this.sessionIdHasChanged) {
			final String sessionId = this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME);
			if (ValidationUtils.isEmpty(sessionId))
				return Optional.of(CommonConstants.EMPTY_STRING);

			return Optional.of(sessionId);
		}

		return Optional.empty();
	}

	/**
	 * <p>
	 * This method converts the cookies map into a {@link Collection} of {@link Tuple2}s. These name-value pairs are
	 * then returned to the caller.
	 * </p>
	 *
	 * @return The cookie name-value pairs
	 */
	public Collection<Tuple2<String, String>> getCookies() {
		return this.cookies.entrySet().stream().map(entry -> new Tuple2<>(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * <p>
	 * This method returns the current session map.
	 * </p>
	 *
	 * @return The session map
	 */
	public Map<String, Object> getSession() {
		return this.session;
	}

	/**
	 * <p>
	 * This method returns the session ID, if one is known and to be had. It takes into account any desired changes in
	 * the session ID that have been requested.
	 * </p>
	 *
	 * @return The session ID
	 */
	public Optional<String> getSessionId() {
		final String sessionId;
		if (Objects.isNull(this.sessionsRecord) || (ValidationUtils.isEmpty(this.sessionsRecord.getId()))) {
			sessionId = this.cookies.get(AuthenticationConstants.SESSION_COOKIE_NAME);
			BaseAction.logger.error("The session id is {}", sessionId);
		} else {
			sessionId = this.sessionsRecord.getId();
			BaseAction.logger.error("The session id is {}", sessionId);
		}
		if (this.sessionIdHasChanged) {
			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug("We are returning a previously changed session ID {}", sessionId);
			}
			return Optional.ofNullable(sessionId);
		}

		if (Objects.nonNull(sessionId)) {
			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug(
						"We are returning the current session ID, which we previously extracted from the request {}",
						sessionId);
			}
			return Optional.of(sessionId);
		}

		if (Objects.nonNull(this.request)) {
			final Optional<Cookie> cookieOptional = Objects
					.isNull(this.request.getCookies())
							? Optional.empty()
							: Arrays.stream(this.request.getCookies())
									.filter(cookie -> Objects.equals(AuthenticationConstants.SESSION_COOKIE_NAME,
											cookie.getName()) && !ValidationUtils.isEmpty(cookie.getValue()))
									.findFirst();

			if (BaseAction.logger.isDebugEnabled()) {
				BaseAction.logger.debug(
						"We are returning the current session ID, which we are extracting from the request {}",
						cookieOptional.isPresent() ? cookieOptional.map(Cookie::getValue)
								: (" -- the cookie wasn't found " + cookieOptional.map(Cookie::getName)));
			}

			cookieOptional.ifPresent(
					cookie -> this.cookies.put(AuthenticationConstants.SESSION_COOKIE_NAME, cookie.getValue()));
			return cookieOptional.map(Cookie::getValue);
		}

		if (BaseAction.logger.isInfoEnabled()) {
			BaseAction.logger.info("We haven't made and couldn't find a session");
		}
		return Optional.empty();
	}

	/**
	 * <p>
	 * This method returns the current {@link SessionsRecord}.
	 * </p>
	 *
	 * @return The {@link SessionsRecord}
	 */
	public SessionsRecord getSessionsRecord() {
		return this.sessionsRecord;
	}

	/**
	 * This method handles unknown requests (e.g. unmapped action paths)
	 *
	 * @throws NotFoundException
	 * @throws ApplicationException
	 */
	public @Nullable String handleUnknownRequest() throws NotFoundException {
		final String ciString = "";
		final StringBuilder fullUrl = new StringBuilder(64);
		final StringBuffer requestURL = this.request.getRequestURL();
		final String queryString = this.request.getQueryString();
		if (!ValidationUtils.isEmpty(requestURL)) {
			fullUrl.append(requestURL);
		}
		if (!ValidationUtils.isEmpty(queryString)) {
			fullUrl.append("?");
			fullUrl.append(queryString);
		}

		BaseAction.logger.warn(ErrorConstants.ERROR_UNKNOWN_REQUEST + " Client Info: {}. Request URL: {}", ciString,
				fullUrl);

		// Set the exception status code
		this.response.setStatus(new NotFoundException().getExceptionStatusCode());
		return ResultConstants.RESULT_EXCEPTION;
	}

	/**
	 * Logs a user out and invalidates their session. The removal procedures are handled in the PreResultListener.
	 *
	 * @param servletRequest
	 *            The request that we are trying to log out
	 * @param servletResponse
	 *            The response tied to the request
	 *
	 * @return success
	 */
	public @NotNull String logout(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) {
		this.request = Objects.isNull(this.request) ? servletRequest : this.request;
		this.response = Objects.isNull(this.response) ? servletResponse : this.response;

		try {
			deleteSessionId();
		} catch (final ApplicationException e) {
			BaseAction.logger.error(e.getMessage(), e);
		}
		if (Objects.nonNull(this.session)) {
			this.session.clear();
		}

		return ResultConstants.RESULT_SUCCESS;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayCreate() throws ApplicationException {
		return false;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayDestroy() throws ApplicationException {
		return false;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayIndex() throws ApplicationException {
		return false;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayPatch() throws ApplicationException {
		return false;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayShow() throws ApplicationException {
		return false;
	}

	/**
	 * @return {@code false}
	 */
	@JsonProperty
	@Override
	public boolean mayUpdate() throws ApplicationException {
		return false;
	}

	@Override
	public void setServletRequest(final HttpServletRequest httpServletRequest) {
		this.request = httpServletRequest;
	}

	@Override
	public void setServletResponse(final HttpServletResponse httpServletResponse) {
		this.response = httpServletResponse;
	}

	@Override
	public void setSession(final Map<String, Object> session) {
		if (BaseAction.logger.isDebugEnabled()) {
			session.forEach((k, v) -> BaseAction.logger.debug("Adding {} to the session map with a value of {}", k, v));
		}
		this.session = session;
	}

	/**
	 * <p>
	 * This method sets the stored {@link SessionsRecord} that is tied to the current action.
	 * </p>
	 *
	 * @param sessionsRecord
	 *            Our desired {@link SessionsRecord}
	 */
	public void setSessionsRecord(final SessionsRecord sessionsRecord) {
		this.sessionsRecord = sessionsRecord;
	}

	/**
	 * <p>
	 * This method sets {@link #createSessionWriter} to false.
	 * </p>
	 */
	public void setShouldCreateSessionWriterToFalse() {
		this.createSessionWriter = false;
	}

	/**
	 * <p>
	 * This method returns whether or not we should create a cookie writer. It will only return {@code true} once.
	 * </p>
	 *
	 * @return {@code true} iff a {@link CookieWriter} should be created
	 */
	public boolean shouldCreateCookieWriter() {
		if (this.createCookieWriter) {
			this.createCookieWriter = false;
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * This method returns whether or not we should create a session writer. It will only return {@code true} once.
	 * </p>
	 *
	 * @return {@code true} iff a {@link SessionWriter} should be created
	 */
	public boolean shouldCreateSessionWriter() {
		if (this.createSessionWriter) {
			this.createSessionWriter = false;
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * This method returns whether or not we should log how long the action took. It will only return {@code true} once.
	 * </p>
	 *
	 * @return Whether or not we should log how long the action took
	 */
	public boolean shouldLogActionTime() {
		if (this.logActionTime) {
			this.logActionTime = false;
			return true;
		}
		return false;
	}
}
