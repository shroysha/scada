/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 *
 * @author Shawn
 */
public final class Employee implements Comparable<Employee>{
    
   
    
    private String name, pager; // pager should equal the ip or something to send to employee
    /*
     * The time the employee starts his/her shift.
     * Expressed in 24 hour format.
     * 8:30PM would be expressed as 19.50. 19th hour of the day; halfway through the hour.
     */
    private double startHour, stopHour;
    private int priority;
    private int dayWorking; // all should equal one of Calendar.SUNDAY, etc.

    public Employee() {
        super();
    }
    
    public Employee(String name, String pager, double startHour, double stopHour, int priority, int dayWorking) {
        super();
        this.name = name;
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

    public String getName() {
        return name;
    }
    
    public String getPager() {
        return pager;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getStartHour() {
        return startHour;
    }

    public void setStartHour(double startHour) {
        this.startHour = startHour;
    }

    public double getStopHour() {
        return stopHour;
    }

    public void setStopHour(double stopHour) {
        this.stopHour = stopHour;
    }
    
    public int getDayWorking() {
        return dayWorking;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPager(String pager) {
        this.pager = pager;
    }

    public void setDayWorking(int dayWorking) {
        this.dayWorking = dayWorking;
    }
    
   
    public void goUpPriority() {
        priority--;
    }
    
    public void goDownPriority() {
        priority++;
    }
    
    public static String timeFormat(double time) {
        int hours = (int) time;
        time -= hours;
        int minutes = (int) (time * 60.0);
        NumberFormat format = new DecimalFormat("00");
        String hoursText = format.format(hours);
        String minutesText = format.format(minutes);
        return "" + hoursText + ":" + minutesText;
    }
}
