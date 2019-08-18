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

import static clostrue.hardcodes.Constant.techStringTrue;

/**
 * Default configuration of the simulator
 * @author Krzysztof Szymiczek
 */
public class DefConf {

    //      additional export parameters
    public static final String cbGenerateSurvivorsAnalytics = Constant.techStringTrue;
    public static final String cbGenerateAnalyticsPNG = Constant.techStringTrue;
    public static final String cbGenerateSimulationPNG = Constant.techStringTrue;
    
    //      technical settings
    public static final String teParallelSimTasksCount = "25";
    public static final String teParallelProcTasksCount = "25";
    public static final String teCloneMinSize = "10";
    public static final String cbPrepareClones = Constant.techStringTrue;
    public static final String teCloneMinLifespan = "3";
    public static final String teCellListerCutOff = "20";

    //      output files related settings
    public static final String cbPrepareCells = Constant.techStringTrue;
    public static final String cbClonesScatter = Constant.techStringFalse;
    public static final String teQuota = "50";
    public static final String cbDriversSactter = Constant.techStringFalse;
    public static final String cbPassengersScatter = Constant.techStringFalse;
    public static final String cbGeneratePngImage = Constant.techStringTrue;
    public static final String cbGenerateGephiGraph = Constant.techStringFalse;
    public static final String cbTeGenerateFishplot = Constant.techStringFalse;
    public static final String teGephiPath = Constant.techStringEmpty;
    
    //      gephi graph creation specific settings
    public static final String cbTeGenerateGraph = Constant.techStringFalse;
    public static final String cbTeCallGephi = Constant.techStringFalse;
    public static final String ggScaleToMaxSize                         = techStringTrue;
    public static final String ggMaxSize                                = "2400";
    
    //      histogram specific settings
    public static final String teResUseGroupping = Constant.techStringFalse;

    //      internal settings for Debugging
    public static final String ggRectangeScale = Constant.techStringTrue;
    public static final String gexfFileMode = Constant.techStringFalse; //if false -> transfer data via memory


    
}

