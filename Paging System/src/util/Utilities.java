/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author Shawn
 */
public abstract class Utilities {
    
    private Utilities() {
        
    }
    
    public static String getMainDirPath() {
        return System.getProperty("user.home") + "/.scada";
    }
}
