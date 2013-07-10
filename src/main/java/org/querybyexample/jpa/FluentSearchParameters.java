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
import static org.querybyexample.jpa.MetamodelUtil.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author speralta
 * 
 * @see SearchParameters
 */
public class FluentSearchParameters extends SearchParameters {

    private static final long serialVersionUID = 1L;

    // -----------------------------------
    // SearchMode
    // -----------------------------------

    public class FluentSearchMode {

        private FluentSearchMode() {
        }

        /**
         * Fluently set the @{link SearchMode}. It defaults to EQUALS.
         * 
         * @see SearchMode#EQUALS
         */
        public FluentSearchParameters set(SearchMode searchMode) {
            setSearchMode(searchMode);
            return FluentSearchParameters.this;
        }

        /**
         * Use the EQUALS @{link SearchMode}.
         * 
         * @see SearchMode#EQUALS
         */
        public FluentSearchParameters equals() {
            return set(SearchMode.EQUALS);
        }

        /**
         * Use the ANYWHERE @{link SearchMode}.
         * 
         * @see SearchMode#ANYWHERE
         */
        public FluentSearchParameters anywhere() {
            return set(SearchMode.ANYWHERE);
        }

        /**
         * Use the STARTING_LIKE @{link SearchMode}.
         * 
         * @see SearchMode#STARTING_LIKE
         */
        public FluentSearchParameters startingLike() {
            return set(SearchMode.STARTING_LIKE);
        }

        /**
         * Use the LIKE @{link SearchMode}.
         * 
         * @see SearchMode#LIKE
         */
        public FluentSearchParameters like() {
            return set(SearchMode.LIKE);
        }

        /**
         * Use the ENDING_LIKE @{link SearchMode}.
         * 
         * @see SearchMode#ENDING_LIKE
         */
        public FluentSearchParameters endingLike() {
            return set(SearchMode.ENDING_LIKE);
        }

        public boolean is(SearchMode searchMode) {
            return getSearchMode() == searchMode;
        }

    }

    public FluentSearchMode searchMode() {
        return new FluentSearchMode();
    }

    // -----------------------------------
    // Predicate mode
    // -----------------------------------

    public class FluentPredicateMode {

        private FluentPredicateMode() {
        }

        /**
         * use <code>and</code> to build the final predicate
         */
        public FluentSearchParameters andMode() {
            setAndMode(true);
            return FluentSearchParameters.this;
        }

        /**
         * use <code>or</code> to build the final predicate
         */
        public FluentSearchParameters orMode() {
            setAndMode(false);
            return FluentSearchParameters.this;
        }

        public boolean isAndMode() {
            return isAndMode();
        }

        public boolean isOrMode() {
            return !isAndMode();
        }

    }

    public FluentPredicateMode predicateMode() {
        return new FluentPredicateMode();
    }

    // -----------------------------------
    // Named query support
    // -----------------------------------

    public class FluentNamedQuery {

