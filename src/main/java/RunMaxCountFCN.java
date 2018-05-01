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

import ij.IJ;
import ij.Prefs;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;

/**
 * Launches the maximum local count DEFCoN plugin.
 * 
 * @author Kyle M. Douglass
 */
public class RunMaxCountFCN implements PlugIn {
    
    /**
     * The name of the key for the path to the DEFCoN density count model.
     */
    private static final String PATHKEY = "defcon.model.maxCount";
    
    /**
     * The default path to the density count model when there is none saved.
     */
    private static final String DEFAULTPATH = "/path/to/tf_max_count";
    
    public void run(String arg) {
        GenericDialog gd = new GenericDialog("DEFCoN Setup: Density Count");
        gd.addMessage("Please specify the path to the saved DEFCoN maximum " +
                      "local model.");
        
        // Loads the previously used path.
        String pathPref = Prefs.get(PATHKEY, DEFAULTPATH);
        
        // Displays the GUI dialog requesting the model path.
        gd.addStringField( "path", pathPref, 128);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Remembers this path for later.
        String path = gd.getNextString();
        Prefs.set(PATHKEY, path);

        // Computes the maximum local count.
        ImagePlus imp = IJ.getImage();
        MaxCountFCN mc = new MaxCountFCN();
        mc.setup(path, imp);

        ImageProcessor ip = imp.getProcessor();
        mc.run(ip);
    }
}

