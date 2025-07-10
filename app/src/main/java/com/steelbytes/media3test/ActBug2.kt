package com.steelbytes.media3test

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Crop
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.steelbytes.media3test.databinding.Actbug2Binding
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

/*

Media3 1.8.0-beta01
Variable frame rate when transforming

*/

@Suppress("DEPRECATION")
@SuppressLint("UnsafeOptInUsageError")
class ActBug2 : ActBase(), View.OnClickListener {

    private lateinit var binding: Actbug2Binding
    private var fileIn:Uri? = null
    private var fileOut:Uri? = null
    private var tempFile = File(MyApp.app.cacheDir, "temp.mp4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Actbug2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.load.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == null)
            return
        when (v.id) {
            R.id.load -> getFileIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> data?.data?.let { f ->
                    gotFileIn(f)
                }
                2 -> data?.data?.let { f ->
                    gotFileOut(f)
                }
            }
        }
    }

    private fun getFileIn() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).also {
            it.type = "video/*"
            it.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
        }, 1)
    }

    private fun gotFileIn(f: Uri) {
        fileIn = f
        val s = Tools.getFilename(fileIn)
        val fn = if (!s.isNullOrEmpty()) {
            val i = s.lastIndexOf('.')
            if (i > 0)
                s.substring(0, i) + "-out.mp4"
            else
                "out.mp4"
        } else {
            "out.mp4"
        }
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).also {
            it.type = "video/mp4"
            it.putExtra(Intent.EXTRA_TITLE, fn)
        }, 2)
    }

    private fun gotFileOut(f: Uri) {
        fileOut = f
        doTransform()
    }

    private fun doTransform() {
        val effectsList = listOf(Crop(-0.9f, 0.9f, -0.9f, 0.9f)) // simple test effect
        val mediaItem = MediaItem.Builder().setUri(fileIn).build()
        val editedMediaItem = EditedMediaItem.Builder(mediaItem).setEffects(Effects(emptyList(), effectsList)).build()
        val composition = Composition.Builder(EditedMediaItemSequence.Builder().addItem(editedMediaItem).build()).build()
        val transformer = Transformer.Builder(this@ActBug2)
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .addListener(object : Transformer.Listener {
                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    killBusy()
                    Toast.makeText(this@ActBug2, "error " + exportException, Toast.LENGTH_SHORT).show()
                }
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    showBusy("Copying to destination")
                    var streamIn: InputStream? = null
                    var streamOut: OutputStream? = null
                    try {
                        val fileOut = this@ActBug2.fileOut!!
                        streamIn = FileInputStream(tempFile)
                        streamOut = contentResolver.openOutputStream(fileOut)!!
                        Tools.copyStream(streamIn, streamOut)
                        MediaScannerConnection.scanFile(MyApp.app, arrayOf(fileOut.path), null, null)
                        Toast.makeText(this@ActBug2, "done", Toast.LENGTH_SHORT).show()
                    } catch (e: Throwable) {
                        Toast.makeText(this@ActBug2, "error " + e, Toast.LENGTH_SHORT).show()
                    } finally {
                        killBusy()
                        Tools.closeCloseable(streamIn)
                        Tools.closeCloseable(streamOut)
                        tempFile.delete()
                    }
                }
            })
            .build()
        tempFile.delete()
        showBusy("Transforming")
        transformer.start(composition, tempFile.path)
    }
}
