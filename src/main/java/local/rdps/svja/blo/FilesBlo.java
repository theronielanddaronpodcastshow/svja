package local.rdps.svja.blo;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.vo.FileVo;

/**
 * <p>
 * This class is the primer workhorse for any file request.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class FilesBlo {
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
