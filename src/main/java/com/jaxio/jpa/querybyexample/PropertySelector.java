/*
 * Copyright 2015 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaxio.jpa.querybyexample;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.metamodel.Attribute;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Used to construct OR predicate for a property value. In other words you can search
 * all entities E having a given property set to one of the selected values.
 */
public class PropertySelector<E, F> implements Serializable {
    /*
     * PropertySelector builder
     */
    public static <E, F> PropertySelector<E, F> newPropertySelector(Attribute<?, ?>... fields) {
        return new PropertySelector<E, F>(checkNotNull(fields));
    }

    /*
     * PropertySelector builder
     */
    public static <E, F> PropertySelector<E, F> newPropertySelector(String path, Class<E> from) {
        return new PropertySelector<E, F>(path, from);
    }

    /*
     * PropertySelector builder
     */
    public static <E, F> PropertySelector<E, F> newPropertySelector(boolean orMode, Attribute<?, ?>... fields) {
        PropertySelector<E, F> ps = new PropertySelector<E, F>(checkNotNull(fields));
        return ps.orMode(orMode);
    }

    private static final long serialVersionUID = 1L;

    private final PathHolder pathHolder;
    private List<F> selected = newArrayList();
    private SearchMode searchMode; // for string property only.
    private Boolean notIncludingNull;
    private boolean orMode = true;

    public PropertySelector(Attribute<?, ?>... attributes) {
        this.pathHolder = new PathHolder(checkNotNull(attributes));
    }

    public PropertySelector(String path, Class<E> from) {
        this.pathHolder = new PathHolder(path, from);
    }

    public List<Attribute<?, ?>> getAttributes() {
        return pathHolder.getAttributes();
    }

    public boolean isNotIncludingNullSet() {
        return notIncludingNull != null;
    }

    public Boolean isNotIncludingNull() {
        return notIncludingNull;
    }

    public PropertySelector<E, F> withoutNull() {
        this.notIncludingNull = true;
        return this;
    }

    /*
     * Get the possible candidates for property.
     */
    public List<F> getSelected() {
        return selected;
    }

    public PropertySelector<E, F> add(F object) {
        this.selected.add(object);
        return this;
    }

    /*
     * Set the possible candidates for property.
     */
    public void setSelected(List<F> selected) {
        this.selected = selected;
    }

    public PropertySelector<E, F> selected(F... selected) {
        setSelected(newArrayList(selected));
        return this;
    }

    public boolean isNotEmpty() {
        return selected != null && !selected.isEmpty();
    }

    public void clearSelected() {
        if (selected != null) {
            selected.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public void setValue(F value) {
        this.selected = newArrayList(value);
    }

    public F getValue() {
        return isNotEmpty() ? selected.get(0) : null;
    }

    public boolean isBoolean() {
        return isType(Boolean.class);
    }

    public boolean isLabelizedEnum() {
        return isType(LabelizedEnum.class);
    }

    public boolean isString() {
        return isType(String.class);
    }

    public boolean isNumber() {
        return isType(Number.class);
    }

    public boolean isType(Class<?> type) {
        return type.isAssignableFrom(getAttributes().get(getAttributes().size() - 1).getJavaType());
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    /*
     * In case, the field's type is a String, you can set a searchMode to use. It is null by default.
     */
    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public PropertySelector<E, F> searchMode(SearchMode searchMode) {
        setSearchMode(searchMode);
        return this;
    }

    public boolean isOrMode() {
        return orMode;
    }

    public void setOrMode(boolean orMode) {
        this.orMode = orMode;
    }

    public PropertySelector<E, F> orMode(boolean orMode) {
        setOrMode(orMode);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}