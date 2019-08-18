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
 * Activities for concole logging
 * @author Krzysztof Szymiczek 
 */
public class Activity {
    
    //  steps
    public static final String garbageCollection                 = "GARBAGE COLLECTION";
    public static final String ParallelPlotterFinish             = " - - - - Ammount of Tasks to finish by Parallel Plotter: ";
    public static final String parallelMtaStep1Finish            = " - - - - Ammount of Tasks to finish Mutation Type Analysis Step1: ";
    public static final String parallelSaFinish                  = " - - - Ammount of Tasks to finish Survivors Analysis: ";
    public static final String parallelMtaStep2Finish            = " - - - - Ammount of Tasks to finish Mutation Type Analysis Step2: ";
    public static final String parallelMtaQuotedDrivers          = " - - - - Ammount of Tasks to finish Quoted Drivers Analaysis: ";
    public static final String parallelMtaQuotedPassengers       = " - - - - Ammount of Tasks to finish Quoted Passengers Analaysis: ";
    public static final String fileDumpingMtaQuotedDrivers       = " - - - - Saving File for Quoted Drivers Analaysis ";
    public static final String fileDumpingMtaQuotedPassengers    = " - - - - Saving File for Quoted Passengers Analaysis ";
    public static final String programRun                        = "PROGRAM RUN";
    public static final String preparingTasks                    = "PREPARING TASKS";
    public static final String createTaskID                      = " - CREATE TASK ID: ";  
    public static final String createLocalSimModelCopy           = " - - CREATE LOCAL SIMULATION MODEL COPY FOR TASK";
    public static final String createInitialCellPopulationSubset = " - - CREATE INITIAL CELL POPULATION SUBSET FOR TASK ID: ";
    public static final String createCellWriter                  = " - - CREATE CELL WRITER FOR TASK ID: ";
    public static final String simulation                        = " - Simulation";
    public static final String threadPool                        = " - Thread Pool";
    public static final String parallelExecution                 = " - - Parallel Execution";
    public static final String closingCellWriters                = " - - Closing Cell Writers";
    public static final String moveCellsToStatistics             = " - - Move Cells To Statistics";
    public static final String copyMem                           = " - Memory Copy Cells";
    public static final String postProcessing                    = " - Postprocessing";
    public static final String analyzeClones                     = " - Analyze Clones";
    public static final String removeShadowPredecessors          = " - Remove Shadow Predecessors from clones";
    public static final String removeShadowPredecessorsInternal  = " - Remove Shadow Predecessors from internal clones";
    public static final String calcHistShadowDriverMutations     = " - Calculate Histogram Shadow Driver Mutations";
    public static final String calcHistShadowPassengerMutations  = " - Calculate Histogram Shadow Passenger Mutations";
    public static final String calcHistSurvivorsDriverMutations     = " - Calculate Histogram Survivors Driver Mutations";
    public static final String calcHistSurvivorsPassengerMutations  = " - Calculate Histogram Survivors Passenger Mutations";
    public static final String analyzeSurvivors                  = " - Analyze Survivors";
    public static final String graphEntryPoint                   = " - Draw Graph Entrypoint";  
    public static final String graphCall                         = " - Draw Graph Call";      
    public static final String prepareFishplot                   = " - Prepare Fishplot";
    public static final String createPopulationGraphic           = " - Create Population Graphic";
    public static final String linkNodes                         = " - - Link Nodes";
    public static final String createNodes                       = " - - Create Nodes";
    public static final String nodesCreated                      = " - - - Percentage of Nodes Created: ";
    public static final String prepareNodesByLevel               = " - - Prepare Nodes by Level";
    public static final String layoutNodesOnPlane                = " - - Layout Nodes on Plane";
    public static final String createSubsets                     = " - - Create subsets";
    public static final String createSubsetsPercent              = " - - - Create subsets: Finished %: ";
    public static final String exportSubsets                     = " - - Export subsets";
    public static final String mutationTypeAnalysis              = " - - Mutation Type Analysis";
 
    public static final String mtaCclikSurvivorsPassengerLocus   = " - - - - Correction: Survivors Passenger Locus";     
    public static final String mtaCclikSurvivorsDriverLocus      = " - - - - Correction: Survivors Driver Locus";     
    public static final String mtaCclikSurvivorsPassengerGene    = " - - - - Correction: Survivors Passenger Gene";     
    public static final String mtaCclikSurvivorsDriverGene       = " - - - - Correction: Survivors Driver Gene";     
    public static final String mtaCclikShadowPassengerLocus      = " - - - - Correction: Shadow Passenger Locus";     
    public static final String mtaCclikShadowDriverLocus         = " - - - - Correction: Shadow Driver Locus";     
    public static final String mtaCclikShadowPassengerGene       = " - - - - Correction: Shadow Passenger Gene";     
    public static final String mtaCclikShadowDriverGene          = " - - - - Correction: Shadow Driver Gene";     

    public static final String saCorEqualSurvivorsDriverPart    = " - - - Correction: Equal Survivor Drivers";
    public static final String saCorEqualSurvivorsPassengerPart = " - - - Correction: Equal Survivor Passengers";
    
