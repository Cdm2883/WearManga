package vip.cdms.wearmanga.utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TimeUtils {
    public static final ArrayList<Timer> timeoutQueue = new ArrayList<>();

    public static int setTimeout(Runnable runnable, int delay) {
        int index = timeoutQueue.size();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
                timeoutQueue.set(index, null);
                cancel();
            }
        }, delay);
        timeoutQueue.add(index, timer);
        return index;
    }
}
