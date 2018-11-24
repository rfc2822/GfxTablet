using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using TCD.System.TouchInjection;

namespace GfxTabletWinDotnet
{
	public partial class GfxTabletSettings : Form
	{
		public GfxTabletSettings()
		{
			InitializeComponent();
            bool ok = Win32Interop.InitializeTouchInjection(10, Win32Interop.enInitTouchInjectionModes.TOUCH_FEEDBACK_DEFAULT);
			System.Diagnostics.Trace.Assert(ok);

		}

		//static int gUniqueFrameId = 1;
		bool bIsDown = false;

		private void Instance_TabletEvent(object sender, Protocol.event_packet e)
		{
			Invoke(new MethodInvoker( delegate () {
				txtX.Text = e.x.ToString();
				txtY.Text = e.y.ToString();
				txtPressure.Text = e.pressure.ToString();
				txtButton.Text = e.button.ToString();


				Win32Interop.POINT screenPos;
				screenPos.x = e.x * 1400 / 65535;
				screenPos.y = e.y * 800 / 65535;

				PointerTouchInfo[] touchEvent = new PointerTouchInfo[1];
				touchEvent[0] = MakePointerTouchInfo(screenPos.x, screenPos.y, (int)Math.Pow(2, Math.Max(1,e.pressure/1000)), 1);

#if true
				if (e.type == Protocol.EVENT_TYPE_BUTTON && e.down != 0)
				{
					e.type = Protocol.EVENT_TYPE_MOTION;
				}
				else if (e.type == Protocol.EVENT_TYPE_MOTION && e.pressure > 23000)
				{
					if (!bIsDown)
						e.type = Protocol.EVENT_TYPE_BUTTON;
				}
#endif

				if (e.type == GfxTabletWinDotnet.Protocol.EVENT_TYPE_MOTION)
				{
					touchEvent[0].PointerInfo.PointerFlags = PointerFlags.UPDATE | PointerFlags.INCONTACT | PointerFlags.INRANGE;
				}
				else
				{
					if (e.button == 0)
					{
						bIsDown = true;
						touchEvent[0].PointerInfo.PointerFlags = PointerFlags.DOWN | PointerFlags.INCONTACT | PointerFlags.INRANGE;
					}
					else
					{
						bIsDown = false;
						touchEvent[0].PointerInfo.PointerFlags = PointerFlags.UP | PointerFlags.INCONTACT | PointerFlags.INRANGE;
					}
				}

				touchEvent[0].Pressure = (uint)e.pressure / 2;


				bool ok = TouchInjector.InjectTouchInput(1, touchEvent);
				if (!ok)
					ok = ok;


				//Win32Interop.PointerTouchInfo[] touchEvent = new Win32Interop.PointerTouchInfo[1];


			//touchEvent[0].PointerInfo.pointerType = Win32Interop.POINTER_INPUT_TYPE.PT_TOUCH;
			//touchEvent[0].PointerInfo.PointerId = 0; //each finger leaving and entering may get a unique id. one is sufficient for our purposes
			////touchEvent[0].PointerInfo.FrameId = (uint)(gUniqueFrameId++);
			////touchEvent[0].pointerInfo.pointerFlags = Win32Interop.POINTER_FLAGS.POINTER_FLAG_INRANGE | Win32Interop.POINTER_FLAGS.POINTER_FLAG_INCONTACT | Win32Interop.POINTER_FLAGS.POINTER_FLAG_PRIMARY | Win32Interop.POINTER_FLAGS.POINTER_FLAG_UPDATE | Win32Interop.POINTER_FLAGS.POINTER_FLAG_DOWN;
			////touchEvent[0].PointerInfo.SourceDevice = IntPtr.Zero;
			////touchEvent[0].PointerInfo.WindowTarget = IntPtr.Zero; //window under mouse? probably assigend by windows
			//touchEvent[0].PointerInfo.PtPixelLocation.X = 640;
			//touchEvent[0].PointerInfo.PtPixelLocation.Y = 480;
			////touchEvent[0].PointerInfo.PtHimetricLocation.X = 0;
			////touchEvent[0].PointerInfo.PtHimetricLocation.Y = 0;
			////touchEvent[0].PointerInfo.PtHimetricLocationRaw.X = 0;
			////touchEvent[0].PointerInfo.PtHimetricLocationRaw.Y = 0;
			////touchEvent[0].PointerInfo.Time = 0; // DateTime.Now.Ticks;
			//touchEvent[0].PointerInfo.HistoryCount = 1;
			////touchEvent[0].PointerInfo.InputData = 0;
			////touchEvent[0].PointerInfo.KeyStates = 0; //todo? probably assigned by windows
			////touchEvent[0].PointerInfo.PerformanceCount = 0;
			////touchEvent[0].pointerInfo.ButtonChangeType = Win32Interop.POINTER_BUTTON_CHANGE_TYPE.POINTER_CHANGE_NONE;

			//touchEvent[0].TouchFlags = Win32Interop.TouchFlags.NONE;

			//touchEvent[0].TouchMasks = Win32Interop.TouchMask.TOUCH_MASK_PRESSURE | Win32Interop.TouchMask.TOUCH_MASK_CONTACTAREA | Win32Interop.TouchMask.TOUCH_MASK_ORIENTATION;
			//touchEvent[0].Orientation = 90; //stift-neigung, default=0
			//touchEvent[0].ContactArea.left = touchEvent[0].PointerInfo.PtPixelLocation.X - 2;
			//touchEvent[0].ContactArea.top = touchEvent[0].PointerInfo.PtPixelLocation.Y - 2;
			//touchEvent[0].ContactArea.right = touchEvent[0].PointerInfo.PtPixelLocation.X + 2;
			//touchEvent[0].ContactArea.bottom = touchEvent[0].PointerInfo.PtPixelLocation.Y + 2;

			//touchEvent[0].Pressure = 32000; // (uint)Math.Min(1024, (int)e.pressure); //0-1024

			//if (e.down == 1)
			//{
			//	touchEvent[0].PointerInfo.PointerFlags = Win32Interop.POINTER_FLAGS.POINTER_FLAG_INRANGE | Win32Interop.POINTER_FLAGS.POINTER_FLAG_INCONTACT | Win32Interop.POINTER_FLAGS.POINTER_FLAG_DOWN;
			//	//touchEvent[0].pointerInfo.ButtonChangeType = Win32Interop.POINTER_BUTTON_CHANGE_TYPE.POINTER_CHANGE_FIRSTBUTTON_DOWN;
			//}
			//else if (e.down == 0)
			//{
			//	touchEvent[0].PointerInfo.PointerFlags = Win32Interop.POINTER_FLAGS.POINTER_FLAG_INRANGE | Win32Interop.POINTER_FLAGS.POINTER_FLAG_INCONTACT | Win32Interop.POINTER_FLAGS.POINTER_FLAG_UP;
			//	//touchEvent[0].pointerInfo.ButtonChangeType = Win32Interop.POINTER_BUTTON_CHANGE_TYPE.POINTER_CHANGE_FIRSTBUTTON_DOWN;
			//}

			//bool ok = Win32Interop.InjectTouchInput(1, touchEvent);

			}));
		}