        /**
         * Fluently set the named query to be used by the DAO layer. Null by
         * default.
         */
        private FluentNamedQuery(String namedQuery) {
            setNamedQuery(namedQuery);
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentNamedQuery parameters(Map<String, Object> parameters) {
            setNamedQueryParameters(parameters);
            return this;
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentNamedQuery parameter(String name, Object value) {
            addNamedQueryParameter(name, value);
            return this;
        }

        public FluentSearchParameters endNamedQuery() {
            return FluentSearchParameters.this;
        }

    }

    public FluentNamedQuery namedQuery(String namedQuery) {
        return new FluentNamedQuery(namedQuery);
    }

    // -----------------------------------
    // Search pattern support
    // -----------------------------------

    /**
     * Fluently set the pattern which may contains wildcards (ex: "e%r%ka" ).
     * The given searchPattern is used by the DAO layer on all string
     * properties. Null by default.
     */
    public FluentSearchParameters searchPattern(String searchPattern) {
        setSearchPattern(searchPattern);
        return this;
    }

    // -----------------------------------
    // Case sensitiveness support
    // -----------------------------------

    public class FluentCaseSensitiveness {

        private FluentCaseSensitiveness() {
        }

        /**
         * Fluently set the case sensitiveness. Defaults to false.
         * 
         * @param caseSensitive
         */
        public FluentSearchParameters set(boolean caseSensitive) {
            setCaseSensitive(caseSensitive);
            return FluentSearchParameters.this;
        }

        /**
         * Fluently set the case sensitiveness to true.
         */
        public FluentSearchParameters sensitive() {
            setCaseSensitive(true);
            return FluentSearchParameters.this;
        }

        /**
         * Fluently set the case sensitiveness to false.
         */
        public FluentSearchParameters insensitive() {
            setCaseSensitive(false);
            return FluentSearchParameters.this;
        }

        public boolean isSensitive() {
            return !isCaseSensitive();
        }

        public boolean isInsensitive() {
            return !isCaseSensitive();
        }

    }

    public FluentCaseSensitiveness caseSensitiveness() {
        return new FluentCaseSensitiveness();
    }

    // -----------------------------------
    // Order by support
    // -----------------------------------

    public class FluentOrderBy {

        private FluentOrderBy() {
        }

        public FluentOrderBy add(OrderBy orderBy) {
            addOrderBy(orderBy);
            return this;
        }

        public FluentOrderBy add(Attribute<?, ?>... attributes) {
            return add(new OrderBy(OrderByDirection.ASC, attributes));
        }

        public FluentOrderBy add(OrderByDirection orderByDirection, List<Attribute<?, ?>> attributes) {
            return add(new OrderBy(orderByDirection, attributes));
        }

        public FluentOrderBy add(OrderByDirection orderByDirection, Attribute<?, ?>... attributes) {
            return add(new OrderBy(orderByDirection, attributes));
        }

        public FluentOrderBy add(String property, Class<?> clazz) {
            return add(new OrderBy(OrderByDirection.ASC, toAttributes(property, clazz)));
        }

        public FluentOrderBy add(OrderByDirection orderByDirection, String property, Class<?> clazz) {
            return add(new OrderBy(orderByDirection, toAttributes(property, clazz)));
        }

        public FluentSearchParameters endOrderBy() {
            return FluentSearchParameters.this;
        }

    }

    public FluentOrderBy orderBy() {
        return new FluentOrderBy();
    }

    // -----------------------------------
    // Search by range support
    // -----------------------------------

    public FluentSearchParameters addRanges(Range<?, ?>... ranges) {
        for (Range<?, ?> range : checkNotNull(ranges)) {
            addRange(range);
        }
        return this;
    }

    // -----------------------------------
    // Search by property selector support
    // -----------------------------------

    public class FluentPropertySelector {

        private FluentPropertySelector() {
        }

        public FluentPropertySelector add(PropertySelector<?, ?>... propertySelectors) {
            for (PropertySelector<?, ?> propertySelector : checkNotNull(propertySelectors)) {
                addProperty(propertySelector);
            }
            return this;
        }

        public FluentPropertySelector add(SearchMode searchMode, Attribute<?, ?>... attributes) {
            return add(PropertySelector.newPropertySelector(searchMode, attributes));
        }

        public FluentSearchParameters endProperties() {
            return FluentSearchParameters.this;
        }

    }

    public FluentPropertySelector properties() {
        return new FluentPropertySelector();
    }

    // -----------------------------------
    // Search by entity selector support
    // -----------------------------------

    public class FluentEntitySelector {

        private FluentEntitySelector() {
        }

        /**
         * Add the given {@link EntitySelector}s to construct predicate for the
         * underlying foreign key.
         */
        public FluentEntitySelector add(EntitySelector<?, ?, ?>... entitySelectors) {
            for (EntitySelector<?, ?, ?> entitySelector : checkNotNull(entitySelectors)) {
                addEntity(entitySelector);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public <E2, T2 extends Identifiable<TPK2>, TPK2 extends Serializable> FluentEntitySelector add(SingularAttribute<E2, T2> field, T2... selected) {
            return add(EntitySelector.newEntitySelector(field, selected));
        }

        public FluentSearchParameters endEntities() {
            return FluentSearchParameters.this;
        }

    }

    public FluentEntitySelector entities() {
        return new FluentEntitySelector();
    }

    // -----------------------------------
    // Pagination support
    // -----------------------------------

    public class FluentPagination {

        private FluentPagination() {
        }

        public FluentPagination maxResults(int maxResults) {
            setMaxResults(maxResults);
            return this;
        }

        public FluentPagination noLimitAnd() {
            setMaxResults(-1);
            return this;
        }

        public FluentPagination limitBroadSearch() {
            setMaxResults(500);
            return this;
        }

        public FluentPagination first(int first) {
            setFirst(first);
            return this;
        }

        public FluentPagination pageSize(int pageSize) {
            setPageSize(pageSize);
            return this;
        }

        public FluentSearchParameters endPagination() {
            return FluentSearchParameters.this;
        }

    }

    public FluentPagination pagination() {
        return new FluentPagination();
    }

    // -----------------------------------------
    // Fetch associated entity using a LEFT Join
    // -----------------------------------------

    public class FluentLeftJoin {

        private FluentLeftJoin() {
        }

        /**
         * Fluently set the join attribute
         */
        public FluentLeftJoin add(SingularAttribute<?, ?>... xToOneAttributes) {
            for (SingularAttribute<?, ?> xToOneAttribute : checkNotNull(xToOneAttributes)) {
                addLeftJoin(xToOneAttribute);
            }
            return this;
        }

        public FluentSearchParameters endLeftJoins() {
            return FluentSearchParameters.this;
        }

    }

    public FluentLeftJoin leftJoins() {
        return new FluentLeftJoin();
    }

    // -----------------------------------
    // Caching support
    // -----------------------------------

    public class FluentCaching {

        private FluentCaching() {
        }

        public FluentCaching set(boolean cacheable) {
            setCacheable(cacheable);
            return this;
        }

        public FluentCaching enable() {
            return set(true);
        }

        public FluentCaching disable() {
            return set(false);
        }

        public FluentCaching region(String cacheRegion) {
            setCacheRegion(checkNotNull(cacheRegion));
            return this;
        }

        public FluentSearchParameters endCaching() {
            return FluentSearchParameters.this;
        }

    }

    public FluentCaching caching() {
        return new FluentCaching();
    }

    // -----------------------------------
    // Extra parameters
    // -----------------------------------

    public class FluentExtraParameters {

        private FluentExtraParameters() {
        }

        /**
         * add additionnal parameter.
         */
        public FluentExtraParameters addExtraParameter(String key, Object o) {
            getExtraParameters().put(checkNotNull(key), o);
            return this;
        }

        public FluentSearchParameters endExtraParameters() {
            return FluentSearchParameters.this;
        }

    }

    public FluentExtraParameters extraParameters() {
        return new FluentExtraParameters();
    }

    // -----------------------------------
    // Use and in NN Search
    // -----------------------------------

    public class FluentInManyToMany {

        private FluentInManyToMany() {
        }

        public FluentSearchParameters useOR() {
            return useAND(false);
        }

        public FluentSearchParameters useAND() {
            return useAND(true);
        }

        public FluentSearchParameters useAND(boolean useANDInNNSearch) {
            setUseANDInManyToMany(useANDInNNSearch);
            return FluentSearchParameters.this;
        }
    }

    public FluentInManyToMany inManyToMany() {
        return new FluentInManyToMany();
    }

    // ---------------------------------
    // Distinct
    // ---------------------------------

    public FluentSearchParameters distinct(boolean useDistinct) {
        setDistinct(useDistinct);
        return this;
    }

    public FluentSearchParameters distinct() {
        return distinct(true);
    }

}