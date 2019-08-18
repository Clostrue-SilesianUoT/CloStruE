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

package clostrue.postprocessing.analysis;

import java.util.concurrent.ConcurrentHashMap;
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.GenomePart;
import clostrue.Settings;
import clostrue.Simulation;
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Param;
import clostrue.collections.CellCollectionWithHolderData;
import clostrue.collections.CellIndexHolder;
import clostrue.postprocessing.DriverMutationHistogramKey;
import clostrue.postprocessing.PassengerMutationHistogramKey;
import clostrue.toolbox.StaticConsoleLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 *
 * Container for miscelanious statistics
 */
public class Statistics {

    private final ConcurrentHashMap <DriverMutationHistogramKey, List<Cell>> histogramDriverMutations;    
    private final ConcurrentHashMap <PassengerMutationHistogramKey, List<Cell>> histogramPassengerMutations;
    private final ConcurrentHashMap <Integer, Integer> histogramShadowDriverMutations;    
    private final ConcurrentHashMap <Integer, Integer> histogramShadowPassengerMutations;
    private final ConcurrentHashMap <Integer, Integer> histogramSurvivorsDriverMutationsCount;    
    private final ConcurrentHashMap <Integer, Integer> histogramSurvivorsPassengerMutations;
    private final ConcurrentHashMap <Double,  Integer> histogramShadowDrivePassRatio;
    private final ConcurrentHashMap <Integer, Integer> histogramPopulationSize;
        
    private final AtomicIntegerArray    initialCellSubsetSize;          //the initial sizes per each calculation task
    private final AtomicIntegerArray    historyCellCountN;              //subsequent changes in population size over the time
    private final AtomicIntegerArray    historyDriverMutationCount;     //subsequent cumulated driver mutation count
    private final AtomicIntegerArray    historyPassengerMutationCount;  //subsequent cumulated passenger mutation count
    private final double[]              historyDriverPassRatio;         //subsequent changes in driver / passenger muatations ratio    
    private ConcurrentHashMap<GenomePart, List<Cell>> clones;
    private final HashMap<GenomePart, CellIndexHolder> internalClones;
    
    private final int iteration;
    private final GenomeSynchronizedCollection genomes;
    private final CellCollection cellCollection;

    public CellCollection getCellCollection() {
        return cellCollection;
    }    

    public HashMap<GenomePart, CellIndexHolder> getInternalClones() {
        return internalClones;
    }
    
    /**
 * Default consturctor
     * @param sim
     * @param settings
 */
    public Statistics(Simulation sim, Settings settings) {
        cellCollection = new CellCollection();
        genomes = sim.getGenomes();
        histogramDriverMutations    = new ConcurrentHashMap<> ();
        histogramPassengerMutations = new ConcurrentHashMap<> ();
        histogramShadowDriverMutations = new ConcurrentHashMap<> ();
        histogramShadowPassengerMutations = new ConcurrentHashMap<> ();
        histogramSurvivorsDriverMutationsCount = new ConcurrentHashMap<> ();
        histogramSurvivorsPassengerMutations = new ConcurrentHashMap<> ();
        histogramShadowDrivePassRatio     = new ConcurrentHashMap<> ();
        histogramPopulationSize     = new ConcurrentHashMap<> ();
        clones                      = new ConcurrentHashMap<> ();
        internalClones              = new HashMap<>();

        // create table for storring the initial count of cells per tasks (subset)
        initialCellSubsetSize = new AtomicIntegerArray(sim.getSimModel().getTechParams().getSimTasksCount()); 

        // create the table where population sizes over generations will be saved
        historyCellCountN = new AtomicIntegerArray(settings.getIntValue(Param.inMaxCycles) + 2);
        
        //create tables for statistical data
        historyDriverMutationCount      = new AtomicIntegerArray(settings.getIntValue(Param.inMaxCycles) + 2);
        historyPassengerMutationCount   = new AtomicIntegerArray(settings.getIntValue(Param.inMaxCycles) + 2);
        historyDriverPassRatio          = new double[settings.getIntValue(Param.inMaxCycles) + 2];           
        
        iteration                   = sim.getIteration();
        
    }

