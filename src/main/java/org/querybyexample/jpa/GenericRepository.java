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

import static com.google.common.base.Preconditions.*;
import static org.querybyexample.jpa.JpaUtil.*;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA 2 {@link GenericRepository} implementation
 */
public abstract class GenericRepository<E extends Identifiable<PK>, PK extends Serializable> {
    @Inject
    protected ByExampleUtil byExampleUtil;
    @Inject
    protected ByPatternUtil byPatternUtil;
    @Inject
    protected ByRangeUtil byRangeUtil;
    @Inject
    protected ByNamedQueryUtil byNamedQueryUtil;
    @Inject
    protected ByPropertySelectorUtil byPropertySelectorUtil;
    @Inject
    protected OrderByUtil orderByUtil;
    @PersistenceContext
    protected EntityManager entityManager;
    protected Class<E> type;
    protected Logger log;
    protected String cacheRegion;

    /**
     * This constructor needs the real type of the generic type E so it can be
     * given to the {@link EntityManager}.
     */
    public GenericRepository(Class<E> type) {
        this.type = type;
        this.log = LoggerFactory.getLogger(getClass());
        this.cacheRegion = type.getCanonicalName();
    }

    public Class<E> getType() {
        return type;
    }

    /**
     * Create a new instance of the repository type.
     * 
     * @return a new instance with no property set.
     */
    public abstract E getNew();

    /**
     * Creates a new instance and initializes it with some default values.
     * 
     * @return a new instance initialized with default values.
     */
    public E getNewWithDefaults() {
        return getNew();
    }

