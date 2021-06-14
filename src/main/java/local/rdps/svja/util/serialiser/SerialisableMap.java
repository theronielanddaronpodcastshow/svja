package local.rdps.svja.util.serialiser;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class allows us to serialise and deserialise maps, enforcing type-safety and things of that nature.
 * </p>
 *
 * @param <K>
 *            The type for the keys
 * @param <V>
 *            The type for the values
 *
 * @author DaRon
 * @since 1.0
 */
public class SerialisableMap<K, V> implements Externalizable, Map<K, V> {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 2070000L;
	private Map<K, V> map;

	/**
	 * <p>
	 * This is so that we can externalize the class.
	 * </p>
	 */
	public SerialisableMap() {
		// This is so that we can externalize the class

		super();
	}

	/**
	 * <p>
	 * Encapsulate the given map into this handler.
	 * </p>
	 *
	 * @param map
	 *            The map to handle
	 */
	public SerialisableMap(final Map<K, V> map) {
		this.map = map;
	}

	@Override
	public void clear() {
		if (Objects.isNull(this.map))
			return;
		this.map.clear();
	}

	@Override
	public boolean containsKey(final Object key) {
		return Objects.nonNull(this.map) && this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return Objects.nonNull(this.map) && this.map.containsValue(value);
	}

	@Override
	public @NotNull Set<Map.Entry<K, V>> entrySet() {
		if (Objects.isNull(this.map))
			return Collections.emptySet();
		return this.map.entrySet();
	}

	@Override
	public @Nullable V get(final Object key) {
		if (Objects.isNull(this.map))
			return null;
		return this.map.get(key);
	}

	/**
	 * <p>
	 * Return the underlying map.
	 * </p>
	 *
	 * @return The underlying map
	 */
	public Map<K, V> getMap() {
		return this.map;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(this.map) || this.map.isEmpty();
	}

	@Override
	public @NotNull Set<K> keySet() {
		if (Objects.isNull(this.map))
			return Collections.emptySet();
		return this.map.keySet();
	}

	@Override
	public V put(final K key, final V value) {
		if (Objects.isNull(this.map)) {
			this.map = new HashMap<>();
		}
		return this.map.put(key, value);
	}

	@Override
	public void putAll(final @NotNull Map<? extends K, ? extends V> m) {
		if (Objects.isNull(this.map)) {
			this.map = new HashMap<>();
		}
		this.map.putAll(m);
	}

	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final @NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		SerialisableMap.logger.debug("Constructing object...");

		final int sizemap = in.readInt();
		this.map = new HashMap<>(sizemap + 4);

		// TODO Recurse if we're pumping in collections/maps/etc
		for (int i = 0; i < sizemap; i++) {
			final K keymap = (K) in.readObject();
			final Object v = in.readObject();

			final V value = (V) (v instanceof SerialisableDeterminant ? ((SerialisableDeterminant) v).getObject() : v);
			if (!ValidationUtils.isEmpty(keymap)) {
				this.map.put(keymap, value);
			}
		}
	}

	@Override
	public @Nullable V remove(final Object key) {
		if (Objects.isNull(this.map))
			return null;
		return this.map.remove(key);
	}

	@Override
	public int size() {
		if (Objects.isNull(this.map))
			return 0;
		return this.map.size();
	}

	@Override
	public @NotNull String toString() {
		if (Objects.isNull(this.map))
			return "";
		final StringBuilder sb = new StringBuilder(512);

		for (final Map.Entry<K, V> item : this.map.entrySet()) {
			sb.append(item.getKey()).append(':').append(item.getValue()).append(", ");
		}

		return sb.deleteCharAt(sb.length() - 2).toString();
	}

	@Override
	public @NotNull Collection<V> values() {
		if (Objects.isNull(this.map))
			return Collections.emptyList();
		return this.map.values();
	}

	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final @NotNull ObjectOutput out) throws IOException {
		SerialisableMap.logger.debug("Externalizing object...");

		if (Objects.nonNull(this.map)) {
			out.writeInt(this.map.size());
			for (final Map.Entry<K, V> entry : this.map.entrySet()) {
				final K key = entry.getKey();
				// Recurse on collections
				if (key instanceof Collection) {
					out.writeObject(new SerialisableCollection<>((Collection<?>) key));
				} else if (key instanceof Map) {
					out.writeObject(new SerialisableMap<>((Map<?, ?>) key));
					// No recursion needed
				} else {
					out.writeObject(key);
				}

				final V value = entry.getValue();
				// Recurse on collections
				if (value instanceof Collection) {
					out.writeObject(new SerialisableCollection<>((Collection<?>) value));
				} else if (value instanceof Map) {
					out.writeObject(new SerialisableMap<>((Map<?, ?>) value));
					// No recursion needed
				} else {
					out.writeObject(new SerialisableDeterminant(value));
				}
			}
		} else {
			out.writeInt(0);
		}
	}
}
