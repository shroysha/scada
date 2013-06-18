/*
 * Author: Peter O'Connor
 * Purpose: To implement SCADA Monitoring throughout Washington County
 * Version: 1.0a
 * 
 * Contact: avogadrosg1@gmail.com
 * 
 */
package washcoscadaserver;

import SCADASite.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.JTextArea;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import modem.PageWithModem;

/**
 *
 * @author Peter O'Connor
 * 
 */
public class SCADAServer 
{
    Logger log = Logger.getGlobal();
    ArrayList<SCADASite> sites = new ArrayList<SCADASite>();
    File siteList = new File("SiteConfigs.dat");
    int second = 0;
    
    private ScheduledExecutorService scheduler;
    private final long initDelay;
    private final long delay;
    private final int DISCRETE_OFFSET = 10001;
    private final int REGISTER_OFFSET = 30001;
  
    private static final int NUM_THREADS = 1;
    private static final boolean DONT_INTERRUPT_IF_RUNNING = false;
    
    private ArrayList<ClientConnection> clients = new ArrayList<ClientConnection>();
    
    private boolean connected = false;
    private boolean listening = true;
    private PrintWriter clientPrinter;
    private String totalStatus = "";
    private final int DEVICE_INFO_LINES = 4;
    private JTextArea textArea;
    
    private PageWithModem pageServ;
    private int currentJobID = 1;
    
    public SCADAServer()
    {
        clientPrinter = null;
        scheduler = null;
        pageServ = null;
        initDelay = 5;
        delay = 5;
        
        log.info("Starting up sites.");
        this.startUpSites();
        
        
        Thread cc = new Thread(new ClientConnector());
        cc.start();

        log.log(Level.INFO, "Started Client Listening Thread.");
    }
    
    private void startUpSites()
    {
        boolean starting = true;
        String name = "";
        String lon = "";
        String lat = "";
        String compName = "";
        String compIP = "";
        int isModBus = 0;
        ArrayList<Discrete> discreteList = new ArrayList<Discrete>();
        ArrayList<Register> registerList = new ArrayList<Register>();
        ArrayList<SCADAComponent> components = new ArrayList<SCADAComponent>();
       
        try
        {
            Scanner in = new Scanner(siteList);
            
            while(in.hasNextLine())
            {   
                String stuff = in.nextLine();
                
                log.log(Level.INFO, "Processing line: {0}", stuff);
                
                if(stuff.equals("") || stuff.charAt(0) == '#')
                    continue;
                
                if(stuff.contains("Site Name"))
                {
                    name = stuff.substring(stuff.indexOf("=")+1).trim();
                }
                
                if(stuff.contains("Lat"))
                {
                    lat = stuff.substring(stuff.indexOf("=")+1).trim();
                }
                
                if(stuff.contains("Long"))
                {
                    lon = stuff.substring(stuff.indexOf("=")+1).trim();
                }
                
                if (stuff.contains("Device Name"))
                {
                    compName = stuff.substring(stuff.indexOf("=")+1).trim();
                    
                    String temp = in.nextLine();
                    compIP = temp.substring(temp.indexOf("=")+1).trim();
                    
                    temp = in.nextLine();
                    isModBus = Integer.parseInt(temp.substring(temp.indexOf("=")+1).trim());
                    
                    //Setup the Discrete List
                    String discretes = in.nextLine();
                    StringTokenizer tokenizer = new StringTokenizer(discretes.substring(discretes.indexOf("=")+1), ",\n");
                    
                    while(tokenizer.hasMoreTokens())
                    {
                        int warningType = 0;
                        String discreteTemp = tokenizer.nextToken();
                        String discreteName = discreteTemp.substring(0,discreteTemp.indexOf(":"));
                        discreteTemp = discreteTemp.substring(discreteTemp.indexOf(":") + 1);
                        
                        int discretePort = Integer.parseInt(discreteTemp.substring(0, discreteTemp.length()-1));
                        String warningStr = discreteTemp.substring(discreteTemp.length()-1);
                        
                        if(warningStr.equals("w"))
                        {
                            warningType = 1;
                        }
                        else if(warningStr.equals("c"))
                        {
                            warningType = 2;
                        }
                        
                        discreteList.add(new Discrete(discreteName, discretePort, warningType));
                    }
                    
                    //Setup the Register List
                    String registers = in.nextLine();
                    tokenizer = new StringTokenizer(registers.substring(registers.indexOf("=")+1), ",\n");
                    
                    while(tokenizer.hasMoreTokens())
                    {
                        int warningType = 0;
                        String registerTemp = tokenizer.nextToken();
                        String registerName = registerTemp.substring(0,registerTemp.indexOf(":"));
                        registerTemp = registerTemp.substring(registerTemp.indexOf(":") + 1);
                        
                        int registerPort = Integer.parseInt(registerTemp.substring(0, registerTemp.length()-1));
                        String warningStr = registerTemp.substring(registerTemp.length()-1);
                        
                        if(warningStr.equals("w"))
                        {
                            warningType = 1;
                        }
                        else if(warningStr.equals("c"))
                        {
                            warningType = 2;
                        }
                        
                        registerList.add(new Register(registerName, registerPort, warningType));
                    }
                    
                    //Add the compnent to the ArrayList
                    components.add(new SCADAComponent(compName, compIP, isModBus, discreteList, registerList));
                    
                  
                }
                
                //Finally, add the new site
                if(stuff.equalsIgnoreCase("end"))
                {
                    log.info("Reached end of site!");
                    sites.add(new SCADASite(name, lat, lon, components));
                    name = "";
                    lat = "";
                    lon = "";
                    compName = "";
                    compIP = "";
                    isModBus = 0;
                    discreteList = new ArrayList<Discrete>();
                    registerList = new ArrayList<Register>();
                    components = new ArrayList<SCADAComponent>();
                }              
            }
            
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found!");
        }
        
    }
    
