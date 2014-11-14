
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
#include <X11/Xlib.h>
#include <dirent.h>
#include "protocol.h"
#include <X11/extensions/Xrandr.h>
#include <getopt.h>
#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}


int udp_socket;
// parse args
int screenId = -1;
int width    = -1;
int height   = -1;
int xOffset  = 0;
int yOffset  = 0;

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

gfx_host_monitor* getMonitors() {
	unsigned size = sizeof(gfx_host_monitor);
	Display *display = XOpenDisplay(0);
	Window root = RootWindow(display, 0);

	XRRScreenResources* screenResources = XRRGetScreenResources(display, root);
	RROutput primary = XRRGetOutputPrimary(display, root);

	int screenCount = 0;
	gfx_host_monitor* monitors = malloc(size);
	for (int i = 0; i < screenResources->ncrtc; i++) {
		XRRCrtcInfo* crtcInfo = XRRGetCrtcInfo(display, screenResources, screenResources->crtcs[i]);

		for (int j = 0; j < crtcInfo->noutput; j++) {
			XRROutputInfo* outputInfo = XRRGetOutputInfo(display, screenResources, crtcInfo->outputs[j]);

			if (outputInfo->connection != RR_Connected) {
				XRRFreeOutputInfo(outputInfo);
				continue;
			}

			size += sizeof(gfx_host_monitor);
			gfx_host_monitor* rallac = realloc(monitors, size);
			if (rallac) {
				monitors = rallac;
				if (screenCount > 0) {
					monitors[i].x = crtcInfo->x + monitors[i - 1].x;
					monitors[i].y = crtcInfo->y + monitors[i - 1].x;
				} else {
					monitors[i].x = crtcInfo->x;
					monitors[i].y = crtcInfo->y;
				}
				monitors[i].width = crtcInfo->width;
				monitors[i].height = crtcInfo->height;
				monitors[i].mm_width = outputInfo->mm_width;
				monitors[i].mm_height = outputInfo->mm_height;
			} else {
				// 
			}

			XRRFreeOutputInfo(outputInfo);
			screenCount++;
		}
		XRRFreeCrtcInfo(crtcInfo);
	}

	XRRFreeScreenResources(screenResources);
	for (unsigned i = 0; i < screenCount; i++) {
		printf("Monitor %d: w:%d h:%d x:%d y:%d\n", i, monitors[i].width, monitors[i].height, monitors[i].x, monitors[i].y);
	}

	return monitors;
}

int main(int argc, char *argv[])
{
	Display *display = XOpenDisplay(NULL);
 	Window root = RootWindow(display, 0);
 	int totalWidth = width = XDisplayWidth(display, 0);
 	int totalHeight = height = XDisplayHeight(display, 0);

 	gfx_host_monitor* monitors = getMonitors();
  	printf("totalWidth %d\n", totalWidth);

	int opt;
	while ((opt = getopt(argc, argv, "s:w:h:x:y:")) != -1) {
		switch (opt) {
		case 's': screenId = atoi(optarg); width = monitors[screenId].width; height = monitors[screenId].height; break;
		case 'w': width = atoi(optarg); break;
		case 'h': height = atoi(optarg); break;
		case 'x': xOffset = atoi(optarg); break;
		case 'y': yOffset = atoi(optarg); break;
		default:
			fprintf(stderr, "Usage: %s [-s=SCREEN_NO -w=WIDTH -h=HEIGHT -x=X_OFFSET -y=Y-OFFSET]\n", argv[0]);
			exit(EXIT_FAILURE);
		}
	}

	int device;
	struct event_packet ev_pkt;

	if ((device = open("/dev/uinput", O_WRONLY | O_NONBLOCK)) < 0)
		die("error: open");

	init_device(device);
	udp_socket = prepare_socket();

	printf("GfxTablet driver (protocol version %u) is ready and listening on 0.0.0.0:%u (UDP)\n"
		"Hint: Make sure that this port is not blocked by your firewall.\n", PROTOCOL_VERSION, GFXTABLET_PORT);

	signal(SIGINT, quit);
	signal(SIGTERM, quit);

	

	if (screenId != -1) {
		
		xOffset = monitors[screenId].x + xOffset;
		yOffset = monitors[screenId].y + yOffset;
	}

	//print_screen_information();
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
		ev_pkt.pressure = ntohs(ev_pkt.pressure);
		printf("x: %hi, y: %hi, pressure: %hi\n", ev_pkt.x, ev_pkt.y, ev_pkt.pressure);

		ev_pkt.x = (short) ((float) ev_pkt.x * (float) totalWidth / (float) SHRT_MAX);
		ev_pkt.x = (short) ((float) ev_pkt.x * (float) width / (float) totalWidth);
		ev_pkt.x = (short) ((float) SHRT_MAX / totalWidth * (xOffset + ev_pkt.x));
		ev_pkt.y = (short) ((float) ev_pkt.y * (float) totalHeight / SHRT_MAX);
		ev_pkt.y = (short) ((float) ev_pkt.y * (float) height / totalHeight);
		ev_pkt.y = (short) ((float) SHRT_MAX / totalHeight * (yOffset + ev_pkt.y));
		send_event(device, EV_ABS, ABS_X, ev_pkt.x);
		send_event(device, EV_ABS, ABS_Y, ev_pkt.y);

		//send_event(device, EV_ABS, ABS_PRESSURE, ev_pkt.pressure);

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

	printf("Removing network tablet from device list\n");
	ioctl(device, UI_DEV_DESTROY);
	close(device);

	printf("GfxTablet driver shut down gracefully\n");
	return 0;
}
