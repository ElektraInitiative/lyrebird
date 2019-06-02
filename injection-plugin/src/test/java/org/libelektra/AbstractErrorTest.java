package org.libelektra;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.libelektra.service.KDBService;

import java.util.Iterator;

import static org.libelektra.InjectionPlugin.ROOT_KEY;

public abstract class AbstractErrorTest {

    protected static KDBService kdbService;

    protected final static String TEST_NAMESPACE = "user/tests";
    protected final static String INJECT_NAMESPACE = "user/tests/inject";
    protected final static String APPLY_NAMESPACE = "user/tests/applied";

    @BeforeClass
    public static void init() throws KDB.KDBException {
        kdbService = new KDBService(TEST_NAMESPACE);
    }


    @After
    public void cleanUp() throws KDB.KDBException {
        KeySet allKeys = kdbService.getAllKeys();
        Iterator<Key> iterator = allKeys.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(TEST_NAMESPACE)) {
                iterator.remove();
            }
        }
        kdbService.set(allKeys, Key.create(TEST_NAMESPACE));
    }

    @AfterClass
    public static void close() {
        kdbService.close();
    }
}
