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
package clostrue.postprocessing.plotter;
 
import clostrue.enumerations.PlotterTaskWorkToDo;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;

/**
 * Implements one single Calculation Task.
 * 
 * A calculation task is handling the sumulation computations
 * for a particular subset of cells of the whole population
 * A calculation task is started in background progressing
 * By the Simulation object
 * 
 * @author Krzysztof
 */ 
public class PlotterTask extends SwingWorker<Void,Void> {

    private Plotter                 _plotter;               //plotter object
    private PlotterTaskWorkToDo     _workToDo;              //the ID of PlotterTaskWorkToDO
    private String                  _geneTag;               //gene Tag
    private final AtomicInteger     _notProcessedCounter;
    
    /**
     * Default (empty) constructor
     */
    public PlotterTask(AtomicInteger notProcessedCounter) {
        _notProcessedCounter = notProcessedCounter;
    }
        
    /**
     * Parameter passing and object creation.
     * The constructor of SwingWorker extending class does not allow
     * to pass parameters. Instead this method is created and acts
     * as "normal" class constructor
     * 
     * @param inPlotter                     plotter object
     * @param inWorkToDo                    the ID of PlotterTaskWorkToDo
     * @param inGeneTag                     gene Tag
     */
    public void passParameters(
         Plotter                inPlotter,
         PlotterTaskWorkToDo    inWorkToDo,
         String                 inGeneTag
         ) {
  
        this._plotter                       = inPlotter;
        this._workToDo                      = inWorkToDo;
        this._geneTag                       = inGeneTag;
        
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
            case saveJFreeChartPopulationSizeToPNG:
                _plotter.saveJFreeChartPopulationSizeToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartCumulatedDriverToPNG:
                _plotter.saveJFreeChartCumulatedDriverToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartCumulatedPassengerToPNG:
                _plotter.saveJFreeChartCumulatedPassengerToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartDrivPassRatioToPNG:
                _plotter.saveJFreeChartDrivPassRatioToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramShadowDriverMutationCountToPNG:
                _plotter.saveJFreeChartHistogramShadowDriverMutationCountToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramShadowPassengerMutationCountToPNG:
                _plotter.saveJFreeChartHistogramShadowPassengerMutationCountToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramSurvivorsDriverMutationCountToPNG:
                _plotter.saveJFreeChartHistogramSurvivorsDriverMutationCountToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramSurvivorsPassengerMutationCountToPNG:
                _plotter.saveJFreeChartHistogramSurvivorsPassengerMutationCountToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
//            case saveJFreeChartHistogramDrivPassRatioToPNG:
//                _plotter.saveJFreeChartHistogramShadowDrivPassRatioToPNG();
//                _notProcessedCounter.decrementAndGet();
//                break;
            case saveJFreeChartHistogramPopulationSizeToPNG:
                _plotter.saveJFreeChartHistogramPopulationSizeToPNG();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeGeneDriverToPNGgeneTag:
                _plotter.saveJFreeChartSurvivorsMutTypeGeneDriverToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeGeneDriverToPNGgeneTag:
                _plotter.saveJFreeChartShadowMutTypeGeneDriverToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeLocusDriverToPNGgeneTag:  
                _plotter.saveJFreeChartSurvivorsMutTypeLocusDriverToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeLocusDriverToPNGgeneTag:  
                _plotter.saveJFreeChartShadowMutTypeLocusDriverToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeGenePassengerToPNGgeneTag:
                _plotter.saveJFreeChartSurvivorsMutTypeGenePassengerToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeGenePassengerToPNGgeneTag:
                _plotter.saveJFreeChartShadowMutTypeGenePassengerToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeLocusPassengerToPNGgeneTag: 
                _plotter.saveJFreeChartSurvivorsMutTypeLocusPassengerToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeLocusPassengerToPNGgeneTag: 
                _plotter.saveJFreeChartShadowMutTypeLocusPassengerToPNG(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartPopulationSizeToCSV:
                _plotter.saveJFreeChartPopulationSizeToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartCumulatedDriverToCSV:
                _plotter.saveJFreeChartCumulatedDriverToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartCumulatedPassengerToCSV:
                _plotter.saveJFreeChartCumulatedPassengerToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartDrivPassRatioToCSV:
                _plotter.saveJFreeChartDrivPassRatioToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramShadowDriverMutationCountToCSV:
                _plotter.saveJFreeChartHistogramShadowDriverMutationCountToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramShadowPassengerMutationCountToCSV:
                _plotter.saveJFreeChartHistogramShadowPassengerMutationCountToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramSurvivorsDriverMutationCountToCSV:
                _plotter.saveJFreeChartHistogramSurvivorsDriverMutationCountToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartHistogramSurvivorsPassengerMutationToCSV:
                _plotter.saveJFreeChartHistogramSurvivorsPassengerMutationToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
//            case saveJFreeChartHistogramDrivPassRatioToCSV:
//                _plotter.saveJFreeChartHistogramShadowDrivPassRatioToCSV();
//                _notProcessedCounter.decrementAndGet();
//                break;
            case saveJFreeChartHistogramPopulationSizeToCSV:
                _plotter.saveJFreeChartHistogramPopulationSizeToCSV();
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeGeneDriverToCSVgeneTag:
                _plotter.saveJFreeChartSurvivorsMutTypeGeneDriverToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeGeneDriverToCSVgeneTag:
                _plotter.saveJFreeChartShadowMutTypeGeneDriverToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeLocusDriverToCSVgeneTag:  
                _plotter.saveJFreeChartSurvivorsMutTypeLocusDriverToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeLocusDriverToCSVgeneTag:  
                _plotter.saveJFreeChartShadowMutTypeLocusDriverToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeGenePassengerToCSVgeneTag:
                _plotter.saveJFreeChartSurvivorsMutTypeGenePassengerToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeGenePassengerToCSVgeneTag:
                _plotter.saveJFreeChartShadowMutTypeGenePassengerToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartSurvivorsMutTypeLocusPassengerToCSVgeneTag:
                _plotter.saveJFreeChartSurvivorsMutTypeLocusPassengerToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;
            case saveJFreeChartShadowMutTypeLocusPassengerToCSVgeneTag:
                _plotter.saveJFreeChartShadowMutTypeLocusPassengerToCSV(_geneTag);
                _notProcessedCounter.decrementAndGet();
                break;

        }
        return null;
            
        
    }
    
}
