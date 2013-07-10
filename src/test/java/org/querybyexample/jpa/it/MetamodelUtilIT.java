package org.querybyexample.jpa.it;

import static org.fest.assertions.Assertions.assertThat;
import static org.querybyexample.jpa.MetamodelUtil.toAttributes;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.app.Account;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
public class MetamodelUtilIT {

    @Transactional
    @Test
    public void initMetamodelUsingTransactionAnnotation() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknowThrowsException() {
        toAttributes("unknown", Account.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void knownPathOnWrongClassThrowsException() {
        toAttributes("city", Account.class);
    }

    @Test
    public void simplePath() {
        assertThat(toAttributes("username", Account.class)).hasSize(1);
    }

    @Test
    public void compositePath() {
        assertThat(toAttributes("homeAddress.city", Account.class)).hasSize(2);
    }

    @Test
    @Ignore("To be fixed")
    public void compositePathFails() {
        assertThat(toAttributes("roles.roleName", Account.class)).hasSize(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCompositePathThrowsException() {
        assertThat(toAttributes("homeAddress.unknown", Account.class)).hasSize(2);
    }
}
