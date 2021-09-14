package com.example.myalarmmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.myalarmmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var binding : ActivityMainBinding? = null
    private lateinit var alarmReceiver : AlarmReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Listener one time alarm
        binding?.btnOnceDate?.setOnClickListener(this)
        binding?.btnOnceTime?.setOnClickListener(this)
        binding?.btnSetOnceAlarm?.setOnClickListener(this)

        alarmReceiver = AlarmReceiver()

    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_once_date -> {

            }
            R.id.btn_once_time -> {

            }
            R.id.btn_set_once_alarm -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding  = null
    }
}