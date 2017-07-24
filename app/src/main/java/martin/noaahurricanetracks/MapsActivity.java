package martin.noaahurricanetracks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MapsActivity extends AppCompatActivity {

    private MapsActivity instance = this;
    private GoogleMap map = null;
    private ArrayList<Hurricane> hurricaneList = new ArrayList<Hurricane>();
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ArrayList<Polyline> polylineList = new ArrayList<Polyline>();
    private Hurricane selectedHurricane = null;
    private TrackPoint selectedTrackPoint = null;
    private static final String TAG = MapsActivity.class.getSimpleName();

    public static final int EXTRATROP = Color.rgb(170,170,170);
    public static final int TROPDEPR = Color.rgb(28,84,255);
    public static final int TROPSTORM = Color.rgb(109,195,67);
    public static final int CATONE = Color.rgb(255,195,9);
    public static final int CATTWO = Color.rgb(255,115,9);
    public static final int CATTHREE = Color.rgb(232,59,12);
    public static final int CATFOUR = Color.rgb(232,12,174);
    public static final int CATFIVE = Color.rgb(189,0,255);

    public static final int FADEDEXTRATROP = Color.argb(64,170,170,170);
    public static final int FADEDTROPDEPR = Color.argb(64,28,84,255);
    public static final int FADEDTROPSTORM = Color.argb(64,109,195,67);
    public static final int FADEDCATONE = Color.argb(64,255,195,9);
    public static final int FADEDCATTWO = Color.argb(64,255,115,9);
    public static final int FADEDCATTHREE = Color.argb(64,232,59,12);
    public static final int FADEDCATFOUR = Color.argb(64,232,12,174);
    public static final int FADEDCATFIVE = Color.argb(64,189,0,255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //get variables
        Button forwardButton = (Button) findViewById(R.id.forwardButton);
        Button backButton = (Button) findViewById(R.id.backButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView progressTV = (TextView) findViewById(R.id.progressTextView);

        progressTV.setText("Getting file(s)");
        ArrayList<Scanner> scanners = new ArrayList<Scanner>();
        //Get selected basin from intent
        Bundle bdl = getIntent().getExtras();
        boolean[] basin = (boolean[]) bdl.get("basin");
        //Get data from CSV file downloaded from https://www.ncdc.noaa.gov/ibtracs/index.php?name=wmo-data
        if(basin[0]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.na)));
        }
        if(basin[1]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.sa)));
        }
        if(basin[2]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.ep)));
        }
        if(basin[3]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.wp)));
        }
        if(basin[4]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.sp)));
        }
        if(basin[5]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.ni)));
        }
        if(basin[6]){
            scanners.add(new Scanner(getResources().openRawResource(R.raw.si)));
        }

        int hurrNum = -1;
        String tempSerNum = "";
        progressTV.setText("Reading Files");
        int beginSeason = bdl.getInt("beginSeason");
        int endSeason = bdl.getInt("endSeason");
        int[] seasonArray = new int[endSeason-beginSeason+1];
        int count = 0;
        while(count<seasonArray.length){
            seasonArray[count] = beginSeason+count;
            count++;
        }
        try {
            for(Scanner scanner: scanners) {
                while (scanner.hasNextLine()) {
                    String[] row = scanner.nextLine().replace(" ", "").split(",");
                    //Put info into objects for easier manipulation
                    //Season Selection
                    if (contains(seasonArray, Integer.parseInt(row[1]))) {
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
            }
            //remove hurricane if intensity not selected
            boolean[] intensity = bdl.getBooleanArray("intensity");
            boolean skip = true;
            for(boolean b: intensity){
                if(b){
                    skip = false;
                }
            }
            int i = 0;
            if(intensity[8] || !skip) {
                while (i < hurricaneList.size()) {
                    ArrayList<Hurricane.Category> categories = hurricaneList.get(i).getCategories();
                    boolean remove = true;
                    if (intensity[0] && categories.contains(Hurricane.Category.TD)) {
                        remove = false;
                    } else if (intensity[1] && categories.contains(Hurricane.Category.TS)) {
                        remove = false;
                    } else if (intensity[2] && categories.contains(Hurricane.Category.C1)) {
                        remove = false;
                    } else if (intensity[3] && categories.contains(Hurricane.Category.C2)) {
                        remove = false;
                    } else if (intensity[4] && categories.contains(Hurricane.Category.C3)) {
                        remove = false;
                    } else if (intensity[5] && categories.contains(Hurricane.Category.C4)) {
                        remove = false;
                    } else if (intensity[6] && categories.contains(Hurricane.Category.C5)) {
                        remove = false;
                    } else if (intensity[7] && categories.contains(Hurricane.Category.ET)) {
                        remove = false;
                    }
                    if (remove) {
                        hurricaneList.remove(i);
                    } else {
                        i++;
                    }
                }
            }
            Log.d(TAG, "Number of hurricanes: " + hurricaneList.size());
            if(hurricaneList.size()==0){
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                Toast.makeText(this, "No recorded storm this season", Toast.LENGTH_LONG).show();
                return;
            }
        } finally {
            for(Scanner scanner: scanners) {
                scanner.close();
            }
        }

        progressTV.setText("Starting map");
        //get map fragment and ready it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap mMap) {
                map = mMap;

                // Add track point markers
                for(int i = 0; i<hurricaneList.size(); i++) {
                    TrackPoint oldPoint = hurricaneList.get(i).getTrackPoints().get(0);
                    for (int j = 0; j < hurricaneList.get(i).getTrackPoints().size(); j++) {
                        final TrackPoint point = hurricaneList.get(i).getTrackPoints().get(j);
                        int dot = R.drawable.bluedot;

                        //draw lines
                        PolylineOptions polyLineOptions = new PolylineOptions();
                        polyLineOptions.add(oldPoint.getLatLng());
                        //decide color based on intensity
                        Log.d(TAG, String.valueOf(point.getWind()));
                        Log.d(TAG, point.getNature());
                        polyLineOptions.color(TROPDEPR);
                        switch(point.getCategory()){
                            case ET:
                                polyLineOptions.color(EXTRATROP);
                                dot = R.drawable.greydot;
                                break;
                            case TD:
                                polyLineOptions.color(TROPDEPR);
                                dot = R.drawable.bluedot;
                                break;
                            case TS:
                                polyLineOptions.color(TROPSTORM);
                                dot = R.drawable.greendot;
                                break;
                            case C1:
                                polyLineOptions.color(CATONE);
                                dot = R.drawable.yellowdot;
                                break;
                            case C2:
                                polyLineOptions.color(CATTWO);
                                dot = R.drawable.orangedot;
                                break;
                            case C3:
                                polyLineOptions.color(CATTHREE);
                                dot = R.drawable.reddot;
                                break;
                            case C4:
                                polyLineOptions.color(CATFOUR);
                                dot = R.drawable.pinkdot;
                                break;
                            case C5:
                                polyLineOptions.color(CATFIVE);
                                dot = R.drawable.purpledot;
                                break;
                        }
                        polyLineOptions.add(point.getLatLng());
                        //make clickable
                        oldPoint = point;
                        Polyline polyLine = mMap.addPolyline(polyLineOptions);
                        polyLine.setTag(hurricaneList.get(i));
                        polyLine.setClickable(true);
                        hurricaneList.get(i).setPolyline(polyLine);
                        polylineList.add(polyLine);

                        //add markers for trackpoints
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(dot))
                                .position(point.getLatLng())
                        );
                        marker.setVisible(false);
                        marker.setTag(point);
                        point.setMarker(marker);
                        markerList.add(marker);
                        //change on click action for markers from info window to textView split
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                        {

                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                trackPointSelected(marker);
                                return true;
                            }

                        });
                        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
                        {
                            @Override
                            public void onPolylineClick(Polyline polyline)
                            {
                                hurricaneSelected(polyline);
                            }
                        });
                    }
                }
                //zoom to fit trackPoints
                ArrayList<TrackPoint> points = new ArrayList<TrackPoint>();
                for(Marker marker: markerList){
                    points.add((TrackPoint)marker.getTag());
                }

                /*
                RelativeLayout loadContainer = (RelativeLayout) findViewById(R.id.loadingScreen);
                LinearLayout.LayoutParams  loadParams = (LinearLayout.LayoutParams) loadContainer.getLayoutParams();
                loadParams.height = 0;
                loadContainer.setLayoutParams(loadParams);

                FrameLayout mapContainer = (FrameLayout) findViewById(R.id.map);
                LinearLayout.LayoutParams  params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                mapContainer.setLayoutParams(params);
                */

                zoomToFitTrackPoints(points, mMap);

                //only show visible markers, listen for camera zoom
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //disappear info window
                        RelativeLayout infoContainer = (RelativeLayout) findViewById(R.id.info);
                        LinearLayout.LayoutParams  params = (LinearLayout.LayoutParams) infoContainer.getLayoutParams();
                        params.height = 0;
                        infoContainer.setLayoutParams(params);

                        Log.d(TAG,"Clicked Map");
                        selectedHurricane = null;
                        selectedTrackPoint = null;
                        for (Marker marker : markerList) {
                            marker.setVisible(false);
                        }
                        for(Polyline line: polylineList){
                            if(!line.getTag().equals(selectedHurricane)){
                                int color = line.getColor();
                                if(color == EXTRATROP || color == FADEDEXTRATROP)
                                    line.setColor(EXTRATROP);
                                else if(color == TROPDEPR || color == FADEDTROPDEPR)
                                    line.setColor(TROPDEPR);
                                else if(color == TROPSTORM || color == FADEDTROPSTORM)
                                    line.setColor(TROPSTORM);
                                else if(color == CATONE || color == FADEDCATONE)
                                    line.setColor(CATONE);
                                else if(color == CATTWO || color == FADEDCATTWO)
                                    line.setColor(CATTWO);
                                else if(color == CATTHREE || color == FADEDCATTHREE)
                                    line.setColor(CATTHREE);
                                else if(color == CATFOUR || color == FADEDCATFOUR)
                                    line.setColor(CATFOUR);
                                else if(color == CATFIVE || color == FADEDCATFIVE)
                                    line.setColor(CATFIVE);
                            }
                        }
                    }
                });
                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        if(selectedHurricane==null) {
                            if (mMap.getCameraPosition().zoom < 6) {
                                for (Marker marker : markerList) {
                                    marker.setVisible(false);
                                }
                            } else {
                                for (Marker marker : markerList) {
                                    marker.setVisible(true);
                                }
                            }
                        }
                    }
                });
                //end map ready
            }
        });

        //forward button, moves to next hurricane in season, or next trackpoint in track
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Forward button");
                if(selectedTrackPoint!=null){
                    Log.d(TAG,"Forward trackpoint");
                    int indexOfNext = selectedHurricane.getTrackPoints().indexOf(selectedTrackPoint) + 1;
                    trackPointSelected(selectedHurricane.getTrackPoints().get(indexOfNext).getMarker());
                }
                else if(selectedHurricane!=null){
                    Log.d(TAG,"Forward hurricane");
                    int indexOfNext = hurricaneList.indexOf(selectedHurricane)+1;
                    hurricaneSelected(hurricaneList.get(indexOfNext).getPolyline());
                }
            }
        });

        //back button, moves to previous hurricane in season, or next previous point in track
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Back button");
                if(selectedTrackPoint!=null){
                    Log.d(TAG,"Back trackpoint");
                    int indexOfPrev = selectedHurricane.getTrackPoints().indexOf(selectedTrackPoint) - 1;
                    trackPointSelected(selectedHurricane.getTrackPoints().get(indexOfPrev).getMarker());
                }
                else if(selectedHurricane!=null){
                    Log.d(TAG,"Back hurricane");
                    int indexOfPrev = hurricaneList.indexOf(selectedHurricane)-1;
                    hurricaneSelected(hurricaneList.get(indexOfPrev).getPolyline());
                }
            }
        });
    }

    //https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/MarkerDemoActivity.java
    private void zoomToFitTrackPoints(ArrayList<TrackPoint> trackPointList, GoogleMap map){
        //zoom to fit hurricane track
        //Calculate the markers to get their position
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (TrackPoint trackPoint : trackPointList) {
            builder.include(trackPoint.getLatLng());
        }
        LatLngBounds bounds = builder.build();
        //Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 30);
        map.animateCamera(cu);
    }

    private void trackPointSelected(Marker marker){
        //make info window appear
        RelativeLayout infoContainer = (RelativeLayout) findViewById(R.id.info);
        LinearLayout.LayoutParams  params = (LinearLayout.LayoutParams) infoContainer.getLayoutParams();
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        infoContainer.setLayoutParams(params);

        TrackPoint trackPoint = (TrackPoint) marker.getTag();
        Log.d(TAG, "trackpoint index " + trackPoint.getHurricane().getTrackPoints().indexOf(trackPoint));
        if(trackPoint.getHurricane().getTrackPoints().indexOf(trackPoint) == (trackPoint.getHurricane().getTrackPoints().size()-1)){
            findViewById(R.id.forwardButton).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.forwardButton).setVisibility(View.VISIBLE);
        }
        if(trackPoint.getHurricane().getTrackPoints().indexOf(trackPoint) == 0){
            findViewById(R.id.backButton).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        }
        selectedTrackPoint = trackPoint;
        selectedHurricane = trackPoint.getHurricane();
        map.animateCamera(CameraUpdateFactory.newLatLng(trackPoint.getLatLng()));
        trackPoint.displayInfo(instance);
    }

    private void hurricaneSelected(Polyline polyline){
        //make info window appear
        RelativeLayout infoContainer = (RelativeLayout) findViewById(R.id.info);
        LinearLayout.LayoutParams  params = (LinearLayout.LayoutParams) infoContainer.getLayoutParams();
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        infoContainer.setLayoutParams(params);

        Hurricane hurricane = (Hurricane) polyline.getTag();

        //make forward/backward button disappear
        if(hurricaneList.indexOf(hurricane) == (hurricaneList.size()-1)){
            findViewById(R.id.forwardButton).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.forwardButton).setVisibility(View.VISIBLE);
        }
        if(hurricaneList.indexOf(hurricane) == 0){
            findViewById(R.id.backButton).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        }
        selectedHurricane = hurricane;
        selectedTrackPoint = null;
        //dispay info and markers

        hurricane.displayInfo(instance);
        for(Marker marker: markerList) {
            TrackPoint point = (TrackPoint) marker.getTag();
            if(point.getHurricane().equals(hurricane)) {
                marker.setVisible(true);
            }
            else{
                marker.setVisible(false);
            }
        }
        zoomToFitTrackPoints(hurricane.getTrackPoints(), map);

        for(Polyline line: polylineList){
            if(!line.getTag().equals(selectedHurricane)){
                int color = line.getColor();
                if(color == EXTRATROP)
                    line.setColor(FADEDEXTRATROP);
                else if(color == TROPDEPR)
                    line.setColor(FADEDTROPDEPR);
                else if(color == TROPSTORM)
                    line.setColor(FADEDTROPSTORM);
                else if(color == CATONE)
                    line.setColor(FADEDCATONE);
                else if(color == CATTWO)
                    line.setColor(FADEDCATTWO);
                else if(color == CATTHREE)
                    line.setColor(FADEDCATTHREE);
                else if(color == CATFOUR)
                    line.setColor(FADEDCATFOUR);
                else if(color == CATFIVE)
                    line.setColor(FADEDCATFIVE);
            }
            else{
                int color = line.getColor();
                if(color == EXTRATROP || color == FADEDEXTRATROP)
                    line.setColor(EXTRATROP);
                else if(color == TROPDEPR || color == FADEDTROPDEPR)
                    line.setColor(TROPDEPR);
                else if(color == TROPSTORM || color == FADEDTROPSTORM)
                    line.setColor(TROPSTORM);
                else if(color == CATONE || color == FADEDCATONE)
                    line.setColor(CATONE);
                else if(color == CATTWO || color == FADEDCATTWO)
                    line.setColor(CATTWO);
                else if(color == CATTHREE || color == FADEDCATTHREE)
                    line.setColor(CATTHREE);
                else if(color == CATFOUR || color == FADEDCATFOUR)
                    line.setColor(CATFOUR);
                else if(color == CATFIVE || color == FADEDCATFIVE)
                    line.setColor(CATFIVE);
            }
        }
    }

    private boolean contains(int[] array, int num){
        for(int n: array){
            if(n==num){
                return true;
            }
        }
        return false;
    }

}
