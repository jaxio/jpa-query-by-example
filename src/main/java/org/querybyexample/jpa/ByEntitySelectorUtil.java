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
import static java.lang.Boolean.TRUE;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.querybyexample.jpa.Identifiable;

/**
 * Helper to create a predicate out of {@link EntitySelector}s.
 */
public class ByEntitySelectorUtil {

    public static <E> Predicate byEntitySelectors(Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        List<EntitySelector<?, ?, ?>> selectors = sp.getEntities();
        List<Predicate> predicates = newArrayList();

        for (EntitySelector<?, ?, ?> s : selectors) {
            @SuppressWarnings("unchecked")
            EntitySelector<? super E, ? extends Identifiable<?>, ?> selector = (EntitySelector<? super E, ? extends Identifiable<?>, ?>) s;

            if (selector.isNotEmpty()) {
                List<Predicate> selectorPredicates = newArrayList();

                for (Identifiable<?> selection : selector.getSelected()) {
                    selectorPredicates.add(builder.equal(getExpression(root, selector), selection.getId()));
                }

                if (TRUE == selector.getIncludeNull()) {
                    selectorPredicates.add(builder.or(builder.isNull(getExpression(root, selector))));
                }

                predicates.add(JpaUtil.orPredicate(builder, selectorPredicates));
            } else if (selector.isIncludeNullSet()) {
                if (selector.getIncludeNull()) {
                    predicates.add(builder.isNull(getExpression(root, selector)));
                } else {
                    predicates.add(builder.isNotNull(getExpression(root, selector)));
                }
            }
        }

        return JpaUtil.concatPredicate(sp, builder, predicates);
    }

    private static <E> Expression<?> getExpression(Root<E> root, EntitySelector<? super E, ? extends Identifiable<?>, ?> selector) {
        if (selector.getField() != null) {
            return root.get(selector.getField()).get("id");
        } else {
            return root.get(selector.getCpkField()).get(selector.getCpkInnerField().getName());
        }
    }
}