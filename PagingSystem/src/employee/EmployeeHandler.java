/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee;

import employee.gui.EmployeePanel;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import util.Utilities;
import static util.Utilities.getMainDirPath;

/**
 *
 * @author Shawn
 */
public class EmployeeHandler {
    
    private final static File employeeDir = new File(getMainDirPath() + "/pagingsystem/employees/");
    private final static String[] days = Utilities.getDaysOfWeek();
    private ArrayList<Employee> allEmployees;
    private final EmployeePanel parent;
    
    
    
    public EmployeeHandler() {
        super();
        readEmployees();
        parent = new EmployeePanel(this);
    }
    
    private void readEmployees() {
        //if there is an error parsing employee
        int lineStop = 1;
        String fileStop = null;
        File file = null;
        try{
            if(!employeeDir.exists())
                employeeDir.mkdirs();
                
            allEmployees = new ArrayList();
           
            for(int i = 0; i < days.length; i++) {
                
                try {
                fileStop = days[i];
                file = new File(employeeDir.getPath() + "/" + days[i] + ".csv");

                Scanner scanner = new Scanner(file);
                lineStop = 1;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(!line.trim().equals("")) {
                        String[] tokens = line.split(",");
                        String name = tokens[0];
                        String pager = tokens[1];
                        double startTime = Double.parseDouble(tokens[2]);
                        double stopTime = Double.parseDouble(tokens[3]);
                        int priority = Integer.parseInt(tokens[4]);
                        Employee employee = new Employee(name, pager, startTime, stopTime, priority, i + 1);
                        allEmployees.add(employee);
                        lineStop++;
                    }
                }
                
                }catch(FileNotFoundException ex) {
                    try {
                        file.createNewFile();
                    } catch(IOException ex1) {
                        JOptionPane.showMessageDialog(parent, "Couldn't create file " + fileStop + ".csv");
                        Logger.getLogger(EmployeeHandler.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(5);
                    }
                }
            }
            
        }  catch(Exception ex) {
            JOptionPane.showMessageDialog(parent, "Error parsing employee in " + fileStop + ".csv at line " + lineStop);
            Logger.getLogger(EmployeeHandler.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public static void sortByPriority(Employee[] employees) {
        Arrays.sort(employees);
    }
    
    public static void sortByPriority(ArrayList<Employee> employeesList) {
        Collections.sort(employeesList);
    }
    
    public EmployeePanel getEmployeePanel() {
        return parent;
    }
    
    public void save() {
        ArrayList<Employee>[] daysEmployees = new ArrayList[Utilities.getDaysOfWeek().length];
        for(int i = 0; i < daysEmployees.length; i++) {
            daysEmployees[i] = new ArrayList();
        }
        
        for(Employee employee: getAllEmployees()) {
            daysEmployees[employee.getDayWorking() - 1].add(employee);
        }
        
        for(int i = 0; i < days.length; i++) {
            String day = days[i];
            File file = new File(employeeDir.getPath() + "/" + day + ".csv");
            try {   
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                
                String text;
                for(Employee employee: daysEmployees[i]) {
                    text = "" + employee.getName() + "," + employee.getPager() + "," + employee.getStartHour() + "," + employee.getStopHour() + "," + employee.getPriority();
                    writer.println(text);
                }
                
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(EmployeeHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}

