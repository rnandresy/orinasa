package com.orinasa.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// ⚠️ Remplace par tes valeurs Cloudinary
private const val CLOUD_NAME    = "di6bq2h1d"
private const val UPLOAD_PRESET = "orinasa"

object CloudinaryUploader {

    private const val BOUNDARY   = "OrinasaBoundary1A2E"
    private const val LINE_END   = "\r\n"
    private const val TWO_DASHES = "--"

    // ── Image ─────────────────────────────────────────────────────────────────
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        onProgress: ((Int) -> Unit)? = null
    ): String {
        val (bytes, mime, name) = readUri(context, uri)
        return uploadBytes(bytes, mime, name, endpointFor("image"), onProgress)
    }

    // ── Vidéo ─────────────────────────────────────────────────────────────────
    suspend fun uploadVideo(
        context: Context,
        uri: Uri,
        onProgress: ((Int) -> Unit)? = null
    ): String {
        val (bytes, mime, name) = readUri(context, uri)
        return uploadBytes(bytes, mime, name, endpointFor("video"), onProgress)
    }

    // ── Audio (fichier local après enregistrement) ────────────────────────────
    suspend fun uploadAudio(
        file: File,
        onProgress: ((Int) -> Unit)? = null
    ): String {
        val bytes = file.readBytes()
        return uploadBytes(bytes, "audio/m4a", file.name, endpointFor("video"), onProgress)
    }

    // ── Fichier quelconque ────────────────────────────────────────────────────
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        onProgress: ((Int) -> Unit)? = null
    ): Pair<String, String> {
        val (bytes, mime, name) = readUri(context, uri)
        val url = uploadBytes(bytes, mime, name, endpointFor("raw"), onProgress)
        return url to name
    }

    // ── Interne ───────────────────────────────────────────────────────────────

    private fun endpointFor(type: String) =
        "https://api.cloudinary.com/v1_1/$CLOUD_NAME/$type/upload"

    private data class UriData(val bytes: ByteArray, val mime: String, val name: String)

    private fun readUri(context: Context, uri: Uri): UriData {
        val bytes = context.contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
            ?: error("Impossible de lire le fichier")
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val name = getFileName(context, uri)
        return UriData(bytes, mime, name)
    }

    private suspend fun uploadBytes(
        bytes: ByteArray,
        mime: String,
        fileName: String,
        apiUrl: String,
        onProgress: ((Int) -> Unit)?
    ): String = withContext(Dispatchers.IO) {

        val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            doInput        = true
            doOutput       = true
            useCaches      = false
            requestMethod  = "POST"
            connectTimeout = 30_000
            readTimeout    = 180_000
            setRequestProperty("Connection", "Keep-Alive")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
        }

        DataOutputStream(conn.outputStream).use { out ->
            // upload_preset
            out.writeBytes("$TWO_DASHES$BOUNDARY$LINE_END")
            out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"$LINE_END")
            out.writeBytes(LINE_END)
            out.writeBytes(UPLOAD_PRESET)
            out.writeBytes(LINE_END)

            // file
            out.writeBytes("$TWO_DASHES$BOUNDARY$LINE_END")
            out.writeBytes(
                "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$LINE_END"
            )
            out.writeBytes("Content-Type: $mime$LINE_END")
            out.writeBytes(LINE_END)

            // Écriture par chunks avec progression
            val chunkSize = 8192
            var offset    = 0
            while (offset < bytes.size) {
                val end = minOf(offset + chunkSize, bytes.size)
                out.write(bytes, offset, end - offset)
                offset = end
                onProgress?.invoke(offset * 100 / bytes.size)
            }

            out.writeBytes(LINE_END)
            out.writeBytes("$TWO_DASHES$BOUNDARY$TWO_DASHES$LINE_END")
            out.flush()
        }

        val code = conn.responseCode
        if (code != HttpURLConnection.HTTP_OK) {
            val err = conn.errorStream
                ?.let { BufferedReader(InputStreamReader(it)).readText() }
                ?: "Erreur $code"
            error("Upload Cloudinary échoué ($code) : $err")
        }

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        conn.disconnect()
        JSONObject(body).getString("secure_url")
    }

    private fun getFileName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (col >= 0 && cursor.moveToFirst()) cursor.getString(col) else null
        }
    }.getOrNull() ?: "file_${System.currentTimeMillis()}"
}