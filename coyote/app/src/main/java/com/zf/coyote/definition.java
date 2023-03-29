package com.zf.coyote;

import static com.zf.coyote.definition.MotionType.MOTION_DRAG;
import static com.zf.coyote.definition.MotionType.MOTION_NONE;
import static com.zf.coyote.definition.MotionType.MOTION_SYNC;
import static com.zf.coyote.definition.MotionType.MOTION_TAP;
import static com.zf.coyote.definition.MotionType.MOTION_TRANS;

import java.util.HashMap;

public class definition {
    public static String V_ID = "id";
    public static String V_TYPE = "type";
    public static String V_PARAM1 = "param1";
    public static String V_PARAM2 = "param2";
    public static String V_CHECKSUM = "checksum";


    public static class EventType {
        public static int TYPE_KEYBOARD = 0;
        public static int TYPE_MOUSE_AXIS = 1;
        public static int TYPE_MOUSE_BUTTON = 2;
        public static int TYPE_MOUSE_WHEEL = 3;
        public static int TYPE_CONTROL = 4;

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
        public static int MAX_MOUSE_BUTTONS = 5;
    }

    public static class WheelEvent {
        public static int ROLL_BACK = 0;
        public static int ROLL_FORWARD = 1;
    }


    public static class ControlType {
        public static int MAIN_MODE = 0;
        public static int SUB_MODE = 1;
        public static int MAP_MODE = 2;
        public static int TRANSPARENT_MODE = 3;
    }
    public static class MapModeStatus {
        public static int MAP_MODE_OFF = 0;
        public static int MAP_MODE_ON = 1;
    }

    public static class TransPointStatus {
        public static int TRANSPARENT_OFF = 0;
        public static int TRANSPARENT_ON = 1;
    }

    public static class SubModeType {

        public static int NONE_SUB_MODE = 0;
        public static int SUB_MODE_OFFSET = 1;
        public static int DRIVE_MOTO = 1;
        public static int DRIVE_CHOPPER = 2;
        public static int DRIVE_COYOTE = 3;
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
    public static int VK_M = 50;
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

    static class ActionType {

        public static int ACT_DOWN = 0;
        public static int ACT_UP = 1;
        public static int ACT_MOVE = 2;
        public static int ACT_DELAY = 3;
        public static int ACT_TAP = 4;
    }

    static class MotionType {

        public static int MOTION_NONE = 0;
        public static int MOTION_TAP = 1;
        public static int MOTION_SYNC = 2;
        public static int MOTION_DRAG = 3;
        public static int MOTION_COMB = 4;
        public static int MOTION_TRANS = 5;
    }

    public static class KeyMotions {
        String description;
        int type;
        int[][] moves;

        KeyMotions(String s, int t, int[][] m) {
            this.description = s;
            this.type = t;
            this.moves = m;
  /*          if (t == MOTION_COMB) {
                this.moves = new int[m.length][3];
                for(int i = 0; i<m.length; i++)
                    this.moves[i] = m[i].clone();
            }*/

        }
    }


