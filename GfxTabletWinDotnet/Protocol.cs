using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Runtime.InteropServices;
using System.Net;
using System.Net.Sockets;
using System.Net.NetworkInformation;

namespace GfxTabletWinDotnet
{
	class Protocol
	{

		public const int GFXTABLET_PORT = 40118;

		public const int PROTOCOL_VERSION = 2;


		public const int EVENT_TYPE_MOTION = 0;
		public const int EVENT_TYPE_BUTTON = 1;

		[StructLayout(System.Runtime.InteropServices.LayoutKind.Sequential, Pack = 1, CharSet = CharSet.Ansi)]
		public struct event_packet
		{
			[MarshalAs( UnmanagedType.ByValArray, SizeConst =9)]
			public byte[] signature;
			public ushort version;
			public byte type;  /* EVENT_TYPE_... */


			/* required */
			public ushort x, y;
			public ushort pressure;

			/* only required for EVENT_TYPE_BUTTON */
			public byte button;        /* number of button, beginning with 1 */
			public byte down;      /* 1 = button down, 0 = button up */
		}
	}

	internal class Listener
	{
		public static readonly Listener Instance = new Listener();

		private Listener()
		{
		}

		//public Socket socket;
		public IPAddress listenAddress;

		public void Start()
		{			
			IPHostEntry hostInfo = Dns.GetHostEntry(Dns.GetHostName());
			// Get the DNS IP addresses associated with the host.
			IPAddress[] IPaddresses = hostInfo.AddressList;

			//var hostEndPoint = new IPEndPoint(IPAddress.Any, Protocol.GFXTABLET_PORT);
			//socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
			//socket.Bind(hostEndPoint);

			//socket.Listen(0);
			////socket.Listen(2);
			//socket.BeginAccept(new AsyncCallback(NewConnection), socket);

			UdpClient server = new UdpClient(Protocol.GFXTABLET_PORT);
			IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);

			var nic = NetworkInterface.GetAllNetworkInterfaces().Where(o => o.OperationalStatus == OperationalStatus.Up && o.GetIPProperties().GatewayAddresses.Count>0).FirstOrDefault();
			foreach(var ip in nic.GetIPProperties().UnicastAddresses)
			{
				if(ip.Address.AddressFamily == AddressFamily.InterNetwork) //ignore ipv4 
					RemoteIpEndPoint = new IPEndPoint(ip.Address, 0);
			}

			listenAddress = RemoteIpEndPoint.Address;

			var thread = new System.Threading.Thread(delegate ()
			{
				while (true)
				{
					Byte[] receiveBytes = server.Receive(ref RemoteIpEndPoint);

					Protocol.event_packet eventData = ByteArrayToStructure<Protocol.event_packet>(receiveBytes);
					ntohs(ref eventData.x);
					ntohs(ref eventData.y);
					ntohs(ref eventData.pressure);
					if (TabletEvent != null)
						TabletEvent(this, eventData);
				}
			});
			thread.Start();
		}

		private static void ntohs(ref ushort n)
		{
			n= (ushort)( ((uint)(n & 0xff))<<8 | ((uint)(n & 0xff00)) >>8 );
        }

		public delegate void TabletEventHandler(object sender, Protocol.event_packet e);
		public event TabletEventHandler TabletEvent;

		T ByteArrayToStructure<T>(byte[] bytes) where T : struct
		{
			GCHandle handle = GCHandle.Alloc(bytes, GCHandleType.Pinned);
			T stuff = (T)Marshal.PtrToStructure(handle.AddrOfPinnedObject(),
				typeof(T));
			handle.Free();
			return stuff;
		}

		class StateObject
		{
			public static int BufferSize = 1024;
			internal Socket workSocket;
			internal byte[] buffer = new byte[1024];

		}

		void NewConnection(IAsyncResult ar)
		{
			Socket listenerSocket = (Socket)ar.AsyncState;

			// Create the state object
			StateObject state = new StateObject();
			state.workSocket = (Socket)listenerSocket.EndAccept(ar);
			state.workSocket.BeginReceive(state.buffer, 0, Marshal.SizeOf(typeof(Protocol.event_packet)), 0, new AsyncCallback(ReceiveCallback), state);
		}

		private void ReceiveCallback(IAsyncResult ar)
		{
			//ar.AsyncState;
		}
	}
}
