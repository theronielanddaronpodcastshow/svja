package local.rdps.svja.dao;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.UpdatableRecord;

import local.rdps.svja.construct.ItemVo;
import local.rdps.svja.exception.ApplicationException;
import local.rdps.svja.exception.NotFoundException;

/**
 * <p>
 * This class serves as the gateway for the {@link CommonDao}, giving all other packages access to the methods contained
 * therein.
 * </p>
 *
 * @author DaRon
 * @since 1.0
 */
public class CommonDaoGateway {
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
	 * @throws NotFoundException
	 *             Iff the item does not exist or the item is invalid
	 * @throws ApplicationException
	 *             Iff there is an error trying to connect to the database
	 */
	public static <I extends ItemVo> @NotNull Collection<I> getItems(final @NotNull I item)
			throws ApplicationException {
		if (Objects.isNull(item))
			throw new NotFoundException("We were trying to get a null item or items");
		return CommonDao.getItems(item);
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
	 * @throws NotFoundException
	 *             Iff the item does not exist or the item is invalid
	 * @throws ApplicationException
	 *             Iff there is an error trying to connect to the database
	 */
	public static <I extends ItemVo> @NotNull Collection<I> getItems(final @NotNull I item,
			final @NotNull Condition condition) throws ApplicationException {
		if (Objects.isNull(item))
			throw new NotFoundException("We were trying to get a null item or items");
		if (Objects.isNull(condition))
			return CommonDaoGateway.getItems(item);
		return CommonDao.getItems(item, condition);
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
	public static <I extends ItemVo, R extends UpdatableRecord<R>> Optional<I> upsertItem(final @NotNull I item)
			throws ApplicationException {
		if (Objects.isNull(item))
			throw new NotFoundException("We were trying to set a null item");
		return CommonDao.upsertItem(item);
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
	public static <R extends UpdatableRecord<R>> Optional<UpdatableRecord<R>> upsertItem(
			final @NotNull UpdatableRecord<R> item) throws ApplicationException {
		if (Objects.isNull(item))
			throw new NotFoundException("We were trying to set a null item");
		return CommonDao.upsertItem(item);
	}

	/**
	 * <p>
	 * No one should be instantiating this class.
	 * </p>
	 */
	private CommonDaoGateway() {
		// Do nothing
	}
}
