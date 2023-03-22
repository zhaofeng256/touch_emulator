package com.zf.coyote;

import java.util.Arrays;
import java.util.HashMap;

public class cStruct {
    HashMap<String, descriptor> names = new HashMap<>();
    int size;

    public static class descriptor {
        int size;
        byte[] data;
        int offset;

        public descriptor(int size) {
            this.size = size;
            this.data = new byte[size];
        }
    }

    public cStruct(field[] fs) {
        for (field f : fs) {
            descriptor d = new descriptor(f.size);
            d.offset = this.size;
            this.size += f.size;
            names.put(f.name, d);
        }
    }
    public byte[] _get(String name) {
        descriptor f = names.get(name);
        assert f != null;
        return Arrays.copyOf(f.data, f.size);
    }

    public int get(String name) {
        descriptor f = names.get(name);
        assert f != null;
        if (f.size == 4) {
            return ((f.data[3] & 0xFF) << 24) |
                    ((f.data[2] & 0xFF) << 16) |
                    ((f.data[1] & 0xFF) << 8) |
                    (f.data[0] & 0xFF);
        } else if (f.size == 2) {
            return (((f.data[1] & 0xFF) << 8) | (f.data[0] & 0xFF)) & 0xFFFF;
        } else if (f.size == 1) {
            return f.data[0] & 0xFF;
        }
        return 0;
    }

    public void set(String name, byte[] data) {
        descriptor f = names.get(name);
        assert f != null;
        if (f.size >= 0) System.arraycopy(data, 0, f.data, 0, f.size);
    }

    public int offset(String name) {
        descriptor f = names.get(name);
        assert f != null;
        return f.offset;
    }

    public int size(String name) {
        descriptor f = names.get(name);
        assert f != null;
        return f.size;
    }


}
