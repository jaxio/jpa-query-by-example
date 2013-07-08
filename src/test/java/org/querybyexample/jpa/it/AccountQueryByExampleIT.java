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
import static org.querybyexample.jpa.OrderByDirection.ASC;
import static org.querybyexample.jpa.OrderByDirection.DESC;
import static org.querybyexample.jpa.PropertySelector.newPropertySelector;
import static org.querybyexample.jpa.app.Account_.homeAddress;
import static org.querybyexample.jpa.app.Account_.username;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.OrderBy;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.AccountQueryByExample;
import org.querybyexample.jpa.app.Account_;
import org.querybyexample.jpa.app.Address;
import org.querybyexample.jpa.app.Address_;
import org.querybyexample.jpa.app.Role;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

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

    private static final int NB_ACCOUNTS = 6;

    @Test
    @Rollback
    public void all() {
        assertThat(accountQBE.find()).hasSize(NB_ACCOUNTS);
        assertThat(accountQBE.find(new Account())).hasSize(NB_ACCOUNTS);
        assertThat(accountQBE.find(new SearchParameters())).hasSize(NB_ACCOUNTS);
        assertThat(accountQBE.find(new Account(), new SearchParameters())).hasSize(NB_ACCOUNTS);
    }

    @Test
    @Rollback
    public void usernameMatch() {
        Account admin = new Account();
        admin.setUsername("admin");
        assertThat(accountQBE.find(admin)).hasSize(1);
    }

    @Test
    @Rollback
    public void usernameDoesNotMatch() {
        Account noMatch = new Account();
        noMatch.setUsername("noMatch");
        assertThat(accountQBE.find(noMatch)).isEmpty();
    }

    @Test
    @Rollback
    public void usernameAndEmailMatch() {
        Account example = new Account();
        example.setUsername("admin");
        example.setEmail("admin@example.com");
        assertThat(accountQBE.find(example)).hasSize(1);
    }

    @Test
    @Rollback
    public void usernameAndEmailDoesNotMatch() {
        Account noMatch = new Account();
        noMatch.setUsername("admin");
        noMatch.setEmail("noMatch");
        assertThat(accountQBE.find(noMatch)).isEmpty();
    }

    @Test
    @Rollback
    public void usernameStartingLikeAdm() {
        Account example = new Account();
        example.setUsername("adm");
        assertThat(accountQBE.find(example)).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters())).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters().startingLike())).hasSize(1);
    }

    @Test
    @Rollback
    public void usernameEndingLikeMin() {
        Account example = new Account();
        example.setUsername("min");
        assertThat(accountQBE.find(example)).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters())).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters().endingLike())).hasSize(1);
    }

    @Test
    @Rollback
    public void usernameContainingMin() {
        Account example = new Account();
        example.setUsername("mi");
        assertThat(accountQBE.find(example)).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters())).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters().anywhere())).hasSize(1);
    }

    @Test
    @Rollback
    public void usernameEqualsAdminCaseSensitive() {
        Account example = new Account();
        example.setUsername("AdMiN");
        assertThat(accountQBE.find(example)).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters())).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters().caseSensitive())).isEmpty();
        assertThat(accountQBE.find(example, new SearchParameters().caseInsensitive())).hasSize(1);
    }

    @Test
    @Rollback
    public void leftJoinHomeAddress() {
        assertThat(accountQBE.find(new SearchParameters().leftJoin(homeAddress))).hasSize(NB_ACCOUNTS);
    }

    @Test
    @Rollback
    public void byManyToOnePropertyMatch() {
        Address paris = new Address();
        paris.setCity("Paris");

        Account example = new Account();
        example.setHomeAddress(paris);

        assertThat(accountQBE.find(example)).hasSize(1);
    }

    @Test
    @Rollback
    public void byPropertySelector() {
        PropertySelector<Address, String> city = newPropertySelector(Account_.homeAddress, Address_.city);
        city.setSelected(Lists.newArrayList("Paris"));

        assertThat(accountQBE.find(new SearchParameters().property(city))).hasSize(1);
    }

    @Test
    @Rollback
    public void byManyToOnePropertyDoesNotMatch() {
        Address invalidAddress = new Address();
        invalidAddress.setCity("noMatch");

        Account noMatch = new Account();
        noMatch.setHomeAddress(invalidAddress);

        assertThat(accountQBE.find(noMatch)).isEmpty();
    }

    @Test
    @Rollback
    public void byManyToOnePropertyEndingLike() {
        Address almostParis = new Address();
        almostParis.setCity("ris");
        Account example = new Account();
        example.setHomeAddress(almostParis);

        assertThat(accountQBE.find(example, new SearchParameters().endingLike())).hasSize(1);
    }

    @Test
    @Rollback
    public void byManyToMany() {
        Account adminOnly = new Account();
        adminOnly.addRole(entityManager.find(Role.class, 1));
        assertThat(accountQBE.find(adminOnly)).hasSize(1);
        assertThat(accountQBE.find(adminOnly, new SearchParameters())).hasSize(1);
        assertThat(accountQBE.find(adminOnly, new SearchParameters().distinct())).hasSize(1);

        Account users = new Account();
        users.addRole(entityManager.find(Role.class, 2));
        assertThat(accountQBE.find(users)).hasSize(3);
        assertThat(accountQBE.find(users, new SearchParameters().distinct())).hasSize(3);

        Account userOrAdmin = new Account();
        userOrAdmin.addRole(entityManager.find(Role.class, 1));
        userOrAdmin.addRole(entityManager.find(Role.class, 2));
        assertThat(accountQBE.find(userOrAdmin)).hasSize(1);
        assertThat(accountQBE.find(userOrAdmin, new SearchParameters())).hasSize(1);
        assertThat(accountQBE.find(userOrAdmin, new SearchParameters().distinct())).hasSize(1);
        assertThat(accountQBE.find(userOrAdmin, new SearchParameters().useORInManyToMany())).hasSize(4);
        assertThat(accountQBE.find(userOrAdmin, new SearchParameters().useORInManyToMany().distinct())).hasSize(3);

        Account unassigned = new Account();
        unassigned.addRole(entityManager.find(Role.class, 3));
        assertThat(accountQBE.find(unassigned)).isEmpty();
        assertThat(accountQBE.find(unassigned, new SearchParameters().distinct())).isEmpty();
    }

    @Test
    @Rollback
    public void orderByFieldname() {
        List<Account> accounts = accountQBE.find(new SearchParameters().orderBy("username"));
        assertThat(first(accounts).getUsername()).isEqualTo("admin");

        accounts = accountQBE.find(new SearchParameters().orderBy(Account_.username));
        assertThat(first(accounts).getUsername()).isEqualTo("admin");

        accounts = accountQBE.find(new SearchParameters().orderBy(DESC, Account_.username));
        assertThat(first(accounts).getUsername()).isEqualTo("user");

        accounts = accountQBE.find(new SearchParameters().orderBy(new OrderBy(DESC, Account_.username)));
        assertThat(first(accounts).getUsername()).isEqualTo("user");
    }

    @Test
    @Rollback
    public void orderByAttribute() {
        List<Account> accounts = accountQBE.find(new SearchParameters().orderBy(username));
        assertThat(first(accounts).getUsername()).isEqualTo("admin");

        accounts = accountQBE.find(new SearchParameters().orderBy(ASC, username));
        assertThat(first(accounts).getUsername()).isEqualTo("admin");

        accounts = accountQBE.find(new SearchParameters().orderBy(DESC, username));
        assertThat(first(accounts).getUsername()).isEqualTo("user");

        accounts = accountQBE.find(new SearchParameters().orderBy(new OrderBy(DESC, username)));
        assertThat(first(accounts).getUsername()).isEqualTo("user");
    }

    @Test
    @Rollback
    public void bySearchPatternOnAllStringFields() {
        assertThat(accountQBE.find(new SearchParameters().searchPattern("admin"))).hasSize(1);
        assertThat(accountQBE.find(new SearchParameters().searchPattern("min").anywhere())).hasSize(1);
        assertThat(accountQBE.find(new SearchParameters().searchPattern("no_match").anywhere())).isEmpty();
    }

    @Test
    @Rollback
    public void maxResults() {
        assertThat(accountQBE.find()).hasSize(NB_ACCOUNTS);
        assertThat(accountQBE.find(new SearchParameters().maxResults(4))).hasSize(4);
        assertThat(first(accountQBE.find(new SearchParameters().maxResults(4))).getUsername()).isEqualTo("admin");
    }

    @Test
    @Rollback
    public void firstResult() {
        assertThat(first(accountQBE.find(new SearchParameters())).getUsername()).isEqualTo("admin");
        assertThat(first(accountQBE.find(new SearchParameters().first(2))).getUsername()).isNotEqualTo("admin");
        assertThat(accountQBE.find(new SearchParameters().first(4).maxResults(4))).hasSize(2);

        // first and maxResults are not part of count
        assertThat(accountQBE.findCount(new SearchParameters().first(4).maxResults(4))).isEqualTo(6);
    }

    private Account first(List<Account> accounts) {
        return accounts.iterator().next();
    }
}
