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
package clostrue.benchmark;

import clostrue.toolbox.MathTools;
import clostrue.Settings;
import clostrue.Simulation;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.HeaderPart;
import clostrue.hardcodes.file.Name;
import clostrue.hardcodes.file.NamePart;
import clostrue.hardcodes.plots.Setting;
import clostrue.hardcodes.plots.Texts;
import clostrue.model.SimModel;
import clostrue.postprocessing.plotter.PlotterTools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This is a class for benchmarking the performance of parallel executed 
 * simulation
 * 
 * @author Krzysztof Szymiczek 
 */

public class ParallelBenchmark {
    
    private static final boolean NO_GROUPPING = false;
    private static final int RESOLUTION_NOT_SHRINKED = 0;
    private final static boolean DO_NOT_ROUND_TO_INTEGER = false;
    private final boolean TRASH_LAST_ONE = true;
    
    private Simulation simulation = null;
    private Settings settings = null;
    private final SimModel simModel;
    private final ConcurrentHashMap <Integer, ArrayList<BenchmarkEntry>> benchmarks;
    private final ConcurrentHashMap <Integer, Double> meanDutyCycle;
    private final ConcurrentHashMap <Double, Long> dutyCycleHistogramData;
    private final double[] meanDutyCyclesOverSimulationCycles;
    private final double[] dutyCyclesVarianceOverSimulationCycles;
    private final double[] dutyCyclesStdDeviationOverSimulationCycles;
    private final double[] populationSizeGainOverSimulationCycles;
    private Double totalMeanDutyCycle;
    private final long simulationBegin;
    private long simulationDuration;
    private final int iteration;
    private final String workDirFromGui;
    private final String fileWithBenchmarkSummary;
    
    public ParallelBenchmark(Simulation inSimulation) {
        
        simulation              = inSimulation;
        simModel                = new SimModel(simulation.getIteration(), inSimulation.getSimModel());
        settings                = this.simulation.getSettings();
        benchmarks              = new ConcurrentHashMap<>();
        meanDutyCycle           = new ConcurrentHashMap<>();   
        dutyCycleHistogramData  = new ConcurrentHashMap<>();
        simulationBegin         = System.nanoTime();
        iteration               = simulation.getIteration();

        workDirFromGui 
                = settings.getStringValue(Param.teWorkDir) 
                + java.io.File.separator;
        fileWithBenchmarkSummary = workDirFromGui + Name.benchmarkSummaryFileName;
        
        Long zero = new Long(0);
        for (int i = 0; i <= 100; i++){
            Double val = i / 100.0;
            dutyCycleHistogramData.put(MathTools.round(val,2), zero);
        }

        // create the table where process duty cycles over simulation cycles will be saved
        int maxNeededTableSize                      = settings.getIntValue(Param.inMaxCycles) + 1;
        meanDutyCyclesOverSimulationCycles          = new double[maxNeededTableSize];
        dutyCyclesVarianceOverSimulationCycles      = new double[maxNeededTableSize]; 
        dutyCyclesStdDeviationOverSimulationCycles  = new double[maxNeededTableSize];
        populationSizeGainOverSimulationCycles      = new double[maxNeededTableSize];
    }
    
    public synchronized void addEntry(BenchmarkEntry entry){
        ArrayList<BenchmarkEntry> entriesForCycle = benchmarks.get(entry.getCycle());
        if (entriesForCycle != null){
            entriesForCycle.add(entry);
        } else {
            entriesForCycle = new ArrayList<>();
            entriesForCycle.add(entry);
            benchmarks.put(entry.getCycle(), entriesForCycle);
        }
    }
    
    private long getMaxDurationForCycle(Integer cycle){
        long maxDuration = 0;
        ArrayList<BenchmarkEntry> entrySetForCycle = benchmarks.get(cycle);
        for (BenchmarkEntry entry : entrySetForCycle){
            maxDuration = Math.max(maxDuration, entry.getDuration());
        }
        return maxDuration;
        
    }
    
    public void doTheBenchmark(){
        prepareAnalysis();
        createBenchmarkFiles();
    }
    
