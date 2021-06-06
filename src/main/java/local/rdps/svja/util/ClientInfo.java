package local.rdps.svja.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * This class serves to extract and hold client data from a request that can be used to consistently identify a client.
 * This method plays a major role in detecting session hijacking, particularly in a consistent manner.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ClientInfo implements Externalizable {
	private static final String COLON = ": ";
	private static final Pattern DEFLATE = Pattern.compile("deflate");
	/**
	 * These are header keys that we are interested in dumping.
	 */
	private static final String[] FINGERPRINT_KEYS = {"User-Agent", "Accept-Encoding", "Accept-Language", "Host", "DNT",
			"Via", "X-Forwarded-For", "X-Forwarded-Proto", "Referer", "Cookie", "Accept"};
	private static final Pattern GZIP = Pattern.compile("gzip");
	// TODO replace this with a lockable, unmodifiable iterable
	private static final String[] KEYS = {"User-Agent", "Accept-Language", "Host"/* , "DNT" */, "Via",
			"X-Forwarded-For", "X-Forwarded-Proto"};
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1000000L;
	/**
	 * These are the codes that are allowed to switch between the shorthand and the long-hand with extensions. Clients
	 * really shouldn't switch, but some do and we have to account for them. AA, AB, AE, AF, AK, AN, AS, AST, AV, AY,
	 * AZ, BA, BE, BG, BH, BI, BM, BN, BO, BR, BS, CA, CH, CO, CR, CS, CSB, CU, CV, CY, CZ, DA, DSB, DV, DZ, EE, EL, EO,
	 * EN, ET, FA, FF, FI, FJ, FO, FR, FUR, FY, GA, GD, GL, GN, GU, KA, KL, KW, HA, HAW, HE, HI, HIL, HO, HR, HSB, HT,
	 * HU, HY, HZ, IA, ID, IE, IG, IK, IO, IS, IU, JA, JV, KA, KG, KI, KK, KL, KM, KN, KOK, KR, KS, KU, KV, KW, KY, LA,
	 * LB, LG, LI, LN, LO, LT, LU, LV, MG, MH, MI, MK, ML, MN, MR, MS, MT, MY, NA, NB, ND, NE, NG, NN, NO, NR, NSO, NV,
	 * OC, OJ, OM, OR, OS, PI, PL, PS, QU, RM, RN, RW, SA, SC, SD, SG, SI, SK, SL, SO, SQ, SR, ST, SU, SV, SW, TA, TE,
	 * TG, TH, TI, TK, TL, TN, TO, TR, TS, TT, TW, TY, UG, UK, UR, UZ, VE, VI, VO, WA, WO, XH, YI, YO, ZA, ZU. Please
	 * note that we are not including Chinese even though some browsers will sometimes oscillate between long-hand and
	 * short-hand if using zh-cn.
	 */
	private static final Pattern SHORTHANDABLE_ACCEPT_LANGUAGES = Pattern.compile(
			"(\\s*[,]?\\s*(a[abefknsvyz]|b[aeghimnors]|c[ahorsuvyz]|d[avz]|e[elont]|f[afijory]|g[adlnu]|h[aeiortuyz]|i[adegkosu]|j[av]|k[agiklmnrsuvwy]|l[abginotuv]|m[ghiklnrsty]|n[abdegnorv]|o[cjmrs]|p[ilsu]|r[mnw]|s[acdgikloqrstuvw]|t[aeghiklnorstwy]|u[gkrz]|v[eio]|w[ao]|xh|y[io]|z[au]|ast|csb|dsb|fur|haw|hil|hsb|kok|nso))\\-[^,]+");
	private static final char TAB = '\t';
	private @Nullable String info;

	/**
	 * <p>
	 * Builds a "fingerprint" of client data from a request. Useful for logging additional information.
	 * </p>
	 *
	 * @param request
	 *            The request from which the fingerprint shall be built
	 * @param userId
	 *            The user's ID in the underlying application
	 * @return The client's fingerprint, as a string
	 */
	public static String getClientFingerprintFromRequest(final @NotNull HttpServletRequest request,
			final @Nullable Long userId) {
		final StringBuilder data = new StringBuilder(512);

		if (!ValidationUtils.isEmpty(userId)) {
			data.append(System.lineSeparator()).append("User Id: ").append(userId).append(System.lineSeparator());
		}
		data.append("Remote Address: ").append(request.getRemoteAddr()).append(System.lineSeparator());
		data.append("Client Info: ").append(new ClientInfo(request)).append(System.lineSeparator());
		data.append("Context Path: ").append(request.getContextPath()).append(System.lineSeparator());
		data.append("Protocol and Query String: ").append(request.getProtocol()).append(' ')
				.append(request.getQueryString()).append(System.lineSeparator());
		data.append("Query URL: ").append(request.getMethod()).append(' ').append(request.getRequestURL())
				.append(System.lineSeparator());
		data.append("Content Length: ").append(request.getContentLength()).append(System.lineSeparator());
		data.append("Content Type: ").append(request.getContentType()).append(System.lineSeparator());

		data.append("Attributes:").append(System.lineSeparator());
		for (final String key : IterableEnumeration.make(request.getAttributeNames())) {
			data.append('\t').append(key).append(ClientInfo.COLON).append(request.getAttribute(key))
					.append(System.lineSeparator());
		}

		data.append("Header Data:").append(System.lineSeparator());
		// Grab header data
		data.append(Arrays.stream(ClientInfo.FINGERPRINT_KEYS)
				.map(key -> ClientInfo.TAB + key + ClientInfo.COLON + request.getHeader(key))
				.collect(Collectors.joining(System.lineSeparator())));

		if ("POST".equalsIgnoreCase(request.getMethod())) {
			data.append(System.lineSeparator()).append("Parameter Map:").append(System.lineSeparator());
			data.append(request.getParameterMap().entrySet().stream()
					.map(entry -> '\t' + entry.getKey() + ": " + Arrays.toString(entry.getValue()))
					.collect(Collectors.joining(System.lineSeparator())));
		}

		return data.toString();
	}

	/**
	 * <p>
	 * This is so that we can externalize the class
	 * </p>
	 */
	public ClientInfo() {
		// This is so that we can externalize the class

		super();
	}

	/**
	 * <p>
	 * This constructor generates a new ClientInfo object, pulling specific data from the provided request. This data is
	 * considered to be unique enough to detect almost any remote session hijacking attacks (excluding CSRFs).
	 * </p>
	 *
	 * @param request
	 *            The HTTP request
	 */
	public ClientInfo(final @NotNull HttpServletRequest request) {
		super();

		// Bring it all together
		final StringBuilder distinguishingMarks = new StringBuilder(256);

		for (final String key : ClientInfo.KEYS) {
			String temp = request.getHeader(key);
			if (Objects.nonNull(temp)) {
				// This is because some browsers (IE?) kept changing the case
				temp = temp.toLowerCase();
			}

			// We have to do this because the latest versions of Chrome and Safari have the Accept-Encoding change,
			// sometimes *adding* sdch, so we wanted to "future-proof" this section
			// We are ignoring: compress, exi, identity, pack200-gzip, bzip2, lzma, peerdist, sdch, and xz
			// Our rationale for ignoring the above is 1) our server only really supports gzip; 2) those are either
			// defunct or rare; 3) some of those, because of poor client programming, are only sent sometimes
			if (Objects.equals("Accept-Encoding", key)) {
				if (Objects.nonNull(temp)) {
					if (ClientInfo.GZIP.matcher(temp).find()) {
						distinguishingMarks.append('1');
					} else {
						distinguishingMarks.append('0');
					}
					if (ClientInfo.DEFLATE.matcher(temp).find()) {
						distinguishingMarks.append('1');
					} else {
						distinguishingMarks.append('0');
					}
				} else {
					distinguishingMarks.append("00");
				}
			}
			// We have to do this because some browsers for some languages like to alternate between the shorthand and
			// non-shorthand versions of the Accept-Language. They most often do this by moving from specific (e.g.
			// jp-ja) to less specific (e.g. jp). This is really only supposed to be possible with certain languages,
			// like jp, where there are no alternatives (unlike en where there are a ton -- en-us, en-uk, en-au, and
			// so-on). This gelds our Accept-Language portion a little, but not by so much that we should just get rid
			// of this portion.
			if (Objects.nonNull(temp)) {
				if (Objects.equals("Accept-Language", key)) {
					final Matcher match = ClientInfo.SHORTHANDABLE_ACCEPT_LANGUAGES.matcher(temp);
					distinguishingMarks.append(match.replaceAll("$1"));
				} else {
					distinguishingMarks.append(temp);
				}
			}
			// We aren't "Accept-Encoding"
			else {
				distinguishingMarks.append(temp);
			}
			distinguishingMarks.append('|');
		}

		this.info = distinguishingMarks.toString();
	}

	@Override
	public boolean equals(final Object clientInfo) {
		final ClientInfo otherClientInfo = ConversionUtils.as(ClientInfo.class, clientInfo);

		return Objects.nonNull(otherClientInfo) && Objects.equals(this.info, otherClientInfo.info);
	}

	/**
	 * <p>
	 * This returns a string containing the specific client data of interest.
	 * </p>
	 *
	 * @return The client data
	 */
	public @Nullable String getInfo() {
		return this.info;
	}

	/**
	 * <p>
	 * Simply return the value of the info hashed (or -1 if it's null).
	 * </p>
	 */
	@Override
	public int hashCode() {
		return (Objects.isNull(this.info)) ? -1 : this.info.hashCode();
	}

	@Override
	public void readExternal(final @NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		ClientInfo.logger.debug("Constructing object...");

		this.info = ConversionUtils.as(String.class, in.readObject());
	}

	@Override
	public @Nullable String toString() {
		return this.info;
	}

	@Override
	public void writeExternal(final @NotNull ObjectOutput out) throws IOException {
		ClientInfo.logger.debug("Externalizing object...");

		out.writeObject(this.info);
	}
}
