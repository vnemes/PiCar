using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using HidLibrary;
using System.Data.SQLite;

namespace DS4BLE
{
    class Program
    {
        private static string DateTimeSQL(DateTime dt)
        {
            string DTF = "{0}-{1}-{2} {3}:{4}:{5}";
            return string.Format(DTF, dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second, dt.Millisecond);
        }

        static void Main(string[] args)
        {
            Console.Out.WriteLine("Starting Root Process");
            ThreadStart padStart = new ThreadStart(new MyDS4().init);
            Thread padThread = new Thread(padStart);
            padThread.Start();
            padThread.Join();
            Console.Out.WriteLine("Pad Thread ended, exiting in 5 seconds");
            Thread.Sleep(5000);
        }
    }
}
