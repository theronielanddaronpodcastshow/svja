package local.rdps.svja.action.interceptor;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionInvocation;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.action.preresultlistener.ActionTimer;
import local.rdps.svja.util.ConversionUtils;

/**
 * <p>
 * This interceptor exists solely to perform any final things that you want when all is said and done and the action has
 * completed.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class FinalizerInterceptor extends BaseInterceptor {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -7364255315075006588L;

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
			if (action.shouldLogActionTime()) {
				invocation.addPreResultListener(new ActionTimer(action));
			} else if (FinalizerInterceptor.logger.isDebugEnabled()) {
				FinalizerInterceptor.logger.debug("We have already added an action timer");
			}
		} else {
			FinalizerInterceptor.logger.warn("We are being called on something other than a BaseAction -- {}",
					invocation.getClass());
		}

		// Move on through the invocation stack
		return invocation.invoke();
	}
}
