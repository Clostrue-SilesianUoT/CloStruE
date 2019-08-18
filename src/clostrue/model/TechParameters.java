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
import clostrue.hardcodes.Param;


/**
 * Technical simulation parameters
 * @author Krzysztof Szymiczek
 */
public class TechParameters {
    
    private final int       simTasksCount;                      //the amount of parallel tasks for simulation
    private final int       procTasksCount;                     //the amount of parallel tasks for processing
    private final int       maxRowsPerFile;                     //Max rows per single file   
    
    public TechParameters(Settings settings) {
   
        maxRowsPerFile                      = Constant.maxRowsPerCellFile;
        
        if (settings.getIntValue(Param.teParallelSimTasksCount) > 0) {
            this.simTasksCount = settings.getIntValue(Param.teParallelSimTasksCount);
        } else {
            this.simTasksCount = 1;
        }

        if (settings.getIntValue(Param.teParallelProcTasksCount) > 0) {
            this.procTasksCount = settings.getIntValue(Param.teParallelProcTasksCount);
        } else {
            this.procTasksCount = 1;
        }
        
    }

    public TechParameters(TechParameters source){
        
        simTasksCount                       = source.simTasksCount;
        procTasksCount                      = source.procTasksCount;
        maxRowsPerFile                      = source.maxRowsPerFile;
                
    }

    public int getSimTasksCount() {
        return simTasksCount;
    }

    public int getProcTasksCount() {
        return procTasksCount;
    }    

    public int getMaxRowsPerFile() {
        return maxRowsPerFile;
    }

}
