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
/**
 * Genome collection which allows accessing genomes by index (less memory)
 * @author Krzysztof Szymiczek 
 */


public class GenomePartCollection {
    
    private static GenomePart dummy = new GenomePart(); //dummy reference used when there is no mutation
    
    private final GenomePart[] genomeParts;

    public GenomePartCollection(GenomePartSynchronizedCollection sc) {
        int size = sc.size();
        this.genomeParts = new GenomePart[size+1];
        for (int i = 0; i < size; i++)
                genomeParts[i] = sc.getByIndex(i);
    }
       
    public GenomePart getByIndex(int genomePartIndex){
        return genomeParts[genomePartIndex];
    }
    
    public int size(){
        return genomeParts.length;
    }

    public int addAndReturnIndex(GenomePart genomePart){
        int index = genomeParts.length - 1;
        this.genomeParts[index] = genomePart;
        return ( index );
    }
    
}
