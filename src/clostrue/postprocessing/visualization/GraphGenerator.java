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
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.scene.control.ProgressBar;
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.Genome;
import clostrue.biology.genome.GenomePart;
import java.awt.Color;
import java.util.Arrays;
import clostrue.Simulation;
import clostrue.collections.CellCollection;
import clostrue.collections.CellIndexHolder;
import clostrue.collections.GenomeCollection;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.file.Name;
import clostrue.hardcodes.file.NamePart;
import clostrue.hardcodes.LogText;
import clostrue.hardcodes.Message;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Extension;
import clostrue.model.SimModel;
import clostrue.toolbox.StaticConsoleLogger;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Generator class
 * this class generates the graphical representation of the sumulation run
 * the produced output is bitmap file
 * 
 * @author Krzysztof Szymiczek
 */
public class GraphGenerator {

    private String outputFilePathGephi;  
    private String outputFilePathPng;  
    public  static ProgressBar graphProgressBar;    
    private final String workDir;
    int maxDriverMutationsPerOneCell;
    double maxDriverDivByPassengerPerOneCell;
    int maxLevel = 0;
    int currentIteration;
    protected int canvasXSize = 0;
    protected int canvasYSize = 0;
    Simulation simulation;
    SimModel simModel;
    HashMap<GenomePart, CellIndexHolder> clones = null;
    GenomeCollection genomes = null;
    CellCollection cells = null;
    
    /**
     * hashtable of nodes by 
     */
    protected NodeCollection nodes;

    /**
     * map of Node lists group by levels
     */
    protected Map<Integer, List<Node>>  nodesByLevel;
    
    /**
     * space between nodes in x 
     */
    protected static float progress = 0;
    protected static float oldProgress = 0;

    /**
     * space between nodes in x 
     */
    protected static long nodeSpace = 500;

    /**
     * max graph width
     */
    protected static long maxWidth = 1000000;

    /**
     * space between nodes in y
     */
    public static int nodeYSpace = -5000;
    public static int nodeXSpace = +5000;
      
    /**
     * Java logger
     */
    private static final Logger logger = Logger.getLogger(GraphGenerator.class.getName());
    public  static final ArrayList <Color> colorsTable = new ArrayList<> (

       Arrays.asList(
               new Color(128,0,0),      //maroon
               new Color(255,0,0),      //red
               new Color(255,165,0),    //orange
               new Color(255,215,0),    //gold
               new Color(128,128,0),    //olive
               new Color(124,252,0),    //lawn
               new Color(0,128,0),      //green
               new Color(0,250,154),    //medium spring green
               new Color(32,178,170),   //light sea green
               new Color(0,255,255),    //aqua
               new Color(100,158,160),  //cadet blue
               new Color(100,149,237),  //corn flower blue
               new Color(0,0,139),      //dark blue
               new Color(0,0,255),      //blue
               new Color(138,43,226),   //blue violet
               new Color(75,0,130),     //indigo
               new Color(255,20,147),   //deep pink
               new Color(139,69,19),    //saddle brown
               new Color(112,128,144)   //slatte gray            
       )
    );
       
    public static float getProgress() {
        return progress;
    }

    public String getOutputFilePathGephi() {
        return outputFilePathGephi;
    }

    public String getOutputFilePathPng() {
        return outputFilePathPng;
    }

    
    /**
     * constructor
     * @param simulation calling simulation
     * @param inGraphProgressBar progress bar for creating graph
     * @throws java.io.IOException IOException
     */
    public GraphGenerator(
            Simulation simulation,
            ProgressBar inGraphProgressBar 
            ) throws IOException{
        
        clones = simulation.getStatistics().getInternalClones();
        genomes = new GenomeCollection(simulation.getGenomes());
        cells = simulation.getStatistics().getCellCollection();
        
        this.currentIteration = simulation.getIteration();
        this.simulation = simulation;
        this.nodes = new NodeCollection(genomes,cells);
        this.simModel = new SimModel(simulation.getIteration(), simulation.getSimModel());
        
        maxDriverMutationsPerOneCell            = 0;
        this.maxDriverDivByPassengerPerOneCell  = 0;
        
        GraphGenerator.graphProgressBar     = inGraphProgressBar;

//        if(simulation.getSettings().getBooleanValue(Param.cbLogGraphToFile)) {
//            logger.setUseParentHandlers(true);
//        } else {
            logger.setUseParentHandlers(false);
//        }
               
        workDir = simModel.getFilePaths().getWorkDirGraphics();
        String path = workDir;
       
        // Creating path to file
        path += java.io.File.separator + Name.processingLog + Extension.dotLog;
        
//        if(simulation.getSettings().getBooleanValue(Param.cbLogGraphToConsole)) {
//            FileHandler fh;   
//            fh = new FileHandler(path);   
//            logger.addHandler(fh);
//            SimpleFormatter formatter = new SimpleFormatter();  
//            fh.setFormatter(formatter);
//        }
    }
        
