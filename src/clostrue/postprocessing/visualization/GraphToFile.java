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
 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import clostrue.Settings;
import clostrue.collections.CellCollection;
import clostrue.collections.GenomeCollection;
import clostrue.hardcodes.Param;
import clostrue.hardcodes.file.Extension;
import clostrue.hardcodes.file.NamePart;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is for saving graph to file
 * 
 * @author Krzysztof Szymiczek  
 */
public class GraphToFile {

    private static int intColor(byte color){
        return (int)color + (int)128;
    }
    
    public static String saveGraphToPngFile(CellCollection cells, GenomeCollection genomes, int canvasXSize, int canvasYSize, String inWorkDir, Collection <Node> nodes, Settings settings, String fileNamePart, boolean drawBorders, boolean useColorMap) throws IOException
    {     
        float progressByNode = (float) 25.0;       
        int localCanvasXSize;
        int localCanvasYSize;
        int scaleX = 1;
        int scaleY = 1;
        
        if (settings.getBooleanValue(Param.ggRectangeScale)){
            if (((double)canvasXSize / (double)canvasYSize) > (double)2.0){
                scaleY = (int)((double)canvasXSize / (double)canvasYSize);
            } else if (((double)canvasYSize / (double)canvasXSize) > (double)2.0){
                scaleX = (int)((double)canvasYSize / (double)canvasXSize);
            }            
        }

        localCanvasXSize = canvasXSize * scaleX;
        localCanvasYSize = canvasYSize * scaleY;
        int ratioTmp;
        if (settings.getBooleanValue(Param.ggScaleToMaxSize)){
            int maxSize = settings.getIntValue(Param.ggMaxSize);
            ratioTmp = Math.max(localCanvasXSize/maxSize, localCanvasYSize/maxSize);
            if (ratioTmp < 1){
                ratioTmp = 1;
            }
        } else {
            ratioTmp = 1;
        }
        final int ratio = ratioTmp;
        localCanvasXSize /= ratio;
        localCanvasYSize /= ratio;
        final int xSize = localCanvasXSize;
        final int ySize = localCanvasYSize;
        
        int nodesCount = (int) nodes.size() ;
        float progressStepNode = (progressByNode / nodesCount);       
        
        String path = inWorkDir 
                + java.io.File.separator 
                + NamePart.image 
                + fileNamePart
                + NamePart.scale
                + String.valueOf(ratio)
                + Extension.dotPng;
        BufferedImage bImage = new BufferedImage( localCanvasXSize, localCanvasYSize, BufferedImage.TYPE_INT_RGB );
        
        final int fScaleX = scaleX;
        final int fScaleY = scaleY;
        
        WritableRaster rast = bImage.getRaster();
        int[] pixels    = new int[bImage.getWidth() * bImage.getHeight()];
        int[] cloneIDs  = new int[bImage.getWidth() * bImage.getHeight()]; 
        Arrays.fill(pixels, Color.WHITE.getRGB());
        Arrays.fill(cloneIDs, 0);
        final int width = bImage.getWidth();
        final int height = bImage.getHeight();
        nodes.stream().forEach((Node node) -> {
            GraphGenerator.setProgress(GraphGenerator.getProgress() + progressStepNode);     

            if (useColorMap){
                Color nodeColor = GraphGenerator.colorsTable.get(node.colorIndex);
                for (int moveScaleX = 0; moveScaleX < fScaleX; moveScaleX++){
                        for (int moveScaleY = 0; moveScaleY < fScaleY; moveScaleY++){
                            int xCoord = Math.min((( node.getOrder() * fScaleX + moveScaleX ) / ratio), xSize-1);
                            int yCoord = Math.min((( node.getLevel(cells) * fScaleY + moveScaleY ) / ratio), ySize-1);
                            pixels[yCoord * width + xCoord ] = nodeColor.getRGB();
                            cloneIDs[yCoord * width + xCoord ] = node.getDriverCloneGroupID(cells, genomes);
                        }                        

                }
            } else {       
                for (int moveScaleX = 0; moveScaleX < fScaleX; moveScaleX++){
                        for (int moveScaleY = 0; moveScaleY < fScaleY; moveScaleY++){
                            int xCoord = Math.min((( node.getOrder() * fScaleX + moveScaleX ) / ratio), xSize-1);
                            int yCoord = Math.min((( node.getLevel(cells) * fScaleY + moveScaleY ) / ratio), ySize-1);
                            pixels[yCoord * width + xCoord ] 
                                    = new Color(
                                            intColor(node.color_r),
                                            intColor(node.color_g),
                                            intColor(node.color_b)).getRGB();
                            cloneIDs[yCoord * width + xCoord ] = node.getDriverCloneGroupID(cells, genomes);

                        }                        
                }
            }
            });
        
        if (drawBorders){
            drawBorders(pixels,cloneIDs,3,width,height);
        }
        
        try {
            rast.setDataElements(0, 0, width, height, pixels);
            bImage.setData(rast);
            ImageIO.write(bImage, Extension.png, new File(path));
        } catch ( IOException e) {
        }

        return path;
    }
    
