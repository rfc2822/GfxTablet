using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace GfxTabletWinDotnet
{
	internal static class Win32_Windows8Touch_Interop
	{
		internal enum enInitTouchInjectionModes : UInt32
		{
			TOUCH_FEEDBACK_DEFAULT = 1,
			TOUCH_FEEDBACK_INDIRECT = 2,
			TOUCH_FEEDBACK_NONE = 3
		}

		internal const int MAX_TOUCH_COUNT = 256;

		[DllImport("user32.dll", SetLastError = true)]
		internal static extern bool InitializeTouchInjection(Int32 maxCount, enInitTouchInjectionModes dwMode);

		internal enum POINTER_INPUT_TYPE
		{
			PT_POINTER = 0x00000001,
			PT_TOUCH = 0x00000002,
			PT_PEN = 0x00000003,
			PT_MOUSE = 0x00000004,
			PT_TOUCHPAD = 0x00000005
		}

		internal enum POINTER_BUTTON_CHANGE_TYPE
		{
			POINTER_CHANGE_NONE = 0,
			POINTER_CHANGE_FIRSTBUTTON_DOWN,
			POINTER_CHANGE_FIRSTBUTTON_UP ,
			POINTER_CHANGE_SECONDBUTTON_DOWN ,
			POINTER_CHANGE_SECONDBUTTON_UP ,
			POINTER_CHANGE_THIRDBUTTON_DOWN ,
			POINTER_CHANGE_THIRDBUTTON_UP  ,
			POINTER_CHANGE_FOURTHBUTTON_DOWN  ,
			POINTER_CHANGE_FOURTHBUTTON_UP  ,
			POINTER_CHANGE_FIFTHBUTTON_DOWN  ,
			POINTER_CHANGE_FIFTHBUTTON_UP 
		}


		[Flags]
		internal enum POINTER_FLAGS
		{
			POINTER_FLAG_NONE =    0x00000000,
			POINTER_FLAG_NEW =     0x00000001,		//Indicates the arrival of a new pointer.
			POINTER_FLAG_INRANGE = 0x00000002,		//Indicates that this pointer continues to exist.When this flag is not set, it indicates the pointer has left detection range.
													//This flag is typically not set only when a hovering pointer leaves detection range (POINTER_FLAG_UPDATE is set) or when a pointer in contact with a window surface leaves detection range(POINTER_FLAG_UP is set).

			POINTER_FLAG_INCONTACT =    0x00000004,				// Indicates that this pointer is in contact with the digitizer surface.When this flag is not set, it indicates a hovering pointer.

			POINTER_FLAG_FIRSTBUTTON = 0x00000010,			//	Indicates a primary action, analogous to a left mouse button down.
															//A touch pointer has this flag set when it is in contact with the digitizer surface.
															//A pen pointer has this flag set when it is in contact with the digitizer surface with no buttons pressed.
															//A mouse pointer has this flag set when the left mouse button is down.
			POINTER_FLAG_SECONDBUTTON = 0x00000020,			//Indicates a secondary action, analogous to a right mouse button down.
															//A touch pointer does not use this flag.
															//A pen pointer has this flag set when it is in contact with the digitizer surface with the pen barrel button pressed.
															//A mouse pointer has this flag set when the right mouse button is down.
			POINTER_FLAG_THIRDBUTTON= 0x00000040,			//Analogous to a mouse wheel button down.
															//A touch pointer does not use this flag.
															//A pen pointer does not use this flag.
															//A mouse pointer has this flag set when the mouse wheel button is down.
			POINTER_FLAG_FOURTHBUTTON  = 0x00000080,		//Analogous to a first extended mouse (XButton1) button down.
															 //   A touch pointer does not use this flag.
															 //   A pen pointer does not use this flag.
															 //   A mouse pointer has this flag set when the first extended mouse (XBUTTON1) button is down.
			POINTER_FLAG_FIFTHBUTTON = 0x00000100,			//Analogous to a second extended mouse (XButton2) button down.
															//A touch pointer does not use this flag.			
															//A pen pointer does not use this flag.
															//A mouse pointer has this flag set when the second extended mouse (XBUTTON2) button is down.
			POINTER_FLAG_PRIMARY = 0x00002000,				//Indicates that this pointer has been designated as the primary pointer. A primary pointer is a single pointer that can perform actions beyond those available to non-primary pointers. For example, when a primary pointer makes contact with a window’s surface, it may provide the window an opportunity to activate by sending it a WM_POINTERACTIVATE message.
															//The primary pointer is identified from all current user interactions on the system (mouse, touch, pen, and so on). As such, the primary pointer might not be associated with your app. The first contact in a multi-touch interaction is set as the primary pointer. Once a primary pointer is identified, all contacts must be lifted before a new contact can be identified as a primary pointer. For apps that don't process pointer input, only the primary pointer's events are promoted to mouse events.
			POINTER_FLAG_CONFIDENCE = 0x000004000,			//Confidence is a suggestion from the source device about whether the pointer represents an intended or accidental interaction, which is especially relevant for PT_TOUCH pointers where an accidental interaction(such as with the palm of the hand) can trigger input.The presence of this flag indicates that the source device has high confidence that this input is part of an intended interaction.
			POINTER_FLAG_CANCELED = 0x000008000,			//Indicates that the pointer is departing in an abnormal manner, such as when the system receives invalid input for the pointer or when a device with active pointers departs abruptly.If the application receiving the input is in a position to do so, it should treat the interaction as not completed and reverse any effects of the concerned pointer.
			POINTER_FLAG_DOWN = 0x00010000,					//	Indicates that this pointer transitioned to a down state; that is, it made contact with the digitizer surface.
			POINTER_FLAG_UPDATE = 0x00020000,				//	Indicates that this is a simple update that does not include pointer state changes.
			POINTER_FLAG_UP = 0x00040000,					// indicates that this pointer transitioned to an up state; that is, contact with the digitizer surface ended.
			POINTER_FLAG_WHEEL =	0x00080000,				//	Indicates input associated with a pointer wheel.For mouse pointers, this is equivalent to the action of the mouse scroll wheel (WM_MOUSEHWHEEL).
			POINTER_FLAG_HWHEEL = 0x00100000,				//   Indicates input associated with a pointer h - wheel.For mouse pointers, this is equivalent to the action of the mouse horizontal scroll wheel(WM_MOUSEHWHEEL).
			POINTER_FLAG_CAPTURECHANGED =  0x00200000,		//  Indicates that this pointer was captured by(associated with) another element and the original element has lost capture(see WM_POINTERCAPTURECHANGED).
			POINTER_FLAG_HASTRANSFORM =   0x00400000,		//  Indicates that this pointer has an associated transform.
		}

		[StructLayout(LayoutKind.Sequential, Pack = 1)]
		internal struct POINT
		{
			public Int32 x;
			public Int32 y;
		}

		[StructLayout(LayoutKind.Sequential, Pack = 1)]
		internal struct RECT
		{
			public Int32 left;
			public Int32 top;
			public Int32 right;
			public Int32 bottom;
		}

		//https://msdn.microsoft.com/de-de/library/windows/desktop/hh454907%28v=vs.85%29.aspx
		[StructLayout(LayoutKind.Sequential, Pack = 1)]
		internal struct POINTER_INFO
		{
			public POINTER_INPUT_TYPE pointerType;
			private UInt32 padding;
			public UInt32 pointerId;
			public UInt32 frameId;
			public POINTER_FLAGS pointerFlags;
			public IntPtr sourceDevice;
			public IntPtr hwndTarget;
			public POINT ptPixelLocation;
			public POINT ptHimetricLocation;
			public POINT ptPixelLocationRaw;
			public POINT ptHimetricLocationRaw;
			public UInt32 dwTime;
			public UInt32 historyCount;
			public Int32 inputData;
			public UInt32 dwKeyStates;
			public UInt64 PerformanceCount;
			public POINTER_BUTTON_CHANGE_TYPE ButtonChangeType;
		}


		[StructLayout(LayoutKind.Sequential)]
		public struct TouchPoint
		{
			/// <summary>
			/// The x-coordinate of the point.
			/// </summary>
			public int X;
			/// <summary>
			/// The y-coordinate of the point.
			/// </summary>
			public int Y;
		}
		[StructLayout(LayoutKind.Sequential)]
		public struct PointerInfo
		{
			/// <summary>
			/// A value from the PointerInputType enumeration that specifies the pointer type.
			/// </summary>
			public POINTER_INPUT_TYPE pointerType;

			/// <summary>
			/// An identifier that uniquely identifies a pointer during its lifetime. A pointer comes into existence when it is first detected and ends its existence when it goes out of detection range. Note that if a physical entity (finger or pen) goes out of detection range and then returns to be detected again, it is treated as a new pointer and may be assigned a new pointer identifier.
			/// </summary>
			public uint PointerId;

			/// <summary>
			/// An identifier common to multiple pointers for which the source device reported an update in a single input frame. For example, a parallel-mode multi-touch digitizer may report the positions of multiple touch contacts in a single update to the system.
			/// Note that frame identifier is assigned as input is reported to the system for all pointers across all devices. Therefore, this field may not contain strictly sequential values in a single series of messages that a window receives. However, this field will contain the same numerical value for all input updates that were reported in the same input frame by a single device.
			/// </summary>
			public uint FrameId;

			/// <summary>
			/// May be any reasonable combination of flags from the Pointer Flags constants.
			/// </summary>
			public POINTER_FLAGS PointerFlags;

			/// <summary>
			/// Handle to the source device that can be used in calls to the raw input device API and the digitizer device API.
			/// </summary>
			public IntPtr SourceDevice;

			/// <summary>
			/// Window to which this message was targeted. If the pointer is captured, either implicitly by virtue of having made contact over this window or explicitly using the pointer capture API, this is the capture window. If the pointer is uncaptured, this is the window over which the pointer was when this message was generated.
			/// </summary>
			public IntPtr WindowTarget;

			/// <summary>
			/// Location in screen coordinates.
			/// </summary>
			public TouchPoint PtPixelLocation;

			/// <summary>
			/// Location in device coordinates.
			/// </summary>
			public TouchPoint PtPixelLocationRaw;

			/// <summary>
			/// Location in HIMETRIC units.
			/// </summary>
			public TouchPoint PtHimetricLocation;

			/// <summary>
			/// Location in device coordinates in HIMETRIC units.
			/// </summary>
			public TouchPoint PtHimetricLocationRaw;

			/// <summary>
			/// A message time stamp assigned by the system when this input was received.
			/// </summary>
			public uint Time;

			/// <summary>
			/// Count of inputs that were coalesced into this message. This count matches the total count of entries that can be returned by a call to GetPointerInfoHistory. If no coalescing occurred, this count is 1 for the single input represented by the message.
			/// </summary>
			public uint HistoryCount;

			/// <summary>
			/// A value whose meaning depends on the nature of input. 
			/// When flags indicate PointerFlag.WHEEL, this value indicates the distance the wheel is rotated, expressed in multiples or factors of WHEEL_DELTA. A positive value indicates that the wheel was rotated forward and a negative value indicates that the wheel was rotated backward. 
			/// When flags indicate PointerFlag.HWHEEL, this value indicates the distance the wheel is rotated, expressed in multiples or factors of WHEEL_DELTA. A positive value indicates that the wheel was rotated to the right and a negative value indicates that the wheel was rotated to the left. 
			/// </summary>
			public uint InputData;

			/// <summary>
			/// Indicates which keyboard modifier keys were pressed at the time the input was generated. May be zero or a combination of the following values. 
			/// POINTER_MOD_SHIFT – A SHIFT key was pressed. 
			/// POINTER_MOD_CTRL – A CTRL key was pressed. 
			/// </summary>
			public uint KeyStates;

			/// <summary>
			/// TBD
			/// </summary>
			public ulong PerformanceCount;

			/// <summary>
			/// ???
			/// </summary>
			public POINTER_BUTTON_CHANGE_TYPE ButtonChangeType;
		}

		[StructLayout(LayoutKind.Explicit)]
		public struct ContactArea
		{
			[FieldOffset(0)]
			public int left;
			[FieldOffset(4)]
			public int top;
			[FieldOffset(8)]
			public int right;
			[FieldOffset(12)]
			public int bottom;
		}
		public enum TouchFlags
		{
			/// <summary>
			/// Indicates that no flags are set.
			/// </summary>
			NONE = 0x00000000
		}

		[StructLayout(LayoutKind.Sequential)]
		public struct PointerTouchInfo
		{
			///<summary>
			/// Contains basic pointer information common to all pointer types.
			///</summary>
			public PointerInfo PointerInfo;

			///<summary>
			/// Lists the touch flags.
			///</summary>
			public TouchFlags TouchFlags;

			/// <summary>
			/// Indicates which of the optional fields contain valid values. The member can be zero or any combination of the values from the Touch Mask constants.
			/// </summary>
			public TouchMask TouchMasks;

			///<summary>
			/// Pointer contact area in pixel screen coordinates. 
			/// By default, if the device does not report a contact area, 
			/// this field defaults to a 0-by-0 rectangle centered around the pointer location.
			///</summary>
			public ContactArea ContactArea;

			/// <summary>
			/// A raw pointer contact area.
			/// </summary>
			public ContactArea ContactAreaRaw;

			///<summary>
			/// A pointer orientation, with a value between 0 and 359, where 0 indicates a touch pointer 
			/// aligned with the x-axis and pointing from left to right; increasing values indicate degrees
			/// of rotation in the clockwise direction.
			/// This field defaults to 0 if the device does not report orientation.
			///</summary>
			public uint Orientation;

			///<summary>
			/// Pointer pressure normalized in a range of 0 to 256.
			///</summary>
			public uint Pressure;

			///// <summary>
			///// Move the touch point, together with its ContactArea
			///// </summary>
			///// <param name="deltaX">the change in the x-value</param>
			///// <param name="deltaY">the change in the y-value</param>
			//public void Move(int deltaX, int deltaY)
			//{
			//	PointerInfo.PtPixelLocation.X += deltaX;
			//	PointerInfo.PtPixelLocation.Y += deltaY;
			//	ContactArea.left += deltaX;
			//	ContactArea.right += deltaX;
			//	ContactArea.top += deltaY;
			//	ContactArea.bottom += deltaY;
			//}
		}

		[Flags]
		internal enum TouchMask : UInt32
		{
			TOUCH_MASK_NONE = 0x00000000,
			TOUCH_MASK_CONTACTAREA = 0x00000001,
			TOUCH_MASK_ORIENTATION = 0x00000002,
			TOUCH_MASK_PRESSURE  = 00000004
		}

		//https://msdn.microsoft.com/de-de/library/windows/desktop/hh454910%28v=vs.85%29.aspx
		[StructLayout(LayoutKind.Sequential, Pack =1)]
		internal struct POINTER_TOUCH_INFO
		{
			public POINTER_INFO pointerInfo;
			public Int32 touchFlags; //only TOUCH_FLAG_NONE 0 possible
			public TouchMask touchMask; //defines valid fields
			public RECT rcContact;  //identical raw or 0,0,0,0
			public RECT rcContactRaw;
			public UInt32 orientation;
			public UInt32 pressure;
		}

		[DllImport("user32.dll",SetLastError =true)]
		internal static extern bool InjectTouchInput(UInt32 count, [MarshalAs(UnmanagedType.LPArray), In] PointerTouchInfo[] contacts);



	}
}
