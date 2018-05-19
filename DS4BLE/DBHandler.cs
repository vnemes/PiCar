using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Data.SQLite;
using System.IO;

namespace DS4BLE
{
    class DBHandler
    {
        private static SQLiteConnection db;

        //Returns a formatted string of the given date and time
        private static string SQLDateFormat(DateTime dt)
        {
            string DTF = "{0}-{1}-{2} {3}:{4}:{5}";
            return string.Format(DTF, dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second);
        }

        //Initializes and opens the database from the location given by str
        public static void init(String str)
        {
            if (File.Exists(str))
            {
                db = new SQLiteConnection("Data Source=" + str + ";Version=3;");
                db.Open();
            }
            else create(str);
        }

        //Creates a new file for the database and initializes the data table
        private static void create(String str)
        {
            SQLiteConnection.CreateFile(str);
            db = new SQLiteConnection("Data Source=" + str + ";Version=3;");
            db.Open();
            string createTable = "create table data (ID INTEGER NOT NULL primary key, Connect varchar(50), Disconnect varchar(50), IP varchar(50), distance varchar(50))";
            SQLiteCommand cmd = new SQLiteCommand(createTable, db);
            cmd.ExecuteNonQuery();
        }


        //Published a new entry into the data table with connection and disconnection time, current IP address and distance traveled
        public static void publish(DateTime connect,DateTime disconnect,string IP,double distance)
        {
            if (db == null)
            {
                Console.Out.WriteLine("Database is not open");
                return;
            }
            string sql = "insert into data (Connect, Disconnect, IP, distance) values ("+
                            "'"+SQLDateFormat(connect)+"', "+
                            "'"+SQLDateFormat(disconnect)+"', "+
                            "'"+IP+"', "+
                            "'"+distance+"')";
            //Console.Out.WriteLine(sql);
            SQLiteCommand cmd = new SQLiteCommand(sql, db);
            cmd.ExecuteNonQuery();
        }


        
        public static void printTable()
        {
            if (db == null)
            {
                Console.Out.WriteLine("Database is not open");
                return;
            }
            string sql = "select * from data";
            SQLiteCommand cmd = new SQLiteCommand(sql, db);
            SQLiteDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                Console.Out.Write("ID: " + reader["ID"]+", ");
                Console.Out.Write("Connection Time: " + reader["Connect"] + ", ");
                Console.Out.Write("Disconnect Time: " + reader["Disconnect"] + ", ");
                Console.Out.Write("IP: " + reader["IP"] + ", ");
                Console.Out.WriteLine("Distance: " + reader["distance"]);
            }
        }

        //Closes the currently opened database
        public static void close()
        {
            if (db == null)
            {
                Console.Out.WriteLine("Database is not open");
                return;
            }
            db.Close();
        }
    }
}
