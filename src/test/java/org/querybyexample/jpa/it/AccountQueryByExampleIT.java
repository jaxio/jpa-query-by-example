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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.EntitySelector;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.Ranges;
import org.querybyexample.jpa.SearchMode;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.AccountQueryByExample;
import org.querybyexample.jpa.app.Account_;
import org.querybyexample.jpa.app.Address;
import org.querybyexample.jpa.app.Role;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test illustrating the use of JPA Query By Example project. 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
@Transactional
public class AccountQueryByExampleIT {
    private static final Logger log = Logger.getLogger(AccountQueryByExampleIT.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private AccountQueryByExample accountQBE;

    @Test
    @Rollback
    public void defaultSearch() {
        List<Account> accounts = accountQBE.find(new Account(), new SearchParameters());
        logResults(accounts);
    }

    @Test
    @Rollback
    public void findUsingBasicExample() {
        SearchParameters sp = new SearchParameters();

        Account example = new Account();
        example.setUsername("admin");

        sp.setSearchMode(SearchMode.EQUALS);

        List<Account> accounts = accountQBE.find(example, sp);
        logResults(accounts);
    }

    @Test
    @Rollback
    public void findUsingBasicExample2() {
        SearchParameters sp = new SearchParameters();

        Account example = new Account();
        example.setUsername("dmi");
        example.setIsEnabled(true);

        sp.setSearchMode(SearchMode.ANYWHERE);

        List<Account> accounts = accountQBE.find(example, sp);
        logResults(accounts);
    }

    @Test
    @Rollback
    public void findUsingPropertySelector() {
        SearchParameters sp = new SearchParameters();

        // Selector for username
        PropertySelector<Account, String> psUsername = PropertySelector.newPropertySelector(Account_.username);
        List<String> possibleValues = new ArrayList<String>();
        possibleValues.add("demo");
        possibleValues.add("admin");
        psUsername.setSelected(possibleValues);

        sp.addPropertySelector(psUsername);

        List<Account> accounts = accountQBE.find(new Account(), sp);
        logResults(accounts);
    }

    @Test
    @Rollback
    public void findByRangeAndFetchJoinAddress() throws ParseException {
        SearchParameters sp = new SearchParameters();

        // date range
        Ranges.RangeDate<Account> rangeBirthday = Ranges.RangeDate.newRangeDate(Account_.birthDate);
        rangeBirthday.setFrom(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE).parse("01/01/1972"));
        rangeBirthday.setTo(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE).parse("01/01/1982"));
        sp.addRange(rangeBirthday);

        // fetch join address
        sp.addLeftJoinAttribute(Account_.homeAddress);

        List<Account> accounts = accountQBE.find(new Account(), sp);
        logResults(accounts);
    }

    @Test
    @Rollback
    public void findByExampleOnAssociatedAddress() throws ParseException {
        SearchParameters sp = new SearchParameters();

        Account example = new Account();
        example.setHomeAddress(new Address());
        example.getHomeAddress().setCity("Paris");

        List<Account> accounts = accountQBE.find(example, sp);
        logResults(accounts);
    }
    
    @Test
    @Rollback
    public void findUsingEntitySelectorOnManyToOne() {
        SearchParameters sp = new SearchParameters();

        // Selector for associated address
        EntitySelector<Account, Address, Integer> esAddressId = EntitySelector.newEntitySelector(Account_.addressId);
        List<Address> possibleValues = new ArrayList<Address>();
        possibleValues.add(entityManager.find(Address.class, 1));
        possibleValues.add(entityManager.find(Address.class, 2));
        esAddressId.setSelected(possibleValues);

        sp.addEntitySelector(esAddressId);

        List<Account> accounts = accountQBE.find(new Account(), sp);
        logResults(accounts);
    }
    

    @Test
    @Rollback
    public void findWithManyToMany() {
        SearchParameters sp = new SearchParameters();

        Account example = new Account();
        example.addRole(entityManager.find(Role.class, 1));
        example.addRole(entityManager.find(Role.class, 2));

        List<Account> accounts = accountQBE.find(example, sp);
        logResults(accounts);
    }        

    private void logResults(List<Account> accounts) {
        for (Account account : accounts) {
            log.info("Got account " + account.getUsername());
        }
    }
}