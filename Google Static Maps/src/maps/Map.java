/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maps;

import event.MapUpdateListener;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Shawn
 */
public class Map {
    
    public static void main(String[] args) {
        final Map map = new Map();
        Marker marker = new Marker(map, 62.107733,-145.5419, "blue");
        Marker marker2 = new Marker(map, 63.259591, -144.667969, "red");
        
        map.addMarker(marker);
        map.addMarker(marker2);
        System.out.println(map.generateWebsite());
        JFrame frame = new JFrame();
        ImageIcon icon = new ImageIcon(map.getImage());
        final JLabel label = new JLabel(icon);
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
        
        map.addMapUpdateListener(new MapUpdateListener() {
            @Override
            public void onUpdate() {
                System.out.println("okay");
                label.setIcon(new ImageIcon(map.getImage()));
            }
        });
        Scanner scanner = new Scanner(System.in);
        while(true) {
            
            String line = scanner.nextLine();
            String[] cut = line.split(" ");
            double latitude = Double.parseDouble(cut[0]);
            double longitude = Double.parseDouble(cut[1]);
            String color = cut[2];
            Marker marker3 = new Marker(map, latitude, longitude, color);
            map.addMarker(marker3);
        }
    }
    
    private Stack<Marker> markers = new Stack();;
    private Stack<MapUpdateListener> updateListeners = new Stack();
    private final String BASE = "http://maps.googleapis.com/maps/api/staticmap?";
    private int width = 480, height = 480;
    private boolean imageNeedsUpdate = true;
    private Image mapImage;
    
    public Map() {
        super();
    }
    
    public void addMarker(Marker marker) {
        markers.add(marker);
        notifyMapListeners();
    }
    
    public void removeMarker(Marker marker) {
        markers.remove(marker);
        notifyMapListeners();
    }
    
    public Marker[] getMarkers() {
        Marker[] marks = new Marker[markers.size()];
        
        for(int i = 0; i < marks.length; i++) {
            marks[i] = markers.get(i);
        }
        
        return marks;
    }
    
    public void addMapUpdateListener(MapUpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public void removeMapUpdateListener(MapUpdateListener listener) {
        updateListeners.remove(listener);
    }
    
    public MapUpdateListener[] getMapUpdateListeners() {
        MapUpdateListener[] listeners = new MapUpdateListener[updateListeners.size()];
        
        for(int i = 0; i < listeners.length; i++) {
            listeners[i] = updateListeners.get(i);
        }
        
        return listeners;
    }
    
    public void update() {
        notifyMapListeners();
    }
    
    private void notifyMapListeners() {
        imageNeedsUpdate = true;
        for(MapUpdateListener listener: updateListeners) {
            listener.onUpdate();
        }
    }
    
    private String generateWebsite() {
        String website = "";
        website += BASE;
        
        website += "size=" + width + "x" + height;
        
        String markerText = "";
        for(Marker marker: markers) {
            markerText += "&markers=" + marker.toString();
        }
        
        website += markerText;
        
        website += "&sensor=false";
        
        return website;
    }
    
    @Override
    public String toString() {
        return generateWebsite();
    }

    public void setWidth(int width) {
        this.width = width;
        notifyMapListeners();
    }

    public void setHeight(int height) {
        this.height = height;
        notifyMapListeners();
    }
    
    public Image getImage() {
        try {
            if(imageNeedsUpdate) {
                mapImage = ImageIO.read(new URL(generateWebsite()));
                imageNeedsUpdate = false;
            }
            
            return mapImage;
        } catch (IOException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
