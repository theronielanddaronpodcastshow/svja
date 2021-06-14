package local.rdps.svja.action;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.NotFoundException;
import local.rdps.svja.vo.JsonErrorVo;

/**
 * <p>
 * This action class handles all unknown actions. In short, it does nothing because by doing nothing we throw the
 * appropriate exceptions.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class UnknownAction extends RestAction {
	/**
	 * The error message that we will always return
	 */
	private static final JsonErrorVo error = new JsonErrorVo(Integer.valueOf(404),
			"URL Not Found -- please check the URL for typos");
	private static final long serialVersionUID = -5879968772814531938L;

	/**
	 * <p>
	 * This method always returns a {@link JsonErrorVo} instance that states a 404 has occurred and to check the URL.
	 * </p>
	 *
	 * @return An error stating that a 404 Not Found exception has occurred
	 */
	@Override
	public JsonErrorVo getError() {
		return UnknownAction.error;
	}

	@Override
	public boolean isIdSet() {
		return false;
	}

	@Override
	public boolean mayCreate() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}

	@Override
	public boolean mayDestroy() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}

	@Override
	public boolean mayIndex() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}

	@Override
	public boolean mayPatch() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}

	@Override
	public boolean mayShow() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}

	@Override
	public boolean mayUpdate() throws ApplicationException {
		throw new NotFoundException("The action does not exist");
	}
}
