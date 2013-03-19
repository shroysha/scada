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
import modem.PageWithModem;

/**
 *
 * @author Avogadro
 */
public class SCADAServer 
{
    ArrayList<SCADASite> sites = new ArrayList<SCADASite>();
    File siteList = new File("SiteConfigs.dat");
    //Timer poller;
    int second = 0;
    
    private final ScheduledExecutorService scheduler;
    private final long initDelay;
    private final long delay;
    private final int DISCRETE_OFFSET = 10001;
    private final int REGISTER_OFFSET = 30001;
    //private final long fShutdownAfter;
  
    /** If invocations might overlap, you can specify more than a single thread.*/ 
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
        this.startUpSites();
        initDelay = 5;
        delay = 5;
        //fShutdownAfter = 10;
        scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        
        Thread cc = new Thread(new ClientConnector());
        cc.start();
        
        pageServ = new PageWithModem();
        this.startChecking(); 
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
                    System.out.println("Reached end of site!");
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
    
    private void printToClients()
    {
        for(ClientConnection oos: clients)
        {
            
            for(SCADASite ss: sites)
            {
                try 
                {
                    System.out.println(ss.getStatus());
                    totalStatus += ss.getStatus();
                    oos.printSite(ss);
                }catch (IOException ex)
                {
                    
                }
            }
            
            try 
            {
                oos.printString("End Sites");
                oos.resetOutStream();
            }
            catch (IOException se)
            {
                se.printStackTrace();
                oos.connectionProblem();
                System.out.println("Printing End Sites didn't work.");
            }
            System.out.println("Sent to client");
            
        }
        
    }
    
    private void removeClients()
    {
        for(int i = 0; i < clients.size(); i++)
        {
            //System.out.println("I'm checking a client");
            if(clients.get(i).connectionDown())
            {
                System.out.println("Removing: " + clients.get(i).getSocket().getInetAddress());
                clients.remove(i);
            }
        }
    }
    
    private synchronized void checkForAlarms()
    {
            
        //System.out.println("Started Checking at: " + System.currentTimeMillis()/1000);
        for(SCADASite ss: sites)
        {
            ss.checkAlarms();
            if(ss.isNewAlarm()) {
                pageServ.startPage(currentJobID, ss.getCritcialInfo());
                currentJobID++;
            }
            
        }
        //System.out.println("Stopped Checking at: " + System.currentTimeMillis()/1000);
        this.printToClients();
        this.removeClients();
        
    }
    
    public String getInformation()
    {
        totalStatus = "";
        for(SCADASite ss: sites)
            {
                    //System.out.println();
                    totalStatus += ss.getStatus();
            }
        return totalStatus;
    }
    
    private void startChecking()
    {
        Runnable checkAlarmTask = new CheckAlarmTask();
        scheduler.scheduleWithFixedDelay(checkAlarmTask, initDelay, delay, TimeUnit.SECONDS);
    }
    
    private final class CheckAlarmTask implements Runnable 
    {
        @Override
        public void run() 
        {
            second+=delay;
            checkForAlarms(); 
            //printToClients(totalStatus);
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
                System.err.println("Could not listen on port: "+ port);
                System.exit(1);
            }
            while(listening)
            {
                try 
                {
                    clients.add(new ClientConnection(serverSocket.accept()));
                    System.out.println("connected.");
                    connected = true;
                } 
                catch (IOException e) 
                {
                    System.err.println("Accept failed.");
                    System.exit(1);
                }
            }
        }
    }
}
