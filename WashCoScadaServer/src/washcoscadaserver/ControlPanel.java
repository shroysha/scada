/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package washcoscadaserver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;


public class ControlPanel extends JPanel
{
    private SCADAServer server;
    private JButton pageButton, modemButton, clearAllButton, startServButton;
    
    public ControlPanel(SCADAServer aServ)
    {
        super();
        server = aServ;
        setPreferredSize(new Dimension(200,700));
        setBackground(Color.black);
        makeButtons();
    }
    
    public void makeButtons()
    {
        ActionListener al = new ControlListener();
        pageButton = new JButton("Turn Paging On/Off");
        pageButton.setForeground(Color.red);
        clearAllButton = new JButton("Clear all Notifications");
        startServButton = new JButton("Start/Stop SCADA Server");
        startServButton.setForeground(Color.red);
        
        clearAllButton.addActionListener(al);
        pageButton.addActionListener(al);
        startServButton.addActionListener(al);
        
        this.add(startServButton);
        this.add(pageButton);
        this.add(clearAllButton);
    }

    private class ControlListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            boolean result= false;
            if(e.getSource() == pageButton)
            {
                result = server.switchPaging();
            
                if (result)
                    pageButton.setForeground(Color.green.darker());
                else
                    pageButton.setForeground(Color.red);
            }
            else if (e.getSource() == startServButton)
            {
                if(!server.isChecking())
                {
                    server.startChecking();
                    startServButton.setForeground(Color.green.darker());
                }
                else
                {
                    server.stopChecking();
                    startServButton.setForeground(Color.red);
                }
            }
            else if (e.getSource() == clearAllButton)
            {
                server.clearAllPages();
            }
        }

        
    }
}
