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
package clostrue.model;

import clostrue.Settings;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.DirName;
import clostrue.hardcodes.Param;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * Creates and returns paths to directories in simulation output directory
 * 
 * @author Krzysztof Szymiczek
 */
public class FilePaths {

    /*
    Technical simulation parameters - file paths
    */
    
    private static String runWorkDir        = "";
    
    private String workDir                  = ""; 
    private String workDirTextAnalytics     = "";
    private String workDirCellFiles         = "";
    private String workDirGraphics          = "";
    private String workDirBenchmark         = "";
    private String workDirGraphicDataSource = ""; 
    private String workDirTechOutput        = ""; 
       
    public FilePaths(Settings settings, Integer iteration) {

        SimpleDateFormat sdfDate    = new SimpleDateFormat(Constant.outDateFormatForFileNames);
        String strDate              = sdfDate.format(new Date());
        
        if(iteration == 1) {
            runWorkDir = settings.getStringValue(Param.teWorkDir)
                          + java.io.File.separator
                          + Constant.runPathName
                          + strDate;
        }
        
        workDir 
                = runWorkDir
                + java.io.File.separator 
                + "Iteration_" 
                + String.valueOf(iteration);
        try {
            Files.createDirectories(Paths.get(workDir));
        } catch (IOException ex) {
            Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public FilePaths(FilePaths source){
        workDir                     = source.workDir;
        workDirTextAnalytics        = source.workDirTextAnalytics;
        workDirCellFiles            = source.workDirCellFiles;
        workDirGraphics             = source.workDirGraphics;
        workDirBenchmark            = source.workDirBenchmark;
        workDirGraphicDataSource    = source.workDirGraphicDataSource;
        workDirTechOutput           = source.workDirTechOutput;   
    }
    
    synchronized public String getWorkDirTextAnalytics() {
        if ("".equals(workDirTextAnalytics)){
            workDirTextAnalytics = workDir + java.io.File.separator + DirName.subDirTextAnalytics;           
            try {
                Files.createDirectories(Paths.get(workDirTextAnalytics));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirTextAnalytics;
    }
    
    synchronized public String getWorkDirTechOutput() {
        if ("".equals(workDirTechOutput)){
            workDirTechOutput = workDir + java.io.File.separator + DirName.subDirTechOutput;           
            try {
                Files.createDirectories(Paths.get(workDirTechOutput));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirTechOutput;
    }

    synchronized public String getWorkDirCellFiles() {
        if ("".equals(workDirCellFiles)){
            workDirCellFiles = workDir + java.io.File.separator + DirName.subDirCellFiles;          
            try {
                Files.createDirectories(Paths.get(workDirCellFiles));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirCellFiles;
    }

    synchronized public String getWorkDirGraphics() {
        if ("".equals(workDirGraphics)){
            workDirGraphics = workDir + java.io.File.separator + DirName.subDirGraphics;         
            try {
                Files.createDirectories(Paths.get(workDirGraphics));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirGraphics;
    }

    synchronized public String getWorkDirBenchmark(){
        if ("".equals(workDirBenchmark)){
            workDirBenchmark = workDir + java.io.File.separator + DirName.subDirBenchmark;        
            try {
                Files.createDirectories(Paths.get(workDirBenchmark));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirBenchmark;        
    }
    
    synchronized public String getWorkDirGraphicDataSource() {
        if ("".equals(workDirGraphicDataSource)){
            workDirGraphicDataSource = workDir + java.io.File.separator + DirName.subDirGraphicsDataSource;         
            try {
                Files.createDirectories(Paths.get(workDirGraphicDataSource));
            } catch (IOException ex) {
                Logger.getLogger(FilePaths.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return workDirGraphicDataSource;
    }

    public String getWorkDir() {
        return workDir;
    }        

    public static String getRunWorkDir() {
        return runWorkDir;
    }
}
