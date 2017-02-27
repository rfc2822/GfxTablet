
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/X.h>
#include <cairo.h>
#include <cairo-xlib.h>
#include <pthread.h>
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
#include <stdint.h>
#include "protocol.h"

#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}


int udp_socket, sending;

typedef struct event_packet event_packet;
typedef struct sockaddr_in sockaddr_in;

typedef struct sending_t {
	sockaddr_in from;
	int slen;
} sending_t;


void init_device(int fd)
{
	struct uinput_user_dev uidev;

	// enable synchronization
	if (ioctl(fd, UI_SET_EVBIT, EV_SYN) < 0)
		die("error: ioctl UI_SET_EVBIT EV_SYN");

	// enable 1 button
	if (ioctl(fd, UI_SET_EVBIT, EV_KEY) < 0)
		die("error: ioctl UI_SET_EVBIT EV_KEY");
	if (ioctl(fd, UI_SET_KEYBIT, BTN_TOUCH) < 0)
		die("error: ioctl UI_SET_KEYBIT");
	if (ioctl(fd, UI_SET_KEYBIT, BTN_TOOL_PEN) < 0)
		die("error: ioctl UI_SET_KEYBIT");
	if (ioctl(fd, UI_SET_KEYBIT, BTN_STYLUS) < 0)
		die("error: ioctl UI_SET_KEYBIT");
	if (ioctl(fd, UI_SET_KEYBIT, BTN_STYLUS2) < 0)
		die("error: ioctl UI_SET_KEYBIT");

	// enable 2 main axes + pressure (absolute positioning)
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
	uidev.absmax[ABS_X] = UINT16_MAX;
	uidev.absmin[ABS_Y] = 0;
	uidev.absmax[ABS_Y] = UINT16_MAX;
	uidev.absmin[ABS_PRESSURE] = 0;
	uidev.absmax[ABS_PRESSURE] = INT16_MAX;
	if (write(fd, &uidev, sizeof(uidev)) < 0)
		die("error: write");

	if (ioctl(fd, UI_DEV_CREATE) < 0)
		die("error: ioctl");
}

int prepare_socket()
{
	int s;
	sockaddr_in addr;

	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
		die("error: prepare_socket()");

	bzero(&addr, sizeof(sockaddr_in));
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
		die("error: write()");
}

void quit(int signal) {
	close(udp_socket);
}

void msleep(int ms){
	struct timespec req = {0};
	req.tv_sec = 0;
	req.tv_nsec = ms * 1000000L;
	nanosleep(&req, (struct timespec *)NULL);
}

void *send_current_screen(void *arg){
	sending_t *args = (sending_t*) arg;
	sockaddr_in from = args->from;
	int slen = args->slen;
	Display *disp = XOpenDisplay(":0");

	if (sending || !disp) {
		return NULL;
	}
	printf("\nsend_thread\n");

	msleep(300);
	sending=1;
	Window root;
	cairo_surface_t *surface;
	int scr;
	scr = DefaultScreen(disp);
	root = DefaultRootWindow(disp);
	/* get the root surface on given display */
	surface = cairo_xlib_surface_create(disp, root, DefaultVisual(disp, scr),
													DisplayWidth(disp, scr),
													DisplayHeight(disp, scr));
	/* right now, the tool only outputs PNG images */
	cairo_surface_write_to_png( surface, "test.png" );
	/* free the memory*/
	cairo_surface_destroy(surface);

	FILE *istream;
	if ( (istream = fopen("test.png", "r" ) ) == NULL ){
		die("file non-existant!");
	}
	from.sin_port = htons(GFXTABLET_PORT);
	int max=60000;
	char buff[max+30];
	int n=1;
	while(fread(buff,sizeof(char),max,istream) != 0){
		printf("Send packet to %s:%d\n", inet_ntoa(from.sin_addr), ntohs(from.sin_port));
		buff[max +29] = n;
		if (sendto(udp_socket, buff, sizeof(buff), 0, (struct sockaddr*) &from, slen) == -1){
			die("sendto()");
		}
		n++;
		for (int r = 0; r <= max ; r++){
			buff[r] = 0;
		}
	}
	fclose (istream );
	char buf[1];
	buf[0] = n-1;
	if (sendto(udp_socket, buf, strlen(buff)+1, 0, (struct sockaddr*) &from, slen) == -1){
		die("sendto()");
	}
	sending=0;
}

int main(void){
	int device;
	sending=0;
	event_packet ev_pkt;
	sending_t sock_t;
	pthread_t ev_retreive_t, screen_send_t;

	if ((device = open("/dev/uinput", O_WRONLY | O_NONBLOCK)) < 0)
		die("error: open");

	init_device(device);
	udp_socket = prepare_socket();

	printf("GfxTablet driver (protocol version %u) is ready and listening on 0.0.0.0:%u (UDP)\n"
		"Hint: Make sure that this port is not blocked by your firewall.\n", PROTOCOL_VERSION, GFXTABLET_PORT);

	signal(SIGINT, quit);
	signal(SIGTERM, quit);

	while (recvfrom(udp_socket, &ev_pkt, sizeof(event_packet), 0, (struct sockaddr *) &sock_t.from, &sock_t.slen) >= 9) {		// every packet has at least 9 bytes
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
		printf("x: %hu, y: %hu, pressure: %hu\n", ev_pkt.x, ev_pkt.y, ev_pkt.pressure);

		send_event(device, EV_ABS, ABS_X, ev_pkt.x);
		send_event(device, EV_ABS, ABS_Y, ev_pkt.y);
		send_event(device, EV_ABS, ABS_PRESSURE, ev_pkt.pressure);

		switch (ev_pkt.type) {
			case EVENT_TYPE_MOTION:
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;
			case EVENT_TYPE_BUTTON:
				// stylus hovering
				if (ev_pkt.button == -1)
					send_event(device, EV_KEY, BTN_TOOL_PEN, ev_pkt.down);
				// stylus touching
				if (ev_pkt.button == 0)
					send_event(device, EV_KEY, BTN_TOUCH, ev_pkt.down);
				// button 1
				if (ev_pkt.button == 1)
					send_event(device, EV_KEY, BTN_STYLUS, ev_pkt.down);
				// button 2
				if (ev_pkt.button == 2)
					send_event(device, EV_KEY, BTN_STYLUS2, ev_pkt.down);
				printf("sent button: %hhi, %hhu\n", ev_pkt.button, ev_pkt.down);
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;
		}

		if (ev_pkt.pressure == 0 && memcmp(inet_ntoa(sock_t.from.sin_addr), "0.0.0.0", 7) != 0) {
			if(pthread_create(&screen_send_t, NULL, send_current_screen, &sock_t)) {
				fprintf(stderr, "Error creating thread\n");
				return 1;
			}
		}
	}
	close(udp_socket);

	printf("Removing network tablet from device list\n");
	ioctl(device, UI_DEV_DESTROY);
	close(device);

	printf("GfxTablet driver shut down gracefully\n");
	return 0;
}
