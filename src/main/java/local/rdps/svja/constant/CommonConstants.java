package local.rdps.svja.constant;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This class houses the constants that are commonly used across the application.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CommonConstants {
	/**
	 * An empty, non-null {@code byte} array.
	 */
	@NotNull
	public static final byte[] EMPTY_BYTE_ARRAY = {};
	/**
	 * An empty, non-null {@link Object} array.
	 */
	@NotNull
	public static final Object[] EMPTY_OBJECT_ARRAY = {};
	/**
	 * An empty, non-null string.
	 */
	@NotNull
	public static final String EMPTY_STRING = "";
	/**
	 * An empty, non-null {@link String} array.
	 */
	@NotNull
	public static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * This is a constants class, so should never be instantiated.
	 */
	private CommonConstants() {
		// Do nothing
	}
}
