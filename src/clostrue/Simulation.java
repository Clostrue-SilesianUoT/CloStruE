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
 
import clostrue.model.mam.MutationAdvModel;
import clostrue.biology.genome.GenomePart;
import clostrue.biology.cell.Cell;
import clostrue.benchmark.ParallelBenchmark;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.hardcodes.Activity;
import clostrue.postprocessing.analysis.Statistics;
import clostrue.hardcodes.EvtProperty;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.Constant;
import clostrue.loadballancer.LoadBallancer;
import clostrue.loadballancer.LoadBallancerDefault;
import clostrue.loadballancer.LoadBallancerEmpty;
import clostrue.loadballancer.LoadBallancerEqualizeCellCount;
import clostrue.loadballancer.LoadBallancerEqualizeCellCountWithGain;
import clostrue.loadballancer.LoadBallancerEqualizeDutyCycleByRatio;
import clostrue.loadballancer.LoadBallancerEqualizeDutyCycleByRatioWithGain;
import clostrue.model.SimModel;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.postprocessing.visualization.Fishplot;
import clostrue.crossIterationAnalysis.CrossIterationAnalysis;
import clostrue.hardcodes.FishPlot;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the whole simulation controll and progress.
 *
 * This class object is responsible for synchronisation between calculation
 * tasks used for the calculations performed for single simulation
 *
 * @author Krzysztof Szymiczek
 */
public class Simulation {

    private final GenomeSynchronizedCollection genomes;             //Collection of cell genomes
    public CyclicBarrier barrier;                       //synchronization object for parallel tasks
    private final AtomicBoolean technicalLastSaveCycle; //flag for last cycle for saving data
    private final SimModel simModel;                    //simulation model
    private final LiveStats liveStats;                  //Live! Statistics
    private long  loadBalancerTotalTime = 0;            //load ballancer total time
    private final LoadBallancer loadBallancer;          //load ballancer
    private final ParallelBenchmark benchmark;          //parallel benchmark
    public int cCurrFullProgress;                       //full progress
    private ExecutorService threadPool;                 //the pool of all calculation tasks              
    int currentCycle;                                   //current cycle of the simulation
    private int lastProgess;                            //last notified progress value
    private final boolean cbPrepareCells;               //Prepare Cell Files
    private final PropertyChangeListener pl;            //Reference to calling application
    private final List<CalcTask> calcTasks;             //All the calculation tasks used in simulation
    private final Settings settings;                    //settings object
    private final Statistics statistics;                //simulaton statistics
    private final Analytics analytics;                  //simulation analytics
    private Fishplot fishplot;                          //fishplot object
    private final int iteration;                        //current iteration
    private boolean stopped = false;                    //simulation is stopped
    private final CrossIterationAnalysis repSimAnalysis;
    int lastCycle       = 0;
    static public int currentIteration = 0;             //the number of current iteration

    private final GenomePart dummyGenomePart = new GenomePart();

    public List<CalcTask> getCalcTasks() {
        return calcTasks;
    }

    public LoadBallancer getLoadBallancer() {
        return loadBallancer;
    }    
    
    public GenomePart getDummyGenomePart() {
        return dummyGenomePart;
    }
   
    public boolean isStopped() {
        return stopped;
    }

//    // this calculates the probability that in the current live tick
//    // the driver mutation will occur
    public double getDriverMutationProbability() {
        return simModel.getModParams().getMutRate() 
                * simModel.getModParams().getMAM().getMpTargetDriversTd();
    }
//
//    // this calculates the probability that in the current live tick
//    // the passenger mutation will occur
    public double getPassengerMutationProbability() {
        return simModel.getModParams().getMutRate() 
                * simModel.getModParams().getMAM().getMpTargetPassengersTp();
    }

//    //this calculates the probability that there will be no mutation
//    //in the current cell division
    public double getNoMutationProbability() {
        double _noMutationProb = ((double) 1.0)
                - getDriverMutationProbability()
                - getPassengerMutationProbability();
        if (_noMutationProb < 0.0) {
            _noMutationProb = 0;
        }
        return _noMutationProb;
    }    
    
