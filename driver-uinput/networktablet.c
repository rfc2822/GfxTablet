#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <linux/input.h>
#include <linux/uinput.h>
#include "protocol.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}

int udp_socket;

void init_device(int fd){
	struct uinput_user_dev uidev = {
		.name = "Network Tablet",
		.id = {
			.bustype = BUS_VIRTUAL,
			.vendor = 0x1,
			.product = 0x1,
			.version = 1
		},
		.absmin = {
			0
		},
		.absmax = {
			[ABS_X] = UINT16_MAX,
			[ABS_Y] = UINT16_MAX,
			[ABS_PRESSURE] = UINT16_MAX
		}
	};

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

	if (write(fd, &uidev, sizeof(uidev)) < 0)
		die("error: write");

	if (ioctl(fd, UI_DEV_CREATE) < 0)
		die("error: ioctl");
}

int prepare_socket(char* bindhost, char* port){
	int fd = -1, status, yes = 1;
	struct addrinfo hints;
	struct addrinfo* info;
	struct addrinfo* addr_it;

	memset(&hints, 0, sizeof(hints));

	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_DGRAM;
	hints.ai_flags = AI_PASSIVE;

	status = getaddrinfo(bindhost, port, &hints, &info);
	if(status){
		fprintf(stderr, "Failed to get socket info for %s port %s: %s\n", bindhost, port, gai_strerror(status));
		return -1;
	}

	for(addr_it = info; addr_it != NULL; addr_it = addr_it->ai_next){
		fd = socket(addr_it->ai_family, addr_it->ai_socktype, addr_it->ai_protocol);
		if(fd < 0){
			continue;
		}

		yes = 1;
		if(setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, (void*)&yes, sizeof(yes)) < 0){
			fprintf(stderr, "Failed to set SO_REUSEADDR on socket\n");
		}

		yes = 1;
		if(setsockopt(fd, SOL_SOCKET, SO_BROADCAST, (void*)&yes, sizeof(yes)) < 0){
			fprintf(stderr, "Failed to set SO_BROADCAST on socket\n");
		}

		status = bind(fd, addr_it->ai_addr, addr_it->ai_addrlen);
		if(status < 0){
			close(fd);
			continue;
		}

		break;
	}

	freeaddrinfo(info);

	if(!addr_it){
		fprintf(stderr, "Failed to create listening socket for %s port %s\n", bindhost, port);
		return -1;
	}
	return fd;
}

void send_event(int device, int type, int code, int value){
	struct input_event ev = {
		.type = type,
		.code = code,
		.value = value
	};
	if (write(device, &ev, sizeof(ev)) < 0)
		die("error: write()");
}

void handle_signal(int signal){
	close(udp_socket);
}

int main(int argc, char** argv){
	int device;
	struct event_packet ev_pkt;

	if ((device = open("/dev/uinput", O_WRONLY | O_NONBLOCK)) < 0)
		die("error: open");

	init_device(device);
	udp_socket = prepare_socket(argc > 1 ? argv[1] : GFXTABLET_DEFAULT_HOST, argc > 2 ? argv[2] : GFXTABLET_DEFAULT_PORT);

	printf("GfxTablet driver (protocol version %u) is ready and listening\n", PROTOCOL_VERSION);

	signal(SIGINT, handle_signal);
	signal(SIGTERM, handle_signal);

	while (recv(udp_socket, &ev_pkt, sizeof(ev_pkt), 0) >= 9) {		// every packet has at least 9 bytes
		printf(".");
		fflush(stdout);

		if (memcmp(ev_pkt.signature, "GfxTablet", 9) != 0) {
			fprintf(stderr, "\nIgnoring a malformed packet\n");
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
	}

	printf("Removing network tablet from device list\n");
	ioctl(device, UI_DEV_DESTROY);
	close(device);
	close(udp_socket);

	printf("GfxTablet driver shut down gracefully\n");
	return 0;
}
