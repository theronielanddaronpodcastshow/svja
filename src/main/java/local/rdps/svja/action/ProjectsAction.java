package local.rdps.svja.action;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.blo.FilesBloGateway;
import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.DatabaseManager;
import local.rdps.svja.dao.PermissionsDaoGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.FileVo;
import local.rdps.svja.vo.PermissionsVo;
import local.rdps.svja.vo.ProjectVo;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This class represents what it means to be a project and an action... not much, it seems.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ProjectsAction extends RestAction {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	/**
	 * Whether or not we are to return an excel export
	 */
	private boolean excelExport;
	/**
	 * The export
	 */
	private FileVo export;
	/**
	 * Stores whether or not we have passed the basic conditions necessary for any request to this action, if set,
	 * otherwise indicates that the basic conditions have not been checked.
	 */
	private Optional<Boolean> passedBasicChecks = Optional.empty();
	/**
	 * The current user's permissions
	 */
	private PermissionsVo permissions;
	/**
	 * The project that we want to return to the user
	 */
	private ProjectVo project;
	/**
	 * The ID of the project that we are looking for or working with
	 */
	private Long projectId;
	/**
	 * The projects that we want to return to the user
	 */
	private Collection<ProjectVo> projects;

	/**
	 * <p>
	 * This method grabs the current user's permissions.
	 * </p>
	 *
	 * @return The user's permissions
	 */
	private PermissionsVo getPermissions() {
		if (Objects.nonNull(this.permissions))
			return this.permissions;

		final UserVo user = new UserVo();
		user.setId(DatabaseManager.getUid());
		try {
			this.permissions = PermissionsDaoGateway.getUserPermissions(user);
		} catch (final ApplicationException e) {
			this.permissions = new PermissionsVo();
		}
		return this.permissions;
	}

	@Override
	public String create() throws ApplicationException {
		final Optional<ProjectVo> newProject = CommonDaoGateway.upsertItem(this.project);
		if (newProject.isPresent()) {
			this.project = newProject.get();
			this.projectId = this.project.getId();
		}
		return ResultConstants.RESULT_SUCCESS;
	}

	/**
	 * <p>
	 * This method returns the user's export.
	 * </p>
	 *
	 * @return The requested data in an XLSX format
	 */
	@JsonProperty
	public FileVo getExport() {
		return this.export;
	}

	@JsonProperty
	public ProjectVo getProject() {
		if (Objects.nonNull(this.project) && ValidationUtils.not(ValidationUtils.isId(this.project.getId()))) {
			this.project.setId(this.projectId);
		}
		return this.project;
	}

	@JsonProperty
	public Long getProjectId() {
		if (ValidationUtils.not(ValidationUtils.isId(this.projectId))) {
			if (Objects.nonNull(this.project)) {
				this.projectId = this.project.getId();
			}
		}

		return this.projectId;
	}

	@JsonProperty
	public Collection<ProjectVo> getProjects() {
		return this.projects;
	}

	@Override
	public String index() throws ApplicationException {
		if (this.excelExport) {
			if (Objects.isNull(this.projects)) {
				this.projects = CommonDaoGateway.getItems(new ProjectVo());
			}
			this.export = FilesBloGateway.createExcelExport(this.projects.toArray(new ProjectVo[0]));
			this.projects = null;
		} else {
			this.projects = CommonDaoGateway.getItems(new ProjectVo());
		}

		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public boolean isIdSet() {
		return ValidationUtils.isId(this.projectId);
	}

	/**
	 * @return {@code true} iff {@link ProjectsAction#passesBasicConditions()} returns {@code true} and the user is
	 *         allowed to edit this item type
	 */
	@Override
	public boolean mayCreate() throws ApplicationException {
		// Perform the basic checks necessary for the request
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));
		if (!this.passedBasicChecks.filter(v -> !Objects.isNull(v)).orElse(Boolean.FALSE).booleanValue())
			return false;

		// Check to see if the user has the necessary permissions to perform the request
		return getPermissions().getMayWrite();
	}

	/**
	 * @return {@code true} iff {@link ProjectsAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayIndex() throws ApplicationException {
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));

		return this.passedBasicChecks.filter(v -> !Objects.isNull(v)).orElse(Boolean.FALSE).booleanValue();
	}

	/**
	 * @return {@code true} iff {@link ProjectsAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayShow() throws ApplicationException {
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));

		return this.passedBasicChecks.filter(v -> !Objects.isNull(v)).orElse(Boolean.FALSE).booleanValue();
	}

	/**
	 * @return {@code true} iff {@link ProjectsAction#passesBasicConditions()} returns {@code true} and the user is
	 *         allowed to edit this item type
	 */
	@Override
	public boolean mayUpdate() throws ApplicationException {
		// Perform the basic checks necessary for the request
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));
		if (!this.passedBasicChecks.filter(v -> !Objects.isNull(v)).orElse(Boolean.FALSE).booleanValue())
			return false;

		// Check to see if the user has the necessary permissions to perform the request
		return getPermissions().getMayWrite();
	}

	/**
	 * <p>
	 * This method sets whether or not the user is requesting us to return the data in an Excel export.
	 * </p>
	 *
	 * @param excelExport
	 *            Whether or not to export via an XLSX file
	 */
	@JsonProperty
	public void setExcelExport(final boolean excelExport) {
		this.excelExport = excelExport;
	}

	public void setProject(final ProjectVo project) {
		if (Objects.nonNull(project)) {
			this.project = project;
		}
	}

	public void setProjectId(final Long projectId) {
		if (ValidationUtils.isId(projectId)) {
			this.projectId = projectId;
		}
	}

	public void setProjects(final Collection<ProjectVo> projects) {
		this.projects = projects;
	}

	@Override
	public String show() throws ApplicationException {
		if (Objects.nonNull(this.projects)) {
			ProjectsAction.logger.error("Our Projects was of size {}", this.projects.size());
		}
		final ProjectVo proj = new ProjectVo(this.projectId);
		this.project = CommonDaoGateway.getItems(proj).stream().findFirst().orElse(null);
		if (this.excelExport) {
			this.export = FilesBloGateway.createExcelExport(this.project);
			this.project = null;
		}
		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public String update() throws ApplicationException {
		if (isIdSet() && Objects.nonNull(this.project)) {
			if (ValidationUtils.not(ValidationUtils.isId(this.project.getId()))) {
				this.project.setId(this.projectId);
			}
		}
		CommonDaoGateway.upsertItem(this.project);
		return ResultConstants.RESULT_SUCCESS;
	}
}
