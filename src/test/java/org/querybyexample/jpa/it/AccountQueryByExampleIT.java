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
import static org.querybyexample.jpa.OrderByDirection.*;
import static org.querybyexample.jpa.PropertySelector.*;
import static org.querybyexample.jpa.app.Account_.*;

import java.util.Arrays;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.OrderBy;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.SearchMode;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.AccountQueryByExample;
import org.querybyexample.jpa.app.Account_;
import org.querybyexample.jpa.app.Address;
import org.querybyexample.jpa.app.Address_;
import org.querybyexample.jpa.app.Legacy;
import org.querybyexample.jpa.app.Legacy_;
import org.querybyexample.jpa.app.Role;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

/**
 * Integration test illustrating the use of JPA Query By Example project.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
@Transactional
public class AccountQueryByExampleIT {
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private AccountQueryByExample accountQBE;

    private static final int NB_ACCOUNTS = 8;

    @Test
    public void noRestriction() {
        assertSize(new Account(), NB_ACCOUNTS);
        assertSize(new SearchParameters(), NB_ACCOUNTS);
        assertSize(new Account(), new SearchParameters(), NB_ACCOUNTS);
    }

    @Test
    public void usernameMatchExactly() {
        Account admin = new Account();
        admin.setUsername("admin");
        assertSize(admin, 1);
    }

    @Test
    public void usernameDoesNotMatch() {
        Account noMatch = new Account();
        noMatch.setUsername("noMatch");
        assertEmpty(noMatch);
    }

    @Test
    public void usernameAndEmailMatch() {
        Account example = new Account();
        example.setUsername("admin");
        example.setEmail("admin@example.com");
        assertSize(example, 1);
    }

    @Test
    public void usernameAndEmailDoesNotMatch() {
        Account noMatch = new Account();
        noMatch.setUsername("admin");
        noMatch.setEmail("noMatch");
        assertEmpty(noMatch);
    }

    @Test
    public void usernameOrEmailDoMatch() {
        Account noMatch = new Account();
        noMatch.setUsername("admin");
        noMatch.setEmail("noMatch");
        SearchParameters sp = new SearchParameters();
        sp.setAndMode(false);
        assertSize(noMatch, sp, 1);
    }

    @Test
    public void usernameStartingLikeAdm() {
        Account example = new Account();
        example.setUsername("adm");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        SearchParameters sp = new SearchParameters();
        sp.setSearchMode(SearchMode.STARTING_LIKE);
        assertSize(example, sp, 1);
    }

    @Test
    public void usernameEndingLikeMin() {
        Account example = new Account();
        example.setUsername("min");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        SearchParameters sp = new SearchParameters();
        sp.setSearchMode(SearchMode.ENDING_LIKE);
        assertSize(example, sp, 1);
    }

    @Test
    public void usernameContainsMinAnywhere() {
        Account example = new Account();
        example.setUsername("mi");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        SearchParameters sp = new SearchParameters();
        sp.setSearchMode(SearchMode.ANYWHERE);
        assertSize(example, sp, 1);
    }

    @Test
    public void usernameEqualsAdminCaseInsensitive() {
        Account example = new Account();
        example.setUsername("AdMiN");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        SearchParameters sp = new SearchParameters();
        sp.setCaseSensitive(false);
        assertSize(example, sp, 1);
    }

    @Test
    public void leftJoinHomeAddress() {
        SearchParameters sp = new SearchParameters();
        sp.addLeftJoin(homeAddress);
        assertSize(sp, NB_ACCOUNTS);
    }

    @Test
    public void byManyToOnePropertyMatch() {
        Address paris = new Address();
        paris.setCity("Paris");

        assertSize(new Account(paris), 1);
    }

    @Test
    public void byManyToOnePropertyDoesNotMatch() {
        Address invalidAddress = new Address();
        invalidAddress.setCity("noMatch");

        assertEmpty(new Account(invalidAddress));
    }

    @Test
    public void byManyToOnePropertyEndingLike() {
        Address almostParis = new Address();
        almostParis.setCity("ris");

        SearchParameters sp = new SearchParameters();
        sp.setSearchMode(SearchMode.ENDING_LIKE);
        assertSize(new Account(almostParis), sp, 1);
    }

    @Test
    public void byEntiySelector() {
        SearchParameters sp = new SearchParameters();
        sp.addProperty(newPropertySelector(Arrays.asList(adminHomeAddress()), Account_.homeAddress));
        assertSize(sp, 1);
    }

    @Test
    public void byEntiySelectorAndIncludingNull() {
        SearchParameters sp = new SearchParameters();
        sp.addProperty(newPropertySelector(Arrays.asList(adminHomeAddress(), null), Account_.homeAddress));
        assertSize(sp, 3);
    }

    @Test
    public void byEntiySelectorIncludingNull() {
        SearchParameters sp = new SearchParameters();
        sp.addProperty(newPropertySelector(Arrays.asList((Address) null), Account_.homeAddress));
        assertSize(sp, 2);
    }

    @Test
    public void byEntiySelectorNotIncludingNull() {
        SearchParameters sp = new SearchParameters();
        sp.addProperty(newPropertySelector(Account_.homeAddress).withoutNull());
        assertSize(sp, NB_ACCOUNTS - 2);
    }

    @Test
    public void byEntiySelectorInnerPk() {
        SearchParameters sp = new SearchParameters();
        sp.addProperty(newPropertySelector(Arrays.asList(legacyAccount().getId()), Account_.legacy, Legacy_.id));
        assertSize(sp, 1);
    }

    @Test
    public void byPropertySelector() {
        PropertySelector<Address, String> city = newPropertySelector(Account_.homeAddress, Address_.city);
        city.add("Paris");
        SearchParameters sp = new SearchParameters();
        sp.addProperty(city);
        assertSize(sp, 1);
    }

    @Test
    public void byManyToMany() {
        Account adminOnly = new Account(adminRole());
        assertSize(adminOnly, 1);
        assertSize(adminOnly, new SearchParameters(), 1);
        SearchParameters sp = new SearchParameters();
        sp.setDistinct(true);
        assertSize(adminOnly, sp, 1);

        Account users = new Account(userRole());
        assertSize(users, 3);
        sp = new SearchParameters();
        sp.setDistinct(true);
        assertSize(users, sp, 3);

        Account userOrAdmin = new Account(adminRole(), userRole());
        assertSize(userOrAdmin, 1);
        assertSize(userOrAdmin, new SearchParameters(), 1);
        sp = new SearchParameters();
        sp.setDistinct(true);
        assertSize(userOrAdmin, sp, 1);
        sp = new SearchParameters();
        sp.setUseANDInManyToMany(false);
        assertSize(userOrAdmin, sp, 4);
        sp = new SearchParameters();
        sp.setUseANDInManyToMany(false);
        sp.setDistinct(true);
        assertSize(userOrAdmin, sp, 3);

        Account unassigned = new Account(unassignedRole());
        assertEmpty(unassigned);
        sp = new SearchParameters();
        sp.setDistinct(true);
        assertEmpty(unassigned, sp);
    }

    @Test
    public void noOrderByUsesDefaultFromRepository() {
        assertFirstUsername(new SearchParameters(), "admin");
    }

    @Test
    public void orderBy() {
        assertFirstUsername(new SearchParameters(), "admin");
        SearchParameters sp = new SearchParameters();
        sp.addOrderBy(new OrderBy(ASC, username));
        assertFirstUsername(sp, "admin");
        sp = new SearchParameters();
        sp.addOrderBy(new OrderBy(DESC, username));
        assertFirstUsername(sp, "user");
    }

    @Test
    public void orderByManyToOne() {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addOrderBy(new OrderBy(ASC, Account_.homeAddress, Address_.city));
        assertSize(searchParameters, NB_ACCOUNTS);
        assertFirstUsername(searchParameters, "homeless");
    }

    @Test
    public void bySearchPatternOnAllStringFields() {
        SearchParameters sp = new SearchParameters();
        sp.setSearchPattern("admin");
        assertSize(sp, 1);
        sp = new SearchParameters();
        sp.setSearchPattern("dmin");
        assertEmpty(sp);
        sp = new SearchParameters();
        sp.setSearchPattern("no_match");
        assertEmpty(sp);
    }

    @Test
    public void maxResults() {
        assertSize(new SearchParameters(), NB_ACCOUNTS);
        SearchParameters sp = new SearchParameters();
        sp.setMaxResults(-1);
        assertThat(accountQBE.find(sp)).hasSize(NB_ACCOUNTS);
        sp = new SearchParameters();
        sp.setMaxResults(1);
        assertThat(accountQBE.find(sp)).hasSize(1);
        sp = new SearchParameters();
        sp.setMaxResults(4);
        assertThat(accountQBE.find(sp)).hasSize(4);
        sp = new SearchParameters();
        sp.setMaxResults(NB_ACCOUNTS + 1);
        assertThat(accountQBE.find(sp)).hasSize(NB_ACCOUNTS);
        sp = new SearchParameters();
        sp.setMaxResults(1);
        assertFirstUsername(sp, "admin");
    }

    @Test
    public void maxResultsDoesNotImpactFindCount() {
        SearchParameters sp = new SearchParameters();
        sp.setMaxResults(41);
        assertThat(accountQBE.findCount(sp)).isEqualTo(NB_ACCOUNTS);
    }

    @Test
    public void firstResult() {
        assertFirstUsername(new SearchParameters(), "admin");
        SearchParameters sp = new SearchParameters();
        sp.setFirst(0);
        assertFirstUsername(sp, "admin");
        sp = new SearchParameters();
        sp.setFirst(1);
        assertFirstUsername(sp, "user");
        sp = new SearchParameters();
        sp.setFirst(2);
        assertFirstUsername(sp, "demo");
    }

    @Test
    public void firstResultDoesNotImpactFindCount() {
        SearchParameters sp = new SearchParameters();
        sp.setFirst(4);
        assertThat(accountQBE.findCount(sp)).isEqualTo(NB_ACCOUNTS);
    }

    @Test
    public void firstAndMaxResultCombined() {
        SearchParameters sp = new SearchParameters();
        sp.setFirst(4);
        sp.setMaxResults(2);
        assertThat(accountQBE.find(sp)).hasSize(2);
        assertThat(accountQBE.findCount(sp)).isEqualTo(NB_ACCOUNTS);
    }

    private Role adminRole() {
        return getRole(1);
    }

    private Role userRole() {
        return getRole(2);
    }

    private Role unassignedRole() {
        return getRole(3);
    }

    private Role getRole(int primaryKey) {
        return entityManager.find(Role.class, primaryKey);
    }

    private Address adminHomeAddress() {
        Account admin = new Account();
        admin.setUsername("admin");
        return accountQBE.find(admin).get(0).getHomeAddress();
    }

    private Legacy legacyAccount() {
        Account admin = new Account();
        admin.setUsername("legacy");
        return accountQBE.find(admin).get(0).getLegacy();
    }

    private void assertEmpty(Account account) {
        assertSize(account, 0);
    }

    private void assertEmpty(Account account, SearchParameters searchParameters) {
        assertSize(account, searchParameters, 0);
    }

    private void assertEmpty(SearchParameters searchParameters) {
        assertSize(searchParameters, 0);
    }

    private void assertSize(Account account, int expectedSize) {
        assertThat(accountQBE.find(account)).hasSize(expectedSize);
        assertThat(accountQBE.findCount(account)).isEqualTo(expectedSize);
    }

    private void assertSize(SearchParameters searchParameters, int expectedSize) {
        assertThat(accountQBE.find(searchParameters)).hasSize(expectedSize);
        assertThat(accountQBE.findCount(searchParameters)).isEqualTo(expectedSize);
    }

    private void assertSize(Account account, SearchParameters searchParameters, int expectedSize) {
        assertThat(accountQBE.find(account, searchParameters)).hasSize(expectedSize);
        assertThat(accountQBE.findCount(account, searchParameters)).isEqualTo(expectedSize);
    }

    private void assertFirstUsername(SearchParameters searchParameters, String expectedUsername) {
        assertThat(Iterables.getFirst(accountQBE.find(searchParameters), null).getUsername()).isEqualTo(expectedUsername);
    }
}
