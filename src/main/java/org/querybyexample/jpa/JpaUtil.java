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

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

public class JpaUtil {

    public static boolean isEntityIdManuallyAssigned(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (isPrimaryKey(method)) {
                return isManuallyAssigned(method);
            }
        }
        return false; // no pk found, should not happen
    }

    private static boolean isPrimaryKey(Method method) {
        return isPublic(method.getModifiers()) && (method.getAnnotation(Id.class) != null || method.getAnnotation(EmbeddedId.class) != null);
    }

    private static boolean isManuallyAssigned(Method method) {
        if (method.getAnnotation(Id.class) != null) {
            return method.getAnnotation(GeneratedValue.class) == null;
        } else if (method.getAnnotation(EmbeddedId.class) != null) {
            return true;
        } else {
            return true;
        }
    }

    public static Predicate andPredicate(CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return andPredicate(builder, Arrays.asList(predicatesNullAllowed));
    }

    public static Predicate andPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = withoutNullEntries(predicatesNullAllowed);
        if (predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {    
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    public static Predicate orPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = withoutNullEntries(predicatesNullAllowed);
        if (predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {
            return builder.or(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    public static <E> Predicate stringPredicate(Expression<String> path, Object attrValue, SearchParameters sp, CriteriaBuilder builder) {
        if (sp.isCaseInsensitive()) {
            path = builder.lower(path);
            attrValue = ((String) attrValue).toLowerCase();
        }

        switch (sp.getSearchMode()) {
        case EQUALS:
            return builder.equal(path, attrValue);
        case ENDING_LIKE:
            return builder.like(path, "%" + attrValue);
        case STARTING_LIKE:
            return builder.like(path, attrValue + "%");
        case ANYWHERE:
            return builder.like(path, "%" + attrValue + "%");
        case LIKE:
            return builder.like(path, (String) attrValue); // assume user provide the wild cards
        default:
            throw new IllegalStateException("expecting a search mode!");
        }
    }

    public static <E> List<Order> buildJpaOrders(Iterable<OrderBy> orders, Root<E> root, CriteriaBuilder builder) {
        List<Order> jpaOrders = new ArrayList<Order>();

        for (OrderBy ob : orders) {
            Path<?> path = getPropertyPath(root, ob.getProperty());

            if (ob.isOrderDesc()) {
                jpaOrders.add(builder.desc(path));
            } else {
                jpaOrders.add(builder.asc(path));
            }
        }
        return jpaOrders;
    }

    /**
     * Convert the passed propertyPath into a JPA path.
     * Note: JPA will do joins if the property is in an associated entity.
     */
    private static <E> Path<?> getPropertyPath(Path<E> root, String propertyPath) {
        String[] pathItems = StringUtils.split(propertyPath, ".");

        Path<?> path = root.get(pathItems[0]);
        for (int i = 1; i < pathItems.length; i++) {
            path = path.get(pathItems[i]);
        }
        return path;
    }
    
    private static <T> List<T> withoutNullEntries(Iterable<T> input) {
        List<T> output = new ArrayList<T>();
        for (T element : input) {
            if (element != null) {
                output.add(element);
            }
        }
        return output;        
    }
}