    public void convertClonesToInternalClones(){
        for( Map.Entry<GenomePart, List<Cell>> clone : clones.entrySet() ){
            List<Cell> cloneCells = clone.getValue();
            CellIndexHolder cih = new CellIndexHolder();
            for (int i = 0; i < cloneCells.size(); i++){
                Cell cell = cloneCells.get(i);
                int cellIndex = cellCollection.addAndReturnIndex(cell); 
                cih.add(cellIndex);
            }
            internalClones.put(clone.getKey(), cih);
        }
        clones = null;
        System.gc();
    }
    
    /**
     * add cell to statistics
     * @param cell cell to add to statistic
     */
    public void addCellToHistogramStats(Cell cell){
        if (cell != null){
          addCellToHistogramDriverMutations(cell);
          addCellToHistogramPassengerMutations(cell);
          addCellToHistogramShadowDrivePassRatio(cell);            
        }
    }
    
    /**
     * add cell to histogram for driver mutations in cells
     * @param cell cell to add
     */
    public void addCellToHistogramDriverMutations(Cell cell){
        if (cell.getGenome(genomes) != null){
            DriverMutationHistogramKey key = new DriverMutationHistogramKey(cell, genomes);
            List<Cell> cells = histogramDriverMutations.get(key);
            if (cells == null){
                cells = Collections.synchronizedList(new ArrayList<>());
                cells.add(cell);
                histogramDriverMutations.put(key, cells);
            } else{
                cells.add(cell);
            }                                          
        }
    }    

    /**
     * add cell to histogram for passenger mutations in cells
     * @param cell cell to add
     */
    public void addCellToHistogramPassengerMutations(Cell cell){
        if (cell.getGenome(genomes) != null){        
            PassengerMutationHistogramKey key = new PassengerMutationHistogramKey(cell, genomes);
            List<Cell> cells = histogramPassengerMutations.get(key);
            if (cells == null){
                cells = Collections.synchronizedList(new ArrayList<>());
                cells.add(cell);
                histogramPassengerMutations.put(key, cells);
            } else{
                cells.add(cell);
            }       
        }
    }   
    
    
    /**
     * add cell to histogram for driver / passenger mutations ratio
     * @param cell cell to add
     */
    public void addCellToHistogramShadowDrivePassRatio(Cell cell){
        if (cell.getGenome(genomes) != null){
            Double key = (double)cell.getGenome(genomes).getDriverPassengerRatio();
            if ( key != 0){
                Integer count;
                count = histogramShadowDrivePassRatio.get(key);
                if (count == null){
                    count = 1;
                    histogramShadowDrivePassRatio.put(key, count);
                } else{
                    histogramShadowDrivePassRatio.replace(key, ++count);
                }                               
            }
        }
    }

    /**
     * Adds simulation to histogram data for the population size
     * @param simulation simulation object
     */
    synchronized public void addSimulationToHistogramPopulationSize(Simulation simulation){
        for( int i = 0; i < historyCellCountN.length(); i++){
            int value = historyCellCountN.get(i);
            Integer key = value;
            if ( value > 0){
                Integer count;
                count = histogramPopulationSize.get(key);
                if (count == null){
                    count = 1;
                    histogramPopulationSize.put(key, count);
                } else{
                    histogramPopulationSize.replace(key, ++count);
                }                
            }
        }
    }
    
    /**
     * This calculates histogram data for shadow driver mutations
     */
    public void calculateHistogramShadowDriverMutations(){

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistShadowDriverMutations, Activity.started);        

