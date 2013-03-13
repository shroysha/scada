/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WashCoSCADAMonitor;

import SCADASite.SCADASite;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Avogadro
 */
public class InfoPanel extends JPanel
{
    JButton alarmsButton, sitesButton;
    JTree displayTree;
    DefaultMutableTreeNode displayNodes;
    private ArrayList<SCADASite> sites;
    
    public InfoPanel()
    {
        this.setPreferredSize(new Dimension(250,500));
        this.setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(250,40));
        
        sitesButton = new JButton("Sites");
        alarmsButton = new JButton("Alarms");
        
        buttonPanel.add(sitesButton, BorderLayout.NORTH);
        buttonPanel.add(alarmsButton, BorderLayout.NORTH);
        
        createSiteTree();
        
        displayTree = new JTree(displayNodes);
        
        JScrollPane treeView = new JScrollPane(displayTree);
        treeView.setPreferredSize(new Dimension(225, 425));
        
        this.add(treeView, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.NORTH);
    }
    
    /*public void setSiteList(ArrayList<SCADASite> aSites)
    {
        sites = aSites;
        this.createSiteTree();
    }*/
    
    private void createSiteTree()
    {
        DefaultMutableTreeNode category;
        displayNodes = new DefaultMutableTreeNode("Top");
        
        category = new DefaultMutableTreeNode("Alarms");
        
        category.add(new DefaultMutableTreeNode("Site 1"));
        category.add(new DefaultMutableTreeNode("Site 4"));
        category.add(new DefaultMutableTreeNode("Site 3"));
        category.add(new DefaultMutableTreeNode("Site 8"));
        
        displayNodes.add(category);
        
        category = new DefaultMutableTreeNode("Warnings"); 
        
        category.add(new DefaultMutableTreeNode("Site 21"));
        category.add(new DefaultMutableTreeNode("Site 24"));
        category.add(new DefaultMutableTreeNode("Site 23"));
        category.add(new DefaultMutableTreeNode("Site 28"));
        
        displayNodes.add(category);
    }
    
    public void createAlarmTree()
    {
        DefaultMutableTreeNode category;
        displayNodes = new DefaultMutableTreeNode("Top");
        
        category = new DefaultMutableTreeNode("Alarms");
        
        category.add(new DefaultMutableTreeNode("Site 1"));
        category.add(new DefaultMutableTreeNode("Site 4"));
        category.add(new DefaultMutableTreeNode("Site 3"));
        category.add(new DefaultMutableTreeNode("Site 8"));
        
        displayNodes.add(category);
        
        category = new DefaultMutableTreeNode("Warnings"); 
        
        category.add(new DefaultMutableTreeNode("Site 21"));
        category.add(new DefaultMutableTreeNode("Site 24"));
        category.add(new DefaultMutableTreeNode("Site 23"));
        category.add(new DefaultMutableTreeNode("Site 28"));
        
        displayNodes.add(category);
    }
}
