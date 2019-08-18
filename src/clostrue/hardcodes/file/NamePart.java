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

package clostrue.hardcodes.file;
 
/** 
 * File Artifacts - Output file name parts
 * @author Krzysztof Szymiczek
 */
public class NamePart {
    
    public static final String heatmapRatio = "_Heatmap_Ratio";
    public static final String cloneMarker   = "_Clone_Marker";
    public static final String cloneIlandsHeatmap = "_Clone_Islands_Heatmap";
    public static final String image = "Image_";
    public static final String scale = "_Scale_1_To_";

    //  file names subname part
    public static final String snDutyCycleVarianceOverTime = "Duty_Cycle_Variance_Over_Time";
    public static final String snDrivPassRatioHistogram = "Driv_Pass_Ratio_Histogram";
    public static final String snDutyCyclesHistogram = "Duty_Cycles_Histogram";
    public static final String snHistogramSurvivorsPassengerMutCount = "Survivors - Histogram of Passenger Mutation Count";
    public static final String snHistogramShadowDriverMutCount = "Shadow - Histogram of Driver Mutation Count";
    public static final String snHistogramSurvivorsDriverMutCount = "Survivors - Histogram of Driver Mutation Count";
    public static final String snShadowCumulatedDriver = "Time Serie - Occured Driver Mutations";
    public static final String snPopulationSize = "Time Serie - Population Size";
    public static final String snDutyCycleStdDevOverTime = "Duty_Cycle_Std_Dev_Over_Time";
    public static final String snDrivPassRatio = "Time Serie - Driver-Passenger Mutation Ratio Over Time";
    public static final String snHistogramPopulationSize = "Population Size Histogram";
    public static final String snPopulationSizeGainOverTime = "Population_Size_Gain_Over_Time";
    public static final String snCumulatedPassenger = "Time Serie - Occured Passenger Mutations";
    public static final String snMeanDutyCycleOverTime = "Mean_Duty_Cycle_Over_Time";
    public static final String snHistogramShadowPassengerMutCount = "Shadow - Histogram of Passenger Mutation Count";
}
