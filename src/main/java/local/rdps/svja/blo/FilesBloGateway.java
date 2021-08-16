package local.rdps.svja.blo;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.FileVo;
import local.rdps.svja.vo.ProjectVo;

/**
 * <p>
 * This class serves as the gateway for the {@link FilesBlo}, giving all other packages access to the methods contained
 * therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class FilesBloGateway {
	/**
	 * <p>
	 * This method creates a CSV export using the given data.
	 * </p>
	 *
	 * @param projects
	 *            The projects to export
	 * @return The export
	 */
	public static @Nullable FileVo createCsvExport(final @Nullable ProjectVo... projects) {
		if (Objects.isNull(projects) || (projects.length < 1))
			return null;

		return FilesBlo.createCsvExport(projects);
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
	public static @Nullable FileVo createExcelExport(final @Nullable ProjectVo... projects) {
		if (Objects.isNull(projects) || (projects.length < 1))
			return null;

		return FilesBlo.createExcelExport(projects);
	}

	/**
	 * <p>
	 * This method grabs the given file and file data from the file system and database. This method searches for the
	 * file using the file id, if set in the given {@link FileVo}, or the file name, if the ID isn't set but the name
	 * is.
	 * </p>
	 *
	 * @param file
	 *            A {@link FileVo} with either the file id or file name set
	 * @return A filled in {@link FileVo}
	 * @throws ApplicationException
	 * @throws IllegalParameterException
	 *             If the file name is invalid
	 */
	public static @Nullable FileVo getFile(@NotNull final FileVo file) throws ApplicationException {
		if (Objects.isNull(file))
			throw new IllegalParameterException("The FileVo is null");

		// If we have the file id, use that, otherwise use the file name; merge the VO when done to "save time"
		return FileVo.mergeFileVos(file, ValidationUtils.isId(file.getId()) ? FilesBloGateway.getFile(file.getId())
				: FilesBloGateway.getFile(file.getFile()));
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
	 * @throws IllegalParameterException
	 *             If the ID is invalid
	 */
	public static @Nullable FileVo getFile(@NotNull final Long fileId) throws ApplicationException {
		if (ValidationUtils.not(ValidationUtils.isId(fileId)))
			throw new IllegalParameterException("The file ID is not valid: " + fileId);

		return FilesBlo.getFile(fileId);
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
	 * @throws ApplicationException
	 * @throws IllegalParameterException
	 *             If the file name is invalid
	 */
	public static @Nullable FileVo getFile(@NotNull final String fileName) throws ApplicationException {
		if (ValidationUtils.isEmpty(fileName))
			throw new IllegalParameterException("The filename is invalid: " + fileName);
		if (ValidationUtils.isId(fileName)) {
			FilesBloGateway.getFile(Long.valueOf(fileName));
		}

		return FilesBlo.getFile(fileName);
	}
}