    /**
     * Gets from the repository the E entity instance.
     * 
     * DAO for the local database will typically use the primary key or unique
     * fields of the given entity, while DAO for external repository may use a
     * unique field present in the entity as they probably have no knowledge of
     * the primary key. Hence, passing the entity as an argument instead of the
     * primary key allows you to switch the DAO more easily.
     * 
     * @param entity
     *            an E instance having a primary key set
     * @return the corresponding E persistent instance or null if none could be
     *         found.
     */
    @Transactional(readOnly = true)
    public E get(E entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Transactional(readOnly = true)
    public E getById(PK pk) {
        if (pk == null) {
            return null;
        }

        E entityFound = entityManager.find(type, pk);
        if (entityFound == null) {
            log.warn("get returned null with id={}", pk);
        }
        return entityFound;
    }

    /**
     * Refresh the given entity with up to date data. Does nothing if the given
     * entity is a new entity (not yet managed).
     * 
     * @param entity
     *            the entity to refresh.
     */
    @Transactional(readOnly = true)
    public void refresh(E entity) {
        if (entityManager.contains(entity)) {
            entityManager.refresh(entity);
        }
    }

    /**
     * Find and load all instances.
     */
    @Transactional(readOnly = true)
    public List<E> find() {
        return find(getNew(), new SearchParameters());
    }

    /**
     * Find and load a list of E instance.
     * 
     * @param entity
     *            a sample entity whose non-null properties may be used as
     *            search hints
     * @return the entities matching the search.
     */
    @Transactional(readOnly = true)
    public List<E> find(E e) {
        return find(e, new SearchParameters());
    }

    /**
     * Find and load a list of E instance.
     * 
     * @param searchParameters
     *            carries additional search information
     * @return the entities matching the search.
     */
    @Transactional(readOnly = true)
    public List<E> find(SearchParameters searchParameters) {
        return find(getNew(), searchParameters);
    }

    /**
     * Find and load a list of E instance.
     * 
     * @param entity
     *            a sample entity whose non-null properties may be used as
     *            search hints
     * @param searchParameters
     *            carries additional search information
     * @return the entities matching the search.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<E> find(E entity, SearchParameters sp) {
        if (sp.hasNamedQuery()) {
            return byNamedQueryUtil.findByNamedQuery(sp);
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = builder.createQuery(type);
        if (sp.getDistinct()) {
            criteriaQuery.distinct(true);
        }
        Root<E> root = criteriaQuery.from(type);

        // predicate
        Predicate predicate = getPredicate(root, builder, entity, sp);
        if (predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        // left join
        for (SingularAttribute<?, ?> arg : sp.getLeftJoins()) {
            root.fetch((SingularAttribute<E, ?>) arg, JoinType.LEFT);
        }

        // order by
        criteriaQuery.orderBy(orderByUtil.buildJpaOrders(sp.getOrders(), root, builder, sp));

        TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
        applyCacheHints(typedQuery, sp);
        applyPagination(typedQuery, sp);
        List<E> entities = typedQuery.getResultList();
        log.debug("Returned {} elements", entities.size());

        return entities;
    }

    /**
     * Count the number of E instances.
     * 
     * @param searchParameters
     *            carries additional search information
     * @return the number of entities matching the search.
     */
    @Transactional(readOnly = true)
    public int findCount(SearchParameters sp) {
        return findCount(getNew(), sp);
    }

    /**
     * Count the number of E instances.
     * 
     * @param entity
     *            a sample entity whose non-null properties may be used as
     *            search hint
     * @return the number of entities matching the search.
     */
    @Transactional(readOnly = true)
    public int findCount(E entity) {
        return findCount(entity, new SearchParameters());
    }

    /**
     * Count the number of E instances.
     * 
     * @param entity
     *            a sample entity whose non-null properties may be used as
     *            search hint
     * @param searchParameters
     *            carries additional search information
     * @return the number of entities matching the search.
     */
    @Transactional(readOnly = true)
    public int findCount(E entity, SearchParameters sp) {
        checkNotNull(entity, "The entity cannot be null");
        checkNotNull(sp, "The searchParameters cannot be null");

        if (sp.hasNamedQuery()) {
            return byNamedQueryUtil.numberByNamedQuery(sp).intValue();
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<E> root = criteriaQuery.from(type);

        if (sp.getDistinct()) {
            criteriaQuery = criteriaQuery.select(builder.countDistinct(root));
        } else {
            criteriaQuery = criteriaQuery.select(builder.count(root));
        }

        // predicate
        Predicate predicate = getPredicate(root, builder, entity, sp);
        if (predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        orderByUtil.forceJoinOrder(root, sp);

        TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);

        applyCacheHints(typedQuery, sp);
        Long count = typedQuery.getSingleResult();

        if (count != null) {
            return count.intValue();
        } else {
            log.warn("findCount returned null");
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public E findUnique(SearchParameters sp) {
        return findUnique(getNew(), sp);
    }

    @Transactional(readOnly = true)
    public E findUnique(E e) {
        return findUnique(e, new SearchParameters());
    }

    @Transactional(readOnly = true)
    public E findUnique(E entity, SearchParameters sp) {
        E result = findUniqueOrNone(entity, sp);
        if (result != null) {
            return result;
        }
        throw new NoResultException("Developper: You expected 1 result but found none !");
    }

    @Transactional(readOnly = true)
    public E findUniqueOrNone(E entity) {
        return findUniqueOrNone(entity, new SearchParameters());
    }

    /**
     * We request at most 2, if there's more than one then we throw a
     * {@link NonUniqueResultException}
     * 
     * @throws NonUniqueResultException
     */
    @Transactional(readOnly = true)
    public E findUniqueOrNone(E entity, SearchParameters sp) {
        // this code is an optimization to prevent using a count
        sp.setFirst(0);
        sp.setMaxResults(2);
        List<E> results = find(entity, sp);

        if ((results == null) || results.isEmpty()) {
            return null;
        } else if (results.size() > 1) {
            throw new NonUniqueResultException("Developper: You expected 1 result but we found more ! sample: " + entity);
        } else {
            return results.iterator().next();
        }
    }

    protected <R> Predicate getPredicate(Root<E> root, CriteriaBuilder builder, E entity, SearchParameters sp) {
        return JpaUtil.andPredicate(builder, //
                bySearchPredicate(root, builder, entity, sp), //
                byMandatoryPredicate(root, builder, entity, sp));
    }

    protected <R> Predicate bySearchPredicate(Root<E> root, CriteriaBuilder builder, E entity, SearchParameters sp) {
        return JpaUtil.concatPredicate(sp, builder, //
                byRanges(root, builder, sp, type), //
                byPropertySelectors(root, builder, sp), //
                byExample(root, builder, sp, entity), //
                byPattern(root, builder, sp, type));
    }

    protected Predicate byExample(Root<E> root, CriteriaBuilder builder, SearchParameters sp, E entity) {
        return byExampleUtil.byExampleOnEntity(root, entity, builder, sp);
    }

    protected Predicate byPropertySelectors(Root<E> root, CriteriaBuilder builder, SearchParameters sp) {
        return byPropertySelectorUtil.byPropertySelectors(root, builder, sp);
    }

    protected Predicate byRanges(Root<E> root, CriteriaBuilder builder, SearchParameters sp, Class<E> type) {
        return byRangeUtil.byRanges(root, builder, sp, type);
    }

    protected Predicate byPattern(Root<E> root, CriteriaBuilder builder, SearchParameters sp, Class<E> type) {
        return byPatternUtil.byPattern(root, builder, sp, type);
    }

    /**
     * You may override this method to add a Predicate to the default find
     * method.
     */
    protected <R> Predicate byMandatoryPredicate(Root<E> root, CriteriaBuilder builder, E entity, SearchParameters sp) {
        return null;
    }

    /**
     * Save or update the given entity E to the repository. Assume that the
     * entity is already present in the persistence context. No merge is done.
     * 
     * @param entity
     *            the entity to be saved or updated.
     */
    @Transactional
    public void save(E entity) {
        checkNotNull(entity, "The entity to save cannot be null");

        // creation with auto generated id
        if (!entity.isIdSet()) {
            entityManager.persist(entity);
            return;
        }

        // creation with manually assigned key
        if (JpaUtil.isEntityIdManuallyAssigned(type) && !entityManager.contains(entity)) {
            entityManager.persist(entity);
            return;
        }
        // other cases are update
        // the simple fact to invoke this method, from a service method
        // annotated with @Transactional,
        // does the job (assuming the give entity is present in the persistence
        // context)
    }

    /**
     * Persist the given entity.
     */
    @Transactional
    public void persist(E entity) {
        entityManager.persist(entity);
    }

    /**
     * Merge the state of the given entity into the current persistence context.
     */
    @Transactional
    public E merge(E entity) {
        return entityManager.merge(entity);
    }

    /**
     * Delete the given entity E from the repository.
     * 
     * @param entity
     *            the entity to be deleted.
     */
    @Transactional
    public void delete(E entity) {
        if (entityManager.contains(entity)) {
            entityManager.remove(entity);
        } else {
            // could be a delete on a transient instance
            E entityRef = entityManager.getReference(type, entity.getId());

            if (entityRef != null) {
                entityManager.remove(entityRef);
            } else {
                log.warn("Attempt to delete an instance that is not present in the database: {}", entity);
            }
        }
    }

    // -----------------
    // Util
    // -----------------

    /**
     * Return the optimistic version value, if any.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Comparable<Object> getVersion(E entity) {
        EntityType<E> entityType = entityManager.getMetamodel().entity(type);
        if (!entityType.hasVersionAttribute()) {
            return null;
        }
        return (Comparable<Object>) JpaUtil.getValue(entity, getVersionAttribute(entityType));
    }

    /**
     * _HACK_ too bad that JPA does not provide this entityType.getVersion();
     * 
     * @see http
     *      ://stackoverflow.com/questions/13265094/generic-way-to-get-jpa-entity
     *      -version
     */
    private SingularAttribute<? super E, ?> getVersionAttribute(EntityType<E> entityType) {
        for (SingularAttribute<? super E, ?> sa : entityType.getSingularAttributes()) {
            if (sa.isVersion()) {
                return sa;
            }
        }
        return null;
    }

    // -----------------
    // Commons
    // -----------------

    /**
     * Set hints for 2d level cache.
     */
    protected void applyCacheHints(TypedQuery<?> typedQuery, SearchParameters sp) {
        if (sp.isCacheable()) {
            typedQuery.setHint("org.hibernate.cacheable", true);

            if (sp.hasCacheRegion()) {
                typedQuery.setHint("org.hibernate.cacheRegion", sp.getCacheRegion());
            } else {
                typedQuery.setHint("org.hibernate.cacheRegion", cacheRegion);
            }
        }
    }
}