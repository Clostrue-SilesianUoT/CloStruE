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
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.Message;
import clostrue.hardcodes.ModelParam;
import clostrue.hardcodes.file.Artifact;
import clostrue.hardcodes.file.Column;
import java.util.HashMap;

/**
 * Models the mutation advantage model.
 * 
 * The mutation advantage model contains information about the advantages
 * and disadvantages caused by each possible mutation on each of the 
 * driver and passenger genes named (contained) in the mutation advantage model.
 * The model can be created from the standard simulation parameters
 * (Max driver and passenger mutations and the Driver and Passenger Advantage),
 * or can be directly loaded from flat csv file, where each gene is named and 
 * classified (driver or passenger) and where each of the genes has assigned
 * the mutation advantage / disadvantage per one mutation on that gene.
 * 
 * @author Krzysztof Szymiczek
 */

public class MutationAdvModel {

    private MutationAdvantageData drivers[];        //Driver Mutation Advantages
    private MutationAdvantageData passengers[];     //Passenger Mutation Advantages
    private double mpTargetDriversTd;               //ammount of possible Loci's for Driver Mutation
    private double mpTargetPassengersTp;            //ammount of possible Loci's for Passenger Mutation
    private RegionTossMap regionMapDrivers;         //toss map for drivers
    private RegionTossMap regionMapPassengers;      //toss map for passengers  
    private int genomeDriversPartSize;              //part size of the genome occupied by Driver Genes
    private int genomePassengersPartSize;           //part size of the genome occupied by Passenger Genes
    private boolean _modelOK;                       //indicator of no errors in the model
    private String driverTags[];                    //unqiue tags of driver genes
    private String passengerTags[];                 //unique tags of passenger genes
    
    /**
     * This is a simple constructor of the Mutation Advantage Model.
     * 
     * Based on the parameters form TGS Screen, a artificial list of genes with
     * obligatory names D_* and P_* is created (for Drivers and Passengers)
     * The number of genes
     * of each category is read from TGS Screen (Simulation Settings). 
     * The mutation advantages for driver genes and the mutation disadvantages
     * for the passenger genes are assigned equal for each gene according to
     * the values read from TGS Screen (Simulation Settings)
     * 
     * @param inDriverFitAdvantageTda       //Fit advantage of a single driver mutation
     * @param inDriverGenesTdg              //The ammount of driver genes
//     * @param inDriverMutationsTdm          //Maximal count of mutations per driver gene
     * @param inDmSize                      //Width of driver mutation
     * @param inPassengerFitAdvantageTpa    //Fit disadvantage of a single passenger mutation
     * @param inPassengerGenesTpg           //Tge ammount of passenger genes
//     * @param inPassengerMutationsTpm       //Maximal count of mutations per passenger gene.
     * @param inPmSize                      //Width of passenger mutation
     */
        public MutationAdvModel(
            double inDriverFitAdvantageTda,
            int inDriverGenesTdg,
//            int inDriverMutationsTdm,
            int inDmSize,
            double inPassengerFitAdvantageTpa,
            int inPassengerGenesTpg,
//            int inPassengerMutationsTpm,
            int inPmSize) {

// generate the predefined ammount of driver and passenger genes
// advantage is stored separatelly per each gene (this identical when this 
// constructor is used from the "Simple Model" -> screen parameters
        _modelOK            = true;
        driverTags          = new String[1];
        passengerTags       = new String[1];
        driverTags[0]       = ModelParam.commonGeneTag;
        passengerTags[0]    = ModelParam.commonGeneTag;
        drivers             = new MutationAdvantageData[inDriverGenesTdg];
        passengers          = new MutationAdvantageData[inPassengerGenesTpg];

        for (int i = 0; i < inDriverGenesTdg; i++) {
            String geneName = "D_" + String.valueOf(i);
            MutationAdvantageData singleDriverAdvantage
                    = new MutationAdvantageData(
                            geneName, 
                            inDriverFitAdvantageTda, 
                            inDmSize,
                            ModelParam.commonGeneTag,
                            ModelParam.commonGeneTagId);
            drivers[i] = singleDriverAdvantage;
            mpTargetDriversTd += inDmSize;
        }

        for (int i = 0; i < inPassengerGenesTpg; i++) {
            String geneName = "P_" + String.valueOf(i);
            MutationAdvantageData singlePassengerAdvantage
                    = new MutationAdvantageData(
                            geneName, 
                            inPassengerFitAdvantageTpa, 
                            inPmSize,
                            ModelParam.commonGeneTag,
                            ModelParam.commonGeneTagId);
            passengers[i] = singlePassengerAdvantage;
            mpTargetPassengersTp += inPmSize;
        }

        regionMapDrivers    = new RegionTossMap(drivers);
        regionMapPassengers = new RegionTossMap(passengers);
        
        genomeDriversPartSize = RegionTossMap.calculateIntSize(drivers);
        genomePassengersPartSize = RegionTossMap.calculateIntSize(passengers);

    }

