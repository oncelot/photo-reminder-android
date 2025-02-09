package com.example.myapplication

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Service {

    fun getPhotosByDate(contentResolver: ContentResolver, day: Int, month: Int): List<PhotoData> {

        val photos = mutableListOf<PhotoData>()


        var anni=20;

        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.DATE_TAKEN)
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} < ?"
        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var selectionArgs = arrayOf("", "")

        for (i in 0..anni){


        val dateString = "${String.format("%02d", day)}/${ String.format("%02d", month)}/${currentYear-i}"
        val dateString2 = "${String.format("%02d", day+1)}/${ String.format("%02d", month)}/${currentYear-i}"

        val targetDate: Date? = dateFormatter.parse(dateString)
        val targetDate2: Date? = dateFormatter.parse(dateString2)
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        targetDate?.let {
            calendar.time = it
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
        targetDate2?.let {
            calendar2.time = it
            calendar2.set(Calendar.HOUR_OF_DAY, 0)
            calendar2.set(Calendar.MINUTE, 0)
            calendar2.set(Calendar.SECOND, 0)
            calendar2.set(Calendar.MILLISECOND, 0)
        }

        val startTimestamp = calendar.timeInMillis
        val endTimestamp = calendar2.timeInMillis

         selectionArgs = arrayOf(startTimestamp.toString(), endTimestamp.toString())

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor: Cursor? = contentResolver.query(queryUri, null, selection, selectionArgs, sortOrder)

        cursor?.use {

            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val idColumn2 = it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)


            while (it.moveToNext()) {
                val dateTaken = it.getLong(dateColumn)
                val id = it.getLong(idColumn)
                val path = it.getLong(idColumn2)
                var dataExif="";
                //getExifDate(contentResolver, uri)

                var calendar = java.util.Calendar.getInstance().apply { timeInMillis = dateTaken }
                /* if (calendar.get(java.util.Calendar.YEAR) == 1970){
                     dataExif=   service.getExifDate(contentResolver,  MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build())

                      calendar = java.util.Calendar.getInstance().apply { time = dateFormatter.parse(dataExif) }
                 }*/
                // Log.d("MyTag", calendar.get(java.util.Calendar.DAY_OF_MONTH).toString()  + calendar.get(java.util.Calendar.MONTH).toString() + calendar.get(java.util.Calendar.YEAR).toString());

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
                val dateString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
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



    fun getImageIdFromPath(contentResolver: ContentResolver, imagePath: String): Long? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(imagePath)

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            }
        }
        return null // Se non trova l'immagine
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
    fun getexternalPath(storageManager: StorageManager):String{

        var externalpath="";
        val storageVolumes: List<StorageVolume> = storageManager.storageVolumes
        for (volume in storageVolumes) {
            val path = volume.directory?.absolutePath
            if (path != null && !path.contains("emulated")) {
                externalpath = path // Questo è il percorso della microSD
            }
        }
return  externalpath;
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