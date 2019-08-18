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
package clostrue.toolbox;

import clostrue.biology.genome.GenomePart;
import clostrue.collections.CellIndexHolder;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.HashmapName;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Various calculations for HashMap Sizes
 * @author Krzysztof Szymiczek
 */
public class HashMapSizeTools {
    
    private static final int intSize = 4; //bytes
    private static final int integerReference = 8; //bytes
    private static final int genomeReference = 8; //byres
    
    private static long getHM1Size(HashMap<Integer, HashMap<GenomePart, CellIndexHolder>> hm){
        long size = 0; //in bytes
        for (Map.Entry<Integer, HashMap<GenomePart, CellIndexHolder>> entry : hm.entrySet()){
            size += integerReference; //Integer Reference
            for (Map.Entry<GenomePart, CellIndexHolder> entryDeep : entry.getValue().entrySet()){
                size += genomeReference; //Genome Part Reference
                size += ( entryDeep.getValue().size() * intSize );
            }     
        }
        return size;
    }
    
    public static void logHM1Size(HashMap<Integer, HashMap<GenomePart, CellIndexHolder>> hm, String hmName){
        if (Constant.logHmSize){
            long size = getHM1Size(hm);
            StaticConsoleLogger.log(HashmapName.hashMapSize + hmName + HashmapName.isEqual + String.valueOf(size));            
        }
    }
    
    private static long getHM2Size(ConcurrentHashMap<Integer, CellIndexHolder>[] hm){
        long size = 0; //in bytes
        int tabSize = hm.length;
        for (int i = 0; i < tabSize; i++){
            for (Map.Entry<Integer, CellIndexHolder> entry : hm[i].entrySet()){
                size += integerReference;
                size += ( entry.getValue().size() * intSize );
            }
        }
        return size;
    }

    public static void logHM2Size(ConcurrentHashMap<Integer, CellIndexHolder>[] hm, String hmName){
        if (Constant.logHmSize){
            long size = getHM2Size(hm);
            StaticConsoleLogger.log(HashmapName.hashMapSize + hmName + HashmapName.isEqual + String.valueOf(size));            
        }
    }
    
    private static long getHM3Size(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> hm){
        long size = 0;
         for (Map.Entry<String, ConcurrentHashMap<Integer, Integer>> entry : hm.entrySet()){
             size += entry.getKey().length();
             size += ( 2 * integerReference * entry.getValue().size());
    }
        return size;
    }
    
    public static void logHM3Size(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> hm, String hmName){
        if (Constant.logHmSize){
            long size = getHM3Size(hm);
            StaticConsoleLogger.log(HashmapName.hashMapSize + hmName + HashmapName.isEqual + String.valueOf(size));            
        }
        
    }
    
}
