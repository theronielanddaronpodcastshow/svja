package local.rdps.svja.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.serialiser.SerialisableDeterminant;

/**
 * <p>
 * This class provides a type-safe abstraction of the session map. It allows safely storing and retrieving data in a
 * memory efficient way.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class SessionUtils {
	private static final char COMPACT_BYTES_DELIMITER = 58;
	private static final char COMPACT_BYTES_EMPTY_CHAR = 59;
	private static final Logger logger = LogManager.getLogger();

	/**
	 * <p>
	 * Convert a compacted byte array into a list of long values. Each byte represents two digits.
	 * </p>
	 *
	 * @param bytes
	 *            The bytes to unpack
	 * @return A list of {@link Long} instances representing the formerly compacted numbers
	 */
	private static @NotNull List<Long> compactBytesToNumbers(final @Nullable byte[] bytes) {
		if (Objects.isNull(bytes))
			return new ArrayList<>();

		final StringBuilder numbersString = new StringBuilder();
		for (final byte aByte : bytes) {
			numbersString.append((char) (((aByte >> 4) & 0xF) + 48));
			final char b = (char) ((aByte & 0xF) + 48);
			if (b != SessionUtils.COMPACT_BYTES_EMPTY_CHAR) {
				numbersString.append(b);
			}
		}
		return Arrays.stream(numbersString.toString().split("[" + SessionUtils.COMPACT_BYTES_DELIMITER + "]"))
				.filter(ValidationUtils::isId).map(ConversionUtils::stringToLong).collect(Collectors.toList());
	}

	/**
	 * <p>
	 * Generalized function to retrieve a list of ids from the session.
	 * </p>
	 *
	 * @param key
	 *            The session key
	 * @return The list from the session, based on the session key
	 */
	private static @NotNull List<Long> getListOfIdsFromSession(final String key) {
		final Map<String, Object> session = CommonUtils.getSession();
		final byte[] ids = (byte[]) session.get(key);
		return Objects.isNull(ids) ? new ArrayList<>() : SessionUtils.compactBytesToNumbers(ids);
	}

	/**
	 * <p>
	 * Generalized function to save a list of ids into the session.
	 * </p>
	 *
	 * @param key
	 *            The session key
	 * @param ids
	 *            The list of ids
	 */
	private static void setListOfIdsIntoSession(final String key, final List<Long> ids) {
		final Map<String, Object> session = CommonUtils.getSession();
		if (!ValidationUtils.isEmpty(ids)) {
			session.put(key, SessionUtils.numbersToCompactBytes(ids));
		} else {
			session.remove(key);
		}
	}

	/**
	 * <p>
	 * Create a compressed base 64 string from a session map.
	 * </p>
	 *
	 * @param sessionMap
	 *            The session map to serialize
	 * @return A string containing the session data serialized
	 * @throws IOException
	 * @throws ApplicationException
	 */
	public static @Nullable String createBytesFromSessionMap(final @NotNull Map<String, Object> sessionMap)
			throws IOException, ApplicationException {
		if (ValidationUtils.isEmpty(sessionMap))
			return null;

		final Instant start = Instant.now();
		try (final ByteArrayOutputStream sessionMapWriterStream = new ByteArrayOutputStream();
				final SnappyOutputStream compressionStream = new SnappyOutputStream(sessionMapWriterStream);
				final ObjectOutput sessionMapOOS = new ObjectOutputStream(compressionStream)) {
			int sizeOfDataWritten = sessionMapWriterStream.size();
			// write out the map
			sessionMapOOS.writeInt(sessionMap.size());
			if (SessionUtils.logger.isDebugEnabled()) {
				sessionMapOOS.flush();
				SessionUtils.logger.debug("We wrote {} bytes stating that we would have {} entries in our session",
						Integer.valueOf(sessionMapWriterStream.size() - sizeOfDataWritten),
						Integer.valueOf(sessionMap.size()));
				sizeOfDataWritten = sessionMapWriterStream.size();
			}
			for (final Map.Entry<String, Object> sessionItem : sessionMap.entrySet()) {
				sessionMapOOS.writeObject(sessionItem.getKey());
				if (SessionUtils.logger.isDebugEnabled()) {
					sessionMapOOS.flush();
					SessionUtils.logger.debug(
							"We wrote {} bytes stating that we will be writing an {} item to the session",
							Integer.valueOf(sessionMapWriterStream.size() - sizeOfDataWritten), sessionItem.getKey());
					sizeOfDataWritten = sessionMapWriterStream.size();
				}
				sessionMapOOS.writeObject(new SerialisableDeterminant(sessionItem.getValue()));
				if (SessionUtils.logger.isDebugEnabled()) {
					sessionMapOOS.flush();
					SessionUtils.logger.debug("We wrote {} bytes of {} data to the session",
							Integer.valueOf(sessionMapWriterStream.size() - sizeOfDataWritten), sessionItem.getKey());
					sizeOfDataWritten = sessionMapWriterStream.size();
				}
			}

			sessionMapOOS.flush();
			final String sessionData = EncodingUtils.base64EncodeString(sessionMapWriterStream.toByteArray());
			if (SessionUtils.logger.isDebugEnabled()) {
				SessionUtils.logger.debug(
						"We wrote a total of {} bytes serializing {} items of the session and took {}",
						Integer.valueOf(sessionMapWriterStream.size()), Integer.valueOf(sessionMap.size()),
						Duration.between(start, Instant.now()));
				SessionUtils.logger.debug("The session map has been turned into {} and contains:{}{}", sessionData,
						System.lineSeparator(),
						sessionMap.entrySet().stream().map(entry -> entry.getKey() + ':' + entry.getValue())
								.collect(Collectors.joining(System.lineSeparator())));
			}
			return sessionData;
		}
	}

	/**
	 * <p>
	 * Create a session map from a compressed base64 string.
	 * </p>
	 *
	 * @param serializedSession
	 *            The serialized session
	 * @return A session map
	 */
	public static @NotNull Map<String, Object> createSessionMapFromBytes(final @NotNull String serializedSession) {
		if (ValidationUtils.isEmpty(serializedSession))
			return new HashMap<>(0);

		final Instant start = Instant.now();
		try (final ByteArrayInputStream sessionMapReaderStream = new ByteArrayInputStream(
				EncodingUtils.base64Decode(serializedSession));
				final SnappyInputStream decompressionStream = new SnappyInputStream(sessionMapReaderStream);
				final ObjectInput sessionMapOOS = new ObjectInputStream(decompressionStream)) {
			// read in the map
			int bytesStillToBeRead = sessionMapReaderStream.available();
			if (SessionUtils.logger.isDebugEnabled()) {
				SessionUtils.logger.debug("We have {} bytes of data to read from our serialized session",
						Integer.valueOf(bytesStillToBeRead));
				bytesStillToBeRead = sessionMapReaderStream.available();
			}
			final int sizeOfMap = sessionMapOOS.readInt();
			if (SessionUtils.logger.isDebugEnabled()) {
				SessionUtils.logger.debug(
						"We read {} bytes from our serialized session to find out that there are {} items in the stream",
						Integer.valueOf(bytesStillToBeRead - sessionMapReaderStream.available()),
						Integer.valueOf(sizeOfMap));
				bytesStillToBeRead = sessionMapReaderStream.available();
			}
			final Map<String, Object> sessionFromDynamo = new HashMap<>(sizeOfMap + 4);
			for (int i = 0; i < sizeOfMap; i++) {
				final String key = ConversionUtils.as(String.class, sessionMapOOS.readObject());
				if (SessionUtils.logger.isDebugEnabled()) {
					SessionUtils.logger.debug(
							"We read {} bytes from our serialized session to find out that the next item is a {} item",
							Integer.valueOf(bytesStillToBeRead - sessionMapReaderStream.available()), key);
					bytesStillToBeRead = sessionMapReaderStream.available();
				}
				final SerialisableDeterminant value = ConversionUtils.as(SerialisableDeterminant.class,
						sessionMapOOS.readObject());
				if (SessionUtils.logger.isDebugEnabled()) {
					SessionUtils.logger.debug("We read {} bytes from our serialized session extracting the {} data",
							Integer.valueOf(bytesStillToBeRead - sessionMapReaderStream.available()), key);
					bytesStillToBeRead = sessionMapReaderStream.available();
				}
				if (!ValidationUtils.isEmpty(key)) {
					if (ValidationUtils.isEmpty(value)) {
						SessionUtils.logger.error(
								"Something in the session that wasn't null, wasn't a map, wasn't a collection, but didn't flow through SerialisablePrimitive... that shouldn't happen");
						sessionFromDynamo.put(key, value);
					} else {
						sessionFromDynamo.put(key, value.getObject());
					}
				}
			}

			if (SessionUtils.logger.isDebugEnabled()) {
				SessionUtils.logger.debug(
						"We read a total of {} characters deserializing {} items of the session and took {}",
						Integer.valueOf(serializedSession.length()), Integer.valueOf(sessionFromDynamo.size()),
						Duration.between(start, Instant.now()));
				SessionUtils.logger.debug("The session map contains:{}{}", System.lineSeparator(),
						sessionFromDynamo.entrySet().stream().map(entry -> entry.getKey() + ':' + entry.getValue())
								.collect(Collectors.joining(System.lineSeparator())));
			}
			return sessionFromDynamo;
		} catch (final @NotNull IOException | ClassNotFoundException e) {
			SessionUtils.logger.error(e.getMessage(), e);
		}

		return new HashMap<>(0);
	}

	/**
	 * <p>
	 * Place provided long values into a compacted string with each digit being represented by 4 bits (2 digits per
	 * byte).
	 * </p>
	 *
	 * @param numbers
	 *            The numbers to be compacted
	 * @return A byte array containing the compacted numbers
	 */
	public static @Nullable byte[] numbersToCompactBytes(final @Nullable List<Long> numbers) {
		if (ValidationUtils.isEmpty(numbers))
			return null;

		final String numbersString = numbers.stream().map(Object::toString)
				.collect(Collectors.joining("" + SessionUtils.COMPACT_BYTES_DELIMITER));
		final byte[] bytes = new byte[(int) Math.ceil(numbersString.length() / 2.0)];
		for (int i = 0; i < bytes.length; i++) {
			final char a = (char) (numbersString.charAt(i * 2) - 48);
			final char b = numbersString.length() > ((i * 2) + 1) ? (char) (numbersString.charAt((i * 2) + 1) - 48)
					: SessionUtils.COMPACT_BYTES_EMPTY_CHAR;
			bytes[i] = (byte) (((a & 0xF) << 4) | (b & 0xF));
		}
		return bytes;
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private SessionUtils() {
		// Do nothing
	}
}
