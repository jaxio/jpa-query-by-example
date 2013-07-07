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

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.querybyexample.jpa.JpaUtil;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.SearchParameters;

/**
 * Helper to create a predicate out of {@link PropertySelector}s.
 */
public class ByPropertySelectorUtil {
    @SuppressWarnings("unchecked")
    public static <E> Predicate byPropertySelectors(Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        List<PropertySelector<?, ?>> selectors = sp.getProperties();
        List<Predicate> predicates = newArrayList();

        for (PropertySelector<?, ?> selector : selectors) {
            if (selector.isBoolean()) {
                byBooleanSelector(root, builder, predicates, (PropertySelector<? super E, Boolean>) selector);
            } else {
                byObjectSelector(root, builder, predicates, sp, (PropertySelector<? super E, ?>) selector);
            }
        }
        return JpaUtil.concatPredicate(sp, builder, predicates);
    }

    private static <E> void byBooleanSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, PropertySelector<? super E, Boolean> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = newArrayList();

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

    private static <E> void byObjectSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
            PropertySelector<? super E, ?> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = newArrayList();

            for (Object selection : selector.getSelected()) {
                if (selection instanceof String) {
                    @SuppressWarnings("unchecked")
                    Path<String> path = (Path<String>) root.get(selector.getField());
                    selectorPredicates.add(JpaUtil.stringPredicate(path, selection, selector.getSearchMode(), sp, builder));
                } else {
                    Path<?> path = root.get(selector.getField());
                    selectorPredicates.add(selection == null ? builder.isNull(path) : builder.equal(path, selection));
                }
            }
            predicates.add(JpaUtil.orPredicate(builder, selectorPredicates));
        }
    }
}