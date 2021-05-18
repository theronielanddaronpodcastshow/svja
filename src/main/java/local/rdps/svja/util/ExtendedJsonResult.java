package local.rdps.svja.util;

import org.apache.struts2.json.JSONResult;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * <p>
 * This lets us take error data and push it to the JSON response.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ExtendedJsonResult extends JSONResult {
	private static final long serialVersionUID = 1L;

	/**
	 * Set errorCode with support for OGNL expressions
	 *
	 * @param errorCode
	 */
	public void setErrorCode(final String errorCode) {
		final ValueStack stack = ActionContext.getContext().getValueStack();
		setErrorCode(Integer.parseInt(TextParseUtil.translateVariables(errorCode, stack)));
	}

	/**
	 * Set statusCode with support OGNL expressions
	 *
	 * @param statusCode
	 */
	public void setStatusCode(final String statusCode) {
		final ValueStack stack = ActionContext.getContext().getValueStack();
		setStatusCode(Integer.parseInt(TextParseUtil.translateVariables(statusCode, stack)));
	}

}
