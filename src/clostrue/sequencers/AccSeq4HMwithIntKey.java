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
 
import clostrue.comparators.IntKeyedHashMapAccessSequenceComparator;
import java.util.ArrayList;

/**
 * Access sequencer for HashMaps with Integer as a key
 * @author Krzysztof Szymiczek
 */
public class AccSeq4HMwithIntKey {

    int curentKey = 0;
    ArrayList<AccSeqElement4HMwithIntKey> sequence;
    
    public AccSeq4HMwithIntKey() {
        
        sequence = new ArrayList<>();
        
    }
    
    public void addNewEntry(int key, int size){
        
        AccSeqElement4HMwithIntKey element = new AccSeqElement4HMwithIntKey(key, size);
        sequence.add(element);
        
    }
    
    public void sort(){
        IntKeyedHashMapAccessSequenceComparator comp = new IntKeyedHashMapAccessSequenceComparator();
        sequence.sort(comp);
        curentKey = 0;
    }
    
    public Integer getNextKey(){
        if( curentKey < sequence.size())
            return sequence.get(curentKey++).getKey();         
        else
            return null;
    }
    
}
