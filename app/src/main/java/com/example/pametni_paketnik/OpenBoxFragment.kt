package com.example.pametni_paketnik

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentFirstBinding
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
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.example.pametni_paketnik.models.Unlocked
import com.google.android.material.snackbar.Snackbar


import java.util.zip.ZipEntry

import java.util.zip.ZipInputStream

import java.io.FileInputStream
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */


class OpenBoxFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var openBoxViewModel: OpenBoxViewModel
    private lateinit var userViewModel: UserViewModel

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
        openBoxViewModel = ViewModelProvider(this).get(OpenBoxViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        openBoxViewModel.getTokenResult.observe(viewLifecycleOwner, Observer { getTokenResult ->
            getTokenResult ?: return@Observer
            //loadingProgressBar.visibility = View.GONE
            getTokenResult.error?.let {
                Toast.makeText(requireContext(), "Nimate dovoljenja za odklep tega paketnika!", Toast.LENGTH_LONG).show()
            }
            getTokenResult.success?.let {
                playToken(it)
            }
        })

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
        val user =  userViewModel.user.value
        var splitBoxId = boxId.split('/')[1]
        openBoxViewModel.openBox(user!!, splitBoxId)
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
                alertDialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                    openBoxViewModel.saveNewUnlock(Unlocked("", userViewModel.user.value!!.id, Date(), true, ""))
                })
                alertDialogBuilder.setNegativeButton("No", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                    openBoxViewModel.saveNewUnlock(Unlocked(openBoxViewModel.idParcelLocker.value!!, userViewModel.user.value!!.id, Date(), false, ""))
                })
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