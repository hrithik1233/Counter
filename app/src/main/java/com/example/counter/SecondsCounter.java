package com.example.counter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;


interface SecondsCounterInterface {
    void updateTimeFormatSecondsCounter(long hr, long min, long sec);

    void updateStringFormatSecondsCounter(String timmeStamp);

    void updateTotalSecondsSecondsCounter(long totalSeondsCounter);


}


public class SecondsCounter {
    private long hr = 0, min = 0, sec = 0;
    private boolean ispause = false;
    public boolean iscounting = false;

    SecondsCounterInterface counterInterface = null;

    SecondsCounter(SecondsCounterInterface counterInterface) {
        this.counterInterface = counterInterface;
        callinterface = true;
    }


    Thread thread;
    boolean destruct = false;
    private boolean callinterface;

    SecondsCounter() {
    }

    SecondsCounter(long hr, long min, long sec) {
        this.hr = hr;
        this.min = min;
        this.sec = sec;
    }

    public void setUpdateInterfaceCall(SecondsCounterInterface counterInterface, boolean call) {
        callinterface = call;
        this.counterInterface = counterInterface;

    }

    public void setInterfaceCallbacksFlag(boolean callback) {
        callinterface = callback;
    }

    Handler mainHandler = new Handler(Looper.getMainLooper());

    public synchronized void start() {
        thread = new Thread(() -> {
            try {
                while (!destruct) {
                    if (!ispause) {
                        sec++;
                        min = min + sec / 60;
                        hr = hr + min / 60;
                        sec = sec % 60;
                        min = min % 60;
                        // INTERFACE CALLS ARE MADE
                        mainHandler.post(() -> {
                            if (counterInterface != null && callinterface) {
                                counterInterface.updateStringFormatSecondsCounter(getToStringFormat());
                                counterInterface.updateTimeFormatSecondsCounter(hr, min, sec);
                                counterInterface.updateTotalSecondsSecondsCounter(getTotalSeconds());
                            }
                        });

                        Thread.sleep(1000);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        if (!thread.isAlive())
            thread.start();

    }

    public void pause() {
        ispause = true;
        iscounting = false;

    }

    public void resume() {
        ispause = false;
        iscounting = true;
    }

    public void reset() {
        ispause = false;
        destruct = false;
        if (counterInterface != null) {
            counterInterface.updateStringFormatSecondsCounter(getToStringFormat());
            counterInterface.updateTimeFormatSecondsCounter(hr, min, sec);
            counterInterface.updateTotalSecondsSecondsCounter(getTotalSeconds());
        }

        hr = min = sec = 0;
    }

    public void stop() {
        hr = min = sec = 0;
        destruct = true;
        ispause = true;
    }

    public boolean isCounting() {
        return iscounting;
    }

    public long getHour() {
        return hr;

    }

    public long getMinutes() {
        return min;

    }

    public long getSeconds() {
        return sec;
    }

    @SuppressLint("DefaultLocale")
    public String getToStringFormat() {
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }

    public long getTotalSeconds() {
        return (hr * 60 * 60) + (min * 60) + sec;
    }

    protected void finalize() {
        destruct = true;

    }

    public void setHr(long hr) {

        this.hr = hr;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public void setSec(long sec) {
        this.sec = sec;
    }
}
