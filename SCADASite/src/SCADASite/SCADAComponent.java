/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avogadro
 */
public class SCADAComponent implements Serializable
{
    private String name;
    private InetAddress IP;
    private boolean ModBus = false;
    private ArrayList<Discrete> discretes = new ArrayList<Discrete>();
    private ArrayList<Register> registers;
    
    public SCADAComponent(String aName, String aIP, int aIsModBus, ArrayList<Discrete> aDiscretes, ArrayList<Register> aRegisters)
    {
        name = aName;
        try 
        {
            IP = InetAddress.getByName(aIP);
        } catch (UnknownHostException ex) 
        {
            Logger.getLogger(SCADAComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(aIsModBus == 1)
            ModBus = true;
        
        discretes = aDiscretes;
        registers = aRegisters;
    }
    
    public String getName()
    {
        return name;
    }
    
    public InetAddress getIP()
    {
        return IP;
    }
    
    public boolean isModBus()
    {
        return ModBus;
    }
    
    public ArrayList<Discrete> getDiscretes()
    {
        return discretes;
    }
    
    public ArrayList<Register> getRegisters()
    {
        return registers;
    }
    
    @Override
    public String toString()
    {
        return "\n\nName: " + name + "\nIP: " + IP + "\nModBus Enabled: " + ModBus;
    }
    
}
