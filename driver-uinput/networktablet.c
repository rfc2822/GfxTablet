// networktablet.c -- GfxTablet network manager daemon.
//
// This code has been modified from it's original source on
// GitHub -- https://github.com/edtrind/GfxTablet-master
// Author:  bitfire web engineering/GfxTablet -- Richard Hirner
// http://rfc2822.github.io/GfxTablet/
//
// Modified by quadcore-dev/loopyd - Robert Smith
// lupinedreamexpress@gmail.com
//
// This version provides higher compatability for GIMP (by
// converting the output values to larger ones so that the
// pressure curve function becomes usable in "Input Settings"
// -> "Network Tablet".
//
// It also contains some eyecandy, a visible pressure bar that
// indicates how hard your last stroke was.  It does not spam
// your terminal window with the events, but will exit with
// an error code if there is a problem.
//
// This code was tested on Ubuntu 14.04 LTS Studio and is
// verified working on this platform.
//
// I am not responsible if my code causes your GfxTablet
// daemon to stop functioning, your computer to catch fire,
// and your pets to die, etc.
//
// Have a wonderful life!
//
// ~quadcore-dev/loopyd
// "We write code because we like to."

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <arpa/inet.h>
#include <linux/input.h>
#include <linux/uinput.h>
#include "protocol.h"
#include "qccolor.h"

#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}

int udp_socket;

void init_device(int fd)
{
	struct uinput_user_dev uidev;

	// 1 button
	if (ioctl(fd, UI_SET_EVBIT, EV_KEY) < 0)
		die("error: ioctl UI_SET_EVBIT EV_KEY");
	if (ioctl(fd, UI_SET_KEYBIT, BTN_TOUCH) < 0)
		die("error: ioctl UI_SET_KEYBIT");

	// 2 main axes + pressure (absolute positioning)
	if (ioctl(fd, UI_SET_EVBIT, EV_ABS) < 0)
		die("error: ioctl UI_SET_EVBIT EV_ABS");
	if (ioctl(fd, UI_SET_ABSBIT, ABS_X) < 0)
		die("error: ioctl UI_SETEVBIT ABS_X");
	if (ioctl(fd, UI_SET_ABSBIT, ABS_Y) < 0)
		die("error: ioctl UI_SETEVBIT ABS_Y");
	if (ioctl(fd, UI_SET_ABSBIT, ABS_PRESSURE) < 0)
		die("error: ioctl UI_SETEVBIT ABS_PRESSURE");

	memset(&uidev, 0, sizeof(uidev));
	snprintf(uidev.name, UINPUT_MAX_NAME_SIZE, "Network Tablet");
	uidev.id.bustype = BUS_VIRTUAL;
	uidev.id.vendor  = 0x1;
	uidev.id.product = 0x1;
	uidev.id.version = 1;
	uidev.absmin[ABS_X] = 0;
	uidev.absmax[ABS_X] = SHRT_MAX;
	uidev.absmin[ABS_Y] = 0;
	uidev.absmax[ABS_Y] = SHRT_MAX;
	uidev.absmin[ABS_PRESSURE] = 0;
	uidev.absmax[ABS_PRESSURE] = SHRT_MAX/2;
	if (write(fd, &uidev, sizeof(uidev)) < 0)
		die("error: write");

	if (ioctl(fd, UI_DEV_CREATE) < 0)
		die("error: ioctl");
}

int prepare_socket()
{
	int s;
	struct sockaddr_in addr;

	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
		die("error: prepare_socket()");

	bzero(&addr, sizeof(struct sockaddr_in));
	addr.sin_family = AF_INET;
	addr.sin_port = htons(GFXTABLET_PORT);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(s, (struct sockaddr *)&addr, sizeof(addr)) == -1)
		die("error: prepare_socket()");

	return s;
}

void send_event(int device, int type, int code, int value)
{
	struct input_event ev;
	ev.type = type;
	ev.code = code;
	ev.value = value;
	if (write(device, &ev, sizeof(ev)) < 0)
		error("error: write()");
}

void quit(int signal) {
	close(udp_socket);
}

int main(void)
{
	int device;
	struct event_packet ev_pkt;

	if ((device = open("/dev/uinput", O_WRONLY | O_NONBLOCK)) < 0)
		die("error: open");

	init_device(device);
	udp_socket = prepare_socket();

	cls_screen();
	textcolor(RESET, WHITE, BLACK);
	textcolor(BRIGHT, GREEN, BLACK);
	printf("GfxTablet driver (protocol version %u) is ready and listening on:\n", PROTOCOL_VERSION);
	printf("\t0.0.0.0:%u (UDP)\n\n", GFXTABLET_PORT);
	textcolor(BRIGHT, RED, BLACK);
	printf("Hint: Make sure that this port is not blocked by your firewall!\n");
	textcolor(BRIGHT, WHITE, BLACK);
	printf("FIX applied by quadcore-dev/loopyd\nRobert Smith -- lupinedreamexpress@gmail.com.\n");
	textcolor(DIM, WHITE, BLACK);
	printf("For GIMP Pressure curve + eyecandy pressure bar!\n\n");

	signal(SIGINT, quit);
	signal(SIGTERM, quit);

	long fixed_pressure = 0L;
	short progj = 0L;

	while (recv(udp_socket, &ev_pkt, sizeof(ev_pkt), 0) >= 9) {		// every packet has at least 9 bytes
		printf("."); fflush(0);

		if (memcmp(ev_pkt.signature, "GfxTablet", 9) != 0) {
			fprintf(stderr, "\nGot unknown packet on port %i, ignoring\n", GFXTABLET_PORT);
			continue;
		}
		ev_pkt.version = ntohs(ev_pkt.version);
		if (ev_pkt.version != PROTOCOL_VERSION) {
			fprintf(stderr, "\nGfxTablet app speaks protocol version %i but driver speaks version %i, please update\n",
				ev_pkt.version, PROTOCOL_VERSION);
			break;
		}

		ev_pkt.x = ntohs(ev_pkt.x);
		ev_pkt.y = ntohs(ev_pkt.y);
		
		// FIX:  More precise pressure value for GIMP
		fixed_pressure = (long) ((float) (LONG_MAX / SHRT_MAX) * (float) ntohs(ev_pkt.pressure));
		ev_pkt.pressure = fixed_pressure;

		// Calculate and draw our pressure bar.
		progj = (short) (((float) fixed_pressure / (float) LONG_MAX) * 64.00f);
		textcolor(RESET, WHITE, BLACK);
		printf("A1 (X): %hi, A2 (Y): %hi, \t A3 (Pressure):  ", ev_pkt.x, ev_pkt.y);
		progbar(progj, 16);
		textcolor(RESET, WHITE, BLACK);
		printf("       \r");

		// Send the events to the app.
		send_event(device, EV_ABS, ABS_X, ev_pkt.x);
		send_event(device, EV_ABS, ABS_Y, ev_pkt.y);
		send_event(device, EV_ABS, ABS_PRESSURE, ev_pkt.pressure);

		switch (ev_pkt.type) {
			case EVENT_TYPE_MOTION:
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;
			case EVENT_TYPE_BUTTON:
				if (ev_pkt.button == 0)
					send_event(device, EV_KEY, BTN_TOUCH, ev_pkt.down);
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;

		}
	}
	close(udp_socket);

	textcolor(RESET, WHITE, BLACK);
	printf("\nRemoving network tablet from device list\n");
	ioctl(device, UI_DEV_DESTROY);
	close(device);

	printf("GfxTablet driver shut down gracefully\n");
	return 0;
}
