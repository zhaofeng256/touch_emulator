package com.zf.coyote;

import static com.zf.coyote.definition.EventType;
import static com.zf.coyote.definition.KeyEvent;
import static com.zf.coyote.definition.MOUSE_CODE;
import static com.zf.coyote.definition.VK_A;
import static com.zf.coyote.definition.VK_D;
import static com.zf.coyote.definition.VK_S;
import static com.zf.coyote.definition.VK_W;
import static com.zf.coyote.definition.V_ID;
import static com.zf.coyote.definition.V_PARAM1;
import static com.zf.coyote.definition.V_PARAM2;
import static com.zf.coyote.definition.V_TYPE;
import static com.zf.coyote.definition.WHEEL_CODE;
import static com.zf.coyote.definition.position;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    static class Step extends Point {
        public int steps;
    }

    public static final String TAG = TouchService.class.getSimpleName();
    static Instrumentation mInst = new Instrumentation();
    Point point_w = new Point(243, 424);
    Point point_a = new Point(154, 513);
    Point point_s = new Point(243, 602);
    Point point_d = new Point(332, 513);
    Point point_aw = new Point(180, 450);
    Point point_dw = new Point(305, 450);
    Point point_as = new Point(180, 575);
    Point point_ds = new Point(305, 575);
    Point point_wasd_start = new Point(243, 513);
    Point point_wasd_end = new Point(243, 513);
    Point point_wasd_end_old = new Point(243, 513);
    Point point_wasd_now = new Point(243, 513);
    Point point_touch_start = new Point(846, 264);  //1080x2400  1280x720
    Point point_touch_end = new Point();
    Point point_mouse_start = new Point();
    Point point_mouse_old = new Point();


    Step wasd_step = new Step();
    long mouse_timeout;
    boolean mouse_pressed = false;
    boolean w_pressed = false;
    boolean a_pressed = false;
    boolean s_pressed = false;
    boolean d_pressed = false;
    boolean last_wasd_pressed = false;
    int MAX_SLOT = 10;
    boolean[] slots = new boolean[MAX_SLOT];
    Point[] point_slots = new Point[MAX_SLOT];
    MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[MAX_SLOT];
    MotionEvent.PointerCoords[] pointerCoordinates = new MotionEvent.PointerCoords[MAX_SLOT];
    int wasd_slot;
    int mouse_slot;
    int mouse_button_slot[] = new int[5];
    HashMap<Integer, int[]> key_map = new HashMap<Integer, int[]>();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public TouchService() {
        init_pointers();

        for (int[] ints : position) {
            key_map.put(ints[0], new int[]{ints[1], ints[2]});
        }

        for (int k : key_map.keySet()) {

            int [] axis = key_map.get(k);
            Log.d(TAG, "key: "+ k + " " + Arrays.toString(axis));
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(() -> {
            //test();
            while (true) {
                synchronized (TcpService.sync_key) {
                    try {

//                        1. 取数据
//                            1.鼠标
//                            如鼠标已抬起 发送按下 设置按下
//                            发送坐标发送坐标 设置超时时间
//                            2.wasd
//                            按下 设置终点 如wasd全部抬起 发送按下
//                            抬起 如未全抬起 设置终点
//                            如全部抬起 发送抬起
//
//                        2.如鼠标已按下 && 超时未动 发送抬起 设置抬起
//                        3.如wasd未全部抬起 && 未到达终点 向终点移动一格

                        while (TcpService.data_list.isEmpty()) {
                            TcpService.sync_key.wait(100);

                            long now = SystemClock.uptimeMillis();
                            if (mouse_pressed && now > mouse_timeout) {
                                mouse_pressed = false;
                                action_up(point_touch_end, mouse_slot);
                            }
                        }
                        if (!TcpService.data_list.isEmpty()) {
                            TcpService.TcpData data = TcpService.data_list.remove(0);

                            int id = data.get(V_ID);
                            int type = data.get(V_TYPE);
                            int param1 = data.get(V_PARAM1);
                            int param2 = data.get(V_PARAM2);

                            Log.d(TAG, " id=" + id + " type=" + type +
                                    " param1=" + param1 + " param2=" + param2);

                            if (EventType.TYPE_MOUSE == type) {

                                if (!mouse_pressed) {
                                    mouse_pressed = true;
                                    mouse_slot = action_down(point_touch_start);
                                    point_mouse_start.x = param1;
                                    point_mouse_start.y = param2;
                                } else {

                                    if (param1 != point_mouse_old.x || param2 != point_mouse_old.y) {
                                        point_touch_end.x = point_touch_start.x + (param1 - point_mouse_start.x) * 2400 / 1366;
                                        point_touch_end.y = point_touch_start.y + (param2 - point_mouse_start.y) * 1080 / 768;
                                        action_move(point_touch_end, mouse_slot);
                                        point_mouse_old.x = param1;
                                        point_mouse_old.y = param2;
                                    }
                                }

                                mouse_timeout = SystemClock.uptimeMillis() + 100;

                            } else if (EventType.TYPE_KEYBOARD == type) {
                                if (is_wasd(param1)) {
                                    set_wasd_status(param1, param2);
                                    get_wasd_end();
                                    if (wasd_end_changed())
                                        get_wasd_step(point_wasd_now, point_wasd_end, 1);
                                    back_wasd_end();

                                    if (KeyEvent.KEY_DOWN == param2) {

                                        if (!last_wasd_pressed) {
                                            point_wasd_now.x = point_wasd_start.x;
                                            point_wasd_now.y = point_wasd_start.y;
                                            wasd_slot = action_down(point_wasd_now);
                                        }

                                        wasd_one_step();

                                    } else if (KeyEvent.KEY_UP == param2) {
                                        if (last_wasd_pressed && is_wasd_all_release()) {
                                            action_up(point_wasd_now, wasd_slot);
                                            point_wasd_now.x = point_wasd_start.x;
                                            point_wasd_now.y = point_wasd_start.y;
                                        }
                                    }

                                    last_wasd_pressed = !is_wasd_all_release();
                                } else {
                                   int[] pos = key_map.get(param1);
                                    if(pos != null) {
                                        if (param2 == KeyEvent.KEY_DOWN) {
                                            tap(pos[0], pos[1]);
                                        }
                                    }
                                }
                            }
                            else if (EventType.TYPE_BUTTON == type) {
                                int[] pos = key_map.get(param1 + MOUSE_CODE);
                                if (pos != null) {
                                    Point p = new Point(pos[0], pos[1]);
                                    if (param2 == KeyEvent.KEY_DOWN) {
                                        mouse_button_slot[param1] = action_down(p);

                                    } else if (param2 == KeyEvent.KEY_UP) {
                                        action_up(p, mouse_button_slot[param1]);
                                    }
                                }
                            }
                            else if (EventType.TYPE_WHEEL == type) {
                                int[] pos = key_map.get(param1 + WHEEL_CODE);
                                if (pos != null) {
                                    tap(pos[0], pos[1]);
                                }
                            }
                        }

//                        if (!is_wasd_all_release() && wasd_step.steps-- > 0) {
//                            point_wasd_now.x += wasd_step.x;
//                            point_wasd_now.y += wasd_step.y;
//                            action_move(point_wasd_now, wasd_slot);
//                        }

                    } catch (InterruptedException e) {
                        // Happens if someone interrupts your thread.
                        Log.e(TAG, "exception" + e);
                    }

                }
            }

        }).start();


        return START_STICKY;
    }

    public void set_wasd_status(int key, int stat) {
        if (VK_W == key)
            w_pressed = stat == KeyEvent.KEY_DOWN;
        else if (VK_A == key)
            a_pressed = stat == KeyEvent.KEY_DOWN;
        else if (VK_S == key)
            s_pressed = stat == KeyEvent.KEY_DOWN;
        else if (VK_D == key)
            d_pressed = stat == KeyEvent.KEY_DOWN;
    }

    public boolean is_wasd(int code) {
        return code == VK_W || code == VK_A || code == VK_S || code == VK_D;
    }

    public boolean is_wasd_all_release() {
        return !(w_pressed || a_pressed || s_pressed || d_pressed);
    }

    public void get_wasd_end() {
        if (w_pressed && !s_pressed && a_pressed == d_pressed) {
            point_wasd_end.x = point_w.x;
            point_wasd_end.y = point_w.y;
        } else if (a_pressed && !d_pressed && w_pressed == s_pressed) {
            point_wasd_end.x = point_a.x;
            point_wasd_end.y = point_a.y;
        } else if (s_pressed && !w_pressed && a_pressed == d_pressed) {
            point_wasd_end.x = point_s.x;
            point_wasd_end.y = point_s.y;
        } else if (d_pressed && !a_pressed && w_pressed == s_pressed) {
            point_wasd_end.x = point_d.x;
            point_wasd_end.y = point_d.y;
        } else if (a_pressed && w_pressed && !s_pressed && !d_pressed) {
            point_wasd_end.x = point_aw.x;
            point_wasd_end.y = point_aw.y;
        } else if (d_pressed && w_pressed && !a_pressed && !s_pressed) {
            point_wasd_end.x = point_dw.x;
            point_wasd_end.y = point_dw.y;
        } else if (a_pressed && s_pressed && !w_pressed && !d_pressed) {
            point_wasd_end.x = point_as.x;
            point_wasd_end.y = point_as.y;
        } else if (d_pressed && s_pressed && !w_pressed && !a_pressed) {
            point_wasd_end.x = point_ds.x;
            point_wasd_end.y = point_ds.y;
        } else {
            point_wasd_end.x = point_wasd_start.x;
            point_wasd_end.y = point_wasd_start.y;
        }

    }

    public boolean wasd_end_changed() {
        return point_wasd_end_old.x != point_wasd_end.x || point_wasd_end_old.y != point_wasd_end.y;
    }

    void back_wasd_end() {
        point_wasd_end_old.x = point_wasd_end.x;
        point_wasd_end_old.y = point_wasd_end.y;
    }

    public boolean isZero(double value, double threshold) {
        return value >= -threshold && value <= threshold;
    }

    void wasd_one_step() {
        float x = point_wasd_end.x - point_wasd_now.x;
        float y = point_wasd_end.y - point_wasd_now.y;
        if (Math.abs(wasd_step.x) > Math.abs(x))
            wasd_step.x = x;
        if (Math.abs(wasd_step.y) > Math.abs(y))
            wasd_step.y = y;

        if (!(isZero(wasd_step.x, 0.1) && isZero(wasd_step.y, 0.1))) {
            point_wasd_now.x += wasd_step.x;
            point_wasd_now.y += wasd_step.y;
            action_move(point_wasd_now, wasd_slot);
        }
    }

    int get_slot() {
        for (int i = 0; i < MAX_SLOT; i++) {
            if (!slots[i]) {
                slots[i] = true;
                return i;
            }
        }
        return -1;
    }

    void release_slot(int slot) {
        if (slot >= 0 && slot < MAX_SLOT)
            slots[slot] = false;
    }

    boolean is_slot_empty() {
        for (boolean b : slots)
            if (b) return false;
        return true;
    }

    int slot_count() {
        int i = 0;
        for (boolean b : slots)
            if (b) i++;
        return i;
    }

    void init_pointers() {

        for (int i = 0; i < MAX_SLOT; i++) {
            point_slots[i] = new Point();

            pointerProperties[i] = new MotionEvent.PointerProperties();
            pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;

            pointerCoordinates[i] = new MotionEvent.PointerCoords();
            pointerCoordinates[i].pressure = 1;
            pointerCoordinates[i].size = 1;

        }
    }

    public void action(Point p, int slot, int action) {

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
        slot_count();

        MotionEvent event = MotionEvent.obtain(startTime, startTime, action, j,
                pointerProperties, pointerCoordinates,
                0, 0, 1, 1, 0, 0, 0, 0);

        if (mInst == null) {
            mInst = new Instrumentation();
        }
        mInst.sendPointerSync(event);
    }

    public int action_down(Point p) {
        int slot = get_slot();
        if (slot < 0)
            return -1;
        int a;
        if (1 == slot_count())
            a = MotionEvent.ACTION_DOWN;
        else
            a = MotionEvent.ACTION_POINTER_DOWN + (slot << MotionEvent.ACTION_POINTER_INDEX_SHIFT);

        action(p, slot, a);
        return slot;
    }

    public void action_up(Point p, int slot) {
        if (slot < 0 || slot >= MAX_SLOT)
            return;
        int a;
        if (1 == slot_count())
            a = MotionEvent.ACTION_UP;
        else if (1 < slot_count())
            a = MotionEvent.ACTION_POINTER_UP + (slot << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        else
            return;
        action(p, slot, a);
        release_slot(slot);
    }

    public void action_move(Point p, int slot) {
        action(p, slot, MotionEvent.ACTION_MOVE);
    }

    public void get_wasd_step(Point start, Point end, int steps) {
        wasd_step.steps = steps;
        wasd_step.x = (end.x - start.x) / steps;
        wasd_step.y = (end.y - start.y) / steps;
    }

    void test() {

        Point p = new Point(400, 400);
        Point p1 = new Point(500, 400);
        action_down(p);

        for (int i = 0; i < 10; i++) {
            p.x += 20;
            p.y += 20;
            action_move(p, 0);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        action_down(p1);

        for (int i = 0; i < 10; i++) {
            p1.x += 10;
            p1.y += 10;
            action_move(p1, 1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        action_up(p, 0);

        for (int i = 0; i < 10; i++) {
            p1.x += 10;
            p1.y -= 10;
            action_move(p1, 1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        action_up(p1, 1);
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
        int slot = action_down(p);
        action_up(p, slot);
    }

    public static void drag(float x1, float y1, float x2, float y2, float duration) {
        final int interval = 25;
        int steps = (int) (duration / interval + 1);
        float dx = (x2 - x1) / steps;
        float dy = (y2 - y1) / steps;
        down(x1, y1);
        for (int step = 0; step < steps; step++) {
            SystemClock.sleep(interval);
            move(x1 + step * dx, y1 + step * dy);
        }
        SystemClock.sleep(interval);
        up(x2, y2);
    }

    private static void down(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }
        //mUi =  mInst.getUiAutomation();

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, x, y, 0);

        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mInst.sendPointerSync(event);
        event.recycle();
        //mUi.injectInputEvent(event, true);
    }

    private static void up(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, x, y, 0);

        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        mInst.sendPointerSync(event);
        event.recycle();
    }

    private static void move(float x, float y) {
        if (mInst == null) {
            mInst = new Instrumentation();
        }

        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, x, y, 0);

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
        event = MotionEvent.obtain(startTime, eventTime,
                MotionEvent.ACTION_DOWN, 1, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

        if (mInst == null) {
            mInst = new Instrumentation();
        }
        mInst.sendPointerSync(event);

        // Step 2
        event = MotionEvent.obtain(startTime, eventTime,
                MotionEvent.ACTION_POINTER_DOWN + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2,
                properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
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

            event = MotionEvent.obtain(startTime, eventTime,
                    MotionEvent.ACTION_MOVE, 2, properties,
                    pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
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
        event = MotionEvent.obtain(startTime, eventTime,
                MotionEvent.ACTION_POINTER_UP + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        mInst.sendPointerSync(event);

        // Step 6
        eventTime += eventMinInterval;
        event = MotionEvent.obtain(startTime, eventTime,
                MotionEvent.ACTION_UP, 1, properties,
                pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        mInst.sendPointerSync(event);
    }

}


