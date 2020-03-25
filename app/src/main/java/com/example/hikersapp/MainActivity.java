package com.example.hikersapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    TextView output;
    TextView output2;

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... Urls) {
            String result="";
            URL url ;
            HttpURLConnection urlConnection=null;
            try {
                url=new URL(Urls[0]);
                urlConnection= (HttpURLConnection)url.openConnection();
                InputStream in =urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                int data=reader.read();

                while (data!=-1){
                    char current=(char) data;
                    result+=current;
                    data=reader.read();
                }
                return result;
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Could not find Weather :(",Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);

                String mainInfojson=jsonObject.getString("main");
                JSONObject mainInfo = new JSONObject(mainInfojson);
                String temp_min=mainInfo.getString("temp_min")+"°C";
                String temp_max=mainInfo.getString("temp_max")+"°C";
                String humidity=mainInfo.getString("humidity")+"%";
                String pressure=mainInfo.getString("pressure")+" mbar";

                String windInfojson=jsonObject.getString("wind");
                JSONObject windInfo = new JSONObject(windInfojson);
                String speed=windInfo.getString("speed")+" m/sec";
                String deg=windInfo.getString("deg")+"°";

                String weatherInfo=jsonObject.getString("weather");
                //Log.i("Weather Info",weatherInfo);
                JSONArray arr=new JSONArray(weatherInfo);
                String message="";
                for(int i=0;i<arr.length();i++){
                    JSONObject part=arr.getJSONObject(i);
                    String main=part.getString("main");
                    String description=part.getString("description");
                    if(!main.equals("")&&!description.equals("")){
                        message+=main+": "+description+"\n";
                    }
                }
                if(!temp_max.equals("")&&!temp_min.equals("")&&!humidity.equals("")){
                    message+="\nTemp: (min) "+temp_min+", (max) "+temp_max+"\n"+"Humidity: "+humidity+"\n"+"Pressure: "+pressure+"\n"+"Wind: "+speed+" "+deg+"\n\n";
                }
                if(!message.equals("")) {
                    output.setText(message);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Could not find Weather :(",Toast.LENGTH_SHORT).show();
                }

            }
            catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Could not find Weather :(",Toast.LENGTH_SHORT).show();
            }

            //Log.i("JSON",s);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output=(TextView)findViewById(R.id.output);
        output2=(TextView)findViewById(R.id.output2);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Log.i("Location", location.toString());
                //Toast.makeText(MapsActivity.this,location.toString(),Toast.LENGTH_LONG).show();

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String out="";
                    if (listAddresses != null && listAddresses.size() > 0) {
                        //Log.i("Place",listAddresses.get(0).toString());
                        String address = "";

                        if (listAddresses.get(0).getThoroughfare() != null) {
                            address += listAddresses.get(0).getThoroughfare() + " ";
                        }
                        if (listAddresses.get(0).getLocality() != null) {
                            address += listAddresses.get(0).getLocality() + " ";
                        }
                        if (listAddresses.get(0).getPostalCode() != null) {
                            address += listAddresses.get(0).getPostalCode() + " ";
                        }
                        if (listAddresses.get(0).getAdminArea() != null) {
                            address += listAddresses.get(0).getAdminArea() + " ";


                            try {
                                //String URL = X;
                                DownloadTask task = new DownloadTask();
                                task.execute("https://openweathermap.org/data/2.5/weather?q=" +listAddresses.get(0).getAdminArea() + "&appid=b6907d289e10d714a6e88b30761fae22");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            

                        }
                        if (listAddresses.get(0).getCountryName() != null) {
                            address += listAddresses.get(0).getCountryName() + " ";
                        }
                        out+="Latitute: "+location.getLatitude()+"\n";
                        out+="Longitude: "+location.getLongitude()+"\n";
                        out+="Altitude: "+location.getAltitude()+"\n";
                        out+="Accuracy: "+location.getAccuracy()+"\n";
                        out+="Address: \n"+address;
                        output2.setText(out);
                        //Toast.makeText(getApplicationContext(), out, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }

    }
}
