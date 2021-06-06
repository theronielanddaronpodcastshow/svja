package local.rdps.svja.action.interceptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.json.JSONException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.Interceptor;

import local.rdps.svja.action.BaseAction;
import local.rdps.svja.action.RestAction;
import local.rdps.svja.constant.ErrorConstants;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.JsonErrorVo;

/**
 * <p>
 * This interceptor monitors and handles errors, turning them into a JSON response.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class JsonErrorInterceptor implements Interceptor {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 9156313660873257945L;

	/**
	 * Create a json error object from an exception
	 *
	 * @param e
	 * @return
	 */
	private static JsonErrorVo createJsonErrorObject(final Exception e) {
		return e instanceof ApplicationException
				? new JsonErrorVo(((ApplicationException) e).getExceptionStatusCode(),
						((ApplicationException) e).getUiMessage())
				: e instanceof ApplicationException
						? new JsonErrorVo(((ApplicationException) e).getExceptionStatusCode(), "")
						// could throw an ApplicationException here to wrap any non-ApplicationException
						: new JsonErrorVo(500, "");
	}

	/**
	 * Get the full stack trace as a string from the provided exception
	 * <p>
	 * This output is capped at 10000 characters for preventing DoS by flooding stack traces
	 *
	 * @param e
	 * @return
	 */
	private static String getFullStackTrace(final Throwable e) {
		if (ValidationUtils.isEmpty(e))
			return "";

		final StringWriter stringWriter = new StringWriter();
		try (final PrintWriter printWriter = new PrintWriter(stringWriter)) {
			e.printStackTrace(printWriter);
		}
		final String fullStack = stringWriter.toString();
		return fullStack.length() > 10000 ? fullStack.substring(0, 10000) : fullStack;
	}

	/**
	 * Log exception data
	 *
	 * @param e
	 */
	private static void logExceptionData(final ApplicationException e) {
		final String COLON = ": ";

		if (JsonErrorInterceptor.logger.isEnabled(e.getLogLevel())) {
			final String message = e.getExceptionStatusCode() + " (telling Struts: '" + e.getExceptionReturnString()
					+ "') - " + e.getExceptionTitle() + COLON + System.lineSeparator() + System.lineSeparator();
			// Check how much we log and for any other particulars
			switch (e.getStackTraceLogDirective()) {
				case ErrorConstants.DO_NOT_LOG_STACK_TRACE:
					// Log the exception, but not the stack trace
					JsonErrorInterceptor.logger.log(e.getLogLevel(), message + e);
					break;
				case ErrorConstants.LOG_LIMITED_STACK_TRACE:
					// Log the exception and a limited stack trace
					JsonErrorInterceptor.logger.log(e.getLogLevel(),
							message + '\n' + JsonErrorInterceptor.getFullStackTrace(e));
					break;
				case ErrorConstants.LOG_STACK_TRACE_AS_INFO:
					// Log the exception separately from the stack trace
					JsonErrorInterceptor.logger.log(e.getLogLevel(),
							message + '\n' + JsonErrorInterceptor.getFullStackTrace(e));
					if (JsonErrorInterceptor.logger.isInfoEnabled()) {
						JsonErrorInterceptor.logger.info(message, e);
					}
					break;
				default:
					// Make anything else an error and log the stack trace
					JsonErrorInterceptor.logger.error(message + '\n' + JsonErrorInterceptor.getFullStackTrace(e));
			}
		}
	}

	/**
	 * Process the exception: - log the exception - send an email about the exception if appropriate (code >= 500)
	 *
	 * @param action
	 * @param e
	 */
	private static void processException(final BaseAction action, final Exception e) {
		final ApplicationException error = e instanceof ApplicationException ? (ApplicationException) e
				: new ApplicationException(e.getMessage(), e);

		// log the exception
		JsonErrorInterceptor.logExceptionData(error);

		// setup error that displays on the front-end
		if (action instanceof RestAction) {
			final JsonErrorVo jsonError = JsonErrorInterceptor.createJsonErrorObject(error);
			((RestAction) action).setError(jsonError);
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init() {
		// Ensure that everything is in UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public String intercept(final ActionInvocation invocation) throws Exception {
		try {
			return invocation.invoke();
		} catch (final Exception e) {
			final ActionContext context = invocation.getInvocationContext();
			final HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);
			final ApplicationException error = e instanceof ApplicationException ? (ApplicationException) e
					// wrap json parsing exceptions as illegal parameter exceptions
					: (e instanceof JSONException) || (e instanceof NumberFormatException)
							? new IllegalParameterException(e.getMessage(), e)
							// wrap all other exceptions as an application exception
							: new ApplicationException(e.getMessage(), e);
			// add exception reference to the front-end
			invocation.getStack().push(new ExceptionHolder(error));

			JsonErrorInterceptor.processException((BaseAction) invocation.getAction(), error);

			// Set the HTTP response status code and return the exception return string
			response.setStatus(error.getExceptionStatusCode());
			return ResultConstants.RESULT_EXCEPTION;
		}
	}
}
