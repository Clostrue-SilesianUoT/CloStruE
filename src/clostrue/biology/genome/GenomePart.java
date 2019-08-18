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
import clostrue.enumerations.MutationType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Genome consists of two parts -> the information about 
 * passenger gene mutations and driver gene mutations  is stored separatelly
 * in two sub-objects: GenomePart.
 * This class holds the information about mutation in one of the fwo
 * parts even driver or passenger. Please reffer to Genome class for the usage
 * The information about mutations is hold in that way that only curent mutation
 * is storred and a reference to parent cell's genome part. To track the whole
 * mutations in the genome part you have to travel-up along parrents and add the 
 * single mutation from each parent genome part to complete the whole list
 * the mother cell (top-up parent) will have no mutations.
 * 
 * @author Krzysztof Szymiczek 
 */
public class GenomePart {

    private static short lastCloneGroupID;                //last created clone Group ID. Used for autonummeration purpouses    
    private static int dummyPartIndex = 0;                    //index of dummy reference used when there is no mutation
    private int mutation;                                 //curently added mutation
    private final int parentPartIndex;                    //reference to the genome part of the parrent

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.mutation;
        hash = 47 * hash + this.parentPartIndex;
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
        final GenomePart other = (GenomePart) obj;
        if (this.mutation != other.mutation) {
            return false;
        }
        return this.parentPartIndex == other.parentPartIndex;
    }


    
    /**
     * get dummy reference genome where there is no mutation
     * @param genomes synchronized collection of genomes
     * @return dummy reference genome
     */
    public static GenomePart getDummyDriver(GenomeSynchronizedCollection genomes) {
        return genomes.getDrivPartByIndex(dummyPartIndex);
    }

    /**
     * get dummy reference genome where there is no mutation
     * @param genomes collection of genomes
     * @return dummy reference genome
     */
    public static GenomePart getDummyDriver(GenomeCollection genomes) {
        return genomes.getDrivPartByIndex(dummyPartIndex);
    }
    
    
    /**
     * get dummy reference genome where there is no mutation
     * @param genomes sychronized collection of genomes
     * @return dummy reference genome
     */
    public static GenomePart getDummyPassenger(GenomeSynchronizedCollection genomes) {
        return genomes.getPassPartByIndex(dummyPartIndex);
    }
    /**
     * Returns dummy reference genome
     * @param genomes genome collection
     * @return dummy genome part
     */
    public static GenomePart getDummyPassenger(GenomeCollection genomes) {
        return genomes.getPassPartByIndex(dummyPartIndex);
    }
    
    /**
     * sets the current obtained mutation
     * @param mutation locus of the mutation
     */
    public void setMutation(int mutation) {
        this.mutation = mutation;
    }
  
    /**
     * Constructor for the genome part for the cells from the initial population
     */
    public GenomePart() {
        mutation = Integer.MIN_VALUE;
        parentPartIndex = dummyPartIndex;
//        cloneGroupID = 0;
    }

    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param mT mutation type
     * @param genomes sychronized collection of genomes
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getMutations(MutationType mT, GenomeSynchronizedCollection genomes) {
        if (mT.equals(MutationType.Driver)){
            return getDrivMutations(genomes);
        }
        if (mT.equals(MutationType.Passenger)){
            return getPassMutations(genomes);
        }
        return null;
    }     
    
    /*
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getMutations(MutationType mT, GenomeCollection genomes) {
        if (mT.equals(MutationType.Driver)){
            return getDrivMutations(genomes);
        }
        if (mT.equals(MutationType.Passenger)){
            return getPassMutations(genomes);
        }
        return null;
    }     

    
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes synchornized collection of genomes
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getDrivMutations(GenomeSynchronizedCollection genomes) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        int prevParentPartIndex = -1;
        GenomePart dummyDriver = GenomePart.getDummyDriver(genomes);
        while ( ( iterator != dummyDriver ) 
            && ( iterator.parentPartIndex != prevParentPartIndex) ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
            prevParentPartIndex = iterator.parentPartIndex;
            iterator = genomes.getDrivPartByIndex(iterator.parentPartIndex);
        }
        return mutations;
    }     

    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes collection of genomes
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getDrivMutations(GenomeCollection genomes) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        int prevParentPartIndex = -1;
        GenomePart dummyDriver = GenomePart.getDummyDriver(genomes);
        while ( ( iterator != dummyDriver ) 
            && ( iterator.parentPartIndex != prevParentPartIndex) ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
            prevParentPartIndex = iterator.parentPartIndex;
            iterator = genomes.getDrivPartByIndex(iterator.parentPartIndex);
        }
        return mutations;
    }        
    
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes sycnhronized collection of genomes
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getPassMutations(GenomeSynchronizedCollection genomes) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        int prevParentPartIndex = -1;
        GenomePart dummyPassenger = GenomePart.getDummyPassenger(genomes);
        while ( ( iterator != dummyPassenger ) 
            && ( iterator.parentPartIndex != prevParentPartIndex) ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
            prevParentPartIndex = iterator.parentPartIndex;
            iterator = genomes.getPassPartByIndex(iterator.parentPartIndex);
        }
        return mutations;
    }     
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes collection of genomes
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getPassMutations(GenomeCollection genomes) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        int prevParentPartIndex = -1;
        GenomePart dummyPassenger = GenomePart.getDummyPassenger(genomes);
        while ( ( iterator != dummyPassenger ) 
            && ( iterator.parentPartIndex != prevParentPartIndex) ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
            prevParentPartIndex = iterator.parentPartIndex;
            iterator = genomes.getPassPartByIndex(iterator.parentPartIndex);
        }
        return mutations;
    }   
    
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes synchronized collection of genomes
     * @param blockMap if parent genome is already on the blockMap, the algorithm stops 
     * traveling upward (for optimization for repeated simulation analysis)
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getDrivMutationsWithBlockMap(GenomeSynchronizedCollection genomes, HashMap<GenomePart,Integer> blockMap) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        GenomePart dummyDriver = GenomePart.getDummyDriver(genomes);
        while ( iterator != dummyDriver ){ 
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
             
            iterator = genomes.getDrivPartByIndex(iterator.parentPartIndex);
            Integer genPartWasProcessed = blockMap.get(iterator);
            if( genPartWasProcessed != null ){
                return mutations;
            } else {
                blockMap.put(iterator, 1);
            }
        }
        return mutations;
    }    

    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param blockMap if parent genome is already on the blockMap, the algorithm stops 
     * traveling upward (for optimization for repeated simulation analysis)
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getDrivMutationsWithBlockMap(GenomeCollection genomes, HashMap<GenomePart,Integer> blockMap) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        GenomePart dummyDriver = GenomePart.getDummyDriver(genomes);
        while ( iterator != dummyDriver ){ 
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
             
            iterator = genomes.getDrivPartByIndex(iterator.parentPartIndex);
            Integer genPartWasProcessed = blockMap.get(iterator);
            if( genPartWasProcessed != null ){
                return mutations;
            } else {
                blockMap.put(iterator, 1);
            }
        }
        return mutations;
    }    
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes sychronized collection of genomes
     * @param blockMap if parent genome is already on the blockMap, the algorithm stops 
     * traveling upward (for optimization for repeated simulation analysis)
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */
    public ArrayList<Integer> getPassMutationsWithBlockMap(GenomeSynchronizedCollection genomes, HashMap<GenomePart,Integer> blockMap) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        GenomePart dummyPassenger = GenomePart.getDummyPassenger(genomes);
        while ( iterator != dummyPassenger ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
             
            iterator = genomes.getPassPartByIndex(iterator.parentPartIndex);
            Integer genPartWasProcessed = blockMap.get(iterator);
            if( genPartWasProcessed != null ){
                return mutations;
            } else {
                blockMap.put(iterator, 1);
            }
        }
        return mutations;
    }    
    /**
     * Returns all the mutations in the given genome part by traveling subsequently
     * up in the genealogy from cell to parent, parent's parent, etc...
     * See the class description for more information
     * @param genomes collection of genomes
     * @param blockMap if parent genome is already on the blockMap, the algorithm stops 
     * traveling upward (for optimization for repeated simulation analysis)
     * @return array list of all mutations (accumulated in the cell) 
     * in fact this is a list of locus-es.
     */    
    public ArrayList<Integer> getPassMutationsWithBlockMap(GenomeCollection genomes, HashMap<GenomePart,Integer> blockMap) {
        ArrayList<Integer> mutations = new ArrayList<> (); 
        GenomePart iterator;
        iterator = this;
        GenomePart dummyPassenger = GenomePart.getDummyPassenger(genomes);
        while ( iterator != dummyPassenger ){
            if ( iterator.mutation != Integer.MIN_VALUE ){
                mutations.add(iterator.mutation);                
            }
             
            iterator = genomes.getPassPartByIndex(iterator.parentPartIndex);
            Integer genPartWasProcessed = blockMap.get(iterator);
            if( genPartWasProcessed != null ){
                return mutations;
            } else {
                blockMap.put(iterator, 1);
            }
        }
        return mutations;
    }    
        
    /**
     * Constructor for the genome part for the cells which are created by the
     * event of cell division. 
     * @param sourceGenomePartIndex
     */
    public GenomePart(int sourceGenomePartIndex){        
       mutation = Integer.MIN_VALUE;
       parentPartIndex = sourceGenomePartIndex;
       
    }

    /**
     * Returns the acquired mutation
     * @return acquired mutation
     */
    public int getMutation() {
        return mutation;
    }

    /**
     * Initializes the unique cell id autonummeration value.
     * Called once at the simulation begin to set zero value.
     */
    static public synchronized void clearLastCloneGroupID(){
        lastCloneGroupID = 0;
    }

    // todo make this more optimal -> may be using some class to unique ID's
    public static synchronized short getNewCloneGroupID(){
        return ++lastCloneGroupID;
    }

    /**
     * Returns parent genome part
     * @param mT mutation type
     * @param genomes synchronized collection of genomes
     * @return parent genome part
     */
    public GenomePart getParentPart(MutationType mT, GenomeSynchronizedCollection genomes) {
        if (mT.equals(MutationType.Driver)){
            return getParentDrivPart(genomes);            
        }
        if (mT.equals(MutationType.Passenger)){
            return getParentPassPart(genomes);            
        }
        return null;
    }    

    /**
     * Returns parent genome part
     * @param genomes synchronized collection of genomes
     * @return parent genome part
     */
    public GenomePart getParentDrivPart(GenomeSynchronizedCollection genomes) {
        return genomes.getDrivPartByIndex(parentPartIndex);            
    }

    public GenomePart getParentPassPart(GenomeSynchronizedCollection genomes) {
        return genomes.getPassPartByIndex(parentPartIndex);            
    }

    public int getParentCloneGroupID(){
        return parentPartIndex;
    }
}
