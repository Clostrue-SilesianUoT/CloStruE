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
package clostrue.toolbox;
 
/**
 * Several static tools for Math Operations are available
 * by this class
 * @author Krzysztof Szymiczek
 */
public class MathTools {

    /**
     * Rounding double to n decimal places
     * @param value     value to round
     * @param decimals  decimal places
     * @return          rounded value
     */
    public static double round(double value, double decimals){
        double dResult = value;
        for (int i = 0; i < decimals; i++){
            dResult *= 10;
        }
        int iResult = (int)Math.round(dResult);
        dResult = iResult;
        for (int i = 0; i < decimals; i++){
            dResult /= 10;
        }
        return dResult;
    }
    
    /**
     * Power for integers
     * @param base   base of the power
     * @param power  power of the power
     * @return result of the power operation
    */
    public static int intPower(int base, int power){
        int result = 1;
        
        for (int i = 0; i < power; i++){
            result *= base;
        }
        
        return result;
    }
   
}
