package com.sarapham.mapapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String requestURL;
    JSONObject response;
    JSONArray results;
    JSONObject geometry;
    JSONObject location;
    JSONArray addycomp;
    JSONObject adco;

    String respLat;
    String respLong;
    String respZip;
    boolean zipExists = false;



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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drop_addmarkers:
                addMarker();
                return true;
            case R.id.HYBRID_map:
                if(mMap != null){
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                }
            case R.id.NONE_map:
                if(mMap != null){
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    return true;
                }
            case R.id.NORMAL_map:
                if(mMap != null){
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                }
            case R.id.SATELLITE_map:
                if(mMap != null){
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                }
            case R.id.TERRAIN_map:
                if(mMap != null){
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMarker(){
        if(mMap != null){

            //create custom LinearLayout programmatically
            LinearLayout layout = new LinearLayout(MapsActivity.this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText titleField = new EditText(MapsActivity.this);
            titleField.setHint("Title");

            final EditText latField = new EditText(MapsActivity.this);
            latField.setHint("Address");
            //latField.setInputType(InputType.TYPE_CLASS_NUMBER
            //        | InputType.TYPE_NUMBER_FLAG_DECIMAL
            //        | InputType.TYPE_NUMBER_FLAG_SIGNED);


            layout.addView(titleField);
            layout.addView(latField);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Marker");
            builder.setView(layout);
            AlertDialog alertDialog = builder.create();

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean parsable = true;
                    Double lat = null, lon = null;

                    String strLat = latField.getText().toString();
                    String strTitle = titleField.getText().toString();

                    //this is where all the magic is gonna happen
                    //key: AIzaSyADaSJfPtEY9UeK2I_TsVSk9OEvDOx9iig
                    //example call: https://maps.googleapis.com/maps/api/geocode/json?address=Dallas,+TX&key=AIzaSyADaSJfPtEY9UeK2I_TsVSk9OEvDOx9iig

                    String call1 = "https://maps.googleapis.com/maps/api/geocode/json?address=";
                    String call2 = "";
                    String call3 = "&key=AIzaSyADaSJfPtEY9UeK2I_TsVSk9OEvDOx9iig";

                    String[] split = strLat.split(" ");

                    int i;
                    for(i = 0; i < split.length; i++) {
                        call2 = call2 + split[i];
                        call2 = call2 + "+";
                    }

                    requestURL = call1 + call2 + call3;
                    try {
                        String strResponse = new GetUrlContentTask().execute(requestURL).get();
                        extractVars(strResponse);
                    }
                    catch(Exception e) {
                        //((EditText)findViewById(R.id.temp)).setText("lol sike, no.");
                        parsable = false;
                    }



                    //update lat and lon manually after the call to the API
                    lat = Double.parseDouble(respLat);
                    lon = Double.parseDouble(respLong);

                    DecimalFormat df = new DecimalFormat("##.####");

                    df.setRoundingMode(RoundingMode.FLOOR);

                    double r1 = new Double(df.format(lat));
                    double r2 = new Double(df.format(lon));


                    //strTitle = strTitle + "\n" + lat.toString();
                    //rstrTitle = strTitle + "\n" + lon.toString();

                    if(parsable){
                        LatLng targetLatLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions;

                        if(!zipExists) {
                            MarkerOptions markerOptions1 = new MarkerOptions().position(targetLatLng).title(strTitle).snippet("Latitude: " + r1 + " Longitude: " + r2);
                            markerOptions = markerOptions1;
                        } else {
                            MarkerOptions markerOptions2 = new MarkerOptions().position(targetLatLng).title(strTitle).snippet("Latitude: " + r1 + " Longitude: " + r2 + " Zip: " + respZip);
                            markerOptions = markerOptions2;
                        }


                        mMap.addMarker(markerOptions);

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLatLng));
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();
        }else{
            Toast.makeText(MapsActivity.this, "Map not ready", Toast.LENGTH_LONG).show();
        }
    }


    private class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String content = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inLine;
                StringBuilder response = new StringBuilder();

                while((inLine = in.readLine()) != null) {

                    response.append(inLine);
                }
                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content;
        }
    }


    public void extractVars(String resp) {
        try {
            response = new JSONObject(resp);
            results = response.getJSONArray("results");

            addycomp = results.getJSONObject(0).getJSONArray("address_components");

            int i;
            int zipIndex = 0;
            for(i = 0; i < addycomp.length(); i++) {
                if(addycomp.getJSONObject(i).getJSONArray("types").getString(0).equals("postal_code")) {
                    zipExists = true;
                    zipIndex = i;
                    break;
                }
            }
            if(zipExists) {
                respZip = addycomp.getJSONObject(zipIndex).getString("long_name");
            }


            geometry = results.getJSONObject(0).getJSONObject("geometry");
            location = geometry.getJSONObject("location");
            respLat = location.getString("lat").toString();
            respLong = location.getString("lng").toString();
        } catch (JSONException e) {
            //nada
        }
    }

}
