package local.rdps.svja.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.constant.FileConstants;
import local.rdps.svja.constant.SessionConstants;

/**
 * <p>
 * Provides methods to perform input validation and boundary validation.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ValidationUtils {
	/**
	 * <p>
	 * This class serves to store the magic numbers associated with a file type (e.g. exe, jpg, gif) and performs
	 * validation services against a byte array using those magic numbers.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	private static class Type {
		private static final @NotNull Collection<byte[]> emptyCollection = Collections.emptyList();
		private static final @NotNull Map<byte[], Integer> emptyMap = Collections.emptyMap();
		private final @NotNull Collection<byte[]> headers;
		private final @NotNull Map<byte[], Integer> subheaders;
		private final @NotNull Collection<byte[]> trailers;

		/**
		 * <p>
		 * This method checks to see if the needle is present in the haystack at byte 0.
		 * </p>
		 *
		 * @param needle
		 *            The set of bytes we expect to find at 0 in the haystack
		 * @param haystack
		 *            The file data
		 * @return {@code true} iff the needle is in the haystack at byte 0
		 */
		private static boolean bytesMatch(final byte[] needle, final byte[] haystack) {
			return ValidationUtils.Type.bytesMatch(needle, haystack, 0);
		}

		/**
		 * <p>
		 * This method checks to see if the needle is present in the haystack at the specified location.
		 * </p>
		 *
		 * @param needle
		 *            The set of bytes we expect to find at the specified offset
		 * @param haystack
		 *            The file data
		 * @param haystackOffset
		 *            The start point where we expect the needle to begin
		 * @return {@code true} iff the needle is in the haystack at the specified location
		 */
		private static boolean bytesMatch(final @Nullable byte[] needle, final @Nullable byte[] haystack,
				final int haystackOffset) {
			if (Objects.isNull(needle) || Objects.isNull(haystack)
					|| (haystack.length < (haystackOffset + needle.length)))
				return false;

			for (int i = 0, j = haystackOffset; i < needle.length; i += 1, j += 1) {
				if (needle[i] != haystack[j])
					return false;
			}
			return true;
		}

		/**
		 * <p>
		 * Creates a new Type that will have to contain one of the specified headers.
		 * </p>
		 *
		 * @param headers
		 *            The possible headers that the type may have
		 */
		Type(final @Nullable byte[][] headers) {
			this(headers, null, null);
		}

		/**
		 * <p>
		 * Creates a new Type that will have to contain one of the specified headers and one of the subheaders.
		 * </p>
		 *
		 * @param headers
		 *            The possible headers that the type may have
		 * @param offsets
		 *            An array (of equal length to subheaders) of byte offsets from which to begin the search
		 * @param subheadersArray
		 *            An array of each subheader (this is an OR, not an AND) that may be seen
		 */
		Type(final @Nullable byte[][] headers, final @Nullable int[] offsets,
				final @Nullable byte[][] subheadersArray) {
			this(headers, offsets, subheadersArray, null);
		}

		/**
		 * <p>
		 * Creates a new Type that will have to contain one of the specified headers and one of the subheaders.
		 * </p>
		 *
		 * @param headers
		 *            The possible headers that the type may have
		 * @param offsets
		 *            An array (of equal length to subheaders) of byte offsets from which to begin the search
		 */
		Type(final @Nullable byte[][] headers, final @Nullable int[] offsets, final @Nullable byte[][] subheaders,
				final @Nullable byte[][] trailers) {
			// Set the headers
			if (Objects.nonNull(headers)) {
				final Collection<byte[]> newList = new ArrayList<>(headers.length);
				for (final byte[] header : headers) {
					if (Objects.nonNull(header)) {
						newList.add(header);
					}
				}

				this.headers = Collections.unmodifiableCollection(newList);
			} else {
				this.headers = ValidationUtils.Type.emptyCollection;
			}

			// Set the subheaders
			if (Objects.isNull(offsets) || Objects.isNull(subheaders) || (offsets.length != subheaders.length)) {
				this.subheaders = ValidationUtils.Type.emptyMap;
			} else {
				final Map<byte[], Integer> newMap = new HashMap<>(offsets.length);
				for (int i = 0; i < offsets.length; i++) {
					if (Objects.nonNull(subheaders[i])) {
						newMap.put(subheaders[i], Integer.valueOf(offsets[i]));
					}
				}
				this.subheaders = Collections.unmodifiableMap(newMap);
			}

			// Set the trailers
			if (Objects.nonNull(trailers)) {
				final Collection<byte[]> newList = new ArrayList<>(trailers.length);
				for (final byte[] trailer : trailers) {
					if (Objects.nonNull(trailer)) {
						newList.add(trailer);
					}
				}

				this.trailers = Collections.unmodifiableCollection(newList);
			} else {
				this.trailers = ValidationUtils.Type.emptyCollection;
			}
		}

		/**
		 * <p>
		 * This method ensures that the file contains a required subheader.
		 * </p>
		 *
		 * @param file
		 *            The file data
		 * @return {@code true} iff the file contains a valid subheader
		 */
		private boolean validateSubHeaders(final byte... file) {
			if (this.subheaders.isEmpty())
				return true;

			for (final Map.Entry<byte[], Integer> item : this.subheaders.entrySet()) {
				final Integer value = item.getValue();
				if ((Objects.nonNull(value)) && ValidationUtils.Type.bytesMatch(item.getKey(), file, value.intValue()))
					return true;
			}

			return false;
		}

		/**
		 * <p>
		 * This method ensures that the file contains a valid trailer.
		 * </p>
		 *
		 * @param file
		 *            The file data
		 * @return {@code true} iff the file contains a trailer
		 */
		private boolean validateTrailers(final @NotNull byte... file) {
			if (this.trailers.isEmpty())
				return true;

			for (final byte[] trailer : this.trailers) {
				if ((Objects.nonNull(trailer)) && (trailer.length > 0)
						&& ValidationUtils.Type.bytesMatch(trailer, file, file.length - trailer.length))
					return true;
			}

			return false;
		}

		/**
		 * <p>
		 * Verifies that the file contains the required magic numbers (and in the right locations).
		 * </p>
		 *
		 * @param file
		 *            The file data
		 * @return {@code true} iff the array contains the required magic numbers <em>in the right locations</em>
		 */
		public boolean validate(final @Nullable byte... file) {
			if (Objects.nonNull(file)) {
				if (this.headers.isEmpty()) {
					// We have no headers, so make sure the file is ASCII
					for (final byte data : file) {
						if ((data > (byte) 0x7E) || (data < (byte) 0x09)
								|| ((data > (byte) 0x0D) && (data < (byte) 0x20)))
							return false;
					}
					return true;
				}

				for (final byte[] header : this.headers) {
					if (ValidationUtils.Type.bytesMatch(header, file) && validateSubHeaders(file)
							&& validateTrailers(file))
						return true;
				}
			}

			return false;
		}
	}

	private static final String A_PARTIAL_REG_EX_ALPHA_NUMERIC_ID = "(?=[a-zA-Z0-9/_=+-]{20,200}$)([a-zA-Z0-9]+[/_=+-])*[a-zA-Z0-9]+";
	private static final String A_PARTIAL_REG_EX_BASE_64 = "([a-zA-Z0-9/_+-]{4})*([a-zA-Z0-9/_+-]{4}|[a-zA-Z0-9/_+-]{2}(==|[.]{2})|[a-zA-Z0-9/_+-]{3}[=.]){1}";
	private static final String A_PARTIAL_REG_EX_DATE_ALPHA_MONTHS = "(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|Jun(e)?|Jul(y)?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?)";
	private static final String A_PARTIAL_REG_EX_HOST = "([a-zA-Z0-9][a-zA-Z0-9-]{0,62}[.]){0,62}[a-zA-Z0-9][a-zA-Z0-9-]{0,62}";
	private static final String A_PARTIAL_REG_EX_IDENTIFIER = "unknown|_[a-zA-Z]+|(("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4 + "|" + ValidationUtils.A_PARTIAL_REG_EX_HOST + ")" + "(:"
			+ ValidationUtils.A_PARTIAL_REG_EX_PORT + ")?)|" + "(\"\\[" + ValidationUtils.A_PARTIAL_REG_EX_IPV_6
			+ "\\](:" + ValidationUtils.A_PARTIAL_REG_EX_PORT + ")?\")";
	private static final String A_PARTIAL_REG_EX_IPV_4 = "((1?\\d{1,2}|2([0-4]\\d|5[0-5]{1}))[.]){3}(1?\\d{1,2}|2([0-4]\\d|5[0-5]{1}))";
	private static final String A_PARTIAL_REG_EX_IPV_6 = "((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4 + ")|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4
			+ ")|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4
			+ "))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4
			+ "))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4
			+ "))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4
			+ "))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4 + "))|:)))(%.+)?";
	private static final String A_PARTIAL_REG_EX_PORT = "(\\d{1,4}|([1-5]\\d{4}|6([0-4]\\d{3}|5([0-4]\\d{2}|5([0-2]\\d|3[0-5])))))";
	private static final String A_PARTIAL_REG_EX_QUERY_STRING = "(/([a-zA-Z0-9_-]+([.][a-zA-Z0-9._-]+)?/?)*(\\?(([a-zA-Z_-]+([.][a-zA-Z_\\\\\\-]+)*=[.a-zA-Z0-9_%,\\\\\\+-]*)(&[a-zA-Z_-]+([.][a-zA-Z_\\\\\\-]+)*=[.a-zA-Z0-9_%,\\\\\\+-]*)*)?)?(#[a-zA-Z0-9_-]*)?)?";
	private static final String A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP = "(("
			+ ValidationUtils.A_PARTIAL_REG_EX_IPV_4 + ")|(" + ValidationUtils.A_PARTIAL_REG_EX_IPV_6 + ")|("
			+ ValidationUtils.A_PARTIAL_REG_EX_HOST + "))";
	private static final String A_PARTIAL_REG_EX_WEB_TOKEN = "[a-zA-Z0-9-_=]+\\.[a-zA-Z0-9-_=]+\\.?[a-zA-Z0-9-_.+/=]+";
	private static final String A_PARTIAL_REG_EX_YEAR = "(19|20|21)\\d{2}";
	private static final Pattern ALL_DIGITS = Pattern.compile("^[+-]?\\d+([.]\\d+)?$");
	private static final Pattern ALPHA_NUMERIC_ID = Pattern
			.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_ALPHA_NUMERIC_ID + '$');
	private static final Pattern ALPHA_NUMERIC_UNDERSCORE = Pattern.compile("^[a-zA-Z0-9_]{1,255}$");
	private static final Pattern BASE_64 = Pattern.compile("^" + ValidationUtils.A_PARTIAL_REG_EX_BASE_64 + "$");
	private static final Pattern BASE_64_ALLOW_SPACES = Pattern.compile(
			"^([a-zA-Z0-9/_ +-]{4})*([a-zA-Z0-9/_ +-]{4}|[a-zA-Z0-9/_ +-]{2}(==|[.]{2})|[a-zA-Z0-9/_ +-]{3}[=.]){1}$");
	/**
	 * A <em>bearer token</em>, replete with the Bearer header
	 */
	private static final Pattern BEARER_WEB_TOKEN = Pattern
			.compile("^([Bb]earer )?" + ValidationUtils.A_PARTIAL_REG_EX_WEB_TOKEN + "$");
	private static final Pattern COMMA = Pattern.compile(",");
	private static final Pattern DATE_MMDDYYYY = Pattern
			.compile("^(1[0-2]|0?[1-9])/([0-2]?\\d|3[0-1])/" + ValidationUtils.A_PARTIAL_REG_EX_YEAR + '$');
	private static final Pattern DATE_MMM_DDYYYY = Pattern
			.compile(
					'^' + ValidationUtils.A_PARTIAL_REG_EX_DATE_ALPHA_MONTHS + " ([0-2]?\\d|3[0-1])[,]? "
							+ ValidationUtils.A_PARTIAL_REG_EX_YEAR + '$',
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern DATE_MMM_YYYY = Pattern
			.compile(
					'^' + ValidationUtils.A_PARTIAL_REG_EX_DATE_ALPHA_MONTHS + "[,]? "
							+ ValidationUtils.A_PARTIAL_REG_EX_YEAR + '$',
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern DATE_MMYYYY = Pattern
			.compile("^(1[0-2]|0?[1-9])[/ -]" + ValidationUtils.A_PARTIAL_REG_EX_YEAR + '$');
	private static final Pattern DATE_MONTH_MONTH_NUMERIC = Pattern.compile("^([0]?[1-9]{1}|1[0-2]{1})$");
	private static final Pattern DATE_YEAR = Pattern.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_YEAR + '$');
	private static final Pattern DATE_YYYY_MM_DD = Pattern
			.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_YEAR + "\\-(1[0-2]|0?[1-9])\\-([0-2]?\\d|3[0-1])" + '$');
	private static final Pattern DIGITS_AND_PERIODS = Pattern.compile("^(\\d+[.])*\\d+$");
	private static final Pattern EMAIL = Pattern.compile(
			"^[\\w-]+([.][\\w-]+)*@([a-zA-Z0-9-]+([.][a-zA-Z0-9-]+)*?[.][a-zA-Z]{2,6}|(\\d{1,3}[.]){3}\\d{1,3})(:\\d{4})?$");
	private static final Pattern FILE_NAME = Pattern.compile(
			"[^\\u0020-\\u0021\\u0023-\\u0025\\u002d-\\u002e\\u0030-\\u0039\\u0041-\\u005a\\u005f\\u0061-\\u007a]");
	/**
	 * This Map stores all known file extensions and the corresponding Type so that comparisons, verifications, and
	 * validations can be made quickly.
	 */
	private static final Map<String, ValidationUtils.Type> FILE_TYPES = ValidationUtils.getFileTypesMap();
	private static final Set<ValidationUtils.Type> FILE_TYPES_SET = ValidationUtils.getFileTypesSet();
	private static final Pattern HEX_STRING_OF_BYTES = Pattern.compile("^([0-9a-fA-F][0-9a-fA-F])+$");
	private static final Pattern HTML_TAGS = Pattern.compile("<[a-zA-Z]+[^>]*>");
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_SEARCH_STRING_LENGTH = 255;
	private static final Pattern NUMBER = Pattern.compile("^[+-]?\\d*$");
	private static final Pattern NUMBER_DEC = Pattern.compile("^[+-]?\\d*[.]?\\d*$");
	private static final Pattern ONLY_VISIBLE_AND_WHITESPACE_CHARACTERS = Pattern.compile("^[\\p{Graph}\\p{Space}]*$",
			Pattern.UNICODE_CHARACTER_CLASS | Pattern.CANON_EQ);
	private static final Pattern PORT = Pattern.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_PORT + '$');
	private static final Pattern POSITIVE_NUMBER = Pattern.compile("^[1-9]\\d*$");
	private static final Pattern POSTGRES_BIGINT = Pattern.compile(
			"^[\\+]?[0]*(\\d{1,18}|[1-8]\\d{18}|9([01]\\d{17}|2([01]\\d{16}|2([0-2]\\d{15}|3([0-2]\\d{14}|3([0-6]\\d{13}|7([01]\\d{12}|20([0-2]\\d{10}|3([0-5]\\d{9}|6([0-7]\\d{8}|8([0-4]\\d{7}|5([0-3]\\d{6}|4([0-6]\\d{5}|7([0-6]\\d{4}|7([0-4]\\d{3}|5([0-7]\\d{2}|80[0-7]))))))))))))))))$");
	private static final Pattern REQUEST_HEADER_FORWARDED = Pattern
			.compile("[Pp][Rr][Oo][Tt][Oo]=(http|https)|[Hh][Oo][Ss][Tt]="
					+ ValidationUtils.A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP + "|([Ff][Oo][Rr]|[Bb][Yy])="
					+ ValidationUtils.A_PARTIAL_REG_EX_IDENTIFIER);
	private static final Pattern REQUEST_HEADER_VIA = Pattern
			.compile("^([a-zA-Z]+\\/)?[0-9]+([.][0-9]{1,10}) ([a-zA-Z]+|"
					+ ValidationUtils.A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP + "(:" + ValidationUtils.PORT
					+ ")?)$");
	private static final Pattern RFC_COMPLIANT_IP_AND_HOST = Pattern
			.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP + '$');
	private static final Pattern RFC_COMPLIANT_IP_AND_HOST_AND_PORT = Pattern
			.compile('^' + ValidationUtils.A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP + "(:"
					+ ValidationUtils.A_PARTIAL_REG_EX_PORT + ")?$");
	private static final Pattern SEARCH_STRING = Pattern.compile("[^\\p{Graph}\\p{Space}]+",
			Pattern.UNICODE_CHARACTER_CLASS | Pattern.CANON_EQ);
	private static final Pattern THIRTY_TWO_BIT_HEX = Pattern.compile("^[a-fA-F0-9]{8}$");
	private static final Pattern URL_ENCODED = Pattern.compile("^(?:[a-zA-Z0-9_.*+-]|[%][a-fA-F0-9]{2})*$");
	private static final Pattern URL_GENERAL = Pattern
			.compile("^[hH][tT][tT][pP][sS]?://" + ValidationUtils.A_PARTIAL_REG_EX_RFC_COMPLIANT_HOST_AND_IP
					+ "(:(80|443|8080))?" + ValidationUtils.A_PARTIAL_REG_EX_QUERY_STRING + '$');
	private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.]{1,25}$");
	private static final Pattern WEB_TOKEN = Pattern.compile("^" + ValidationUtils.A_PARTIAL_REG_EX_WEB_TOKEN + "$");
	private static final Pattern WHITESPACE_OR_EMPTY = Pattern.compile("^[\\p{Space}]*$");

	private static Map<String, ValidationUtils.Type> getFileTypesMap() {
		if (!ValidationUtils.isEmpty(ValidationUtils.FILE_TYPES))
			return ValidationUtils.FILE_TYPES;

		// TODO Eventually add in trailer verifications, but that more makes sure that the whole file was
		// uploaded than anything
		final Map<String, ValidationUtils.Type> map = new HashMap<>();
		// Let's put in the files with no headers -- these are txt-based files
		map.put("txt", null);
		map.put("csv", null);

		// EXTENSION_JPG
		byte[][] headers = new byte[6][];
		headers[0] = ConversionUtils.hexStringToByteArray("FFD8FFE0");
		headers[1] = ConversionUtils.hexStringToByteArray("FFD8FFE1");
		headers[2] = ConversionUtils.hexStringToByteArray("FFD8FFE2");
		headers[3] = ConversionUtils.hexStringToByteArray("FFD8FFE3");
		headers[4] = ConversionUtils.hexStringToByteArray("FFD8FFED");
		headers[5] = ConversionUtils.hexStringToByteArray("FFD8FFEF");
		ValidationUtils.Type type = new ValidationUtils.Type(headers);
		map.put("jpg", type);
		map.put("jpeg", type);
		map.put("jpe", type);
		map.put("jfif", type);

		// EXTENSION_GIF
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("474946383761");
		headers[1] = ConversionUtils.hexStringToByteArray("474946383961");
		map.put("gif", new ValidationUtils.Type(headers));

		// RTF
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("7B5C72746631");
		map.put("rtf", new ValidationUtils.Type(headers));

		// EXTENSION_PNG
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("89504E470D0A1A0A");
		map.put("png", new ValidationUtils.Type(headers));

		// TIFF
		headers = new byte[5][];
		headers[0] = ConversionUtils.hexStringToByteArray("492049");
		headers[1] = ConversionUtils.hexStringToByteArray("49492A00");
		headers[2] = ConversionUtils.hexStringToByteArray("49492B00");
		headers[3] = ConversionUtils.hexStringToByteArray("4D4D002A");
		headers[4] = ConversionUtils.hexStringToByteArray("4D4D002B");
		type = new ValidationUtils.Type(headers);
		map.put("tiff", type);
		map.put("tif", type);

		// BMP
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("424D");
		type = new ValidationUtils.Type(headers);
		map.put("bmp", type);

		// XML, SVG
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("3C3F786D6C2076657273696F6E3D22312E30223F3E");
		headers[1] = ConversionUtils.hexStringToByteArray("3C3F786D6C2076657273696F6E3D22312E31223F3E");
		type = new ValidationUtils.Type(headers);
		map.put("xml", type);
		map.put("svg", type);

		// EXTENSION_PDF
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("25504446");
		map.put("pdf", new ValidationUtils.Type(headers));

		// PPS, PUB, VSD
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		type = new ValidationUtils.Type(headers);
		map.put("pps", type);
		map.put("pub", type);
		map.put("vsd", type);

		// EXTENSION_DOC
		headers = new byte[4][];
		// From MS Office
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		// From Deskmate
		headers[1] = ConversionUtils.hexStringToByteArray("0D444F43");
		// From PerfectOffice
		headers[2] = ConversionUtils.hexStringToByteArray("CF11E0A1B11AE100");
		// From Word 2.0
		headers[3] = ConversionUtils.hexStringToByteArray("DBA52D00");
		byte[][] subHeaders = new byte[1][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("ECA5C100");
		int[] offsets = new int[1];
		offsets[0] = 512;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("doc", type);

		// EXTENSION_PPT
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("006E1EF0");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("0F00E803");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("A0461DF0");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("FDFFFFFF0E000000");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("FDFFFFFF1C000000");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("FDFFFFFF43000000");
		offsets = new int[6];
		offsets[0] = 512;
		offsets[1] = 512;
		offsets[2] = 512;
		offsets[3] = 512;
		offsets[4] = 512;
		offsets[5] = 512;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("ppt", type);

		// EXTENSION_XLS
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		subHeaders = new byte[7][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("0908100000060500");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("FDFFFFFF10");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("FDFFFFFF1F");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("FDFFFFFF22");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("FDFFFFFF23");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("FDFFFFFF28");
		subHeaders[6] = ConversionUtils.hexStringToByteArray("FDFFFFFF29");
		offsets = new int[7];
		offsets[0] = 512;
		offsets[1] = 512;
		offsets[2] = 512;
		offsets[3] = 512;
		offsets[4] = 512;
		offsets[5] = 512;
		offsets[6] = 512;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("xls", type);

		// ZIP
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("504B0304");
		map.put("zip", new ValidationUtils.Type(headers));

		// The new Office formats (EXTENSION_DOCX, EXTENSION_XLSX, EXTENSION_PPTX, etc)
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("504B0304");
		headers[1] = ConversionUtils.hexStringToByteArray("504B030414000600");
		type = new ValidationUtils.Type(headers);
		map.put("docx", type);
		map.put("pptx", type);
		map.put("xlsx", type);
		map.put("ppsx", type);

		// WMV, ASF, WMA
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("3026B2758E66CF11A6D900AA0062CE6C");
		type = new ValidationUtils.Type(headers);
		map.put("asf", type);
		map.put("wma", type);
		map.put("wmv", type);

		// AVI
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("52494646");
		subHeaders = new byte[1][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("415649204C495354");
		offsets = new int[1];
		offsets[0] = 8;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("avi", type);

		// FLV
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("464C5601");
		type = new ValidationUtils.Type(headers);
		map.put("flv", type);

		// MP4, M4V, M4A
		headers = new byte[4][];
		headers[0] = ConversionUtils.hexStringToByteArray("0000001466747970");
		headers[1] = ConversionUtils.hexStringToByteArray("0000001866747970");
		headers[2] = ConversionUtils.hexStringToByteArray("0000001C66747970");
		headers[3] = ConversionUtils.hexStringToByteArray("0000002066747970");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("33677035");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("4D344120");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("4D534E56012900464D534E566D703432");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("69736F6D");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("6D703432");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("7174202020");
		offsets = new int[6];
		offsets[0] = 8;
		offsets[1] = 8;
		offsets[2] = 8;
		offsets[3] = 8;
		offsets[4] = 8;
		offsets[5] = 8;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("m4a", type);
		map.put("m4v", type);
		map.put("mp4", type);

		// MPG, MPEG
		headers = new byte[16][];
		headers[0] = ConversionUtils.hexStringToByteArray("000001B0");
		headers[1] = ConversionUtils.hexStringToByteArray("000001B1");
		headers[2] = ConversionUtils.hexStringToByteArray("000001B2");
		headers[3] = ConversionUtils.hexStringToByteArray("000001B3");
		headers[4] = ConversionUtils.hexStringToByteArray("000001B4");
		headers[5] = ConversionUtils.hexStringToByteArray("000001B5");
		headers[6] = ConversionUtils.hexStringToByteArray("000001B6");
		headers[7] = ConversionUtils.hexStringToByteArray("000001B7");
		headers[8] = ConversionUtils.hexStringToByteArray("000001B8");
		headers[9] = ConversionUtils.hexStringToByteArray("000001B9");
		headers[10] = ConversionUtils.hexStringToByteArray("000001BA");
		headers[11] = ConversionUtils.hexStringToByteArray("000001BB");
		headers[12] = ConversionUtils.hexStringToByteArray("000001BC");
		headers[13] = ConversionUtils.hexStringToByteArray("000001BD");
		headers[14] = ConversionUtils.hexStringToByteArray("000001BE");
		headers[15] = ConversionUtils.hexStringToByteArray("000001BF");
		// While these trailers are *supposed* to be there, it seems that a lot of MPGs don't have it
		// byte[][] trailers = new byte[2][];
		// trailers[0] = ConversionUtils.hexStringToByteArray("000001B7");
		// trailers[1] = ConversionUtils.hexStringToByteArray("000001B9");
		// type = new Type(headers, null, null, trailers);
		type = new ValidationUtils.Type(headers);
		map.put("mpg", type);
		map.put("mpeg", type);
		map.put("vob", type);

		// 3GG, 3GP, 3G2
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("0000001466747970336770");
		headers[1] = ConversionUtils.hexStringToByteArray("0000002066747970336770");
		type = new ValidationUtils.Type(headers);
		map.put("3gg", type);
		map.put("3gp", type);
		map.put("3g2", type);

		// MOV
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("000000146674797071742020");
		headers[1] = ConversionUtils.hexStringToByteArray("000007B56D6F6F76");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("66726565");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("6D646174");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("77696465");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("706E6F74");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("736B6970");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("6C6D766864");
		offsets = new int[6];
		offsets[0] = 24;
		offsets[1] = 24;
		offsets[2] = 24;
		offsets[3] = 24;
		offsets[4] = 24;
		offsets[5] = 11;
		type = new ValidationUtils.Type(headers, offsets, subHeaders);
		map.put("mov", type);

		return Collections.unmodifiableMap(map);
	}

	/**
	 * <p>
	 * Creates a set of magic headers that we will accept. Please note that we do <strong>not</strong> accept all of the
	 * same things as we did in the map because some things are dangerous without that extension.
	 * </p>
	 *
	 * @return A set of magic headers against which files may be validated
	 */
	private static Set<ValidationUtils.Type> getFileTypesSet() {
		if (!ValidationUtils.isEmpty(ValidationUtils.FILE_TYPES_SET))
			return ValidationUtils.FILE_TYPES_SET;

		// TODO Eventually add in trailer verifications, but that more makes sure that the whole file was
		// uploaded than anything
		final Set<ValidationUtils.Type> set = new HashSet<>();

		// TXT, CSV
		set.add(new ValidationUtils.Type(null));

		// EXTENSION_JPG
		byte[][] headers = new byte[6][];
		headers[0] = ConversionUtils.hexStringToByteArray("FFD8FFE0");
		headers[1] = ConversionUtils.hexStringToByteArray("FFD8FFE1");
		headers[2] = ConversionUtils.hexStringToByteArray("FFD8FFE2");
		headers[3] = ConversionUtils.hexStringToByteArray("FFD8FFE3");
		headers[4] = ConversionUtils.hexStringToByteArray("FFD8FFED");
		headers[5] = ConversionUtils.hexStringToByteArray("FFD8FFEF");
		set.add(new ValidationUtils.Type(headers));

		// EXTENSION_GIF
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("474946383761");
		headers[1] = ConversionUtils.hexStringToByteArray("474946383961");
		set.add(new ValidationUtils.Type(headers));

		// RTF
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("7B5C72746631");
		set.add(new ValidationUtils.Type(headers));

		// EXTENSION_PNG
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("89504E470D0A1A0A");
		set.add(new ValidationUtils.Type(headers));

		// TIFF
		headers = new byte[5][];
		headers[0] = ConversionUtils.hexStringToByteArray("492049");
		headers[1] = ConversionUtils.hexStringToByteArray("49492A00");
		headers[2] = ConversionUtils.hexStringToByteArray("49492B00");
		headers[3] = ConversionUtils.hexStringToByteArray("4D4D002A");
		headers[4] = ConversionUtils.hexStringToByteArray("4D4D002B");
		set.add(new ValidationUtils.Type(headers));

		// BMP
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("424D");
		set.add(new ValidationUtils.Type(headers));

		// XML, SVG
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("3C3F786D6C2076657273696F6E3D22312E30223F3E");
		headers[1] = ConversionUtils.hexStringToByteArray("3C3F786D6C2076657273696F6E3D22312E31223F3E");
		set.add(new ValidationUtils.Type(headers));

		// EXTENSION_PDF
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("25504446");
		set.add(new ValidationUtils.Type(headers));

		// PPS, PUB, VSD
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		set.add(new ValidationUtils.Type(headers));

		// EXTENSION_DOC
		headers = new byte[4][];
		// From MS Office
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		// From Deskmate
		headers[1] = ConversionUtils.hexStringToByteArray("0D444F43");
		// From PerfectOffice
		headers[2] = ConversionUtils.hexStringToByteArray("CF11E0A1B11AE100");
		// From Word 2.0
		headers[3] = ConversionUtils.hexStringToByteArray("DBA52D00");
		byte[][] subHeaders = new byte[1][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("ECA5C100");
		int[] offsets = new int[1];
		offsets[0] = 512;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		// EXTENSION_PPT
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("006E1EF0");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("0F00E803");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("A0461DF0");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("FDFFFFFF0E000000");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("FDFFFFFF1C000000");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("FDFFFFFF43000000");
		offsets = new int[6];
		offsets[0] = 512;
		offsets[1] = 512;
		offsets[2] = 512;
		offsets[3] = 512;
		offsets[4] = 512;
		offsets[5] = 512;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		// EXTENSION_XLS
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("D0CF11E0A1B11AE1");
		subHeaders = new byte[7][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("0908100000060500");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("FDFFFFFF10");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("FDFFFFFF1F");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("FDFFFFFF22");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("FDFFFFFF23");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("FDFFFFFF28");
		subHeaders[6] = ConversionUtils.hexStringToByteArray("FDFFFFFF29");
		offsets = new int[7];
		offsets[0] = 512;
		offsets[1] = 512;
		offsets[2] = 512;
		offsets[3] = 512;
		offsets[4] = 512;
		offsets[5] = 512;
		offsets[6] = 512;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		// ZIP
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("504B0304");
		set.add(new ValidationUtils.Type(headers));

		// The new Office formats (EXTENSION_DOCX, EXTENSION_XLSX, EXTENSION_PPTX, etc)
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("504B0304");
		headers[1] = ConversionUtils.hexStringToByteArray("504B030414000600");
		set.add(new ValidationUtils.Type(headers));

		// WMV, ASF, WMA
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("3026B2758E66CF11A6D900AA0062CE6C");
		set.add(new ValidationUtils.Type(headers));

		// AVI
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("52494646");
		subHeaders = new byte[1][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("415649204C495354");
		offsets = new int[1];
		offsets[0] = 8;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		// FLV
		headers = new byte[1][];
		headers[0] = ConversionUtils.hexStringToByteArray("464C5601");
		set.add(new ValidationUtils.Type(headers));

		// MP4, M4V, M4A
		headers = new byte[4][];
		headers[0] = ConversionUtils.hexStringToByteArray("0000001466747970");
		headers[1] = ConversionUtils.hexStringToByteArray("0000001866747970");
		headers[2] = ConversionUtils.hexStringToByteArray("0000001C66747970");
		headers[3] = ConversionUtils.hexStringToByteArray("0000002066747970");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("33677035");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("4D344120");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("4D534E56012900464D534E566D703432");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("69736F6D");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("6D703432");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("7174202020");
		offsets = new int[6];
		offsets[0] = 8;
		offsets[1] = 8;
		offsets[2] = 8;
		offsets[3] = 8;
		offsets[4] = 8;
		offsets[5] = 8;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		// MPG, MPEG
		headers = new byte[16][];
		headers[0] = ConversionUtils.hexStringToByteArray("000001B0");
		headers[1] = ConversionUtils.hexStringToByteArray("000001B1");
		headers[2] = ConversionUtils.hexStringToByteArray("000001B2");
		headers[3] = ConversionUtils.hexStringToByteArray("000001B3");
		headers[4] = ConversionUtils.hexStringToByteArray("000001B4");
		headers[5] = ConversionUtils.hexStringToByteArray("000001B5");
		headers[6] = ConversionUtils.hexStringToByteArray("000001B6");
		headers[7] = ConversionUtils.hexStringToByteArray("000001B7");
		headers[8] = ConversionUtils.hexStringToByteArray("000001B8");
		headers[9] = ConversionUtils.hexStringToByteArray("000001B9");
		headers[10] = ConversionUtils.hexStringToByteArray("000001BA");
		headers[11] = ConversionUtils.hexStringToByteArray("000001BB");
		headers[12] = ConversionUtils.hexStringToByteArray("000001BC");
		headers[13] = ConversionUtils.hexStringToByteArray("000001BD");
		headers[14] = ConversionUtils.hexStringToByteArray("000001BE");
		headers[15] = ConversionUtils.hexStringToByteArray("000001BF");
		// While these trailers are *supposed* to be there, it seems that a lot of MPGs don't have it
		// byte[][] trailers = new byte[2][];
		// trailers[0] = ConversionUtils.hexStringToByteArray("000001B7");
		// trailers[1] = ConversionUtils.hexStringToByteArray("000001B9");
		// set.add(new Type(headers, null, null, trailers));
		set.add(new ValidationUtils.Type(headers));

		// 3GG, 3GP, 3G2
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("0000001466747970336770");
		headers[1] = ConversionUtils.hexStringToByteArray("0000002066747970336770");
		set.add(new ValidationUtils.Type(headers));

		// MOV
		headers = new byte[2][];
		headers[0] = ConversionUtils.hexStringToByteArray("000000146674797071742020");
		headers[1] = ConversionUtils.hexStringToByteArray("000007B56D6F6F76");
		subHeaders = new byte[6][];
		subHeaders[0] = ConversionUtils.hexStringToByteArray("66726565");
		subHeaders[1] = ConversionUtils.hexStringToByteArray("6D646174");
		subHeaders[2] = ConversionUtils.hexStringToByteArray("77696465");
		subHeaders[3] = ConversionUtils.hexStringToByteArray("706E6F74");
		subHeaders[4] = ConversionUtils.hexStringToByteArray("736B6970");
		subHeaders[5] = ConversionUtils.hexStringToByteArray("6C6D766864");
		offsets = new int[6];
		offsets[0] = 24;
		offsets[1] = 24;
		offsets[2] = 24;
		offsets[3] = 24;
		offsets[4] = 24;
		offsets[5] = 11;
		set.add(new ValidationUtils.Type(headers, offsets, subHeaders));

		return Collections.unmodifiableSet(set);
	}

	@Contract("null -> false")
	private static boolean isDateMmmDDYYYY(final @Nullable String string) {
		if (ValidationUtils.isEmpty(string))
			return false;
		if (ValidationUtils.DATE_MMM_DDYYYY.matcher(string).matches()) {
			final String[] date = string.split(" ");
			// Remove the comma
			if (!date[1].isEmpty() && (date[1].charAt(date[1].length() - 1) == ',')) {
				date[1] = ValidationUtils.COMMA.matcher(date[1]).replaceFirst("");
			}
			final String month = date[0];
			final int day = Integer.parseInt(date[1]);
			final int year = Integer.parseInt(date[2]);

			switch (month) {
				case "January":
				case "Jan":
					return ValidationUtils.verifyDaysInMonth(day, 1, year);
				case "February":
				case "Feb":
					return ValidationUtils.verifyDaysInMonth(day, 2, year);
				case "March":
				case "Mar":
					return ValidationUtils.verifyDaysInMonth(day, 3, year);
				case "April":
				case "Apr":
					return ValidationUtils.verifyDaysInMonth(day, 4, year);
				case "May":
					return ValidationUtils.verifyDaysInMonth(day, 5, year);
				case "June":
				case "Jun":
					return ValidationUtils.verifyDaysInMonth(day, 6, year);
				case "July":
				case "Jul":
					return ValidationUtils.verifyDaysInMonth(day, 7, year);
				case "August":
				case "Aug":
					return ValidationUtils.verifyDaysInMonth(day, 8, year);
				case "September":
				case "Sep":
					return ValidationUtils.verifyDaysInMonth(day, 9, year);
				case "October":
				case "Oct":
					return ValidationUtils.verifyDaysInMonth(day, 10, year);
				case "November":
				case "Nov":
					return ValidationUtils.verifyDaysInMonth(day, 11, year);
				case "December":
				case "Dec":
					return ValidationUtils.verifyDaysInMonth(day, 12, year);
				default:
					return false;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Checks to see if there are the right NUMBER of days in a month, to include taking into account leap year. This
	 * method artificially caps the years to any year between 1900 and 2200.
	 * </p>
	 *
	 * @param day
	 *            The day from the date
	 * @param month
	 *            The month (1-12) from the date
	 * @param year
	 *            The year (1900-2200) from the date
	 * @return {@code true} iff the day is within the bounds for that month
	 */
	private static boolean verifyDaysInMonth(final int day, final int month, final int year) {
		if (day < 1)
			return false;
		if ((year < 1900) || (year > 2200))
			return false;
		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				return day <= 31;
			case 4:
			case 6:
			case 9:
			case 11:
				return day <= 30;
			case 2:
				if ((year & 3) == 0)
					return day <= 29;
				return day <= 28;
			default:
				return false;
		}
	}

	/**
	 * <p>
	 * Check to see if the Long objects are valid IDs for use in the database. Confirms that each Long is an acceptable
	 * Id.
	 * </p>
	 *
	 * @param supposedNumbers
	 *            Long objects to check
	 * @return boolean false if <em>any</em> of the objects is not valid Id
	 * @see #isId(Long...)
	 */
	@Contract("null -> false")
	public static boolean areIds(final @Nullable Iterable<Long> supposedNumbers) {
		if (ValidationUtils.isEmpty(supposedNumbers))
			return false;
		for (final Long supposedNumber : supposedNumbers) {
			if (!ValidationUtils.isId(supposedNumber))
				return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Determines if the string contains html tags. If the string is empty or null, then return false. This function
	 * checks particularly for opening tags.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string contains HTML
	 */
	@Contract("null -> false")
	public static boolean containsHtmlTags(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.HTML_TAGS.matcher(string).find();
	}

	/**
	 * <p>
	 * Returns a clean String for search.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return A clean
	 */
	@Contract("null -> !null")
	public static String getCleanSearchString(final @Nullable CharSequence string) {
		String resultantString = CommonConstants.EMPTY_STRING;
		if (ValidationUtils.isEmpty(string))
			return resultantString;

		resultantString = ValidationUtils.SEARCH_STRING.matcher(string).replaceAll(resultantString);
		if (resultantString.length() > ValidationUtils.MAX_SEARCH_STRING_LENGTH)
			return resultantString.substring(0, ValidationUtils.MAX_SEARCH_STRING_LENGTH);
		return resultantString;
	}

	/**
	 * <p>
	 * Checks if the passed string is a valid 32-bit hex string.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths and right contents
	 */
	@Contract("null -> false")
	public static boolean is32BitHex(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.THIRTY_TWO_BIT_HEX.matcher(string).find();
	}

	/**
	 * <p>
	 * Checks to see if the provided string is an alpha-numeric identifier (so may also include +-_=). These kinds of
	 * IDs are commonly used in auto-generated ids like those of DynamoDB.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string is non-empty and is a valid identifier
	 */
	@Contract("null -> false")
	public static boolean isAlphaNumericId(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.ALPHA_NUMERIC_ID.matcher(string).matches();
	}

	/**
	 * <p>
	 * Determines if a given String contains only alphas, numerics, and the underscore. This method is particularly
	 * useful for LKU type code strings and similar data.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} if the input contains only alphas, numerics, and underscores and is between 1 and 255
	 *         characters long
	 */
	@Contract("null -> false")
	public static boolean isAlphaNumericUnderscore(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.ALPHA_NUMERIC_UNDERSCORE.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks if the passed string is a valid base64 string.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths and right contents
	 */
	@Contract("null -> false")
	public static boolean isBase64(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.BASE_64.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks if the passed string is a valid base64 string.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths and right contents
	 */
	@Contract("null -> false")
	public static boolean isBase64AllowingSpaces(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.BASE_64_ALLOW_SPACES.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks if the passed string represents a bearer token, including the Bearer header, and containing a JSON Web
	 * Token (JWT) object. Please note that it does not verify or validate the JWT, itself, only that the string can
	 * represent a JWT.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the object is structured in accordance with Bearer token and JWT requirements
	 */
	@Contract("null -> false")
	public static boolean isBearerToken(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.BEARER_WEB_TOKEN.matcher(string).matches();
	}

	/**
	 * <p>
	 * Validates an Integer that stores 0/1 byte flag values.
	 * </p>
	 *
	 * @param b
	 *            The integer to check
	 * @return {@code true} iff the integer is not {@code null} and of the right values
	 */
	public static boolean isByteFlag(final @Nullable Integer b) {
		if (Objects.isNull(b))
			return false;

		final int bVal = b.intValue();
		return (bVal == 0) || (bVal == 1);
	}

	/**
	 * <p>
	 * Validates a String that stores 0/1 byte flag values.
	 * </p>
	 *
	 * @param b
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths and right contents
	 */
	public static boolean isByteFlag(final @Nullable String b) {
		return "1".equals(b) || "0".equals(b);
	}

	/**
	 * <p>
	 * Struts2 has a hard time sending checkbox values back so this method is a good catch-all.
	 * </p>
	 *
	 * @param checkboxValue
	 *            The value of the checkbox
	 * @return {@code true} iff the string indicates that the checkbox is checked
	 */
	@Contract("null -> false")
	public static boolean isCheckboxChecked(final @Nullable String checkboxValue) {
		return "true".equalsIgnoreCase(checkboxValue) || "yes".equalsIgnoreCase(checkboxValue)
				|| "on".equalsIgnoreCase(checkboxValue);
	}

	/**
	 * <p>
	 * Determines if a given String is a valid Date, such that we can convert it to a Date without an exception
	 * occurring. The method even ensures that the day is valid for the month and year, taking leap year into account.
	 * </p>
	 * <p>
	 * The currently accepted formats are:
	 * </p>
	 * <ul>
	 * <li>mm/dd/YYYY</li>
	 * <li>mm/YYYY</li>
	 * <li>mm-YYYY</li>
	 * <li>mm YYYY</li>
	 * <li>Month dd YYYY</li>
	 * <li>Month dd, YYYY</li>
	 * <li>Month, YYYY</li>
	 * <li>Month YYYY</li>
	 * </ul>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the input can be converted to a valid Date
	 */
	@Contract("null -> false")
	public static boolean isDate(final @Nullable String string) {
		if (ValidationUtils.isEmpty(string))
			return false;
		// mm/dd/YYYY
		if (ValidationUtils.DATE_MMDDYYYY.matcher(string).matches()) {
			final String[] date = string.split("/");
			return ValidationUtils.verifyDaysInMonth(Integer.parseInt(date[1]), Integer.parseInt(date[0]),
					Integer.parseInt(date[2]));
		}

		// Month dd YYYY and Month dd, YYYY
		if (ValidationUtils.isDateMmmDDYYYY(string))
			return true;

		// Month, YYYY and Month YYYY
		if (ValidationUtils.DATE_MMM_YYYY.matcher(string).matches())
			return true;

		// mm/YYYY and mm-YYYY and mm YYYY
		return ValidationUtils.DATE_MMYYYY.matcher(string).matches();

		// TODO Add in other formats and DateTime formats... or maybe make a isDateTime method...
	}

	/**
	 * <p>
	 * Determines if the string is a valid date in the ISO 8601 format, specifically, YYYY-MM-DD.
	 * </p>
	 *
	 * @param string
	 *            The string to examine.
	 * @return {@code true} iff the String is an ISO 8601-compliant date
	 */
	@Contract("null -> false")
	public static boolean isDateISO8601(final @Nullable CharSequence string) {
		// YYYY-mm-dd
		return !ValidationUtils.isEmpty(string) && ValidationUtils.DATE_YYYY_MM_DD.matcher(string).matches();

	}

	/**
	 * <p>
	 * Checks the provided CharSequence to ensure that it is non-empty and contains only digits and periods. This method
	 * does not allow leading periods and allows for multiple periods, meaning it does not determine if something is a
	 * number, it determines if something is only some digits, a period, some more digits, a period, and so-on, or just
	 * digits.
	 * </p>
	 *
	 * @param string
	 *            The text to check
	 * @return {@code true} iff the text is non-empty and only contains digits and periods
	 */
	@Contract("null -> false")
	public static boolean isDigitsAndPeriods(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.DIGITS_AND_PERIODS.matcher(string).matches();
	}

	/**
	 * <p>
	 * This method ensures that the given string can be converted to a Double without throwing an exception.
	 * </p>
	 *
	 * @param string
	 *            The supposed double
	 * @return {@code true} iff the string can be converted to {@code double} without a NumberFormatException being
	 *         raised
	 */
	@Contract("null -> false")
	public static boolean isDouble(final @Nullable String string) {
		if (ValidationUtils.isEmpty(string) || !ValidationUtils.ALL_DIGITS.matcher(string).matches())
			return false;
		try {
			final Double validDouble = Double.valueOf(string);
			return !validDouble.isNaN();
		} catch (final @NotNull NumberFormatException ignored) {
			return false;
		}
	}

	/**
	 * <p>
	 * Validates email addresses using regular expression pattern that is relatively compliant with RFC 822, RFC 3696,
	 * RFC 5321. We do NOT follow them completely, though, because we block some characters, nor do we follow RFC
	 * 6530-6533 at all simply because we use Latin characters. It is possible to allow any character set, but that can
	 * lead to SQL injection.
	 * </p>
	 * <p>
	 * Similarly, it would be possible to follow RFC 3696 to the letter, particularly with regard to the local-parts and
	 * the limit of 255 octets on the domain name (each label being no more than 63 octets long), but that is deemed
	 * overkill, plus it would mean that we would have to sanitize the input for SQL AND SMTP.
	 * </p>
	 *
	 * @param string
	 *            The alleged email address to be checked
	 * @return True iff it is less than 256 characters long, isn't null/empty, and matches our regular expression
	 */
	@Contract("null -> false")
	public static boolean isEmailAddress(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && (string.length() < 256)
				&& ValidationUtils.EMAIL.matcher(string).matches();
	}

	/**
	 * <p>
	 * Check to see if the String is empty, including all whitespace, or null.
	 * </p>
	 *
	 * @param charSequence
	 *            Object String to check
	 * @return boolean true if empty
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable CharSequence charSequence) {
		return (Objects.isNull(charSequence)) || ValidationUtils.WHITESPACE_OR_EMPTY.matcher(charSequence).matches();
	}

	/**
	 * <p>
	 * Check to see if the Collection is empty or null.
	 * </p>
	 *
	 * @param collection
	 *            The Collection to check
	 * @return boolean {@code true} iff the Collection is null or empty
	 */
	@Contract("null -> true")
	public static <T> boolean isEmpty(final @Nullable Collection<T> collection) {
		return Objects.isNull(collection) || collection.isEmpty();
	}

	/**
	 * <p>
	 * Check to see if the {@link Date} is null.
	 * </p>
	 *
	 * @param temporal
	 *            The {@link Date} to check
	 * @return boolean true if empty
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable Date temporal) {
		return Objects.isNull(temporal);
	}

	/**
	 * <p>
	 * Check to see if the {@link Iterable} is null.
	 * </p>
	 *
	 * @param iterable
	 *            The Iterable to check
	 * @return boolean {@code true} iff the Iterable is null
	 */
	@Contract("null -> true")
	public static <T> boolean isEmpty(final @Nullable Iterable<T> iterable) {
		return Objects.isNull(iterable);
	}

	/**
	 * <p>
	 * Check to see if the Map is empty or null.
	 * </p>
	 *
	 * @param map
	 *            The Map to check
	 * @return boolean {@code true} iff the Map is null or empty
	 */
	@Contract("null -> true")
	public static <K, V> boolean isEmpty(final @Nullable Map<K, V> map) {
		return Objects.isNull(map) || map.isEmpty();
	}

	/**
	 * <p>
	 * Check to see if the {@link Number} is null.
	 * </p>
	 *
	 * @param number
	 *            {@link Number} to check
	 * @return boolean true if null
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable Number number) {
		return Objects.isNull(number);
	}

	/**
	 * <p>
	 * Check to see if Object is empty or null.
	 * </p>
	 *
	 * @param object
	 *            The object to check
	 * @return boolean {@code true} iff the Object is null or determined to be empty (using methods that it provides --
	 *         if it doesn't provide such methods, it's only empty if it's null)
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable Object object) {
		if (Objects.isNull(object))
			return true;

		try {
			// Try to use the object class's isEmpty method to check if we're empty, now that we know we're
			// not NULL
			final Method method = object.getClass().getMethod("isEmpty");
			final Object result = method.invoke(object);

			if (result instanceof Boolean)
				return ((Boolean) result).booleanValue();
		} catch (final @NotNull NoSuchMethodException | InvocationTargetException | IllegalArgumentException
				| IllegalAccessException | SecurityException ignored) {
			// We couldn't invoke... let's go to the next common method
		}

		try {
			// Try to use the object class's length method to check if we're empty, now that we know we're
			// not NULL
			final Method method = object.getClass().getMethod("length");
			final Object result = method.invoke(object);

			if (result instanceof Number)
				return ((Number) result).longValue() <= 0L;
		} catch (final @NotNull NoSuchMethodException | InvocationTargetException | IllegalArgumentException
				| IllegalAccessException | SecurityException ignored) {
			// We couldn't invoke... let's go to the next common method
		}

		try {
			// Try to use the object class's size method to check if we're empty, now that we know we're
			// not NULL
			final Method method = object.getClass().getMethod("size");
			final Object result = method.invoke(object);

			if (result instanceof Number)
				return ((Number) result).longValue() <= 0L;
		} catch (final @NotNull NoSuchMethodException | InvocationTargetException | IllegalArgumentException
				| IllegalAccessException | SecurityException ignored) {
			// We couldn't invoke... but we're not null... treat it like an Object
		}

		// Let's treat it like an Object... we're not null, so we're not empty
		return false;
	}

	/**
	 * <p>
	 * Check to see if the array of Objects is empty or null.
	 * </p>
	 *
	 * @param object
	 *            Object Array to check
	 * @return boolean true if empty
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable Object... object) {
		return (Objects.isNull(object) || (object.length == 0));
	}

	/**
	 * <p>
	 * Check to see if the Optional is not present or null. We have overloaded this because, in the past, this because
	 * isEmpty is being used on Optionals and this will ensure that we are checking the Optional's
	 * {@link Optional#isPresent() isPresent}.
	 * </p>
	 *
	 * @param optional
	 *            Optional to check
	 * @return boolean true if null or not present
	 */
	@Contract("null -> true")
	public static <T> boolean isEmpty(final @Nullable Optional<T> optional) {
		return Objects.isNull(optional) || !optional.isPresent();
	}

	/**
	 * <p>
	 * Check to see if the {@link ServletRequest} is empty or null.
	 * </p>
	 *
	 * @param request
	 *            The ServletRequest to check
	 * @return boolean {@code true} iff the ServletRequest is null
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable ServletRequest request) {
		return Objects.isNull(request);
	}

	/**
	 * <p>
	 * Check to see if the {@link Temporal} is null.
	 * </p>
	 *
	 * @param temporal
	 *            The {@link Temporal} to check
	 * @return boolean true if empty
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable Temporal temporal) {
		return Objects.isNull(temporal);
	}

	/**
	 * <p>
	 * Check to see if the {@link TemporalAmount} is null.
	 * </p>
	 *
	 * @param temporalAmount
	 *            The {@link TemporalAmount} to check
	 * @return boolean true if empty
	 */
	@Contract("null -> true")
	public static boolean isEmpty(final @Nullable TemporalAmount temporalAmount) {
		return Objects.isNull(temporalAmount);
	}

	/**
	 * <p>
	 * This method determines the equality of two doubles using a default scale.
	 * </p>
	 *
	 * @param left
	 *            The first value
	 * @param right
	 *            The second value
	 * @return {@code true} iff the two values are within the default scale
	 */
	public static boolean isEqual(final double left, final double right) {
		return ValidationUtils.isEqual(left, right, 0.0001);
	}

	/**
	 * <p>
	 * This method determines the equality of two doubles using the given scale to aid in the determination.
	 * </p>
	 *
	 * @param left
	 *            The first value
	 * @param right
	 *            The second value
	 * @param scale
	 *            The fuzz-factor allowed within which equality is declared
	 * @return {@code true} iff the two values are within the given scale
	 */
	public static boolean isEqual(final double left, final double right, final double scale) {
		return Math.abs(left - right) < scale;
	}

	/**
	 * <p>
	 * Determine if the provided file is one of the expected file types. Note, this DOES NOT work on text-based files
	 * like .txt and .csv. Data within those files should be evaluated separately.
	 * </p>
	 *
	 * @param expectedFileTypes
	 *            List of extensions
	 * @param filename
	 *            Name of the file being validated
	 * @param file
	 *            Bytes of the file being validated
	 * @return {@code true} iff the file's magic numbers match what we would expect for that "file type"
	 */
	public static boolean isExpectedFileType(final @NotNull Collection<String> expectedFileTypes,
			final @NotNull CharSequence filename, final @Nullable byte... file) {
		// retrieve the file extension and check it against the expected file types
		// if it fails, return false
		final String fileExtension = FilenameUtils.getExtension(filename.toString()).toLowerCase();
		if (expectedFileTypes.stream().noneMatch(expectedType -> expectedType.equalsIgnoreCase(fileExtension)))
			return false;

		// determine if a validator exists for the given file type, if it does, then
		// use it to determine if the file is valid.
		final Type fileValidator = ValidationUtils.FILE_TYPES.get(fileExtension);
		return !ValidationUtils.isEmpty(fileValidator) && fileValidator.validate(file);

		// if you have made it this far you have passed all the current tests
	}

	/**
	 * <p>
	 * Checks if a string contains something other than:<br />
	 * \x20-\x21<br />
	 * \x23-\x25<br />
	 * \x2d-\x2e<br />
	 * \x30-\x39<br />
	 * \x41-\x5a<br />
	 * \x5f<br />
	 * \x61-\x7a
	 * </p>
	 * <p>
	 * This method also ensures that the string does not equal '.' or '..' as these are both directories.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the provided input is null, empty, or ultimately only contains characters that would
	 *         make up a valid filename
	 */
	@Contract("null -> false")
	public static boolean isFileName(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && !ValidationUtils.FILE_NAME.matcher(string).find()
				&& !(".".contentEquals(string) || "..".contentEquals(string));
	}

	/**
	 * <p>
	 * Checks if the passed string is a valid hex string of bytes.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths and right contents
	 */
	@Contract("null -> false")
	public static boolean isHexStringOfBytes(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.HEX_STRING_OF_BYTES.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if the given string is a properly formated hostname or IP address.
	 * </p>
	 *
	 * @param string
	 *            The alleged hostname/ip address
	 * @return {@code true} iff the string fits the form of a hostname or IP address
	 */
	@Contract("null -> false")
	public static boolean isHost(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.RFC_COMPLIANT_IP_AND_HOST.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if the given string is a properly formated hostname or IP address.
	 * </p>
	 *
	 * @param string
	 *            The alleged hostname/ip address
	 * @return {@code true} iff the string fits the form of a hostname or IP address
	 */
	@Contract("null -> false")
	public static boolean isHostAllowingPort(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string)
				&& ValidationUtils.RFC_COMPLIANT_IP_AND_HOST_AND_PORT.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if a string contains a valid, positive 64-bit long, which fits into PostgreSQL's BIGINT.
	 * </p>
	 *
	 * @param string
	 *            String to check
	 * @return boolean true if the string contains a valid, positive 64-bit long, which fits into PostgreSQL's BIGINT.
	 */
	@Contract("null -> false")
	public static boolean isId(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.POSTGRES_BIGINT.matcher(string).matches();
	}

	/**
	 * <p>
	 * Check to see if the Long object is a valid ID for use in the database. Confirms that the Long is not null and is
	 * positive (greater than zero).
	 * </p>
	 *
	 * @param supposedNumbers
	 *            Long object to check
	 * @return boolean true if empty
	 */
	@Contract("null -> false")
	public static boolean isId(final @Nullable Long... supposedNumbers) {
		if (ValidationUtils.isEmpty((Object[]) supposedNumbers))
			return false;

		for (final Long supposedNumber : supposedNumbers) {
			if (ValidationUtils.isEmpty(supposedNumber) || (supposedNumber.longValue() < 0L))
				return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Check to see if the Long object is a valid ID for use in the database. Confirms that the Long is not null and is
	 * positive.
	 * </p>
	 *
	 * @param supposedNumber
	 *            Long object to check
	 * @return boolean true if empty
	 */
	@Contract("null -> false")
	public static boolean isId(final @Nullable Optional<Long> supposedNumber) {
		return Objects.nonNull(supposedNumber) && supposedNumber.isPresent()
				&& ValidationUtils.isId(supposedNumber.get());
	}

	/**
	 * <p>
	 * Checks if the passed string represents a JSON Web Token (JWT) object. Please note that it does not verify or
	 * validate the JWT, itself, only that the string can represent a JWT.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the object is structured in accordance with JWT requirements
	 */
	@Contract("null -> false")
	public static boolean isJsonWebToken(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.WEB_TOKEN.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if a string contains only numbers (or a negative sign and then numbers) or not. This method should
	 * <em>only</em> be used to check things like UUIDs that will not be converted to an int, long, etc. All other use
	 * cases should point to {@link ValidationUtils#isId(CharSequence)}
	 * </p>
	 *
	 * @param string
	 *            String to check
	 * @return boolean true if the string contains only digits (or a negative sign and only digits thereafter)
	 * @see #isId(CharSequence)
	 */
	@Contract("null -> false")
	public static boolean isNumber(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.NUMBER.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if a string contains only numbers and at most one decimal.
	 * </p>
	 *
	 * @param string
	 *            String to check
	 * @return boolean true if the string contains only digits and at most one decimal
	 */
	@Contract("null -> false")
	public static boolean isNumberWithDecimal(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.NUMBER_DEC.matcher(string).matches();
	}

	/**
	 * <p>
	 * Determines if a given String is a valid numeric representation of a Month, such that we can convert it to a Month
	 * without an exception occurring.
	 * </p>
	 *
	 * @param string
	 *            The string to examine
	 * @return {@code true} iff the input can be converted to a valid month
	 */
	@Contract("null -> false")
	public static boolean isNumericMonth(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.DATE_MONTH_MONTH_NUMERIC.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks if a String contains something other than a printable character or whitespace
	 * </p>
	 * <p>
	 * This method does NOT check for injection attacks, so much as it looks for invalid characters that may indicate
	 * overflow attacks and things of that nature. It also helps determine if there are funny characters.
	 * </p>
	 * <p>
	 * <strong>NOTE THAT THIS METHOD RETURNS TRUE IF THE INPUT IS NULL OR EMPTY</strong>
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the provided input is null, empty, or ultimately only contains printable (other than \r
	 *         and \n, which are allowed) characters
	 */
	@Contract("null -> true")
	public static boolean isOnlyVisibleAndWhitespaceCharacters(final @Nullable CharSequence string) {
		return ValidationUtils.isEmpty(string)
				|| ValidationUtils.ONLY_VISIBLE_AND_WHITESPACE_CHARACTERS.matcher(string).matches();
	}

	/**
	 * <p>
	 * Tests to see if the given string is properly formed to be a valid TCP/UDP port (0-65535). Note that we do allow
	 * 0, which is invalid in some OSes as a listening port.
	 * </p>
	 *
	 * @param string
	 *            The alleged port
	 * @return {@code true} iff the string fits the form of an unsigned short
	 */
	@Contract("null -> false")
	public static boolean isPort(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.PORT.matcher(string).matches();
	}

	/**
	 * <p>
	 * Tests to see if the given int is properly formed to be a valid TCP/UDP port (0-65535). Note that we do allow 0,
	 * which is invalid in some OSes as a listening port.
	 * </p>
	 *
	 * @param port
	 *            The alleged port
	 * @return {@code true} iff the int fits the form of an unsigned short
	 */
	public static boolean isPort(final int port) {
		return (port >= 0) && (port <= 65535);
	}

	/**
	 * <p>
	 * Checks to see if a string contains only numbers without negative sign.
	 * </p>
	 *
	 * @param string
	 *            String to check
	 * @return boolean true if the string contains only digits and no negative sign
	 */
	@Contract("null -> false")
	public static boolean isPositiveNumber(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.POSITIVE_NUMBER.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if a {@link Integer} is greater than zero.
	 * </p>
	 *
	 * @param number
	 *            The {@link Integer} to check
	 * @return boolean true if the {@link Integer} is a number greater than zero
	 */
	@Contract("null -> false")
	public static boolean isPositiveNumber(final @Nullable Integer number) {
		return Objects.nonNull(number) && (Integer.valueOf(0).compareTo(number) < 0);
	}

	/**
	 * <p>
	 * Checks to see if a {@link Long} is greater than zero.
	 * </p>
	 *
	 * @param number
	 *            The {@link Long} to check
	 * @return boolean true if the {@link Long} is a number greater than zero
	 */
	@Contract("null -> false")
	public static boolean isPositiveNumber(final @Nullable Long number) {
		return Objects.nonNull(number) && (Long.valueOf(0L).compareTo(number) < 0);
	}

	/**
	 * <p>
	 * Checks to see if a String is a valid search string, of the proper length (which includes null and empty string)
	 * and containing the proper data.
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string has the right lengths (to include null or empty string) and right contents
	 */
	@Contract("null -> true")
	public static boolean isSearchString(final @Nullable CharSequence string) {
		return ValidationUtils.isEmpty(string) || ValidationUtils.SEARCH_STRING.matcher(string).matches();
	}

	/**
	 * <p>
	 * Determines if a given Integer is a valid TRL (0-9, single digit).
	 * </p>
	 *
	 * @param trl
	 *            The trl string to examine
	 * @return {@code true} iff the input is a valid TRL
	 */
	@Contract("null -> false")
	public static boolean isTrl(final @Nullable Integer trl) {
		return !ValidationUtils.isEmpty(trl) && (trl.intValue() >= 1) && (trl.intValue() <= 9);
	}

	/**
	 * <p>
	 * Checks if a string is a valid URL encoded string. The way that we do this is verify that zero invalid characters
	 * exist and that all {@code %} (percent) characters are followed by a proper hex string.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the provided input is a valid URL encoded string
	 */
	@Contract("null -> false")
	public static boolean isUrlEncoded(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.URL_ENCODED.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks if a string is a valid general (any domain) URL.
	 * </p>
	 *
	 * @param string
	 *            The String to examine
	 * @return {@code true} iff the provided input is a valid URL going to a general (any domain) website
	 */
	@Contract("null -> false")
	public static boolean isUrlGeneral(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.URL_GENERAL.matcher(string).matches();
	}

	/**
	 * <p>
	 * Checks to see if a valid general URL with path and attributes
	 * </p>
	 *
	 * @param string
	 *            The string to check
	 * @return {@code true} iff the string is a valid URL
	 */
	@Contract("null -> false")
	public static boolean isUrlWithPathAndAttributes(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.URL_GENERAL.matcher(string).matches();
	}

	/**
	 * <p>
	 * Tests to see if the given string is a properly formed to be a username.
	 * </p>
	 *
	 * @param string
	 *            The alleged username
	 * @return {@code true} iff the string fits the form of a username
	 */
	@Contract("null -> false")
	public static boolean isUserName(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.USERNAME.matcher(string).matches();
	}

	/**
	 * <p>
	 * Determines if a given String is an acceptable content-type.
	 * </p>
	 *
	 * @param string
	 *            The string to examine
	 * @return {@code true} iff the input is an acceptable content type
	 */
	@Contract("null -> false")
	public static boolean isValidApiContentType(final @Nullable String string) {
		return FileConstants.CONTENT_TYPE_JSON.equalsIgnoreCase(string)
				|| FileConstants.CONTENT_TYPE_XML.equalsIgnoreCase(string);
	}

	/**
	 * <p>
	 * This method checks to see if the provided string is a valid Forwarded header.
	 * </p>
	 *
	 * @param string
	 *            The forward header to validate
	 * @return {@code true} iff the string is a valid FORWARDED header
	 */
	@Contract("null -> false")
	public static boolean isValidForwardedHeader(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.REQUEST_HEADER_FORWARDED.matcher(string).matches();
	}

	/**
	 * <p>
	 * This method determines if a file is a valid library file, to include verifying that the extension claimed is what
	 * the file actually is (not accounting for bad data, but instead ensuring that the headers/subheaders/etc are
	 * correct).
	 * </p>
	 *
	 * @param filename
	 *            The name of the file (the extension is the most important piece)
	 * @param file
	 *            The bytes that make up the file
	 * @return {@code true} iff the file extension is allowed and the magic bytes match the extension
	 */
	public static boolean isValidLibraryFile(final @Nullable CharSequence filename, final @Nullable byte... file) {
		if (ValidationUtils.isEmpty(filename) || Objects.isNull(file) || (file.length == 0)) {
			ValidationUtils.logger.error("Filename was empty, file was empty, or the file length was zero.");
			ValidationUtils.logger.error("File failed validator check.  Our signature is: {}",
					ConversionUtils.byteArrayToHexString(file, 0, 256));

			return false;
		}

		// TODO I'd like to get back to this, but at the moment Struts stomps over the file extension and
		// replaces it with .tmp
		// final String[] split = SLASHDOT.split(filename);
		// final String extension = split[split.length - 1].toLowerCase();
		// final Type type = FILE_TYPES.get(extension);
		// return !ValidationUtils.isEmpty(type) && type.validate(file);

		// TODO This is the way we have to do it to ignore that .tmp
		for (final ValidationUtils.Type validator : ValidationUtils.FILE_TYPES_SET) {
			if (validator.validate(file))
				return true;
		}

		// TODO Do we want to send an email with the attachment? I can see a case for doing so if the rec. is savvy
		ValidationUtils.logger.error("File failed validator check.  Our signature is: {}",
				ConversionUtils.byteArrayToHexString(file, 0, 256));
		return false;
	}

	/**
	 * <p>
	 * This method checks to see if the provided string is a valid Via header.
	 * </p>
	 *
	 * @param string
	 *            The string to examine
	 * @return {@code true} iff the string is a valid VIA header
	 */
	@Contract("null -> false")
	public static boolean isValidRequestHeaderVia(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.REQUEST_HEADER_VIA.matcher(string).matches();
	}

	/**
	 * This method checks to see if a Session Map is valid -- this means that the session map cannot be null or empty
	 * and must contain an authenticated user ID (which must be an Oracle Number).
	 *
	 * @param userSession
	 *            The session map to check
	 * @return {@code true} iff the session map is not null/empty and contains an authenticated user ID that is an
	 *         oracle number
	 */
	@Contract("null -> false")
	public static boolean isValidSessionMap(final @Nullable Map<String, Object> userSession) {
		return !ValidationUtils.isEmpty(userSession) && ValidationUtils
				.isId(ConversionUtils.as(Long.class, userSession.get(SessionConstants.AUTHENTICATED_USER_ID)));
	}

	/**
	 * <p>
	 * Determines if a given String is a valid Year, such that we can convert it to a Year without an exception
	 * occurring. The method even ensures that the year is in the 1900s, 2000s, and 2100s.
	 * </p>
	 *
	 * @param string
	 *            The string to examine
	 * @return {@code true} iff the input can be converted to a valid year
	 */
	@Contract("null -> false")
	public static boolean isYear(final @Nullable CharSequence string) {
		return !ValidationUtils.isEmpty(string) && ValidationUtils.DATE_YEAR.matcher(string).matches();
	}

	/**
	 * <p>
	 * Determines if a given Integer is a valid Year, such that we can convert it to a Year without an exception
	 * occurring. The method even ensures that the year is in the 1900s, 2000s, and 2100s.
	 * </p>
	 *
	 * @param year
	 *            The Integer to examine
	 * @return {@code true} iff the input can be converted to a valid year
	 */
	@Contract("null -> false")
	public static boolean isYear(final @Nullable Integer year) {
		return !ValidationUtils.isEmpty(year) && (year.intValue() >= 1900) && (year.intValue() <= 2200);
	}

	/**
	 * <p>
	 * This method simply inverses the provided value, allowing reduction of negations in conditions via keywords that
	 * can be more easily done accidentally and missed in review.
	 * </p>
	 *
	 * @param value
	 *            The boolean value to flip
	 * @return {@code true} iff the previous value was false
	 */
	public static boolean not(final boolean value) {
		return !value;
	}

	/**
	 * <p>
	 * This method returns a negation of the given {@link Predicate}.
	 * </p>
	 *
	 * @param t
	 *            The {@link Predicate} to negate
	 * @return The negated predicate
	 * @see StreamUtils#not(Predicate)
	 */
	public static <T> Predicate<T> not(final Predicate<T> t) {
		return StreamUtils.not(t);
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private ValidationUtils() {
		// Do nothing
	}
}
