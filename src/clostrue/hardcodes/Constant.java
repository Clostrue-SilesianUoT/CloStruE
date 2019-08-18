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
package clostrue.hardcodes;

/**
 * Internal constanst for rough settings.
 * @author Krzysztof Szymiczek
 */
public class Constant {
  
//  application version and name
    public static final String appV                                 = "v.1.00";
    public static final String appN                                 = "CloStruE - Stochastic Clonal Structure Evolution (Simulator) ";
        
    public static final boolean heavyLocusAnalysis                  = false;      
    
//  internal technical constants for making the TGS constants more readable 
    public static final String techStringFalse                      = String.valueOf(false);
    public static final String techStringTrue                       = String.valueOf(true);
    private static final String techProgramDirectory                = (new java.io.File(".").getAbsolutePath());
    public static final String techStringEmpty                      = "";
    public static final int     techNoParam                         = -1;
    public static final Integer maxRowsPerCellFile                  = 1000000;
    public static final int fileBufferSize                          = 1048576;   //one MByte    
    public static final boolean logHmSize                           = true;
    public static final int maxCategoriesOnHistogramAxis            = 10;
    public static final int cFullProgress1                          = 100;
    public static final int c99Progress                             = cFullProgress1 - 1;
    public static final String outDateFormatForFileNames            = "yyyy-MM-dd_HHmmss";
    
//      settings file related constants    
    public static final String settingFileHeadeLine                 = "TGS Application Settings";
    public static final String settingFileDefaultName               = "default_config.settings";
    public static final String mamGeneTypeDriver                    = "DRIVER";
    public static final String mamGeneTypePassenger                 = "PASSENGER";
    public static final String mamAllowedHeader                     = "GENE_NAME;TYPE (DRIVER/PASSENGER);MUTATION_ADVANTAGE;GENE_SIZE;GENE_TAG";
    
//      texts
    public static final String drivers                              = "Drivers";
    public static final String passengers                           = "Passengers";
    public static final String dialogForGephiExe                    = "Select Gephi executable file";
    public static final String drawGraphFromOutputFiles             = "Select Full Set of Output Files";
    public static final String dialogSimulationFinish               = "Last Iteration Finished";       
    public static final String repSimAnalysisContDriver             = "ContsRepSimAnalysisDrivPerCycle";
    public static final String repSimAnalysisContPassen             = "ContsRepSimAnalysisPassPerCycle";
    public static final String fileNameClonesPerPopSizeHavingQuota  = "Clones_Per_Pop_Size_Having_Quota";
    public static final String repSimAnalysisContDriverPopSize      = "ContsRepSimAnalysisDrivPerPopSize";
    public static final String repSimAnalysisContPassenPopSize      = "ContsRepSimAnalysisPassPerPopSize";        
    public static final String repSimAnalysisDiscClones             = "DiscreteRepSimAnalysisClonesPerCycle";
    public static final String repSimAnalysisDiscDriver             = "DiscreteRepSimAnalysisDrivPerCycle";
    public static final String repSimAnalysisDiscPassen             = "DiscreteRepSimAnalysisPassPerCycle";            
    public static final String cycleTaskCellPattern                 = "Cycle: %-6s Task: %-4s Cell: %-6s ";
    public static final String cycleTaskPattern                     = "Cycle: %-6s Task: %-4s            ";
    public static final String cyclePattern                         = "Cycle: %-6s ";        
    public static final String runPathName                          = "Run_";
        
    public static final String getTeWorkDir(){
        return techProgramDirectory.substring(0,techProgramDirectory.length()-1);
    }
    
}

