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
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingNode;
import javafx.scene.chart.XYChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import clostrue.Settings;
import clostrue.Simulation;
import clostrue.GuiController;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.ModelParam;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Name;
import clostrue.hardcodes.file.NamePart;
import clostrue.hardcodes.plots.Setting;
import clostrue.hardcodes.plots.Texts;
import clostrue.model.SimModel;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.postprocessing.analysis.mutationtType.MutationTypeAnalysis;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;

/**
 * Plotter object is a tool for processing plots, charts and histograms
 * in the program gui and graphical file export as well.
 */

public class Plotter {

    private final static boolean   NO_GROUPPING         = false;
    private final static boolean   OMMIT_ZEROES         = true;
    private final static boolean   Y_ROUND_TRUE         = true;
    private final static boolean   Y_ROUND_FALSE        = false;
    
    private final GuiController guiController;
    private final Simulation    simulation;
    private final SimModel      simModel;
    private final Settings      settings;
    private final Analytics     analytics;
    private final MutationTypeAnalysis mta;
    private final int           iteration;
    private final AtomicInteger tasksToFinish = new AtomicInteger(0);
    
    private ExecutorService threadPool;                 //the pool of all tasks
    private final List<PlotterTask> tasks;              //All the tasks to be executed
    
    /**
     * Default constructor
         * @param guiController GUI Controller (contains simulation)
     */    
    public Plotter(GuiController guiController) {       
        this.guiController  = guiController;
        this.simulation     = guiController.getSimulation();
        this.iteration      = simulation.getIteration();
        this.settings       = simulation.getSettings();
        this.analytics      = simulation.getAnalytics();
        this.mta            = analytics.getMta();
        this.tasks          = new ArrayList<>();
        this.simModel       = new SimModel(iteration, simulation.getSimModel());
    }
    
    /**
     * Plotter logic entry point
     * @param updateGui
     */
    public void handleAllPlots(boolean updateGui){
               
        if (updateGui == true){
            Platform.runLater(() -> {
                diplayGUIPlots();    
            });                      
        }
        saveGUIPlotsPNG();
        saveGUIPlotsCSV();
        simulation.getBenchmark().doTheBenchmark();
        
        if (!tasks.isEmpty()){
            threadPool = Executors.newFixedThreadPool(tasks.size());
            tasks.stream().forEach((task) -> {
                threadPool.submit(task);
            });  
            int prevTasksToFinish = 0;
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                }
                int curTasksToFinish = tasksToFinish.get();
                if (prevTasksToFinish != curTasksToFinish){
                    StaticConsoleLogger.logActivity(iteration, Activity.ParallelPlotterFinish, String.valueOf(curTasksToFinish));                    
                    prevTasksToFinish = curTasksToFinish;
                }
            } while (tasksToFinish.get() != 0);
            
        threadPool.shutdown();
        }

    }
           
    /**
     * Displays all the plots on the program GUI
     */
    private void diplayGUIPlots(){
        plotJFreeChartPopulationSize();
        plotJFreeChartCumulatedDriver();
        plotJFreeChartCumulatedPassenger();
        plotJFreeChartDrivPassRatio();
        plotJFreeHistogramShadowDriverMutation();
        plotJFreeHistogramShadowPassengerMutation();
        plotJFreeHistogramPopulationSize();
        plotJFreepSurvivorsMutTypeGeneDriver();
        plotJFreepMutTypeGenePassenger();
        plotJFreepMutTypeLocusDriver();
        plotJFreepMutTypeLocusPassenger();
    }

    /**
     * Saves all the simulation plots in PNG Files
     */    
    private void saveGuiSimulationPlotsToPng(){
               
        if (settings.getBooleanValue(Param.cbGenerateSimulationPNG)){

            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationPlotsToPNG, Activity.started);

            PlotterTask task;
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartPopulationSizeToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartCumulatedDriverToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartCumulatedPassengerToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartDrivPassRatioToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationPlotsToPNG, Activity.scheduled);
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationPlotsToPNG, Activity.skipped);
        }       
    }
    
    private void saveGuiAnalyticsPlotsToPng(){
        
        if (settings.getBooleanValue(Param.cbGenerateAnalyticsPNG)){
            
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsPlotsToPNG, Activity.started);             
            
            PlotterTask task;
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramShadowDriverMutationCountToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramShadowPassengerMutationCountToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramSurvivorsDriverMutationCountToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramSurvivorsPassengerMutationCountToPNG, null);
            tasks.add(task);            
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramPopulationSizeToPNG, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            for ( String geneTag : simModel.getModParams().getMAM().getDriverTags()){

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeGeneDriverToPNGgeneTag, geneTag);
                tasks.add(task); 
                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeLocusDriverToPNGgeneTag, geneTag);
                tasks.add(task);            
                tasksToFinish.incrementAndGet();

                    
