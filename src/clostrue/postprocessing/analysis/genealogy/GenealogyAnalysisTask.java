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
package clostrue.postprocessing.analysis.genealogy;
 
import clostrue.Simulation;
import clostrue.biology.genome.GenomePart;
import clostrue.collections.CellCollection;
import clostrue.postprocessing.analysis.mutationtType.MutationTypeAnalysis;
import clostrue.hardcodes.Activity;
import clostrue.collections.CellIndexHolder;
import clostrue.collections.GenomeCollection;
import clostrue.enumerations.MutationType;
import clostrue.enumerations.QuotedGenesTaskWorkToDo;
import clostrue.enumerations.GenealogyAnalysisTaskWorkToDo;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.Content;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.file.Header;
import clostrue.hardcodes.file.HeaderPart;
import clostrue.hardcodes.file.Name;
import clostrue.model.SimModel;
import clostrue.model.mam.MutationAdvantageData;
import clostrue.model.mam.RegionTossMap;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.sequencers.AccSeq4HMwithGenomePartKey;
import clostrue.sequencers.AccSeq4HMwithIntKey;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Implements one single Survivors Analysis Task.
 * 
 * Handles one task of survivors analysis which
 * Can be parallelized as the work is on different
 * Already prepared objects.
 * This is mainly file generation
 * 
 * @author Krzysztof Szymiczek
 */ 
public class GenealogyAnalysisTask extends SwingWorker<Void,Void> {

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
        
    private final GenealogyAnalysis                 _sa;               //calling object
    private final GenealogyAnalysisTaskWorkToDo     _workToDo;              //the ID of TaskToDo
    private final int                                  iteration;
    private final AtomicInteger                        _notProcessedCounter;
    SimModel   model;
    GenomeCollection genomes;
    CellCollection cellCollection;
    Simulation simulation;
    Analytics analytics;
    
    /**
     * Default constructor
     * @param sa
     * @param inWorkToDo
     * @param tasksToFinish
     */
    public GenealogyAnalysisTask(GenealogyAnalysis sa, GenealogyAnalysisTaskWorkToDo inWorkToDo, AtomicInteger tasksToFinish) {
        _workToDo                           = inWorkToDo;              
        _notProcessedCounter                = tasksToFinish;
        _notProcessedCounter.incrementAndGet();
        _sa                                = sa;
        analytics = _sa.getAnalytics();
        simulation = _sa.getAnalytics().getSim();
        iteration                          = _sa.iteration;
        model                               = _sa.getSimModel();
        genomes = _sa.getGenomes();
        cellCollection = analytics.getCellCollection();
        
        equalShadowDriverPart               = sa.getAnalytics().getEqualShadowDriverPart();
        equalShadowPassengerPart            = sa.getAnalytics().getEqualShadowPassengerPart();
        equalSurvivorsDriverPart            = sa.getAnalytics().getEqualSurvivorsDriverPart();
        equalSurvivorsPassengerPart         = sa.getAnalytics().getEqualSurvivorsPassengerPart();
        equalSurvivorsSinglePassengerLocus  = sa.getAnalytics().equalSurvivorsSinglePassengerLocus;  
        equalSurvivorsSingleDriverLocus     = sa.getAnalytics().equalSurvivorsSingleDriverLocus; 
        equalSurvivorsSinglePassengerGene   = sa.getAnalytics().equalSurvivorsSinglePassengerGene; 
        equalSurvivorsSingleDriverGene      = sa.getAnalytics().equalSurvivorsSingleDriverGene;        
        equalShadowSinglePassengerLocus     = sa.getAnalytics().equalShadowSinglePassengerLocus; 
        equalShadowSingleDriverLocus        = sa.getAnalytics().equalShadowSingleDriverLocus; 
        equalShadowSinglePassengerGene      = sa.getAnalytics().equalShadowSinglePassengerGene; 
        equalShadowSingleDriverGene         = sa.getAnalytics().equalShadowSingleDriverGene; 
    }
            
