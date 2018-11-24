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
		}


		private void Form1_Load(object sender, EventArgs e)
		{
			Listener.Instance.TabletEvent += Instance_TabletEvent;
			Listener.Instance.Start();
            this.txtIpAddress.Text = Listener.Instance.ListenAddress.ToString();
        }
    }
}
