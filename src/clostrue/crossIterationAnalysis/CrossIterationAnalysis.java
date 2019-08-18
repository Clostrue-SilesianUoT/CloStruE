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
package clostrue.crossIterationAnalysis;


import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.DirName;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.Header;
import clostrue.hardcodes.file.HeaderPart;
import clostrue.hardcodes.plots.Setting;
import clostrue.hardcodes.file.Name;
import clostrue.hardcodes.plots.Texts;
import clostrue.model.FilePaths;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.postprocessing.plotter.PlotterTools;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * Cross-Iteration Analysis for making results of all iterations
 * on same charts (and csv data for the basis of the charts)
 * 
 * @author Krzysztof Szymiczek 
 */

public class CrossIterationAnalysis {

    private final HashMap<Integer, ArrayList<CsvEntryForQuotedClones>>     quotedClones;
    private final HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>>  quotedDrivers;
    private final HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>>  quotedPassengers;
    
    private String rootPath;
    private String repeatedDir;
    private String graphicsDir;
    private String graphicsSourceDir;
    
    private boolean clones;
    private boolean drivers;
    private boolean passengers;
    
    private final Analytics analytics;
    private final int quotasCount;
    
    public CrossIterationAnalysis(Analytics analytics) {
        
        this.analytics = analytics;
        
        quotedClones             = new HashMap<>();
        quotedDrivers            = new HashMap<>();
        quotedPassengers         = new HashMap<>();
        
        repeatedDir = "";
        graphicsDir = "";
        graphicsSourceDir = "";

        clones = analytics.getSim().getSettings().getBooleanValue(Param.cbClonesScatter);
        drivers = analytics.getSim().getSettings().getBooleanValue(Param.cbDriversSactter);
        passengers = analytics.getSim().getSettings().getBooleanValue(Param.cbPassengersScatter);
        
        quotasCount = analytics.getSim().getSettings().getIntValue(Param.teQuota) + 1;
        
    }
    
