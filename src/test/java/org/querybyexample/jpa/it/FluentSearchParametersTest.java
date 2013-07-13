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
package org.querybyexample.jpa.it;

import static org.fest.assertions.Assertions.*;

import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.FluentSearchParameters;
import org.querybyexample.jpa.OrderBy;
import org.querybyexample.jpa.OrderByDirection;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.Range;
import org.querybyexample.jpa.SearchMode;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.Account_;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Need to be transactional for metamodel.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
@Transactional
public class FluentSearchParametersTest {

    @Test
    public void full() {
        Range<Account, Date> range = new Range<>(Account_.birthDate);
        String cacheRegion = "toto";
        String key1 = "K1";
        String value1 = "V1";
        SingularAttribute<?, ?> leftJoin = Account_.addressId;
        String namedQuery = "namedQuery";
        String key2 = "K2";
        String value2 = "V2";
        OrderBy orderBy = new OrderBy(OrderByDirection.ASC, Account_.email);
        int first = 10;
        int maxResults = 300;
        int pageSize = 20;
        PropertySelector<?, ?> propertySelector = new PropertySelector<Account, String>(Account_.email);
        String searchPattern = "searchPattern";

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addRange(range);
        searchParameters.setCacheable(true);
        searchParameters.setCacheRegion(cacheRegion);
        searchParameters.setCaseSensitive(false);
        searchParameters.setDistinct(true);
        searchParameters.addExtraParameter(key1, value1);
        searchParameters.setUseANDInManyToMany(false);
        searchParameters.addLeftJoin(leftJoin);
        searchParameters.setNamedQuery(namedQuery);
        searchParameters.addNamedQueryParameter(key2, value2);
        searchParameters.addOrderBy(orderBy);
        searchParameters.setFirst(first);
        searchParameters.setMaxResults(maxResults);
        searchParameters.setPageSize(pageSize);
        searchParameters.setAndMode(false);
        searchParameters.addProperty(propertySelector);
        searchParameters.setSearchMode(SearchMode.ENDING_LIKE);
        searchParameters.setSearchPattern(searchPattern);

        SearchParameters fluentSearchParameters = new FluentSearchParameters() //
                .addRanges(range) //
                .caching().enable().region(cacheRegion).endCaching() //
                .caseSensitiveness().insensitive() //
                .distinct() //
                .extraParameters().addExtraParameter(key1, value1).endExtraParameters() //
                .inManyToMany().useOR() //
                .leftJoins().add(leftJoin).endLeftJoins() //
                .namedQuery(namedQuery).parameter(key2, value2).endNamedQuery() //
                .orderBy().add(orderBy).endOrderBy() //
                .pagination().first(first).maxResults(maxResults).pageSize(pageSize).endPagination() //
                .predicateMode().orMode() //
                .properties().add(propertySelector).endProperties() //
                .searchMode().endingLike() //
                .searchPattern(searchPattern) //
                .toSearchParameters();

        assertThat(fluentSearchParameters.getRanges()).isEqualTo(searchParameters.getRanges());
        assertThat(fluentSearchParameters.isCacheable()).isEqualTo(searchParameters.isCacheable());
        assertThat(fluentSearchParameters.getCacheRegion()).isEqualTo(searchParameters.getCacheRegion());
        assertThat(fluentSearchParameters.isCaseSensitive()).isEqualTo(searchParameters.isCaseSensitive());
        assertThat(fluentSearchParameters.getDistinct()).isEqualTo(searchParameters.getDistinct());
        assertThat(fluentSearchParameters.getExtraParameters()).isEqualTo(searchParameters.getExtraParameters());
        assertThat(fluentSearchParameters.getUseANDInManyToMany()).isEqualTo(searchParameters.getUseANDInManyToMany());
        assertThat(fluentSearchParameters.getLeftJoins()).isEqualTo(searchParameters.getLeftJoins());
        assertThat(fluentSearchParameters.getNamedQuery()).isEqualTo(searchParameters.getNamedQuery());
        assertThat(fluentSearchParameters.getNamedQueryParameters()).isEqualTo(searchParameters.getNamedQueryParameters());
        assertThat(fluentSearchParameters.getOrders()).isEqualTo(searchParameters.getOrders());
        assertThat(fluentSearchParameters.getFirst()).isEqualTo(searchParameters.getFirst());
        assertThat(fluentSearchParameters.getMaxResults()).isEqualTo(searchParameters.getMaxResults());
        assertThat(fluentSearchParameters.getPageSize()).isEqualTo(searchParameters.getPageSize());
        assertThat(fluentSearchParameters.isAndMode()).isEqualTo(searchParameters.isAndMode());
        assertThat(fluentSearchParameters.getProperties()).isEqualTo(searchParameters.getProperties());
        assertThat(fluentSearchParameters.getSearchMode()).isEqualTo(searchParameters.getSearchMode());
        assertThat(fluentSearchParameters.getSearchPattern()).isEqualTo(searchParameters.getSearchPattern());
    }
}
