/*
 * Copyright 2011, MyCellar
 *
 * This file is part of MyCellar.
 *
 * MyCellar is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCellar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
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

        private FluentNamedQuery() {
        }

        /**
         * Fluently set the named query to be used by the DAO layer. Null by
         * default.
         */
        public FluentSearchParameters set(String namedQuery) {
            setNamedQuery(namedQuery);
            return FluentSearchParameters.this;
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentSearchParameters parameters(Map<String, Object> parameters) {
            setNamedQueryParameters(parameters);
            return FluentSearchParameters.this;
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentSearchParameters parameter(String name, Object value) {
            addNamedQueryParameter(name, value);
            return FluentSearchParameters.this;
        }

        /**
         * Fluently set the named query to be used by the DAO layer. Null by
         * default.
         */
        public FluentNamedQuery setAnd(String namedQuery) {
            setNamedQuery(namedQuery);
            return this;
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentNamedQuery parametersAnd(Map<String, Object> parameters) {
            setNamedQueryParameters(parameters);
            return this;
        }

        /**
         * Fluently set the parameters for the named query.
         */
        public FluentNamedQuery parameterAnd(String name, Object value) {
            addNamedQueryParameter(name, value);
            return this;
        }

    }

    public FluentNamedQuery namedQuery() {
        return new FluentNamedQuery();
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

        public FluentSearchParameters add(OrderBy orderBy) {
            addOrderBy(orderBy);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters add(Attribute<?, ?>... attributes) {
            return add(new OrderBy(OrderByDirection.ASC, attributes));
        }

        public FluentSearchParameters add(OrderByDirection orderByDirection, List<Attribute<?, ?>> attributes) {
            return add(new OrderBy(orderByDirection, attributes));
        }

        public FluentSearchParameters add(OrderByDirection orderByDirection, Attribute<?, ?>... attributes) {
            return add(new OrderBy(orderByDirection, attributes));
        }

        public FluentSearchParameters add(String property, Class<?> clazz) {
            return add(new OrderBy(OrderByDirection.ASC, toAttributes(property, clazz)));
        }

        public FluentSearchParameters add(OrderByDirection orderByDirection, String property, Class<?> clazz) {
            return add(new OrderBy(orderByDirection, toAttributes(property, clazz)));
        }

        public boolean has() {
            return !getOrders().isEmpty();
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

        public FluentSearchParameters add(PropertySelector<?, ?>... propertySelectors) {
            for (PropertySelector<?, ?> propertySelector : checkNotNull(propertySelectors)) {
                addProperty(propertySelector);
            }
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters add(SearchMode searchMode, Attribute<?, ?>... attributes) {
            return add(PropertySelector.newPropertySelector(searchMode, attributes));
        }

        public boolean has() {
            return !getProperties().isEmpty();
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
        public FluentSearchParameters add(EntitySelector<?, ?, ?>... entitySelectors) {
            for (EntitySelector<?, ?, ?> entitySelector : checkNotNull(entitySelectors)) {
                addEntity(entitySelector);
            }
            return FluentSearchParameters.this;
        }

        @SuppressWarnings("unchecked")
        public <E2, T2 extends Identifiable<TPK2>, TPK2 extends Serializable> FluentSearchParameters add(SingularAttribute<E2, T2> field, T2... selected) {
            return add(EntitySelector.newEntitySelector(field, selected));
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

        public FluentSearchParameters maxResults(int maxResults) {
            setMaxResults(maxResults);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters noLimit() {
            setMaxResults(-1);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters limitBroadSearch() {
            setMaxResults(500);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters first(int first) {
            setFirst(first);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters pageSize(int pageSize) {
            setPageSize(pageSize);
            return FluentSearchParameters.this;
        }

        public FluentPagination maxResultsAnd(int maxResults) {
            setMaxResults(maxResults);
            return this;
        }

        public FluentPagination noLimitAnd() {
            setMaxResults(-1);
            return this;
        }

        public FluentPagination limitBroadSearchAnd() {
            setMaxResults(500);
            return this;
        }

        public FluentPagination firstAnd(int first) {
            setFirst(first);
            return this;
        }

        public FluentPagination pageSizeAnd(int pageSize) {
            setPageSize(pageSize);
            return this;
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
        public FluentSearchParameters add(SingularAttribute<?, ?>... xToOneAttributes) {
            for (SingularAttribute<?, ?> xToOneAttribute : checkNotNull(xToOneAttributes)) {
                addLeftJoin(xToOneAttribute);
            }
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

        public FluentSearchParameters set(boolean cacheable) {
            setCacheable(cacheable);
            return FluentSearchParameters.this;
        }

        public FluentSearchParameters enable() {
            return set(true);
        }

        public FluentSearchParameters disable() {
            return set(false);
        }

        public FluentSearchParameters region(String cacheRegion) {
            setCacheRegion(checkNotNull(cacheRegion));
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
        public FluentSearchParameters addExtraParameter(String key, Object o) {
            getExtraParameters().put(checkNotNull(key), o);
            return FluentSearchParameters.this;
        }

        /**
         * add additionnal parameter.
         */
        public FluentExtraParameters addExtraParameterAnd(String key, Object o) {
            getExtraParameters().put(checkNotNull(key), o);
            return this;
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
