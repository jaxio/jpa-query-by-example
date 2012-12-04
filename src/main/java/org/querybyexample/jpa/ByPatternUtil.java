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

import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

@Named
@Singleton
public class ByPatternUtil {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Lookup entities having at least one String attribute matching the passed sp's pattern
	 */
	public <T> Predicate byPattern(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder, final SearchParameters sp, final Class<T> type) {
		if (!sp.hasSearchPattern()) {
			return null;
		}

		List<Predicate> predicates = new ArrayList<Predicate>();
		EntityType<T> entity = em.getMetamodel().entity(type);
		String pattern = sp.getSearchPattern();

		for (Attribute<T, ?> attr : entity.getDeclaredSingularAttributes()) {
			if (attr.getPersistentAttributeType() == MANY_TO_ONE || attr.getPersistentAttributeType() == ONE_TO_ONE) {
				continue;
			}

			if (attr.getJavaType() == String.class) {
				predicates.add(JpaUtil.stringPredicate(root.get(attribute(entity, attr)), pattern, sp, builder));
			}
		}

		return JpaUtil.orPredicate(builder, predicates);
	}

	private static <T> SingularAttribute<T, String> attribute(EntityType<T> entity, Attribute<T, ?> attr) {
		return entity.getDeclaredSingularAttribute(attr.getName(), String.class);
	}
}