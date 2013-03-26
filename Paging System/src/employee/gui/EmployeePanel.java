/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee.gui;

import employee.Employee;
import employee.EmployeeHandler;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        private EmployeeDayPanel[] dayPanels;
        
        public EmployeePanel(EmployeeHandler handler) {
            super(new BorderLayout());
            this.handler = handler;
            init();
        }
        
        private void init() {
            String[] namesOfDaysOfWeek = Utilities.getDaysOfWeek();
            dayPanels = new EmployeeDayPanel[daysOfWeek.length];
            JTabbedPane weekTabbed = new JTabbedPane();
            for(int i = 0; i < daysOfWeek.length; i++) {
                dayPanels[i] = new EmployeeDayPanel(daysOfWeek[i]);
                weekTabbed.addTab(namesOfDaysOfWeek[i], dayPanels[i]);
                System.out.println(daysOfWeek[i]);
            }
            this.add(weekTabbed, BorderLayout.CENTER);
        }
        
        private void updateLists() {
            for(EmployeeDayPanel panel: dayPanels) {
                panel.updateList();
            }
        }
        
        /**
         * Displays all the employees that work on the day imputed into the constructor. They are sorted by priority
         */
        private class EmployeeDayPanel extends JPanel {
            
            private final int dayOfWeek;
            
            private EmployeeList list;
            
            public EmployeeDayPanel(int dayOfWeek) {
                super(new BorderLayout());
                this.dayOfWeek = dayOfWeek;
                init();
            }
            
            private void init() {
                this.setBorder(new EmptyBorder(10,10,10,10));
                
                JScrollPane scroller = new JScrollPane();
                list = new EmployeeList();
                updateList();
                
                JButton upButton = new JButton("^");
                upButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int sIndex = list.getSelectedIndex();
                        int tIndex = sIndex - 1;
                        
                        if(sIndex != -1) {
                            if(tIndex >= 0) {
                                ListModel lm = list.getModel();
                                Employee[] array = new Employee[lm.getSize()];
                                for(int i = 0; i < array.length; i++) {
                                    array[i] = (Employee) lm.getElementAt(i);
                                }
                                
                                array[sIndex].goUpPriority();
                                array[tIndex].goDownPriority();
                                
                                updateLists();
                            } else System.out.println("OOB");
                        }
                        else System.out.println("Nothing selected");
                                
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
                                
                                updateLists();
                            }
                    }
                });
                
                JButton addButton = new JButton("Add");
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        Employee newEmp = new Employee();
                        handler.getAllEmployees().add(newEmp);
                        EmployeeEditDialog dialog = new EmployeeEditDialog(newEmp);
                        dialog.setVisible(true);
                        System.out.println("HIT");
                        if(newEmp.getName() == null) {
                            handler.getAllEmployees().remove(newEmp);
                        } else {
                            updateLists();
                        }
                    }
                });
                
                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int index = list.getSelectedIndex();
                        if(index != -1) {
                            ListModel lm = list.getModel();
                            Employee[] array = new Employee[lm.getSize() - 1];
                            Employee delete = (Employee) lm.getElementAt(index);
                            
                            handler.getAllEmployees().remove(delete);
                            updateLists();
                        }
                    }
                });
                
                scroller.setViewportView(list);
                
                JPanel buttonPanel = new JPanel(new GridLayout(2,1));
                buttonPanel.add(upButton);
                buttonPanel.add(downButton);
                
                JPanel buttonPanel2 = new JPanel(new GridLayout(1,2));
                buttonPanel2.add(addButton);
                buttonPanel2.add(removeButton);
                
                this.add(scroller, BorderLayout.CENTER);
                this.add(buttonPanel, BorderLayout.EAST);
                this.add(buttonPanel2, BorderLayout.SOUTH);
                
                
            }
            
            private void updateList() {
                ArrayList<Employee> onDay = new ArrayList();
                for(Employee employee: handler.getAllEmployees()) {
                    if(employee.getDayWorking() == dayOfWeek) {
                        onDay.add(employee);
                    }
                }
                
                EmployeeHandler.sortByPriority(onDay);
                
                rectifyPriority(onDay);
                
                list.setListData(onDay.toArray(new Employee[onDay.size()]));
                
                handler.save();
            }
            
            private void rectifyPriority(ArrayList<Employee> emps) {
                for(int i = 0; i < emps.size(); i++) {
                    emps.get(i).setPriority(i);
                }
            }
    
            
            private class EmployeeList extends JList {
                
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
                
                
                public EmployeeList() {
                    super();
                    
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
                        changeStart(""+ Employee.timeFormat(employee.getStartHour()));
                        changeStop(""+Employee.timeFormat(employee.getStopHour()));

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
                        stopHourLabel = new JLabel("Employee End of Shift"),
                        dayWorkingLabel = new JLabel("Day Working");
                
                private JTextField nameArea, pagerIDArea;
                private JSpinner startTimeSpinner, stopTimeSpinner;
                private JComboBox dayWorkingCombo;
                
                public EmployeeEditDialog(Employee employee) {
                    this(null, true, "Edit Employee", employee);
                }
                
                private EmployeeEditDialog(Frame frame, boolean bln, String string, Employee employee) {
                    super(frame, string, bln);
                    this.employee = employee;
                    
                    nameArea = new JTextField(employee.getName());
                    pagerIDArea = new JTextField(employee.getPager());
                    
                    DateFormat format = new SimpleDateFormat("HH:mm");
                    try {
                        Date start = format.parse(Employee.timeFormat(employee.getStartHour()));
                        Date stop = format.parse(Employee.timeFormat(employee.getStopHour()));

                        Date minDate = format.parse("00:00");
                        Date maxDate = format.parse("23:59");
                        SpinnerDateModel startModel = new SpinnerDateModel(start, minDate, maxDate, Calendar.MINUTE);
                        SpinnerDateModel stopModel = new SpinnerDateModel(stop, minDate, maxDate, Calendar.MINUTE);
                        startTimeSpinner = new JSpinner(startModel);
                        stopTimeSpinner = new JSpinner(stopModel);
                        JSpinner.DateEditor de = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
                        startTimeSpinner.setEditor(de);
                        de = new JSpinner.DateEditor(stopTimeSpinner, "HH:mm");
                        stopTimeSpinner.setEditor(de);
                    } catch (ParseException ex) {ex.printStackTrace(System.err);}
                    
                    
                    
                    
                    dayWorkingCombo = new JComboBox(Utilities.getDaysOfWeek());
                    dayWorkingCombo.setSelectedIndex(employee.getDayWorking() - 1);
                           
                    
                    init();
                    
                }
                
                private void init() {
                    this.setLayout(new BorderLayout());
                    
                    JPanel contentPanel = new JPanel(new BorderLayout());
                    contentPanel.setBorder(new EmptyBorder(10,10,10,10));
                    
                    JPanel changePanel = new JPanel(new GridLayout(5, 2, 15,15));
                    
                    changePanel.add(nameLabel);
                    changePanel.add(nameArea);
                    
                    changePanel.add(pagerLabel);
                    changePanel.add(pagerIDArea);
                    
                    changePanel.add(startHourLabel);
                    changePanel.add(startTimeSpinner);
                    
                    changePanel.add(stopHourLabel);
                    changePanel.add(stopTimeSpinner);
                    
                    changePanel.add(dayWorkingLabel);
                    changePanel.add(dayWorkingCombo);
                    
                    JButton saveButton = new JButton("Save");
                    saveButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            save();
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
                    double startHour = getStartHour();
                    double stopHour = getStopHour();
                    int dayWorking = dayWorkingCombo.getSelectedIndex() + 1;
                    
                    if(name.equals("")) {
                        JOptionPane.showMessageDialog(this, "Must enter employee name", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if(page.equals("")){
                        JOptionPane.showMessageDialog(this, "Must enter pager ID", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if(startHour >= stopHour) {
                        JOptionPane.showMessageDialog(this, "Start of shift must be less than end of shift", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if(dayWorking == 0) {
                        JOptionPane.showMessageDialog(this, "Must choose a day to work", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                        
                        
                    employee.setName(name);
                    employee.setPager(page);
                    employee.setStartHour(startHour);
                    employee.setStopHour(stopHour);
                    employee.setDayWorking(dayWorking);
                    handler.save();
                    this.dispose();
                    updateLists();
                }
                
                private double getStartHour() {
                    Calendar cal = getCal(startTimeSpinner);
                    return getTime(cal);
                }
                
                private double getStopHour() {
                    Calendar cal = getCal(stopTimeSpinner);
                    return getTime(cal);
                }
                
                private Calendar getCal(JSpinner spinner) {
                    Calendar cal = Calendar.getInstance();
                    Date date = ((SpinnerDateModel)spinner.getModel()).getDate();
                    cal.setTime(date);
                    return cal;
                }
                
                private double getTime(Calendar cal) {
                    double hour = cal.get(Calendar.HOUR_OF_DAY);
                    double minute = cal.get(Calendar.MINUTE);
                    return hour + (minute / 60.0);
                } 
                
                private void cancel() {
                    this.dispose();
                }

           
            }
            
        }
        
    }