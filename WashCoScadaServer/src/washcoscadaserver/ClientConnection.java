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
import java.net.*;

/**
 *
 * @author Avogadro
 */
public class ClientConnection 
{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int connectionAttempts;
    private String ip;
    
    public ClientConnection(Socket aSocket) throws IOException
    {
        socket = aSocket;
        ip = socket.getInetAddress().getHostAddress();
        socket.setSoTimeout(5000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        connectionAttempts = 0;
    }
    
    public String readString() throws IOException, ClassNotFoundException
    {
        Object temp = in.readObject();
        if(temp instanceof String)
            return (String) temp;
        else
            return "Error in Stream, not a String.";
    }
    
    public void connectionProblem()
    {
        connectionAttempts++;
    }
    
    public boolean connectionDown()
    {
        return connectionAttempts > 10;
    }
    
    public boolean shutDownConnection()
    {
        try
        {
        in.close();
        out.close();
        socket.close();
        return true;
        } catch (IOException e)
        {
            System.out.println("Problem closing socket.");
        }
        
        return false;
    }
    
    public void printSite(SCADASite site) throws IOException
    {
        out.writeObject(site);
    }
    
    public void printString(String message) throws IOException
    {
        out.writeObject(message);
    }
    
    public void resetOutStream() throws IOException
    {
        out.reset();
    }
    
    public boolean isClosed()
    {
        System.out.println(socket.isClosed());
        return socket.isClosed();
    }
    
    public String getIP()
    {
        return ip;
    }
    
    public Socket getSocket()
    {
        return socket;
    }
}
