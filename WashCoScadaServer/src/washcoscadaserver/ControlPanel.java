/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package washcoscadaserver;

import gui.PagingGUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class ControlPanel extends JPanel
{
    private SCADAServer server;
    private JButton pageButton, modemButton, clearAllButton, startServButton;
    protected PagingGUI pagePanel;
    private JFrame scadaFrame;
    
    public ControlPanel(SCADAServer aServ, JFrame aFrame)
    {
        super();
        server = aServ;
        scadaFrame = aFrame;
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
                if(server.isChecking())
                {
                result = server.switchPaging();

                if (result)
                {
                    //BorderLayout bl = (BorderLayout) scadaFrame.getLayout();
                    //bl.getLayoutComponent(BorderLayout.SOUTH);
                    //scadaFrame.remove(((BorderLayout)scadaFrame.getLayout()).getLayoutComponent(BorderLayout.SOUTH));
                    scadaFrame.remove(SCADARunner.pagingHolder);
                    server.pageServ.getPagingGUI().setPreferredSize(new Dimension(700,250));
                    scadaFrame.add(server.pageServ.getPagingGUI(), BorderLayout.SOUTH);
                    scadaFrame.revalidate();
                    pageButton.setForeground(Color.green.darker());
                }
                else
                {   
                    JPanel pagingHolder = new JPanel();
                    pagingHolder.setPreferredSize(new Dimension(700, 250));
                    JLabel labelTemp = new JLabel("Paging System Inactive.");
                    pagingHolder.add(labelTemp);
                    pageButton.setForeground(Color.red);
                }
                }
                else
                    JOptionPane.showMessageDialog(null, "Paging not started, server is not active.");
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
                    server.pagingOff();
                    pageButton.setForeground(Color.red);
                }
            }
            else if (e.getSource() == clearAllButton)
            {
                server.clearAllPages();
            }
        }

        
    }
}
