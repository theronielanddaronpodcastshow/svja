package local.rdps.svja.action;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.NotFoundException;
import local.rdps.svja.exception.UnauthorizedAccessException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.JsonErrorVo;

/**
 * <p>
 * This class defines and provides key implementations tied to being a proper RESTful application.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public abstract class RestAction extends BaseAction {
	/**
	 * <p>
	 * The REQUEST_METHOD enum defines the method that was requested for this action.
	 * </p>
	 * <p>
	 * INDEX - GET (if isIdSet is false) CREATE - POST UPDATE - POST/PUT PATCH - PATCH DESTROY - DELETE SHOW - GET (if
	 * isIdSet is true)
	 * </p>
	 */
	public enum REQUEST_METHOD {
		CREATE, DESTROY, INDEX, PATCH, SHOW, UPDATE
	}

	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	public static final String DELETE = "DELETE";
	public static final String GET = "GET";
	public static final String PATCH = "PATCH";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	private JsonErrorVo error;
	private String nonce;
	private REQUEST_METHOD requestMethod = REQUEST_METHOD.INDEX;

	/**
	 * Create a new object
	 *
	 * @return
	 * @throws ApplicationException
	 * @throws IOException
	 */
	public String create() throws ApplicationException, IOException {
		RestAction.logger.error(" create() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("create() not defined for action.");
	}

	/**
	 * Delete an existing object
	 *
	 * @return
	 * @throws ApplicationException
	 */
	public String destroy() throws ApplicationException {
		RestAction.logger.error(" destroy() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("destroy() not defined for action.");
	}

	/**
	 * The RestAction class maps the appropriate action based on the HTTP method.
	 * <p>
	 * GET /movie index POST /movie create NOW POST PUT /movie/Thrillers update id="Thrillers" DELETE /movie/Thrillers
	 * destroy id="Thrillers" GET /movie/Thrillers show id="Thrillers"
	 *
	 * @throws IOException
	 */
	@Override
	public String execute() throws ApplicationException, IOException {
		if (ValidationUtils.isEmpty(getRequestMethod()))
			throw new NotFoundException("REST method not set by the apiInterceptor.");
		switch (getRequestMethod()) {
			case CREATE:
				if (mayCreate())
					return create();
				throw new UnauthorizedAccessException("The user does not have the right to create this object");
			case DESTROY:
				if (mayDestroy())
					return destroy();
				throw new UnauthorizedAccessException("The user does not have the right to destroy this object");
			case INDEX:
				if (mayIndex())
					return index();
				throw new UnauthorizedAccessException("The user does not have the right to index on this object");
			case SHOW:
				if (mayShow())
					return show();
				throw new UnauthorizedAccessException("The user does not have the right to show on this object");
			case UPDATE:
				if (mayUpdate())
					return update();
				throw new UnauthorizedAccessException(
						"The user does not have the right to update this object or the object state does not allow for it");
			case PATCH:
				if (mayPatch())
					return patch();
				throw new UnauthorizedAccessException(
						"The user does not have the right to patch this object or the object state does not allow for it");
			default:
				throw new NotFoundException("REST method not set by the apiInterceptor.");
		}
	}

	/**
	 * Get the json error
	 *
	 * @return
	 */
	@JsonProperty
	public JsonErrorVo getError() {
		return this.error;
	}

	/**
	 * Get the nonce
	 *
	 * @return
	 */
	@NotNull
	@JsonProperty
	public String getNonce() {
		return this.nonce;
	}

	/**
	 * Utility method used to extract JSON key names from a request root node.
	 *
	 * @param rootNodeName
	 *            The name of the root node. Will attempt to match regardless of case.
	 * @return
	 */
	public LinkedList<String> getRequestKeysForRootNode(final String rootNodeName) {
		if (ValidationUtils.isEmpty(rootNodeName))
			return new LinkedList<>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode body = mapper.readTree(this.request.getReader());
			final LinkedList<String> keys = new LinkedList<>();
			if (!ValidationUtils.isEmpty(body) && !ValidationUtils.isEmpty(body.fieldNames())) {
				String rootKey = "";
				final Iterator<String> iterator = body.fieldNames();
				while (iterator.hasNext()) {
					final String fieldName = iterator.next();
					if (rootNodeName.equalsIgnoreCase(fieldName)) {
						rootKey = fieldName;
					}
				}
				final JsonNode rootObject = body.get(rootKey);
				if (!ValidationUtils.isEmpty(rootObject) && !ValidationUtils.isEmpty(rootObject.fieldNames())) {
					rootObject.fieldNames().forEachRemaining(keys::add);
				}
			}
			return keys;
		} catch (final IOException e) {
			RestAction.logger.error("Error processing request body to get keys for root node. ", e);
			return new LinkedList<>();
		}
	}

	/**
	 * Get the request method
	 *
	 * @return
	 */
	public REQUEST_METHOD getRequestMethod() {
		return this.requestMethod;
	}

	/**
	 * Index the objects
	 *
	 * @return
	 * @throws ApplicationException
	 */
	public String index() throws ApplicationException {
		RestAction.logger.error(" index() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("index() not defined for action.");
	}

	/**
	 * All RestAction children must define the logic to determine if one or more IDs have been set in order to determine
	 * the difference between show() and index()
	 *
	 * @return
	 */
	public abstract boolean isIdSet();

	/**
	 * Patch an existing object
	 *
	 * @return
	 * @throws ApplicationException
	 */
	public String patch() throws ApplicationException {
		RestAction.logger.error(" patch() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("patch() not defined for action.");
	}

	/**
	 * Set the json error
	 *
	 * @param error
	 */
	public void setError(final JsonErrorVo error) {
		this.error = error;
	}

	/**
	 * Set the nonce
	 *
	 * @param nonce
	 */
	public void setNonce(final String nonce) {
		this.nonce = nonce;
	}

	/**
	 * Set the request method
	 *
	 * @param requestMethod
	 */
	public void setRequestMethod(final REQUEST_METHOD requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * Show an existing object
	 *
	 * @return
	 * @throws ApplicationException
	 */
	public String show() throws ApplicationException {
		RestAction.logger.error(" show() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("show() not defined for action.");
	}

	/**
	 * Update an existing object
	 *
	 * @return
	 * @throws ApplicationException
	 */
	public String update() throws ApplicationException {
		RestAction.logger.error(" update() - " + ServletActionContext.getRequest().getMethod());
		throw new NotFoundException("update() not defined for action.");
	}
}
