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
package ch.epfl.leb.defcon.ij;

import ch.epfl.leb.defcon.predictors.Predictor;
import ch.epfl.leb.defcon.predictors.internal.DefaultPredictor;
import ch.epfl.leb.defcon.predictors.internal.SessionClosedException;
import ch.epfl.leb.defcon.predictors.internal.ImageBitDepthException;
import ch.epfl.leb.defcon.predictors.internal.UninitializedPredictorException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.WindowManager;
import ij.ImageStack;

/**
 * Computes a density map estimate for counting objects within an image.
 * 
 * @author Baptiste Ottino
 */
public class DensityCount implements PlugInFilter {
    private ImagePlus image;
    private ResultsTable rt;
    private Predictor predictor = new DefaultPredictor();
    private Roi roi;
    private ImageStack densityStack;

    /**
     * Computes a density map from the selected image stack.
     * 
     * A results table that indicates the count is displayed in addition to the
     * density map image.
     * 
     * @param ip The input image processor.
     */
    public void run(ImageProcessor ip) {
        int stackSize = image.getImageStackSize();
        roi = WindowManager.getCurrentImage().getRoi();
        Roi reshapedRoi = initRoiAndStack();
        double count;
 
        for (int i=1; i <= stackSize; i++ ) {
            ImageProcessor proc = image.getImageStack().getProcessor(i);
            ImagePlus slice = new ImagePlus("DEFCoN", proc);
            
            // Crop the image to the ROI
            slice.setRoi(reshapedRoi);

            rt.incrementCounter();
            
            // Make the density map prediction.
            try {
                predictor.predict(slice.crop().getProcessor());
                densityStack.addSlice(predictor.getDensityMap());
                count = predictor.getCount();
            } catch (ImageBitDepthException 
                     | UninitializedPredictorException
                     | SessionClosedException ex) {
                IJ.log(ex.getMessage());
                predictor.close();
                return;
            }
                        
            // Build the results table.
            rt.addValue("Rounded count", Math.round(count));
            rt.addValue("Exact count", count);
            IJ.showProgress(i, stackSize);
        }
        predictor.close();

        // Display the stack of density maps with viridis colormap
        ImagePlus densityImage = new ImagePlus("Density map", densityStack);
        IJ.run(densityImage, "mpl-viridis", "");
        densityImage.show();
        rt.show("Fluorophore count");
    }

    /**
     * Sets up the PlugInFilter.
     * 
     * @param pathToModel The path to a saved TensorFlow model bundle.
     * @param imp The currently active image.
     * @return A flag indicating which types of images this plugin handles.
     * @see <a href="https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/PlugInFilter.html">PlugInFilter</a>
     */
    public int setup(String pathToModel, ImagePlus imp) {
        // Unlocks the image.
        if (imp.isLocked()) {imp.unlock();}
        image = imp;
        predictor.setup(pathToModel);
        
        // Create the results table
        rt = new ResultsTable();

        // Only accepts 8-bit and 16-bit images.
        return DOES_8G | DOES_16;
    }

    /**
     * Gets the current ROI in ImageJ and crops it so that each dimension is divisible by 4.
     * 
     * The limitation on the size of the ROI is a requirement of DEFCoN.
     * 
     * @return The resized ROI.
     */
    private Roi initRoiAndStack() {
        int image_width = image.getWidth();
        int image_height = image.getHeight();

        if (roi == null) {
            roi = new Roi(0,0,image_width,image_height);
        }
        Roi reshapedRoi = new Roi(roi.getBounds().x,
                roi.getBounds().y,
                roi.getBounds().width - roi.getBounds().width % 4,
                roi.getBounds().height - roi.getBounds().height % 4);

        WindowManager.getCurrentImage().setRoi(reshapedRoi);
        densityStack = new ImageStack(reshapedRoi.getBounds().width,
                                      reshapedRoi.getBounds().height);
        return reshapedRoi;
    }
    
}
