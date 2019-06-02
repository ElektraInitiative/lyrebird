package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.*;
import org.libelektra.service.KDBService;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class InjectionPluginTest {

    private InjectionPlugin injectionPlugin;


    @Before
    public void setUp() throws Exception {
        KDBService kdbService = new KDBService();
        injectionPlugin = new InjectionPlugin(
                mock(StructureError.class),
                mock(TypoError.class),
                mock(SemanticError.class),
                mock(ResourceError.class),
                mock(DomainError.class),
                mock(LimitError.class), kdbService);
    }

    @Test
    public void getAllPossibleInjections() {
        Key key = Key.create("user/tests");
        key.setMeta("types", "3, 5");
        key.setMeta("inject/resource/#0", "/devvv/not_existing");
        key.setMeta("inject/resource/#1", "/etc/shadow");
        key.setMeta("inject/domain/#0", "eth0");
        Collection<InjectionMeta> metaData = injectionPlugin.getAllPossibleInjections(key);
        assertThat(metaData, hasItem(isA(TypoError.Metadata.class)));
        assertThat(metaData, hasItem(isA(StructureError.Metadata.class)));
        assertThat(metaData, hasItem(isA(DomainError.Metadata.class)));
        assertThat(metaData, hasItem(isA(ResourceError.Metadata.class)));
        assertThat(metaData, not(hasItem(isA(LimitError.Metadata.class))));
        assertThat(metaData, not(hasItem(isA(SemanticError.Metadata.class))));
    }
}