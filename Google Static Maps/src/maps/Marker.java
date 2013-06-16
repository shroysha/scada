/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maps;

/**
 *
 * @author Shawn
 */
public class Marker {
    
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    public static final String GREEN = "green";
    
    private double longitude, latitude;
    private String color;
    private final Map map;

    public Marker(Map map, double latitude, double longitude, String color) {
        this.map = map;
        this.longitude = longitude;
        this.latitude = latitude;
        this.color = color;
    }
    
    @Override
    public String toString() {
        return "color:" + color + "|" + latitude + "," + longitude;
    }

    public void setColor(String color) {
        if(!this.color.equals(color)) {
            this.color = color;
            map.update();
        }
    }

    public void setLatitude(double lat) {
        if(latitude != lat) {
            latitude = lat;
            map.update();
        }
    }

    public void setLongtitude(double longT) {
        if(longitude != longT) {
            longitude = longT;
            map.update();
        }
    }

    public String getColor() {
        return color;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    
    
    
}
