This directory contains plugins for the ImageJ image processing system.
See http://rsb.info.nih.gov/ij/

CB: 2009.05.27:
This plugin is used by the ENM workflows.  To get this to work, you must put the plugins directory in the root of kepler.  The imagej actor will only look for plugins in this location.  Since it is not possible to automate this for the build, this will have to be an installer task.

If the plugins directory is not in the right place, you will get the error message "ASC TextReader not found" when you run the ENM workflows.
