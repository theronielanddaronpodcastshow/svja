package local.rdps.svja.action;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.constant.ResultConstants;
import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.dao.PermissionsDaoGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;
import local.rdps.svja.vo.Permissions;
import local.rdps.svja.vo.Project;
import local.rdps.svja.vo.User;

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
	 * Stores whether or not we have passed the basic conditions necessary for any request to this action, if set,
	 * otherwise indicates that the basic conditions have not been checked.
	 */
	private Optional<Boolean> passedBasicChecks = Optional.empty();
	/**
	 * The current user's permissions
	 */
	private Permissions permissions;
	/**
	 * The project that we want to return to the user
	 */
	private Project project;
	/**
	 * The ID of the project that we are looking for or working with
	 */
	private Long projectId;
	/**
	 * The projects that we want to return to the user
	 */
	private Collection<Project> projects;

	/**
	 * <p>
	 * This method grabs the current user's permissions.
	 * </p>
	 *
	 * @return The user's permissions
	 */
	private Permissions getPermissions() {
		if (Objects.nonNull(this.permissions))
			return this.permissions;

		final User user = new User();
		// TODO Restore me
		// user.setId(DatabaseManager.getUid());
		user.setId(Long.valueOf(2L));
		try {
			this.permissions = PermissionsDaoGateway.getUserPermissions(user);
		} catch (final ApplicationException e) {
			this.permissions = new Permissions();
		}
		return this.permissions;
	}

	@Override
	public String create() throws ApplicationException {
		// this.project.setDirtyFields(super.getRequestKeysForRootNode(Project.class.getSimpleName()));
		// final Optional<Project> newProject = CommonWriteDAOGateway.insertUpdateItem(this.project);
		// if (newProject.isPresent()) {
		// this.project = newProject.get();
		// this.projectId = this.project.getProjectId();
		// }
		return ResultConstants.RESULT_SUCCESS;
	}

	@JsonProperty
	public Project getProject() {
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
	public Collection<Project> getProjects() {
		return this.projects;
	}

	@Override
	public String index() throws ApplicationException {
		this.projects = CommonDaoGateway.getItems(new Project());
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
		if (!this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue())
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

		return this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue();
	}

	/**
	 * @return {@code true} iff {@link ProjectsAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayShow() throws ApplicationException {
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));

		return this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue();
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
		if (!this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue())
			return false;

		// Check to see if the user has the necessary permissions to perform the request
		return getPermissions().getMayWrite();
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	public void setProjectId(final Long projectId) {
		this.projectId = projectId;
	}

	public void setProjects(final List<Project> projects) {
		this.projects = projects;
	}

	@Override
	public String show() throws ApplicationException {
		final Project proj = new Project();
		proj.setId(this.projectId);
		ProjectsAction.logger.info("Our projectId is {} and our project ID is {}", this.projectId, proj.getId());
		this.project = CommonDaoGateway.getItems(proj).stream().findFirst().orElse(null);
		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public String update() throws ApplicationException {
		// this.project.setDirtyFields(super.getRequestKeysForRootNode(Project.class.getSimpleName()));
		// if (isIdSet()) {
		// this.project.setProjectId(this.projectId);
		// }
		// CommonWriteDAOGateway.insertUpdateItem(this.project);
		// if (!ValidationUtils.isEmpty(this.project.getReferences())) {
		// for (final ProjectReference reference : this.project.getReferences()) {
		// reference.setProjectId(this.projectId);
		// CommonWriteDAOGateway.insertUpdateItem(reference);
		// }
		// }
		return ResultConstants.RESULT_SUCCESS;
	}
}
