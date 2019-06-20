package org.libelektra;

import com.sun.tools.javac.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libelektra.errortypes.*;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InjectionPluginTest {

    @Autowired
    private InjectionPlugin injectionPlugin;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getAllPossibleInjections() {
        Key key = Key.create("user/tests");
        key.setMeta("types", "3, 5");
        key.setMeta("inject/resource/#0", "/devvv/not_existing");
        key.setMeta("inject/resource/#1", "/etc/shadow");
        key.setMeta("inject/domain/#0", "eth0");
        Collection<AbstractErrorType> metaData = injectionPlugin.getAllPossibleInjections(key);
        assertThat(metaData, hasItem(isA(TypoError.class)));
        assertThat(metaData, hasItem(isA(StructureError.class)));
        assertThat(metaData, hasItem(isA(DomainError.class)));
        assertThat(metaData, hasItem(isA(ResourceError.class)));
        assertThat(metaData, not(hasItem(isA(LimitError.class))));
        assertThat(metaData, not(hasItem(isA(SemanticError.class))));
    }
}