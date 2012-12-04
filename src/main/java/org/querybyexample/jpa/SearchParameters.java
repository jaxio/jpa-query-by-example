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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.querybyexample.jpa.Ranges.RangeDate.rangeDate;
import static org.querybyexample.jpa.Ranges.RangeLocalDate.rangeLocalDate;
import static org.querybyexample.jpa.Ranges.RangeLocalDateTime.rangeLocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.querybyexample.jpa.Ranges.RangeDate;
import org.querybyexample.jpa.Ranges.RangeInteger;
import org.querybyexample.jpa.Ranges.RangeLocalDate;
import org.querybyexample.jpa.Ranges.RangeLocalDateTime;

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
 * @see QueryByExample
 * @see SearchMode
 * @see OrderBy
 * @see Range
 * @see NamedQueryUtil
 * @see PropertySelector
 * @see EntitySelector
 */
public class SearchParameters implements Serializable {
	static final private long serialVersionUID = 1L;

	private SearchMode searchMode = SearchMode.EQUALS;

	// named query related
	private String namedQuery;
	private Map<String, Object> parameters = new HashMap<String, Object>();

	private List<OrderBy> orders = new ArrayList<OrderBy>();

	// technical parameters
	private boolean caseSensitive = false;

	// Pagination
	private int maxResults = 500;
	private int firstResult = 0;

	// Joins
	private Map<JoinType, List<SingularAttribute<?, ?>>> joinAttributes = new HashMap<JoinType, List<SingularAttribute<?, ?>>>();

	// ranges
	private List<Range<?, ?>> ranges = new ArrayList<Range<?, ?>>();

	// property selectors
	private List<PropertySelector<?, ?>> properties = new ArrayList<PropertySelector<?, ?>>();

	// entity selectors
	private List<EntitySelector<?, ? extends Identifiable<?>, ?>> entities = new ArrayList<EntitySelector<?, ? extends Identifiable<?>, ?>>();

	// pattern to match against all strings.
	private String searchPattern;
	// cache
	private Boolean cacheable = true;
	private String cacheRegion;
	// distinct
	private Boolean distinct = false;

	public SearchParameters() {

	}

	// -----------------------------------
	// SearchMode
	// -----------------------------------

	public SearchParameters(SearchMode searchMode) {
		setSearchMode(searchMode);
	}

	/**
	 * Fluently set the @{link SearchMode}. It defaults to EQUALS.
	 * 
	 * @see SearchMode#EQUALS
	 */
	public void setSearchMode(SearchMode searchMode) {
		Validate.notNull(searchMode, "searchMode must not be null");
		this.searchMode = searchMode;
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
		Validate.notNull(parameters, "parameters must not be null");
		this.parameters = parameters;
	}

