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
package clostrue.postprocessing.visualization;
 
import clostrue.biology.cell.Cell;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.biology.genome.GenomePart;
import clostrue.toolbox.MathTools;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.FishPlot;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.Name;
import clostrue.sequencers.AccSeq4HMwithGenomePartKey;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class provides the creation of script for R environment
 * Which is used to plot the clonal structure using fishplot package
 * 
 * @author Krzysztof Szymiczek
 */

public class Fishplot {

    private ArrayList<Integer> cycles;                                          //cycles
    private ArrayList<Integer> timePoints;                                      //time points
    private ArrayList<Integer> verticalLines = null;                            //vertical lines locations
    private ArrayList<String>  verticalLinesLabels = null;                      //vertical lines labels
    private ArrayList<int[]> fractionsInCycles;                                 //population fractions
    private HashMap<Integer,
            HashMap<GenomePart, List<Cell>>> equalGenomePart;    //input data from analytics
    HashMap<Integer, ArrayList<Integer>> cloneInclusions;             //clone inclusions
    private int cloneGroupParents[];                                            //group id of parent clones
    private int maxCycle;                                                       //max simulation cycle
    private int maxCloneGroupIndex;                                             //max clone group (fraction) index
    private int maxPopulationSize;                                              //max clone group (fraction) size
    private String script;                                                      //R script to write to file
    private final Analytics analytics;                                          //input analytics data
    private final int decimals;                                                 //the number of decimals in accuracy
    private final int multiplier;                                               //the multiplier for cell-to-fracion calc
    private final int divider;                                                  //the divider for cell-to-fracion calc
    private final String decFormatString;                                       //format string for decimals    
    private ArrayList<int[]> stackedCellsInCycles;                              //stacked cell fracions in cycles
    private int inclusionKeys[];                                                //keys of the inclusion list
    int maxStackedCellsPerSimulation;                                           //max stacked cell count per simulation
    private final int iteration;
    private final GenomeSynchronizedCollection genomes;
    
    /*
    Constructor. Requires analytics object (can be empty inside)
    At the time point of creation of fishplot
    */
    public Fishplot(Analytics inAnalytics, int inDecimals) {
        analytics = inAnalytics;
        iteration = analytics.getSim().getIteration();
        genomes = analytics.getSim().getGenomes();
        decimals = inDecimals;
        multiplier = MathTools.intPower(10,2+decimals);
        divider = MathTools.intPower(10,decimals);
        decFormatString = getDecimalFormatString();
    }
    
    /*
    Checks that there are non-zero fractions of a given clone
    In previous cycles of simulation
    */
    private boolean isNonZeroBefore(int cycleIndexStartToLookBack, int fractionIndex){
        
        boolean nonZeroBefore = false;
            for ( int cycleIndex = cycleIndexStartToLookBack; cycleIndex >= 0; cycleIndex--){
                if (fractionsInCycles.get(cycleIndex)[fractionIndex] > 0){
                    nonZeroBefore = true;
                    break;
                }                
            }               
        return nonZeroBefore;
        
    }
    /*
    due to rounding, some clones can become 0 in a fraction sequence 
    like: 0001234544433210012345566...
    We have to "glue up" the zeros in between non-zero fractions
    so the clone is a continuous entity from the cycle where it is born
    to the cycle where it dies
    */
    private void fixMissingClonesDueToRoundings(){
        if (fractionsInCycles.isEmpty())
            return;
        int totalCycles = fractionsInCycles.size();
        int fractionsSize = fractionsInCycles.get(0).length;
        
        for (int fractionIndex = 0; fractionIndex < fractionsSize; fractionIndex++){
            int laterOnFractionSize = 0;
            for ( int cycle = totalCycles; cycle > 0; cycle--){
                int cycleIndex = cycle - 1;
                int fractionInCycle = fractionsInCycles.get(cycleIndex)[fractionIndex];
                if (fractionInCycle > 0){
                    laterOnFractionSize = fractionInCycle;
                } else {
                    if (laterOnFractionSize > 0){
                        if (isNonZeroBefore(cycleIndex, fractionIndex)){
                            fractionsInCycles.get(cycleIndex)[fractionIndex] = laterOnFractionSize;                            
                        }
                    }
                }
            }
        }
        
    }
    
