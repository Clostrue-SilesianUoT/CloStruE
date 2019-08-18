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
package clostrue.postprocessing.analysis.mutationtType;
 
import clostrue.postprocessing.analysis.Analytics;
import clostrue.postprocessing.plotter.PlotterTools;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import clostrue.Settings;
import clostrue.Simulation;
import clostrue.enumerations.MutationTypeAnalysisTaskWorkToDo;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.plots.Setting;
import clostrue.model.SimModel;
import clostrue.collections.CellIndexHolder;
import clostrue.hardcodes.HashmapName;
import clostrue.toolbox.HashMapSizeTools;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jfree.data.statistics.SimpleHistogramDataset;

/**
 * Handles Mutation Type Analysis
 * @author Krzysztof Szymiczek
 */

public class MutationTypeAnalysis {

    private final static boolean   DO_NOT_INSERT_ZEROS  = false;
    
    private final Simulation    simulation;
    private final SimModel      simModel;
    private final Settings      settings;
    private  Analytics     analytics;
    public final int           iteration;
  
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmSurvivorsMutTypeGeneDriver = null;      
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmShadowMutTypeGeneDriver = null;  
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmSurvivorsMutTypeGenePassenger = null;   
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmShadowMutTypeGenePassenger = null;   
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmSurvivorsMutTypeLocusDriver = null;     
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmShadowMutTypeLocusDriver = null;     
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmSurvivorsMutTypeLocusPassenger = null;  
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>                       chmShadowMutTypeLocusPassenger = null;          
    
    private ExecutorService threadPoolStep1;                 //the pool of all tasks
    private final List<MutationTypeAnalysisTask> tasksStep1;              //All the tasks to be executed    
    private ExecutorService threadPoolStep2;                 //the pool of all tasks
    private final List<MutationTypeAnalysisTask> tasksStep2;              //All the tasks to be executed    

    public final AtomicInteger tasksToFinishStep1 = new AtomicInteger(0);
    public final AtomicInteger tasksToFinishStep2 = new AtomicInteger(0);

