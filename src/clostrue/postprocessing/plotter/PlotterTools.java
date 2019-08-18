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
 
import clostrue.GuiController;
import clostrue.Settings;
import clostrue.toolbox.MathTools;
import clostrue.toolbox.StaticConsoleLogger;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.LogText;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.plots.Setting;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.chart.XYChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 * VARIOUS PLOTTER AND DATA SERIES OPERATIONS
 * 
 */
public class PlotterTools {
    
    
    
    /**
     * Data format conversion from Concurent Hash Map of Double - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @param logToConsole
     * @return              XYChartSerie
     */        
    public static XYChart.Series<Double, Long> convertCHMAsDoubleSerie(
            ConcurrentHashMap<Double, Long> cHM, 
            boolean useGroupping, 
            int resolution, 
            boolean logToConsole){
        ArrayList<Double> keys;
        keys = new ArrayList<> ();
        XYChart.Series<Double, Long> series = new XYChart.Series<>();
        
        cHM.entrySet().forEach((entry) -> {
            keys.add(entry.getKey());
            if (logToConsole){
                StaticConsoleLogger.log(
                LogText.histogramKey + entry.getKey().toString() +
                LogText.histogramValue + entry.getValue().toString()
                );
            }
        });

        Collections.sort(keys);

        if (useGroupping){
            int pointDistance = keys.size() / resolution;
            if (pointDistance < 1){
                pointDistance = 1;
            }
            int point = pointDistance;
            Double calculatedKey = new Double(0);  
            Long   calculatedVal = new Long(0);
            for ( Double key : keys ){
                if (point == 0){
                    calculatedKey /= pointDistance;
                    series.getData().add(new XYChart.Data(MathTools.round(calculatedKey,6), calculatedVal));                                
                    calculatedVal = cHM.get(key);
                    calculatedKey = key;
                    point = pointDistance - 1;
                } else {
                    calculatedVal += cHM.get(key);
                    calculatedKey += key;
                    point--;
                }
            }                  
        } else {
            Long value;
            for ( Double key : keys ){
                value = cHM.get(key);
                if (value == null){
                    series.getData().add(new  XYChart.Data(MathTools.round(key,6), new Long(0)));
                } else {
                    series.getData().add(new  XYChart.Data(MathTools.round(key,6), value));
                }

            }                  
        }

        return series;
    }          

    /**
     * saves single HistogramDataset to csv file
     * @param iteration
     * @param dataset Histogram DataSet
     * @param fullFileName filename to save the category dataset
     * @param xDescription
     * @param yDescription
     * @param yIntRound
     */
    public static void saveSimpleHistogramDataSetToCSV(int iteration, SimpleHistogramDataset dataset, String fullFileName, String xDescription, String yDescription, boolean yIntRound){
        
        if (dataset == null){
            return;
        }
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fullFileName, Activity.started);

        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try {
            fileWriter = new FileWriter(fullFileName);
            bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
            bufferedWriter.write(xDescription + Artifact.csvColumnSeparator);
            bufferedWriter.write(yDescription + Artifact.csvColumnSeparator);
            bufferedWriter.write(Artifact.outCSVeol);
            
            for (int i = 0; i < dataset.getItemCount(0); i++){
                try {
                    bufferedWriter.write(String.valueOf((int)Math.round(dataset.getX(0, i).doubleValue())));
                    bufferedWriter.write(Artifact.csvColumnSeparator);
                    if (!yIntRound){
                        bufferedWriter.write(String.valueOf(dataset.getY(0, i)));
                    } else {
                        dataset.getEndYValue(0, 0);
                        bufferedWriter.write(String.valueOf((int)Math.round((double)dataset.getY(0, i))));                        
                    }
                    bufferedWriter.write(Artifact.outCSVeol);                    
                } catch (IOException ex) {
                    Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fullFileName, Activity.finished);
        
    }  

    /**
     * Sets the XY chart backrouund properties
     * @param chart chart which should be changed in regard to backround properties
     * @param bars
     * @param integerTickUnits
     */
    public static void setXYChartBackgroundProperties(JFreeChart chart, boolean bars, boolean integerTickUnits){
        chart.getPlot().setBackgroundPaint(java.awt.Color.WHITE);
        if (chart.getXYPlot() != null){
            chart.getXYPlot().setDomainGridlinePaint(java.awt.Color.GRAY);
            chart.getXYPlot().setRangeGridlinePaint(java.awt.Color.GRAY); 
            chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
            if (bars){            
                XYBarRenderer renderer1 = (XYBarRenderer) chart.getXYPlot().getRenderer();
                StandardXYBarPainter xybp = new StandardXYBarPainter();
                renderer1.setBarPainter(xybp);
                if (integerTickUnits){
                    chart.getXYPlot().getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());                    
                }
                chart.getXYPlot().setDomainZeroBaselineVisible(false);
            }
        }

    }    

