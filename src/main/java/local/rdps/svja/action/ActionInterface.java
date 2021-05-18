package local.rdps.svja.action;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.interceptor.ParameterNameAware;

import local.rdps.svja.exception.ApplicationException;

/**
 * <p>
 * This interceptor defines what it means to be an Action.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public interface ActionInterface extends SessionAware, ServletRequestAware, ServletResponseAware, ParameterNameAware {
	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run create requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run create requests
	 * @throws ApplicationException
	 */
	boolean mayCreate() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run delete requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run delete requests
	 * @throws ApplicationException
	 */
	boolean mayDestroy() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run index requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run index requests
	 * @throws ApplicationException
	 */
	boolean mayIndex() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run patch requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run patch requests
	 * @throws ApplicationException
	 */
	boolean mayPatch() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run show requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run show requests
	 * @throws ApplicationException
	 */
	boolean mayShow() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user is allowed to run update requests.
	 * </p>
	 *
	 * @return {@code true} iff the user is allowed to run update requests
	 * @throws ApplicationException
	 */
	boolean mayUpdate() throws ApplicationException;

	/**
	 * <p>
	 * This method determines whether or not the user passes basic conditions necessary to make a valid request against
	 * the action. By default, this means that the user currently exists and is not a foreign national. Override this
	 * implementation to change the basic conditions.
	 * </p>
	 *
	 * @return {@code true} iff the user passes the basic conditions required by this action to perform <em>any</em>
	 *         request against it
	 * @throws ApplicationException
	 */
	default boolean passesBasicConditions() throws ApplicationException {
		// final UsersRecord ur = CommonReadDAOGateway.findRecord(Users.USERS.USER_ID, DatabaseManager.getUid());
		// return Objects.nonNull(ur) && Boolean.FALSE.equals(ur.getForeignNational());
		return true;
	}
}