    //corrects the cell count in the current cycle by a delta value
    //which is determined by a single calculation tasks.
    public synchronized void correctCurrentHistoryCellCount(int correction) {
        statistics.correctHistoryCellCount(currentCycle, correction);
    }
    
    //lets all the calculation tasks know, that a new simulation cycle has begun.
    //all the calculation tasks with cell population equal to zero (empty), will
    //receive the half of cells from the calculation tasks with the biggest population.
    //(idea of load balancing)
    public void setNextCycle() {
        currentCycle++;
        if (currentCycle <= simModel.getModParams().getMaxCycles()) {
            statistics.copyHistoryCellCountNFromPreviousCycle(currentCycle);
            if (getCurrentCycle() > 1)
                loadBallancer.ballanceWork();                    
        }

    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    /**
     * Simulation constructor.
     *
     * This constructor is called each time a new simulation is started.
     *
     * @param inIteration            Curent Iteration
     * @param mutationAdvantageModel Mutation Advantage Model (Control Paramter)
     * @param pl                     Property change listener (GUI)
     * @param inSettings             Simulation settings
     */
    public Simulation(
            Integer inIteration,
            MutationAdvModel mutationAdvantageModel,
            PropertyChangeListener pl,
            Settings inSettings) {
        
        genomes                     = new GenomeSynchronizedCollection();
        technicalLastSaveCycle      = new AtomicBoolean(false);
        currentCycle                = 0;
        iteration                   = inIteration;
        this.pl                     = pl;
        lastProgess                 = 0;
        settings                    = inSettings;
        stopped                     = false;
        simModel                    = new SimModel(settings, mutationAdvantageModel, iteration);
        liveStats                   = new LiveStats(simModel);
        cbPrepareCells              = settings.getBooleanValue(Param.cbPrepareCells);
        statistics                  = new Statistics(this, settings);
        analytics                   = new Analytics(this);
        fishplot                    = new Fishplot(analytics, FishPlot.fractionDecimalPlaces);
        calcTasks                   = new ArrayList<>();   
        repSimAnalysis              = new CrossIterationAnalysis(analytics);
        
        barrier = new CyclicBarrier(
                simModel.getTechParams().getSimTasksCount(),
                new Runnable() {
                    public void run(){
                        doOnBarrier();
                    }
                });
        
        currentIteration++;
        Cell.clearLastCellID();
        GenomePart.clearLastCloneGroupID();
        this.settings.saveUnderLocation(simModel.getFilePaths().getWorkDir() + java.io.File.separator + "Simulation_Setup.txt");


        //set the update rate for Live! View
        cCurrFullProgress = Constant.cFullProgress1;
        
//        loadBallancer = new LoadBallancerEmpty(this,"",false);
//        loadBallancer = new LoadBallancerDefault(this);
//        loadBallancer = new LoadBallancerEqualizeDutyCycleByRatio(this);
//        loadBallancer = new LoadBallancerEqualizeDutyCycleByRatioWithGain(this);
//        loadBallancer = new LoadBallancerEqualizeDutyCycleBySubtraction(this);
        loadBallancer = new LoadBallancerEqualizeCellCount(this);
//        loadBallancer = new LoadBallancerEqualizeDutyCycleBySubtractionWithGain(this);
//        loadBallancer = new LoadBallancerEqualizeCellCountWithGain(this);
        benchmark = new ParallelBenchmark(this);
    }

    public GenomeSynchronizedCollection getGenomes() {
        return genomes;
    }
    
    /*
    What to do on barrier to synchronize work ?
    */
    private void doOnBarrier(){
        if (!technicalLastSaveCycle.get()){
            determineTechnicalLastSaveCycle();
            updateHistoryStats();
            setNextCycle();
        } else {
            updateHistoryStats();
            calcTasks.get(0).doFirePropertyChange(EvtProperty.epFinishCurrentIteration, 0, 1);
            shutdownThreadPool();
        }        
    }
    
    public void determineTechnicalLastSaveCycle(){
        technicalLastSaveCycle.set((calculateProgress() >= cCurrFullProgress));
        if (technicalLastSaveCycle.get())
            StaticConsoleLogger.log("Determined technical last save cycle : " + String.valueOf(technicalLastSaveCycle.get()));
    }

    public void updateLiveModeVariables(int deltaCellCount, int deltaDrivers, int deltaPassengers){
        correctCurrentHistoryCellCount(deltaCellCount);
        liveStats.correctliDriverMutations(deltaDrivers);
        liveStats.correctliPassengerMutations(deltaPassengers);        
    }
    
    public boolean getTechnicalLastSaveCycle(){
        return technicalLastSaveCycle.get();
    }
    
    public void addLoadBallancingTime(long time){
        loadBalancerTotalTime += time;
    }

    public long getLoadBalancerTotalTime() {
        return loadBalancerTotalTime;
    }    
    
    public synchronized ParallelBenchmark getBenchmark() {
        return benchmark;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public Settings getSettings() {
        return settings;
    }
    
    public boolean isCbPrepareCells() {
        return cbPrepareCells;
    }

    public int getIteration() {
        return iteration;
    }

// this prepares tasks for running the execution parallel in background    
    /**
     * Creates the calculation tasks to be used in simulation calculations.
     *
     * This method creates the calculation tasks. Each calculation task will be
     * created with the subset of the cells from initial population. Size of the
     * subset is calculated from the ratio of the amount of calculation tasks to
     * the initial population size.
     *
     * @throws IOException
     */
    public void tasksPrepare() throws IOException {

        // calculate the starting cells subset size for each tasks
        // and the initial population size
        int cellsPerTask;
        cellsPerTask = (int) Math.round( (double)simModel.getModParams().getInitCellCountK()
                / (double)simModel.getTechParams().getSimTasksCount());
        for (int i = 0; i < simModel.getTechParams().getSimTasksCount() - 1; i++) {
            statistics.setInitialCellSubsetSizeInCycle(i, cellsPerTask);
            statistics.addCountToHistoryCellCountNInCycle(0, cellsPerTask);
            statistics.addCountToHistoryCellCountNInCycle(1, cellsPerTask);
        }
        statistics.setInitialCellSubsetSizeInCycle(
                simModel.getTechParams().getSimTasksCount() - 1, 
                simModel.getModParams().getInitCellCountK() - (simModel.getTechParams().getSimTasksCount() - 1) * cellsPerTask);
        statistics.addCountToHistoryCellCountNInCycle(0, statistics.getInitialCellSubsetSizeInCycle(simModel.getTechParams().getSimTasksCount() - 1));
        statistics.addCountToHistoryCellCountNInCycle(1, statistics.getInitialCellSubsetSizeInCycle(simModel.getTechParams().getSimTasksCount() - 1));

        //create mother cell
        Cell motherCell = new Cell(
           simModel,
            genomes,
                null);        

        // create tasks for background execution
        for (int i = 0; i < simModel.getTechParams().getSimTasksCount(); i++) {
            CalcTask task = new CalcTask(
                this,
                i,
                statistics.getInitialCellSubsetSizeInCycle(i),
                motherCell
            );
            task.addPropertyChangeListener(pl);
            calcTasks.add(task);
        }

    }

    /**
     * Executes all the calculation tasks at once in the background.
     */
    public void tasksExecute() {
        
        StaticConsoleLogger.logActivity(iteration, Activity.simulation, Activity.started);
        threadPool = Executors.newFixedThreadPool(simModel.getTechParams().getSimTasksCount() + 1);
        calcTasks.stream().forEach((task) -> {
            threadPool.submit(task);
        });
    
    }

    /**
     * Calculates the curent simulation progress.
     *
     * Additionally, when the curet progress is 1% of of the previous progress
     * and when the Live Mode! is switched on, also other properties are
     * updated, so the GUI can display actual information about the model.
     *
     * @return
     */

    synchronized public int calculateProgress(){

        int curCycle        = currentCycle;
        int maxCycle        = simModel.getModParams().getMaxCycles();
        int curCellCount    = statistics.getHistoryCellCountInCycle(curCycle);
        int maxCellCount    = simModel.getModParams().getMaxCellCountAtStartOfCycle();
        int progress        = 0;
               
        if        (stopped){
            progress = cCurrFullProgress;            
        } else if (curCycle >= maxCycle){
            progress = cCurrFullProgress;
        } else if (curCellCount >= maxCellCount){
            progress = cCurrFullProgress;
        } else if (curCellCount == 0){
            progress = cCurrFullProgress;
        } else {
            double processed = ((double) curCycle / (double) maxCycle);
            progress = (int) ( (double) processed * (double) cCurrFullProgress );
            if ( progress == cCurrFullProgress){
                progress -= 1;
            }
        }
        
        liveModeSetProgress(progress);        
        
        if (progress == cCurrFullProgress){
            lastCycle = curCycle;
        }
        
        return progress;
    }    

    public int getLastCycle() {
        return lastCycle;
    }

    synchronized public void updateHistoryStats(){
        statistics.setHistoryDriverMutationCountInCycle(currentCycle, liveStats.getLiDriverMutations());
        statistics.setHistoryPassengerMutationCountInCycle(currentCycle, liveStats.getLiPassengerMutations()); 
        statistics.calcHistoryDriverPassRationInCycle(currentCycle);
    }
    
    synchronized public void liveModeSetProgress(int progress){
        if (progress != lastProgess) {
            calcTasks.get(0).doFirePropertyChange(EvtProperty.epCurentProgress, lastProgess, progress);
            lastProgess = progress;
            // raise some propertyChanges to update the GUI
            calcTasks.get(0).doFirePropertyChange(EvtProperty.epCurentCycle, currentCycle - 1, currentCycle);
            calcTasks.get(0).doFirePropertyChange(EvtProperty.epCurentDriverMutations, liveStats.getLiDriverMutations() - 1, liveStats.getLiDriverMutations());
            calcTasks.get(0).doFirePropertyChange(EvtProperty.epCurentPassengerMutations, liveStats.getLiPassengerMutations() - 1, liveStats.getLiPassengerMutations());
            if (currentCycle > 1){
                calcTasks.get(0).doFirePropertyChange(EvtProperty.epCurentPopulationSize, 
                        liveStats.getPrevPopulationSize(), statistics.getHistoryCellCountInCycle(currentCycle));
               liveStats.setPrevPopulationSize(statistics.getHistoryCellCountInCycle(currentCycle));                            
            }
        }        
    }
    
    /**
     * Returns information about the curent (pending) simulation cycle.
     *
     * @return the number (numeric id) of the curent cycle.
     */
    public int getCurrentCycle() {
        return currentCycle;
    }

    public void shutdownThreadPool(){
        StaticConsoleLogger.logActivity(iteration, Activity.threadPool, Activity.destroyed);
        threadPool.shutdownNow();
    }
    
    public Statistics getStatistics() {
        return statistics;
    }

    public SimModel getSimModel() {
        return simModel;
    }

    public Fishplot getFishplot() {
        return fishplot;
    }

    public void destroyFishplot() {
        fishplot = null;
    } 

    public LiveStats getLiveStats() {
        return liveStats;
    }

    public CrossIterationAnalysis getRepSimAnalysis() {
        return repSimAnalysis;
    }
    
    
}
