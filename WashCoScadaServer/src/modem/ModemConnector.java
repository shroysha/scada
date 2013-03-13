/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modem;

import java.io.*;
import java.net.*;
import java.util.Stack;

public class ModemConnector
{
    String ip = "";
    String port = "";
    Socket modem;
    OutputStreamWriter out;
    InputStreamReader in;
    ModemListener listen;
    String lastPin = "";
    boolean readyForRead;
    
    Thread listenThread;
    
    final String setDefaults = "ATZ\r";
    final String voiceCommand = "AT+FCLASS=8\r";
    final String voiceInit = "ATVIP\r";
    final String hangup = "ATH0\r";
    
    public ModemConnector(String aIp, String aPort) throws UnknownHostException, IOException
    {
        ip = aIp;
        port = aPort;
        modem = new Socket(ip, Integer.parseInt(port));
        out = new OutputStreamWriter(modem.getOutputStream());
        in = new InputStreamReader(modem.getInputStream());
        
    }

    public void start()
    {        
        listenThread = new Thread(new ModemListener());
        listenThread.start();
        
        if (init() == 1)
        {
            System.out.println("Modem Ready.");
        }
        else
        {
            System.out.println("Uh oh.");
        }
    }
    
    public int init()
    {
        try
        {
            out.write(hangup, 0, hangup.length());
            out.flush();
                try 
                {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            out.write(setDefaults, 0, setDefaults.length());
            out.flush();
                        try 
                {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            out.write(voiceCommand, 0, voiceCommand.length());
            out.flush();
                        try 
                {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            out.write(voiceInit, 0, voiceInit.length());
            out.flush();
                        try 
                {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            return 1;
        }
        catch(IOException ex)
        {
            return -2;
        }
    }
    
    public synchronized boolean hasRead()
    {
        return readyForRead;
    }
    
    public String read()
    {
        if(readyForRead)
        {
            String temp = lastPin;
            lastPin = "";
            readyForRead = false;
            return temp;
        }
        else
            return null;
    }
    
    private class ModemListener implements Runnable
    {

        @Override
        public void run() 
        {
            String message = "";
            boolean ring = false;
            boolean zero = false;
                    
            while(true)
            {
                try 
                {
                    char inc = (char) in.read();
                    
                    if (inc == 'R')
                    {
                        message = "";
                        ring = true;
                    }
                    
                    if(ring && !zero)
                    {
                        if (inc == '0')
                            zero = true;
                    }
                    
                    if(ring && zero)
                    {
                        if (inc > '0' && inc <= '9')
                            message += inc;
                        if (inc == '#' || inc == '*')
                        {
                            lastPin = message;
                            ring = false;
                            zero = false;
                            readyForRead = true;
                            init();
                            System.out.println("Hang up." + lastPin + readyForRead);
                            notifyAllAckListeners(message);
                        }
                    }
                    
                } 
                catch (IOException ex) 
                {
                    System.out.println("Error");
                }
            }
            
        }
        
    }
    
    private Stack<ReadListener> readListeners = new Stack();
    
    public void notifyAllAckListeners(String pin) {
        for(ReadListener listener: readListeners) {
            listener.onRead(pin);
        }
    }
    
    public void addReadListener(ReadListener listener) {
        readListeners.add(listener);
    }
    
    public void removeReadListener(ReadListener listener) {
        readListeners.remove(listener);
    }
}
