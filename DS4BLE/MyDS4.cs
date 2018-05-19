using System;
using System.CodeDom;
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
        public const int CIRCLE = 0b01000000;
        public const int CROSS = 0b00100000;
        public const int SQUARE = 0b00010000;
        public const int R3 = 0b10000000;
        public const int L3 = 0b01000000;
        public const int OPT = 0b00100000;
        public const int SHARE = 0b00010000;
        public const int R1 = 0b00000010;
        public const int L1 = 0b00000001;
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
        private bool isGyro = false, isCamera = false, killSwitch = false;
        private NetSocket ns = new NetSocket("192.168.10.125");
        private DateTime connectTime, disconnectTime;
        private const string DATABASE_FILE = "DB.sqlite";
        private const string CAMERA_COMMAND = "/C D:\\Software\\VLC\\vlc.exe tcp/h264://192.168.10.1:1324/ -f";
        private System.Diagnostics.Process process;
        private double distance = 0;


        //Maps data read from USB from the byte array inp into our readable structure
        //http://miku.sega.com/futuretone/manual/img/controls.png for details on button mapping
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


        //Checks which buttons were pressed and triggers corresponding action
        private void switches()
        {
            if (nState.Triangle && !(cState.Triangle)) isGyro = !isGyro;
            if (nState.Square && !(cState.Square)) startVideoStream();
            if (nState.Options && !(cState.Options == false)) killSwitch = true;
        }


        //Starts a video stream from the car's camera using VLC
        private void startVideoStream()
        {
            if (!isCamera)
            {
                process = new System.Diagnostics.Process();
                System.Diagnostics.ProcessStartInfo startInfo = new System.Diagnostics.ProcessStartInfo();
                startInfo.WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden;
                startInfo.FileName = "cmd.exe";
                startInfo.Arguments = CAMERA_COMMAND;
                process.StartInfo = startInfo;
                process.Start();
                isCamera = true;
            }
            else

                Console.WriteLine("Video stream already started!");
        }


        //Applies correction to the value read by the gyroscope to match the value read from moving the analogue stick
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


        //Calculates distance traveled this session
        private void addDistance(byte val)
        {
            val -= 60;
            double x = (val * 0.4) / 195;
            distance += x;
        }

        //Packages data related to speed, direction and steering into 4 bytes to be sent over WiFi
        private byte[] getData(State st)
        {
            byte[] data = new byte[4];

            if (st.L2 > 10)
            {
                data[0] = 0x00;
                data[1] = st.L2;
                if ((st.L2 > 60) && st.Equals(nState)) addDistance(st.L2);
            }
            else
            {
                data[0] = 0x01;
                data[1] = st.R2;
                if ((st.R2 > 60) && st.Equals(nState)) addDistance(st.R2);
            }

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
                data[3] = Convert.ToByte(LX);
            }
            else
            {
                LX = LX * (-1);
                data[2] = 0x00;
                if (LX > 255) LX = 255;
                data[3] = Convert.ToByte(LX);
            }
            return data;
        }


        //Sends data over the WiFi only when a change has occurred when comparing the last 2 read values
        private void sendData()
        {
            byte[] current = getData(nState);
            byte[] prev = getData(cState);
            if ((current[1] != prev[1]) || (current[3] != prev[3])) ns.sendData(current);
        }

        public void init()
        {
            try
            {
                IEnumerable<HidDevice> devices = HidDevices.Enumerate(0x054C, 0x09CC);
                foreach (HidDevice device in devices)
                {
                    Console.Out.WriteLine("Found Controller: VID:" + device.Attributes.VendorHexId + " PID:" +
                                          device.Attributes.ProductHexId);
                    device.OpenDevice(Global.getUseExclusiveMode());
                    if (device.IsOpen)
                    {
                        pad = device;               
                        cState = new State();       //Initializes gamepad states
                        nState = new State();
                        nState.Options = false;
                        Console.Out.WriteLine("Attempting Connection");
                        ns.connectStream();         //Connects over WiFi
                        connectTime = DateTime.Now;
                        DBHandler.init(DATABASE_FILE);
                        Console.Out.WriteLine("Initialization Successful");

                        run();                      //Start reading data and sending it over, as explained further below

                        ns.closeStream();           //Disconnects from WiFi
                        disconnectTime = DateTime.Now;
                        DBHandler.publish(connectTime, disconnectTime, distance);   //Saves info to database
                        DBHandler.close();
                    }
                }

                if (devices.Count() == 0)
                {
                    Console.Out.WriteLine("No device found, exiting");
                    Thread.Sleep(3000);
                }
            }
            catch (Exception e)
            {
                Console.Out.WriteLine(e.StackTrace);
            }
        }

        public void run()
        {
            while (!killSwitch)
            {
                cState = nState;
                pad.ReadFile(inputData);                    //Reads polled data from USB byte by byte
                mapButtons(inputData);                      //Maps data that we need to gamepad buttons
                switches();                                 //Triggers actions on button presses
                sendData();                                 //Sends data over WiFi to the car
                Thread.Sleep(millisecondsTimeout: 50);
            }
        }
    }
}