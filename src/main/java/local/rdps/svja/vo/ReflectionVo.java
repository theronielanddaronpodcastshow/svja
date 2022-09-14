package local.rdps.svja.vo;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This is a view object designed to hold Java reflection data.
 * </p>
 *
 * @author DaRon
 * @since 1.0.1
 */
public class ReflectionVo {
	/**
	 * The class name that we are to pull via reflection
	 */
	private @Nullable String className;
	/**
	 * The method that we are to reflectively invoke
	 */
	private @Nullable String methodName;
	/**
	 * The namespace of the class that we are to pull via reflection
	 */
	private @Nullable String namespace;
	/**
	 * The parameters to pass to the method
	 */
	private @Nullable Map<String, Object> parameters;

	/**
	 * <p>
	 * This method gets the name of the class that we are to call to reflectively.
	 * </p>
	 *
	 * @return The class to call to reflectively
	 */
	public @NotNull String getClassName() {
		if (ValidationUtils.isEmpty(this.className))
			return CommonConstants.EMPTY_STRING;

		return this.className;
	}

	/**
	 * <p>
	 * This method gets the name of the method that we will invoke reflectively.
	 * </p>
	 *
	 * @return The method to invoke
	 */
	public @NotNull String getMethodName() {
		if (ValidationUtils.isEmpty(this.methodName))
			return CommonConstants.EMPTY_STRING;

		return this.methodName;
	}

	/**
	 * <p>
	 * This method gets the namespace for the class that we are to call to reflectively.
	 * </p>
	 *
	 * @return The namespace of the class that we call to reflectively
	 */
	public @NotNull String getNamespace() {
		if (ValidationUtils.isEmpty(this.namespace))
			return CommonConstants.EMPTY_STRING;

		return this.namespace + '.';
	}

	/**
	 * <p>
	 * This method gets the parameters that we pass to the method during the invocation thereof.
	 * </p>
	 *
	 * @return A map of parameter type and parameter value key-value pairing
	 */
	public @NotNull Map<String, Object> getParameters() {
		if (ValidationUtils.isEmpty(this.parameters))
			return Collections.emptyMap();

		return this.parameters;
	}

	/**
	 * <p>
	 * This method returns whether or not the {@link ReflectionVo} instance is empty.
	 * </p>
	 *
	 * @return {@code true} iff either the class name or method name is empty
	 */
	public boolean isEmpty() {
		return getClassName().isEmpty() || getMethodName().isEmpty();
	}

	/**
	 * <p>
	 * This method sets the name of the class that we are to call to reflectively.
	 * </p>
	 *
	 * @param className
	 *            The class to call to reflectively
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setClassName(final @Nullable String className) {
		LogManager.getLogger().info("Setting the class to {}", className);
		this.className = className;
	}

	/**
	 * <p>
	 * This method sets the name of the method that we will invoke reflectively.
	 * </p>
	 *
	 * @param methodName
	 *            The method to invoke
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setMethodName(final @Nullable String methodName) {
		LogManager.getLogger().info("Setting the method to {}", methodName);
		this.methodName = methodName;
	}

	/**
	 * <p>
	 * This method sets the namespace for the class that we are to call to reflectively.
	 * </p>
	 *
	 * @param namespace
	 *            The namespace of the class that we call to reflectively
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public void setNamespace(final @Nullable String namespace) {
		LogManager.getLogger().info("Setting the method to {}", namespace);
		this.namespace = namespace;
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
	public void setParameters(final @Nullable Map<String, Object> parameters) {
		LogManager.getLogger()
				.debug("Setting the parameters to {}",
						Objects.nonNull(parameters)
								? parameters.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue())
										.collect(Collectors.joining(","))
								: null);
		this.parameters = parameters;
	}
}
