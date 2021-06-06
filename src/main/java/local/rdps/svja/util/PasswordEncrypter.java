package local.rdps.svja.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.jetbrains.annotations.NotNull;

/**
 * This class provides methods to create a salted one-way hash or to verify a salted one-way hash.
 *
 * @author DaRon
 * @since 1.0
 */
public final class PasswordEncrypter {
	private static final int ITERATION_COUNT = 5;
	private static final SecureRandom prng = new SecureRandom();

	private static void strengthenSalt(final @NotNull byte[] salt, final @NotNull byte... fortifier) {
		int positionInSalt = 0;
		for (final int fortus : fortifier) {
			if (positionInSalt >= salt.length) {
				positionInSalt = 0;
			}
			salt[positionInSalt] = (byte) (fortus ^ salt[positionInSalt]);
			positionInSalt++;
		}
	}

	/**
	 * This method should be used to prepare a password to be stored in the database
	 *
	 * @param password
	 *            This should be a password that is in plaintext
	 * @param username
	 *            The user's name
	 * @return A hashed and base64ed password to be stored in the database
	 * @throws NoSuchAlgorithmException
	 */
	public static String encryptForDatabase(final @NotNull String password, final String username)
			throws NoSuchAlgorithmException {
		final MessageDigest digest;
		digest = MessageDigest.getInstance("SHA-512");

		digest.reset();
		byte[] btPass;
		btPass = digest.digest((username + password).getBytes(StandardCharsets.UTF_8));

		for (int i = 0; i < PasswordEncrypter.ITERATION_COUNT; i++) {
			digest.reset();
			btPass = digest.digest(EncodingUtils.base64EncodeUrlSafe(btPass));
		}

		return EncodingUtils.base64EncodeUrlSafeString(btPass);
	}

	/**
	 * This method returns a randomly generated long that can be used as a cryptographically secure salt.
	 *
	 * @return A random Long
	 */
	public static long getRandomNumber() {
		final byte[] randomNum = new byte[16];
		PasswordEncrypter.prng.nextBytes(randomNum);
		final byte[] mixerA = new byte[48];
		PasswordEncrypter.prng.nextBytes(mixerA);
		final byte[] mixerB = new byte[32];
		PasswordEncrypter.prng.nextBytes(mixerB);

		final byte[] fortifier = new byte[mixerA.length + mixerB.length];
		System.arraycopy(mixerA, 0, fortifier, 0, mixerA.length);
		System.arraycopy(mixerB, 0, fortifier, mixerA.length, mixerB.length);

		PasswordEncrypter.strengthenSalt(randomNum, fortifier);
		PasswordEncrypter.prng.setSeed(randomNum);
		PasswordEncrypter.prng.nextBytes(randomNum);

		return PasswordEncrypter.prng.nextLong();
	}

	/**
	 * This method should be used to get a basic SHA-512 hash.
	 *
	 * @param string
	 *            String to hash
	 * @return A hashed and base64ed string
	 * @throws NoSuchAlgorithmException
	 */
	public static String singleRoundHash(final @NotNull String string) throws NoSuchAlgorithmException {
		final MessageDigest digest = MessageDigest.getInstance("SHA-512");

		digest.reset();
		final byte[] hash;
		hash = digest.digest((string).getBytes(StandardCharsets.UTF_8));

		return EncodingUtils.base64EncodeString(hash);
	}

	private PasswordEncrypter() {
	}
}
