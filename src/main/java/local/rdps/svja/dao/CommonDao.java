package local.rdps.svja.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class is the primer workhorse for any request that is going to the database.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
class CommonDao {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * <p>
	 * This method examines the provided {@link ItemVo} instance and, if an ID is present, retrieves a collection
	 * containing that, and only that, record from the database. If no ID is present, all records of that type are
	 * returned.
	 * </p>
	 *
	 * @param item
	 *            An instance of the item that we would like to retrieve, with its ID set if we want just one
	 * @param <I>
	 *            A class that extends {@link ItemVo}
	 * @return A collection of filled out VOs
	 * @throws ApplicationException
	 *             Iff there is an error trying to connect to the database
	 */
	static <I extends ItemVo> Collection<I> getItems(final @NotNull I item) throws ApplicationException {
		final Table<Record> t = item.getReferenceTable();
		if (Objects.isNull(t)) {
			CommonDao.logger.error("Our ItemVo implementation is lacking a reference table");
			return Collections.emptyList();
		}

		try (final Connection readConn = DatabaseManager.getConnection(false)) {
			final DSLContext readContext = DatabaseManager.getBuilder(readConn);
			try (final SelectQuery<Record> query = readContext.selectQuery(t)) {
				if (ValidationUtils.isId(item.getId())) {
					query.addConditions(DSL.field(t.getQualifiedName().append("id"), Long.class).equal(item.getId()));
				}

				return (Collection<I>) query.fetchInto(item.getClass());
			}
		} catch (final @NotNull SQLException e) {
			CommonDao.logger.error(e.getMessage(), e);
		}

		return Collections.emptyList();
	}

	/**
	 * <p>
	 * This method uses the provided {@link Condition} to retrieve a collection containing that, and only that, record
	 * from the database. If no condition is present, all records of that type are returned.
	 * </p>
	 *
	 * @param <I>
	 *            A class that extends {@link ItemVo}
	 * @param item
	 *            An instance of the item that we would like to retrieve
	 * @param condition
	 *            The conditions to use when trying to get the item(s)
	 * @return A collection of filled out VOs
	 * @throws ApplicationException
	 *             Iff there is an error trying to connect to the database
	 */
	static <I extends ItemVo> Collection<I> getItems(final @NotNull I item, final @NotNull Condition condition)
			throws ApplicationException {
		final Table<Record> t = item.getReferenceTable();
		if (Objects.isNull(t)) {
			CommonDao.logger.error("Our ItemVo implementation is lacking a reference table");
			return Collections.emptyList();
		}

		try (final Connection readConn = DatabaseManager.getConnection(false)) {
			final DSLContext readContext = DatabaseManager.getBuilder(readConn);
			try (final SelectQuery<Record> query = readContext.selectQuery(t)) {
				query.addConditions(condition);

				return (Collection<I>) query.fetchInto(item.getClass());
			}
		} catch (final @NotNull SQLException e) {
			CommonDao.logger.error(e.getMessage(), e);
		}

		return Collections.emptyList();
	}

	/**
	 * <p>
	 * This method attempts to insert/update the given item in the database.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> this method does not update or insert any child items.
	 * </p>
	 *
	 * @param <I>
	 *            A {@link ItemVo}
	 * @param <R>
	 *            The jooq-generated {@link Record} specific to the {@link ItemVo}
	 * @param item
	 *            The item to be upserted
	 * @return The new/updated record or {@link Optional#empty()} if nothing was updated
	 * @throws ApplicationException
	 *             If too many rows were updated or we didn't like the item being upserted
	 */
	static <I extends ItemVo, R extends UpdatableRecord<R>> Optional<I> upsertItem(final @NotNull I item)
			throws ApplicationException {
		final Table<R> t = item.getReferenceTable();
		if (Objects.isNull(t)) {
			CommonDao.logger.error("Our ItemVo implementation is lacking a reference table");
			return Optional.empty();
		}

		// Set the modified* data
		item.setModifiedBy(DatabaseManager.getUid());
		item.setModifiedDate(Instant.now());

		final DSLContext writeContext = DatabaseManager.getBuilder();
		final UpdatableRecord<R> record = writeContext.newRecord(t, item);
		return CommonDao.upsertItem(record).map(r -> (I) r.into(item.getClass()));
	}

