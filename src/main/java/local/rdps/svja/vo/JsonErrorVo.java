package local.rdps.svja.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Class that represents a json error object, holding key error data that will make its way to a JSON response.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class JsonErrorVo {
	private final Integer code;
	private final String message;

	/**
	 * Create a new JsonError with the given parameters
	 *
	 * @param code
	 *            The error code to push via JSON as part of the content
	 * @param message
	 *            The error message to push via JSON as part of the content
	 */
	public JsonErrorVo(final Integer code, final String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Get the error code
	 *
	 * @return The error code to push via JSON as part of the content
	 */
	@JsonProperty
	public Integer getCode() {
		return this.code;
	}

	/**
	 * Get the error message
	 *
	 * @return The error message to push via JSON as part of the content
	 */
	@JsonProperty
	public String getMessage() {
		return this.message;
	}
}
