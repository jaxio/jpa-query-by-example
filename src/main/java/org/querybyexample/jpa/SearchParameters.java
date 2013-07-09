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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The SearchParameters is used to pass search parameters to the DAO layer.
 * 
 * Its usage keeps 'find' method signatures in the DAO/Service layer simple.
 * 
 * A SearchParameters helps you drive your search in the following areas:
 * <ul>
 * <li>Configure the search mode (EQUALS, LIKE, ...)</li>
 * <li>Pagination: it allows you to limit your search results to a specific range.</li>
 * <li>Allow you to specify ORDER BY and ASC/DESC</li>
 * <li>Enable/disable case sensitivity</li>
 * <li>Enable/disable 2d level cache</li>
 * <li>LIKE search against all string values: simply set the searchPattern property</li>
 * <li>Named query: if you set a named query it will be executed. Named queries can be defined in annotation or src/main/resources/META-INF/orm.xml</li>
 * </ul>
 * 
 * Note : All requests are limited to a maximum number of elements to prevent resource exhaustion.
 * 
 * @see GenericDao
 * @see SearchMode
 * @see OrderBy
 * @see Range
 * @see ByNamedQueryUtil
 * @see PropertySelector
 * @see EntitySelector
 */
public class SearchParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    private SearchMode searchMode = SearchMode.EQUALS;
    private boolean andMode = true;

    // named query related
    private String namedQuery;
    private Map<String, Object> parameters = newHashMap();

    private List<OrderBy> orders = newArrayList();

    // technical parameters
    private boolean caseSensitive = true;

    // pagination
    private int maxResults = -1;
    private int first = 0;
    private int pageSize = 0;

    // joins
    private List<SingularAttribute<?, ?>> leftJoins = newArrayList();

    // ranges
    private List<Range<?, ?>> ranges = newArrayList();

    // property selectors
    private List<PropertySelector<?, ?>> properties = newArrayList();

    // entity selectors
    private List<EntitySelector<?, ?, ?>> entities = newArrayList();

    // pattern to match against all strings.
    private String searchPattern;

    // Warn: before enabling cache for queries,
    // check this: https://hibernate.atlassian.net/browse/HHH-1523
    private Boolean cacheable = false;
    private String cacheRegion;

    // extra parameters
    private Map<String, Object> extraParameters = newHashMap();

    private boolean useANDInManyToMany = true;

    private boolean useDistinct = false;

    // -----------------------------------
    // SearchMode
    // -----------------------------------

    /**
     * Fluently set the @{link SearchMode}. It defaults to EQUALS.
     * 
     * @see SearchMode#EQUALS
     */
    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = checkNotNull(searchMode);
    }

    /**
     * Fluently set the @{link SearchMode}. It defaults to EQUALS.
     * 
     * @see SearchMode#EQUALS
     */
    public SearchParameters searchMode(SearchMode searchMode) {
        setSearchMode(searchMode);
        return this;
    }

    /**
     * Use the EQUALS @{link SearchMode}.
     * 
     * @see SearchMode#EQUALS
     */
    public SearchParameters equals() {
        return searchMode(SearchMode.EQUALS);
    }

    /**
     * Use the ANYWHERE @{link SearchMode}.
     * 
     * @see SearchMode#ANYWHERE
     */
    public SearchParameters anywhere() {
        return searchMode(SearchMode.ANYWHERE);
    }

    /**
     * Use the STARTING_LIKE @{link SearchMode}.
     * 
     * @see SearchMode#STARTING_LIKE
     */
    public SearchParameters startingLike() {
        return searchMode(SearchMode.STARTING_LIKE);
    }

    /**
     * Use the LIKE @{link SearchMode}.
     * 
     * @see SearchMode#LIKE
     */
    public SearchParameters like() {
        return searchMode(SearchMode.LIKE);
    }

    /**
     * Use the ENDING_LIKE @{link SearchMode}.
     * 
     * @see SearchMode#ENDING_LIKE
     */
    public SearchParameters endingLike() {
        return searchMode(SearchMode.ENDING_LIKE);
    }

    /**
     * Return the @{link SearchMode}. It defaults to EQUALS.
     * 
     * @see SearchMode#EQUALS
     */
    public SearchMode getSearchMode() {
        return searchMode;
    }

    public boolean is(SearchMode searchMode) {
        return getSearchMode() == searchMode;
    }

    // -----------------------------------
    // Predicate mode
    // -----------------------------------

    /**
     * use <code>and</code> to build the final predicate
     */
    public SearchParameters andMode() {
        andMode = true;
        return this;
    }

    /**
     * use <code>or</code> to build the final predicate
     */
    public SearchParameters orMode() {
        andMode = false;
        return this;
    }

    public boolean isAndMode() {
        return andMode;
    }

    // -----------------------------------
    // Named query support
    // -----------------------------------

    /**
     * Returns true if a named query has been set, false otherwise. When it returns true, the DAO layer will call the namedQuery.
     */
    public boolean hasNamedQuery() {
        return isNotBlank(namedQuery);
    }

    /**
     * Set the named query to be used by the DAO layer. Null by default.
     */
    public void setNamedQuery(String namedQuery) {
        this.namedQuery = namedQuery;
    }

    /**
     * Fluently set the named query to be used by the DAO layer. Null by default.
     */
    public SearchParameters namedQuery(String namedQuery) {
        setNamedQuery(namedQuery);
        return this;
    }

    /**
     * Return the name of the named query to be used by the DAO layer.
     */
    public String getNamedQuery() {
        return namedQuery;
    }

    /**
     * Set the parameters for the named query.
     */
    public void setNamedQueryParameters(Map<String, Object> parameters) {
        this.parameters = checkNotNull(parameters);
    }

    /**
     * Set the parameters for the named query.
     */
    public void addNamedQueryParameter(String name, Object value) {
        parameters.put(checkNotNull(name), checkNotNull(value));
    }

    /**
     * Fluently set the parameters for the named query.
     */
    public SearchParameters namedQueryParameters(Map<String, Object> parameters) {
        setNamedQueryParameters(parameters);
        return this;
    }

    /**
     * Fluently set the parameters for the named query.
     */
    public SearchParameters namedQueryParameter(String name, Object value) {
        addNamedQueryParameter(name, value);
        return this;
    }

    /**
     * The parameters associated with the named query, if any.
     */
    public Map<String, Object> getNamedQueryParameters() {
        return parameters;
    }

    /**
     * Return the value of the given parameter name.
     */
    public Object getNamedQueryParameter(String parameterName) {
        return parameters.get(checkNotNull(parameterName));
    }

    // -----------------------------------
    // Search pattern support
    // -----------------------------------

    /**
     * When it returns true, it indicates to the DAO layer to use the given searchPattern on all string properties.
     */
    public boolean hasSearchPattern() {
        return isNotBlank(searchPattern);
    }

    /**
     * Set the pattern which may contains wildcards (ex: "e%r%ka" ). The given searchPattern is used by the DAO layer on all string properties. Null by default.
     */
    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    /**
     * Fluently set the pattern which may contains wildcards (ex: "e%r%ka" ). The given searchPattern is used by the DAO layer on all string properties. Null by
     * default.
     */
    public SearchParameters searchPattern(String searchPattern) {
        setSearchPattern(searchPattern);
        return this;
    }

    /**
     * Returns the search pattern to be used by the DAO layer.
     */
    public String getSearchPattern() {
        return searchPattern;
    }

    // -----------------------------------
    // Case sensitiveness support
    // -----------------------------------

    /**
     * Set the case sensitiveness. Defaults to false.
     * 
     * @param caseSensitive
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Fluently set the case sensitiveness. Defaults to false.
     * 
     * @param caseSensitive
     */
    public SearchParameters caseSensitive(boolean caseSensitive) {
        setCaseSensitive(caseSensitive);
        return this;
    }

    /**
     * Fluently set the case sensitiveness to true.
     */
    public SearchParameters caseSensitive() {
        setCaseSensitive(true);
        return this;
    }

    /**
     * Fluently set the case sensitiveness to false.
     */
    public SearchParameters caseInsensitive() {
        setCaseSensitive(false);
        return this;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isCaseInsensitive() {
        return !caseSensitive;
    }

    // -----------------------------------
    // Order by support
    // -----------------------------------

    public List<OrderBy> getOrders() {
        return orders;
    }

    public void addOrderBy(OrderBy orderBy) {
        orders.add(checkNotNull(orderBy));
    }

    public SearchParameters orderBy(OrderBy orderBy) {
        addOrderBy(orderBy);
        return this;
    }

    public SearchParameters orderBy(Attribute<?, ?>... attributes) {
        return orderBy(new OrderBy(OrderByDirection.ASC, attributes));
    }

    public SearchParameters orderBy(OrderByDirection orderByDirection, Attribute<?, ?>... attributes) {
        return orderBy(new OrderBy(orderByDirection, attributes));
    }

    public SearchParameters orderBy(String property) {
        return orderBy(new OrderBy(OrderByDirection.ASC, property));
    }

    public SearchParameters orderBy(OrderByDirection orderByDirection, String property) {
        return orderBy(new OrderBy(orderByDirection, property));
    }

    public boolean hasOrders() {
        return !orders.isEmpty();
    }

    // -----------------------------------
    // Search by range support
    // -----------------------------------

    public List<Range<?, ?>> getRanges() {
        return ranges;
    }

    public void addRange(Range<?, ?> range) {
        ranges.add(checkNotNull(range));
    }

    public SearchParameters range(Range<?, ?>... ranges) {
        for (Range<?, ?> range : checkNotNull(ranges)) {
            addRange(range);
        }
        return this;
    }

    public boolean hasRanges() {
        return !ranges.isEmpty();
    }

    // -----------------------------------
    // Search by property selector support
    // -----------------------------------

    public List<PropertySelector<?, ?>> getProperties() {
        return properties;
    }

    public void addProperty(PropertySelector<?, ?> propertySelector) {
        properties.add(checkNotNull(propertySelector));
    }

    public SearchParameters property(PropertySelector<?, ?>... propertySelectors) {
        for (PropertySelector<?, ?> propertySelector : checkNotNull(propertySelectors)) {
            addProperty(propertySelector);
        }
        return this;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    // -----------------------------------
    // Search by entity selector support
    // -----------------------------------

    public List<EntitySelector<?, ?, ?>> getEntities() {
        return entities;
    }

    public void addEntity(EntitySelector<?, ?, ?> entitySelector) {
        checkNotNull(entitySelector);
        entities.add(entitySelector);
    }

    /**
     * Add the given {@link EntitySelector}s to construct predicate for the underlying foreign key.
     */
    public SearchParameters entity(EntitySelector<?, ?, ?>... entitySelectors) {
        for (EntitySelector<?, ?, ?> entitySelector : checkNotNull(entitySelectors)) {
            addEntity(entitySelector);
        }
        return this;
    }

    public boolean hasEntities() {
        return !entities.isEmpty();
    }

    // -----------------------------------
    // Pagination support
    // -----------------------------------

    /**
     * Set the maximum number of results to retrieve. Pass -1 for no limits.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public SearchParameters maxResults(int maxResults) {
        setMaxResults(maxResults);
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public SearchParameters noLimit() {
        setMaxResults(-1);
        return this;
    }

    public SearchParameters limitBroadSearch() {
        setMaxResults(500);
        return this;
    }

    /**
     * Set the position of the first result to retrieve.
     * 
     * @param first position of the first result, numbered from 0
     */
    public void setFirst(int first) {
        this.first = first;
    }

    public SearchParameters first(int first) {
        setFirst(first);
        return this;
    }

    public int getFirst() {
        return first;
    }

    /**
     * Set the page size, that is the maximum number of result to retrieve.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public SearchParameters pageSize(int pageSize) {
        setPageSize(pageSize);
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    // -----------------------------------------
    // Fetch associated entity using a LEFT Join
    // -----------------------------------------

    /**
     * Returns the attribute (x-to-one association) which must be fetched with a left join.
     */
    public List<SingularAttribute<?, ?>> getLeftJoins() {
        return leftJoins;
    }

    public boolean hasLeftJoins() {
        return !leftJoins.isEmpty();
    }

    /**
     * The given attribute (x-to-one association) will be fetched with a left join.
     */
    public void addLeftJoin(SingularAttribute<?, ?> xToOneAttribute) {
        leftJoins.add(checkNotNull(xToOneAttribute));
    }

    /**
     * Fluently set the join attribute
     */
    public SearchParameters leftJoin(SingularAttribute<?, ?>... xToOneAttributes) {
        for (SingularAttribute<?, ?> xToOneAttribute : checkNotNull(xToOneAttributes)) {
            addLeftJoin(xToOneAttribute);
        }
        return this;
    }

    // -----------------------------------
    // Caching support
    // -----------------------------------

    /**
     * Default to false. Please read https://hibernate.atlassian.net/browse/HHH-1523 before using cache.
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public SearchParameters cacheable(boolean cacheable) {
        setCacheable(cacheable);
        return this;
    }

    public SearchParameters enableCache() {
        setCacheable(true);
        return this;
    }

    public SearchParameters disableCache() {
        setCacheable(false);
        return this;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public boolean hasCacheRegion() {
        return isNotBlank(cacheRegion);
    }

    public void setCacheRegion(String cacheRegion) {
        this.cacheRegion = cacheRegion;
    }

    public SearchParameters cacheRegion(String cacheRegion) {
        setCacheRegion(checkNotNull(cacheRegion));
        return this;
    }

    public String getCacheRegion() {
        return cacheRegion;
    }

    // -----------------------------------
    // Extra parameters
    // -----------------------------------

    /**
     * Set additionnal parameters.
     */
    public void setExtraParameters(Map<String, Object> extraParameters) {
        this.extraParameters = extraParameters;
    }

    public Map<String, Object> getExtraParameters() {
        return extraParameters;
    }

    /**
     * add additionnal parameter.
     */
    public SearchParameters addExtraParameter(String key, Object o) {
        extraParameters.put(checkNotNull(key), o);
        return this;
    }

    /**
     * get additionnal parameter.
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtraParameter(String key) {
        return (T) extraParameters.get(key);
    }

    // -----------------------------------
    // Use and in NN Search
    // -----------------------------------

    public SearchParameters useORInManyToMany() {
        return useANDInManyToMany(false);
    }

    public SearchParameters useANDInManyToMany() {
        return useANDInManyToMany(true);
    }

    public SearchParameters useANDInManyToMany(boolean useANDInNNSearch) {
        this.useANDInManyToMany = useANDInNNSearch;
        return this;
    }

    public boolean getUseANDInManyToMany() {
        return useANDInManyToMany;
    }

    // -----------------------------------
    // Distinct
    // -----------------------------------

    public void setDistinct(boolean useDistinct) {
        this.useDistinct = useDistinct;
    }

    public boolean getDistinct() {
        return useDistinct;
    }

    public SearchParameters distinct(boolean useDistinct) {
        setDistinct(useDistinct);
        return this;
    }

    public SearchParameters distinct() {
        return distinct(true);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}