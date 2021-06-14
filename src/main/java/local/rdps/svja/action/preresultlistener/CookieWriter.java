package local.rdps.svja.action.preresultlistener;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsStatics;
import org.jetbrains.annotations.NotNull;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.util.SessionUtils;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This pre-result listener writes the cookie(s) stored in the {@link BaseAction} into the response.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CookieWriter implements PreResultListener {
	private static final Logger logger = LogManager.getLogger();
	private final BaseAction action;

	/**
	 * <p>
	 * This constructor creates a new CookieWriter using the given {@link BaseAction}.
	 * </p>
	 *
	 * @param action
	 *            The {@link BaseAction} from which this pre-result listener has been started
	 */
	public CookieWriter(final @NotNull BaseAction action) {
		this.action = action;
		if (CookieWriter.logger.isDebugEnabled()) {
			CookieWriter.logger.debug("We have created a new CookieWriter using the action {}", action);
		}
	}

	/**
	 * <p>
	 * This method logs how long an action took, in total, to execute.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public void beforeResult(final @NotNull ActionInvocation invocation, final String resultCode) {
		if (!ValidationUtils.isEmpty(this.action)) {
			final ActionContext context = invocation.getInvocationContext();
			final HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
			final HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);
			if (CookieWriter.logger.isDebugEnabled()) {
				CookieWriter.logger.debug(
						"Writing cookies -- {} -- for {}", this.action.getCookies().stream()
								.map(tuple -> tuple.v1() + " - " + tuple.v2()).collect(Collectors.joining("; ")),
						this.action.getActionName());
			}
			this.action.getCookies()
					.forEach(tuple -> SessionUtils.manageCookie(request, response, tuple.v1(), tuple.v2()));
		} else {
			if (CookieWriter.logger.isWarnEnabled()) {
				CookieWriter.logger.warn(
						"We are running the session writer against something other than a BaseForm or the action is null; action = {} and the invocation was {}",
						this.action, invocation.getInvocationContext().getName());
			}
		}
	}
}
