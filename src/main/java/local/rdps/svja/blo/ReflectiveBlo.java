package local.rdps.svja.blo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.vo.ReflectionVo;

/**
 * <p>
 * This class serves to provide reflective services to the caller.
 * </p>
 *
 * @author DaRon
 * @since 1.0.1
 */
class ReflectiveBlo {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * <p>
	 * This method uses the given VO to perform a reflective call.
	 * </p>
	 *
	 * @param action
	 *            The {@link ReflectionVo} instance containing the data that we need to perform our reflective call
	 * @return The results of the reflective call
	 */
	static @Nullable Object reflectivelyCallMethod(final @NotNull ReflectionVo action) {
		try {
			// Load the class
			if (ReflectiveBlo.logger.isInfoEnabled()) {
				ReflectiveBlo.logger.info("Loading {}...", action.getNamespace() + action.getClassName());
			}
			final Class<?> clazz = Class.forName(action.getNamespace() + action.getClassName());
			if (ReflectiveBlo.logger.isInfoEnabled()) {
				ReflectiveBlo.logger.info("{} loaded; finding method {}...", action.getNamespace() + clazz.getName(),
						action.getMethodName());
			}

			// If we have parameters, we need to suck those into the getDeclaredMethod call to ensure that we get the
			// right method
			final Method method;
			if (action.getParameters().isEmpty()) {
				method = clazz.getDeclaredMethod(action.getMethodName());
			} else {
				final Class<?>[] parameterClasses = new Class<?>[action.getParameters().size()];
				int i = 0;
				for (final Object obj : action.getParameters().values()) {
					parameterClasses[i++] = obj.getClass();
				}
				method = clazz.getDeclaredMethod(action.getMethodName(), parameterClasses);
			}
			if (ReflectiveBlo.logger.isInfoEnabled()) {
				ReflectiveBlo.logger.info("{} loaded; invoking method...", method.getName());
			}

			// Invoke the method
			return Modifier.isStatic(method.getModifiers())
					? method.invoke(null, action.getParameters().values().toArray())
					: method.invoke(clazz.getConstructor().newInstance(), action.getParameters().values().toArray());
		} catch (@NotNull final InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			ReflectiveBlo.logger.error("Could not find the class " + action.getClassName() + " with a method "
					+ action.getMethodName() + "("
					+ (action.getParameters().isEmpty() ? CommonConstants.EMPTY_STRING
							: String.join(",", action.getParameters().keySet()))
					+ ")... " + e.getMessage(), e);
		}

		return CommonConstants.EMPTY_STRING;
	}
}
