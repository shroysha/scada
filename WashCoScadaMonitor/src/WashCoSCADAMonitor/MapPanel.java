/*
 * Author: Peter O'Connor
 * Purpose: To implement SCADA Monitoring throughout Washington County
 * Version: 1.0a
 * 
 * Contact: avogadrosg1@gmail.com
 * 
 */
package WashCoSCADAMonitor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class MapPanel extends JPanel implements WashCoSCADAConstants
{
    BufferedImage background;
    boolean alarmLabelOn;
    private final int width = 1000;
    private final int height = 600;
    private final int BOTTOM_OFFSET = 15;
    private final double Y_POINT_ADJUST = 39.879741552001455;
    private final double X_POINT_ADJUST = -78.597212047634912;
    private final double X_DISTANCE_TOTAL = 1.125403047634912;
    private final double Y_DISTANCE_TOTAL = 0.556531552001455;
    private final double LON_RATIOX = 1000 / this.X_DISTANCE_TOTAL;
    private final double LAT_RATIOY = 478 / this.Y_DISTANCE_TOTAL;
    private final int BUFFER_SIZE = 128000;
    
    private File soundFile;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private SourceDataLine sourceLine;
    private int LEFT_INDENT = 15;
    private ArrayList<SitePoint> sitePoints;
    private boolean firstAlarm;
    
    public MapPanel()
    {
        sitePoints = new ArrayList<SitePoint>();
        
        firstAlarm = true;
        
        this.setPreferredSize(new Dimension(1000,600));
        this.setBackground(Color.white);
        try
        {
            background = ImageIO.read(new File("WashCoClient.jpg"));
        }
        catch(Exception e)
        {
            System.out.println("Could not find image background.");
        }
    }
    
    public void setSitePoints(ArrayList<SitePoint> aSitePoints)
    {
        this.sitePoints = aSitePoints;
        System.out.println("The size of passed sitepoints: " + aSitePoints.size());
        repaint(); 
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        g.drawImage(background, 0, 0,1000,475, this);

        
        g.setFont(new Font("Calibri", Font.BOLD, 20));
        g.setColor(Color.red);
        g.drawString("Washington County SCADA System Monitor", LEFT_INDENT, this.getHeight() - BOTTOM_OFFSET);
        
        g.setFont(new Font("Calibri", Font.BOLD, 60));
     
        boolean alarmPaint = false;
        boolean warningPaint = false;
        
        for(int i = 0; i < sitePoints.size(); i++)
        {
            SitePoint sp = sitePoints.get(i);

            if(sp.getAlarmStatus() == 1)
            {
                System.out.println("Warning in Point");
                warningPaint = true;
                g2.setColor(Color.ORANGE);
            }
            else if(sp.getAlarmStatus() == 2)
            {
                alarmPaint = true;
                g2.setColor(Color.RED);
                
                if(firstAlarm)
                {
                    this.playSound();
                    firstAlarm = false;
                }
            }
            else 
                g2.setColor(Color.GREEN.darker());
            
            
            int x = (int) sp.getX();
            int y = (int) sp.getY();
            
            
            g2.fillOval(x, y, siteCircleDiameter, siteCircleDiameter);
        }
        
        String toDisplay = "Normal";
        if(alarmPaint)
        {
            g2.setColor(Color.red);
            toDisplay = "ALARM";
        }
        else if(warningPaint)
        {
            g2.setColor(Color.orange);
            toDisplay = "WARNING";
            firstAlarm = true;
        }
        else
        {
            g2.setColor(Color.green.darker());
            toDisplay = "Normal";
            firstAlarm = true;
        }
        
        g2.drawString(toDisplay, 10, 250);
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
    
    
    
    public void mouseClick(MouseEvent me)
    {
        
    }
    
    public void buttonClick(ActionEvent me)
    {
        
    }
    
}
