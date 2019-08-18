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


/**
 * SimModel is a class which represents settings for the Simulation Model
 * @author Krzysztof Szymiczek
 */
public class SimModel {
    
    ModParameters   modParams;
    TechParameters  techParams;
    FilePaths       filePaths;

    public SimModel(Settings settings, MutationAdvModel mutAdvModel, Integer iteration) {
        modParams   = new ModParameters(settings, mutAdvModel);
        techParams  = new TechParameters(settings);
        filePaths   = new FilePaths(settings, iteration);
    }
    
    public SimModel(int iteration, SimModel source){
        modParams   = new ModParameters(source.modParams);
        techParams  = new TechParameters(source.techParams);
        filePaths   = new FilePaths(source.filePaths);
    }

    public ModParameters getModParams() {
        return modParams;
    }

    public TechParameters getTechParams() {
        return techParams;
    }

    public FilePaths getFilePaths() {
        return filePaths;
    }

}
