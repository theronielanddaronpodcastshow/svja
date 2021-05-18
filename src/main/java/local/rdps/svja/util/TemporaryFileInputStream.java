package local.rdps.svja.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * This class serves to let us delete temporary files when we close the input stream.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class TemporaryFileInputStream extends FileInputStream {
	private @Nullable File file;

	/**
	 * Creates a {@code FileInputStream} by opening a connection to an actual file, the file named by the {@code File}
	 * object {@code file} in the file system. A new {@code FileDescriptor} object is created to represent this file
	 * connection.
	 * <p>
	 * First, if there is a security manager, its {@code checkRead} method is called with the path represented by the
	 * {@code file} argument as its argument.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be
	 * opened for reading then a {@code FileNotFoundException} is thrown.
	 *
	 * @param file
	 *            the file to be opened for reading.
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a regular file, or for some other reason
	 *             cannot be opened for reading.
	 * @throws SecurityException
	 *             if a security manager exists and its {@code checkRead} method denies read access to the file.
	 * @see java.io.File#getPath()
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public TemporaryFileInputStream(final @NotNull File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}

	/**
	 * Creates a {@code FileInputStream} by opening a connection to an actual file, the file named by the path name
	 * {@code name} in the file system. A new {@code FileDescriptor} object is created to represent this file
	 * connection.
	 * <p>
	 * First, if there is a security manager, its {@code checkRead} method is called with the {@code name} argument as
	 * its argument.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be
	 * opened for reading then a {@code FileNotFoundException} is thrown.
	 *
	 * @param name
	 *            the system-dependent file name.
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a regular file, or for some other reason
	 *             cannot be opened for reading.
	 * @throws SecurityException
	 *             if a security manager exists and its {@code checkRead} method denies read access to the file.
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public TemporaryFileInputStream(final @NotNull String name) throws FileNotFoundException {
		this(new File(name));
	}

	/**
	 * Closes the stream and deletes the file.
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if ((Objects.nonNull(this.file)) && this.file.exists()) {
				this.file.delete();
				this.file = null;
			}
		}
	}
}
