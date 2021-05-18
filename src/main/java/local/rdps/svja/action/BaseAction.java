package local.rdps.svja.action;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import local.rdps.svja.constant.ErrorConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.NotFoundException;
import local.rdps.svja.util.ValidationUtils;

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
	private boolean sessionDeleted;
	private boolean sessionIdHasChanged;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Map<String, Object> userSession;
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
	 * Get the current action's name
	 *
	 * @return
	 */
	public String getActionName() {
		return ActionContext.getContext().getName();
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
		return this.userSession;
	}

	/**
	 * This method handles unknown requests (e.g. unmapped action paths)
	 *
	 * @throws NotFoundException
	 * @throws ApplicationException
	 */
	public @Nullable String handleUnknownRequest() throws NotFoundException {
		// TODO:
		// AttackProtectionUtils.checkForDenialOfServiceAttacks(AttackProtectionUtils.PROTECTION_TYPE_HEAVY_ACTIONS_DOS);
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

		// this.response.setStatus(404);
		BaseAction.logger.warn(ErrorConstants.ERROR_UNKNOWN_REQUEST + " Client Info: {}. Request URL: {}", ciString,
				fullUrl);
		throw new NotFoundException("Could not find the action: " + ciString + " " + fullUrl,
				"The requested URL does not exist.");
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
		this.userSession = session;
	}

	/**
	 * <p>
	 * This method sets {@link createSessionWriter} to false.
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
