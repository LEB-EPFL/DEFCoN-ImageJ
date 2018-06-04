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
package ch.epfl.leb.defcon.predictors;


import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

/**
 * Makes density map predictions from images.
 * 
 * @author Kyle M. Douglass
 */
public interface Predictor {
    
    /**
     * Closes resources associated with this predictor.
     */
    public void close();
    
    /**
     * Returns the most recent count.
     * 
     * @return The predicted count from the density map.
     * @throws ch.epfl.leb.defcon.predictors.internal.UninitializedPredictorException
     */
    public double getCount() throws UninitializedPredictorException;
    
    /**
     * Returns the most recently calculated density map prediction.
     * 
     * @return The predicted density map.
     * @throws ch.epfl.leb.defcon.predictors.internal.UninitializedPredictorException
     */
    public FloatProcessor getDensityMap() throws UninitializedPredictorException;
    
    /**
     * Returns the most recently-calculated local count map.
     * 
     * @return The most recently calculated local count map.
     * @throws ch.epfl.leb.defcon.predictors.NoLocalCountMapException
     */
    public FloatProcessor getLocalCountMap() throws NoLocalCountMapException;
    
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
    public double getMaximumLocalCount(int boxSize)
           throws UninitializedPredictorException;
    
    /**
     * Makes a density map prediction from a 2D  image.
     * 
     * @param ip The image to perform a prediction on.
     */
    public void predict(final ImageProcessor ip) throws ImageBitDepthException,
                                                      SessionClosedException;
    
    /**
     * Initializes the predictor with a saved TensorFlow model bundle.
     * 
     * @param pathToModel The path to a saved TensorFlow model bundle.
     */
    public void setup(String pathToModel);
    
}
