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
	 * @param message
	 */
	public JsonErrorVo(final Integer code, final String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Get the error code
	 *
	 * @return
	 */
	@JsonProperty
	public Integer getCode() {
		return this.code;
	}

	/**
	 * Get the error message
	 *
	 * @return
	 */
	@JsonProperty
	public String getMessage() {
		return this.message;
	}
}
