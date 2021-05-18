package local.rdps.svja.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.util.ValidationUtils;

/**
 * <p>
 * This class is the primer workhorse any request that is going to the database.
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

		try (final Connection writeConn = DatabaseManager.getConnection(true)) {
			final DSLContext writeContext = DatabaseManager.getBuilder(writeConn);
			final UpdatableRecord<R> record = writeContext.newRecord(t, item);
			// // Use the VO's dirty fields list to determine what changed
			// Arrays.stream(record.fields()).forEach(field -> record.changed(field, false));
			// for (final String field : item.getDirtyFields()) {
			// if (!ValidationUtils.isEmpty(field)) {
			// try {
			// record.changed(CommonUtils.convertCamelCaseToUnderscore(field), true);
			// } catch (final IllegalArgumentException e) {
			// CommonDao.logger.error(e.getMessage(), e);
			// }
			// } else {
			// CommonDao.logger.error("Ignoring empty field name in the dirty fields list");
			// }
			// }
			record.changed("modified_by", true);
			record.changed("modified_date", true);

			final int updated;
			final UniqueKey<R> pk = t.getPrimaryKey();
			if (pk.getFields().stream().anyMatch(field -> Objects.isNull(record.getValue(field)))) {
				updated = record.insert();
			} else {
				Arrays.stream(record.fields()).filter(field -> pk.getFields().contains(field))
						.filter(field -> Objects.nonNull(record.get(field)))
						.forEach(field -> record.changed(field, true));
				updated = record.update();
			}

			// Check that we didn't delete too many rows
			if (updated > 1) {
				writeConn.setAutoCommit(false);
				writeConn.rollback();
				throw new ApplicationException("We updated too many items (" + Integer.valueOf(updated)
						+ " updated, but we only expected 1) so we rolled back:" + record);
			}
			if (updated == 0)
				return Optional.empty();

			record.refresh();
			return Optional.ofNullable((I) record.into(item.getClass()));
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
