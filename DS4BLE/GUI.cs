using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace DS4BLE
{
    public partial class GUI : Form
    {
        public GUI()
        {
            InitializeComponent();
        }

        private void TakePicture_Click(object sender, EventArgs e)
        {
            using (WebClient client = new WebClient())
            {
                client.DownloadFile(new Uri(url), @"c:\temp\image35.png");
            }
        }
    }
}
