package com.example.nkuskov.mapgps;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for creating Path between two points on Google Map. By parsing JSON file with polylines.
 * Created by nkuskov on 4/27/2017.
 */

public class PathCreator {

    Marker mMarker = null;
    Polyline line;
    MapsActivity mMapsActivity;
    static String json = "";

    PathCreator(MapsActivity mapsActivity) {
        mMapsActivity = mapsActivity;
    }

    /**
     * Creating URL for requesting JSON file.
     * And call AsyncTask class for creating path.
     *
     * @param sourcelat Latitude of source location
     * @param sourcelog Longitude of source location
     * @param destlat   Latitude of destination
     * @param destlog   Longitude of destination
     * @return
     */
    public void makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        //Making URL request for getting JSON file.
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");//from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination="); //to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyAvKrvyhgnE0D2Ljn-RfoM7lHB_Eld8xKE");
        //Call AsyncTask class for creating path
        connectAsyncTask asyncTask = new connectAsyncTask(urlString.toString());
        asyncTask.execute();
    }

    /**
     * Drawing path by using List<LatLng>. This List was getting from Parsing JSON file
     * and taking from coding polylines.
     *
     * @param path
     */
    public void drawPath(String path) {
        if (line != null) {
            line.remove();
        }
        if (mMarker != null) {
            mMarker.remove();
        }

        try {
            final JSONObject jsonObject = new JSONObject(path);
            JSONArray routeArray = jsonObject.getJSONArray("routes");

            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Random rand = new Random();
            line = mMapsActivity.mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)))
                    .geodesic(true));

            mMarker = mMapsActivity.mMap.addMarker(new MarkerOptions().position(list.get(list.size() - 1)));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decoding Polylines from JSON file. To List<LatLng>
     *
     * @param encoded
     * @return
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        LatLng latLng = new LatLng(mMapsActivity.mLastLocation.getLatitude(), mMapsActivity.mLastLocation.getLongitude());
        poly.add(latLng);
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);

        }
        return poly;
    }


    /**
     * Getting JSON file from URL request.
     *
     * @param urls
     * @return
     */
    public String getJSONFromUrl(String urls) {

        //making http request
        try {
            URL url = new URL(urls);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            //Create String from HttpRequest
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            json = stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;

    }

    /**
     * class for creating and drawing Path.
     */
    private class connectAsyncTask extends AsyncTask<Void, Void, String> {

        private ProgressDialog mProgressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mMapsActivity);
            mProgressDialog.setMessage("Fetching road, please wait...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();


        }

        @Override
        protected String doInBackground(Void... params) {
            return getJSONFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }
}



