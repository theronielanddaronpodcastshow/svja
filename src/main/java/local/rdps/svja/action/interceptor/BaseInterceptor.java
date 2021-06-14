package local.rdps.svja.action.interceptor;

import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsStatics;
import org.jetbrains.annotations.NotNull;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * <p>
 * This is the base interceptor -- most if not all Struts interceptors should extend this interceptor.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public abstract class BaseInterceptor implements Interceptor {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;

	@Override
	public void destroy() {
		// Do nothing
	}

	@Override
	public void init() {
		// Ensure that everything is in UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * This is the main interceptor -- it makes calls to all other interceptors, which then make calls to perform the
	 * action. This interceptor method is crucial because it is what handles <em>all</em> bubbled up exceptions and
	 * makes decisions based on them.
	 *
	 * @param actioninvocation
	 *            The action to perform
	 * @return The Struts response String that tells Struts what to do, if it's excepting a returning String from the
	 *         action
	 * @throws Exception
	 */
	@Override
	public final String intercept(final @NotNull ActionInvocation actioninvocation) throws Exception {
		final String result = svjaIntercept(actioninvocation);
		return result;
	}

	/**
	 * This method checks if the user is authenticated. If the user is not, this method kicks the user over to the
	 * authentication page, unless this is a Public release build, in which case the user is given a fake user ID.
	 *
	 * @param invocation
	 *            The action the user wants to perform
	 * @return Either a command to struts that causes authentication or the results of the action the user was trying to
	 *         hit
	 * @throws Exception
	 *             Any exceptions that occur and are not caught get bubbled up
	 */
	public abstract String svjaIntercept(ActionInvocation invocation) throws Exception;
}
