package local.rdps.svja.vo;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.dao.jooq.tables.Users;

/**
 * <p>
 * This is a view object designed to hold user data.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class UserVo extends ItemVo {
	/**
	 * When the last date-time was that the user logged in
	 */
	private @Nullable Instant lastLoginDate;
	/**
	 * How many times the user has logged in
	 */
	private @Nullable Long loginCount;
	/**
	 * The user's password
	 */
	private @Nullable String password;
	/**
	 * The user's username
	 */
	private @Nullable String username;

	/**
	 * Default constructor -- does nothing special
	 */
	public UserVo() {
		super(null);
	}

	/**
	 * <p>
	 * This constructor creates a new instance, setting the instance's user ID to that provided.
	 * </p>
	 *
	 * @param userId
	 *            The ID of the user
	 */
	public UserVo(@Nullable final Long userId) {
		super(userId);
	}

	/**
	 * <p>
	 * This method returns the user's last login date-time.
	 * </p>
	 *
	 * @return The last login date-time of the user
	 */
	@JsonProperty
	public @Nullable Instant getLastLoginDate() {
		return this.lastLoginDate;
	}

	/**
	 * <p>
	 * This method returns the user's login count.
	 * </p>
	 *
	 * @return The login count of the user
	 */
	@JsonProperty
	public @Nullable Long getLoginCount() {
		return this.loginCount;
	}

	/**
	 * <p>
	 * This method returns the user's password.
	 * </p>
	 *
	 * @return The password of the user
	 */
	public @Nullable String getPassword() {
		return this.password;
	}

	@Override
	public @NotNull Users getReferenceTable() {
		return Users.USERS;
	}

	/**
	 * <p>
	 * This method returns the user's username.
	 * </p>
	 *
	 * @return The username of the user
	 */
	@JsonProperty
	public @Nullable String getUsername() {
		return this.username;
	}

	/**
	 * <p>
	 * This method sets the user's last login date-time.
	 * </p>
	 *
	 * @param lastLoginDate
	 *            The last login date-time of the user
	 */
	public void setLastLoginDate(@Nullable final Instant lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 * <p>
	 * This method sets the user's login count.
	 * </p>
	 *
	 * @param loginCount
	 *            The login count of the user
	 */
	public void setLoginCount(@Nullable final Long loginCount) {
		this.loginCount = loginCount;
	}

	/**
	 * <p>
	 * This method sets the user's password.
	 * </p>
	 *
	 * @param password
	 *            The password of the user
	 */
	public void setPassword(@Nullable final String password) {
		this.password = password;
	}

	/**
	 * <p>
	 * This method sets the user's username.
	 * </p>
	 *
	 * @param username
	 *            The username of the user
	 */
	public void setUsername(@Nullable final String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "username: " + this.username;
	}
}
