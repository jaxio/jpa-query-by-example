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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.reflect.Modifier.isPublic;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.querybyexample.jpa.Identifiable;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;

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
            return false;
        }
    }

    public static Predicate concatPredicate(SearchParameters sp, CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return concatPredicate(sp, builder, newArrayList(predicatesNullAllowed));
    }

    public static Predicate concatPredicate(SearchParameters sp, CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        if (sp.isAndMode()) {
            return andPredicate(builder, predicatesNullAllowed);
        } else {
            return orPredicate(builder, predicatesNullAllowed);
        }
    }

    public static Predicate andPredicate(CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return andPredicate(builder, newArrayList(predicatesNullAllowed));
    }

    public static Predicate orPredicate(CriteriaBuilder builder, Predicate... predicatesNullAllowed) {
        return orPredicate(builder, newArrayList(predicatesNullAllowed));
    }

    public static Predicate andPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = newArrayList(filter(predicatesNullAllowed, notNull()));
        if (predicates == null || predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {
            return builder.and(toArray(predicates, Predicate.class));
        }
    }

    public static Predicate orPredicate(CriteriaBuilder builder, Iterable<Predicate> predicatesNullAllowed) {
        List<Predicate> predicates = newArrayList(filter(predicatesNullAllowed, notNull()));
        if (predicates == null || predicates.isEmpty()) {
            return null;
        } else if (predicates.size() == 1) {
            return predicates.get(0);
        } else {
            return builder.or(toArray(predicates, Predicate.class));
        }
    }

    public static <E> Predicate stringPredicate(Expression<String> path, Object attrValue, SearchMode searchMode, SearchParameters sp, CriteriaBuilder builder) {
        if (sp.isCaseInsensitive()) {
            path = builder.lower(path);
            attrValue = ((String) attrValue).toLowerCase(LocaleContextHolder.getLocale());
        }

        switch (searchMode != null ? searchMode : sp.getSearchMode()) {
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

    public static <E> Predicate stringPredicate(Expression<String> path, Object attrValue, SearchParameters sp, CriteriaBuilder builder) {
        return stringPredicate(path, attrValue, null, sp, builder);
    }

    public static <E> List<Order> buildJpaOrders(Iterable<OrderBy> orders, Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        List<Order> jpaOrders = newArrayList();

        for (OrderBy ob : orders) {
            Path<?> path = getPropertyOrderPath(root, ob.getProperty(), sp);

            if (ob.isOrderDesc()) {
                jpaOrders.add(builder.desc(path));
            } else {
                jpaOrders.add(builder.asc(path));
            }
        }
        return jpaOrders;
    }

    /**
     * Convert the passed propertyPath into a JPA path. <br>
     * Note: JPA will do joins if the property is in an associated entity.
     */
    @SuppressWarnings("unchecked")
    private static <E> Path<?> getPropertyOrderPath(Root<E> root, String propertyPath, SearchParameters sp) {
        String[] pathItems = StringUtils.split(propertyPath, ".");

        Path<?> path = null;

        String pathItem = pathItems[0];
        if (sp.getDistinct()) {
            // handle case when order on already fetched attribute
            for (Fetch<E, ?> fetch : root.getFetches()) {
                if (pathItem.equals(fetch.getAttribute().getName()) && fetch instanceof Join<?, ?>) {
                    path = (Join<E, ?>) fetch;
                }
            }
            for (Join<E, ?> join : root.getJoins()) {
                if (pathItem.equals(join.getAttribute().getName())) {
                    path = join;
                }
            }
        }

        // if no fetch matches the required path item, load it from root
        if (path == null) {
            path = root.get(pathItem);
        }

        for (int i = 1; i < pathItems.length; i++) {
            path = path.get(pathItems[i]);
        }
        return path;
    }

    public static <T extends Identifiable<?>> String compositePkPropertyName(T entity) {
        for (Method m : entity.getClass().getMethods()) {
            if (m.getAnnotation(EmbeddedId.class) != null) {
                return BeanUtils.findPropertyForMethod(m).getName();
            }
        }
        for (Field f : entity.getClass().getFields()) {
            if (f.getAnnotation(EmbeddedId.class) != null) {
                return f.getName();
            }
        }
        return null;
    }

    public static <T> boolean isPk(ManagedType<T> mt, SingularAttribute<? super T, ?> attr) {
        try {
            Method m = BeanUtils.findMethod(mt.getJavaType(), "get" + WordUtils.capitalize(attr.getName()));
            if (m != null && m.getAnnotation(Id.class) != null) {
                return true;
            }

            Field field = mt.getJavaType().getField(attr.getName());
            return field.getAnnotation(Id.class) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> Object getValue(T example, Attribute<? super T, ?> attr) {
        try {
            if (attr.getJavaMember() instanceof Method) {
                return ReflectionUtils.invokeMethod((Method) attr.getJavaMember(), example);
            } else {
                return ReflectionUtils.getField((Field) attr.getJavaMember(), example);
            }
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    public static <T, A> SingularAttribute<? super T, A> attribute(ManagedType<? super T> mt, Attribute<? super T, A> attr) {
        return mt.getSingularAttribute(attr.getName(), attr.getJavaType());
    }

    public static <T> SingularAttribute<? super T, String> stringAttribute(ManagedType<? super T> mt, Attribute<? super T, ?> attr) {
        return mt.getSingularAttribute(attr.getName(), String.class);
    }

    public static <T extends Identifiable<?>> boolean hasSimplePk(T entity) {
        for (Method m : entity.getClass().getMethods()) {
            if (m.getAnnotation(Id.class) != null) {
                return true;
            }
        }
        for (Field f : entity.getClass().getFields()) {
            if (f.getAnnotation(Id.class) != null) {
                return true;
            }
        }
        return false;
    }

    public static String[] toNames(SingularAttribute<?, ?>... attributes) {
        List<String> ret = newArrayList();
        for (SingularAttribute<?, ?> attribute : attributes) {
            ret.add(attribute.getName());
        }
        return ret.toArray(new String[ret.size()]);
    }

    public static List<String> toNamesList(List<SingularAttribute<?, ?>> attributes) {
        List<String> ret = newArrayList();
        for (SingularAttribute<?, ?> attribute : attributes) {
            ret.add(attribute.getName());
        }
        return ret;
    }

    public static String getEntityName(Identifiable<?> entity) {
        Entity entityAnnotation = findAnnotation(entity.getClass(), Entity.class);
        if (isBlank(entityAnnotation.name())) {
            return getClassWithoutInitializingProxy(entity).getSimpleName();
        }
        return entityAnnotation.name();
    }

    public static String methodToProperty(Method m) {
        return BeanUtils.findPropertyForMethod(m).getName();
    }

    public static Object getValueFromField(Field field, Object object) {
        boolean accessible = field.isAccessible();
        try {
            return ReflectionUtils.getField(field, object);
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void applyPagination(Query query, SearchParameters sp) {
        if (sp.getFirst() > 0) {
            query.setFirstResult(sp.getFirst());
        }
        if (sp.getPageSize() > 0) {
            query.setMaxResults(sp.getPageSize());
        } else if (sp.getMaxResults() > 0) {
            query.setMaxResults(sp.getMaxResults());
        }
    }
}