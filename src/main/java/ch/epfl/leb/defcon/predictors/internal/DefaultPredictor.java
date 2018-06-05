/**
 * Copyright (C) 2018 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne (EPFL), Switzerland
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.defcon.predictors.internal;

import ch.epfl.leb.defcon.predictors.ImageBitDepthException;
import ch.epfl.leb.defcon.predictors.SessionClosedException;
import ch.epfl.leb.defcon.predictors.NoLocalCountMapException;
import ch.epfl.leb.defcon.predictors.UninitializedPredictorException;
import ch.epfl.leb.defcon.predictors.Predictor;

import ij.gui.Roi;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tensorflow.Tensor;

/**
 * Makes density map predictions from images.
 * 
 * @author Baptiste Ottino
 * @author Kyle M. Douglass
 */
public class DefaultPredictor extends AbstractPredictor implements Predictor {
    
    private final static Logger LOGGER = Logger.getLogger(
            DefaultPredictor.class.getName());
    
    /**
     * The most-recently determined count.
     */
    private Double count;
    
    /**
     * The most-recently computed density map.
     */
    private FloatProcessor densityMap;
    
    /**
     * The most-recently computer maximum local count map.
     */
    private FloatProcessor localCountMap;
    
    /**
     * Checks that an image's dimensions are divisible by four and crops it if not.
     * 
     * This restriction on the size of an image is a requirement of DEFCoN.
     * 
     * @return The input image, possibly cropped.
     */
    private ImageProcessor checkDimensions(ImageProcessor ip) {
        
        Rectangle currRoi = ip.getRoi();
        ip.setRoi(new Roi(currRoi.x, currRoi.y,
                          currRoi.width - currRoi.width % 4,
                          currRoi.height - currRoi.height % 4));
        return ip.crop();
    }
    
    /**
     * Returns the most recent count.
     * 
     * @return The predicted count from the density map.
     * @throws ch.epfl.leb.defcon.predictors.UninitializedPredictorException 
     */
    @Override
    public double getCount() throws UninitializedPredictorException {
        if (count == null) {
            String msg = "The Predictor has not yet performed any calcuations.";
            LOGGER.log(Level.WARNING, msg);
            throw new UninitializedPredictorException(msg);
        }
        return count;
    }
    
    /**
     * Returns the most recently calculated density map prediction.
     * 
     * @return The predicted density map.
     * @throws ch.epfl.leb.defcon.predictors.UninitializedPredictorException
     */
    public FloatProcessor getDensityMap() throws UninitializedPredictorException {
        if (densityMap == null) {
            String msg = "The Predictor has not yet performed any calcuations.";
            LOGGER.log(Level.SEVERE, msg);
            throw new UninitializedPredictorException(msg);
        }
        return densityMap;
    }
    
    /**
     * Returns the most recently-calculated local count map.
     * 
     * @return The most recently calculated local count map.
     * @throws ch.epfl.leb.defcon.predictors.NoLocalCountMapException
     */
    @Override
    public FloatProcessor getLocalCountMap() throws NoLocalCountMapException {
        if (localCountMap == null) {
            String msg = "The Predictor has not yet performed any local " + 
                         "count estimates.";
            LOGGER.log(Level.SEVERE, msg);
            throw new NoLocalCountMapException(msg);
        }
        return localCountMap;
    }
    
    /**
     * Returns the maximum local count value.
     * 
     * This value is obtained by convolving the density map with a square kernel
     * whose values are all 1 and then taking the maximum of the resulting map.
     * It effectively produces the highest count value over length scales equal
     * to the size of the kernel.
     * 
     * @param boxSize The width of the square kernel.
     * @return The maximum local count from the density map.
     * @throws ch.epfl.leb.defcon.predictors.UninitializedPredictorException
     */
    @Override
    public double getMaximumLocalCount(int boxSize)
           throws UninitializedPredictorException {
        if (densityMap == null) {
            String msg = "The Predictor has not yet performed any calcuations.";
            LOGGER.log(Level.WARNING, msg);
            throw new UninitializedPredictorException(msg);
        }
        Convolver convolver = new Convolver();
        convolver.setNormalize(false);
        localCountMap = (FloatProcessor) densityMap.clone();
        float kernel[] = new float[boxSize * boxSize];
        Arrays.fill(kernel, 1.0f);
        
        convolver.convolveFloat(localCountMap, kernel, boxSize, boxSize);
        int halfBoxSize = boxSize / 2;
        localCountMap.setRoi(new Roi(halfBoxSize, halfBoxSize,
                        localCountMap.getWidth() - boxSize + 1 ,
                        localCountMap.getHeight() - boxSize + 1));
        localCountMap = localCountMap.crop().convertToFloatProcessor();
        localCountMap.resetMinAndMax();
        return localCountMap.getMax();
    }
    
    /**
     * Makes a density map prediction from a 2D 8-bit  image.
     * 
     * @param bp The 8-bit image to perform a prediction on.
     */
    private void predict(final ByteProcessor bp) {
        ShortProcessor sp = bp.convertToShortProcessor();
        predict(sp);
    }
    
    /**
     * Makes a density map prediction from a 2D image.
     * 
     * If either of the image's width or height is not divisible by 4, they will
     * be cropped to the next largest multiple of four.
     * 
     * @param ip The image to perform a prediction on.
     * @throws ch.epfl.leb.defcon.predictors.ImageBitDepthException
     * @throws ch.epfl.leb.defcon.predictors.SessionClosedException
     */
    @Override
    public void predict(final ImageProcessor ip) throws ImageBitDepthException,
                                                      SessionClosedException {
        
        if (isClosed) {
            String msg = "Cannot call the predict() method:\n "
                       + "the TensorFlow session has been closed.";
            LOGGER.log(Level.WARNING, msg);
            throw new SessionClosedException(msg);
        }
        
        int bitDepth = ip.getBitDepth();
        switch (bitDepth) {
            case 16:
                predict(checkDimensions(ip).convertToShortProcessor());
                break;
            case 8:
                predict(checkDimensions(ip).convertToByteProcessor());
                break;
            default:
                String msg = "The predictor only works on 8 and 16-bit images.";
                LOGGER.log(Level.SEVERE, msg);
                throw new ImageBitDepthException(msg);
        }
    }
    
    /**
     * Makes a density map prediction from a 2D 16-bit image.
     * 
     * @param sp The 16-bit image to perform a prediction on.
     */
    private void predict(final ShortProcessor sp) {
        ImagePlus imp = new ImagePlus("", sp);
        int height = imp.getHeight();
        int width = imp.getWidth();
        
        // Converts the ImagePlus input to a tensorflow tensor
        Tensor<Float> inputTensor = imageToTensor(imp);

        // Make the prediction with DEFCoN
        Tensor<Float> outputTensor = tfSession.runner()
                .feed("input_tensor", inputTensor)
                .fetch("output_tensor")
                .run().get(0).expect(Float.class);

        // Transforms outputTensor into a (1,height,width,1) float array.
        float[][][][] pred = outputTensor.copyTo(
                new float[1][height][width][1]);

        // Creates a new FloatProcessor for the output density map image.
        densityMap = new FloatProcessor(width, height);

        // Assigns the pixels of the density map and sums the pixels.
        count = 0.0;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                count += pred[0][y][x][0];
                densityMap.setf(x, y, pred[0][y][x][0]);
            }
        }

    }
    
    /**
     * Failsafe in case the TensorFlow session has not been closed.
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            tfSession.close();
        } finally {
            super.finalize();
        }
    }
}
