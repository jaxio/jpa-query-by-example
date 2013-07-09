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

import static org.fest.assertions.Assertions.assertThat;
import static org.querybyexample.jpa.EntitySelector.newEntitySelector;
import static org.querybyexample.jpa.EntitySelector.newEntitySelectorInCpk;
import static org.querybyexample.jpa.OrderByDirection.ASC;
import static org.querybyexample.jpa.OrderByDirection.DESC;
import static org.querybyexample.jpa.PropertySelector.newPropertySelector;
import static org.querybyexample.jpa.app.Account_.homeAddress;
import static org.querybyexample.jpa.app.Account_.username;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.EntitySelector;
import org.querybyexample.jpa.OrderBy;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.AccountQueryByExample;
import org.querybyexample.jpa.app.Account_;
import org.querybyexample.jpa.app.Address;
import org.querybyexample.jpa.app.Address_;
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

    private static final int NB_ACCOUNTS = 7;

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
        assertSize(noMatch, new SearchParameters().orMode(), 1);
    }

    @Test
    public void usernameStartingLikeAdm() {
        Account example = new Account();
        example.setUsername("adm");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        assertSize(example, new SearchParameters().startingLike(), 1);
    }

    @Test
    public void usernameEndingLikeMin() {
        Account example = new Account();
        example.setUsername("min");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        assertSize(example, new SearchParameters().endingLike(), 1);
    }

    @Test
    public void usernameContainsMinAnywhere() {
        Account example = new Account();
        example.setUsername("mi");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        assertSize(example, new SearchParameters().anywhere(), 1);
    }

    @Test
    public void usernameEqualsAdminCaseInsensitive() {
        Account example = new Account();
        example.setUsername("AdMiN");
        assertEmpty(example);
        assertEmpty(example, new SearchParameters());
        assertSize(example, new SearchParameters().caseInsensitive(), 1);
    }

    @Test
    public void leftJoinHomeAddress() {
        assertSize(new SearchParameters().leftJoin(homeAddress), NB_ACCOUNTS);
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

        assertSize(new Account(almostParis), new SearchParameters().endingLike(), 1);
    }

    @Test
    public void byEntiySelector() {
        assertSize(new SearchParameters().entity(newEntitySelector(Account_.homeAddress, adminHomeAddress())), 1);
    }

    @Test
    public void byEntiySelectorAndIncludingNull() {
        assertSize(new SearchParameters().entity(newEntitySelector(Account_.homeAddress, adminHomeAddress()).includeNull()), 2);
    }

    @Test
    public void byEntiySelectorIncludingNull() {
        assertSize(new SearchParameters().entity(newEntitySelector(Account_.homeAddress).includeNull()), 1);
    }

    @Test
    public void byEntiySelectorNotIncludingNull() {
        assertSize(new SearchParameters().entity(newEntitySelector(Account_.homeAddress).withoutNull()), NB_ACCOUNTS - 1);
    }

    @Test
    public void byEntiySelectorInnerPk() {
        EntitySelector<Account, Address, Integer> role = newEntitySelectorInCpk(Account_.homeAddress, Address_.id);
        role.add(adminHomeAddress());

        assertSize(new SearchParameters().entity(role), 1);
    }

    @Test
    public void byPropertySelector() {
        PropertySelector<Address, String> city = newPropertySelector(Account_.homeAddress, Address_.city);
        city.add("Paris");

        assertSize(new SearchParameters().property(city), 1);
    }

    @Test
    public void byManyToMany() {
        Account adminOnly = new Account(adminRole());
        assertSize(adminOnly, 1);
        assertSize(adminOnly, new SearchParameters(), 1);
        assertSize(adminOnly, new SearchParameters().distinct(), 1);

        Account users = new Account(userRole());
        assertSize(users, 3);
        assertSize(users, new SearchParameters().distinct(), 3);

        Account userOrAdmin = new Account(adminRole(), userRole());
        assertSize(userOrAdmin, 1);
        assertSize(userOrAdmin, new SearchParameters(), 1);
        assertSize(userOrAdmin, new SearchParameters().distinct(), 1);
        assertSize(userOrAdmin, new SearchParameters().useORInManyToMany(), 4);
        assertSize(userOrAdmin, new SearchParameters().useORInManyToMany().distinct(), 3);

        Account unassigned = new Account(unassignedRole());
        assertEmpty(unassigned);
        assertEmpty(unassigned, new SearchParameters().distinct());
    }

    @Test
    public void noOrderByUsesDefaultFromRepository() {
        assertFirstUsername(new SearchParameters(), "admin");
    }

    @Test
    public void orderByFieldname() {
        assertFirstUsername(new SearchParameters().orderBy(), "admin");
        assertFirstUsername(new SearchParameters().orderBy("username"), "admin");
        assertFirstUsername(new SearchParameters().orderBy(ASC, "username"), "admin");
        assertFirstUsername(new SearchParameters().orderBy(DESC, "username"), "user");
        assertFirstUsername(new SearchParameters().orderBy(new OrderBy(DESC, "username")), "user");
        assertFirstUsername(new SearchParameters().orderBy(new OrderBy(ASC, "username")), "admin");
    }

    @Test
    public void orderByAttribute() {
        assertFirstUsername(new SearchParameters().orderBy(), "admin");
        assertFirstUsername(new SearchParameters().orderBy(username), "admin");
        assertFirstUsername(new SearchParameters().orderBy(ASC, username), "admin");
        assertFirstUsername(new SearchParameters().orderBy(DESC, username), "user");
        assertFirstUsername(new SearchParameters().orderBy(new OrderBy(ASC, username)), "admin");
        assertFirstUsername(new SearchParameters().orderBy(new OrderBy(DESC, username)), "user");
    }

    @Test
    public void orderByManyToOneAttribute() {
        SearchParameters searchParameter = new SearchParameters().orderBy(ASC, Account_.homeAddress, Address_.city);
        assertSize(searchParameter, NB_ACCOUNTS);
        assertFirstUsername(searchParameter, "homeless");
    }

    @Test
    public void bySearchPatternOnAllStringFields() {
        assertSize(new SearchParameters().searchPattern("admin"), 1);
        assertEmpty(new SearchParameters().searchPattern("dmin"));
        assertEmpty(new SearchParameters().searchPattern("no_match"));
    }

    @Test
    public void maxResults() {
        assertSize(new SearchParameters(), NB_ACCOUNTS);
        assertThat(accountQBE.find(new SearchParameters().maxResults(-1))).hasSize(NB_ACCOUNTS);
        assertThat(accountQBE.find(new SearchParameters().maxResults(1))).hasSize(1);
        assertThat(accountQBE.find(new SearchParameters().maxResults(4))).hasSize(4);
        assertThat(accountQBE.find(new SearchParameters().maxResults(NB_ACCOUNTS + 1))).hasSize(NB_ACCOUNTS);
        assertFirstUsername(new SearchParameters().maxResults(1), "admin");
    }

    @Test
    public void maxResultsDoesNotImpactFindCount() {
        assertThat(accountQBE.findCount(new SearchParameters().maxResults(4))).isEqualTo(NB_ACCOUNTS);
    }

    @Test
    public void firstResult() {
        assertFirstUsername(new SearchParameters(), "admin");
        assertFirstUsername(new SearchParameters().first(0), "admin");
        assertFirstUsername(new SearchParameters().first(1), "user");
        assertFirstUsername(new SearchParameters().first(2), "demo");
    }

    @Test
    public void firstResultDoesNotImpactFindCount() {
        assertThat(accountQBE.findCount(new SearchParameters().first(4))).isEqualTo(NB_ACCOUNTS);
    }

    @Test
    public void firstAndMaxResultCombined() {
        assertThat(accountQBE.find(new SearchParameters().first(4).maxResults(2))).hasSize(2);
        assertThat(accountQBE.findCount(new SearchParameters().first(4).maxResults(2))).isEqualTo(NB_ACCOUNTS);
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
