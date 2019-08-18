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
package clostrue.collections;
import java.util.Arrays;

/**
 * This is an object for modelling holding several cells (indexes) together.
 * @author Krzysztof Szymiczek 
 */


public class CellIndexHolder {
    
    private     int bufSize = 0;
    private     int nextIndex = 0;   
    private     int usedSize = 0;
    private     int[] indexes = new int[bufSize];
    
    public CellIndexHolder(){
        //should be empty
    }
    
    public void removeDuplicates(){
        int existingIndexes[] = getIndexes();
        int newIndexes[] = new int[existingIndexes.length];
        Arrays.sort(existingIndexes);
        int lastIndex = Integer.MIN_VALUE;
        int lastSaveLocation = 0;
        for (int i = 0; i < existingIndexes.length; i++){
            if( existingIndexes[i] != lastIndex){
                newIndexes[lastSaveLocation] = existingIndexes[i];
                lastSaveLocation++;
                lastIndex = existingIndexes[i];
            }
        }
        indexes = newIndexes;
        bufSize = newIndexes.length;
        usedSize = lastSaveLocation;
    }
    
    public void add(int cellId){
        
        int newSize = 0;
        if (bufSize > usedSize){
            indexes[nextIndex] = cellId;
        } else {
            if ( bufSize == 0 ){
                newSize = 1;                
            } else {
                newSize = (int)((float)bufSize * (float)1.6 + (float)0.5);
            }

            int[] newIndexes = new int[newSize];
            bufSize = newSize;
            if( usedSize > 0)
                System.arraycopy(indexes, 0, newIndexes, 0, usedSize);
            indexes = newIndexes;
            indexes[nextIndex] = cellId;
        }
        nextIndex++;
        usedSize++;
    }
        
    public int size(){
        return usedSize;
    }
    
    public int[] getIndexes(){
        int[] occuped = new int[usedSize];
        System.arraycopy(indexes, 0, occuped, 0, usedSize);
        return occuped;
    }
    
    public int getFirst(){
        return indexes[0];
    }
    
    public void clear(){
        bufSize = 0;
        nextIndex = 0;
        usedSize = 0;
        indexes = new int[bufSize];
        
    }
    
    public int get(int i){
        return indexes[i];
    }
}
