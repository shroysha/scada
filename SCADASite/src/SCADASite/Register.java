/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;

/**
 *
 * @author Avogadro
 */
public class Register implements Serializable
{
    final String name;
    final int port;
    final int warningType;
    
    public Register(String aName, int aPort, int aWarning)
    {
        name = aName;
        port = aPort;
        warningType = aWarning;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getWarning()
    {
        return warningType;
    }
    
    
}
