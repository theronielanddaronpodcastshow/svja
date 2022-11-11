package local.rdps.svja.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.blo.SuperSecretBloGateway;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.vo.SecretSquirrelVo;

/**
 * <p>
 * This class provides a means of having totally secret conversations with a front-end or client system.
 * </p>
 *
 * @author DaRon
 * @since 1.0.2
 */
public class CipherAction extends RestAction {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	/**
	 * The {@link SecretSquirrelVo} telling us what to call to reflectively
	 */
	private @Nullable SecretSquirrelVo action;
	/**
	 * The data coming back from the reflective call, if anything
	 */
	private @Nullable Object results;

	/**
	 * <p>
	 * This method gets the {@link SecretSquirrelVo} item containing the data that we need to do our super secret
	 * action.
	 * </p>
	 *
	 * @return The {@link SecretSquirrelVo} telling us everything that we need to know to call to the action in a
	 *         discrete manner
	 */
	public @Nullable SecretSquirrelVo getAction() {
		return this.action;
	}

	/**
	 * <p>
	 * This method returns the results from the reflective call.
	 * </p>
	 *
	 * @return The data returned by the reflective call
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public @Nullable Object getResults() {
		return this.results;
	}

	@Override
	public String index() throws ApplicationException {
		this.results = SuperSecretBloGateway.secretelyCallAction(this.action);
		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public boolean isIdSet() {
		return false;
	}

	/**
	 * @return {@code true} iff {@link FilesAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayIndex() throws ApplicationException {
		return true;
	}

	/**
	 * <p>
	 * This method sets the {@link SecretSquirrelVo} item containing the data that we need to do our super secret
	 * action.
	 * </p>
	 *
	 * @param action
	 *            The {@link SecretSquirrelVo} telling us everything that we need to know to call to the action in a
	 *            discrete manner
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setAction(final @Nullable SecretSquirrelVo action) {
		this.action = action;
	}
}
