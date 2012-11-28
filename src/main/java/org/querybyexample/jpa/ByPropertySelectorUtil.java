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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Helper to create a predicate out of {@link PropertySelector}s.
 */
public class ByPropertySelectorUtil {
    @SuppressWarnings("unchecked")
    public static <E> Predicate byPropertySelectors(Root<E> root, CriteriaBuilder builder, final List<PropertySelector<?, ?>> selectors) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (PropertySelector<?, ?> selector : selectors) {
            if (selector.isBoolean()) {
                byBooleanSelector(root, builder, predicates, (PropertySelector<E, Boolean>) selector);
            } else {
                byObjectSelector(root, builder, predicates, (PropertySelector<E, ?>) selector);
            }
        }
        return JpaUtil.andPredicate(builder, predicates);
    }

    private static <E> void byBooleanSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, PropertySelector<E, Boolean> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = new ArrayList<Predicate>();

            for (Boolean selection : selector.getSelected()) {
                Path<Boolean> path = root.get(selector.getField());
                if (selection == null) {
                    selectorPredicates.add(builder.isNull(path));
                } else {
                    selectorPredicates.add(selection ? builder.isTrue(path) : builder.isFalse(path));
                }
            }
            predicates.add(JpaUtil.orPredicate(builder, selectorPredicates));
        }
    }

    private static <E> void byObjectSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, PropertySelector<E, ?> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = new ArrayList<Predicate>();

            for (Object selection : selector.getSelected()) {
                Path<?> path = root.get(selector.getField());
                selectorPredicates.add(selection == null ? builder.isNull(path) : builder.equal(path, selection));
            }
            predicates.add(JpaUtil.orPredicate(builder, selectorPredicates));
        }
    }
}
