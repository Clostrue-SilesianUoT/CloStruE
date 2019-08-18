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

package clostrue.postprocessing.visualization;
 
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Wrapper for node collection
 * 
 * @author Krzysztof Szymiczek
 */


public class NodeCollection {
    
    protected HashMap <Long, Node> nodes_ht;
    GenomeCollection genomes;
    CellCollection cells;

    public NodeCollection(GenomeCollection inGenomes, CellCollection inCells) {
        this.nodes_ht = new HashMap<>();
        this.genomes = inGenomes;
        this.cells = inCells;
    }   

    public int size(){
        return nodes_ht.size();
    }
   
    public void setCloneMarkerColor(CellCollection cells){
        for (Map.Entry<Long, Node> e : nodes_ht.entrySet()){
            e.getValue().setCloneMarkerColor(cells, genomes,GraphGenerator.colorsTable);
        };         
    }
    
    public void setHeatMapColorDrivers(CellCollection cells, int maxValue){
        for (Map.Entry<Long, Node> e : nodes_ht.entrySet()){
            e.getValue().setHeatMapColorDrivers(genomes, cells, maxValue);
        };                      
    }
    
    public void put(Node node){
        Long key = node.getuIDLong();
        nodes_ht.put(key, node);
    }
    
    public void linkNodes(){
        for (Map.Entry<Long, Node> e : nodes_ht.entrySet()){
            Node child = e.getValue();
            Node parent = nodes_ht.get(e.getValue().getpIDLong());
            if (parent != null){
                parent.addChildren(child);                
            }
        }        
    }
    
    public Map<Integer, List<Node>> getNodesByLevel(CellCollection cells){
        return
            this.nodes_ht.values()
            .stream()
            .collect(Collectors.groupingBy(Node::getLevel));       
    }

    public Node get(int cycle, int id){
        Long key = Node.buildLongID(cycle, id);
        return nodes_ht.get(key);
    }
    
    public Node get(long key){
        return nodes_ht.get(key);
    }
    
    public void remove(Node n){
        Long key = n.getuIDLong();
        nodes_ht.remove(key); //strange
    }

    public Collection<Node> values(){
        return nodes_ht.values();
    }
}
