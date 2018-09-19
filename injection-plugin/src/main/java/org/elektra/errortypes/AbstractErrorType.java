package org.elektra.errortypes;

import org.libelektra.Elektra;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;

public class AbstractErrorType {

    protected KDB kdb;
    protected KeySet currentKeySet;

    protected void openKDB(Key parentKey) throws KDB.KDBException {
        kdb = KDB.open(parentKey);
        currentKeySet = KeySet.create();
        kdb.get(currentKeySet, parentKey);
    }
}
