package martin.noaahurricanetracks;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Scanner;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Hurricane> hurricaneList = new ArrayList<Hurricane>();
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int EXTRATROP = Color.GRAY;
    private static final int TROPDEPR = Color.BLUE;
    private static final int TROPSTORM = Color.GREEN;
    private static final int CATONE = Color.YELLOW;
    private static final int CATTWO = Color.rgb(250,132,14);
    private static final int CATTHREE = Color.RED;
    private static final int CATFOUR = Color.rgb(253,140,217);
    private static final int CATFIVE = Color.MAGENTA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Scanner s = new Scanner(getResources().openRawResource(R.raw.stormdata));
        //Get selected basin from intent
        Bundle bdl = getIntent().getExtras();
        String basin = bdl.getString("basin");
        //Get data from CSV file downloaded from https://www.ncdc.noaa.gov/ibtracs/index.php?name=wmo-data
        switch (basin){
            case "North Atlantic": s = new Scanner(getResources().openRawResource(R.raw.na));
                break;
            case "South Atlantic": s = new Scanner(getResources().openRawResource(R.raw.sa));
                break;
            case "West Pacific": s = new Scanner(getResources().openRawResource(R.raw.wp));
                break;
            case "East Pacific": s = new Scanner(getResources().openRawResource(R.raw.ep));
                break;
            case "South Pacific": s = new Scanner(getResources().openRawResource(R.raw.sp));
                break;
            case "North Indian": s = new Scanner(getResources().openRawResource(R.raw.ni));
                break;
            case "South Indian": s = new Scanner(getResources().openRawResource(R.raw.si));
                break;
        }

        int hurrNum = -1;
        String tempSerNum = "";
        try {
            while (s.hasNextLine()) {
                String[] row = s.nextLine().replace(" ", "").split(",");
                //Put info into objects for easier manipulation
                //Season Selection
                if (Integer.parseInt(row[1]) == bdl.getInt("season")) {
                    if (!row[0].equals(tempSerNum)) {
                        hurrNum++;
                        //Hurricane(String serialNumber, int season, int num, String basin, String subBasin, String name)
                        hurricaneList.add(new Hurricane(row[0], Integer.parseInt(row[1]), Integer.parseInt(row[2]), row[3], row[4], row[5]));
                        tempSerNum = hurricaneList.get(hurrNum).getSerialNumber();
                    }
                    //addTrackPoint(String time, String nature, float latitude, float longitude, int wind, int pressure, String center, String trackType)
                    hurricaneList.get(hurrNum).addTrackPoint(row[6], row[7], Float.parseFloat(row[8]), Float.parseFloat(row[9]), Float.parseFloat(row[10]), Float.parseFloat(row[11]), row[12], row[15]);
                }
            }
        } finally {
            s.close();
        }

        for(int i = 0; i<hurricaneList.size(); i++){
            Log.d(TAG, hurricaneList.get(i).toString());
        }
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

        // Add track point markers
        for(int i = 0; i<hurricaneList.size(); i++) {
            TrackPoint oldPoint = hurricaneList.get(i).getTrackPoints().get(0);
            for (int j = 1; j < hurricaneList.get(i).getTrackPoints().size(); j++) {
                TrackPoint point = hurricaneList.get(i).getTrackPoints().get(j);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(point.getLatitude(),point.getLongitude()))
                        .title(hurricaneList.get(i).getName())
                        .snippet("" +
                                "Date: " + point.getISO_time() + "" +
                                "Wind(kt): " + point.getWind() + "" +
                                "Pressure(mb): " + point.getPressure() +
                                ""));
                //draw lines
                PolylineOptions polyLineOptions = new PolylineOptions();
                polyLineOptions.add(new LatLng(oldPoint.getLatitude(), oldPoint.getLongitude()));
                //decide color based on intensity
                Log.d(TAG, String.valueOf(point.getWind()));
                Log.d(TAG, point.getNature());
                polyLineOptions.color(TROPDEPR);
                if (point.getWind() >= 34.0) {
                    polyLineOptions.color(TROPSTORM);
                }
                if (point.getWind() >= 64.0) {
                    polyLineOptions.color(CATONE);
                }
                if (point.getWind() >= 83.0) {
                    polyLineOptions.color(CATTWO);
                }
                if (point.getWind() >= 96.0) {
                    polyLineOptions.color(CATTHREE);
                }
                if (point.getWind() >= 113.0) {
                    polyLineOptions.color(CATFOUR);
                }
                if (point.getWind() >= 137.0){
                    polyLineOptions.color(CATFIVE);
                }
                if (point.getNature().equals("ET")){
                    polyLineOptions.color(EXTRATROP);
                }
                if(point.getNature().equals("DS")) {
                    polyLineOptions.color(TROPDEPR);
                }
                polyLineOptions.add(new LatLng(point.getLatitude(),point.getLongitude()));
                oldPoint = point;
                mMap.addPolyline(polyLineOptions);
            }
        }
    }

    //https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/MarkerDemoActivity.java
}
