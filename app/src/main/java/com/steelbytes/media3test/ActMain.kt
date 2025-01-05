package com.steelbytes.media3test

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.common.collect.ImmutableList
import com.steelbytes.media3test.databinding.ActmainBinding

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@SuppressLint("UnsafeOptInUsageError")
class ActMain : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActmainBinding
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var uri:Uri? = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.load.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == null)
            return
        when (v.id) {
            R.id.load -> doLoad()
        }
    }

    override fun onResume() {
        super.onResume()
        doStart()
    }

    override fun onPause() {
        doStop()
        super.onPause()
    }

    private fun doLoad() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { f ->
                        contentResolver.takePersistableUriPermission(f, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        doStop()
                        uri = f
                        doStart()
                    }
                }
            }
        }
    }

    private fun doStart() {
        if (uri == null)
            return
        var player = player
        if (player != null) return

        player = ExoPlayer.Builder(this).build()
        this.player = player
        player.playWhenReady = false
        player.repeatMode = ExoPlayer.REPEAT_MODE_OFF

        val playerView = PlayerView(this)
        this.playerView = playerView
        binding.frame.addView(playerView, 0)
        (playerView.layoutParams as FrameLayout.LayoutParams).let { lp ->
            lp.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            lp.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        }
        playerView.player = player

        val effects = ArrayList<Effect>()
        effects.add(OverlayEffect(ImmutableList.Builder<TextureOverlay>().add(TextOverlay.createStaticTextOverlay(SpannableString("test"))).build()))
        player.setVideoEffects(effects)
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun doStop() {
        val player = player ?: return
        val playerView = this.playerView ?: return
        player.stop()
        player.release()
        playerView.player = null
        binding.frame.removeView(playerView)
        this.player = null
        this.playerView = null
    }

}
