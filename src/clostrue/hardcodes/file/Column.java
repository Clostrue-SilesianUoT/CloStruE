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
 * Order of columns in output files
 * @author Krzysztof Szymiczek
 */
public class Column {
    
    /**
     * contains fileds
     */
    static public enum cellFileFields {
	ModelCycle(0),
        CellCount(1),
        CellID(2),
        ParentCellID(3),
        CellAge(4),
        Drivers(5),
        Passengers(6),
        Birth(7),
        Death(8),
        DeathProb(9),
        DivisionProb(10),
        noMutationProb(11),
        driverMutProb(12),
        passengerMutProb(13),
        cloneGroup(14);
 
	private final int index;

	private cellFileFields(int i) {
            this.index = i;
	}
 
	public int getIndex() {
            return this.index;
	}
    }    
    
    static public enum mamFileFields {
	geneName(0),
        geneType(1),
        fitAdvantage(2),
//        maxMutations(3),
        geneSize(3),
        geneTag(4);
        
	private final int index;

	private mamFileFields(int i) {
            this.index = i;
	}
 
	public int getIndex() {
            return this.index;
	}
    }        
    
}