    //logger
    private void log(String log)
    {
//        if(simulation.getSettings().getBooleanValue(Param.cbLogGraphToConsole)) {
//            GraphGenerator.logger.info(log);
//        }
    }

    public static void setProgress(float progress) {
        if (oldProgress == 0){
            oldProgress = (float)1.0;           
            return;
        }
        if ((progress/oldProgress) > 1){
            GraphGenerator.progress = progress;
            GraphGenerator.graphProgressBar.setProgress(GraphGenerator.getProgress()/(float)100.0);    
            oldProgress = progress;
        }
    }
    
    /**
     *Start drawing process
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public void run() throws InterruptedException, IOException
    {
        GraphGenerator.setProgress(0);
        this.log(LogText.running);
        
        this.createNodes();
        
        GraphGenerator.setProgress(GraphGenerator.getProgress() + 5);      
        this.linkNodes();
        GraphGenerator.setProgress(GraphGenerator.getProgress() + 10);

        this.prepareNodesByLevel();
        GraphGenerator.setProgress(GraphGenerator.getProgress() + 15);

        this.LayOutTheNodesOnPlane();
        GraphGenerator.setProgress(GraphGenerator.getProgress() + 5);

        modeCenterCorrection(simulation.getSettings().getBooleanValue(Param.ggRectangeScale));

        GraphGenerator.setProgress(GraphGenerator.getProgress() + 15);
             
        this.removeSimulationNode();

        GraphGenerator.setProgress(GraphGenerator.getProgress() + 5);

        this.log(LogText.numberOfNodes + this.nodes.size());
  
        nodes.setCloneMarkerColor(cells);
        nodes.setHeatMapColorDrivers(cells,maxDriverMutationsPerOneCell);      
        this.saveToPngFile(NamePart.cloneMarker, false, true);
        this.saveToPngFile(NamePart.cloneIlandsHeatmap, true, false);

        setProgress((float)100.0);
        
        StaticConsoleLogger.logActivity(currentIteration, Activity.garbageCollection, Activity.started);
        System.gc();
        StaticConsoleLogger.logActivity(currentIteration, Activity.garbageCollection, Activity.finished);        
        
    }
    
    private int getWidestLevelWidth(){
        int widestLevelSize = 0;
        boolean error = false;
        for(int level = 0; level < this.nodesByLevel.size(); level++) {
            if (this.nodesByLevel.get(level) == null){
                StaticConsoleLogger.log(Message.nodeByLeveNull + String.valueOf(level));
                error = true;
            } else {
                if(widestLevelSize < this.nodesByLevel.get(level).size()) {
                    widestLevelSize = this.nodesByLevel.get(level).size();
                }
            }
        }

        if (error){
            for(int level = 0; level < this.nodesByLevel.size(); level++) {
                String size;
                if (this.nodesByLevel.get(level) == null){
                    size = "Null";
                } else {
                    size = String.valueOf(this.nodesByLevel.get(level).size());
                }
                System.out.println(LogText.level + String.valueOf(level) + LogText.size + size );
            }            
        }
        
        return widestLevelSize;
    }
    
    
    private void modeCorrection(Integer maxPosition, boolean center, boolean justify, boolean rectangeCorrection){
       
        int maxPosX;
        int locLevels = this.nodesByLevel.size();
        for(int level = 0 ; level < locLevels; level++) {
            List <Node> nodesOnLevel = nodesByLevel.get(level);           
            if(this.simModel.getTechParams().getSimTasksCount() == 1){
                Collections.sort(nodesOnLevel);                
            }
            int curMaxPosX = nodesOnLevel.size();
            if ( maxPosition != 0) {
                maxPosX = maxPosition;
            } else {
                maxPosX = curMaxPosX;
            }
            int xShift = ( maxPosX - curMaxPosX ) / 2;
            if (!center){
                xShift = 0;
            }

            double xMultiplicator = (double) 1.0 / (double)curMaxPosX * (double)maxPosX;
            if (!justify){
                xMultiplicator = 1;
            }
            double yMultiplicator;
            if (rectangeCorrection){
                if (maxPosition > ( locLevels + 1 ) ){
                    yMultiplicator = (double) maxPosition / (double) (locLevels + 1);
                } else {
                    yMultiplicator = 1;
                }
            } else {
                yMultiplicator = 1;
            }

            int xPosition = 1;            
            for(int nodeIndex = 0; nodeIndex < nodesOnLevel.size(); nodeIndex++) {
                nodesOnLevel.get(nodeIndex).forceSetCords(
                    (((double) xPosition + (double) xShift) * (double) nodeXSpace * (double)xMultiplicator),
                     ((double) level )                      * (double) nodeYSpace * (double)yMultiplicator);
                nodesOnLevel.get(nodeIndex).setOrder(xPosition++ + xShift);
            }
        }
        canvasYSize = 10 + locLevels;
        canvasXSize = 10 + maxPosition;
    }
    
    private void modeCenterCorrection(boolean rectangeCorrection){        
        int widestLevelWidth = getWidestLevelWidth();
        modeCorrection(widestLevelWidth,true, false, rectangeCorrection);
    }
    
    //reading cells from memory and transform into nodes
    private void createNodes()
    {      
        float progressOnBegin = GraphGenerator.getProgress();
        float progressBy = (float) 25.0;

        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.createNodes, Activity.started);
        
        //create fake cell for the simulation node
        Cell fakeSimulationCell = new Cell(genomes, true);
        int fakeCellIndex = cells.addAndReturnIndex(fakeSimulationCell);
        
        Node.setCells(cells);
        
        //adding simulation node
        Node nodeZero = new Node(fakeSimulationCell,fakeCellIndex,cells);
        nodes.put(nodeZero);
       
        if (clones.entrySet().isEmpty()){
            StaticConsoleLogger.log(Message.emptyClonesEntrySet);
        }
        
        int clonesCount = clones.entrySet().size();
        int clonesProcessed = 0;
        int percProcessed = 0;
        int prevPercProcessed = 0;
        boolean logMemoryCells = false;
        
        for( Map.Entry<GenomePart, CellIndexHolder> clone : clones.entrySet() ){            
            int cloneSize = clone.getValue().size();
            for (int i = 0; i < cloneSize; i++){
                int cellIndex = clone.getValue().get(i);
                Cell cell = cells.getByIndex(cellIndex);
                if (cell.isAlive()){
                    if (logMemoryCells){
                        System.out.println(LogText.importingCell + cell);
                    }
                    Node node = new Node(cell,cellIndex,cells);

                    maxDriverMutationsPerOneCell = Math.max(maxDriverMutationsPerOneCell, node.getDrivers(genomes, cells));

                    double driverPassengerRatio 
                            = Genome.staticCalculateDriverPassengerRatio(
                                    (int)node.getDrivers(genomes,cells), 
                                    (int)node.getPassangers(genomes,cells));
                    maxDriverDivByPassengerPerOneCell = Math.max(maxDriverDivByPassengerPerOneCell,driverPassengerRatio);

                    nodes.put(node);    
                }                
            }
            clonesProcessed++;
            percProcessed = (int)((double)100 * (double)clonesProcessed / (double)clonesCount);
            if (percProcessed != prevPercProcessed){
                prevPercProcessed = percProcessed;
                StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.nodesCreated, String.valueOf(percProcessed));        
            }
        }

        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.createNodes, Activity.finished);        
        
        if(GraphGenerator.getProgress() != (progressOnBegin + progressBy)) {
          GraphGenerator.setProgress(progressOnBegin + progressBy);
        }
    }
    
    //adding children to parrent
    private void linkNodes() 
    {
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.linkNodes, Activity.started);
        nodes.linkNodes();
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.linkNodes, Activity.finished);
    }
    
    //fills hashTables
    private void prepareNodesByLevel()
    {
        this.log("Filling nodesByLevel Map");
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.prepareNodesByLevel, Activity.started);

        this.nodesByLevel = nodes.getNodesByLevel(cells);
        
        if(this.simModel.getTechParams().getSimTasksCount() > 1) {
            Comparator<? super Node> c = new Comparator<Node>() {
                    @Override
                    public int compare(Node o1, Node o2) {
                        return new Integer(o1.getDriverCloneGroupID(cells, genomes)).compareTo( o2.getDriverCloneGroupID(cells, genomes));
                    }

                };

            //sorting nodes on levels
            for( Map.Entry<Integer, List<Node>> level : this.nodesByLevel.entrySet()) {
                level.getValue().sort(c);
            }
        }
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.prepareNodesByLevel, Activity.finished);
    }
    
    /**
     * counting max numbeer of nodes for each node from first level
     * @param nodesForProcess - node hook
     * @return                - sum of max number of nodes from each level
     */
    private int getToDiv(List<Node> nodesForProcess){
        int toDiv = 0;

        if (nodesForProcess == null){
                StaticConsoleLogger.log(Message.getToDivNodesForProcessIsNull);            
        } else {
            for (Node n : nodesForProcess){
                if (n == null){
                    StaticConsoleLogger.log(Message.getToDivNodeIsNull); 
                } else {
                    toDiv += n.getNodeMaxPosterityNumber();                
                }
            }
        }

        return toDiv;
    }
    
