/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import pagingsystem.PagingSystem;

/**
 *
 * @author Shawn
 */
public class PagingGUI extends JFrame {
    
    public static void main(String[] args) {
       
        
        PagingGUI gui = null;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());;
        } catch (Exception ex) {}
        try {
            gui = new PagingGUI();
        } catch (IOException ex) {
            Logger.getLogger(PagingGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        gui.setVisible(true);
    }
    
    private PagingSystem ps;
    
    public PagingGUI() throws IOException {
        super("Paging System");
        
        
        ps = new PagingSystem();
        init();
    }
    
    private void init() {
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbed = new JTabbedPane();
        tabbed.addTab("Paging System", ps.getPagingSystemPanel());
        tabbed.addTab("Alert Monitor System", ps.getAlertMonitorPanel());
        tabbed.addTab("Employees", ps.getEmployeePanel());
        
        this.add(tabbed, BorderLayout.CENTER);
        this.setSize(400,300);
    }
}
