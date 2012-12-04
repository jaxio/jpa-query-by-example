/*
 *  Copyright 2012 JAXIO http://www.jaxio.com
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
 * Used to construct OR predicate for a single foreign key value. In other words you can search
 * all entities E having their x-to-one association value set to one of the selected values.
 * To avoid a join we rely on the foreign key field, not the association itself.
 */
public class EntitySelector<E, T extends Identifiable<TPK>, TPK extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SingularAttribute<E, TPK> field;
    private final SingularAttribute<E, ?> cpkField;
    private final SingularAttribute<?, TPK> cpkInnerField;
    private List<T> selected = new ArrayList<T>();

    /**
     * @param field the property holding an foreign key.
     */
    public EntitySelector(SingularAttribute<E, TPK> field, T... values) {
        this.field = field;
        this.cpkField = null;
        this.cpkInnerField = null;
        for(T value : values) {
        	selected.add(value);
        }
    }

    public EntitySelector(SingularAttribute<E, ?> cpkField, SingularAttribute<?, TPK> cpkInnerField) {
        this.cpkField = cpkField;
        this.cpkInnerField = cpkInnerField;
        this.field = null; // not used        
    }

    public SingularAttribute<E, TPK> getField() {
        return field;
    }

    public SingularAttribute<E, ?> getCpkField() {
        return cpkField;
    }

    public SingularAttribute<?, TPK> getCpkInnerField() {
        return cpkInnerField;
    }

    /**
     * Get the possible candidates for the x-to-one association.
     */
    public List<T> getSelected() {
        return selected;
    }

    /**
     * Set the possible candidates for the x-to-one association.
     */
    public void setSelected(List<T> selected) {
        this.selected = selected;
    }

    public void selected(T... selected) {
    	List<T> t = new ArrayList<T>();
    	for(T s : selected){
    		t.add(s);
    	}
        this.selected = t;
    }

    public boolean isNotEmpty() {
        return selected != null && !selected.isEmpty();
    }

    public void clearSelected() {
        if (selected != null) {
            selected.clear();
        }
    }

    /**
     * Import statically this helper for smooth instanciation.
     */
    static public <E2, T2 extends Identifiable<TPK2>, TPK2 extends Serializable> EntitySelector<E2, T2, TPK2> newEntitySelector(
            SingularAttribute<E2, TPK2> field, T2... values) {
        return new EntitySelector<E2, T2, TPK2>(field, values);
    }

    /**
     * Import statically this helper for smooth instanciation.
     * It is used in the case where the PK is composite AND the pk member(s) are/is also a foreign key. 
     */
    static public <E2, T2 extends Identifiable<TPK2>, TPK2 extends Serializable, CPK2> EntitySelector<E2, T2, TPK2> newEntitySelectorInCpk(
            SingularAttribute<E2, CPK2> cpkField, SingularAttribute<CPK2, TPK2> cpkInnerField) {
        return new EntitySelector<E2, T2, TPK2>(cpkField, cpkInnerField);
    }
}