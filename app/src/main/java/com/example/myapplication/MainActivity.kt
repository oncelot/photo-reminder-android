package com.example.myapplication

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


import kotlinx.coroutines.withContext

import java.util.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val service = Service();


        // Controlla se la scansione è necessaria
        //val externalpath = File(Environment.getExternalStorageDirectory(),  Environment.MEDIA_MOUNTED )

        scheduleDailyPhotoCheck(this)
        //requestPermissions(arrayOf(Manifest.permission.), 100)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Greeting(contentResolver)
                }
            }
        }
    }
data class PhotoData(val uri: String, val date: String)

fun scheduleDailyPhotoCheck(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<PhotoWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_photo_check",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
}

fun calculateInitialDelay(): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9) // Imposta l'orario desiderato (ad esempio, 9:00)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val now = Calendar.getInstance().timeInMillis
    val target = calendar.timeInMillis

    return if (target > now) {
        target - now
    } else {
        target + TimeUnit.DAYS.toMillis(1) - now
    }
}

@Composable
fun Greeting(contentResolver: ContentResolver) {

    val service= Service();
    val today = LocalDate.now()
    var day by remember { mutableStateOf(today.dayOfMonth) }
    var month by remember { mutableStateOf(today.monthValue) }
    var photos by remember { mutableStateOf(listOf<PhotoData>()) }
    PermissionHandler(contentResolver) {

        photos = service.getPhotosByDate(contentResolver, day, month);
    }
    RequestNotificationPermission{


    }
    val context = LocalContext.current



    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    var externalpath=service.getexternalPath(storageManager);
    val file = File(externalpath);
    val path = File(Environment.getExternalStorageDirectory(),
        Environment.MEDIA_MOUNTED )

    /*
    val uri: Uri = Uri.parse(externalpath+"/DCIM/Camera/IMG_20181013_092126.jpg")
    service.getExifDate(contentResolver, uri);

   if (service.shouldScanMedia(context)) {
            scanMediaAsync(
                context,
                listOf(externalpath+"/DCIM/Camera/DSC01068.JPG", externalpath+"/Pictures")
            )
       }
    val uri: Uri = Uri.parse(externalpath+"/DCIM/Camera/DSC01068.JPG")
   var id= service.getImageIdFromPath(contentResolver,externalpath+"/DCIM/Camera/DSC01068.JPG")
    var dataExif=   service.getExifDate(contentResolver,  MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build())
    */



    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) } // Indice della foto selezionata


    android.Manifest.permission.READ_EXTERNAL_STORAGE;
    android.Manifest.permission.READ_MEDIA_IMAGES;

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Button(
            onClick = {
                // Mostra il DatePicker
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // Aggiorna giorno e mese
                        day = selectedDay
                        month = selectedMonth + 1
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("${stringResource(R.string.selectData)}")

        }
        //   Button(onClick = {photos = getPhotosByDate(contentResolver,day,month) }){ Text("Cerca foto") }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.fillMaxSize(),

            ) {
            items(photos.size) { index ->
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = rememberAsyncImagePainter(photos[index].uri),
                        contentDescription = null,

                        modifier = Modifier.size(100.dp).padding(4.dp).clickable {
                            selectedPhotoIndex = index
                        } // Imposta la foto selezionata
                    )
                    Text(
                        text = photos[index].date,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (selectedPhotoIndex != null) {
            Dialog(onDismissRequest = { selectedPhotoIndex = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text =   "${stringResource(R.string.TitleDettaglioFoto)} ${photos[selectedPhotoIndex!!].date}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                        Image(
                            painter = rememberAsyncImagePainter(photos[selectedPhotoIndex!!].uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Mostra la foto precedente
                                    selectedPhotoIndex =
                                        (selectedPhotoIndex!! - 1 + photos.size) % photos.size
                                }
                            ) {
                                Text("<")
                            }
                            Button(onClick = { selectedPhotoIndex = null }) {
                                Text("X")
                            }
                            Button(onClick = {
                                // Apri la foto nell'album o nell'app di default
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(photos[selectedPhotoIndex!!].uri)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(intent)
                            }) {
                                Text("${stringResource(R.string.apri)}")
                            }
                            Button(
                                onClick = {
                                    // Mostra la foto successiva
                                    selectedPhotoIndex = (selectedPhotoIndex!! + 1) % photos.size
                                }
                            ) {
                                Text(">")
                            }
                        }
                    }
                }
            }
        }
    }


}





