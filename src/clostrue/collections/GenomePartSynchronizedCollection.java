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
package clostrue.collections;

import clostrue.biology.genome.GenomePart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Genome collection which allows accessing genomes by index (less memory)
 * @author Krzysztof Szymiczek 
 */


public class GenomePartSynchronizedCollection {
    
    private static GenomePart dummy = new GenomePart(); //dummy reference used when there is no mutation
    
    private final List<GenomePart> genomeParts;

    public GenomePartSynchronizedCollection() {
        this.genomeParts = Collections.synchronizedList(new ArrayList<>());
        genomeParts.add(dummy);
    }
    
    public int addAndReturnIndex(GenomePart genomePart){
        this.genomeParts.add(genomePart);
        return ( genomeParts.size() - 1 );
    }
    
    public GenomePart getByIndex(int genomePartIndex){
        return genomeParts.get(genomePartIndex);
    }
    
    public int size(){
        return genomeParts.size();
    }
    
}
