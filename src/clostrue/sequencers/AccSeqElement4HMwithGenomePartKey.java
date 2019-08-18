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

package clostrue.sequencers;

import clostrue.biology.cell.Cell;
import clostrue.biology.genome.GenomePart;

/**
 * Access sequencer element for HashMaps with GenomePart as a key
 * @author Krzysztof Szymiczek
 */
public class AccSeqElement4HMwithGenomePartKey {
    
    GenomePart key;
    int size;
    Cell cell;

    public Cell getCell() {
        return cell;
    }

    
    
    public AccSeqElement4HMwithGenomePartKey(GenomePart key, int size, Cell cell) {
        this.key = key;
        this.size = size;
        this.cell = cell;
    }    

    public GenomePart getKey() {
        return key;
    }

    public Integer getSize() {
        return size;
    }
}
