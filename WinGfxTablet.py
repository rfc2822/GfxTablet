import socket
import select
import struct
import ctypes

device_res_x = 32767/800
device_res_y = 32767/480

class Receiver(object):
	def __init__(self):
		self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		self.sock.bind(("", 40118))
		self.sock.setblocking(0)

	def recv(self):
		result = select.select([self.sock],[],[])
		return result[0][0].recv(24)

	def net_event(self):
		data = self.recv()
		netevent = {}
		netevent["signature"] = data[0:9]
		netevent["version"] = struct.unpack(">h",data[9:11])[0]
		netevent["type"] = int(data[11])
		netevent["x"] = int(struct.unpack(">h",data[12:14])[0] / device_res_x)
		netevent["y"] = int(struct.unpack(">h",data[14:16])[0] / device_res_y)
		netevent["pressure"] = struct.unpack(">h",data[16:18])[0]
		if netevent["type"] == 1:
			netevent["button"] = data[18]
			netevent["button_down"] = data[19]
		return netevent

class GFXTablet(object):
	def __init__(self):
		self.receiver = Receiver()

	def run(self):
		topx = int((ctypes.windll.user32.GetSystemMetrics(0) - device_res_x) * 0.25)
		topy = int((ctypes.windll.user32.GetSystemMetrics(1) - device_res_y) * 0.25)
		while True:
			event = self.receiver.net_event()
			ctypes.windll.user32.SetCursorPos(event["x"]+topx,event["y"]+topy)
			if event["type"] == 1:
				if event["button_down"] == 0:
					ctypes.windll.user32.mouse_event(4,0,0,0,0)
				elif event["button_down"] == 1:
					ctypes.windll.user32.mouse_event(2,0,0,0,0)

if __name__ == "__main__":
	app = GFXTablet()
	app.run()
