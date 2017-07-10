package martin.noaahurricanetracks;

import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Scanner;

public class MapsActivity extends AppCompatActivity {

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
                    hurricaneList.get(hurrNum).addTrackPoint(hurricaneList.get(hurrNum), row[6], row[7], new LatLng(Float.parseFloat(row[8]), Float.parseFloat(row[9])), Float.parseFloat(row[10]), Float.parseFloat(row[11]), row[12], row[15]);
                }
            }
        } finally {
            s.close();
        }

        for(int i = 0; i<hurricaneList.size(); i++){
            Log.d(TAG, hurricaneList.get(i).toString());
        }

        //get map fragment and ready it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap mMap) {

                // Add track point markers
                for(int i = 0; i<hurricaneList.size(); i++) {
                    TrackPoint oldPoint = hurricaneList.get(i).getTrackPoints().get(0);
                    for (int j = 1; j < hurricaneList.get(i).getTrackPoints().size(); j++) {
                        final TrackPoint point = hurricaneList.get(i).getTrackPoints().get(j);
                        int dot = R.drawable.bluedot;

                        //draw lines
                        PolylineOptions polyLineOptions = new PolylineOptions();
                        polyLineOptions.add(oldPoint.getLatLng());
                        //decide color based on intensity
                        Log.d(TAG, String.valueOf(point.getWind()));
                        Log.d(TAG, point.getNature());
                        polyLineOptions.color(TROPDEPR);
                        if (point.getWind() >= 34.0) {
                            polyLineOptions.color(TROPSTORM);
                            dot = R.drawable.greendot;
                        }
                        if (point.getWind() >= 64.0) {
                            polyLineOptions.color(CATONE);
                            dot = R.drawable.yellowdot;
                        }
                        if (point.getWind() >= 83.0) {
                            polyLineOptions.color(CATTWO);
                            dot = R.drawable.orangedot;
                        }
                        if (point.getWind() >= 96.0) {
                            polyLineOptions.color(CATTHREE);
                            dot = R.drawable.reddot;
                        }
                        if (point.getWind() >= 113.0) {
                            polyLineOptions.color(CATFOUR);
                            dot = R.drawable.pinkdot;
                        }
                        if (point.getWind() >= 137.0){
                            polyLineOptions.color(CATFIVE);
                            dot = R.drawable.purpledot;
                        }
                        if (point.getNature().equals("ET")){
                            polyLineOptions.color(EXTRATROP);
                            dot = R.drawable.greydot;
                        }
                        if(point.getNature().equals("DS")) {
                            polyLineOptions.color(TROPDEPR);
                            dot = R.drawable.bluedot;
                        }
                        polyLineOptions.add(point.getLatLng());
                        //make clickable
                        oldPoint = point;
                        Polyline polyLine = mMap.addPolyline(polyLineOptions);
                        polyLine.setTag(hurricaneList.get(i));
                        polyLine.setClickable(true);

                        //add markers for trackpoints
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(dot))
                                .position(point.getLatLng())
                        );
                        marker.setTag(point);
                        //change on click action for markers from info window to textView split
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                        {

                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                TrackPoint trackPoint = (TrackPoint) marker.getTag();
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(trackPoint.getLatLng()));
                                TextView tv = (TextView) findViewById(R.id.trackPointTitleTextView);
                                tv.setText(trackPoint.getHurricane().getName());
                                TextView tv2 = (TextView) findViewById(R.id.trackPointInfoTextView);
                                tv2.setText("Date: " + trackPoint.getISO_time() + "\n" +
                                        "Wind(kt): " + trackPoint.getWind() + "\n" +
                                        "Pressure(mb): " + trackPoint.getPressure());
                                return true;
                            }

                        });
                        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
                        {
                            @Override
                            public void onPolylineClick(Polyline polyline)
                            {
                                Hurricane hurricane = (Hurricane) polyline.getTag();
                                TextView tv = (TextView) findViewById(R.id.trackPointTitleTextView);
                                tv.setText(hurricane.getSerialNumber());
                            }
                        });
                    }
                }
            }
        });

    }
    //https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/MarkerDemoActivity.java
}
