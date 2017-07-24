package martin.noaahurricanetracks;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 7/6/2017.
 */

public class TrackPoint{
    private Hurricane hurricane;
    private String ISO_time;
    private String nature;
    private LatLng latLng;
    private float wind;
    private float pressure;
    private String center;
    private String trackType;
    private Marker marker;

    public Hurricane getHurricane(){ return hurricane;}

    public String getISO_time() {
        return ISO_time;
    }

    public String getNature() {
        return nature;
    }

    public LatLng getLatLng(){
        return latLng;
    }

    public float getWind() {
        return wind;
    }

    public float getPressure() {
        return pressure;
    }

    public String getCenter() {
        return center;
    }

    public String getTrackType() {
        return trackType;
    }

    public Marker getMarker(){
        return marker;
    }

    public Hurricane.Category getCategory(){
        if (nature.equals("ET")){
            return Hurricane.Category.ET;
        }
        if(nature.equals("DS")) {
            return Hurricane.Category.TD;
        }
        if (wind >= 137.0){
            return Hurricane.Category.C5;
        }
        if (wind >= 113.0) {
            return Hurricane.Category.C4;
        }
        if (wind >= 96.0) {
            return Hurricane.Category.C3;
        }
        if (wind >= 83.0) {
            return Hurricane.Category.C2;
        }
        if (wind >= 64.0) {
            return Hurricane.Category.C1;
        }
        if (wind >= 34.0) {
            return Hurricane.Category.TS;
        }
        return Hurricane.Category.TD;
    }

    public void displayInfo(MapsActivity instance){
        TextView tv = (TextView) instance.findViewById(R.id.trackPointTitleTextView);
        tv.setText(this.getHurricane().getName() + " " + this.getHurricane().getSeason());
        TextView tv2 = (TextView) instance.findViewById(R.id.trackPointInfoTextView);
        tv2.setText("Date: " + this.getISO_time() + "\n" +
                "Pressure(mb): " + this.getPressure() + " Wind(kt): " + this.getWind());
        //set up chart
        LineChart chart = (LineChart) instance.findViewById(R.id.chart);
        List<Entry> pressureEntries = new ArrayList<Entry>();
        List<Entry> windEntries = new ArrayList<Entry>();
        for(int i = 0; i<hurricane.getTrackPoints().size(); i++){
            pressureEntries.add(new Entry(i, hurricane.getTrackPoints().get(i).getPressure()));
            windEntries.add(new Entry(i, hurricane.getTrackPoints().get(i).getWind()));
        }

        LineDataSet pressureDataSet = new LineDataSet(pressureEntries, "Pressure");
        pressureDataSet.setColor(Color.BLUE);
        pressureDataSet.setDrawValues(false);
        pressureDataSet.setDrawCircles(false);
        pressureDataSet.setAxisDependency(chart.getAxisLeft().getAxisDependency());

        LineDataSet windDataSet = new LineDataSet(windEntries, "Wind");
        windDataSet.setColor(Color.RED);
        windDataSet.setDrawValues(false);
        windDataSet.setDrawCircles(false);
        windDataSet.setAxisDependency(chart.getAxisRight().getAxisDependency());

        LineData lineData = new LineData();
        lineData.addDataSet(pressureDataSet);
        lineData.addDataSet(windDataSet);
        chart.setData(lineData);
        //highlight marker
        float markerIndex = hurricane.getTrackPoints().indexOf(this);
        chart.highlightValue(new Highlight(markerIndex,0,0),false);

        chart.invalidate();
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    TrackPoint(Hurricane hurricane,String time, String nature, LatLng latLng, float wind, float pressure, String center, String trackType){
        this.hurricane = hurricane;
        this.ISO_time = time;
        this.nature = nature;
        this.latLng = latLng;
        this.wind = wind;
        this.pressure = pressure;
        this.center = center;
        this.trackType = trackType;
    }


}