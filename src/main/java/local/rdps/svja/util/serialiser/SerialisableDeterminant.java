package local.rdps.svja.util.serialiser;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * This class works to provide a tight method for serialising and deserialising primitives, maps, and collections. While
 * it is largely designed to operate on the above, it will handle any non-primitive (or Objects that can't be reduced to
 * a primitive) by doing a writeObject and readObject call. For boxed primitives we unbox and then write, which is much
 * slimmer. For collections we use SerialisableCollection. For maps we use SerialisableMap. Because of that, calls can
 * be made through this rather than through SerialisableCollection or SerialisableMap with minimal overhead.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SerialisableDeterminant implements Externalizable {
	private static final byte BOOL_VAL = 0x01;
	private static final byte BYTE_VAL = 0x02;
	private static final byte CHAR_VAL = 0x03;
	private static final byte COLLECTION_VAL = 0x04;
	private static final byte DOUBLE_VAL = 0x05;
	private static final byte FLOAT_VAL = 0x06;
	private static final byte INT_VAL = 0x07;
	private static final Logger logger = LogManager.getLogger();
	private static final byte LONG_VAL = 0x08;
	private static final byte MAP_VAL = 0x09;
	private static final long serialVersionUID = 2070000L;
	private static final byte SHORT_VAL = 0x10;
	private static final byte UNKNOWN_VAL = 0x11;
	private Object obj;
	private byte type;

	/**
	 * <p>
	 * This is so that we can externalize the class. <strong>Do NOT instantiate using this constructor.</strong>
	 * </p>
	 */
	public SerialisableDeterminant() {
		// This is so that we can externalize the class

		super();
	}

	/**
	 * <p>
	 * This is the constructor to actually MAKE a SerialisableDerminant.
	 * </p>
	 *
	 * @param obj
	 *            The object we wish to work with
	 */
	public SerialisableDeterminant(final Object obj) {
		this.obj = obj;
		this.type = getType();
	}

	/**
	 * <p>
	 * Returns an integer that symbolizes the type of primitive or known object that makes up the stored Object.
	 * </p>
	 *
	 * @return An instance telling us which type the object is
	 */
	byte getType() {
		if (this.obj instanceof Boolean)
			return SerialisableDeterminant.BOOL_VAL;
		if (this.obj instanceof Byte)
			return SerialisableDeterminant.BYTE_VAL;
		if (this.obj instanceof Character)
			return SerialisableDeterminant.CHAR_VAL;
		if (this.obj instanceof Double)
			return SerialisableDeterminant.DOUBLE_VAL;
		if (this.obj instanceof Float)
			return SerialisableDeterminant.FLOAT_VAL;
		if (this.obj instanceof Integer)
			return SerialisableDeterminant.INT_VAL;
		if (this.obj instanceof Long)
			return SerialisableDeterminant.LONG_VAL;
		if (this.obj instanceof Short)
			return SerialisableDeterminant.SHORT_VAL;
		if (this.obj instanceof Collection)
			return SerialisableDeterminant.COLLECTION_VAL;
		if (this.obj instanceof Map)
			return SerialisableDeterminant.MAP_VAL;
		return SerialisableDeterminant.UNKNOWN_VAL;
	}

	/**
	 * <p>
	 * Returns the object held by this class.
	 * </p>
	 *
	 * @return The object we're holding
	 */
	public Object getObject() {
		return this.obj;
	}

	/**
	 * <p>
	 * Returns true if the stored Object is a known Object type and, as such, will be handled specially (note that most
	 * Objects don't need to be known or handled specially).
	 * </p>
	 *
	 * @return {@code true} iff the Object is of a known type
	 */
	public boolean isAKnownObject() {
		return this.type != SerialisableDeterminant.UNKNOWN_VAL;
	}

	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final @NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		SerialisableDeterminant.logger.debug("Constructing object...");

		this.type = in.readByte();
		if (SerialisableDeterminant.logger.isDebugEnabled()) {
			SerialisableDeterminant.logger.debug("Reading an item with a type of: {}", this.type);
		}
		switch (this.type) {
			case SerialisableDeterminant.BOOL_VAL:
				this.obj = Boolean.valueOf(in.readBoolean());
				break;
			case SerialisableDeterminant.BYTE_VAL:
				this.obj = Byte.valueOf(in.readByte());
				break;
			case SerialisableDeterminant.CHAR_VAL:
				this.obj = Character.valueOf(in.readChar());
				break;
			case SerialisableDeterminant.COLLECTION_VAL:
				this.obj = ((SerialisableCollection<?>) in.readObject()).getCollection();
				break;
			case SerialisableDeterminant.DOUBLE_VAL:
				this.obj = Double.valueOf(in.readDouble());
				break;
			case SerialisableDeterminant.FLOAT_VAL:
				this.obj = Float.valueOf(in.readFloat());
				break;
			case SerialisableDeterminant.INT_VAL:
				this.obj = Integer.valueOf(in.readInt());
				break;
			case SerialisableDeterminant.LONG_VAL:
				this.obj = Long.valueOf(in.readLong());
				break;
			case SerialisableDeterminant.MAP_VAL:
				this.obj = ((SerialisableMap<?, ?>) in.readObject()).getMap();
				break;
			case SerialisableDeterminant.SHORT_VAL:
				this.obj = Short.valueOf(in.readShort());
				break;
			default:
				this.obj = in.readObject();
				break;
		}
	}

	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final @NotNull ObjectOutput out) throws IOException {
		SerialisableDeterminant.logger.debug("Externalizing object...");

		out.writeByte(this.type);
		if (SerialisableDeterminant.logger.isDebugEnabled()) {
			SerialisableDeterminant.logger.debug("Writing an item with a type of: {}", this.type);
		}
		switch (this.type) {
			case SerialisableDeterminant.BOOL_VAL:
				out.writeBoolean(((Boolean) this.obj).booleanValue());
				break;
			case SerialisableDeterminant.BYTE_VAL:
				out.writeByte(((Byte) this.obj).byteValue());
				break;
			case SerialisableDeterminant.CHAR_VAL:
				out.writeChar((((Character) this.obj).charValue()));
				break;
			case SerialisableDeterminant.COLLECTION_VAL:
				out.writeObject(new SerialisableCollection<>((Collection<?>) this.obj));
				break;
			case SerialisableDeterminant.DOUBLE_VAL:
				out.writeDouble(((Double) this.obj).doubleValue());
				break;
			case SerialisableDeterminant.FLOAT_VAL:
				out.writeFloat(((Float) this.obj).floatValue());
				break;
			case SerialisableDeterminant.INT_VAL:
				out.writeInt(((Integer) this.obj).intValue());
				break;
			case SerialisableDeterminant.LONG_VAL:
				out.writeLong(((Long) this.obj).longValue());
				break;
			case SerialisableDeterminant.MAP_VAL:
				out.writeObject(new SerialisableMap<>((Map<?, ?>) this.obj));
				break;
			case SerialisableDeterminant.SHORT_VAL:
				out.writeShort(((Short) this.obj).shortValue());
				break;
			default:
				out.writeObject(this.obj);
				break;
		}
	}
}