    private MutationAdvantageData[] cloneMAMpart(MutationAdvantageData[] sourceMAMPart){
        MutationAdvantageData[] clonedMAMpart = new MutationAdvantageData[sourceMAMPart.length]; 
        for (int i = 0; i < sourceMAMPart.length; i++)
            clonedMAMpart[i] = new MutationAdvantageData(sourceMAMPart[i]);        
        return clonedMAMpart;
    }
        
    public MutationAdvModel(MutationAdvModel source){

        drivers                     = cloneMAMpart(source.drivers);
        passengers                  = cloneMAMpart(source.passengers);
        driverTags                  = source.driverTags.clone();
        passengerTags               = source.passengerTags.clone();
        mpTargetDriversTd           = source.mpTargetDriversTd;
        mpTargetPassengersTp        = source.mpTargetPassengersTp;
        regionMapDrivers            = new RegionTossMap(source.regionMapDrivers);
        regionMapPassengers         = new RegionTossMap(source.regionMapPassengers);
        genomeDriversPartSize       = source.genomeDriversPartSize;
        genomePassengersPartSize    = source.genomePassengersPartSize;
        _modelOK                    = source._modelOK;    //indicator of no errors in the model

    }
        
    public boolean isModelOK() {
        return _modelOK;
    }

    public int getGenomeDriversPartSize() {
        return genomeDriversPartSize;
    }

    public int getGenomePassengersPartSize() {
        return genomePassengersPartSize;
    }

