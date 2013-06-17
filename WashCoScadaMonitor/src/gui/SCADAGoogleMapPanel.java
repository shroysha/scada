/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.JPanel;
import maps.Map;
import maps.Marker;

/**
 *
 * @author Shawn
 */
public class SCADAGoogleMapPanel extends JPanel {
    
    private Map googleMap;
    private ArrayList<SCADASite> sites;
    
    public SCADAGoogleMapPanel() {
        super();
        googleMap = new Map();
        sites = new ArrayList();
    }
    
    
    public void setSCADASites(ArrayList<SCADASite> sites) {
        // remove all previous markers
        Marker[] markers = googleMap.getMarkers();
        for(Marker marker: markers) {
            googleMap.removeMarker(marker);
        }
        
        //then add the new ones
        markers = new Marker[sites.size()];
        for(int i = 0; i < markers.length; i++) {
            SCADASite site = sites.get(i);
            
            String color;
            
            if(site.getAlarm()) {
                color = "red";
            } else if(site.getWarning()) {
                color = "orange";
            } else {
                color = "green";
            }
            
            Marker marker = new Marker(googleMap, site.getLat(), site.getLon(), color);
            googleMap.addMarker(marker);
        }
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        
        int width = this.getWidth();
        int height = this.getHeight();
        
        googleMap.setWidth(width);
        googleMap.setHeight(height);
        
        Image image = googleMap.getImage();
        grphcs.drawImage(image, 0, 0, this);
    }
    
    
}