     public static String saveGraphToPngFileIdexes(CellCollection cells, GenomeCollection genomes, int canvasXSize, int canvasYSize, String inWorkDir, Collection <Node> nodes, Settings settings, String fileNamePart, boolean drawBorders, boolean useColorMap) throws IOException
    {     
        float progressByNode = (float) 25.0;       
        int localCanvasXSize;
        int localCanvasYSize;
        int scaleX = 1;
        int scaleY = 1;
        
        if (settings.getBooleanValue(Param.ggRectangeScale)){
            if (((double)canvasXSize / (double)canvasYSize) > (double)2.0){
                scaleY = (int)((double)canvasXSize / (double)canvasYSize);
            } else if (((double)canvasYSize / (double)canvasXSize) > (double)2.0){
                scaleX = (int)((double)canvasYSize / (double)canvasXSize);
            }            
        }

        localCanvasXSize = canvasXSize * scaleX;
        localCanvasYSize = canvasYSize * scaleY;
        int ratioTmp;
        if (settings.getBooleanValue(Param.ggScaleToMaxSize)){
            int maxSize = settings.getIntValue(Param.ggMaxSize);
            ratioTmp = Math.max(localCanvasXSize/maxSize, localCanvasYSize/maxSize);
            if (ratioTmp < 1){
                ratioTmp = 1;
            }
        } else {
            ratioTmp = 1;
        }
        final int ratio = ratioTmp;
        localCanvasXSize /= ratio;
        localCanvasYSize /= ratio;
        final int xSize = localCanvasXSize;
        final int ySize = localCanvasYSize;
        
        int nodesCount = (int) nodes.size() ;
        float progressStepNode = (progressByNode / nodesCount);       
        
        String path = inWorkDir 
                + java.io.File.separator 
                + NamePart.image + "INDEXES_" 
                + fileNamePart
                + NamePart.scale
                + String.valueOf(ratio)
                + Extension.dotPng;
        BufferedImage bImage = new BufferedImage( localCanvasXSize, localCanvasYSize, BufferedImage.TYPE_INT_RGB );
        
        final int fScaleX = scaleX;
        final int fScaleY = scaleY;
        
        WritableRaster rast = bImage.getRaster();
        int[] pixels    = new int[bImage.getWidth() * bImage.getHeight()];
        int[] cloneIDs  = new int[bImage.getWidth() * bImage.getHeight()]; 
        Arrays.fill(pixels, Color.WHITE.getRGB());
        Arrays.fill(cloneIDs, 0);
        final int width = bImage.getWidth();
        final int height = bImage.getHeight();
        nodes.stream().forEach((Node node) -> {
            GraphGenerator.setProgress(GraphGenerator.getProgress() + progressStepNode);     

            Color nodeColor = new Color(node.cellIndex);//GraphGenerator.colorsTable.get(node.colorIndex);
                for (int moveScaleX = 0; moveScaleX < fScaleX; moveScaleX++){
                        for (int moveScaleY = 0; moveScaleY < fScaleY; moveScaleY++){
                            int xCoord = Math.min((( node.getOrder() * fScaleX + moveScaleX ) / ratio), xSize-1);
                            int yCoord = Math.min((( node.getLevel(cells) * fScaleY + moveScaleY ) / ratio), ySize-1);
                            
                            cloneIDs[yCoord * width + xCoord ] = node.getDriverCloneGroupID(cells, genomes);
                            pixels[yCoord * width + xCoord ] = cloneIDs[yCoord * width + xCoord];
                        }
                }
            });
        
        if (drawBorders){
            drawBorders(pixels,cloneIDs,3,width,height);
        }
        
        try {
            rast.setDataElements(0, 0, width, height, pixels);
            bImage.setData(rast);
            ImageIO.write(bImage, Extension.png, new File(path));
        } catch ( IOException e) {
            //
        }

        return path;
    }

   private static void drawBorders(int[] pixels, int[] cloneIDs, int thickness, int width, int height){
        
        int currRGB;
        int prevCloneID = 0;
        int currCloneID;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                currRGB = pixels[y * width + x ];
                currCloneID = cloneIDs[y * width + x ];
                if (currRGB != Color.BLACK.getRGB()){
                    if (x > (thickness + 1) &&  prevCloneID != currCloneID ){
                        for (int w = 0; w < thickness; w++){
                            pixels[y * width + (x-1-w) ] = Color.BLACK.getRGB();
                        }
                    } 
                    prevCloneID = currCloneID;
                }                  
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                currRGB = pixels[y * width + x];
                currCloneID = cloneIDs[y * width + x];
                if (currRGB != Color.BLACK.getRGB()){                
                    if ( (y > thickness + 1) && prevCloneID != currCloneID ){
                        for (int w = 0; w < thickness; w++){
                            pixels[(y-1-w) * width + x ] = Color.BLACK.getRGB();
                        } 
                    }
                    prevCloneID = currCloneID;
                }
            }
        } 
    }    
    
}
