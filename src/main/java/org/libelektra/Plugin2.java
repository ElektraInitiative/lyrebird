package org.libelektra;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.ArrayList;
import java.util.List;

public class Plugin2 extends Structure {

    public interface KdbOpen extends Callback {
        int invoke(Plugin2 plugin2, Pointer errorKey);
    }

    public interface KdbClose extends Callback {
        int invoke(Plugin2 plugin2, Pointer errorKey);
    }
    public interface KdbGet extends Callback {
        int invoke(Plugin2 handle, Pointer returned, Pointer parentKey);
    }
    public interface KdbSet extends Callback {
        int invoke(Plugin2 handle, Pointer returned, Pointer parentKey);
    }

    public interface KdbError extends Callback {
        int invoke(Plugin2 handle, Pointer returned, Pointer parentKey);
    }

    public Pointer config;
    public KdbOpen kdbOpen;
    public KdbClose kdbClose;
    public KdbGet kdbGet;
    public KdbSet kdbSet;
    public KdbError kdbError;
    public String name;


    @Override
    protected List<String> getFieldOrder() {
        List<String> list = new ArrayList<>();
        list.add("config");
        list.add("kdbOpen");
        list.add("kdbClose");
        list.add("kdbGet");
        list.add("kdbSet");
        list.add("kdbError");
        list.add("name");
//            return new ArrayList<>();
        return list;
    }
}
