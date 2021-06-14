package local.rdps.svja.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This is an encapsulating class that makes Enumerations iterable. This allows forEach and other iterable processes
 * against the enumeration held by this class.
 * </p>
 *
 * @param <T>
 *            The data type of the objects stored
 * @author DaRon
 * @since 1.0
 */
public final class IterableEnumeration<T> implements Iterable<T> {
	/**
	 * <p>
	 * This class encapsulates an {@link Enumeration} and makes it as though it were an {@link Iterator}.
	 * </p>
	 *
	 * @param <U>
	 * @author DaRon
	 * @since 1.0
	 */
	private static final class EncapsulatingIterator<U> implements Iterator<U> {
		private final Enumeration<? extends U> encapsulatedEnumeration;

		EncapsulatingIterator(final Enumeration<? extends U> enumeration) {
			this.encapsulatedEnumeration = enumeration;
		}

		@Override
		public boolean hasNext() {
			return (Objects.nonNull(this.encapsulatedEnumeration)) && this.encapsulatedEnumeration.hasMoreElements();
		}

		@Override
		public U next() {
			if ((Objects.isNull(this.encapsulatedEnumeration)) || !this.encapsulatedEnumeration.hasMoreElements())
				throw new NoSuchElementException("No more elements exist");
			return this.encapsulatedEnumeration.nextElement();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("This operation is unsupported");
		}
	}

	private final Enumeration<T> enumeration;

	/**
	 * <p>
	 * This method creates and returns an Iterable type object that encapsulates an enumeration, allowing the resulting
	 * object to be treated like any other Iterable.
	 * </p>
	 *
	 * @param en
	 *            The enumeration to encapsulate
	 * @return An Iterable object
	 */
	public static @NotNull <T> Iterable<T> make(final Enumeration<T> en) {
		return new IterableEnumeration<>(en);
	}

	/**
	 * <p>
	 * A non-static means of making an Iterable enumeration.
	 * </p>
	 *
	 * @param enumeration
	 *            The enumeration to encapsulate
	 */
	public IterableEnumeration(final Enumeration<T> enumeration) {
		this.enumeration = enumeration;
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		return new IterableEnumeration.EncapsulatingIterator<>(this.enumeration);
	}
}
