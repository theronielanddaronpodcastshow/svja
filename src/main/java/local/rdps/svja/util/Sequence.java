package local.rdps.svja.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * This class allows the generation of sequences of numbers using either the default incrementation of 1 or the
 * specified incrementation. The class can be used to create streams.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class Sequence implements Spliterator.OfInt {
	/**
	 * <p>
	 * This class determines the median value, taking the incrementation into account. Calling to apply or applyAsInt
	 * returns the true median, taking the increment interval into account.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	private static class FindMiddle implements IntBinaryOperator {
		private int median;
		private int oneStepLeftOfMedian;
		private final int step;

		/**
		 * <p>
		 * Prepares a new run to find the median.
		 * </p>
		 *
		 * @param step
		 *            The incrementation interval
		 */
		FindMiddle(final int step) {
			this.step = step;
		}

		/**
		 * <p>
		 * Returns the value one interval left of the median if {@code applyAsInt} has been called.
		 * </p>
		 *
		 * @return One interval left of the median
		 */
		int getOneStepLeftOfMedian() {
			return this.oneStepLeftOfMedian;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int applyAsInt(final int left, final int right) {
			final int diff = left - right;
			int slots = Math.abs(diff / this.step) + 1;

			// If we're odd, slosh over one to the right
			if ((slots % 2) == 1) {
				slots++;
			}

			// The next step is the real median, but we want to cut the two as equally as possible, so we claim that the
			// median is over one
			this.median = left + ((slots / 2) * this.step);
			this.oneStepLeftOfMedian = this.median - (1 * this.step);

			return this.median;
		}
	}

	private static final Logger logger = LogManager.getLogger();
	private final int end;
	private final AtomicInteger place;
	private final int start;

	private final int step;

	/**
	 * <p>
	 * Creates a new Stream using a new Sequence, allowing iteration from start to end using the default increment of 1.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence
	 * @return A new stream
	 */
	public static IntStream parallelStream(final int start, final int end) {
		return StreamSupport.intStream(new Sequence(start, end), true);
	}

	/**
	 * <p>
	 * Creates a new Stream using a new Sequence, allowing iteration from start to end using the specified increments.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence, if it can be incremented into from {@code start}, or the number to
	 *            increment up to -- never going past this number
	 * @param step
	 *            The amount to increment
	 * @return A new stream
	 */
	public static IntStream parallelStream(final int start, final int end, final int step) {
		return StreamSupport.intStream(new Sequence(start, end, step), true);
	}

	/**
	 * <p>
	 * Creates a new Stream using a new Sequence, allowing iteration from start to end using the default increment of 1.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence
	 * @return A new stream
	 */
	public static IntStream stream(final int start, final int end) {
		return StreamSupport.intStream(new Sequence(start, end), false);
	}

	/**
	 * <p>
	 * Creates a new Stream using a new Sequence, allowing iteration from start to end using the specified increments.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence, if it can be incremented into from {@code start}, or the number to
	 *            increment up to -- never going past this number
	 * @param step
	 *            The amount to increment
	 * @return A new stream
	 */
	public static IntStream stream(final int start, final int end, final int step) {
		return StreamSupport.intStream(new Sequence(start, end, step), false);
	}

	/**
	 * <p>
	 * Creates a new Sequence with the specified start and end (inclusive), incrementing by one as the sequence is
	 * traversed.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence
	 */
	public Sequence(final int start, final int end) {
		this(start, end, 1);
	}

	/**
	 * <p>
	 * Creates a new Sequence with the specified start and end (inclusive, if it can be stepped into from the start),
	 * incrementing by the value of step, which will always go toward the end value.
	 * </p>
	 *
	 * @param start
	 *            The first number in the sequence
	 * @param end
	 *            The last number in the sequence, if it can be incremented into from {@code start}, or the number to
	 *            increment up to -- never going past this number
	 * @param step
	 *            The amount to increment
	 */
	public Sequence(final int start, final int end, final int step) {
		final int transStep;
		if (step == 0) {
			Sequence.logger.error("We do not allow steps of 0; setting to 1");
			transStep = 1;
		} else {
			transStep = Math.abs(step);
		}

		this.start = start;
		this.place = new AtomicInteger(start);
		this.end = end;

		// If the start is greater, then we're counting down
		if (start > end) {
			this.step = -transStep;
		}
		// We're counting up
		else {
			this.step = transStep;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int characteristics() {
		return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.IMMUTABLE
				| Spliterator.NONNULL | Spliterator.SIZED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object other) {
		final Sequence otherSeq = ConversionUtils.as(Sequence.class, other);

		return Objects.nonNull(otherSeq) && (this.end == otherSeq.end) && (this.start == otherSeq.start)
				&& (this.step == otherSeq.step);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long estimateSize() {
		return Math.abs((this.end - this.place.get()) / this.step) + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<? super Integer> getComparator() {
		return Integer::compare;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(this.end), Integer.valueOf(this.start), Integer.valueOf(this.step));
	}

	/**
	 * <p>
	 * Creates a new parallel stream from the starting position (not the current position) that will run through this
	 * Sequence without modifying the sequence.
	 * </p>
	 *
	 * @return A new stream
	 */
	public Stream<Integer> parallelStream() {
		return StreamSupport.stream(new Sequence(this.start, this.end, this.step), true);
	}

	/**
	 * <p>
	 * Creates a new parallel stream from the current position that will run through this Sequence without modifying the
	 * sequence.
	 * </p>
	 *
	 * @return A new stream
	 */
	public IntStream parallelStreamFromCurrentPosition() {
		return Sequence.parallelStream(this.place.get(), this.end, this.step);
	}

	/**
	 * <p>
	 * Resets the position within the Sequence to the start point.
	 * </p>
	 *
	 * @return The position prior to reset
	 */
	public int reset() {
		return this.place.getAndSet(this.start);
	}

	/**
	 * <p>
	 * Creates a new stream from the starting position (not the current position) that will run through this Sequence
	 * without modifying the sequence.
	 * </p>
	 *
	 * @return A new stream
	 */
	public Stream<Integer> stream() {
		return StreamSupport.stream(new Sequence(this.start, this.end, this.step), false);
	}

	/**
	 * <p>
	 * Creates a new stream from the current position that will run through this Sequence without modifying the
	 * sequence.
	 * </p>
	 *
	 * @return A new stream
	 */
	public IntStream streamFromStartFromCurrentPosition() {
		return Sequence.stream(this.place.get(), this.end, this.step);
	}

	@Override
	public String toString() {
		return "start: " + this.start + " | place: " + this.place + " | end:" + this.end + " | step:" + this.step;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean tryAdvance(final Consumer<? super Integer> action) {
		final int prev = this.place.getAndAdd(this.step);

		// If counting up, our previous should be less than or equal to the end point
		if (this.step > 0) {
			if (prev <= this.end) {
				action.accept(Integer.valueOf(prev));
				return true;
			}
		}
		// If counting down, our previous should be greater than or equal to the end point
		else if (this.step < 0) {
			if (prev >= this.end) {
				action.accept(Integer.valueOf(prev));
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean tryAdvance(final IntConsumer action) {
		final int prev = this.place.getAndAdd(this.step);

		// If counting up, our previous should be less than or equal to the end point
		if (this.step > 0) {
			if (prev <= this.end) {
				action.accept(prev);
				return true;
			}
		}
		// If counting down, our previous should be greater than or equal to the end point
		else if (this.step < 0) {
			if (prev >= this.end) {
				action.accept(prev);
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable java.util.Spliterator.OfInt trySplit() {
		if (this.start == this.end)
			return null;

		final Sequence.FindMiddle findMiddle = new Sequence.FindMiddle(this.step);
		final int prev = this.place.getAndAccumulate(this.end, findMiddle);

		// If counting up, our previous should be less than one left of the median
		if (this.step > 0) {
			if (prev < findMiddle.getOneStepLeftOfMedian())
				return new Sequence(prev, findMiddle.getOneStepLeftOfMedian(), this.step);
		}
		// If counting down, our previous should be greater than one left of the median
		else if (this.step < 0) {
			if (prev > findMiddle.getOneStepLeftOfMedian())
				return new Sequence(prev, findMiddle.getOneStepLeftOfMedian(), this.step);
		}

		return null;
	}
}
