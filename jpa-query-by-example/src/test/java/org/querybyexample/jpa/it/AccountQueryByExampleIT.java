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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.PropertySelector;
import org.querybyexample.jpa.SearchParameters;
import org.querybyexample.jpa.app.Account;
import org.querybyexample.jpa.app.AccountQueryByExample;
import org.querybyexample.jpa.app.Account_;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test on AccountDaoImpl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
@Transactional
public class AccountQueryByExampleIT {
    private static final Logger log = Logger.getLogger(AccountQueryByExampleIT.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private AccountQueryByExample accountDao;

    @Test
    @Rollback
    public void defaultSearch() {        
        List<Account> accounts = accountDao.find(new Account(), new SearchParameters());
        for (Account account : accounts) {
            log.info("Got account " + account.getUsername());
        }
   }
    
    @Test
    @Rollback
    public void findDemoOrAdmin() {
        SearchParameters sp = new SearchParameters();
        PropertySelector<Account, String> ps = PropertySelector.newPropertySelector(Account_.username);
        List<String> possibleValues = new ArrayList<String>();
        possibleValues.add("demo");
        possibleValues.add("admin");
        ps.setSelected(possibleValues);        
        sp.addPropertySelector(ps);
        
        List<Account> accounts = accountDao.find(new Account(), sp);
        for (Account account : accounts) {
            log.info("Got account " + account.getUsername());
        }
   }    
}