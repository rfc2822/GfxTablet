#define GFXTABLET_PORT 40118

#define PROTOCOL_VERSION 2


#pragma pack(push)
#pragma pack(1)

#define EVENT_TYPE_MOTION 0
#define EVENT_TYPE_BUTTON 1

struct event_packet
{
	char signature[9];
	uint16_t version;
	uint8_t type;	/* EVENT_TYPE_... */
	struct {	/* required */
		uint16_t x, y;
		uint16_t pressure;
	};

	struct {	/* only required for EVENT_TYPE_BUTTON */
		int8_t button;		/* button id:
								-1 = stylus in range,
								 0 = tap/left click/button 0,
								 1 = button 1,
								 2 = button 2 */
		int8_t down;		/* 1 = button down, 0 = button up */
	};
};

#pragma pack(pop)