    /**
     * Prepares a JFreeChart of XY type to be saved to file (mostly resizing elements of the plot)
     * @param chartToSave chart to be prepared for saving
     */
    public static void prepareXYChartToSaveToPNGFile(JFreeChart chartToSave){

        if (chartToSave == null) return;
        Font font;
        font = chartToSave.getXYPlot().getDomainAxis().getTickLabelFont();
        chartToSave.getXYPlot().getDomainAxis().setTickLabelFont(new Font(font.getFontName(), Font.PLAIN, 
                (int)(Setting.tickLabelFontSizeRatio * Setting.size)));
        chartToSave.getXYPlot().getRangeAxis().setTickLabelFont(chartToSave.getXYPlot().getDomainAxis().getTickLabelFont());  
        font = chartToSave.getTitle().getFont();
        chartToSave.getTitle().setFont(new Font(font.getFontName(), Font.PLAIN, 
                (int)(Setting.titleFontSizeRatio * Setting.size)));
        font = chartToSave.getXYPlot().getDomainAxis().getLabelFont();
        chartToSave.getXYPlot().getDomainAxis().setLabelFont(new Font(font.getFontName(), Font.PLAIN, 
                        (int)(Setting.axisLabelFontSizeRatio * Setting.size)));
        chartToSave.getXYPlot().getRangeAxis().setLabelFont(chartToSave.getXYPlot().getDomainAxis().getLabelFont());
        chartToSave.getXYPlot().getRenderer().setSeriesStroke(0, 
                new BasicStroke((int)(Setting.seriesStrokeFontSizeRatio * Setting.size)));
    }    

