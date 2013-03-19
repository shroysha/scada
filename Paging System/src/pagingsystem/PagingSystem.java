/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingsystem;

import alert.Alert;
import alert.AlertMonitoringSystem;
import employee.Employee;
import employee.EmployeeHandler;
import employee.gui.EmployeePanel;
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
import util.AlertListener;
import util.LogListener;
import util.UpdateListener;
import static util.Utilities.getMainDirPath;


/**
 *
 * @author Shawn
 */
public final class PagingSystem implements AlertListener, UpdateListener {
    
    private static final File configFile = new File(getMainDirPath() + "/pagingsystem/settings.cfg"); /* need to update this*/
    private static final String IP_PROPERTY = "pagingServerIP", PORT_PROPERTY = "pagingServerPort";

    private JPanel parent;
    private AlertMonitoringSystem ams;
    private EmployeeHandler eh;
    private Socket socket;
    private OutputStream os;
    private InputStream is;
    private Properties props;
    
    private Stack<UpdateListener> updateListeners = new Stack();
    private Stack<LogListener> logListeners = new Stack();
    
    public PagingSystem() throws IOException {
        super();
        
        eh = new EmployeeHandler();
        
        addUpdateListener(this);
        
        loadProperties();
        checkProperties();
        
        updateSocket();
        
        ams = new AlertMonitoringSystem();
        
        ams.addAlertListener(this);
        
        parent = new PagingSystemPanel(this);
    }
    
    private synchronized void sendPage(Page page) {
        try {
            // Send message
            if(os == null) {
                throw new IOException("Unexpected error sending message");
            }
            
            os.write(page.toString().getBytes());
            os.flush();
            
            String buffer = readBuffer();
            
            notifyAllLogListeners("Received " + buffer + " from paging server");
        } catch(IOException ex) {
            Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            errorRecovery(ex);
            sendPage(page);
        }
    }
    
    private String readBuffer() throws IOException {
        String buffer= "";
        
        do {
            int i = is.read();
            if(i == -1)
                throw new IOException("The connection was broken.");

            buffer += (char) i;
        } while(is.available() > 0);
        
        return buffer;
    }
    
    @Override
    public void alertReceived(Alert alert) {
        PageThread thread = new PageThread(this, alert);
        thread.start();
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
        props.store(new FileOutputStream(configFile), "PagingServerProperties");
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
    }
    
    protected void setPort(String port) {
        props.setProperty(PORT_PROPERTY, port);
        checkPort();
        
        alertAllUpdateListeners();
    }
    
    /**
     * Checks the ip address until a valid ip is entered
     * Just because this method returns true, it doesn't mean that the client can connect to it
     */
    private void checkIPv4Address() { 
        while(!isValidIPv4(props.getProperty(IP_PROPERTY))) {
            String response = JOptionPane.showInputDialog(parent, "Enter the paging server's ip");
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
            String response = JOptionPane.showInputDialog(parent, "Enter the paging server's port");
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
    
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public void removeUpdateListner(UpdateListener listener) {
        updateListeners.remove(listener);
    }
    
    private void alertAllUpdateListeners() {
        for(UpdateListener listener: updateListeners) {
            listener.onUpdate();
        }
    }
    
    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }
    
    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }
    
    private void notifyAllLogListeners(String logText) {
        for(LogListener listener: logListeners) {
            listener.onLog(logText);
        }
    }

