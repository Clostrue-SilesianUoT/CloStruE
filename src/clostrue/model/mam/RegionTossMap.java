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
package clostrue.model.mam;
 
import clostrue.toolbox.StaticConsoleLogger;

/**
 * Tossing map which maps the tosses to selected genes
 * 
 * @author Krzysztof Szymiczek
 */
public class RegionTossMap {
    
    private final int[] genomeRegion;
     
    public static int calculateIntSize(MutationAdvantageData mutationAdvantages[]){
        int size = 0;
        for (int i = 0; i < mutationAdvantages.length; i++)
            size += mutationAdvantages[i].getGeneSize();
        return size;
    }    
    
    public RegionTossMap(MutationAdvantageData mutationAdvantages[]) {

        genomeRegion = new int[calculateIntSize(mutationAdvantages)];
        int curentValue = 0;
        int curentGene  = 0;
        
        for (MutationAdvantageData mad : mutationAdvantages){           
            for (int i = 0; i < mad.getGeneSize(); i++)
                genomeRegion[curentValue++] = curentGene;
            curentGene++;
        }
    }    
    
    
    public RegionTossMap(RegionTossMap source){

        genomeRegion = source.genomeRegion.clone();
        
    }
    
    // returns gene based on single toss
    public int getGeneBasedOnToss(int toss){
        if (toss >= genomeRegion.length){
            StaticConsoleLogger.log("Critical error. genomeRegion.length (" 
                    +
                    genomeRegion.length
                    + ") <= toss index ("
                    + String.valueOf(toss)
                    + ")");
            return 0;
        }
        return genomeRegion[toss];
    }
    
    // returns the size of the region
    public int getSize(){
        return genomeRegion.length;
    }
    
}


