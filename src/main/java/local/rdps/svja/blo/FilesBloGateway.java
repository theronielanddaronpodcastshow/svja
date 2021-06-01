package local.rdps.svja.blo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.FileVo;

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
	 * This method grabs the given file and file data from the file system and database. This method searches for the
	 * file using the file ID.
	 * </p>
	 *
	 * @param fileId
	 *            The ID of the file to retrieve from the database
	 * @return A filled in {@link FileVo}
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
