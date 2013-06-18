package gui;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Shawn
 */
public final class JLightSwitch extends AbstractButton {

    public static void main(String[] args) {
        JFrame frame = new JFrame("HELLo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        JLightSwitch swit = new JLightSwitch("Hello");
        swit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("ON");
            }
        });
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.add(swit);
        
        frame.add(panel, BorderLayout.CENTER);
        frame.setSize(300,300);
        frame.setVisible(true);
    }
    
    private static final Color DEF_COLOR = Color.gray;
    private static final int ARC_LENGTH = 25;
    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final Color ON_COLOR = Color.green;
    private static final Color OFF_COLOR = Color.red;
    private static final int pWidth = 125, pHeight = 25;
    private static final int BUFFER = 10;
    
    private int titleWidth;
    
    public JLightSwitch() {
        this("", DEF_COLOR);
    }
    
    public JLightSwitch(Color color) {
        this("", color);
    }
    
    public JLightSwitch(String title) {
        this(title, DEF_COLOR);
    }
    
    public JLightSwitch(String title, Color color) {
        super();
        setForeground(color);
        setText(title);
        setFont(new JLabel().getFont());
        
        FontMetrics fontMetrics = this.getFontMetrics(getFont());
        titleWidth = fontMetrics.stringWidth(title) + BUFFER * 2;
        
        setModel(new JToggleButton.ToggleButtonModel());
        
        setSelected(false);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                super.mouseClicked(me);
                setSelected(!isSelected());
                ActionEvent e = new ActionEvent(JLightSwitch.this, 0, JLightSwitch.this.getActionCommand());
                fireActionPerformed(e);
            }
        });
        
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        
        if(selected) { 
            setBackground(ON_COLOR);
        } else {
            setBackground(OFF_COLOR);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(pWidth + titleWidth, pHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Dimension dim = this.getPreferredSize();
        Color background = getBackground();
        Color foreground = getForeground();
        
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();

        if(!this.getText().equals("")) {
            String title = getText();
            
            Rectangle2D stringBounds = metrics.getStringBounds(title, g);
            titleWidth = BUFFER * 2 + metrics.stringWidth(title);
            int titleHeight = (int) stringBounds.getHeight();
            
            int x = BUFFER;
            int y = dim.height / 2 + titleHeight / 2; 
            g.setColor(Color.black);
            g.drawString(title, x, y);
        } else {
            titleWidth = 0;
        }
        
        g.setColor(background);
        
        final int backgroundX = titleWidth;
        final int backgroundY = 0;
        final int backgroundWidth = dim.width - titleWidth;
        final int backgroundHeight = dim.height;
        g.fillRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight);
        
        g.setColor(foreground);
      
        // draw slider
        if(isSelected()) {
            int x = backgroundX;
            int y = backgroundY;
            int width = backgroundWidth / 2;
            int height = backgroundHeight;
            g.fillRect(x, y, width, height);
        } else {
            int x = backgroundX + backgroundWidth /2 + 1;
            int y = backgroundY;
            int width = backgroundWidth / 2;
            int height = backgroundHeight;
            g.fillRect(x, y, width, height);
        }
        
        // draw on or off
        g.setColor(Color.black);
        if(!isSelected()) {
            int x = backgroundX;
            int y = backgroundY;
            int width = backgroundWidth / 2;
            int height = backgroundHeight;
            
            int offWidth = metrics.stringWidth(OFF);
            Rectangle2D stringBounds = metrics.getStringBounds(OFF, g);
            int offHeight = (int) stringBounds.getHeight();
            
            int offX = x + width / 2 - offWidth/2;
            int offY = (int) (height / 2 + stringBounds.getHeight() / 2); 
            g.drawString(OFF, offX, offY);
        } else {
            int x = backgroundX + backgroundWidth /2;
            int y = backgroundY;
            int width = backgroundWidth / 2;
            int height = backgroundHeight;
            
            int onWidth = metrics.stringWidth(ON);
            Rectangle2D stringBounds = metrics.getStringBounds(ON, g);
            int onHeight = (int) stringBounds.getHeight();
            
            int onX = x + width / 2  - onWidth / 2;
            int onY = (int) (height / 2 + stringBounds.getHeight() / 2); 
            g.drawString(ON, onX, onY);
        }
        
        g.fillRect(backgroundX, backgroundY, backgroundWidth, 2);
        g.fillRect(backgroundX, backgroundY, 2, backgroundHeight);
        g.fillRect(backgroundX, backgroundHeight - 2, backgroundWidth, 2);
        g.fillRect(backgroundX + backgroundWidth - 2, backgroundY, 2, backgroundHeight);
        g.fillRect(backgroundX + backgroundWidth / 2 - 1, backgroundY, 3, backgroundHeight);
    }
    
}
