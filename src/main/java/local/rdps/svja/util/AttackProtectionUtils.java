package local.rdps.svja.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.w3c.tidy.Tidy;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * Contains various methods designed to help strain and validate input for injection and time-based attacks.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class AttackProtectionUtils {
	private static final Pattern DIGITS_AND_DECIMAL = Pattern.compile("[^\\d\\.]");
	private static final Pattern DOUBLE_QUOTATIONS = Pattern.compile("[\\u201c-\\u201f]");
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern NONALPHA_NUMERIC_PERIOD_HYPHEN = Pattern.compile("[^a-zA-Z0-9.\\-]");
	private static final Pattern NONALPHA_NUMERIC_SPACE_PERIOD_HYPHEN = Pattern.compile("[^\\sa-zA-Z0-9.\\-]");
	private static final Pattern NONVISIBLE_AND_NONWHITESPACE = Pattern.compile("([^\\p{Graph}\\p{Space}]|\\u000B)+",
			Pattern.UNICODE_CHARACTER_CLASS | Pattern.CANON_EQ);
	private static final Pattern SINGLE_QUOTATIONS = Pattern.compile("[`\\u2018-\\u201b]");
	/**
	 * A protection type to protect the general system by providing a basic rate-limit.
	 */
	public static final Byte PROTECTION_TYPE_GENERAL_RATE_LIMIT = Byte.valueOf((byte) 1);
	/**
	 * A protection type to protect the system against DoS attacks that make use of particularly heavy actions --
	 * computationally, memory, or in length of time to service.
	 */
	public static final Byte PROTECTION_TYPE_HEAVY_ACTIONS_DOS = Byte.valueOf((byte) 4);
	/**
	 * A protection type to protect the system against DoS attacks that make use of particularly light actions --
	 * computationally, memory, or in length of time to service.
	 */
	public static final Byte PROTECTION_TYPE_LIGHT_ACTIONS_DOS = Byte.valueOf((byte) 5);
	/**
	 * A protection type to protect against mailbombing users.
	 */
	public static final Byte PROTECTION_TYPE_MAILBOMB = Byte.valueOf((byte) 0);
	/**
	 * A protection type to protect against remote logging, time-based attacks.
	 */
	public static final Byte PROTECTION_TYPE_REMOTE_LOGGING_ATTACKS = Byte.valueOf((byte) 3);
	/**
	 * A protection type to protect against time-based session generation attacks.
	 */
	public static final Byte PROTECTION_TYPE_SESSION_GENERATION_ATTACKS = Byte.valueOf((byte) 2);

	/**
	 * Cleanse a file name of reserved characters, replacing them with an underscore
	 *
	 * @param input
	 *            The filename to clean
	 * @param allowSpaces
	 *            Whether or not to allow whitespace
	 * @return A clean filename or empty string
	 */
	public static @NotNull String cleanseFileName(final @NotNull String input, final boolean allowSpaces) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;

		// Replace invalid characters with underscores
		if (allowSpaces)
			return AttackProtectionUtils.NONALPHA_NUMERIC_SPACE_PERIOD_HYPHEN.matcher(input).replaceAll("_");
		return AttackProtectionUtils.NONALPHA_NUMERIC_PERIOD_HYPHEN.matcher(input).replaceAll("_");
	}

	/**
	 * This method cleans out strings, performing all required work to make text for Microsoft Office files 'safe', such
	 * that the resulting file doesn't have errors thanks to characters.
	 *
	 * @param input
	 *            The string to clean
	 * @return A String that can be input into Office
	 */
	public static String cleanseForOffice(final @NotNull CharSequence input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;

		return AttackProtectionUtils.SINGLE_QUOTATIONS.matcher(AttackProtectionUtils.DOUBLE_QUOTATIONS
				.matcher(AttackProtectionUtils.removeHTML(AttackProtectionUtils.removeControlCharacters(input)))
				.replaceAll("\"")).replaceAll("'");
	}

	/**
	 * This method should be used if not using DocX4J's API to build the contents of an Office document. If you are only
	 * using DocX4J, please just use cleanseForOffice or you will likely see &,<, and > getting all messed up in your
	 * document. This method cleans out strings, performing all required work to make text for Microsoft Office files
	 * 'safe', such that the resulting file doesn't have errors thanks to characters. This method also encodes the three
	 * big reserved characters in XML, &, <, and >, preventing any SAXParser errors. This method relies on two other
	 * methods -- cleanseForOffice and EncodingUtils.encodeReservedXml.
	 *
	 * @param input
	 *            The string to clean
	 * @return A String that can be input into Office
	 */
	public static @Nullable String cleanseForUnmarshalledOffice(final @NotNull CharSequence input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;

		return EncodingUtils.encodeReservedXml(AttackProtectionUtils.cleanseForOffice(input));
	}

	/**
	 * Removes any characters that aren't digits or period from a string
	 *
	 * @param str
	 *            String to clean
	 * @return A string free of anything that isn't a digit or period
	 */
	public static String cleanseNumberString(final @NotNull CharSequence str) {
		if (!ValidationUtils.isEmpty(str)) {
			final String number = AttackProtectionUtils.DIGITS_AND_DECIMAL.matcher(str).replaceAll("");
			if (!ValidationUtils.isEmpty(number))
				return number;
		}
		return "0";
	}

	/**
	 * Remove characters in the provided input that are outside of the visible (printable) characters in the UTF-8
	 * standard. This method doesn't 'pretty up' or 'tidy up' the input, simply remove all non-printable characters. It
	 * is important to note that prior to removing, the input is converted to UTF-8 -- this allows the straining out of
	 * malicious non-UTF-8 characters that might otherwise cause harm.
	 *
	 * @param input
	 *            The input to be cleaned
	 * @return A string wherein no non-printable special characters or invalid characters exist
	 */
	public static @NotNull String removeControlCharacters(final @NotNull CharSequence input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;

		final Matcher matcher = AttackProtectionUtils.NONVISIBLE_AND_NONWHITESPACE.matcher(input);
		if (AttackProtectionUtils.logger.isInfoEnabled()) {
			int start = 0;
			while (matcher.find(start)) {
				final char[] characters = matcher.group().toCharArray();
				final StringBuilder output = new StringBuilder(characters.length * 2);
				for (final char character : matcher.group().toCharArray()) {
					output.append('\\').append(Integer.toHexString(character));
				}
				AttackProtectionUtils.logger.info("Removed (characters in hex): {}", output);
				start = matcher.end();
			}
		}
		return matcher.replaceAll("");
	}

	/**
	 * Uses Jsoup tool to remove all HTML from a given string.
	 *
	 * @param htmlString
	 * @return A clean String with no HTML
	 */
	public static @NotNull String removeHTML(final @NotNull String htmlString) {
		return ValidationUtils.isEmpty(htmlString) ? "" : Jsoup.parse(htmlString).text();
	}

	/**
	 * Uses the JTidy tool to tidy up HTML (inserting missing tags, etc..)
	 *
	 * @param input
	 *            the input to fix up
	 * @return A string with 'good' HTML
	 */
	public static String tidyHtml(final @NotNull String input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;
		final Tidy tidy = new Tidy();
		tidy.setPrintBodyOnly(true);
		tidy.setShowWarnings(false);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		tidy.parse(new ByteArrayInputStream(input.getBytes()), os);
		return os.toString();
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private AttackProtectionUtils() {
		// Do nothing
	}
}
