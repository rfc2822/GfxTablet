
#define GFXTABLET_PORT 40118

#define PROTOCOL_VERSION 1


#pragma pack(push)
#pragma pack(1)

#define EVENT_TYPE_MOTION 0
#define EVENT_TYPE_BUTTON 1

struct event_packet
{
	char signature[9];
	unsigned short version;
	char type;	/* EVENT_TYPE_... */
	struct {	/* required */
		short x, y;
		short pressure;
	};

	struct {	/* only required for EVENT_TYPE_BUTTON */
		char button;		/* number of button, beginning with 1 */
		char down;		/* 1 = button down, 0 = button up */
	};
};

#pragma pack(pop)
