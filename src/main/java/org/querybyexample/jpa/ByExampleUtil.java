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

import static javax.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Helper to create predicate by example. It processes associated entities (1 level deep).
 */
@Named
@Singleton
public class ByExampleUtil {
    @PersistenceContext
    private EntityManager em;

    public <T extends Identifiable<?>> Predicate byExampleOnEntity(Root<T> rootPath, final T entityValue, SearchParameters sp, CriteriaBuilder builder) {
        if (entityValue == null) {
            return null;
        }

        Class<T> type = rootPath.getModel().getBindableJavaType();
        ManagedType<T> mt = em.getMetamodel().entity(type);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.addAll(byExample(mt, rootPath, entityValue, sp, builder));
        try {
            if (mt.getAttribute("id").getPersistentAttributeType().compareTo(EMBEDDED) == 0) {
                predicates.addAll(byExampleOnCompositePk(rootPath, entityValue, sp, builder));
            }
        } catch (IllegalArgumentException iae) {            
        }
        predicates.addAll(byExampleOnXToOne(mt, rootPath, entityValue, sp, builder)); // 1 level deep only
        predicates.addAll(byExampleOnManyToMany(mt, rootPath, entityValue, sp, builder));
        return JpaUtil.andPredicate(builder, predicates);
    }

    protected <T extends Identifiable<?>> List<Predicate> byExampleOnCompositePk(Root<T> root, T entity, SearchParameters sp, CriteriaBuilder builder) {
        String compositePropertyName = "id";
        List<Predicate> result = new ArrayList<Predicate>();
        if (compositePropertyName != null) {
            result.add(byExampleOnEmbeddable(root.get(compositePropertyName), entity.getId(), sp, builder));
        }
        return result;
    }

    public <E> Predicate byExampleOnEmbeddable(Path<E> embeddablePath, final E embeddableValue, SearchParameters sp, CriteriaBuilder builder) {
        if (embeddableValue == null) {
            return null;
        }

        Class<E> type = embeddablePath.getModel().getBindableJavaType();
        ManagedType<E> mt = em.getMetamodel().embeddable(type); // note: calling .managedType() does not work

        return JpaUtil.andPredicate(builder, byExample(mt, embeddablePath, embeddableValue, sp, builder));
    }

    /**
     * Add a predicate for each simple property whose value is not null.
     */
    public <T> List<Predicate> byExample(ManagedType<T> mt, Path<T> mtPath, final T mtValue, SearchParameters sp, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (SingularAttribute<? super T, ?> attr : mt.getSingularAttributes()) {
            if (attr.getPersistentAttributeType() == MANY_TO_ONE //
                    || attr.getPersistentAttributeType() == ONE_TO_ONE //
                    || attr.getPersistentAttributeType() == EMBEDDED) {
                continue;
            }

            Object attrValue = getValue(mtValue, attr);
            if (attrValue != null) {
                if (attr.getJavaType() == String.class) {
                    if (isNotEmpty((String) attrValue)) {
                        predicates.add(JpaUtil.stringPredicate(mtPath.get(stringAttribute(mt, attr)), attrValue, sp, builder));
                    }
                } else {
                    predicates.add(builder.equal(mtPath.get(attribute(mt, attr)), attrValue));
                }
            }
        }
        return predicates;
    }

    /**
     * Invoke byExample method for each not null x-to-one association when their pk is not set. This allows you to search entities based on an associated
     * entity's properties value.
     */
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<?>, M2O extends Identifiable<?>> List<Predicate> byExampleOnXToOne(ManagedType<T> mt, Root<T> mtPath, final T mtValue,
            SearchParameters sp, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (SingularAttribute<? super T, ?> attr : mt.getSingularAttributes()) {
            if (attr.getPersistentAttributeType() == MANY_TO_ONE || attr.getPersistentAttributeType() == ONE_TO_ONE) { //
                M2O m2oValue = (M2O) getValue(mtValue, mt.getAttribute(attr.getName()));
                if (m2oValue != null && !mtValue.isIdSet()) {
                    Class<M2O> m2oType = (Class<M2O>) attr.getBindableJavaType();
                    ManagedType<M2O> m2oMt = em.getMetamodel().entity(m2oType);
                    Path<M2O> m2oPath = (Path<M2O>) mtPath.get(attr);
                    predicates.addAll(byExample(m2oMt, m2oPath, m2oValue, sp, builder));
                }
            }
        }
        return predicates;
    }

    /**
     * Construct a join predicate on collection (eg many to many, List)
     */
    public <T> List<Predicate> byExampleOnManyToMany(ManagedType<T> mt, Root<T> mtPath, final T mtValue, SearchParameters sp, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (PluralAttribute<T, ?, ?> pa : mt.getDeclaredPluralAttributes()) {
            if (pa.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                List<?> value = (List<?>) getValue(mtValue, mt.getAttribute(pa.getName()));

                if (value != null && !value.isEmpty()) {
                    ListJoin<T, ?> join = mtPath.join(mt.getDeclaredList(pa.getName()));
                    predicates.add(join.in(value));
                }
            }
        }
        return predicates;
    }

    private <T> Object getValue(T example, Attribute<? super T, ?> attr) {
        try {
            return ((Method) attr.getJavaMember()).invoke(example, new Object[0]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, A> SingularAttribute<? super T, A> attribute(ManagedType<? super T> mt, Attribute<? super T, A> attr) {
        return mt.getSingularAttribute(attr.getName(), attr.getJavaType());
    }

    private <T> SingularAttribute<? super T, String> stringAttribute(ManagedType<? super T> mt, Attribute<? super T, ?> attr) {
        return mt.getSingularAttribute(attr.getName(), String.class);
    }
}