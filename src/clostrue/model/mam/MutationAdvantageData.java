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
 
/**
 * Models the fitness advantage of a mutation on a particular gene.
 * 
 * @author Krzysztof Szymiczek
 */

public class MutationAdvantageData {
    
    private final String    geneName;       //Gene Name (Description)
    private final double    fitAdvantage;   //Fitess Advantage / Disadvantage
    private final int       geneSize;       //Gene Width -> is the gene Size
    private final String    geneTag;        //Gene Tag for groupping
    private final int       geneTagId;      //Gene Tag ID for groupping
    
    /**
     * Constructor.
     * Creates a single mutation advantage
     * 
     * @param inGeneName        Gene Name (Description)
     * @param inFitAdvantage    Fitness Advantage / Disadvantage
     * @param inGeneSize        Gene size
     * @param inGeneTag         String Tag for Gene Groupping
     */
    public MutationAdvantageData(
            String inGeneName,
            double inFitAdvantage,
            int inGeneSize,
            String inGeneTag,
            Integer inGeneTagId){
        
        geneName        = inGeneName;
        fitAdvantage    = inFitAdvantage;
        geneSize        = inGeneSize;
        geneTag         = inGeneTag;
        geneTagId       = inGeneTagId;
        
    }

    public MutationAdvantageData(MutationAdvantageData source){
        geneName        = source.geneName;
        fitAdvantage    = source.fitAdvantage;
        geneSize        = source.geneSize;
        geneTag         = source.geneTag;
        geneTagId       = source.geneTagId;
    }
    
    public String getGeneName() {
        return geneName;
    }

    public int getGeneSize() {
        return geneSize;
    }

    public double getFitAdvantage() {
        return fitAdvantage;
    }

    public String getGeneTag() {
        return geneTag;
    }

    public int getGeneTagId() {
        return geneTagId;
    }


    
}
