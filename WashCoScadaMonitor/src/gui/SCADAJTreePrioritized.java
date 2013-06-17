/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * Just a copy of the previous jtree, however it has 
 * different nodes for critical and warning
 * @author Shawn
 */
public class SCADAJTreePrioritized extends SCADAJTree{

    public SCADAJTreePrioritized() {
        super();
        this.setRootVisible(false);
    }

    
    
    @Override
    public void setSCADASites(ArrayList<SCADASite> sites) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultMutableTreeNode critical = new DefaultMutableTreeNode("Critical");
        DefaultMutableTreeNode warning = new DefaultMutableTreeNode("Warning");
        
        root.add(critical);
        root.add(warning);
        
        TreeModel siteModel = new DefaultTreeModel(root);
        
        for(SCADASite site: sites) {
            if(site.getAlarm()) {
                critical.add(new SCADANode(site));
            }
            if(site.getWarning()) {
                warning.add(new SCADANode(site));
            }
        }
        
        this.setModel(siteModel);
    }
    
    
    
    
}
