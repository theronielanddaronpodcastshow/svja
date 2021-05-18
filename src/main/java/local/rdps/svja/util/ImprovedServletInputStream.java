package local.rdps.svja.util;

import java.nio.ByteBuffer;
import java.util.Objects;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.jetbrains.annotations.Nullable;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * This class implements an improved {@link ServletInputStream} that supports mark, reset, and other functionality
 * unavailable to users of the {@link ServletInputStream}.
 * </p>
 * <p>
 * Please note that this class is not thread safe.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ImprovedServletInputStream extends ServletInputStream {
	/**
	 * The data of which this stream is composed
	 */
	private final ByteBuffer data;

	/**
	 * <p>
	 * This constructor creates a new instance using the provided {@link ByteBuffer} to offer an array-backed
	 * {@link ServletInputStream} that allows marking and resetting the stream.
	 * </p>
	 *
	 * @param data
	 *            The {@link ByteBuffer} filled with data to leverage
	 */
	public ImprovedServletInputStream(@Nullable final ByteBuffer data) {
		if (Objects.isNull(data)) {
			this.data = ByteBuffer.allocate(0).asReadOnlyBuffer();
		} else {
			this.data = data.duplicate().asReadOnlyBuffer();
		}
		this.data.rewind();
	}

	/**
	 * <p>
	 * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without
	 * blocking by the next invocation of a method for this input stream. The next invocation might be the same thread
	 * or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes.
	 * </p>
	 */
	@Override
	public int available() {
		return this.data.remaining();
	}

	/**
	 * <p>
	 * Clears this stream. The position is set to {@code 0}, the limit is set to the capacity, and the mark is
	 * discarded.
	 * </p>
	 *
	 * <p>
	 * This method does not actually clear or close the underlying array, but does prevent the stream from returning any
	 * data on future read operations.
	 * </p>
	 */
	@Override
	public void close() {
		this.data.clear();
	}

	@Override
	public boolean isFinished() {
		return ValidationUtils.not(this.data.hasRemaining());
	}

	@Override
	public boolean isReady() {
		return true;
	}

	/**
	 * <p>
	 * Marks the current position in this input stream. A subsequent call to the {@code reset} method repositions this
	 * stream at the last marked position so that subsequent reads re-read the same bytes.
	 * </p>
	 * <p>
	 * The readlimit arguments is ignored in this implementation.
	 * </p>
	 * <p>
	 * The general contract of mark is that, if the method markSupported returns true, the stream remembers all the
	 * bytes read after the call to mark and stands ready to supply those same bytes again if and whenever the method
	 * reset is called. However, the stream is not required to remember any data at all if more than readlimit bytes are
	 * read from the stream before reset is called, although this implementation ignored readlimit so will always
	 * remember the marked position.
	 * </p>
	 * <p>
	 * Marking a closed stream should not have any effect on the stream.
	 * </p>
	 *
	 * @param readlimit
	 *            This parameter is ignored due to the underlying structure backing this input stream
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		this.data.mark();
	}

	/**
	 * <p>
	 * Returns {@code true} as this input stream supports the mark and reset methods.
	 * </p>
	 *
	 * @return {@code true} as mark and reset are always supported by this class
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * <p>
	 * Reads the next byte of data from the input stream. The value byte is returned as an {@code int} in the range
	 * {@code 0} to {@code 255}. If no byte is available because the end of the stream has been reached, the value
	 * {@code -1} is returned.
	 * </p>
	 * <p>
	 * This method is non-blocking
	 * </p>
	 */
	@Override
	public int read() {
		if (this.data.remaining() > 0)
			return (0xFF) & this.data.get();
		return -1;
	}

	/**
	 * <p>
	 * Reads some number of bytes from the input stream and stores them into the buffer array {@code b}. The number of
	 * bytes actually read is returned as an integer.
	 * </p>
	 * <p>
	 * This method is non-blocking
	 * </p>
	 * <p>
	 * If the length of {@code b} is zero, then no bytes are read and {@code 0} is returned; otherwise, there is an
	 * attempt to read at least one byte. If no byte is available because the stream is at the end of the file, the
	 * value {@code -1} is returned; otherwise, at least one byte is read and stored into {@code b}.
	 * </p>
	 * <p>
	 * The first byte read is stored into element {@code b[0]}, the next one into {@code b[1]}, and so on. The number of
	 * bytes read is, at most, equal to the length of {@code b}. Let {@code k} be the number of bytes actually read;
	 * these bytes will be stored in elements {@code b[0]} through {@code b[k-1]}, leaving elements {@code b[k]} through
	 * {@code b[b.length-1]} unaffected.
	 * </p>
	 * <p>
	 * The {@code read(b)} method for class InputStream has the same effect as:
	 * </p>
	 * <p>
	 * {@code read(b, 0, b.length)}
	 * </p>
	 */
	@Override
	public int read(final byte[] b) {
		if (Objects.isNull(b))
			return 0;

		return read(b, 0, b.length);
	}

	/**
	 * <p>
	 * Reads up to {@code len} bytes of data from the input stream into an array of bytes. An attempt is made to read as
	 * many as {@code len} bytes, but a smaller number may be read. The number of bytes actually read is returned as an
	 * integer.
	 * </p>
	 * <p>
	 * This method is non-blocking.
	 * </p>
	 * <p>
	 * If {@code len} is zero, then no bytes are read and {@code 0} is returned; otherwise, there is an attempt to read
	 * at least one byte. If no byte is available because the stream is at end of file, the value {@code -1} is
	 * returned; otherwise, at least one byte is read and stored into {@code b}.
	 * </p>
	 * <p>
	 * The first byte read is stored into element {@code b[off]}, the next one into {@code b[off+1]}, and so on. The
	 * number of bytes read is, at most, equal to {@code len}. Let {@code k} be the number of bytes actually read; these
	 * bytes will be stored in elements {@code b[off]} through {@code b[off+k-1]}, leaving elements {@code b[off+k]}
	 * through {@code b[off+len-1]} unaffected.
	 * </p>
	 * <p>
	 * In every case, elements {@code b[0]} through {@code b[off]} and elements {@code b[off+len]} through
	 * {@code b[b.length-1]} are unaffected.
	 * </p>
	 *
	 * @throws IndexOutOfBoundsException
	 *             If len or off is less than 0
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) {
		if (Objects.isNull(b) || (b.length == 0) || (len == 0))
			return 0;
		if (!this.data.hasRemaining())
			return -1;
		if (off < 0)
			throw new IndexOutOfBoundsException("The offset cannot be less than 0");
		if (len < 0)
			throw new IndexOutOfBoundsException("The length cannot be less than 0");

		final int length = Math.min(Math.min(b.length - off, len), this.data.remaining());
		if (length < 0)
			return -1;

		this.data.get(b, off, length);
		return length;
	}

	/**
	 * <p>
	 * Reads all remaining bytes from the input stream. This method does not close the input stream.
	 * </p>
	 * <p>
	 * This method is non-blocking.
	 * </p>
	 * <p>
	 * When this stream reaches end of stream, further invocations of this method will return an empty byte array.
	 * </p>
	 * <p>
	 * Note that this method is intended for simple cases where it is convenient to read all bytes into a byte array. It
	 * is not intended for reading input streams with large amounts of data.
	 * </p>
	 * <p>
	 * The behavior for the case where the input stream is asynchronously closed, or the thread interrupted during the
	 * read, is highly input stream specific, and therefore not specified.
	 * </p>
	 * <p>
	 * If an I/O error occurs reading from the input stream, then it may do so after some, but not all, bytes have been
	 * read. Consequently the input stream may not be at end of stream and may be in an inconsistent state. It is
	 * strongly recommended that the stream be promptly closed if an I/O error occurs.
	 * </p>
	 *
	 * @return An array containing all remaining bytes from the input stream
	 */
	@Override
	public byte[] readAllBytes() {
		if (!this.data.hasRemaining())
			return CommonConstants.EMPTY_BYTE_ARRAY;

		final byte[] buffer = new byte[this.data.remaining()];
		if (read(buffer) == buffer.length)
			return buffer;
		return readAllBytes();
	}

	/**
	 * <p>
	 * Reads up to {@code len} bytes of data from the input stream into an array of bytes, stopping when the end of the
	 * stream is reached, a newline character is reached, or {@code len} is reached -- whatever happens first. An
	 * attempt is made to read as many as {@code len} bytes, but a smaller number may be read. The number of bytes
	 * actually read is returned as an integer.
	 * </p>
	 * <p>
	 * This method is non-blocking.
	 * </p>
	 * <p>
	 * If {@code len} is zero, then no bytes are read and {@code 0} is returned; otherwise, there is an attempt to read
	 * at least one byte. If no byte is available because the stream is at end of file, the value {@code -1} is
	 * returned; otherwise, at least one byte is read and stored into {@code b}.
	 * </p>
	 * <p>
	 * The first byte read is stored into element {@code b[off]}, the next one into {@code b[off+1]}, and so on. The
	 * number of bytes read is, at most, equal to {@code len}. Let {@code k} be the number of bytes actually read; these
	 * bytes will be stored in elements {@code b[off]} through {@code b[off+k-1]}, leaving elements {@code b[off+k]}
	 * through {@code b[off+len-1]} unaffected.
	 * </p>
	 * <p>
	 * In every case, elements {@code b[0]} through {@code b[off]} and elements {@code b[off+len]} through
	 * {@code b[b.length-1]} are unaffected.
	 * </p>
	 *
	 * @param b
	 *            the buffer into which the data is read
	 * @param off
	 *            the buffer into which the data is read.
	 * @param len
	 *            the buffer into which the data is read
	 * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
	 *         stream has been reached
	 */
	@Override
	public int readLine(final byte[] b, final int off, final int len) {
		if (Objects.isNull(b) || (b.length == 0) || (len == 0))
			return 0;
		if (!this.data.hasRemaining())
			return -1;
		if (off < 0)
			throw new IndexOutOfBoundsException("The offset cannot be less than 0");
		if (len < 0)
			throw new IndexOutOfBoundsException("The length cannot be less than 0");

		int length = 0;
		byte singleByte;
		while ((b.length > (off + length)) && this.data.hasRemaining() && ((singleByte = this.data.get()) != (byte) 0xa)
				&& (length < len)) {
			b[length + off] = singleByte;
			length++;
		}

		return length;
	}

	/**
	 * <p>
	 * Resets this input stream's position to the previously-marked position.
	 * </p>
	 * <p>
	 * Invoking this method neither changes nor discards the mark's value.
	 * </p>
	 */
	@Override
	public synchronized void reset() {
		this.data.reset();
	}

	@Override
	public void setReadListener(final ReadListener readListener) {
		// Do nothing
	}

	/**
	 * <p>
	 * Skips over and discards {@code n} bytes of data from this input stream. The skip method may, for a variety of
	 * reasons, end up skipping over some smaller number of bytes, possibly {@code 0}. This may result from any of a
	 * number of conditions; reaching the end of the stream before {@code n} bytes have been skipped is only one
	 * possibility. The actual number of bytes skipped is returned. If {@code n} is negative, the skip method always
	 * returns {@code 0}, and no bytes are skipped.
	 * </p>
	 */
	@Override
	public long skip(final long n) {
		if (n < 0)
			return -1L;

		final int length = Math.min((int) Math.min(n, Integer.MAX_VALUE), this.data.remaining());
		// We use get so that we don't lose the mark like we would if we used position()
		this.data.get(new byte[length]);
		return length;
	}
}
