package sg.edu.nus.iss.a3a_permissions

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // STEP 2: Request permissions in activity
    private val permissions : Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val REQ_RECORD_AUDIO = 1

    // Prepare media recorder/player and output file
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val outputFile by lazy { "${externalCacheDir?.absolutePath}/audio.3gp" }

    // Prevent multiple start on already started recording
    private var isRecording = false

    // Lazy load buttons
    private val startBtn by lazy { findViewById<Button>(R.id.start_btn) }
    private val stopBtn by lazy { findViewById<Button>(R.id.stop_btn) }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Request permission ON CLICK
        startBtn.setOnClickListener {

            // If already recording, don't allow user to start another recording
            if (isRecording) {
                Toast.makeText(
                    this,
                    "Already recording",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener

            } else {

                // If permission has not been granted, request permission
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQ_RECORD_AUDIO
                    )

                // If permission was already granted before, start recording
                } else {
                    startRecording()
                }

            }
        }

        // Set listener to stop event
        stopBtn.setOnClickListener {
            // Stop audio recording
            stopRecording()

            // Start playing recording
            startPlaying()
        }

    }

    // STEP 3: Handle permission result
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, record audio
                startRecording()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot record audio.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // If user closes app while recording/playing, stop
    override fun onStop() {
        super.onStop()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Helper: start recording
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        try {
            // Create mediaRecorder instance and start
            // Should create on every record and delete on every stop bc it is resource heavy
            mediaRecorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            isRecording = true

            // When recording, disable start button
            startBtn.isEnabled = false
            startBtn.background = AppCompatResources.getDrawable(this, R.color.grey)

            Toast.makeText(
                this,
                "Audio recording started",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper: stop recording
    private fun stopRecording() {
        // Stop audio recording
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        isRecording = false

        // When done recording, enable start button
        startBtn.isEnabled = true
        startBtn.background = AppCompatResources.getDrawable(this, R.color.green)

        Toast.makeText(
            this,
            "Audio recording stopped",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Helper: start playing recording
    private fun startPlaying() {

        // If a player is already running, stop it first
        mediaPlayer?.release()

        // Start playing
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(outputFile)
                prepare()
                start()

                Toast.makeText(
                    this@MainActivity,
                    "Now playing...",
                    Toast.LENGTH_SHORT
                ).show()

                // Release resources when done playing
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    Toast.makeText(
                        this@MainActivity,
                        "Finished playing",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}