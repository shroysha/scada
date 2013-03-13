/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingsystem;

import alert.Alert;

/**
 *
 * @author Shawn
 */
class Page {

    private static final char STX = 0x02;
    private static final char ETX = 0x03;
    private static final char CR = 0x0D;
    
    private final String message;
    private final String checkSum;
    private final String pagerNumber;
    
    protected Page(Alert alert, String pagerNumber) {
        super();
        this.message = alert.getMessage();
        this.pagerNumber = pagerNumber;
        this.checkSum = calculateChecksum();
    }
    
    private String calculateChecksum() {
        String bob = STX + pagerNumber + CR + message + CR + ETX;
        char[] bobints = bob.toCharArray();
        int total = 0;
        
        for(char c : bobints)
        {
            total += c;
        }
        
        total %=4096;
        
        String hexString = Integer.toHexString(total).toUpperCase();
        
        while(hexString.length() < 3)
            hexString = "0" + hexString;
        
        int[] hexPlaces = new int[3];
        
        for(int i = 0; i < hexPlaces.length; i++)
        {
            hexPlaces[i] = Integer.parseInt(hexString.substring(i, i+1), 16) + 0x30;
        }
        
        String checkSum = "";
        
        for(int digit : hexPlaces)
        {
            checkSum += (char) digit;
        }

        return checkSum;
    }
    
    public String getMessage() {
        return message;
    }
    
    
    @Override
    public String toString() {
        String string = STX + pagerNumber + "\r" + message + "\r" + ETX + checkSum + "\r";
        System.out.println(string);
        return string;
    }
}
