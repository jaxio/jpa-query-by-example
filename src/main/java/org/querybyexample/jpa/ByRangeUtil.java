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
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Helper to create a predicate out of {@link Range}s.
 */
public class ByRangeUtil {

    public static <E> Predicate byRanges(Root<E> root, CriteriaBuilder builder, SearchParameters sp, Class<E> type) {
        List<Range<?, ?>> ranges = sp.getRanges();
        List<Predicate> predicates = newArrayList();
        for (Range<?, ?> r : ranges) {
            @SuppressWarnings("unchecked")
            Range<E, ?> range = (Range<E, ?>) r;
            if (range.isSet()) {
                Predicate rangePredicate = buildRangePredicate(range, root, builder);

                if (rangePredicate != null) {
                    if (!range.isIncludeNullSet() || range.getIncludeNull() == FALSE) {
                        predicates.add(rangePredicate);
                    } else {
                        predicates.add(builder.or(rangePredicate, builder.isNull(root.get(range.getField()))));
                    }
                } else {
                    // no from/to is set, but include null or not could be:
                    if (TRUE == range.getIncludeNull()) {
                        predicates.add(builder.isNull(root.get(range.getField())));
                    } else if (FALSE == range.getIncludeNull()) {
                        predicates.add(builder.isNotNull(root.get(range.getField())));
                    }
                }
            }
        }

        return JpaUtil.concatPredicate(sp, builder, predicates);
    }

    private static <D extends Comparable<? super D>, E> Predicate buildRangePredicate(Range<E, D> range, Root<E> root, CriteriaBuilder builder) {
        if (range.isBetween()) {
            return builder.between(root.get(range.getField()), range.getFrom(), range.getTo());
        } else if (range.isFromSet()) {
            return builder.greaterThanOrEqualTo(root.get(range.getField()), range.getFrom());
        } else if (range.isToSet()) {
            return builder.lessThanOrEqualTo(root.get(range.getField()), range.getTo());
        }
        return null;
    }
}