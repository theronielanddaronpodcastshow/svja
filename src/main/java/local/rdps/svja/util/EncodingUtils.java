package local.rdps.svja.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.unbescape.html.HtmlEscape;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.constant.CommonPatterns;

/**
 * <p>
 * This class contains the code required to Encode data or Decode data from one type to another. Note that none of these
 * are <em>conversions</em>, they are <em>encodings</em>.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class EncodingUtils {
	private static final Pattern AMPERSAND = Pattern.compile("&");
	private static final Pattern GREATER_THAN = Pattern.compile(">");
	private static final Pattern HTML_ENCODED_AT_SYMBOL = Pattern.compile("&#64;");
	private static final Pattern LESS_THAN = Pattern.compile("<");
	private static final Pattern LINE_TERMINATORS = Pattern.compile("[\\r\\n]");
	private static final Pattern NONVISIBLE_AND_NONWHITESPACE_CHARACTERS = Pattern
			.compile("([^\\p{Graph}\\p{Space}]|\\u000B|\\u0013)+", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CANON_EQ);
	private static final Pattern PERCENT = Pattern.compile("%");

	private static byte[] base64Pad(final byte... input) {
		final int toAdd = 4 - (input.length % 4);
		if (toAdd == 4)
			return input;

		final byte[] output = Arrays.copyOf(input, input.length + toAdd);
		final byte equalsByte = (byte) '=';
		for (int i = 1; i <= toAdd; i++) {
			output[output.length - i] = equalsByte;
		}

		return output;
	}

	private static String base64Pad(final String input) {
		final int toAdd = 4 - (input.length() % 4);
		switch (toAdd) {
			case 2:
				return input + "==";
			case 1:
				return input + "=";
			default:
				return input;
		}
	}

	/**
	 * Decodes a Base64 String into octets.
	 *
	 * <strong>Note:</strong> this method seamlessly handles data encoded in URL-safe or normal mode.
	 *
	 * @param string
	 *            String containing Base64 data
	 * @return Array containing decoded data.
	 */
	public static @NotNull byte[] base64Decode(final String string) {
		if (Objects.isNull(string) || Objects.equals(CommonConstants.EMPTY_STRING, string))
			return CommonConstants.EMPTY_BYTE_ARRAY;

		final byte[] decoded = Base64.decodeBase64(string);
		return Objects.isNull(decoded) ? CommonConstants.EMPTY_BYTE_ARRAY : decoded;
	}

	/**
	 * Produces an unchunked, Base64 encoded String of type UTF-8.
	 *
	 * @param input
	 *            Data to encode
	 * @return An unchunked, Base64 encoded UTF-8 String
	 */
	public static @NotNull String base64EncodeString(final byte... input) {
		if (Objects.isNull(input))
			return CommonConstants.EMPTY_STRING;

		return EncodingUtils
				.base64Pad(EncodingUtils.LINE_TERMINATORS.matcher(Base64.encodeBase64String(input)).replaceAll(""));
	}

	/**
	 * Encodes binary data using the base64 algorithm.
	 *
	 * @param input
	 *            Data to encode
	 * @return An array of Base64 encoded characters
	 */
	public static @NotNull byte[] base64EncodeUrlSafe(final byte... input) {
		if (Objects.isNull(input))
			return CommonConstants.EMPTY_BYTE_ARRAY;

		return EncodingUtils.base64Pad(Base64.encodeBase64URLSafe(input));
	}

	/**
	 * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the output. The
	 * url-safe variation emits - and _ instead of + and / characters.
	 *
	 * @param input
	 *            Data to encode
	 * @return String containing Base64 characters
	 */
	public static @NotNull String base64EncodeUrlSafeString(final byte... input) {
		if (Objects.isNull(input))
			return CommonConstants.EMPTY_STRING;

		return EncodingUtils.base64Pad(
				EncodingUtils.LINE_TERMINATORS.matcher(Base64.encodeBase64URLSafeString(input)).replaceAll(""));
	}

	/**
	 * <p>
	 * This method decodes HTML-encoded characters. It should not be used on strings that are presented in an HTML
	 * context as this will create HTML and XSS injection flaws.
	 * </p>
	 *
	 * @param string
	 *            The text to clean
	 * @return A String that will no longer contain HTML-encoded and HTML-escaped characters
	 */
	public static @NotNull String decodeHtml(final CharSequence string) {
		String temp = CommonConstants.EMPTY_STRING;
		if (Objects.nonNull(string) && !CommonConstants.EMPTY_STRING.contentEquals(string)) {
			temp = EncodingUtils.HTML_ENCODED_AT_SYMBOL.matcher(string).replaceAll("@");
			temp = HtmlEscape.unescapeHtml(temp);
		}

		return temp;
	}

	/**
	 * Escapes HTML characters in the provided input. This method doesn't 'pretty up' or 'tidy up' the input, simply
	 * escape all HTML special characters. It is important to note that prior to escaping, the input is run through our
	 * controlCharacter removal method -- this allows the straining out of malicious characters that might otherwise
	 * cause harm.
	 *
	 * @param input
	 *            The input to be escaped
	 * @return A string wherein no HTML special characters or invalid characters exist
	 */
	public static @NotNull String encodeReservedHtml(final CharSequence input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;

		final StringBuilder cleanStr = new StringBuilder(input.length());
		final char[] letters = AttackProtectionUtils.removeControlCharacters(input).toCharArray();
		for (final char letter : letters) {
			final int letterIntVal = (letter & 0xff);
			// Don't escape: \t\n\r\ !#$%()*+,\-./0-9:;=?@A-Z[]^_`a-z{|}~
			if (((letterIntVal >= 0x9) && (letterIntVal <= 0xd)) || ((letterIntVal >= 0x20) && (letterIntVal <= 0x21))
					|| ((letterIntVal >= 0x23) && (letterIntVal <= 0x25))
					|| ((letterIntVal >= 0x28) && (letterIntVal <= 0x3b)) || (letterIntVal == 0x3d)
					|| ((letterIntVal >= 0x3f) && (letterIntVal <= 0x5b))
					|| ((letterIntVal >= 0x5d) && (letterIntVal <= 0x7e))) {
				cleanStr.append(letter);
			}
			// Escape everything in here (we'll use HTML names for some of the special characters we are more
			// likely to see and numbers for the rest)
			else {
				switch (letterIntVal) {
					case 0x22:
						cleanStr.append("&quot;");
						break;
					case 0x26:
						cleanStr.append("&amp;");
						break;
					case 0x3c:
						cleanStr.append("&lt;");
						break;
					case 0x3e:
						cleanStr.append("&gt;");
						break;
					case 0xa9:
						cleanStr.append("&copy;");
						break;
					case 0xae:
						cleanStr.append("&reg;");
						break;
					// Use the HTML number because, while it likely has a name, it's not common and this gets
					// the same result
					default:
						cleanStr.append("&#x").append(Integer.toHexString(letterIntVal)).append(';');
						break;
				}
			}
		}

		return cleanStr.toString();
	}

	/**
	 * Cleans up a String for a JSON statement.
	 *
	 * @param string
	 *            The String to clean
	 * @return A String that won't break JSON
	 */
	public static @NotNull String encodeReservedJSON(final CharSequence string) {
		String temp = CommonConstants.EMPTY_STRING;
		if (Objects.nonNull(string) && ValidationUtils.not(CommonConstants.EMPTY_STRING.contentEquals(string))) {
			temp = EncodingUtils.LINE_TERMINATORS.matcher(string).replaceAll("");
			temp = EncodingUtils.NONVISIBLE_AND_NONWHITESPACE_CHARACTERS.matcher(temp).replaceAll("");

			// These get replaced with another character
			temp = temp.replace("\"", "'");

			// These get escaped
			temp = temp.replace("\\", "\\\\");
			temp = temp.replace("/", "\\/");
			temp = temp.replace("\t", "\\t");
			temp = temp.replace("\f", "\\f");
		}

		return temp;
	}

	/**
	 * Replaces any reserved XML characters
	 *
	 * @param string
	 *            The text to clean
	 * @return A String that won't break DocX4j or Microsoft Office files that use XML (the .*x extensions, like .docx
	 *         and .xlsx)
	 */
	public static @NotNull String encodeReservedXml(final CharSequence string) {
		String temp = CommonConstants.EMPTY_STRING;
		if (Objects.nonNull(string) && ValidationUtils.not(CommonConstants.EMPTY_STRING.contentEquals(string))) {
			temp = EncodingUtils.AMPERSAND.matcher(string).replaceAll("&amp;");
			temp = EncodingUtils.GREATER_THAN.matcher(temp).replaceAll("&gt;");
			temp = EncodingUtils.LESS_THAN.matcher(temp).replaceAll("&lt;");
			temp = EncodingUtils.PERCENT.matcher(temp).replaceAll("&#37;");
			temp = CommonPatterns.SINGLE_QUOTE.matcher(temp).replaceAll("&apos;");
			temp = CommonPatterns.DOUBLE_QUOTE.matcher(temp).replaceAll("&quot;");
		}

		return temp;
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private EncodingUtils() {
		// Do nothing
	}
}
