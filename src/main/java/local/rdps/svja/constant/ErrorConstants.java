package local.rdps.svja.constant;

/**
 * <p>
 * This class contains the various error constants used by the application to create or adjust error handling.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public final class ErrorConstants {
	/**
	 * Used to help make logging decisions. This constant means that we should not log the stack trace at all.
	 *
	 * @since 1.0
	 */
	public static final byte DO_NOT_LOG_STACK_TRACE = 0;
	public static final String ERROR_BLO_ILLEGAL_PARAMETER = "Illegal parameter in BLO: ";
	public static final String ERROR_BLO_NOTFOUND = "Object not found (BLO): ";
	public static final String ERROR_CREATE_PERMISSION = "Create Permission error: userId = ";

	public static final String ERROR_DAO_ILLEGAL_PARAMETER = "Illegal parameter in DAO: ";
	public static final String ERROR_DAO_NOTFOUND = "Object not found (DAO): ";
	public static final String ERROR_DELETE_PERMISSION = "Delete Permission error: userId = ";
	public static final String ERROR_FILE_NOTFOUND = "File not found: ";
	public static final String ERROR_INVALID_SESSION = "User session failed validation check.";
	public static final String ERROR_MODERATE_PERMISSION = "Moderate Permission error: userId = ";
	public static final String ERROR_READ_PERMISSION = "Read Permission error: userId = ";
	public static final String ERROR_TYPE_CONVERSION = "Error trying to convert data types: ";
	public static final String ERROR_UNKNOWN_REQUEST = "User attempted to access an unmapped action.";
	public static final String ERROR_UPDATE_PERMISSION = "Update Permission error: userId = ";
	/**
	 * Used to help make logging decisions. This constant means that we should log only some of the stack track with the
	 * exception -- all in one entry and at the same level (trace follows message).
	 *
	 * @since 1.0
	 */
	public static final byte LOG_LIMITED_STACK_TRACE = 3;
	/**
	 * Used to help make logging decisions. This constant means that we should log the stack trace, but do so in a
	 * separate, INFO message.
	 *
	 * @since 1.0
	 */
	public static final byte LOG_STACK_TRACE_AS_INFO = 2;
	/**
	 * Used to help make logging decisions. This constant means that we should log the stack trace with the exception --
	 * all in one entry and at the same level (trace follows message).
	 *
	 * @since 1.0
	 */
	public static final byte LOG_STACK_TRACE_WITH_EXCEPTION = 1;

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 *
	 * @since 1.0
	 */
	private ErrorConstants() {
		// Do nothing
	}
}