    /*
    Prepares the fishplot based on analytics from simulation
    */
    public void prepareFishplot(){

        StaticConsoleLogger.logActivity(iteration, Activity.prepareFishplot, Activity.started);
        equalGenomePart = analytics.prepEqualShadowDriverPart();
        calculateStatistics();
        calculateCloneGroupParents();
        calculateCloneInclusions();
        calcFractionsInCycles();
        createScript();        
        StaticConsoleLogger.logActivity(iteration, Activity.prepareFishplot, Activity.finished);

    }
    
    /*
    Saves the fishplot to file
    */
    public void saveToFile(){

        try {
            String fileName = analytics.getSim().getSimModel().getFilePaths().getWorkDirGraphicDataSource()
                    + java.io.File.separator
                    + Name.snFishplotScriptForREnvironment + Extension.dotR;
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
            StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.started); 
            bufferedWriter.write(script);
            bufferedWriter.flush();
            bufferedWriter.close();
            StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.finished); 
            
        } catch (IOException ex) {
            Logger.getLogger(Fishplot.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    
    /*
    the ancestor clones have to sum to to the predecessor clone fraction
    in any given simulation cycle. This is to correct parents fraction
    if the sum of children fractions is greater than parent fraction
    If the sum will be greater than 100 percent (due to roundings)
    Data for this given cycle will be skipped while crating script
    There can be no children correction to lower the childred fraction
    Since this could cause missing clones discontinuities
    */
    private void correctFractionsInCycles(){
        for (int cycleIndex = 0; cycleIndex < stackedCellsInCycles.size(); cycleIndex++){
            int[] cloneFractions        = fractionsInCycles.get(cycleIndex);
            int[] correctedFractions    = correctFractions(cloneFractions, inclusionKeys);           
            fractionsInCycles.set(cycleIndex, correctedFractions);           
        }        
    }

    /*
    Calculates fractions of each clone population
    in each cycle of the simulation
    The result value is uncorrected
    */    
    private void calcFractionsInCyclesUncorrected(){

        fractionsInCycles = new ArrayList<>();        
        for (int cycle = 0; cycle < stackedCellsInCycles.size(); cycle++){
            int[] cloneFractions = getCloneFractionsForCycle(stackedCellsInCycles, cycle, maxStackedCellsPerSimulation);
            cloneFractions = correctFractions(cloneFractions, inclusionKeys);
            fractionsInCycles.add(cloneFractions);
        }
       
    }
    
    /*
    Calculates fractions of each clone population
    in each cycle of the simulation
    And corrects the values for roundings and discontinuities
    */
    private void calcFractionsInCycles(){

        calcStackedCellsInCycles();     
        calcMaxStackedCellsPerSimulation();
        calcInclusionKeys();
        calcFractionsInCyclesUncorrected();         
        fixMissingClonesDueToRoundings();
        correctFractionsInCycles();      
        
    }
    
    /*
    Calculates some statistics required for further operations
    While calculating fractions and preparing data for R script
    */
    private void calculateStatistics(){
        
        maxCloneGroupIndex       = 0;
        maxPopulationSize   = 0;
       
        int populationSizeInCycle;
        
        cycles = new ArrayList<> ();        
        for (Entry entry: equalGenomePart.entrySet()){
            populationSizeInCycle = 0;
            cycles.add((Integer) entry.getKey());   
            for (Entry cycleData : equalGenomePart.get((Integer)entry.getKey()).entrySet()){
                GenomePart gpr = (GenomePart)cycleData.getKey();
                List<Cell> cells = (List<Cell>)cycleData.getValue();
                maxCloneGroupIndex = Math.max(maxCloneGroupIndex, cells.get(0).getGenome(genomes).getDriverCloneGroupID());
                populationSizeInCycle += cells.size();
            }
            maxPopulationSize = Math.max(maxPopulationSize,populationSizeInCycle);
        }
        
        if (!cycles.isEmpty()){
            Collections.sort(cycles);        
            maxCycle = cycles.get(cycles.size()-1);            
        }
        
    }

    /*
    Creates the R script content for drawing fishplot
    */    
    private void createScript(){
       
        String generatedBy      = getScriptGeneratedByString();
        String library          = getScriptLibraryString();
        String colorsVector     = getColorsVector();
        String parentSequence   = getScriptParentsSequenceString();
        String fractions        = getScriptFractionsString();
        String fishplotCall     = getScriptFishplotCallString();
        String timePointsStr    = getScriptTimePointsString();
        
        script = generatedBy
               + library
               + timePointsStr
               + parentSequence
               + colorsVector
               + fractions
               + fishplotCall;
               
    }

    /*
    Formats the clone fractions data to a string
    which will be used as a part of the generated R script
    */
    private String getScriptFractionsString(){

//    #provide a matrix with the fraction of each population
//    #present at each timepoint
//    frac.table = matrix(
//      c(100, 45, 00, 00,
//         02, 00, 00, 00,
//         02, 00, 02, 01,
//         98, 00, 95, 40),
//      ncol=length(timepoints))        

        String fractions = Artifact.outCSVeol
                
            + "#provide a matrix with the fraction of each population"
            + Artifact.outCSVeol
            + "#present at each timepoint"
            + Artifact.outCSVeol
            + "frac.table = matrix("
            + Artifact.outCSVeol
            + "c("
            + Artifact.outCSVeol
            + getFractionsSequence()
            + "),"
            + Artifact.outCSVeol
            + "ncol=length(timepoints))"        
            + Artifact.outCSVeol;

        return fractions;
    }

    /*
    Convers colors to String RGB representation
    */
    private String convColorToFormatForR(Color color){
        
        String colStr 
            = "rgb("        
            + new DecimalFormat("000").format(color.getRed())   + ","
            + new DecimalFormat("000").format(color.getGreen()) + ","
            + new DecimalFormat("000").format(color.getBlue())  + ","
            + "maxColorValue=255)";
        
        return colStr;
    }
    
    /*
    Prepares the string containing colors for subsequent fractions
    which will be used as a part of the generated R script
    */
    private String getScriptColorsSequenceString(){
       
        int colorIndex;
        String colorsSeq = "";
        for (int i = 0; i <maxCloneGroupIndex; i++){
            colorIndex = i % (GraphGenerator.colorsTable.size());
            colorsSeq   = colorsSeq.concat(convColorToFormatForR(GraphGenerator.colorsTable.get(colorIndex)) 
                        + ",# Color for clone group: " 
                        + new DecimalFormat("00000").format(i+1)
                        + Artifact.outCSVeol);
        }
        if (maxCloneGroupIndex > 0){
            colorIndex = maxCloneGroupIndex % (GraphGenerator.colorsTable.size());            
            colorsSeq   = colorsSeq.concat(
                        convColorToFormatForR(GraphGenerator.colorsTable.get(colorIndex)) 
                        + " # Color for clone group: " 
                        + new DecimalFormat("00000").format(maxCloneGroupIndex+1));
        }
                
        return colorsSeq;        
    }
    

    /*
    Formats the fractions color data to a string
    which will be used as a part of the generated R script
    */    
    private String getColorsVector(){ 

        String colors = Artifact.outCSVeol
            + "#provide a vector containing colors for each clone"
            + Artifact.outCSVeol
            + "color.vector = c("
            + Artifact.outCSVeol
            + getScriptColorsSequenceString()
            + Artifact.outCSVeol
            + ")"
            + Artifact.outCSVeol;

        return colors;
    }
    
    /*
    The width of the parents have to be at least sum of children clone fractions
    in each simulation cycle.
    This method stacks the fraction so this requirement is meet
    */
    private void calcStackedCellsInCycles(){
        
        stackedCellsInCycles = new ArrayList<> ();
        for( Integer cycle : cycles ){
            int cellsPerClone[] = calcCellsPerCloneForCycle(cycle);
            int stackedCellsPerCloneInCycle[] = stackCellsPerClone(cellsPerClone);
            stackedCellsInCycles.add(stackedCellsPerCloneInCycle);
        }
        
    }
    
    /*
    Calculates the maximum stacked cells count
    So fractions of each cell population can be determined by dividing
    the size of the fraction to the maximum stacked cells count in the simulation
    */
    private void calcMaxStackedCellsPerSimulation(){

        maxStackedCellsPerSimulation = 0;
        for (int stackedCellsInCycle[] : stackedCellsInCycles){
            int maxPerCycle = 0;
            for (int i = 0; i < stackedCellsInCycle.length; i++){
                maxPerCycle = Math.max(maxPerCycle, stackedCellsInCycle[i]);
            }
            maxStackedCellsPerSimulation = Math.max(maxStackedCellsPerSimulation, maxPerCycle);                
        }
                
    }
    
    /*
    Prepares the inclusion keys table which contains information
    Which child clones are originating from which parent clone
    The nesting goes one level in deep
    */
    private void calcInclusionKeys(){

        inclusionKeys = new int[cloneInclusions.size()];
        int index = 0;
        for (Integer key : cloneInclusions.keySet()) {
            inclusionKeys[index++] = key;            
        }
        Arrays.sort(inclusionKeys);
        
    }
    
    /*
    Calculates clone fractions for a given simulation cycle (RAW)
    */
    private int[] getCloneFractionsForCycle(ArrayList<int[]> stackedCellsPerCycle, int cycle, int maxStackedCellsPerSimulation){

        int stackedCellsInCycle[] = stackedCellsPerCycle.get(cycle);
        int[] cloneFractions = new int[stackedCellsInCycle.length];
        for (int i = 0; i < stackedCellsInCycle.length; i++){
            cloneFractions[i] = (int) Math.round((double)multiplier * (double)stackedCellsInCycle[i] / (double)maxStackedCellsPerSimulation);
        }        
        return cloneFractions;
        
    }    

    /*
    Creates decimal format for fractions so the script looks nice and clean
    */
    private String getDecimalFormatString(){
        String fStr = "000";
        if (decimals > 0){
            fStr = fStr.concat(".");
            for (int i = 1; i <= decimals; i++){
                fStr = fStr.concat("0");
            }
        }
        return fStr;
    }
    
    /*
    Formats the clone fractions data to a string
    which will be used as a part of the generated R script
    */
    private String getFractionsSequence(){
       
        timePoints = new ArrayList<>();
        timePoints.add(1);
        String fractionsSeq = "";
        
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');     
        
        String fractionsSeqDummyNext = ", " + decFormatString;
        
        //dummy fraction for introduction
        fractionsSeq = fractionsSeq.concat("0.");
        for (int i = 0; i < decFormatString.length() -2 ; i++){
            fractionsSeq = fractionsSeq.concat("0");
        }        
        fractionsSeq = fractionsSeq.concat("1");
        for( int i = 0; i < cloneGroupParents.length -1 ; i++ ){
            fractionsSeq = fractionsSeq.concat(fractionsSeqDummyNext); 
        }
        fractionsSeq = fractionsSeq.concat(", # dummy fraction not to have the initial spread from 0 to initial population size"
                + Artifact.outCSVeol);
        
        //calculate fractions
        for (int cycle = 0; cycle < stackedCellsInCycles.size(); cycle++){
            
            int[] cloneFractions = fractionsInCycles.get(cycle);
            int timepoint = cycle + 2;
            
            if (hasFractionGreaterThan100(cloneFractions))
                fractionsSeq = fractionsSeq.concat("#");
            else{
                fractionsSeq = fractionsSeq.concat(" ");
                timePoints.add(timepoint);
            }           
            
            for (int j = 0; j < cloneFractions.length; j++){                
                if ( (cycle == maxCycle) && ( j == ( cloneFractions.length - 1) ) ){
                    fractionsSeq = fractionsSeq.concat(
                        new DecimalFormat(decFormatString, decimalSymbols).format((double)cloneFractions[j]/(double)divider));
                } else {
                    fractionsSeq = fractionsSeq.concat(
                        new DecimalFormat(decFormatString, decimalSymbols).format((double)cloneFractions[j]/(double)divider) + ", ");
                }
            }
            if (cycle < maxCycle)
                fractionsSeq = fractionsSeq.concat("# Timepoint: " + String.valueOf(cycle+2) 
                       + " cycle: " + String.valueOf(cycle) 
                       + Artifact.outCSVeol);            
        }

        return fractionsSeq;
        
    }    
    
    /*
    Calculates the sequence of vertical lines (plot divisions) to be plot
    to divide the timeline of the drawings
    into number of blocks specified in Constants.
    */
    private void getVerticalLinesSequence(){

        if (FishPlot.countOfPlotBlocks > 1){
            verticalLines = new ArrayList<>();
            int step = timePoints.size() / (FishPlot.countOfPlotBlocks);                   
            int currIndex = step;
            verticalLines.add(0);

            for (int lineToAddNo = 1; lineToAddNo < FishPlot.countOfPlotBlocks; lineToAddNo++){
                verticalLines.add(timePoints.get(currIndex));
                currIndex+= step;
            }
            
            verticalLines.add(timePoints.get(timePoints.size()-1));            
        }
    }
   
    /*
    Preapre the sequence of vertical lines labels
    */
    
    private void getVerticalLinesLabels(){
        verticalLinesLabels = new ArrayList<>();
        verticalLinesLabels.add("\"BEGIN\"");
        for (int lineIndex = 1; lineIndex < verticalLines.size() - 1; lineIndex++){
            verticalLinesLabels.add("\"CYCLE: " + String.valueOf(verticalLines.get(lineIndex)) + "\"");
        }
        verticalLinesLabels.add("\"END\"");
    }

    /*
    Formats the vertical lines data to a string
    which will be used as a part of the generated R script    
    The expected result is like: vlines=c(0,150,300)
    */
    private String getScriptVerticalLinesString(){
        
        String vLines = "vlines=c(";
        if (FishPlot.countOfPlotBlocks == 0)
            return "";

        vLines = vLines.concat(String.valueOf(verticalLines.get(0)));
        for ( int i = 1; i < verticalLines.size() - 1; i++){
            vLines = vLines.concat("," + String.valueOf(verticalLines.get(i)));
        }
        vLines = vLines.concat(
                "," 
                + String.valueOf(verticalLines.get(verticalLines.size() - 1))
                + ")");        
        return vLines;       
    }

    /*
    Formats the vertical lines labels data to a string
    which will be used as a part of the generated R script    
    The expected result is like: vlab=c(\"day 0\",\"day 150\")"
    */
    private String getScriptVerticalLinesLabelsString(){
        
        String vLabels = "vlab=c(";
        if (FishPlot.countOfPlotBlocks == 0)
            return "";

        vLabels = vLabels.concat(verticalLinesLabels.get(0));
        for ( int i = 1; i < verticalLinesLabels.size() - 1; i++){
            vLabels = vLabels.concat("," + verticalLinesLabels.get(i));
        }
        vLabels = vLabels.concat(
            "," 
            + verticalLinesLabels.get(verticalLinesLabels.size() - 1) 
            + ")");        
        return vLabels;       
    }
    
    /*
    This is to check if (due to corrections of children fractions
    summing up to (at least) parent fractions) the parent fraction
    is grater than 100%
    */
    private boolean hasFractionGreaterThan100(int[] fractions){
        return (Arrays.stream(fractions).max().getAsInt() > 100);
    }

    /*
    correct fractions due to possible roundings
    in fishplot all child clones can sum up to parent clone -> not more      
    */
    private int[] correctFractions(int[] inFractions, int[] inclusionKeys){

        int cloneFractions[] = new int[inFractions.length];
        cloneFractions = inFractions.clone();
        
        for (int parentCloneGroupIndexOnInclusionKeys = inclusionKeys.length - 1; parentCloneGroupIndexOnInclusionKeys >= 0; parentCloneGroupIndexOnInclusionKeys--){
            int panertCloneGroup = inclusionKeys[parentCloneGroupIndexOnInclusionKeys];
            int parentCloneIndex = panertCloneGroup - 1;
            int parentCloneFraction = cloneFractions[parentCloneIndex];
            int includedClonesFractionSum = 0;
            for (Integer childClone : cloneInclusions.get(panertCloneGroup)){
                int childCloneIndex = childClone - 1;
                includedClonesFractionSum += cloneFractions[childCloneIndex];                
            }
            int diff = includedClonesFractionSum - parentCloneFraction;
            if ( diff > 0 ){
                cloneFractions[parentCloneIndex] += diff;                    
            }
        }
        for (int i = 0; i < cloneFractions.length; i++)
            cloneFractions[i] = Math.min(cloneFractions[i], multiplier);
        
        return cloneFractions;
            
    }    
    
    /*
    Stack the cell numbers in each clone so that they sum up to 
    parent clone size
    */
    private int[] stackCellsPerClone(int[] inFractions){
        
        int corrected[] = new int[inFractions.length];
        int tmp[] = new int[inFractions.length];
        corrected = inFractions.clone();
        tmp = inFractions.clone();
        
        for (int i = inFractions.length -1 ; i > 0 ; i--){
            int parent = cloneGroupParents[i];
            int parentIndex = parent - 1;           
            tmp[parentIndex] += tmp[i];
        }
        for (int i = 0 ; i < inFractions.length -1 ; i++){
            corrected[i] = Math.max(corrected[i], tmp[i]);
        }
        return corrected;
    }    

    /*
    creates another form of clone group parents notation for Fishplot
    input: 01111222222777
    output: 1<-2,3,4,5
            2<-6,7,8,9,10,11
            7<-12,13,14    
    */
    private void calculateCloneInclusions(){
        
        cloneInclusions = new HashMap<> ();        
        for (int i = cloneGroupParents.length - 1; i > 0; i--){
            ArrayList<Integer> inclMembers = cloneInclusions.get(cloneGroupParents[i]);
            if ( inclMembers != null){
                inclMembers.add(i+1);
            } else {
                ArrayList<Integer> inclMembersNew = new ArrayList<>();
                inclMembersNew.add(i+1);
                cloneInclusions.put(cloneGroupParents[i], inclMembersNew);
            }
        }
        
    }

    /*
    Formats the timepoints data into string
    which will be used as a part of the generated R script
    */      
    private String getScriptTimePointsString(){  
                
        String timePointsStr = ""
            + Artifact.outCSVeol
            + "#provide a list of timepoints to plot"
            + Artifact.outCSVeol
            + "timepoints=c("
            + Artifact.outCSVeol;    
        for ( int i = 0; i < timePoints.size(); i++){
            timePointsStr += new DecimalFormat("00000").format(timePoints.get(i));
            if (i != (timePoints.size() - 1)){
                timePointsStr += ",";
            }
            if ( i > 0 && (i+1)%10 == 0){
                timePointsStr = timePointsStr.concat("# Timepoints: " 
                    + new DecimalFormat("00000").format(timePoints.get(i-9)) 
                    + " - " 
                    + new DecimalFormat("00000").format(timePoints.get(i))
                    + Artifact.outCSVeol);
            }
        }
        timePointsStr = timePointsStr.concat(Artifact.outCSVeol
            + ")"
            + Artifact.outCSVeol);

        return timePointsStr;
               
    }

    /*
    Calculates the amount of cells in clone per simulation cycle
    */
    private int[] calcCellsPerCloneForCycle(int cycle){
        
        int fractions[] = new int[maxCloneGroupIndex+1];
        
        Set<Entry<GenomePart, List<Cell>>> cycleDataEntries = equalGenomePart.get(cycle).entrySet();
        for( Entry<GenomePart, List<Cell>> entry : cycleDataEntries ){
            int groupID = entry.getValue().get(0).getGenome(genomes).getDriverCloneGroupID();
            fractions[groupID] = entry.getValue().size();
        }   
        return fractions;
    }
    
    /*
    Prapres the clone group parents array in a form where
    on the index is clone group and on the value is clone
    grup which is the parent
    ex. 2,3,4 originates from 1 and
        5,6,7 originates from 3, than the sequence will be:
    0,1,1,1,3,3,3
    */
    private void calculateCloneGroupParents(){
        
        cloneGroupParents   = new int[maxCloneGroupIndex+1];
        
        cycles = new ArrayList<> ();
        equalGenomePart.entrySet().forEach((entry) -> {
            cycles.add(entry.getKey());
        });
        Collections.sort(cycles);

        for(Integer cycle : cycles){

            AccSeq4HMwithGenomePartKey sequence = new AccSeq4HMwithGenomePartKey();
            equalGenomePart.get(cycle).entrySet().forEach((entry) -> {
                sequence.addNewEntry(entry.getKey(), entry.getValue().size(), entry.getValue().get(0));
            });
            sequence.sort();
            GenomePart nextKey;
            while((nextKey = sequence.getNextKey()) != null){
                int cloneGroupID = sequence.getSampleCell().getGenome(genomes).getDriverCloneGroupID();
                int parentCloneGroupID = nextKey.getParentCloneGroupID();
                cloneGroupParents[cloneGroupID] = parentCloneGroupID;
            }

        };
        //shift by one due to R notation
        //first clone has no parents
        for (int i = 1; i < cloneGroupParents.length ; i++){
            cloneGroupParents[i] += 1;
        }         
    }
    
    /*
    Formats the clone group parents data into string
    which will be used as a part of the generated R script
    */    
    private String getScriptParentsSequenceString(){
       
        String parentSequence = Artifact.outCSVeol
            + "#provide a vector listing each clone's parent"
            + Artifact.outCSVeol
            + "#(0 indicates no parent)"
            + Artifact.outCSVeol
            + "parents = c("
            + Artifact.outCSVeol;
        
        for ( int i = 0; i < cloneGroupParents.length; i++){
            parentSequence += new DecimalFormat("00000").format(cloneGroupParents[i]);
            if (i != (cloneGroupParents.length - 1)){
                parentSequence = parentSequence.concat(",");
            }
            if ( i > 0 && (i+1)%10 == 0){
                parentSequence = parentSequence.concat("# Parents for clones: " 
                     + new DecimalFormat("00000").format(i-8) 
                     + " - " 
                     + new DecimalFormat("00000").format(i+1)
                     + Artifact.outCSVeol);
            }
        }

        parentSequence  = parentSequence.concat(Artifact.outCSVeol
            + ")"
            + Artifact.outCSVeol);
        
        return parentSequence;
    }

    /*
    Generates the "created by" comment part into string
    which will be used as a part of the generated R script
    */      
    private String getScriptGeneratedByString(){
        String generatedBy = "";

        generatedBy = "# Generated by: "
                    + Constant.appN + " " + Constant.appV
                    + Artifact.outCSVeol
                    + "# Experiment: "
                    + analytics.getSim().getSimModel().getFilePaths().getWorkDir()
                    + Artifact.outCSVeol
                    + Artifact.outCSVeol;
        
        return generatedBy;
    }

    /*
    Generates the fishplot librbay inclusion into string
    which will be used as a part of the generated R script
    */          
    private String getScriptLibraryString(){
        return Artifact.outCSVeol + "library(fishplot)" + Artifact.outCSVeol;
    }
    
    /*
    Generates the fishplot call to create plot into string
    which will be used as a part of the generated R script
    */       
    private String getScriptFishplotCallString(){

        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');   
        
        String fishPlotCall = Artifact.outCSVeol
            + "#create a fish object"
            + Artifact.outCSVeol
            + "fish = createFishObject(frac.table,parents,timepoints=timepoints)"
            + Artifact.outCSVeol
            + Artifact.outCSVeol
            + "#calculate the layout of the drawing"
            + Artifact.outCSVeol
            + "fish = layoutClones(fish)"
            + Artifact.outCSVeol
            + Artifact.outCSVeol      
            + "#pass the colors vector"
            + Artifact.outCSVeol
            + "fish = setCol(fish, color.vector)"
            + Artifact.outCSVeol
            + Artifact.outCSVeol            
            + "#draw the plot, using the splining method (recommended)"
            + Artifact.outCSVeol
            + "#and providing both timepoints to label and a plot title"
            + Artifact.outCSVeol
            + "fishPlot(fish,title.btm="
            + FishPlot.title
            + ","
            + Artifact.outCSVeol
            + "cex.title="
            + new DecimalFormat("0.0", decimalSymbols).format(FishPlot.titleSize);        
        if (FishPlot.countOfPlotBlocks > 0){
            getVerticalLinesSequence();
            fishPlotCall = fishPlotCall.concat("," + Artifact.outCSVeol + getScriptVerticalLinesString());
            getVerticalLinesLabels();
            fishPlotCall = fishPlotCall.concat("," + Artifact.outCSVeol + getScriptVerticalLinesLabelsString());
        }
        fishPlotCall = fishPlotCall.concat(Artifact.outCSVeol
            + ")" 
            + Artifact.outCSVeol);
        return fishPlotCall;
    }
    
}
