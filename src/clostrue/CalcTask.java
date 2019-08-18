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
package clostrue;

import clostrue.model.SimModel;
import clostrue.biology.cell.CellPopulation;
import clostrue.biology.cell.Cell;
import clostrue.benchmark.BenchmarkEntry;
import clostrue.hardcodes.Activity;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Implements one single Calculation Task.
 * 
 * A calculation task is handling the sumulation computations
 * for a particular subset of cells of the whole population
 * A calculation task is started in background progressing
 * By the Simulation object
 * 
 * @author Krzysztof Szymiczek
 */ 
public class CalcTask extends SwingWorker<Void,Void> {

    private int             id;                     //task identifier
    private int             lastProcCycle;          //last cycle processed by task
    private Simulation      sim;                    //reference to the simulation object
    private SimModel        model;                  //simulation model
    private CellPopulation  population;             //cell population handled by the task
    private CellWriter      writer;                 //tool for writing cell data to files      
    private BenchmarkEntry  benchmarkEntry;         //entry for benchmarking

    /**
     * Starts benchmarking
     */
    public void benchmarkStart(){
        benchmarkEntry = new BenchmarkEntry(lastProcCycle, this, population.curentHistoryCellCount);
    }

    /**
     * Fires property change
     * @param propertyName property name
     * @param oldValue old value
     * @param newValue new value
     */
    public final void doFirePropertyChange(String propertyName, Object oldValue, Object newValue) {
        firePropertyChange(propertyName, oldValue, newValue);
    }    
    /**
     * returns benchmark entry
     * @return benchmark entry
     */
    public BenchmarkEntry getBenchmarkEntry() {
        return benchmarkEntry;
    }
    
    /**
     * Stops (finishes) benchmarking
     * @param deltaCellCount cellSizeDifference
     */
    public synchronized void benchmarkStop(int deltaCellCount){
        benchmarkEntry.benchmarkStop(population.curentHistoryCellCount + deltaCellCount);
        sim.getBenchmark().addEntry(benchmarkEntry);
    }

    public double getCellCountGain(){
        return benchmarkEntry.getCellCountGain();
    }
        
    /**
     * Parameter passing and object creation.
     * The constructor of SwingWorker extending class does not allow
     * to pass parameters. Instead this method is created and acts
     * as "normal" class constructor
     * 
     * @param callingSimulation             reference to Calling Simulation 
     * @param taskID                        unique task identifier
     * @param initialCellSubsetSize         the cell population size when the task is created
     * @param motherCell                    Mother cell to ensure all cells from initial population
     *                                      Are in the same clone group
     */
    public CalcTask(
         Simulation         callingSimulation,
         int                taskID,
         int                initialCellSubsetSize, 
         Cell               motherCell)
    {

        StaticConsoleLogger.logActivity(callingSimulation.getIteration(), 
            Activity.createTaskID + 
            String.valueOf(taskID), Activity.started);

        sim            = callingSimulation;
        id             = taskID;
        lastProcCycle  = 0;
        model          = new SimModel(sim.getIteration(), sim.getSimModel());        
        writer         = new CellWriter(this);
     
        try {
            population = new CellPopulation(
                    this,
                    initialCellSubsetSize,    
                    motherCell);
        } catch (IOException ex) {
            Logger.getLogger(CalcTask.class.getName()).log(Level.SEVERE, null, ex);
        }

        StaticConsoleLogger.logActivity(callingSimulation.getIteration(), 
                Activity.createTaskID + 
                String.valueOf(taskID), Activity.finished);

    }
           
    /**
     * get local simulation model copy
     * @return local simulation model copy
     */
    public SimModel getModel() {
        return model;
    }
   
    /**
     * Starts the simulation logic in background as infinite loop.
     * The loop is executes as long as the doNextCycle of the Cell Population
     * Returns true. If the cellPupulation decides that there are no further steps
     * The simulation task will end. Before the file handler for saving data
     * is closed also.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Override    
    @SuppressWarnings("empty-statement")
    public Void doInBackground()
        throws InterruptedException, ExecutionException, IOException {
        while (population.doNextCycle());
        return null;                      
    }

    /**
     * Returns the technical task identifier.
     * 
     * @return task id
     */
    public int getId() {
        return id;
    }
    
    /**
     * Adds one cell to the population handled by the calculation task.
     * This is usefull in load balancing between tasks, where
     * cells can be shifted between calculation tasks
     * 
     * @param cell
     */
    public void addCellToPopulation(Cell cell){
        population.getCells().add(cell);
    }
    
    /**
     * Removes one cell from the population handled by the calculation task.
     * This is usefull in load balancing between tasks, where
     * cells can be shifted between calculation tasks
     * 
     * @param cell
     */
    public void removeCellFromPopulation(Cell cell){
        population.getCells().remove(cell);
    }
    
    /**
     * Gets one (first) cell from the population handled by the calculation task.
     * This is usefull in load balancing between tasks, where
     * cells can be shifted between calculation tasks
     * 
     * @return
     */
    public Cell getCellFromPopulation(){
        return population.getCells().get(0);
    }

    /**
     * get last processed cycle
     * @return last processed cycle
     */
    public int getLastProcCycle() {
        return lastProcCycle;
    }

    /**
     * sets last processed cycle
     * @param lastProcCycle last processed cycle
     */
    public void setLastProcCycle(int lastProcCycle) {
        this.lastProcCycle = lastProcCycle;
    }
 
    /**
     * return simulation
     * @return simulatio
     */
    public Simulation getSim() {
        return sim;
    }

    /**
     * Returns the CellWriter object assigned to the calculation task.
     * Each calculation task has it's own writer object assigned to it.
     * This prevents I/O access problems if multiple tasks would like to 
     * Write data at the same time.
     * 
     * @return
     */
    public CellWriter getWriter() {
        return writer;
    }
    
    /**
     * get cell population size
     * @return cell population size
     */
    public int getCellPopulationSize(){
        return population.getCells().size();
    }    

    /**
     * get cell population
     * @return cell population
     */
    public CellPopulation getPopulation() {
        return population;
    }

}
