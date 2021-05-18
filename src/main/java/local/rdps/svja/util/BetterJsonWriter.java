package local.rdps.svja.util;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * <p>
 * This class implements a more tested json serializer than the default struts provided serializer.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class BetterJsonWriter implements JSONWriter {
	private final ObjectMapper mapper = BetterJsonWriter.setupMapper();

	/**
	 * Setup the json mapper
	 * <p>
	 * Use @JsonProperty to annotate properties that need to be serialized
	 *
	 * @return
	 */
	private static ObjectMapper setupMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.setVisibility(mapper.getVisibilityChecker().withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
						.withFieldVisibility(JsonAutoDetect.Visibility.NONE)
						.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withSetterVisibility(JsonAutoDetect.Visibility.NONE));
		mapper.registerModule(new Jdk8Module());
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper;
	}

	@Override
	public void setCacheBeanInfo(final boolean b) {

	}

	@Override
	public void setDateFormatter(final String s) {

	}

	@Override
	public void setEnumAsBean(final boolean b) {

	}

	@Override
	public void setExcludeProxyProperties(final boolean b) {

	}

	@Override
	public void setIgnoreHierarchy(final boolean b) {

	}

	/**
	 * Serialize object
	 *
	 * @param object
	 *            Object to be serialized into JSON
	 * @return
	 * @throws JSONException
	 */
	@Override
	public String write(final Object object) throws JSONException {
		try {
			return this.mapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			throw new JSONException(e);
		}
	}

	/**
	 * Serialize object
	 *
	 * @param object
	 *            Object to be serialized into JSON
	 * @param excludeProperties
	 *            Patterns matching properties to ignore
	 * @param includeProperties
	 *            Patterns matching properties to include
	 * @param excludeNullProperties
	 *            enable/disable excluding of null properties
	 * @return JSON string for object
	 * @throws org.json.JSONException
	 *             in case of error during serialize
	 */
	@Override
	public String write(final Object object, final Collection<Pattern> excludeProperties,
			final Collection<Pattern> includeProperties, final boolean excludeNullProperties) throws JSONException {
		// we are choosing to ignore the struts provided properties here, but we can add them in in the future.
		try {
			return this.mapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			throw new JSONException(e);
		}
	}
}
