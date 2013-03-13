/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee;

import java.awt.BorderLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.*;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import static util.Utilities.getMainDirPath;

/**
 *
 * @author Shawn
 */
public class EmployeeHandler {
    
    private final static File employeeDir = new File(getMainDirPath() + "/pagingsystem/employees/");
    private final static String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    
    private ArrayList<Employee> allEmployees;
    private final EmployeePanel parent;
    
    
    
    public EmployeeHandler() {
        super();
        readEmployees();
        parent = new EmployeePanel();
    }
    
    private void readEmployees() {
        //if there is an error parsing employee
        int lineStop = 1;
        String fileStop = null;
        try{
            if(!employeeDir.exists())
                employeeDir.mkdirs();
                
            allEmployees = new ArrayList();
           
            for(int i = 0; i < days.length; i++) {
                fileStop = days[i];
                File file = new File(employeeDir.getPath() + "/" + days[i] + ".csv");
                Scanner scanner = new Scanner(file);
                lineStop = 1;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] tokens = line.split(",");
                    String pager = tokens[0];
                    double startTime = Double.parseDouble(tokens[1]);
                    double stopTime = Double.parseDouble(tokens[2]);
                    int priority = Integer.parseInt(tokens[3]);
                    Employee employee = new Employee(pager, startTime, stopTime, priority, i + 1);
                    allEmployees.add(employee);
                    lineStop++;
                }
            }
            
        } catch(FileNotFoundException ex) {
            JOptionPane.showMessageDialog(parent, "Please place a file called \"" + fileStop + ".csv\" into " + employeeDir.getParent() + "/");
            System.exit(5);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(parent, "Error parsing employee in " + fileStop + ".csv at line " + lineStop);
            System.exit(4);
        }
    }
    
    public ArrayList<Employee> getAllEmployees() {
        return allEmployees;
    }
    
    public Employee[] getAvailibleEmployees() {
        ArrayList<Employee> employees = getAllEmployees();
        ArrayList<Employee> workingEmployees = new ArrayList();
        
        for(int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            if(employee.isCurrentlyWorking())
                workingEmployees.add(employee);
        }
        
        return workingEmployees.toArray(new Employee[workingEmployees.size()]);
    }
    
    public Employee[] getCurrentPrioritizedEmployees() {
        Employee[] availible = getAvailibleEmployees();
        sortByPriority(availible);
        return availible;
    }
    
    private static void sortByPriority(Employee[] employees) {
        Arrays.sort(employees);
    }
    
    private static void sortByPriority(ArrayList<Employee> employeesList) {
        Collections.sort(employeesList);
    }
    
    public EmployeePanel getEmployeePanel() {
        return parent;
    }
    
    public class EmployeePanel extends JPanel {
        
        private final int[] daysOfWeek = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
        private final String[] namesOfDayOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        public EmployeePanel() {
            super(new BorderLayout());
            init();
        }
        
        private void init() {
            JTabbedPane weekTabbed = new JTabbedPane();
            for(int i = 0; i < daysOfWeek.length; i++) {
                weekTabbed.addTab(namesOfDayOfWeek[i], new EmployeeDayPanel(daysOfWeek[i]));
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
                sortByPriority(onDayEmployees);
                String[] numbers = new String[onDayEmployees.size()];
                for(int i = 0; i < numbers.length; i++) {
                    numbers[i] = onDayEmployees.get(i).getPager();
                }
                
                JList list = new JList(numbers);
                this.add(list, BorderLayout.CENTER);
                
            }
            
            private void addAllEmployees() {
                for(Employee employee: allEmployees) {
                    if(dayOfWeek == employee.getDayWorking())
                        onDayEmployees.add(employee);
                }
            }
            
    
        }
    }
}

