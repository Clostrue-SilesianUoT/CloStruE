/* 
 * Copyright (C) 2019 Silesian Technical University, Gliwice, Poland
 * Authors / Contributors: Krzysztof Szymiczek and Andrzej Polański
 * Affiliation: Department of Informatics
 *
 * This program is intended to be solely used for reaserch purpouses
 * by the Students and Employees of the
 * Silesian Technical University in Gliwice, Poland (Politechnika Śląska)
 * and for other research and development non-commercial activities
 * by researchers world-wide interrested in the area of simulations
 * of cancer clonal evolution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 *
 */
package clostrue.toolbox;

import clostrue.CalcTask;
import clostrue.GuiController;
import clostrue.Simulation;
import clostrue.biology.cell.Cell;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.LogText;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Artifact;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * Implements various console and file logging options with formatting
 * @author Krzysztof Szymiczek
 */
public class StaticConsoleLogger {
    static AtomicLong     id = new AtomicLong(0); //message ID        
    static boolean        logToConsole;           //swithc if logging to console 
    static boolean        logToFile;              //swithc if logging to file 
    static String         logFilePath;            //path to log file
    static FileWriter     logFile;                //log file
    static BufferedWriter fileBuffer;             //log file buffer
    
    /**
     * Create log file
     * @param inLogFilePath path for file with simulation log
     */
    public static void createLogFile(String inLogFilePath) {
        StaticConsoleLogger.logFilePath = inLogFilePath;
        
        if(!StaticConsoleLogger.logToFile) {
            return;
        }
        
        try {
            StaticConsoleLogger.logFile = new FileWriter(StaticConsoleLogger.logFilePath);
        } catch (IOException ex) {
            Logger.getLogger(StaticConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void openLogFileBuffer() {
        if(!StaticConsoleLogger.logToFile) {
            return;
        }
        
        StaticConsoleLogger.fileBuffer = new BufferedWriter(StaticConsoleLogger.logFile, Constant.fileBufferSize);
    }
    
    public static void closeLogFileBuffer() {
        if(!StaticConsoleLogger.logToFile) {
            return;
        }
        
        try {
            StaticConsoleLogger.fileBuffer.close();
            StaticConsoleLogger.fileBuffer = null;
        } catch (IOException ex) {
            Logger.getLogger(StaticConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void flushLogFileBuffer() {
        if(!StaticConsoleLogger.logToFile) {
            return;
        }
        
        try {
            StaticConsoleLogger.fileBuffer.flush();
        } catch (IOException ex) {
            Logger.getLogger(StaticConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Sets the flag if console logging should be switched ON or OFF
     * @param inLogToConsole 
     */
    public static void setLogToConsole(boolean inLogToConsole){
        logToConsole = inLogToConsole;
    }
    
    /**
     * Sets the flag if file logging should be switched ON or OFF 
     * @param inLogToFile file logging (boolean)
     */
    public static void setLogToFile(boolean inLogToFile){
        logToFile = inLogToFile;
    }
    
    /**
     * Returns the Cycle/Task/Cell String for console logging
     * @param calcTask calculation task
     * @param cell cell for which the data should be displayed
     * @return String with Cycle/Task/Cell
     */
    public static String getCycleTaskCellString(CalcTask calcTask, Cell cell){
        
        return getCycleTaskCellString(
            calcTask.getSim().getCurrentCycle(),
            calcTask.getId(),
            cell);
        
    }

    /**
     * Returns the Cycle/Task/Cell String for console logging
     * @param cycle current simulation cycle
     * @param taskID current task id
     * @param cell current cell
     * @return String with Cycle/Task/Cell
     */
    public static String getCycleTaskCellString(int cycle, int taskID, Cell cell){
        if (cell != null){
            return String.format(Constant.cycleTaskCellPattern,
                String.valueOf(cycle),
                String.valueOf(taskID),
                cell.getId()
                );      
        } else {
            return String.format(Constant.cycleTaskPattern,
                String.valueOf(cycle),
                String.valueOf(taskID)
                );                  
        }
    }
    
    /**
     * Logs the curent cell count to the console
     * @param calcTask
     */
    public static void consoleLogCellCount(CalcTask calcTask){
        if (logToConsole){
            if ( calcTask.getCellPopulationSize() > 0 ){
                log(getCycleTaskCellString(calcTask, null) +                        
                    LogText.hasCellPopulationSize + String.valueOf(calcTask.getCellPopulationSize())
                );        
            } else {
                log(getCycleTaskCellString(calcTask, null) +                        
                    LogText.hasEmptyPopulation 
                );                        
            }
        }       
    }
    
    /**
     * Logs to the console the information that cells have been saved
     * @param calcTask
     */
    public static void consoleLogSavedState(CalcTask calcTask){
        if (logToConsole){
            StaticConsoleLogger.log(getCycleTaskCellString(calcTask, null) +                        
                LogText.savedCells
            );                   
        }        
    }

     /**
     * Logs to the console the information that cells will be saved
     * @param calcTask
     */
    public static void consoleLogWillSaveState(CalcTask calcTask){
        if (logToConsole){
            StaticConsoleLogger.log(getCycleTaskCellString(calcTask, null) +                        
                LogText.willSaveCells
            );                   
        }        
    }
    
    /**
     * Logs to the console the information that no cells are available for saving
     * @param calcTask
     */
    public static void consoleLogNothingToSave(CalcTask calcTask){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, null) +                        
                LogText.nothingToSave
            );                   
        }           
    }

    /**
     * Logs to the concole that there are no cells to process (empty population)
     * @param calcTask
     */
    public static void consoleLogNoCellsToProcess(CalcTask calcTask){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, null) +                        
                LogText.nothingToProcess
            );                   
        }           
    }

    /**
     * Logs activity
     * @param calcTask calculation tasks
     * @param activity activity string
     * @param state activity state string
     */
    public static void consoleLogActivity(CalcTask calcTask, String activity, String state){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, null) +                        
                activity + Artifact.space + state
            );                   
        }
    }           