//                task = new  PlotterTask(tasksToFinish);
//                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeGeneDriverToPNGgeneTag, geneTag);
//                tasks.add(task); 
//                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeLocusDriverToPNGgeneTag, geneTag);
                tasks.add(task);       
                tasksToFinish.incrementAndGet();          
                
            }
            
            for ( String geneTag : simModel.getModParams().getMAM().getPassengerTags()){
                
                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeGenePassengerToPNGgeneTag, geneTag);
                tasks.add(task); 
                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeLocusPassengerToPNGgeneTag, geneTag);
                tasks.add(task);                     
                tasksToFinish.incrementAndGet();

                //switched off due to heavy performance issues (file content not tested as well)
                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeGenePassengerToPNGgeneTag, geneTag);
                tasks.add(task); 
                tasksToFinish.incrementAndGet();                
                
                if (1 == 2){

                    task = new  PlotterTask(tasksToFinish);
                    task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeLocusPassengerToPNGgeneTag, geneTag);
                    tasks.add(task);    
                    tasksToFinish.incrementAndGet();
                    
                }             
                
            }            

            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsPlotsToPNG, Activity.scheduled); 
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsPlotsToPNG, Activity.skipped);
        }       
        
    }
    
    /**
     * Saves all the plots in PNG Files
     */
    private void saveGUIPlotsPNG(){

       
        if (settings.getBooleanValue(Param.cbGenerateSimulationPNG) ||
            settings.getBooleanValue(Param.cbGenerateAnalyticsPNG)){

            StaticConsoleLogger.logActivity(iteration, Activity.savePlotImagesToPNG, Activity.started);

            saveGuiSimulationPlotsToPng();
            saveGuiAnalyticsPlotsToPng();

            StaticConsoleLogger.logActivity(iteration, Activity.savePlotImagesToPNG, Activity.finished);
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.savePlotImagesToPNG, Activity.skipped);            
        }
        
    }
    
    /**
     * Saves all the simulation plots in CSV Files
     */        
    private void saveGUISimulationPlotsToCsv(){

        if (settings.getBooleanValue(Param.cbGenerateSimulationPNG)){

            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationDataToCSV, Activity.started);

            PlotterTask task;
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartPopulationSizeToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartCumulatedDriverToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartCumulatedPassengerToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartDrivPassRatioToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationDataToCSV, Activity.scheduled);
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiSimulationDataToCSV, Activity.skipped);
        }     
        
    }
    
    /**
     * Save all the analytics plots in CSV Files
     */
    private void saveGUIAnalyticsPlotsToCsv(){

        if (settings.getBooleanValue(Param.cbGenerateAnalyticsPNG)){
            
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsDataToCSV, Activity.started);

            PlotterTask task;
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramShadowDriverMutationCountToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramShadowPassengerMutationCountToCSV, null);
            tasks.add(task);                
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramSurvivorsDriverMutationCountToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();

            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramSurvivorsPassengerMutationToCSV, null);
            tasks.add(task);            
            tasksToFinish.incrementAndGet();
            
            task = new  PlotterTask(tasksToFinish);
            task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartHistogramPopulationSizeToCSV, null);
            tasks.add(task);
            tasksToFinish.incrementAndGet();
            
            for ( String geneTag : simModel.getModParams().getMAM().getDriverTags()){

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeGeneDriverToCSVgeneTag, geneTag);
                tasks.add(task);
                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeLocusDriverToCSVgeneTag, geneTag);
                tasks.add(task);
                tasksToFinish.incrementAndGet();

//                task = new  PlotterTask(tasksToFinish);
//                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeGeneDriverToCSVgeneTag, geneTag);
//                tasks.add(task);
//                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeLocusDriverToCSVgeneTag, geneTag);
                tasks.add(task);   
                tasksToFinish.incrementAndGet();
                                             
            }

            for ( String geneTag : simModel.getModParams().getMAM().getPassengerTags()){

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeGenePassengerToCSVgeneTag, geneTag);
                tasks.add(task);
                tasksToFinish.incrementAndGet();

                task = new  PlotterTask(tasksToFinish);
                task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartSurvivorsMutTypeLocusPassengerToCSVgeneTag, geneTag);
                tasks.add(task);
                tasksToFinish.incrementAndGet();

                    task = new  PlotterTask(tasksToFinish);
                    task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeGenePassengerToCSVgeneTag, geneTag);
                    tasks.add(task);
                    tasksToFinish.incrementAndGet();                
                
                if (1 == 2 ){

//                    //switched off due to heavy performance issues (file content not tested as well)
//                    task = new  PlotterTask(tasksToFinish);
//                    task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeGenePassengerToCSVgeneTag, geneTag);
//                    tasks.add(task);
//                    tasksToFinish.incrementAndGet();

                    task = new  PlotterTask(tasksToFinish);
                    task.passParameters(this, PlotterTaskWorkToDo.saveJFreeChartShadowMutTypeLocusPassengerToCSVgeneTag, geneTag);
                    tasks.add(task);  
                    tasksToFinish.incrementAndGet();
                    
                }              
                
            }     
        
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsDataToCSV, Activity.scheduled);
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.saveGuiAnalyticsDataToCSV, Activity.skipped);
        }

    }
    
    /**
     * Saves all the plots in CSV Files
     */
    private void saveGUIPlotsCSV(){

        if (settings.getBooleanValue(Param.cbGenerateSimulationPNG) ||
            settings.getBooleanValue(Param.cbGenerateAnalyticsPNG)){        
        
            StaticConsoleLogger.logActivity(iteration, Activity.savePlotDataToCSV, Activity.started);
         
            saveGUISimulationPlotsToCsv();
            saveGUIAnalyticsPlotsToCsv();

            StaticConsoleLogger.logActivity(iteration, Activity.savePlotDataToCSV, Activity.finished);
            
        } else {
            StaticConsoleLogger.logActivity(iteration, Activity.savePlotDataToCSV, Activity.skipped);
        }
 
    }
    
    /**
     * Plots the Population Size plot on the "Simulation" Tab
     */
    private void plotJFreeChartPopulationSize(){
       
        JFreeChart chartToSave = getChartPopulationSize(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spPopulationSize);
        
    }       

    /**
     * Plots the Histogram of Driver Mutation plot on the "Analytics" Tab
     */
    private void plotJFreeHistogramShadowDriverMutation(){
       
        JFreeChart chartToSave = getChartHistogramShadowDriverMutations(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spHistogramDriver);
        
    }         
    
    /**
     * Plots the Histogram of Passenger Mutation plot on the "Analytics" Tab
     */
    private void plotJFreeHistogramShadowPassengerMutation(){
       
        JFreeChart chartToSave = getChartHistogramShadowPassengerMutations(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spHistogramPassenger);
        
    }         

    /**
     * Plots the Histogram of Passenger Mutation plot on the "Analytics" Tab
     */
    private void plotJFreeHistogramPopulationSize(){
       
        JFreeChart chartToSave = getChartHistogramPopulationSize(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spHistogramPopulationSize);
        
    }         

     /**
     * Plots the Histogram of the Mutation Type Analysis for Gene Drivers on the "Mutation Types" Tab
     */
    private void plotJFreepSurvivorsMutTypeGeneDriver(){
       
        JFreeChart chartToPlot 
                = getChartSurvivorsMutTypeGeneDriver(settings.getBooleanValue(Param.teResUseGroupping),
                        ModelParam.commonGeneTag);
        ChartPanel chartpanel = new ChartPanel(chartToPlot);
        if (chartToPlot != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spMutTypeGeneDriver);
        
    }        

     /**
     * Plots the Histogram of the Mutation Type Analysis for Gene Passenger on the "Mutation Types" Tab
     */
    private void plotJFreepMutTypeGenePassenger(){
       
        JFreeChart chartToPlot 
                = getChartSurvivorsMutTypeGenePassenger(settings.getBooleanValue(Param.teResUseGroupping),
                        ModelParam.commonGeneTag);
        ChartPanel chartpanel = new ChartPanel(chartToPlot);
        if (chartToPlot != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spMutTypeGenePassenger);
        
    }        

     /**
     * Plots the Histogram of the Mutation Type Analysis for Locus Drivers on the "Mutation Types" Tab
     */
    private void plotJFreepMutTypeLocusDriver(){
       
        JFreeChart chartToPlot 
                = getChartSurvivorsMutTypeLocusDriver(settings.getBooleanValue(Param.teResUseGroupping),
                        ModelParam.commonGeneTag);
        ChartPanel chartpanel = new ChartPanel(chartToPlot);
        if (chartToPlot != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spMutTypeLocusDriver);
        
    }        

     /**
     * Plots the Histogram of the Mutation Type Analysis for Locus Passenger on the "Mutation Types" Tab
     */
    private void plotJFreepMutTypeLocusPassenger(){
       
        JFreeChart chartToPlot 
                = getChartSurvivorsMutTypeLocusPassenger(settings.getBooleanValue(Param.teResUseGroupping),
                        ModelParam.commonGeneTag);
        ChartPanel chartpanel = new ChartPanel(chartToPlot);
        if (chartToPlot != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spMutTypeLocusPassenger);
        
    }        
    
    