        for ( Entry<DriverMutationHistogramKey, List<Cell>> entry : histogramDriverMutations.entrySet()){

            int key = entry.getKey().getMutationsCount();
            Integer count;
            count = histogramShadowDriverMutations.get(key);
            if (count == null){
                count = entry.getValue().size();
                histogramShadowDriverMutations.put(key, count);
            } else{
                histogramShadowDriverMutations.replace(key, count + entry.getValue().size());
            }                                          
            
        }

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistShadowDriverMutations, Activity.finished);        
        
    }    

    
    /**
     * This calculates histogram data for shadow driver mutations
     */
    public void calculateHistogramSurvivorsDriverMutations(int lastCycle){

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistSurvivorsDriverMutations, Activity.started);                
        
        for ( Entry<DriverMutationHistogramKey, List<Cell>> entry : histogramDriverMutations.entrySet()){
            if( entry.getKey().getModelCycle() == lastCycle){
                int key = entry.getKey().getMutationsCount();
                Integer count;
                count = histogramSurvivorsDriverMutationsCount.get(key);

                if (count == null){
                    count = entry.getValue().size();
                    histogramSurvivorsDriverMutationsCount.put(key, count);
                } else{
                    histogramSurvivorsDriverMutationsCount.replace(key, count + entry.getValue().size());
                }
            }
            
        }

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistSurvivorsDriverMutations, Activity.finished);                
        
    }        
    
    /**
     * This calculates histogram data for shadow driver mutations
     */
    public void calculateHistogramShadowPassengerMutations(){
 
        StaticConsoleLogger.logActivity(iteration, Activity.calcHistShadowPassengerMutations, Activity.started);                
        
        for ( Entry<PassengerMutationHistogramKey, List<Cell>> entry : histogramPassengerMutations.entrySet()){

            int key = entry.getKey().getMutationsCount();
            Integer count;
            count = histogramShadowPassengerMutations.get(key);
            if (count == null){
                count = entry.getValue().size();
                histogramShadowPassengerMutations.put(key, count);
            } else{
                histogramShadowPassengerMutations.replace(key, count + entry.getValue().size());
            }                                          
            
        }

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistShadowPassengerMutations, Activity.started);                        

    }        
    
    /**
     * This calculates histogram data for shadow driver mutations
     */
    public void calculateHistogramSurvivorsPassengerMutations(int lastCycle){

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistSurvivorsPassengerMutations, Activity.started);                
        
        for ( Entry<PassengerMutationHistogramKey, List<Cell>> entry : histogramPassengerMutations.entrySet()){
            if( entry.getKey().getModelCycle() == lastCycle){
                int key = entry.getKey().getMutationsCount();
                Integer count;
                count = histogramSurvivorsPassengerMutations.get(key);
                if (count == null){
                    count = entry.getValue().size();
                    histogramSurvivorsPassengerMutations.put(key, count);
                } else{
                    histogramSurvivorsPassengerMutations.replace(key, count + entry.getValue().size());
                }
            }
            
        }

        StaticConsoleLogger.logActivity(iteration, Activity.calcHistSurvivorsPassengerMutations, Activity.finished);                
        
    }   
    
   public ConcurrentHashMap<Integer, Integer> getHistogramShadowDriverMutations() {
        return histogramShadowDriverMutations;
    }

    public ConcurrentHashMap<Integer, Integer> getHistogramSurvivorsDriverMutationsCount() {
        return histogramSurvivorsDriverMutationsCount;
    }    
    
    public ConcurrentHashMap<Integer, Integer> getHistogramShadowPassengerMutations() {
        return histogramShadowPassengerMutations;
    }

    public ConcurrentHashMap<Integer, Integer> getHistogramSurvivorsPassengerMutations() {
        return histogramSurvivorsPassengerMutations;
    }        
    
    public ConcurrentHashMap<Double, Integer> getHistogramShadowDrivePassRatio() {
        return histogramShadowDrivePassRatio;
    }

    public ConcurrentHashMap<Integer, Integer> getHistogramPopulationSize() {
        return histogramPopulationSize;
    }    
    
    public double[] getHistoryDriverPassRatio() {
        return historyDriverPassRatio;
    }

    public AtomicIntegerArray getHistoryPassengerMutationCount() {
        return historyPassengerMutationCount;
    }
    
    public AtomicIntegerArray getHistoryDriverMutationCount() {
        return historyDriverMutationCount;
    }

    public AtomicIntegerArray getHistoryCellCountN() {
        return historyCellCountN;
    }

    //corrects the cell count in the current cycle by a delta value
    //which is determined by a single calculation tasks.
    public void correctHistoryCellCount(int cycle,int correction) {
        historyCellCountN.getAndAdd(cycle, correction);
    }

    public int getHistoryCellCountInCycle(int cycle){
        return historyCellCountN.get(cycle);
    }
    
    public int getHistoryDriverMutationCountInCycle( int cycle) {
        return historyDriverMutationCount.get(cycle);
    }
    
    public void setHistoryDriverMutationCountInCycle( int cycle, int count){
        historyDriverMutationCount.set(cycle, count);
    }
    
    public int getHistoryPassengerMutationCountInCycle( int cycle) {
        return historyPassengerMutationCount.get(cycle);
    }
        
    public void setHistoryPassengerMutationCountInCycle( int cycle, int count){
        historyPassengerMutationCount.set(cycle, count);
    }
    
    public synchronized double getHistoryDriverPassRatioInCycle( int cycle) {
        return historyDriverPassRatio[cycle];
    }
    public void calcHistoryDriverPassRationInCycle( int cycle ){

        int drivers     = historyDriverMutationCount.get(cycle);
        int passengers  = historyPassengerMutationCount.get(cycle);
        if ( drivers != 0 && passengers != 0 ){
            historyDriverPassRatio[cycle]       = (double)drivers / (double)passengers;
        } else {
            historyDriverPassRatio[cycle]       = 0;            
        }
        
    }
    
    public void copyHistoryCellCountNFromPreviousCycle(int cycle){
        if( cycle != 0 ){
            int count = historyCellCountN.get(cycle - 1);
            historyCellCountN.set(cycle, count);
        }           
    }

    public void setInitialCellSubsetSizeInCycle(int cycle, int size){
        initialCellSubsetSize.set(cycle, size);
    }

    public int getInitialCellSubsetSizeInCycle(int cycle){
        return initialCellSubsetSize.get(cycle);
    }
    
    public void setHistoryCellCountNInCycle(int cycle, int count){
        historyCellCountN.set(cycle, count);
    }
    
    public void addCountToHistoryCellCountNInCycle(int cycle, int add){
        int count = historyCellCountN.get(cycle);
        count += add;
        historyCellCountN.set(cycle, count);
    }

    public void addCellToClonesCollection(Cell cell){

        List<Cell> cloneList = clones.get(cell.getGenome(genomes).getDrivers(genomes));

        if (cloneList == null){ 
            List<Cell> newCloneList = Collections.synchronizedList(new ArrayList<> ());
            newCloneList.add(cell);
            clones.put(cell.getGenome(genomes).getDrivers(genomes), newCloneList);
        } else {
            cloneList.add(cell);
        }
    }

    public void removeShadowPredecessorsFromInternalClones(){
        StaticConsoleLogger.logActivity(iteration, Activity.removeShadowPredecessorsInternal, Activity.started);        
        for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
            CellCollectionWithHolderData cellsWithHolderData = new CellCollectionWithHolderData(cellCollection, clone.getValue());
            ArrayList<Cell> cloneCells = new ArrayList<>(cellsWithHolderData.getCells());
            cloneCells = removeShadowPredecessors(cloneCells);           
            clone.setValue(cellsWithHolderData.getCellIndexHolder(cloneCells));
        }
        StaticConsoleLogger.logActivity(iteration, Activity.removeShadowPredecessorsInternal, Activity.finished);        
    }   
    
    /**
     * Returns only the "Freshiest" copies of cells from the ArrayList
     * If the same cell id appears several times in the input list, only the cell
     * having the greatest cycle will be left in the outList.
     * @param inList Cells
     * @return Cells
     */
    private static ArrayList<Cell> removeShadowPredecessors(ArrayList<Cell> inList){

        ArrayList<Cell> outList = new ArrayList<>();

        HashMap<Integer,Integer> maxCyclesPerCellID = new HashMap<>();
        for( Cell c : inList){
            Integer currMaxCycleForCell = maxCyclesPerCellID.get(c.getId());
            if( currMaxCycleForCell == null){
                maxCyclesPerCellID.put(c.getId(), c.getModelCycle());
            }
            else{
                maxCyclesPerCellID.replace(c.getId(), Math.max(c.getModelCycle(), currMaxCycleForCell));
            } 
        }
        for( Cell c : inList){
            if(maxCyclesPerCellID.get(c.getId()).equals(c.getModelCycle())){
                outList.add(c);
            }
        }
        
        return outList;        

    }
    
}



