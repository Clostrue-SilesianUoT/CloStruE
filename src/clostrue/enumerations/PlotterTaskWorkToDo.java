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
package clostrue.enumerations;

/**
 * Enumarates the possible work to be done by the PlotterTask
 * Each enum corresponds to call of one of the methods of PlotterTask
 * @author Krzysztof Szymiczek
 */
public enum PlotterTaskWorkToDo {
    
    saveJFreeChartPopulationSizeToPNG,
    saveJFreeChartCumulatedDriverToPNG,
    saveJFreeChartCumulatedPassengerToPNG,
    saveJFreeChartDrivPassRatioToPNG,
    saveJFreeChartHistogramShadowDriverMutationCountToPNG,
    saveJFreeChartHistogramShadowPassengerMutationCountToPNG,
    saveJFreeChartHistogramSurvivorsDriverMutationCountToPNG,
    saveJFreeChartHistogramSurvivorsPassengerMutationCountToPNG,
    saveJFreeChartHistogramPopulationSizeToPNG,
    saveJFreeChartSurvivorsMutTypeGeneDriverToPNGgeneTag,
    saveJFreeChartShadowMutTypeGeneDriverToPNGgeneTag,
    saveJFreeChartSurvivorsMutTypeLocusDriverToPNGgeneTag,  
    saveJFreeChartShadowMutTypeLocusDriverToPNGgeneTag,  
    saveJFreeChartSurvivorsMutTypeGenePassengerToPNGgeneTag,
    saveJFreeChartShadowMutTypeGenePassengerToPNGgeneTag,
    saveJFreeChartSurvivorsMutTypeLocusPassengerToPNGgeneTag, 
    saveJFreeChartShadowMutTypeLocusPassengerToPNGgeneTag, 
    saveJFreeChartPopulationSizeToCSV,
    saveJFreeChartCumulatedDriverToCSV,
    saveJFreeChartCumulatedPassengerToCSV,
    saveJFreeChartDrivPassRatioToCSV,
    saveJFreeChartHistogramShadowDriverMutationCountToCSV,
    saveJFreeChartHistogramShadowPassengerMutationCountToCSV,
    saveJFreeChartHistogramSurvivorsDriverMutationCountToCSV,
    saveJFreeChartHistogramSurvivorsPassengerMutationToCSV,    
    saveJFreeChartHistogramPopulationSizeToCSV,
    saveJFreeChartSurvivorsMutTypeGeneDriverToCSVgeneTag,
    saveJFreeChartShadowMutTypeGeneDriverToCSVgeneTag,    
    saveJFreeChartSurvivorsMutTypeLocusDriverToCSVgeneTag,  
    saveJFreeChartShadowMutTypeLocusDriverToCSVgeneTag,  
    saveJFreeChartSurvivorsMutTypeGenePassengerToCSVgeneTag,
    saveJFreeChartShadowMutTypeGenePassengerToCSVgeneTag,
    saveJFreeChartSurvivorsMutTypeLocusPassengerToCSVgeneTag, 
    saveJFreeChartShadowMutTypeLocusPassengerToCSVgeneTag, 
   
    
}

