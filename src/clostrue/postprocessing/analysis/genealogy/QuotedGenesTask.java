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
package clostrue.postprocessing.analysis.genealogy;
 
import clostrue.collections.CellIndexHolder;
import clostrue.biology.cell.Cell;
import clostrue.biology.genome.GenomePart;
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeCollection;
import clostrue.enumerations.QuotedGenesTaskWorkToDo;
import clostrue.hardcodes.file.Artifact;
import clostrue.model.SimModel;
import clostrue.postprocessing.analysis.Analytics;
import clostrue.toolbox.StaticConsoleLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import javax.swing.SwingWorker;

/**
 * Implements one single Mutation Type Analysis Task.
 * 
 * Handles one task of mutation type analysis which
 * Can be parallelized as the work is on different
 * Already prepared HashMaps
 * 
 * @author Krzysztof Szymiczek
 */ 
public class QuotedGenesTask extends SwingWorker<Void,Void> {

    private final int                         _taskId;
    private final QuotedGenesTaskWorkToDo     _workToDo;              //the ID of TaskToDo
    private final int                         _iteration;
    private final AtomicInteger               _notProcessedCounter;
    private final Analytics                   _anal;
    private final ArrayList<Integer>          _cycles;
    private final AtomicIntegerArray          _popSize;
    private final SimModel                    _model;
    private final int                         _quotasCount;
    private final CellCollection              _cellCollection;
    private final GenomeCollection            _genomes;
    private final HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> _equalGenomePart;
    private final double[]                    _quotaTreshold;
    private final List<String[]>              _mutationsListHavingMinQuotas;        
    private final List<Integer[]>             _mutationsCountHavingMinQuotas;
    
    /**
     * Default constructor
     * @param inTaskId
     * @param anal
     * @param inWorkToDo
     * @param cycles
     * @param inPopSize
     * @param tasksToFinish
     * @param inEqualGenomePart
     * @param inMutationsListHavingMinQuotas
     * @param inMutationsCountHavingMinQuotas
     */
    public QuotedGenesTask(int inTaskId,
            Analytics anal, 
            QuotedGenesTaskWorkToDo inWorkToDo, 
            AtomicInteger tasksToFinish,
            ArrayList<Integer> cycles,
            AtomicIntegerArray inPopSize,
            HashMap<Integer,HashMap<GenomePart, CellIndexHolder>> inEqualGenomePart,
            List<String[]> inMutationsListHavingMinQuotas,        
            List<Integer[]>   inMutationsCountHavingMinQuotas            
    ) {
        _taskId                             = inTaskId;
        _workToDo                           = inWorkToDo;              
        _notProcessedCounter                = tasksToFinish;
        _anal                               = anal;
        _iteration                          = anal.getMta().iteration;
        _cycles                             = cycles;
        _popSize                            = inPopSize;
        _quotasCount                        = anal.getQuotasCount();
        _cellCollection                     = anal.getCellCollection();
        _equalGenomePart                    = inEqualGenomePart;
        _genomes                            = anal.getGenomes();
        _quotaTreshold                      = anal.getQuotaTreshold();
        _model                              = anal.getSim().getSimModel();
        _mutationsListHavingMinQuotas       = inMutationsListHavingMinQuotas;
        _mutationsCountHavingMinQuotas      = inMutationsCountHavingMinQuotas;
        
        _notProcessedCounter.incrementAndGet();

    }
            
