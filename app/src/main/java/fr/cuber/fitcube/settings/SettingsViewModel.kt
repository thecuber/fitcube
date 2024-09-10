package fr.cuber.fitcube.settings

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {

    fun downloadDatabase(context: Context) = viewModelScope.launch {
        val db = context.getDatabasePath("fitcube_database")
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val downloadFile = File(downloadFolder, "fitcube")
        db.copyTo(downloadFile, true)
    }

    fun uploadDatabase(file: Uri, context: Context) = viewModelScope.launch {
        FileOutputStream(context.getDatabasePath("fitcube_database")).use { output ->
            context.contentResolver.openInputStream(file)?.use { input ->
                input.copyTo(output)
            }
        }
    }

}