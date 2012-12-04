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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

/**
 * Helper class to create named query supporting dynamic sort order and pagination.
 */
@Named
@Singleton
public class NamedQueryUtil {
	private static final Logger log = Logger.getLogger(NamedQueryUtil.class);

	private static final String NAMED_PARAMETER_NOW = "now";

	@PersistenceContext
	private EntityManager entityManager;

	public NamedQueryUtil() {
	}

	public NamedQueryUtil(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	public <T> List<T> findByNamedQuery(SearchParameters sp) {
		if (sp == null || !sp.hasNamedQuery()) {
			throw new IllegalArgumentException("searchParameters must be non null and must have a namedQuery");
		}

		Query query = entityManager.createNamedQuery(sp.getNamedQuery());
		String queryString = getQueryString(query);

		// append order by if needed
		if (queryString != null && sp.hasOrders()) {
			// create the sql restriction clausis
			StringBuilder orderClausis = new StringBuilder("order by ");
			boolean first = true;
			for (OrderBy orderBy : sp.getOrders()) {
				if (!first) {
					orderClausis.append(", ");
				}
				orderClausis.append(orderBy.getColumn());
				orderClausis.append(orderBy.isOrderDesc() ? " desc" : " asc");
				first = false;
			}

			if (log.isDebugEnabled()) {
				log.debug("appending: [" + orderClausis.toString() + "] to " + queryString);
			}

			query = recreateQuery(query, queryString + " " + orderClausis.toString());
		}

		// pagination
		if (sp.getFirstResult() >= 0) {
			query.setFirstResult(sp.getFirstResult());
		}
		if (sp.getMaxResults() > 0) {
			query.setMaxResults(sp.getMaxResults());
		}

		// named parameters
		setQueryParameters(query, sp);

		// execute
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) query.getResultList();

		if (result != null && log.isDebugEnabled()) {
			log.debug(sp.getNamedQuery() + " returned a List of size: " + result.size());
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T byNamedQuery(SearchParameters sp) {
		return (T) objectByNamedQuery(sp);
	}

	public Number numberByNamedQuery(SearchParameters sp) {
		return (Number) objectByNamedQuery(sp);
	}

	public Object objectByNamedQuery(SearchParameters sp) {
		if (sp == null || !sp.hasNamedQuery()) {
			throw new IllegalStateException("Invalid search template provided: could not determine which namedQuery to use");
		}

		Query query = entityManager.createNamedQuery(sp.getNamedQuery());
		String queryString = getQueryString(query);

		// append select count if needed
		if (queryString != null && queryString.toLowerCase().startsWith("from") && !queryString.toLowerCase().contains("count(")) {
			query = recreateQuery(query, "select count(*) " + queryString);
		}

		setQueryParameters(query, sp);

		if (log.isDebugEnabled()) {
			log.debug("objectNamedQuery " + sp.toString());
		}

		// execute
		Object result = query.getSingleResult();

		if (log.isDebugEnabled()) {
			log.debug(sp.getNamedQuery() + " returned a " + (result == null ? "null" : result.getClass()) + " object");
			if (result instanceof Number) {
				log.debug(sp.getNamedQuery() + " returned a number with value : " + result);
			}
		}

		return result;
	}

	private void setQueryParameters(Query query, SearchParameters sp) {
		// add default parameter if specified in the named query
		for (Parameter<?> p : query.getParameters()) {
			if (NAMED_PARAMETER_NOW.equals(p.getName())) {
				query.setParameter(NAMED_PARAMETER_NOW, Calendar.getInstance().getTime());
			}
		}

		// add parameters for the named query
		for (String paramName : sp.getNamedQueryParameters().keySet()) {
			query.setParameter(paramName, sp.getNamedQueryParameter(paramName));
		}
	}

	/**
	 * If the named query has the "query" hint, it uses the hint value (which must be jpa QL) to create a new query and append to it the proper order by clause.
	 */
	private String getQueryString(Query query) {
		Map<String, Object> hints = query.getHints();
		return hints != null ? (String) hints.get("query") : null;
	}

	private Query recreateQuery(Query current, String newSqlString) {
		Query result = entityManager.createQuery(newSqlString);
		Map<String, Object> hints = current.getHints();
		for (String hintName : hints.keySet()) {
			result.setHint(hintName, hints.get(hintName));
		}
		return result;
	}
}