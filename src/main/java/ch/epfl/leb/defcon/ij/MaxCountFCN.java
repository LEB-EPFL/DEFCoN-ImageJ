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

import ch.epfl.leb.defcon.predictors.internal.AbstractPredictor;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.WindowManager;

import net.imglib2.type.numeric.RealType;
import org.tensorflow.*;


public class MaxCountFCN extends AbstractPredictor implements PlugInFilter {
    private ImagePlus image;
    private ResultsTable rt;
    private Session tfSession;
    private Roi roi;

    // Runs DEFCoN on the selected image stack. It ouputs a results table with the maximum local count
    public void run(ImageProcessor ip) {
        int stack_size = image.getImageStackSize();
        roi = WindowManager.getCurrentImage().getRoi();
        Roi reshapedRoi = initRoi();

        for (int i=1; i <= stack_size; i++ ) {
            ImageProcessor proc = image.getImageStack().getProcessor(i);
            ImagePlus slice = new ImagePlus("DEFCoN", proc);

            rt.incrementCounter();
            // Make the prediction
            float prediction = predict(slice, reshapedRoi);
            // Build the results table.
            // TODO Make the (7x7) label more general. It currently depends
            // on the network using 7x7 subregions.
            rt.addValue("Max local count (7x7)", prediction);
            IJ.showProgress(i, stack_size);
        }

        rt.show("Maximum local count");
    }

    /**
     * Sets up the PlugInFilter.
     * 
     * Please see
     * 
     * for more information on the PlugInFilter API.
     * 
     * @param pathToModel The path to a saved TensorFlow model bundle.
     * @param imp The currently active image.
     * @return A flag indicating which types of images this plugin handles.
     * @see <a href="https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/PlugInFilter.html">PlugInFilter</a>
     */
    public int setup(String pathToModel, ImagePlus imp) {
        // Unlock the image
        if (imp.isLocked()) {imp.unlock();}
        image = imp;

        // Loading DEFCoN tensorflow model
        SavedModelBundle smb = SavedModelBundle.load(pathToModel, "serve");
        tfSession = smb.session();

        // Create the results table
        rt = new ResultsTable();

        // Only accepts 8bit and 16bit images.
        return DOES_8G | DOES_16;
    }

    // Gets the current ROI in ImageJ and crops it so that each dimension is divisible by 4
    private Roi initRoi() {
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
        return reshapedRoi;
    }

    // Makes a DEFCoN max local count prediction on an ImagePlus
    private <T extends RealType<T>> float predict(final ImagePlus imp, Roi reshapedRoi) {

        // Crop the image to the ROI
        imp.setRoi(reshapedRoi);
        ImagePlus impRoi = imp.crop();

        // Converts the ImagePlus input to a tensorflow tensor
        Tensor<Float> inputTensor = imageToTensor(impRoi);

        // Make the prediction with DEFCoN
        Tensor<Float> outputTensor = tfSession.runner()
                .feed("input_tensor", inputTensor)
                .fetch("output_tensor")
                .run().get(0).expect(Float.class);

        // Transforms the predicted tensor to a float scalar
        float[][] pred = outputTensor.copyTo(new float[1][1]);
        return pred[0][0];
    }
}
