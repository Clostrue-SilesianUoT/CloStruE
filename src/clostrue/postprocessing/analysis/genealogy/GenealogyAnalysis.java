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
 
import clostrue.postprocessing.analysis.Analytics;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import clostrue.Simulation;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Param;
import clostrue.model.SimModel;
import clostrue.collections.GenomeCollection;
import clostrue.enumerations.GenealogyAnalysisTaskWorkToDo;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.HashmapName;
import clostrue.toolbox.HashMapSizeTools;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
/**
 * Handles Survivors Analysis
 * 
 * @author Krzysztof Szymiczek
 */

public class GenealogyAnalysis {

    private final Simulation    simulation;
    private final SimModel      simModel;
    private final GenomeCollection genomes;
    private final  Analytics     analytics;
    public final int           iteration;  
    private ExecutorService threadPoolStep1;                 //the pool of all tasks
    private final List<GenealogyAnalysisTask> tasksStep1;              //All the tasks to be executed    

    public final AtomicInteger tasksToFinishStep1 = new AtomicInteger(0);

    /**
     * Default constructor
     * @param inAnalytics
     */    
    public GenealogyAnalysis(Analytics inAnalytics) {       
        
        this.analytics      = inAnalytics;
        this.simulation     = inAnalytics.getSim();
        this.iteration      = simulation.getIteration();
        this.tasksStep1          = new ArrayList<>();
        this.simModel       = new SimModel(iteration, simulation.getSimModel());
        this.genomes = analytics.getGenomes();
  
    }

    public GenomeCollection getGenomes() {
        return genomes;
    }

    public SimModel getSimModel() {
        return simModel;
    }

    public Analytics getAnalytics() {
        return analytics;
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
                    Logger.getLogger(GenealogyAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }

                int curTasksToFinish = tasksToFinishStep1.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.parallelSaFinish, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }                
            } while (tasksToFinishStep1.get() != 0);
            
        threadPoolStep1.shutdown();
        }       
    }
    
    public void handleStep1Tasks(){
               
        createStep1Tasks();
        executeStep1Tasks();       

    }           
    
    public void logHMSizes(){
        
        HashMapSizeTools.logHM1Size(analytics.getEqualShadowDriverPart(), HashmapName.equalShadowDriverPart);
        HashMapSizeTools.logHM1Size(analytics.getEqualShadowPassengerPart(), HashmapName.equalShadowPassengerPart);
        HashMapSizeTools.logHM1Size(analytics.getEqualSurvivorsDriverPart(), HashmapName.equalSurvivorsDriverPart);
        HashMapSizeTools.logHM1Size(analytics.getEqualSurvivorsPassengerPart(), HashmapName.equalSurvivorsPassengerPart);
        HashMapSizeTools.logHM2Size(analytics.equalSurvivorsSinglePassengerGene, HashmapName.equalSurvivorsSinglePassengerGene);
        HashMapSizeTools.logHM2Size(analytics.equalSurvivorsSingleDriverGene, HashmapName.equalSurvivorsSingleDriverGene);
        HashMapSizeTools.logHM2Size(analytics.equalShadowSinglePassengerGene, HashmapName.equalShadowSinglePassengerGene);
        HashMapSizeTools.logHM2Size(analytics.equalShadowSingleDriverGene, HashmapName.equalShadowSingleDriverGene);        

        if (Constant.heavyLocusAnalysis){
            HashMapSizeTools.logHM2Size(analytics.equalSurvivorsSinglePassengerLocus, HashmapName.equalSurvivorsSinglePassengerLocus);
            HashMapSizeTools.logHM2Size(analytics.equalSurvivorsSingleDriverLocus, HashmapName.equalSurvivorsSingleDriverLocus);
            HashMapSizeTools.logHM2Size(analytics.equalShadowSinglePassengerLocus, HashmapName.equalShadowSinglePassengerLocus);
            HashMapSizeTools.logHM2Size(analytics.equalShadowSingleDriverLocus, HashmapName.equalShadowSingleDriverLocus);            
        }
        
    }
    
    private void createStep1Tasks(){
    
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalDriverGenome, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalPassengerGenome, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalDriverGene, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalPassengerGene, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateFileDriversPerCycle, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateFilePassengersPerCycle, tasksToFinishStep1));

        if (Constant.heavyLocusAnalysis){
            tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateShadowsFileForIdenticalPassangerGenome, tasksToFinishStep1));
            tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalDriverLocus, tasksToFinishStep1));
            tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateSurvivorsFileForIdenticalPassengerLocus, tasksToFinishStep1));                                
        }
        
        if (simulation.getSettings().getBooleanValue(Param.cbClonesScatter))
          tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateQuotedClonesFile, tasksToFinishStep1));
        if (simulation.getSettings().getBooleanValue(Param.cbDriversSactter))
          tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateQuotedDriversFile, tasksToFinishStep1));
        if (simulation.getSettings().getBooleanValue(Param.cbPassengersScatter))
          tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateQuotedPassengersFile, tasksToFinishStep1));      

        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateFileDriversPerPopSize, tasksToFinishStep1));
        tasksStep1.add(new  GenealogyAnalysisTask(this, GenealogyAnalysisTaskWorkToDo.saCreateFilePassengersPerPopSize, tasksToFinishStep1));   

    }

    
       
}