    @Override
    public void onUpdate() {
        try {
            saveProperties();
        } catch (IOException ex) {
            Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateSocket();
    }
    
    private void updateSocket() {
        try {
            if(socket != null)
                try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(is != null)
                try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(os != null)
                try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(props.getProperty(IP_PROPERTY) == null || props.getProperty(PORT_PROPERTY) == null)
                return;
            
            String ip = props.getProperty(IP_PROPERTY);
            int port = Integer.parseInt(props.getProperty(PORT_PROPERTY));
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            
            //LOGON
            //get attention
            char ESC = 0x1B;
            char CR  = 0x0D;
            String everything = "" + CR + ESC + (char)0x050 + (char)0x47 + (char)0x31 +CR;
            os.write(everything.getBytes());
            // lets just assume for now the logon is accepted
            
            
        } catch (IOException ex) {
            Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 

            errorRecovery(ex);
        }
    }
    
    private void errorRecovery(Exception ex) {
        final String RETRY = "Retry";
        final String CHANGE_IP = "Change IP";
        final String CHANGE_PORT = "Change Port";
        final String QUIT = "Quit";

        String[] options = {QUIT, CHANGE_PORT, CHANGE_IP, RETRY};

        int choseInt = JOptionPane.CLOSED_OPTION;
        while(choseInt == JOptionPane.CLOSED_OPTION) {
            choseInt = JOptionPane.showOptionDialog(parent, "Paging Server Connection Error\n"+ex.getMessage(), "Error Recovery", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, RETRY);
        }

        String chose = options[choseInt];
        if(chose.equals(RETRY)) {
            updateSocket();
        } else if(chose.equals(CHANGE_IP)) {
            setIPAddress("");
        } else if(chose.equals(CHANGE_PORT)) {
            setPort("");
        } else if(chose.equals(QUIT))
            System.exit(4);
    }
    
    public JPanel getPagingSystemPanel() {
        return parent;
    }
    
    private final int FIFTEEN = 15 * 60 * 1000;
    private final int TOTALTIME = 3 * 60 * 60 * 1000;
    
    private class PageThread extends Thread {

        private final PagingSystem ps;
        private final Alert alert;
        
        public PageThread(PagingSystem ps, Alert alert) {
            super();
            this.ps = ps;
            this.alert = alert;
        }
        
        @Override
        public void run() {
            Employee[] employees = eh.getCurrentPrioritizedEmployees();
            ArrayList<Employee> cascade = new ArrayList();
            
            if(employees.length == 0) {
                ps.notifyAllLogListeners("There are no employees on duty");
                return;
            }
            
            int index = 0;
            
            while(!alert.isAcknowledged()) { // person with the lowest rank (kinda like golf) goes first
                if(cascade.size() < employees.length) {
                    cascade.add(employees[index]);
                    index++;
                }
                
                for(Employee employee: cascade) {
                    Page page = new Page(alert, employee.getPager());
                    ps.sendPage(page);
                }
                
                hold(FIFTEEN);
            }
        }
        
        private void hold(int time) {
            try {
                Thread.sleep(1000 * 60 * 15); // 15 minutes
            } catch (InterruptedException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public class PagingSystemPanel extends JPanel implements UpdateListener, LogListener {

        private PagingSystem ps;
        
        private JTextArea logArea;
        private JLabel ipLabel, portLabel;
        private JButton changeIPButton, changePortButton;
        
        protected PagingSystemPanel(PagingSystem aThis) {
            super();
            ps = aThis;
            init();
        }
        
        private void init() {
            this.setBorder(new EmptyBorder(10,10,10,10));
            this.setLayout(new BorderLayout());
            
            JPanel contentPanel = new JPanel(new BorderLayout());
            
            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setIPLabelText();
            setPortLabelText();
            
            changeIPButton = new JButton("Change IP");
            changeIPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ps.props.setProperty(IP_PROPERTY, "");
                    ps.checkIPv4Address();
                    setIPLabelText();
                }
            });
            
            changePortButton = new JButton("Change Port");
            changePortButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ps.props.setProperty(PORT_PROPERTY, "");
                    ps.checkPort();
                    setPortLabelText();
                }
            });
            
            logArea = new JTextArea();
            
            JPanel alertPanel = new JPanel(new GridLayout(2,2));
            
            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);
            
            contentPanel.add(alertPanel, BorderLayout.CENTER);
            
            this.add(logArea, BorderLayout.CENTER);
            this.add(contentPanel, BorderLayout.NORTH);
            
            ps.addUpdateListener(this);
            ps.addLogListener(this);
            
        }
        
        private void setIPLabelText() {
            ipLabel.setText("IP: " + ps.getIPv4Address());
        }
        
        private void setPortLabelText() {
            portLabel.setText("Port: " + ps.getPort());
        }
        
        @Override
        public void onUpdate() {
            setIPLabelText();
            setPortLabelText();
        }
        
        @Override
        public void onLog(String logText) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(logText + " at " + formattedDate + "\n");
        }
    }
    
    public JPanel getAlertMonitorPanel() {
        return ams.getAlertMonitoringPanel();
    }
    
    public EmployeePanel getEmployeePanel() {
        return eh.getEmployeePanel();
    }
}
