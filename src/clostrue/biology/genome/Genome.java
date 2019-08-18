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
package clostrue.biology.genome;
import clostrue.collections.GenomeCollection;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.model.SimModel;
import java.util.Random;

/**
 * Implements the genome of a single cell within the model.
 * 
 * The genome stores the information about mutation count on each gene locations
 * and the influence of those mutations on the fit advantage / disadvantage.
 * Passenger and Driver mutations can be introduced into the genome using
 * methods inside this class.
 * 
 * @author Krzysztof Szymiczek 
 */

public class Genome {

    final private int driversPartIndex;           //Genome part drivers index
    final private int passengersPartIndex;        //Genome part passengers
    private float   totalDriversAdv;            //Fit advantage caused by mutations Equivalent to (1+Sd)^d 
    private float   totalPassengersAdv;         //Fit disadvantage caused by mutations Equivalent to (1+Sp)^p
    private float  driverPassengerRatio;       //driver to passenger ratio for heatmaps
    private int     driverMutationCount;        //the number of driver mutations
    private int     passengerMutationCount;     //the number of passenger mutations

    public int getDriversPartIndex() {
        return driversPartIndex;
    }

    public int getPassengersPartIndex() {
        return passengersPartIndex;
    }
    
    
    
    public int getDriverCloneGroupID(){
        return driversPartIndex;
    }
   
    /**
     * Get genome part Drivers
     * @param genomes sychronized collection of genomes
     * @return genome part Drivers
     */
    public GenomePart getDrivers(GenomeSynchronizedCollection genomes) {
        return genomes.getDrivPartByIndex(driversPartIndex);
    }
    public GenomePart getDrivers(GenomeCollection genomes) {
        return genomes.getDrivPartByIndex(driversPartIndex);
    }

    /**
     * Get genome part Passengers
     * @param genomes sychronized collection of genomes
     * @return genome part Passengers
     */
    public GenomePart getPassengers(GenomeSynchronizedCollection genomes) {
        return genomes.getPassPartByIndex(passengersPartIndex);
    }    
    public GenomePart getPassengers(GenomeCollection genomes) {
        return genomes.getPassPartByIndex(passengersPartIndex);
    } 
    /**
     * Returns the ammount of driver mutations accumulated in the cell
     * @return ammount of driver mutations
     */
    public int getDriverMutationCount() {
            return driverMutationCount;
    }

    /**
     * Returns the ammount of passenger mutations accumulated in the cell
     * @return ammount of passenger mutations
     */
    public int getPassengerMutationCount() {
            return passengerMutationCount;
    }    
    
    /**
     * Cell genome constructor for a fresh "clean" cell of the initial population
     * 
     * This consturctor is called for cells created for the initial population
     * at the start of the simulation. The source of information about the genes
     * is the mutation advantage model passed to this constructor. The created
     * cell has no mutation, thus the birt rate is equal to 1.
     * 
     * @param genomes
     */
    public Genome(GenomeSynchronizedCollection genomes) {

        GenomePart drivers          = new GenomePart();
        driversPartIndex            = genomes.addDrivPartAndReturnIndex(drivers);

        GenomePart passengers       = new GenomePart();
        passengersPartIndex         = genomes.addPassPartAndReturnIndex(passengers);
                                  
        totalDriversAdv             = 1;
        totalPassengersAdv          = 1;
        driverPassengerRatio        = 0;
        driverMutationCount         = 0;
        passengerMutationCount      = 0;
        
    }
    public Genome(GenomeCollection genomes) {

        GenomePart drivers          = new GenomePart();
        driversPartIndex            = genomes.addDrivPartAndReturnIndex(drivers);

        GenomePart passengers       = new GenomePart();
        passengersPartIndex         = genomes.addPassPartAndReturnIndex(passengers);
                                  
        totalDriversAdv             = 1;
        totalPassengersAdv          = 1;
        driverPassengerRatio        = 0;
        driverMutationCount         = 0;
        passengerMutationCount      = 0;
        
    }
    /**
     * Calculates the mutation ratio for the heatmap colloring
     * @param driverMutationsCount      //driver mutations count
     * @param passengerMutationsCount   //passenger mutations count
     * @return                          //calculated ratio
     */
    static public float staticCalculateDriverPassengerRatio(int driverMutationsCount, int passengerMutationsCount){
        if ( driverMutationsCount == 0 || passengerMutationsCount == 0)
            return 0; 
        else{
            return (float)Math.log10(
                    (double) driverMutationsCount  
                  / (double) passengerMutationsCount 
                  + (double)1 
            ) ;
        }            
    }
  
    /**
     * Get Driver to Passenger mutation count ratio
     * @return driver to passenger mutation count ratio
     */
    public float getDriverPassengerRatio() {
        return driverPassengerRatio;
    }

