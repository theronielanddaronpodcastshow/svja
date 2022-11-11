package local.rdps.svja.blo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.JSONException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.action.RestAction;
import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.NotFoundException;
import local.rdps.svja.util.BetterJsonWriter;
import local.rdps.svja.util.ConversionUtils;
import local.rdps.svja.util.NetworkTrafficEncrypter;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.SecretSquirrelVo;

/**
 * <p>
 * This class serves to provide a cryptographically secure means of calling to actions, allowing front-ends and clients
 * to call protect their user's data.
 * </p>
 *
 * @author DaRon
 * @since 1.0.2
 */
class SuperSecretBlo {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Determine the REST method to call depending on whether the ID for the given action has been set and what HTTP
	 * method was used.
	 *
	 * @param action
	 * @return
	 * @throws NotFoundException
	 */
	private static RestAction.REQUEST_METHOD determineRestMethod(final String restfulAction, final boolean isIdSet)
			throws NotFoundException {
		switch (restfulAction.toUpperCase(Locale.ROOT)) {
			case RestAction.GET:
				if (isIdSet)
					return RestAction.REQUEST_METHOD.SHOW;
				return RestAction.REQUEST_METHOD.INDEX;
			case RestAction.POST:
				if (isIdSet)
					return RestAction.REQUEST_METHOD.UPDATE;
				return RestAction.REQUEST_METHOD.CREATE;
			case RestAction.PUT:
				return RestAction.REQUEST_METHOD.UPDATE;
			case RestAction.PATCH:
				return RestAction.REQUEST_METHOD.PATCH;
			case RestAction.DELETE:
				return RestAction.REQUEST_METHOD.DESTROY;
			default:
				// API Call Not Found
				throw new NotFoundException(
						restfulAction.toUpperCase(Locale.ROOT) + " http method does not exist.");
		}
	}

	/**
	 * <p>
	 * This method uses the given VO to perform a call to the desired action in a cryptographically secure manner.
	 * </p>
	 *
	 * @param action
	 *            The {@link SecretSquirrelVo} instance containing the data that we need to perform our action
	 * @return The results of the action call
	 */
	static @Nullable Object secretelyCallAction(final @NotNull SecretSquirrelVo action) {
		// Find and load the class
		String actionName;
		try {
			actionName = NetworkTrafficEncrypter.decrypt(action.getActionName());
		} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | NoSuchProviderException e) {
			SuperSecretBlo.logger.error(e.getMessage(), e);
			return e.getMessage();
		}
		// Make certain that we have an action name
		if (ValidationUtils.isEmpty(actionName))
			return CommonConstants.EMPTY_STRING;
		Class<?> clazz;
		try {
			clazz = Class.forName("local.rdps.svja.action." + actionName);
		} catch (final ClassNotFoundException e) {
			SuperSecretBlo.logger.error("Could not find the class local.rdps.svja.action." + action.getActionName()
					+ "... " + e.getMessage(), e);
			return CommonConstants.EMPTY_STRING;
		}

		// Create an instance of the action
		RestAction restAction;
		try {
			restAction = (RestAction) clazz.getConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException
				| NoSuchMethodException e) {
			SuperSecretBlo.logger.error("Could not create a new instance of the class local.rdps.svja.action."
					+ action.getActionName() + "... " + e.getMessage(), e);
			return CommonConstants.EMPTY_STRING;
		}

		// If we have parameters we need to call the setters, thereby stuffing the action
		if (ValidationUtils.not(action.getParameters().isEmpty())) {
			final Method[] methods = clazz.getMethods();

			for (final Map.Entry<String, String> item : action.getParameters().entrySet()) {
				if (ValidationUtils.isEmpty(item.getKey()) || ValidationUtils.isEmpty(item.getValue())) {
					continue;
				}

				try {
					final String parameterName = NetworkTrafficEncrypter.decrypt(item.getKey());
					final String parameterValue = NetworkTrafficEncrypter.decrypt(item.getValue());
					// If we actually have data, let's do something with it
					if (ValidationUtils.not(ValidationUtils.isEmpty(parameterName))
							&& ValidationUtils.not(ValidationUtils.isEmpty(parameterValue))) {
						for (final Method method : methods) {
							if (method.getName().equalsIgnoreCase("set" + parameterName)) {
								final Parameter[] parameters = method.getParameters();

								// If we found a method that seems to match and we can coerce the data to meet the
								// method parameter, invoke it
								if ((parameters.length == 1) && Objects
										.nonNull(ConversionUtils.as(parameters[0].getType(), parameterValue))) {
									try {
										method.invoke(restAction,
												ConversionUtils.as(parameters[0].getType(), parameterValue));
										continue;
									} catch (IllegalAccessException
											| InvocationTargetException invocationTargetException) {
										// We just didn't find the right method, so let's silently move on
									}
								}
							}
						}
					}
				} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException
						| NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
					// Silently gobble it up
				}
			}
		}

		// Set the request method
		String restfulMethod;
		try {
			restfulMethod = NetworkTrafficEncrypter.decrypt(action.getRestfulMethod());
			if (ValidationUtils.isEmpty(restfulMethod)) {
				restfulMethod = RestAction.GET;
			} else {
				restfulMethod = restfulMethod.toUpperCase(Locale.ROOT);
			}
		} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | NoSuchProviderException e) {
			return e.getMessage();
		}
		try {
			restAction.setRequestMethod(SuperSecretBlo.determineRestMethod(restfulMethod, restAction.isIdSet()));
		} catch (final NotFoundException e) {
			SuperSecretBlo.logger.error(e.getMessage(), e);
			try {
				return NetworkTrafficEncrypter.encrypt(e.getMessage().getBytes(StandardCharsets.UTF_8));
			} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | NoSuchProviderException ex) {
				SuperSecretBlo.logger.error(ex.getMessage(), ex);
				return "There was an error";
			}
		}

		// Execute the action
		try {
			restAction.execute();
		} catch (ApplicationException | IOException e) {
			SuperSecretBlo.logger.error(e.getMessage(), e);
			try {
				return NetworkTrafficEncrypter.encrypt(e.getMessage().getBytes(StandardCharsets.UTF_8));
			} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | NoSuchProviderException ex) {
				SuperSecretBlo.logger.error(ex.getMessage(), ex);
				return "There was an error";
			}
		}

		// Serialise the rest action, as would normally happen
		final BetterJsonWriter serialiser = new BetterJsonWriter();
		final String serialisedAction;
		try {
			serialisedAction = serialiser.write(restAction);
		} catch (final JSONException e) {
			SuperSecretBlo.logger.error(e.getMessage(), e);
			try {
				return NetworkTrafficEncrypter.encrypt(e.getMessage().getBytes(StandardCharsets.UTF_8));
			} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | NoSuchProviderException ex) {
				SuperSecretBlo.logger.error(ex.getMessage(), ex);
				return "There was an error";
			}
		}

		// Return the serialised, encrypted rest action data
		if (ValidationUtils.not(ValidationUtils.isEmpty(serialisedAction))) {
			try {
				return NetworkTrafficEncrypter.encrypt(serialisedAction.getBytes(StandardCharsets.UTF_8));
			} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | NoSuchProviderException e) {
				SuperSecretBlo.logger.error(e.getMessage(), e);
				try {
					return NetworkTrafficEncrypter.encrypt(e.getMessage().getBytes(StandardCharsets.UTF_8));
				} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException
						| NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException ex) {
					SuperSecretBlo.logger.error(ex.getMessage(), ex);
					return "There was an error";
				}
			}
		}
		return CommonConstants.EMPTY_STRING;
	}
}