//    /**
//     * Plots the Histogram of Driver/Passenger Ratio plot on the "Analytics" Tab
//     */
//    private void plotJFreeHistogramShadowDrivPassRatio(){
//       
//        JFreeChart chartToSave = getChartHistogramShadowDrivPassRatio(settings.getBooleanValue(Param.teResUseGroupping));
//        ChartPanel chartpanel = new ChartPanel(chartToSave);
//        if (chartToSave != null){
//            chartpanel.setDomainZoomable(true);                    
//        }
//        final SwingNode swingNode = new SwingNode();
//        guiController.createSwingContent(swingNode, chartpanel, guiController.spHistogramDrivPass);
//        
//    }         
        
    
    /**
     * Plots the Driver/Passenger ratio plot on the "Simulation" Tab
     */
    private void plotJFreeChartDrivPassRatio(){
       
        JFreeChart chartToSave = getChartDrivPassRatio(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spDrivPassRatio);
        
    }         
    
    /**
     * Plots the Cumulated Driver plot on the "Simulation" Tab
     */
    private void plotJFreeChartCumulatedDriver(){
       
        JFreeChart chartToSave = getChartCumulatedDriver(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spCumulatedDriver);
        
    }    

    /**
     * Plots the Cumulated Driver plot on the "Simulation" Tab
     */
    private void plotJFreeChartCumulatedPassenger(){
       
        JFreeChart chartToSave = getChartCumulatedPassenger(settings.getBooleanValue(Param.teResUseGroupping));
        ChartPanel chartpanel = new ChartPanel(chartToSave);
        if (chartToSave != null){
            chartpanel.setDomainZoomable(true);        
        }
        final SwingNode swingNode = new SwingNode();
        guiController.createSwingContent(swingNode, chartpanel, guiController.spCumulatedPassenger);
        
    }   
    
    /**
     * Saves the Population Size plot in PNG File
     */
    public void saveJFreeChartPopulationSizeToPNG(){
       
        JFreeChart chartToSave = getChartPopulationSize(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snPopulationSize, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }       

   
    /**
     * Saves the Histogram Driver Mutations plot in PNG File
     */
    public void saveJFreeChartHistogramShadowDriverMutationCountToPNG(){
       
        JFreeChart chartToSave = getChartHistogramShadowDriverMutations(NO_GROUPPING);
        if (chartToSave != null){                
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snHistogramShadowDriverMutCount, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }        
    
    /**
     * Saves the Histogram Passenger Mutations plot in PNG File
     */
    public void saveJFreeChartHistogramShadowPassengerMutationCountToPNG(){
       
        JFreeChart chartToSave = getChartHistogramShadowPassengerMutations(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snHistogramShadowPassengerMutCount, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }          

    /**
     * Saves the Histogram Driver Mutations plot in PNG File
     */
    public void saveJFreeChartHistogramSurvivorsDriverMutationCountToPNG(){
       
        JFreeChart chartToSave = getChartHistogramSurvivorsDriverMutations(NO_GROUPPING);
        if (chartToSave != null){                
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snHistogramSurvivorsDriverMutCount, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }        
    
    /**
     * Saves the Histogram Passenger Mutations plot in PNG File
     */
    public void saveJFreeChartHistogramSurvivorsPassengerMutationCountToPNG(){
       
        JFreeChart chartToSave = getChartHistogramSurvivorsPassengerMutations(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snHistogramSurvivorsPassengerMutCount, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }        
    
    /**
     * Saves the Histogram Population Size plot in PNG File
     */
    public void saveJFreeChartHistogramPopulationSizeToPNG(){
       
        JFreeChart chartToSave = getChartHistogramPopulationSize(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snHistogramPopulationSize, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }              
    
    /**
     * Saves the Histogram Mutation Type Analysis for Gene Drivers in PNG File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeGeneDriverToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartSurvivorsMutTypeGeneDriver(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snSurvivorsHistogramMutTypeGeneDriver, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }         

    /**
     * Saves the Histogram Mutation Type Analysis for Gene Drivers in PNG File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeGeneDriverToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartShadowMutTypeGeneDriver(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snShadowHistogramMutTypeGeneDriver, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }        
    
    /**
     * Saves the Histogram Mutation Type Analysis for Gene Passenger in PNG File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeGenePassengerToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartSurvivorsMutTypeGenePassenger(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snSurvivorsHistogramMutTypeGenePassenger, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }       

    /**
     * Saves the Histogram Mutation Type Analysis for Gene Passenger in PNG File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeGenePassengerToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartShadowMutTypeGenePassenger(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snShadowHistogramMutTypeGenePassenger, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }       

    
    /**
     * Saves the Histogram Mutation Type Analysis for Gene Drivers in PNG File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeLocusDriverToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartSurvivorsMutTypeLocusDriver(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snSurvivorsHistogramMutTypeLocusDriver, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }         

    /**
     * Saves the Histogram Mutation Type Analysis for Gene Drivers in PNG File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeLocusDriverToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartShadowMutTypeLocusDriver(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snShadowHistogramMutTypeLocusDriver, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    } 
    
    /**
     * Saves the Histogram Mutation Type Analysis for Gene Passenger in PNG File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeLocusPassengerToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartSurvivorsMutTypeLocusPassenger(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snSurvivorsHistogramMutTypeLocusPassenger, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }       

    /**
     * Saves the Histogram Mutation Type Analysis for Gene Passenger in PNG File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeLocusPassengerToPNG(String geneTag){
       
        JFreeChart chartToSave 
                = getChartShadowMutTypeLocusPassenger(NO_GROUPPING, geneTag);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileNameWithTag(geneTag,
                    Name.snShadowHistogramMutTypeLocusPassenger, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }     
        
    /**
     * Saves the Population Size plot in PNG File
     */
    public void saveJFreeChartDrivPassRatioToPNG(){
       
        JFreeChart chartToSave = getChartDrivPassRatio(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snDrivPassRatio, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }        
    
    /**
     * Saves the Cumulated Driver Mutations plot in PNG File
     */
    public void saveJFreeChartCumulatedDriverToPNG(){
       
        JFreeChart chartToSave = getChartCumulatedDriver(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snShadowCumulatedDriver, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }   

    /**
     * Saves the Cumulated Passenger Mutations plot in PNG File
     */
    public void saveJFreeChartCumulatedPassengerToPNG(){
       
        JFreeChart chartToSave = getChartCumulatedPassenger(NO_GROUPPING);
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snCumulatedPassenger, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }
    }   
    
    /**
     * Saves the Population Size plot in CSV File
     */
    public void saveJFreeChartPopulationSizeToCSV(){
       
        XYSeries serie = getDataSeriePopulationSize(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snPopulationSize, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, serie, fileName, Texts.populationSizeXAxis, Texts.populationSizeYAxis,Y_ROUND_TRUE);

    }        
    
    /**
     * Saves the Histogram of Driver Mutations plot in CSV File
     */
    public void saveJFreeChartHistogramShadowDriverMutationCountToCSV(){
       
        SimpleHistogramDataset dataset = getHistogramDatasetShadowDriverMutations(NO_GROUPPING);
        if (dataset != null){
            String fileName = prepareFileName(NamePart.snHistogramShadowDriverMutCount, 
                    Extension.dotCsv);
            PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                    dataset, 
                    fileName, 
                    Texts.shadowDriverHistogramXAxis, 
                    Texts.shadowDriverHistogramYAxis,
                    Y_ROUND_TRUE);            
        }

    }        

    /**
     * Saves the Histogram of Driver Mutations plot in CSV File
     */
    public void saveJFreeChartHistogramSurvivorsDriverMutationCountToCSV(){
       
        SimpleHistogramDataset dataset = getHistogramDatasetSurvivorsDriverMutationsCount(NO_GROUPPING);
        if (dataset != null){
            String fileName = prepareFileName(NamePart.snHistogramSurvivorsDriverMutCount, 
                    Extension.dotCsv);
            PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                    dataset, 
                    fileName, 
                    Texts.survivorsDriverHistogramXAxis, 
                    Texts.survivorsDriverHistogramYAxis,
                    Y_ROUND_TRUE);            
        }

    }        
    
    /**
     * Saves the Histogram of Passenger Mutations plot in CSV File
     */
    public void saveJFreeChartHistogramShadowPassengerMutationCountToCSV(){
       
        SimpleHistogramDataset dataset = getHistogramDatasetShadowPassengerMutations(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snHistogramShadowPassengerMutCount, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.shadowPassengerHistogramXAxis, 
                Texts.shadowPassengerHistogramYAxis,
                Y_ROUND_TRUE);

    }      

    /**
     * Saves the Histogram of Passenger Mutations plot in CSV File
     */
    public void saveJFreeChartHistogramSurvivorsPassengerMutationToCSV(){
       
        SimpleHistogramDataset dataset = getHistogramDatasetSurvivorsPassengerMutations(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snHistogramSurvivorsPassengerMutCount, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.survivorsPassengerHistogramXAxis, 
                Texts.survivorsPassengerHistogramYAxis,
                Y_ROUND_TRUE);

    }          
    
    /**
     * Saves the Histogram of Population Size plot in CSV File
     */
    public void saveJFreeChartHistogramPopulationSizeToCSV(){
       
        SimpleHistogramDataset dataset = getHistogramDatasetPopulationSize(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snHistogramPopulationSize, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.popSizeHistogramXAxis, 
                Texts.popSizeHistogramYAxis,
                Y_ROUND_TRUE);

    }       

    /**
     * Saves the Mutation Type Analysis for Gene Drivers in CSV File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeGeneDriverToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeGeneDriver(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snSurvivorsHistogramMutTypeGeneDriver, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeGeneDriverXAxis, 
                Texts.snHistogramMutTypeGeneDriverYAxis,
                Y_ROUND_TRUE);

    }       

    /**
     * Saves the Mutation Type Analysis for Gene Drivers in CSV File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeGeneDriverToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeGeneDriver(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snShadowHistogramMutTypeGeneDriver, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeGeneDriverXAxis, 
                Texts.snHistogramMutTypeGeneDriverYAxis,
                Y_ROUND_TRUE);

    }      
    
    /**
     * Saves the Mutation Type Analysis for Gene Passenger in CSV File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeGenePassengerToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeGenePassenger(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snSurvivorsHistogramMutTypeGenePassenger, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeGenePassengerXAxis, 
                Texts.snHistogramMutTypeGenePassengerYAxis,
                Y_ROUND_TRUE);

    }      

    /**
     * Saves the Mutation Type Analysis for Gene Passenger in CSV File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeGenePassengerToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeGenePassenger(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snShadowHistogramMutTypeGenePassenger, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeGenePassengerXAxis, 
                Texts.snHistogramMutTypeGenePassengerYAxis,
                Y_ROUND_TRUE);

    }         
    
    /**
     * Saves the Mutation Type Analysis for Lous Drivers in CSV File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeLocusDriverToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeLocusDriver(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snSurvivorsHistogramMutTypeLocusDriver, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeLocusDriverXAxis, 
                Texts.snHistogramMutTypeLocusDriverYAxis,
                Y_ROUND_TRUE);

    }       

    /**
     * Saves the Mutation Type Analysis for Lous Drivers in CSV File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeLocusDriverToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeLocusDriver(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snShadowHistogramMutTypeLocusDriver, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeLocusDriverXAxis, 
                Texts.snHistogramMutTypeLocusDriverYAxis,
                Y_ROUND_TRUE);

    }       
    
    /**
     * Saves the Mutation Type Analysis for Locus Passenger in CSV File
     * @param geneTag
     */
    public void saveJFreeChartSurvivorsMutTypeLocusPassengerToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeLocusPassenger(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snSurvivorsHistogramMutTypeLocusPassenger, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeLocusPassengerXAxis, 
                Texts.snHistogramMutTypeLocusPassengerYAxis,
                Y_ROUND_TRUE);

    }      

    /**
     * Saves the Mutation Type Analysis for Locus Passenger in CSV File
     * @param geneTag
     */
    public void saveJFreeChartShadowMutTypeLocusPassengerToCSV(String geneTag){
       
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeLocusPassenger(NO_GROUPPING, geneTag);
        String fileName = prepareFileNameWithTag(geneTag,
                Name.snShadowHistogramMutTypeLocusPassenger, 
                Extension.dotCsv);
        PlotterTools.saveSimpleHistogramDataSetToCSV(iteration, 
                dataset, 
                fileName, 
                Texts.snHistogramMutTypeLocusPassengerXAxis, 
                Texts.snHistogramMutTypeLocusPassengerYAxis,
                Y_ROUND_TRUE);

    }          
    
    /**
     * Saves the Driver/Passenger Ratio plot in CSV File
     */
    public void saveJFreeChartDrivPassRatioToCSV(){
       
        XYSeries serie = getDataSerieDrivPassRatio(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snDrivPassRatio, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, serie, fileName, Texts.historyDriverPassRatioXAxis, Texts.historyDriverPassRatioYAxis, Y_ROUND_FALSE);

    }         
    
    /**
     * Saves the Cumulated Drivers plot in CSV File
     */
    public void saveJFreeChartCumulatedDriverToCSV(){
       
        XYSeries serie = getDataSerieCumulatedDriver(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snShadowCumulatedDriver, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, serie, fileName, Texts.historyDriveMutationXAxis, Texts.historyDriveMutationYAxis, Y_ROUND_TRUE);

    }          

    /**
     * Saves the Cumulated Passengers plot in CSV File
     */
    public void saveJFreeChartCumulatedPassengerToCSV(){
       
        XYSeries serie = getDataSerieCumulatedPassenger(NO_GROUPPING);
        String fileName = prepareFileName(NamePart.snCumulatedPassenger, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, serie, fileName, Texts.historyPassengerMutationXAxis, Texts.historyPassengerMutationYAxis, Y_ROUND_TRUE);

    }  
    
    /**
     * Creates the DataSerie for plot based on a table of double values
     * updated for each cycle of the simulation
     */
    private XYSeries getDataSerieFromDoubleTable(boolean noZeroes, double[] values, boolean useGroupping, String description, int resolution){
        
        XYSeries series = new XYSeries(description);
        int curentCycle = simulation.getCurrentCycle();
        boolean added = false;
        
        if (curentCycle == simModel.getModParams().getMaxCycles()+1){
            curentCycle--;
        }
        
        if (useGroupping){
            int pointDistance = (simModel.getModParams().getMaxCycles()+1) / resolution;
            if (pointDistance < 1){
                pointDistance = 1;
            }        

            int point = pointDistance;
            double value = 0;     
            for (int i = 0; i <= curentCycle; i++) {
                if (point == 0){
                    value /= (double)pointDistance;
                    if (noZeroes){
                        if (value != 0){
                            series.add(i, value); 
                            added = true;
                        } 
                    } else {
                        series.add(i, value);   
                        added = true;
                    }
                    value = values[i];
                    point = pointDistance - 1;
                } else {
                    value += values[i];
                    point--;
                }
            }            
        } else {
            for (int i = 0; i <= curentCycle; i++) {
                if (noZeroes){
                    if (values[i] != 0){
                        series.add(i, values[i]); 
                        added = true;
                    }
                } else {
                    series.add(i, values[i]);      
                    added = true;
                }
            }            
        }
        
        if (!added){
            series.add(0,0);
        }
        
        return series;
    }    
    
    

    /**
     * Creates the DataSerie for plot based on a XYChart Serie of Integer - Long
     * Used for histograms
     */
    private SimpleHistogramDataset getHistogramDatasetFromXYChartSeriesIntegerLong(XYChart.Series<Integer, Long> inSerie){
        
        if (inSerie.getData().isEmpty()){
            return null;
        }        
        
        SimpleHistogramDataset dataset = new SimpleHistogramDataset(1);
        
        inSerie.getData().stream().map((entry) -> {
            dataset.addBin(new SimpleHistogramBin(entry.getXValue()-0.5, entry.getXValue()+0.4999999,true,true));
            return entry;
        }).forEachOrdered((entry) -> {
            // do not forget to divide by the bin width if the bin width will change in the future (now it is more or less one)
            for (Long repetitions = new Long(0); repetitions < entry.getYValue().longValue(); repetitions++){
                dataset.addObservation(entry.getXValue());
            }
        });

        return dataset;
    }                                         

    /**
     * Creates the DataSerie for plot based on a XYChart Serie of Integer - Long
     * Used for histograms
     */
    private SimpleHistogramDataset getHistogramDatasetFromXYChartSeriesIntegerInteger(XYChart.Series<Integer, Integer> inSerie){
        
        if (inSerie.getData().isEmpty()){
            return null;
        }        
        
        SimpleHistogramDataset dataset = new SimpleHistogramDataset(1);
        
        inSerie.getData().stream().map((entry) -> {
            dataset.addBin(new SimpleHistogramBin(entry.getXValue()-0.5, entry.getXValue()+0.4999999,true,true));
            return entry;
        }).forEachOrdered((entry) -> {
            // do not forget to divide by the bin width if the bin width will change in the future (now it is more or less one)
            for (Integer repetitions = new Integer(0); repetitions < entry.getYValue(); repetitions++){
                dataset.addObservation(entry.getXValue());
            }
        });

        return dataset;
    }       
    
    /**
     * Creates the DataSerie for plot for Population Size
     */
    private XYSeries getDataSeriePopulationSize(boolean useGroupping){
        
        return PlotterTools.getDataSerieFromIntTable(PlotterTools.convertAtomicIntegerArrayToIntArray(simulation.getStatistics().getHistoryCellCountN()), 
                useGroupping, 
                Texts.populationSizeTitle,
                Setting.teResPopulationSize,
                simulation.getCurrentCycle(),
                simModel.getModParams().getMaxCycles());
    }
    
    /**
     * Creates the DataSerie for plot for Histogram of Driver Mutations
     */
    private SimpleHistogramDataset getHistogramDatasetShadowDriverMutations(boolean useGroupping){
        
        return getHistogramDatasetFromXYChartSeriesIntegerInteger(convertCHMIIAsIntegerSerie(simulation.getStatistics().getHistogramShadowDriverMutations(),
                    useGroupping,
                    Setting.teResAnalytics));
        
    }    

    /**
     * Creates the DataSerie for plot for Histogram of Driver Mutations
     */
    private SimpleHistogramDataset getHistogramDatasetSurvivorsDriverMutationsCount(boolean useGroupping){
        
        return getHistogramDatasetFromXYChartSeriesIntegerInteger(convertCHMIIAsIntegerSerie(simulation.getStatistics().getHistogramSurvivorsDriverMutationsCount(),
                    useGroupping,
                    Setting.teResAnalytics));
        
    }    
    
    /**
     * Creates the DataSerie for plot for Histogram of Passenger Mutations
     */
    private SimpleHistogramDataset getHistogramDatasetShadowPassengerMutations(boolean useGroupping){
        
        return getHistogramDatasetFromXYChartSeriesIntegerInteger(convertCHMIIAsIntegerSerie(simulation.getStatistics().getHistogramShadowPassengerMutations(),
                    useGroupping,
                    Setting.teResAnalytics));
        
    }        

    /**
     * Creates the DataSerie for plot for Histogram of Passenger Mutations
     */
    private SimpleHistogramDataset getHistogramDatasetSurvivorsPassengerMutations(boolean useGroupping){
        
        return getHistogramDatasetFromXYChartSeriesIntegerInteger(convertCHMIIAsIntegerSerie(simulation.getStatistics().getHistogramSurvivorsPassengerMutations(),
                    useGroupping,
                    Setting.teResAnalytics));
        
    }        
    
    /**
     * Creates the DataSerie for plot for Histogram of Population Size
     */
    private SimpleHistogramDataset getHistogramDatasetPopulationSize(boolean useGroupping){
        
        return getHistogramDatasetFromXYChartSeriesIntegerInteger(convertCHMIIAsIntegerSerie(simulation.getStatistics().getHistogramPopulationSize(),
                    useGroupping,
                    Setting.teResAnalytics));
        
    }     
   

        
    
    /**
     * Creates the DataSerie for plot for Driver/Passenger Ratio
     */
    private XYSeries getDataSerieDrivPassRatio(boolean useGroupping){
        
        return getDataSerieFromDoubleTable(OMMIT_ZEROES,
                simulation.getStatistics().getHistoryDriverPassRatio(), 
                useGroupping, 
                Texts.historyDriverPassRatioTitle,
                Setting.teResSimulation);
    }    
    
    /**
     * Creates the DataSerie for plot for Cumulated Driver Mutations
     */
    private XYSeries getDataSerieCumulatedDriver(boolean useGroupping){

        return PlotterTools.getDataSerieFromIntTable(PlotterTools.convertAtomicIntegerArrayToIntArray(simulation.getStatistics().getHistoryDriverMutationCount()), 
            useGroupping, 
            Texts.historyDriveMutationTitle,
            Setting.teResSimulation,
            simulation.getCurrentCycle(),
            simModel.getModParams().getMaxCycles());
    }    
    
    /**
     * Creates the DataSerie for plot for Cumulated Passenger Mutations
     */
    private XYSeries getDataSerieCumulatedPassenger(boolean useGroupping){

        return PlotterTools.getDataSerieFromIntTable(PlotterTools.convertAtomicIntegerArrayToIntArray(simulation.getStatistics().getHistoryPassengerMutationCount()), 
            useGroupping, 
            Texts.historyPassengerMutationTitle,
            Setting.teResSimulation,
            simulation.getCurrentCycle(),
            simModel.getModParams().getMaxCycles());
    }       
    
    /**
     * Creates the JFreeChart for Population Size
     * @return chart for population size
     */
    private JFreeChart getChartPopulationSize(boolean useGroupping){
        
        JFreeChart chart;
        XYSeries series = getDataSeriePopulationSize(useGroupping);
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.populationSizeTitle, 
                Texts.populationSizeXAxis, 
                Texts.populationSizeYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }

    /**
     * Creates the JFreeChart for Histogram of Driver Mutations
     * @return chart for population size
     */
    private JFreeChart getChartHistogramShadowDriverMutations(boolean useGroupping){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset = getHistogramDatasetShadowDriverMutations(useGroupping);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.shadowDriverCountHistogramTitle, 
                    Texts.shadowDriverHistogramXAxis, 
                    Texts.shadowDriverHistogramYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);            
            return chart;
        } else {
            return null;
        }     

    }    
 
    /**
     * Creates the JFreeChart for Histogram of Driver Mutations
     * @return chart for population size
     */
    private JFreeChart getChartHistogramSurvivorsDriverMutations(boolean useGroupping){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset = getHistogramDatasetSurvivorsDriverMutationsCount(useGroupping);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.survivorsDriverCountHistogramTitle, 
                    Texts.survivorsDriverHistogramXAxis, 
                    Texts.survivorsDriverHistogramYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);            
            return chart;
        } else {
            return null;
        }     

    }        
    
    /**
     * Creates the JFreeChart for Histogram of Passenger Mutations
     * @return chart for population size
     */
    private JFreeChart getChartHistogramShadowPassengerMutations(boolean useGroupping){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset = getHistogramDatasetShadowPassengerMutations(useGroupping);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.shadowPassengerCountHistogramTitle, 
                    Texts.shadowPassengerHistogramXAxis, 
                    Texts.shadowPassengerHistogramYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }        

    /**
     * Creates the JFreeChart for Histogram of Passenger Mutations
     * @return chart for population size
     */
    private JFreeChart getChartHistogramSurvivorsPassengerMutations(boolean useGroupping){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset = getHistogramDatasetSurvivorsPassengerMutations(useGroupping);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.survivorsPassengerCountHistogramTitle, 
                    Texts.survivorsPassengerHistogramXAxis, 
                    Texts.survivorsPassengerHistogramYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }     
    
    /**
     * Creates the JFreeChart for Histogram of Population Size
     * @return chart for histogram of population size
     */
    private JFreeChart getChartHistogramPopulationSize(boolean useGroupping){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset = getHistogramDatasetPopulationSize(useGroupping);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.popSizeHistogramTitle, 
                    Texts.popSizeHistogramXAxis, 
                    Texts.popSizeHistogramYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }            
    
    /**
     * Creates the JFreeChart for Mutation Type Analysis for Gene Drivers
     * @return chart for Mutation Type Analysis for Gene Drivers
     */
    private JFreeChart getChartSurvivorsMutTypeGeneDriver(
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeGeneDriver(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramSurvivorsMutTypeGeneDriverTitle, 
                    Texts.snHistogramMutTypeGeneDriverXAxis, 
                    Texts.snHistogramMutTypeGeneDriverYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }                

    /**
     * Creates the JFreeChart for Mutation Type Analysis for Gene Drivers
     * @return chart for Mutation Type Analysis for Gene Drivers
     */
    private JFreeChart getChartShadowMutTypeGeneDriver(
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeGeneDriver(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramShadowMutTypeGeneDriverTitle, 
                    Texts.snHistogramMutTypeGeneDriverXAxis, 
                    Texts.snHistogramMutTypeGeneDriverYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }           
    
    /**
     * Creates the JFreeChart for Mutation Type Analysis for Gene Passenger
     * @return chart for Mutation Type Analysis for Gene Passenger
     */
    private JFreeChart getChartSurvivorsMutTypeGenePassenger (
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeGenePassenger(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramMutTypeGenePassengerTitle, 
                    Texts.snHistogramMutTypeGenePassengerXAxis, 
                    Texts.snHistogramMutTypeGenePassengerYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }                

    /**
     * Creates the JFreeChart for Mutation Type Analysis for Gene Passenger
     * @return chart for Mutation Type Analysis for Gene Passenger
     */
    private JFreeChart getChartShadowMutTypeGenePassenger (
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeGenePassenger(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramMutTypeGenePassengerTitle, 
                    Texts.snHistogramMutTypeGenePassengerXAxis, 
                    Texts.snHistogramMutTypeGenePassengerYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }         
    
    /**
     * Creates the JFreeChart for Mutation Type Analysis for Locus Drivers
     * @return chart for Mutation Type Analysis for Locus Drivers
     */
    private JFreeChart getChartSurvivorsMutTypeLocusDriver(
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeLocusDriver(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramSurvivorsMutTypeLocusDriverTitle, 
                    Texts.snHistogramMutTypeLocusDriverXAxis, 
                    Texts.snHistogramMutTypeLocusDriverYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }                

    /**
     * Creates the JFreeChart for Mutation Type Analysis for Locus Drivers
     * @return chart for Mutation Type Analysis for Locus Drivers
     */
    private JFreeChart getChartShadowMutTypeLocusDriver(
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeLocusDriver(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramShadowMutTypeLocusDriverTitle, 
                    Texts.snHistogramMutTypeLocusDriverXAxis, 
                    Texts.snHistogramMutTypeLocusDriverYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }       
    
    /**
     * Creates the JFreeChart for Mutation Type Analysis for Locus Passenger
     * @return chart for Mutation Type Analysis for Locus Passenger
     */
    private JFreeChart getChartSurvivorsMutTypeLocusPassenger (
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetSurvivorsMutTypeLocusPassenger(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramMutTypeLocusPassengerTitle, 
                    Texts.snHistogramMutTypeLocusPassengerXAxis, 
                    Texts.snHistogramMutTypeLocusPassengerYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }                

    /**
     * Creates the JFreeChart for Mutation Type Analysis for Locus Passenger
     * @return chart for Mutation Type Analysis for Locus Passenger
     */
    private JFreeChart getChartShadowMutTypeLocusPassenger (
            boolean useGroupping,
            String geneTag){
        
        JFreeChart chart;
        SimpleHistogramDataset dataset 
                = mta.getSimpleHistogramDatasetShadowMutTypeLocusPassenger(useGroupping, geneTag);
        if (dataset != null){
            chart = ChartFactory.createHistogram(Texts.snHistogramMutTypeLocusPassengerTitle, 
                    Texts.snHistogramMutTypeLocusPassengerXAxis, 
                    Texts.snHistogramMutTypeLocusPassengerYAxis, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true);
            PlotterTools.setXYChartBackgroundProperties(chart,true,true);
            return chart;            
        } else {
            return null;
        }

    }  
        
    /**
     * Creates the JFreeChart for Driver/Passenger ratio
     * @return chart for driver/passenger ratio
     */
    private JFreeChart getChartDrivPassRatio(boolean useGroupping){
        
        JFreeChart chart;
        XYSeries series = getDataSerieDrivPassRatio(useGroupping);
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.historyDriverPassRatioTitle, 
                Texts.historyDriverPassRatioXAxis, 
                Texts.historyDriverPassRatioYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }    
    
    /**
     * Creates the JFreeChart for Cumulated Drivers
     * @return chart for cumulated drivers
     */
    private JFreeChart getChartCumulatedDriver(boolean useGroupping){
        
        JFreeChart chart;
        XYSeries series = getDataSerieCumulatedDriver(useGroupping);
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.historyDriveMutationTitle, 
                Texts.historyDriveMutationXAxis, 
                Texts.historyDriveMutationYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }    
    
    /**
     * Creates the JFreeChart for Cumulated Passengers
     * @return chart for cumulated passengers
     */
    private JFreeChart getChartCumulatedPassenger(boolean useGroupping){
        
        JFreeChart chart;
        XYSeries series = getDataSerieCumulatedPassenger(useGroupping);
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.historyPassengerMutationTitle, 
                Texts.historyPassengerMutationXAxis, 
                Texts.historyPassengerMutationYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }        
    
    /**
     * prepares the file name for the file saving with tag in name
     * @param subname   the plot distinctive description
     * @param extension file extension
     * @return 
     */
    private String prepareFileNameWithTag(String geneTag, String subname, String extension){
        if (Extension.dotPng.equals(extension)){
            return simModel.getFilePaths().getWorkDirGraphics() 
                    + java.io.File.separator 
                    + subname 
                    + " Tag "
                    + geneTag
                    + extension;                    
        } else {
            return simModel.getFilePaths().getWorkDirGraphicDataSource()
                    + java.io.File.separator 
                    + subname 
                    + " Tag "
                    + geneTag
                    + extension;                    
        }

    }

    /**
     * prepares the file name for the file saving
     * @param subname   the plot distinctive description
     * @param extension file extension
     * @return 
     */
    private String prepareFileName(String subname, String extension){
        if (Extension.dotPng.equals(extension)){
            return simModel.getFilePaths().getWorkDirGraphics() 
                    + java.io.File.separator 
                    + subname 
                    + extension;                    
        } else {
            return simModel.getFilePaths().getWorkDirGraphicDataSource()
                    + java.io.File.separator 
                    + subname 
                    + extension;                    
        }

    }
    
    /**
     * Data format conversion from Concurent Hash Map of Integer - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @return              XYChartSerie
     */    
    public XYChart.Series<Integer, Long> convertCHMAsIntegerSerie(ConcurrentHashMap<Integer, Long> cHM , boolean useGroupping, int resolution){
        ArrayList<Integer> keys;
        Integer maxKey = Integer.MIN_VALUE;
        Integer minKey = Integer.MAX_VALUE;
        Long valueTmp;
        Long value = new Long(0);
        keys = new ArrayList<> ();
        XYChart.Series<Integer, Long> series = new XYChart.Series<>();
        cHM.entrySet().forEach((entry) -> {
            keys.add(entry.getKey());
        });
        for (Integer i : keys){
            if ( i > maxKey )maxKey = i;
            if ( i < minKey )minKey = i;
        }
            
        if (useGroupping){
            if ( ( 2 * (maxKey - minKey) ) < resolution ){
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (valueTmp != null){
                        series.getData().add(new  XYChart.Data(key, valueTmp));
                    }
                }            
            } else {
                int pointDistance = (maxKey - minKey) / resolution;
                if (pointDistance < 1){
                    pointDistance = 1;
                }               
                int point = pointDistance;
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (point == 0){
                        if ( value != 0 ){
                            series.getData().add(new  XYChart.Data(key, value));                               
                        } 
                        if (valueTmp != null){
                            value = valueTmp;
                        } else {
                            value = (long)0;
                        }                           
                        point = pointDistance - 1;
                    } else {
                        if (valueTmp != null){
                            value += valueTmp;
                        }
                        point--;
                    }
                }    
            }            
        } else {
            for ( Integer key = minKey ; key <= maxKey ; key++){
                value = cHM.get(key);
                if (value != null){
                    series.getData().add(new  XYChart.Data(key, value));
                }

            }               
        }
        
        return series;
    }    

    /**
     * Data format conversion from Concurent Hash Map of Integer - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @return              XYChartSerie
     */    
    public XYChart.Series<Integer, Integer> convertCHMIIAsIntegerSerie(ConcurrentHashMap<Integer, Integer> cHM , boolean useGroupping, int resolution){
        ArrayList<Integer> keys;
        Integer maxKey = Integer.MIN_VALUE;
        Integer minKey = Integer.MAX_VALUE;
        Integer valueTmp;
        Integer value = new Integer(0);
        keys = new ArrayList<> ();
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        cHM.entrySet().forEach((entry) -> {
            keys.add(entry.getKey());
        });
        for (Integer i : keys){
            if ( i > maxKey )maxKey = i;
            if ( i < minKey )minKey = i;
        }
            
        if (useGroupping){
            if ( ( 2 * (maxKey - minKey) ) < resolution ){
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (valueTmp != null){
                        series.getData().add(new  XYChart.Data(key, valueTmp));
                    }
                }            
            } else {
                int pointDistance = (maxKey - minKey) / resolution;
                if (pointDistance < 1){
                    pointDistance = 1;
                }               
                int point = pointDistance;
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (point == 0){
                        if ( value != 0 ){
                            series.getData().add(new  XYChart.Data(key, value));                               
                        } 
                        if (valueTmp != null){
                            value = valueTmp;
                        } else {
                            value = (int)0;
                        }                           
                        point = pointDistance - 1;
                    } else {
                        if (valueTmp != null){
                            value += valueTmp;
                        }
                        point--;
                    }
                }    
            }            
        } else {
            for ( Integer key = minKey ; key <= maxKey ; key++){
                value = cHM.get(key);
                if (value != null){
                    series.getData().add(new  XYChart.Data(key, value));
                }

            }               
        }
        
        return series;
    }    
    
}
