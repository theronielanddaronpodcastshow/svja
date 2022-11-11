package local.rdps.svja.blo;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.vo.SecretSquirrelVo;

/**
 * <p>
 * This class serves as the gateway for the {@link SuperSecretBlo}, giving all other packages access to the methods
 * contained therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0.2
 */
public class SuperSecretBloGateway {
	/**
	 * <p>
	 * This method uses the given VO to perform a call to the desired action in a cryptographically secure manner.
	 * </p>
	 *
	 * @param action
	 *            The {@link SecretSquirrelVo} instance containing the data that we need to perform our action
	 * @return The results of the action call
	 */
	public static @Nullable Object secretelyCallAction(final @NotNull SecretSquirrelVo action) {
		if (Objects.isNull(action) || (action.isEmpty()))
			return CommonConstants.EMPTY_STRING;

		return SuperSecretBlo.secretelyCallAction(action);
	}
}
