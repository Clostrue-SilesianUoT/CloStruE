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
package clostrue.biology.cell;

import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.CalcTask;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.enumerations.LifeTick;
import clostrue.model.SimModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a container for a subset of all cells in simulation.
 * 
 * If there is one calculation task -> all cells from the initial population
 * will be stored in ine cell population. 
 * If more calculations tasks will be created -> the initial cells will be
 * evenly distributed in cell populations of each calculation tasks.
 * 
 * The cell populations  i a container for cells and allow the calculation
 * of next cycle in simulation. 
 * 
 * @author Krzysztof Szymiczek 
 */
public final class CellPopulation {

    private final List<Cell> cells;             //Array List with cells in population
    private final Random _randomGenerator;      //Random Generator
    private final CalcTask calcTask;            //reference to calculation task.
    private final GenomeSynchronizedCollection genomes;     //genome collection
    private int deltaCellCount = 0;             //population size change over cycle 
    private int deltaDrivers = 0;               //driver mutation count change over cycle
    private int deltaPassengers = 0;            //passenger mutation count change over cycle
    public int curentHistoryCellCount = 0;      //curent cell count
    private final double              _noMutationProb;    //Division with no mutation probability in next cycle
    private final double              _drivMutationProb;  //Division with Driver Mutation probability in next cycle
    private final double              _passMutationProb;  //Division with Passenger Mutation probability in next cycle
    
    /**
     * Constructor of Cell population.
     * 
     * Cell population is created when calculation task is created
     * 
     * @param calcTask              reference to calculation task
     * @param initialCellSubsetSize size of the population-subset
     * @param motherCell            Mother cell to copy the genome from to have all cells from initial population
     *                              In the same clone group
     * @throws IOException
     */
    public CellPopulation(
            CalcTask            calcTask,
            int                 initialCellSubsetSize,
            Cell                motherCell
    ) throws IOException {    
        
        this.genomes = calcTask.getSim().getGenomes();
        this.calcTask = calcTask;
        _randomGenerator = new Random();    

        //Create each cell (the "starting" population according to model)
        //a subset of the initial population will be created (other calculation
        //tasks have their own subsets summing up to population size in total
        cells = Collections.synchronizedList(new ArrayList<> ());
        SimModel simModel = calcTask.getSim().getSimModel();
        
        //create next cells from the initial population
        for (int i = 0; i < initialCellSubsetSize; i++) {
            Cell createdCell = new Cell(
                    simModel,
                    genomes,
                    motherCell);
            cells.add(createdCell);
        }

        _drivMutationProb = calcTask.getSim().getDriverMutationProbability();
        _passMutationProb = calcTask.getSim().getPassengerMutationProbability();
        _noMutationProb   = calcTask.getSim().getNoMutationProbability();

    }

    /**
     * removes dead cells from the population
     */
    private void removeDeadCells(){
        for (int i = (cells.size() - 1); i >= 0; i--) {
            if (cells.get(i).isDead()) {
                cells.remove(cells.get(i));
            }
        }        
    }

    /**
     * Return the difference in cell count between begin and end 
     * of the simulation cycle
     * @return delta cell count
     */
    public int getDeltaCellCount() {
        return deltaCellCount;
    }

    /**
     * Return the difference in driver mutation count between begin and end
     * of the simulation cycle
     * @return delta driver mutation count
     */
    public int getDeltaDrivers() {
        return deltaDrivers;
    }

    /**
     * Returns the difference in passenger mutation count between begin and end
     * of the simulation cycle
     * @return delta passenger mutation count
     */
    public int getDeltaPassengers() {
        return deltaPassengers;
    }

    /**
     * Creates child cell with passenger mutation
     * @param parent parent cell
     */
    private void createChildCellwithPassengerMutation(Cell parent){
        deltaCellCount++;
        Cell newCell01 = new Cell(calcTask.getModel(), genomes, parent, false, true,
                curentHistoryCellCount,
                _randomGenerator);
        cells.add(newCell01);
        deltaPassengers++;
        StaticConsoleLogger.consoleLogPassengerDivision(calcTask, parent);
    }

    /**
     * Creates child cell with driver mutation
     * @param parent  parent cell
     */
    private void createChildCellwithDriverMutation(Cell parent){                     
        deltaCellCount++;
        Cell newCell10 = new Cell(calcTask.getModel(), genomes, parent, true, false,
                curentHistoryCellCount, 
                _randomGenerator);
        cells.add(newCell10);
        deltaDrivers++;
        StaticConsoleLogger.consoleLogDriverDivision(calcTask, parent);        
    }
    
    /**
     * Creates child cell with no mutation (identical clone)
     * @param parent parent cell
     */
    private void createChildCloneCell(Cell parent){
        // new cell is born without Mutation (Clean "Clone" Division)
        deltaCellCount++;
        Cell newCell00 = new Cell(calcTask.getModel(), genomes, parent, false, false,
                curentHistoryCellCount,
                _randomGenerator);
        cells.add(newCell00);
        StaticConsoleLogger.consoleLogClanCloneDivision(calcTask, parent);        
    }
    
