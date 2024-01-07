package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.util.Log
import dev.seabat.android.usbdebugswitch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class LicenseListCreator(val context: Context) {
    suspend fun create(): List<LibraryLicense> {
        val licenses = loadLibraries().map {
            LibraryLicense(it.name, loadLicense(it))
        }
        return licenses
    }

    private suspend fun loadLibraries(): List<Library> {
        return withContext(Dispatchers.IO) {
            val inputSteam = context.resources.openRawResource(R.raw.third_party_license_metadata)
            inputSteam.use { inputSteam ->
                val reader = BufferedReader(InputStreamReader(inputSteam, "UTF-8"))
                reader.use { bufferedReader ->
                    val libraries = mutableListOf<Library>()
                    while (true) {
                        val line = bufferedReader.readLine() ?: break
                        val (position, name) = line.split(' ', limit = 2)
                        val (offset, length) = position.split(':').map { it.toInt() }
                        libraries.add(Library(name, offset, length))
                    }
                    libraries.toList()
                }
            }
        }
    }

    private suspend fun loadLicense(library: Library): String {
        Log.d("LicenseList", "${library.name} ${library.offset} ${library.length}")
        return withContext(Dispatchers.IO) {
            val charArray = CharArray(library.length)
            val inputStream = context.resources.openRawResource(R.raw.third_party_licenses)
            inputStream.use { stream ->
                val bufferedReader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                bufferedReader.use { reader ->
                    reader.skip(library.offset.toLong())
                    reader.read(charArray, 0, library.length)
                }
            }
            Log.d("LicenseList", "${charArray}")
            String(charArray)
        }
    }
}

data class Library(
    val name: String,
    val offset: Int,
    val length: Int,
)

data class LibraryLicense(
    val name: String,
    val terms: String //条文
)