    public static HashMap<Integer, KeyMotions> map_multiplayer = new HashMap<>() {{
        put(VK_ALT, new KeyMotions("", MOTION_TAP, new int[][]{{1090, 660}}));
        put(VK_SPACE, new KeyMotions("", MOTION_TAP, new int[][]{{1227, 510}}));
        put(VK_CAPS, new KeyMotions("", MOTION_TAP, new int[][]{{1208, 644}}));
        put(VK_G, new KeyMotions("", MOTION_TAP, new int[][]{{895, 642}}));
        put(VK_R, new KeyMotions("", MOTION_TAP, new int[][]{{986, 618}}));
        put(VK_1, new KeyMotions("", MOTION_TAP, new int[][]{{631, 630}}));
        put(VK_2, new KeyMotions("", MOTION_TAP, new int[][]{{791, 634}}));
        put(VK_3, new KeyMotions("", MOTION_TAP, new int[][]{{519, 633}}));
        put(VK_4, new KeyMotions("", MOTION_TAP, new int[][]{{447, 635}}));
        put(VK_5, new KeyMotions("", MOTION_TAP, new int[][]{{371, 642}}));
        put(VK_6, new KeyMotions("", MOTION_TAP, new int[][]{{406, 587}}));
        put(VK_B, new KeyMotions("", MOTION_TAP, new int[][]{{856, 481}}));
        put(VK_E, new KeyMotions("", MOTION_TAP, new int[][]{{1204, 248}}));
        put(VK_F, new KeyMotions("", MOTION_TAP, new int[][]{{650, 558}}));
        put(VK_M, new KeyMotions("", MOTION_TAP, new int[][]{{1238, 56}}));
        put(VK_Q, new KeyMotions("", MOTION_TAP, new int[][]{{870, 590}}));
        put(VK_T, new KeyMotions("", MOTION_TAP, new int[][]{{981, 95}}));
        put(VK_X, new KeyMotions("", MOTION_TAP, new int[][]{{1110, 228}}));
        put(VK_Y, new KeyMotions("", MOTION_TAP, new int[][]{{981, 148}}));
        put(VK_EQUAL, new KeyMotions("", MOTION_TAP, new int[][]{{986, 222}}));
        put(VK_TAB, new KeyMotions("", MOTION_TAP, new int[][]{{63, 59}}));
        put(VK_ESC, new KeyMotions("", MOTION_TAP, new int[][]{{979, 36}}));
        put(VK_F2, new KeyMotions("", MOTION_TAP, new int[][]{{1060, 224}}));
        put(BT_LEFT, new KeyMotions("fire", MOTION_SYNC, new int[][]{{1090, 534}}));
        put(BT_RIGHT, new KeyMotions("scope", MOTION_SYNC, new int[][]{{1118, 389}}));
        put(BT_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{1003, 457}}));
        put(WL_FORWARD, new KeyMotions("", MOTION_TAP, new int[][]{{631, 630}}));
        put(WL_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{791, 634}}));
        put(MV_CENTER, new KeyMotions("move center", MOTION_NONE, new int[][]{{243, 513}}));
        put(VIEW_START, new KeyMotions("view start", MOTION_NONE, new int[][]{{846, 264}}));
    }};

    public static HashMap<Integer, KeyMotions> map_battle_ground = new HashMap<>() {{
        put(VK_ALT, new KeyMotions("", MOTION_TAP, new int[][]{{1092, 660}}));
        put(VK_SPACE, new KeyMotions("", MOTION_TAP, new int[][]{{1230, 507}}));
        put(VK_CAPS, new KeyMotions("", MOTION_TAP, new int[][]{{1205, 644}}));
        put(VK_R, new KeyMotions("", MOTION_TAP, new int[][]{{700, 670}}));
        put(VK_1, new KeyMotions("", MOTION_TAP, new int[][]{{557, 620}}));
        put(VK_2, new KeyMotions("", MOTION_TAP, new int[][]{{791, 620}}));
        put(VK_3, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 817, 618}}));
        put(VK_4, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 825, 618}}));
        put(VK_5, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 831, 625}}));
        put(VK_6, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 829, 633}}));
        put(VK_7, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 813, 633}}));
        put(VK_8, new KeyMotions("", MOTION_DRAG, new int[][]{{821, 627, 811, 625}}));
        put(VK_9, new KeyMotions("", MOTION_DRAG, new int[][]{{451, 624, 441, 624}}));
        put(VK_0, new KeyMotions("", MOTION_DRAG, new int[][]{{451, 624, 451, 614}}));
        put(VK_MINUS, new KeyMotions("", MOTION_DRAG, new int[][]{{451, 624, 461, 624}}));
        put(VK_T, new KeyMotions("", MOTION_TAP, new int[][]{{450, 620}}));
        put(VK_TAB, new KeyMotions("", MOTION_TAP, new int[][]{{375, 617}}));
        put(VK_E, new KeyMotions("", MOTION_TAP, new int[][]{{1220, 260}}));
        put(VK_F, new KeyMotions("", MOTION_TAP, new int[][]{{792, 356}}));//502 347  //534 475f //876 402 DRIVE //door 690 400
        put(VK_G, new KeyMotions("", MOTION_TAP, new int[][]{{792, 413}}));//745 347 //879 516 sit CAR
        put(VK_H, new KeyMotions("", MOTION_TAP, new int[][]{{792, 490}}));//502 431
        put(VK_Q, new KeyMotions("", MOTION_TAP, new int[][]{{974, 438}}));//surf
        put(VK_C, new KeyMotions("", MOTION_TAP, new int[][]{{1239, 51}}));
        put(VK_T, new KeyMotions("", MOTION_TAP, new int[][]{{981, 95}}));
        put(VK_X, new KeyMotions("", MOTION_TAP, new int[][]{{1110, 228}}));
        put(VK_Y, new KeyMotions("", MOTION_TAP, new int[][]{{981, 148}}));
        put(VK_EQUAL, new KeyMotions("", MOTION_TAP, new int[][]{{986, 222}}));
        put(VK_ESC, new KeyMotions("", MOTION_TAP, new int[][]{{981, 38}}));
        put(VK_F2, new KeyMotions("", MOTION_TAP, new int[][]{{1047, 227}}));
        put(VK_F4, new KeyMotions("", MOTION_TAP, new int[][]{{921, 41}}));
        put(BT_LEFT, new KeyMotions("", MOTION_SYNC, new int[][]{{1090, 534}}));
        put(BT_RIGHT, new KeyMotions("", MOTION_SYNC, new int[][]{{1216, 366}}));
        put(BT_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{1003, 457}}));
        put(WL_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{696, 622}}));
        put(WL_FORWARD, new KeyMotions("", MOTION_TAP, new int[][]{{545, 622}}));
        put(MV_CENTER, new KeyMotions("", MOTION_NONE, new int[][]{{259, 528}}));
        put(VIEW_START, new KeyMotions("", MOTION_NONE, new int[][]{{743, 409}}));
    }};

    public static HashMap<Integer, KeyMotions> map_pve = new HashMap<>() {{
        put(VK_ALT, new KeyMotions("", MOTION_TAP, new int[][]{{1090, 660}}));
        put(VK_SPACE, new KeyMotions("", MOTION_TAP, new int[][]{{1227, 510}}));
        put(VK_CAPS, new KeyMotions("", MOTION_TAP, new int[][]{{1208, 644}}));
        put(VK_G, new KeyMotions("", MOTION_TAP, new int[][]{{895, 642}}));
        put(VK_R, new KeyMotions("", MOTION_TAP, new int[][]{{986, 618}}));
        put(VK_1, new KeyMotions("", MOTION_TAP, new int[][]{{631, 630}}));
        put(VK_2, new KeyMotions("", MOTION_TAP, new int[][]{{791, 634}}));
        put(VK_3, new KeyMotions("", MOTION_TAP, new int[][]{{519, 633}}));
        put(VK_4, new KeyMotions("", MOTION_TAP, new int[][]{{447, 635}}));
        put(VK_5, new KeyMotions("", MOTION_TAP, new int[][]{{371, 642}}));
        put(VK_6, new KeyMotions("", MOTION_TAP, new int[][]{{406, 587}}));
        put(VK_B, new KeyMotions("", MOTION_TAP, new int[][]{{856, 481}}));
        put(VK_E, new KeyMotions("", MOTION_TAP, new int[][]{{1204, 248}}));
        put(VK_F, new KeyMotions("", MOTION_TAP, new int[][]{{650, 558}}));
        put(VK_M, new KeyMotions("", MOTION_TAP, new int[][]{{1238, 56}}));
        put(VK_Q, new KeyMotions("", MOTION_TAP, new int[][]{{870, 590}}));
        put(VK_T, new KeyMotions("", MOTION_TAP, new int[][]{{981, 95}}));
        put(VK_X, new KeyMotions("", MOTION_TAP, new int[][]{{1110, 228}}));
        put(VK_Y, new KeyMotions("", MOTION_TAP, new int[][]{{981, 148}}));
        put(VK_EQUAL, new KeyMotions("", MOTION_TAP, new int[][]{{986, 222}}));
        put(VK_TAB, new KeyMotions("", MOTION_TAP, new int[][]{{63, 59}}));
        put(VK_ESC, new KeyMotions("", MOTION_TAP, new int[][]{{979, 36}}));
        put(VK_F2, new KeyMotions("", MOTION_TAP, new int[][]{{1060, 224}}));
        put(BT_LEFT, new KeyMotions("fire", MOTION_SYNC, new int[][]{{1090, 534}}));
        put(BT_RIGHT, new KeyMotions("scope", MOTION_SYNC, new int[][]{{1118, 389}}));
        put(BT_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{1003, 457}}));
        put(WL_FORWARD, new KeyMotions("", MOTION_TAP, new int[][]{{631, 630}}));
        put(WL_BACK, new KeyMotions("", MOTION_TAP, new int[][]{{791, 634}}));
        put(MV_CENTER, new KeyMotions("move center", MOTION_NONE, new int[][]{{243, 513}}));
        put(VIEW_START, new KeyMotions("view start", MOTION_NONE, new int[][]{{846, 264}}));
    }};

    public static HashMap<Integer, KeyMotions> map_moto = new HashMap<>() {{
        put(VK_Q, new KeyMotions("head up", MOTION_SYNC, new int[][]{{949, 409}}));
        put(VK_E, new KeyMotions("head down", MOTION_SYNC, new int[][]{{949, 540}}));
        put(VK_SPACE, new KeyMotions("stop", MOTION_SYNC, new int[][]{{1071, 405}}));
        put(VK_G, new KeyMotions("beep", MOTION_SYNC, new int[][]{{1233, 515}}));
        put(VK_F, new KeyMotions("off", MOTION_TAP, new int[][]{{1207, 270}}));
    }};


    public static HashMap<Integer, KeyMotions> map_chopper = new HashMap<>() {{
        put(BT_LEFT, new KeyMotions("up", MOTION_SYNC, new int[][]{{1133, 391}}));
        put(BT_RIGHT, new KeyMotions("down", MOTION_SYNC, new int[][]{{1134, 567}}));
        put(VK_E, new KeyMotions("hot", MOTION_TAP, new int[][]{{989, 439}}));
        put(VK_F, new KeyMotions("off", MOTION_TAP, new int[][]{{1207, 270}}));
    }};


    public static HashMap<Integer, KeyMotions> map_coyote = new HashMap<>() {{
        put(BT_LEFT, new KeyMotions("fire", MOTION_SYNC, new int[][]{{936, 491}}));
        put(BT_RIGHT, new KeyMotions("missile", MOTION_TAP, new int[][]{{954, 611}}));
        put(VK_SPACE, new KeyMotions("stop", MOTION_SYNC, new int[][]{{1026, 415}}));
        put(VK_E, new KeyMotions("hot", MOTION_TAP, new int[][]{{1162, 415}}));
        put(VK_F, new KeyMotions("off", MOTION_TAP, new int[][]{{1207, 270}}));
    }};


    public static HashMap<Integer, KeyMotions> map_map_mode = new HashMap<>() {{
        put(BT_LEFT, new KeyMotions("transparent", MOTION_TRANS, new int[][]{{0, 0}}));
        put(WL_BACK, new KeyMotions("zoom in", MOTION_TAP, new int[][]{{1239, 133}}));
        put(WL_FORWARD, new KeyMotions("zoom out", MOTION_TAP, new int[][]{{1239, 649}}));
        put(BT_RIGHT, new KeyMotions("delete mark", MOTION_TAP, new int[][]{{858, 660}}));
        put(BT_MIDDLE, new KeyMotions("center", MOTION_TAP, new int[][]{{1167, 660}}));
        put(BT_FORWARD, new KeyMotions("self mark", MOTION_TAP, new int[][]{{1041, 660}}));
    }};

    public static HashMap<Integer, KeyMotions> map_transparent_mode = new HashMap<>() {{
        put(BT_LEFT, new KeyMotions("transparent", MOTION_TRANS, new int[][]{{0, 0}}));
    }};

    public static int[][] position1 = new int[][]{
            {VK_ALT, 1092, 660},
            {VK_SPACE, 1230, 507},
            {VK_CAPS, 1205, 644},
            {VK_R, 700, 670},
            {VK_1, 557, 620},
            {VK_2, 791, 620},
            {VK_T, 450, 620},
            {VK_TAB, 375, 617},
            {VK_E, 1220, 260},
            {VK_F, 792, 356},//502 347  //534 475f //876 402 DRIVE //door 690 400
            {VK_G, 792, 413},//745 347 //879 516 sit CAR
            {VK_H, 792, 490},//502 431
            {VK_Q, 974, 438},//surf
            // {VK_Q,      500, 500}, SEARCH
            // {VK_E,      775, 500},
            {VK_C, 1238, 56},
            {VK_T, 981, 95},
            {VK_X, 1110, 228},
            {VK_Y, 981, 148},
            {VK_EQUAL, 986, 222},
            {VK_ESC, 981, 38},
            {VK_F2, 1047, 227},
            {VK_F4, 921, 41},
            {BT_LEFT, 1090, 534},
            {BT_RIGHT, 1216, 366},
            {BT_BACK, 1003, 457},
            {WL_BACK, 696, 622},
            {WL_FORWARD, 545, 622},
            {MV_CENTER, 259, 528},
            {VIEW_START, 743, 409}
            //820 630 // 814 638 //


            //transparent control


            //moto // surf // car // fly moto // boat
            //1069 401  stop // 1234 516 beep // 1210 647 switch site // 1072 554 speed
            //947 408 up    //  947 540 down == surf jump //   ???  get off

            //chopper
            //989 439 hot //1210 647 switch sit //1133 391 up //1134 567 down

            //coyote
            //1207 270 get off // 936 491 fire //1026 415 stop // 1162 415 hot // 954 611 missile //1072 554 speed //1210 647 switch site

            //map
            //wheel->zoom in / zoom out /move-> drag / ctrl + click -> mark / center me / delete mark /self mark

            //widget list  wheel  roll up/down
            //游泳 浮 1166 541 沉 1092 660 == VK_ALT
            //tank

            //技能机枪
            // 796 359 放置 开镜 登上 撤下
            // rpd 过热枪管 开镜  //机甲
            //开门 + 物质列表

    };

    public static int[][] position2 = new int[][]{
            {VK_ALT, 1090, 660},
            {VK_SPACE, 1227, 510},
            {VK_CAPS, 1208, 644},
            {VK_G, 895, 642},
            {VK_R, 986, 618},
            {VK_1, 631, 630},
            {VK_2, 791, 634},
            {VK_3, 519, 633},
            {VK_4, 447, 635},
            {VK_5, 371, 642},
            {VK_6, 406, 587},
            {VK_B, 856, 481},
            {VK_E, 1204, 248},
            {VK_F, 650, 558},
            {VK_M, 1238, 56},
            {VK_Q, 870, 590},
            {VK_T, 981, 95},
            {VK_X, 1110, 228},
            {VK_Y, 981, 148},
            {VK_EQUAL, 986, 222},
            {VK_TAB, 63, 59},
            {VK_ESC, 979, 36},
            {VK_F2, 1060, 224},
            {BT_LEFT, 1090, 534},
            {BT_RIGHT, 1118, 389},
            {BT_BACK, 1003, 457},
            {WL_BACK, 631, 630},
            {WL_FORWARD, 791, 634},
            {MV_CENTER, 243, 513},
            {VIEW_START, 846, 264}

    };
    public static int MV_RADIUS = 89;
    public static int MV_UP = VK_W;
    public static int MV_LEFT = VK_A;
    public static int MV_DOWN = VK_S;
    public static int MV_RIGHT = VK_D;
    public static int MV_SPRINT = VK_SHIFT;
}
