package martin.noaahurricanetracks;

import com.google.android.gms.maps.model.LatLng;

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