/*
 * Copyright 2013 JAXIO http://www.jaxio.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.querybyexample.jpa;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Used to construct OR predicate for a property value. In other words you can search
 * all entities E having a given property set to one of the selected values.
 */
public class PropertySelector<E, F> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SingularAttribute<E, F> field;
    private List<F> selected = newArrayList();
    private SearchMode searchMode; // for string property only.

    /**
     * @param field the property that should match one of the selected value.
     */
    public PropertySelector(SingularAttribute<E, F> field) {
        this.field = checkNotNull(field);
    }

    public SingularAttribute<E, F> getField() {
        return field;
    }

    /**
     * Get the possible candidates for property.
     */
    public List<F> getSelected() {
        return selected;
    }

    /**
     * Set the possible candidates for property.
     */
    public void setSelected(List<F> selected) {
        if (selected == null) {
            clearSelected();
        } else {
            this.selected = selected;
        }
    }

    /**
     * Add a possible candidates for property.
     */
    public void add(F selected) {
        this.selected.add(selected);
    }

    public boolean isNotEmpty() {
        return selected != null && !selected.isEmpty();
    }

    public void clearSelected() {
        if (selected != null) {
            selected.clear();
        }
    }

    public boolean isBoolean() {
        return field.getJavaType().isAssignableFrom(Boolean.class);
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    /**
     * In case, the field's type is a String, you can set a searchMode to use.
     * It is null by default.
     */
    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = checkNotNull(searchMode);
    }

    /**
     * {@link PropertySelector} builder
     */
    public static <E, F> PropertySelector<E, F> newPropertySelector(SingularAttribute<E, F> field) {
        return new PropertySelector<E, F>(field);
    }

    /**
     * {@link PropertySelector} builder
     */
    public static <E, F> PropertySelector<E, F> newPropertySelector(SingularAttribute<E, F> field, SearchMode searchMode) {
        PropertySelector<E, F> ps = new PropertySelector<E, F>(field);
        ps.setSearchMode(searchMode);
        return ps;
    }
}