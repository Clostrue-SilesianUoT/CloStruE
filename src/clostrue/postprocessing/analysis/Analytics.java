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
 
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.GenomePart;
import clostrue.model.SimModel;
import clostrue.Simulation;
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.enumerations.MutationType;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.ModelParam;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Name;
import clostrue.collections.CellCollectionWithHolderData;
import clostrue.collections.CellIndexHolder;
import clostrue.collections.GenomeCollection;
import clostrue.postprocessing.analysis.mutationtType.MutationTypeAnalysis;
import clostrue.postprocessing.analysis.genealogy.GenealogyAnalysis;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.BufferedWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Adds various analytic capabilities to the application (Mutation Type Analysis)
 * @author Krzysztof Szymiczek
 */

public class Analytics {

    Simulation sim;
    SimModel   model;
    MutationTypeAnalysis mta;
    int        iteration;
    int maxDriverTagId;
    int maxPassengerTagId;

    GenomeCollection genomes;
    CellCollection cellCollection;
    HashMap<GenomePart, CellIndexHolder> internalClones;

    private final double[] quotaTreshold;
    private final HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    equalShadowDriverPart; 
    private final HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    equalShadowPassengerPart; 
    private final HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    equalSurvivorsDriverPart;
    private final HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    equalSurvivorsPassengerPart; 

    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalSurvivorsSinglePassengerLocus; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalSurvivorsSingleDriverLocus; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalSurvivorsSinglePassengerGene; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalSurvivorsSingleDriverGene;        
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalShadowSinglePassengerLocus; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalShadowSingleDriverLocus; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalShadowSinglePassengerGene; 
    public final ConcurrentHashMap<Integer, CellIndexHolder>[]        equalShadowSingleDriverGene;   

    public double[] getQuotaTreshold() {
        return quotaTreshold;
    }

    public HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    getEqualShadowDriverPart(){
        return equalShadowDriverPart;
    }
    public HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    getEqualShadowPassengerPart(){
        return equalShadowPassengerPart;
    }
    public HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    getEqualSurvivorsDriverPart(){
        return equalSurvivorsDriverPart;
    }
    public HashMap<Integer, HashMap<GenomePart, CellIndexHolder>>    getEqualSurvivorsPassengerPart(){
        return equalSurvivorsPassengerPart;
    }
    
    public CellCollection getCellCollection() {
        return cellCollection;
    }
    
    public MutationTypeAnalysis getMta() {
        return mta;
    }    
    
    /**
     * default constructor
     * @param inCallingSimulation
     */
    public Analytics(Simulation inCallingSimulation) {

        cellCollection = null;
        internalClones = null;        
        
        sim                         = inCallingSimulation;
        genomes = null;
        model                       = new SimModel(sim.getIteration(), sim.getSimModel());
        iteration                   = sim.getIteration();
        mta                         = new MutationTypeAnalysis(sim);
        maxDriverTagId = model.getModParams().getMAM().getMaxDriverTagId();
        maxPassengerTagId = model.getModParams().getMAM().getMaxPassengerTagId();
        
        equalShadowDriverPart       = new HashMap<> ();
        equalShadowPassengerPart    = new HashMap<> ();
        equalSurvivorsDriverPart    = new HashMap<> ();   
        equalSurvivorsPassengerPart = new HashMap<> ();         

        equalSurvivorsSinglePassengerGene   = new ConcurrentHashMap[maxPassengerTagId + 1];
        equalSurvivorsSingleDriverGene      = new ConcurrentHashMap[maxDriverTagId + 1];
        equalShadowSinglePassengerGene      = new ConcurrentHashMap[maxPassengerTagId + 1];
        equalShadowSingleDriverGene         = new ConcurrentHashMap[maxDriverTagId + 1];

        initializeChmTable(equalSurvivorsSinglePassengerGene); 
        initializeChmTable(equalSurvivorsSingleDriverGene);        
        initializeChmTable(equalShadowSinglePassengerGene); 
        initializeChmTable(equalShadowSingleDriverGene);         
        
        if (Constant.heavyLocusAnalysis){
            equalSurvivorsSinglePassengerLocus      = new ConcurrentHashMap[maxPassengerTagId + 1];
            equalSurvivorsSingleDriverLocus         = new ConcurrentHashMap[maxDriverTagId + 1];   
            equalShadowSinglePassengerLocus         = new ConcurrentHashMap[maxPassengerTagId + 1];
            equalShadowSingleDriverLocus            = new ConcurrentHashMap[maxDriverTagId + 1];
            initializeChmTable(equalSurvivorsSinglePassengerLocus); 
            initializeChmTable(equalSurvivorsSingleDriverLocus); 
            initializeChmTable(equalShadowSinglePassengerLocus); 
            initializeChmTable(equalShadowSingleDriverLocus); 
        } else {
            equalSurvivorsSinglePassengerLocus      = new ConcurrentHashMap[0];
            equalSurvivorsSingleDriverLocus         = new ConcurrentHashMap[0];   
            equalShadowSinglePassengerLocus         = new ConcurrentHashMap[0];
            equalShadowSingleDriverLocus            = new ConcurrentHashMap[0];            
        }
        
        quotaTreshold               = new double[sim.getSettings().getIntValue(Param.teQuota)+1];
        for (int tresholdIndex = 0; tresholdIndex <= sim.getSettings().getIntValue(Param.teQuota); tresholdIndex++)
            quotaTreshold[tresholdIndex] = (double)tresholdIndex / (double)100;
        
    }

