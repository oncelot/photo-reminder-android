package com.example.myapplication

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Service {

    fun getPhotosByDate(contentResolver: ContentResolver, day: Int, month: Int): List<PhotoData> {
        val photos = mutableListOf<PhotoData>()
        val uriList = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )

        val selection = "strftime('%d', datetime(${MediaStore.Images.Media.DATE_TAKEN} / 1000, 'unixepoch')) = ? AND " +
                "strftime('%m', datetime(${MediaStore.Images.Media.DATE_TAKEN} / 1000, 'unixepoch')) = ?"
        val selectionArgs = arrayOf(
            String.format("%02d", day),
            String.format("%02d", month)
        )

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val cursor: Cursor? = contentResolver.query(queryUri, projection, null, null, sortOrder)

        cursor?.use {
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (it.moveToNext()) {
                val dateTaken = it.getLong(dateColumn)
                val id = it.getLong(idColumn)
                var dataExif="";
                //getExifDate(contentResolver, uri)

                var calendar = java.util.Calendar.getInstance().apply { timeInMillis = dateTaken }
                 if (calendar.get(java.util.Calendar.YEAR) == 1970){
                     dataExif=   getExifDate(contentResolver,  MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build())

                      calendar = java.util.Calendar.getInstance().apply { time = dateFormatter.parse(dataExif) }
                 }
                // Log.d("MyTag", calendar.get(java.util.Calendar.DAY_OF_MONTH).toString()  + calendar.get(java.util.Calendar.MONTH).toString() + calendar.get(java.util.Calendar.YEAR).toString());
                if (calendar.get(java.util.Calendar.DAY_OF_MONTH) == day &&
                    calendar.get(java.util.Calendar.MONTH) + 1 == month
                ) {
                    val formattedDate = dateFormatter.format(Date(dateTaken))
                    val photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(id.toString()).build().toString()

                    photos.add(PhotoData(uri = photoUri, date = formattedDate))

                }
            }
        }

        /* }*/
        return photos
    }

    fun getExifDate(contentResolver: ContentResolver, uri: Uri): String {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val dateString = exif.getAttribute(ExifInterface.TAG_DATETIME)
                if (dateString != null) {
                    val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    val date = sdf.parse(dateString)
                    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Data sconosciuta"
    }
    fun getWhatsAppPhotos(): List<PhotoData> {
        val photos = mutableListOf<PhotoData>()
        val whatsAppFolder = File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images")
// val whatsAppFolder2 = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Images")
        whatsAppFolder.listFiles()?.forEach { file ->
            if (file.extension.lowercase() in listOf("jpg", "jpeg", "png")) {
                photos.add(PhotoData(file.toURI().toString(), file.lastModified().toString()))
            }
        }
        return photos
    }

    fun shouldScanMedia(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("media_scanned", true) // Di default, scansiona solo la prima volta
    }

    fun setMediaScanned(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("media_scanned", false).apply()
    }

}