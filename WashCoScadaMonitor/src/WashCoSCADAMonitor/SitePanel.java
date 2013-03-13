/*
 * Author: Peter O'Connor
 * Purpose: To implement SCADA Monitoring throughout Washington County
 * Version: 1.0a
 * 
 * Contact: avogadrosg1@gmail.com
 * 
 */
package WashCoSCADAMonitor;

import java.awt.*;
import javax.swing.*;


public class SitePanel extends JPanel
{
    private JTextArea status;
    
    public SitePanel()
    {
        setPreferredSize(new Dimension(1100, 300));
        setBackground(Color.DARK_GRAY);
        status = new JTextArea(17,80);
        status.setEditable(false);
        
        JScrollPane scrollStatus = new JScrollPane(status);
        scrollStatus.setPreferredSize(new Dimension(1050,290));
        scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollStatus.setAutoscrolls(true);
        
        add(scrollStatus);
    }
    
    public void append(String text)
    {
        status.append(text);
    }
    
    public void setText(String text)
    {
        status.setText(text);
    }
    
    public void clearText()
    {
        status.setText("");
    }
    
}
