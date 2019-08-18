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
package clostrue.crossIterationAnalysis;

/**
 * Entry for cross-iteration analysis files for quoted mutations
 * @author Krzysztof Szymiczek
 */


public class CsvEntryForQuotedMutations {
    private int popSize;
    private int[] mutationCountPerQuotaId;
    private String[] mutationListPerQuotaId;
    private int iteration;
    private int cycle;

    public CsvEntryForQuotedMutations(int popSize, int[] inMutationCountPerQuotaId, String[] inMutationListPerQuotaId, int iteration, int cycle) {
        this.popSize = popSize;
        this.mutationCountPerQuotaId = inMutationCountPerQuotaId;
        this.mutationListPerQuotaId = inMutationListPerQuotaId;
        this.iteration = iteration;
        this.cycle = cycle;
    }

    public int getPopSize() {
        return popSize;
    }

    public void setPopSize(int cycle) {
        this.popSize = cycle;
    }

    public int getMutationCountPerQuotaId(int quotaId) {
        return mutationCountPerQuotaId[quotaId];
    }

    public void setMutationCountPerQuota(int[] inMutationCountPerQuotaId) {
        this.mutationCountPerQuotaId = inMutationCountPerQuotaId;
    }

    public String getMutationListPerQuotaId(int quotaId) {
        return mutationListPerQuotaId[quotaId];
    }

    public void setMutationListPerQuota(String[] inMutationListPerQuotaId) {
        this.mutationListPerQuotaId = inMutationListPerQuotaId;
    }    
    
    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }
}