    /**
     * Saves the chart as PNG with additional logging to console
     * @param iteration
     * @param file
     * @param chart
     * @param width
     * @param height
     */
    public static void saveChartAsPNG(int iteration, File file, JFreeChart chart, int width, int height){

        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + file.getAbsolutePath(), Activity.started);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, width, width);
        } catch(IOException ex) {
            StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + file.getAbsolutePath(), Activity.ioException, ex.toString());
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + file.getAbsolutePath(), Activity.finished);

    }    

    public static int[] convertAtomicIntegerArrayToIntArray(AtomicIntegerArray aia){
        int[] conv = new int[aia.length()];
        for (int i = 0; i < aia.length() ; i++){
            conv[i] = aia.get(i);
        }
        return conv;
    }
    
    /**
     * Creates the DataSerie for plot based on a table of integer values
     * updated for each cycle of the simulation
     * @param values
     * @param useGroupping
     * @param description
     * @param resolution
     * @param curentCycle
     * @param maxCycles
     * @return 
     */
    static public XYSeries getDataSerieFromIntTable(
            int[] values, 
            boolean useGroupping, 
            String description, 
            int resolution,
            int curentCycle,
            int maxCycles){
        
        XYSeries series = new XYSeries(description);
        
        if (curentCycle == maxCycles+1){
            curentCycle--;
        }
        
        if (useGroupping){
            int pointDistance = (maxCycles+1) / resolution;
            if (pointDistance < 1){
                pointDistance = 1;
            }        

            int point = pointDistance;
            int value = 0;     
            for (int i = 0; i <= curentCycle; i++) {
                if (point == 0){
                    value /= pointDistance;
                    series.add(i, value);                
                    value = values[i];
                    point = pointDistance - 1;
                } else {
                    value += values[i];
                    point--;
                }
            }            
        } else {
            for (int i = 0; i <= curentCycle; i++) {
                series.add(i, values[i]);
            }            
        }
        
        return series;
    }    

    /**
     * Creates the DataSerie for plot based on a table of double values
     * updated for each cycle of the simulation
     * @param values
     * @param useGroupping
     * @param description
     * @param resolution
     * @param curentCycle
     * @param maxCycles
     * @param trashLastOne
     * @return 
     */
    static public XYSeries getDataSerieFromDoubleTable(
            double[] values, 
            boolean useGroupping, 
            String description, 
            int resolution,
            int curentCycle,
            int maxCycles,
            boolean trashLastOne){
        
        XYSeries series = new XYSeries(description);
        
        if (curentCycle == maxCycles+1){
            curentCycle--;
        }
        
        if (useGroupping){
            int pointDistance = (maxCycles+1) / resolution;
            if (pointDistance < 1){
                pointDistance = 1;
            }        

            int point = pointDistance;
            double value = 0;     
            for (int i = 0; i <= curentCycle; i++) {
                if (point == 0){
                    value /= pointDistance;
                    series.add(i, value);                
                    value = values[i];
                    point = pointDistance - 1;
                } else {
                    value += values[i];
                    point--;
                }
            }            
        } else {
            if (trashLastOne == true){
                curentCycle--;
            }
            for (int i = 0; i <= curentCycle; i++) {
                series.add(i, values[i]);
            }            
        }
        
        return series;
    }    

    /**
     * saves single data serie of integers to csv file
     * @param iteration iteration
     * @param series data serie
     * @param fullFileName filename to save the serie
     * @param xDescription
     * @param yDescription
     * @param yIntRound
     */
    public static void saveDataSerieToCSV(int iteration, XYSeries series, String fullFileName, String xDescription, String yDescription, boolean yIntRound){
        
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fullFileName, Activity.started);
        
        try {
            fileWriter = new FileWriter(fullFileName);
            bufferedWriter = new BufferedWriter(fileWriter, Constant.fileBufferSize);
            bufferedWriter.write(xDescription + Artifact.csvColumnSeparator);
            bufferedWriter.write(yDescription + Artifact.csvColumnSeparator);
            bufferedWriter.write(Artifact.outCSVeol);
            
            series.getItems().forEach((i) -> {
                XYDataItem item = (XYDataItem) i;
                try {
                    bufferedWriter.write(String.valueOf(item.getXValue()));
                    bufferedWriter.write(Artifact.csvColumnSeparator);
                    if (!yIntRound){
                        bufferedWriter.write(String.valueOf(item.getYValue()));                        
                    } else {
                        bufferedWriter.write(String.valueOf((int)item.getYValue()));                                                
                    }                    
                    bufferedWriter.write(Artifact.outCSVeol);                    
                } catch (IOException ex) {
                    Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            bufferedWriter.flush();
            bufferedWriter.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Plotter.class.getName()).log(Level.SEVERE, null, ex);
        }

        StaticConsoleLogger.logActivity(iteration, Activity.saveLvl2 + fullFileName, Activity.finished);
        
    }

    public XYChart.Series<Integer, Long> convertCHMAsIntegerSerie(ConcurrentHashMap<Integer, Long> cHM, int resolution, Settings settings) {
        ArrayList<Integer> keys;
        Integer maxKey = Integer.MIN_VALUE;
        Integer minKey = Integer.MAX_VALUE;
        Long valueTmp;
        Long value = new Long(0);
        keys = new ArrayList<>();
        XYChart.Series<Integer, Long> series = new XYChart.Series<>();
        cHM.entrySet().forEach((Map.Entry<Integer, Long> entry) -> {
            keys.add(entry.getKey());
        });
        for (Integer i : keys) {
            if (i > maxKey) {
                maxKey = i;
            }
            if (i < minKey) {
                minKey = i;
            }
        }
        if ((2 * (maxKey - minKey)) < resolution) {
            for (Integer key = minKey; key <= maxKey; key++) {
                valueTmp = cHM.get(key);
                if (valueTmp == null) {
                    series.getData().add(new XYChart.Data(String.valueOf(key), 1));
                } else {
                    series.getData().add(new XYChart.Data(String.valueOf(key), valueTmp));
                }
            }
        } else {
            int pointDistance = (maxKey - minKey) / resolution;
            if (pointDistance < 1) {
                pointDistance = 1;
            }
            int point = pointDistance;
            for (Integer key = minKey; key <= maxKey; key++) {
                valueTmp = cHM.get(key);
                if (point == 0) {
                    if (value != 0) {
                        series.getData().add(new XYChart.Data(String.valueOf(key), value));
                    } else {
                        series.getData().add(new XYChart.Data(String.valueOf(key), 1));
                    }
                    if (valueTmp != null) {
                        value = valueTmp;
                    } else {
                        value = (long) 0;
                    }
                    point = pointDistance - 1;
                } else {
                    if (valueTmp != null) {
                        value += valueTmp;
                    }
                    point--;
                }
            }
        }
        return series;
    }

    /**
     * Data format conversion from Concurent Hash Map of Long - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @param insertZeroes  should zeros be explicitly inserted to data?
     * @param settings
     * @return              XYChartSerie
     */    
    public static XYChart.Series<Long, Long> convertCHMAsLongSerie(ConcurrentHashMap<Long, Long> cHM , boolean useGroupping, int resolution, boolean insertZeroes, Settings settings){
        ArrayList<Long> keys;
        Long maxKey = Long.MIN_VALUE;
        Long minKey = Long.MAX_VALUE;
        Long valueTmp;
        Long value = new Long(0);
        keys = new ArrayList<> ();
        XYChart.Series<Long, Long> series = new XYChart.Series<>();
        if (cHM == null){
            return null;
        }
        cHM.entrySet().forEach((entry) -> {
            keys.add(entry.getKey());
        });
        for (Long i : keys){
            maxKey = Math.max(maxKey, i);
            minKey = Math.min(minKey, i);
        }
            
        if (useGroupping){
            if ( ( 2 * (maxKey - minKey) ) < resolution ){
                for ( Long key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (valueTmp == null){
                        if (insertZeroes){
                            series.getData().add(new  XYChart.Data(key, new Long(0)));                            
                        }
                    } else {
                        series.getData().add(new  XYChart.Data(key, valueTmp));
                    }
                }            
            } else {
                Long pointDistance = (maxKey - minKey) / resolution;
                if (pointDistance < 1){
                    pointDistance = new Long(1);
                }               
                Long point = pointDistance;
                for ( Long key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (point == 0){
                        if ( value != 0 ){
                            series.getData().add(new  XYChart.Data(key, value));                               
                        } else {
                            if (insertZeroes){
                               series.getData().add(new  XYChart.Data(key, new Long(0)));                                 
                            }
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
            for ( Long key = minKey ; key <= maxKey ; key++){
                value = cHM.get(key);
                if (value == null){
                    if (insertZeroes){
                       series.getData().add(new  XYChart.Data(key, new Long(0)));                        
                    }
                } else {
                    series.getData().add(new  XYChart.Data(key, value));
                }

            }               
        }
        
        return series;
    }    

    /**
     * Data format conversion from Concurent Hash Map of Long - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @param insertZeroes  should zeros be explicitly inserted to data?
     * @param settings
     * @return              XYChartSerie
     */    
    public static XYChart.Series<Long, Long> convertCHMIIAsLongSerie(ConcurrentHashMap<Integer, Integer> cHM , boolean useGroupping, int resolution, boolean insertZeroes, Settings settings){
        ArrayList<Long> keys;
        Long maxKey = Long.MIN_VALUE;
        Long minKey = Long.MAX_VALUE;
        Integer valueTmp;
        Integer value = new Integer(0);
        keys = new ArrayList<> ();
        XYChart.Series<Long, Long> series = new XYChart.Series<>();
        if (cHM == null){
            return null;
        }
        cHM.entrySet().forEach((entry) -> {
            keys.add((long)entry.getKey());
        });
        for (Long i : keys){
            maxKey = Math.max(maxKey, i);
            minKey = Math.min(minKey, i);
        }
            
        if (useGroupping){
            if ( ( 2 * (maxKey - minKey) ) < resolution ){
                for ( Long key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (valueTmp == null){
                        if (insertZeroes){
                            series.getData().add(new  XYChart.Data(key, new Long(0)));                            
                        }
                    } else {
                        series.getData().add(new  XYChart.Data(key, valueTmp));
                    }
                }            
            } else {
                Long pointDistance = (maxKey - minKey) / resolution;
                if (pointDistance < 1){
                    pointDistance = new Long(1);
                }               
                Long point = pointDistance;
                for ( Long key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (point == 0){
                        if ( value != 0 ){
                            series.getData().add(new  XYChart.Data(key, value));                               
                        } else {
                            if (insertZeroes){
                               series.getData().add(new  XYChart.Data(key, new Long(0)));                                 
                            }
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
            for ( Long key = minKey ; key <= maxKey ; key++){
                value = cHM.get(key);
                if (value == null){
                    if (insertZeroes){
                       series.getData().add(new  XYChart.Data(key, new Long(0)));                        
                    }
                } else {
                    series.getData().add(new  XYChart.Data(key, value));
                }

            }               
        }
        
        return series;
    }        

    /**
     * Data format conversion from Concurent Hash Map of Long - Long
     * @param cHM           concurrent HashMap
     * @param useGroupping  groupping
     * @param resolution    groupping resolution
     * @param insertZeroes  should zeros be explicitly inserted to data?
     * @param settings
     * @return              XYChartSerie
     */    
    public static XYChart.Series<Integer, Integer> convertCHMIIAsIntegerSerie(ConcurrentHashMap<Integer, Integer> cHM , boolean useGroupping, int resolution, boolean insertZeroes, Settings settings){
        ArrayList<Integer> keys;
        Integer maxKey = Integer.MIN_VALUE;
        Integer minKey = Integer.MAX_VALUE;
        Integer valueTmp;
        Integer value = new Integer(0);
        keys = new ArrayList<> ();
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        if (cHM == null){
            return null;
        }
        cHM.entrySet().forEach((entry) -> {
            keys.add(entry.getKey());
        });
        for (Integer i : keys){
            maxKey = Math.max(maxKey, i);
            minKey = Math.min(minKey, i);
        }
            
        if (useGroupping){
            if ( ( 2 * (maxKey - minKey) ) < resolution ){
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (valueTmp == null){
                        if (insertZeroes){
                            series.getData().add(new  XYChart.Data(key, new Integer(0)));                            
                        }
                    } else {
                        series.getData().add(new  XYChart.Data(key, valueTmp));
                    }
                }            
            } else {
                Integer pointDistance = (maxKey - minKey) / resolution;
                if (pointDistance < 1){
                    pointDistance = new Integer(1);
                }               
                Integer point = pointDistance;
                for ( Integer key = minKey ; key <= maxKey ; key++){
                    valueTmp = cHM.get(key);
                    if (point == 0){
                        if ( value != 0 ){
                            series.getData().add(new  XYChart.Data(key, value));                               
                        } else {
                            if (insertZeroes){
                               series.getData().add(new  XYChart.Data(key, new Integer(0)));                                 
                            }
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
                if (value == null){
                    if (insertZeroes){
                       series.getData().add(new  XYChart.Data(key, new Integer(0)));                        
                    }
                } else {
                    series.getData().add(new  XYChart.Data(key, value));
                }

            }               
        }
        
        return series;
    }     
    
    /**
     * Creates the DataSerie for plot based on a XYChart Serie of Long - Long
     * Used for histograms
     * @param inSerie
     * @return 
     */
    public static SimpleHistogramDataset getHistogramDatasetFromXYChartSeriesLongLong(XYChart.Series<Long, Long> inSerie){
    
        if (inSerie == null){
            return null;
        }
        if (inSerie.getData().isEmpty()){
            return null;
        }        
        
        SimpleHistogramDataset dataset = new SimpleHistogramDataset(1);
        
        inSerie.getData().stream().map((entry) -> {
            dataset.addBin(new SimpleHistogramBin(entry.getXValue()-0.5, entry.getXValue()+0.4999999,true,true));
            return entry;
        }).forEachOrdered((entry) -> {
            // do not forget to divide by the bin width if the bin width will change in the future (now it is more or less one)
            for (Long repetitions = new Long(0); repetitions < entry.getYValue(); repetitions++){
                dataset.addObservation(entry.getXValue());
            }
        });

        return dataset;
    }   

    /**
     * Creates the DataSerie for plot based on a XYChart Serie of Long - Long
     * Used for histograms
     * @param inSerie
     * @return 
     */
    public static SimpleHistogramDataset getHistogramDatasetFromXYChartSeriesIntegerInteger(XYChart.Series<Integer, Integer> inSerie){
    
        if (inSerie == null){
            return null;
        }
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
    
}
