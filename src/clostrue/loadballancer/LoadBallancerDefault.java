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

import clostrue.CalcTask;
import clostrue.Simulation;
 
/**
 * This is the default simulation load ballancer
 * 
 * the load balancig works as following: for each calculation task with empty cell population,
 * a task with the biggest calculation is determined.
 * a half of cells from the task with the biggect cell population is transfered to the population
 * of the calculation task with the empty population.
 * this repeats untill all tasks with empty cell population will receive some cells
 * (as long as a task exists with at least 5 cells (optimization)
 *
 * @author Krzysztof Szymiczek 
 */
public class LoadBallancerDefault extends LoadBallancerEmpty implements LoadBallancer{
   
    protected static boolean LOG_TO_CONSOLE = false;
    
    public LoadBallancerDefault(Simulation inSimulation) {
        super(inSimulation, "Empty gets 50% of the Biggest",LOG_TO_CONSOLE);
    }

   
// do the load ballancing
    @Override
    public void ballanceWork() {

        long beginTime = System.nanoTime();
        
        for (CalcTask destination : calcTasks) {
            if (destination.getCellPopulationSize() == 0) {
                CalcTask taskWithBiggestPopulation = getTaskWithBiggestPopulation();
                if (taskWithBiggestPopulation != null) {
                    if (taskWithBiggestPopulation.getCellPopulationSize() > 5) {
                        transportHalfOfCellPopulationBetweenTasks(taskWithBiggestPopulation, destination);
                    }
                }
            }
        }
        
        long duration = System.nanoTime() - beginTime;
        simulation.addLoadBallancingTime(duration);
        
    }

}
