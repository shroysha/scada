/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.*;

/**
 *
 * @author Shawn
 */
public class SCADAJTree extends JTree{
    
    public SCADAJTree() {
        super();
        this.setCellRenderer(new SCADACellRenderer());
    }
    
    public void setSCADASites(ArrayList<SCADASite> sites) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sites");
        TreeModel siteModel = new DefaultTreeModel(root);
        
        for(SCADASite site: sites) {
            root.add(new SCADANode(site));
        }
        
        this.setModel(siteModel);
    }
    
    
    
    class SCADANode extends DefaultMutableTreeNode {
        
        private SCADASite site;
        
        public SCADANode(SCADASite site) {
            this.site = site;
        }
        
        public SCADASite getSite() {
            return site;
        }
    }
    
    private class SCADACellRenderer implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree jtree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = new JLabel();
            
            if(value instanceof SCADANode) {
                SCADANode node = (SCADANode) value;
                SCADASite site = node.getSite();
                label.setText(site.getName());
                
                if(site.getAlarm()) {
                    label.setForeground(Color.red);
                } else if(site.getWarning()) {
                    label.setForeground(Color.orange);
                } else {
                    label.setForeground(Color.black);
                }
                
                if(selected) {
                    if(!hasFocus) {
                        label.setBackground(Color.gray.brighter());
                    } else {
                        label.setBackground(Color.cyan);
                    }
                }
                
            } else if(value instanceof DefaultMutableTreeNode){
                label.setText((String)value);
            } else {
                label.setText("Invalid argument passed");
            }
            
            return label;
        }
        
    }
}
