/*
 * This file is generated by jOOQ.
 */
package local.rdps.svja.dao.jooq.tables.records;


import java.time.LocalDateTime;

import javax.annotation.processing.Generated;

import local.rdps.svja.dao.jooq.tables.Projects;
import local.rdps.svja.dao.jooq.tables.interfaces.IProjects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


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
public class ProjectsRecord extends UpdatableRecordImpl<ProjectsRecord> implements Record5<Integer, String, String, Integer, LocalDateTime>, IProjects {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>projects.id</code>.
     */
    @Override
    public void setId(@NotNull Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>projects.id</code>.
     */
    @NotNull
    @Override
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>projects.title</code>.
     */
    @Override
    public void setTitle(@NotNull String value) {
        set(1, value);
    }

    /**
     * Getter for <code>projects.title</code>.
     */
    @NotNull
    @Override
    public String getTitle() {
        return (String) get(1);
    }

    /**
     * Setter for <code>projects.description</code>.
     */
    @Override
    public void setDescription(@Nullable String value) {
        set(2, value);
    }

    /**
     * Getter for <code>projects.description</code>.
     */
    @Nullable
    @Override
    public String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>projects.modified_by</code>.
     */
    @Override
    public void setModifiedBy(@NotNull Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>projects.modified_by</code>.
     */
    @NotNull
    @Override
    public Integer getModifiedBy() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>projects.modified_date</code>.
     */
    @Override
    public void setModifiedDate(@Nullable LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>projects.modified_date</code>.
     */
    @Nullable
    @Override
    public LocalDateTime getModifiedDate() {
        return (LocalDateTime) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    @NotNull
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    @NotNull
    public Row5<Integer, String, String, Integer, LocalDateTime> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    @NotNull
    public Row5<Integer, String, String, Integer, LocalDateTime> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    @NotNull
    public Field<Integer> field1() {
        return Projects.PROJECTS.ID;
    }

    @Override
    @NotNull
    public Field<String> field2() {
        return Projects.PROJECTS.TITLE;
    }

    @Override
    @NotNull
    public Field<String> field3() {
        return Projects.PROJECTS.DESCRIPTION;
    }

    @Override
    @NotNull
    public Field<Integer> field4() {
        return Projects.PROJECTS.MODIFIED_BY;
    }

    @Override
    @NotNull
    public Field<LocalDateTime> field5() {
        return Projects.PROJECTS.MODIFIED_DATE;
    }

    @Override
    @NotNull
    public Integer component1() {
        return getId();
    }

    @Override
    @NotNull
    public String component2() {
        return getTitle();
    }

    @Override
    @Nullable
    public String component3() {
        return getDescription();
    }

    @Override
    @NotNull
    public Integer component4() {
        return getModifiedBy();
    }

    @Override
    @Nullable
    public LocalDateTime component5() {
        return getModifiedDate();
    }

    @Override
    @NotNull
    public Integer value1() {
        return getId();
    }

    @Override
    @NotNull
    public String value2() {
        return getTitle();
    }

    @Override
    @Nullable
    public String value3() {
        return getDescription();
    }

    @Override
    @NotNull
    public Integer value4() {
        return getModifiedBy();
    }

    @Override
    @Nullable
    public LocalDateTime value5() {
        return getModifiedDate();
    }

    @Override
    @NotNull
    public ProjectsRecord value1(@NotNull Integer value) {
        setId(value);
        return this;
    }

    @Override
    @NotNull
    public ProjectsRecord value2(@NotNull String value) {
        setTitle(value);
        return this;
    }

    @Override
    @NotNull
    public ProjectsRecord value3(@Nullable String value) {
        setDescription(value);
        return this;
    }

    @Override
    @NotNull
    public ProjectsRecord value4(@NotNull Integer value) {
        setModifiedBy(value);
        return this;
    }

    @Override
    @NotNull
    public ProjectsRecord value5(@Nullable LocalDateTime value) {
        setModifiedDate(value);
        return this;
    }

    @Override
    @NotNull
    public ProjectsRecord values(@NotNull Integer value1, @NotNull String value2, @Nullable String value3, @NotNull Integer value4, @Nullable LocalDateTime value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IProjects from) {
        setId(from.getId());
        setTitle(from.getTitle());
        setDescription(from.getDescription());
        setModifiedBy(from.getModifiedBy());
        setModifiedDate(from.getModifiedDate());
    }

    @Override
    public <E extends IProjects> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProjectsRecord
     */
    public ProjectsRecord() {
        super(Projects.PROJECTS);
    }

    /**
     * Create a detached, initialised ProjectsRecord
     */
    public ProjectsRecord(@NotNull Integer id, @NotNull String title, @Nullable String description, @NotNull Integer modifiedBy, @Nullable LocalDateTime modifiedDate) {
        super(Projects.PROJECTS);

        setId(id);
        setTitle(title);
        setDescription(description);
        setModifiedBy(modifiedBy);
        setModifiedDate(modifiedDate);
    }
}
