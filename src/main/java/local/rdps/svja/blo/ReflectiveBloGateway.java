package local.rdps.svja.blo;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.vo.ReflectionVo;

/**
 * <p>
 * This class serves as the gateway for the {@link ReflectiveBlo}, giving all other packages access to the methods
 * contained therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0.1
 */
public class ReflectiveBloGateway {
	/**
	 * <p>
	 * This method uses the given VO to perform a reflective call.
	 * </p>
	 *
	 * @param action
	 *            The {@link ReflectionVo} instance containing the data that we need to perform our reflective call
	 * @return The results of the reflective call
	 */
	public static @Nullable Object reflectivelyCallMethod(final @NotNull ReflectionVo action) {
		if (Objects.isNull(action) || (action.isEmpty()))
			return CommonConstants.EMPTY_STRING;

		return ReflectiveBlo.reflectivelyCallMethod(action);
	}
}
