/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alert;

import util.AlertListener;
import util.UpdateListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.LogListener;
import static util.Utilities.getMainDirPath;

/**
 * This class monitors the alert system
 * @author Shawn
 */
public final class AlertMonitoringSystem implements UpdateListener{
    
    private static final File configFile = new File(getMainDirPath() + "/pagingsystem/alertmonitor/settings.cfg"); 
    private static final String IP_PROPERTY = "alertServerIP", PORT_PROPERTY = "alertServerPort";

    
    private JPanel parent; //incase the AMS becomes a GUI later, we can have a parent for JOptionPanes
    private Properties props;
    
    private AlertMonitorThread amt;
    
    private Stack<AlertListener> alertListeners = new Stack();
    private Stack<UpdateListener> updateListeners = new Stack();
    private Stack<LogListener> logListeners = new Stack();
    
    private ArrayList<Alert> activeAlerts = new ArrayList();
    private Alert[] pastAlerts = new Alert[50];
    
    /**
     * 
     * @throws IOException when the config file cannot be found or there is a read error
     */
    public AlertMonitoringSystem() throws IOException {
        super();
        addUpdateListener(this);
        loadProperties();
        checkProperties();
        
        parent = new AlertMonitoringPanel(this);
        amt = new AlertMonitorThread(this);
        amt.start();
    }
    
    public void addAlertListener(AlertListener listener) {
        alertListeners.add(listener);
    }
    
