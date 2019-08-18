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
 * File Artifacts - Header Lines
 * @author Krzysztof Szymiczek
 */
public class Header {

    public static final String clonesPerPopSizeCSVFileHeader = "population_size;clones_count;iteration;cycle";
    public static final String clonesPerCycleCSVFileHeader = "cycle;clones_count";
    public static final String passengersPerPopSizeCSVFileHeader = "population_size;passanger_count;iteration;cycle";
    public static final String driversPerPopSizeCSVFileHeader = "population_size;drivers_count;iteration;cycle";
    public static final String driversPerCycleCSVFileHeader = "cycle;drivers_count";
    public static final String passengersPerCycleCSVFileHeader = "cycle;passanger_count";
    public static final String multiPopSizeCSVFileHeader = "population_size;value;iteration;cycle";



}
