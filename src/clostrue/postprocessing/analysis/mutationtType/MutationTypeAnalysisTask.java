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
 
import clostrue.enumerations.MutationTypeAnalysisTaskWorkToDo;
import clostrue.hardcodes.Activity;
import clostrue.collections.CellIndexHolder;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;

/**
 * Implements one single Mutation Type Analysis Task.
 * 
 * Handles one task of mutation type analysis which
 * Can be parallelized as the work is on different
 * Already prepared HashMaps
 * 
 * @author Krzysztof Szymiczek
 */ 
public class MutationTypeAnalysisTask extends SwingWorker<Void,Void> {
   
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalSurvivorsSinglePassengerLocus; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalSurvivorsSingleDriverLocus; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalSurvivorsSinglePassengerGene; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalSurvivorsSingleDriverGene;        
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalShadowSinglePassengerLocus; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalShadowSingleDriverLocus; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalShadowSinglePassengerGene; 
    private final ConcurrentHashMap<Integer, CellIndexHolder>[]        _equalShadowSingleDriverGene; 
    
    private final MutationTypeAnalysis                 _mta;               //calling object
    private final MutationTypeAnalysisTaskWorkToDo     _workToDo;              //the ID of TaskToDo
    private final int                                  _iteration;
    private final AtomicInteger                        _notProcessedCounter;
    