    private static void initializeChmTable( ConcurrentHashMap[] chmTable){
        for (int i = 0; i < chmTable.length; i++){
            chmTable[i] = new ConcurrentHashMap<Integer, CellIndexHolder>();
        }
    }
    
    public void importInternalClones(HashMap<GenomePart, CellIndexHolder> ic){
        internalClones = ic;
    }
    
    public void importGenomeCollection(GenomeSynchronizedCollection sc){
        genomes = new GenomeCollection(sc);
    }
    
    public void importCellCollection(CellCollection cc){
        cellCollection = cc;
    }
    
    public double getQuotaTresholdById(int quotaTresholdID) {
        return quotaTreshold[quotaTresholdID];
    }    

    public GenomeCollection getGenomes() {
        return genomes;
    }
    
    public void generateSurvivorsAnalyticFiles() throws IOException{

        StaticConsoleLogger.logActivity(iteration, Activity.saCorEqualSurvivorsDriverPart, Activity.started);       
        correctCellListGenomePartKeyed(equalSurvivorsDriverPart);
        StaticConsoleLogger.logActivity(iteration, Activity.saCorEqualSurvivorsDriverPart, Activity.finished);       
        
        StaticConsoleLogger.logActivity(iteration, Activity.saCorEqualSurvivorsPassengerPart, Activity.started);       
        correctCellListGenomePartKeyed(equalSurvivorsPassengerPart);
        StaticConsoleLogger.logActivity(iteration, Activity.saCorEqualSurvivorsPassengerPart, Activity.started);       

        GenealogyAnalysis sa = new GenealogyAnalysis(this);
        sa.handleStep1Tasks();
        sa.logHMSizes();
        
    }
    
