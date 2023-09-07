package com.thew00xer.temiuserdetection

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.robotemi.sdk.Robot
import com.robotemi.sdk.Robot.Companion.getInstance
import com.robotemi.sdk.listeners.OnDetectionDataChangedListener
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.model.DetectionData

class MainActivity : AppCompatActivity(), OnRobotReadyListener, OnDetectionDataChangedListener, OnDetectionStateChangedListener {

    private lateinit var robot: Robot
    private lateinit var infoText: TextView
    private lateinit var startButton: ImageButton
    private lateinit var stopButton: Button

    private lateinit var detectionStatus: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        robot = getInstance()

        startButton = findViewById(R.id.enterButton)
        stopButton = findViewById(R.id.button2)
        infoText = findViewById(R.id.textView)

        initOnClickListener()
    }

    private fun initOnClickListener() {
        startButton.setOnClickListener {
            robot.addOnDetectionStateChangedListener(this)
            robot.addOnDetectionDataChangedListener(this)
            robot.setDetectionModeOn(true, 0F)
            startButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.enter_key_black))
            startButton.isClickable = false

            // Handler to stop detection if there are no users detected from the beginning.
            Handler().postDelayed({
                if (!startButton.isClickable && infoText.text == "") {
                    Toast.makeText(this@MainActivity, "I was waiting.", Toast.LENGTH_SHORT).show()
                    robot.removeOnDetectionStateChangedListener(this)
                    robot.removeOnDetectionDataChangedListener(this)
                    startButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.enter_key_green))
                    startButton.isClickable = true
                    infoText.text = ""
                }
            }, 5000)

        }
        stopButton.setOnClickListener {
            robot.removeOnDetectionStateChangedListener(this)
            robot.removeOnDetectionDataChangedListener(this)
            robot.setDetectionModeOn(false, 0F)
            startButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.enter_key_green))
            startButton.isClickable = true
            infoText.text = ""
        }
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            try {
                val activityInfo =
                    packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
                robot.onStart(activityInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException(e)
            }
            robot.hideTopBar()
        }
    }

    /**
     * Setting up onRobotReady event listener
     */
    override fun onStart() {
        super.onStart()
        robot.addOnRobotReadyListener(this)
        robot.showTopBar()
    }

    /**
     * Removing the event listeners upon leaving the app.
     */
    override fun onStop() {
        super.onStop()
        robot.removeOnRobotReadyListener(this)
        robot.removeOnDetectionStateChangedListener(this)
        robot.removeOnDetectionDataChangedListener(this)
    }

    override fun onDetectionDataChanged(detectionData: DetectionData) {
        if (!startButton.isClickable) {
            infoText.text = if (detectionData.isDetected) {
                "Detect -> angle ${detectionData.angle}, dist ${detectionData.distance}"
            } else {
                "No detection"
            }
        } else {
            infoText.text = ""
        }
    }

    override fun onDetectionStateChanged(state: Int) {
        detectionStatus = getString(
            R.string.detect_state,
            OnDetectionStateChangedListener.DetectionStatus.fromValue(state)
        )
        if (!startButton.isClickable) {
            infoText.text = detectionStatus
        } else {
            infoText.text = ""
        }
        if (detectionStatus == "Detect State -> Idle") {
            startButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.enter_key_green))
            startButton.isClickable = true
            infoText.text = ""
        }
    }
}