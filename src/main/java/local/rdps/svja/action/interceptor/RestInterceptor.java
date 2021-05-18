package local.rdps.svja.action.interceptor;

import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import local.rdps.svja.action.RestAction;
import local.rdps.svja.exception.NotFoundException;

/**
 * <p>
 * A general interceptor to be used with the REST API.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class RestInterceptor implements Interceptor {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 300000L;

	/**
	 * Determine the REST method to call depending on whether the ID for the given action has been set and what HTTP
	 * method was used.
	 *
	 * @param action
	 * @return
	 * @throws NotFoundException
	 */
	private static RestAction.REQUEST_METHOD determineRestMethod(final RestAction action, final boolean isIdSet)
			throws NotFoundException {
		switch (ServletActionContext.getRequest().getMethod()) {
			case RestAction.GET:
				if (isIdSet)
					return RestAction.REQUEST_METHOD.SHOW;
				return RestAction.REQUEST_METHOD.INDEX;
			case RestAction.POST:
				if (isIdSet)
					return RestAction.REQUEST_METHOD.UPDATE;
				return RestAction.REQUEST_METHOD.CREATE;
			case RestAction.PUT:
				return RestAction.REQUEST_METHOD.UPDATE;
			case RestAction.PATCH:
				return RestAction.REQUEST_METHOD.PATCH;
			case RestAction.DELETE:
				return RestAction.REQUEST_METHOD.DESTROY;
			default:
				// API Call Not Found
				throw new NotFoundException(
						ServletActionContext.getRequest().getMethod() + " http method does not exist.");
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init() {
		// Ensure that everything is in UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Setup the request method so that when the action runs it knows what method to hit depending on the HTTP method -
	 * sets the requestMethod property of RestAction
	 *
	 * @param invocation
	 * @return
	 * @throws Exception
	 */
	@Override
	public String intercept(final ActionInvocation invocation) throws Exception {
		if (invocation.getAction() instanceof RestAction) {
			final RestAction action = (RestAction) invocation.getAction();
			// make sure to reset the request method first, in case it has been edited somehow
			action.setRequestMethod(null);
			action.setRequestMethod(RestInterceptor.determineRestMethod(action, action.isIdSet()));
		}
		return invocation.invoke();
	}
}