    /**
     * Analyzing survivors (the last generation of cells)
     * A file is prepared which contains the informaiton of the common shard
     * mutations among the survivor cells. Exact genome, gene and locus
     * are analyzed and cells are groupped together having exact same set of mutations
     * or exact same mutation on the given gene
     * or exact same mutation on the given gene anf locus.
     */
    public void analyzeSurvivors(){
        
        int driversCount = model.getModParams().getMAM().getDrivers().length;
        Integer[] driverTagIds = new Integer[driversCount];
        for (int i = 0; i < driversCount; i++){
            driverTagIds[i] = model.getModParams().getMAM().getDrivers()[i].getGeneTagId();
        }


        int passengersCount = model.getModParams().getMAM().getPassengers().length;
        Integer[] passengerTagIds = new Integer[passengersCount];
        for (int i = 0; i < passengersCount; i++){
            passengerTagIds[i] = model.getModParams().getMAM().getPassengers()[i].getGeneTagId();            
        }
        
        int commonGeneTagId = ModelParam.commonGeneTagId;

        StaticConsoleLogger.logActivity(iteration, Activity.analyzeSurvivors, Activity.started);
        
        try {

            int lastCycle = getLastCycleFromInternalClones();
            StaticConsoleLogger.logActivity(iteration, Activity.createSubsets, Activity.started);

            int clonesCount = internalClones.entrySet().size();
            int currClone = 0;
            int currPercent = 0;
            int prevPercent = 0;
            
            for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
                currClone++;
                currPercent = (int)((double)100 * (double)currClone / (double)clonesCount);
                if (currPercent != prevPercent){
                    prevPercent = currPercent;
                    StaticConsoleLogger.log(iteration, Activity.createSubsetsPercent + String.valueOf(currPercent));
                }
                for (int c = 0; c < clone.getValue().size(); c++){
                    int cellHolderIndex = clone.getValue().get(c);
                    Cell cell = cellCollection.getByIndex(cellHolderIndex);

                    combineByCycleAndDriverPart(cellHolderIndex, cell, equalShadowDriverPart);
                    combineByCycleAndPassengerPart(cellHolderIndex, cell, equalShadowPassengerPart);
                        
                    ArrayList<Integer> drivMutations = cell.getGenome(genomes).getDrivers(genomes).getMutations(MutationType.Driver, genomes);
                    int drivMutationCount = drivMutations.size();
                    for (int i = 0; i < drivMutationCount; i++){
                        
                        int toss = drivMutations.get(i);
                        int gene = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(toss);
                        Integer geneTagId = driverTagIds[gene];

                        if (Constant.heavyLocusAnalysis){
                            combineBy(equalShadowSingleDriverLocus[commonGeneTagId], toss, cellHolderIndex);
                            combineBy(equalShadowSingleDriverLocus[geneTagId], toss, cellHolderIndex);                            
                        }

                        combineBy(equalShadowSingleDriverGene[commonGeneTagId], gene, cellHolderIndex);
                        combineBy(equalShadowSingleDriverGene[geneTagId], gene, cellHolderIndex);
                        
                    }

                    if (1 == 2){

                        //switched off due to heavy performance issues (file content not tested as well)                        
                        ArrayList<Integer> passMutations = cell.getGenome(genomes).getPassengers(genomes).getMutations(MutationType.Passenger, genomes);
                        int passMutationCount = passMutations.size();
                        for (int i = 0; i < passMutationCount; i++){

                            int toss = passMutations.get(i);
                            int gene = model.getModParams().getMAM().getRegionMapPassengers().getGeneBasedOnToss(toss);
                            Integer geneTagId = passengerTagIds[gene];

                            if (Constant.heavyLocusAnalysis){
                                combineBy(equalShadowSinglePassengerLocus[commonGeneTagId], toss, cellHolderIndex);
                                combineBy(equalShadowSinglePassengerLocus[geneTagId], toss, cellHolderIndex);
                            }
                            
                            combineBy(equalShadowSinglePassengerGene[commonGeneTagId], gene, cellHolderIndex);
                            combineBy(equalShadowSinglePassengerGene[geneTagId], gene, cellHolderIndex);                            
                           

                        }                        
                    }
                    

                    if (cell.getModelCycle() == lastCycle){

                        combineByCycleAndDriverPart(cellHolderIndex, cell, equalSurvivorsDriverPart);
                        combineByCycleAndDriverPart(cellHolderIndex, cell, equalSurvivorsPassengerPart);

                        drivMutations = cell.getGenome(genomes).getDrivers(genomes).getMutations(MutationType.Driver, genomes);
                        drivMutationCount = drivMutations.size();
                    
                        for (int i = 0; i < drivMutationCount; i++){

                            int toss = drivMutations.get(i);
                            int gene = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(toss);

                            Integer geneTagId = driverTagIds[gene];

                            if (Constant.heavyLocusAnalysis){
                                combineBy(equalSurvivorsSingleDriverLocus[commonGeneTagId], toss, cellHolderIndex);
                                combineBy(equalSurvivorsSingleDriverLocus[geneTagId], toss, cellHolderIndex);
                            }                            
                            combineBy(equalSurvivorsSingleDriverGene[commonGeneTagId], gene, cellHolderIndex);
                            combineBy(equalSurvivorsSingleDriverGene[geneTagId], gene, cellHolderIndex);                            
                            
                        }
                        
                        ArrayList<Integer>  passMutations = cell.getGenome(genomes).getPassengers(genomes).getMutations(MutationType.Passenger, genomes);
                        int passMutationCount = passMutations.size();
                        for (int i = 0; i < passMutationCount; i++){

                            int toss = passMutations.get(i);
                            int gene = model.getModParams().getMAM().getRegionMapPassengers().getGeneBasedOnToss(toss);

                            Integer geneTagId = passengerTagIds[gene];

                            if (Constant.heavyLocusAnalysis){
                                combineBy(equalSurvivorsSinglePassengerLocus[commonGeneTagId], toss, cellHolderIndex);
                                combineBy(equalSurvivorsSinglePassengerLocus[geneTagId], toss, cellHolderIndex);
                            }
                            combineBy(equalSurvivorsSinglePassengerGene[commonGeneTagId], gene, cellHolderIndex);
                            combineBy(equalSurvivorsSinglePassengerGene[geneTagId], gene, cellHolderIndex);                            
                            
                        }
                    }
                }
            }

            StaticConsoleLogger.logActivity(iteration, Activity.createSubsets, Activity.finished);
            mutationTypeAnalysis();
            StaticConsoleLogger.logActivity(iteration, Activity.exportSubsets, Activity.started);
            
            if (sim.getSettings().getBooleanValue(Param.cbGenerateSurvivorsAnalytics)){           
                generateSurvivorsAnalyticFiles();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
        }

        StaticConsoleLogger.logActivity(iteration, Activity.exportSubsets, Activity.finished);                
        StaticConsoleLogger.logActivity(iteration, Activity.analyzeSurvivors, Activity.finished);

    }

