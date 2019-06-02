package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.DomainError;
import org.libelektra.errortypes.ResourceError;
import org.libelektra.errortypes.StructureError;
import org.libelektra.errortypes.TypoError;
import org.libelektra.service.KDBService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.*;

public class InjectionPluginTest {

    private InjectionPlugin injectionPlugin;

    @Before
    public void setUp() throws Exception {
        KDBService kdbService = new KDBService();
        injectionPlugin = new InjectionPlugin(kdbService);
    }

    @Test
    public void getAllPossibleInjections() {
        Key key = Key.create("user/tests");
        key.setMeta("types", "3, 5");
        Collection<InjectionMeta> metaData = injectionPlugin.getAllPossibleInjections(key);
        assertThat(metaData, hasItem(isA(TypoError.Metadata.class)));
        assertThat(metaData, hasItem(isA(StructureError.Metadata.class)));
        assertThat(metaData, hasItem(isA(DomainError.Metadata.class)));
        assertThat(metaData, hasItem(isA(ResourceError.Metadata.class)));
    }
}