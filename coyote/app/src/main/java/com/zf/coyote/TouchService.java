package com.zf.coyote;

import static com.zf.coyote.definition.*;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

public class TouchService extends Service {

    public static final String TAG = TouchService.class.getSimpleName();
    static Instrumentation mInst;
    Point point_w = new Point(330, 820);
    Point point_a = new Point(330, 820);
    Point point_s = new Point(330, 820);
    Point point_d = new Point(330, 820);
    Point point_aw = new Point(330, 820);
    Point point_dw = new Point(330, 820);
    Point point_as = new Point(330, 820);
    Point point_ds = new Point(330, 820);
    Point point_wasd_start = new Point(330, 500);
    Point point_wasd_end = point_wasd_start;
    Point point_wasd_now = point_wasd_start;
    Point point_mouse_start = new Point(743, 409);
    Point point_mouse_end = point_mouse_start;

    static class Step extends Point {
        public int steps;
    }

    Step wasd_step;
    long mouse_timeout;
    boolean mouse_pressed = false;
    boolean w_pressed = false;
    boolean a_pressed = false;
    boolean s_pressed = false;
    boolean d_pressed = false;
    boolean last_wasd_pressed = false;

    boolean[] slots = new boolean[10];

    int wasd_slot;
    int mouse_slot;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(() -> {
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

                        while (TcpService.data_list.isEmpty())
                            TcpService.sync_key.wait(25);

                        //test_multi_touch();
                        if (!TcpService.data_list.isEmpty()) {
                            TcpService.TcpData data = (TcpService.TcpData) TcpService.data_list.remove(0);

                            int id = data.get(V_ID);
                            int type = data.get(V_TYPE);
                            int param1 = data.get(V_PARAM1);
                            int param2 = data.get(V_PARAM2);

                            Log.d(TAG, " id=" + id + " type=" + type +
                                    " param1=" + param1 + " param2=" + param2);

                            if (EventType.TYPE_MOUSE == type) {
                                point_mouse_end.x = param1;
                                point_mouse_end.y = param2;
                                if (!mouse_pressed) {
                                    mouse_pressed = true;
                                    mouse_slot = action_down(new Point(param1, param2));
                                }
                                action_move(point_mouse_end, mouse_slot);
                                mouse_timeout = SystemClock.uptimeMillis() + 100;
                            } else if (EventType.TYPE_KEYBOARD == type) {
                                if (is_wasd(param1)) {
                                    set_wasd_status(param1, param2);
                                    point_wasd_end = get_wasd_end();

                                    if (KeyEvent.KEY_DOWN == param2) {
                                        if (!last_wasd_pressed) {
                                            wasd_slot = action_down(point_wasd_start);
                                            wasd_step = get_step(point_mouse_start, point_mouse_end, 10);
                                        }
                                    } else if (KeyEvent.KEY_UP == param2) {
                                        if (last_wasd_pressed && is_wasd_all_release())
                                            action_up(point_wasd_end, wasd_slot);
                                    }

                                    last_wasd_pressed = !is_wasd_all_release();
                                }
                            }
                        }

                        long now = SystemClock.uptimeMillis();
                        if (mouse_pressed && now > mouse_timeout) {
                            mouse_pressed = false;
                            action_up(point_mouse_end, mouse_slot);
                        }

                        if (!is_wasd_all_release() && wasd_step.steps-- > 0) {
                            point_wasd_now.x += wasd_step.x;
                            point_wasd_now.y += wasd_step.y;
                            action_move(point_wasd_now, wasd_slot);
                        }

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

    public Point get_wasd_end() {
        if (w_pressed && !s_pressed && a_pressed == d_pressed)
            return point_w;
        else if (a_pressed && !d_pressed && w_pressed == s_pressed)
            return point_a;
        else if (s_pressed && !w_pressed && a_pressed == d_pressed)
            return point_s;
        else if (d_pressed && a_pressed && w_pressed == s_pressed)
            return point_d;
        else if (a_pressed && w_pressed && !s_pressed && !d_pressed)
            return point_aw;
        else if (d_pressed && w_pressed && !a_pressed && !s_pressed)
            return point_dw;
        else if (a_pressed && s_pressed && !w_pressed && !d_pressed)
            return point_as;
        else if (d_pressed && s_pressed && !w_pressed && !a_pressed)
            return point_ds;
        else
            return point_wasd_start;
    }

    public int get_slot() {
        int i = 0;
        while (slots[i++]) ;
        slots[--i] = true;
        return i;
    }

    public void release_slot(int slot) {
        slots[slot] = false;
    }

    public boolean is_slot_empty() {
        for (boolean b : slots)
            if (b) return false;
        return true;
    }

    public void action(Point p, int slot, int action, boolean empty) {

        MotionEvent.PointerProperties[] pp = new MotionEvent.PointerProperties[1];
        pp[0] = new MotionEvent.PointerProperties();
        pp[0].id = slot;
        pp[0].toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords[] pc = new MotionEvent.PointerCoords[1];
        pc[0] = new MotionEvent.PointerCoords();
        pc[0].x = p.x;
        pc[0].y = p.y;
        pc[0].pressure = 1;
        pc[0].size = 1;

        final long startTime = SystemClock.uptimeMillis();
        int a = action;
        if (!empty) {
            if (action == MotionEvent.ACTION_DOWN)
                a = MotionEvent.ACTION_POINTER_DOWN + (pp[0].id << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            else if (action == MotionEvent.ACTION_UP)
                a = MotionEvent.ACTION_POINTER_UP + (pp[0].id << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        }

        MotionEvent event = MotionEvent.obtain(startTime, startTime, a,1, pp, pc,
                0, 0, 1, 1, 0, 0, 0, 0);

        if (mInst == null) {
            mInst = new Instrumentation();
        }
        mInst.sendPointerSync(event);
    }

    public int action_down(Point p) {
        boolean empty = is_slot_empty();
        int slot = get_slot();
        action(p, slot, MotionEvent.ACTION_DOWN, empty);
        return slot;
    }

    public void action_up(Point p, int slot) {
        release_slot(slot);
        boolean empty = is_slot_empty();
        action(p, slot, MotionEvent.ACTION_UP, empty);
    }

    public void action_move(Point p, int slot) {
        action(p, slot, MotionEvent.ACTION_MOVE, false);
    }

    public Step get_step(Point start, Point end, int steps) {
        Step step = new Step();
        step.steps = steps;
        step.x = (end.x - start.x) / steps;
        step.y = (end.y - start.y) / steps;
        return step;
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
        down(x, y);
        SystemClock.sleep(100);
        up(x, y);
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


