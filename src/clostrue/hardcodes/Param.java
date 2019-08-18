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
 * Parameter names definitions
 * those names are used in setting files
 * 
 * @author Krzysztof Szymiczek
 */
public class Param {
    
    public static final String inIterations                     = "inIterations";
    public static final String inInitialCellCount               = "inInitialCellCount";
    public static final String inMutationRate                   = "inMutationRate";
    public static final String inDriverFitAdvantageTda          = "inDriverFitAdvantageTda";
    public static final String inDriverGenesTdg                 = "inDriverGenesTdg";
    public static final String inPassengerFitAdvantageTpa       = "inPassengerFitAdvantageTpa";
    public static final String inPassengerGenesTpg              = "inPassengerGenesTpg";
    public static final String inMaxCycles                      = "inMaxCycles";
    public static final String inMaxCells                       = "inMaxCells";
    public static final String teParallelSimTasksCount          = "teParallelSimTasksCount";
    public static final String teParallelProcTasksCount         = "teParallelProcTasksCount";    
    public static final String teWorkDir                        = "teWorkDir";
    public static final String cbTeGenerateGraph                = "cbTeGenerateGraph";
    public static final String cbTeGenerateFishplot             = "cbTeGenerateFishplot";
    public static final String cbTeCallGephi                    = "cbTeCallGephi";
    public static final String teGephiPath                      = "teGephiPath";
    public static final String inDmSize                         = "inDmSize";
    public static final String inPmSize                         = "inPmSize";
    public static final String mamPath                          = "mamPath";
    public static final String cbPrepareClones                  = "cbPrepareClones";
    public static final String teCellListerCutOff               = "teCellListerCutOff";
    public static final String teCloneMinSize                   = "teCloneMinSize";
    public static final String teCloneMinLifespan               = "teCloneMinLifespan";
    public static final String ggRectangeScale                  = "ggRectangeScale";    
    public static final String teResUseGroupping                = "teResUseGroupping";
    public static final String cbPrepareCells                   = "cbPrepareCells";
    public static final String cbGenerateGephiGraph             = "cbGenerateGephiGraph";
    public static final String cbGenerateSimulationPNG          = "cbGenerateSimulationPNG";
    public static final String cbGenerateAnalyticsPNG           = "cbGenerateAnalyticsPNG";
    public static final String cbGenerateSurvivorsAnalytics     = "cbGenerateSurvivorsAnalytics";
    public static final String ggScaleToMaxSize                 = "ggScaleToMaxSize";
    public static final String ggMaxSize                        = "ggMaxSize";
    public static final String cbClonesScatter                  = "cbClonesScatter";
    public static final String cbDriversSactter                 = "cbDriversSactter";
    public static final String cbPassengersScatter              = "cbPassengersScatter"; 
    public static final String teQuota                          = "teQuota";
} 
