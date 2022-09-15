package local.rdps.svja.action;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.blo.ReflectiveBloGateway;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.vo.ReflectionVo;

/**
 * <p>
 * This class uses reflection to call specific methods, etc..
 * </p>
 *
 * @author DaRon
 * @since 1.0.1
 */
public class ReflectiveAction extends RestAction {
	private static final long serialVersionUID = 1000010L;
	/**
	 * The {@link ReflectionVo} telling us what to call to reflectively
	 */
	private @Nullable ReflectionVo action;
	/**
	 * The data coming back from the reflective call, if anything
	 */
	private @Nullable Object results;

	/**
	 * <p>
	 * This method returns the {@link ReflectionVo} item containing the data that we need to do our reflective action.
	 * </p>
	 *
	 * @return The {@link ReflectionVo} telling us what to call to reflectively
	 */
	public @Nullable ReflectionVo getAction() {
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
		this.results = ReflectiveBloGateway.reflectivelyCallMethod(this.action);
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
	 * This method sets the {@link ReflectionVo} item containing the data that we need to do our reflective action.
	 * </p>
	 *
	 * @param action
	 *            The {@link ReflectionVo} telling us what to call to reflectively
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setAction(final @Nullable ReflectionVo action) {
		this.action = action;
	}
}
