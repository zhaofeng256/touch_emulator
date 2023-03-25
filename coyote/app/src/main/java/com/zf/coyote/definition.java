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
    public static class MouseButton {
        public static int LEFT = 0;
        public static int RIGHT = 1;
        public static int MIDDLE = 2;
        public static int BACK = 3;
        public static int FORWARD = 4;
    }

    public static class WheelEvent {
        public static int ROLL_BACK = 0;
        public static int ROLL_FORWARD = 1;
    }

    public static int VK_ESC = 1;
    public static int VK_1 = 2;
    public static int VK_2 = 3;
    public static int VK_3 = 4;
    public static int VK_4 = 5;
    public static int VK_5 = 6;
    public static int VK_6 = 7;
    public static int VK_7 = 8;
    public static int VK_8 = 9;
    public static int VK_9 = 10;
    public static int VK_0 = 11;
    public static int VK_MINUS = 12;
    public static int VK_EQUAL = 13;
    public static int VK_BACKSPACE = 14;
    public static int VK_TAB = 15;
    public static int VK_Q = 16;
    public static int VK_W = 17;
    public static int VK_E = 18;
    public static int VK_R = 19;
    public static int VK_T = 20;
    public static int VK_Y = 21;
    public static int VK_A = 30;
    public static int VK_S = 31;
    public static int VK_D = 32;
    public static int VK_F = 33;
    public static int VK_G = 34;
    public static int VK_H = 35;
    public static int VK_J = 36;
    public static int VK_K = 37;
    public static int VK_L = 38;

    public static int VK_SHIFT = 42;
    public static int VK_Z = 44;
    public static int VK_X = 45;
    public static int VK_C = 46;
    public static int VK_V = 47;
    public static int VK_B = 48;
    public static int VK_N = 49;
    public static int VK_M = 50
            ;
    public static int VK_ALT = 56;
    public static int VK_SPACE = 57;
    public static int VK_CAPS = 58;
    public static int VK_F1 = 59;
    public static int VK_F2 = 60;
    public static int VK_F3 = 61;
    public static int VK_F4 = 62;
    public static int VK_F5 = 63;
    public static int VK_F6 = 64;
    public static int VK_F7 = 65;
    public static int VK_F8 = 66;
    public static int VK_F9 = 67;
    public static int VK_F10 = 68;

    public static int MOUSE_CODE = 260;
    public static int BT_LEFT = MOUSE_CODE + MouseButton.LEFT;
    public static int BT_RIGHT = MOUSE_CODE + MouseButton.RIGHT;
    public static int BT_MIDDLE = MOUSE_CODE + MouseButton.MIDDLE;
    public static int BT_BACK = MOUSE_CODE + MouseButton.BACK;
    public static int BT_FORWARD = MOUSE_CODE + MouseButton.FORWARD;
    public static int WHEEL_CODE = 280;
    public static int WL_BACK = WHEEL_CODE + WheelEvent.ROLL_BACK;
    public static int WL_FORWARD = WHEEL_CODE + WheelEvent.ROLL_FORWARD;

    public static int MV_CENTER = 290;
    public static int VIEW_START = 300;

    public static int[][] position = new int[][]{
            {VK_ALT,    1090, 660},
            {VK_SPACE,  1227, 510},
            {VK_CAPS,   1208, 644},
            {VK_G,      895, 642},
            {VK_R,      986, 618},
            {VK_1,      631, 630},
            {VK_2,      791, 634},
            {VK_3,      519, 633},
            {VK_4,      447, 635},
            {VK_5,      371, 642},
            {VK_6,      406, 587},
            {VK_B,      856, 481},
            {VK_E,      1204, 248},
            {VK_F,      650, 558},
            {VK_M,      1238, 56},
            {VK_Q,      870, 590},
            {VK_T,      981, 95},
            {VK_X,      1110, 228},
            {VK_Y,      981, 148},
            {VK_EQUAL,  986, 222},
            {VK_TAB,    63, 59},
            {VK_ESC,    979,36},
            {VK_F2,     1060,224},
            {BT_LEFT,   1090, 534},
            {BT_RIGHT,  1118, 389},
            {BT_BACK,   1003, 457},
            {WL_BACK,   631, 630},
            {WL_FORWARD,791, 634},
            {MV_CENTER, 243, 513},
            {VIEW_START,846, 264}

    };
    public static int MV_RADIUS = 89;
    public static int MV_UP = VK_W;
    public static int MV_LEFT = VK_A;
    public static int MV_DOWN = VK_S;
    public static int MV_RIGHT = VK_D;
    public static int MV_SPRINT = VK_SHIFT;
}
