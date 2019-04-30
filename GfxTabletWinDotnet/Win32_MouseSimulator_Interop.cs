using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace GfxTabletWinDotnet
{
	internal static class Win32_MouseSimulator_Interop
	{
		[DllImport("user32.dll", SetLastError = true)]
		internal static extern bool SendInput(Int32 maxCount, enInitTouchInjectionModes dwMode);


	}
}