	/**
	 * Set the parameters for the named query.
	 */
	public void addNamedQueryParameter(String name, Object value) {
		Validate.notNull(name, "name must not be null");
		Validate.notNull(value, "value must not be null");
		parameters.put(name, value);
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
	 * Return the value of the passed parameter name.
	 */
	public Object getNamedQueryParameter(String parameterName) {
		return parameters.get(parameterName);
	}

	// -----------------------------------
	// Search pattern support
	// -----------------------------------

	/**
	 * When it returns true, it indicates to the DAO layer to use the passed searchPattern on all string properties.
	 */
	public boolean hasSearchPattern() {
		return isNotBlank(searchPattern);
	}

	/**
	 * Set the pattern which may contains wildcards (ex: "e%r%ka" ). The passed searchPattern is used by the DAO layer on all string properties. Null by
	 * default.
	 */
	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	/**
	 * Fluently set the pattern which may contains wildcards (ex: "e%r%ka" ). The passed searchPattern is used by the DAO layer on all string properties. Null
	 * by default.
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

	public boolean hasOrders() {
		return !orders.isEmpty();
	}

	public List<OrderBy> getOrders() {
		return orders;
	}

	public void addOrderBy(String fieldName) {
		Validate.notNull(fieldName, "fieldName must not be null");
		orders.add(new OrderBy(fieldName));
	}

	public void addOrderBy(String fieldName, OrderByDirection direction) {
		Validate.notNull(fieldName, "fieldName must not be null");
		Validate.notNull(direction, "direction must not be null");
		orders.add(new OrderBy(fieldName, direction));
	}

	public void addOrderBy(SingularAttribute<? extends Identifiable<? extends Serializable>, ? extends Serializable> attribute) {
		Validate.notNull(attribute, "attribute must not be null");
		orders.add(new OrderBy(attribute));
	}

	public void addOrderBy(SingularAttribute<? extends Identifiable<? extends Serializable>, ? extends Serializable> attribute, OrderByDirection direction) {
		Validate.notNull(attribute, "fieldName must not be null");
		Validate.notNull(direction, "direction must not be null");
		orders.add(new OrderBy(attribute, direction));
	}

	public void addOrderBy(OrderBy orderBy) {
		Validate.notNull(orderBy, "orderBy must not be null");
		orders.add(orderBy);
	}

	public SearchParameters orderBy(OrderBy orderBy) {
		addOrderBy(orderBy);
		return this;
	}

	public SearchParameters orderBy(String fieldName) {
		addOrderBy(fieldName);
		return this;
	}

	public SearchParameters orderBy(String fieldName, OrderByDirection direction) {
		addOrderBy(fieldName, direction);
		return this;
	}

	public SearchParameters orderBy(SingularAttribute<? extends Identifiable<? extends Serializable>, ? extends Serializable> attribute) {
		addOrderBy(attribute);
		return this;
	}

	public SearchParameters orderBy(SingularAttribute<? extends Identifiable<? extends Serializable>, ? extends Serializable> attribute,
			OrderByDirection direction) {
		addOrderBy(attribute, direction);
		return this;
	}

	public void clearOrders() {
		orders.clear();
	}

	// -----------------------------------
	// Search by range support
	// -----------------------------------
	public SearchParameters(Range<?, ?> range) {
		addRange(range);
	}

	public List<Range<?, ?>> getRanges() {
		return ranges;
	}

	public void addRange(Range<?, ?> range) {
		ranges.add(range);
	}

	/**
	 * Add the passed {@link Range} in order to create a 'range' predicate on the corresponding property.
	 */
	public SearchParameters range(Range<?, ?> range) {
		addRange(range);
		return this;
	}

	public SearchParameters range(SingularAttribute<?, LocalDate> field, LocalDate from, LocalDate to) {
		addRange(rangeLocalDate(field, from, to));
		return this;
	}

	public SearchParameters range(SingularAttribute<?, LocalDateTime> field, LocalDateTime from, LocalDateTime to) {
		addRange(rangeLocalDateTime(field, from, to));
		return this;
	}

	public SearchParameters range(SingularAttribute<?, Date> field, Date from, Date to) {
		addRange(rangeDate(field, from, to));
		return this;
	}

	public SearchParameters after(SingularAttribute<?, LocalDate> field, LocalDate from) {
		RangeLocalDate<?> rangeLocalDate = rangeLocalDate(field);
		rangeLocalDate.setFrom(from);
		addRange(rangeLocalDate);
		return this;
	}

	public SearchParameters after(SingularAttribute<?, LocalDateTime> field, LocalDateTime from) {
		RangeLocalDateTime<?> rangeLocalDateTime = rangeLocalDateTime(field);
		rangeLocalDateTime.setFrom(from);
		addRange(rangeLocalDateTime);
		return this;
	}

	public SearchParameters after(SingularAttribute<?, Date> field, Date from) {
		RangeDate<?> rangeDate = rangeDate(field);
		rangeDate.setFrom(from);
		addRange(rangeDate);
		return this;
	}

	public SearchParameters before(SingularAttribute<?, LocalDate> field, LocalDate setToto) {
		RangeLocalDate<?> rangeLocalDate = rangeLocalDate(field);
		rangeLocalDate.setTo(setToto);
		addRange(rangeLocalDate);
		return this;
	}

	public SearchParameters before(SingularAttribute<?, LocalDateTime> field, LocalDateTime setToto) {
		RangeLocalDateTime<?> rangeLocalDateTime = rangeLocalDateTime(field);
		rangeLocalDateTime.setTo(setToto);
		addRange(rangeLocalDateTime);
		return this;
	}

	public SearchParameters before(SingularAttribute<?, Date> field, Date setToto) {
		RangeDate<?> rangeDate = rangeDate(field);
		rangeDate.setTo(setToto);
		addRange(rangeDate);
		return this;
	}

	public SearchParameters lower(SingularAttribute<?, Integer> field, Integer value) {
		RangeInteger<?> rangeInteger = RangeInteger.rangeInteger(field);
		rangeInteger.setTo(value);
		addRange(rangeInteger);
		return this;
	}

	public SearchParameters greather(SingularAttribute<?, Integer> field, Integer value) {
		RangeInteger<?> rangeInteger = RangeInteger.rangeInteger(field);
		rangeInteger.setFrom(value);
		addRange(rangeInteger);
		return this;
	}


	public void clearRanges() {
		ranges.clear();
	}

	// -----------------------------------
	// Search by property selector support
	// -----------------------------------
	public SearchParameters(PropertySelector<?, ?> propertySelector) {
		property(propertySelector);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SearchParameters(SingularAttribute<?, ?> field, Object... values) {
		property(new PropertySelector(field, values));
	}

	public List<PropertySelector<?, ?>> getProperties() {
		return properties;
	}

	public void addProperty(PropertySelector<?, ?> propertySelector) {
		properties.add(propertySelector);
	}

	/**
	 * Add the passed {@link PropertySelector} in order to construct an OR predicate for the corresponding property.
	 */
	public SearchParameters property(PropertySelector<?, ?> propertySelector) {
		addProperty(propertySelector);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SearchParameters property(SingularAttribute<?, ?> field, Object... values) {
		return property(new PropertySelector(field, values));
	}

	public void clearProperties() {
		properties.clear();
	}

	// -----------------------------------
	// Search by entity selector support
	// -----------------------------------

	public SearchParameters(EntitySelector<?, ? extends Identifiable<?>, ?> entitySelector) {
		addEntity(entitySelector);
	}

	public List<EntitySelector<?, ? extends Identifiable<?>, ?>> getEntities() {
		return entities;
	}

	public void addEntity(EntitySelector<?, ? extends Identifiable<?>, ?> entitySelector) {
		entities.add(entitySelector);
	}

	/**
	 * Add the passed {@link EntitySelector} in order to construct an OR predicate for the underlying foreign key.
	 */
	public SearchParameters entity(EntitySelector<?, ? extends Identifiable<?>, ?> entitySelector) {
		addEntity(entitySelector);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SearchParameters entity(SingularAttribute<?, ?> field, Identifiable<?>... values) {
		return entity(new EntitySelector(field, values));
	}

	public void clearEntity() {
		entities.clear();
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

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public SearchParameters firstResult(int firstResult) {
		setFirstResult(firstResult);
		return this;
	}

	public int getFirstResult() {
		return firstResult;
	}

	// -----------------------------------------
	// Fetch associated entity using a LEFT Join
	// -----------------------------------------

	private List<SingularAttribute<?, ?>> getJoinAttributes(JoinType inner) {
		List<SingularAttribute<?, ?>> left = joinAttributes.get(inner);
		if (left == null) {
			left = new ArrayList<SingularAttribute<?, ?>>();
			joinAttributes.put(inner, left);
		}
		return left;
	}

	/**
	 * Returns the attribute (x-to-one association) which must be fetched with an inner join.
	 */
	public List<SingularAttribute<?, ?>> getInnerJoinAttributes() {
		return getJoinAttributes(JoinType.INNER);
	}

	/**
	 * Returns the attribute (x-to-one association) which must be fetched with a left join.
	 */
	public List<SingularAttribute<?, ?>> getLeftJoinAttributes() {
		return getJoinAttributes(JoinType.LEFT);
	}

	/**
	 * The passed attribute (x-to-one association) will be fetched with a left join.
	 */
	public SearchParameters leftJoin(SingularAttribute<?, ?> xToOneAttribute) {
		getLeftJoinAttributes().add(xToOneAttribute);
		return this;
	}

	/**
	 * The passed attribute (x-to-one association) will be fetched with a inner join.
	 */
	public SearchParameters innerJoin(SingularAttribute<?, ?> xToOneAttribute) {
		getInnerJoinAttributes().add(xToOneAttribute);
		return this;
	}

	public Map<JoinType, List<SingularAttribute<?, ?>>> getJoinAttributes() {
		return joinAttributes;
	}

	// -----------------------------------
	// Caching support
	// -----------------------------------

	/**
	 * Default to true.
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
		setCacheRegion(cacheRegion);
		return this;
	}

	public String getCacheRegion() {
		return cacheRegion;
	}

	// -----------------------------------
	// Distinct
	// -----------------------------------

	public boolean isDistinct() {
		return distinct;
	}

	public SearchParameters distinct() {
		distinct = true;
		return this;
	}

	public SearchParameters distinct(boolean value) {
		distinct = value;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}