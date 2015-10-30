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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Helper to create a predicate out of {@link PropertySelector}s.
 */
@Named
@Singleton
public class ByPropertySelectorUtil {

    @Inject
    private JpaUtil jpaUtil;

    @SuppressWarnings("unchecked")
    public <E> Predicate byPropertySelectors(Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        List<Predicate> predicates = newArrayList();

        for (PropertySelector<?, ?> selector : sp.getProperties()) {
            if (selector.isBoolean()) {
                byBooleanSelector(root, builder, predicates, sp, (PropertySelector<? super E, Boolean>) selector);
            } else if (selector.isString()) {
                byStringSelector(root, builder, predicates, sp, (PropertySelector<? super E, String>) selector);
            } else {
                byObjectSelector(root, builder, predicates, sp, (PropertySelector<? super E, ?>) selector);
            }
        }
        return jpaUtil.concatPredicate(sp, builder, predicates);
    }

    private <E> void byBooleanSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
                                       PropertySelector<? super E, Boolean> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = newArrayList();

            for (Boolean selection : selector.getSelected()) {
                Path<Boolean> path = jpaUtil.getPath(root, selector.getAttributes());
                if (selection == null) {
                    selectorPredicates.add(builder.isNull(path));
                } else {
                    selectorPredicates.add(selection ? builder.isTrue(path) : builder.isFalse(path));
                }
            }
            if (selector.isOrMode()) {
                predicates.add(jpaUtil.orPredicate(builder, selectorPredicates));
            } else {
                predicates.add(jpaUtil.andPredicate(builder, selectorPredicates));
            }
        }
    }

    private <E> void byStringSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
                                      PropertySelector<? super E, String> selector) {
        if (selector.isNotEmpty()) {
            List<Predicate> selectorPredicates = newArrayList();

            for (String selection : selector.getSelected()) {
                Path<String> path = jpaUtil.getPath(root, selector.getAttributes());
                selectorPredicates.add(jpaUtil.stringPredicate(path, selection, selector.getSearchMode(), sp, builder));
            }
            if (selector.isOrMode()) {
                predicates.add(jpaUtil.orPredicate(builder, selectorPredicates));
            } else {
                predicates.add(jpaUtil.andPredicate(builder, selectorPredicates));
            }
        }
    }

    private <E> void byObjectSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
                                      PropertySelector<? super E, ?> selector) {
        if (selector.isNotEmpty()) {
            if (selector.isOrMode()) {
                byObjectOrModeSelector(root, builder, predicates, sp, selector);
            } else {
                byObjectAndModeSelector(root, builder, predicates, sp, selector);
            }
        } else if (selector.isNotIncludingNullSet()) {
            predicates.add(builder.isNotNull(jpaUtil.getPath(root, selector.getAttributes())));
        }
    }

    private <E> void byObjectOrModeSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
                                            PropertySelector<? super E, ?> selector) {
        List<Predicate> selectorPredicates = newArrayList();
        Path<?> path = jpaUtil.getPath(root, selector.getAttributes());
        List<?> selected = selector.getSelected();
        if (selector.getSelected().contains(null)) {
            selected = newArrayList(selector.getSelected());
            selected.remove(null);
            selectorPredicates.add(builder.isNull(path));
        }
        if (isNotEmpty(selected)) {
            if (selected.get(0) instanceof Identifiable) {
                List<Serializable> ids = newArrayList();
                for (Object selection : selected) {
                    ids.add(((Identifiable<?>) selection).getId());
                }
                selectorPredicates.add(path.get("id").in(ids));
            } else {
                selectorPredicates.add(path.in(selected));
            }
        }
        predicates.add(jpaUtil.orPredicate(builder, selectorPredicates));
    }

    private <E> void byObjectAndModeSelector(Root<E> root, CriteriaBuilder builder, List<Predicate> predicates, SearchParameters sp,
                                             PropertySelector<? super E, ?> selector) {
        List<Predicate> selectorPredicates = newArrayList();
        List<?> selected = selector.getSelected();
        if (selector.getSelected().contains(null)) {
            selected = newArrayList(selector.getSelected());
            selected.remove(null);
            selectorPredicates.add(builder.isNull(jpaUtil.getPath(root, selector.getAttributes())));
        }
        for (Object selection : selector.getSelected()) {
            Path<?> path = jpaUtil.getPath(root, selector.getAttributes());
            if (selection instanceof Identifiable) {
                selectorPredicates.add(builder.equal(path.get("id"), ((Identifiable<?>) selection).getId()));
            } else {
                selectorPredicates.add(builder.equal(path, selection));
            }
        }
        predicates.add(jpaUtil.andPredicate(builder, selectorPredicates));
    }

}