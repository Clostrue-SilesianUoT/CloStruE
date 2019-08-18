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
import clostrue.loadballancer.TaskMeasure;

/**
 * Task Measure comparator by measure value
 */
public class TaskMeasureByMeasureValue implements Comparator<TaskMeasure>{

    /**
     * Compare two nodes by Unique ID
     * @param m TaskMeasure measure1
     * @param m1 TaskMeasure measure2
     * @return int
     */
    @Override
    public int compare(TaskMeasure m, TaskMeasure m1) {
        return m.getMeasureValue().compareTo(m1.getMeasureValue());
    }
}
