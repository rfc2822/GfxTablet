using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace GfxTabletWinDotnet
{
	internal static class Win32Interop
	{
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

	}
}
