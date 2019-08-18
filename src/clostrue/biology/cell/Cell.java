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
package clostrue.biology.cell;

import clostrue.biology.genome.Genome;
import clostrue.collections.GenomeCollection;
import clostrue.collections.GenomeSynchronizedCollection;
import clostrue.model.SimModel;
import clostrue.enumerations.LifeTick;
import clostrue.toolbox.StaticConsoleLogger;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements one Physical cell of a population.
 * 
 * @author Krzysztof Szymiczek 
 */
public class Cell {

    //static components for providing the services for autonummeration
    //of Clone Group ID's
    
    private static AtomicInteger lastId     = new AtomicInteger(0); //last created cell ID. Used for autonummeration purpouse
    private short               modelCycle  = -32767;                    //cell model cycle (shifted with place for "-1")
    private boolean             isAlive     = true;    //is cell alive? dead cells leave till they are saved by writer
    private short               age         = -32768;                    //Cell Age (modell cycles) (shifted)
    final private int           id;                 //Unqiue cell Identificator
    private int                 _parentCellID;      //Cell ID of the Parent Cell. 0 for initial population
    final private int          genomeIndex;         //Index to Cell Biological Genome 
    private float              _divisionProb;      //Division Probability in next cycle
    private float              _deathProb;         //Death probability in next cycle
    private int                 _cellCountN;        //Cell count in the population

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.modelCycle;
        hash = 89 * hash + (this.isAlive ? 1 : 0);
        hash = 89 * hash + this.age;
        hash = 89 * hash + this.id;
        hash = 89 * hash + this._parentCellID;
        hash = 89 * hash + this.genomeIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cell other = (Cell) obj;
        if (this.modelCycle != other.modelCycle) {
            return false;
        }
        if (this.isAlive != other.isAlive) {
            return false;
        }
        if (this.age != other.age) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (this._parentCellID != other._parentCellID) {
            return false;
        }
        if (this.genomeIndex != other.genomeIndex) {
            return false;
        }
        return true;
    }

    
    
    /**
     * Cell to text
     * @return unique cell id
     */
    @Override
    public String toString() {
        return "c" + String.valueOf(modelCycle+32767) + "_i" + String.valueOf(id);
    }    
    
    public int getModelCycle() {
        return modelCycle+32767;
    }

    public void setModelCycle(int modelCycle) {
        this.modelCycle = (short)(modelCycle - 32767);
    }

    /**
     * Autonummeration method
     * @return unique cell id for Cell constructor
     */    
    
    // todo make this more optimal -> may be using some class to unique ID's
    public static synchronized int getNewCellID(){
        return lastId.incrementAndGet();
    }  
    
    /**
     * Cell constructor for the initial population.
     * 
     * In the event of creation of initial population for simulation,
     * the created cells have no parents -> thus they cannot inherit either the
     * genome nor the information of the parents.
     * 
     * @param simModel              Simulation Model
     * @param genomes               Genome Collection
     * @param motherCell            Reference to the first created cell. All cells from the initial population 
     *                              Shares the same genome (are in one technical "clone group"
     */
        public Cell(
            SimModel simModel,
            GenomeSynchronizedCollection genomes,
            Cell motherCell) {

        isAlive = true;    
        id             = getNewCellID();

        //creating cell genome based on the Mutation Advantage Model for the first cell evel
        //next cells from the initial population will share the empty genome with the mother cell
        
        if(motherCell == null) {
            Genome genome = new Genome(genomes);
            genomeIndex = genomes.addAndReturnIndex(genome);
        } else {
            genomeIndex = motherCell.genomeIndex;
        }        
        
        // calculate statistics for simulation cycles
        _calcDivisionProbability(genomes);
        _calcDeathProbability(
                simModel.getModParams().getInitCellCountK(), 
                simModel.getModParams().getInitCellCountK());
        
    }
    
    /**
     * Cell constructor for the event of division.
     * 
     * When a cell divide -> new cell is created inheriting the information
     * about it's parent and inheriting the cell genome. Additional count
     * of Driver and Passenger mutation can be introduced and randomly distributed
     * in the genome of the new cell. If the cel is created without mutation
     * the genome does not have to be copied, instead the reference could point
     * to  the same genome to save memory. As long as the genome of cells does not change
     * it can be shared among many cells. Afterward, when first mutation occurs, the genome
     * gets copied, and can be shared again under clones of this division
     * this means that in particular on the memory exists only as much cell genomes, as much
     * different genomes are in the population
     * 
     * @param simModel              Simulation Model
     * @param genomes               GenomeSynchronizedCollection
     * @param sourceCell            Mother cell
     * @param addDrivMutation       Should there be added a Driver mutation?
     * @param addPassMutation       Should there be added a Passenger mutation?
     * @param cellCountN            Curent population size  (used for probabilities)
     * @param _randomGenerator      Random generator used for simulating events
     */
    public Cell(
            SimModel simModel, 
            GenomeSynchronizedCollection genomes,
            Cell sourceCell, 
            boolean addDrivMutation, 
            boolean addPassMutation,
            int cellCountN, 
            Random _randomGenerator) {

        sourceCell.age++;   //source cell gets allocated to the new cycle,
                            //so it's age have to be incremented as well

        isAlive = true;
        id                          = Cell.getNewCellID();
        _parentCellID               = sourceCell.id;
        _cellCountN                 = cellCountN;
        sourceCell._parentCellID    = sourceCell.id;
                
        //copying the cell genome from the source cell (and add mutations if specified).
        //if no mutation - genome can reference to existing one from the source cell.
        if ( ( addDrivMutation || addPassMutation ) == false ){
            //coopy genome reference -> no mutation
            genomeIndex = sourceCell.genomeIndex;
        } else {
            //create new genome and introduce mutation of the given type
            Genome genome = new Genome(sourceCell.genomeIndex, genomes, addDrivMutation, addPassMutation, simModel,_randomGenerator);
            genomeIndex = genomes.addAndReturnIndex(genome);
        }
        
        // calculate statistics for simulation cycles
        _calcDivisionProbability(genomes);
        _calcDeathProbability(cellCountN, simModel.getModParams().getInitCellCountK());

    }

    /**
     * Copying constructor which makes a copy of the cell but with shared genome
     * copies are passed to memory for example for graph generation.
     * @param sourceCell source cell to copy from
     */
    public Cell(Cell sourceCell) {
           
        if (sourceCell == null){
            StaticConsoleLogger.log("Critical error: Source cell is null");
        }
        
        isAlive                     = sourceCell.isAlive;
        id                          = sourceCell.id;
        _parentCellID               = sourceCell._parentCellID;
        age                         = sourceCell.age;
        _cellCountN                 = sourceCell._cellCountN;
        genomeIndex                 = sourceCell.genomeIndex;
        _divisionProb               = sourceCell._divisionProb;
        _deathProb                  = sourceCell._deathProb;
        modelCycle                  = sourceCell.modelCycle;
        
    }        
        
    /**
     * Fake cell constructor for simulation node artificial cell
     * used for plotting only 
     * @param genomes Genome Collection
     * @param fake
     */
    
    public Cell(GenomeSynchronizedCollection genomes, boolean fake){
        isAlive                     = true;
        modelCycle                  = -32768; //real value: -1 (shifted by -32767)
        id                          = 0;
        _parentCellID               = 0;
        Genome genome               = new Genome(genomes);
        genomeIndex                 = genomes.addAndReturnIndex(genome);
    }

    public Cell(GenomeCollection genomes, boolean fake){
        isAlive                     = true;
        modelCycle                  = -32768; //real value: -1 (shifted by -32767)
        id                          = 0;
        _parentCellID               = 0;
        Genome genome               = new Genome(genomes);
        genomeIndex                 = genomes.addAndReturnIndex(genome);
    }
    
    public int getCellCountN() {
        return _cellCountN;
    }       
        
    /**
     * Returns the event "Life Tick" haeppening to the cell.
     * 
     * Cell Can Divide (healthy or with mutations), or Die.
     * 
     * @param _randomGenerator Random generator used for simulating events
     * @return LifeTick the information what haeppened to the cell
     */
    public LifeTick getLifeTick(Random _randomGenerator) {

        // get the value for normalization (stretching the 0..1)
        // cell can divide or die
        double _upperRandomLimit = _divisionProb + _deathProb;

        // lets decide what haeppens in the current live tick
        // based on the normalized (stretched) random from generator
        double _tossResult = _upperRandomLimit * _randomGenerator.nextDouble();

        // return the result of the life tick (what haeppended to the cell
        // in this iteration of modelling)
        if (_tossResult <= _divisionProb) {
            // new cell is born in clean Division without mutations         
            return LifeTick.Division;
        } else {
            // cell dies
            return LifeTick.Death;
        }
    }

    // Calculates the probability that in the current live tick
    // the cell will divide (regaqrdles of the division type: clean,
    // or with passenger mutation or with driver mutation
    private void _calcDivisionProbability(GenomeSynchronizedCollection genomes) {
        Genome genome = genomes.getByIndex(genomeIndex);
        _divisionProb = (float)(genome.getTotalDriversAdv() / genome.getTotalPassengersAdv());
    
    }

    // Calculates the probability that in the current live tick
    // the cell will die
    private void _calcDeathProbability(int cellCountN, int initialCellCountK) {
        _deathProb = ((float) cellCountN) / ((float) initialCellCountK);
    }

    /**
     * Sets the cell state to death
     */
    public void die() {
        isAlive = false;
        age++;
    }

    /**
     * Returns the cell Life state.
     * 
     * This is used by the cell writer.
     * 
     * @return the _liveState
     */
    public boolean isAlive() {
        return isAlive;
    }
    
    public boolean isDead(){
        return !isAlive;
    }
    
    /**
     * Returns the cell Division Probability.
     * 
     * This is used by the cell writer.
     * @return
     */
    public double getDivisionProb() {
        return _divisionProb;
    }

    /**
     * Returns the current cell age
     * @return age of the cell
     */
    public int getAge() {
        return age+32768;
    }

    /**
     * Returns the cell unique-id.
     * This is used by the cell writer.
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the unique id of cell's-parent cell.
     * This is used by the cell writer.
     * @return
     */
    public int getParentCellID() {
        return _parentCellID;
    }

    /**
     * Returns the death probability in next simulation cycle.
     * This is used by the cell writer.
     * @return
     */
    public double getDeathProb() {
        return _deathProb;
    }

    /**
     * Returns the genome of cell.
     * This is used by the cell writer.
     * @param genomes Genome Collection
     * @return 
     */
    public Genome getGenome(GenomeSynchronizedCollection genomes) {
        return genomes.getByIndex(genomeIndex);
    }

    /**
     * Returns the genome of cell.
     * This is used by the cell writer.
     * @param genomes Genome Collection
     * @return 
     */
    public Genome getGenome(GenomeCollection genomes) {
        return genomes.getByIndex(genomeIndex);
    }    
    
    /**
     * Initializes the unique cell id autonummeration value.
     * Called once at the simulation begin to set zero value.
     */
    static public synchronized void clearLastCellID(){
        lastId.set(0);
    }
    
}
