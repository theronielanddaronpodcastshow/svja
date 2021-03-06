/*
 * This file is generated by jOOQ.
 */
package local.rdps.svja.dao.jooq.tables.interfaces;


import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.processing.Generated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.14.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IGroups extends Serializable {

    /**
     * Setter for <code>groups.id</code>.
     */
    public void setId(@NotNull Integer value);

    /**
     * Getter for <code>groups.id</code>.
     */
    @NotNull
    public Integer getId();

    /**
     * Setter for <code>groups.name</code>.
     */
    public void setName(@NotNull String value);

    /**
     * Getter for <code>groups.name</code>.
     */
    @NotNull
    public String getName();

    /**
     * Setter for <code>groups.description</code>.
     */
    public void setDescription(@NotNull String value);

    /**
     * Getter for <code>groups.description</code>.
     */
    @NotNull
    public String getDescription();

    /**
     * Setter for <code>groups.modified_by</code>.
     */
    public void setModifiedBy(@NotNull Integer value);

    /**
     * Getter for <code>groups.modified_by</code>.
     */
    @NotNull
    public Integer getModifiedBy();

    /**
     * Setter for <code>groups.modified_date</code>.
     */
    public void setModifiedDate(@Nullable LocalDateTime value);

    /**
     * Getter for <code>groups.modified_date</code>.
     */
    @Nullable
    public LocalDateTime getModifiedDate();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IGroups
     */
    public void from(IGroups from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IGroups
     */
    public <E extends IGroups> E into(E into);
}
