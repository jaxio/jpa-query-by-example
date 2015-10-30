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

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hibernate.search.jpa.Search.getFullTextEntityManager;

@Named
@Singleton
public class HibernateSearchUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateSearchUtil.class);

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> clazz, SearchParameters sp, List<SingularAttribute<?, ?>> availableProperties) {
        log.info("Searching {} with terms : {} with available Properties: {}", new Object[] { clazz.getSimpleName(), sp.getTerms(), availableProperties });
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(entityManager);
        Query query = sp.getLuceneQueryBuilder().build(fullTextEntityManager, sp, availableProperties);

        if (query == null) {
            return null;
        }

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery( //
                query, clazz);
        if (sp.getMaxResults() > 0) {
            ftq.setMaxResults(sp.getMaxResults());
        }
        return ftq.getResultList();
    }

    /**
     * Same as {@link #find(Class, SearchParameters, String[])} but will return only the id 
     */
    @SuppressWarnings("unchecked")
    public <T> List<Serializable> findId(Class<T> clazz, SearchParameters sp, List<SingularAttribute<?, ?>> availableProperties) {
        log.info("Searching {} with terms : {} with available Properties: {}", new Object[] { clazz.getSimpleName(), sp.getTerms(), availableProperties });
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(entityManager);
        Query query = sp.getLuceneQueryBuilder().build(fullTextEntityManager, sp, availableProperties);

        if (query == null) {
            return null;
        }

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery( //
                query, clazz);
        ftq.setProjection("id");
        if (sp.getMaxResults() > 0) {
            ftq.setMaxResults(sp.getMaxResults());
        }
        List<Serializable> ids = newArrayList();
        List<Object[]> resultList = ftq.getResultList();
        for (Object[] result : resultList) {
            ids.add((Serializable) result[0]);
        }
        return ids;
    }

}