    /**
     * This clone constructor deep copies the cell genome while cell division occurs.
     * 
     * A copy of the source (parent) cell genome part is created using this
     * copying constructor or exact reference is duplicated (when no mutation)
     * 
     * @param parentGenomeIndex       index of parent cell genome
     * @param genomes           collection of genomes
     * @param addDrivMutation   should driver mutation be added?
     * @param addPassMutation   should passenger mutation be added?
     * @param simModel          simulation model
     * @param _randomGenerator  random generator object
     */
    public Genome(int parentGenomeIndex,
            GenomeSynchronizedCollection genomes,
            boolean addDrivMutation, 
            boolean addPassMutation,
            SimModel simModel, 
            Random _randomGenerator) {

        Genome parentCellGenome = genomes.getByIndex(parentGenomeIndex);
                
        if (addDrivMutation == true){
            GenomePart drivers = new GenomePart(parentCellGenome.driversPartIndex);
            driversPartIndex = genomes.addDrivPartAndReturnIndex(drivers);
        } else {
            driversPartIndex = parentCellGenome.driversPartIndex;
        }
        
        if (addPassMutation == true){
            GenomePart passengers = new GenomePart(parentCellGenome.passengersPartIndex);
            passengersPartIndex = genomes.addPassPartAndReturnIndex(passengers);
        } else {
            passengersPartIndex = parentCellGenome.passengersPartIndex;
        }

        
        driverMutationCount         = parentCellGenome.driverMutationCount;        
        passengerMutationCount      = parentCellGenome.passengerMutationCount;        
        totalDriversAdv             = parentCellGenome.totalDriversAdv;
        totalPassengersAdv          = parentCellGenome.totalPassengersAdv;
        driverPassengerRatio        = parentCellGenome.getDriverPassengerRatio();

        if (addDrivMutation == true){
            mutateOnDriverLocci(
                    genomes,
                    simModel, 
                    _randomGenerator);
        }
        if (addPassMutation == true){
            mutateOnPassengerLocci(
                    genomes,
                    simModel, 
                    _randomGenerator);
        }       
        
    }

    /**
     * Creates single (driver) mutation on randlomly choosen driver gene.
     * 
     * After the gene is deteminded by random generator, a driver mutation is reflected
     * in the table with driver mutation count per driver gene.
     * The fit advantage is corrected, so the new mutation has an influence of
     * the birth rate of the cell.
     * 
     * @param genomes
     * @param simModel          simulation model used in sumulation
     * @param randomGenerator   random number generator used in simulation
     */

    public final void mutateOnDriverLocci(GenomeSynchronizedCollection genomes, SimModel simModel, Random randomGenerator) {

        // which gene will be affected
        int _tossResult = randomGenerator.nextInt(simModel.getModParams().getMAM().getGenomeDriversPartSize());
        // get the gene id based on the toss result
        // this is weighted based on the particular gene width in Mutation Advantage Model
        int _tossedGene = simModel.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(_tossResult);

        genomes.getDrivPartByIndex(driversPartIndex).setMutation(_tossResult);
        driverMutationCount++;
        //correct the nominator of Birth Rate
        double _multiplicator = simModel.getModParams().getMAM().getDrivers()[_tossedGene].getFitAdvantage();
        totalDriversAdv *= ((double) 1.0 + _multiplicator);
        //calculate the ratio
        driverPassengerRatio = Genome.staticCalculateDriverPassengerRatio(driverMutationCount,passengerMutationCount); 
    }

    /**
     * Creates single (passenger) mutation on randlomly choosen passenger gene.
     * 
     * After the gene is deteminded by random generator, a passenger mutation is reflected
     * in the table with passenger mutation count per driver gene.
     * The fit advantage is corrected, so the new mutation has an influence of
     * the birth rate of the cell.
     * 
     * @param genomes
     * @param simModel          simulation model used in simulation
     * @param randomGenerator   random number generator used in simulation
     */
    
    public final void mutateOnPassengerLocci(GenomeSynchronizedCollection genomes, SimModel simModel, Random randomGenerator) {

        // which gene will be affected
        int _tossResult = randomGenerator.nextInt(simModel.getModParams().getMAM().getGenomePassengersPartSize());
        // get the gene id based on the toss result
        // this is weighted based on the particular gene width in Mutation Advantage Model
        int _tossedGene = simModel.getModParams().getMAM().getRegionMapPassengers().getGeneBasedOnToss(_tossResult);

        genomes.getPassPartByIndex(passengersPartIndex).setMutation(_tossResult);
        passengerMutationCount++;
        //correct the denominator of Birth Rate
        double _multiplicator = simModel.getModParams().getMAM().getPassengers()[_tossedGene].getFitAdvantage();
        totalPassengersAdv *= ((double) 1.0 + _multiplicator);
        //calculate the ratio
        driverPassengerRatio = Genome.staticCalculateDriverPassengerRatio(driverMutationCount,passengerMutationCount); 
        
    }

    /**
     * Returns the total drivers advantage (nominator of the Birth rate)
     * @return total drivers advantage
     */
    public double getTotalDriversAdv() {
        return totalDriversAdv;
    }

    /**
     * Returns the total passengers disadvantahe (denominator of the Birth rate)
     * @return total passenger disadvantage
     */
    public double getTotalPassengersAdv() {
        return totalPassengersAdv;
    }
      
}
