package local.rdps.svja.vo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.dao.jooq.tables.Projects;

/**
 * <p>
 * This is a view object designed to hold project data.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class Project extends ItemVo {
	/**
	 * The project's description
	 */
	private @Nullable String description;
	/**
	 * The file in question's path and name
	 */
	private @Nullable FileVo file;
	/**
	 * The project's title
	 */
	private @Nullable String title;

	/**
	 * <p>
	 * This method returns the project's description.
	 * </p>
	 *
	 * @return The description of the project
	 */
	@JsonProperty
	public @Nullable String getDescription() {
		return this.description;
	}

	/**
	 * <p>
	 * This method returns the file associated with this VO.
	 * </p>
	 *
	 * @return The file associated with this VO
	 */
	@JsonProperty
	public @Nullable FileVo getFile() {
		return this.file;
	}

	@Override
	public @NotNull Projects getReferenceTable() {
		return Projects.PROJECTS;
	}

	/**
	 * <p>
	 * This method returns the project's title.
	 * </p>
	 *
	 * @return The title of the project
	 */
	@JsonProperty
	public @Nullable String getTitle() {
		return this.title;
	}

	/**
	 * <p>
	 * This method sets the project's description.
	 * </p>
	 *
	 * @param description
	 *            The new description of the project
	 */
	public void setDescription(final @Nullable String description) {
		this.description = description;
	}

	/**
	 * <p>
	 * This method sets the file in question to that which is provided.
	 * </p>
	 *
	 * @param file
	 *            The file we want to point to
	 */
	public void setFile(final @NotNull FileVo file) {
		this.file = file;
	}

	/**
	 * <p>
	 * This method sets the project's title.
	 * </p>
	 *
	 * @param title
	 *            The new title of the project
	 */
	public void setTitle(final @Nullable String title) {
		this.title = title;
	}
}
