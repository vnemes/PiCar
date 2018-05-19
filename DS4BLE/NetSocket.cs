using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace DS4BLE
{
    class NetSocket
    {

        private TcpClient tcpClient = new TcpClient();
        private String IP;

        public NetSocket(String IP)
        {
            this.IP = IP;
        }

        public void connectStream()
        {
            tcpClient.Connect(new IPEndPoint(IPAddress.Parse(IP), 80));
        }

        public void sendData(byte[] data)
        {
            tcpClient.GetStream().Write(data, 0, 4);
        }

        public void closeStream()
        {
            tcpClient.Close();
        }
    }
}
