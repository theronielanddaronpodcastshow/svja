package local.rdps.svja.blo;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.output.CsvOut;
import local.rdps.svja.util.output.ExcelOut;
import local.rdps.svja.vo.FileVo;
import local.rdps.svja.vo.ProjectVo;

/**
 * <p>
 * This class is the primer workhorse for any file request.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class FilesBlo {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * <p>
	 * This method creates a CSV export using the given data.
	 * </p>
	 *
	 * @param projects
	 *            The projects to export
	 * @return The export
	 */
	static @Nullable FileVo createCsvExport(final @Nullable ProjectVo... projects) {
		try (final CsvOut<ProjectVo> export = new CsvOut<>()) {
			export.writeLineToMainOutput(projects);
			return export.export();
		} catch (final IOException e) {
			FilesBlo.logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * <p>
	 * This method creates an excel export (XLSX) using the given data.
	 * </p>
	 *
	 * @param projects
	 *            The projects to export
	 * @return The export
	 */
	static @Nullable FileVo createExcelExport(final @Nullable ProjectVo... projects) {
		try (final ExcelOut export = new ExcelOut()) {
			export.addWorksheet("Projects Export", 5);
			export.writeLineToMainOutput("Project ID", "Project Title", "Project Description", "Last Edited By",
					"Last Edited By Username", "Last Modified Date");
			for (final ProjectVo project : projects) {
				try {
					export.writeLineToMainOutput(project.getId().toString(), project.getTitle(),
							project.getDescription(), project.getModifiedBy().toString(),
							project.getModifiedByUser().getUsername(), project.getModifiedDate().toString());
				} catch (final ApplicationException e) {
					export.writeLineToMainOutput(project.getId().toString(), project.getTitle(),
							project.getDescription(), project.getModifiedBy().toString(), "",
							project.getModifiedDate().toString());
				}
			}
			return export.export();
		}
	}

	/**
	 * <p>
	 * This method grabs the given file and file data from the file system and database. This method searches for the
	 * file using the file ID.
	 * </p>
	 *
	 * @param fileId
	 *            The ID of the file to retrieve from the database
	 * @return A filled in {@link FileVo}
	 * @throws ApplicationException
	 *             If an exception occurred trying to grab the file
	 */
	static @Nullable FileVo getFile(@NotNull final Long fileId) throws ApplicationException {
		final Collection<FileVo> files = CommonDaoGateway.getItems(new FileVo(fileId));

		return files.stream().findFirst().map(f -> FileVo.mergeFileVos(f, FilesBlo.getFile(f.getFile()))).orElse(null);
	}

	/**
	 * <p>
	 * This method grabs the given file and file data from the file system and database. This method searches for the
	 * file using the file name.
	 * </p>
	 *
	 * @param fileName
	 *            The name of the file
	 * @return A filled in {@link FileVo}
	 */
	static @Nullable FileVo getFile(@NotNull final String fileName) {
		final FileVo file = new FileVo();
		file.setFile(fileName);

		return file;
	}
}