    /**
     * Creates cross-iteration csv data from all iterations for the curent simulation
     */
    public void createCSVs() {
                
        try {
            if(clones) {
                for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                    createCSVFileQuotedClonesPerCycle(quotaId, Name.fileNameClonesPerCycleHavingQuota, quotedClones);                    
                    saveGluedQuotedClonesDataToFile(quotaId, Constant.fileNameClonesPerPopSizeHavingQuota, quotedClones);
                }
            }
            if(drivers) {
                for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                    createCSVFileQuotedMutationsPerCycle(quotaId, Name.fileNameDriverMutationsPerCycleHavingQuota, quotedDrivers);                     
                    saveGluedQuotedMutationsDataToFile(quotaId, Name.fileNameDriverMutationsPerPopSizeHavingQuota, quotedDrivers);
                }                              
            }
            if(passengers) {
                for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                    createCSVFileQuotedMutationsPerCycle(quotaId, Name.fileNamePassengerMutationsPerCycleHavingQuota, quotedPassengers);                     
                    saveGluedQuotedMutationsDataToFile(quotaId, Name.fileNamePassengerMutationsPerPopSizeHavingQuota, quotedPassengers);
                }     
            }
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Create charts for the cross-iteration analysis
     */
    public void createCharts() {
        if(clones) {
            for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                createChartForQuotedClonesPerCycle(quotaId, 
                    quotedClones, 
                    Texts.rsaChartClonesTitle, 
                    Texts.rsaChartXAxisCycle, 
                    Texts.rsaChartYAxisClon, 
                    Name.rsaChartFileNameClonesPerCycle);                
                createChartQuotedClonesPerPopulationSize(quotaId, 
                    quotedClones, 
                    Texts.rsaChartClonesTitle, 
                    Texts.rsaChartXAxisPopSize, 
                    Texts.rsaChartYAxisClon, 
                    Name.rsaChartFileNameClonesPerPopSize);                
            }
        }
        
        if(drivers) {
            for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                createChartForQuotedMutationsPerCycle(quotaId, 
                    quotedDrivers, 
                    Texts.rsaChartDriversTitle, 
                    Texts.rsaChartXAxisCycle,
                    Texts.rsaChartYAxisdriv, 
                    Name.rsaChartFileNameDriversPerCycle);
                createChartQuotedMutationsPerPopulationSize(quotaId, 
                    quotedDrivers, 
                    Texts.rsaChartDriversTitle, 
                    Texts.rsaChartXAxisPopSize,
                    Texts.rsaChartYAxisdriv, 
                    Name.rsaChartFileNameDriversPerPopSize);
            }
        }
        
        if(passengers) {
            for (int quotaId = 0; quotaId < quotasCount; quotaId++){
                createChartForQuotedMutationsPerCycle(quotaId, 
                    quotedPassengers, 
                    Texts.rsaChartPassengersTitle, 
                    Texts.rsaChartXAxisCycle,
                    Texts.rsaChartYAxispass, 
                    Name.rsaChartFileNamePassengersPerCycle);
                createChartQuotedMutationsPerPopulationSize(quotaId, 
                    quotedPassengers, 
                    Texts.rsaChartPassengersTitle, 
                    Texts.rsaChartXAxisPopSize,
                    Texts.rsaChartYAxispass, 
                    Name.rsaChartFileNamePassengersPerPopSize);
            }
        }       
    }
    
    /**
     * Reads data from each iteration of the simulation (from Tech Output Folders
     * of correspondinf iterations)
     * @param inRootPath root path
     */
    public void readTechDataFromIterations(String inRootPath) {
        rootPath = inRootPath;

        for(String p : getPathsForIterationFolders(rootPath)) {
            String path = rootPath
                        + java.io.File.separator
                        + p
                        + java.io.File.separator
                        + DirName.subDirTechOutput
                        + java.io.File.separator;
            
            if (clones)
                readQuotedClonesCsvData(quotedClones, path + Name.quotedClones + Extension.dotCsv);
            if (drivers)
                readQuotedMutationsCsvData(quotedDrivers, path + Name.quotedDrivers + Extension.dotCsv, true);
            if (passengers)
                readQuotedMutationsCsvData(quotedPassengers, path + Name.quotedPassengers + Extension.dotCsv, false);
        }

    }

    /**
     * Creates chars for data per cycle
     * @param quotaId quota index 
     * @param w Csv data
     * @param title Chart title
     * @param xAxis xAxis description
     * @param yAxis yAxis description
     * @param fileName file name to save chart into
     */
    private void createChartForQuotedClonesPerCycle(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w, String title, String xAxis, String yAxis, String fileName) {
               
        String quotedFileName = fileName + "_" +  String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        StaticConsoleLogger.logRSA(Activity.rsaStartChart + quotedFileName);
        
        XYDataset dataset = createClonesCountQuotaDatasetPerCycle(quotaId, w);
        String quotedTitle = title + " having quota > " + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        JFreeChart chart = ChartFactory.createScatterPlot(quotedTitle, xAxis, yAxis, dataset);
        
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        
        PlotterTools.prepareXYChartToSaveToPNGFile(chart);
        String filename = getRepeatedDirGraphics()
                        + java.io.File.separator
                        + quotedFileName
                        + Extension.dotPng;

        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart,  Setting.size,  Setting.size);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedChart + quotedFileName);
    }

    /**
     * Creates chars for data per cycle
     * @param quotaId quota index 
     * @param w Csv data
     * @param title Chart title
     * @param xAxis xAxis description
     * @param yAxis yAxis description
     * @param fileName file name to save chart into
     */
    private void createChartForQuotedMutationsPerCycle(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w, String title, String xAxis, String yAxis, String fileName) {
               
        String quotedFileName = fileName + "_" +  String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        StaticConsoleLogger.logRSA(Activity.rsaStartChart + quotedFileName);
        
        XYDataset dataset = createMutationsCountQuotaDatasetPerCycle(quotaId, w);
        String quotedTitle = title + " having quota > " + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        JFreeChart chart = ChartFactory.createScatterPlot(quotedTitle, xAxis, yAxis, dataset);
        
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        
        PlotterTools.prepareXYChartToSaveToPNGFile(chart);
        String filename = getRepeatedDirGraphics()
                        + java.io.File.separator
                        + quotedFileName
                        + Extension.dotPng;

        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart,  Setting.size,  Setting.size);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedChart + quotedFileName);
    }    
    
    /**
     * Creates chars for data per population size
     * @param quotaId quota index 
     * @param w Csv data
     * @param title Chart title
     * @param xAxis xAxis description
     * @param yAxis yAxis description
     * @param fileName file name to save chart into
     */
    private void createChartQuotedClonesPerPopulationSize(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w, String title, String xAxis, String yAxis, String fileName) {
        
        String quotedFileName = fileName + "_" +  String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        StaticConsoleLogger.logRSA(Activity.rsaStartChart + quotedFileName);
        
        XYDataset dataset = createClonesCountQuotaDatasetPerPopSize(quotaId, w);
        String quotedTitle = title + " having quota > " + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        JFreeChart chart = ChartFactory.createScatterPlot(quotedTitle, xAxis, yAxis, dataset);
        
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        
        PlotterTools.prepareXYChartToSaveToPNGFile(chart);
        String filename = getRepeatedDirGraphics()
                        + java.io.File.separator
                        + quotedFileName
                        + Extension.dotPng;

        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart,  Setting.size,  Setting.size);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedChart + quotedFileName);
    }    

    /**
     * Creates chars for data per population size
     * @param quotaId quota index 
     * @param w Csv data
     * @param title Chart title
     * @param xAxis xAxis description
     * @param yAxis yAxis description
     * @param fileName file name to save chart into
     */
    private void createChartQuotedMutationsPerPopulationSize(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w, String title, String xAxis, String yAxis, String fileName) {
        
        String quotedFileName = fileName + "_" +  String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        StaticConsoleLogger.logRSA(Activity.rsaStartChart + quotedFileName);
        
        XYDataset dataset = createMutationsCountQuotaDatasetPerPopSize(quotaId, w);
        String quotedTitle = title + " having quota > " + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));
        JFreeChart chart = ChartFactory.createScatterPlot(quotedTitle, xAxis, yAxis, dataset);
        
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        
        PlotterTools.prepareXYChartToSaveToPNGFile(chart);
        String filename = getRepeatedDirGraphics()
                        + java.io.File.separator
                        + quotedFileName
                        + Extension.dotPng;

        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart,  Setting.size,  Setting.size);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedChart + quotedFileName);
    }    

    /**
     * Create dataset for charts
     * @param quotaId quota ID
     * @param w csv entries
     * @return dataset
     */
    private XYDataset createClonesCountQuotaDatasetPerPopSize(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w){
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {            
            for(CsvEntryForQuotedClones csvE : e.getValue()) {
                series.add(csvE.getPopSize(), csvE.getClonesPerQuotaId(quotaId));
            }
        }
        dataset.addSeries(series); 
        
        return dataset;
    }

    /**
     * Create dataset for charts
     * @param quotaId quota ID
     * @param w csv entries
     * @return dataset
     */
    private XYDataset createMutationsCountQuotaDatasetPerPopSize(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w){
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {            
            for(CsvEntryForQuotedMutations csvE : e.getValue()) {
                series.add(csvE.getPopSize(), csvE.getMutationCountPerQuotaId(quotaId));
            }
        }
        dataset.addSeries(series); 
        
        return dataset;
    }    
    /**
     * Create dataset for charts
     * @param quotaId quota ID
     * @param w csv entries
     * @return dataset
     */    
    private XYDataset createClonesCountQuotaDatasetPerCycle(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w){
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {            
            for(CsvEntryForQuotedClones csvE : e.getValue()) {
                series.add(csvE.getCycle(), csvE.getClonesPerQuotaId(quotaId));
            }
        }
        dataset.addSeries(series); 
        
        return dataset;
    } 
    
    /**
     * Create dataset for charts
     * @param quotaId quota ID
     * @param w csv entries
     * @return dataset
     */    
    private XYDataset createMutationsCountQuotaDatasetPerCycle(int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w){
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {            
            for(CsvEntryForQuotedMutations csvE : e.getValue()) {
                series.add(csvE.getCycle(), csvE.getMutationCountPerQuotaId(quotaId));
            }
        }
        dataset.addSeries(series); 
        
        return dataset;
    } 
        

    /**
     * Find max cycle of all iterations
     * @param w csv data
     * @return max cycle
     */    
    private int findMaxCycle( int quotaId, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w){
        int max = 0;
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {
            if(e.getValue().size() > max) {
                max = e.getValue().size();
            }
        }
        
        return max;
        
    }

    /**
     * Find max cycle of all iterations
     * @param w csv data
     * @return max cycle
     * Quota Id only for having different signature
     */    
    private int findMaxCycle( int quotaId, int quotaId2, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w){
        int max = 0;
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {
            if(e.getValue().size() > max) {
                max = e.getValue().size();
            }
        }
        
        return max;
        
    }

    /**
     * Create csv data for all interations
     * @param quotaId quota ID
     * @param inFileName file name
     * @param w csv data
     * @throws IOException 
     */    
    private void createCSVFileQuotedClonesPerCycle(int quotaId, String inFileName, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w) throws IOException {
                
        String quotaFileName = inFileName + "_" + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));        
        String fileName = getRepeatedDirGraphicsDataSources()
                        + java.io.File.separator 
                        + quotaFileName
                        + Extension.dotCsv;
         
        ;
        
        StaticConsoleLogger.logRSA(Activity.rsaStartCSV + fileName);
        
        FileWriter     fileWriter     = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        createCSVFileHeader(quotaId, bufferedWriter, w);
        
        HashMap<Integer, String> lines = new HashMap<>();
        int               max   = findMaxCycle(quotaId, w);
        
        for(int i = 0; i < max; i++) {
            lines.put(i, "" + i);
        }
                
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {
            ArrayList<CsvEntryForQuotedClones> entries = e.getValue();
            for(int i = 0; i < max; i++) {
                if(i < entries.size()) {
                    lines.replace(i, lines.get(i) + Artifact.csvSeparator + entries.get(i).getClonesPerQuotaId(quotaId));
                } else {
                    lines.replace(i, lines.get(i) + Artifact.csvSeparator + "");
                }
            }
        }
        
        for(Map.Entry<Integer, String> l : lines.entrySet()) {
            bufferedWriter.write(l.getValue());
            bufferedWriter.write(Artifact.outCSVeol);
        }
        
        bufferedWriter.flush();
        bufferedWriter.close();
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedCSV + fileName);
    }    

    /**
     * Create csv data for all interations
     * @param quotaId quota ID
     * @param inFileName file name
     * @param w csv data
     * @throws IOException 
     */    
    private void createCSVFileQuotedMutationsPerCycle(int quotaId, String inFileName, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w) throws IOException {
                
        String quotaFileName = inFileName + "_" + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));        
        String fileName = getRepeatedDirGraphicsDataSources()
                        + java.io.File.separator 
                        + quotaFileName
                        + Extension.dotCsv;
         
        ;
        
        StaticConsoleLogger.logRSA(Activity.rsaStartCSV + fileName);
        
        FileWriter     fileWriter     = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        createCSVFileHeader(quotaId, quotaId, bufferedWriter, w);
        
        HashMap<Integer, String> lines = new HashMap<>();
        int               max   = findMaxCycle(quotaId, quotaId, w);
        
        for(int i = 0; i < max; i++) {
            lines.put(i, "" + i);
        }
                
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {
            ArrayList<CsvEntryForQuotedMutations> entries = e.getValue();
            for(int i = 0; i < max; i++) {
                if(i < entries.size()) {
                    lines.replace(i, lines.get(i) + Artifact.csvSeparator + entries.get(i).getMutationCountPerQuotaId(quotaId));
                } else {
                    lines.replace(i, lines.get(i) + Artifact.csvSeparator + "");
                }
            }
        }
        
        for(Map.Entry<Integer, String> l : lines.entrySet()) {
            bufferedWriter.write(l.getValue());
            bufferedWriter.write(Artifact.outCSVeol);
        }
        
        bufferedWriter.flush();
        bufferedWriter.close();
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedCSV + fileName);
    }   

    /**
     * Create header for CSV data file
     * @param bufferedWriter
     * @param w csv data
     * quotaId only for signature differentiation
     */    
    private void createCSVFileHeader(int quotaId, BufferedWriter bufferedWriter, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w) {
        String header = HeaderPart.headerMulti;
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {
            header += Artifact.csvSeparator + HeaderPart.headerMultiSeparator + e.getKey();
        }
        
        try {
            bufferedWriter.write(header);
            bufferedWriter.write(Artifact.outCSVeol);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create header for CSV data file
     * @param bufferedWriter
     * @param w csv data
     * quotaId only for signature differentiation
     */    
    private void createCSVFileHeader(int quotaId, int quotaId2, BufferedWriter bufferedWriter, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w) {
        String header = HeaderPart.headerMulti;
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {
            header += Artifact.csvSeparator + HeaderPart.headerMultiSeparator + e.getKey();
        }
        
        try {
            bufferedWriter.write(header);
            bufferedWriter.write(Artifact.outCSVeol);
        } catch (IOException ex) {
            Logger.getLogger(CrossIterationAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets path list to folders containing separate iterations
     * @param rootPath start point for search
     * @return path list
     */
    private ArrayList<String> getPathsForIterationFolders(String rootPath) {
        File file = new File(rootPath);
        String[] directories = file.list(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
          }
        });
        
        return new ArrayList<>(Arrays.asList(directories));
    }

    /**
     * Reads the quoted csv data for the given path 
     * @param w csv data
     * @param path path from which the file is read
     */    
    private void readQuotedClonesCsvData(HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w, String path) {
        String  line = "";
        String  a    = path.substring(path.indexOf("Iteration_") + 10, path.indexOf(File.separatorChar + DirName.subDirTechOutput));
        int     iter = Integer.parseInt(a);
        boolean skip = true;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                if(skip) {
                    skip = false;
                    continue;
                }
                String[] data = line.split(Artifact.csvSeparator);
                int[] clonesQuota = new int[quotasCount];
                for (int i = 0; i < quotasCount; i++){
                    clonesQuota[i] = Integer.parseInt(data[i+4]);   //+4 as the fifth column contains data for the quota id 0...
                }
                addToWMap(w, iter, new CsvEntryForQuotedClones(Integer.parseInt(data[0]),
                                                 clonesQuota,
                                                 Integer.parseInt(data[2]),
                                                 Integer.parseInt(data[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    /**
     * Reads the quoted csv data for the given path 
     * @param w csv data
     * @param path path from which the file is read
     */    
    private void readQuotedMutationsCsvData(HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w, String path, boolean list) {
        String  line = "";
        String  a    = path.substring(path.indexOf("Iteration_") + 10, path.indexOf(File.separatorChar + DirName.subDirTechOutput));
        int     iter = Integer.parseInt(a);
        boolean skip = true;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                if(skip) {
                    skip = false;
                    continue;
                }
                String[] data = line.split(Artifact.csvSeparator);
                int[] quotedMutationsCount = new int[quotasCount];
                String[] quotedMutationsList = new String[quotasCount];
                for (int i = 0; i < quotasCount; i++){
                    quotedMutationsCount[i] = Integer.parseInt(data[i+4]);   //+4 as the fifth column contains data for the quota id 0...
                    if (list == true){
                        quotedMutationsList[i] = new String(data[i+4+quotasCount]);
                        if (quotedMutationsList[i].equals("."))
                            quotedMutationsList[i] = "";                        
                    }
                }
                addToWMap(w, iter, new CsvEntryForQuotedMutations(Integer.parseInt(data[0]),
                                                 quotedMutationsCount,
                                                 quotedMutationsList,
                                                 Integer.parseInt(data[2]),
                                                 Integer.parseInt(data[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }        

    /**
     * Add entry to map
     * @param w map
     * @param iter iteration
     * @param entry entry
     */    
    private void addToWMap(HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w, int iter, CsvEntryForQuotedClones entry) {
        ArrayList<CsvEntryForQuotedClones> list = w.get(iter);
        
        if(list == null) {
            list = new ArrayList<>();
            list.add(entry);
            w.put(iter, list);
        } else {
            list.add(entry);
        }
    }

    /**
     * Add entry to map
     * @param w map
     * @param iter iteration
     * @param entry entry
     */    
    private void addToWMap(HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w, int iter, CsvEntryForQuotedMutations entry) {
        ArrayList<CsvEntryForQuotedMutations> list = w.get(iter);
        
        if(list == null) {
            list = new ArrayList<>();
            list.add(entry);
            w.put(iter, list);
        } else {
            list.add(entry);
        }
    }
    
    /**
     * saves cross-iteration quoted data 
     * (glued from single iteration files) to one final file
     * @param name file name
     * @param w data
     * @throws IOException 
     */    
    private void saveGluedQuotedClonesDataToFile(int quotaId, String name, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w) throws IOException {
        
        String quotedName = name + "_" + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));;
        
        String fileName = getRepeatedDirGraphicsDataSources()
                        + java.io.File.separator
                        + quotedName
                        + Extension.dotCsv;
        
        StaticConsoleLogger.logRSA(Activity.rsaStartCSV + quotedName);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        putGluedQuotedClonesDataToCreatedFile(quotaId, bufferedWriter, w);        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedCSV + quotedName);
    }
    
    private void saveGluedQuotedMutationsDataToFile(int quotaId, String name, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w) throws IOException {
        
        String quotedName = name + "_" + String.format("%.2f", analytics.getQuotaTresholdById(quotaId));;
        
        String fileName = getRepeatedDirGraphicsDataSources()
                        + java.io.File.separator
                        + quotedName
                        + Extension.dotCsv;
        
        StaticConsoleLogger.logRSA(Activity.rsaStartCSV + quotedName);
        
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
        
        putGluedQuotedMutationsDataToCreatedFile(quotaId, bufferedWriter, w);        
        bufferedWriter.flush();
        bufferedWriter.close(); 
        
        StaticConsoleLogger.logRSA(Activity.rsaFinishedCSV + quotedName);
    }
    
    /**
     * Convers HasMap data to file content
     * @param bufferedWriter file to write into
     * @param w data to write
     * @throws IOException 
     */    
    private void putGluedQuotedClonesDataToCreatedFile(int quotaId, BufferedWriter bufferedWriter, HashMap<Integer, ArrayList<CsvEntryForQuotedClones>> w) throws IOException {
        addHeaderToCreatedFile(bufferedWriter);
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedClones>> e : w.entrySet()) {
            ArrayList<CsvEntryForQuotedClones> entries = e.getValue();
            
            for(CsvEntryForQuotedClones en : entries) {
                bufferedWriter.write(String.format("%d;%d;%d;%d", en.getPopSize(), en.getClonesPerQuotaId(quotaId), en.getIteration(), en.getCycle()));
                bufferedWriter.write(Artifact.outCSVeol);
            }
        }
    }
    
    /**
     * Convers HasMap data to file content
     * @param bufferedWriter file to write into
     * @param w data to write
     * @throws IOException 
     */    
    private void putGluedQuotedMutationsDataToCreatedFile(int quotaId, BufferedWriter bufferedWriter, HashMap<Integer, ArrayList<CsvEntryForQuotedMutations>> w) throws IOException {
        addHeaderToCreatedFile(bufferedWriter);
        
        for (Map.Entry<Integer, ArrayList<CsvEntryForQuotedMutations>> e : w.entrySet()) {
            ArrayList<CsvEntryForQuotedMutations> entries = e.getValue();
            
            for(CsvEntryForQuotedMutations en : entries) {
                bufferedWriter.write(String.format("%d;%d;%d;%d", en.getPopSize(), en.getMutationCountPerQuotaId(quotaId), en.getIteration(), en.getCycle()));
                bufferedWriter.write(Artifact.outCSVeol);
            }
        }
    }    
    
    /**
     * Add header to created file
     * @param bufferedWriter file to wrtie into
     * @throws IOException 
     */
    private void addHeaderToCreatedFile(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(Header.multiPopSizeCSVFileHeader);
        bufferedWriter.write(Artifact.outCSVeol);
    }
    
    /**
     * Create or returns the directory for cross-iteration analysis
     * @return path
     */
    private String getRepeatedDir() {
        if ("".equals(repeatedDir)){
            repeatedDir = rootPath + java.io.File.separator + DirName.subDirCrossIteration;           
            try {
                Files.createDirectories(Paths.get(repeatedDir));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return repeatedDir;
    }
    
    /**
     * Create or returns the directory for cross-iteration analysis 
     * subdir Graphics
     * @return path
     */
    private String getRepeatedDirGraphics() {
        if ("".equals(graphicsDir)){
            graphicsDir = getRepeatedDir() + java.io.File.separator + DirName.subDirGraphics;           
            try {
                Files.createDirectories(Paths.get(graphicsDir));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return graphicsDir;
    }

    /**
     * Create or returns the directory for cross-iteration analysis 
     * subdir Graphics data soruce
     * @return path
     */    
    private String getRepeatedDirGraphicsDataSources() {
        if ("".equals(graphicsSourceDir)){
            graphicsSourceDir = getRepeatedDir() + java.io.File.separator + DirName.subDirGraphicsDataSource;           
            try {
                Files.createDirectories(Paths.get(graphicsSourceDir));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return graphicsSourceDir;
    }
}
