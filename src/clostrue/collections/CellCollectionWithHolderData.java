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
import java.util.HashMap;
import java.util.Set;

/**
 * Cell collection with holder data
 * @author Krzysztof Szymiczek 
 */
public class CellCollectionWithHolderData {
 
    ArrayList<Cell> cells;
    HashMap<Cell, Integer> orderedCells = new HashMap<>();
    
    public CellCollectionWithHolderData(CellCollection cellCollection, CellIndexHolder cih){
        cells = cellCollection.getByCellIndexHolder(cih);       
        for (int i = 0; i < cells.size(); i++){
            orderedCells.put(cells.get(i), cih.get(i));
        }
    }
    
    public ArrayList<Cell> getCells(){
        return cells;
    }

    public CellIndexHolder getCellIndexHolder(ArrayList<Cell> alc){
        CellIndexHolder cih = new CellIndexHolder();
        alc.forEach((c) -> {
            cih.add(orderedCells.get(c));
        });
        return cih;
    }

    public CellIndexHolder getCellIndexHolder(Set<Cell> sc){
        CellIndexHolder cih = new CellIndexHolder();
        sc.forEach((c) -> {
            cih.add(orderedCells.get(c));
        });
        return cih;
    }

    
}
