package com.tom.maps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMyLocationChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final LatLng SCE = new LatLng(25.025797, 121.537819);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        if (mMap!=null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(this);
        }
        api = new GoogleApiClient.Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        api.connect();
/*        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 10, this);*/

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        /*
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Testing")
                .setSmallIcon(R.drawable.face_sunglasses)
                .build();
        manager.notify(1, noti);
*/
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("hahahah");
        builder.setContentText("Testing");
        builder.setSmallIcon(R.drawable.face_sunglasses);
        Intent intent = new Intent(this, TestActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification noti = builder.build();
        manager.notify(1, noti);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SCE, 18));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        new FriendTask().execute();
    }

    @Override
    public void onLocationChanged(Location loc) {
//        Log.d("onLocationChanged", loc.getLatitude()+"/"+loc.getLongitude());
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

    @Override
    public void onMyLocationChange(Location location) {
//        Log.d("onMyLocationChange", location.getLatitude()+","+location.getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("PLACES", "onConnected");
        testPlaces();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("PLACES", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("PLACES", "onConnectionFailed");
    }


    class FriendTask extends AsyncTask<Void, Void, String>{
        String site = "http://j.snpy.org/friend.json";
        @Override
        protected String doInBackground(Void... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(site);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line = in.readLine();
                while(line!=null){
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("JSON", s);
            try {
                JSONArray array = new JSONArray(s);
                for (int i=0; i<array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.getString("name");
                    JSONObject location = obj.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    Log.d("Friend", name+"/"+lat+"/"+lng);
                    mMap.addMarker(new MarkerOptions()
                            .title(name)
                            .position(new LatLng(lat, lng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.laughing_face))
                    );
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void testPlaces(){
        PendingResult<PlaceLikelihoodBuffer> result =
                Places.PlaceDetectionApi.getCurrentPlace(api, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                Log.d("PLACES", "onResult:"+placeLikelihoods.getCount());
                for (PlaceLikelihood placeLikelihood: placeLikelihoods){
                    Place place = placeLikelihood.getPlace();
                    Log.d("PLACES", place.getName().toString());
                }
            }
        });
    }
}
