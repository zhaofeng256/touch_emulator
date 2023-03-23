package com.zf.coyote;

public class definition {
    public static String V_ID = "id";
    public static String V_TYPE = "type";
    public static String V_PARAM1 = "param1";
    public static String V_PARAM2 = "param2";
    public static String V_CHECKSUM = "checksum";


    public static class EventType {
        public static int TYPE_KEYBOARD = 0;
        public static int TYPE_MOUSE = 1;
        public static int TYPE_BUTTON = 2;
        public static int TYPE_WHEEL = 3;
    }

    public static class KeyEvent {
        public static int KEY_UP = 0;
        public static int KEY_DOWN = 1;
    }

    public static int VK_W = 17;
    public static int VK_A = 30;
    public static int VK_S = 31;
    public static int VK_D = 32;
}
