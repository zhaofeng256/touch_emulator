package com.zf.coyote;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;

public class TouchService extends Service {

    public static final String TAG = TouchService.class.getSimpleName();

    float [] move_forward = {330, 820};
    float [] origin_point = {743, 409};

    public TouchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){

        Handler handler = new Handler();
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            Log.d(TAG, "running... " + finalI);
            handler.postDelayed(touchAt(330, 820, 3000)::start, 4000 * finalI);
        }
//        touchAt(300, 800, 3000).start();
//        touchAt(600, 800, 3000).start();
        return START_STICKY;
    }

    public Thread touchAt(float x, float y, long duration) {
        Log.d(TAG, "touch at " + x + " "+ y);
        return new Thread(() -> {
            Instrumentation inst = new Instrumentation();

            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, x, y, 0));
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP, x, y, 0));
        });
    }
}