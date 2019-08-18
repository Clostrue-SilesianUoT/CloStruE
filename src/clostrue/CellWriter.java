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

import clostrue.model.mam.RegionTossMap;
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.Genome;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.hardcodes.Activity;
import java.io.FileWriter;
import java.io.IOException;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.HeaderPart;
import clostrue.model.mam.MutationAdvantageData;
import clostrue.postprocessing.analysis.Statistics;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a tool which enables writing cell data to csv files of the predefined format.
 * and also to memory collection to be imported later by the graph generator
 * 
 * @author Krzysztof Szymiczek
 */
public class CellWriter {

    private final int maxRowsPerFile;
    private final boolean prepareCells;
    private final GenomeSynchronizedCollection genomes;
    private int lastFileEnd;
    private int savedCellsInFile;
    private final String simulationFileName;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private final List<Cell> memCells;
    private final RegionTossMap regionMapDrivers;
    private final RegionTossMap regionMapPassengers;
    private final MutationAdvantageData[] mamDrivers;
    private final MutationAdvantageData[] mamPassengers;
    private final int taskID;
    private final double              _noMutationProb;    //Division with no mutation probability in next cycle
    private final double              _drivMutationProb;  //Division with Driver Mutation probability in next cycle
    private final double              _passMutationProb;  //Division with Passenger Mutation probability in next cycle
    
    /**
     * Creates cell writer instance for one calculation task.
     * The task ID is passed to form a part of the file name
     * @param inCalcTask
     */
    public CellWriter(CalcTask inCalcTask) {
       
        lastFileEnd             = 0;
        taskID                  = inCalcTask.getId();
        memCells                = Collections.synchronizedList(new ArrayList<> ());
        prepareCells            = inCalcTask.getSim().isCbPrepareCells();
        genomes                 = inCalcTask.getSim().getGenomes();
        regionMapDrivers        = inCalcTask.getModel().getModParams().getMAM().getRegionMapDrivers();
        regionMapPassengers     = inCalcTask.getModel().getModParams().getMAM().getRegionMapPassengers();
        mamDrivers              = inCalcTask.getModel().getModParams().getMAM().getDrivers();
        mamPassengers           = inCalcTask.getModel().getModParams().getMAM().getPassengers();
        simulationFileName      = inCalcTask.getModel().getFilePaths().getWorkDirCellFiles() + java.io.File.separator + "task_" + String.valueOf(taskID);
        maxRowsPerFile          = inCalcTask.getModel().getTechParams().getMaxRowsPerFile();                   

        _drivMutationProb       = inCalcTask.getSim().getDriverMutationProbability();
        _passMutationProb       = inCalcTask.getSim().getPassengerMutationProbability();
        _noMutationProb         = inCalcTask.getSim().getNoMutationProbability();
        
    }

    /**
     * Writes header line to new cell file
     * @throws IOException 
     */
    private void writeHeader() throws IOException{
       
        bufferedWriter.write(HeaderPart.cellFileHeadModelCycle               + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadCellCount                + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadCellID                   + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadParentCellID             + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadCellAge                  + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDrivers                  + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadPassengers               + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadBirth                    + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDeath                    + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDeathProb                + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDivisionProb             + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadNoMutationProb           + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDriverMutationProb       + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadPassengerMutationProb    + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadCloneGroup               + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadDriverMutations          + Artifact.csvColumnSeparator);
        bufferedWriter.write(HeaderPart.cellFileHeadPassengerMutations       + Artifact.outCSVeol);        
    }
    
