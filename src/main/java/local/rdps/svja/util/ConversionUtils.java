package local.rdps.svja.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * Contains methods to convert from one type to another cleanly.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ConversionUtils {
	private static final String DATE_FORMAT_MM_DD_YY_HH_MM_A = "MM/dd/yy hh:mm a";
	private static final String DATE_FORMAT_MM_DD_YYYY_HH_MM_A_ZZZ = "MM/dd/yyyy hh:mm a zzz";
	private static final byte[] emptyBytes = {};
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Convert a array to a delimited string.
	 *
	 * @param a
	 *            String
	 * @param delimiter
	 *            String
	 * @return String
	 */
	public static @NotNull String arrayToString(final @NotNull String[] a, final CharSequence delimiter) {
		if (ValidationUtils.isEmpty((Object[]) a))
			return CommonConstants.EMPTY_STRING;

		return String.join(delimiter, a);
	}

	/**
	 * Allows for a quick, safe cast, returning a {@code null} if the cast is not possible.
	 *
	 * @param aClass
	 *            The class to cast to
	 * @param object
	 *            The Object needing casted
	 * @return {@code null} iff the Object cannot be safely casted, else the object casted to the specified class
	 */
	@Contract("_,null->null;_,!null->!null")
	public static @Nullable <T> T as(final @NotNull Class<T> aClass, final @Nullable Object object) {
		if (Objects.isNull(object))
			return aClass.cast(null);

		if (!aClass.isInstance(object)) {
			ConversionUtils.logger.error(
					"We are trying to cast (using ConversionUtils.as) an object, but it is not an instance of the desired class; (expected: {}; actually: {}"
							+ ')',
					aClass.getName(), object.getClass().getName(), new IllegalArgumentException());

			return aClass.cast(null);
		}
		return aClass.cast(object);
	}

	/**
	 * Provides a means of SAFELY casting an unknown Collection to a Collection of the specified type. In order to do
	 * this, we have to iterate over the entire Collection, looking for elements that are <em>not</em> of the specified
	 * type. This method can incur a cost over larger Collections, so use it only when you really don't know if the
	 * Collection just contains what it should. If the Collection is empty, null, or contains at least one element that
	 * is not of the right type, a {@code null} is returned.
	 *
	 * @param collection
	 *            A Collection of elements where the Collection is raw or the element types are unknown/unenforced
	 * @param elementClass
	 *            The type of objects that we want our collection to contain (and no other... note that we adhere to
	 *            inheritance, etc)
	 * @return {@code null} if the Collection is null, empty, or contains objects of a different type than elementClass
	 *         (excluding inheriting/extending types); otherwise, a casted Collection of the right type
	 */
	@SuppressWarnings("unchecked")
	@Contract("_,null->null;_,!null->!null")
	public static @Nullable <T> Collection<T> as(final @NotNull Iterable<?> collection,
			final @Nullable Class<T> elementClass) {
		if (ValidationUtils.isEmpty(collection) || (Objects.isNull(elementClass)))
			return null;

		for (final Object obj : collection) {
			if (!elementClass.isInstance(obj)) {
				ConversionUtils.logger.error(
						"We are trying to cast (using ConversionUtils.as) an object, but it is not an instance of the desired class; (expected: {}; actually: {}"
								+ ')',
						elementClass.getName(), obj.getClass().getName(), new IllegalArgumentException());

				return null;
			}
		}
		return (Collection<T>) collection;
	}

	/**
	 * Provides a means of SAFELY casting an unknown Map to a Map of the specified type. In order to do this, we have to
	 * iterate over the entire Map, looking for keys and values that are <em>not</em> of the specified type. This method
	 * can incur a cost over larger Maps, so use it only when you really don't know if the Collection just contains what
	 * it should. If the Map is empty, null, or contains at least one key or element that is not of the right type, a
	 * {@code null} is returned.
	 *
	 * @param map
	 *            A Map of elements where the Map is raw or the key/map types are unknown/unenforced
	 * @param keyClass
	 *            The type of objects that we want our map's keys to contain (and no other... note that we adhere to
	 *            inheritance, etc)
	 * @param valueClass
	 *            The type of objects that we want our map's values to contain (and no other... note that we adhere to
	 *            inheritance, etc)
	 * @return {@code null} if the Collection is null, empty, or contains objects of a different type than internalClass
	 *         (excluding inheriting/extending types); otherwise, a casted Collection of the right type
	 */
	@SuppressWarnings("unchecked")
	@Contract("_,null,null->null;_,null,!null->null;_,!null,null->null;_,!null,!null->!null")
	public static @Nullable <K, V> Map<K, V> as(final @NotNull Map<?, ?> map, final @Nullable Class<K> keyClass,
			final @Nullable Class<V> valueClass) {
		if (ValidationUtils.isEmpty(map) || (Objects.isNull(keyClass)) || (Objects.isNull(valueClass))
				|| (ConversionUtils.as(map.keySet(), keyClass) == null)
				|| (ConversionUtils.as(map.values(), valueClass) == null))
			return null;
		return (Map<K, V>) map;
	}

	/**
	 * Converts a byte array into a hex string (e.g. "deadbeef").
	 *
	 * @param bytes
	 *            The array to convert
	 * @param offset
	 *            Where to start in the array; if the length is less than the offset, an CommonConstants.EMPTY_STRING is
	 *            returned
	 * @param amountToConvert
	 *            The amount of bytes to convert
	 * @return A byte array
	 */
	public static @NotNull String byteArrayToHexString(final @NotNull byte[] bytes, final int offset,
			final int amountToConvert) {
		if ((Objects.isNull(bytes)) || ((bytes.length - 1) < offset))
			return CommonConstants.EMPTY_STRING;

		final StringBuilder sb = new StringBuilder(amountToConvert);

		for (int i = offset, j = amountToConvert; (i < bytes.length) && (j > 0); i++, j--) {
			final int valueAsAnInt = bytes[i] & 0xFF;
			sb.append(Integer.toHexString(valueAsAnInt));
		}

		return sb.toString();
	}

	/**
	 * This method takes the provided byte array and turns it into an int. Remembering that an in is 32 bits, the byte
	 * array returned should have a length of 4. Note that we ignore endianness.
	 *
	 * @param number
	 *            The byte array that supposedly makes up our int
	 * @return An int
	 */
	public static int byteArrayToInt(final @NotNull byte... number) {
		int value = 0;
		int shift = 0;
		for (final byte b : number) {
			value |= ((b & 0xFF) << shift);
			shift += 0x8;
		}

		return value;
	}

	/**
	 * Converts a collection of strings into a sentence form
	 *
	 * @param strings
	 *            The collection of strings
	 * @return A string in the form of "string1, string2, and string3", as appropriate.
	 */
	public static @NotNull String collectionToStringSentence(final @NotNull Collection<String> strings) {
		if (ValidationUtils.isEmpty(strings))
			return CommonConstants.EMPTY_STRING;

		final StringBuilder sb = new StringBuilder();
		int i = 1;
		for (final String str : strings) {
			if (strings.size() > 2) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				if (i == strings.size()) {
					sb.append("and ");
				}
			} else if ((strings.size() == 2) && (i == 2)) {
				sb.append(" and ");
			}
			sb.append(str);
			sb.append('s');
			i++;
		}

		return sb.toString();
	}

	/**
	 * This method takes the provided long and turns it into a byte array. Remembering that a long is 64 bits, the byte
	 * array returned should have a length of 8. Note that we ignore endianness.
	 *
	 * @param data
	 *            The long to turn into an array
	 * @return An array of bytes containing the long
	 */
	public static @NotNull byte[] convertLongToByteArray(final long data) {
		final byte[] array = new byte[Long.SIZE >> 3];

		final int byteMask = 0xff;
		for (int i = 0; i < array.length; i++) {
			array[i] = (byte) ((data >> ((array.length - i - 1) << 3)) & byteMask);
		}

		return array;
	}

	/**
	 * Converts the String to ASCII, turning any invalid characters to question marks (?). It is important to note that
	 * this is <em>not</em> extended ASCII.
	 *
	 * @param input
	 *            The string to convert
	 * @return A byte array containing only valid ASCII characters
	 */
	public static @NotNull byte[] convertToAsciiByteArray(final @NotNull String input) {
		if (ValidationUtils.isEmpty(input))
			return ConversionUtils.emptyBytes;
		return input.getBytes(StandardCharsets.US_ASCII);
	}

	/**
	 * Converts the String to UTF-8 (which is <em>not</em> the same as UTF8).
	 *
	 * @param input
	 *            The string to convert
	 * @return A string containing only valid ASCII characters
	 */
	public static @NotNull String convertToUtf_8(final @NotNull String input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;
		return new String(input.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	}

	/**
	 * Converts date to string.
	 *
	 * @param theDate
	 *            The date
	 * @return A string representing the Date
	 */
	public static @NotNull String dateToString(final @NotNull Date theDate) {
		if (!ValidationUtils.isEmpty(theDate)) {
			final Calendar calendar = new GregorianCalendar();
			final SimpleDateFormat sdf = new SimpleDateFormat(ConversionUtils.DATE_FORMAT_MM_DD_YY_HH_MM_A);
			calendar.setTime(theDate);
			return sdf.format(calendar.getTime());
		}
		return CommonConstants.EMPTY_STRING;
	}

	/**
	 * Converts date to string.
	 *
	 * @param theDate
	 *            The date
	 * @param theDateFormat
	 *            An optional field that specifies the date format to be used when creating the String representation of
	 *            the date
	 * @return A string representing the Date
	 */
	public static @NotNull String dateToString(final @NotNull Date theDate, final String theDateFormat) {
		if (ValidationUtils.isEmpty(theDateFormat))
			return ConversionUtils.dateToString(theDate);

		if (!ValidationUtils.isEmpty(theDate)) {
			final Calendar calendar = new GregorianCalendar();
			final SimpleDateFormat sdf = new SimpleDateFormat(
					ValidationUtils.isEmpty(theDateFormat) ? ConversionUtils.DATE_FORMAT_MM_DD_YY_HH_MM_A
							: theDateFormat);
			calendar.setTime(theDate);
			return sdf.format(calendar.getTime());
		}
		return CommonConstants.EMPTY_STRING;
	}

	/**
	 * Gets the fiscal year from a given date
	 *
	 * @param date
	 * @return A string representation of the Fiscal Year
	 */
	public static String getFiscalYearFromDate(final Date date) {
		if (ValidationUtils.isEmpty(date))
			return CommonConstants.EMPTY_STRING;
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		if (calendar.get(Calendar.MONTH) > 8)
			return Integer.toString(calendar.get(Calendar.YEAR) + 1);
		return Integer.toString(calendar.get(Calendar.YEAR));
	}

	/**
	 * Gets the fiscal year from a given date
	 *
	 * @param date
	 * @return A string representation of the Fiscal Year
	 */
	public static Integer getFiscalYearFromDate(final LocalDate date) {
		if (ValidationUtils.isEmpty(date))
			return null;
		if (date.getMonthValue() > 9)
			return Integer.valueOf(date.getYear() + 1);
		return Integer.valueOf(date.getYear());
	}

	/**
	 * Converts a hex string (e.g. "deadbeef") to a byte array.
	 *
	 * @param string
	 *            The string to convert
	 * @return A byte array
	 */
	public static @NotNull byte[] hexStringToByteArray(final @NotNull CharSequence string) {
		final int len = string.length();
		final byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
					+ Character.digit(string.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Convert a list to a delimited string.
	 *
	 * @param a
	 *            List of Strings
	 * @param delimiter
	 *            String
	 * @return String
	 */
	public static @NotNull String listToString(final @NotNull List<String> a, final String delimiter) {
		if (ValidationUtils.isEmpty(a))
			return CommonConstants.EMPTY_STRING;

		final StringBuilder result = new StringBuilder(a.size() << 3);
		result.append(a.get(0));
		for (int i = 1; i < a.size(); i++) {
			result.append(delimiter);
			result.append(a.get(i));
		}

		return result.toString();
	}

	/**
	 * Parses a BigDecimal from a String.
	 *
	 * @param str
	 *            The text to convert
	 * @return A BigDecimal (or BigDecimal.ZERO if the conversion failed)
	 */
	public static BigDecimal stringToBigDecimal(final CharSequence str) {
		final DecimalFormat d = new DecimalFormat();
		d.setParseBigDecimal(true);
		try {
			return (BigDecimal) d.parse(AttackProtectionUtils.cleanseNumberString(str));
		} catch (final @NotNull ParseException ignored) {
			return BigDecimal.ZERO;
		}
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private ConversionUtils() {
		// Do nothing
	}
}
