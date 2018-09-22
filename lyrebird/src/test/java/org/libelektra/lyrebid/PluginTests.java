package org.libelektra.lyrebid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.libelektra.Key.printKeyAndMeta;

class PluginTests {

    private static final String TEST_ROOT = "user/tests";

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    @Test
    public void runPathTest() throws IOException {
        Path path = Paths.get("/tmp/test-file.txt");
        if (!Files.exists(path)) Files.createFile(path);
        Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class).permissions();

        System.out.format("Permissions before: %s%n", PosixFilePermissions.toString(perms));

//        perms.add(PosixFilePermission.OWNER_WRITE);
//        perms.add(PosixFilePermission.OWNER_READ);
//        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_WRITE);
//        perms.add(PosixFilePermission.GROUP_READ);
//        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_WRITE);
//        perms.add(PosixFilePermission.OTHERS_READ);
//        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(path, perms);
        System.out.format("Permissions after:  %s%n", PosixFilePermissions.toString(perms));

        Key key = Key.create(TEST_ROOT);
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);

            Key pathKey = Key.create(TEST_ROOT + "/pathtest", path);
            pathKey.setMeta("check/path", "");
            pathKey.setMeta("check/permission/user", "wespe");
            pathKey.setMeta("check/permission/types", "rwx");
            set.append(pathKey);

            kdb.set(set, key);

            Plugin plugin = new Plugin("path", key);
            int resultCode = plugin.kdbSet(set, key);

            System.out.println("Returned Error Code: " + resultCode);

            KeySet.toString(set);
            printKeyAndMeta(key);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }

        Files.delete(path);
    }

    @AfterEach
    public void tearDown() {

    }

}