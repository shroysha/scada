/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WashCoSCADAMonitor;

import java.awt.geom.Point2D;

/**
 *
 * @author Avogadro
 */
public class SitePoint extends Point2D.Double
{
    Integer alarmStatus;
    String siteName;
    
    public SitePoint(double x, double y, Integer aAlarmStatus, String aSiteName)
    {
        super(x,y);
        alarmStatus = aAlarmStatus;
        siteName = aSiteName;
    }
    
    public String getPointName()
    {
        return siteName;
    }
    
    public Integer getAlarmStatus()
    {
        return alarmStatus;
    }
    
    public void setAlarm(int aAlarm)
    {
        alarmStatus = aAlarm;
    }
            
}