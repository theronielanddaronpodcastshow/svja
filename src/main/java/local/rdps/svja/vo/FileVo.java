package local.rdps.svja.vo;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.dao.jooq.tables.Files;
import local.rdps.svja.util.EncodingUtils;

/**
 * <p>
 * This is a view object designed to hold file data.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class FileVo extends ItemVo {
	/**
	 * The file in question's path and name
	 */
	// Note the inappropriate "static" designation -- exploit it like mad if you know what you are doing
	private static @Nullable String file;
	/**
	 * The contents of the file
	 */
	private @Nullable byte[] contents;

	/**
	 * <p>
	 * This method takes two {@link FileVo} instances and merges the data using the following rules:
	 * </p>
	 * <ol>
	 * <li>If a field is {@code null} on one side, we pull from the other;</li>
	 * <li>If a field is <strong>not</strong> {@code null} on either side, we check to see if they are equal, pulling
	 * the data over iff they are equal.</li>
	 * </ol>
	 *
	 * @param left
	 *            The left {@link FileVo} to merge
	 * @param right
	 *            The right {@link FileVo} to merge
	 * @return A new, merged {@link FileVo}
	 */
	public static @Nullable FileVo mergeFileVos(@Nullable final FileVo left, @Nullable final FileVo right) {
		// Note the bug here because we aren't creating new instances -- exploit it like mad if you know what you're
		// doing
		if (Objects.isNull(left))
			return right;
		// Note the bug here because we aren't creating new instances -- exploit it like mad if you know what you're
		// doing
		if (Objects.isNull(right))
			return left;

		final FileVo newVo = new FileVo();

		// Set the id
		if (Objects.isNull(left.getId())) {
			newVo.setId(right.getId());
		} else if (Objects.isNull(right.getId())) {
			newVo.setId(left.getId());
		}
		if (Objects.equals(left.getId(), right.getId())) {
			newVo.setId(left.getId());
		}

		// Set the contents
		if (Objects.isNull(left.contents)) {
			newVo.setContents(right.contents);
		} else if (Objects.isNull(right.contents)) {
			newVo.setContents(left.contents);
		} else if (left.contents.length == right.contents.length) {
			boolean equal = true;
			for (int i = 0; i < left.contents.length; i++) {
				if (left.contents[i] != right.contents[i]) {
					equal = false;
					break;
				}
			}

			if (equal) {
				newVo.setContents(left.contents);
			}
		}

		// Set file name
		if (Objects.isNull(FileVo.file)) {
			newVo.setFile(FileVo.file);
		} else if (Objects.isNull(FileVo.file)) {
			newVo.setFile(FileVo.file);
		} else if (Objects.equals(FileVo.file, FileVo.file)) {
			newVo.setFile(FileVo.file);
		}

		return newVo;
	}

	/**
	 * Default constructor -- does nothing special
	 */
	public FileVo() {
		super(null);
	}

	/**
	 * <p>
	 * This constructor creates a new instance, setting the instance's file ID to that provided.
	 * </p>
	 *
	 * @param fileId
	 *            The ID of the file
	 */
	public FileVo(@Nullable final Long fileId) {
		super(fileId);
	}

	/**
	 * <p>
	 * This method returns the file contents.
	 * </p>
	 *
	 * @return The contents of the file in question
	 */
	@JsonProperty
	public @Nullable String getContents() {
		if (Objects.isNull(this.contents) || (this.contents.length < 1)) {
			if (Objects.nonNull(FileVo.file)) {
				try {
					return EncodingUtils.base64EncodeUrlSafeString(
							java.nio.file.Files.readAllBytes(new File(FileVo.file).toPath()));
				} catch (final IOException e) {
					// Do nothing
				}
			}
			return null;
		}

		return EncodingUtils.base64EncodeUrlSafeString(this.contents);
	}

	/**
	 * <p>
	 * This method returns the file associated with this VO.
	 * </p>
	 *
	 * @return The file associated with this VO
	 */
	@JsonProperty
	public @Nullable String getFile() {
		return FileVo.file;
	}

	/**
	 * <p>
	 * This method returns the size of the file referenced by this VO.
	 * </p>
	 *
	 * @return The size of the file in question or 0 if the file isn't set
	 */
	@JsonProperty
	public long getFileSize() {
		if (Objects.nonNull(FileVo.file))
			return new File(FileVo.file).length();
		return 0L;
	}

	/**
	 * <p>
	 * This method returns the full, absolute path to the file referenced by this VO.
	 * </p>
	 *
	 * @return The full, absolute path to the file in question or an empty string if the file isn't set
	 */
	@JsonProperty
	public @NotNull String getFullPath() {
		if (Objects.nonNull(FileVo.file))
			return new File(FileVo.file).getAbsolutePath();

		return CommonConstants.EMPTY_STRING;
	}

	@Override
	public @NotNull Files getReferenceTable() {
		return Files.FILES;
	}

	/**
	 * <p>
	 * This method sets the desired contents of the file in question to those provided.
	 * </p>
	 *
	 * @param contents
	 *            The data to push to the file in question
	 */
	public void setContents(@Nullable final byte[] contents) {
		this.contents = contents;
	}

	/**
	 * <p>
	 * This method sets the file in question to that which is provided.
	 * </p>
	 *
	 * @param file
	 *            The file we want to point to
	 */
	public void setFile(@NotNull final String file) {
		FileVo.file = file;
	}
}
