/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package log;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.DateFormatter;

/**
 *
 * @author Shawn
 */
public class LoggingSystem implements LogListener {

    private static LoggingSystem logSys;
    private static final File logFile = new File("log.txt");
    
    public LoggingSystem() {
        super();
        
        addLogListener(this);
        
        logSys = this;
    }
    
    private Stack<LogListener> logListeners = new Stack();
    
    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }
    
    public void removeLogListener(LogListener listener) {
        logListeners.remove(listener);
    }
    
    public void alertAllLogListeners(String logText) {
        for(LogListener listener: logListeners) {
            listener.onLog(logText);
        }
    }
    
    @Override
    public void onLog(String text) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            out.println(text + " " + timeStamp());
            out.close();
        } catch (IOException e) {
            if(!logFile.exists()) {
                try {
                    logFile.mkdirs();
                    logFile.createNewFile();
                    onLog(text);
                } catch (IOException ex) {
                    Logger.getLogger(LoggingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(LoggingSystem.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    private String timeStamp() {
        Calendar calendar = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("MM/dd/yy HH:MM:SS");
        return format.format(calendar);
    }
    
    public static LoggingSystem getLoggingSystem() {
        return logSys;
    }
    
}
