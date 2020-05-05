package com.kakuyomu.deadmail

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    // 歩数カウント
    private var stepCount: Int = 0
    // 〇〇歩毎にメール
    private val mailStepCount: Int = 100
    // メール題名
    private val strSubject: String = "題名"
    // メール本文
    private val strText: String = "本文"
    // 宛先アドレス
    private val strTo: String = "dddRcfgcardplusw@gmail.com"

    // 効果音
    private lateinit var audioAttrivutes: AudioAttributes
    private lateinit var soundPool: SoundPool
    private var notificationSound: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // APIレベル21以下はサウンドが鳴らない、関係のない画像を表示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttrivutes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
            soundPool =
                SoundPool.Builder().setAudioAttributes(audioAttrivutes).setMaxStreams(2).build()
            notificationSound = soundPool.load(this, R.raw.bite1, 1)

            imageView.visibility = View.INVISIBLE
        } else {
            imageView.visibility = View.VISIBLE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount++
            textView.text = "ステップカウンター:${stepCount}\n"
            if (stepCount % mailStepCount == 0) {
                soundPool.play(notificationSound, 1.0f, 1.0f, 0, 0, 1.0f)
                sendMail()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    // メール送信
    private fun sendMail() {
        val email: Intent = Intent(
            Intent.ACTION_SEND,
            Uri.fromParts("mailto", "mailto:${strTo}", null)
        )

        email.putExtra(Intent.EXTRA_SUBJECT, strSubject)
        email.putExtra(Intent.EXTRA_TEXT, strText)
        email.setType("message/rfc822")
        startActivity(Intent.createChooser(email, "send mail......"))
    }
}
