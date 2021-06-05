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
public interface ISessions extends Serializable {

    /**
     * Setter for <code>sessions.id</code>.
     */
    public void setId(@NotNull Integer value);

    /**
     * Getter for <code>sessions.id</code>.
     */
    @NotNull
    public Integer getId();

    /**
     * Setter for <code>sessions.session_data</code>.
     */
    public void setSessionData(@Nullable String value);

    /**
     * Getter for <code>sessions.session_data</code>.
     */
    @Nullable
    public String getSessionData();

    /**
     * Setter for <code>sessions.last_accessed</code>.
     */
    public void setLastAccessed(@Nullable LocalDateTime value);

    /**
     * Getter for <code>sessions.last_accessed</code>.
     */
    @Nullable
    public LocalDateTime getLastAccessed();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface ISessions
     */
    public void from(ISessions from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface ISessions
     */
    public <E extends ISessions> E into(E into);
}