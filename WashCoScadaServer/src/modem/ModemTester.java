/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modem;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avogadro
 */
public class ModemTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        ModemConnector mc = null;
        try 
        {
            mc = new ModemConnector("192.168.73.12", "4000");
        } catch (UnknownHostException ex) {
            Logger.getLogger(ModemTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModemTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        mc.start();

        while(true)
        {
            if(mc.hasRead())
            {
                System.out.println("Hai");
                String read = mc.read();
            }
        }
    }
}
