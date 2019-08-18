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

package clostrue.hardcodes.file;

/** 
 * File Artifacts - Output file names
 * @author Krzysztof Szymiczek
 */
public class Name {
    public static final String clonesShadowAll = "Shadow Clones (Driver-Analysis) All";
    public static final String clonesShadowSig = "Shadow Clones (Driver-Analysis) Significant";
    public static final String clonesSurvivorsAll = "Survivors Clones (Driver-Analysis) All";
    public static final String clonesSurvivorsSig = "Survivors Clones (Driver-Analysis) Significant";
    public static final String processingLog = "Processing";
    public static final String consoleLog = "General Console Log";
    public static final String driversPerPopSize = "Drivers Per Population Size";
    public static final String quotedClones = "Quoted Clones";
    public static final String quotedPassengers = "Quoted Passengers"; 
    public static final String quotedDrivers = "Quoted Drivers";
    public static final String passengersPerPopSize = "Passengers Per Population Size";
    public static final String snShadowClonesIdenticalDriverGenomePart = "Shadow Clones (All) with identical Driver Genome Part";
    public static final String snShadowClonesIdenticalPassangerGenomePart = "Shadow Clones (All) with identical Passenger Genome Part";
    public static final String snSurvivorsIdenticalPassengerLocus = "Survivor Cell Groups Having Exact Same Passenger Mutations (Locus-Wise)";
    public static final String snSurvivorsIdenticalDriverGenomePart = "Survivor Cell Groups With Identical Whole Driver Genome Part";
    public static final String snSurvivorsIdenticalDriverGene = "Survivor Cell Groups Having Exact Same Driver Mutations (Gene-Wise)";
    public static final String snSurvivorsIdenticalPassengerGene = "Survivor Cell Groups Having Exact Same Passenger Mutations (Gene-Wise)";
    public static final String snSurvivorsIdenticalDriverLocus = "Survivor Cell Groups Having Exact Same Driver Mutations (Locus-Wise)";
    public static final String snSurvivorsIdenticalPassengerGenomePart = "Survivor Cell Groups With Identical Whole Passenger Genome Part";
    public static final String snShadowHistogramMutTypeGeneDriver = "Shadow - Histogram of Driver Mutation Types Comparring Genes";
    public static final String snSurvivorsHistogramMutTypeLocusPassenger = "Survivors - Histogram of Passenger Mutation Types Comparring Locus";
    public static final String snSurvivorsHistogramMutTypeLocusDriver = "Survivors - Histogram of Driver Mutation Types Comparring Locus";
    public static final String snSurvivorsHistogramMutTypeGenePassenger = "Survivors - Histogram of Passenger Mutation Types Comparring Genes";
    public static final String snShadowHistogramMutTypeGenePassenger = "Shadow - Histogram of Passenger Mutation Types Comparring Genes";
    public static final String snSurvivorsHistogramMutTypeGeneDriver = "Survivors - Histogram of Driver Mutation Types Comparring Genes";
    public static final String snShadowHistogramMutTypeLocusPassenger = "Shadow - Histogram of Passenger Mutation Types Comparring Locus";
    public static final String snFishplotScriptForREnvironment = "Fishplot Script for R Environment";
    public static final String snShadowHistogramMutTypeLocusDriver = "Shadow - Histogram of Driver Mutation Types Comparring Locus";
    public static final String fileNameDriverMutationsPerCycleHavingQuota = "Driver Mutations Per Cycle Having Quota";
    public static final String fileNamePassengerMutationsPerPopSizeHavingQuota = "Passenger Mutations Per Pop Size Having Quota";
    public static final String fileNameClonesPerCycleHavingQuota = "Clones Per Cycle Having Quota";
    public static final String fileNamePassengerMutationsPerCycleHavingQuota = "Passenger Mutations Per Cycle Having Quota";
    public static final String fileNameDriverMutationsPerPopSizeHavingQuota = "Driver Mutations Per Pop Size Having Quota";
    public static final String driversPerCycleFileName = "Drivers Per Cycle";
    public static final String passengersPerCycleFileName = "Passengers Per Cycle";
    public static final String rsaChartFileNamePassengersPerCycle = "Passengers Per Cycle";
    public static final String rsaChartFileNameDriversPerPopSize = "Drivers Per Pop Size";
    public static final String rsaChartFileNameClonesPerPopSize = "Clones Per Population Size";
    public static final String rsaChartFileNameDriversPerCycle = "Drivers Per Cycle";
    public static final String rsaChartFileNameClonesPerCycle = "Clones Per Cycle";
    public static final String rsaChartFileNamePassengersPerPopSize = "Passengers Per Pop Size";
    public static final String benchmarkSummaryFileName = "Benchmark Summar.csv";
    public static final String benchmarkKeyFiguresFileName = "Runtime Benchmark Key Figures.txt";
}
