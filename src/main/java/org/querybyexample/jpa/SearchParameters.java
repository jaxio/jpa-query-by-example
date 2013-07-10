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
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.apache.commons.lang.StringUtils.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
 * <li>Pagination: it allows you to limit your search results to a specific
 * range.</li>
 * <li>Allow you to specify ORDER BY and ASC/DESC</li>
 * <li>Enable/disable case sensitivity</li>
 * <li>Enable/disable 2d level cache</li>
 * <li>LIKE search against all string values: simply set the searchPattern
 * property</li>
 * <li>Named query: if you set a named query it will be executed. Named queries
 * can be defined in annotation or src/main/resources/META-INF/orm.xml</li>
 * </ul>
 * 
 * Note : All requests are limited to a maximum number of elements to prevent
 * resource exhaustion.
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

    private final List<OrderBy> orders = newArrayList();

    // technical parameters
    private boolean caseSensitive = true;

    // pagination
    private int maxResults = -1;
    private int first = 0;
    private int pageSize = 0;

    // joins
    private final List<SingularAttribute<?, ?>> leftJoins = newArrayList();

    // ranges
    private final List<Range<?, ?>> ranges = newArrayList();

    // property selectors
    private final List<PropertySelector<?, ?>> properties = newArrayList();

    // entity selectors
    private final List<EntitySelector<?, ?, ?>> entities = newArrayList();

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
     * Return the @{link SearchMode}. It defaults to EQUALS.
     * 
     * @see SearchMode#EQUALS
     */
    public SearchMode getSearchMode() {
        return searchMode;
    }

    // -----------------------------------
    // Predicate mode
    // -----------------------------------

    /**
     * @return the andMode
     */
    public boolean isAndMode() {
        return andMode;
    }

    /**
     * @param andMode
     *            the andMode to set
     */
    public void setAndMode(boolean andMode) {
        this.andMode = andMode;
    }

    // -----------------------------------
    // Named query support
    // -----------------------------------

    /**
     * Returns true if a named query has been set, false otherwise. When it
     * returns true, the DAO layer will call the namedQuery.
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
     * When it returns true, it indicates to the DAO layer to use the given
     * searchPattern on all string properties.
     */
    public boolean hasSearchPattern() {
        return isNotBlank(searchPattern);
    }

    /**
     * Set the pattern which may contains wildcards (ex: "e%r%ka" ). The given
     * searchPattern is used by the DAO layer on all string properties. Null by
     * default.
     */
    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
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

    public boolean isCaseSensitive() {
        return caseSensitive;
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

    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Set the position of the first result to retrieve.
     * 
     * @param first
     *            position of the first result, numbered from 0
     */
    public void setFirst(int first) {
        this.first = first;
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

    public int getPageSize() {
        return pageSize;
    }

    // -----------------------------------------
    // Fetch associated entity using a LEFT Join
    // -----------------------------------------

    /**
     * Returns the attribute (x-to-one association) which must be fetched with a
     * left join.
     */
    public List<SingularAttribute<?, ?>> getLeftJoins() {
        return leftJoins;
    }

    public boolean hasLeftJoins() {
        return !leftJoins.isEmpty();
    }

    /**
     * The given attribute (x-to-one association) will be fetched with a left
     * join.
     */
    public void addLeftJoin(SingularAttribute<?, ?> xToOneAttribute) {
        leftJoins.add(checkNotNull(xToOneAttribute));
    }

    // -----------------------------------
    // Caching support
    // -----------------------------------

    /**
     * Default to false. Please read
     * https://hibernate.atlassian.net/browse/HHH-1523 before using cache.
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
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
     * get additionnal parameter.
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtraParameter(String key) {
        return (T) extraParameters.get(key);
    }

    // -----------------------------------
    // Use and in NN Search
    // -----------------------------------

    public void setUseANDInManyToMany(boolean useANDInManyToMany) {
        this.useANDInManyToMany = useANDInManyToMany;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}