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
    private final KeySet allKeys;

    private KDB kdb;
    public static String ROOT = "system";

    public KDBService() throws KDB.KDBException {
        Key key = Key.create(ROOT);
        kdb = KDB.open(key);
        allKeys = KeySet.create();
        kdb.get(allKeys, key);
    }

    public KDB getInstance() {
        return kdb;
    }

    public void printBelowPath(String path) {
        LOG.debug("Printing KeySet below {}", path);
        KeySet toPrint = getKeySetBelowPath(path);
        KeySet.printKeySet(toPrint);
    }

    public KeySet getKeySetBelowPath(String path) {
        Key parentKey = Key.create(path);
        return allKeys.dup().cut(parentKey);
    }

    @PreDestroy
    public void cleanUp() {
        kdb.close();
    }
}
