package demo;

import com.jaxio.jpa.querybyexample.SearchParameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:applicationContext.xml"})
@Transactional
public class AccountQueryByExampleTest {
    @Inject
    AccountRepository accountRepository;

    @Test
    @Rollback
    public void dateRangeQuery() throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date from = dateFormat.parse("1920-12-01");
        Date to = dateFormat.parse("1974-12-01");

        SearchParameters sp = new SearchParameters().range(from, to, Account_.birthDate);
        List<Account> accountList = accountRepository.find(sp);
        Assert.assertThat(accountList.size(), is(1));
        Assert.assertThat(accountList.get(0).getUsername(), is("nico"));
        System.out.println("******************************************");
        System.out.println(accountList.get(0));
        System.out.println("******************************************");
    }

    @Test
    @Rollback
    public void dateRangeAndLikeQuery() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date from = dateFormat.parse("1920-12-01");
        Date to = dateFormat.parse("2020-12-01");

        SearchParameters sp = new SearchParameters().range(from, to, Account_.birthDate);
        sp.anywhere(); // enable search like '%...%'

        Account example = new Account();
        example.setUsername("i"); // will search username Like '%i%'

        List<Account> accountList = accountRepository.find(example, sp);
        System.out.println("******************************************");
        for (Account a : accountList) {
            System.out.println(a);
        }
        System.out.println("******************************************");
    }
}
