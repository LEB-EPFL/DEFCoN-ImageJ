# DEFCoN-ImageJ

- [![Build Status](https://travis-ci.org/LEB-EPFL/DEFCoN-ImageJ.svg?branch=master)](https://travis-ci.org/LEB-EPFL/DEFCoN-ImageJ)

This is the ImageJ plugin for the [density estimation by fully
convolutional networks (DEFCoN)](https://github.com/LEB-EPFL/DEFCoN)
algorithm, an image processing tool for fluorescence spot
counting. With this plugin, you can use trained DEFCoN models directly
on images from within ImageJ.

![](docs/_images/density_map.png)

# Quickstart (Fiji)

0. Download an already trained density map network model from the
   [DEFCoN-ImageJ
   wiki](https://github.com/LEB-EPFL/DEFCoN-ImageJ/wiki). Unzip its
   contents to any directory you wish.
1. Make a backup of your Fiji folder. (This is always a good idea
   before adding an [update
   site](https://imagej.net/List_of_update_sites).)
2. Open Fiji and navigate to *Help > Update...*. Install any updates
   and restart Fiji if necessary.
3. In the ImageJ Update dialog, click the *Manage update sites*
   button, scroll to the bottom of the list, and add
   http://sites.imagej.net/Kmdouglass under the URL column. You may
   give it any name you want, such as LEB-EPFL.
4. Install all the updates and restart Fiji.
5. Open an image or stack of images that contains fluorescent spots.
6. Navigate to *Plugins > DEFCoN* and select *Density map...*.
7. Specify the location of the folder containing the DEFCoN density
   map network model that you downloaded and unzipped in the first
   step. This will be a folder (not a file!) that contains a
   TensorFlow SavedModelBundle and is generated by training the
   [DEFCoN](https://github.com/LEB-EPFL/DEFCoN) network.
8. Click OK to compute a density map estimate from the images.

For more detailed instructions, please see the documentation.

# Documentation

http://defcon-imagej.readthedocs.io/en/latest/

# Acknowledgements

- [ImageJ](http://imagej.net/ImageJ2)
- [Fiji](http://fiji.sc/)
- [ImgLib2](http://imglib2.net/)
- [SciJava](http://scijava.org/)
- [Tensorflow](https://www.tensorflow.org/)


