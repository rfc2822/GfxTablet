
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <arpa/inet.h>
#include <linux/input.h>
#include <linux/uinput.h>
#include "protocol.h"

#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}


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
	uidev.absmax[ABS_X] = INT_MAX;
	uidev.absmin[ABS_Y] = 0;
	uidev.absmax[ABS_Y] = INT_MAX;
	uidev.absmin[ABS_PRESSURE] = 0;
	uidev.absmax[ABS_PRESSURE] = 10000;	// 10,000 instead of 32,767 because sometimes there is pressure >1.0
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
	addr.sin_port = htons(NETWORKTABLET_PORT);
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


int main(void)
{
	int device, socket;
	struct event_packet ev_pkt;
	int x, y, btn_down = 0;
	int max_x = INT_MAX, max_y = INT_MAX;

	if ((device = open("/dev/uinput", O_WRONLY | O_NONBLOCK)) < 0)
		die("error: open");

	init_device(device);
	socket = prepare_socket();

	while (recv(socket, &ev_pkt, sizeof(ev_pkt), 0) >= 7) {		// every packet has at least 7 bytes
		printf("."); fflush(0);

		ev_pkt.x = ntohs(ev_pkt.x);
		ev_pkt.y = ntohs(ev_pkt.y);
		ev_pkt.pressure = ntohs(ev_pkt.pressure);
		//printf("x: %hi, y: %hi, pressure: %hi\n", ev_pkt.x, ev_pkt.y, ev_pkt.pressure);

		x = (long)ev_pkt.x * INT_MAX/max_x;
		y = (long)ev_pkt.y * INT_MAX/max_y;

		send_event(device, EV_ABS, ABS_X, x);
		send_event(device, EV_ABS, ABS_Y, y);
		send_event(device, EV_ABS, ABS_PRESSURE, ev_pkt.pressure);

		switch (ev_pkt.type) {
			case EVENT_TYPE_SET_RESOLUTION: {
				struct input_absinfo absinfo;
				max_x = ev_pkt.x;
				max_y = ev_pkt.y;
				printf("Set resolution to %hix%hi\n", max_x+1, max_y+1);
				break;
			}
			case EVENT_TYPE_MOTION:
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;
			case EVENT_TYPE_BUTTON:
				if (ev_pkt.button == 1)
					send_event(device, EV_KEY, BTN_TOUCH, ev_pkt.down);
				send_event(device, EV_SYN, SYN_REPORT, 1);
				break;

		}
	}

	close(socket);

	ioctl(device, UI_DEV_DESTROY);
	close(device);
	return 0;
}