    private void prepareAnalysis(){

        long simulationEnd = System.nanoTime();
        simulationDuration = simulationEnd - simulationBegin;
        ArrayList<Double> dutyCyclesForEachThread = new ArrayList<>();
        double summedMeanDutyCycle = 0;
        for( Map.Entry<Integer, ArrayList<BenchmarkEntry>> benchmarkEntries : benchmarks.entrySet()){
            int curentAnalyzedSimulationCycle = benchmarkEntries.getKey();
            int previousSimulationCycle = curentAnalyzedSimulationCycle - 1;
            long maxDurationForCycle = getMaxDurationForCycle(curentAnalyzedSimulationCycle);
            double summedDutyCycle = 0;
            dutyCyclesForEachThread.clear();
            for (BenchmarkEntry entry : benchmarkEntries.getValue()){
                entry.calculateDutyCycle(maxDurationForCycle);
                dutyCyclesForEachThread.add(entry.getDutyCycle());
                summedDutyCycle += entry.getDutyCycle();
                Double roundedKey = MathTools.round(entry.getDutyCycle(),2);
                Long count = dutyCycleHistogramData.get(roundedKey);
                if (count != null){
                    count++;
                    dutyCycleHistogramData.replace(roundedKey, count);
                }
            }
            double calculatedMeanDutyCycle = (double)summedDutyCycle / (double)benchmarkEntries.getValue().size();
            meanDutyCycle.put(curentAnalyzedSimulationCycle, calculatedMeanDutyCycle);
            meanDutyCyclesOverSimulationCycles[curentAnalyzedSimulationCycle] = calculatedMeanDutyCycle;
            summedMeanDutyCycle += calculatedMeanDutyCycle;
        
            //append variances over mean duty cycle for each simualtion cycle
            Double variance = new Double(0);
            variance = dutyCyclesForEachThread.stream().map((dutyCycle) -> Math.pow((dutyCycle-calculatedMeanDutyCycle),2)).reduce(variance, (accumulator, _item) -> accumulator + _item);
            variance /= dutyCyclesForEachThread.size();

            dutyCyclesVarianceOverSimulationCycles[curentAnalyzedSimulationCycle] = variance;            
            dutyCyclesStdDeviationOverSimulationCycles[curentAnalyzedSimulationCycle] 
                    = Math.sqrt(dutyCyclesVarianceOverSimulationCycles[curentAnalyzedSimulationCycle]);
           
            if (curentAnalyzedSimulationCycle > 0){
                populationSizeGainOverSimulationCycles[curentAnalyzedSimulationCycle]
                    = (double)simulation.getStatistics().getHistoryCellCountInCycle(curentAnalyzedSimulationCycle)
                        / (double)simulation.getStatistics().getHistoryCellCountInCycle(previousSimulationCycle);                            
                if (populationSizeGainOverSimulationCycles[curentAnalyzedSimulationCycle] == 0){
                    populationSizeGainOverSimulationCycles[curentAnalyzedSimulationCycle] = 1;                    
                }
            } else {
                populationSizeGainOverSimulationCycles[curentAnalyzedSimulationCycle] = 1;
            }
            
        }
        totalMeanDutyCycle = summedMeanDutyCycle / meanDutyCycle.values().size();
    }
    
    private void createBenchmarkFiles(){
    
        saveJFreeChartMeanDutyCycleOverTimeToCSV();
        saveJFreeChartMeanDutyCycleOverTimeToPNG();
        saveJFreeChartDutyCyclesVarianceOverTimeToCSV();
        saveJFreeChartDutyCyclesVarianceOverTimeToPNG();
        saveJFreeChartDutyCyclesStdDeviationOverTimeToCSV();
        saveJFreeChartDutyCyclesStdDeviationOverTimeToPNG();
        saveJFreeChartPopulationSizeGainOverTimeToCSV();
        saveJFreeChartPopulationSizeGainOverTimeToPNG();

        saveBenchmarkKeyFiguresToTxt();
        
    }

