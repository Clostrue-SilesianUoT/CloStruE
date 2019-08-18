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
package clostrue.loadballancer;
 
import clostrue.CalcTask;
import clostrue.biology.cell.Cell;
import clostrue.Simulation;
import clostrue.toolbox.StaticConsoleLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the common superclass for load ballancers.
 * 
 * It gives several tools for load ballancing, such as:
 * finding the taks with the smallest cell population,
 * finding tasks with the longest runtime
 * shifting cells between cell populations among parallel tasks
 * shifting cells between cell populations and shared buffer (bidirectional)
 *
 * @author Krzysztof Szymiczek  
 */
public class LoadBallancerEmpty implements LoadBallancer{
 
    protected final List<CalcTask> calcTasks;
    protected final Simulation simulation;
    protected final String loadBallancerAlghoritmName;
    protected boolean logToConsole;
    
    public LoadBallancerEmpty(Simulation inSimulation, String alghoritmDescription, boolean inLogToConsole) {
        
        this.logToConsole = inLogToConsole;
        this.calcTasks = inSimulation.getCalcTasks();
        this.simulation = inSimulation;
        if (alghoritmDescription.equals("")){
            this.loadBallancerAlghoritmName = "No Load Ballancing";
        } else {
            this.loadBallancerAlghoritmName = alghoritmDescription;
        }
    }

    @Override
    public String getLoadBallancerAlgorithmName() {
        return loadBallancerAlghoritmName;
    }
    
// do the load ballancing
    @Override
    public void ballanceWork(){

        long beginTime = System.nanoTime();
        
        long duration = System.nanoTime() - beginTime;
        simulation.addLoadBallancingTime(duration);
        
    }

//returns the calculation task with the biggest population.
//this method is used in Load Balancing
    protected CalcTask getTaskWithBiggestPopulation() {
        CalcTask taskWithBiggestPopulation = null;
        int biggestPopulation = 0;
        for (CalcTask task : calcTasks) {
            if (task.getCellPopulationSize() >= biggestPopulation) {
                biggestPopulation = task.getCellPopulationSize();
                taskWithBiggestPopulation = task;
            }
        }
        return taskWithBiggestPopulation;
    }

//returns the calculation task with the longest run time.
//this method is used in Load Balancing
    protected CalcTask getTaskWithLongestRunTime() {
        CalcTask taskWithLongestRunTime = null;
        long longestRuntime = 0;
        for (CalcTask task : calcTasks) {
            if (task.getBenchmarkEntry().getDuration() >= longestRuntime) {
                longestRuntime = task.getBenchmarkEntry().getDuration();
                taskWithLongestRunTime = task;
            }
        }
        return taskWithLongestRunTime;
    }    
    
//transports half of cell population between two calculation tasks.
//this method is used in Load Balancing
    protected void transportHalfOfCellPopulationBetweenTasks(
            CalcTask source, CalcTask destination) {

        int cellsToTransport = source.getCellPopulationSize() / 2;
        for (int i = 0; i < cellsToTransport; i++) {
            Cell transferredCell = source.getCellFromPopulation();
            destination.addCellToPopulation(transferredCell);
            source.removeCellFromPopulation(transferredCell);
        }
    }        

//shifts the given amount of cells from the calculation task population
//to external buffer

