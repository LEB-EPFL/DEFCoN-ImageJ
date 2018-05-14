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

import ch.epfl.leb.defcon.utils.GraphBuilder;

import ij.ImagePlus;
import net.imagej.tensorflow.Tensors;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 * A base implementation for DEFCoN predictors.
 * 
 * A predictor is a class that takes an image(s) as input and estimates a
 * density map of object locations from it.
 * 
 * @author Kyle M. Douglass
 */
public abstract class AbstractPredictor {
    
    /**
     * A copy of the current TensorFlow session.
     */
    protected Session tfSession;
    
    /**
     * Has the TensorFlow session been closed?
     */
    protected boolean isClosed = false;
    
    /**
     * Closes resources associated with this predictor.
     */
    public void close() {
        tfSession.close();
        isClosed = true;
    }
    
    /**
     * Initializes the predictor.
     * 
     * @param pathToModel The path to a saved TensorFlow model bundle.
     */
    public void setup(String pathToModel) {
        // Loads a DEFCoN tensorflow model.
        SavedModelBundle smb = SavedModelBundle.load(pathToModel, "serve");
        tfSession = smb.session();

    }
    
    /**
     * Converts an ImageJ image to a TensorFlow tensor.
     * 
     * The original image is preserved.
     * 
     * @param imp The image to convert.
     * @return A tensor representing the data in the original image.
     */
    protected static Tensor<Float> imageToTensor(final ImagePlus imp) {

        // Convert to float Img (imglib2)
        Img<FloatType> img = ImageJFunctions.convertFloat(imp);
        // Normalizes values between 0 and 1 (for int16).
        // This is not that important since DEFCoN normalizes input directly.
        // Img<FloatType> img_divided = divide(img, 65535);

        // Creates a tensorflow tensor from the Img.
        Tensor<Float> imageTensor = Tensors.tensorFloat(img);

        // Adds two dimensions to imageTensor for consistency with the
        // TensorFlow input layer shape.
        // (imageTensor) = (height, width) -> (1, height, width, 1)
        Graph graph = new Graph();

        Output imageTensorOutput = graph.opBuilder("Const", "tensor_image")
                .setAttr("dtype", imageTensor.dataType())
                .setAttr("value", imageTensor)
                .build().output(0);
        Output<Float> expandedTensorOutput = GraphBuilder.expandDims(
                graph,
                "dim-1",
                GraphBuilder.expandDims(graph, "dim0", imageTensorOutput,
                    GraphBuilder.constant(graph, "make_batch", 0)),
                GraphBuilder.constant(graph, "make_channel", -1));

        Session s0 = new Session(graph);
        Tensor<Float> inputTensor = s0.runner()
                                      .fetch(expandedTensorOutput
                                      .op().name()).run().get(0)
                                      .expect(Float.class);

        return inputTensor;
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
