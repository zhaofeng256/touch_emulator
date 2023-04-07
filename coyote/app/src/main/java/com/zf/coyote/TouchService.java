package com.zf.coyote;

import static com.zf.coyote.definition.ActionType.ACT_DELAY;
import static com.zf.coyote.definition.ActionType.ACT_DOWN;
import static com.zf.coyote.definition.ActionType.ACT_MOVE;
import static com.zf.coyote.definition.ActionType.ACT_TAP;
import static com.zf.coyote.definition.ActionType.ACT_UP;
import static com.zf.coyote.definition.BT_LEFT;
import static com.zf.coyote.definition.ControlType.MAIN_MODE;
import static com.zf.coyote.definition.ControlType.MAP_MODE;
import static com.zf.coyote.definition.ControlType.SUB_MODE;
import static com.zf.coyote.definition.ControlType.TRANSPARENT_MODE;
import static com.zf.coyote.definition.EventType;
import static com.zf.coyote.definition.KeyEvent;
import static com.zf.coyote.definition.MOUSE_BUTTON_OFFSET;
import static com.zf.coyote.definition.MOUSE_WHEEL_OFFSET;
import static com.zf.coyote.definition.MV_CENTER;
import static com.zf.coyote.definition.MV_DOWN;
import static com.zf.coyote.definition.MV_LEFT;
import static com.zf.coyote.definition.MV_RADIUS;
import static com.zf.coyote.definition.MV_RIGHT;
import static com.zf.coyote.definition.MV_SPRINT;
import static com.zf.coyote.definition.MV_UP;
import static com.zf.coyote.definition.MapModeStatus.MAP_MODE_OFF;
import static com.zf.coyote.definition.MapModeStatus.MAP_MODE_ON;
import static com.zf.coyote.definition.MotionType.MOTION_COMB;
import static com.zf.coyote.definition.MotionType.MOTION_DRAG;
import static com.zf.coyote.definition.MotionType.MOTION_SYNC;
import static com.zf.coyote.definition.MotionType.MOTION_TAP;
import static com.zf.coyote.definition.MotionType.MOTION_TRANS;
import static com.zf.coyote.definition.MouseButton.MAX_MOUSE_BUTTONS;
import static com.zf.coyote.definition.SettingType.WINDOW_POS;
import static com.zf.coyote.definition.SettingType.WINDOW_SIZE;
import static com.zf.coyote.definition.SubModeType.NONE_SUB_MODE;
import static com.zf.coyote.definition.SubModeType.SUB_MODE_OFFSET;
import static com.zf.coyote.definition.TransPointStatus.TRANSPARENT_OFF;
import static com.zf.coyote.definition.TransPointStatus.TRANSPARENT_ON;
import static com.zf.coyote.definition.VIEW_START;
import static com.zf.coyote.definition.V_ID;
import static com.zf.coyote.definition.V_PARAM1;
import static com.zf.coyote.definition.V_PARAM2;
import static com.zf.coyote.definition.V_TYPE;
import static com.zf.coyote.definition.map_battle_ground;
import static com.zf.coyote.definition.map_chopper;
import static com.zf.coyote.definition.map_coyote;
import static com.zf.coyote.definition.map_map_mode;
import static com.zf.coyote.definition.map_moto;
import static com.zf.coyote.definition.map_multiplayer;
import static com.zf.coyote.definition.map_pve;
import static com.zf.coyote.definition.map_transparent_mode;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class TouchService extends Service {
    public static class Point {
        public float x;
        public float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Point() {
        }
    }

    public static final String TAG = TouchService.class.getSimpleName();
    static Instrumentation mInst = new Instrumentation();
    Point point_w, point_a, point_s, point_d, point_aw, point_dw, point_as, point_ds;
    Point point_sprint_w, point_sprint_aw, point_sprint_dw;
    Point pointMoveStart, pointMoveEnd, pointMoveNow;
    Point point_touch_start;
    Point point_key_drag_start = new Point(0, 0);
    Point point_touch_end = new Point(), point_mouse_start = new Point(), point_mouse_end = new Point();
    long mouse_timeout;
    boolean mouse_pressed = false;
    byte moveKeyStatus;
    int MAX_SLOT = 10;
    boolean[] slots = new boolean[MAX_SLOT];
    Point[] point_slots = new Point[MAX_SLOT];
    MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[MAX_SLOT];
    MotionEvent.PointerCoords[] pointerCoordinates = new MotionEvent.PointerCoords[MAX_SLOT];
    int slotStep;
    int mouse_slot;
    int[] mouse_button_req_id = new int[MAX_MOUSE_BUTTONS];
    int sprint_status;
    int step_len;

    public static final Object sync_key_touch = new Object();
    public static final Object sync_key_move = new Object();
    public static final Object sync_key_release = new Object();
    public static final Object sync_key_slow_motions = new Object();
    boolean start_move;

    static class TouchData {
        int id;
        int action;
        Point p;
    }

    int req_id;
    public int data_id;
    int main_mode = 0;
    int sub_mode = NONE_SUB_MODE;
    HashMap<Integer, Integer> map_id_slot = new HashMap<>();

    public static ArrayList<TouchData> data_list_touch = new ArrayList<>();

    ArrayList<definition.KeyMotions> slowMotionsList = new ArrayList<>();
    ArrayList<HashMap<Integer, definition.KeyMotions>> key_maps = new ArrayList<>();
    ArrayList<HashMap<Integer, definition.KeyMotions>> sub_battle_ground = new ArrayList<>();
    ArrayList<HashMap<Integer, Integer>> list_keyboard_sync_req_id = new ArrayList<>();
    HashMap<HashMap<Integer, definition.KeyMotions>, ArrayList<HashMap<Integer, definition.KeyMotions>>> hash_main_sub = new HashMap<>();
    HashMap<Integer, definition.KeyMotions> hash_alter_keys_pos = new HashMap<>();
    boolean map_mode_on;
    boolean transparent_mode_on;
    boolean key_drag_mode_on;

    public static int[] display_size = {0, 0};
    int[] window_pos = {0, 0};
    int[] window_size = {0, 0};

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void initPointers(int i) {

        HashMap<Integer, definition.KeyMotions> key_map = key_maps.get(i);

        definition.KeyMotions motions = key_map.get(MV_CENTER);

        synchronized (sync_key_move) {
            if (motions != null)
                pointMoveStart = new Point(motions.moves[0][0], motions.moves[0][1]);
            else pointMoveStart = new Point(243, 513);


            pointMoveEnd = new Point(pointMoveStart.x, pointMoveStart.y);
            pointMoveNow = new Point(pointMoveStart.x, pointMoveStart.y);
        }

        point_w = new Point(pointMoveStart.x, pointMoveStart.y - MV_RADIUS);
        point_a = new Point(pointMoveStart.x - MV_RADIUS, pointMoveStart.y);
        point_s = new Point(pointMoveStart.x, pointMoveStart.y + MV_RADIUS);
        point_d = new Point(pointMoveStart.x + MV_RADIUS, pointMoveStart.y);

        float f = (float) Math.sqrt(2) * MV_RADIUS / 2;
        point_aw = new Point(pointMoveStart.x - f, pointMoveStart.y - f);
        point_dw = new Point(pointMoveStart.x + f, pointMoveStart.y - f);
        point_as = new Point(pointMoveStart.x - f, pointMoveStart.y + f);
        point_ds = new Point(pointMoveStart.x + f, pointMoveStart.y + f);

        f = (float) Math.sqrt(2) * (MV_RADIUS + 128) / 2;
        point_sprint_w = new Point(pointMoveStart.x, pointMoveStart.y - MV_RADIUS - 128);
        point_sprint_aw = new Point(pointMoveStart.x - f, pointMoveStart.y - f);
        point_sprint_dw = new Point(pointMoveStart.x + f, pointMoveStart.y - f);

        motions = key_map.get(VIEW_START);
        if (motions != null)
            point_touch_start = new Point(motions.moves[0][0], motions.moves[0][1]);
        else point_touch_start = new Point(846, 264);

        step_len = MV_RADIUS + 128;

    }

    public TouchService() {
        for (int i = 0; i < MAX_SLOT; i++) {
            point_slots[i] = new Point();

            pointerProperties[i] = new MotionEvent.PointerProperties();
            pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;

            pointerCoordinates[i] = new MotionEvent.PointerCoords();
            pointerCoordinates[i].pressure = 1;
            pointerCoordinates[i].size = 1;

        }
        /* list of main map */
        key_maps.add(map_multiplayer);
        key_maps.add(map_battle_ground);
        key_maps.add(map_pve);

        /* sub map list of battle ground map */
        sub_battle_ground.add(map_moto);
        sub_battle_ground.add(map_chopper);
        sub_battle_ground.add(map_coyote);

        /* hash sub to one main */
        hash_main_sub.put(map_battle_ground, sub_battle_ground);


        /* for each main map */
        for (int i = 0; i < key_maps.size(); i++) {
            HashMap<Integer, Integer> m = new HashMap<>();

            /* get sync keys in this main map */
            for (int k : key_maps.get(i).keySet()) {
                if (Objects.requireNonNull(key_maps.get(i).get(k)).type == MOTION_SYNC) {
                    m.put(k, 0);
                    Log.d(TAG, "main map " + i + "sync key " + k);
                }
            }

            /* get all sync keys in sub map */
            for (HashMap<Integer, definition.KeyMotions> h : hash_main_sub.keySet()) {
                /* find this main map in main-sub hash */
                if (h == key_maps.get(i)) {

                    /* for each sub map in the sub list */
                    for (HashMap<Integer, definition.KeyMotions> sub : Objects.requireNonNull(hash_main_sub.get(h))) {
                        /* for each key of sub */
                        for (int k : sub.keySet()) {
                            /* if motion type of this key is sync */
                            if (Objects.requireNonNull(sub.get(k)).type == MOTION_SYNC) {
                                m.put(k, 0);
                                Log.d(TAG, "sub map of " + i + "sync key " + k);
                            }
                        }
                    }
                }
            }

            list_keyboard_sync_req_id.add(m);
        }


        initPointers(main_mode);

        display_size[0] = Resources.getSystem().getDisplayMetrics().widthPixels;
        display_size[1] = Resources.getSystem().getDisplayMetrics().heightPixels;
        Log.d(TAG, "display width=" + display_size[0] + " height=" + display_size[1]);
    }

    float get_relative_x(int x) {
        return (float)(display_size[0] * (x - window_pos[0]) / window_size[0]);
    }

    float get_relative_y(int y) {
        return (float)(display_size[1] * (y - window_pos[1]) / window_size[1]);
    }

    void dataHandle(TcpService.TcpData data) {
        if (data == null) {
            Log.e(TAG, "get null tcp data");
            return;
        }

        int id = data.get(V_ID);
        if (id != data_id + 1) {
            Log.e(TAG, "data id " + id + " != " + data_id);
        }
        data_id = id;

        int type = data.get(V_TYPE);
        int param1 = data.get(V_PARAM1);
        int param2 = data.get(V_PARAM2);


        if (EventType.TYPE_MOUSE_AXIS == type) {
            synchronized (sync_key_release) {

                if (map_mode_on || transparent_mode_on) {
                    point_touch_end.x = get_relative_x(param1);
                    point_touch_end.y = get_relative_y(param2);
                    if (mouse_pressed)
                        actionMoveAsync(point_touch_end, mouse_slot);
                } else if (key_drag_mode_on) {
                    point_touch_end.x = point_key_drag_start.x + param1 - point_mouse_start.x;
                    point_touch_end.y = point_key_drag_start.y + param2 - point_mouse_start.y;
                    if (mouse_pressed)
                        actionMoveAsync(point_touch_end, mouse_slot);
                } else {
                    if (!mouse_pressed) {
                        mouse_pressed = true;
                        mouse_slot = actionDownAsync(point_touch_start);
                        point_mouse_start.x = param1;
                        point_mouse_start.y = param2;
                    } else {
                        if (param1 != point_mouse_end.x || param2 != point_mouse_end.y) {
                            point_touch_end.x = point_touch_start.x + param1 - point_mouse_start.x;
                            point_touch_end.y = point_touch_start.y + param2 - point_mouse_start.y;
                            actionMoveAsync(point_touch_end, mouse_slot);
                        }
                    }
                    if (sub_mode == NONE_SUB_MODE)
                        mouse_timeout = SystemClock.uptimeMillis() + 100;
                    else
                        mouse_timeout = SystemClock.uptimeMillis() + 2000;
                }
                point_mouse_end.x = param1;
                point_mouse_end.y = param2;
            }
        } else if (EventType.TYPE_KEYBOARD == type) {

            //Log.d(TAG, " id=" + id + " type=" + type + " param1=" + param1 + " param2=" + param2);
            if (isMoveKey(param1)) {
                synchronized (sync_key_move) {
                    byte b = presetMoveKeyStatus(moveKeyStatus, param1, param2);
                    pointMoveEnd = getMoveEndPoint(b, sprint_status);
                    if (KeyEvent.KEY_DOWN == param2 && moveKeyStatus == 0) {
                        if (!start_move) {
                            start_move = true;
                            sync_key_move.notify();
                        }
                    } else if (KeyEvent.KEY_UP == param2 && moveKeyStatus == (1 << offsetMoveKey(param1) & 0xf)) {
                        if (start_move) {
                            start_move = false;
                            sync_key_move.notify();
                        }
                    } else if (start_move) {
                        sync_key_move.notify();
                    }

                    moveKeyStatus = b;
/*                if (KeyEvent.KEY_DOWN == param2 && moveKeyStatus == 0) {
                    slotStep = actionDownAsync(pointMoveNow);
                    pointMoveNow.x = pointMoveStart.x;
                    pointMoveNow.y = pointMoveStart.y;
                } else if (KeyEvent.KEY_UP == param2) {
                    int a = (1 << offsetMoveKey(param1)) & 0xf;
                    if (moveKeyStatus == a) {
                        actionUpAsync(pointMoveNow, slotStep);
                        pointMoveNow.x = pointMoveEnd.x;
                        pointMoveNow.y = pointMoveEnd.y;
                    }
                }

                moveOneStep(pointMoveNow, pointMoveEnd);
                moveKeyStatus = b;*/
                }
            } else if (MV_SPRINT == param1) {
                if (param2 != sprint_status) {

                    synchronized (sync_key_move) {
                        sprint_status = param2;
                        pointMoveEnd = getMoveEndPoint(moveKeyStatus, sprint_status);
                        if (start_move) sync_key_move.notify();
                    }
                }

            } else { /*  for keyboard keys except direction/sprint  */
                definition.KeyMotions motions = selectKeyMotions(param1);
                if (motions != null) {
                    if (motions.type == MOTION_TAP) {/* tap */
                        if (param2 == KeyEvent.KEY_DOWN) {
                            tap(motions.moves[0][0], motions.moves[0][1]);
                        }
                    } else if (motions.type == MOTION_SYNC) {/* sync key */
                        Point p = new Point(motions.moves[0][0], motions.moves[0][1]);

                        /* keyboard sync type hash <key, request id> in this main mode */
                        HashMap<Integer, Integer> map = list_keyboard_sync_req_id.get(main_mode);
                        if (map != null) {
                            if (param2 == KeyEvent.KEY_DOWN) {
                                int req_id = actionDownAsync(p);
                                map.put(param1, req_id);
                            } else if (param2 == KeyEvent.KEY_UP) {
                                if (map.containsKey(param1)) {
                                    Object req_id = map.get(param1);
                                    if (null != req_id) actionUpAsync(p, (int) req_id);
                                }
                            }
                        }
                    } else if (motions.type == MOTION_DRAG || motions.type == MOTION_COMB) {/* async motions */
                        synchronized (sync_key_slow_motions) {
                            if (param2 == KeyEvent.KEY_DOWN) {
                                slowMotionsList.add(motions);
                                sync_key_slow_motions.notify();
                            }
                        }
                    } else if (motions.type == MOTION_TRANS) {/* keyboard key drag mode */
                        synchronized (sync_key_release) {
                            if (param2 == KeyEvent.KEY_DOWN) {
                                key_drag_mode_on = true;
                                if (mouse_pressed) {
                                    mouse_pressed = false;
                                    actionUpAsync(point_touch_end, mouse_slot);
                                }

                                point_key_drag_start.x = motions.moves[0][0];
                                point_key_drag_start.y = motions.moves[0][1];
                                point_mouse_start.x = point_mouse_end.x;
                                point_mouse_start.y = point_mouse_end.y;

                                mouse_pressed = true;
                                mouse_slot = actionDownAsync(point_key_drag_start);

                            } else if (param2 == KeyEvent.KEY_UP) {
                                key_drag_mode_on = false;
                                if (mouse_pressed) {
                                    mouse_pressed = false;
                                    actionUpAsync(point_touch_end, mouse_slot);
                                }
                            }
                        }
                    }
                }
            }
        } else if (EventType.TYPE_MOUSE_BUTTON == type) {/*  for mouse buttons  */
            definition.KeyMotions motions = selectKeyMotions(param1 + MOUSE_BUTTON_OFFSET);
            if (motions == null) Log.d(TAG, "key " + param1 + " motion none");
            if (motions != null) {
                Log.d(TAG, "key " + param1 + " motion " + motions.description);
                if (motions.type == MOTION_TAP) {
                    if (param2 == KeyEvent.KEY_DOWN) tap(motions.moves[0][0], motions.moves[0][1]);
                } else if (motions.type == MOTION_SYNC) {
                    Point p = new Point(motions.moves[0][0], motions.moves[0][1]);
                    if (param2 == KeyEvent.KEY_DOWN) {
                        mouse_button_req_id[param1] = actionDownAsync(p);
                    } else if (param2 == KeyEvent.KEY_UP) {
                        actionUpAsync(p, mouse_button_req_id[param1]);
                    }
                } else {
                    synchronized (sync_key_slow_motions) {
                        if (param2 == KeyEvent.KEY_DOWN) {
                            slowMotionsList.add(motions);
                            sync_key_slow_motions.notify();
                        }
                    }
                }

                if (param1 + MOUSE_BUTTON_OFFSET == BT_LEFT && motions.type == MOTION_TRANS) {
                    synchronized (sync_key_release) {
                        if (param2 == KeyEvent.KEY_DOWN) {
                            if (!mouse_pressed) {
                                mouse_pressed = true;
                                mouse_slot = actionDownAsync(point_touch_end);
                            }
                        } else if (param2 == KeyEvent.KEY_UP) {
                            if (mouse_pressed) {
                                mouse_pressed = false;
                                actionUpAsync(point_touch_end, mouse_slot);
                            }
                        }
                    }
                }
            }
        } else if (EventType.TYPE_MOUSE_WHEEL == type) {/*  for mouse wheel  */
            definition.KeyMotions motions = selectKeyMotions(param1 + MOUSE_WHEEL_OFFSET);
            if (motions != null) {
                if (motions.type == MOTION_TAP) {
                    tap(motions.moves[0][0], motions.moves[0][1]);
                } else {
                    synchronized (sync_key_slow_motions) {
                        slowMotionsList.add(motions);
                        sync_key_slow_motions.notify();
                    }
                }
            }
        } else if (EventType.TYPE_CONTROL == type) {
            if (param1 == MAIN_MODE) {
                Log.d(TAG, "main mode switch to " + param2);
                main_mode = param2;
                sub_mode = NONE_SUB_MODE;
                initPointers(param2);
            } else if (param1 == SUB_MODE) {
                Log.d(TAG, "sub mode switch to " + param2);
                sub_mode = param2;
            } else if (param1 == MAP_MODE) {
                Log.d(TAG, "map mode " + param2);
                if (MAP_MODE_ON == param2) {
                    map_mode_on = true;
                } else if (MAP_MODE_OFF == param2) {
                    map_mode_on = false;
                }
                synchronized (sync_key_release) {
                    if (mouse_pressed) {
                        mouse_pressed = false;
                        actionUpAsync(point_touch_end, mouse_slot);
                    }
                }
            } else if (param1 == TRANSPARENT_MODE) {
                Log.d(TAG, "trans point mode " + param2);
                if (TRANSPARENT_ON == param2) {
                    transparent_mode_on = true;
                } else if (TRANSPARENT_OFF == param2) {
                    transparent_mode_on = false;
                }
                synchronized (sync_key_release) {
                    if (mouse_pressed) {
                        mouse_pressed = false;
                        actionUpAsync(point_touch_end, mouse_slot);
                    }
                }
            }
        } else if (EventType.TYPE_ALT_LOCATION == type) {
            int location_type = param1 & 0xFFFF;
            int key_code = param1 >> 16;
            int x = param2 & 0xFFFF;
            int y = param2 >> 16;
            Log.d(TAG, "key " + key_code + " x " + x + " y " + y);
            if (x == 0 && y == 0)
                hash_alter_keys_pos.remove(key_code);
            else
                hash_alter_keys_pos.put(key_code, new definition.KeyMotions("", MOTION_SYNC, new int[][]{{x, y}}));
//            if (SUPPLY_LIST == location_type) {
//            } else if(ALTER_PANEL == location_type) {
//            }

        } else if (EventType.TYPE_SETTING == type) {
            int x = param2 & 0xFFFF;
            int y = param2 >> 16;
            if (param1 == WINDOW_POS) {
                window_pos[0] = x;
                window_pos[1] = y;
                Log.d(TAG, "window position x=" + x + " y=" + y);
            } else if (param1 == WINDOW_SIZE) {
                window_size[0] = x;
                window_size[1] = y;
                Log.d(TAG, "window size x=" + x + " y=" + y);
            }
        }
    }

    definition.KeyMotions selectKeyMotions(int k) {
        definition.KeyMotions motions = key_maps.get(main_mode).get(k);

        /* search alter key list */
        if (hash_alter_keys_pos.containsKey(k))
            motions = hash_alter_keys_pos.get(k);

        if (sub_mode != NONE_SUB_MODE) {
            /* get sub mode list of this main mode */
            ArrayList<HashMap<Integer, definition.KeyMotions>> list = hash_main_sub.get(key_maps.get(main_mode));
            if (list != null) {
                /* get sub map */
                HashMap<Integer, definition.KeyMotions> sub = list.get(sub_mode - SUB_MODE_OFFSET);
                /* if sub contains this key, use sub map instead */
                if (sub.containsKey(k)) {
                    motions = sub.get(k);
                }
            }
        }
        /* if transparent mode on use it instead */
        if (transparent_mode_on) {
            if (map_transparent_mode.containsKey(k)) motions = map_transparent_mode.get(k);
        }

        /* if map opened use map instead */
        if (map_mode_on) {
            if (map_map_mode.containsKey(k)) {
                motions = map_map_mode.get(k);
            }
        }

        return motions;
    }

    Point step(float n, Point p, Point start, Point end, int req_id) {
        Point t = new Point();
        float distance = (float) Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));

        t.x = p.x + n * (end.x - start.x) / distance;
        t.y = p.y + n * (end.y - start.y) / distance;
        actionMoveAsync(t, req_id);
        return t;
    }

    void moveFromTo(Point start, Point end, int req_id) {

        float distance = (float) Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));

        Point p = new Point(start.x, start.y);

        for (int i = 0; i < 5; i++) {
            p = step(4, p, start, end, req_id);
            SystemClock.sleep(5);
        }

        int n = (int) (distance - 4 * 10) / 20;

        for (int i = 0; i < n; i++) {
            p = step(20, p, start, end, req_id);
            SystemClock.sleep(5);
        }

        float len = distance - (2 + n) * 20;
        if (len > 0) step(len, p, start, end, req_id);

        for (int i = 0; i < 5; i++) {
            p = step(4, p, start, end, req_id);
            SystemClock.sleep(5);
        }
        actionMoveAsync(pointMoveEnd, slotStep);
    }

    Thread threadMoveDirection = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (sync_key_move) {
                while (true) {
                    while (!start_move) {
                        try {
                            sync_key_move.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "wait start move interrupt exception" + e);
                        }
                    }

                    pointMoveNow.x = pointMoveStart.x;
                    pointMoveNow.y = pointMoveStart.y;
                    slotStep = actionDownAsync(pointMoveNow);

                    while (start_move) {
                        if (!pointSame(pointMoveNow, pointMoveEnd)) {
                            moveFromTo(pointMoveNow, pointMoveEnd, slotStep);
                            pointMoveNow.x = pointMoveEnd.x;
                            pointMoveNow.y = pointMoveEnd.y;
                        }

                        try {
                            sync_key_move.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "wait start move interrupt exception" + e);
                        }
                    }

                    actionUpAsync(pointMoveEnd, slotStep);
                }

            }
        }
    });

    Thread threadSendPoint = new Thread(() -> {
        TouchData data;
        while (true) {
            synchronized (sync_key_touch) {
                while (data_list_touch.isEmpty()) {
                    try {
                        sync_key_touch.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "wait touch data interrupt exception" + e);
                    }

                }


                data = data_list_touch.remove(0);
            }
            if (data != null) {
                if (ACT_DOWN == data.action) {
                    int slot = actionDown(data.p);
                    if (slot >= 0 && slot < MAX_SLOT) map_id_slot.put(data.id, slot);
                } else if (ACT_UP == data.action) {
                    Integer slot = map_id_slot.get(data.id);
                    if (slot != null) {
                        actionUp(data.p, slot);
                        map_id_slot.remove(data.id, slot);
                    }
                } else if (ACT_MOVE == data.action) {
                    Integer slot = map_id_slot.get(data.id);
                    if (slot != null) actionMove(data.p, slot);
                }
            }
        }
    });

    Thread threadReleaseTouch = new Thread(() -> {
        while (true) {
            if (!transparent_mode_on && !map_mode_on && !key_drag_mode_on) {
                synchronized (sync_key_release) {
                    if (mouse_pressed && SystemClock.uptimeMillis() > mouse_timeout) {
                        mouse_pressed = false;
                        actionUpAsync(point_touch_end, mouse_slot);
                    }
                }
            }
            SystemClock.sleep(100);
        }
    });

    Thread threadGetData = new Thread(() -> {
        synchronized (TcpService.sync_key_tcp) {
            TcpService.TcpData data = new TcpService.TcpData();
            while (true) {
                try {
                    while (TcpService.data_list_tcp.isEmpty()) TcpService.sync_key_tcp.wait();

                    byte[] buf = TcpService.data_list_tcp.remove(0);

                    if (buf != null) {
                        for (String name : data.names.keySet()) {
                            byte[] tmp = Arrays.copyOfRange(buf, data.offset(name), data.offset(name) + data.size(name));
                            data.set(name, tmp);
                        }

                        dataHandle(data);
                    }

                } catch (InterruptedException e) {
                    // Happens if someone interrupts your thread.
                    Log.e(TAG, "wait tcp data interrupt exception" + e);
                }
            }
        }
    });

    Thread threadMotionsDelay = new Thread(() -> {
        synchronized (sync_key_slow_motions) {
            while (true) {
                while (slowMotionsList.isEmpty()) {
                    try {
                        sync_key_slow_motions.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "DELAY thread wait interrupted");
                    }
                }

                definition.KeyMotions motions = slowMotionsList.remove(0);
                if (motions.type == MOTION_DRAG) {
                    drag(motions.moves[0][0], motions.moves[0][1], motions.moves[0][2], motions.moves[0][3], 100);
                } else if (motions.type == MOTION_COMB) {
                    int req_id = 0;
                    for (int[] m : motions.moves) {
                        if (m[0] == ACT_TAP) tap(m[1], m[2]);
                        else if (m[0] == ACT_DOWN) req_id = actionDownAsync(new Point(m[1], m[2]));
                        else if (m[0] == ACT_UP) actionUpAsync(new Point(m[1], m[2]), req_id);
                        else if (m[0] == ACT_MOVE) actionMoveAsync(new Point(m[1], m[2]), req_id);
                        else if (m[0] == ACT_DELAY) SystemClock.sleep(m[1]);

                    }
                }
            }
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        threadMoveDirection.start();
        threadMotionsDelay.start();
        threadSendPoint.start();
        threadReleaseTouch.start();
        threadGetData.start();
        return START_STICKY;
    }

    public boolean isMoveKey(int code) {
        return code == MV_UP || code == MV_LEFT || code == MV_DOWN || code == MV_RIGHT;
    }

    int offsetMoveKey(int key) {
        int i;
        if (key == MV_UP) {
            i = 0;
        } else if (key == MV_LEFT) {
            i = 1;
        } else if (key == MV_DOWN) {
            i = 2;
        } else if (key == MV_RIGHT) {
            i = 3;
        } else i = -1;
        return i;
    }

    public byte presetMoveKeyStatus(byte b, int key, int stat) {
        byte ret = b;
        int i = offsetMoveKey(key);
        if (i < 0 || i > 3) return 0;
        if (stat == KeyEvent.KEY_DOWN) ret |= 1 << i;
        else ret &= ~(1 << i);

        ret &= 0xf;

        return ret;
    }

    public Point getMoveEndPoint(byte b, int sprint_status) {
        boolean w = (b & 0x1) > 0;
        boolean a = (b & 0x2) > 0;
        boolean s = (b & 0x4) > 0;
        boolean d = (b & 0x8) > 0;
        Point end = new Point();
        if (w && !s && a == d) {
            if (sprint_status == 0) {
                end.x = point_sprint_w.x;
                end.y = point_sprint_w.y;
            } else {
                end.x = point_w.x;
                end.y = point_w.y;
            }
        } else if (a && !d && w == s) {
            end.x = point_a.x;
            end.y = point_a.y;
        } else if (s && !w && a == d) {
            end.x = point_s.x;
            end.y = point_s.y;
        } else if (d && !a && w == s) {
            end.x = point_d.x;
            end.y = point_d.y;
        } else if (a && w && !s) {
            if (sprint_status == 0) {
                end.x = point_sprint_aw.x;
                end.y = point_sprint_aw.y;
            } else {
                end.x = point_aw.x;
                end.y = point_aw.y;
            }
        } else if (d && w && !a) {
            if (sprint_status == 0) {
                end.x = point_sprint_dw.x;
                end.y = point_sprint_dw.y;
            } else {
                end.x = point_dw.x;
                end.y = point_dw.y;
            }
        } else if (a && s && !w) {
            end.x = point_as.x;
            end.y = point_as.y;
        } else if (d && s && !w) {
            end.x = point_ds.x;
            end.y = point_ds.y;
        } else {
            end.x = pointMoveStart.x;
            end.y = pointMoveStart.y;
        }
        return end;
    }

    public boolean isZero(double value, double threshold) {
        return value >= -threshold && value <= threshold;
    }

    public boolean pointSame(Point a, Point b) {
        return a.x - b.x >= -0.1 && a.x - b.x <= 0.1 && a.y - b.y >= -0.1 && a.y - b.y <= 0.1;
    }

    void moveOneStep(Point start, Point end) {

        float x = pointMoveEnd.x - pointMoveNow.x;
        float y = pointMoveEnd.y - pointMoveNow.y;

        if (!(isZero(x, 0.1) && isZero(y, 0.1))) {
            float len = (float) Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
            if (len > step_len) {
                x = (end.x - start.x) * step_len / len;
                y = (end.y - start.y) * step_len / len;
            } else {
                x = end.x - start.x;
                y = end.y - start.y;
            }

            pointMoveNow.x += x;
            pointMoveNow.y += y;
            actionMove(pointMoveNow, slotStep);

        }
    }

    int acquireSlot() {
        for (int i = 0; i < MAX_SLOT; i++) {
            if (!slots[i]) {
                slots[i] = true;
                return i;
            }
        }
        return -1;
    }

    void releaseSlot(int slot) {
        if (slot >= 0 && slot < MAX_SLOT) slots[slot] = false;
    }

    int slotCount() {
        int i = 0;
        for (boolean b : slots)
            if (b) i++;
        return i;
    }

    public void sendPoint(Point p, int slot, int action) {

        point_slots[slot].x = p.x;
        point_slots[slot].y = p.y;

        int j = 0;
        for (int i = 0; i < MAX_SLOT; i++) {
            if (slots[i]) {
                pointerProperties[j].id = i;
                pointerCoordinates[j].x = point_slots[i].x;
                pointerCoordinates[j].y = point_slots[i].y;
                j++;
            }
        }

        final long startTime = SystemClock.uptimeMillis();

        MotionEvent event = MotionEvent.obtain(startTime, startTime, action, j, pointerProperties, pointerCoordinates, 0, 0, 1, 1, 0, 0, 0, 0);

        if (mInst == null) {
            mInst = new Instrumentation();
        }
        mInst.sendPointerSync(event);
    }


    public int actionDown(Point p) {
        int slot = acquireSlot();
        if (slot < 0) return -1;
        int a;
        if (1 == slotCount()) a = MotionEvent.ACTION_DOWN;
        else a = MotionEvent.ACTION_POINTER_DOWN + (slot << MotionEvent.ACTION_POINTER_INDEX_SHIFT);

        sendPoint(p, slot, a);
        return slot;
    }

    public void actionUp(Point p, int slot) {
        if (slot < 0 || slot >= MAX_SLOT || !slots[slot]) return;
        int a;
        if (1 == slotCount()) a = MotionEvent.ACTION_UP;
        else if (1 < slotCount())
            a = MotionEvent.ACTION_POINTER_UP + (slot << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        else return;

        sendPoint(p, slot, a);
        releaseSlot(slot);
    }

    public void actionMove(Point p, int slot) {
        if (slot < 0 || slot >= MAX_SLOT) return;
        sendPoint(p, slot, MotionEvent.ACTION_MOVE);
    }

    int actionAsync(int id, int action, Point p) {

        TouchData data = new TouchData();
        data.id = id;
        data.action = action;
        data.p = new Point(p.x, p.y);
        synchronized (sync_key_touch) {
            data_list_touch.add(data);
            sync_key_touch.notify();
        }
        return id;
    }

    int actionDownAsync(Point p) {
        int id = req_id++;
        /* drop 0 */
        if (id == 0) id++;
        return actionAsync(id, ACT_DOWN, p);
    }

    void actionUpAsync(Point p, int id) {
        actionAsync(id, ACT_UP, p);
    }

    void actionMoveAsync(Point p, int id) {
        actionAsync(id, ACT_MOVE, p);
    }

    void test() {

        Point p = new Point(400, 400);
        Point p1 = new Point(500, 400);
        actionDown(p);

        for (int i = 0; i < 10; i++) {
            p.x += 20;
            p.y += 20;
            actionMove(p, 0);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        actionDown(p1);

        for (int i = 0; i < 10; i++) {
            p1.x += 10;
            p1.y += 10;
            actionMove(p1, 1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        actionUp(p, 0);

        for (int i = 0; i < 10; i++) {
            p1.x += 10;
            p1.y -= 10;
            actionMove(p1, 1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        actionUp(p1, 1);
    }

    public void test_multi_touch() {

        Log.d(TAG, "test multi touch" + android.os.Process.myUid());
        Point start1 = new Point(300, 300);
        Point start2 = new Point(400, 600);
        Point end1 = new Point(500, 300);
        Point end2 = new Point(600, 600);
        performPinch(start1, end1, start2, end2);

    }

    public void tap(float x, float y) {
        Point p = new Point(x, y);
        int slot = actionDownAsync(p);
        actionUpAsync(p, slot);
    }

    public void drag(float x1, float y1, float x2, float y2, float duration) {
        final int interval = 25;
        int steps = (int) (duration / interval);
        if (duration % interval > 0) steps++;
        float dx = (x2 - x1) / steps;
        float dy = (y2 - y1) / steps;
        int id = actionDownAsync(new Point(x1, y1));
        for (int step = 0; step < steps; step++) {
            SystemClock.sleep(interval);
            actionMoveAsync(new Point(x1 + step * dx, y1 + step * dy), id);
        }
        SystemClock.sleep(interval);
        actionUpAsync(new Point(x2, y2), id);
    }

    private static void down(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }
        //mUi =  mInst.getUiAutomation();

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0);

        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mInst.sendPointerSync(event);
        event.recycle();
        //mUi.injectInputEvent(event, true);
    }

    private static void up(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);

        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mInst.sendPointerSync(event);
        event.recycle();
    }

    private static void move(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x, y, 0);

        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mInst.sendPointerSync(event);
        event.recycle();
    }


    private static void performPinch(Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2) {
        final int duration = 500;
        final long eventMinInterval = 10;
        final long startTime = SystemClock.uptimeMillis();
        long eventTime = startTime;
        MotionEvent event;
        float eventX1, eventY1, eventX2, eventY2;

        eventX1 = startPoint1.x;
        eventY1 = startPoint1.y;
        eventX2 = startPoint2.x;
        eventY2 = startPoint2.y;

        // Specify the property for the two touch points
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
        pp1.id = 0;
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties pp2 = new MotionEvent.PointerProperties();
        pp2.id = 1;
        pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;

        properties[0] = pp1;
        properties[1] = pp2;

        // Specify the coordination's of the two touch points
        // NOTE: you MUST set the pressure and size value, or it doesn't work
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];
        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
        pc1.x = eventX1;
        pc1.y = eventY1;
        pc1.pressure = 1;
        pc1.size = 1;
        MotionEvent.PointerCoords pc2 = new MotionEvent.PointerCoords();
        pc2.x = eventX2;
        pc2.y = eventY2;
        pc2.pressure = 1;
        pc2.size = 1;
        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        /*
         * Events sequence of zoom gesture:
         *
         * 1. Send ACTION_DOWN event of one start point
         * 2. Send ACTION_POINTER_DOWN of two start points
         * 3. Send ACTION_MOVE of two middle points
         * 4. Repeat step 3 with updated middle points (x,y), until reach the end points
         * 5. Send ACTION_POINTER_UP of two end points
         * 6. Send ACTION_UP of one end point
         */


        // Step 1
        event = MotionEvent.obtain(startTime, eventTime, MotionEvent.ACTION_DOWN, 1, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

        if (mInst == null) {
            mInst = new Instrumentation();
        }
        mInst.sendPointerSync(event);

        // Step 2
        event = MotionEvent.obtain(startTime, eventTime, MotionEvent.ACTION_POINTER_DOWN + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        mInst.sendPointerSync(event);

        // Step 3, 4
        long moveEventNumber = duration / eventMinInterval;

        float stepX1, stepY1, stepX2, stepY2;

        stepX1 = (endPoint1.x - startPoint1.x) / moveEventNumber;
        stepY1 = (endPoint1.y - startPoint1.y) / moveEventNumber;
        stepX2 = (endPoint2.x - startPoint2.x) / moveEventNumber;
        stepY2 = (endPoint2.y - startPoint2.y) / moveEventNumber;

        for (int i = 0; i < moveEventNumber; i++) {
            // Update the move events
            eventTime += eventMinInterval;
            eventX1 += stepX1;
            eventY1 += stepY1;
            eventX2 += stepX2;
            eventY2 += stepY2;

            pc1.x = eventX1;
            pc1.y = eventY1;
            pc2.x = eventX2;
            pc2.y = eventY2;

            pointerCoords[0] = pc1;
            pointerCoords[1] = pc2;

            event = MotionEvent.obtain(startTime, eventTime, MotionEvent.ACTION_MOVE, 2, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
            mInst.sendPointerSync(event);
        }

        // Step 5
        pc1.x = endPoint1.x;
        pc1.y = endPoint1.y;
        pc2.x = endPoint2.x;
        pc2.y = endPoint2.y;
        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        eventTime += eventMinInterval;
        event = MotionEvent.obtain(startTime, eventTime, MotionEvent.ACTION_POINTER_UP + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        mInst.sendPointerSync(event);

        // Step 6
        eventTime += eventMinInterval;
        event = MotionEvent.obtain(startTime, eventTime, MotionEvent.ACTION_UP, 1, properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        mInst.sendPointerSync(event);
    }

}


