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
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import SCADASite.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class WashCoSCADAMonitor extends JFrame implements WashCoSCADAConstants, Runnable
{
   private static final Logger log = Logger.getGlobal();
   
    public static void main(String[] args)
    {
        args = new String[1];
        args[0] = "v";
        
        log.setLevel(Level.ALL);

        if (args.length != 0)
        {
            dispatch(args);
        }
        
        WashCoSCADAMonitor frame = new WashCoSCADAMonitor();
        frame.start();
        frame.setVisible(true);
    }
    
    private JPanel controls;
    private JButton monitorButton, user;
    private MapPanel mp;
    private SitePanel sp;
    private InfoPanel infop;
    private Thread monitor = null;
    private ArrayList<SCADASite> sites;
    private ArrayList<SitePoint> points;
    private boolean newDataIncomming = true;
    private boolean initSites = false;
    private boolean initStream = true;
    private int numSites = 0;
    private boolean monitoring = false;
    private Socket scadaConnection = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private boolean gotSitesOnce = false;
    private Scanner fileIn = null;
    private int atSite;
    private SCADASite siteToMon = null;
    private int siteToMonitor = -1;
    
    public WashCoSCADAMonitor()
    {
        sites = new ArrayList<SCADASite>();
        points = new ArrayList<SitePoint>();
        
        setSize(new Dimension(FRAME_WIDTH,FRAME_HEIGHT));
        this.setLayout(new BorderLayout());
        setTitle("Washington County SCADA System");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        
        monitor = new Thread(this);
        
        //this.makeControlPanel();
        mp = new MapPanel();     
        sp = new SitePanel();
        infop = new InfoPanel();  
        
        this.addMouseListener(new clickListener());
        this.add(mp, BorderLayout.CENTER);
        //this.add(controls, BorderLayout.EAST);
        this.add(sp, BorderLayout.SOUTH);
        //this.add(infop, BorderLayout.EAST);
        monitor = new Thread(WashCoSCADAMonitor.this);
    }
    
    private void makeControlPanel()
    {
        monitorButton = new JButton("Monitor");
        monitorButton.addActionListener(new StartListener());
        user = new JButton("Admin*");
        monitorButton.setBackground(Color.GREEN);
        controls = new JPanel();
        controls.setPreferredSize(new Dimension(100, 600));
        controls.setLayout(new GridLayout(2,1));
        controls.add(monitorButton);
        controls.add(user);
    }
    
    public void start()
    {
        monitor.start();
    }
    
    @Override
    public void run()
    {
        
        if(initStream)
        {
            atSite = 0;
            
            File serverInfo = new File("server.ini");

            try
            {
            fileIn = new Scanner(serverInfo);
            } catch(FileNotFoundException ex)
            {
                log.log(Level.SEVERE, "Configuration File: server.ini not found.");
                JOptionPane.showMessageDialog(null, "Configuration File: server.ini not found.");
            }

            try 
            {
                
                scadaConnection = new Socket(fileIn.nextLine().trim(), 10000);
                out = new ObjectOutputStream(scadaConnection.getOutputStream());
                in = new ObjectInputStream(scadaConnection.getInputStream());
                initStream = false;
                initSites = true;
                log.log(Level.INFO, "Made connection to: {0}", scadaConnection.getInetAddress().toString());
            } catch (UnknownHostException e) 
            {
                log.log(Level.WARNING, "Unknown Host.  Check ini file.  Contact your administrator is this persists.");
                JOptionPane.showMessageDialog(this, "Unknown Host.  Check ini file.  Contact your administrator is this persists.");
            } catch (IOException e) 
            {
                log.log(Level.WARNING, "Had trouble connecting to server.  Contact your administrator is this persists.");
                JOptionPane.showMessageDialog(this, "Had trouble connecting to server.  Contact your administrator is this persists.");
            }
            
        }
        
        while(initSites)
        {
            try 
            {
                try 
                {
                    Object temp = in.readObject(); 
                    if(temp instanceof String)
                    {
                        initSites= false;
                        monitoring = true;
                        if(!gotSitesOnce)
                        {
                        mp.setSitePoints(points);
                        gotSitesOnce = true;
                        }
                        sp.clearText();
                        sp.setText("Monitor Initialized.");
                    }
                    else
                    {
                        numSites++;
                        SCADASite tSite = (SCADASite) temp;
                        sites.add(tSite);
                        
                        int alarmInt = 0;
                        if(tSite.getAlarm())
                            alarmInt = 2;
                        else if(tSite.getWarning())
                            alarmInt = 1;
                        points.add(new SitePoint(tSite.getLon(), tSite.getLat(), alarmInt, tSite.getName()));
                    }
                } catch (ClassNotFoundException ex) 
                {
                    log.log(Level.SEVERE,"Error processing Sites.");
                }


            } catch (IOException ex) 
            {
                log.log(Level.SEVERE, ex.getMessage());
            }
        }
        
        log.log(Level.INFO, "Going into monitoring mode.");
        
        while(monitoring)
        {
            try 
            {

                try 
                {
                    Object temp = in.readObject(); 
                    
                    if(temp instanceof String)
                    {
                        atSite = 0;
                        log.log(Level.INFO, "Got data");
                    }
                    else if(temp instanceof SCADASite)
                    {
                        SCADASite tSite = (SCADASite) temp;
                        log.log(Level.FINE, "Processing site: {0}", tSite.toString());
                        log.log(Level.FINE, "Alarm status: {0}", tSite.getAlarm());
                        log.log(Level.FINE, "Warning status: {0}", tSite.getAlarm());
                        log.log(Level.FINEST, "Value of atSite: {0}", atSite);
                        
                        System.out.println(tSite.getWarning());
                        if(tSite.getAlarm())
                        {
                            points.get(atSite).setAlarm(2);
                        }
                        else if(tSite.getWarning())
                        {
                            System.out.println("It's warning.");
                            points.get(atSite).setAlarm(1);
                        }
                        else
                            points.get(atSite).setAlarm(0);
                        
                        if(siteToMon != null && tSite.getName().equals(siteToMon.getName()))
                        {
                            sp.setText(tSite.getStatus());

                        }
                        
                        sp.repaint();          
                        mp.repaint();
                        
                        atSite++;
                    }
                } catch (ClassNotFoundException ex) 
                {
                    log.log(Level.SEVERE, "Error processing Sites.");
                }


            } catch (IOException ex) 
            {
                log.log(Level.SEVERE, ex.toString());
            }
        }
        
        log.log(Level.SEVERE, "Monitor thread ended.");
    }
    


    private class StartListener implements ActionListener {

        public StartListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) 
        {
            /*if(initSites)
            {
                monitor = new Thread(WashCoSCADAMonitor.this);
                monitor.start();
                monitorButton.setText("Stop");
                monitorButton.setBackground(Color.RED);
            }
            else if(!monitoring)
            {
                monitor = new Thread(WashCoSCADAMonitor.this);
                monitor.start();
                monitoring = true;
                monitorButton.setText("Stop");
                monitorButton.setBackground(Color.RED);
            }
            else
            {
                System.out.println("Stopped!");
                monitoring = false;
                monitorButton.setText("Start");
                monitorButton.setBackground(Color.GREEN);
            }
          mp.buttonClick(ae);*/
        }
    }
    
    private class clickListener implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent me) 
        {
            int xClick = me.getX();
            int yClick = me.getY();
            
            for(int i = 0; i < sites.size(); i++)
            {
                SCADASite ss = sites.get(i);
                /*
                System.out.println("Site X: " + ss.getLon());
                System.out.println("Site Y: " + ss.getLat());
                System.out.println("Your X: " + xClick);
                System.out.println("Your Y: " + (yClick - WashCoSCADAConstants.FRAME_TITLE_OFFSET));
                */
                if(Math.abs(xClick - ss.getLon()) < CLICK_DISTANCE && 
                        Math.abs(yClick - ss.getLat() - FRAME_TITLE_OFFSET) < CLICK_DISTANCE)
                {
                    
                    siteToMonitor = i;
                    siteToMon = ss;
                    sp.setText(ss.getStatus());
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseExited(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
  
    private class InitThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private class GetSitesThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private class MonitorThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    static void dispatch(String[] args)
    {
        for(String s : args)
        {
            s = s.replaceAll("-", "");
            char command = s.charAt(0);
            
            switch (command)
            {
                case 'v':
                    log.setLevel(Level.ALL);
                try 
                {
                    FileHandler fh = new FileHandler("clientlog.xml");
                    log.addHandler(fh);
                } 
                catch (IOException ex) 
                {
                    Logger.getGlobal().info(ex.toString());
                } 
                catch (SecurityException ex) 
                {
                    Logger.getGlobal().info(ex.toString());
                }
                default:
            
            
            }
        }
    }
}
