package local.rdps.svja.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * This class creates an {@link HttpServletRequest} with an infinitely readable InputStream and Reader.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class InfinitelyReadableHttpServletRequest extends HttpServletRequestWrapper {
	private static final Logger _logger = LogManager.getLogger();
	/**
	 * The input stream tied to this request
	 */
	private ImprovedServletInputStream inputStream;

	/**
	 * <p>
	 * This constructor creates a new instance of the
	 * </p>
	 *
	 * @param request
	 *            The original request to "wrap" around
	 */
	public InfinitelyReadableHttpServletRequest(final HttpServletRequest request) {
		super(request);

		// Grab the data from the Reader once and store it because HttpServletRequest is not Closeable or
		// AutoCloseable, pump the body into something that cannot leak
		try (InputStream s = request.getInputStream()) {
			if (Objects.nonNull(s)) {
				int singleByte;
				final List<Byte> data = new LinkedList<>();
				while ((singleByte = s.read()) >= 0) {
					data.add(Byte.valueOf((byte) singleByte));
				}

				final byte[] fullData = new byte[data.size()];
				for (int i = 0; i < fullData.length; i++) {
					fullData[i] = data.get(i).byteValue();
				}
				this.inputStream = new ImprovedServletInputStream(ByteBuffer.wrap(fullData));
			}
		} catch (final IOException e) {
			InfinitelyReadableHttpServletRequest._logger
					.error("While trying to run through the request's Reader we had an error", e);
			this.inputStream = new ImprovedServletInputStream(ByteBuffer.allocate(0));
		}
	}

	@Override
	public ImprovedServletInputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(this.inputStream));
	}

	/**
	 * <p>
	 * This method sets the underling input stream to a new stream that utilizes the given {@link ByteBuffer}.
	 * </p>
	 *
	 * @param data
	 *            The new data to make up the underlying input stream
	 */
	public void setInputStream(final ByteBuffer data) {
		this.inputStream = new ImprovedServletInputStream(data);
	}

	/**
	 * <p>
	 * This method sets the underling input stream to the give input stream.
	 * </p>
	 *
	 * @param inputStream
	 *            The new input stream to be tied to this instance
	 */
	public void setInputStream(final ImprovedServletInputStream inputStream) {
		this.inputStream = inputStream;
	}
}