    /**
     * Writes sinle cell data to cell file
     * @param cell cell from which data should be written into file
     * @param modelCycle current model cycle
     * @param cellCount current cell count (at the begin of model cycle)
     * @throws IOException 
     */
    @SuppressWarnings("UnnecessaryBoxing")
    private void writeCellData(Cell cell, int modelCycle, int cellCount) throws IOException{
        String mutationsString;     
        bufferedWriter.write(String.valueOf(modelCycle));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cellCount));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getId()));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getParentCellID()));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getAge()));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        Genome genome = cell.getGenome(genomes);
        int driverMutationCount = genome.getDriverMutationCount();
        int passengerMutationCount = genome.getPassengerMutationCount();
        bufferedWriter.write(String.valueOf(driverMutationCount));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(passengerMutationCount));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        if (cell.isAlive()
                && cell.getAge() == 0) {
            bufferedWriter.write(Artifact.outCSVex);
        } else {
            bufferedWriter.write(Artifact.outCSVnoEx);
        }
        bufferedWriter.write(Artifact.csvColumnSeparator);
        if (cell.isDead()) {
            bufferedWriter.write(Artifact.outCSVex);
        } else {
            bufferedWriter.write(Artifact.outCSVnoEx); 
        }
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getDeathProb()));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getDivisionProb()));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(_noMutationProb));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(_drivMutationProb));
        bufferedWriter.write(Artifact.csvColumnSeparator);                
        bufferedWriter.write(String.valueOf(_passMutationProb));
        bufferedWriter.write(Artifact.csvColumnSeparator);
        bufferedWriter.write(String.valueOf(cell.getGenome(genomes).getDriverCloneGroupID()));
        bufferedWriter.write(Artifact.csvColumnSeparator);

        int toss = cell.getGenome(genomes).getDrivers(genomes).getMutation();
        if (toss != Integer.MIN_VALUE){
            int mutation = regionMapDrivers.getGeneBasedOnToss(toss);
            mutationsString = mamDrivers[mutation].getGeneName()
                    + Artifact.geneSeparator + String.valueOf(toss)
                    + Artifact.outCSVMutationSeparator;
            bufferedWriter.write(mutationsString);
        }
        bufferedWriter.write(Artifact.csvColumnSeparator);

        toss = cell.getGenome(genomes).getPassengers(genomes).getMutation();
        if (toss != Integer.MIN_VALUE){
            int mutation = regionMapPassengers.getGeneBasedOnToss(toss);
            mutationsString = mamPassengers[mutation].getGeneName()
                    + Artifact.geneSeparator + String.valueOf(toss)
                    + Artifact.outCSVMutationSeparator;
            bufferedWriter.write(mutationsString);
        }
        bufferedWriter.write(Artifact.csvColumnSeparator);

        bufferedWriter.write(Artifact.outCSVeol);
        savedCellsInFile++;        
    }
    
    /**
     * Opens a new cell file (either first time or if the actual cell file
     * has reached it's maximum lines limit
     * @param modelCycle model cycle
     * @throws IOException 
     */
    private void openNewFile(int modelCycle) throws IOException{
        lastFileEnd += 1;
        String fullFileName
                = simulationFileName
                + "_"
                + String.valueOf(lastFileEnd)
                + "_("
                + String.valueOf(modelCycle)
                + ").csv";
        fileWriter = new FileWriter(fullFileName);
        bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        savedCellsInFile = 0;        
    }
    
    /**
     * Wrapper for Writes single cell data to csv file.
     * if first cell is written, or the predefined cell count is written, a new file
     * is created. A new file contains a new header.
     * 
     * @param cell          cell for which the data is saved
     * @param modelCycle    curent cycle of modeling (to save in csv)
     * @param cellCount     curent cell count (to save in csv)
     */
    public void writeToCSV(Cell cell, int modelCycle, int cellCount){
        try {
                        
            //IF this is a first cell also the header has to be written;
            if ((modelCycle == 0 && savedCellsInFile == 0) || (savedCellsInFile == maxRowsPerFile)) {
                try {
                    if (lastFileEnd > 0) {
                        if (prepareCells){
                            closeFile(false, modelCycle);
                        }
                    }
                    openNewFile(modelCycle);
                    writeHeader();
                } catch (IOException ex) {
                    Logger.getLogger(CellWriter.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
            writeCellData(cell, modelCycle, cellCount);
        } catch (IOException ex) {
            Logger.getLogger(CellWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes the file writer.
     * 
     * This method closes the access to a file under which the cell writer
     * has stored the cell data. File stream is flushed and file acces
     * is closed in the operation file system.
     * 
     * @param log should this be logged to console
     * @param cycle curent simulation cycle (for logging information)
     */
        public void closeFile(boolean log, int cycle){
        if (log)
            StaticConsoleLogger.consoleLogActivity(cycle, taskID, Activity.closingCellWriters, Activity.started);
        try {
            bufferedWriter.flush();
            bufferedWriter.close();
            lastFileEnd = 0;
        } catch (IOException ex) {
            Logger.getLogger(CellWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (log)
            StaticConsoleLogger.consoleLogActivity(cycle, taskID, Activity.closingCellWriters, Activity.finished);            
    }

    /**
     * Writes the cell data to memory to be processed by various analytic components
     * @param cell
     * @param modelCycle 
     */
    public void writeToMemory(Cell cell, int modelCycle){
        Cell cellToWrite = new Cell(cell);
        cellToWrite.setModelCycle(modelCycle);
        memCells.add(cellToWrite);
    }

    /**
     * Transfer all the cells to clones Collection for statistical analysis
     * @param calcTask
     * @param cycle 
     */
    public void transferCellsIntoStatistics(CalcTask calcTask, int cycle){
        StaticConsoleLogger.consoleLogActivity(cycle, taskID, Activity.moveCellsToStatistics, Activity.started);
        Statistics stats = calcTask.getSim().getStatistics();
        for (Cell cell : memCells) {
            if (cell.isAlive()){
                stats.addCellToClonesCollection(cell);
                stats.addCellToHistogramStats(cell);                
            }
        }
        
        StaticConsoleLogger.consoleLogActivity(cycle, taskID, Activity.moveCellsToStatistics, Activity.finished);
    }
    
}
