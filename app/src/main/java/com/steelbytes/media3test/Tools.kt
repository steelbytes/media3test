package com.steelbytes.media3test

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

object Tools {

    fun getFilename(uri:Uri?) : String? {
        var s:String? = null
        if (uri != null) {
            var c: Cursor? = null
            try {
                c = MyApp.app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                if (c != null && c.moveToNext())
                    s = c.getString(0)
            } catch (_: Throwable) {
            }
            closeCloseable(c)
            if (s.isNullOrEmpty())
                s = uri.lastPathSegment?.split("/")?.last()?:uri.toString()
        }
        return s
    }

    fun closeCloseable(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (_: Throwable) {
            }
        }
    }

    fun copyStream(inStream: InputStream, outStream: OutputStream) {
        val buf = ByteArray(1024 * 1024)
        while (true) {
            val l = inStream.read(buf)
            if (l <= 0)
                break
            outStream.write(buf, 0, l)
        }
    }
}