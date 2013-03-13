/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WashCoSCADAMonitor;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avogadro
 */
public class CommandClient
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        Socket scadaConnection = null;
        PrintWriter out = null;
        BufferedReader in = null;
                
        try 
        {
            scadaConnection = new Socket("localhost", 10000);
            out = new PrintWriter(scadaConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(scadaConnection.getInputStream()));
        } catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host: taranis.");
            System.exit(1);
        } catch (IOException e) 
        {
            System.err.println("Couldn't get I/O for the connection to: taranis.");
            System.exit(1);
        }

            
            while(true)
            {
                try
                {
                System.out.println(in.readLine());
                }
                catch(IOException e)
                {
                
                }
                
            }

    }

    
}
