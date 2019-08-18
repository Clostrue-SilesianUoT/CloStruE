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

import java.util.Comparator;
import clostrue.postprocessing.visualization.Node;

/**
 * X coordinate Comparator Class for NodeWithCoordinates
 */
public class NodeByUIdComparator implements Comparator<Node>{

    /**
     * Compare two nodes by Unique ID
     * @param t NodeWithCoordinates node1
     * @param t1 NodeWithCoordinates node2
     * @return int
     */
    @Override
    public int compare(Node t, Node t1) {
        return t.getuIDLong().compareTo(t1.getuIDLong());
    }
}
