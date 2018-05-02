package ch.epfl.leb.defcon.ij.gui;

/*
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

import ch.epfl.leb.defcon.ij.DensityCount;
import ij.IJ;
import ij.Prefs;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;

/**
 * Launches the density count DEFCoN plugin.
 * 
 * @author Kyle M. Douglass
 */
public class RunDensityCount implements PlugIn {
    
    /**
     * The name of the key for the path to the DEFCoN density count model.
     */
    private static final String PATHKEY = "defcon.model.density";
    
    /**
     * The default path to the density count model when there is none saved.
     */
    private static final String DEFAULTPATH = "/path/to/tf_density_count";
    
    public void run(String arg) {
        GenericDialog gd = new GenericDialog("DEFCoN Setup: Density Count");
        gd.addMessage("Please specify the path to the saved DEFCoN density " +
                      "map model.");
        
        // Loads the previously used path.
        String pathPref = Prefs.get(PATHKEY, DEFAULTPATH);
        
        // Display the GUI dialog requesting the model path.
        gd.addStringField( "path", pathPref, 64);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Computes this path for later.
        String path = gd.getNextString();
        Prefs.set(PATHKEY, path);

        // Run the density count.
        ImagePlus imp = IJ.getImage();
        DensityCount dc = new DensityCount();
        dc.setup(path, imp);

        ImageProcessor ip = imp.getProcessor();
        dc.run(ip);
    }
    
}
