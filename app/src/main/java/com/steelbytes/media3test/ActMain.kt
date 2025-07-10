package com.steelbytes.media3test

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.effect.Crop
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.steelbytes.media3test.databinding.ActmainBinding
import androidx.core.net.toUri

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@SuppressLint("UnsafeOptInUsageError")
class ActMain : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActmainBinding
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var uri:Uri? = "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4".toUri()
    private var effects: ArrayList<Effect>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        theme.applyStyle(R.style.OptOutEdgeToEdgeEnforcement, /* force */ false)
        super.onCreate(savedInstanceState)
        binding = ActmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.load.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        playerView?.onResume()
        start()
    }

    override fun onPause() {
        //player?.pause() // normal style (and annoyingly will then auto play during onResume)
        playerDestroy() // shows bug
        playerView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        playerViewDestroy()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.load)
            doLoad()
    }

    private fun doLoad() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { f ->
                playerDestroy() // so we can change effects
                uri = f
                start()
            }
        }
    }

    private fun start() {
       var playerView = this.playerView
        if (playerView == null) {
            playerView = PlayerView(this)
            this.playerView = playerView
            binding.frame.addView(playerView, 0)
            (playerView.layoutParams as FrameLayout.LayoutParams).let { lp ->
                lp.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                lp.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }

        var player = this.player
        if (player == null) {
            player = ExoPlayer.Builder(this).build()
            this.player = player
            player.playWhenReady = false
            player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
        }

        playerView.player = player

        val uri = this.uri
        if (uri != null) {
            player.setVideoEffects(listOf<Effect>(
                // any effects shows the bug
                Crop(-0.9f, 0.9f, -0.9f, 0.9f),
                //OverlayEffect(ImmutableList.Builder<TextureOverlay>().add(TextOverlay.createStaticTextOverlay(SpannableString(uri.lastPathSegment))).build())
            ))
            player.setMediaItem(MediaItem.Builder().setUri(uri).build())
            player.prepare()
            //player.play() // normal style. bug happens regardless
        }
    }

    private fun playerDestroy() {
        val player = this.player
        if (player != null) {
            player.stop()
            playerView?.player = null
            player.release() // this triggers the bug when called on the second player that we destroy
            this.player = null
            effects = null
        }
    }

    private fun playerViewDestroy() {
        playerDestroy()
        val playerView = this.playerView
        if (playerView != null) {
            playerView.player = null
            binding.frame.removeView(playerView)
            this.playerView = null
        }
    }

}
