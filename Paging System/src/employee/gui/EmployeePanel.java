/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee.gui;

import employee.Employee;
import employee.EmployeeHandler;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.Utilities;

/**
 *
 * @author Shawn
 */
public class EmployeePanel extends JPanel {
        
        private final int[] daysOfWeek = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
        
        private final EmployeeHandler handler;
        
        public EmployeePanel(EmployeeHandler handler) {
            super(new BorderLayout());
            this.handler = handler;
            init();
        }
        
        private void init() {
            String[] namesOfDaysOfWeek = Utilities.getDaysOfWeek();
            JTabbedPane weekTabbed = new JTabbedPane();
            for(int i = 0; i < daysOfWeek.length; i++) {
                weekTabbed.addTab(namesOfDaysOfWeek[i], new EmployeeDayPanel(daysOfWeek[i]));
            }
            this.add(weekTabbed, BorderLayout.CENTER);
        }
        
        /**
         * Displays all the employees that work on the day imputed into the constructor. They are sorted by priority
         */
        private class EmployeeDayPanel extends JPanel {
            
            private final int dayOfWeek;
            private ArrayList<Employee> onDayEmployees = new ArrayList();
            
            public EmployeeDayPanel(int dayOfWeek) {
                super(new BorderLayout());
                this.dayOfWeek = dayOfWeek;
                init();
            }
            
