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

import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author speralta
 * 
 * @see SearchParameters
 */
public class FluentSearchParameters {

    private final SearchParameters searchParameters = new SearchParameters();

    public SearchParameters toSearchParameters() {
        return searchParameters;
    }

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
            searchParameters.setSearchMode(searchMode);
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
            return searchParameters.getSearchMode() == searchMode;
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

        public FluentSearchParameters andMode() {
            searchParameters.setAndMode(true);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters orMode() {
            searchParameters.setAndMode(false);
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

        private FluentNamedQuery(String namedQuery) {
            searchParameters.setNamedQuery(namedQuery);
        }

        public FluentNamedQuery parameters(Map<String, Object> parameters) {
            searchParameters.setNamedQueryParameters(parameters);
            return this;
        }

        public FluentNamedQuery parameter(String name, Object value) {
            searchParameters.addNamedQueryParameter(name, value);
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
        searchParameters.setSearchPattern(searchPattern);
        return this;
    }

    // -----------------------------------
    // Case sensitiveness support
    // -----------------------------------

    public class FluentCaseSensitiveness {

        private FluentCaseSensitiveness() {
        }

        public FluentSearchParameters set(boolean caseSensitive) {
            searchParameters.setCaseSensitive(caseSensitive);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters sensitive() {
            return set(true);
        }

        public FluentSearchParameters insensitive() {
            return set(false);
        }

        public boolean isSensitive() {
            return !searchParameters.isCaseSensitive();
        }

        public boolean isInsensitive() {
            return !isSensitive();
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
            searchParameters.addOrderBy(orderBy);
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
            searchParameters.addRange(range);
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
                searchParameters.addProperty(propertySelector);
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
    // Pagination support
    // -----------------------------------

    public class FluentPagination {

        private FluentPagination() {
        }

        public FluentPagination maxResults(int maxResults) {
            searchParameters.setMaxResults(maxResults);
            return this;
        }

        public FluentPagination noLimitAnd() {
            return maxResults(-1);
        }

        public FluentPagination limitBroadSearch() {
            return maxResults(500);
        }

        public FluentPagination first(int first) {
            searchParameters.setFirst(first);
            return this;
        }

        public FluentPagination pageSize(int pageSize) {
            searchParameters.setPageSize(pageSize);
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

        public FluentLeftJoin add(SingularAttribute<?, ?>... xToOneAttributes) {
            for (SingularAttribute<?, ?> xToOneAttribute : checkNotNull(xToOneAttributes)) {
                searchParameters.addLeftJoin(xToOneAttribute);
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
            searchParameters.setCacheable(cacheable);
            return this;
        }

        public FluentCaching enable() {
            return set(true);
        }

        public FluentCaching disable() {
            return set(false);
        }

        public FluentCaching region(String cacheRegion) {
            searchParameters.setCacheRegion(checkNotNull(cacheRegion));
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

        public FluentExtraParameters addExtraParameter(String key, Object o) {
            searchParameters.getExtraParameters().put(checkNotNull(key), o);
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
            searchParameters.setUseANDInManyToMany(useANDInNNSearch);
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
        searchParameters.setDistinct(useDistinct);
        return this;
    }

    public FluentSearchParameters distinct() {
        return distinct(true);
    }

}