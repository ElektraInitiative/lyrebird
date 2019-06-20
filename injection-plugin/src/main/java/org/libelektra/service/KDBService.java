package org.libelektra.service;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class KDBService {

    private final static Logger LOG = LoggerFactory.getLogger(KDBService.class);
    private KeySet allKeys;

    private KDB kdb;
    public static String ROOT = "system";
    public final String namespace;

    public KDBService() throws KDB.KDBException {
        this.namespace = ROOT;
        initKDB(ROOT);
    }

    public KDBService(String namespace) throws KDB.KDBException {
        this.namespace = namespace;
        initKDB(namespace);
    }

    private void initKDB(String namespace) throws KDB.KDBException {
        Key key = Key.create(namespace);
        kdb = KDB.open(key);
        allKeys = KeySet.create();
        kdb.get(allKeys, key);
    }

    public KeySet getAllKeys() {
        allKeys.rewind();
        return allKeys;
    }

    public KDB getInstance() {
        return kdb;
    }

    public KeySet getKeySetBelowPath(String path) {
        Key parentKey = Key.create(path);
        return allKeys.dup().cut(parentKey);
    }

    public void set(KeySet set, Key parentKey) throws KDB.KDBException {
        kdb.set(set, parentKey);
    }

    public void set(KeySet set, String parentKey) throws KDB.KDBException {
        kdb.set(set, Key.create(parentKey));
    }

    public void get(KeySet set, Key parentKey) throws KDB.KDBException {
        kdb.get(set, parentKey);
    }

    public void get(KeySet set, String parentKey) throws KDB.KDBException {
        kdb.get(set, Key.create(parentKey));
    }

    public Key lookup(String lookup) throws KDB.KDBException {
        return allKeys.lookup(lookup);
    }

    @PreDestroy
    public void close() {
        kdb.close();
    }

    /** DEBUG HELPERS **/

    public void printBelowPath(String path) {
        LOG.debug("Printing KeySet below {}", path);
        KeySet toPrint = getKeySetBelowPath(path);
        KeySet.printKeySet(toPrint);
    }
}
