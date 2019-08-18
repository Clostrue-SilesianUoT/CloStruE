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

import clostrue.biology.cell.Cell;
import java.util.ArrayList;
import java.util.List;

/**
 * Genome collection which allows accessing genomes by index (less memory)
 * @author Krzysztof Szymiczek 
 */


public class CellCollection {
    
    private final List<Cell> cells;

    public CellCollection() {
        this.cells = new ArrayList<>();
    }
    
    public int addAndReturnIndex(Cell cell){
        this.cells.add(cell);
        int cellIndex = cells.size() - 1;
        return ( cellIndex );
    }
    
    public Cell getByIndex(int cellIndex){
        return cells.get(cellIndex);
    }
    
    public ArrayList<Cell> getByCellIndexHolder(CellIndexHolder cih){
        int[] indexes = cih.getIndexes();
        ArrayList<Cell> list = new ArrayList<>();
        for(int i = 0; i < indexes.length; i++){
            list.add(getByIndex(cih.get(i)));
        }
        return list;
    }
    
}
