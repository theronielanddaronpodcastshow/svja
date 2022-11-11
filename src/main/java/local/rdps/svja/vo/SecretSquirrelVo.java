package local.rdps.svja.vo;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This is a view object designed to hold the data that we need to have super secret conversations with a front-end or
 * client.
 * </p>
 *
 * @author DaRon
 * @since 1.0.2
 */
public class SecretSquirrelVo {
	/**
	 * The action that we are going to call and pump data into
	 */
	private @Nullable String actionName;
	/**
	 * The parameters to pass to the method
	 */
	private @Nullable Map<String, String> parameters;
	/**
	 * The RESTful method we are using
	 */
	private @Nullable String restfulMethod;

	/**
	 * <p>
	 * This method gets the name of the action that we are to call to reflectively.
	 * </p>
	 *
	 * @return The class to call to reflectively
	 */
	public @NotNull String getActionName() {
		if (ValidationUtils.isEmpty(this.actionName))
			return CommonConstants.EMPTY_STRING;

		return this.actionName;
	}

	/**
	 * <p>
	 * This method gets the parameters that we pass to the method during the invocation thereof.
	 * </p>
	 *
	 * @return A map of parameter type and parameter value key-value pairing
	 */
	public @NotNull Map<String, String> getParameters() {
		if (ValidationUtils.isEmpty(this.parameters))
			return Collections.emptyMap();

		return this.parameters;
	}

	/**
	 * <p>
	 * This method gets the name of the RESTful method that we will use when calling the action.
	 * </p>
	 *
	 * @return The RESTful method that we want to use when calling the action
	 */
	public @NotNull String getRestfulMethod() {
		if (ValidationUtils.isEmpty(this.restfulMethod))
			return CommonConstants.EMPTY_STRING;

		return this.restfulMethod;
	}

	/**
	 * <p>
	 * This method returns whether or not the {@link SecretSquirrelVo} instance is empty.
	 * </p>
	 *
	 * @return {@code true} iff either the class name or method name is empty
	 */
	public boolean isEmpty() {
		return getActionName().isEmpty() || getRestfulMethod().isEmpty();
	}

	/**
	 * <p>
	 * This method sets the name of the class that we are to call to reflectively.
	 * </p>
	 *
	 * @param actionName
	 *            The class to call to reflectively
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setActionName(final @Nullable String actionName) {
		this.actionName = actionName;
	}

	/**
	 * <p>
	 * This method sets the parameters that we pass to the method during the invocation thereof.
	 * </p>
	 *
	 * @param parameters
	 *            A map of parameter type and parameter value key-value pairing
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setParameters(final @Nullable Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * <p>
	 * This method sets the name of the RESTful method that we will use when calling the action.
	 * </p>
	 *
	 * @param restfulMethod
	 *            The RESTful method that we want to use when calling the action
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setRestfulMethod(final @Nullable String restfulMethod) {
		this.restfulMethod = restfulMethod;
	}
}
