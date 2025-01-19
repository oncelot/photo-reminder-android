package com.example.myapplication

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate

class PhotoWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val contentResolver = applicationContext.contentResolver
        var today=LocalDate.now();
        val day = today.dayOfMonth // Puoi cambiare con il giorno corrente
        val month = today.monthValue// Puoi cambiare con il mese corrente

        // Ottieni le foto del giorno
        val photos = getPhotosByDate(contentResolver, day, month)

        if (photos.isNotEmpty()) {
            // Prendi la prima foto e invia una notifica
            val photo = photos.first()
            showNotification(
                applicationContext,
                photo.uri,
                "Foto del giorno",
                "Guarda una foto scattata il $day/$month negli anni passati!"
            )
        }

        return Result.success()
    }
}
