
To be informed about updates:

* [follow GfxTablet on Twitter](http://twitter.com/GfxTablet)
* [subscribe to our RSS/Atom feed](http://feeds.feedburner.com/GfxTablet)
* [subscribe to email notifications](http://feedburner.google.com/fb/a/mailverify?uri=GfxTablet)


What is GfxTablet?
==================

GfxTablet shall make it possible to use your Android device (especially
tablets) like a graphics tablet.

It consists of two components:

* the GfxTablet Android app
* the input driver for your PC

The GfxTablet app sends motion and touch events via UDP to a specified host
on port 40118.

The input driver must be installed on your PC. It creates a virtual "network tablet"
on your PC that is controlled by your Android device.

So, you can use your Android tablet or smartphone to control the PC and,
for instance _use GIMP with your Android tablet as a graphics tablet_
(even pressure-sensitive, if your hardware supports it).

Homepage: http://rfc2822.github.io/GfxTablet/

If you want to support this project, please consider a [donation via PayPal](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=ZT8F5NRCBDB2C&no_note=0&no_shipping=1&currency_code=EUR&item_name=GfxTablet+donation).


License
-------

GfxTablet is licensed under The MIT License.

Author: Richard Hirner

Powered by [bitfire web engineering](http://www.bitfire.at) / [gimpusers.com](http://www.gimpusers.com)


Features
--------

* Pressure sensitivity supported
* Size of canvas will be detected and sent to the client
* Option for ignoring events that are not triggered by a stylus pen:
  so you can lay your hand on the tablet and draw with the pen.


Requirements
------------

* App: Any device with Android 4.0+ and touch screen
* Driver: Linux with uinput kernel module (included in modern versions of Fedora, Ubuntu etc.)

If you use Xorg (you probably do):

* Xorg-evdev module loaded and configured
  Probably on by default, but if it doesn't work, you may need to activate the
  module: see https://github.com/rfc2822/GfxTablet/issues/7#issuecomment-13338216


Installation
============

Github repository: http://github.com/rfc2822/GfxTablet


Part 1: uinput driver
---------------------

On your PC, either download one of these binaries (don't forget to `chmod a+x` it):

* [networktablet 64-bit, dynamically linked, tested with Fedora 18 and Ubuntu 12.10](https://github.com/rfc2822/GfxTablet/blob/binaries/networktablet-x86_64?raw=true)

or compile it yourself (don't be afraid, it's only one file)

1. Clone the repository:
   `git clone git://github.com/rfc2822/GfxTablet.git`
2. Install gcc, make and linux kernel header includes (`kernel-headers` on Fedora)
3. `cd driver-uinput; make`

Then, run the binary. The driver runs in user-mode, so it doesn't need any special privileges.
However, it needs access to `/dev/uinput`. If your distribution doesn't create a group for
uinput access, you'll need to do it yourself or just run the driver as root:

`sudo ./networktablet`

Then you should see a status message saying the driver is ready. If you do `xinput list` in a separate
terminal, should show a "Network Tablet" device.

You can start and stop (Ctrl+C) the Network Tablet at any time, but please be aware that applications
which use the device may be confused by that and could crash.

`networktablet` will display a status line for every touch/motion event it receives.


Part 2: App
-----------

You can either

1. compile the app from the source code in the Git repository, or
2. [download it from the open-source market F-Droid](https://f-droid.org/repository/browse/?fdcategory=Multimedia&fdid=at.bitfire.gfxtablet) (**recommended**), or
3. [download it from Samsung Apps](http://apps.samsung.com/earth/topApps/topAppsDetail.as?productId=000000662994)
4. [download it from Google Play](https://play.google.com/store/apps/details?id=at.bitfire.gfxtablet)

After installing, enter your host IP in the Settings / Host name and it should be ready.


Part 3: Use it
--------------

Now you can use your tablet as an input device in every Linux application (including X.org
applications). For instance, when networktablet is running, GIMP should have a "Network Tablet"
entry in "Edit / Input Devices". Set its mode to "Screen" and it's ready to use.


Support
=======

For bug reports, please use the [Github issues page](https://github.com/rfc2822/GfxTablet/issues)
or just fork the repository, fix the bug and send a merge request.