    public String getSites()
    {
        String info = "";
        for(SCADASite ss : sites)
            info += ss.toString();
        
        return info;
    }
    
    private synchronized void printToClients()
    {
        log.info("Printing to clients.");
        for(ClientConnection oos: clients)
        {
            try {
                oos.resetOutStream();
            } catch (IOException ex) {
                Logger.getLogger(SCADAServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.log(Level.INFO, "Printing to client:{0}", oos.getIP());
            for(SCADASite ss: sites)
            {
                try 
                {

                    log.log(Level.FINE, ss.getStatus());
                    oos.printSite(ss);
                }catch (IOException ex)
                {
                    log.log(Level.SEVERE, "Printing to client:" + oos.getIP() + " failed.");
                    log.log(Level.SEVERE, ex.toString());
                }
            }
            
            try 
            {
                oos.printString("End Sites");
                oos.resetOutStream();
                log.info("Sent to client: " + oos.getIP());
            }
            catch (IOException se)
            {
                log.log(Level.SEVERE, se.toString());
                oos.connectionProblem();
                log.log(Level.SEVERE, "Printing End Sites didn't work.");
            }
        }
        
    }
    
    private void removeClients()
    {
        for(int i = 0; i < clients.size(); i++)
        {
            if(clients.get(i).connectionDown())
            {
                log.info("Removing: " + clients.get(i).getSocket().getInetAddress());
                clients.remove(i);
            }
        }
    }
    
    private synchronized void checkForAlarms()
    {
            
        //System.out.println("Started Checking at: " + System.currentTimeMillis()/1000);
        long startSec = System.currentTimeMillis()/1000;
        log.log(Level.INFO, "Started Checking at: {0}", startSec);
        for(SCADASite ss: sites)
        {
            ss.checkAlarms();
            if(pageServ != null && pageServ.isActive() && ss.isNewAlarm()) {
                pageServ.startPage(currentJobID, ss.getCritcialInfo());
                currentJobID++;
            }
            
        }
        long endSec = System.currentTimeMillis()/1000;
        
        log.log(Level.INFO, "Stopped Checking at: {0}", endSec);
        
        this.printToClients();
        log.log(Level.INFO, "Printed to all clients.");
        this.removeClients();
        log.log(Level.INFO, "Removed all nonresponsive clients.");
        
    }
    
    public synchronized String getInformation()
    {
        totalStatus = "";
        for(SCADASite ss: sites)
            {
                    totalStatus += ss.getStatus();
            }
        return totalStatus;
    }
    
    public void startChecking()
    {
        if(scheduler == null || (scheduler != null && scheduler.isShutdown())) 
            scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        Runnable checkAlarmTask = new CheckAlarmTask();
        scheduler.scheduleWithFixedDelay(checkAlarmTask, initDelay, delay, TimeUnit.SECONDS);
        log.log(Level.INFO, "Started Alarm Listening Thread with initial delay of: {0} and continual delay of {1}", new Object[]{initDelay, delay});
        
    }
    
    public void stopChecking()
    {
        scheduler.shutdown();
    }
    
    public boolean isChecking()
    {
        if(scheduler == null)
            return false;
        else
            return !scheduler.isShutdown();
    }
    public boolean switchPaging()
    {
        if (pageServ == null)
        {
            try
            {
                pageServ = new PageWithModem();
            }
            catch(Exception ex)
            {
                log.info(ex.toString());
            }
        }
        
        if (pageServ.isActive())
        {
            pageServ.stop();
            return false;
        }
        else
        {
            pageServ.start();
            return true;
        }
    }
    
    public void clearAllPages()
    {
        if(pageServ != null && pageServ.isActive())
        {
            pageServ.stopAllRunningPages();
        }
    }
    private final class CheckAlarmTask implements Runnable 
    {
        @Override
        public void run() 
        {
            checkForAlarms(); 
        }
    }
    
    private class ClientConnector implements Runnable
    {
        int port = 10000;
        
        @Override
        public void run()
        {
            ServerSocket serverSocket = null;
            try 
            {
                serverSocket = new ServerSocket(port);
            } 
            catch (IOException e) 
            {
                log.log(Level.SEVERE, "Could not listen on port: {0}", port);
            }
            while(listening)
            {
                try 
                {
                    ClientConnection client = new ClientConnection(serverSocket.accept());
                    clients.add(client);
                    log.log(Level.INFO, "Client at: {0}", client.getIP());
                    connected = true;
                } 
                catch (IOException e) 
                {
                    log.log(Level.SEVERE, "Accept failed.");
                }
            }
        }
    }
}
