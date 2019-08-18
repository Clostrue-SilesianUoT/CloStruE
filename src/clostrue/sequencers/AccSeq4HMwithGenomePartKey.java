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

package clostrue.sequencers;
 
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.GenomePart;
import clostrue.comparators.GenomePartKeyedHashMapAccessSequenceComparator;
import java.util.ArrayList;

/**
 * Access sequencer for HashMaps with Genome Part as a key
 * @author Krzysztof Szymiczek
 */
public class AccSeq4HMwithGenomePartKey {

    int curentIndex = 0;
    ArrayList<AccSeqElement4HMwithGenomePartKey> sequence;
    
    public AccSeq4HMwithGenomePartKey() {
        
        sequence = new ArrayList<>();
        
    }
    
    public void addNewEntry(GenomePart key, int size, Cell cell){
        
        AccSeqElement4HMwithGenomePartKey element = new AccSeqElement4HMwithGenomePartKey(key, size, cell);
        sequence.add(element);
        
    }
    
    public void sort(){
        GenomePartKeyedHashMapAccessSequenceComparator comp = new GenomePartKeyedHashMapAccessSequenceComparator();
        sequence.sort(comp);
        curentIndex = 0;
    }
    
    public GenomePart getNextKey(){
        if( curentIndex < sequence.size())
            return sequence.get(curentIndex++).getKey();         
        else
            return null;
    }
    
    public Cell getSampleCell(){
        return sequence.get(0).getCell();
    }
}
