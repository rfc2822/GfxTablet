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

        public IPAddress ListenAddress { get; private set; }
        public string ListenHostname { get; private set; }
        public IPEndPoint ListeningIpEndPoint;

        private Listener()
		{
            this.ListenHostname = Dns.GetHostName();

            IPHostEntry hostInfo = Dns.GetHostEntry(ListenHostname);
            // Get the DNS IP addresses associated with the host.
            IPAddress[] IPaddresses = hostInfo.AddressList;
            ListeningIpEndPoint = new IPEndPoint(IPAddress.Any, 0);

            var nic = NetworkInterface.GetAllNetworkInterfaces().Where(
                            o => o.OperationalStatus == OperationalStatus.Up 
                            && o.GetIPProperties().GatewayAddresses.Count > 0).FirstOrDefault();

            foreach (var ip in nic.GetIPProperties().UnicastAddresses)
            {
                if (ip.Address.AddressFamily == AddressFamily.InterNetwork) //only use ipv4
                    ListeningIpEndPoint = new IPEndPoint(ip.Address, 0);
            }

            ListenAddress = ListeningIpEndPoint.Address;
        }

        public void Start()
		{
            UdpClient server = new UdpClient(Protocol.GFXTABLET_PORT);

            var thread = new System.Threading.Thread(delegate ()
			{
				while (true)
				{
                    IPEndPoint remoteEndpoint = null;
                    Byte[] receiveBytes = server.Receive(ref remoteEndpoint);

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

        void StartInjector()
        {
            bool ok = Win32Interop.InitializeTouchInjection(10, Win32Interop.enInitTouchInjectionModes.TOUCH_FEEDBACK_DEFAULT);
            System.Diagnostics.Trace.Assert(ok);

        }
    }
}
