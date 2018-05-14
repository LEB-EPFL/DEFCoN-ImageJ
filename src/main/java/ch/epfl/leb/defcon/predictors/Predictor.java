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

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Makes density map predictions from images.
 * 
 * @author Kyle M. Douglass
 */
public interface Predictor {
    
    /**
     * Returns the most recent count.
     * 
     * @return The predicted count from the density map.
     */
    public double getCount();
    
    /**
     * Returns the most recently calculated density map prediction.
     * 
     * @return The predicted density map.
     */
    public FloatProcessor getDensityMap();
    
    /**
     * Makes a density map prediction from a 2D image.
     * 
     * If the ImagePlus has more than one image, only the currently 
     * 
     * @param ip The image to perform a prediction on.
     */
    public void predict(final ImageProcessor ip);
    
    /**
     * Initializes the predictor with a saved TensorFlow model bundle.
     * 
     * @param pathToModel The path to a saved TensorFlow model bundle.
     */
    public void setup(String pathToModel);
    
}