    /**
     * combines mutaion by locus or gene for analys
     * @param map           Concurrent Hash Map 
     * @param geneTag       Gene Tag
     * @param mutationToss  Mutation (toss)
     * @param cell          Cell with mutation to add to CHM
     */
    private static void combineBy(
            ConcurrentHashMap<Integer, CellIndexHolder> chmForGeneTag,
            Integer valueToCombineOn,
            int cellHolderIndex){

        CellIndexHolder currentSet;        
        
        if (chmForGeneTag != null){
            currentSet = chmForGeneTag.get(valueToCombineOn);
            if (currentSet != null){
                currentSet.add(cellHolderIndex);
            } else {
                currentSet = new CellIndexHolder();
                currentSet.add(cellHolderIndex);
                chmForGeneTag.put(valueToCombineOn, currentSet);
            }                                
        } else {
            currentSet = new CellIndexHolder();
            currentSet.add(cellHolderIndex);
            chmForGeneTag = new ConcurrentHashMap<>();
            chmForGeneTag.put(valueToCombineOn, currentSet);                                
        }        
    }
    
    private void correctCellListGenomePartKeyed(HashMap<Integer, HashMap<GenomePart, CellIndexHolder>> cHM){       
        for (Entry<Integer,HashMap<GenomePart, CellIndexHolder>> entry :  cHM.entrySet()){
            ArrayList<GenomePart> toRemove = new ArrayList<>();
            for (Entry<GenomePart, CellIndexHolder> entryDeep : entry.getValue().entrySet()){
                Set<Cell> setCells = new HashSet<>();
                CellCollectionWithHolderData cellsWithHolderData = new CellCollectionWithHolderData(cellCollection, entryDeep.getValue());
                ArrayList<Cell> cells = cellsWithHolderData.getCells();
                setCells.addAll(cells);
                entryDeep.setValue(cellsWithHolderData.getCellIndexHolder(setCells));
                if (entryDeep.getValue().size() == 1){
                    toRemove.add(entryDeep.getKey());               
                }
            }
            for (GenomePart gp : toRemove){
                entry.getValue().remove(gp);
            }
            toRemove.clear();
        }
    }    
    
    
    /**
     * Performs the mutation type Analysis
     * Mutation of type "X" is the mutation which occurs in exact "X" cells
     * Histograms are created for locus ang genes separatelly
     * Seperatelly also are analyzed the driver and passenger regions which gives
     * 2x2 = 4 Histograms
     */
    private void mutationTypeAnalysis(){

        mta.setAnalytics(this);
        StaticConsoleLogger.logActivity(iteration, Activity.mutationTypeAnalysis, Activity.started);
        mta.handleStep1Tasks();
        mta.handleStep2Tasks();        
        StaticConsoleLogger.logActivity(iteration, Activity.mutationTypeAnalysis, Activity.finished);           
        mta.logHMSizes();
        
    }
    
