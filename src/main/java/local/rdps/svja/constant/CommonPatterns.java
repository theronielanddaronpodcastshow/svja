package local.rdps.svja.constant;

import java.util.regex.Pattern;

/**
 * <p>
 * This class contains a number of common {@link Pattern}s so that others need not make them.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CommonPatterns {
	/**
	 * This Pattern matches on any '.' character (not to be confused with the wildcard character -- this pattern matches
	 * the period or dot character specifically).
	 *
	 * @since 1.0
	 */
	public static final Pattern DOT = Pattern.compile("\\.");

	/**
	 * This Pattern matches on the double quote (") character.
	 *
	 * @since 1.0
	 */
	public static final Pattern DOUBLE_QUOTE = Pattern.compile("\"");

	/**
	 * This Pattern matches greedily on one or more consecutive whitespaces ( <tab>\r\n)
	 *
	 * @since 1.0
	 */
	public static final Pattern ONE_OR_MORE_SPACES = Pattern.compile("\\s+");

	/**
	 * This Pattern matches on one whitespace character ( <tab>\r\n)
	 *
	 * @since 1.0
	 */
	public static final Pattern ONE_SPACE = Pattern.compile("\\s");

	/**
	 * This Pattern matches on the single quote (') character.
	 *
	 * @since 1.0
	 */
	public static final Pattern SINGLE_QUOTE = Pattern.compile("'");

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private CommonPatterns() {
		// Do nothing
	}
}
