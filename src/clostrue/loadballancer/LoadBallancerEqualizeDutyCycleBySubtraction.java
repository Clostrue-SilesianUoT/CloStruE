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
package clostrue.loadballancer;
 
import clostrue.biology.cell.Cell;
import clostrue.Simulation;
import java.util.ArrayList;


/**
 * This is a load ballancing which tries to equalize the duty cycles
 * 
 * the load balancig works as following: for each calculation a measure is calculated
 * this measure is the duty cycle of the processing time. The task which was calculating the longes
 * hase the duty cycle equal to 1, others have less than one.
 * A mean duty cycle is calculated and duty cycle from each task is divided by the mean duty cycle
 * If the result is > 1 it means that the calculations on the task pefromed longer than the average
 * If the result is < 1 it means that the calculations on this task pefromed shorthen than the average
 * Afterwards a proportional amount of cells is removed from the task with duty cycle ratio > 1
 * Those cells are distributed proportionally among the tasks which duty cycle < 1
 * finally leading to equalization ot the duty cycles .
 * The ideal sitation is that each task performs equally long and the mean duty cycle = 1
 * and each tasks duty cycle is also one.
 */
public class LoadBallancerEqualizeDutyCycleBySubtraction extends LoadBallancerEmpty implements LoadBallancer{
   
    private static boolean LOG_TO_CONSOLE = false;
    private static boolean WITHOUT_GAIN = false;
    
    public LoadBallancerEqualizeDutyCycleBySubtraction(Simulation inSimulation) {
        super(inSimulation, "Equalize DutyCycles By Subtraction without Gain",LOG_TO_CONSOLE);
    }

   
// do the load ballancing
    @Override
    public void ballanceWork() {

        long beginTime = System.nanoTime();

        ArrayList<TaskMeasure> measures = prepareMeasuresBasedOnDutyCycle(WITHOUT_GAIN);

        if (measures.size() > 1){
            double meanMeasureValue = calculateMeanMeasureValue(measures);
            calculateRelationalMeasureValues(meanMeasureValue, measures);

            ArrayList<Cell> cellsToShiftBetweenTasks = shiftCellsFromTasksToBuffer(measures);
            calculateDestinationBallancingFactorsBySubtraction(measures);
            normalizeDestinationBallancingFactors(measures);

            int initialBufferSize = cellsToShiftBetweenTasks.size();
            shiftCellsFromBufferToTasks(measures, cellsToShiftBetweenTasks, initialBufferSize);            
        }
        
        long duration = System.nanoTime() - beginTime;
        simulation.addLoadBallancingTime(duration);
        
    }
    
}