    /**
     * Logs activity
     * @param cycle simulation cycle
     * @param taskID calculation task ID
     * @param activity activity string
     * @param state activity state string
     */    
    public static void consoleLogActivity(int cycle, int taskID, String activity, String state){
        if (logToConsole){
            log(getCycleTaskCellString(cycle, taskID, null) +                        
                activity + Artifact.space + state
            );                   
        }           
    }
    
    /**
     * Logs to the console the event of cell division with passenger mutation
     * @param calcTask calculation task
     * @param cell cell which divided with passenger mutation
     */
    public static void consoleLogPassengerDivision(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) +  
                LogText.divisionTypePassenger
            );
        }        
    }

    /**
     * Logs to the console the information that a given cell will be written
     * to csv Cell File
     * @param calcTask calculation task
     * @param cell to be written to csv Cell File
     */
    public static void consoleLogWillWriteToCSV(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) +  
                LogText.willWriteToCsv
            );
        }        
    }            
   
    /**
     * Logs to the console the information that a given cell has been written
     * to csv Cell File
     * @param calcTask calculation task
     * @param cell cell to be written to csv Cell File
     */
    public static void consoleLogWrittenToCSV(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) +  
                LogText.writtenToCsv
            );
        }        
    }    
    
    /**
     * Logs to the console the event of a cell division with driver muation
     * @param calcTask calculation task
     * @param cell cell which divided with passenger mutation
     */
    public static void consoleLogDriverDivision(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) +  
                LogText.divisionTypeDriver
            );
        }        
    }

    /**
     * Logs to the console the event of a cell division without mutation
     * @param calcTask calculation task
     * @param cell cell which divided without mutation
     */
    public static void consoleLogClanCloneDivision(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) +  
                LogText.divisionTypeCleanClone
            );
        }        
    }

    /**
     * Logs to the console the event of a cell death
     * @param calcTask calculation task
     * @param cell cell which died
     */
    public static void consoleLogCellDied(CalcTask calcTask, Cell cell){
        if (logToConsole){
            log(getCycleTaskCellString(calcTask, cell) + 
                LogText.diesInAgeOf + cell.getAge()
            );
        }        
    }    

    /**
     * Logs task finished current cycle
     * @param calcTask calculation task
     */
    synchronized public static void consoleLogTaskFinished(CalcTask calcTask){
        if (logToConsole){
            StaticConsoleLogger.log(StaticConsoleLogger.getCycleTaskCellString(calcTask, null) +                        
                LogText.finishedCurrentCycle
            );                   
        }        
    }    
    /**
     * Logs task started current cycle
     * @param calcTask calculation task
     */
    synchronized public static void consoleLogTaskStarted(CalcTask calcTask){
        if (logToConsole){
            StaticConsoleLogger.log(StaticConsoleLogger.getCycleTaskCellString(calcTask, null) +                        
                LogText.startedCurrentCycle
            );                   
        }        
    }     
    
    /**
     * Logs not yet processed tasks to console
     * @param simulation simulation
     * @param curentCycle current cycle
     * @param tasksNotYetProcessed tasks not yet processed
     */
    synchronized public static void tasksToConsole(
            Simulation simulation, 
            int curentCycle, 
            int tasksNotYetProcessed){
        if (logToConsole){
            log(String.format(Constant.cyclePattern,
                String.valueOf(curentCycle)
                ) +       
                LogText.notYetProcessedTasks +
                String.valueOf(tasksNotYetProcessed)
            );
        }        
    }

    //synchronized System.Out.Println for messages in chronologic order
    public static synchronized void sSystemOutPrintln(String str) {
        System.out.println(str);
    }
    
    //synchronized System.Out.Println for messages in chronologic order
    public static void systemOutPrintln(String str) {
        sSystemOutPrintln(String.format(LogText.messageOrder,
                String.valueOf(id.incrementAndGet())
                ) + str);
    }
    
    public static void logToFile(String msg) {
        if(!StaticConsoleLogger.logToFile) {
            return;
        }
                
        if(StaticConsoleLogger.fileBuffer == null) {
            return;
        }
               
        try {
            StaticConsoleLogger.fileBuffer.write(String.format(LogText.messageOrder,
                                                 String.valueOf(id.incrementAndGet())
                                                 ) + msg);
            StaticConsoleLogger.fileBuffer.write("\n");
        } catch (IOException ex) {
            Logger.getLogger(StaticConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //log Message
    public static void log(String str) {
        StaticConsoleLogger.systemOutPrintln(str);
        StaticConsoleLogger.logToFile(str);
    }

    //save iteration steps log to console in chronologic order
    public static synchronized void systemOutPrintlnIterationStep(
            Integer currIter, 
            Integer maxIter, 
            String str) {
        Date date = new Date();
        StaticConsoleLogger.log(date.toString() 
                + LogText.iteration 
                + String.valueOf(currIter) 
                + LogText.of 
                + String.valueOf(maxIter) + " " + str);
    }
    
    public static synchronized void logRSA(String msg) {
        Date date = new Date();
        StaticConsoleLogger.log(date.toString() + msg);
    }

    /**
     * Logs to console a current state of the current activity
     * @param iteration current iteration
     * @param activity current activity
     * @param state currect state (started / stopped / skipped)
     */
    public static void logActivity(int iteration, String activity, String state) {
        StaticConsoleLogger.systemOutPrintlnIterationStep(iteration, 
                GuiController.getSettings().getIntValue(Param.inIterations), 
                activity + Artifact.space + state);
    }

    /**
     * Logs to console a current state of the current activity
     * @param iteration current iteration
     * @param text text to log
     */
    public static void log(int iteration, String text) {
        StaticConsoleLogger.systemOutPrintlnIterationStep(iteration, 
                GuiController.getSettings().getIntValue(Param.inIterations), 
                text);
    }    
    
    /**
     * Logs to console a current state of the current activity
     * @param iteration current iteration
     * @param activity current activity
     * @param state currect state (started / stopped / skipped)
     * @param part current part of the activity
     */
    public static void logActivity(int iteration, String activity, String state, String part) {
        StaticConsoleLogger.systemOutPrintlnIterationStep(iteration, 
                GuiController.getSettings().getIntValue(Param.inIterations), 
                activity + Artifact.space + part + Artifact.space + state);
    }
    
}