/**
 * Cerca in modo ricorsivo tutti i file immagine in una determinata directory e filtra quelli
 * che, in base ai metadati EXIF, sono stati scattati in un certo giorno e mese.
 *
 * @param context Il contesto per eventuali operazioni.
 * @param directory La directory (ad esempio la cartella della microSD) da scansionare.
 * @param targetDay Il giorno desiderato (es. 16).
 * @param targetMonth Il mese desiderato (es. 1 per gennaio).
 * @return Una lista di file che corrispondono al criterio.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(contentResolver: ContentResolver, onPermissionGranted: () -> Unit) {
val context = LocalContext.current
val permissionState = rememberPermissionState(
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      android.Manifest.permission.READ_MEDIA_IMAGES
  } else {
      android.Manifest.permission.READ_EXTERNAL_STORAGE
  }
)

// Gestire il permesso
LaunchedEffect(key1 = Unit) {
  if (!permissionState.status.isGranted) {
      permissionState.launchPermissionRequest()
  }
}

when {
  permissionState.status.isGranted -> {
      // Se il permesso è concesso, esegui l'azione
      onPermissionGranted()
  }
  permissionState.status.shouldShowRationale -> {
      // Mostra un messaggio personalizzato per spiegare perché è necessario
      Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
      ) {
          Text("${stringResource(R.string.accessoFoto)}")
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = { permissionState.launchPermissionRequest() }) {
              Text("${stringResource(R.string.concediPermesso)}")
          }
      }
  }
  else -> {
      // Caso in cui il permesso è stato negato definitivamente
      var showDialog by remember { mutableStateOf(true) } // Controlla se mostrare il popup

      if (showDialog) {

          AlertDialog(
              onDismissRequest = { showDialog = false },
              title = { Text(stringResource(R.string.permessoNegato)) },
              text = { Text(stringResource(R.string.vaiImpostazioniDescrizione)) },
              confirmButton = {
                  Button(onClick = {
                      showDialog = false
                      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                          data = Uri.fromParts("package", context.packageName, null)
                      }
                      context.startActivity(intent)
                  }) {
                      Text(stringResource(R.string.vaiImpostazioni))
                  }
              },
              dismissButton = {
                  Button(onClick = { showDialog = false }) {
                      Text(stringResource(R.string.annulla))
                  }
              }
          )
      }
  }
}
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission(onPermissionGranted: @Composable () -> Unit) {
val context = LocalContext.current
val notificationPermissionState = rememberPermissionState(
  permission = android.Manifest.permission.POST_NOTIFICATIONS
)

LaunchedEffect(Unit) {
  if (!notificationPermissionState.status.isGranted) {
      notificationPermissionState.launchPermissionRequest()
  }
}

when {
  notificationPermissionState.status.isGranted -> {
      onPermissionGranted()
  }
  notificationPermissionState.status.shouldShowRationale -> {
      Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
      ) {
          Text("${stringResource(R.string.concediPermessoNotifiche)}")
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = {

              notificationPermissionState.launchPermissionRequest()
          }) {
              Text("${stringResource(R.string.concediPermesso)}")
          }
      }
  }
  else -> {
      // Se il permesso è negato definitivamente
      var showDialog by remember { mutableStateOf(true) } // Controlla se mostrare il popup

      if (showDialog) {

      AlertDialog(
          onDismissRequest = { showDialog = false },
          title = { Text(stringResource(R.string.concediPermessoNotificheNegato)) },
          text = { Text(stringResource(R.string.vaiImpostazioniDescrizione)) },
          confirmButton = {
              Button(onClick = {
                  showDialog = false
                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                      data = Uri.fromParts("package", context.packageName, null)
                  }
                  context.startActivity(intent)
              }) {
                  Text(stringResource(R.string.vaiImpostazioni))
              }
          },
          dismissButton = {
              Button(onClick = { showDialog = false }) {
                  Text(stringResource(R.string.annulla))
              }
          }
      )
  }
  }
}
}


fun showNotification(context: Context, photoUri: String, title: String, message: String) {

val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val channelId = "daily_photos_channel"

// Crea un canale di notifica (necessario per Android 8+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
  val channel = NotificationChannel(
      channelId,
      "Photo of the day",
      NotificationManager.IMPORTANCE_HIGH
  ).apply {
      description = "Daily notifications with photos taken on this date in past years."
  }
  notificationManager.createNotificationChannel(channel)
}

// Crea un PendingIntent per aprire la foto nell'app predefinita
val intent = Intent(context, MainActivity::class.java).apply {
  flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

// Carica l'immagine per la notifica
val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(photoUri))

// Crea la notifica
val notification = NotificationCompat.Builder(context, channelId)
  .setContentTitle(title)
  .setContentText(message)
  .setSmallIcon(R.drawable.ic_stat_name) // Sostituisci con un'icona valida
  .setContentIntent(pendingIntent)
  .setAutoCancel(true)
  .setStyle(NotificationCompat.BigPictureStyle()
      .bigPicture(bitmap)
      )
  .build()

notificationManager.notify(1, notification)
}


@androidx.annotation.OptIn(UnstableApi::class)
fun scanMediaAsync(context: Context, paths: List<String>) {

  MediaScannerConnection.scanFile(
      context, paths.toTypedArray(), null)
       {
                                                        path, uri -> Log.d("MediaScanner", "Scansione completata: $path -> $uri")}

}


