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

import ch.epfl.leb.defcon.predictors.Predictor;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import ij.process.ImageProcessor;
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
     * Returns the most recent count.
     * 
     * @return The predicted count from the density map.
     * @throws ch.epfl.leb.defcon.predictors.internal.UninitializedPredictorException 
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
     * @throws ch.epfl.leb.defcon.predictors.internal.UninitializedPredictorException
     */
    public FloatProcessor getDensityMap() throws UninitializedPredictorException {
        if (densityMap == null) {
            String msg = "The Predictor has not yet performed any calcuations.";
            LOGGER.log(Level.WARNING, msg);
            throw new UninitializedPredictorException(msg);
        }
        return densityMap;
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
     * @param ip The image to perform a prediction on.
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
                predict(ip.convertToShortProcessor());
                break;
            case 8:
                predict(ip.convertToByteProcessor());
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
