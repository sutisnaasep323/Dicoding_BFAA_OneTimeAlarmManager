package com.example.myalarmmanager

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TYPE_ONE_TIME = "OneTimeAlarm"
        const val TYPE_REPEATING = "RepeatingAlarm"
        /*
        Dua baris di atas adalah konstanta untuk menentukan tipe alarm. Dan selanjutnya,
        dua baris di bawah ini adalah konstanta untuk intent key.
         */
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_TYPE = "type"

        // siapkan 2 id untuk 2 macam alarm
        /*
        Di sini kita menggunakan dua konstanta bertipe data integer untuk menentukan notif ID
        sebagai ID untuk menampilkan notifikasi kepada pengguna.
         */
        private const val ID_ONETIME = 100
        private const val ID_REPEATING = 101

        private const val DATE_FORMAT = "yyyy-mm-dd"
        private const val TIME_FORMAT = "HH:mm"
    }

    /*
   Ketika kondisi sesuai, maka akan BroadcastReceiver akan running dengan semua proses yang terdapat di dalam metode onReceive()
   */
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val type = intent.getStringExtra(EXTRA_TYPE)
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        val title = if (type.equals(TYPE_ONE_TIME, ignoreCase = true)) TYPE_ONE_TIME else TYPE_REPEATING
        val notifId = if (type.equals(TYPE_ONE_TIME, ignoreCase = true)) ID_ONETIME else ID_REPEATING

        showToast(context, title, message)

        if (message != null) {
            showAlarmNotification(context, title, message, notifId)
        }
    }

    private fun showToast(context: Context, title: String, message: String?){
        Toast.makeText(context,"$title : $message", Toast.LENGTH_LONG).show()
    }

    /*
    Pada kode di bawah kita membuat sebuah obyek untuk AlarmManager. Kemudian kita menyiapkan
    sebuah Intent yang akan menjalankan AlarmReceiver dan membawa data berupa alarm dan pesan
     */
    fun setOneTimeAlarm(context: Context, type: String, date: String, time: String, message: String) {
        if (isDateInvalid(date, DATE_FORMAT) || isDateInvalid(time, TIME_FORMAT)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_TYPE, type)

        Log.e("ONE TIME", "$date $time")
        /*
        Pada kode di bawah kita memecah data date dan time untuk mengambil nilai tahun, bulan, hari, jam dan menit.
         */
        val dateArray = date.split("-").toTypedArray()
        val timeArray = time.split(":").toTypedArray()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, Integer.parseInt(dateArray[0]))
        /*
        Mengapa kode di bawah, data kita kurangi 1? Misal tanggal yang kita masukkan adalah 2016-09-27.
        Jika kita pecah, maka kita akan memperoleh nilai 2016 (tahun), 9 (bulan), dan 27 (hari).

        Masalahnya adalah, nilai bulan ke 9 pada kelas Calendar bukanlah bulan September. Ini karena
        indeksnya dimulai dari 0. Jadi, untuk memperoleh bulan September, maka nilai 9 tadi harus kita kurangi 1
         */
        calendar.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1)
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]))
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        /*
        Intent yang dibuat akan dieksekusi ketika waktu alarm sama dengan waktu pada sistem Android.
        Di sini komponen PendingIntent akan diberikan kepada BroadcastReceiver.
         */

        /*
        Yang membedakan satu alarm dengan alarm lain adalah pada ID. Jika kita merubah nilai waktu
        dan menjalankan ulang alarm dengan ID yang sama, maka akan merubah yang sudah diset sebelumnya.
         */
        val pendingIntent = PendingIntent.getBroadcast(context, ID_ONETIME, intent, 0)
        // kita memasang alarm yang dibuat dengan tipe RTC_WAKEUP, Tipe alarm ini dapat membangunkan
        // peranti (jika dalam posisi sleep) untuk menjalankan obyek PendingIntent
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(context, "One time alarm set up", Toast.LENGTH_SHORT).show()
    }

    // Metode ini digunakan untuk menjalankan alarm repeating
    fun setRepeatingAlarm(context: Context, type: String, time: String, message: String) {

        // Validasi inputan waktu terlebih dahulu
        if (isDateInvalid(time, TIME_FORMAT)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        val putExtra = intent.putExtra(EXTRA_TYPE, type)

        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(context, ID_REPEATING, intent, 0)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        Toast.makeText(context, "Repeating alarm set up", Toast.LENGTH_SHORT).show()
    }

    // Gunakan metode ini untuk mengecek apakah alarm tersebut sudah terdaftar di alarm manager
    fun isAlarmSet(context: Context, type: String): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = if (type.equals(TYPE_ONE_TIME, ignoreCase = true)) ID_ONETIME else ID_REPEATING

        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE) != null
    }

    // Metode ini digunakan untuk validasi date dan time
    fun isDateInvalid(date: String, format:String): Boolean {
        return try {
            val df = SimpleDateFormat(format, Locale.getDefault())
            df.isLenient = false
            df.parse(date)
            false
        } catch (e: ParseException){
            true
        }
    }

    /*
    Metode di bawah merupakan sebuah metode untuk membuat dan menampilkan notifikasi yang kompatibel dengan beragam API dari Android
     */
    private fun showAlarmNotification(context: Context, title: String, message: String, notifId: Int) {
        val channelId = "Channel_1"
        val channelName = "AlarmManager channel"
        val notificationManagerCompat = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_baseline_time_black)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(channelId)
            notificationManagerCompat.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notificationManagerCompat.notify(notifId, notification)
    }
}