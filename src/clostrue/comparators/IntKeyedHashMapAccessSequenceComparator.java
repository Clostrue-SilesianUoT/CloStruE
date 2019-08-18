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
package clostrue.comparators;

import clostrue.sequencers.AccSeqElement4HMwithIntKey;
import java.util.Comparator;

/**
 * X coordinate Comparator Class for NodeWithCoordinates
 */
public class IntKeyedHashMapAccessSequenceComparator implements Comparator<AccSeqElement4HMwithIntKey>{

    /**
     * Compare two nodes by Unique ID
     * @param t NodeWithCoordinates node1
     * @param t1 NodeWithCoordinates node2
     * @return int
     */
    @Override
    public int compare(AccSeqElement4HMwithIntKey t, AccSeqElement4HMwithIntKey t1) {
        if (t1.getSize().compareTo(t.getSize()) != 0)
            return t1.getSize().compareTo(t.getSize());
        else
            return t1.getKey().compareTo(t.getKey());
    }
}
