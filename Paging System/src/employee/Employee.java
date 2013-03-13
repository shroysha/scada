/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee;

import java.util.Calendar;

/**
 *
 * @author Shawn
 */
public final class Employee implements Comparable<Employee>{
    
   
    
    private String pager; // pager should equal the ip or something to send to employee
    /*
     * The time the employee starts his/her shift.
     * Expressed in 24 hour format.
     * 8:30PM would be expressed as 19.50. 19th hour of the day; halfway through the hour.
     */
    private double startHour, stopHour;
    private int priority;
    private int dayWorking; // all should equal one of Calendar.SUNDAY, etc.

    
    public Employee(String pager, double startHour, double stopHour, int priority, int dayWorking) {
        super();
        this.pager = pager;
        this.startHour = startHour;
        this.stopHour = stopHour;
        this.dayWorking = dayWorking;
        this.priority = priority;
    }
    
    public boolean isCurrentlyWorking() {
        Calendar now = Calendar.getInstance();
        
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        
        double minuteInDec = (double)minute / 60.0; // convert minutes to decimal. 30 minutes converts to 0.5
        double adjustedHour = (double)hour + minuteInDec;
        
        if(adjustedHour >= startHour && adjustedHour <= stopHour) { //if the adjusted time is in between their hours
            if(dayWorking == dayOfWeek)
                return true;
        }
        
        return false;
    }
    

    @Override
    public int compareTo(Employee t) {
        return priority - t.priority;
    }

    public String getPager() {
        return pager;
    }

    public int getDayWorking() {
        return dayWorking;
    }
    
   
}
