/*
; *  Copyright 2012 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.querybyexample.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Used to construct OR predicate for a property value. In other words you can search all entities E having a given property set to one of the selected values.
 */
public class PropertySelector<E, F> implements Serializable {
	private static final long serialVersionUID = 1L;

	private final SingularAttribute<E, F> field;
	private List<F> selected = new ArrayList<F>();

	/**
	 * @param field the property that should match one of the selected value.
	 */
	public PropertySelector(SingularAttribute<E, F> field, F... values) {
		this.field = field;
		for (F value : values) {
			selected.add(value);
		}
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
		this.selected = selected;
	}

	public PropertySelector<E, F> value(F v) {
		selected.add(v);
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

	public boolean isBoolean() {
		return field != null && field.getJavaType().isAssignableFrom(Boolean.class);
	}

	/**
	 * {@link PropertySelector} builder
	 */
	static public <E, F> PropertySelector<E, F> property(SingularAttribute<E, F> field, F... values) {
		return new PropertySelector<E, F>(field, values);
	}
}