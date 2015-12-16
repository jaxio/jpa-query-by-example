package demo;

import com.jaxio.jpa.querybyexample.*;
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
import java.util.Arrays;
import java.util.Calendar;
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
    public void simpleQueryOnLastName() throws Exception {
        Account example = new Account().lastName("Jagger");
        //SearchParameters sp = new SearchParameters().caseInsensitive();
        List<Account> result = accountRepository.find(example);
        Assert.assertThat(result.get(0).getUsername(), is("mick"));
    }

    @Test
    @Rollback
    public void simpleQueryOnLastNameCaseInsensitiveWithOrderBy() throws Exception {
        Account example = new Account().lastName("Jagger");
        example.setLastName("Jagger");
        SearchParameters sp = new SearchParameters().caseInsensitive().orderBy(OrderByDirection.ASC, Account_.lastName);
        List<Account> result = accountRepository.find(example, sp);
        Assert.assertThat(result.get(0).getUsername(), is("mick"));
    }

    @Test
    @Rollback
    public void simpleQueryOnLastNameWithOrderByAndPagination() throws Exception {
        Account example = new Account();
        example.setLastName("Jagger");
        SearchParameters sp = new SearchParameters().orderBy(OrderByDirection.ASC, Account_.lastName) //
                .first(50).maxResults(25);
        List<Account> result = accountRepository.find(example, sp);
        Assert.assertThat(result.size(), is(0));
    }

    @Test
    @Rollback
    public void simpleQueryOnLastNameWithLike() throws Exception {
        Account example = new Account();
        example.setLastName("Jag");
        SearchParameters sp = new SearchParameters().startingLike();
        List<Account> result = accountRepository.find(example, sp);
        Assert.assertThat(result.size(), is(1));
    }

    @Test
    @Rollback
    public void simpleQueryOnSeveralFields_AND() throws Exception {
        Account example = new Account();
        example.setLastName("Jag");
        example.setBirthDate(new Date());
        SearchParameters sp = new SearchParameters().orderBy(OrderByDirection.ASC, Account_.lastName).startingLike();
        List<Account> result = accountRepository.find(example, sp);
        Assert.assertThat(result.size(), is(0));
    }

    @Test
    @Rollback
    public void simpleQueryOnSeveralFields_OR() throws Exception {
        Account example = new Account();
        example.setLastName("Jag");
        example.setBirthDate(new Date());
        SearchParameters sp = new SearchParameters().orMode().orderBy(OrderByDirection.ASC, Account_.lastName).startingLike();
        List<Account> result = accountRepository.find(example, sp);
        Assert.assertThat(result.size(), is(1));
    }

    @Test
    @Rollback
    public void dateRangeQuery() throws Exception {
        Account example = new Account();
        example.setLastName("Jagger");

        Calendar from = Calendar.getInstance();
        from.set(1940, 0, 1);

        Calendar to = Calendar.getInstance();
        to.set(1945, 11, 31);

        Range<Account, Date> birthDateRange = Range.newRange(Account_.birthDate);
        birthDateRange.from(from.getTime()).to(to.getTime());

        SearchParameters sp = new SearchParameters().range(birthDateRange);
        List<Account> result = accountRepository.find(example, sp);

        Assert.assertThat(result.size(), is(1));
        Assert.assertThat(result.get(0).getUsername(), is("mick"));
        System.out.println("******************************************");
        System.out.println(result.get(0));
        System.out.println("******************************************");
    }

    @Test
    @Rollback
    public void dateRangeQueryVariation() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date from = dateFormat.parse("1920-12-01");
        Date to = dateFormat.parse("1974-12-01");

        SearchParameters sp = new SearchParameters().range(from, to, Account_.birthDate);
        List<Account> accountList = accountRepository.find(sp);
        Assert.assertThat(accountList.size(), is(4));
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


    @Test
    @Rollback
    public void matchAllStringProperty() throws Exception {
        SearchParameters sp = new SearchParameters().searchMode(SearchMode.STARTING_LIKE).searchPattern("Jag");
        List<Account> result = accountRepository.find(sp);
        Assert.assertThat(result.size(), is(1));
        Assert.assertThat(result.get(0).getUsername(), is("mick"));
    }

    @Test
    @Rollback
    public void propertySelectorOnLastName() throws Exception {
        PropertySelector<Account, String> lastNameSelector = PropertySelector.newPropertySelector(Account_.lastName);
        lastNameSelector.setSelected(Arrays.asList("Jagger", "Richards", "Jones", "Watts", "taylor", "Wyman", "Wood"));

        SearchParameters sp = new SearchParameters().property(lastNameSelector);

        List<Account> result = accountRepository.find(sp);
        Assert.assertThat(result.size(), is(3));
    }

    @Test
    @Rollback
    public void queryInvolvingManyToOne() throws Exception {
        Account example = new Account();
        example.setHomeAddress(new Address());
        example.getHomeAddress().setCity("Paris");
        List<Account> result = accountRepository.find(example);
        Assert.assertThat(result.size(), is(2));
    }
}