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
 

import java.util.ArrayList;
import java.awt.Color;
import clostrue.biology.cell.Cell;
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeCollection;
import clostrue.collections.GenomeSynchronizedCollection;
import static clostrue.postprocessing.visualization.Node.buildLongID;
import static clostrue.postprocessing.visualization.Node.cells;

/**
 * Node class
 * The node is a single cell representation in a graph produced by the program
 * each cell in each generation is represented as a single graph note, thus if
 * one cell lives several generations, it will have several nodes in graph
 * the graph edges between nodes are modelling the relation
 * between cell / parent cells in the modell in subsequent generations
 * 
 * @author Krzysztof Szymiczek
 */
public class Node implements Comparable<Node>{

    protected static CellCollection cells;
    protected int cellIndex;
    protected int order;
    protected byte colorIndex  = 0;
    protected double cord_x     = 0;
    protected double cord_y     = 0;
    protected byte color_r;
    protected byte color_g;
    protected byte color_b;

    /**
     * indicates that X,Y coordinates of the node can be further changed
     * or the node position on the canvas is "glued"
     */
    protected boolean canBeMove = true;

    public String toString(CellCollection cells){
        Cell _originCell = cells.getByIndex(cellIndex);
        return "c" + _originCell.getModelCycle() + "_i" + _originCell.getId();
    }
    
    public static void setCells(CellCollection inCells){
        cells = inCells;
    }
    
    /**
     * reference to child node number 1
     */
    protected Node  children1;

