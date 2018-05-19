using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using HidLibrary;
using ScpControl;

namespace DS4BLE
{

    static class Con
    {
        public const int TRIANGLE = 0b10000000;
        public const int CIRCLE =   0b01000000;
        public const int CROSS =    0b00100000;
        public const int SQUARE =   0b00010000;
        public const int R3 =       0b10000000;
        public const int L3 =       0b01000000;
        public const int OPT =      0b00100000;
        public const int SHARE =    0b00010000;
        public const int R1 =       0b00000010;
        public const int L1 =       0b00000001;

    }

    public struct State
    {
        public byte LX, LY, RX, RY, L2, R2;
        public bool Square, Triangle, Circle, Cross, Share, Options, TouchButton, L1, R1, L3, R3, PS;
        //public bool DpadUp, DpadRight, DpadDown, DpadLeft;
        public short gyroX;
    }


    class MyDS4
    {
        private HidDevice pad;
        private byte[] inputData = new byte[64];
        private State cState, nState; //respresents current/next state used for flipping
        private bool isGyro=false;
        private NetSocket ns = new NetSocket("192.168.10.125");
        private DateTime connectTime, disconnectTime;

        private void mapButtons(byte[] inp)
        {
            nState.LX = inp[1];
            nState.LY = inp[2];
            nState.RX = inp[3];
            nState.RY = inp[4];
            nState.L2 = inp[8];
            nState.R2 = inp[9];
            
            nState.Triangle = ((inp[5] & Con.TRIANGLE) == Con.TRIANGLE);
            nState.Circle = ((inp[5] & Con.CIRCLE) == Con.CIRCLE);
            nState.Square = ((inp[5] & Con.SQUARE) == Con.SQUARE);
            nState.Cross = ((inp[5] & Con.CROSS) == Con.CROSS);

            nState.Options = ((inp[6] & Con.OPT) == Con.OPT);

            nState.gyroX = gyroCompute(BitConverter.ToInt16(inp, 19));
        }

        private void switches()
        {
            if (nState.Triangle && !(cState.Triangle)) isGyro = !isGyro;
            if (nState.Square && !(cState.Square)) takeScreenshot();
        }

        private void takeScreenshot()
        {
            byte[] data = new byte[]{ 0x55,0,0,0};
            ns.sendData(data);
            Console.Out.WriteLine("SCREENSHOT");
        }

        private bool killSwitch()
        {
            if ((nState.Options==true) && (cState.Options==false)) return true;
            else return false;
        }

        private short gyroCompute(short sh)
        {
            sh *= -1;
            if (sh > 8000) sh = 8000;
            if (sh < -8000) sh = -8000;
            sh /= 62;
            sh += 128;
            if (sh < 0) sh = 0;
            if (sh > 255) sh = 255;
            return sh;
        }

        public string gyroStatus()
        {
            int LX;
            if (!isGyro)
            {
                LX = nState.LX;
            }
            else
            {
                LX=nState.gyroX;
            }
            LX = LX * 2;
            LX = LX - 256;
            return LX.ToString();
        }

        private void outDebug()
        {
            //Console.Out.WriteLine("Accel:" + nState.R2 + "  Brake:" + nState.L2 + "  Steering:" + gyroStatus() + "  Gyro:" + isGyro);
        }

        private byte[] getData(State st)
        {
            byte[] data = new byte[4];

            if (st.L2 > 10) { data[0] = 0x00; data[1] = st.L2; }
            else { data[0] = 0x01; data[1] = st.R2; }

            int LX;
            if (!isGyro)
            {
                LX = st.LX;
            }
            else
            {
                LX = st.gyroX;
            }
            LX = LX * 2;
            LX = LX - 256;
            if (LX >= 0)
            {
                data[2] = 0x01;
                if (LX > 255) LX = 255;
                data[3] = Convert.ToByte(LX); }
            else
            {
                LX = LX * (-1);
                data[2] = 0x00;
                if (LX > 255) LX = 255;
                data[3] = Convert.ToByte(LX);
            }
            Console.Out.WriteLine(data[0] + " "+ data[1] + " "+ data[2] + " "+ data[3] + " ");
            return data;
        }

        private void sendData()
        {
            byte[] current = getData(nState);
            byte[] prev = getData(cState);
            if ((current[1] != prev[1]) || (current[3] != prev[3])) ns.sendData(current);
        }

        private void getDate()
        {
            Console.Out.WriteLine(connectTime.Day + "/" + connectTime.Month + "/" + connectTime.Year +
                            " " + connectTime.Hour + ":" + connectTime.Minute + ":" + connectTime.Second +
                            " to " + disconnectTime.Hour + ":" + disconnectTime.Minute + ":" + disconnectTime.Second);
        }

        private void createDB()
        {
            
        }

        public void work()
        {
            try
            {
                IEnumerable<HidDevice> devices = HidDevices.Enumerate(0x054C, 0x09CC);
                Console.Out.WriteLine(devices.Count()); Thread.Sleep(2000);
                
                foreach (HidDevice device in devices)
                {
                    Console.Out.WriteLine("Found Controller: VID:" + device.Attributes.VendorHexId + " PID:" + device.Attributes.ProductHexId);
                    device.OpenDevice(Global.getUseExclusiveMode());
                    if (device.IsOpen)
                    {
                        //Console.Out.WriteLine("TEST");
                        pad = device;
                        cState = new State();
                        nState = new State();
                        nState.Options = false;
                        ns.connectStream();
                        connectTime = DateTime.Now;
                        while(!killSwitch())
                        {
                            cState = nState;
                            pad.ReadFile(inputData);
                            mapButtons(inputData);
                            switches();
                            sendData();
                            outDebug();
                            Thread.Sleep(millisecondsTimeout: 50);
                        }
                        ns.closeStream();
                        disconnectTime = DateTime.Now;
                    }
                }
                if (devices.Count() == 0) {
                    Console.Out.WriteLine("No device found");
                    Thread.Sleep(3000);
                }
            }
            catch (Exception e)
            {
                Console.Out.WriteLine(e.StackTrace);
            }
        }
    }
}
