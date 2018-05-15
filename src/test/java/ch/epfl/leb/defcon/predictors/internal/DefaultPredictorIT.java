/**
 * Copyright (C) 2018 Laboratory of Experimental Biophysics, Ecole
 * Polytechnique Federale de Lausanne (EPFL), Switzerland
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

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import java.io.File;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for the DefaultPredictor class.
 * 
 * @author Kyle M. Douglass
 */
public class DefaultPredictorIT {
    
    public DefaultPredictorIT() {
    }
    
    /**
     * The path to the test resources directory.
     */
    private final File RESOURCES_DIR = new File("src/test/resources");
    
    /**
     * Stack of test images.
     */
    private final File TEST_STACK = new File(RESOURCES_DIR, "test_data.tif");
    
    /**
     * Saved TensorFlow model.
     */
    private final File SAVED_MODEL = new File(RESOURCES_DIR, "tf_density_count");
    
    /**
     * The test images.
     */
    private ImagePlus imp;
    
    /**
     * The test instance.
     */
    private DefaultPredictor predictor;
    
    /**
     * Sets up the integration test.
     */
    @Before
    public void setUp() {
        imp = IJ.openImage(TEST_STACK.getAbsolutePath());
        
        predictor = new DefaultPredictor();
        predictor.setup(SAVED_MODEL.getAbsolutePath());
    }
    
    /**
     * Test of getCount method, of class DefaultPredictor.
     */
    @Test
    public void testGetCount() throws Exception {
        System.out.println("testGetCount");
        predictor.predict(imp.getProcessor());
        double count = predictor.getCount();
        predictor.close();
        
        System.out.format("Estimated count: %.4f\n", count);
        assert(count >= 0);
    }

    /**
     * Test of getDensityMap method, of class DefaultPredictor.
     */
    @Test
    public void testGetDensityMap() throws Exception {
        System.out.println("testGetDensityMap");
        predictor.predict(imp.getProcessor());
        FloatProcessor fp = predictor.getDensityMap();
        predictor.close();
        
        assertEquals(imp.getHeight(), fp.getHeight());
        assertEquals(imp.getWidth(), fp.getWidth());
    }
    
    /**
     * Test of getDensityMap method, of class DefaultPredictor.
     * 
     * This test verifies that images whose dimensions are not multiples of four
     * are automatically cropped before computing the density map.
     */
    @Test
    public void testGetDensityMapResized() throws Exception {
        System.out.println("testGetDensityMapResized");
        ImageProcessor ip = imp.getProcessor();
        
        // Reduce the size of the test data to a non-multiple of four.
        ip.setRoi(0, 0, ip.getWidth() - 1, ip.getHeight() - 1);
        
        predictor.predict(ip.crop());
        FloatProcessor fp = predictor.getDensityMap();
        predictor.close();
        
        assertEquals(imp.getWidth() - 4, fp.getWidth());
        assertEquals(imp.getHeight() - 4, fp.getHeight());
    }

    /**
     * Test of predict method, of class DefaultPredictor.
     */
    @Test
    public void testPredict() throws Exception {
        System.out.println("testPredict");
        predictor.predict(imp.getProcessor());
        predictor.close();
    }
    
}
