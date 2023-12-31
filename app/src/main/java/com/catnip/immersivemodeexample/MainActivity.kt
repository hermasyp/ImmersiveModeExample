package com.catnip.immersivemodeexample

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.catnip.immersivemodeexample.databinding.ActivityMainBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var youtubePlayer: YouTubePlayer? = null

    private var isFullScreen = false

    private val windowInsetsController: WindowInsetsControllerCompat by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    private var previousOrientation : Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        initYoutube()
        val orientationEventListener = object: OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val newOrientation = when (orientation) {
                    in 0 .. 44 -> 0
                    in 45 .. 134 -> 1
                    in 135 .. 224 -> 2
                    in 225 .. 314 -> 3
                    in 315 .. 359 -> 0
                    else -> ORIENTATION_UNKNOWN
                }
                if (newOrientation != previousOrientation && !isFullScreen) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                }
                previousOrientation = newOrientation
            }
        }
        val autoRotationOn = Settings.System.getInt(contentResolver,
            Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        if (autoRotationOn) {
            orientationEventListener.enable()
        } else {
            orientationEventListener.disable()
        }
    }

    private fun initYoutube() {
        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .fullscreen(1) // enable full screen button
            .build()
        binding.youtubePlayerView.apply {
            enableAutomaticInitialization = false
            addFullscreenListener(object : FullscreenListener {
                override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                    enterFullScreen(fullscreenView)
                }

                override fun onExitFullscreen() {
                    exitFullscreen()
                }

            })
            initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    this@MainActivity.youtubePlayer = youTubePlayer
                    youTubePlayer.loadVideo("dQw4w9WgXcQ", 0f)
                }
            }, iFramePlayerOptions)
        }
        lifecycle.addObserver(binding.youtubePlayerView)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val oldOrientation = requestedOrientation
        val newOrientation = newConfig.orientation
        Log.d("MainActivity", "onConfigurationChanged Old Orientation: $oldOrientation")
        Log.d("MainActivity", "onConfigurationChanged New Orientation: $newOrientation")
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!isFullScreen) {
                youtubePlayer?.toggleFullscreen()
            }
        } else {
            if (isFullScreen) {
                youtubePlayer?.toggleFullscreen()
            }
        }

        super.onConfigurationChanged(newConfig)
    }

    private fun exitFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        isFullScreen = false
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        binding.cvLayout.isVisible = true
        binding.youtubePlayerView.isVisible = true
        binding.flFullscreen.apply {
            isVisible = false
            removeAllViews()
        }
    }

    private fun enterFullScreen(view: View) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        isFullScreen = true
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        binding.cvLayout.isVisible = false
        binding.youtubePlayerView.isVisible = false
        binding.flFullscreen.apply {
            isVisible = true
            addView(view)
        }
    }

}