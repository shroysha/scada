/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;
import java.util.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.*;

/**
 *
 * @author Avogadro
 */
public class SCADASite implements Serializable
{
    private String name, statusString, critInfo;
    private double lon, lat;
    private ArrayList<SCADAComponent> components = new ArrayList<SCADAComponent>();
    private final int DISCRETE_OFFSET = 10001;
    private final int REGISTER_OFFSET = 30001;
    private boolean alarm, warning, connected, newAlarm;
    private long startdis; 
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private Date date;

    public SCADASite(String aName, String aLat, String aLon, ArrayList<SCADAComponent> scs)
    {
        name = aName;
        lon = Double.parseDouble(aLon);
        lat = Double.parseDouble(aLat);
        components = scs;
        alarm = false;
        statusString = "";
        startdis = -1;
        connected = true;
        newAlarm = true;
        date = new Date();
    }
    
    //Returns the SCADAComponents
    public ArrayList<SCADAComponent> getComponents()
    {
        return components;
    }
    
    @Override
    public String toString()
    {
        String give = name + "\n" + lon + lat + "\n" + components;
        return give;
    }
    
    public String getName()
    {
        return name;
    }
    
    public double getLat()
    {
        return lat;
    }
    
    public double getLon()
    {
        return lon;
    }
    
    //Checking for alarms by going through all of the SCADAComponents
    public void checkAlarms()
    {
        statusString = this.getName() + "\n";
        //alarm = false;
        //warning = false;
   
        for(int siteid = 0; siteid < components.size(); siteid++)
            {
                SCADAComponent sc = components.get(siteid);
                if(sc.isModBus())
                {
                    if(connected)
                        statusString += sc.getName() + "\n" + dateFormat.format(date) + "\n";
                    else
                        statusString += "Disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                    
                    
                    try 
                    {
                        ArrayList<Discrete> discretes = sc.getDiscretes();
                        ArrayList<Register> registers = sc.getRegisters();
                        
                        InetAddress astr = sc.getIP();
                        ModbusTCPMaster mbm = new ModbusTCPMaster(astr.getHostAddress(), 502);
                        mbm.connect();
                        
                        
                        for(int i = 0; i < discretes.size(); i++)
                        {
                            Discrete currentD = discretes.get(i);
                            int addy = currentD.getPort();
                            String dname = currentD.getName();
                            
                            //System.out.println(addy);
                            BitVector bv = mbm.readInputDiscretes(addy-DISCRETE_OFFSET, 1);
                            statusString += "\n"+ dname + " at Discrete: \t" + addy + ":\t";
                            if(bv.getBit(0) && currentD.getWarning() == 2)
                            {
                                statusString += "CRITICAL\n";
                                alarm = true;
                                warning = false;
                                critInfo = currentD.getName();
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 1)
                            {
                                statusString += "Warning\n";
                                warning = true;
                                alarm = false;
                                newAlarm = true;
                                critInfo = "";
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 0)
                            {
                                statusString += "Not Normal\n";
                                alarm = false;
                                warning = false;
                                newAlarm = true;
                                critInfo = "";
                            }
                            else
                            {
                                statusString += "Normal\n";
                                alarm = false;
                                warning = false;
                                newAlarm = true;
                                critInfo = "";
                            }
                            
                        }
                        
                        for(int i = 0; i < registers.size(); i++)
                        {
                            int addy = registers.get(i).getPort();
                            String rname = registers.get(i).getName();
                            
                            InputRegister[] ir = mbm.readInputRegisters(addy-REGISTER_OFFSET, 1);
                            statusString += "\n" + rname + " at Register: \t" + addy + ":\t" + ir[0].getValue() + "\n";
                        }
                        
                        //Got through connections
                        date = new Date();
                        connected = true;
                        startdis = -1;
                        
                        mbm.disconnect();
                    }
                    catch(Exception e)
                    {
                        System.out.println("Disconnected");
                        statusString += "Disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                        warning = true;
                        connected = false;
                        siteid = components.size();
                    }
                }
            }
    }
    public String getStatus()
    {
            return statusString;
    }
    
    public boolean getWarning()
    {
        return warning;
    }
    
    public boolean getAlarm()
    {
        if(newAlarm)
            newAlarm = false;
        return alarm;
    }
    
    public String getCritcialInfo()
    {
        return critInfo;
    }
    public boolean isNewAlarm()
    {
        return alarm && newAlarm;
    }
    
    public boolean connected()
    {
        return connected;
    }
    
    public boolean getConnected()
    {
        return connected;
    }
}