    /**
     * Prepares the analytics for clones 
     */
    public void analyzeShadowSignificantClones() {
        
        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.started);
    
        List<Cell> cloneCells;
        int size;
        int beginCycle;
        int endCycle;
        int lifeSpan;
        int driverMutationsCount;
        String mutationsString;
        if (sim.getSettings().getBooleanValue(Param.cbPrepareClones)){
            String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                    + java.io.File.separator 
                    + Name.clonesShadowSig
                    + Extension.dotTxt;
            try {        
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
                    for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
                        cloneCells = cellCollection.getByCellIndexHolder(clone.getValue());
                        size = cloneCells.size();
                        beginCycle = cloneCells.get(0).getModelCycle();
                        driverMutationsCount = cloneCells.get(0).getGenome(genomes).getDriverMutationCount();
                        endCycle = cloneCells.get(cloneCells.size()-1).getModelCycle();
                        lifeSpan = endCycle - beginCycle;
                        if ( ( size > 1 ) &&
                                ( driverMutationsCount > 0 ) &&
                                ( size >= sim.getSettings().getIntValue(Param.teCloneMinSize)) &&
                                ( lifeSpan >= sim.getSettings().getIntValue(Param.teCloneMinLifespan) ) ){
                            bufferedWriter.write("---------- New Cell Clone Group --------");
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells in group: " + String.valueOf(size));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Parent Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDrivers(genomes).getParentCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone begin in cycle: " + String.valueOf(beginCycle));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone lifespan: " + String.valueOf(endCycle - beginCycle + 1));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Driver mutation count: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverMutationCount()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Mutations: ");
                            bufferedWriter.write(Artifact.outCSVeol);

                            mutationsString = "";
                            for (Integer mutation : clone.getKey().getDrivMutations(genomes)){
                                int driver = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(mutation);
                                mutationsString = model.getModParams().getMAM().getDrivers()[driver].getGeneName()
                                        + "@" + String.valueOf(mutation)
                                        + Artifact.outCSVMutationSeparator
                                        + mutationsString;
                            }
                            bufferedWriter.write(mutationsString);

                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells: ");
                            bufferedWriter.write(Artifact.outCSVeol);
                            if (cloneCells.size() > sim.getSettings().getIntValue(Param.teCellListerCutOff)){
                                bufferedWriter.write(" << not listed due to CutOff limitation in program >> ");
                            } else {
                                for (Cell cell : cloneCells){
                                    bufferedWriter.write("c"+cell.getModelCycle()+"_i"+cell.getId());
                                    bufferedWriter.write(" " + Artifact.outCSVMutationSeparator + " ");
                                }
                            }
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write(Artifact.outCSVeol);
                        }
                    }
                    bufferedWriter.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.finished);

    }

    /**
     * Prepares the analytics for clones 
     */
    public void analyzeShadowAllClones() {
        
        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.started);
    
        List<Cell> cloneCells;
        int size;
        int beginCycle;
        int endCycle;
        int lifeSpan;
        int driverMutationsCount;
        String mutationsString;
        if (sim.getSettings().getBooleanValue(Param.cbPrepareClones)){
            String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                    + java.io.File.separator 
                    + Name.clonesShadowAll
                    + Extension.dotTxt;
            try {        
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
                    for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
                        cloneCells = cellCollection.getByCellIndexHolder(clone.getValue());
                        size = cloneCells.size();
                        beginCycle = cloneCells.get(0).getModelCycle();
                        driverMutationsCount = cloneCells.get(0).getGenome(genomes).getDriverMutationCount();
                        endCycle = cloneCells.get(cloneCells.size()-1).getModelCycle();
                        lifeSpan = endCycle - beginCycle;
                        if ( ( size > 0 ) && ( driverMutationsCount > 0 ) ){
                            bufferedWriter.write("---------- New Cell Clone Group --------");
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells in group: " + String.valueOf(size));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Parent Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDrivers(genomes).getParentCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone begin in cycle: " + String.valueOf(beginCycle));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone lifespan: " + String.valueOf(endCycle - beginCycle + 1));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Driver mutation count: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverMutationCount()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Mutations: ");
                            bufferedWriter.write(Artifact.outCSVeol);

                            mutationsString = "";
                            for (Integer mutation : clone.getKey().getDrivMutations(genomes)){
                                int driver = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(mutation);
                                mutationsString = model.getModParams().getMAM().getDrivers()[driver].getGeneName()
                                        + "@" + String.valueOf(mutation)
                                        + Artifact.outCSVMutationSeparator
                                        + mutationsString;
                            }
                            bufferedWriter.write(mutationsString);

                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells: ");
                            bufferedWriter.write(Artifact.outCSVeol);
                            if (cloneCells.size() > sim.getSettings().getIntValue(Param.teCellListerCutOff)){
                                bufferedWriter.write(" << not listed due to CutOff limitation in program >> ");
                            } else {
                                for (Cell cell : cloneCells){
                                    bufferedWriter.write("c"+cell.getModelCycle()+"_i"+cell.getId());
                                    bufferedWriter.write(" " + Artifact.outCSVMutationSeparator + " ");
                                }
                            }
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write(Artifact.outCSVeol);
                        }
                    }
                    bufferedWriter.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.finished);

    }    
    
    /**
     * Prepares the analytics for clones 
     */
    public void analyzeSurvivorsSignificantClones(int lastCycle) {
        
        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.started);
    
        List<Cell> cloneCells;
        int size;
        int beginCycle;
        int endCycle;
        int lifeSpan;
        int driverMutationsCount;
        String mutationsString;
        if (sim.getSettings().getBooleanValue(Param.cbPrepareClones)){
            String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                    + java.io.File.separator 
                    + Name.clonesSurvivorsSig
                    + Extension.dotTxt;
            try {        
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
                    for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
                        cloneCells = cellCollection.getByCellIndexHolder(clone.getValue());
                        beginCycle = cloneCells.get(0).getModelCycle();
                        driverMutationsCount = cloneCells.get(0).getGenome(genomes).getDriverMutationCount();
                        endCycle = cloneCells.get(cloneCells.size()-1).getModelCycle();                        
                        cloneCells = getCellsFromLastCycle(cloneCells, lastCycle);
                        size = cloneCells.size();
                        lifeSpan = endCycle - beginCycle;
                        if ( ( size > 1 ) &&
                                ( driverMutationsCount > 0 ) &&
                                ( size >= sim.getSettings().getIntValue(Param.teCloneMinSize)) &&
                                ( lifeSpan >= sim.getSettings().getIntValue(Param.teCloneMinLifespan) ) &&
                                ( endCycle == lastCycle ) ){
                            bufferedWriter.write("---------- New Cell Clone Group --------");
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells in group: " + String.valueOf(size));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Parent Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDrivers(genomes).getParentCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone begin in cycle: " + String.valueOf(beginCycle));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone lifespan: " + String.valueOf(endCycle - beginCycle + 1));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Driver mutation count: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverMutationCount()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Mutations: ");
                            bufferedWriter.write(Artifact.outCSVeol);

                            mutationsString = "";
                            for (Integer mutation : clone.getKey().getDrivMutations(genomes)){
                                int driver = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(mutation);
                                mutationsString = model.getModParams().getMAM().getDrivers()[driver].getGeneName()
                                        + "@" + String.valueOf(mutation)
                                        + Artifact.outCSVMutationSeparator
                                        + mutationsString;
                            }
                            bufferedWriter.write(mutationsString);

                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells: ");
                            bufferedWriter.write(Artifact.outCSVeol);
                            if (cloneCells.size() > sim.getSettings().getIntValue(Param.teCellListerCutOff)){
                                bufferedWriter.write(" << not listed due to CutOff limitation in program >> ");
                            } else {
                                for (Cell cell : cloneCells){
                                    bufferedWriter.write("c"+cell.getModelCycle()+"_i"+cell.getId());
                                    bufferedWriter.write(" " + Artifact.outCSVMutationSeparator + " ");
                                }
                            }
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write(Artifact.outCSVeol);
                        }
                    }
                    bufferedWriter.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.finished);

    }
    
    /**
     * Return from clone only cells from last cycle
     * @param cellsIn all cells in the clone
     * @param lastCycle last cycle
     * @return  cells from the clone which are only from last cycle
     */
    public List<Cell> getCellsFromLastCycle(List<Cell> cellsIn, int lastCycle){
        ArrayList<Cell> cellsOut = new ArrayList<>();
        for(Cell c : cellsIn){
            if (c.getModelCycle() == lastCycle){
                cellsOut.add(c);
            }
        }
        return cellsOut;
    }
    
    /**
     * Prepares the analytics for clones 
     */
    public void analyzeSurvivorsAllClones(int lastCycle) {
        
        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.started);
    
        List<Cell> cloneCells;
        int size;
        int beginCycle;
        int endCycle;
        int lifeSpan;
        int driverMutationsCount;
        String mutationsString;
        if (sim.getSettings().getBooleanValue(Param.cbPrepareClones)){
            String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                    + java.io.File.separator 
                    + Name.clonesSurvivorsAll
                    + Extension.dotTxt;
            try {        
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
                    for( Map.Entry<GenomePart, CellIndexHolder> clone : internalClones.entrySet() ){
                        cloneCells = cellCollection.getByCellIndexHolder(clone.getValue());
                        beginCycle = cloneCells.get(0).getModelCycle();
                        driverMutationsCount = cloneCells.get(0).getGenome(genomes).getDriverMutationCount();
                        endCycle = cloneCells.get(cloneCells.size()-1).getModelCycle();                        
                        cloneCells = getCellsFromLastCycle(cloneCells, lastCycle);
                        size = cloneCells.size();
                        lifeSpan = endCycle - beginCycle;
                        if ( ( size > 0 ) && ( driverMutationsCount > 0 ) && ( endCycle == lastCycle ) ){
                            bufferedWriter.write("---------- New Cell Clone Group --------");
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells in group: " + String.valueOf(size));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Parent Group ID: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDrivers(genomes).getParentCloneGroupID()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone begin in cycle: " + String.valueOf(beginCycle));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Clone lifespan: " + String.valueOf(endCycle - beginCycle + 1));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Driver mutation count: " + String.valueOf(cloneCells.get(0).getGenome(genomes).getDriverMutationCount()));
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Mutations: ");
                            bufferedWriter.write(Artifact.outCSVeol);

                            mutationsString = "";
                            for (Integer mutation : clone.getKey().getDrivMutations(genomes)){
                                int driver = model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(mutation);
                                mutationsString = model.getModParams().getMAM().getDrivers()[driver].getGeneName()
                                        + "@" + String.valueOf(mutation)
                                        + Artifact.outCSVMutationSeparator
                                        + mutationsString;
                            }
                            bufferedWriter.write(mutationsString);

                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write("Cells: ");
                            bufferedWriter.write(Artifact.outCSVeol);
                            if (cloneCells.size() > sim.getSettings().getIntValue(Param.teCellListerCutOff)){
                                bufferedWriter.write(" << not listed due to CutOff limitation in program >> ");
                            } else {
                                for (Cell cell : cloneCells){
                                    bufferedWriter.write("c"+cell.getModelCycle()+"_i"+cell.getId());
                                    bufferedWriter.write(" " + Artifact.outCSVMutationSeparator + " ");
                                }
                            }
                            bufferedWriter.write(Artifact.outCSVeol);
                            bufferedWriter.write(Artifact.outCSVeol);
                        }
                    }
                    bufferedWriter.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        StaticConsoleLogger.logActivity(iteration, Activity.analyzeClones, Activity.finished);

    }        
    
    public int getLastCycleFromInternalClones(){
        
        int lastCycle                       = 0;
        List<Cell> cloneCells;
        
        for( Map.Entry<GenomePart, CellIndexHolder> clone : sim.getStatistics().getInternalClones().entrySet() ){
            cloneCells = cellCollection.getByCellIndexHolder(clone.getValue());
            for (int i = 0; i < cloneCells.size(); i++){
                Cell cell = cloneCells.get(i);
                lastCycle = Math.max(lastCycle, cell.getModelCycle());                    
            }
        }

        return lastCycle;
    }    
    
    private void combineByCycleAndDriverPart(
            int cellHolderIndex,
            Cell cell, 
            HashMap<Integer, HashMap<GenomePart, CellIndexHolder>> destinationByCycle){
        
        HashMap<GenomePart, CellIndexHolder> curentDestination;

        curentDestination = destinationByCycle.get(cell.getModelCycle());
        if (curentDestination != null){
            combineByDriverPart(cellHolderIndex, cell, curentDestination);
        } else {
            curentDestination = new HashMap<> ();
            combineByDriverPart(cellHolderIndex, cell, curentDestination);
            destinationByCycle.put(cell.getModelCycle(), curentDestination);
        }        
    }   
    
    private void combineByCycleAndPassengerPart(
            int cellHolderIndex,
            Cell cell, 
            HashMap<Integer, HashMap<GenomePart, CellIndexHolder>> destinationByCycle){
        
        HashMap<GenomePart, CellIndexHolder> curentDestination;

//        StaticConsoleLogger.log("Cell model cycle:" + cell.getModelCycle());
        curentDestination = destinationByCycle.get(cell.getModelCycle());
        if (curentDestination != null){
            combineByPassengerPart(cellHolderIndex, cell, curentDestination);
        } else {
            curentDestination = new HashMap<> ();
            combineByPassengerPart(cellHolderIndex, cell, curentDestination);
            destinationByCycle.put(cell.getModelCycle(), curentDestination);
        }        
    }    
    
    private void combineByDriverPart(int cellHolderIndex, Cell cell, HashMap<GenomePart, CellIndexHolder> destination){
        
        CellIndexHolder currentSet;
        currentSet = destination.get(cell.getGenome(genomes).getDrivers(genomes));
        if (currentSet != null){
            currentSet.add(cellHolderIndex);
        } else {
            currentSet = new CellIndexHolder();
            currentSet.add(cellHolderIndex);
            destination.put(cell.getGenome(genomes).getDrivers(genomes), currentSet);
        }        
    }    
    
    private void combineByPassengerPart(int cellHolderIndex, Cell cell, HashMap<GenomePart, CellIndexHolder> destination){
        
        CellIndexHolder currentSet;
        currentSet = destination.get(cell.getGenome(genomes).getPassengers(genomes));
        if (currentSet != null){
            currentSet.add(cellHolderIndex);
        } else {
            currentSet = new CellIndexHolder();
            currentSet.add(cellHolderIndex);
            destination.put(cell.getGenome(genomes).getPassengers(genomes), currentSet);
        }        
    }    
    
    public HashMap<Integer, HashMap<GenomePart, List<Cell>>> prepEqualShadowDriverPart() {

        //TODO May be better to rework flishplot to be able to handle CellIndexHolder...
        //instead of crating as below
        HashMap<Integer, HashMap<GenomePart, List<Cell>>> chm_external = new HashMap<>();

        for(Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> e : equalShadowDriverPart.entrySet()){
            HashMap<GenomePart, List<Cell>> chm_internal = new HashMap<>();
            for(Map.Entry<GenomePart, CellIndexHolder> f : e.getValue().entrySet()){
                List<Cell> listCells = Collections.synchronizedList(new ArrayList<Cell>());
                listCells.addAll(cellCollection.getByCellIndexHolder(f.getValue()));
                chm_internal.put(f.getKey(), listCells);
            }
            chm_external.put(e.getKey(), chm_internal);
        }

        return chm_external;
    }    
    
    public Simulation getSim() {
        return sim;
    } 
    
    public int getQuotasCount(){
        return sim.getSettings().getIntValue(Param.teQuota) + 1;
    }
    
}

