/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
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
    boolean crit = false, warn = false;
    private File soundFile;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private SourceDataLine sourceLine;
    private int LEFT_INDENT = 15;
    private boolean firstAlarm = true;
    private final int BUFFER_SIZE = 128000;
    
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
            
            crit = false;
            warn = false;
                    
            if(site.getAlarm()) 
            {
                color = "red";
                crit = true;
                playSound();
                firstAlarm = false;
            } else if(site.getWarning()) {
                color = "orange";
                warn = true;
                firstAlarm = true;
            } else {
                color = "green";
                firstAlarm = true;
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
        
        Graphics2D g2 = (Graphics2D) grphcs;
        
        String toDisplay = "Normal";
        if(crit)
        {
            g2.setColor(Color.red);
            toDisplay = "ALARM";
        }
        else if(warn)
        {
            g2.setColor(Color.orange);
            toDisplay = "WARNING";
        }
        else
        {
            g2.setColor(Color.green.darker());
            toDisplay = "Normal";
        }
        
        g2.setFont(new Font("Calibri", Font.BOLD, 60));
        g2.drawString(toDisplay, 10, 60);
    }
    
        public void playSound()
    {

        String strFilename = "beep.wav";

        try {
            soundFile = new File(strFilename);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e){
            e.printStackTrace();
           System.exit(1);
        }

        audioFormat = audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }
    
    
}