    //finding widest levels in graph and seting it coordinates, LayOutTheNodesOnPlane whole tree
    private void LayOutTheNodesOnPlane()
    {
        this.log("start laying out the nodes");
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.layoutNodesOnPlane, Activity.started);
        //centering simulation node
        Node nodeSimulation = this.nodes.get(-1,0);
        
        nodeSimulation.setCords(GraphGenerator.maxWidth / 2, nodeYSpace * (-1));
        
        //geting nodes from first level
        List <Node> nodesForProcess = nodesByLevel.get(1);
        
        //counting max numbeer of nodes for each node from first level
        int toDiv = getToDiv(nodesForProcess);
               
        this.log("max nodes in row from all levels = " + toDiv);
        
        if (toDiv == 0)
            return;
        
        //seting new space between nodes
        GraphGenerator.nodeSpace = GraphGenerator.maxWidth / toDiv;            

        float startFrom = 0;
        float endOn;
        
        //looping through all nodes from first level
        for(Node nodeToProcess : nodesForProcess) {
            this.log("drowing for node  " + nodeToProcess + " from first level");
            
            //geting max number of posterity for currently processing node
            int x = nodeToProcess.getNodeMaxPosterityNumber();
            
            //calculate end cooridnate
            endOn = startFrom + ((maxWidth / toDiv) * x);
            
            //geting nodes from level with max posterity number
            ArrayList <Node> maxLvlNodes = nodeToProcess.getNodesOnMaxPosterity(cells);
            
            int count = maxLvlNodes.size();
            
            //if node haven't children skip
            if(count == 0) {
                continue;
            }
            
            //calculate space beetwen nodes in max posterity
            double xForNode = ((endOn - startFrom) / count);
            
            int nodeInRowNumber = 0;
            
            //setting position for nodes in max posterity
            for(Node maxNode : maxLvlNodes) {
                maxNode.setCords((nodeInRowNumber * xForNode) + startFrom, maxNode.getLevel() * (nodeYSpace));
                maxNode.fixPosition();
               
                nodeInRowNumber++;
                
                this.log("widest set " + maxNode + " " + maxNode.cord_x + " " + maxNode.cord_y);
            }
            
            //get root from c0_ix to end
            ArrayList <Node> nodesInRoot = nodeToProcess.returnAllNodesToEndStartFromThisNode();
            
            Map<Integer, List<Node>> nodesInRootByLevel;
            
            //grouping nodes form nodesInRoot by level
            nodesInRootByLevel =
                nodesInRoot
                .stream()
                .collect(Collectors.groupingBy(Node::getLevel));
            
            Comparator<? super Node> c = new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return new Integer(o1.getDriverCloneGroupID(cells, genomes)).compareTo( o2.getDriverCloneGroupID(cells, genomes));
                }
                
            };
        
            //sorting nodes on levels
            if(this.simModel.getTechParams().getSimTasksCount() > 1) {
                for( Map.Entry<Integer, List<Node>> level : nodesInRootByLevel.entrySet()) {
                    level.getValue().sort(c);
                }
            }
            
            //looping by levels up start form max posterity levle end on first
            for(int i = maxLvlNodes.get(0).getLevel(); i >= 0; i--) {
                int j = i;
                
                //geting nodes from nodesInRootByLevel and current level
                List <Node> nodesInRootOnILvl = nodesInRootByLevel.get(j);
                
                int devByNumberOfNodes = 1;
                
                //if nodesInRootOnILvl is not empty counting number of nodes
                if(nodesInRootOnILvl != null) {
                    if(this.simModel.getTechParams().getSimTasksCount() > 1) {
                        nodesInRootOnILvl.sort(c);
                    }
                    
                    if(!nodesInRootOnILvl.isEmpty()) {
                        devByNumberOfNodes = nodesInRootOnILvl.size();
                    }
                }
                
                //countig space between nodes for current level
                double gap = (endOn - startFrom) / devByNumberOfNodes;
                
                int counterInRow = 0;
                
                //seting position for nodes from current level
                if(nodesInRootOnILvl != null) {
                    for(Node nodeInRootOnILvl : nodesInRootOnILvl) {
                        nodeInRootOnILvl.setCords(startFrom + (gap * counterInRow), nodeInRootOnILvl.getLevel() * (nodeYSpace));
                        counterInRow++;
                    }
                }
            }

            //seting position of next nodes
            this.loopDown(maxLvlNodes, startFrom, endOn, maxLvlNodes.get(maxLvlNodes.size() -1).getLevel());

            startFrom = endOn;
        }
        
        //centering parrents
        nodesForProcess.forEach((nodeToProcess) -> {
            ArrayList <Node> maxLvlNodes = nodeToProcess.getNodesOnMaxPosterity(cells);
            ArrayList <Node> nodesInRoot = nodeToProcess.returnAllNodesToEndStartFromThisNode();
            //centering parents up
            if(this.simModel.getTechParams().getSimTasksCount() == 1) {
                this.centerUp(maxLvlNodes, nodesInRoot, 1);
            }
        }); 
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.layoutNodesOnPlane, Activity.finished);

    }
    
    //finding next widest level in root and seting coordinates
    private void loopDown(ArrayList <Node> startFromNodes, double boxStart, double boxEnd, int stopOnLvl)
    {
        ArrayList<Node> nodesForProcess = startFromNodes;
        
        int toDiv = 0;
        
        //counting max nodes number for ech node from nodesForProcess
        toDiv = nodesForProcess.stream().map((nodeToProcess) -> nodeToProcess.getNodeMaxPosterityNumber()).map((x) -> x).reduce(toDiv, Integer::sum);
        
        //if toDiv exit
        if(toDiv == 0) {
            this.log("DEBUGGING to div is 0");
            return;
        }
        
        //calculate distance between nodes
        double localNodeSpace = (boxEnd - boxStart) / toDiv;
        
        //boxStart
        double spaceStartToThisSpecRoot = boxStart;

        int nodeInRowNumber = 0;
        
        //seting nodes from max width
        for(Node nodeToProcess : nodesForProcess) {            
            ArrayList <Node> maxLvlNodes = nodeToProcess.getNodesOnMaxPosterity(cells);
            
            //counting nodes
            int count = maxLvlNodes.size();
            
            if(count != 0) {
                //set nodes form widest level
                for(Node maxNode : maxLvlNodes) {
                    maxNode.setCords((nodeInRowNumber * localNodeSpace) + spaceStartToThisSpecRoot, maxNode.getLevel() * (nodeYSpace));
                    maxNode.fixPosition();

                    nodeInRowNumber++;

                    this.log("Node on the widest level " + maxNode + " " + maxNode.cord_x + " " + maxNode.cord_y);
                }
                
                //getting all nodes to end start from curent processing nodes
                ArrayList <Node> nodesInRoot = nodeToProcess.returnAllNodesToEndStartFromThisNode();
                
                Map<Integer, List<Node>> nodesInRootByLevel;
                
                //grouping nodesInRoot by level
                nodesInRootByLevel =
                    nodesInRoot
                    .stream()
                    .collect(Collectors.groupingBy(Node::getLevel));
                                
                //seting nodes in eq distance from end of nodesInRootByLevel to current processing nodes
                for(int i = maxLvlNodes.get(0).getLevel(); i >= stopOnLvl; i--) {
                    int j = i;

                    //geting ondes on current processing level
                    List <Node> nodesInRootOnILvl = nodesInRootByLevel.get(j);

                    int devByNumberOfNodes = 1;

                    //copunting number of nodes for process
                    if(!nodesInRootOnILvl.isEmpty()) {
                        devByNumberOfNodes = nodesInRootOnILvl.size();
                    }

                    //calcualting new space between nodes
                    double gap = (maxLvlNodes.get(maxLvlNodes.size() -1).cord_x - maxLvlNodes.get(0).cord_x) / devByNumberOfNodes;

                    int counterInRow = 0;

                    //seting nodes in new position
                    for(Node nodeInRootOnILvl : nodesInRootOnILvl) {
                        nodeInRootOnILvl.setCords(maxLvlNodes.get(0).cord_x + ((double)gap * counterInRow), nodeInRootOnILvl.getLevel() * (nodeYSpace));
                        counterInRow++;
                    }
                }
                
                //centering parents if necessary
                if(this.simModel.getTechParams().getSimTasksCount() == 1) {
                    this.centerUp(maxLvlNodes, nodesInRoot, stopOnLvl);
                }
            }
            
            //cal this function with new parameters until array is not empty
            if(maxLvlNodes.size() > 0)
                this.loopDown(maxLvlNodes, (int)maxLvlNodes.get(0).cord_x, (int)maxLvlNodes.get(maxLvlNodes.size() -1).cord_x, maxLvlNodes.get(maxLvlNodes.size() - 1).getLevel());
        }
    }
    
    //center nodes from down to up by calculating average from children
    private void centerUp(ArrayList <Node> startFromNodes, ArrayList <Node> root, int end)
    {
        Map<Integer, List<Node>> rootByLevel;

        //grouping nodes from root by level
        rootByLevel =
            root
            .stream()
            .collect(Collectors.groupingBy(Node::getLevel));
        
        if (startFromNodes.isEmpty()){
            return;
        }
        
        ArrayList <Node> nodesForProcerss = new ArrayList<> (); 
        ArrayList <Node> curentProcessing = new ArrayList<> ();
        
        nodesForProcerss.addAll(startFromNodes);
        
        //looping through all nodes from root start from first node and continue by geting they parnts
        while (nodesForProcerss.get(0).getLevel() > end) {
            //geting pareents for first node
//            StaticConsoleLogger.log("A1 " + (nodesForProcerss.get(0).getLevel() - 1));
            List <Node> parrents = rootByLevel.get(nodesForProcerss.get(0).getLevel() - 1);

            //get nodes from curent level from root
            List <Node> get = rootByLevel.get(nodesForProcerss.get(0).getLevel());
            
            Map<Integer, List<Node>> getByHasChild;
            
            //grouping nodes by if they have children
            getByHasChild =
                get
                .stream()
                .collect(Collectors.groupingBy(Node::gethasChild));
            
            //adding nodes to append only if they have children
            List <Node> nodesToAppend = getByHasChild.get(true);
            
            //appending if nodesToAppend is not empty
            if(nodesToAppend != null)
                nodesForProcerss.addAll(nodesToAppend);
            
            ArrayList<Node> parentsLvl = new ArrayList<> ();
            
            parentsLvl.addAll(parrents);
            
            //loping through nodes for process
            nodesForProcerss.stream().map((Node nodeForProcess) -> {
                GraphGenerator.this.log("processing node " + nodeForProcess + " in centerUp");
                return nodeForProcess;
            }).forEachOrdered((Node nodeForProcess) -> {
                //geting node siblings
                ArrayList <Node> siblings = nodeForProcess.getNodeSiblings(cells, nodes);
                int siblingsNumber = 0;
                //counting siblings
                if(siblings != null) {
                    siblingsNumber = siblings.stream().map((_item) -> 1).reduce(siblingsNumber, Integer::sum);
                }   
                if (siblingsNumber == 0) {
                    Node parent = nodeForProcess.getNodeParent(cells, nodes);
                    double oldX = parent.cord_x;
                    double newX = nodeForProcess.cord_x;
                    //move left
                    if (oldX - newX > 0) {
                        //checking if parent can be moved
                        if (parent.canBeMoveLeft(parentsLvl, nodeForProcess.cord_x)) {
                            parent.setCords(nodeForProcess.cord_x, parent.getLevel() * (nodeYSpace));
                            parent.fixPosition();
                            GraphGenerator.this.log("centering node" + parent);
                        }
                    }
                    //move right
                    if (oldX - newX < 0) {
                        //checking if parent can be moved
                        if (parent.canBeMoveRight(parentsLvl, nodeForProcess.cord_x)) {
                            parent.setCords(nodeForProcess.cord_x, parent.getLevel() * (nodeYSpace));
                            parent.fixPosition();
                            GraphGenerator.this.log("centering node" + parent);
                        }
                    }
                    //if curentProcessing not contains parent append it
                    if(!curentProcessing.contains(parent)) {
                        curentProcessing.add(parent);
                    }
                }
                if (siblingsNumber == 1) {
                    Node parent = nodeForProcess.getNodeParent(cells, nodes);
                    Node sibling = siblings.get(0);
                    double oldX = parent.cord_x;
                    //calculate new x (average from children x)
                    double cordX = (((double)nodeForProcess.cord_x + (double)sibling.cord_x) / (double)2.0);
                    //move left
                    if (oldX - cordX > 0) {
                        //checking if parent can be moved
                        if (parent.canBeMoveLeft(parentsLvl, cordX)) {
                            parent.setCords(cordX, parent.getLevel() * (nodeYSpace));
                            parent.fixPosition();
                            log("centering node" + parent);
                        }
                    }
                    //move right
                    if (oldX - cordX < 0) {
                        //checking if parent can be moved
                        if (parent.canBeMoveRight(parentsLvl, cordX)) {
                            parent.setCords(cordX, parent.getLevel() * (nodeYSpace));
                            parent.fixPosition();
                            log("centering node" + parent);
                        } 
                    }
                    if (!curentProcessing.contains(parent)) {
                        curentProcessing.add(parent);
                    }
                }
            });
            
            //seting next nodes for process
            nodesForProcerss.clear();
            nodesForProcerss.addAll(parrents);
            curentProcessing.clear();
        }
    }
    
    /**
     * set color nodes
     * @param r red
     * @param g green
     * @param b blue
     */
    
    //removing node c-1_i0 and related edges
    private void removeSimulationNode()
    {
        Node nodeSimulation = this.nodes.get(-1,0);       
        this.nodes.remove(nodeSimulation);
    }
    
    private void saveToPngFile(String fileNamePart, boolean drawBorders, boolean useColorMap) throws IOException
    {
        this.log("saving to png file");
        
        outputFilePathPng = GraphToFile.saveGraphToPngFile(cells, genomes, canvasXSize, canvasYSize, workDir, nodes.values(), simulation.getSettings(), fileNamePart, drawBorders, useColorMap);
        
        //cell indexes
//        GraphToFile.saveGraphToPngFileIdexes(cells, genomes, canvasXSize, canvasYSize, workDir, nodes.values(), simulation.getSettings(), fileNamePart, drawBorders, useColorMap);
        
        
        this.log(outputFilePathPng);
    }
}
