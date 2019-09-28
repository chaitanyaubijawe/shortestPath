package com.example.myapplication;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.ui.tasks.DirectionsJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.example.myapplication.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> allPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng HAMBURG = new LatLng(53.558, 9.927);
        LatLng KIEL = new LatLng(53.551, 9.993);
        LatLng KIEL2 = new LatLng(53.545, 9.998);


        mMap.addMarker(new MarkerOptions().position(HAMBURG).title("Marker in HAMBURG"));
        mMap.addMarker(new MarkerOptions().position(KIEL).title("Marker in KIEL"));

        // Getting URL to the Google Directions API
//        String url = getDirectionsUrl(HAMBURG, KIEL);

        String url = getDirectionsUrl(HAMBURG,KIEL, Arrays.asList(KIEL, KIEL2));
        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        //downloadTask.execute(url);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(HAMBURG));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                allPoints.add(point);

                if(allPoints.size() >= 4){

                    String url = getDirectionsUrl(allPoints.get(0),allPoints.get(allPoints.size()-1), allPoints);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
//                    getGraphData(allPoints);
                }

                mMap.addMarker(new MarkerOptions().position(point));
            }
        });
    }

    private double[][] getGraphData(List<LatLng> allPoints){
        double[] [] a = new double [allPoints.size()] [allPoints.size()];

        List<List<Double>> paths = new ArrayList<>();


        for(LatLng point: allPoints){

            List<Double> path = new ArrayList<>();

            for(LatLng pointInner: allPoints){

                if((point.latitude == pointInner.latitude) && (point.longitude == pointInner.longitude)){

                    path.add(0D);

                }else{

                    path.add(12D);
                    // call API here. get distance put it into array.
                    String url = getDirectionsUrl(point, pointInner);
                    Log.d("Url - ", url);

                }
            }

            paths.add(path);
        }



        return a;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route

// Drawing polyline in the Google Map for the i-th route
            mMap.clear();
            mMap.addPolyline(lineOptions);

            for(LatLng point : allPoints){

                mMap.addMarker(new MarkerOptions().position(point));
            }

        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";
//        String key = "&key=AIzaSyAVxPL35seBc7DBLmi-uHvD2otT2_KuzzI";
        String key = "&key=AIzaSyAVxPL35seBc7DBLmi-uHvD2otT2_KuzzI";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + key;


        return url;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, List<LatLng> wayPoints) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Destination of route
        String way = "wayPoints=";
        int i = 0;
        for(LatLng d : wayPoints){
            i ++;
            if (i==1 || i== 4){
                continue;
            }
            way += d.latitude + "," + d.longitude +"|";
        }


        way = way.substring(0,way.length() - 1);


        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor  + "&" + way;

        // Output format
        String output = "json";
//        String key = "&key=AIzaSyAVxPL35seBc7DBLmi-uHvD2otT2_KuzzI";
        String key = "&key=AIzaSyAVxPL35seBc7DBLmi-uHvD2otT2_KuzzI";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + key;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


}