    /**
     * Starts the step to be done
     * 
     * @return nothing
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public Void executeToDo() throws IOException, InterruptedException, ExecutionException{         

        doInBackground();
        return null;        
    
    }
    
    /**
     * Starts the step to be done in background
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Override    
    public Void doInBackground()
        throws InterruptedException, ExecutionException, IOException {
        
        switch(_workToDo){
            case drivers:
                processDrivers();
                _notProcessedCounter.decrementAndGet();
                break;
            case passengers:
                processPassengers();
                _notProcessedCounter.decrementAndGet();
                break;
        }
        return null;
            
    }

    private void processDrivers(){
        int prevPercent = -1;
        for( int cId = 0; cId < _cycles.size(); cId++){
            Integer cycle = _cycles.get(cId);
            int percent = (int)((double)100 * (double)cId / (double)_cycles.size());
            if (percent != prevPercent){
                StaticConsoleLogger.log(_iteration, " - - - Quoted Drivers Task: " + _taskId + " finished: " + percent + " %");                
                prevPercent = percent;
            }
            
            double populationSizeInCycle = (double)_popSize.get(cycle);
            int[] ammountOfCellsHavingMutation = new int[_anal.getSim().getSimModel().getModParams().getMAM().getGenomeDriversPartSize()];   
         
            for (int i = 0; i < _quotasCount; i++)
                if ( cycle < _mutationsListHavingMinQuotas.size())                     
                    _mutationsListHavingMinQuotas.get(cycle)[i] = ".";                

            
            for(Map.Entry<GenomePart, CellIndexHolder> e : _equalGenomePart.get(cycle).entrySet()) {
                int cellGroupSize = e.getValue().size();
                Cell c = _cellCollection.getByIndex(e.getValue().getFirst()); //one cell is enough - all in the same clone will have exact same mutations
                ArrayList<Integer> mutations = c.getGenome(_genomes).getDrivers(_genomes).getDrivMutations(_genomes);                    
                for (Integer mutation : mutations){
                    ammountOfCellsHavingMutation[mutation] += cellGroupSize; 
                }
            }

            for (int i = 0; i < ammountOfCellsHavingMutation.length; i++){                    
                  
                  if (ammountOfCellsHavingMutation[i] > 0){
                    double quota = (double)ammountOfCellsHavingMutation[i] / populationSizeInCycle;
                    for (int quotaTresholdId = 0; quotaTresholdId < _quotasCount; quotaTresholdId++)
                        if (quota >= _quotaTreshold[quotaTresholdId]){
                            if (cycle < _mutationsCountHavingMinQuotas.size()){
                                _mutationsCountHavingMinQuotas.get(cycle)[quotaTresholdId]++;
                                int gene = _model.getModParams().getMAM().getRegionMapDrivers().getGeneBasedOnToss(i);
                                String geneName = _model.getModParams().getMAM().getDrivers()[gene].getGeneName();
                                if (_mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId].equals("."))
                                    _mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId] = "";
                                _mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId] = 
                                        _mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId]
                                        + geneName
                                        + Artifact.geneSeparator 
                                        + String.valueOf(i)
                                        + Artifact.outCSVMutationSeparator;
                            }
                        }                    
                }
            }
        }
    }
    
    private void processPassengers(){
        int prevPercent = -1;
        for( int cId = 0; cId < _cycles.size(); cId++){
            Integer cycle = _cycles.get(cId);
            int percent = (int)((double)100 * (double)cId / (double)_cycles.size());
            if (percent != prevPercent){
                StaticConsoleLogger.log(_iteration, " - - - Quoted Passengers Task: " + _taskId + " finished: " + percent + " %");                
                prevPercent = percent;
            }                        
                
            double populationSizeInCycle = (double)_popSize.get(cycle);
            int[] ammountOfCellsHavingMutation = new int[_anal.getSim().getSimModel().getModParams().getMAM().getGenomePassengersPartSize()];   
                       
            for (int i = 0; i < _quotasCount; i++)
                if (cycle < _mutationsListHavingMinQuotas.size())
                    _mutationsListHavingMinQuotas.get(cycle)[i] = ".";                

            
            for(Map.Entry<GenomePart, CellIndexHolder> e : _equalGenomePart.get(cycle).entrySet()) {
                int cellGroupSize = e.getValue().size();
                Cell c = _cellCollection.getByIndex(e.getValue().getFirst()); //one cell is enough - all in the same clone will have exact same mutations
                ArrayList<Integer> mutations = c.getGenome(_genomes).getPassengers(_genomes).getPassMutations(_genomes);                    
                for (Integer mutation : mutations){
                    ammountOfCellsHavingMutation[mutation] += cellGroupSize; 
                }
            }

            for (int i = 0; i < ammountOfCellsHavingMutation.length; i++){                    
                  
                  if (ammountOfCellsHavingMutation[i] > 0){
                    double quota = (double)ammountOfCellsHavingMutation[i] / populationSizeInCycle;
                    for (int quotaTresholdId = 0; quotaTresholdId < _quotasCount; quotaTresholdId++)
                        if (quota >= _quotaTreshold[quotaTresholdId]){
                            if (cycle < _mutationsCountHavingMinQuotas.size()){
                                _mutationsCountHavingMinQuotas.get(cycle)[quotaTresholdId]++;
// The list is to slow for passengers at the moment
//                            int gene = _model.getModParams().getMAM().getRegionMapPassengers().getGeneBasedOnToss(i);
//                            String geneName = _model.getModParams().getMAM().getPassengers()[gene].getGeneName();
//                            if (mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId].equals("."))
//                                mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId] = "";
//                            mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId] = 
//                                    mutationsListHavingMinQuotas.get(cycle)[quotaTresholdId]
//                                    + geneName
//                                    + Artifact.geneSeparator 
//                                    + String.valueOf(i)
//                                    + Artifact.outCSVMutationSeparator;
                            }
                        }                    
                }
            }
        }        
    }
    
}
