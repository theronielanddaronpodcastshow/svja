package local.rdps.svja.dao;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.DeleteConditionStep;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteType;
import org.jooq.Query;
import org.jooq.Update;
import org.jooq.UpdateConditionStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DefaultExecuteListener;

import local.rdps.svja.exception.SqlPerformanceWarning;
import local.rdps.svja.util.Sequence;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class acts as a listener for jooq executes. Here is where we control automated, uniform tasks and
 * debugging/monitoring activities.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class DatabaseListener extends DefaultExecuteListener {
	private static final Logger _logger = LogManager.getLogger();
	private static final Pattern NULL_CHARACTER = Pattern.compile("[\u0000]");
	private static final long serialVersionUID = 2070000L;
	// 5 seconds
	private static final Duration SQL_EXECUTION_TIME_THRESHOLD = Duration.ofSeconds(5);
	private static final Pattern WHERE_CLAUSE = Pattern.compile("\\s[wW][hH][eE][rR][eE]\\s");
	private Instant startTime;

	/**
	 * Hook into the query execution lifecycle after executing queries to monitor for queries that take too long, which
	 * we then print. Also, print off the number of rows impacted by the execution.
	 *
	 * @param ctx
	 *            The execution context
	 */
	@Override
	public void executeEnd(final @NotNull ExecuteContext ctx) {
		super.executeEnd(ctx);

		final Duration time = Duration.between(this.startTime, Instant.now()).abs();
		if (time.compareTo(DatabaseListener.SQL_EXECUTION_TIME_THRESHOLD) > 0) {
			// Ignore non-user actions
			try {
				final HttpServletRequest request = ServletActionContext.getRequest();
				if (Objects.isNull(request))
					return;
			} catch (final NullPointerException e) {
				return;
			}

			final String query;
			// If we're executing a query
			if (Objects.nonNull(ctx.query())) {
				query = ctx.query().getSQL(ParamType.INLINED);
			}
			// If we're executing a routine
			else if (ValidationUtils.not(ValidationUtils.isEmpty(ctx.routine()))) {
				query = ctx.routine().toString();
			}
			// If we're executing anything else (e.g. plain SQL)
			else {
				query = ctx.sql();
			}

			final SqlPerformanceWarning e = new SqlPerformanceWarning(query);
			DatabaseListener._logger.log(e.getLogLevel(),
					"(this is a warning that the user never sees) - " + e.getExceptionTitle() + " (took " + time
							+ ", which is greater than the threshold of "
							+ DatabaseListener.SQL_EXECUTION_TIME_THRESHOLD + "); " + e.getMessage()
							+ System.lineSeparator() + e.getLimitedStackTrace());
		}

		if (DatabaseListener._logger.isDebugEnabled()) {
			DatabaseListener._logger.debug("The query impacted {} rows", Integer.valueOf(ctx.rows()));
		}
	}

	/**
	 * Hook into the query execution lifecycle before executing queries. We are simply printing out the queries.
	 *
	 *
	 * @param ctx
	 *            The execution context
	 */
	@Override
	public void executeStart(final @NotNull ExecuteContext ctx) {
		if (DatabaseListener._logger.isDebugEnabled()) {
			final DSLContext create = DatabaseManager.getBuilder();
			// If we're executing a query
			if (Objects.nonNull(ctx.query())) {
				final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				final StringBuilder st = new StringBuilder(512);
				Sequence.stream(0, Math.min(stack.length, 25) - 1)
						.forEach(i -> st.append(stack[i]).append(System.lineSeparator()));
				DatabaseListener._logger.debug("{};{}{}", create.renderInlined(ctx.query()), System.lineSeparator(),
						st);
			}

			// If we're executing a routine
			else if (!ValidationUtils.isEmpty(ctx.routine())) {
				DatabaseListener._logger.debug(create.renderInlined(ctx.routine()));
			}
			// If we're executing anything else (e.g. plain SQL)
			else if (!ValidationUtils.isEmpty(ctx.sql()))
				throw new RuntimeException(
						"Someone is trying to send pure SQL queries... we don't allow that anymore (use jOOq): "
								+ ctx.sql());
		}
		super.executeStart(ctx);
		this.startTime = Instant.now();
	}

	/**
	 * Hook into the query execution lifecycle after fetching from the database. We are simply returning the last
	 * fetched record.
	 *
	 * @param ctx
	 *            The execution context
	 */
	@Override
	public void fetchEnd(final ExecuteContext ctx) {
		if (DatabaseListener._logger.isDebugEnabled()) {
			if (Objects.nonNull(ctx.record())) {
				DatabaseListener._logger.debug("The query's last record retrieved was {}", ctx.record());
			}
		}
	}

	/**
	 * Hook into the query execution lifecycle before rendering queries. Check for 'run-away' updates and deletes.
	 *
	 *
	 * @param ctx
	 *            The execution context
	 */
	@Override
	public void renderStart(final @NotNull ExecuteContext ctx) {
		if (ctx.type() != ExecuteType.WRITE)
			return;

		try (final Query query = ctx.query()) {
			// Is our Query object empty? If not, let's run through it
			if (!ValidationUtils.isEmpty(query)) {
				// Get rid of nulls
				query.getParams().entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
						.filter(entry -> !ValidationUtils.isEmpty(entry.getValue().getValue()))
						.filter(entry -> CharSequence.class.isAssignableFrom(entry.getValue().getDataType().getType()))
						.filter(entry -> DatabaseListener.NULL_CHARACTER
								.matcher((CharSequence) entry.getValue().getValue()).find())
						.forEach(entry -> query.bind(entry.getKey(), DatabaseListener.NULL_CHARACTER
								.matcher((CharSequence) entry.getValue().getValue()).replaceAll("")));

				if (query instanceof Update) {
					if (!(query instanceof UpdateConditionStep)) {
						if (!DatabaseListener.WHERE_CLAUSE.matcher(query.getSQL(ParamType.INDEXED)).find()) {
							final String queryString = query.getSQL(ParamType.INLINED);
							throw new RuntimeException(
									"Someone is trying to run an UPDATE query without a WHERE clause ("
											+ query.getClass() + "): " + queryString);
						}
					}
				} else if (query instanceof Delete) {
					if (!(query instanceof DeleteConditionStep)) {
						if (!DatabaseListener.WHERE_CLAUSE.matcher(query.getSQL(ParamType.INDEXED)).find()) {
							final String queryString = query.getSQL(ParamType.INLINED);
							throw new RuntimeException(
									"Someone is trying to run a DELETE query without a WHERE clause ("
											+ query.getClass() + "): " + queryString);
						}
					}
				}
			} else
				throw new RuntimeException(
						"Someone is trying to send pure SQL queries... we don't allow that anymore (use jOOq): "
								+ ctx.sql());
		}
	}
}
