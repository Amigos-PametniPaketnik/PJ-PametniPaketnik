package com.example.pametni_paketnik

import android.os.Bundle
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
import java.io.IOException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lateinit var data: String

        if (arguments?.containsKey("scan") == true)
            getTokenForParcelLocker(arguments?.getString("scan")!!)
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
            val client = OkHttpClient()
            val token = "9ea96945-3a37-4638-a5d4-22e89fbc998f"
            val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

            val payload = """{"boxId" : $boxId, "tokenFormat" : 2}"""

            val request = Request.Builder()
                .url("https://api-ms-stage.direct4.me/sandbox/v1/Access/openbox")
                .addHeader("Authorization", "Bearer " + token)
                .post(payload.toRequestBody(MEDIA_TYPE_JSON))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body!!.string()
                val jsonObject = JSONObject(responseData)
                val boxToken = jsonObject.getString("data") // Here is token base64 encoded and zipped token for playing to open a box
                println("Response from Direct4me: $responseData")
                //activity?.runOnUiThread { Toast.makeText(this.requireContext(), "Received from Direct4me: $boxToken", Toast.LENGTH_LONG).show() }
            }
        }).start()
    }
}