    /**
     * Default constructor
     * @param mta
     * @param inWorkToDo
     * @param tasksToFinish
     */
    public MutationTypeAnalysisTask(MutationTypeAnalysis mta, MutationTypeAnalysisTaskWorkToDo inWorkToDo, AtomicInteger tasksToFinish) {
        _workToDo                           = inWorkToDo;              
        _notProcessedCounter                = tasksToFinish;
        _notProcessedCounter.incrementAndGet();
        _mta                                = mta;
        _iteration                          = mta.iteration;
        _equalSurvivorsSinglePassengerLocus = mta.getAnalytics().equalSurvivorsSinglePassengerLocus;  
        _equalSurvivorsSingleDriverLocus    = mta.getAnalytics().equalSurvivorsSingleDriverLocus; 
        _equalSurvivorsSinglePassengerGene  = mta.getAnalytics().equalSurvivorsSinglePassengerGene; 
        _equalSurvivorsSingleDriverGene     = mta.getAnalytics().equalSurvivorsSingleDriverGene;        
        _equalShadowSinglePassengerLocus    = mta.getAnalytics().equalShadowSinglePassengerLocus; 
        _equalShadowSingleDriverLocus       = mta.getAnalytics().equalShadowSingleDriverLocus; 
        _equalShadowSinglePassengerGene     = mta.getAnalytics().equalShadowSinglePassengerGene; 
        _equalShadowSingleDriverGene        = mta.getAnalytics().equalShadowSingleDriverGene; 
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
        
        switch(_workToDo){
            case mtaCclikSurvivorsPassengerLocus:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsPassengerLocus, Activity.started);        
                _mta.correctCellListsIntegerKeyed(_equalSurvivorsSinglePassengerLocus);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsPassengerLocus, Activity.finished);  
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikSurvivorsDriverLocus:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsDriverLocus, Activity.started);        
                _mta.correctCellListsIntegerKeyed(_equalSurvivorsSingleDriverLocus);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsDriverLocus, Activity.finished);
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikSurvivorsPassengerGene:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsPassengerGene, Activity.started);        
                _mta.correctCellListsIntegerKeyed(_equalSurvivorsSinglePassengerGene);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsPassengerGene, Activity.finished);  
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikSurvivorsDriverGene:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsDriverGene, Activity.started);        
                _mta.correctCellListsIntegerKeyed(_equalSurvivorsSingleDriverGene);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikSurvivorsDriverGene, Activity.finished); 
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikShadowPassengerLocus:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowPassengerLocus, Activity.started);                
                _mta.correctCellListsIntegerKeyed(_equalShadowSinglePassengerLocus);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowPassengerLocus, Activity.finished);   
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikShadowDriverLocus:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowDriverLocus, Activity.started);                
                _mta.correctCellListsIntegerKeyed(_equalShadowSingleDriverLocus);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowDriverLocus, Activity.finished); 
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikShadowPassengerGene:     
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowPassengerGene, Activity.started);                
                _mta.correctCellListsIntegerKeyed(_equalShadowSinglePassengerGene);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowPassengerGene, Activity.finished);    
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaCclikShadowDriverGene:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowDriverGene, Activity.started);                
                _mta.correctCellListsIntegerKeyed(_equalShadowSingleDriverGene);
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaCclikShadowDriverGene, Activity.finished);   
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaSurvivorsDriverLocus:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsDriverLocus, Activity.started);
                String[] geneTagsD1 = _mta.getSimModel().getModParams().getMAM().getDriverTags();
                _mta.setChmSurvivorsMutTypeLocusDriver(_mta.mutationTypeAllTagsAnalysisII(_equalSurvivorsSingleDriverLocus, geneTagsD1));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsDriverLocus, Activity.finished);  
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaShadowDriverLocus:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowDriverLocus, Activity.started);                                
                String[] geneTagsD2 = _mta.getSimModel().getModParams().getMAM().getDriverTags();
                _mta.setChmShadowMutTypeLocusDriver(_mta.mutationTypeAllTagsAnalysisII(_equalShadowSingleDriverLocus,geneTagsD2));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowDriverLocus, Activity.finished);   
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaSurvivorsPassengerLocus:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsPassengerLocus, Activity.started);                                
                String[] geneTagsP1 = _mta.getSimModel().getModParams().getMAM().getPassengerTags();
                _mta.setChmSurvivorsMutTypeLocusPassenger(_mta.mutationTypeAllTagsAnalysisII(_equalSurvivorsSinglePassengerLocus,geneTagsP1));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsPassengerLocus, Activity.finished); 
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaShadowPassengerLocus:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowPassengerLocus, Activity.started);                                
                String[] geneTagsP2 = _mta.getSimModel().getModParams().getMAM().getPassengerTags();
                _mta.setChmShadowMutTypeLocusPassenger(_mta.mutationTypeAllTagsAnalysisII(_equalShadowSinglePassengerLocus,geneTagsP2));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowPassengerLocus, Activity.finished);                                
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaSurvivorsDriverGene:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsDriverGene, Activity.started);                                        
                String[] geneTagsD3 = _mta.getSimModel().getModParams().getMAM().getDriverTags();
                _mta.setChmSurvivorsMutTypeGeneDriver(_mta.mutationTypeAllTagsAnalysisII(_equalSurvivorsSingleDriverGene,geneTagsD3));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsDriverGene, Activity.finished);   
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaShadowDriverGene:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowDriverGene, Activity.started);                                                
                String[] geneTagsD4 = _mta.getSimModel().getModParams().getMAM().getDriverTags();
                _mta.setChmShadowMutTypeGeneDriver(_mta.mutationTypeAllTagsAnalysisII(_equalShadowSingleDriverGene,geneTagsD4));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowDriverGene, Activity.finished);    
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaSurvivorsPassengerGene:
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsPassengerGene, Activity.started);                                                        
                String[] geneTagsP3 = _mta.getSimModel().getModParams().getMAM().getPassengerTags();
                _mta.setChmSurvivorsMutTypeGenePassenger(_mta.mutationTypeAllTagsAnalysisII(_equalSurvivorsSinglePassengerGene, geneTagsP3));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaSurvivorsPassengerGene, Activity.finished);  
                _notProcessedCounter.decrementAndGet();
                break;
            case mtaAtaShadowPassengerGene:            
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowPassengerGene, Activity.started);                                                        
                String[] geneTagsP4 = _mta.getSimModel().getModParams().getMAM().getPassengerTags();
                _mta.setChmShadowMutTypeGenePassenger(_mta.mutationTypeAllTagsAnalysisII(_equalShadowSinglePassengerGene, geneTagsP4));
                StaticConsoleLogger.logActivity(_iteration, Activity.mtaAtaShadowPassengerGene, Activity.finished);  
                _notProcessedCounter.decrementAndGet();
                break;
        }
        return null;
            
    }
    
}
