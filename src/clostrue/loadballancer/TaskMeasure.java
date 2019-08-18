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

/**
 *
 * Measure for a given task used for load Ballancing
 * 
 * @author Krzysztof Szymiczek
 */
public class TaskMeasure {
    
    private final Double    measureValue;
    private Double          relationalMeasureValue;
    private Double          ballancingFactor;
    private final CalcTask  calcTask;
    private final Double    cellPopulationSize;

    public TaskMeasure(Double measureValue, CalcTask calcTask) {
        this.measureValue       = measureValue;
        this.calcTask           = calcTask;
        this.cellPopulationSize = (double)calcTask.getCellPopulationSize();
        this.ballancingFactor   = 0.0;
    }

    public void setBallancingFactor(Double ballancingFactor) {
        this.ballancingFactor = ballancingFactor;
    }
    
    public void calculateBallancingFactorByRelation(double measureSum){
        ballancingFactor = measureSum / relationalMeasureValue;
    }

    public void calculateBallancingFactorBySubtraction(){
        ballancingFactor = 1 - relationalMeasureValue;
    }
    
    public Double getBallancingFactor() {
        return ballancingFactor;
    }
    
    public Double getMeasureValue() {
        return measureValue;
    }

    public void calculateRelationalMeasureValue(double withRelationTo){
            relationalMeasureValue = measureValue / withRelationTo;            
    }

    public Double getRelationalMeasureValue() {
        return relationalMeasureValue;
    }

    public Double getCellPopulationSize() {
        return cellPopulationSize;
    }

    public CalcTask getCalcTask() {
        return calcTask;
    }    
    
}
