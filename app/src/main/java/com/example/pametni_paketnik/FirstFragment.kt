package com.example.pametni_paketnik

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Base64.decode
import android.util.JsonReader
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentFirstBinding
import com.example.pametni_paketnik.util.ApiRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.FileOutputStream
import android.media.MediaPlayer
import android.net.Uri
import com.google.android.material.snackbar.Snackbar


import java.util.zip.ZipEntry

import java.util.zip.ZipInputStream

import java.io.FileInputStream




/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lateinit var data: String

        if (arguments?.containsKey("scan") == true) {
            try {
                getTokenForParcelLocker(arguments?.getString("scan")!!)
            }
            catch (e: Exception) {
                Snackbar.make(this.requireView(), "${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getTokenForParcelLocker(boxId: String) {
        Thread(Runnable {
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

                    val boxTokenBytes: ByteArray = Base64.decode(boxToken, Base64.DEFAULT)
                    var fileNameZip = writeBytesAsZip(boxTokenBytes)
                    var SoundFileName = unzip(
                        "/data/data/com.example.pametni_paketnik/token/" + fileNameZip,
                        "/data/data/com.example.pametni_paketnik/token"
                    )

                    //activity?.runOnUiThread { Toast.makeText(this.requireContext(), "Received from Direct4me: $boxToken", Toast.LENGTH_LONG).show() }
                    playToken(SoundFileName);

                }
            }
            catch (e: Exception) {
                activity?.runOnUiThread { Snackbar.make(this.requireView(), "${e.message}", Snackbar.LENGTH_LONG).show() }
            }
        }).start()
    }
    fun unzip(_zipFile: String?, _targetLocation: String):String {

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
            return file.name
        } catch (e: Exception) {
            println("unzip error: "+ e)
        }
        return ""
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

    private var mediaPlayer: MediaPlayer? = null

    fun playToken(tokenPath: String) {
        if (mediaPlayer == null) {
            println(tokenPath)
            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse("/data/data/com.example.pametni_paketnik/token/$tokenPath")) //get base64 decoded and unziped wav token
            mediaPlayer!!.setOnCompletionListener(MediaPlayer.OnCompletionListener { // When playback of token is over ask user if his box has opened
                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setTitle("Package opening")
                alertDialogBuilder.setMessage("Has the package opened?")
                alertDialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()})
                alertDialogBuilder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            })
            mediaPlayer!!.start()
            this.activity?.runOnUiThread { Snackbar.make(this.requireView(), "Playing token", mediaPlayer!!.duration).show() }
        } else mediaPlayer!!.start()
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}