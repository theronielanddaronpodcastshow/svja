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
import local.rdps.svja.vo.FileVo;
import local.rdps.svja.vo.PermissionsVo;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This class represents what it means to be a file and an action... not much, it seems.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class FilesAction extends RestAction {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	/**
	 * The file that we want to return to the user
	 */
	private FileVo file;
	/**
	 * The ID of the file that we are looking for or working with
	 */
	private Long fileId;
	/**
	 * The files that we want to return to the user
	 */
	private Collection<FileVo> files;
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
		// TODO Restore me
		// user.setId(DatabaseManager.getUid());
		user.setId(Long.valueOf(2L));
		try {
			this.permissions = PermissionsDaoGateway.getUserPermissions(user);
		} catch (final ApplicationException e) {
			this.permissions = new PermissionsVo();
		}
		return this.permissions;
	}

	@Override
	public String create() throws ApplicationException {
		// this.file.setDirtyFields(super.getRequestKeysForRootNode(FileVo.class.getSimpleName()));
		// final Optional<FileVo> newFileVo = CommonWriteDAOGateway.insertUpdateItem(this.file);
		// if (newFileVo.isPresent()) {
		// this.file = newFileVo.get();
		// this.fileId = this.file.getFileVoId();
		// }
		return ResultConstants.RESULT_SUCCESS;
	}

	@JsonProperty
	public FileVo getFile() {
		if (Objects.nonNull(this.file) && ValidationUtils.not(ValidationUtils.isId(this.file.getId()))) {
			this.file.setId(this.fileId);
		}
		return this.file;
	}

	@JsonProperty
	public Long getFileId() {
		if (ValidationUtils.not(ValidationUtils.isId(this.fileId))) {
			if (Objects.nonNull(this.file)) {
				this.fileId = this.file.getId();
			}
		}

		return this.fileId;
	}

	@JsonProperty
	public Collection<FileVo> getFiles() {
		return this.files;
	}

	@Override
	public String index() throws ApplicationException {
		this.files = CommonDaoGateway.getItems(new FileVo());
		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public boolean isIdSet() {
		return ValidationUtils.isId(this.fileId);
	}

	/**
	 * @return {@code true} iff {@link FileVosAction#passesBasicConditions()} returns {@code true} and the user is
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
	 * @return {@code true} iff {@link FileVosAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayIndex() throws ApplicationException {
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));

		return this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue();
	}

	/**
	 * @return {@code true} iff {@link FileVosAction#passesBasicConditions()} returns {@code true}
	 */
	@Override
	public boolean mayShow() throws ApplicationException {
		this.passedBasicChecks = Optional
				.ofNullable(this.passedBasicChecks.orElse(Boolean.valueOf(super.passesBasicConditions())));

		return this.passedBasicChecks.map(v -> Objects.isNull(v) ? Boolean.FALSE : v).orElse(Boolean.FALSE)
				.booleanValue();
	}

	/**
	 * @return {@code true} iff {@link FileVosAction#passesBasicConditions()} returns {@code true} and the user is
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

	public void setFile(final FileVo file) {
		if (Objects.nonNull(file)) {
			this.file = file;
		}
	}

	public void setFileId(final Long fileId) {
		if (ValidationUtils.isId(fileId)) {
			this.fileId = fileId;
		}
	}

	public void setFiles(final List<FileVo> files) {
		this.files = files;
	}

	@Override
	public String show() throws ApplicationException {
		if (Objects.nonNull(this.files)) {
			FilesAction.logger.error("Our FileVos was of size {}", this.files.size());
		}
		final FileVo file = new FileVo(this.fileId);
		FilesAction.logger.info("Our fileId is {} and our file ID is {}", this.fileId, file.getId());
		this.file = CommonDaoGateway.getItems(file).stream().findFirst().orElse(null);
		return ResultConstants.RESULT_SUCCESS;
	}

	@Override
	public String update() throws ApplicationException {
		// this.file.setDirtyFields(super.getRequestKeysForRootNode(FileVo.class.getSimpleName()));
		// if (isIdSet()) {
		// this.file.setFileVoId(this.fileId);
		// }
		// CommonWriteDAOGateway.insertUpdateItem(this.file);
		// if (!ValidationUtils.isEmpty(this.file.getReferences())) {
		// for (final FileVoReference reference : this.file.getReferences()) {
		// reference.setFileVoId(this.fileId);
		// CommonWriteDAOGateway.insertUpdateItem(reference);
		// }
		// }
		return ResultConstants.RESULT_SUCCESS;
	}
}
