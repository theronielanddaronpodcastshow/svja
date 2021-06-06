package local.rdps.svja.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * This class is used to generate or compare in a consistent manner a 'nonce' or random piece of information designed to
 * help hamper various attacks against a user's session.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class Nonce implements Externalizable {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	private @Nullable String nonce;
	private long time;
	private @Nullable String username;

	/**
	 * <p>
	 * This is so that we can externalize the class
	 */
	public Nonce() {
		// This is so that we can externalize the class
		super();
	}

	/**
	 * <p>
	 * Create a new Nonce using the provided information. This constructor uses the current time as part of the mixing,
	 * so getTime() will return the time in which this constructor was called.
	 * </p>
	 *
	 * @param request
	 *            The ServletRequest to pull data from to make the nonce
	 * @param username
	 *            If the user is logged in, the username should be passed, otherwise a random number should be used.
	 * @throws IllegalArgumentException
	 *             If nulls are passed -or- we fail to generate the nonce
	 */
	public Nonce(final @NotNull HttpServletRequest request, final String username) throws IllegalArgumentException {
		this(new Date().getTime(), request, username);
	}

	/**
	 * <p>
	 * Create a new Nonce using the provided information.
	 * </p>
	 *
	 * @param time
	 *            The time when the original nonce was created or thought to have been created
	 * @param request
	 *            The ServletRequest to pull data from to make the nonce
	 * @param username
	 *            If the user is logged in, the username should be passed, otherwise a random number should be used.
	 * @throws IllegalArgumentException
	 *             If nulls are passed -or- we fail to generate the nonce
	 */
	public Nonce(final long time, final @NotNull HttpServletRequest request, final @NotNull String username)
			throws IllegalArgumentException {
		super();
		if (ValidationUtils.isEmpty(request))
			throw new IllegalArgumentException("The servlet request is null or empty");
		if (ValidationUtils.isEmpty(username))
			throw new IllegalArgumentException("The username is null or empty");
		this.time = time;
		this.username = username;

		try {
			this.nonce = PasswordEncrypter.encryptForDatabase(new ClientInfo(request).toString() + time, username);
		} catch (final NoSuchAlgorithmException e) {
			Nonce.logger.error("There was an issue with the encryption algorithm.", e);
			this.nonce = null;
		}
	}

	/**
	 * <p>
	 * This constructor creates a new Nonce using the given nonce string.
	 * </p>
	 *
	 * @param nonce
	 *            The precomputed nonce
	 */
	public Nonce(final String nonce) {
		this.nonce = nonce;
	}

	/**
	 * <p>
	 * This method examines the given Nonce against this instance for equality, returning {@code true} if they are
	 * equal.
	 * </p>
	 *
	 * @param otherNonce
	 *            The nonce whose equality is being compared against this instance
	 * @return {@code true} iff the contents of the two nonces are equal
	 */
	public boolean equals(final Nonce otherNonce) {
		return Objects.nonNull(otherNonce) && (this.time == otherNonce.time)
				&& Objects.equals(this.nonce, otherNonce.nonce);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object anonce) {
		return equals(ConversionUtils.as(Nonce.class, anonce));
	}

	/**
	 * <p>
	 * Returns the nonce that can be displayed or compared.
	 * </p>
	 *
	 * @return A random string based on unique identifiers, the date, and other key pieces
	 */
	public @Nullable String getNonce() {
		return this.nonce;
	}

	/**
	 * <p>
	 * The time the nonce was created -- note that this is important because it helps play a part in the value of the
	 * nonce.
	 * </p>
	 *
	 * @return The nonce creation time
	 */
	public long getTime() {
		return this.time;
	}

	/**
	 * <p>
	 * The username under which the nonce was created -- note that this is important because it helps play a part in the
	 * value of the nonce.
	 * </p>
	 *
	 * @return The username under which the none was created
	 */
	public @Nullable String getUsername() {
		return this.username;
	}

	/**
	 * <p>
	 * Simply return the value of the nonce hashed (or -1 if it's null).
	 * </p>
	 */
	@Override
	public int hashCode() {
		return (Objects.nonNull(this.nonce)) ? this.nonce.hashCode() : -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readExternal(final @NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		Nonce.logger.debug("Constructing object...");

		this.nonce = ConversionUtils.as(String.class, in.readObject());
		this.time = in.readLong();
		this.username = ConversionUtils.as(String.class, in.readObject());
	}

	/**
	 * <p>
	 * This method returns the nonce as a unique string, tied to the user's session, a random number, and other key
	 * data.
	 * </p>
	 *
	 * @return The unique nonce string
	 */
	@Override
	public @Nullable String toString() {
		return this.nonce;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(final @NotNull ObjectOutput out) throws IOException {
		Nonce.logger.debug("Externalizing object...");

		out.writeObject(this.nonce);
		out.writeLong(this.time);
		out.writeObject(this.username);
	}
}
