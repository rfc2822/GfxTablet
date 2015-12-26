
To be informed about updates:

* [follow GfxTablet on Twitter](https://twitter.com/GfxTablet)
* [GfxTablet forum](https://forums.bitfire.at/category/3/gfxtablet)


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

Homepage: https://gfxtablet.bitfire.at

Help and discussion: https://forums.bitfire.at/category/3/gfxtablet

If you want to support this project, please consider a [donation](https://gfxtablet.bitfire.at/donate).


License
-------

GfxTablet is licensed under The MIT License.

Author: Ricki Hirner / powered by [bitfire web engineering](https://www.bitfire.at) / [gimpusers.com](http://www.gimpusers.com)


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

* Xorg-evdev module loaded and configured – probably on by default, but if it doesn't work, you may
  need to [activate the module](https://forums.bitfire.at/topic/15/gfxtablet-and-archlinux).


Installation
============

Github repository: https://github.com/rfc2822/GfxTablet


Part 1: uinput driver
---------------------

On your PC, either download one of these binaries (don't forget to `chmod a+x` it):

* [networktablet 64-bit, dynamically linked, tested with Fedora 23](https://github.com/rfc2822/GfxTablet/releases/download/android-app-1.4/networktablet)

or compile it yourself (don't be afraid, it's only one file)

1. Clone the repository:
   `git clone git://github.com/rfc2822/GfxTablet.git`
2. Install gcc, make and linux kernel header includes (`kernel-headers` on Fedora)
3. `cd GfxTablet/driver-uinput; make`

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
2. [download it from the open-source market F-Droid](https://f-droid.org/repository/browse/?fdcategory=Multimedia&fdid=at.bitfire.gfxtablet), or
3. download it from Samsung Galaxy Apps, or
4. [download it directly from Github](https://github.com/rfc2822/GfxTablet/releases), or
5. [download it from Google Play](https://play.google.com/store/apps/details?id=at.bitfire.gfxtablet)

After installing, enter your host IP in the Settings / Host name and it should be ready.


Part 3: Use it
--------------

Now you can use your tablet as an input device in every Linux application (including X.org
applications). For instance, when networktablet is running, GIMP should have a "Network Tablet"
entry in "Edit / Input Devices". Set its mode to "Screen" and it's ready to use.


Frequently Asked Questions
==========================

Using with multiple monitors
----------------------------

If you're using multiple screens, you can assign the Network Tablet device to a specific screen
once it's running (thanks to @symbally and @Evi1M4chine, see https://forums.bitfire.at/topic/82/multi-monitor-problem):

1. Use `xrandr` to identify which monitor you would like to have the stylus picked up on. In this example, `DVI-I-1`
   is the display to assign.
2. Do `xinput map-to-output "$( xinput list --id-only "Network Tablet" )" DVI-I-1`.

Known issues
------------

* With Gnome 3.16 (as shipped with Fedora 22), [Gnome Shell crashes when using GfxTablet](https://bugzilla.redhat.com/show_bug.cgi?id=1209008).


Donate
------

If you find GfxTablet useful, please feel free to [send a donation](https://gfxtablet.bitfire.at/donate).

[![Flattr GfxTablet](https://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=bitfire&url=https://gfxtablet.bitfire.at&title=GfxTablet&category=software)
