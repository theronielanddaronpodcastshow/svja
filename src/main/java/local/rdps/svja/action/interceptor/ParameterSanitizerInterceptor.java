package local.rdps.svja.action.interceptor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter;
import org.apache.struts2.dispatcher.Parameter.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONWriter;
import org.noggit.JSONParser;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.json.JsonSanitizer;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import local.rdps.svja.constant.CommonConstants;
import local.rdps.svja.exception.IllegalParameterException;
import local.rdps.svja.util.EncodingUtils;
import local.rdps.svja.util.InfinitelyReadableHttpServletRequest;

/**
 * <p>
 * This interceptor cleanses all input of inappropriate HTML, CSS, JavaScript, and JSON code that may lead to cross-site
 * scripting via several different policies and methods.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class ParameterSanitizerInterceptor extends BaseInterceptor {
	/**
	 * <p>
	 * This class serves as an all-allowing input scanner that simply returns the user input.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	@SuppressWarnings("synthetic-access")
	private static final class AnythingGoesScanner extends Scanner {
		/**
		 * <p>
		 * This method simply returns the input, since we are really allowing any input.
		 * </p>
		 */
		@Override
		@NotNull
		String cleanString(@Nullable final String userInput) {
			return userInput;
		}
	}

	/**
	 * <p>
	 * This class provides logging functionality to the Java HTML Sanitizer code. When pushed to the sanitizer,
	 * instances of this class will log data about what was changed as part of sanitizing activities.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	@ThreadSafe
	private static final class InputChangeLogger implements HtmlChangeListener<ActionContext> {
		private static final Logger logger = LogManager.getLogger();

		/**
		 * <p>
		 * This method logs the discarded attribute(s).
		 * </p>
		 *
		 * @param context
		 *            The context of the request being sanitized
		 * @param tagName
		 *            The name of the tag holding the attributes that were discarded
		 * @param attributeNames
		 *            The attribute(s) that were discarded
		 */
		@Override
		public void discardedAttributes(@Nullable final ActionContext context, @NotNull final String tagName,
				@NotNull final String @NotNull... attributeNames) {
			if (InputChangeLogger.logger.isInfoEnabled()) {
				InputChangeLogger.logger.info(
						"The attributes [{}] were discarded from the tag [{}], under the context {}",
						String.join(", ", attributeNames), tagName, context);
			}
		}

		/**
		 * <p>
		 * This method logs the discarded tag(s)
		 * </p>
		 *
		 * @param context
		 *            The context of the request being sanitized
		 * @param elementName
		 *            The name of the element that was discarded
		 */
		@Override
		public void discardedTag(@Nullable final ActionContext context, @NotNull final String elementName) {
			if (InputChangeLogger.logger.isInfoEnabled()) {
				InputChangeLogger.logger.info("The tag [{}], was discarded, under the context {}", elementName,
						context);
			}
		}
	}

	/**
	 * <p>
	 * This class serves as the JSON input scanner, making use of the {@link JsonSanitizer "OWASP JSON Sanitizer"} to
	 * sanitize the input.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	@SuppressWarnings("synthetic-access")
	private static final class JsonScanner extends Scanner {
		private static final Logger _logger = LogManager.getLogger();
		/**
		 * Used to check for the most basic JSON 'structure' -- {}
		 */
		private static final Pattern JSON = Pattern.compile("^\\s*\\{.*\\}\\s*$", Pattern.DOTALL);
		/**
		 * The secondary scanner is used to sanitize strings
		 */
		private final Scanner secondaryScanner;

		JsonScanner(final String secondaryScannerName, final ActionContext context) {
			if (Objects.isNull(secondaryScannerName) || (secondaryScannerName.length() < 1)) {
				this.secondaryScanner = new StandardScanner(null, context);
			} else {
				switch (secondaryScannerName.toLowerCase()) {
					case "anything_goes":
						this.secondaryScanner = new AnythingGoesScanner();
						break;
					case "slashdot":
					case "tinymce":
						this.secondaryScanner = new StandardScanner(secondaryScannerName.toLowerCase(), context);
						break;
					default:
						JsonScanner._logger.error("The cross-site scripting policy {} could not be found",
								secondaryScannerName);
						this.secondaryScanner = new StandardScanner(null, context);
				}
			}
		}

		/**
		 * <p>
		 * This method runs through the parser, pumping the "cleaned" data into the writer.
		 * </p>
		 *
		 * @param parser
		 *            The parser, primed with data
		 * @param writer
		 *            The writer to write the clean JSON data to
		 * @throws IOException
		 */
		private void cleanJson(final JSONParser parser, final JSONWriter writer) throws IOException {
			int eventCode;
			while ((eventCode = parser.nextEvent()) != JSONParser.EOF) {
				switch (eventCode) {
					case JSONParser.OBJECT_START:
						writer.object();
						break;
					case JSONParser.OBJECT_END:
						writer.endObject();
						break;
					case JSONParser.ARRAY_START:
						writer.array();
						break;
					case JSONParser.ARRAY_END:
						writer.endArray();
						break;
					case JSONParser.BOOLEAN:
						writer.value(parser.getBoolean());
						break;
					case JSONParser.LONG:
						writer.value(parser.getLong());
						break;
					case JSONParser.NUMBER:
						writer.value(parser.getDouble());
						break;
					case JSONParser.BIGNUMBER:
						writer.value(parser.getNumberChars());
						break;
					case JSONParser.NULL:
						parser.getNull();
						writer.value(null);
						break;
					case JSONParser.STRING:
						if (parser.wasKey()) {
							writer.key(this.secondaryScanner.cleanString(parser.getString()));
						} else {
							writer.value(this.secondaryScanner.cleanString(parser.getString()));
						}
						break;
					default:
						JsonScanner._logger.error(
								"We don't understand event code {}, which the parser returned at position {}, so we are going to throw it away",
								Integer.valueOf(eventCode), Long.valueOf(parser.getPosition()));
				}
			}
		}

		@Override
		@NotNull
		String cleanString(@Nullable final String userInput) {
			if (Objects.isNull(userInput))
				return "null";

			// Check if the data is JSON -- if it isn't, just use the secondary scanner
			if (!JsonScanner.JSON.matcher(userInput).matches())
				return this.secondaryScanner.cleanString(userInput);

			String cleanString = JsonSanitizer.sanitize(userInput);
			try (final Writer stringWriter = new StringWriter(cleanString.length())) {
				final JSONWriter writer = new JSONWriter(stringWriter);
				cleanJson(new JSONParser(cleanString), writer);
				stringWriter.flush();
				cleanString = stringWriter.toString();
			} catch (final IOException e) {
				JsonScanner._logger.error("An error occurred trying to create a cleaned string of the JSON input:"
						+ System.lineSeparator() + cleanString, e);
			}

			return cleanString;
		}
	}

	/**
	 * <p>
	 * This abstract class dictates what it means to be an input scanner.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	private abstract static class Scanner {
		private static final Logger _logger = LogManager.getLogger();
		/**
		 * A {@link Map} containing action names as the keys and the corresponding HTML policy that should be used for
		 * that action name
		 */
		private static final Map<String, String> ACTION_HTML_POLICIES = Scanner.loadActionHtmlPolicies();

		/**
		 * <p>
		 * This method reads the struts.xml file and pulls out policies explicitly defined for each action.
		 * </p>
		 *
		 * @return A {@link Map} that uses the action's name as the key and the cross-site scripting profile tied to the
		 *         action as the value
		 */
		private static Map<String, String> loadActionHtmlPolicies() {
			final String HTML_POLICY_PROPERTY_NAME = "xssProtectionsPolicy";
			final Pattern PATTERN_FIND_DYNAMIC_ACTION = Pattern.compile("\\{[^}]+}");

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final Map<String, String> actionHtmlPolicies = new HashMap<>();
			try {
				final DocumentBuilder builder = factory.newDocumentBuilder();
				final URL strutsXml = Thread.currentThread().getContextClassLoader().getResource("struts.xml");
				final Document doc = builder.parse(strutsXml.openStream());
				doc.getDocumentElement().normalize();
				final NodeList struts = doc.getElementsByTagName("struts");
				Element strutsElement = null;
				for (int s = 0; s < struts.getLength(); s++) {
					final Node strutsNode = struts.item(s);
					if (strutsNode.getNodeType() == Node.ELEMENT_NODE) {
						strutsElement = (Element) strutsNode;
					}
				}

				final NodeList packages = strutsElement.getElementsByTagName("package");
				for (int p = 0; p < packages.getLength(); p++) {
					final Node pack = packages.item(p);
					final Element packElement = (Element) pack;
					final NodeList actions = packElement.getElementsByTagName("action");
					for (int a = 0; a < actions.getLength(); a++) {
						final Node action = actions.item(a);
						if (action.getNodeType() == Node.ELEMENT_NODE) {
							final Element actionElement = (Element) action;
							final String actionName = actionElement.getAttribute("name");
							final NodeList params = actionElement.getElementsByTagName("param");
							for (int j = 0; j < params.getLength(); j++) {
								final Node param = params.item(j);
								final Element paramElement = (Element) param;
								if (HTML_POLICY_PROPERTY_NAME.equals(paramElement.getAttribute("name"))) {
									actionHtmlPolicies.put(
											PATTERN_FIND_DYNAMIC_ACTION.matcher(actionName).replaceAll(
													ParameterSanitizerInterceptor.DYNAMIC_ACTION_PLACEHOLDER),
											paramElement.getTextContent());
								}
							}
						}
					}
				}
			} catch (final ParserConfigurationException | SAXException | IOException e) {
				Scanner._logger.error("Error parsing the struts.xml file for HTML policies. {}", e.getMessage());
			}
			return Collections.unmodifiableMap(actionHtmlPolicies);
		}

		/**
		 * <p>
		 * This method more completely logs changes made by the scanner to input.
		 * </p>
		 *
		 * @param original
		 *            The original input
		 * @param modified
		 *            The input after being cleaned
		 */
		private static void logHtmlRemoval(@Nullable final String original, @Nullable final String modified) {
			if (Objects.equals(original, modified)
					|| (Objects.equals(CommonConstants.EMPTY_STRING, original) && Objects.isNull(modified))
					|| (Objects.equals(CommonConstants.EMPTY_STRING, modified) && Objects.isNull(original)))
				return;

			if (Scanner._logger.isInfoEnabled()) {
				Scanner._logger.info("We have cleaned the string: {}; changing it to: {}", original, modified);
			}
		}

		/**
		 * <p>
		 * This method returns either a {@link Scanner} instance that will support the desired scanning capabilities and
		 * policy requirements.
		 * </p>
		 *
		 * @param actionName
		 *            The action that we are going to scan, having all dynamic portions of the action replaced with {}
		 * @param context
		 *            The context in which we are operating
		 * @return A scanner that can scan user input
		 */
		@SuppressWarnings("synthetic-access")
		@NotNull
		static Scanner getScanner(@Nullable final String actionName, final ActionContext context) {
			if (Objects.isNull(actionName))
				return new StandardScanner(actionName, context);

			final String policyName = Scanner.ACTION_HTML_POLICIES.get(actionName);
			if (local.rdps.svja.util.ValidationUtils.isEmpty(policyName))
				return new JsonScanner("tinymce", context);

			switch (policyName.toLowerCase()) {
				case "anything_goes":
					return new AnythingGoesScanner();
				case "slashdot":
				case "tinymce":
					return new StandardScanner(policyName.toLowerCase(), context);
				case "json":
					return new JsonScanner("tinymce", context);
				case "json|anything_goes":
					return new JsonScanner("anything_goes", context);
				case "json|slashdot":
					return new JsonScanner("slashdot", context);
				case "json|tinymce":
					return new JsonScanner("tinymce", context);
				default:
					Scanner._logger.error("The cross-site scripting policy {} could not be found", policyName);
					return new StandardScanner(null, context);
			}
		}

		/**
		 * <p>
		 * This method cleans all parameters within the given {@link HttpParameters}, including any multi-value
		 * parameters. The newly cleaned parameters are placed back into the given {@link HttpParameters}.
		 * </p>
		 * <p>
		 * <em>Please note that this is a <u>mutative</u> method.</em>
		 * </p>
		 *
		 * @param parameters
		 *            The {@link HttpParameters} that need to be cleaned
		 */
		void cleanAllParameters(@Nullable final Map<String, Parameter> parameters) {
			if (Objects.nonNull(parameters)) {
				for (final Entry<String, Parameter> parameterEntry : parameters.entrySet()) {
					final Parameter parameter = parameterEntry.getValue();
					if (parameter.isMultiple()) {
						if (parameter.getMultipleValues().length > 0) {
							// We need to sanitize all values in this multi-value parameter
							final String[] paramVals = new String[parameter.getMultipleValues().length];
							String value;
							for (int i = 0; i < parameter.getMultipleValues().length; i++) {
								value = parameter.getMultipleValues()[i];
								paramVals[i] = cleanString(value);
								Scanner.logHtmlRemoval(value, paramVals[i]);
							}

							// Put in our sanitized parameter
							final Parameter newParameterValue = new Request(parameter.getName(), paramVals);
							parameterEntry.setValue(newParameterValue);
						}
					} else {
						final String goodParameterValue = cleanString(parameter.getValue());
						Scanner.logHtmlRemoval(parameter.getValue(), goodParameterValue);

						// Put in our sanitized parameter
						final Parameter newParameterValue = new Request(parameter.getName(), goodParameterValue);
						parameterEntry.setValue(newParameterValue);
					}
				}
			}
		}

		/**
		 * <p>
		 * This method cleans the request's InputStream, actually swapping it out with a freshly cleaned stream.
		 * </p>
		 * <p>
		 * <em>Please note that this is a <u>mutative</u> method.</em>
		 * </p>
		 *
		 * @param request
		 *            The {@link HttpServletRequest} that needs to be cleaned
		 */
		void cleanInputStream(@Nullable final HttpServletRequest request) {
			final InfinitelyReadableHttpServletRequest newRequest = new InfinitelyReadableHttpServletRequest(request);
			ServletActionContext.setRequest(newRequest);
			final byte[] bytes = newRequest.getInputStream().readAllBytes();
			final String body = new String(bytes);
			final String cleanBody = cleanString(body);
			Scanner.logHtmlRemoval(body, cleanBody);
			newRequest.setInputStream(ByteBuffer.wrap(cleanBody.getBytes()));
		}

		/**
		 * <p>
		 * This method cleans the given {@link String} and returns the cleaned output.
		 * </p>
		 *
		 * @param userInput
		 *            The user's input
		 * @return The user's input after it has been cleaned according to the scanner's policies
		 */
		@NotNull
		abstract String cleanString(@Nullable String userInput);
	}

	/**
	 * <p>
	 * This class serves as the standard input scanner, making use of the {@link PolicyFactory "OWASP Java HTML
	 * Sanitizer"}.
	 * </p>
	 *
	 * @author DaRon
	 * @since 1.0
	 */
	private static final class StandardScanner extends Scanner {
		/**
		 * Matches only on a string that consists of nothing or up to two consecutive digits
		 */
		private static final Pattern _PATTERN_UP_TO_A_COUPLE_OF_DIGITS = Pattern.compile("^[0-9]{0,2}$");
		/**
		 * At the moment, these are added for some reason by the Java HTML Sanitizer if curly brackets are present
		 */
		private static final Pattern EMPTY_HTML_COMMENTS = Pattern.compile("[{]<![-]{2} [-]{2}>");
		/**
		 * A policy definition that allows <strong>no</strong> HTML
		 */
		private static final PolicyFactory HTML_SANITIZER_ALLOW_NOTHING_POLICY = new HtmlPolicyBuilder().toFactory();
		/**
		 * A policy definition that allows the same HTML as allowed by SlashDot
		 */
		private static final PolicyFactory HTML_SANITIZER_SLASHDOT_POLICY = new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				// Allow title="..." on any element
				.allowAttributes("title").globally()
				// Allow lang= with an alphabetic value on any element
				.allowAttributes("lang").matching(Pattern.compile("^[a-zA-Z][a-zA-Z-]{0,18}[a-zA-Z]$")).globally()
				// Allow href="..." on <a> elements
				.allowAttributes("href").onElements("a")
				// Defeat link spammers
				.requireRelNofollowOnLinks()
				// The align attribute on <p> elements can have any value below
				.allowAttributes("align").matching(true, "center", "left", "right", "justify", "char").onElements("p")
				// These elements are allowed
				.allowElements("a", "p", "div", "i", "b", "em", "blockquote", "tt", "strong", "br", "ul", "ol", "li")
				// Custom slashdot tags
				// These could be rewritten in the sanitizer using an ElementPolicy
				.allowElements("quote", "ecode").toFactory();
		/**
		 * A policy definition that allows only the kinds of HTML that TinyMCE produces
		 */
		private static final PolicyFactory HTML_SANITIZER_TINYMCE_POLICY = new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				// Allow our elements
				.allowElements("a", "b", "blockquote", "br", "center", "dd", "div", "dl", "dt", "em", "hr", "i", "li",
						"ol", "p", "s", "span", "strike", "strong", "sub", "sup", "u", "ul", "table", "tbody", "td",
						"thead", "tr", "tt")
				// Allow empty versions of most of the above tags
				.allowWithoutAttributes("b", "blockquote", "br", "center", "dd", "div", "dl", "dt", "em", "hr", "i",
						"li", "ol", "p", "s", "span", "strike", "strong", "sub", "sup", "u", "ul", "table", "tbody",
						"td", "thead", "tr", "tt")
				// Require our a tags to have attributes
				.disallowWithoutAttributes("a")
				// Disallow the global attributes for br, center, hr, sub, sup, and strike tags, meaning that these tags
				// should be empty
				.disallowAttributes("lang", "style", "title").onElements("br", "center", "hr", "sub", "sup", "strike")
				/*
				 * Set up our attributes
				 */
				/*
				 * Globals first!
				 */
				// Allow lang attributes for any element, so long as it is is alphaalpha+ or alpha+, plus a hyphen, plus
				// alpha+
				.allowAttributes("lang").matching(Pattern.compile("^[a-zA-Z][a-zA-Z-]{0,18}[a-zA-Z]$")).globally()
				// Allow style attributes for any element
				.allowAttributes("style").globally()
				// Allow title attributes for any element, with all of the characters needed to make most any title
				.allowAttributes("title").matching(Pattern.compile("^[a-zA-Z0-9\\s_',:\\[\\]!\\./\\\\\\(\\)&-]*$"))
				.globally()
				/*
				 * Tag-specific attributes
				 */
				// Allow valid align in p tags
				.allowAttributes("align")
				.matching(Pattern.compile(
						"^([cC][eE][nN][tT][eE][rR]|[lL][eE][fF][tT]|[rR][iI][gG][hH][tT]|[jJ][uU][sS][tT][iI][fF][yY]|[cC][hH][aA][rR])$"))
				.onElements("p")
				// Allow border in table tags
				.allowAttributes("border").matching(StandardScanner._PATTERN_UP_TO_A_COUPLE_OF_DIGITS)
				.onElements("table")
				// Allow cellpadding in table tags
				.allowAttributes("cellpadding").matching(StandardScanner._PATTERN_UP_TO_A_COUPLE_OF_DIGITS)
				.onElements("table")
				// Allow cellspacing in table tags
				.allowAttributes("cellspacing").matching(StandardScanner._PATTERN_UP_TO_A_COUPLE_OF_DIGITS)
				.onElements("table")
				// Allow href in a tags
				.allowAttributes("href")
				.matching(Pattern.compile(
						"^(([\\p{L}\\p{N}\\p{Zs}/\\.\\?=&amp;\\-~])+|(\\s)*((ht|f)tps?://|mailto:)[A-Za-z0-9]+[~a-zA-Z0-9_\\.@\\#\\$%&;:,\\?=/\\+!\\\\\\(\\)-]*(\\s)*)$"))
				.onElements("a")
				// Allow the rel attribute in our a tags if they say "nofollow"
				.allowAttributes("rel").matching(Pattern.compile("[nN][oO][fF][oO][lL][lL][oO][wW]")).onElements("a")
				// Allow width in table tags
				.allowAttributes("width").matching(Pattern.compile("^[0-9]{1,4}(\\spx[;]?)?$")).onElements("table")
				// Build this puppy
				.toFactory();
		/**
		 * Used for logging when the scanner makes a change to the content
		 */
		@SuppressWarnings("synthetic-access")
		private static final InputChangeLogger INPUT_CHANGE_LOGGER = new InputChangeLogger();
		private static final Logger logger = LogManager.getLogger();
		/**
		 * The the context under which we are currently running
		 */
		final ActionContext context;
		/**
		 * The Java HTML Sanitizer scanner we will use to scan the data
		 */
		final PolicyFactory scanner;

		/**
		 * <p>
		 * This constructor makes a new instance of this class that uses either the specified policy or the default one
		 * if the specific policy cannot be found or no policy is requested.
		 * </p>
		 *
		 * @param policyName
		 *            The policy name that we are supposed to use when determining what scanner to use
		 * @param context
		 *            The context from which this {@link HttpParameters} is being run, used for logging purposes
		 */
		@SuppressWarnings("synthetic-access")
		StandardScanner(@Nullable final String policyName, @Nullable final ActionContext context) {
			this.context = context;

			if (Objects.isNull(policyName)) {
				this.scanner = StandardScanner.HTML_SANITIZER_ALLOW_NOTHING_POLICY;
			} else {
				final String lowercasePolicyName = policyName.toLowerCase();
				if (Objects.equals("slashdot", lowercasePolicyName)) {
					this.scanner = StandardScanner.HTML_SANITIZER_SLASHDOT_POLICY;
				} else if (Objects.equals("tinymce", lowercasePolicyName)) {
					this.scanner = StandardScanner.HTML_SANITIZER_TINYMCE_POLICY;
				} else {
					if (StandardScanner.logger.isWarnEnabled()) {
						StandardScanner.logger.warn(
								"We couldn't find a cross-site scripting policy tied to the requested policy name ({}) for the action ({}) and are failing-secure",
								lowercasePolicyName, context);
					}
					this.scanner = StandardScanner.HTML_SANITIZER_ALLOW_NOTHING_POLICY;
				}
			}
		}

		@Override
		@NotNull
		String cleanString(@Nullable final String userInput) {
			if (local.rdps.svja.util.ValidationUtils.isEmpty(userInput))
				return CommonConstants.EMPTY_STRING;
			final String cleanString = this.scanner.sanitize(userInput, StandardScanner.INPUT_CHANGE_LOGGER,
					this.context);
			return EncodingUtils.decodeHtml(StandardScanner.EMPTY_HTML_COMMENTS.matcher(cleanString).replaceAll("{"));
		}
	}

	/**
	 * This placeholder goes in place of the dynamic portion of a dynamic action's name
	 */
	private static final String DYNAMIC_ACTION_PLACEHOLDER = "{}";

	/**
	 * Finds all consecutive digits
	 */
	private static final Pattern PATTERN_FIND_DIGITS = Pattern.compile("\\d+");

	private static final long serialVersionUID = 3040000L;

	/**
	 * <p>
	 * Scan the response for anything HTML within the parameters that isn't allowed.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public String svjaIntercept(final ActionInvocation invocation) throws Exception {
		if (Objects.isNull(invocation))
			throw new IllegalParameterException("The invocation cannot be null");

		final ActionContext context = invocation.getInvocationContext();
		if (Objects.isNull(context))
			throw new IllegalParameterException("The Action Context inside of the invocation cannot be null");

		final HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		if (Objects.isNull(request))
			throw new IllegalParameterException("The HttpServletRequest cannot be null");

		final String actionName;
		if (local.rdps.svja.util.ValidationUtils.isEmpty(context.getName())) {
			actionName = CommonConstants.EMPTY_STRING;
		} else {
			actionName = ParameterSanitizerInterceptor.PATTERN_FIND_DIGITS.matcher(context.getName())
					.replaceAll(ParameterSanitizerInterceptor.DYNAMIC_ACTION_PLACEHOLDER);
		}
		final Scanner scanner = Scanner.getScanner(actionName, context);
		scanner.cleanAllParameters(context.getParameters());
		// Let's make certain that we scanned the data -- scan the input stream directly if we don't think that we got
		// everything, such as if we are using application/json (note that application/* is not pumped into the
		// parameter map by Struts because all application/* Content Types require you to read the InputStream and
		// understand how to parse it)
		if (Objects.isNull(request.getContentType())
				|| Objects.equals(request.getContentType().toLowerCase(), "application/json")
				|| Objects.isNull(context.getParameters()) || context.getParameters().isEmpty()) {
			scanner.cleanInputStream(request);
		}

		// Move on through the invocation stack
		return invocation.invoke();
	}
}