    /**
     * Modells the cell division event within the simulator
     * @param cell cell which will divide
     */
    private void makeCellDivision(Cell cell){
        // in this situation a new cell is born and all the properties
        // driver or passenger mutation can occur, so we have to calculate the eventual mutation:
        // preparing the upper limit for normalization
        double _upperRandomLimit
                = _passMutationProb
                + _drivMutationProb
                + _noMutationProb;

        // lets decide if a mutation occurs, and if so, which kind is this
        double _tossResult = _upperRandomLimit * _randomGenerator.nextDouble();       
        
        if (_tossResult <= _passMutationProb)
            createChildCellwithPassengerMutation(cell);
        else if (_tossResult <= _passMutationProb
                + _drivMutationProb)
            createChildCellwithDriverMutation(cell);
        else if (_tossResult <= _passMutationProb
                + _drivMutationProb
                + _noMutationProb)
            createChildCloneCell(cell);
    }
    
    /**
     * Simulates the event of cell death
     * @param cell cell to be dead
     */
    private void makeCellDeath(Cell cell){
        // in this situation the cell is marked as dead
        // dead cells will be deleted after the current simulation cycle
        cell.die();
        deltaCellCount--;
        StaticConsoleLogger.consoleLogCellDied(calcTask, cell);        
    }
    
    /**
     * Performs next cycle of simulation for the contained cells.
     * 
     * This method does the calculations for the simulation cycle and is called 
     * by Backrgorund Task Trigger synchronously among all calculation tasks
     * which contain non empty cell populations.
     * 
     * @return                      true if next cycle should be executed or 
     *                              false when the curent cycle is the last one
     *                              according to coMaxCycles
     */
    @SuppressWarnings("empty-statement") //int mpInitialCellCountK,
    public boolean doNextCycle(){     

        StaticConsoleLogger.consoleLogTaskStarted(calcTask);
        
        int curentSimulationCycle               = calcTask.getSim().getCurrentCycle();//ok
        curentHistoryCellCount                  = calcTask.getSim().getStatistics().getHistoryCellCountInCycle(curentSimulationCycle);//ok    
        deltaCellCount                          = 0;
        deltaDrivers                            = 0;
        deltaPassengers                         = 0;
               
        calcTask.benchmarkStart();
        calcTask.setLastProcCycle(curentSimulationCycle); 
        
        StaticConsoleLogger.consoleLogCellCount(calcTask);

        if (curentSimulationCycle > 0)
            saveCells(curentSimulationCycle-1);//ok       
        
        if (calcTask.getSim().getTechnicalLastSaveCycle()){//raczej ok
            if (calcTask.getSim().isCbPrepareCells()){
                calcTask.getWriter().closeFile(true, curentSimulationCycle);                
            }            
            calcTask.getWriter().transferCellsIntoStatistics(calcTask, curentSimulationCycle);
            calcTask.benchmarkStop(deltaCellCount);          
            StaticConsoleLogger.consoleLogTaskFinished(calcTask);
            try {
                calcTask.getSim().barrier.await();
            } catch (InterruptedException | BrokenBarrierException ex) {
                Logger.getLogger(CellPopulation.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        removeDeadCells();

        //Simulate what haeppens with each cell from the population
        //in the curent simulation cycle
        int cellsAtTheBeginOfCycle = cells.size();
        // have to be indexed loop instead of iteration over all elements
        // becouse the life-tick effect may be adding cells, wchich will cause
        // the alghoritm to fall into endless loop if there will be divisio after division
        // and this would no stop at all, so leave ALWAYS the loop based on 
        // cellsAtTheBeginOfCycle. This is IMPORTANT !!!
        for (int i = 0; i < cellsAtTheBeginOfCycle; i++) {//raczej ok i tak sie wywaliło przy lini 210
            LifeTick _cellLifeTick = cells.get(i).getLifeTick(_randomGenerator);
            switch (_cellLifeTick) {
                case Division:
                    makeCellDivision(cells.get(i));
                    break;
                case Death:
                    makeCellDeath(cells.get(i));
                    break;
            }
        }
                
        //mark the current cycle as processed
        //return true if this as not the last cycle
        calcTask.benchmarkStop(deltaCellCount);
        StaticConsoleLogger.consoleLogTaskFinished(calcTask);
        try {
            calcTask.getSim().barrier.await();
            calcTask.getSim().updateLiveModeVariables(deltaCellCount, deltaDrivers, deltaPassengers);
        } catch (InterruptedException | BrokenBarrierException ex) {
            Logger.getLogger(CellPopulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Returns the cells within population.
     * This is used by the cell writer.
     * @return the ArrayList of cells within population
     */
    public List<Cell> getCells(){
        return cells;
    }  

    /**
     * Saves cell to file and memory
     * @param curentSimulationCycle curent simulation cycle
     */
    private void saveCells(int curentSimulationCycle){
        
        if (cells.size() > 0){        
            for (Cell cell : cells) {
                if (cell == null){
                    StaticConsoleLogger.log("Critical error while saving: cell is null");
                }
                
                Cell cellToSave = new Cell(cell);

                if (calcTask.getSim().isCbPrepareCells()){
                    StaticConsoleLogger.consoleLogWillWriteToCSV(calcTask, cellToSave );
                    calcTask.getWriter().writeToCSV(cellToSave , curentSimulationCycle, curentHistoryCellCount);
                    StaticConsoleLogger.consoleLogWrittenToCSV(calcTask, cellToSave );
                }
                    calcTask.getWriter().writeToMemory(cellToSave , curentSimulationCycle);
              
//                if (calcTask.getModel().getTechParams().isRbHistoryPickAll()){
//                    calcTask.getSim().getStatistics().addCellToHistogramStats(cellToSave);
//                }

            }
            calcTask.getSim().getLiveStats().incrementShadowSize(cells.size());

        } else {
            StaticConsoleLogger.consoleLogNothingToSave(calcTask);
        }        
    }
    
}
