package com.zf.coyote;

import android.app.Instrumentation;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.method.Touch;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.io.IOException;

public class TouchService extends Service {

    public static final String TAG = TouchService.class.getSimpleName();
    static Instrumentation mInst;
    static UiAutomation mUi;
    static int points = 0;
    float[] move_forward = {330, 820};
    float[] origin_point = {743, 409};

    public TouchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(() -> {
            test_multi_touch();

        }).start();
        return START_STICKY;
    }

    public void test_multi_touch() {

        Log.d(TAG,"test multi touch" + android.os.Process.myUid());
        Point start1 = new Point(300,300);
        Point start2 = new Point(400,400);
        Point end1 = new Point(500,500);
        Point end2 = new Point(600,600);
        performPinch(start1, end1, start2, end2);

    }
    public void tap(float x, float y) {
      down(x, y);
      SystemClock.sleep(100);
      up(x,y);
    }

    public static void drag(float x1, float y1, float x2, float y2, float duration) throws InterruptedException {
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
    public class Point {
        float x;
        float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    };
    private static void performPinch( Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2) {
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

        // Specify the coordinations of the two touch points
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