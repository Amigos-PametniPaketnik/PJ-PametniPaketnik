package com.example.pametni_paketnik

import android.app.Application
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.pametni_paketnik.data.Result
import com.example.pametni_paketnik.data.model.LoggedInUser
import com.example.pametni_paketnik.models.Unlocked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class OpenBoxViewModel(val _app: Application): AndroidViewModel(_app) {
    val app = _app as MyApplication
    private val _getTokenResult = MutableLiveData<GetTokenResult>()
    val getTokenResult: LiveData<GetTokenResult> = _getTokenResult
    private val _saveUnlockResult = MutableLiveData<SaveUnlockResult>()
    private val _authOpener = MutableLiveData<Boolean?>(null)
    val authOpener: LiveData<Boolean?> = _authOpener
    val saveUnlockResult: LiveData<SaveUnlockResult> = _saveUnlockResult
    private val _idParcelLocker = MutableLiveData<String?>(null)
    val idParcelLocker: LiveData<String?> = _idParcelLocker
    private val _loggedUser = MutableLiveData<LoggedInUser>()
    val loggedInUser: LiveData<LoggedInUser> = _loggedUser

    fun openBox(user: LoggedInUser, parcelLocker: String) {
        viewModelScope.launch {
            val authOpenerResult = try {
                authenticateOpener(user, parcelLocker)
            } catch (e: Exception) {
                Result.Error(e)
            }

           when(authOpenerResult) {
               is Result.Success -> {
                   _idParcelLocker.value = authOpenerResult.data.getString("_id")
                   val result = try {
                       getTokenAndProcess(parcelLocker)
                   }
                   catch (e: Exception) {
                       Result.Error(e)
                   }

                   when(result) {
                       is Result.Success -> {
                           _getTokenResult.value = GetTokenResult(success = result.data)
                       }
                       else -> {
                           _getTokenResult.value = GetTokenResult(error = "Getting and processing of token failed!")
                       }
                   }
               }
               else -> {
                   _getTokenResult.value = GetTokenResult(error = "You don't have permission to open this box")
               }
           }
        }
    }

    private suspend fun getTokenAndProcess(scanedQrCode: String): Result<String> {
        val rawBoxTokenBase64Zip = getToken(scanedQrCode)

        if (rawBoxTokenBase64Zip is Result.Success) {
            val boxTokenBytes: ByteArray = Base64.decode(rawBoxTokenBase64Zip.data, Base64.DEFAULT)
            var fileNameZip = writeBytesAsZip(boxTokenBytes)
            var soundFileName = unzip(
                "/data/data/com.example.pametni_paketnik/token/" + fileNameZip,
                "/data/data/com.example.pametni_paketnik/token"
            )
            return soundFileName
        }
        return rawBoxTokenBase64Zip
    }

    suspend fun getToken(scanedQrCode: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = "9ea96945-3a37-4638-a5d4-22e89fbc998f"
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

                val payload = """{"boxId" : "000542", "tokenFormat" : 2}"""

                val request = Request.Builder()
                    .url("https://api-ms-stage.direct4.me/sandbox/v1/Access/openbox")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(payload.toRequestBody(MEDIA_TYPE_JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    val jsonObject = JSONObject(responseData)
                    val boxToken =
                        jsonObject.getString("data") // Here is token base64 encoded and zipped token for playing to open a box
                    return@use Result.Success(boxToken)
                }
            }
            catch (e: Throwable) {
                return@withContext Result.Error(IOException(e.message))
            }
        }
    }
    fun unzip(_zipFile: String?, _targetLocation: String): Result<String> {

        //create target location folder if not exist
        val f = File(_targetLocation)
        if(!f.isDirectory()){
            f.mkdir()
        }
        try {
            val fin = FileInputStream(_zipFile)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry? = null
            val path = File("/data/data/com.example.pametni_paketnik/token")
            var file = File.createTempFile("token",".wav", path)
            while (zin.nextEntry.also { ze = it } != null) {
                val fout = FileOutputStream(file)
                var c = zin.read()
                while (c != -1) {
                    fout.write(c)
                    c = zin.read()
                }
                zin.closeEntry()
                fout.close()
            }
            zin.close()
            return Result.Success(file.name)
        } catch (e: Exception) {
            println("Error occured while unzipping token: "+ e)
            return Result.Error(e)
        }
    }

    fun writeBytesAsZip(bytes : ByteArray):String {
        val path = File("/data/data/com.example.pametni_paketnik/token")
        if(!path.isDirectory()){
            path.mkdir()
        }

        var file = File.createTempFile("token",".zip", path) // unzip base64 decoded token
        var os = FileOutputStream(file);
        os.write(bytes);
        os.close();
        return file.name
    }

    fun saveNewUnlock(unlocked: Unlocked) {
        viewModelScope.launch {
            val result = try {
                postNewUnlock(unlocked)
            }
            catch (e: Exception) {
                Result.Error(e)
            }

            when(result) {
                is Result.Success -> {
                    _saveUnlockResult.value = SaveUnlockResult(success = result.data)
                }
                else -> {
                    _saveUnlockResult.value = SaveUnlockResult(error = "Getting and processing of token failed!")
                }
            }
        }
    }

    private suspend fun postNewUnlock(unlocked: Unlocked): Result<Unlocked> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = app.getAccessToken()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

                val payload = """{"idParcelLocker" : "${_idParcelLocker.value}", "idUser" : "${unlocked.idUser}", "opened" : ${unlocked.opened}}"""
                Log.e("Payload: ", payload)

                val request = Request.Builder()
                    .url("http://192.168.1.104:3001/api/unlocks")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(payload.toRequestBody(MEDIA_TYPE_JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    val jsonObject = JSONObject(responseData)
                    val unlock = Unlocked(jsonObject.getString("idParcelLocker"), jsonObject.getString("idUser"), Date(), jsonObject.getBoolean("opened"), jsonObject.getString("_id"))
                    return@use Result.Success(unlock)
                }
            }
            catch (e: Throwable) {
                return@withContext Result.Error(IOException(e.message))
            }
        }
    }

    fun checkOpenerPremission(user: LoggedInUser, parcelLocker: String) {
        viewModelScope.launch {
            val result = try {
                authenticateOpener(user, parcelLocker)
            }
            catch (e: Exception) {
                Result.Error(e)
            }

            when(result) {
                is Result.Success -> {
                    _authOpener.value = true
                    _idParcelLocker.value = result.data.getString("_id")
                }
                else -> {
                    _authOpener.value = false
                    _idParcelLocker.value = null
                }
            }
        }
    }

    private suspend fun authenticateOpener(user: LoggedInUser, parcelLocker: String): Result<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = app.getAccessToken()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

                val payload = """{"idParcelLocker" : "${parcelLocker}"}"""

                val request = Request.Builder()
                    .url("http://192.168.1.104:3001/api/users/hasPremissionToOpen/"+user.id)
                    .addHeader("Authorization", "Bearer " + token)
                    .post(payload.toRequestBody(MEDIA_TYPE_JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.e("Response", "${response.body.toString()}")
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    val jsonObject = JSONObject(responseData)
                    return@use Result.Success(jsonObject)
                }
            }
            catch (e: Throwable) {
                return@withContext Result.Error(IOException(e.message))
            }
        }
    }
    fun setLoggedUser(loggedInUser: LoggedInUser) {
        _loggedUser.value = loggedInUser
    }
}