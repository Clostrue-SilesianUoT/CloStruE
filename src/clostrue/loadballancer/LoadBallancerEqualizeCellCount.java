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
import clostrue.toolbox.StaticConsoleLogger;
import java.util.ArrayList;

/**
 * This is a load ballancing which tries to equalize cell count among the tasks
 * 
 * the load balancig works as following: for each calculation a measure is calculated
 * this measure is the final count of cells in population processed by the task.
 * The task which was calculating biggest population size
 * hase the fill ratio equal to 1, others have less than one.
 * A mean fill ratio is calculated and fill ratio from each task is divided by the mean fill ratio
 * If the result is > 1 it means that the task population is oversized
 * If the result is < 1 it means that the task population is undersized
 * Afterwards a proportional amount of cells is removed from the task with fill ratio > 1
 * Those cells are distributed proportionally among the tasks which fill ratio < 1
 * finally leading to equalization ot the amount of cells between tasks
 * The ideal sitation is that each task has the same amount of cells
 * and each tasks fill ratio is also equal to 1
 * 
 * @author Krzysztof Szymiczek   
 */
public class LoadBallancerEqualizeCellCount extends LoadBallancerEmpty implements LoadBallancer{
   
    private static boolean LOG_TO_CONSOLE = false;
    private static boolean WITHOUT_GAIN = false;
    
    public LoadBallancerEqualizeCellCount(Simulation inSimulation) {
        super(inSimulation, "Equalize CellCount without gain",LOG_TO_CONSOLE);
    }

   
// do the load ballancing
    @Override
    public void ballanceWork() {

        long beginTime = System.nanoTime();

        ArrayList<TaskMeasure> measures = prepareMeasuresBasedOnCellCount(WITHOUT_GAIN);
        
        if (logToConsole){
            printCellCount(measures, "Before Load Ballancing", getCellPopulationSizeSumFromAllTasks(simulation));            
        }

        if (measures.size() > 1){
            double meanMeasureValue = calculateMeanMeasureValue(measures);
            
            if (logToConsole){
                StaticConsoleLogger.log("Mean measure value: " + String.valueOf(meanMeasureValue));                
            }

            calculateRelationalMeasureValues(meanMeasureValue, measures);

            if (logToConsole){
                printRelationalMeasureValues(measures);                
            }

            ArrayList<Cell> cellsToShiftBetweenTasks = shiftCellsFromTasksToBuffer(measures);
            calculateDestinationBallancingFactorsBySubtraction(measures);

            if (logToConsole){
                printBallancingFactors(measures, "Ballancing factors before normalization");                
            }

            normalizeDestinationBallancingFactors(measures);

            if (logToConsole){
                printBallancingFactors(measures, "Ballancing factors after normalization");                            
            }

            int initialBufferSize = cellsToShiftBetweenTasks.size();
            shiftCellsFromBufferToTasks(measures, cellsToShiftBetweenTasks, initialBufferSize);            
        }
        
        if(logToConsole){
            printCellCount(measures, "After Load Ballancing", getCellPopulationSizeSumFromAllTasks(simulation));                    
        }

        long duration = System.nanoTime() - beginTime;
        simulation.addLoadBallancingTime(duration);
        
    }
   
}
