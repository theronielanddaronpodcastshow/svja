package local.rdps.svja.util;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>
 * This class provides some functionalities that enhance Java 8 streams and should probably have been included in them
 * in the first place.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class StreamUtils {
	/**
	 * <p>
	 * This method returns a {@link Predicate} that ands the inputed {@link Predicate}s together.
	 * </p>
	 *
	 * @param predicates
	 *            The {@link Predicate}s to combine
	 * @return A {@link Predicate} that ands the given inputs
	 */
	@SafeVarargs
	public static <T> Predicate<T> and(final Predicate<T>... predicates) {
		return Arrays.stream(predicates).reduce(Predicate::and).get();
	}

	/**
	 * <p>
	 * This method returns a {@link Predicate} that performs a distinctBy filter using the provided {@link Function}
	 * which can look at properties within objects.
	 * </p>
	 *
	 * @param keyExtractor
	 *            The function that will extract our key
	 * @return A {@link Predicate} that performs a distinct by
	 */
	public static <T> Predicate<T> distinctBy(final Function<? super T, ?> keyExtractor) {
		final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * <p>
	 * This method returns a negation of the given {@link Predicate}.
	 * </p>
	 *
	 * @param t
	 *            The {@link Predicate} to negate
	 * @return The negated predicate
	 */
	public static <T> Predicate<T> not(final Predicate<T> t) {
		return t.negate();
	}

	/**
	 * <p>
	 * This method returns a {@link Predicate} that ors the inputed {@link Predicate}s together.
	 * </p>
	 *
	 * @param predicates
	 *            The {@link Predicate}s to combine
	 * @return A {@link Predicate} that ors the given inputs
	 */
	@SafeVarargs
	public static <T> Predicate<T> or(final Predicate<T>... predicates) {
		return Arrays.stream(predicates).reduce(Predicate::or).get();
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private StreamUtils() {
		// Do nothing
	}
}
