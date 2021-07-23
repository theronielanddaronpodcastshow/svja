package local.rdps.svja.construct;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class provides username translation for federated services.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class UsernameRegex {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * A regular expression to extract a perl-style regular expression
	 */
	private static final Pattern PERL_STYLE_REPLACE_REGEX_EXTRACTOR = Pattern
			.compile("^/(?<match>.*[^\\\\])/(?<replace>.*[^\\\\])/$", Pattern.CASE_INSENSITIVE);
	/**
	 * The regular expression to apply against the given text
	 */
	private final Optional<Pattern> regex;
	/**
	 * The text to replace whatever matches {@link #regex}
	 */
	@NotNull
	private final String replaceWith;

	/**
	 * <p>
	 * This constructor creates a new username regex construct that transforms usernames.
	 * </p>
	 *
	 * @param perlStyleReplaceRegex
	 *            The perl-style regular expression to apply against any username
	 */
	public UsernameRegex(final String perlStyleReplaceRegex) {
		if (ValidationUtils.isEmpty(perlStyleReplaceRegex)) {
			this.regex = Optional.empty();
			this.replaceWith = "";
		} else {
			final Matcher matcher = UsernameRegex.PERL_STYLE_REPLACE_REGEX_EXTRACTOR.matcher(perlStyleReplaceRegex);
			if (matcher.find()) {
				final String regex = matcher.group("match");
				if (ValidationUtils.isEmpty(regex)) {
					this.regex = Optional.empty();
				} else {
					this.regex = Optional.of(Pattern.compile(regex));
				}

				final String replaceWith = matcher.group("replace");
				if (ValidationUtils.isEmpty(replaceWith)) {
					this.replaceWith = "";
				} else {
					this.replaceWith = replaceWith;
				}
			} else {
				this.regex = Optional.empty();
				this.replaceWith = "";
			}
		}
	}

	/**
	 * <p>
	 * This method transforms the given username, based on the regular expression that makes up this instance and the
	 * replace-with data. If the regular expression is not set, nothing happens.
	 * </p>
	 *
	 * @param username
	 *            The username to transform
	 * @return A transformed username based on the regular expression and its replace-with data
	 */
	public String transform(final String username) {
		if (this.regex.isEmpty())
			return username;

		if (UsernameRegex.logger.isDebugEnabled()) {
			UsernameRegex.logger.debug(
					"Using the regular expression {} to match against {} (matches will be replaced with {})",
					this.regex.get(), username, this.replaceWith);
		}
		final Matcher matcher = this.regex.get().matcher(username);
		return matcher.replaceAll(this.replaceWith);
	}
}
