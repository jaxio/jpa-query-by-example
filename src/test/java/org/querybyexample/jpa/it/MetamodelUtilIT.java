package org.querybyexample.jpa.it;

import static org.fest.assertions.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.querybyexample.jpa.MetamodelUtil;
import org.querybyexample.jpa.app.Account;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" })
@Transactional
public class MetamodelUtilIT {

    @Inject
    private MetamodelUtil metamodelUtil;

    @Test(expected = IllegalArgumentException.class)
    public void unknowThrowsException() {
        metamodelUtil.toAttributes("unknown", Account.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void knownPathOnWrongClassThrowsException() {
        metamodelUtil.toAttributes("city", Account.class);
    }

    @Test
    public void knownPathOnValid() {
        assertThat(metamodelUtil.toAttributes("username", Account.class)).hasSize(1);
    }

    @Test
    public void knownCompositePathOnValid() {
        assertThat(metamodelUtil.toAttributes("homeAddress.city", Account.class)).hasSize(2);
    }
}
