package local.rdps.svja.action.interceptor;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionInvocation;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.action.preresultlistener.SessionWriter;
import local.rdps.svja.util.ConversionUtils;

/**
 * <p>
 * This interceptor exists solely to ensure that the session data is properly saved to the database when all is said and
 * done.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionWriterInterceptor extends BaseInterceptor {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * <p>
	 * This field
	 * </p>
	 */
	private static final long serialVersionUID = -8217440605409011061L;

	/**
	 * <p>
	 * Perform any final things (like log how long the action took).
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public String svjaIntercept(final ActionInvocation invocation) throws Exception {
		final BaseAction action = ConversionUtils.as(BaseAction.class, invocation.getAction());
		if (Objects.nonNull(action)) {
			// We'd like to know how long everything took
			if (action.shouldCreateSessionWriter()) {
				invocation.addPreResultListener(new SessionWriter(action));
			} else if (SessionWriterInterceptor.logger.isDebugEnabled()) {
				SessionWriterInterceptor.logger.debug("We have already added an action timer");
			}
		} else {
			SessionWriterInterceptor.logger.warn("We are being called on something other than a BaseAction -- {}",
					invocation.getClass());
		}

		// Move on through the invocation stack
		return invocation.invoke();
	}
}
