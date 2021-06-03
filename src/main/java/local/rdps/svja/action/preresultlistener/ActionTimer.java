package local.rdps.svja.action.preresultlistener;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This pre-result listener logs how long the action took, using the time tracked in the action.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ActionTimer implements PreResultListener {
	private static final Logger logger = LogManager.getLogger();
	private final BaseAction action;

	/**
	 * <p>
	 * This constructor creates a new ActionTimer using the given {@link BaseAction}.
	 * </p>
	 *
	 * @param action
	 *            The {@link BaseAction} from which this pre-result listener has been started
	 */
	public ActionTimer(final @NotNull BaseAction action) {
		this.action = action;
		if (ActionTimer.logger.isDebugEnabled()) {
			ActionTimer.logger.debug("We have created a new ActionTimer using the action {}", action);
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
			if (ActionTimer.logger.isInfoEnabled()) {
				ActionTimer.logger.info("The action {} under {} took {}", this.action.getActionName(),
						this.action.getClass().getName(), Duration.between(this.action.startTime, Instant.now()).abs());
			}
		} else {
			if (ActionTimer.logger.isWarnEnabled()) {
				ActionTimer.logger.warn(
						"We are running the session writer against something other than a BaseAction or the action is null; action = {} and the invocation was {}",
						this.action, invocation.getInvocationContext().getName());
			}
		}
	}
}