    protected ArrayList<Cell> pullNCellsFromCalcTask(CalcTask source, long cellsToPull){
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = 0; i < cellsToPull; i++) {
            Cell transferredCell = source.getCellFromPopulation();
            if (transferredCell != null){
                cells.add(transferredCell);
                source.removeCellFromPopulation(transferredCell);                
            }

        }
        return cells;
    }
    
    protected void pushNCellsToCalcTask(CalcTask destination, long cellsToPush, ArrayList<Cell> buffer){
        int obtainedCells = 0;
        for (int i = 0; i < cellsToPush; i++){
            if (buffer.size() > 0){
                destination.addCellToPopulation(buffer.get(0));
                buffer.remove(0);
                obtainedCells++;
            }
        }
        
        if (logToConsole){
            StaticConsoleLogger.log("Task: " + destination.getId() + " obtainded cells: " + obtainedCells + " (planned: " + cellsToPush + ")");            
        }

    }

    protected double calculateMeasureRelativeValueSumForDestinationTasks(ArrayList<TaskMeasure> measures){
        
        double measureRelativeValueSum = 0;
        
        for (TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
            measureRelativeValueSum += measure.getMeasureValue();                
            }
        }        
        
        return measureRelativeValueSum;
        
    }

    protected void calculateDestinationBallancingFactorsByRelation(ArrayList<TaskMeasure> measures, double measureValueSum){

        for (TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
            measure.calculateBallancingFactorByRelation(measureValueSum);
            }
        }        
        
    }

    protected void calculateDestinationBallancingFactorsBySubtraction(ArrayList<TaskMeasure> measures){

        for (TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
            measure.calculateBallancingFactorBySubtraction();
            }
        }        
        
    }
    
    protected void normalizeDestinationBallancingFactors(ArrayList<TaskMeasure> measures){

        double sum = 0.0;
        
        for (TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
            sum+=measure.getBallancingFactor();
            }
        }        
        
        for (TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
                double newBallancingFactor = measure.getBallancingFactor() / sum;
                measure.setBallancingFactor(newBallancingFactor);
            }
        }        
        
    }    
    
    protected double calculateMeanMeasureValue(ArrayList<TaskMeasure> measures){

        double measureValueSum = 0;
        
        for (TaskMeasure measure : measures){
            measureValueSum += measure.getMeasureValue();
        }

        return measureValueSum / measures.size();
    }

    protected void calculateRelationalMeasureValues(double asRatioTo, ArrayList<TaskMeasure> measures){
        
        for (TaskMeasure measure : measures){
            measure.calculateRelationalMeasureValue(asRatioTo);
        }
     
    }
    
    protected ArrayList<Cell> shiftCellsFromTasksToBuffer(ArrayList<TaskMeasure> measures){
        
        ArrayList<Cell> cellsBuffer = new ArrayList<>();
        ArrayList<Cell> removedCells;

        for (TaskMeasure measure : measures){
            if( measure.getRelationalMeasureValue() > 1){
                removedCells = removeCellsProportionalFromTask(measure);
                cellsBuffer.addAll(removedCells);
            }
        }
        
        return cellsBuffer;
        
    }
    
    protected ArrayList<Cell> removeCellsProportionalFromTask(TaskMeasure measure){
        
        ArrayList<Cell> removedCells;
    
        Double cells = measure.getCellPopulationSize();    
        long cellsToRemove = Math.round(new Double(cells - ( cells / measure.getRelationalMeasureValue())));
        
        if ((cellsToRemove > 0) && (cellsToRemove == measure.getCalcTask().getCellPopulationSize())){
            cellsToRemove--; //leave at least one cell
        }
        
        removedCells = pullNCellsFromCalcTask(measure.getCalcTask(), cellsToRemove);
    
        if (logToConsole){
            StaticConsoleLogger.log("Task: " + measure.getCalcTask().getId() + " removed cells: " + removedCells.size());            
        }

        return removedCells;        
            
    }

    protected void shiftCellsFromBufferToTasks(ArrayList<TaskMeasure> measures, ArrayList<Cell> buffer, int initialBufferSize){

        TaskMeasure lastProcessedMeasure = null;
        
        for( TaskMeasure measure : measures){
            if (measure.getRelationalMeasureValue() < 1){
                long countOfCellsToShift = Math.round(measure.getBallancingFactor() * (double)initialBufferSize);
                pushNCellsToCalcTask(measure.getCalcTask(), countOfCellsToShift, buffer);
                lastProcessedMeasure = measure;
            }
        }         
        
        if (!buffer.isEmpty()){
            if (lastProcessedMeasure != null){
                pushNCellsToCalcTask(lastProcessedMeasure.getCalcTask(), buffer.size(), buffer);                
            }
        }
        
    }
    
    protected void printCellCount(ArrayList<TaskMeasure> measures, String title, Double cellCount){

        StaticConsoleLogger.log("---" + title + "---");
        StaticConsoleLogger.log("Cells total: " + cellCount);
        StaticConsoleLogger.log("MeanCells per task: " + (cellCount / measures.size()));
        for( TaskMeasure measure : measures){
            StaticConsoleLogger.log("Task: " + measure.getCalcTask().getId() + " Cells: " + measure.getCalcTask().getCellPopulationSize());
        }             
        
    }
    
    protected void printRelationalMeasureValues(ArrayList<TaskMeasure> measures)
    {
        StaticConsoleLogger.log("Relative measures: ");
        for( TaskMeasure measure : measures){
            StaticConsoleLogger.log("Task: " + measure.getCalcTask().getId() + " Relational Measure: " + measure.getRelationalMeasureValue());
        }                     
    }    

    protected void printBallancingFactors(ArrayList<TaskMeasure> measures, String title)
    {
        StaticConsoleLogger.log("Ballancing Factors " + title + ": ");
        for( TaskMeasure measure : measures){
            StaticConsoleLogger.log("Task: " + measure.getCalcTask().getId() + " Ballancing Factor: " + measure.getBallancingFactor());
        }                     
    }    
    
    
    protected double getCellPopulationSizeSumFromAllTasks(Simulation simulation){

        double sum = 0;
        
        for (CalcTask calcTask : simulation.getCalcTasks()) {
            sum += calcTask.getCellPopulationSize();            
        }        
        
        return sum;
    }
    
    protected ArrayList<TaskMeasure> prepareMeasuresBasedOnDutyCycle(boolean withGain){

        ArrayList<TaskMeasure> measures = new ArrayList<>();
        CalcTask taskWithLongestRuntime = getTaskWithLongestRunTime();
        Long longestRuntime = taskWithLongestRuntime.getBenchmarkEntry().getDuration();
        
        for (CalcTask calcTask : calcTasks) {
 
            double gain;
            if (withGain == true){
                gain = 1.0 + 0.1 * calcTask.getBenchmarkEntry().getCellCountGain();
            } else {
                gain = 1.0;
            }
            calcTask.getBenchmarkEntry().calculateDutyCycle(longestRuntime);
            TaskMeasure measure = new TaskMeasure(calcTask.getBenchmarkEntry().getDutyCycle() * gain,calcTask);
            measures.add(measure);
            
        }        
        
        return measures;
    }    
    
    protected ArrayList<TaskMeasure> prepareMeasuresBasedOnCellCount(boolean withGain){

        ArrayList<TaskMeasure> measures = new ArrayList<>();

        Double cellPopulationSize = getCellPopulationSizeSumFromAllTasks(simulation);
       
        for (CalcTask calcTask : calcTasks) {

            double gain;
            if (withGain == true){
                gain = 1.0 + 0.1 * calcTask.getBenchmarkEntry().getCellCountGain();
            } else {
                gain = 1.0;
            }            
            calcTask.getBenchmarkEntry().calculateFillRatio(cellPopulationSize);
            TaskMeasure measure = new TaskMeasure(calcTask.getBenchmarkEntry().getFillRatio() * gain,calcTask);
            measures.add(measure);
            
        }        
        
        return measures;
    }    
    
}