	/**
	 * <p>
	 * This method attempts to insert/update the given item in the database.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> this method does not update or insert any child items.
	 * </p>
	 *
	 * @param <R>
	 *            The jooq-generated {@link Record} specific to the {@link ItemVo}
	 * @param item
	 *            The item to be upserted
	 * @return The new/updated record or {@link Optional#empty()} if nothing was updated
	 * @throws ApplicationException
	 *             If too many rows were updated or we didn't like the item being upserted
	 */
	static <R extends UpdatableRecord<R>> Optional<UpdatableRecord<R>> upsertItem(
			final @NotNull UpdatableRecord<R> item) throws ApplicationException {
		final Table<R> t = item.getTable();
		if (Objects.isNull(t)) {
			CommonDao.logger.error("Our ItemVo implementation is lacking a reference table");
			return Optional.empty();
		}

		// Set the modified* data
		if (Arrays.stream(item.fields())
				.anyMatch(field -> field.getUnqualifiedName().equalsIgnoreCase(DSL.name("modified_by")))) {
			item.set(DSL.field("modified_by", SQLDataType.BIGINT), DatabaseManager.getUid());
			item.changed("modified_by", true);
		}
		if (Arrays.stream(item.fields())
				.anyMatch(field -> field.getUnqualifiedName().equalsIgnoreCase(DSL.name("modified_date")))) {
			item.set(DSL.field("modified_date", SQLDataType.INSTANT), Instant.now());
			item.changed("modified_date", true);
		}
		if (Arrays.stream(item.fields())
				.anyMatch(field -> field.getUnqualifiedName().equalsIgnoreCase(DSL.name("last_accessed")))) {
			item.set(DSL.field("last_accessed", SQLDataType.LOCALDATETIME(0)), LocalDateTime.now());
			item.changed("last_accessed", true);
		}

		try (final Connection writeConn = DatabaseManager.getConnection(true)) {
			DatabaseManager.setConfiguration(item, writeConn);

			// Ensure that the PK is present in the merge
			boolean upsertWithPk = false;
			final UniqueKey<R> pk = item.getTable().getPrimaryKey();
			if (Objects.nonNull(pk)) {
				if (ValidationUtils.not(ValidationUtils.isEmpty((Object[]) pk.getFieldsArray()))) {
					upsertWithPk = pk.getFields().stream().allMatch(field -> Objects.nonNull(item.get(field)));
					pk.getFields().stream().filter(field -> Objects.nonNull(item.get(field)))
							.forEach(field -> item.changed(field, true));
				}
			}

			// We don't have a PK, so we're going to insert and tell the thing to return data back to us
			if (ValidationUtils.not(upsertWithPk)) {
				final DSLContext writeContext = DatabaseManager.getBuilder(writeConn);
				final InsertQuery<R> query = writeContext.insertQuery(t);
				query.setReturning(t.fields());
				query.addRecord((R) item);
				query.execute();
				return Optional.ofNullable(query.getReturnedRecord());
			}

			final int updated = item.merge();

			// Check that we didn't update too many rows
			if (updated > 1) {
				writeConn.setAutoCommit(false);
				writeConn.rollback();
				throw new ApplicationException("We updated too many items (" + Integer.valueOf(updated)
						+ " updated, but we only expected 1) so we rolled back:" + item);
			}
			if (updated == 0)
				return Optional.empty();

			item.refresh();
			return Optional.ofNullable(item);
		} catch (final @NotNull SQLException e) {
			CommonDao.logger.error(e.getMessage(), e);
		}

		CommonDao.logger.error("We failed to upsert the record.");
		return Optional.empty();
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private CommonDao() {
		// Do nothing
	}
}