    private void saveBenchmarkKeyFiguresToTxt(){

        boolean APPEND_TRUE = true;
        
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter1 = null;
        BufferedWriter bufferedWriter1 = null;
        try {
            String fileName = simModel.getFilePaths().getWorkDirBenchmark() + java.io.File.separator + Name.benchmarkKeyFiguresFileName;
            fileWriter = new FileWriter(fileName);
            bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);

            Long simulationDurationInSeconds = TimeUnit.SECONDS.convert(simulationDuration, TimeUnit.NANOSECONDS);
            bufferedWriter.write("Simulation duration in seconds: " + simulationDurationInSeconds.toString());
            bufferedWriter.write(Artifact.outCSVeol);

            bufferedWriter.write("Calculation Threads: " + simModel.getTechParams().getSimTasksCount());
            bufferedWriter.write(Artifact.outCSVeol);            
            
            bufferedWriter.write("Duty Cycles: MEAN: " + totalMeanDutyCycle.toString());
            bufferedWriter.write(Artifact.outCSVeol);

            bufferedWriter.write("Used Load Ballancer: " + simulation.getLoadBallancer().getLoadBallancerAlgorithmName());
            bufferedWriter.write(Artifact.outCSVeol);           

            Long loadBallancerTotalTimeInSeconds = TimeUnit.SECONDS.convert(simulation.getLoadBalancerTotalTime(), TimeUnit.NANOSECONDS);
            bufferedWriter.write("Load Ballancing total time overhead in seconds: " + loadBallancerTotalTimeInSeconds);
            bufferedWriter.write(Artifact.outCSVeol);           

            Double loadBalancingTimeRatio 
                    = (double)simulation.getLoadBalancerTotalTime()
                    / (double)simulationDuration;                    
            bufferedWriter.write("Load Ballancing relational computation time consumption: " + loadBalancingTimeRatio);
            bufferedWriter.write(Artifact.outCSVeol);           

            Double variance = new Double(0);
            variance = meanDutyCycle.keySet().stream().map((key) -> Math.pow((meanDutyCycle.get(key)-totalMeanDutyCycle),2)).reduce(variance, (accumulator, _item) -> accumulator + _item);
            variance /= meanDutyCycle.keySet().size();
            bufferedWriter.write("Duty Cycles: VARIANCE: " + variance.toString());
            bufferedWriter.write(Artifact.outCSVeol);

            Double stdDev = Math.sqrt(variance);
            bufferedWriter.write("Duty Cycles: STDANDARD DEVIATION: " + stdDev.toString());
            bufferedWriter.write(Artifact.outCSVeol);
            
            int lastCycle = simulation.getCurrentCycle();
            if (lastCycle == simModel.getModParams().getMaxCycles()+1){
                lastCycle--;
            }            
            bufferedWriter.write("Cycles: " + String.valueOf(lastCycle));
            bufferedWriter.write(Artifact.outCSVeol);

            int cellCountAtBegin = simulation.getStatistics().getHistoryCellCountN().get(0);
            bufferedWriter.write("Cells at the begin of simulation: " + String.valueOf(cellCountAtBegin));
            bufferedWriter.write(Artifact.outCSVeol);

            int cellCountAtEnd = simulation.getStatistics().getHistoryCellCountN().get(lastCycle);
            bufferedWriter.write("Cells at the end of simulation: " + String.valueOf(cellCountAtEnd));
            bufferedWriter.write(Artifact.outCSVeol);
            
            double endToEndPopulationSizeGain = (double)cellCountAtEnd / (double)cellCountAtBegin;
            bufferedWriter.write("End-to-end population size gain: " + String.valueOf(endToEndPopulationSizeGain));
            bufferedWriter.write(Artifact.outCSVeol);

            bufferedWriter.write("Shadow population size: " + String.valueOf(simulation.getLiveStats().getShadowSize()));
            bufferedWriter.write(Artifact.outCSVeol);

            if (Files.notExists(Paths.get(fileWithBenchmarkSummary))){

                fileWriter1 = new FileWriter(fileWithBenchmarkSummary);
                bufferedWriter1 = new BufferedWriter(fileWriter1, Constant.fileBufferSize);
                
                bufferedWriter1.write(HeaderPart.parBenchFileHeadExecutionID);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadLoadBallancer);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadSimulationTime);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadParallelTasks);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadMeanDutyCycle);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadLoadBallancingTime);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadLoadBallToSimTimeRatio);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadDutyCycleVariance);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadDutyCycleStdDev);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadSimulationCycles);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadEEPopulationSize);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadShadowPopulationSize);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadCellsAtBegin);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);

                bufferedWriter1.write(HeaderPart.parBenchFileHeadCellsAtEnd);                
                bufferedWriter1.write(Artifact.csvColumnSeparator);
                
                bufferedWriter1.write(Artifact.outCSVeol);

            
            } else {
                fileWriter1 = new FileWriter(fileWithBenchmarkSummary, APPEND_TRUE);
                bufferedWriter1 = new BufferedWriter(fileWriter1, Constant.fileBufferSize);
            }

            bufferedWriter1.write(simModel.getFilePaths().getWorkDir());
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(simulation.getLoadBallancer().getLoadBallancerAlgorithmName());
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(simulationDurationInSeconds.toString());
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(String.valueOf(simModel.getTechParams().getSimTasksCount()));
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(totalMeanDutyCycle.toString().replace('.', ','));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(loadBallancerTotalTimeInSeconds.toString());            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(loadBalancingTimeRatio.toString().replace('.', ','));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(variance.toString().replace('.', ','));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(stdDev.toString().replace('.', ','));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(String.valueOf(lastCycle));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(String.valueOf(endToEndPopulationSizeGain).replace('.', ','));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(String.valueOf(simulation.getLiveStats().getShadowSize()));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);

            bufferedWriter1.write(String.valueOf(cellCountAtBegin));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);
            
            bufferedWriter1.write(String.valueOf(cellCountAtEnd));            
            bufferedWriter1.write(Artifact.csvColumnSeparator);
            
            bufferedWriter1.write(Artifact.outCSVeol);

        } catch (IOException ex) {
            Logger.getLogger(ParallelBenchmark.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedWriter.flush();
                bufferedWriter.close();
                bufferedWriter1.flush();
                bufferedWriter1.close();
            } catch (IOException ex) {
                Logger.getLogger(ParallelBenchmark.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
   
    private void saveJFreeChartPopulationSizeGainOverTimeToCSV(){
       
        XYSeries serie = getDataSeriePopulationSizeGainOverTime();
        String fileName = prepareFileName(NamePart.snPopulationSizeGainOverTime, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, 
                serie, 
                fileName, 
                Texts.populationSizeGainXAxis, 
                Texts.populationSizeGainYAxis,
                DO_NOT_ROUND_TO_INTEGER);
    }                   
        
    private void saveJFreeChartDutyCyclesStdDeviationOverTimeToCSV(){
       
        XYSeries serie = getDataSerieDutyCyclesStdDeviationOverTime();
        String fileName = prepareFileName(NamePart.snDutyCycleStdDevOverTime, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, 
                serie, 
                fileName, 
                Texts.dutyCyclesStdDevXAxis, 
                Texts.dutyCyclesStdDevYAxis,
                DO_NOT_ROUND_TO_INTEGER);
    }           
        
    private void saveJFreeChartDutyCyclesVarianceOverTimeToCSV(){
       
        XYSeries serie = getDataSerieDutyCyclesVarianceOverTime();
        String fileName = prepareFileName(NamePart.snDutyCycleVarianceOverTime, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, 
                serie, 
                fileName, 
                Texts.dutyCyclesVarianceXAxis, 
                Texts.dutyCyclesVarianceYAxis,
                DO_NOT_ROUND_TO_INTEGER);
    }        
            
    private void saveJFreeChartMeanDutyCycleOverTimeToCSV(){
       
        XYSeries serie = getDataSerieMeanDutyCycleOverTime();
        String fileName = prepareFileName(NamePart.snMeanDutyCycleOverTime, 
                Extension.dotCsv);
        PlotterTools.saveDataSerieToCSV(iteration, 
                serie, 
                fileName, 
                Texts.dutyCyclesXAxis, 
                Texts.dutyCyclesYAxis,
                DO_NOT_ROUND_TO_INTEGER);
    }        
       
    public void saveJFreeChartDutyCyclesVarianceOverTimeToPNG(){
       
        JFreeChart chartToSave = getChartDutyCyclesVarianceOverTime();
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snDutyCycleVarianceOverTime, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }           
    
    public void saveJFreeChartPopulationSizeGainOverTimeToPNG(){
       
        JFreeChart chartToSave = getChartPopulationSizeGainOverTime();
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snPopulationSizeGainOverTime, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }       
        
    public void saveJFreeChartDutyCyclesStdDeviationOverTimeToPNG(){
       
        JFreeChart chartToSave = getChartDutyCyclesStdDeviationOverTime();
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snDutyCycleStdDevOverTime, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }       
    
    public void saveJFreeChartMeanDutyCycleOverTimeToPNG(){
       
        JFreeChart chartToSave = getChartMeanDutyCycleOverTime();
        if (chartToSave != null){
            PlotterTools.prepareXYChartToSaveToPNGFile(chartToSave);
            String filename = prepareFileName(NamePart.snMeanDutyCycleOverTime, 
                    Extension.dotPng);
            PlotterTools.saveChartAsPNG(iteration, new File(filename), chartToSave, Setting.size, Setting.size);
        }

    }             

    private JFreeChart getChartPopulationSizeGainOverTime(){
        
        JFreeChart chart;
        XYSeries series = getDataSeriePopulationSizeGainOverTime();
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.populationSizeGainTitle, 
                Texts.populationSizeGainXAxis, 
                Texts.populationSizeGainYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }               
        
    private JFreeChart getChartDutyCyclesStdDeviationOverTime(){
        
        JFreeChart chart;
        XYSeries series = getDataSerieDutyCyclesStdDeviationOverTime();
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.dutyCyclesStdDevTitle, 
                Texts.dutyCyclesStdDevXAxis, 
                Texts.dutyCyclesStdDevYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }         
    
    private JFreeChart getChartDutyCyclesVarianceOverTime(){
        
        JFreeChart chart;
        XYSeries series = getDataSerieDutyCyclesVarianceOverTime();
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.dutyCyclesVarianceTitle, 
                Texts.dutyCyclesVarianceXAxis, 
                Texts.dutyCyclesVarianceYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }        
        
    private JFreeChart getChartMeanDutyCycleOverTime(){
        
        JFreeChart chart;
        XYSeries series = getDataSerieMeanDutyCycleOverTime();
        XYSeriesCollection dataset;

        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart(Texts.dutyCyclesTitle, 
                Texts.dutyCyclesXAxis, 
                Texts.dutyCyclesYAxis, 
                dataset, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                true);
        PlotterTools.setXYChartBackgroundProperties(chart,false,true);
        
        return chart;
    }

    private XYSeries getDataSeriePopulationSizeGainOverTime(){
        
        return PlotterTools.getDataSerieFromDoubleTable(populationSizeGainOverSimulationCycles, 
                NO_GROUPPING, 
                Texts.populationSizeGainTitle,
                RESOLUTION_NOT_SHRINKED,
                simulation.getCurrentCycle(),
                simModel.getModParams().getMaxCycles(),
                TRASH_LAST_ONE);
    }          
        
    private XYSeries getDataSerieDutyCyclesStdDeviationOverTime(){
        
        return PlotterTools.getDataSerieFromDoubleTable(dutyCyclesStdDeviationOverSimulationCycles, 
                NO_GROUPPING, 
                Texts.dutyCyclesStdDevTitle,
                RESOLUTION_NOT_SHRINKED,
                simulation.getCurrentCycle(),
                simModel.getModParams().getMaxCycles(),
                TRASH_LAST_ONE);
    }        
        
    private XYSeries getDataSerieDutyCyclesVarianceOverTime(){
        
        return PlotterTools.getDataSerieFromDoubleTable(dutyCyclesVarianceOverSimulationCycles, 
                NO_GROUPPING, 
                Texts.dutyCyclesVarianceTitle,
                RESOLUTION_NOT_SHRINKED,
                simulation.getCurrentCycle(),
                simModel.getModParams().getMaxCycles(),
                TRASH_LAST_ONE);
    }
    
    private XYSeries getDataSerieMeanDutyCycleOverTime(){
        
        return PlotterTools.getDataSerieFromDoubleTable(meanDutyCyclesOverSimulationCycles, 
                NO_GROUPPING, 
                Texts.dutyCyclesTitle,
                RESOLUTION_NOT_SHRINKED,
                simulation.getCurrentCycle(),
                simModel.getModParams().getMaxCycles(),
                TRASH_LAST_ONE);
    }   
    
    private String prepareFileName(String subname, String extension){
        return simModel.getFilePaths().getWorkDirBenchmark() 
                + java.io.File.separator 
                + subname 
                + extension;                    
    }
    
}

