package com.example.nkuskov.mapgps;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.location.places.Place;

import java.text.DecimalFormat;

/**
 * Created by nkuskov on 5/3/2017.
 */

public class PathStopWatch extends AsyncTask<Void, Void, Void> {

    MapsActivity mMapsActivity;
    Location startLocation;
    Location stopLocation;
    float distance = 0.0f;
    Handler mHandler;
    Stopwatch timer;

    final int MSG_START_TIMER = 0;
    final int MSG_UPDATE_TIMER = 1;
    final int MSG_STOP_TIMER = 2;
    final int MSG_CANCEL_TIMER = 3;
    final int REFRESH_RATE = 100;

    /**
     * @param startLocation Location from where starting
     * @param mapsActivity  current maps activity
     */
    public PathStopWatch(Location startLocation, Place place, MapsActivity mapsActivity) {
        this.stopLocation = new Location("");
        this.stopLocation.setLatitude(place.getLatLng().latitude);
        this.stopLocation.setLongitude(place.getLatLng().longitude);
        this.startLocation = startLocation;
        this.mMapsActivity = mapsActivity;
        this.timer = new Stopwatch();
        createHandler();

    }

    private void createHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_START_TIMER:
                        timer.start();
                        mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                        break;

                    case MSG_UPDATE_TIMER:
                        mMapsActivity.currentTime.setText(getStopWatchText(timer.getElapsedTime()));
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER, REFRESH_RATE);
                        break;

                    case MSG_STOP_TIMER:
                        mHandler.removeMessages(MSG_UPDATE_TIMER);
                        timer.stop();
                        mMapsActivity.currentTime.setText(getStopWatchText(timer.getElapsedTime()));
                        break;

                    case MSG_CANCEL_TIMER:
                        mHandler.removeMessages(MSG_UPDATE_TIMER);
                        mMapsActivity.currentTime.clearComposingText();
                        break;

                    default:
                        break;
                }
            }
        };
    }

    public static String getStopWatchText(long timeInMs) {
        long hours = timeInMs / (1000 * 60 * 60);
        long minutes = (timeInMs / (1000 * 60)) % 60;
        long seconds = (timeInMs / (1000)) % 60;

        String minutesSeconds;
        if (seconds < 10) {
            minutesSeconds = "" + minutes + ":0" + seconds;
        } else {
            minutesSeconds = "" + minutes + ":" + seconds;
        }

        if (hours != 0) {
            return "" + hours + ":" + minutesSeconds;
        } else {
            return minutesSeconds;
        }
    }

//    public static double distanceBetween(Location firstLocation, Location secondLocation) {
//
//        double firstLat = Math.toDegrees(firstLocation.getLatitude());
//        double firstLng = Math.toDegrees(firstLocation.getLongitude());
//        double secondLat = Math.toDegrees(secondLocation.getLatitude());
//        double secondLng = Math.toDegrees(secondLocation.getLongitude());
//
//        return Math.round(6371210 * Math.acos(Math.cos(firstLat) * Math.cos(secondLat) * Math.cos(firstLng - secondLng) + Math.sin(firstLat) * Math.sin(secondLat)));
//    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Void doInBackground(Void... params) {
        do {
            distance = startLocation.distanceTo(mMapsActivity.mLastLocation);
            if (isCancelled()) {
                return null;
            }
        } while (distance <= 50.0f);
        mHandler.sendEmptyMessage(MSG_START_TIMER);
        do {
            distance = stopLocation.distanceTo(mMapsActivity.mLastLocation);
            if (isCancelled()){
                return null;
            }
        } while (distance >= 50.0f);
        mHandler.sendEmptyMessage(MSG_STOP_TIMER);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }

    @Override
    protected void onCancelled(Void result) {
        mHandler.sendEmptyMessage(MSG_CANCEL_TIMER);
    }

    private class Stopwatch {
        private long startTime = 0;
        private long stopTime = 0;
        private boolean running = false;

        public void start() {
            this.startTime = System.currentTimeMillis();
            this.running = true;
        }

        public void stop() {
            this.stopTime = System.currentTimeMillis();
            this.running = false;
        }

        public long getElapsedTime() {
            if (running) {
                return System.currentTimeMillis() - startTime;
            }
            return stopTime - startTime;
        }


    }

}