		public static PointerTouchInfo MakePointerTouchInfo(int x, int y, int radius, uint id, uint orientation = 90, uint pressure = 32000)
		{
			PointerTouchInfo contact = new PointerTouchInfo();
			contact.PointerInfo.pointerType = PointerInputType.TOUCH;
			contact.TouchFlags = TouchFlags.NONE;
			contact.Orientation = orientation;
			contact.Pressure = pressure;
			contact.PointerInfo.PointerFlags = PointerFlags.DOWN | PointerFlags.INRANGE | PointerFlags.INCONTACT; //
			contact.TouchMasks = TouchMask.CONTACTAREA | TouchMask.ORIENTATION | TouchMask.PRESSURE;
			contact.PointerInfo.PtPixelLocation.X = x;
			contact.PointerInfo.PtPixelLocation.Y = y;
			contact.PointerInfo.PointerId = id;
			contact.ContactArea.left = x - radius;
			contact.ContactArea.right = x + radius;
			contact.ContactArea.top = y - radius;
			contact.ContactArea.bottom = y + radius;
			return contact;
		}

		private void Form1_Load(object sender, EventArgs e)
		{
			Listener.Instance.TabletEvent += Instance_TabletEvent;
			Listener.Instance.Start();
            this.txtIpAddress.Text = Listener.Instance.listenAddress.ToString();
        }
    }
}
