package local.rdps.svja.construct;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.dao.CommonDaoGateway;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.vo.UserVo;

/**
 * <p>
 * This abstract class defines what it means to be an item view object for any item in the database.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public abstract class ItemVo {
	/**
	 * Our item's ID
	 */
	private @Nullable Long id;
	/**
	 * Who last modified the item
	 */
	private @Nullable Long modifiedBy;
	/**
	 * The user who last made the modification
	 */
	private @Nullable UserVo modifiedByUser;
	/**
	 * When the item was last modified
	 */
	private @Nullable Instant modifiedDate;

	/**
	 * <p>
	 * This method returns the item's ID for this specific instance.
	 * </p>
	 *
	 * @return The current ID for the item
	 */
	@JsonProperty
	public final @Nullable Long getId() {
		return this.id;
	}

	/**
	 * <p>
	 * This method returns the user ID of the user who last modified this item.
	 * </p>
	 *
	 * @return The ID of the last user to modify the item
	 */
	@JsonProperty
	public final @Nullable Long getModifiedBy() {
		if (Objects.isNull(this.modifiedBy)) {
			if (Objects.nonNull(this.modifiedByUser)) {
				this.modifiedBy = this.modifiedByUser.getId();
			}
		}
		return this.modifiedBy;
	}

	/**
	 * <p>
	 * This method returns the user ID of the user who last modified this item.
	 * </p>
	 *
	 * @return The ID of the last user to modify the item
	 */
	@JsonProperty
	public final @Nullable UserVo getModifiedByUser() throws ApplicationException {
		if (Objects.isNull(this.modifiedByUser)) {
			if (Objects.nonNull(this.modifiedBy)) {
				final UserVo user = new UserVo();
				user.setId(this.modifiedBy);
				this.modifiedByUser = CommonDaoGateway.getItems(user).stream().findFirst().orElse(null);
			}
		}
		return this.modifiedByUser;
	}

	/**
	 * <p>
	 * This method returns the date-time that the item was last modified.
	 * </p>
	 *
	 * @return When the item was last modified
	 */
	@JsonProperty
	public final @Nullable Instant getModifiedDate() {
		return this.modifiedDate;
	}

	/**
	 * <p>
	 * This method gets the reference jooq table that this item VO uses.
	 * </p>
	 *
	 * @param <R>
	 *            The jooq-generated {@link Record} specific to this item VO
	 * @param <T>
	 *            A jooq-generated {@link Table} specific to this item VO
	 * @return The jooq-generated {@link Table} that holds the data that this item VO represents
	 */
	public abstract <R extends Record, T extends Table<R>> @NotNull T getReferenceTable();

	/**
	 * <p>
	 * This method sets the ID of this item to that which is provided. Please note that setting the ID to an invalid ID
	 * will likely result in exceptions down the stack.
	 * </p>
	 *
	 * @param id
	 *            The new ID for the item
	 */
	public final void setId(final @Nullable Long id) {
		this.id = id;
	}

	/**
	 * <p>
	 * This method sets the user ID of the user who last modified this item. Please note that setting the user ID to an
	 * invalid user ID will likely result in an exception down the stack.
	 * </p>
	 *
	 * @param modifiedBy
	 *            The ID of the last user to modify the item
	 */
	public final void setModifiedBy(final @Nullable Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * <p>
	 * This method sets the date-time that the item was last modified.
	 * </p>
	 *
	 * @param modifiedDate
	 *            When the item was last modified
	 */
	public final void setModifiedDate(final @Nullable Instant modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
