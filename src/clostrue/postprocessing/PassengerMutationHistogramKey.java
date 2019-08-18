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

package clostrue.postprocessing;
 
import clostrue.biology.cell.Cell;
import clostrue.collections.GenomeSynchronizedCollection;
import java.util.Objects;

/**
 *
 * Histogram key for mutations
 * @author Krzysztof Szymiczek
 */


public class PassengerMutationHistogramKey {
    int modelCycle;
    int mutationsCount;

    public PassengerMutationHistogramKey(Cell c, GenomeSynchronizedCollection genomes) {
        this.modelCycle = c.getModelCycle();
        this.mutationsCount = c.getGenome(genomes).getPassengerMutationCount();
    }

    public Integer getModelCycle() {
        return modelCycle;
    }

    public Integer getMutationsCount() {
        return mutationsCount;
    } 

    @Override
    public boolean equals(Object o){
        
        if (o == this) 
            return true;
        if (!(o instanceof PassengerMutationHistogramKey))
            return false;

        PassengerMutationHistogramKey obj = (PassengerMutationHistogramKey) o;
        return Integer.valueOf(modelCycle).equals(obj.modelCycle)
                && Integer.valueOf(mutationsCount).equals(obj.mutationsCount);        
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.modelCycle);
        hash = 67 * hash + Objects.hashCode(this.mutationsCount);
        return hash;
    }
        
}
