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

import static org.querybyexample.jpa.JpaUtil.buildJpaOrders;
import static org.querybyexample.jpa.ByEntitySelectorUtil.byEntitySelectors;
import static org.querybyexample.jpa.ByPropertySelectorUtil.byPropertySelectors;
import static org.querybyexample.jpa.ByRangeUtil.byRanges;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * JPA 2 Generic DAO with find by example/range/pattern and CRUD support.
 */
public abstract class QueryByExample<E extends Identifiable<PK>, PK extends Serializable> {

	@Inject
	private ByExampleUtil byExampleUtil;
	@Inject
	private ByPatternUtil byPatternUtil;
	@Inject
	private NamedQueryUtil namedQueryUtil;
	@PersistenceContext
	private EntityManager entityManager;

	private Class<E> type;
	private Logger log;
	private String cacheRegion;

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected ByExampleUtil getByExampleUtil() {
		return byExampleUtil;
	}

	protected ByPatternUtil getByPatternUtil() {
		return byPatternUtil;
	}

	protected NamedQueryUtil getNamedQueryUtil() {
		return namedQueryUtil;
	}

	/**
	 * This constructor needs the real type of the generic type E so it can be passed to the {@link EntityManager}.
	 */
	public QueryByExample(Class<E> type) {
		this.type = type;
		this.log = Logger.getLogger(getClass());
		this.cacheRegion = type.getCanonicalName();
	}

	/**
	 * Gets from the repository the E entity instance.
	 * 
	 * DAO for the local database will typically use the primary key or unique fields of the passed entity, while DAO for external repository may use a unique
	 * field present in the entity as they probably have no knowledge of the primary key. Hence, passing the entity as an argument instead of the primary key
	 * allows you to switch the DAO more easily.
	 * 
	 * @param entity an E instance having a primary key set
	 * @return the corresponding E persistent instance or null if none could be found.
	 */
	public E get(E entity) {
		if (entity == null) {
			return null;
		}

		Serializable id = entity.getId();
		if (id == null) {
			return null;
		}

		E entityFound = getEntityManager().find(type, id);

		if (entityFound == null) {
			log.warn("get returned null with pk=" + id);
		}

		return entityFound;
	}

	/**
	 * Find and load a list of E instance.
	 * 
	 * @param entity a sample entity whose non-null properties may be used as search hints
	 * @return the entities matching the search.
	 */
	public List<E> find(E entity) {
		return find(entity, new SearchParameters());
	}

	public List<E> find(SearchParameters sp) {
		return find(newInstance(), sp);
	}

	/**
	 * as per JPA contract you need to have no-arg constructors, therefore we can instanciate it
	 */
	private E newInstance() {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Find and load a list of E instance.
	 * 
	 * @param entity a sample entity whose non-null properties may be used as search hints
	 * @param searchParameters carries additional search information
	 * @return the entities matching the search.
	 */
	@SuppressWarnings("unchecked")
	public List<E> find(E entity, SearchParameters sp) {
		if (sp.hasNamedQuery()) {
			return getNamedQueryUtil().findByNamedQuery(sp);
		}
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = builder.createQuery(type);
		
		if (sp.isDistinct()) {
			criteriaQuery.distinct(true);
		}
		Root<E> root = criteriaQuery.from(type);

		// predicate
		Predicate predicate = getPredicate(root, criteriaQuery, builder, entity, sp);
		if (predicate != null) {
			criteriaQuery = criteriaQuery.where(predicate);
		}

		// join fetch
		for(Map.Entry<JoinType, List<SingularAttribute<?, ?>>> joins : sp.getJoinAttributes().entrySet()) {
			for (SingularAttribute<?, ?> join : joins.getValue()) {
				root.fetch((SingularAttribute<E, ?>) join, joins.getKey());
			}
		}

		// order by
		criteriaQuery.orderBy(buildJpaOrders(sp.getOrders(), root, builder));

		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		// cache
		setCacheHints(typedQuery, sp);

		// pagination
		if (sp.getFirstResult() >= 0) {
			typedQuery.setFirstResult(sp.getFirstResult());
		}
		if (sp.getMaxResults() > 0) {
			typedQuery.setMaxResults(sp.getMaxResults());
		}

		// execution
		List<E> entities = typedQuery.getResultList();
		if (log.isDebugEnabled()) {
			log.debug("Returned " + entities.size() + " elements");
		}

		return entities;
	}

	/**
	 * Count the number of E instances.
	 * 
	 * @param entity a sample entity whose non-null properties may be used as search hint
	 * @param searchParameters carries additional search information
	 * @return the number of entities matching the search.
	 */
	public int findCount(E entity, SearchParameters sp) {
		Validate.notNull(entity, "The entity cannot be null");

		if (sp.hasNamedQuery()) {
			return getNamedQueryUtil().numberByNamedQuery(sp).intValue();
		}
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<E> root = criteriaQuery.from(type);

		// count
		criteriaQuery = criteriaQuery.select(builder.count(root));

		// predicate
		Predicate predicate = getPredicate(root, criteriaQuery, builder, entity, sp);
		if (predicate != null) {
			criteriaQuery = criteriaQuery.where(predicate);
		}

		TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);

		// cache
		setCacheHints(typedQuery, sp);

		// execution
		Long count = typedQuery.getSingleResult();

		if (count != null) {
			return count.intValue();
		} else {
			log.warn("findCount returned null!");
			return 0;
		}
	}

