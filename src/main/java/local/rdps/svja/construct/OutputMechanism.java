package local.rdps.svja.construct;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>
 * This interface provides a clean, clear mechanism for pushing reports to any of the output mechanisms.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public interface OutputMechanism extends AutoCloseable {
	/**
	 * <p>
	 * This method closes all underlying resources and ensures that the output mechanism is disposed of, which includes
	 * finalizing the output and ferrying it along its way.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	void close() throws IOException;

	/**
	 * <p>
	 * This method creates a temporary file with the given name, if it makes sense for the mechanism, rather than using
	 * a randomly named one if {@link #writeLineToTemporaryFile(String...)} is called first. This method also signals
	 * that yet another file, if appropriate, is made and {@link #writeLineToTemporaryFile(String...)} will point to
	 * that file. The file may or may not be compressed, depending on the mechanism and the size.
	 * </p>
	 *
	 * @param filename
	 *            The filename for the temporary file
	 */
	void createTemporaryFile(String filename);

	/**
	 * <p>
	 * This method returns the underlying {@link OutputStream} that is managed by the output mechanism.
	 * </p>
	 *
	 * @param line
	 *            The data to write to the main output stream
	 */
	void writeLineToMainOutput(String... line);

	/**
	 * <p>
	 * This method writes, if applicable, an {@link OutputStream}, which is managed by the output mechanism, that is
	 * tied to a temporary file. If {@link #createTemporaryFile(String)} has been called, then the stream is from that
	 * file (the one pointed to by the last call to {@link #createTemporaryFile(String)}), otherwise it is a randomly
	 * named file. If a temporary file is not applicable to this mechanism, it writes to the {@link OutputStream} used
	 * by {@link #writeLineToMainOutput(String...)}.
	 * </p>
	 *
	 * @param line
	 *            The data to write to the temporary file
	 */
	void writeLineToTemporaryFile(String... line);
}