    /**
     * reference to child node number 2
     */
    protected Node  children2;
    
   
    public double getCord_y() {
        return cord_y;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    
    public static Long buildLongID(int cycle, int id){
        Long lid = 100000000000l * cycle + id;
        return lid;        
    }
    
    public Node(Cell cell, int inCellIndex, CellCollection cells){
        
        this.children1        = null;
        this.children2        = null;
        this.cellIndex                 = inCellIndex;

    }

    public Long getuIDLong() {
        Cell _originCell = cells.getByIndex(cellIndex);
        return buildLongID(_originCell.getModelCycle(), _originCell.getId());
    }

    public Long getpIDLong() {
        Cell _originCell = cells.getByIndex(cellIndex);
        if (_originCell.getModelCycle() < 0){
            return getuIDLong();
        } else {
            return buildLongID(_originCell.getModelCycle() - 1, _originCell.getParentCellID());
        }
    }
       
    
    /**
     * seting x coordinate
     * @param x float
     */
    public void setCordX(double x) {
        if(canBeMove) {
            this.cord_x = x;
        }
    }
    public void forceSetCordX(double x) {
            this.cord_x = x;
    }
    /**
     * seting y coordinate
     * @param y float
     */
    public void setCordY(double y) {
        if(canBeMove) {
            this.cord_y = y;
        }
    }
    public void forceSetCordY(double y) {
            this.cord_y = y;
    }

    /**
     * seting x and y cooordinates
     * @param x float
     * @param y float
     */
    public void setCords(double x, double y) {
        if(canBeMove) {
            this.setCordX(x);
            this.setCordY(y);
        }
    }
    public void forceSetCords(double x, double y) {
            this.forceSetCordX(x);
            this.forceSetCordY(y);
    }
    
    /**
     * seting node coolor
     * @param R red int
     * @param G green int
     * @param B blue int
     */
    public void setColor(int R, int G, int B)
    {
        this.color_r = (byte)(R - 128);
        this.color_g = (byte)(G - 128);
        this.color_b = (byte)(B - 128);
    }
    
    //in the blue channel we will temporary encode the index of color of clone group
    //it cannot be to big becouse it would influence the heatmap color but this will be enough
    //for painting vertical borders bewtween cells with the same amount of driver mutations
    //but from different clonal groups
    public void setHeatMapColorDrivers(
            GenomeSynchronizedCollection genomes, 
            CellCollection cells,
            int maxValue){
        if ( maxValue > 0 ){
            double percent = (double)100 * (double)getDrivers(genomes, cells) / (double)maxValue;
            setHeatMapColorByPercent(percent);
        }
    }
    public void setHeatMapColorDrivers(
            GenomeCollection genomes, 
            CellCollection cells,
            int maxValue){
        if ( maxValue > 0 ){
            double percent = (double)100 * (double)getDrivers(genomes, cells) / (double)maxValue;
            setHeatMapColorByPercent(percent);
        }
    }
    
    private void setHeatMapColorByPercent(double percent){
        if (100 < percent){
            percent = (double)99;
        }
        if (0 > percent){
            percent = (double)0;
        }
        if (percent < (double)50){
            setColor((int)(((double)255 * (percent / (double)50))), 255, 0);
        }
        else{
            setColor(255,(int)(((double)255 * (double)(((double)100 - percent) / (double)50))), 0);
        }        
    }
    
    public void setCloneMarkerColor(CellCollection cells, GenomeSynchronizedCollection genomes, ArrayList<Color> Colors){
        colorIndex = (byte)(getDriverCloneGroupID(cells, genomes) % (Colors.size()));
    }
    
    public int getDriverCloneGroupID(CellCollection cells, GenomeSynchronizedCollection genomes){
        Cell _originCell = cells.getByIndex(cellIndex);
        return _originCell.getGenome(genomes).getDriverCloneGroupID();
    }

    public int getDriverCloneGroupID(CellCollection cells, GenomeCollection genomes){
        Cell _originCell = cells.getByIndex(cellIndex);
        return _originCell.getGenome(genomes).getDriverCloneGroupID();
    }    
    
    public void setCloneMarkerColor(CellCollection cells, GenomeCollection genomes, ArrayList<Color> Colors){
        colorIndex = (byte)(getDriverCloneGroupID(cells, genomes) % (Colors.size()));
    }
    
    /**
     * fixing position after this node can't be moved
     */
    public void fixPosition()
    {
        this.canBeMove = false;
    }
    
    /**
     * adding children to node
     * @param node Node children
     */
    public void addChildren(Node node)
    {
        if(children1 == null) {
            children1 = node;
        } else {
            children2 = node;
        }
    }

    /**
     * checking if node has children
     * @return true/false
     */
    public boolean hasChild()
    {
        return (this.children1 != null) || (this.children2 != null);
    }
   
    /**
     * get node parent
     * @return node parent
     */
    public Node getNodeParent(CellCollection cells, NodeCollection nodes)
    {
        Cell _originCell = cells.getByIndex(cellIndex);
        
//this is only for simulation node c-1_i0
        if(_originCell.getId() == 0) {
            return this;
        }
           
        return nodes.get(getpIDLong());
    }

    /**
     * get node children
     * @return ArrayList Node
     */
    public ArrayList<Node> getNodeChildren()
    {
        ArrayList <Node> children = new ArrayList<> ();
        
        if(this.children1 != null) {
            children.add(children1);
        }
        
        if(this.children2 != null) {
            children.add(children2);
        }
        
        return children;
    }
    
    /**
     * get node siblings
     * @return ArrayList Node
     */
    public ArrayList<Node> getNodeSiblings(CellCollection cells, NodeCollection nodes)
    {        
        ArrayList <Node> siblings = new ArrayList<> ();
        
        Node parent = this.getNodeParent(cells, nodes);
        
        Node p_c_1 = parent.children1;
        Node p_c_2 = parent.children2;
        Long thisUidLong = this.getuIDLong();
        
        if (p_c_1 != null){
            Long pc1UidLong = p_c_1.getuIDLong();
            if (pc1UidLong == null ? thisUidLong != null : !pc1UidLong.equals(thisUidLong))
                siblings.add(p_c_1);            
        }

        
        if (p_c_2 != null){
            Long pc2UidLong = p_c_2.getuIDLong();            
            if (pc2UidLong == null ? thisUidLong != null : !pc2UidLong.equals(thisUidLong))
                siblings.add(p_c_2);
        }

        return siblings;
    }

    /**
     * get Node Max Posterity Number
     * @return int 
     */
    public int getNodeMaxPosterityNumber()
    {
        ArrayList <Node> nodesToProcess        = new ArrayList<> ();
        ArrayList <Node> nodesCurentProcessing = new ArrayList<> ();
        
        int returnNumber = 0;
        
        //adding current node to process
        nodesToProcess.add(this);
        
        while (!nodesToProcess.isEmpty()) {
            nodesToProcess.stream().map((Node nodeToProcess) -> {
                //geting nodes children
                ArrayList <Node> children = new ArrayList<> ();
                children.addAll(nodeToProcess.getNodeChildren());
                return children;
            }).filter((children) -> (!children.isEmpty())).forEachOrdered((children) -> {
                nodesCurentProcessing.addAll(children);
            });
            
            if(nodesCurentProcessing.size() >= returnNumber) {
                returnNumber = nodesCurentProcessing.size();
            }
            
            nodesToProcess.clear();
            nodesToProcess.addAll(nodesCurentProcessing);
            nodesCurentProcessing.clear();
        }
        
        return returnNumber;
    }
    
    /**
     * get Nodes On Max Posterity
     * @return ArrayList Node
     */
    public ArrayList<Node> getNodesOnMaxPosterity(CellCollection cells)
    {
        ArrayList <Node> nodesToReturn         = new ArrayList<> ();
        ArrayList <Node> nodesToProcess        = new ArrayList<> ();
        ArrayList <Node> nodesCurentProcessing = new ArrayList<> ();
        
        int returnNumber = 0;
        
        nodesToProcess.add(this);
        
        while (!nodesToProcess.isEmpty()) {
            nodesToProcess.stream().map((nodeToProcess) -> {
                ArrayList <Node> children = new ArrayList<> ();
                children.addAll(nodeToProcess.getNodeChildren());
                return children;
            }).filter((children) -> (!children.isEmpty())).forEachOrdered((children) -> {
                nodesCurentProcessing.addAll(children);
            });
            
            if(nodesCurentProcessing.size() >= returnNumber) {
                returnNumber  = nodesCurentProcessing.size();
                nodesToReturn.clear();
                nodesToReturn.addAll(nodesCurentProcessing);
            }
            
            nodesToProcess.clear();
            nodesToProcess.addAll(nodesCurentProcessing);
            nodesCurentProcessing.clear();
        }
        
        return nodesToReturn; 
    }
    
    /**
     * get All Nodes To End Start From This Node
     * @return ArrayList Node
     */
    public ArrayList<Node> returnAllNodesToEndStartFromThisNode()
    {
        ArrayList <Node> nodesToReturn         = new ArrayList<> ();
        ArrayList <Node> nodesToProcess        = new ArrayList<> ();
        ArrayList <Node> nodesCurentProcessing = new ArrayList<> ();
        
        nodesToProcess.add(this);
        nodesToReturn.add(this);
        
        while (!nodesToProcess.isEmpty()) {            
            nodesToProcess.stream().map((nodeToProcess) -> nodeToProcess.getNodeChildren()).forEachOrdered((children) -> {
                nodesCurentProcessing.addAll(children);
            });
            
            nodesToReturn.addAll(nodesCurentProcessing);
            
            nodesToProcess.clear();
            nodesToProcess.addAll(nodesCurentProcessing);
            
            nodesCurentProcessing.clear();
        }
        
        return nodesToReturn;
    }
    
    /**
     * get Prev Node
     * @param nodesOnXLvl ArrayList Node
     * @return Node
     */
    public Node getPrevNode(ArrayList<Node> nodesOnXLvl)
    {
        Node currentNode = this;
        
        int indx = nodesOnXLvl.indexOf(currentNode); 
        
        if(indx != 0) {
            return nodesOnXLvl.get(indx - 1);
        }
        
        return null;
    }
    
    /**
     * get Next Node
     * @param nodesOnXLvl ArrayList Node
     * @return Node
     */
    public Node getNextNode(ArrayList<Node> nodesOnXLvl)
    {
        Node currentNode = this;
        
        int index_l = nodesOnXLvl.indexOf(currentNode);
        
        if(index_l != nodesOnXLvl.size()-1) {
            return nodesOnXLvl.get(index_l + 1);
        }
        
        return null;
    }
    
    /**
     * can Be Move
     * @param nodesOnXLvl ArrayList Node
     * @param newX float
     * @return true/falses
     */
    public boolean canBeMove(ArrayList<Node> nodesOnXLvl, float newX)
    {
        return (canBeMoveLeft(nodesOnXLvl, newX) && canBeMoveRight(nodesOnXLvl, newX));
    }
    
    /**
     * can node be move left
     * @param nodesOnXLvl ArrayList Node
     * @param newX float
     * @return true/falses
     */
    public boolean canBeMoveLeft(ArrayList<Node> nodesOnXLvl, double newX)
    {
        Node prevNode = this.getPrevNode(nodesOnXLvl);
        
        double prevNodeX = 0;
        
        boolean canBeMoveLeft  = false;
        
        if(prevNode != null) {
            prevNodeX = prevNode.cord_x;
        }

        //check left
        if((prevNodeX == 0) || ((newX - 5001) > prevNodeX)) {
            canBeMoveLeft = true;
        }
        
        return canBeMoveLeft;
    }
    
    /**
     * can node be move right
     * @param nodesOnXLvl ArrayList Node 
     * @param newX float
     * @return true/falses
     */
    public boolean canBeMoveRight(ArrayList<Node> nodesOnXLvl, double newX)
    {
        Node nextNode = this.getNextNode(nodesOnXLvl);

        double nextNodeX = 0;
        
        boolean canBeMoveRight = false;

        if(nextNode != null) {
            nextNodeX = nextNode.cord_x;
        }

        //check right
        if((nextNodeX == 0) || ((newX + 5001) < nextNodeX)) {
            canBeMoveRight = true;
        }
        
        return canBeMoveRight;
    }
    
    /**
     * get Level
     * @return int
     */
    public int getLevel()
    {
        Cell _originCell = cells.getByIndex(cellIndex);
        return _originCell.getModelCycle() + 1;
    }
    public int getLevel(CellCollection inCells)
    {
        Cell _originCell = inCells.getByIndex(cellIndex);
        return _originCell.getModelCycle() + 1;
    }    
    /**
     * get Parrent Cell ID
     * @return int
     */
    public int getParrentCellID(CellCollection cells)
    {
        Cell _originCell = cells.getByIndex(cellIndex);
        return _originCell.getParentCellID();
    }
    
    /**
     * get Cell ID
     * @return int
     */
    public int getCellID(CellCollection cells)
    {
        Cell _originCell = cells.getByIndex(cellIndex);
        return _originCell.getId();
    }
    
    /**
     * get has Child
     * @return int
     */
    public int gethasChild()
    {
        if(this.hasChild())
            return 1;
        
        return 0;
    }

    public int getDrivers(GenomeSynchronizedCollection genomes, CellCollection cells) {
        Cell _originCell = cells.getByIndex(cellIndex);
        if (_originCell.getGenome(genomes) != null){
            return (int) _originCell.getGenome(genomes).getDriverMutationCount();
        } else {
            return -1;
        }
    }

    public int getPassangers(GenomeSynchronizedCollection genomes, CellCollection cells) {
        Cell _originCell = cells.getByIndex(cellIndex);
        if (_originCell.getGenome(genomes) != null){
            return (int) _originCell.getGenome(genomes).getPassengerMutationCount();
        } else {
            return -1;
        }
    }

    public int getDrivers(GenomeCollection genomes, CellCollection cells) {
        Cell _originCell = cells.getByIndex(cellIndex);
        if (_originCell.getGenome(genomes) != null){
            return (int) _originCell.getGenome(genomes).getDriverMutationCount();
        } else {
            return -1;
        }
    }

    public int getPassangers(GenomeCollection genomes, CellCollection cells) {
        Cell _originCell = cells.getByIndex(cellIndex);
        if (_originCell.getGenome(genomes) != null){
            return (int) _originCell.getGenome(genomes).getPassengerMutationCount();
        } else {
            return -1;
        }
    }

    
    /**
     * compare two nodes by x coordinate
     * @param t node2
     * @return int 
     */
    @Override
    public int compareTo(Node t)
    {
        return new Double(cord_x).compareTo( t.cord_x);
    }

    public double getCord_x() {
        return cord_x;
    }
    
}
