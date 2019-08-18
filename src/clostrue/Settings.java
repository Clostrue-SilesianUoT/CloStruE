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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.DefConf;
import clostrue.hardcodes.ModelParam;

/**
 * Simulation settings this is a general class for storing settings
 * and interacting with all the controlls from application gui
 * which stores the configuration settings
 * 
 * @author Krzysztof Szymiczek
 */
public final class Settings {
   
    private static Properties properties;           //properties (all)
    private String fileName;                        //file name for storing
    private static GuiController applController;    //GUI Controller 

    /**
     * Default constructir
     * @param inController GUI Controller 
     */
    public Settings(GuiController inController) {
        applController = inController;
        properties = new Properties();
    }

    /**
     * Returns file name under which the configuration is expected
     * to be saved or retrieved
     * @return configuration file full path
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name under which the configuration is expected
     * to be saved or retrieved
     * @param inFileName configuration file full path
     */
    public void setFileName(String inFileName) {
        fileName = inFileName;
    }
    
    /**
     * load the configuration from file
     */
    public void loadFromFile(){

        File configFile = new File(fileName);

        try {
            try (FileReader reader = new FileReader(configFile)) {
                properties.clear();
                properties.load(reader);
            }
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
    }

    /**
     * Saves the configuration to file location previously set
     * @return success
     */
    public boolean saveToFile(){

        return saveUnderLocation(fileName);
    }      
    
    /**
     * Saves the configuration to pointed file location
     * @param fName file location to save
     * @return success
     */
    public boolean saveUnderLocation(String fName){

        File configFile = new File(fName);
 
        try {
            try (FileWriter writer = new FileWriter(configFile)) {
                properties.store(writer, Constant.settingFileHeadeLine);
                writer.flush();
                writer.close();
            }
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
        
        return true;
        
    }      
     
    /**
     * Transfers settings from GUI controlls to properties table
     */
    public void tranferSettingsFromGUI(){
        
        properties.setProperty(Param.inIterations,applController.inIterations.getText());        
        properties.setProperty(Param.inInitialCellCount,applController.inInitialCellCount.getText());        
        properties.setProperty(Param.inMutationRate,applController.inMutationRate.getText());        
        properties.setProperty(Param.inDriverFitAdvantageTda,applController.inDriverFitAdvantageTda.getText());        
        properties.setProperty(Param.inDriverGenesTdg,applController.inDriverGenesTdg.getText());              
        properties.setProperty(Param.inDmSize,applController.inDmSize.getText());        
        properties.setProperty(Param.inPassengerFitAdvantageTpa,applController.inPassengerFitAdvantageTpa.getText());        
        properties.setProperty(Param.inPassengerGenesTpg,applController.inPassengerGenesTpg.getText());                
        properties.setProperty(Param.inPmSize,applController.inPmSize.getText());             
        properties.setProperty(Param.inMaxCycles,applController.inMaxCycles.getText());
        properties.setProperty(Param.inMaxCells,applController.inMaxCells.getText());
        properties.setProperty(Param.teParallelSimTasksCount,applController.teParallelSimTasksCount.getText());
        properties.setProperty(Param.teParallelProcTasksCount,applController.teParallelProcTasksCount.getText());
        properties.setProperty(Param.teWorkDir,applController.teWorkDir.getText());     
        properties.setProperty(Param.cbTeGenerateGraph,String.valueOf(applController.cbTeGenerateGraph.isSelected()));   
        properties.setProperty(Param.cbTeGenerateFishplot,String.valueOf(applController.cbTeGenerateFishplot.isSelected()));                     
        properties.setProperty(Param.mamPath, applController.mamPath.getText());
        properties.setProperty(Param.cbPrepareClones, String.valueOf(applController.cbPrepareClones.isSelected()));
        properties.setProperty(Param.teCellListerCutOff, applController.teCellListerCutOff.getText());
        properties.setProperty(Param.teCloneMinSize, applController.teCloneMinSize.getText());        
        properties.setProperty(Param.teCloneMinLifespan, applController.teCloneMinLifespan.getText());                      
        properties.setProperty(Param.ggRectangeScale, String.valueOf(applController.ggRectangeScale.isSelected()));        
        properties.setProperty(Param.teResUseGroupping, String.valueOf(applController.teResUseGroupping.isSelected()));        
        properties.setProperty(Param.cbPrepareCells, String.valueOf(applController.cbPrepareCells.isSelected()));
        properties.setProperty(Param.cbGenerateSimulationPNG, String.valueOf(applController.cbGenerateSimulationPNG.isSelected()));        
        properties.setProperty(Param.cbGenerateAnalyticsPNG, String.valueOf(applController.cbGenerateAnalyticsPNG.isSelected()));        
        properties.setProperty(Param.cbGenerateSurvivorsAnalytics, String.valueOf(applController.cbGenerateSurvivorsAnalytics.isSelected()));           
        properties.setProperty(Param.ggScaleToMaxSize, String.valueOf(applController.ggScaleToMaxSize.isSelected()));
        properties.setProperty(Param.ggMaxSize, applController.ggMaxSize.getText());
        properties.setProperty(Param.cbClonesScatter, String.valueOf(applController.cbClonesScatter.isSelected()));
        properties.setProperty(Param.cbDriversSactter, String.valueOf(applController.cbDriversSactter.isSelected()));
        properties.setProperty(Param.cbPassengersScatter, String.valueOf(applController.cbPassengersScatter.isSelected()));
        properties.setProperty(Param.teQuota, applController.teQuota.getText());
    }

    /**
     * transfer settings from properties table to GUI Controlls.
     */
    public void TransferSettingsToGUI(){
        
        applController.inIterations.setText(properties.getProperty(Param.inIterations,ModelParam.inIterations));
        applController.inInitialCellCount.setText(properties.getProperty(Param.inInitialCellCount,ModelParam.inInitialCellCount));
        applController.inMutationRate.setText(properties.getProperty(Param.inMutationRate,ModelParam.inMutationRate));
        applController.inDriverFitAdvantageTda.setText(properties.getProperty(Param.inDriverFitAdvantageTda,ModelParam.inDriverFitAdvantageTda));
        applController.inDriverGenesTdg.setText(properties.getProperty(Param.inDriverGenesTdg,ModelParam.inDriverGenesTdg));
        applController.inDmSize.setText(properties.getProperty(Param.inDmSize,ModelParam.inDmSize));
        applController.inPassengerFitAdvantageTpa.setText(properties.getProperty(Param.inPassengerFitAdvantageTpa,ModelParam.inPassengerFitAdvantageTpa));
        applController.inPassengerGenesTpg.setText(properties.getProperty(Param.inPassengerGenesTpg,ModelParam.inPassengerGenesTpg));
        applController.inPmSize.setText(properties.getProperty(Param.inPmSize,ModelParam.inPmSize));
        applController.inMaxCycles.setText(properties.getProperty(Param.inMaxCycles,ModelParam.inMaxCycles));
        applController.inMaxCells.setText(properties.getProperty(Param.inMaxCells,ModelParam.inMaxCells));
        applController.teParallelSimTasksCount.setText(properties.getProperty(Param.teParallelSimTasksCount,DefConf.teParallelSimTasksCount));
        applController.teParallelProcTasksCount.setText(properties.getProperty(Param.teParallelProcTasksCount,DefConf.teParallelProcTasksCount));
        applController.teWorkDir.setText(properties.getProperty(Param.teWorkDir,Constant.getTeWorkDir()));
        applController.cbTeGenerateGraph.setSelected(Boolean.valueOf(properties.getProperty(Param.cbTeGenerateGraph,DefConf.cbTeGenerateGraph)));
        applController.cbTeGenerateFishplot.setSelected(Boolean.valueOf(properties.getProperty(Param.cbTeGenerateFishplot,DefConf.cbTeGenerateFishplot)));       
        applController.mamPath.setText(properties.getProperty(Param.mamPath,ModelParam.mamPath));
        applController.cbPrepareClones.setSelected(Boolean.valueOf(properties.getProperty(Param.cbPrepareClones, DefConf.cbPrepareClones)));
        applController.teCellListerCutOff.setText(properties.getProperty(Param.teCellListerCutOff,DefConf.teCellListerCutOff));
        applController.teCloneMinSize.setText(properties.getProperty(Param.teCloneMinSize,DefConf.teCloneMinSize));
        applController.teCloneMinLifespan.setText(properties.getProperty(Param.teCloneMinLifespan,DefConf.teCloneMinLifespan));     
        applController.ggRectangeScale.setSelected(Boolean.valueOf(properties.getProperty(Param.ggRectangeScale, DefConf.ggRectangeScale)));
        applController.teResUseGroupping.setSelected(Boolean.valueOf(properties.getProperty(Param.teResUseGroupping, DefConf.teResUseGroupping)));        
        applController.cbPrepareCells.setSelected(Boolean.valueOf(properties.getProperty(Param.cbPrepareCells, DefConf.cbPrepareCells)));
        applController.cbGenerateSimulationPNG.setSelected(Boolean.valueOf(properties.getProperty(Param.cbGenerateSimulationPNG, DefConf.cbGenerateSimulationPNG)));
        applController.cbGenerateAnalyticsPNG.setSelected(Boolean.valueOf(properties.getProperty(Param.cbGenerateAnalyticsPNG, DefConf.cbGenerateAnalyticsPNG)));
        applController.cbGenerateSurvivorsAnalytics.setSelected(Boolean.valueOf(properties.getProperty(Param.cbGenerateSurvivorsAnalytics, DefConf.cbGenerateSurvivorsAnalytics)));
        applController.ggScaleToMaxSize.setSelected(Boolean.valueOf(properties.getProperty(Param.ggScaleToMaxSize, DefConf.ggScaleToMaxSize)));
        applController.ggMaxSize.setText(properties.getProperty(Param.ggMaxSize,DefConf.ggMaxSize));
        applController.cbClonesScatter.setSelected(Boolean.valueOf(properties.getProperty(Param.cbClonesScatter, DefConf.cbClonesScatter)));
        applController.cbDriversSactter.setSelected(Boolean.valueOf(properties.getProperty(Param.cbDriversSactter, DefConf.cbDriversSactter)));
        applController.cbPassengersScatter.setSelected(Boolean.valueOf(properties.getProperty(Param.cbPassengersScatter, DefConf.cbPassengersScatter)));
        applController.teQuota.setText(properties.getProperty(Param.teQuota,DefConf.teQuota));
    }
    
    /**
     * Sets default configuration
     */
    public void resetToDefaults(){
        properties.clear();
    }
    
    /**
     * Retrieve configuration setting as String
     * @param name name of the setting
     * @return value of the setting as String
     */
    public String getStringValue(String name){
        return properties.getProperty(name);
    }
    
    /**
     * Retrieve configuration setting as Integer
     * @param name name of the setting
     * @return value of the setting as Integer
     */
    public Integer getIntValue(String name){
        return Integer.valueOf(getStringValue(name));
    }
    
    /**
     * Retrieve configuration setting as boolean
     * @param name name of the setting
     * @return  value of the setting as Boolean
     */
    public Boolean getBooleanValue(String name){
        return Boolean.valueOf(getStringValue(name));
    }

    /**
     * Retrieve configuration setting as Double
     * @param name name of the setting
     * @return value of the setting as Double
     */
    public Double getDoubleValue(String name){
        return Double.valueOf(getStringValue(name));
    }

    /**
     * Sets the configuration setting as String
     * @param name name of the setting
     * @param value value of the setting as String
     */
    public void setValue(String name, String value){
        properties.setProperty(name, value);     
    }
    
}