    /**
     * This is a precise constructor of the Mutation Advantage Model.
     * 
     * Based on the MOM file (MOM = Mutation Advantage Model)
     * loaded from the TGS Screen, a true list of genes with
     * given names is created. The genes are read from flat csv file.
     * The mutation advantages for driver genes and the mutation disadvantages
     * for the passenger genes are also defined in flat file and does not have
     * to be equal among each gene group.
     * Example file part:
     * GENE_NAME;TYPE (DRIVER/PASSENGER);MUTATION_ADVANTAGE;GENE_SIZE;GENE_TAG
     * CSV_DRI_1;DRIVER;0.15;1000;TG_1
     * CSV_DRI_2;DRIVER;0.15;1000;TG_1
     * CSV_DRI_3;DRIVER;0.15;1000;TG_2
     * CSV_DRI_4;DRIVER;0.15;1000;TG_2   
     * 
     * @param filePath the path under which the MAM file is stored.
     */
        public MutationAdvModel(String filePath) {

// generate the predefined ammount of driver and passenger genes
// advantage is stored separatelly per each gene (this identical when this 
// constructor is used from the "Simple Model" -> screen parameters
        
        _modelOK        = false;
        ArrayList<MutationAdvantageData> locDrivers     = new ArrayList<>();
        ArrayList<MutationAdvantageData> locPassengers  = new ArrayList<>();
        HashMap<String,Integer> locDriverTagsHm           = new HashMap<>();
        HashMap<String,Integer> locPassengerTagsHm           = new HashMap<>();

        Integer currDriverTagId = 1;
        Integer currPassengerTagId = 1;
        
        
        BufferedReader br;
        String line;
        boolean firstSkipped = false;

        br = null;

        locDriverTagsHm.put(ModelParam.commonGeneTag,ModelParam.commonGeneTagId);
        locPassengerTagsHm.put(ModelParam.commonGeneTag,ModelParam.commonGeneTagId);           
        
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                if (firstSkipped) {

                    String[] columns = line.split(Artifact.csvColumnSeparator);
                    Integer tagId = -1;
                    
                    if (columns.length == 5) {
                        String geneName = columns[Column.mamFileFields.geneName.getIndex()];
                        double fitAdvantage = Double.valueOf(columns[Column.mamFileFields.fitAdvantage.getIndex()]);
//                        int maxMutations = Integer.valueOf(columns[Column.mamFileFields.maxMutations.getIndex()]);
                        int geneSize = Integer.valueOf(columns[Column.mamFileFields.geneSize.getIndex()]);
                        String geneTag = columns[Column.mamFileFields.geneTag.getIndex()];
                        if (Constant.mamGeneTypeDriver.equals(columns[Column.mamFileFields.geneType.getIndex()])) {
                            mpTargetDriversTd += geneSize;                          
                            tagId = locDriverTagsHm.get(geneTag);
                            if (tagId == null){
                                tagId = currDriverTagId;
                                locDriverTagsHm.put(geneTag, currDriverTagId);
                                currDriverTagId++;
                            }
                        }
                        if (Constant.mamGeneTypePassenger.equals(columns[Column.mamFileFields.geneType.getIndex()])) {
                            mpTargetPassengersTp += geneSize;  
                            tagId = locPassengerTagsHm.get(geneTag);
                            if (tagId == null){
                                tagId = currPassengerTagId;
                                locPassengerTagsHm.put(geneTag, currPassengerTagId);
                                currPassengerTagId++;
                            }
                        }
                        MutationAdvantageData singleMutationAdvantage
                                = new MutationAdvantageData(
                                        geneName, 
                                        fitAdvantage, 
                                        geneSize,
                                        geneTag,
                                        tagId);
                        if (Constant.mamGeneTypeDriver.equals(columns[Column.mamFileFields.geneType.getIndex()])) {
                            locDrivers.add(singleMutationAdvantage);
                        }
                        if (Constant.mamGeneTypePassenger.equals(columns[Column.mamFileFields.geneType.getIndex()])) {
                            locPassengers.add(singleMutationAdvantage);
                        }
                    }

                } else {
                    if (!Constant.mamAllowedHeader.equals(line)) {
                        JOptionPane.showMessageDialog(null,
                                Message.messMamFileHeaderNotCorrect,
                                Message.messMamModelLoadError,
                                JOptionPane.ERROR_MESSAGE);
                        try {
                            br.close();
                            return;
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null,
                                    Message.messFileCouldNotBeClosed,
                                    Message.messError,
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        firstSkipped = true;
                    }
                }
            }
            
            drivers         = new MutationAdvantageData[locDrivers.size()];
            passengers      = new MutationAdvantageData[locPassengers.size()];
            driverTags      = new String[locDriverTagsHm.size()];
            passengerTags   = new String[locPassengerTagsHm.size()];
            
            for (int i = 0; i < locDrivers.size(); i++)
                drivers[i] = locDrivers.get(i);

            for (int i = 0; i < locPassengers.size(); i++)
                passengers[i] = locPassengers.get(i);
            
            locDriverTagsHm.entrySet().forEach((entry) -> {
                driverTags[entry.getValue()] = entry.getKey();
            });

            locPassengerTagsHm.entrySet().forEach((entry) -> {
                passengerTags[entry.getValue()] = entry.getKey();
            });
            
            regionMapDrivers            = new RegionTossMap(drivers);
            regionMapPassengers         = new RegionTossMap(passengers);
            genomeDriversPartSize       = RegionTossMap.calculateIntSize(drivers);
            genomePassengersPartSize    = RegionTossMap.calculateIntSize(passengers);
                    
            _modelOK = true;

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    Message.messFileNotFound,
                    Message.messError,
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    Message.messFileAccess,
                    Message.messError,
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            Message.messFileCouldNotBeClosed,
                            Message.messError,
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public RegionTossMap getRegionMapDrivers() {
        return regionMapDrivers;
    }

    public RegionTossMap getRegionMapPassengers() {
        return regionMapPassengers;
    }        
        
    /**
     * Returns the ammount of possible mutations on all Driver Genes.
     * 
     * This value is used in simulation for weighing the random generator.
     * @return the ammount of possible driver mutations
     */
    public double getMpTargetDriversTd() {
        return mpTargetDriversTd;
    }

    /**
     * Returns the ammount of possible mutations on all Passenger Genes.
     * 
     * This value is used in simulation for weighing the random generator.
     * @return the ammount of possible passenger mutations
     */
    public double getMpTargetPassengersTp() {
        return mpTargetPassengersTp;
    }

    /**
     * Returns the list of driver advantages
     * @return list of driver advantages
     */
    public MutationAdvantageData[] getDrivers() {
        return drivers;
    }

    /**
     * Returns the list of passenger disadvantages
     * @return list of passenger disadvantages
     */
    public MutationAdvantageData[] getPassengers() {
        return passengers;
    }

    /**
     * return list of unique driver tags
     * @return driver tags
     */
    public String[] getDriverTags() {
        return driverTags;
    }

    /**
     * returns list of unique passenger tags
     * @return passenger tags
     */
    public String[] getPassengerTags() {
        return passengerTags;
    }    

    public int getMaxDriverTagId() {
        return driverTags.length - 1;
    }
    
    public int getMaxPassengerTagId() {
        return passengerTags.length - 1;
    }
    
    public String getPassengerTag(int index){
        return passengerTags[index];
    }

    public String getDriverTag(int index){
        return driverTags[index];
    }

    
}