            private void init() {
                this.setBorder(new EmptyBorder(10,10,10,10));
                addAllEmployees();
                EmployeeHandler.sortByPriority(onDayEmployees);
                
                JScrollPane scroller = new JScrollPane();
                final EmployeeList list = new EmployeeList(onDayEmployees.toArray(new Employee[onDayEmployees.size()]));
                
                JButton upButton = new JButton("^");
                upButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int sIndex = list.getSelectedIndex();
                        int tIndex = sIndex - 1;
                        
                        if(sIndex != -1)
                            if(tIndex >= 0) {
                                ListModel lm = list.getModel();
                                Employee[] array = new Employee[lm.getSize()];
                                for(int i = 0; i < lm.getSize(); i++) {
                                    array[i] = (Employee) lm.getElementAt(i);
                                }
                                
                                array[sIndex].goUpPriority();
                                array[tIndex].goDownPriority();
                                
                                EmployeeHandler.sortByPriority(array);
                                
                                list.setListData(array);
                            }
                    }
                });

                JButton downButton = new JButton("v");
                downButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int sIndex = list.getSelectedIndex();
                        int tIndex = sIndex + 1;
                        
                        if(sIndex != -1)
                            if(tIndex < list.getModel().getSize()) {
                                ListModel lm = list.getModel();
                                Employee[] array = new Employee[lm.getSize()];
                                for(int i = 0; i < lm.getSize(); i++) {
                                    array[i] = (Employee) lm.getElementAt(i);
                                }
                                
                                array[sIndex].goDownPriority();
                                array[tIndex].goUpPriority();
                                
                                EmployeeHandler.sortByPriority(array);
                                
                                list.setListData(array);
                            }
                    }
                });
                
                scroller.setViewportView(list);
                
                JPanel buttonPanel = new JPanel(new GridLayout(2,1));
                buttonPanel.add(upButton);
                buttonPanel.add(downButton);
                
                this.add(scroller, BorderLayout.CENTER);
                this.add(buttonPanel, BorderLayout.EAST);
                
                
            }
            
            private void addAllEmployees() {
                for(Employee employee: handler.getAllEmployees()) {
                    if(dayOfWeek == employee.getDayWorking())
                        onDayEmployees.add(employee);
                }
            }
            
    
            
            private class EmployeeList extends JList {
                
                
                
                private KeyStroke keyStroke;
                private Action action = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int index = EmployeeList.this.getSelectedIndex();
                        System.out.println("ACTION");
                        if(index != -1) {
                            System.out.println("INDEX");
                            Employee employee = (Employee) EmployeeList.this.getModel().getElementAt(index);
                            EmployeeEditDialog frame = new EmployeeEditDialog(employee);
                            frame.setVisible(true);
                        }
                    }
                };
                
                
                private final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                private final String ACTION_KEY = "ACTION-ALERTED";
                
                
                public EmployeeList(Employee[] obs) {
                    super(obs);
                    
                    InputMap im = this.getInputMap();
                    im.put(ENTER, ACTION_KEY);
                    
                    //  Add the Action to the ActionMap
                    this.getActionMap().put(ACTION_KEY, action);

                    //  Handle mouse double click

                    this.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e){
                            
                                    if (e.getClickCount() == 2)
                                    {
                                            Action action = EmployeeList.this.getActionMap().get(ACTION_KEY);

                                            if (action != null)
                                            {
                                                    ActionEvent event = new ActionEvent(
                                                            EmployeeList.this,
                                                            ActionEvent.ACTION_PERFORMED,
                                                            "");
                                                    action.actionPerformed(event);
                                            }
                                    }
                            }
                    });
                    this.setCellRenderer(new EmployeeListRenderer());
                }
                
                private class EmployeeListRenderer extends JPanel implements ListCellRenderer {

                    private JLabel nameLabel, pagerLabel, startLabel, stopLabel;
                    
                    public EmployeeListRenderer() {
                        super(new GridLayout(2,2));
                        init();
                    }
                    
                    private void init() {
                        nameLabel = new JLabel();
                        pagerLabel = new JLabel();
                        startLabel = new JLabel();
                        stopLabel = new JLabel();
                        
                        this.add(nameLabel);
                        this.add(pagerLabel);
                        this.add(startLabel);
                        this.add(stopLabel);
                        
                        this.setPreferredSize(new Dimension(400,50));
                    }
                    
                    @Override
  	            protected void paintComponent(Graphics grphcs) {
                        super.paintComponent(grphcs);
                        
	                Graphics2D g2d = (Graphics2D) grphcs;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
	 
	                GradientPaint gp = new GradientPaint(0, 0,
	                        getBackground().brighter().brighter(), 0, getHeight(),
	                        getBackground().darker());
	 
	                g2d.setPaint(gp);
	                g2d.fillRect(0, 0, getWidth(), getHeight());
	 
	            }
                    
                    @Override
                    public Component getListCellRendererComponent(JList jlist, Object o, int index, boolean isSelected, boolean cellFocused) {
                        Color background;
                        Color foreground;

                        // check if this cell represents the current DnD drop location
                        JList.DropLocation dropLocation = jlist.getDropLocation();
                        if (dropLocation != null
                                && !dropLocation.isInsert()
                                && dropLocation.getIndex() == index) {

                            background = Color.BLACK;
                            foreground = Color.WHITE;

                        // check if this cell is selected
                        } else if (isSelected) {
                            background = Color.CYAN;
                            foreground = Color.WHITE;

                        // unselected, and not the DnD drop location
                        } else {
                            background = Color.WHITE;
                            foreground = Color.BLACK;
                        }
                        
                        Employee employee = (Employee) o;
                        changeName(employee.getName());
                        changePager(employee.getPager());
                        changeStart(""+employee.getStartHour());
                        changeStop(""+employee.getStopHour());

                        setBackground(background);
                        setForeground(foreground);

                        repaint();
                        
                        return this;
                    }
                    
                    private void changeName(String name) {
                        nameLabel.setText("Name: " + name);
                    }
                    
                    private void changePager(String pager) {
                        pagerLabel.setText("Pager: " + pager);
                    }
                    
                    private void changeStart(String start) {
                        startLabel.setText("Shift Start: " + start);
                    }
                    
                    private void changeStop(String stop) {
                        stopLabel.setText("Shift Stop: " + stop);
                    }
                    
                }
                
            }
            
            private class EmployeeEditDialog extends JDialog {

                private final Employee employee;
                
                private JLabel nameLabel = new JLabel("Employee Name"), 
                        pagerLabel = new JLabel("Employee Pager ID"), 
                        startHourLabel = new JLabel("Employee Start of Shift"), 
                        stopHourLabel = new JLabel("Employee End of Shift");
                
                private JTextField nameArea, pagerIDArea, startHourArea, stopHourArea;
                
                public EmployeeEditDialog(Employee employee) {
                    this(null, true, "Edit Employee", employee);
                }
                
                private EmployeeEditDialog(Frame frame, boolean bln, String string, Employee employee) {
                    super(frame, string, bln);
                    this.employee = employee;
                    
                    nameArea = new JTextField(employee.getName());
                    pagerIDArea = new JTextField(employee.getPager());
                    startHourArea = new JTextField("" + employee.getStartHour());
                    stopHourArea = new JTextField("" + employee.getStopHour());
                    
                    init();
                }
                
                private void init() {
                    this.setLayout(new BorderLayout());
                    
                    JPanel contentPanel = new JPanel(new BorderLayout());
                    contentPanel.setBorder(new EmptyBorder(10,10,10,10));
                    
                    JPanel changePanel = new JPanel(new GridLayout(4, 2, 15,15));
                    
                    changePanel.add(nameLabel);
                    changePanel.add(nameArea);
                    
                    changePanel.add(pagerLabel);
                    changePanel.add(pagerIDArea);
                    
                    changePanel.add(startHourLabel);
                    changePanel.add(startHourArea);
                    
                    changePanel.add(stopHourLabel);
                    changePanel.add(stopHourArea);
                    
                    
                    JButton saveButton = new JButton("Save");
                    saveButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            save();
                            cancel();
                        }
                    });
                    
                    contentPanel.add(changePanel, BorderLayout.CENTER);
                    contentPanel.add(saveButton, BorderLayout.SOUTH);
                    
                    this.add(contentPanel, BorderLayout.CENTER);
                    
                    this.pack();
                    this.setMinimumSize(this.getSize());
                }
                
                private void save() {
                    String name = nameArea.getText();
                    String page = pagerIDArea.getText();
                    String startHourText = startHourArea.getText();
                    double startHour = Double.parseDouble(startHourText);
                    String stopHourText = stopHourArea.getText();
                    double stopHour = Double.parseDouble(stopHourText);
                    
                    employee.setName(name);
                    employee.setPager(page);
                    employee.setStartHour(startHour);
                    employee.setStopHour(stopHour);
                    handler.save();
                }
                
                private void cancel() {
                    this.dispose();
                }
                
            }
            
        }
        
    }