    public void removeAlertListner(AlertListener listener) {
        alertListeners.remove(listener);
    }
    
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public void removeUpdateListner(UpdateListener listener) {
        updateListeners.remove(listener);
    }
    
    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }
    
    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }
    
    private void alertAllLogListeners(String log) {
        for(LogListener listener: logListeners) {
            listener.onLog(log);
        }
    }
    
    private void alertAllUpdateListeners() {
        for(UpdateListener listener: updateListeners) {
            listener.onUpdate();
        }
    }
    
    private void alertAllAlertListeners(Alert alert) {
        for(AlertListener listener: alertListeners) {
            listener.alertReceived(alert); // go through all of the listners and tell them the alert
        }
    }
    
    private void loadProperties() throws IOException {
        if(props == null)
            props = new Properties();
        
        if(!configFile.exists()) {
            String makePath = configFile.getPath().replace("settings.cfg", "");
            new File(makePath).mkdirs();
            configFile.createNewFile();
            loadProperties(); // if the config file doesnt exist, then create the config file and try to load the properties again
        } else {
            props.load(new FileInputStream(configFile)); 
        }
    }
    
    private void checkProperties() { // check if all the properties exist and have a valid entry
        checkIPv4Address();
        checkPort();
    }
    
    private void saveProperties() throws IOException {
        System.out.println("Saved");
        props.store(new FileOutputStream(configFile), "AlertMonitorSystemProperties");
    }
    
    protected String getIPv4Address() {
        return props.getProperty(IP_PROPERTY);
    }
    
    protected String getPort() {
        return props.getProperty(PORT_PROPERTY);
    }
    
    protected void setIPAddress(String address) {
        props.setProperty(IP_PROPERTY, address);
        checkIPv4Address();
        alertAllUpdateListeners();
        resetMonitoringThread();
    }
    
    protected void setPort(String port) {
        props.setProperty(PORT_PROPERTY, port);
        checkPort();
        alertAllUpdateListeners();
        resetMonitoringThread();
    }
    
    private void resetMonitoringThread() {
        if(amt != null) {
            Thread thread = amt;
            amt = new AlertMonitorThread(this);
            amt.start();
            thread.interrupt();
            
        }
    }
    /**
     * Checks the ip address until a valid ip is entered
     * Just because this method returns true, it doesn't mean that the client can connect to it
     */
    private void checkIPv4Address() { 
        while(!isValidIPv4(props.getProperty(IP_PROPERTY))) {
            String response = JOptionPane.showInputDialog(parent, "Enter the alert server's ip");
            if(response != null) {
                response = response.trim();
                setIPAddress(response);
            }
        }
    }
    
    /**
     * Checks the port and until a valid port is entered.
     * Just because this method returns true, it doesn't mean that the client can connect to it
     */
    private void checkPort() { 
        while(!isValidPort(props.getProperty(PORT_PROPERTY))) {
            String response = JOptionPane.showInputDialog(parent, "Enter the alert server's port");
            if(response != null) {
                response = response.trim();
                setPort(response);
            }
        }
    }
    
    private boolean isValidIPv4(String ip) {
        if(ip == null || ip.equals(""))
            return false;
        
        try {
            final InetAddress inet = InetAddress.getByName(ip);
            return inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
        } catch (final UnknownHostException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
            return false;
        }
    }
    
    private boolean isValidPort(String portString) {
        if(portString == null || portString.equals(""))
            return false;
        
        try {
            int port = Integer.parseInt(portString);
            if(port >= 0 && port <= 65535)
                return true;
            else throw new IllegalArgumentException("Port must be between 0 and 65535");
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
            return false;
        }
 
    }
    
    @Override
    public void onUpdate() {
        try {
            saveProperties();
        } catch (IOException ex) {
            Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public JPanel getAlertMonitoringPanel() {
        return parent;
    }
    
    private static final int BADSYNTAX = -2;
    private static final int SUCCESS = 1;
    private static final int EXCEPTION = 2;
    private static final int NOTINSYSTEM = 3;

    private static final String AAP = "AAP"; // all active pages
    private static final String STATUS = "S";
    private static final String START = "ST"; // start paging
    private static final String STOP = "SP"; // stop paging
    private static final String ACKNOWLEDGE = "ACK";
    private static final String STOPALL = "SPA";
    
    public synchronized int doTask(String task) {
        try {
            String[] split = task.split(" ", 2); //split by spaces
            String command = split[0];
            String rest = split[1].trim();

            if(command.equals(STATUS)) {
                int jobID = Integer.parseInt(rest);
                int status = getStatus(jobID);
                return status;
            } else if (command.equals(START)) {
                Alert alert = parseAlert(rest);
                try {
                    activeAlerts.add(alert);
                    alertAllAlertListeners(alert);

                    alertAllLogListeners("Created alert: " + alert.toString());

                    return SUCCESS;
                } catch(Exception ex) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                    return EXCEPTION;
                }


            } else if (command.equals(STOP)) {
                try {
                    int jobID;
                    try {
                        jobID = Integer.parseInt(rest);
                    } catch(Exception ex) {
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                        return BADSYNTAX;
                    }

                    int index = searchFor(jobID);
                    if(index == -1) {
                        return NOTINSYSTEM;
                    }

                    Alert remove = activeAlerts.remove(index); //and since arraylist works on .equals(), it will remove the active alert that has the jobid
                    remove.acknowledge();
                    addToPastAlerts(remove);
                    alertAllLogListeners("Stopped alert: " + remove.toString());
                    return SUCCESS;
                } catch(Exception ex) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                    return EXCEPTION;
                }

            } else if(command.equals(ACKNOWLEDGE)) {

                int jobID;
                try {
                    jobID = Integer.parseInt(rest);
                } catch(Exception ex) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                    return BADSYNTAX;
                }

                int index = searchFor(jobID);

                if(index == -1) {
                    return NOTINSYSTEM;
                }

                Alert remove = activeAlerts.remove(index); //and since arraylist works on .equals(), it will remove the active alert that has the jobid
                remove.acknowledge();
                addToPastAlerts(remove);
                alertAllLogListeners("Acknowledged alert: " + remove.toString());
                return SUCCESS;

            } else if(command.equals(STOPALL)) {
                try {
                    for(Alert alert: activeAlerts) {
                        alert.acknowledge();
                    }

                    alertAllLogListeners("Stopped all alerts");
                    return SUCCESS;
                } catch(Exception ex) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                    return EXCEPTION;
                }

            } else {
                // syntax is incorrect
                return BADSYNTAX;
            }
        } catch(Exception ex) {
            return BADSYNTAX;
        }
    }
    


    

    private Alert parseAlert(String alertText) {
        String[] split = alertText.split(" ", 2);
        int jobID = Integer.parseInt(split[0]);
        String message = split[1];
        Alert alert = new Alert(jobID, message);
        return alert;
    }

    private int searchFor(int jobID) {
        for(int i = 0; i < activeAlerts.size(); i++) {
            Alert alert = activeAlerts.get(i);
            if(alert.getJobID() == jobID)
                return i;
        }

        return -1;
    }

    private String getAllAlertText() {
        String alertText = "";
        for(int i = 0; i < activeAlerts.size(); i++) {
            alertText += activeAlerts.get(i);
            if(i != activeAlerts.size() - 1)
                alertText += "\n";
        }
        return alertText;
    }

    private static final int ALREADY = 1;
    private static final int PENDING = 2;
    
    private int getStatus(int jobID) {
        // check if already acknowledged
        boolean hitNull = false;
        for(int i = 0; i < pastAlerts.length && !hitNull; i++) {
            Alert alert = pastAlerts[i];
            if(alert == null)
                hitNull = true;
            else {
                if(jobID == alert.getJobID())
                    return ALREADY;
            }
        }
        
        for(Alert alert: activeAlerts) {
            if(alert.getJobID() == jobID)
                return PENDING;
        }
        
        return NOTINSYSTEM;
    }
    
    private void addToPastAlerts(Alert alert) {
        // move all to right
        for(int i = 0; i < pastAlerts.length - 1; i++) {
            pastAlerts[i + 1] = pastAlerts[i];
        }
        pastAlerts[0] = alert;
    }
    
    private class AlertMonitorThread extends Thread {
        
        private final AlertMonitoringSystem ams;

        private Socket socket = null;
        private InputStream is = null;
        private OutputStream os = null;
        
        public AlertMonitorThread(AlertMonitoringSystem ams) {
            super();
            this.ams = ams;
        }
        
        public void run() {
            
            try {
                String ip = props.getProperty(IP_PROPERTY);
                int port = Integer.parseInt(props.getProperty(PORT_PROPERTY));
                
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 1000);
                is = socket.getInputStream();
                os = socket.getOutputStream();
                
                while(true) {
                    
                    String buffer = "";
                    do {
                        int read = is.read();
                        if(read == -1)
                            throw new IOException("The connection was broken");
                        buffer += (char) read;
                        
                    } while(is.available() > 0);
                    
                    if(buffer.equals(AAP)) {
                        os.write(getAllAlertText().getBytes());
                        os.flush();
                    } else {
                        write(doTask(buffer));
                    }
                }
            } catch (Exception ex) {
                
                if(amt != this) {
                    System.out.println("User wanted to change something");
                    return;
                }
                
                System.out.println(ex.getClass());
                
                if(is != null)
                    try {
                    is.close();
                } catch (IOException ex1) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                }
                
                if(os != null)
                    try {
                    os.close();
                } catch (IOException ex1) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                }
                
                if(socket != null)
                    try {
                    socket.close();
                } catch (IOException ex1) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                }
                    
                Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                
                System.out.println(ex.getClass());
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
                
                final String RETRY = "Retry";
                final String CHANGE_IP = "Change IP";
                final String CHANGE_PORT = "Change Port";
                final String QUIT = "Quit";
                
                String[] options = {QUIT, CHANGE_PORT, CHANGE_IP, RETRY};
                
                int choseInt = JOptionPane.CLOSED_OPTION;
                while(choseInt == JOptionPane.CLOSED_OPTION) {
                    choseInt = JOptionPane.showOptionDialog(parent, "Alert Server Connection Error", "Error Recovery", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, RETRY);
                }
                
                String chose = options[choseInt];
                
                if(chose.equals(CHANGE_IP)) {
                    props.setProperty(IP_PROPERTY, "");
                    ams.checkIPv4Address();
                } else if(chose.equals(CHANGE_PORT)) {
                    props.setProperty(PORT_PROPERTY, "");
                    ams.checkPort();
                } else if(chose.equals(QUIT))
                    System.exit(4);
                else if(chose.equals(RETRY)) {
                    ams.amt = new AlertMonitorThread(ams);
                    amt.start();
                }
            }
            
            
            
        }

        /**
        * Simplified way of writing status codes
        */
        private void write(int i) throws IOException {
            os.write(("" + i).getBytes());
            os.flush();
        }
        
        @Override
        public void interrupt() {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (Exception ex) {
                Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            super.interrupt();
        }
        
        
    }
    
    public class AlertMonitoringPanel extends JPanel implements UpdateListener, LogListener {

        private AlertMonitoringSystem ams;
        
        private JLabel ipLabel, portLabel;
        private JButton changeIPButton, changePortButton;
        private JTextArea logArea;
        
        protected AlertMonitoringPanel(AlertMonitoringSystem aThis) {
            super();
            ams = aThis;
            init();
        }
        
        private void init() {
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());
            
            this.setBorder(new EmptyBorder(10,10,10,10));
            this.setLayout(new BorderLayout());
            
            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setIPLabelText();
            setPortLabelText();
            
            changeIPButton = new JButton("Change IP");
            changeIPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ams.props.setProperty(IP_PROPERTY, "");
                    ams.checkIPv4Address();
                    setIPLabelText();
                }
            });
            
            changePortButton = new JButton("Change Port");
            changePortButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ams.props.setProperty(PORT_PROPERTY, "");
                    ams.checkPort();
                    setPortLabelText();
                }
            });
            
            logArea = new JTextArea();
            logArea.setEditable(false);
            
            JPanel alertPanel = new JPanel(new GridLayout(2,2));
            
            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);
            
            contentPanel.add(alertPanel, BorderLayout.CENTER);
            
            this.add(contentPanel, BorderLayout.NORTH);
            this.add(logArea, BorderLayout.CENTER);
            
            ams.addUpdateListener(this);
            ams.addLogListener(this);
        }
        
        private void setIPLabelText() {
            ipLabel.setText("IP: " + ams.getIPv4Address());
        }
        
        private void setPortLabelText() {
            portLabel.setText("Port: " + ams.getPort());
        }
        
        @Override
        public void onUpdate() {
            setIPLabelText();
            setPortLabelText();
            log("Changed alert address to \"" + ams.getIPv4Address() + ":" + ams.getPort() + "\"");
        }
        
        private void log(String toLog) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(toLog + " at " + formattedDate + "\n");
        }

        @Override
        public void onLog(String logText) {
            log(logText);
        }
    }
    
    
    
}
    

