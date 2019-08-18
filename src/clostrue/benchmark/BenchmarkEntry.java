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
package clostrue.benchmark;

import clostrue.CalcTask;

/**
 * One Benchmark entry for one simulation iteration
 * The benchmark entry can be saved to file and contains the information
 * about the time the simulation cycle took to execute and the statistics
 * which allow to judge how much calculations was performed within the simulation
 * cell counts, gains, duty cycles for taks are storred.
 * This is mainly used to check and evaluate various load ballancers.
 * 
 * @author Krzysztof Szymiczek 
 */
public class BenchmarkEntry {
 
    private int cycle;                      //simulation cycle
    private final long startTime;           //start time of iteration
    private long duration;                  //iteration duration
    private final double startCellCount;    //initial population size
    private double endCellCount;            //final population size
    private double cellCountGain;           //ratio of final to begin population size
    private double dutyCycle;               //duty cycle
    private double fillRatio;               //fill ration
    private final CalcTask _calcTask;       //calculation task
    
    /**
     * Constructor
     * @param cycle simulation cycle
     * @param calcTask calculation task
     * @param cellCount cell count
     */
    public BenchmarkEntry(int cycle, CalcTask calcTask, int cellCount) {
        this.cycle = cycle;
        this._calcTask = calcTask;
        this.startCellCount = cellCount;
        this.startTime = System.nanoTime();
    }

    /**
     * return final population size
     * @return final population size
     */
    public double getEndCellCount() {
        return endCellCount;
    }    

    /**
     * return the fill ratio
     * @return fill ratio
     */
    public double getFillRatio() {
        return fillRatio;
    }
    
    /**
     * Stops the benchmarking calculation time
     * @param cellCount final population size
     */
    public void benchmarkStop(int cellCount){
        long finish = System.nanoTime();
        duration = finish - startTime;

        endCellCount = cellCount;
        if (startCellCount > 0) {
            cellCountGain = endCellCount / startCellCount;            
        } else {
            cellCountGain = 1;
        }

    }

    /**
     * Calculates calculation task duty cycle
     * @param maxDurationPerCycle 
     */
    public void calculateDutyCycle(long maxDurationPerCycle){       
        if (maxDurationPerCycle != 0){
            dutyCycle = (double)duration / (double)maxDurationPerCycle;            
        } 
    }

    /**
     * calculates fill ratio
     * @param biggestPopulationSize biggest population size
     */
    public void calculateFillRatio(double biggestPopulationSize){       
        if (biggestPopulationSize != 0){
            fillRatio = _calcTask.getCellPopulationSize() / biggestPopulationSize;            
        } 
    }

    /**
     * return calculation task processing time duty cycle
     * @return duty cycle
     */
    public double getDutyCycle() {
        return dutyCycle;
    }
    
    /**
     * returns simulation cycle
     * @return simulation cycle
     */
    public int getCycle() {
        return cycle;
    }

    /**
     * get simulation iteration duration
     * @return simulation iteration duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * returns cell count gain
     * @return cell count gain
     */
    public double getCellCountGain() {
        return cellCountGain;
    }

    /**
     * sets the curent simulation cycle
     * @param cycle simulation cycle
     */
    public void setCycle(int cycle) {
        this.cycle = cycle;
    }
    
    

}

