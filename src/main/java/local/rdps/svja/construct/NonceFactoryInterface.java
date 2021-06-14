package local.rdps.svja.construct;

import javax.annotation.concurrent.ThreadSafe;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.HijackingException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.Nonce;

/**
 * <p>
 * This interface defines a nonce factory, which is used to create and manage nonces. The implementing factory and its
 * methods <em>must</em> be thread-safe.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
@ThreadSafe
public interface NonceFactoryInterface {
	/**
	 * <p>
	 * This method creates a nonce, places it in the factory's storage location, and ties it to the given session ID.
	 * The newly created nonce is then returned to the caller.
	 * </p>
	 *
	 * @param sessionId
	 *            The session's ID
	 * @return Returns a newly created nonce that is tied to the given session ID
	 * @throws IllegalParameterException
	 *             If there is something wrong with the parameters passed to the method
	 */
	Nonce createNonce(String sessionId) throws IllegalParameterException;

	/**
	 * <p>
	 * This method attempts to verify the given nonce from the factory's storage location. Nonces can only be verified
	 * once -- verification destroys the nonce.
	 * </p>
	 *
	 * @param nonce
	 *            The nonce to verify from the nonce factory
	 * @param sessionId
	 *            The session's ID
	 * @throws HijackingException
	 *             If the nonce does not exist or otherwise cannot be retrieved, a Hijacking Exception is thrown
	 * @throws IllegalParameterException
	 *             If there is something wrong with the parameters passed to the method
	 * @throws ApplicationException
	 *             If there is some other exception, such as multiple matching nonces were found
	 */
	void verifyNonce(Nonce nonce, String sessionId) throws ApplicationException;
}
