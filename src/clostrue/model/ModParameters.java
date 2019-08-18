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

import clostrue.model.mam.MutationAdvModel;
import clostrue.Settings;
import clostrue.hardcodes.Param;
 
/**
 * Simulation model parametes
 * 
 * @author Krzysztof Szymiczek
 */
public class ModParameters {

   
    private final MutationAdvModel MAM;                 //Mutation Advantage Model
    private final int initCellCountK;                   //the initial population size
    private final double mutRate;                       //the probability of mutation
    private final int maxCycles;                        //max calculation cycles (Controll Parameter)
    private final int maxCellCountAtStartOfCycle;       //threshold population size to interrupt the simulation
    
    public ModParameters(Settings settings, MutationAdvModel inMutAdvModel) {

        MAM                         = inMutAdvModel;
        initCellCountK              = settings.getIntValue(Param.inInitialCellCount);
        mutRate                     = settings.getDoubleValue(Param.inMutationRate);
        maxCycles                   = settings.getIntValue(Param.inMaxCycles);
        maxCellCountAtStartOfCycle  = settings.getIntValue(Param.inMaxCells);     

    }

    public ModParameters(ModParameters source){
        MAM                        = new MutationAdvModel(source.MAM);
        initCellCountK             = source.initCellCountK;
        mutRate                    = source.mutRate;
        maxCycles                  = source.maxCycles;
        maxCellCountAtStartOfCycle = source.maxCellCountAtStartOfCycle;
    }
    
    public MutationAdvModel getMAM() {
        return MAM;
    }

    public int getInitCellCountK() {
        return initCellCountK;
    }

    public double getMutRate() {
        return mutRate;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public int getMaxCellCountAtStartOfCycle() {
        return maxCellCountAtStartOfCycle;
    }
        
}
