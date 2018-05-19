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
            /*
            Console.Out.WriteLine("Starting Root Process");
            ThreadStart padStart = new ThreadStart(new MyDS4().work);
            Thread padThread = new Thread(padStart);
            padThread.Start();
            padThread.Join();
            Console.Out.WriteLine("Pad Thread ended, exiting in 5 seconds");
            Thread.Sleep(5000);
            */


            /*
            IEnumerable<HidDevice> devices = HidDevices.Enumerate();
            foreach(HidDevice dev in devices)
            {
                Console.Out.WriteLine(dev.ToString());
            }
            Thread.Sleep(10000);
            */

            /*
            SQLiteConnection db;
            db = new SQLiteConnection("Data Source=LoginDB.sqlite;Version=3;");
            db.Open();
            //string sql = "create table data (ID INTEGER NOT NULL primary key, Connect varchar(50), Disconnect varchar(50), IP varchar(50), distance varchar(50))";
            string sql = "insert into data (Connect, Disconnect, IP, distance) values ('"+DateTimeSQL(DateTime.Now)+"', '"+DateTimeSQL(DateTime.Now)+"', '192.168.7.3', 50)";
            //string sql = "delete from data";
            SQLiteCommand cmd = new SQLiteCommand(sql, db);
            cmd.ExecuteNonQuery();
            //Console.Out.WriteLine(sql);
            //SQLiteCommand cmd = new SQLiteCommand(sql, db);
            //cmd.ExecuteNonQuery();
            
            sql = "select * from data";
            cmd = new SQLiteCommand(sql, db);
            SQLiteDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                //Console.Out.WriteLine(reader["Connect"].ToString());
                //DateTime datetime = (DateTime)reader["Connect"];
                Console.Out.WriteLine(reader["ID"] + " " + reader["Connect"] + " " + reader["IP"]);
            }
            Thread.Sleep(5000);
            db.Close();*/


            DBHandler.init("DB.sqlite");
            DBHandler.publish(DateTime.Now, DateTime.Now, "192.168.5.4", 457.34754);
            DBHandler.publish(DateTime.Now, DateTime.Now, "192.168.5.8", 546.345);
            DBHandler.printTable();
            DBHandler.close();
            Thread.Sleep(5000);
        }
    }
}