    public static final String saCreateSurvivorsFileForIdenticalDriverGenome    = " - - - Analysis: Equal Survivors Driver Genome";
    public static final String saCreateSurvivorsFileForIdenticalPassengerGenome = " - - - Analysis: Equal Survivors Passenger Genome";
    public static final String saCreateShadowsFileForIdenticalDriverGenome      = " - - - Analysis: Equal Shadow Driver Genome";
    public static final String saCreateShadowsFileForIdenticalPassangerGenome   = " - - - Analysis: Equal Shadow Passenger Genome";
    public static final String saCreateSurvivorsFileForIdenticalDriverGene      = " - - - Analysis: Equal Survivors Driver Gene";
    public static final String saCreateSurvivorsFileForIdenticalPassengerGene   = " - - - Analysis: Equal Survivors Passenger Gene";
    public static final String saCreateSurvivorsFileForIdenticalDriverLocus     = " - - - Analysis: Equal Survivors Driver Locus";
    public static final String saCreateSurvivorsFileForIdenticalPassengerLocus  = " - - - Analysis: Equal Survivors Passenger Locus";             
    public static final String saCreateFileDriversPerCycle                      = " - - - Analysis: Equal Drivers Per Cycle";
    public static final String saCreateFilePassengersPerCycle                   = " - - - Analysis: Equal Passengers Per Cycle";
    public static final String saCreateQuotedClonesFile                         = " - - - Analysis: Quoted Clones";
    public static final String saCreateQuotedDriversFile                        = " - - - Analysis: Quoted Drivers";
    public static final String saCreateQuotedPassengersFile                     = " - - - Analysis: Quoted Passengers";
    public static final String saCreateFileDriversPerPopSize                    = " - - - Analysis: Drivers Per Population Size";
    public static final String saCreateFilePassengersPerPopSize                 = " - - - Analysis: Passengers Per Population Size";
    
    public static final String mtaAtaSurvivorsDriverLocus        = " - - - - Analysis: Survivors Driver Locus";
    public static final String mtaAtaShadowDriverLocus           = " - - - - Analysis: Shadow Driver Locus";
    public static final String mtaAtaSurvivorsPassengerLocus     = " - - - - Analysis: Survivors Passenger Locus";
    public static final String mtaAtaShadowPassengerLocus        = " - - - - Analysis: Shadow Passenger Locus";
    public static final String mtaAtaSurvivorsDriverGene         = " - - - - Analysis: Survivors Driver Gene";
    public static final String mtaAtaShadowDriverGene            = " - - - - Analysis: Shadow Driver Gene";
    public static final String mtaAtaSurvivorsPassengerGene      = " - - - - Analysis: Survivors Passenger Gene";
    public static final String mtaAtaShadowPassengerGene         = " - - - - Analysis: Shadow Passenger Gene";
    
    public static final String savePlotImagesToPNG               = " - Save Plot Images to PNG";
    public static final String saveGuiSimulationPlotsToPNG       = " - - Save Simulation Plot Images to PNG";
    public static final String saveGuiAnalyticsPlotsToPNG        = " - - Save Analytics Plot Images to PNG";
    public static final String savePlotDataToCSV                 = " - Save Plot Data to CSV";
    public static final String saveGuiSimulationDataToCSV        = " - - Save Simulation Data to CSV";
    public static final String saveGuiAnalyticsDataToCSV         = " - - Save Analytics Data to CSV";
    public static final String saveClonesPerCycleToCSV           = " - - Save Clones Per Cycle Data to CSV";
    public static final String saveDriversPerCycleToCSV          = " - - Save Drivers Per Cycle Data to CSV";
    public static final String savePassengersPerCycleToCSV       = " - - Save Passengers Per Cycle Data to CSV";
    public static final String saveClonesPerPopSizeToCSV         = " - - Save Clones Per Population Size Data to CSV";
    public static final String saveQuotedDriversToCSV            = " - - Save Quoted Drivers Data to CSV";    
    public static final String saveQuotedPassengersToCSV         = " - - Save Quoted Passengers Data to CSV";
    public static final String saveDriversPerPopSizeToCSV        = " - - Save Drivers Per Population Size Data to CSV";
    public static final String savePassengersPerPopSizeToCSV     = " - - Save Passengers Per Population Size Data to CSV";
    public static final String saveLvl2                          = " - - - Save ";

    public static final String rsaStartCSV                       = " - Started creating CSV file for RSA fileName: ";
    public static final String rsaFinishedCSV                    = " - Finished creating CSV file for RSA fileName: ";
    public static final String rsaStartChart                     = " - Started creating Chart for RSA fileName: ";
    public static final String rsaFinishedChart                  = " - Finished creating Chart for RSA fileName: ";      
    //states
    public static final String destroyed                         = "Destroyed";
    public static final String initialized                       = "Initialized";
    public static final String started                           = "Started";
    public static final String finished                          = "Finished";
    public static final String scheduled                         = "Scheduled";
    public static final String skipped                           = "Skipped";
    public static final String waiting                           = "Waiting";
    public static final String ioException                       = "IO Exception: ";
}
