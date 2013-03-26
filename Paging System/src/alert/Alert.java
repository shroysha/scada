/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alert;

/**
 *
 * @author Shawn
 */
public class Alert {
    
    private boolean acknowledged = false;
    private final int jobID;
    private final String message;
    
    public Alert(int jobID, String message) {
        super();
        
        this.jobID = jobID;
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void acknowledge() {
        acknowledged = true;
    }
    
    public boolean isAcknowledged() {
        return acknowledged;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof Alert) {
            Alert a = (Alert) o;
            if(a.jobID == jobID)
                return true;
        }
        
        return false;
    }
    
    public String toString() {
        return "" + jobID + " " + message;
    }

    public int getJobID() {
        return jobID;
    }
}
