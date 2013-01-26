
XorgTablet
==========

XorgTablet is an Android app that sends motion and touch events
via UDP to a specified host on port 40117.

It is especially useful in combination with the xf86-networktablet
X.org input driver (https://github.com/rfc2822/xf86-networktablet)
that allows these touch events to be processed by the X server. So,
you can use your Android tablet or smartphone to control the X
server and, for instance *use GIMP with your Android tablet
as a graphics tablet* (even pressure-sensitive, if your hardware
supports it).


Requirements
------------

Any device with Android 4.0+ and touch screen


Download
--------

You can find the latest binary on SourceForge:
https://sourceforge.net/projects/xorgtablet/files/XorgTablet/


Features
--------

* Pressure sensitive
* Size of canvas will be detected and sent to the client
* Option for ignoring events that are not triggered by a stylus pen:
  so you can lay your hand on the tablet and draw with the pen.


Technical details
-----------------

The used protocol:
https://github.com/rfc2822/xf86-networktablet/blob/master/protocol.h
