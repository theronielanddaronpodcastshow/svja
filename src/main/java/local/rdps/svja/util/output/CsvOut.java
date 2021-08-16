package local.rdps.svja.util.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.construct.OutputMechanism;
import local.rdps.svja.vo.FileVo;

/**
 * <p>
 * A class that makes CSV exports.
 * </p>
 *
 * @param <Vo>
 *            The {@link ItemVo} to write to the CSV.
 * @author DaRon
 * @since 1.0
 */
public class CsvOut<Vo extends ItemVo> implements OutputMechanism {
	private static final Logger logger = LogManager.getLogger();
	private final StatefulBeanToCsv<Vo> csvBuilder;
	private Path finalPath;
	private java.io.File reportFile;
	private final Writer writer;

	/**
	 * <p>
	 * This constructor creates a new instance of ExcelOut using a temporary file.
	 * </p>
	 */
	public CsvOut() {
		// Set up a temporary file
		final FileAttribute<Set<PosixFilePermission>> permissions = PosixFilePermissions
				.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
		Writer writer;
		StatefulBeanToCsv<Vo> csvBuilder;

		try {
			this.finalPath = Files.createTempFile("export-", "-main.csv", permissions);
			this.reportFile = this.finalPath.toFile();
			this.reportFile.deleteOnExit();

			writer = new FileWriter(this.reportFile);
			csvBuilder = new StatefulBeanToCsvBuilder<Vo>(writer).withEscapechar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
					.withLineEnd(ICSVWriter.RFC4180_LINE_END).withQuotechar(ICSVWriter.DEFAULT_QUOTE_CHARACTER)
					.withSeparator(ICSVWriter.DEFAULT_SEPARATOR).build();
		} catch (final IOException e) {
			CsvOut.logger.fatal(e.getMessage(), e);
			writer = null;
			csvBuilder = null;
		}

		this.writer = writer;
		this.csvBuilder = csvBuilder;
	}

	/**
	 * <p>
	 * Creates a new instance of ExcelOut for the Utility Suite.
	 * </p>
	 *
	 * @param exportFile
	 * @throws IOException
	 */
	public CsvOut(final @NotNull File exportFile) throws IOException {
		if (exportFile == null) {
			CsvOut.logger.error("File required to create excel export, falling back to default.");
			final FileAttribute<Set<PosixFilePermission>> permissions = PosixFilePermissions
					.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
			this.finalPath = Files.createTempFile("export", "csv", permissions);
			this.reportFile = this.finalPath.toFile();
			this.reportFile.deleteOnExit();
		} else {
			this.finalPath = exportFile.toPath();
			this.reportFile = exportFile;
		}

		this.writer = new FileWriter(this.reportFile);
		this.csvBuilder = new StatefulBeanToCsvBuilder<Vo>(this.writer)
				.withEscapechar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER).withLineEnd(ICSVWriter.RFC4180_LINE_END)
				.withQuotechar(ICSVWriter.DEFAULT_QUOTE_CHARACTER).withSeparator(ICSVWriter.DEFAULT_SEPARATOR).build();
	}

	@Override
	public void close() throws IOException {
		if (Objects.isNull(this.reportFile)) {
			this.reportFile = this.finalPath.toFile();
		}
		export();
	}

	/**
	 * <p>
	 * This method does nothing -- {@link CsvOut} does not support the concept of temporary files and, instead, will
	 * write to the main file.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void createTemporaryFile(final String filename) {
		// Do nothing
	}

	/**
	 * <p>
	 * Prepares the CSV file for export.
	 * </p>
	 *
	 * @return FileVo pointing to the file to be exported
	 * @throws IOException
	 *             If there is a problem trying to close the writer
	 */
	public @Nullable FileVo export() throws IOException {
		this.writer.close();
		final FileVo file = new FileVo();
		file.setFile(this.reportFile.getAbsolutePath());
		return file;
	}

	@Override
	public void writeLineToMainOutput(final String... line) {
		if (Objects.nonNull(line)) {
			try {
				this.writer.write(Arrays.stream(line)
						.collect(Collectors.joining("\",\"", "\"", "\"" + ICSVWriter.RFC4180_LINE_END)));
			} catch (final IOException e) {
				CsvOut.logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 *
	 * <p>
	 * This method writes each of the given items to the CSV.
	 * </p>
	 *
	 * @param items
	 *            The items to write to the CSV
	 */
	public void writeLineToMainOutput(final Vo... items) {
		if (Objects.nonNull(items)) {
			for (final Vo item : items) {
				try {
					this.csvBuilder.write(item);
				} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
					CsvOut.logger.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * <p>
	 * This method does nothing -- {@link CsvOut} does not support the concept of temporary files and, instead, will
	 * write to the main file.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void writeLineToTemporaryFile(final String... line) {
		writeLineToMainOutput(line);
	}

	/**
	 * <p>
	 * This method does nothing -- {@link CsvOut} does not support the concept of temporary files and, instead, will
	 * write to the main file.
	 * </p>
	 *
	 * @param items
	 *            The items to write to the CSV
	 * @see #writeLineToMainOutput(ItemVo...)
	 */
	public void writeLineToTemporaryFile(final Vo... items) {
		writeLineToMainOutput(items);
	}
}
