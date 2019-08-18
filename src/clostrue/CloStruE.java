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
import clostrue.hardcodes.Activity;
import clostrue.hardcodes.Constant;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.file.Name;
import clostrue.toolbox.StaticConsoleLogger;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Class of the simulation program
 * NetBeans generated main class.
 * 
 * Loads Gui components and launches the application
 * This class starts has the method for starting the simulation
 * 
 * @author Krzysztof Szymiczek
 */
public class CloStruE extends Application {

    public static Stage intStage;           //NetBeans insert
    public static Parent intRoot;           //NetBeans insert
    public static Scene intScene;           //NetBeans insert
    public static Simulation simulation;    //The main simulation object

    public static Simulation getSimulation() {
        return simulation;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(Constant.appN + Constant.appV);

        String pathToCfg = Constant.getTeWorkDir() + Constant.settingFileDefaultName;

        final Parameters params = getParameters();
        final List<String> parameters = params.getRaw();
        boolean humanOperator = true;
        if ( !parameters.isEmpty() ){
            pathToCfg = parameters.get(0);
            humanOperator = false;
        }
        final String fp = pathToCfg;
        final boolean ho = humanOperator;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CloStruE.fxml"));
        loader.setControllerFactory(c -> {
            return new GuiController(fp,ho);
        });
        intRoot = loader.load();
        intScene = new Scene(intRoot);
        stage.setScene(intScene);
        intStage = stage;
        stage.setOnCloseRequest(event -> {
            GuiController.getSettings().saveToFile();
        });

        stage.show();
    }

    /**
     * Default launcher method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates the simulation and starts the background tasks.
     * This is called when the user pushes the "Start" button.
     * 
     * @param iterationID ID of the iteration
     * @param mutationAdvantageModel mutation advantage model
     * @param pl property change listener
     * @param settings settings
     * @return simulation object
     * @throws InterruptedException         //Thrown exception
     * @throws ExecutionException           //Thrown exception
     * @throws IOException                  //Thrown exception
     */
    public static Simulation startSimulation(
            Integer iterationID,
            MutationAdvModel mutationAdvantageModel,
            PropertyChangeListener pl,
            Settings settings
    ) throws InterruptedException, ExecutionException, IOException {

        // start new Background Task for Simulation and Pass parameters
        simulation = new Simulation(
                iterationID,
                mutationAdvantageModel,
                pl,
                settings
        );

        if (iterationID == 1){
            StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.programRun, Activity.started);
            
            StaticConsoleLogger.setLogToFile(true);
            StaticConsoleLogger.createLogFile(simulation.getSimModel().getFilePaths().getRunWorkDir() + java.io.File.separator + Name.consoleLog + Extension.dotTxt);
            StaticConsoleLogger.openLogFileBuffer();
        }
        
        // Prepare and execute the background calculation tasks
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.preparingTasks, Activity.started);
        simulation.tasksPrepare();
        StaticConsoleLogger.logActivity(simulation.getIteration(), Activity.preparingTasks, Activity.finished);
        simulation.tasksExecute();
        
        StaticConsoleLogger.flushLogFileBuffer();
        
        return simulation;
    }

    public static void stopSimulation(){
        simulation.setStopped(true);
    }

}