	public E findUnique(E entity, SearchParameters sp) {
		E result = findUniqueOrNone(entity, sp);

		if (result == null) {
			throw new NoResultException("Developper: You expected 1 result but we found none ! sample: " + entity);
		}

		return result;
	}

	/**
	 * We request at most 2, if there's more than one then we throw a {@link NonUniqueResultException}
	 * 
	 * @throws NonUniqueResultException
	 */
	public E findUniqueOrNone(E entity, SearchParameters sp) {
		// this code is an optimization to prevent using a count
		sp.setFirstResult(0);
		sp.setMaxResults(2);
		List<E> results = find(entity, sp);

		if (results == null || results.isEmpty()) {
			return null;
		}

		if (results.size() > 1) {
			throw new NonUniqueResultException("Developper: You expected 1 result but we found more ! sample: " + entity);
		}

		return results.iterator().next();
	}

	protected <R> Predicate getPredicate(Root<E> root, CriteriaQuery<R> query, CriteriaBuilder builder, E entity, SearchParameters sp) {
		return JpaUtil.andPredicate(builder, //
				byRanges(root, query, builder, sp.getRanges(), type), //
				byPropertySelectors(root, builder, sp.getPropertySelectors()), //
				byEntitySelectors(root, builder, sp.getEntitySelectors()), //
				getByExamplePredicate(root, entity, sp, builder), //
				byPatternUtil.byPattern(root, query, builder, sp, type), //
				getExtraPredicate(root, query, builder, entity, sp));
	}

	protected Predicate getByExamplePredicate(Root<E> root, E entity, SearchParameters sp, CriteriaBuilder builder) {
		return byExampleUtil.byExampleOnEntity(root, entity, sp, builder);
	}

	/**
	 * You may override this method to add a Predicate to the default find method.
	 */
	protected <R> Predicate getExtraPredicate(Root<E> root, CriteriaQuery<R> query, CriteriaBuilder builder, E entity, SearchParameters sp) {
		return null;
	}

	// -----------------
	// Commons
	// -----------------

	/**
	 * Set hints for 2d level cache.
	 */
	protected void setCacheHints(TypedQuery<?> typedQuery, SearchParameters sp) {
		if (sp.isCacheable()) {
			typedQuery.setHint("org.hibernate.cacheable", true);

			if (sp.hasCacheRegion()) {
				typedQuery.setHint("org.hibernate.cacheRegion", sp.getCacheRegion());
			} else {
				typedQuery.setHint("org.hibernate.cacheRegion", cacheRegion);
			}
		}
	}

	// -----------------
	// Hibernate Search
	// -----------------
	protected String[] getIndexedFields() {
		return new String[0];
	}
}