    public Analytics getAnalytics() {
        return analytics;
    }  
    
    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }
    
    public void setChmSurvivorsMutTypeGeneDriver(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmSurvivorsMutTypeGeneDriver) {
        this.chmSurvivorsMutTypeGeneDriver = chmSurvivorsMutTypeGeneDriver;
    }

    public void setChmShadowMutTypeGeneDriver(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmShadowMutTypeGeneDriver) {
        this.chmShadowMutTypeGeneDriver = chmShadowMutTypeGeneDriver;
    }

    public void setChmSurvivorsMutTypeGenePassenger(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmSurvivorsMutTypeGenePassenger) {
        this.chmSurvivorsMutTypeGenePassenger = chmSurvivorsMutTypeGenePassenger;
    }

    public void setChmShadowMutTypeGenePassenger(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmShadowMutTypeGenePassenger) {
        this.chmShadowMutTypeGenePassenger = chmShadowMutTypeGenePassenger;
    }

    public void setChmSurvivorsMutTypeLocusDriver(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmSurvivorsMutTypeLocusDriver) {
        this.chmSurvivorsMutTypeLocusDriver = chmSurvivorsMutTypeLocusDriver;
    }

    public void setChmShadowMutTypeLocusDriver(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmShadowMutTypeLocusDriver) {
        this.chmShadowMutTypeLocusDriver = chmShadowMutTypeLocusDriver;
    }

    public void setChmSurvivorsMutTypeLocusPassenger(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmSurvivorsMutTypeLocusPassenger) {
        this.chmSurvivorsMutTypeLocusPassenger = chmSurvivorsMutTypeLocusPassenger;
    }

    public void setChmShadowMutTypeLocusPassenger(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chmShadowMutTypeLocusPassenger) {
        this.chmShadowMutTypeLocusPassenger = chmShadowMutTypeLocusPassenger;
    }
    
    /**
     * Default constructor
     * @param inSimulation
     */    
    public MutationTypeAnalysis(Simulation inSimulation) {       
        
        this.simulation     = inSimulation;
        this.iteration      = simulation.getIteration();
        this.settings       = simulation.getSettings();
        this.analytics      = null;
        this.tasksStep1          = new ArrayList<>();
        this.tasksStep2          = new ArrayList<>();
        this.simModel       = new SimModel(iteration, simulation.getSimModel());
             
        chmSurvivorsMutTypeGeneDriver        = new ConcurrentHashMap<> ();
        chmShadowMutTypeGeneDriver        = new ConcurrentHashMap<> ();
        chmSurvivorsMutTypeGenePassenger     = new ConcurrentHashMap<> ();
        chmShadowMutTypeGenePassenger     = new ConcurrentHashMap<> ();
        chmSurvivorsMutTypeLocusDriver       = new ConcurrentHashMap<> ();
        chmShadowMutTypeLocusDriver       = new ConcurrentHashMap<> ();
        chmSurvivorsMutTypeLocusPassenger    = new ConcurrentHashMap<> ();
        chmShadowMutTypeLocusPassenger    = new ConcurrentHashMap<> ();                 
        
    }

    public SimModel getSimModel() {
        return simModel;
    }

    
    
    public void handleStep1Tasks(){
               
        createStep1Tasks();
        executeStep1Tasks();

    }           

    private void executeStep1Tasks(){
        if (!tasksStep1.isEmpty()){
            threadPoolStep1 = Executors.newFixedThreadPool(tasksStep1.size());
            tasksStep1.stream().forEach((task) -> {
                threadPoolStep1.submit(task);
            });
            int prevTasksToFinish = 0;
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MutationTypeAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }

                int curTasksToFinish = tasksToFinishStep1.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.parallelMtaStep1Finish, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }                
            } while (tasksToFinishStep1.get() != 0);
            
        threadPoolStep1.shutdown();
        }        
    }
    
    public void handleStep2Tasks(){
               
        createStep2Tasks();
        executeStep2Tasks();

    }           
    
    private void executeStep2Tasks(){
        if (!tasksStep2.isEmpty()){
            threadPoolStep2 = Executors.newFixedThreadPool(tasksStep2.size());
            tasksStep2.stream().forEach((task) -> {
                threadPoolStep2.submit(task);
            });  
            int prevTasksToFinish = 0;
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MutationTypeAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }
                int curTasksToFinish = tasksToFinishStep2.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.parallelMtaStep2Finish, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }
            } while (tasksToFinishStep2.get() != 0);
            
        threadPoolStep2.shutdown();
        }        
    }
    
    public void logHMSizes(){
        HashMapSizeTools.logHM3Size(chmShadowMutTypeGeneDriver, HashmapName.chmShadowMutTypeGeneDriver);
        HashMapSizeTools.logHM3Size(chmSurvivorsMutTypeGeneDriver, HashmapName.chmSurvivorsMutTypeGeneDriver);      
        HashMapSizeTools.logHM3Size(chmShadowMutTypeGeneDriver, HashmapName.chmShadowMutTypeGeneDriver);  
        HashMapSizeTools.logHM3Size(chmSurvivorsMutTypeGenePassenger, HashmapName.chmSurvivorsMutTypeGenePassenger);   
        HashMapSizeTools.logHM3Size(chmShadowMutTypeGenePassenger, HashmapName.chmShadowMutTypeGenePassenger);   
        HashMapSizeTools.logHM3Size(chmSurvivorsMutTypeLocusDriver, HashmapName.chmSurvivorsMutTypeLocusDriver);     
        HashMapSizeTools.logHM3Size(chmShadowMutTypeLocusDriver, HashmapName.chmShadowMutTypeLocusDriver);     
        HashMapSizeTools.logHM3Size(chmSurvivorsMutTypeLocusPassenger, HashmapName.chmSurvivorsMutTypeLocusPassenger);  
        HashMapSizeTools.logHM3Size(chmShadowMutTypeLocusPassenger, HashmapName.chmShadowMutTypeLocusPassenger);                  
    }
    
    private void createStep1Tasks(){
            
      tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikSurvivorsPassengerGene, tasksToFinishStep1));
      tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikSurvivorsDriverGene, tasksToFinishStep1));
      tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikShadowPassengerGene, tasksToFinishStep1));
      tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikShadowDriverGene, tasksToFinishStep1));

      if (Constant.heavyLocusAnalysis){
        tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikSurvivorsPassengerLocus, tasksToFinishStep1));
        tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikSurvivorsDriverLocus, tasksToFinishStep1));
        tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikShadowPassengerLocus, tasksToFinishStep1));
        tasksStep1.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaCclikShadowDriverLocus, tasksToFinishStep1));          
      }
      
    }

    private void createStep2Tasks(){
        
      tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaSurvivorsDriverGene, tasksToFinishStep2));
      tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaShadowDriverGene, tasksToFinishStep2));
      tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaSurvivorsPassengerGene, tasksToFinishStep2));
      tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaShadowPassengerGene, tasksToFinishStep2));
      
      if (Constant.heavyLocusAnalysis){
        tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaSurvivorsDriverLocus, tasksToFinishStep2));
        tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaShadowDriverLocus, tasksToFinishStep2));
        tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaSurvivorsPassengerLocus, tasksToFinishStep2));
        tasksStep2.add(new  MutationTypeAnalysisTask(this, MutationTypeAnalysisTaskWorkToDo.mtaAtaShadowPassengerLocus, tasksToFinishStep2));          
      }
      
    }


    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Driver Genes
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetSurvivorsMutTypeGeneDriver(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmSurvivorsMutTypeGeneDriver.get(geneTag),

                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));
        
    }         

    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Driver Genes
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetShadowMutTypeGeneDriver(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmShadowMutTypeGeneDriver.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));
        
    }          
    
    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Passenger Genes
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetSurvivorsMutTypeGenePassenger(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmSurvivorsMutTypeGenePassenger.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));

    }         

    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Passenger Genes
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetShadowMutTypeGenePassenger(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmShadowMutTypeGenePassenger.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));

    }       
    
    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Driver Locus
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetSurvivorsMutTypeLocusDriver(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmSurvivorsMutTypeLocusDriver.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));
        
    }         

    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Driver Locus
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetShadowMutTypeLocusDriver(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmShadowMutTypeLocusDriver.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));
        
    }     
    
    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Passenger Locus
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetSurvivorsMutTypeLocusPassenger(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmSurvivorsMutTypeLocusPassenger.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));

    }             

    /**
     * Creates the DataSerie for plot for Mutation Type Analysis for Passenger Locus
     * @param useGroupping
     * @param geneTag
     * @return 
     */
    public SimpleHistogramDataset getSimpleHistogramDatasetShadowMutTypeLocusPassenger(
            boolean useGroupping,
            String geneTag){
        
        return PlotterTools.getHistogramDatasetFromXYChartSeriesIntegerInteger(PlotterTools.convertCHMIIAsIntegerSerie(chmShadowMutTypeLocusPassenger.get(geneTag),
                    useGroupping,
                    Setting.teResAnalytics,
                    DO_NOT_INSERT_ZEROS,
                    settings));

    }      
    
    /**
     * Mutation type analysis for subset (single tag)
     * @param equalLocus Equal Mutation type hashMap
     * @return CHM for histogram for Mutation Types 
     */
    public ConcurrentHashMap<Long, Long> mutationTypeSingleTagAnalysis(
            ConcurrentHashMap<Integer, CellIndexHolder> equalLocus){
        
        ConcurrentHashMap<Long, Long> resultCHM = new ConcurrentHashMap<> ();
        
        //build histogram data
        int maxType = 0;
        for( Map.Entry<Integer, CellIndexHolder> entry : equalLocus.entrySet()){
            int size = entry.getValue().size();
            Long lSize = new Long(size);
            maxType = Math.max(maxType, size);
            Long count = resultCHM.get(lSize);
            if( count == null ){
                resultCHM.put(lSize, new Long(1));
            } else {
                count++;
                resultCHM.replace(lSize, count);
            }
        }

        return resultCHM;
            
    }        

    /**
     * Mutation type analysis for subset (single tag)
     * @param equalLocus Equal Mutation type hashMap
     * @return CHM for histogram for Mutation Types 
     */
    public ConcurrentHashMap<Integer, Integer> mutationTypeSingleTagAnalysisII(
            ConcurrentHashMap<Integer, CellIndexHolder> equalLocus){
        
        ConcurrentHashMap<Integer, Integer> resultCHM = new ConcurrentHashMap<> ();
        
        //build histogram data
        int maxType = 0;
        for( Map.Entry<Integer, CellIndexHolder> entry : equalLocus.entrySet()){
            int size = entry.getValue().size();
            Integer lSize = new Integer(size);
            maxType = Math.max(maxType, size);
            Integer count = resultCHM.get(lSize);
            if( count == null ){
                resultCHM.put(lSize, new Integer(1));
            } else {
                count++;
                resultCHM.replace(lSize, count);
            }
        }

        return resultCHM;
            
    }         
    
    /**
     * Mutation type analysis
     * @param equalLocus Equal Mutation type hashMap
     * @return CHM for histogram for Mutation Types 
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>> mutationTypeAllTagsAnalysis(
            ConcurrentHashMap<Integer, CellIndexHolder>[] equalLocus, String[] geneTags){
        
        ConcurrentHashMap<String, ConcurrentHashMap<Long, Long>> resultCHM = new ConcurrentHashMap<> ();
        
        for(int i = 0; i < equalLocus.length; i++){
            ConcurrentHashMap<Long, Long> resultCHMforSingleTag
                    = mutationTypeSingleTagAnalysis(equalLocus[i]);
            resultCHM.put(geneTags[i], resultCHMforSingleTag);
        }
        
        return resultCHM;
            
    }          

    /**
     * Mutation type analysis
     * @param equalLocus Equal Mutation type hashMap
     * @param geneTags
     * @return CHM for histogram for Mutation Types 
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> mutationTypeAllTagsAnalysisII(
            ConcurrentHashMap<Integer, CellIndexHolder>[] equalLocus, String[] geneTags){
        
        ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> resultCHM = new ConcurrentHashMap<> ();
        
        for(int i = 0; i < equalLocus.length; i++){
            ConcurrentHashMap<Integer, Integer> resultCHMforSingleTag
                    = mutationTypeSingleTagAnalysisII(equalLocus[i]);
            resultCHM.put(geneTags[i], resultCHMforSingleTag);
        }
        
        return resultCHM;
            
    }        
    
    /**
     * Part of the Analyze Survivors functionality. Removing groups containing
     * only one cell from ConcurrentHashMap (Indexed with Integer)
     * Duplicate removal also performed
     * @param cHM ConcurrentHashMap to modify
     */
    public void correctCellListsIntegerKeyed(ConcurrentHashMap<Integer, CellIndexHolder>[] cHM){

        for (int i = 0; i < cHM.length; i++){
            for (Map.Entry<Integer, CellIndexHolder> entryDeep : cHM[i].entrySet()){

                entryDeep.getValue().removeDuplicates();
                if (entryDeep.getValue().size() == 1){
                    cHM[i].remove(entryDeep.getKey());                
                }
            }
        }

    }
    
}
