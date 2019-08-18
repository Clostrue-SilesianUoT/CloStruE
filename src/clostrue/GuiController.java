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

package clostrue;

import clostrue.model.mam.MutationAdvModel;
import eu.hansolo.medusa.Gauge;
import clostrue.hardcodes.EvtProperty;
import clostrue.hardcodes.Constant;
import clostrue.postprocessing.visualization.GraphGenerator;
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Param;
import clostrue.model.FilePaths;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import clostrue.postprocessing.plotter.Plotter;
import clostrue.toolbox.StaticConsoleLogger;

/**
 * FXML Controller class
 *
 * @author Krzysztof Szymiczek
 */
public class GuiController
        implements Initializable, PropertyChangeListener {

    private MutationAdvModel loadedMAM;
    private static Settings settings = null;
    public JFreeChart chart;
    
    @FXML
    private Button buttonStart;
    @FXML
    private Button buttonLoadMAM;
    @FXML
    private Button buttonUnloadMAM;
    @FXML
    private Button buttonChangeWorkDir;
    @FXML
    public TextField teWorkDir;
    @FXML
    public TextField inInitialCellCount;
    @FXML
    public TextField inMaxCycles;
    @FXML
    private ProgressBar progressBar;
    @FXML
    public TextField inMutationRate;
    @FXML
    public TextField inMaxCells;
    @FXML
    public TextField liDriverMutations1;
    @FXML
    public TextField liCurentCycle1;
    @FXML
    public TextField liPassengerMutations1;
    @FXML
    public TextField liPopulationSize1;
    @FXML
    public CheckBox cbTeGenerateGraph;
    @FXML
    public TextField inDriverFitAdvantageTda;
    @FXML
    public TextField inDriverGenesTdg;
    @FXML
    public TextField inPassengerFitAdvantageTpa;
    @FXML
    public TextField inPassengerGenesTpg;
    @FXML
    public TextField mamPassengerGenesTpg;
    @FXML
    public TextField mamDriverGenesTdg;
    @FXML
    private Label laliDriverMutations1;
    @FXML
    private Label laliPassengerMutations1;
    @FXML
    private Label laliCurentCycle1;
    @FXML
    private Label laliPopulationSize1;
    @FXML
    private ProgressBar progressGraph;
    @FXML
    public TextField inDmSize;
    @FXML
    public TextField inPmSize;
    @FXML
    public TextField mamDriverRegionWidth;
    @FXML
    public TextField mamPassengerRegionWidth;
    @FXML
    public TextField mamPath;
    @FXML
    public CheckBox cbPrepareClones;
    @FXML
    public TextField teCellListerCutOff;
    @FXML
    public TextField teCloneMinSize;
    @FXML
    public TextField teCloneMinLifespan;
    @FXML
    private Accordion tpDisplayDebug;
    @FXML
    public CheckBox ggRectangeScale;
    @FXML
    private TitledPane cloneFileSettings;
    @FXML
    public CheckBox teResUseGroupping;
    @FXML
    private Gauge gaugeAccelerator;   
    @FXML
    private Gauge gaugeDrivPass;
    @FXML
    private Label lateCellListerCutOff;
    @FXML
    private Label lateCloneMinSize;
    @FXML
    private Label lateCloneMinLifespan;
    @FXML
    private TextField liShadowSize1;
    @FXML
    private Label laliShadowSize1;
    @FXML
    public CheckBox cbPrepareCells;
    @FXML
    private AnchorPane apSimulation;
    @FXML
    private AnchorPane apAnalytics;
    @FXML
    public CheckBox cbGenerateSimulationPNG;
    @FXML
    public CheckBox cbGenerateAnalyticsPNG;
    @FXML
    public CheckBox cbGenerateSurvivorsAnalytics;
    @FXML
    public StackPane spPopulationSize;
    @FXML
    public CheckBox cbClonesScatter;
    @FXML
    public CheckBox cbDriversSactter;
    @FXML
    public CheckBox cbPassengersScatter;
    @FXML
    public TextField teQuota;
    
    
    double prev0 = 0;
    double prev1 = 0;
    double prev2 = 0;
    double prev3 = 0;
    double prev4 = 0;
    double prev5 = 0;
    double prev6 = 0;
    double prev7 = 0;
    double prev8 = 0;
    double prev9 = 0;
    
    Simulation simulation = null;
    @FXML
    public StackPane spCumulatedDriver;
    @FXML
    public StackPane spCumulatedPassenger;
    @FXML
    public StackPane spDrivPassRatio;
    @FXML
    public StackPane spHistogramDriver;
    @FXML
    public StackPane spHistogramPassenger;
    @FXML
    public StackPane spHistogramDrivPass;
    @FXML
    public StackPane spHistogramPopulationSize;
    @FXML
    public StackPane spMutTypeGeneDriver;
    @FXML
    public StackPane spMutTypeLocusDriver;
    @FXML
    public StackPane spMutTypeGenePassenger;
    @FXML
    public StackPane spMutTypeLocusPassenger;
    @FXML
    public TextField inIterations;
    @FXML
    public TextField liIteration;

    public static Integer iterationsLeft = 0;
    @FXML
    public CheckBox ggScaleToMaxSize;
    @FXML
    public TextField ggMaxSize;
    @FXML
    public CheckBox cbTeGenerateFishplot;
    @FXML
    public TextField teParallelSimTasksCount;
    @FXML
    public TextField teParallelProcTasksCount;
    
    String pathToCfg;
    boolean humanOperator;
    
    public GuiController(String inPathToCfg, boolean inHumanOperator){
        pathToCfg = inPathToCfg;
        humanOperator = inHumanOperator;
    }
    
    /**
     * Get simulation
     * @return simulation
     */
    public Simulation getSimulation() {
        return simulation;
    }
    
    /**
     * Get settings
     * @return settings
     */
    public static Settings getSettings() {
        return settings;
    }    
    
    /**
     * Sets initial GUI component attributes
     */
    private void setGUIcomponentProperties(){
        Platform.runLater(() -> {
            progressBar.setProgress(0);
            progressGraph.setProgress(0);
            gaugeAccelerator.setValue(0);
            gaugeDrivPass.setValue(0);
        });         
    }
    
    /**
     * Sets the binding between GUI components.
     */
    private void setGUIcomponentBindings(){
        teCellListerCutOff.visibleProperty().bind(cbPrepareClones.selectedProperty());
        teCloneMinSize.visibleProperty().bind(cbPrepareClones.selectedProperty());
        teCloneMinLifespan.visibleProperty().bind(cbPrepareClones.selectedProperty());
        lateCellListerCutOff.visibleProperty().bind(cbPrepareClones.selectedProperty());
        lateCloneMinSize.visibleProperty().bind(cbPrepareClones.selectedProperty());
        lateCloneMinLifespan.visibleProperty().bind(cbPrepareClones.selectedProperty()); 
        ggScaleToMaxSize.visibleProperty().bind(cbTeGenerateGraph.selectedProperty());
        ggMaxSize.visibleProperty().bind(cbTeGenerateGraph.selectedProperty());
        ggRectangeScale.visibleProperty().bind(cbTeGenerateGraph.selectedProperty());
    }
    
    /**
     * Application initialization
     * @param url   none
     * @param rb    none
     */
    @Override    
    public void initialize(URL url, ResourceBundle rb) {
               
        settings = new Settings(this);
        settings.setFileName(pathToCfg);
        settings.loadFromFile();
        settings.TransferSettingsToGUI();
        loadMAM(true);

        setGUIcomponentBindings();
        setGUIcomponentProperties();
       
        if ( humanOperator == false){
            iterationsLeft = settings.getIntValue(Param.inIterations);
            try {            
                startSimulationIteration();
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    /**
     * Draws the chart in a separate task to not lock the main GUI
     * @param swingNode 
     * @param chartpanel 
     * @param stackPane 
     */
    public void createSwingContent(final SwingNode swingNode, final ChartPanel chartpanel, final StackPane stackPane) {
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(chartpanel);
        });
        stackPane.getChildren().clear();
        stackPane.getChildren().add(swingNode);
    }
    
    /**
     * Swtiches the guii parts as disable when simulation is in progress
     * @param disableEnable true = GUI disable, false = GUI enable
     */
    private void guiSetDisable(boolean disableEnable){
        buttonStart.setDisable(disableEnable);
    }
    
    @FXML
    /**
     * Button handler for START Button. Starts the simulation
     */
    private void handleButtonStart(ActionEvent event) throws InterruptedException, ExecutionException, IOException {
        
        iterationsLeft = settings.getIntValue(Param.inIterations);
        startSimulationIteration();
        
    }

    /**
     * Starts new iteration of simulation
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException 
     */
    private void startSimulationIteration() throws InterruptedException, ExecutionException, IOException{

        int currentIteration = settings.getIntValue(Param.inIterations) - iterationsLeft + 1;        
        StaticConsoleLogger.logActivity(currentIteration, Activity.garbageCollection, Activity.started);
        System.gc();
        StaticConsoleLogger.logActivity(currentIteration, Activity.garbageCollection, Activity.finished);
        
        if (iterationsLeft <= 0){
            return;            
        }
        
        guiSetDisable(true);
        settings.tranferSettingsFromGUI();
        settings.saveToFile();
        
        MutationAdvModel mutationAdvantageModel;

        // if no mutation advantage model created (loaded from lie)        
        if (loadedMAM == null || (loadedMAM != null && !loadedMAM.isModelOK())) {
            // create Mutation Advantage Model for the simulation
            // in case no model has been read from file            
            mutationAdvantageModel = new MutationAdvModel(
                    Double.valueOf(inDriverFitAdvantageTda.getText()),
                    Integer.valueOf(this.inDriverGenesTdg.getText()),
//                    Integer.valueOf(this.inDriverMutationsTdm.getText()),
                    Integer.valueOf(this.inDmSize.getText()),
                    Double.valueOf(inPassengerFitAdvantageTpa.getText()),
                    Integer.valueOf(this.inPassengerGenesTpg.getText()),
//                    Integer.valueOf(this.inPassengerMutationsTpm.getText()),
                    Integer.valueOf(this.inPmSize.getText()));
        } else {
            mutationAdvantageModel = loadedMAM;
        }

        //initialize some controls
        setGUIcomponentProperties();       
        
        //Start the simulation from the main CloStruE Class
        simulation = CloStruE.startSimulation(
                currentIteration,
                mutationAdvantageModel,
                this,
                settings
                );
        
        iterationsLeft--;
 
        Platform.runLater(() -> {
            liIteration.setText(String.valueOf(currentIteration));
        });        
        
    }
    /**
     * Calls creating of Gexf graf
     */
    private void callDrawGexf(){
        
        try { 

            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.createPopulationGraphic, Activity.started);

            GraphGenerator gen = new GraphGenerator(
                    CloStruE.getSimulation(),
                    progressGraph
            );

            gen.run();
            gen = null;
            System.gc();

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        }

        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.createPopulationGraphic, Activity.finished);

    }      
    
    /**
     * Call creation of plotters
     * @param updateGui 
     */
    private void plot(boolean updateGui) {
        if (CloStruE.simulation != null){
            Plotter plotter = new Plotter(this);
            plotter.handleAllPlots(updateGui);
        }
    }

    @FXML
    /**
     * Button handler for LOAD MAM.
     * creates Mutation Advantage Model from loaded file
     */
    private void handleButtonLoadMAM(ActionEvent event) throws InterruptedException, ExecutionException, IOException {
        loadMAM(false);
        settings.tranferSettingsFromGUI();
    }

    /**
     * Load data and create the mudation advantae model based on file
     * @param silent - do not ask for path
     */
    private void loadMAM(boolean silent){
        String localMamPath;
        loadedMAM = null;
        if (!silent){
            if (mamPath.getText().equals("")){
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    localMamPath = fileChooser.getSelectedFile().getAbsolutePath();
                } else {
                    localMamPath = mamPath.getText();
                }
            } else {
                localMamPath = mamPath.getText();
            }
        } else {
            localMamPath = mamPath.getText();
        }
        
        if (!localMamPath.equals("")){
            
            mamDriverGenesTdg.setText("");
            mamPassengerGenesTpg.setText("");
            loadedMAM = new MutationAdvModel(localMamPath);
            if (loadedMAM != null) {
                if (loadedMAM.isModelOK()) {
                    mamDriverGenesTdg.setText(String.valueOf(loadedMAM.getDrivers().length));
                    mamPassengerGenesTpg.setText(String.valueOf(loadedMAM.getPassengers().length));
                    mamDriverRegionWidth.setText(String.valueOf(loadedMAM.getRegionMapDrivers().getSize()));
                    mamPassengerRegionWidth.setText(String.valueOf(loadedMAM.getRegionMapPassengers().getSize()));
                    mamPath.setText(localMamPath);
                    if ( humanOperator == true ){
                        JOptionPane.showMessageDialog(null, "Mutation Advantage Model (MAM) Loaded Successfully.");                        
                    }
                }
            }
        }     
    }
    
    @FXML
    /**
     * Button handler for Unload MAM.
     * Unloads the preloaded Mutation Advantage Model
     */
    private void handleButtonUnloadMAM(ActionEvent event) throws InterruptedException, ExecutionException, IOException {
        loadedMAM = null;
        mamDriverGenesTdg.setText("");
        mamPassengerGenesTpg.setText("");
        mamDriverRegionWidth.setText("");
        mamPassengerRegionWidth.setText("");
        mamPath.setText("");
        JOptionPane.showMessageDialog(null, "Mutation Advantage Model (MAM) Unloaded Successfully.");
        settings.tranferSettingsFromGUI();
    }

    /**
     * Event handler for properity change.
     * Displays vaules in GUI fields and updates the progress bar fullfillment position
     * this is for updating the gui also in Live! Mode and for various property-driver operations
     * on the screen.
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EvtProperty.epCurentProgress.equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            this.progressBar.setProgress((double)progress / (double)simulation.cCurrFullProgress);
        }
        if (EvtProperty.epFinishCurrentIteration.equals(evt.getPropertyName())){      
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.simulation, Activity.finished);
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.copyMem, Activity.started);
            
            CloStruE.getSimulation().getStatistics().addSimulationToHistogramPopulationSize(CloStruE.getSimulation());
            CloStruE.getSimulation().getStatistics().convertClonesToInternalClones();
            CloStruE.getSimulation().getAnalytics().importCellCollection(CloStruE.getSimulation().getStatistics().getCellCollection());
            CloStruE.getSimulation().getAnalytics().importInternalClones(CloStruE.getSimulation().getStatistics().getInternalClones());
            CloStruE.getSimulation().getAnalytics().importGenomeCollection(CloStruE.getSimulation().getGenomes());
            
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.copyMem, Activity.finished);
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.postProcessing, Activity.started);
                    
            //generate Gephi Graph file (if applicable)
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.graphEntryPoint, Activity.started);
            if ( cbTeGenerateGraph.isSelected() ) {
                StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.graphCall, Activity.started);
                callDrawGexf();
            }
 
            //this has to be done after plotting, as plotting (Gexf and Fishplot)
            //requires shadow predecessors  
            CloStruE.getSimulation().getStatistics().removeShadowPredecessorsFromInternalClones();
            int lastCycle = CloStruE.getSimulation().getLastCycle();
            if ( cbGenerateAnalyticsPNG.isSelected()){
                CloStruE.getSimulation().getStatistics().calculateHistogramShadowDriverMutations();
                CloStruE.getSimulation().getStatistics().calculateHistogramShadowPassengerMutations();
                CloStruE.getSimulation().getStatistics().calculateHistogramSurvivorsDriverMutations(lastCycle);
                CloStruE.getSimulation().getStatistics().calculateHistogramSurvivorsPassengerMutations(lastCycle);
            }
            if ( cbPrepareClones.isSelected()){
                CloStruE.getSimulation().getAnalytics().analyzeShadowSignificantClones();
                CloStruE.getSimulation().getAnalytics().analyzeShadowAllClones();                
                CloStruE.getSimulation().getAnalytics().analyzeSurvivorsSignificantClones(lastCycle);
                CloStruE.getSimulation().getAnalytics().analyzeSurvivorsAllClones(lastCycle);            
                CloStruE.getSimulation().getAnalytics().analyzeSurvivors();
            }

            if (cbTeGenerateFishplot.isSelected()){
                CloStruE.getSimulation().getFishplot().prepareFishplot();
                CloStruE.getSimulation().getFishplot().saveToFile();
                CloStruE.getSimulation().destroyFishplot();                
            }       
            
            if (simulation.getIteration() == settings.getIntValue(Param.inIterations)){
                plot(true);
                
                simulation.getRepSimAnalysis().readTechDataFromIterations(FilePaths.getRunWorkDir());
                simulation.getRepSimAnalysis().createCSVs();
                simulation.getRepSimAnalysis().createCharts();
                
                guiSetDisable(false);    
                Platform.runLater(() -> {
                    StaticConsoleLogger.closeLogFileBuffer();
                    if ( humanOperator == true ){
                        JOptionPane.showMessageDialog(null, Constant.dialogSimulationFinish);                                     
                    } else {
                        Platform.exit();
                        System.exit(0);
                    }

                });
            } else {
                plot(false);
            }

        
            
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.postProcessing, Activity.finished);

            //if there are not processed simulation iterations -> start a new one
            if (iterationsLeft > 0){
                try {
                    startSimulationIteration();
                } catch (InterruptedException | ExecutionException | IOException ex) {
                    Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.programRun, Activity.finished);
            }        
        }
            
        if (EvtProperty.epCurentDriverMutations.equals(evt.getPropertyName())) {
            int driverMutations = (Integer) evt.getNewValue();
            Platform.runLater(() -> {
                liDriverMutations1.setText(String.valueOf(driverMutations));
            });
        }
        if (EvtProperty.epCurentPassengerMutations.equals(evt.getPropertyName())) {
            int passengerMutations = (Integer) evt.getNewValue();
            Platform.runLater(() -> {
                liPassengerMutations1.setText(String.valueOf(passengerMutations));
            });
        }
        if (EvtProperty.epCurentCycle.equals(evt.getPropertyName())) {
            int _curentCycle = (Integer) evt.getNewValue();
            Platform.runLater(() -> {
                liCurentCycle1.setText(String.valueOf(_curentCycle));
            });
        }
        if (EvtProperty.epCurentPopulationSize.equals(evt.getPropertyName())) {
            double populationSize = ((Integer) evt.getNewValue()).doubleValue();
            double prevPopSize    = ((Integer) evt.getOldValue()).doubleValue();
            
            Double meanAcc2 = getMeanAcceleration(populationSize, prevPopSize);
            double passengers;
            double drivers;
            double driPasRatio;
            String passMutations = liPassengerMutations1.getText();
            if (passMutations == null){
                passengers = 0;
            } else{
                if (passMutations.isEmpty()){
                passengers = 0;
                } else {
                    passengers = Double.valueOf(passMutations);
                }
            }
            String drivMutations = liDriverMutations1.getText();
            if (drivMutations == null){
                drivers = 0;
            } else{
                if (drivMutations.isEmpty()){
                drivers = 0;
                } else {
                    drivers = Double.valueOf(drivMutations);
                }
            }

            if (passengers != 0){
                driPasRatio = (double) 10000.0 * drivers / passengers;
            } else {
                driPasRatio = 0;
            }
            int shadowSize = simulation.getLiveStats().getShadowSize();         
            Platform.runLater(() -> {
                liPopulationSize1.setText(String.valueOf(populationSize));
                gaugeAccelerator.setValue( meanAcc2 );     
                gaugeDrivPass.setValue(driPasRatio);
                liShadowSize1.setText(String.valueOf(shadowSize));
            });
        }

    }

    /**
     * Calculates mean acceleration of population size for the gauges
     * @param populationSize population size
     * @param prevPopSize previous population size
     * @return  mean acceleration of population size
     */
    private Double getMeanAcceleration(double populationSize, double prevPopSize){
        
        Double acceleration;
        Double meanAcc;
        Double meanAcc2;
        
        if (prevPopSize != 0){
            acceleration = ( ( populationSize - prevPopSize ) / prevPopSize ) * (double) 100;                    
        } else {
            acceleration = (Double) 0.0;
        }

        prev9 = prev8;
        prev8 = prev7;
        prev7 = prev6;
        prev6 = prev5;
        prev5 = prev4;
        prev4 = prev3;
        prev3 = prev2;
        prev2 = prev1;
        prev1 = prev0;
        prev0 = acceleration;

        meanAcc = ( prev0 + prev1 + prev2 + prev3 + prev4 
                  + prev5 + prev6 + prev7 + prev8 + prev9 ) / (double) 10.0; 

        if (meanAcc < -10) meanAcc = -10.0;
        if (meanAcc > 10)  meanAcc = 10.0;

        meanAcc2 = meanAcc;
        return meanAcc2;
    }
    
    @FXML
    /**
     * Button handler for changing the working directory where the application
     * saves all the otuput files
     */
    private void handleButtonChangeWorkDir(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        if ( teWorkDir.getText().equals(""))
        {
            fileChooser.setCurrentDirectory(new java.io.File("."));
        }
        else {
            fileChooser.setCurrentDirectory(new java.io.File(teWorkDir.getText()));
        }
            fileChooser.setDialogTitle("Select Working Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            teWorkDir.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }        
    }

    @FXML
    /**
     * When user presses a key - value gets updated in Settings object
     */
    private void handleOnKeyInInputField(KeyEvent event) {
        settings.tranferSettingsFromGUI();
    }

    @FXML
    /**
     * Handles the loading of default setup values
     */
    private void handleMenuLoadDefaults(ActionEvent event) {
        settings.resetToDefaults();
        settings.TransferSettingsToGUI();
        settings.tranferSettingsFromGUI();
        settings.setFileName(Constant.getTeWorkDir() + Constant.settingFileDefaultName);
        settings.saveToFile();
    }

    @FXML
    /**
     * Handles the menu option for loading settings
     */
    private void handleMenuLoadSettings(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Application Settings...");
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            settings.setFileName(fileChooser.getSelectedFile().getAbsolutePath());
            settings.loadFromFile();
            settings.TransferSettingsToGUI();
        }
    }

    @FXML
    /**
     * Handles the menu option for saving settings
     */
    private void handleMenuSaveSettings(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Application Settings...");
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            settings.setFileName(fileChooser.getSelectedFile().getAbsolutePath());
            if ( settings.saveToFile() ){
                JOptionPane.showMessageDialog(null, "Application Settings saved in file: " 
                        + fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    @FXML
    /**
     * Handles mouse change on radiobutton and checkbox field
     * in order to update the settings
     */
    private void handleonMouseReleased(MouseEvent event) {
        settings.tranferSettingsFromGUI();
    }

    @FXML
    /**
     * Handles mouse change on radiobutton and checkbox field
     * in order to update the settings
     */
    private void handleOnMouseReleased(MouseEvent event) {
        settings.tranferSettingsFromGUI();
    }
    
    /**
     * Handles mouse change on radiobutton and checkbox field
     * in order to update the settings
     */
    private void handleOnMouseReleased(KeyEvent event) {
        settings.tranferSettingsFromGUI();
    }

    /**
     * Handles the button which should stop the curent simulation
     */
    private void handleButtonStopSimulation(KeyEvent event) {
        CloStruE.stopSimulation();
    }


    @FXML
    private void handleOKRteParallelTasksCount(KeyEvent event) {
        settings.tranferSettingsFromGUI();
    }

    private void handleOMRButtonStopSimulation(MouseEvent event) {
        CloStruE.stopSimulation();        
    }

}


