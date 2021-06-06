package local.rdps.svja.action.interceptor;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionInvocation;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.action.preresultlistener.CookieWriter;
import local.rdps.svja.util.ConversionUtils;

/**
 * <p>
 * This interceptor exists solely to write cookies to the response.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CookieWriterInterceptor extends BaseInterceptor {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * <p>
	 * This field
	 * </p>
	 */
	private static final long serialVersionUID = 6540566249624120444L;

	/**
	 * <p>
	 * Write the cookie(s) to the response.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public String svjaIntercept(final ActionInvocation invocation) throws Exception {
		final BaseAction action = ConversionUtils.as(BaseAction.class, invocation.getAction());
		if (Objects.nonNull(action)) {
			// Save our cookies
			if (action.shouldCreateCookieWriter()) {
				invocation.addPreResultListener(new CookieWriter(action));
			} else if (CookieWriterInterceptor.logger.isDebugEnabled()) {
				CookieWriterInterceptor.logger.debug("We have already added a cookie writer");
			}
		} else {
			CookieWriterInterceptor.logger.warn("We are being called on something other than a BaseAction -- {}",
					invocation.getClass());
		}

		// Move on through the invocation stack
		return invocation.invoke();
	}
}