    /**
     * Starts the step to be done
     * 
     * @return nothing
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @SuppressWarnings("empty-statement")
    public Void executeToDo() throws IOException, InterruptedException, ExecutionException{         

        doInBackground();
        return null;        
    
    }
    
    /**
     * Starts the step to be done in background
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Override    
    public Void doInBackground()
        throws InterruptedException, ExecutionException, IOException {
        
        switch (_workToDo){
            case saCreateSurvivorsFileForIdenticalDriverGenome:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverGenome, Activity.started);        
                createSurvivorsFileForIdenticalDriverGenome();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverGenome, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateSurvivorsFileForIdenticalPassengerGenome:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerGenome, Activity.started);        
                createSurvivorsFileForIdenticalPassengerGenome();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerGenome, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();                
                break;
            case saCreateShadowsFileForIdenticalDriverGenome:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateShadowsFileForIdenticalDriverGenome, Activity.started);        
                createShadowsFileForIdenticalDriverGenome();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateShadowsFileForIdenticalDriverGenome, Activity.finished);                 
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateShadowsFileForIdenticalPassangerGenome: 
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateShadowsFileForIdenticalPassangerGenome, Activity.started);        
                createShadowsFileForIdenticalPassangerGenome();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateShadowsFileForIdenticalPassangerGenome, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateSurvivorsFileForIdenticalDriverGene:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverGene, Activity.started);        
                createSurvivorsFileForIdenticalDriverGene();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverGene, Activity.finished);                 
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateSurvivorsFileForIdenticalPassengerGene:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerGene, Activity.started);        
                createSurvivorsFileForIdenticalPassengerGene();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerGene, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateSurvivorsFileForIdenticalDriverLocus:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverLocus, Activity.started);        
                createSurvivorsFileForIdenticalDriverLocus();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalDriverLocus, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateSurvivorsFileForIdenticalPassengerLocus:             
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerLocus, Activity.started);        
                createSurvivorsFileForIdenticalPassengerLocus();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateSurvivorsFileForIdenticalPassengerLocus, Activity.finished);                 
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateFileDriversPerCycle:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFileDriversPerCycle, Activity.started);        
                createFileDriversPerCycle();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFileDriversPerCycle, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateFilePassengersPerCycle:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFilePassengersPerCycle, Activity.started);        
                createFilePassengersPerCycle();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFilePassengersPerCycle, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateQuotedClonesFile:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedClonesFile, Activity.started);        
                createQuotedClonesFile();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedClonesFile, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateQuotedDriversFile:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedDriversFile, Activity.started);        
                createQuotedDriversFile();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedDriversFile, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateQuotedPassengersFile:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedPassengersFile, Activity.started);        
                createQuotedPassengersFile();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateQuotedPassengersFile, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateFileDriversPerPopSize:
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFileDriversPerPopSize, Activity.started);        
                createFileDriversPerPopSize();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFileDriversPerPopSize, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
            case saCreateFilePassengersPerPopSize:            
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFilePassengersPerPopSize, Activity.started);        
                createFilePassengersPerPopSize();
                StaticConsoleLogger.logActivity(iteration, Activity.saCreateFilePassengersPerPopSize, Activity.finished);                  
                _notProcessedCounter.decrementAndGet();
                break;
        }
        return null;
            
    }

    private void createSurvivorsFileForIdenticalDriverGenome() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalDriverGenomePart
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        asLogGroupWithIdenticalGenomeRegion(fileName, 
                Constant.drivers, 
                model.getModParams().getMAM().getRegionMapDrivers(), 
                model.getModParams().getMAM().getDrivers(), 
                bufferedWriter, 
                equalSurvivorsDriverPart);
        bufferedWriter.flush();
        bufferedWriter.close();        
    }
    
    private void createShadowsFileForIdenticalDriverGenome() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snShadowClonesIdenticalDriverGenomePart 
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        asLogGroupWithIdenticalGenomeRegion(fileName, 
                Constant.drivers, 
                model.getModParams().getMAM().getRegionMapDrivers(), 
                model.getModParams().getMAM().getDrivers(), 
                bufferedWriter, 
                equalShadowDriverPart);
        bufferedWriter.flush();
        bufferedWriter.close();           
    }
        
    private void createShadowsFileForIdenticalPassangerGenome() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snShadowClonesIdenticalPassangerGenomePart 
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        asLogGroupWithIdenticalGenomeRegion(fileName, 
                Constant.passengers, 
                model.getModParams().getMAM().getRegionMapPassengers(), 
                model.getModParams().getMAM().getPassengers(), 
                bufferedWriter, 
                equalShadowPassengerPart);
        bufferedWriter.flush();
        bufferedWriter.close();           
    }
    
    private void createSurvivorsFileForIdenticalPassengerGenome() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalPassengerGenomePart
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        asLogGroupWithIdenticalGenomeRegion(fileName,
                Constant.passengers, 
                model.getModParams().getMAM().getRegionMapPassengers(), 
                model.getModParams().getMAM().getPassengers(),
                bufferedWriter, 
                equalSurvivorsPassengerPart);                
        bufferedWriter.flush();
        bufferedWriter.close();        
    }
    
    private void createSurvivorsFileForIdenticalDriverGene() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalDriverGene
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        addHeader(bufferedWriter);
               
        for (int i = 0; i < equalSurvivorsSingleDriverGene.length; i++){
            asLogGroupWithIdenticalMutationOnGene(fileName,
                    model.getModParams().getMAM().getDriverTags()[i], 
                    Constant.drivers, 
                    model.getModParams().getMAM().getDrivers(), 
                    bufferedWriter, 
                    equalSurvivorsSingleDriverGene[i]);                                
        }
        
        addFooter(bufferedWriter);
        bufferedWriter.flush();
        bufferedWriter.close();        
    }
    
    private void createSurvivorsFileForIdenticalPassengerGene() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalPassengerGene
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        for (int i = 0; i < equalSurvivorsSinglePassengerGene.length; i++){
            asLogGroupWithIdenticalMutationOnGene(fileName,
                    model.getModParams().getMAM().getPassengerTags()[i], 
                    Constant.passengers, 
                    model.getModParams().getMAM().getPassengers(), 
                    bufferedWriter, 
                    equalSurvivorsSinglePassengerGene[i]);                                
        }        
                       
        bufferedWriter.flush();
        bufferedWriter.close();        
    }
    
    private void createSurvivorsFileForIdenticalDriverLocus() throws IOException{
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalDriverLocus
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);

        for (int i = 0; i < equalSurvivorsSingleDriverLocus.length; i++){
            asLogGroupWithIdenticalMutationOnLocus(fileName,
                    model.getModParams().getMAM().getDriverTags()[i], 
                    Constant.drivers, 
                    model.getModParams().getMAM().getRegionMapDrivers(), 
                    model.getModParams().getMAM().getDrivers(), 
                    bufferedWriter, 
                    equalSurvivorsSingleDriverLocus[i]);                                
        }        

        bufferedWriter.flush();
        bufferedWriter.close();        
    }

    private void createSurvivorsFileForIdenticalPassengerLocus() throws IOException{    
        String fileName = model.getFilePaths().getWorkDirTextAnalytics() 
                + java.io.File.separator 
                + Name.snSurvivorsIdenticalPassengerLocus
                + Extension.dotTxt;            
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
  
        for (int i = 0; i < equalSurvivorsSinglePassengerLocus.length; i++){
            asLogGroupWithIdenticalMutationOnLocus(fileName,
                    model.getModParams().getMAM().getPassengerTags()[i], 
                    Constant.passengers, 
                    model.getModParams().getMAM().getRegionMapPassengers(), 
                    model.getModParams().getMAM().getPassengers(), 
                    bufferedWriter, 
                    equalSurvivorsSinglePassengerLocus[i]);                                
        }            
        
        bufferedWriter.flush();
        bufferedWriter.close();
   }

    private void createQuotedClonesFile() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.quotedClones
                + Extension.dotCsv;
                
        StaticConsoleLogger.logActivity(iteration, Activity.saveClonesPerPopSizeToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillQuotedClonesFileContent(bufferedWriter, equalShadowDriverPart);
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveClonesPerPopSizeToCSV + fileName, Activity.finished);
    }

    private void createQuotedDriversFile() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.quotedDrivers
                + Extension.dotCsv;
                
        StaticConsoleLogger.logActivity(iteration, Activity.saveQuotedDriversToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillQuotedDriversFileContent(bufferedWriter, equalShadowDriverPart);       
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveQuotedDriversToCSV + fileName, Activity.finished);
    }

    private void createQuotedPassengersFile() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.quotedPassengers
                + Extension.dotCsv;
                
        StaticConsoleLogger.logActivity(iteration, Activity.saveQuotedPassengersToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillQuotedPassengersFileContent(bufferedWriter, equalShadowPassengerPart);       
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveQuotedPassengersToCSV + fileName, Activity.finished);
    }

    private void createFileDriversPerPopSize() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.driversPerPopSize
                + Extension.dotCsv;
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveDriversPerPopSizeToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillDriversPerPopSizeFileContent(bufferedWriter, equalShadowDriverPart);
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveDriversPerPopSizeToCSV + fileName, Activity.finished);
    }
    
    private void fillDriversPerPopSizeFileContent(BufferedWriter bufferedWriter, 
                                   HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {
        ArrayList<Integer> cycles = new ArrayList<> ();
        
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }       
        Collections.sort(cycles);
        
        addDriversPerPopSizeHeader(bufferedWriter);
        
        AtomicIntegerArray popSize = simulation.getStatistics().getHistoryCellCountN();
        
        for (Integer cycle : cycles){
            
            List<Integer> withDuplicates = new ArrayList<Integer>();
            HashMap<GenomePart, Integer> blockMap = new HashMap<>();
            for (Map.Entry<GenomePart, CellIndexHolder> e : equalGenomePart.get(cycle).entrySet()) {                    
                for(int m : e.getKey().getDrivMutationsWithBlockMap(genomes, blockMap)) {
                    withDuplicates.add(m);
                }
            }                
               
            Set<Integer> withoutDuplicates = new LinkedHashSet<Integer>(withDuplicates);
                    
            try {
                bufferedWriter.write(String.format("%d;%d;%d;%d", popSize.get(cycle), withoutDuplicates.size(), iteration, cycle));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }

    private void addDriversPerPopSizeHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.driversPerPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.outCSVeol);
    }
    
    private void createFilePassengersPerPopSize() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.passengersPerPopSize
                + Extension.dotCsv;
        
        StaticConsoleLogger.logActivity(iteration, Activity.savePassengersPerPopSizeToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillPassengersPerPopSizeFileContent(bufferedWriter, equalShadowPassengerPart);
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.savePassengersPerPopSizeToCSV + fileName, Activity.finished);
    }
    
    private void fillPassengersPerPopSizeFileContent(BufferedWriter bufferedWriter, 
                                   HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {
        
        ArrayList<Integer> cycles = new ArrayList<> ();
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }       
        Collections.sort(cycles);
        
        addPassengersPerPopSizeHeader(bufferedWriter);
        
        AtomicIntegerArray popSize = simulation.getStatistics().getHistoryCellCountN();
        
        for (Integer cycle : cycles ){
            
            List<Integer> withDuplicates = new ArrayList<Integer>();
            HashMap<GenomePart, Integer> blockMap = new HashMap<>();
            for (Map.Entry<GenomePart, CellIndexHolder> e : equalGenomePart.get(cycle).entrySet()) {                    
                for(int m : e.getKey().getPassMutationsWithBlockMap(genomes, blockMap)) {
                    withDuplicates.add(m);
                }
            }                                       
            Set<Integer> withoutDuplicates = new LinkedHashSet<Integer>(withDuplicates);
            
            try {
                bufferedWriter.write(String.format("%d;%d;%d;%d", popSize.get(cycle), withoutDuplicates.size(), iteration, cycle));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void addPassengersPerPopSizeHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.passengersPerPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.outCSVeol);
    }

    private void createFileDriversPerCycle() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.driversPerCycleFileName
                + Extension.dotCsv;
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveDriversPerCycleToCSV + fileName, Activity.started);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillDriversPerCycleFileContent(bufferedWriter, equalShadowDriverPart);
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveDriversPerCycleToCSV + fileName, Activity.finished);
    }
    
    private void fillDriversPerCycleFileContent(BufferedWriter bufferedWriter, 
                                   HashMap<Integer,HashMap<GenomePart,CellIndexHolder>> equalGenomePart) throws IOException {
        
        ArrayList<Integer> cycles = new ArrayList<> ();
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }       
        Collections.sort(cycles);
        
        addDriversPerCycleHeader(bufferedWriter);
        
        for (Integer cycle : cycles){
            try {
                List<Integer> withDuplicates = new ArrayList<>();
                HashMap<GenomePart, Integer> blockMap = new HashMap<>();
                for (Map.Entry<GenomePart, CellIndexHolder> e : equalGenomePart.get(cycle).entrySet()) {                    
                    for(int m : e.getKey().getDrivMutationsWithBlockMap(genomes, blockMap)) {
                        withDuplicates.add(m);
                    }
                }

                Set<Integer> withoutDuplicates = new LinkedHashSet<>(withDuplicates);
                              
                bufferedWriter.write(String.format("%d;%d", cycle, withoutDuplicates.size()));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void addDriversPerCycleHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.driversPerCycleCSVFileHeader);
        bufferedWriter.write(Artifact.outCSVeol);
    }
    
    private void createFilePassengersPerCycle() throws IOException {
        String fileName = model.getFilePaths().getWorkDirTechOutput() 
                + java.io.File.separator 
                + Name.passengersPerCycleFileName
                + Extension.dotCsv;
        
        StaticConsoleLogger.logActivity(iteration, Activity.savePassengersPerCycleToCSV + fileName, Activity.started);
       
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        fillPassengersPerCycleFlieContent(bufferedWriter, equalShadowPassengerPart);
        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
       StaticConsoleLogger.logActivity(iteration, Activity.savePassengersPerCycleToCSV + fileName, Activity.finished);
    }
    
    private void fillPassengersPerCycleFlieContent(BufferedWriter bufferedWriter, 
                               HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {

        ArrayList<Integer> cycles = new ArrayList<> ();
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }       
        Collections.sort(cycles);
        
        addPassengersPerCycleHeader(bufferedWriter);
        
        for (Integer cycle : cycles){
            List<Integer>                withDuplicates = new ArrayList<>();
            HashMap<GenomePart, Integer> blockMap       = new HashMap<>();
           
            for (Map.Entry<GenomePart, CellIndexHolder> e : equalGenomePart.get(cycle).entrySet()) {                    
                for(int m : e.getKey().getPassMutationsWithBlockMap(genomes, blockMap)) {
                    withDuplicates.add(m);
                }
            }
                
            Set<Integer> withoutDuplicates = new LinkedHashSet<>(withDuplicates);
                        
            try {     
                bufferedWriter.write(String.format("%d;%d", cycle, withoutDuplicates.size()));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        };        
    }
    
    private void addPassengersPerCycleHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.passengersPerCycleCSVFileHeader);
        bufferedWriter.write(Artifact.outCSVeol);
    }

   private void fillQuotedPassengersFileContent(BufferedWriter bufferedWriter, 
                             HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {

       ArrayList<Integer> cycles = new ArrayList<> ();
              
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }              
        Collections.sort(cycles);
               
        AtomicIntegerArray popSize = simulation.getStatistics().getHistoryCellCountN();       
        int quotasCount = analytics.getQuotasCount();
            
        List<String[]>        mutationsListHavingMinQuotas = new ArrayList<>();        
        List<Integer[]>       mutationsCountHavingMinQuotas = new ArrayList<>();
        
// SPlit cycles to the amount of parallel tasks
        int taskCount = model.getTechParams().getProcTasksCount();
        ArrayList<Integer>[] cyclesPerTask = new ArrayList[taskCount];
        for (int i = 0; i < taskCount; i++)
            cyclesPerTask[i] = new ArrayList<>();
        for (Integer cycle : cycles){
            String[] st = new String[quotasCount];
            for(int i = 0; i < st.length; i++)
                st[i] = ".";
            mutationsListHavingMinQuotas.add(st);
            Integer[] it = new Integer[quotasCount];
            for(int i = 0; i < it.length; i++)
                it[i] = 0;
            mutationsCountHavingMinQuotas.add(it);
            cyclesPerTask[cycle  % taskCount].add(cycle);            
        }

// Create Tasks
        ExecutorService threadPool = Executors.newFixedThreadPool(taskCount);
        List<QuotedGenesTask> tasks = new ArrayList<>();              //All the tasks to be executed        
        AtomicInteger tasksToFinish = new AtomicInteger(0);    
        for (int taskId = 0; taskId < taskCount; taskId++){
            tasks.add(new  QuotedGenesTask(
                    taskId,
                    analytics,
                    QuotedGenesTaskWorkToDo.passengers, 
                    tasksToFinish, 
                    cyclesPerTask[taskId],
                    popSize,
                    equalGenomePart,
                    mutationsListHavingMinQuotas,        
                    mutationsCountHavingMinQuotas
            ));
        }
// Execute Tasks
        if (!tasks.isEmpty()){
            tasks.stream().forEach((task) -> {
                threadPool.submit(task);
            });
            int prevTasksToFinish = 0;
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MutationTypeAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }

                int curTasksToFinish = tasksToFinish.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.parallelMtaQuotedPassengers, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }                
            } while (tasksToFinish.get() != 0);
            
        threadPool.shutdown();
        }
        StaticConsoleLogger.logActivity(iteration, Activity.fileDumpingMtaQuotedPassengers, Activity.started);
         
        //      Dump the data to file
        addQuotedPassengersFileHeader(bufferedWriter);
        String recordPattern = getRecordPattern();
//      String recordPattern = getRecordPattern2();  //to slow / to big
        for( Integer cycle : cycles){          
                        
            try {
                List<Object> args = new ArrayList<Object>();
                args.add(popSize.get(cycle));
                args.add(equalGenomePart.get(cycle).size());
                args.add(iteration);
                args.add(cycle);
                if (cycle < mutationsCountHavingMinQuotas.size())
                    for (int i = 0; i < quotasCount; i++) 
                        args.add(mutationsCountHavingMinQuotas.get(cycle)[i]);
                else
                    for (int i = 0; i < quotasCount; i++) 
                        args.add(0);
//to slow / to big begin                
//                if (cycle < mutationsListHavingMinQuotas.size())                
//                    for (int i = 0; i < quotasCount; i++) 
//                        args.add(mutationsListHavingMinQuotas.get(cycle)[i]); 
//                else
//                    for (int i = 0; i < quotasCount; i++) 
//                        args.add(".");
//to slow / to big end
                bufferedWriter.write(
                    String.format(
                        recordPattern,
                        args.toArray()                    
                    ));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        StaticConsoleLogger.logActivity(iteration, Activity.fileDumpingMtaQuotedPassengers, Activity.finished);
        
    }   

    private void addQuotedClonesFileHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.clonesPerPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.csvColumnSeparator);

        addHeaderQuotaPart(bufferedWriter, 
                HeaderPart.rsaCsvCHMQClonesHeader, 
                Artifact.csvColumnSeparator);
        
        bufferedWriter.write(Artifact.outCSVeol);
    }   

    private void addHeaderQuotaPart(BufferedWriter bufferedWriter, String pretext, String sep){

        int quotasCount = analytics.getQuotasCount();
        double[] quotaTreshold = analytics.getQuotaTreshold();
        
        try {
            for (int i = 0; i < quotasCount; i++)
                bufferedWriter.write(pretext + quotaTreshold[i] + sep);
        } catch (IOException ex) {
            Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void addQuotedDriversFileHeader(BufferedWriter bufferedWriter) throws IOException {
        
        bufferedWriter.write(Header.driversPerPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.csvColumnSeparator);
        
        addHeaderQuotaPart(bufferedWriter, 
                HeaderPart.rsaCsvCHMQDriversCountHeader, 
                Artifact.csvColumnSeparator);

        addHeaderQuotaPart(bufferedWriter, 
                HeaderPart.rsaCsvCHMQDriversListHeader, 
                Artifact.csvColumnSeparator);     
        
        bufferedWriter.write(Artifact.outCSVeol);
    }    

    private void addQuotedPassengersFileHeader(BufferedWriter bufferedWriter) throws IOException {
        
        bufferedWriter.write(Header.passengersPerPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.csvColumnSeparator);
        
        addHeaderQuotaPart(bufferedWriter, 
                HeaderPart.rsaCsvCHMQPassengersCountHeader, 
                Artifact.csvColumnSeparator);
        
        bufferedWriter.write(Artifact.outCSVeol);
    }

    private String getRecordPattern(){
        String recordPatternTmp = "%d;%d;%d;%d";
        int quotasCount = analytics.getQuotasCount();
        for (int i = 0; i < quotasCount; i++)
            recordPatternTmp += ";%d";
        return recordPatternTmp;
    }
 
   private String getRecordPattern2(){
        String recordPatternTmp = "%d;%d;%d;%d";
        int quotasCount = analytics.getQuotasCount();
        for (int i = 0; i < quotasCount; i++)
            recordPatternTmp += ";%d";
        for (int i = 0; i < quotasCount; i++)
            recordPatternTmp += ";%s";
        return recordPatternTmp;
    }    

   private void fillQuotedDriversFileContent(BufferedWriter bufferedWriter, 
                                HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {

       ArrayList<Integer> cycles = new ArrayList<> ();
              
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }              
        Collections.sort(cycles);
              
        AtomicIntegerArray popSize = simulation.getStatistics().getHistoryCellCountN();       
        int quotasCount = analytics.getQuotasCount();
            
        List<String[]>        mutationsListHavingMinQuotas = new ArrayList<>();        
        List<Integer[]>       mutationsCountHavingMinQuotas = new ArrayList<>();
        
// SPlit cycles to the amount of parallel tasks
        int taskCount = model.getTechParams().getProcTasksCount();
        ArrayList<Integer>[] cyclesPerTask = new ArrayList[taskCount];
        for (int i = 0; i < taskCount; i++)
            cyclesPerTask[i] = new ArrayList<>();
        for (Integer cycle : cycles){
            String[] st = new String[quotasCount];
            for(int i = 0; i < st.length; i++)
                st[i] = ".";
            mutationsListHavingMinQuotas.add(st);
            Integer[] it = new Integer[quotasCount];
            for(int i = 0; i < it.length; i++)
                it[i] = 0;
            mutationsCountHavingMinQuotas.add(it);
            cyclesPerTask[cycle  % taskCount].add(cycle);            
        }

// Create Tasks
        ExecutorService threadPool = Executors.newFixedThreadPool(taskCount);
        List<QuotedGenesTask> tasks = new ArrayList<>();              //All the tasks to be executed        
        AtomicInteger tasksToFinish = new AtomicInteger(0);    
        for (int taskId = 0; taskId < taskCount; taskId++){
            tasks.add(new  QuotedGenesTask(
                    taskId,
                    analytics,
                    QuotedGenesTaskWorkToDo.drivers, 
                    tasksToFinish, 
                    cyclesPerTask[taskId],
                    popSize,
                    equalGenomePart,
                    mutationsListHavingMinQuotas,        
                    mutationsCountHavingMinQuotas
            ));
        }
// Execute Tasks
        if (!tasks.isEmpty()){
            tasks.stream().forEach((task) -> {
                threadPool.submit(task);
            });
            int prevTasksToFinish = 0;
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MutationTypeAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }

                int curTasksToFinish = tasksToFinish.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.parallelMtaQuotedDrivers, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }                
            } while (tasksToFinish.get() != 0);
            
        threadPool.shutdown();
        }
        StaticConsoleLogger.logActivity(iteration, Activity.fileDumpingMtaQuotedDrivers, Activity.started);
        
        //      Dump the data to file
        addQuotedDriversFileHeader(bufferedWriter);
        String recordPattern = getRecordPattern2();
        for( Integer cycle : cycles){
                                   
            try {
                List<Object> args = new ArrayList<>();
                args.add(popSize.get(cycle));
                args.add(equalGenomePart.get(cycle).size());
                args.add(iteration);
                args.add(cycle);
                if ( cycle < mutationsCountHavingMinQuotas.size())
                    for (int i = 0; i < quotasCount; i++) 
                        args.add(mutationsCountHavingMinQuotas.get(cycle)[i]);
                else
                    for (int i = 0; i < quotasCount; i++)
                        args.add(0);
                if ( cycle < mutationsListHavingMinQuotas.size())
                    for (int i = 0; i < quotasCount; i++) 
                        args.add(mutationsListHavingMinQuotas.get(cycle)[i]);
                else
                    for (int i = 0; i < quotasCount; i++)
                        args.add(".");
                bufferedWriter.write(
                    String.format(
                        recordPattern,
                        args.toArray()                    
                    ));
                bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        StaticConsoleLogger.logActivity(iteration, Activity.fileDumpingMtaQuotedDrivers, Activity.finished);
        
    }       

    private void fillQuotedClonesFileContent(BufferedWriter bufferedWriter, 
                                   HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) throws IOException {
        
        ArrayList<Integer> cycles = new ArrayList<> ();
              
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }
        Collections.sort(cycles);
        
        addQuotedClonesFileHeader(bufferedWriter);
        
        AtomicIntegerArray popSize = simulation.getStatistics().getHistoryCellCountN();
        
        int quotasCount         = analytics.getQuotasCount();
        double[] quotaTreshold = analytics.getQuotaTreshold();
        String recordPattern    = getRecordPattern();
        
        for (Integer cycle : cycles){
            
            int cloneCountHavingMinQuotas[] = new int[quotasCount];
            
            for(Map.Entry<GenomePart, CellIndexHolder> e : equalGenomePart.get(cycle).entrySet()) {
                
                double quota = (double)e.getValue().size() / (double)popSize.get(cycle);
                
                for (int quotaTresholdId = 0; quotaTresholdId < quotasCount; quotaTresholdId++)
                    if (quota >= quotaTreshold[quotaTresholdId])
                        cloneCountHavingMinQuotas[quotaTresholdId]++;

            }
            
            try {
                List<Object> args = new ArrayList<Object>();
                args.add(popSize.get(cycle));
                args.add(equalGenomePart.get(cycle).size());
                args.add(iteration);
                args.add(cycle);
                for (int i = 0; i < quotasCount; i++) 
                    args.add(cloneCountHavingMinQuotas[i]);
                bufferedWriter.write(
                    String.format(
                        recordPattern,
                        args.toArray()                    
                    ));
                        bufferedWriter.write(Artifact.outCSVeol);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Part of the Analyze Survivors functionality. Fills the output file for Analyze Survivors
     * with the information of the cells which share the same exact set of mutation in the given
     * region of the genome
     * @param groupName         text for concatenating in the file - "Drivers" or "Passengers"
     * @param rtm               Region toss map
     * @param madList           List of Mutation Advantage Data
     * @param fileWriter        Hook for file writer which saves the Analyze Survivors
     * @param equalGenomePart   The ConcurrentHashMap with data
     * @param maxPart           The size of the largest cell group in the ConcurrentHashMap
     * @throws IOException 
     */
    private void asLogGroupWithIdenticalGenomeRegion(
            String fileName, 
            String groupName, 
            RegionTossMap rtm, 
            MutationAdvantageData madTable[], 
            BufferedWriter bufferedWriter, 
            HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> equalGenomePart) 
            throws IOException{
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.started);       
        addHeader(bufferedWriter);
        
