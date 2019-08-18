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
 * Event properties (names)
 * @author Krzysztof Szymiczek
 */
public class EvtProperty {
    
// dynamic changes while simulation in progress    
    public static final String epCurentProgress             = "epCurentProgress";
    public static final String epCurentGEXFProgress         = "epCurentGEXFProgress";
    public static final String epCurentDriverMutations      = "epCurentDriverMutations";
    public static final String epCurentPassengerMutations   = "epCurentPassengerMutations";
    public static final String epCurentCycle                = "epCurentCycle";
    public static final String epCurentPopulationSize       = "epCurentPopulationSize";
    
// controlling application flow    
    public static final String epTriggerCreateGEXF          = "epTriggerCreateGEXF";
    public static final String epFinishCurrentIteration     = "epFinishCurrentIteration";
    
}
// Change Tracking Block                                                    //
// Entry 1 -----------------------------------------------------------------//
// 2017.10.21 Code review done