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
	 * The contents of the file
	 */
	private @Nullable byte[] contents;
	/**
	 * The file in question's path and name
	 */
	private static @Nullable String file;

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
			if (Objects.nonNull(this.file)) {
				try {
					return EncodingUtils
							.base64EncodeUrlSafeString(java.nio.file.Files.readAllBytes(new File(this.file).toPath()));
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
		return this.file;
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
		if (Objects.nonNull(this.file))
			return new File(this.file).length();
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
		if (Objects.nonNull(this.file))
			return new File(this.file).getAbsolutePath();

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
		this.file = file;
	}
}
