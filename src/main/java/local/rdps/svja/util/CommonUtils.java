package local.rdps.svja.util;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DataType;
import org.jooq.SelectField;

import com.opensymphony.xwork2.ActionContext;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * This utilities class contains common utilities that don't really go anywhere specific at the moment.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CommonUtils {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern UPPER_CASE_LETTERS = Pattern.compile("(?<uletter>\\p{Lu})");

	/**
	 * <p>
	 * Convert a camel case phrase into an underscore phrase Ex. thisIsACamelCaseIdentifier ->
	 * this_is_a_camel_case_identifier.
	 * </p>
	 *
	 * @param input
	 *            The input camel case string
	 * @return The string using underscores and all lowercase
	 */
	public static @NotNull String convertCamelCaseToUnderscore(final String input) {
		if (ValidationUtils.isEmpty(input))
			return CommonConstants.EMPTY_STRING;
		return CommonUtils.UPPER_CASE_LETTERS.matcher(input).replaceAll("_$1").toLowerCase();
	}

	/**
	 * <p>
	 * This method checks that the given text fits in the field. If the text does not fit it is trimmed such that it
	 * will fit and the trimmed string is then returned.
	 * </p>
	 *
	 * @param text
	 *            The text to check
	 * @param field
	 *            The field in which the text is to be stuffed
	 * @return A string that fits
	 */
	public static @Nullable String ensureTextFitsField(final String text, final SelectField<String> field) {
		if (Objects.isNull(text))
			return text;

		final DataType<String> dataType = field.getDataType();
		if (dataType.hasLength()) {
			final int fieldMaxLength = dataType.length();
			if (fieldMaxLength < text.length()) {
				CommonUtils.logger.error(
						"The text is too large to fit in the field such as it is and will be truncated (Field: {}; Field Max: {}; Text Length: {}; Overage: {}):{}{}",
						field.getName(), Integer.valueOf(fieldMaxLength), Integer.valueOf(text.length()),
						Integer.valueOf(text.length() - fieldMaxLength), System.lineSeparator(), text,
						new IllegalArgumentException("String too long to fit in database"));
				return text.substring(0, fieldMaxLength);
			}
		}
		return text;
	}

	/**
	 * <p>
	 * This method retrieves the current user's session map and returns it.
	 * </p>
	 *
	 * @return The current user's session map
	 */
	public static @Nullable Map<String, Object> getSession() {
		final ActionContext context = ActionContext.getContext();
		if (Objects.isNull(context))
			return null;

		return context.getSession();
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private CommonUtils() {
		// Do nothing
	}
}
