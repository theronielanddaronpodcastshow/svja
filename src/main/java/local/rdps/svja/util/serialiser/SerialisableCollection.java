package local.rdps.svja.util.serialiser;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * This class allows us to serialise and deserialise collections, enforcing type-safety and things of that nature.
 * </p>
 *
 * @param <T>
 *            The type of the Objects we are holding in our Collection
 * @author DaRon
 * @since 1.0
 */
public class SerialisableCollection<T> implements Externalizable, Collection<T> {
	private static final int BUFFER_SIZE = 4;
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 2070000L;
	private Collection<T> collection;

	/**
	 * <p>
	 * This is so that we can externalize the class
	 * </p>
	 */
	public SerialisableCollection() {
		// This is so that we can externalize the class

		super();
	}

	/**
	 * <p>
	 * Encapsulate the given Collection into this handler.
	 * </p>
	 *
	 * @param collection
	 *            The Collection to encapsulate
	 */
	public SerialisableCollection(final Collection<T> collection) {
		this.collection = collection;
	}

	@Override
	public boolean add(final T e) {
		if (Objects.isNull(this.collection)) {
			this.collection = new ArrayList<>();
		}
		return this.collection.add(e);
	}

	@Override
	public boolean addAll(final @NotNull Collection<? extends T> c) {
		if (Objects.isNull(this.collection)) {
			this.collection = new ArrayList<>();
		}
		return this.collection.addAll(c);
	}

	@Override
	public void clear() {
		if (Objects.isNull(this.collection))
			return;
		this.collection.clear();
	}

	@Override
	public boolean contains(final Object o) {
		return Objects.nonNull(this.collection) && this.collection.contains(o);
	}

	@Override
	public boolean containsAll(final @NotNull Collection<?> c) {
		return Objects.nonNull(this.collection) && this.collection.containsAll(c);
	}

	/**
	 * <p>
	 * Return the underlying Collection.
	 * </p>
	 *
	 * @return The underlying Collection
	 */
	public Collection<T> getCollection() {
		return this.collection;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(this.collection) || this.collection.isEmpty();
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		if (Objects.isNull(this.collection))
			return Collections.emptyIterator();
		return this.collection.iterator();
	}

	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final @NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		SerialisableCollection.logger.debug("Constructing object...");

		final int sizecollection = in.readInt();
		this.collection = new ArrayList<>(sizecollection + SerialisableCollection.BUFFER_SIZE);

		// TODO make this recurse on collections, maps, etc
		for (int i = 0; i < sizecollection; i++) {
			final Object obj = in.readObject();
			if (obj instanceof SerialisableDeterminant) {
				this.collection.add((T) ((SerialisableDeterminant) obj).getObject());
			} else {
				this.collection.add((T) obj);
			}
		}
	}

	@Override
	public boolean remove(final Object o) {
		return Objects.nonNull(this.collection) && this.collection.remove(o);
	}

	@Override
	public boolean removeAll(final @NotNull Collection<?> c) {
		return Objects.nonNull(this.collection) && this.collection.removeAll(c);
	}

	@Override
	public boolean retainAll(final @NotNull Collection<?> c) {
		return Objects.nonNull(this.collection) && this.collection.retainAll(c);
	}

	@Override
	public int size() {
		if (Objects.isNull(this.collection))
			return 0;
		return this.collection.size();
	}

	@Override
	public @NotNull Object[] toArray() {
		if (Objects.isNull(this.collection))
			return CommonConstants.EMPTY_OBJECT_ARRAY;
		return this.collection.toArray();
	}

	@Override
	public @NotNull <K> K[] toArray(final @NotNull K[] a) {
		if (Objects.isNull(this.collection))
			return a;
		return this.collection.toArray(a);
	}

	@Override
	public @NotNull String toString() {
		if (Objects.isNull(this.collection))
			return "";
		final StringBuilder sb = new StringBuilder(512);

		for (final T item : this.collection) {
			sb.append(item).append(", ");
		}

		return sb.deleteCharAt(sb.length() - 2).toString();
	}

	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final @NotNull ObjectOutput out) throws IOException {
		SerialisableCollection.logger.debug("Externalizing object...");

		if (Objects.nonNull(this.collection)) {
			out.writeInt(this.collection.size());
			for (final T item : this.collection) {
				// Recurse on collections
				if (item instanceof Collection) {
					out.writeObject(new SerialisableCollection<>((Collection<?>) item));
				} else if (item instanceof Map) {
					out.writeObject(new SerialisableMap<>((Map<?, ?>) item));
					// No recursion needed
				} else {
					out.writeObject(new SerialisableDeterminant(item));
				}
			}
		} else {
			out.writeInt(0);
		}
	}
}
