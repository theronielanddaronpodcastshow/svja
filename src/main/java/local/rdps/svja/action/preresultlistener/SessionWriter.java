package local.rdps.svja.action.preresultlistener;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.blo.SessionBloGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.SessionUtils;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This pre-result listener writes the session to the database, allowing changes and adjustments to the data therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionWriter implements PreResultListener {
	private static final Logger logger = LogManager.getLogger();
	private final BaseAction action;

	/**
	 * <p>
	 * This constructor creates a new SessionWriter using the given {@link BaseAction}.
	 * </p>
	 *
	 * @param action
	 *            The {@link BaseAction} from which this pre-result listener has been started
	 */
	public SessionWriter(final @NotNull BaseAction action) {
		this.action = action;
		if (SessionWriter.logger.isDebugEnabled()) {
			SessionWriter.logger.debug("We have created a new SessionWriter using the action {}", action);
		}
	}

	/**
	 * <p>
	 * This method writes any session data in the action to the database.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public void beforeResult(final @NotNull ActionInvocation invocation, final String resultCode) {
		if (!ValidationUtils.isEmpty(this.action)) {
			if (ValidationUtils.isEmpty(this.action.getSessionsRecord())) {
				SessionWriter.logger.warn("We couldn't find the session to be saved:{}{}", System.lineSeparator(),
						this.action.getSessionsRecord());
			} else {
				try {
					final String sessionData = SessionUtils.createBytesFromSessionMap(this.action.getSession());
					if (Objects.equals(this.action.getSessionsRecord().getSessionData(), sessionData)) {
						// If zero data is being updated, just update the last accessed time
						SessionBloGateway.createNewSessionOrUpdateLastAccessed(this.action.getSessionsRecord().getId());
						if (SessionWriter.logger.isDebugEnabled()) {
							SessionWriter.logger.debug("We updated the last access time for our session, {}",
									this.action.getSessionsRecord().getId());
						}
					} else {
						this.action.getSessionsRecord().setSessionData(sessionData);
						if (SessionWriter.logger.isDebugEnabled()) {
							SessionWriter.logger.debug("Saving the session {}", this.action.getSessionsRecord());
						}

						SessionBloGateway.saveSession(this.action.getSessionsRecord());
					}
				} catch (ApplicationException | IOException e) {
					SessionWriter.logger.error(e.getMessage(), e);
				}
			}
		} else {
			SessionWriter.logger.warn(
					"We are running the session writer against something other than a BaseAction or the action is null; action = {} and the invocation was {}",
					this.action, invocation.getInvocationContext().getName());
		}
	}
}
