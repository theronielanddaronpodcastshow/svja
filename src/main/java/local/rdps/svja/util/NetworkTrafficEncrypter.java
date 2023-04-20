package local.rdps.svja.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import local.rdps.svja.constant.CommonConstants;

/**
 * <p>
 * This class provides methods to encrypt and decrypt network data.
 * </p>
 *
 * @author DaRon
 * @since 1.0.2
 */
public class NetworkTrafficEncrypter {
	/**
	 * Imported code
	 */
	private static final String HEX = "0123456789ABCDEF";
	/**
	 * Shh... I'm a secret
	 */
	private static final String STRING1 = "actof";
	/**
	 * Shh... I'm a secret
	 */
	private static final String STRING2 = "the";
	/**
	 * Shh... I'm a secret
	 */
	private static String STRING3 = "b3ingb@d";

	/**
	 * <p>
	 * This method turns the given hex string into a byte array.
	 * </p>
	 *
	 * @param hexString
	 *            The hex string to transform
	 * @return The corresponding byte array based on the given hex string
	 */
	private static byte[] hexToByte(final String hexString) {
		final int len = hexString.length() / 2;
		final byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
		}
		return result;
	}

	/**
	 * <p>
	 * This method turns the given byte array into a hex string.
	 * </p>
	 *
	 * @param ciphertext
	 *            The ciphertext to convert to a hex string
	 * @return A hex string representing the contents of the byte array
	 */
	private static String toHex(final byte[] ciphertext) {
		if (ciphertext == null)
			return CommonConstants.EMPTY_STRING;
		final StringBuffer result = new StringBuffer(ciphertext.length * 2);
		for (final byte b : ciphertext) {
			result.append(NetworkTrafficEncrypter.HEX.charAt((b >> 4) & 15))
					.append(NetworkTrafficEncrypter.HEX.charAt(b & 15));
		}
		return result.toString();
	}

	/**
	 * <p>
	 * This method decrypts the ciphertext represented by the given hex string, returning a plaintext string
	 * representation of the data.
	 * </p>
	 *
	 * @param hexString
	 *            A hex string containing the ciphertext to decrypt
	 * @return Plaintext corresponding to the decrypted ciphertext
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 */
	public static String decrypt(final String hexString) throws IllegalBlockSizeException, BadPaddingException,
			NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		final SecretKeySpec keySpec = new SecretKeySpec(NetworkTrafficEncrypter.getEncryptionKey(), "AES");
		final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		cipher.init(2, keySpec);
		final byte[] decrypted = cipher.doFinal(NetworkTrafficEncrypter.hexToByte(hexString));
		return new String(decrypted);
	}

	/**
	 * <p>
	 * This method encrypts the specified data, returning a hex string representing the ciphertext.
	 * </p>
	 *
	 * @param dataToEncrypt
	 *            The data to encrypt
	 * @return The ciphertext as a hex string
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static String encrypt(final byte[] dataToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		final SecretKeySpec keySpec = new SecretKeySpec(NetworkTrafficEncrypter.getEncryptionKey(), "AES");
		final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		cipher.init(1, keySpec);
		final byte[] encryptedBytes = cipher.doFinal(dataToEncrypt);
		return NetworkTrafficEncrypter.toHex(encryptedBytes);
	}

	/**
	 * <p>
	 * This method returns the encryption key used to encrypt and decrypt network traffic.
	 * </p>
	 *
	 * @return The encryption key
	 */
	public static byte[] getEncryptionKey() {
		return (NetworkTrafficEncrypter.STRING2 + NetworkTrafficEncrypter.STRING1 + NetworkTrafficEncrypter.STRING3)
				.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * <p>
	 * This method sets the encryption key used to encrypt and decrypt network traffic.
	 * </p>
	 *
	 * @param key
	 *            The final component of the network key
	 */
	public static void setEncryptionKey(final String key) {
		NetworkTrafficEncrypter.STRING3 = key;
	}
}
