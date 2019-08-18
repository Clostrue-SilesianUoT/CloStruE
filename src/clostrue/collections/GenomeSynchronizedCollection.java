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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Genome collection which allows accessing genomes by index (less memory)
 * @author Krzysztof Szymiczek 
 */


public class GenomeSynchronizedCollection {
    
    private final List<Genome> genomes;

    private final GenomePartSynchronizedCollection driverGenomeParts;//Collection of parts of genomes
    private final GenomePartSynchronizedCollection passengerGenomeParts;//Collection of parts of genomes    
    
    public GenomeSynchronizedCollection() {
        this.genomes = Collections.synchronizedList(new ArrayList<Genome>());
        this.driverGenomeParts = new GenomePartSynchronizedCollection();
        this.passengerGenomeParts = new GenomePartSynchronizedCollection();
    }
    
    public int addAndReturnIndex(Genome genome){
        this.genomes.add(genome);
        return ( genomes.size() - 1 );
    }
    
    public Genome getByIndex(int genomeIndex){
        return genomes.get(genomeIndex);
    }

    public GenomePartSynchronizedCollection getDriverGenomeParts() {
        return driverGenomeParts;
    }

    public GenomePartSynchronizedCollection getPassengerGenomeParts() {
        return passengerGenomeParts;
    }

    public int addDrivPartAndReturnIndex(GenomePart part){
        return driverGenomeParts.addAndReturnIndex(part);
    }

    public int addPassPartAndReturnIndex(GenomePart part){
        return passengerGenomeParts.addAndReturnIndex(part);
    }

    public GenomePart getDrivPartByIndex(int genomePartIndex){
        return driverGenomeParts.getByIndex(genomePartIndex);
    }    

    public GenomePart getPassPartByIndex(int genomePartIndex){
        return passengerGenomeParts.getByIndex(genomePartIndex);
    }  

    public List<Genome> getGenomes() {
        return genomes;
    }
    
}
