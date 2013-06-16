/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.wimpi.modbus.*;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.*;

/**
 *
 * @author Avogadro
 */
public class SCADATester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, Exception 
    {
        InetAddress IP = InetAddress.getByName("192.168.41.30");
        ModbusTCPMaster mbm = new ModbusTCPMaster(IP.getHostAddress(), 502);
        System.out.println("About to connect");
        mbm.connect();
        System.out.println("Connected");
        BitVector bv = mbm.readInputDiscretes(1, 1);
        System.out.println(bv.getBit(0));
        mbm.disconnect();
    }
}