        ArrayList<Integer> cycles = new ArrayList<> ();
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : equalGenomePart.entrySet()){
            cycles.add(entry.getKey());           
        }       
        Collections.sort(cycles);

        for (Integer cycle : cycles){
            try {
                asLogGroupWithIdenticalGenomeRegionDeep(
                        groupName, 
                        rtm, 
                        madTable, 
                        bufferedWriter, 
                        equalGenomePart.get(cycle),
                        cycle);
            } catch (IOException ex) {
                Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
            }
        };

        addFooter(bufferedWriter);        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.finished);
    }

    /**
     * Part of the Analyze Survivors functionality. Fills the output file for Analyze Survivors
     * with the information of the cells which share the same exact set of mutation in the given
     * region of the genome
     * @param groupName         text for concatenating in the file - "Drivers" or "Passengers"
     * @param rtm               Region toss map
     * @param madList           List of Mutation Advantage Data
     * @param fileWriter        Hook for file writer which saves the Analyze Survivors
     * @param equalGenomePart   The ConcurrentHashMap with data
     * @param maxPart           The size of the largest cell group in the ConcurrentHashMap
     * @throws IOException 
     */
    private void asLogGroupWithIdenticalGenomeRegionDeep( 
            String groupName, 
            RegionTossMap rtm, 
            MutationAdvantageData[] madTable, 
            BufferedWriter bufferedWriter, 
            HashMap<GenomePart, CellIndexHolder> equalGenomePart,
            Integer cycle) 
            throws IOException{
        
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Content.sectionBeginSeparator 
                + String.format(Content.cycle,cycle)
                + String.valueOf(equalGenomePart.size()) 
                + Content.cellGroups 
                + groupName 
                + Content.genomeMutations
                + Content.sectionEndSeparator);
        bufferedWriter.write(Artifact.outCSVeol);

        AccSeq4HMwithGenomePartKey sequence = new AccSeq4HMwithGenomePartKey();
        for(Map.Entry<GenomePart, CellIndexHolder> entry : equalGenomePart.entrySet()){
            sequence.addNewEntry(
                    entry.getKey(), 
                    entry.getValue().size(), 
                    cellCollection.getByIndex(entry.getValue().getFirst()));
        }
        sequence.sort();
        GenomePart nextKey;
        String cloneGroupID;
        MutationType mT;
        while((nextKey = sequence.getNextKey()) != null){
            CellIndexHolder cellGroup = equalGenomePart.get(nextKey);
            bufferedWriter.write(Artifact.outCSVeol);
            cloneGroupID = "";
            if ( Constant.drivers.equals(groupName) ){
                mT = MutationType.Driver;
                int driverCloneGroupID
                        = cellCollection.getByIndex(cellGroup.getFirst()).getGenome(genomes).getDriverCloneGroupID();
                cloneGroupID += String.format(Content.inCloneId, String.valueOf(driverCloneGroupID));
                int parentDriverCloneGroupID
                        = cellCollection.getByIndex(cellGroup.getFirst()).getGenome(genomes).getDrivers(genomes).getParentCloneGroupID();
                cloneGroupID += String.format(Content.parentCloneId, String.valueOf(parentDriverCloneGroupID)); 
            } else {
                mT = MutationType.Passenger;
            }
            bufferedWriter.write(String.format
                    ("%-6s ", String.valueOf(cellGroup.size())) + 
                    " cells " + 
                    cloneGroupID +
                    " share(s) exact same " + groupName + " mutations (including locus): ");                         
            String mutationsString = "";
            for (Integer mutation : nextKey.getMutations(mT, genomes)){
                int gene = rtm.getGeneBasedOnToss(mutation);
                mutationsString = madTable[gene].getGeneName()
                    + Artifact.geneSeparator + String.valueOf(mutation)
                    + Artifact.outCSVMutationSeparator
                    + mutationsString;
                }
                if (nextKey.getMutations(mT, genomes).isEmpty()){
                    mutationsString = "<< No " + groupName + " mutations. >>";                                                   
                }                
                bufferedWriter.write(mutationsString);
                bufferedWriter.write(Artifact.outCSVeol); 
                
        }
        
    }
    
    /**
     * Part of the Analyze Survivors functionality. Fills the output file for Analyze Survivors
     * with the information of the cells which share mutation in the same Gene in the given
     * region of the genome
     * @param geneTag       gene tagging (from MAM file)
     * @param groupName     text for concatenating in the file - "Drivers" or "Passengers"
     * @param madList       List of Mutation Advantage Data
     * @param fileWriter    Hook for file writer which saves the Analyze Survivors
     * @param equalGene     The ConcurrentHashMap with data
     * @param maxPart       The size of the largest cell group in the ConcurrentHashMap
     * @throws IOException 
     */
    private void asLogGroupWithIdenticalMutationOnGene(
            String fileName, 
            String geneTag, 
            String groupName, 
            MutationAdvantageData madTable[], 
            BufferedWriter bufferedWriter, 
            ConcurrentHashMap<Integer, 
            CellIndexHolder> equalGene)
            throws IOException{
    
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.started, "("+geneTag+") part");
                
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Content.sectionBeginSeparator 
                + String.valueOf(equalGene.size()) 
                + Content.cellGroups
                + groupName 
                + Content.genesNotLocusWise
                + geneTag
                + Content.sectionEndSeparator);
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Artifact.outCSVeol);
        
        AccSeq4HMwithIntKey sequence = new AccSeq4HMwithIntKey();
        for (Map.Entry<Integer,CellIndexHolder> entry : equalGene.entrySet()){
            sequence.addNewEntry(entry.getKey(), entry.getValue().size());
        }
        sequence.sort();
        Integer nextKey;
        while((nextKey = sequence.getNextKey()) != null){
            CellIndexHolder cellGroup = equalGene.get(nextKey);
            Integer gene = nextKey;              
            bufferedWriter.write(
                String.format("%-6s ", String.valueOf(cellGroup.size())) +
                Content.cellsHaveMutationIn + groupName + 
                Content.gene + madTable[gene].getGeneName() +
                Content.taggedAs + geneTag);
            bufferedWriter.write(Artifact.outCSVeol);   
        }
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.finished, "("+geneTag+") part");
    }

    /**
     * Part of the Analyze Survivors functionality. Fills the output file for Analyze Survivors
     * with the information of the cells which share mutation in the same Gene and Locus in the given
     * region of the genome
     * @param geneTag       gene tagging (from MAM file)
     * @param groupName     text for concatenating in the file - "Drivers" or "Passengers"
     * @param rtm           Region toss map
     * @param madList       List of Mutation Advantage Data
     * @param fileWriter    Hook for file writer which saves the Analyze Survivors
     * @param equalLocus    The ConcurrentHashMap with data
     * @param maxPart       The size of the largest cell group in the ConcurrentHashMap
     * @throws IOException 
     */
    private void asLogGroupWithIdenticalMutationOnLocus(
            String fileName, 
            String geneTag, 
            String groupName, 
            RegionTossMap rtm, 
            MutationAdvantageData[] madTable, 
            BufferedWriter bufferedWriter, 
            ConcurrentHashMap<Integer, 
            CellIndexHolder> equalLocus)
            throws IOException{
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.started, "("+geneTag+") part");
        
        addHeader(bufferedWriter);
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Content.sectionBeginSeparator
                + String.valueOf(equalLocus.size()) 
                + Content.cellGroups
                + groupName 
                + Content.genesLocusWise
                + geneTag 
                + Content.sectionEndSeparator);
        bufferedWriter.write(Artifact.outCSVeol);
        bufferedWriter.write(Artifact.outCSVeol);
        
        AccSeq4HMwithIntKey sequence = new AccSeq4HMwithIntKey();
        for (Map.Entry<Integer,CellIndexHolder> entry : equalLocus.entrySet()){
            sequence.addNewEntry(entry.getKey(), entry.getValue().size());
        }
        sequence.sort();
        Integer nextKey;
        while((nextKey = sequence.getNextKey()) != null){
            CellIndexHolder cellGroup = equalLocus.get(nextKey);
            int locus = nextKey;
            int gene = rtm.getGeneBasedOnToss(locus);              
            bufferedWriter.write(
                    String.format("%-6s ", String.valueOf(cellGroup.size())) +
                    Content.cellsHaveMutationIn + groupName + Content.geneAtLocus 
                            + madTable[gene].getGeneName() + Artifact.geneSeparator + String.valueOf(locus)
                            + Content.taggedAs + geneTag);
            bufferedWriter.write(Artifact.outCSVeol);   
        }
        
        addFooter(bufferedWriter);       
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fileName, Activity.finished, "("+geneTag+") part");
    }        

    public void addHeader(BufferedWriter writer){
        try {
            writer.write(Content.statisticsBegin);
            writer.write(Artifact.outCSVeol);
            writer.write(Artifact.outCSVeol);
        } catch (IOException ex) {
            Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addFooter(BufferedWriter writer){
        try {
            writer.write(Artifact.outCSVeol);
            writer.write(Artifact.outCSVeol);        
            writer.write(Content.statisticsEnd);
            writer.write(Artifact.outCSVeol);
        } catch (IOException ex) {
            Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
