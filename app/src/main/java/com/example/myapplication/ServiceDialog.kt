package com.example.myapplication

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

class ServiceDialog {

    @Composable
    fun DialogPreNotifiche(context: Context,onDismiss: () -> Unit){

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(stringResource(R.string.attenzione)) },
            text = { Text(stringResource(R.string.textPrenotifice)) },
            confirmButton = {

            },
            dismissButton = {
                Button(onClick = {  onDismiss()  }) {
                    Text(stringResource(R.string.okclose))
                }
            }
        )
    }

}