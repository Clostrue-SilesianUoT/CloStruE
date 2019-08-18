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
package clostrue;

import clostrue.model.SimModel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live Statistics of simulation
 * @author Krzysztof Szymiczek
 */
public class LiveStats {
    
    private final AtomicInteger liDriverMutations;      //actual count of Driver Mutations
    private final AtomicInteger liPassengerMutations;   //actual count of Passenger Mutations
    private final AtomicInteger prevPopulationSize;     //previous population size
    private final AtomicInteger shadowSize;             //shadow population size

    /**
     * Constructor for simulation model
     * @param simModel 
     */
    public LiveStats(SimModel simModel) {
        
        shadowSize              = new AtomicInteger(0);
        prevPopulationSize      = new AtomicInteger(simModel.getModParams().getInitCellCountK());
        liDriverMutations       = new AtomicInteger(0);
        liPassengerMutations    = new AtomicInteger(0);
        
    }    

    /**
     * get previous population size
     * @return previous population size
     */
    public int getPrevPopulationSize() {
        return prevPopulationSize.get();
    }

    /**
     * set previous population size
     * @param inPrevPopulationSize previous population size
     */
    public void setPrevPopulationSize(int inPrevPopulationSize) {
        prevPopulationSize.set(inPrevPopulationSize);
    }

    /**
     * get driver mutation count
     * @return driver mutation count
     */
    public int getLiDriverMutations() {
        return liDriverMutations.get();
    }

    /**
     * get passenger mutation count
     * @return passenger mutation count
     */
    public int getLiPassengerMutations() {
        return liPassengerMutations.get();
    }

    /**
     * set driver mutation count
     * @param liDriverMutations driver mutation count
     */
    public void setLiDriverMutations(int liDriverMutations) {
        this.liDriverMutations.set(liDriverMutations);
    }

    /**
     * set passenger mutation count
     * @param liPassengerMutations passenger mutation count
     */
    public void setLiPassengerMutations(int liPassengerMutations) {
        this.liPassengerMutations.set(liPassengerMutations);
    }

    /**
     * correct the counter of driver mutations by a delta value
     * which is determined by a single calculation task
     * @param correction the correction factor
     */
    public void correctliDriverMutations(int correction) {
        liDriverMutations.addAndGet(correction);
    }

    /**
     * correct the counter of passenger mutations by a delta value
     * which is determined by a single calculation task.
     * @param correction the correction factor
     */
    public void correctliPassengerMutations(int correction) {
        liPassengerMutations.addAndGet(correction);
    }  

    /**
     * returns the shadow population size which is the sum of
     * population size added together from each cycle of the simulation
     * @return shadow population size
     */
    public int getShadowSize() {
        return shadowSize.get();
    }

    /**
     * increments the shadow population size
     * @param delta shadow population size correction
     */
    public void incrementShadowSize(int delta) {
        shadowSize.addAndGet(delta);
    }
    
}
