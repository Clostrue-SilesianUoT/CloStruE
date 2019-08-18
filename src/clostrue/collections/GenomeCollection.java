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

import clostrue.biology.genome.Genome;
import clostrue.biology.genome.GenomePart;

/**
 * Genome collection which allows accessing genomes by index (less memory)
 * @author Krzysztof Szymiczek 
 */


public class GenomeCollection {
    
    private final Genome[] genomes;

    private final GenomePartCollection driverGenomeParts;//Collection of parts of genomes
    private final GenomePartCollection passengerGenomeParts;//Collection of parts of genomes    
    
    public GenomeCollection(GenomeSynchronizedCollection sc) {
        int size = sc.getGenomes().size();
        this.genomes = new Genome[size+1];
        for (int i = 0; i < size; i++){
            genomes[i] = sc.getByIndex(i);
        }
        this.driverGenomeParts = new GenomePartCollection(sc.getDriverGenomeParts());
        this.passengerGenomeParts = new GenomePartCollection(sc.getPassengerGenomeParts());
    }

    public int addAndReturnIndex(Genome genome){
        int index = genomes.length - 1;
        this.genomes[index] = genome;
        return index;
    }    
    
    public Genome getByIndex(int genomeIndex){
        return genomes[genomeIndex];
    }

    public GenomePartCollection getDriverGenomeParts() {
        return driverGenomeParts;
    }

    public GenomePartCollection getPassengerGenomeParts() {
        return passengerGenomeParts;
    }

    public GenomePart getDrivPartByIndex(int genomePartIndex){
        return driverGenomeParts.getByIndex(genomePartIndex);
    }    

    public GenomePart getPassPartByIndex(int genomePartIndex){
        return passengerGenomeParts.getByIndex(genomePartIndex);
    }  

    public int addDrivPartAndReturnIndex(GenomePart part){
        return driverGenomeParts.addAndReturnIndex(part);
    }

    public int addPassPartAndReturnIndex(GenomePart part){
        return passengerGenomeParts.addAndReturnIndex(part);
    }
    
}
