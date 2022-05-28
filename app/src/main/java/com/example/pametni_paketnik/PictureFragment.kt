package com.example.pametni_paketnik

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentFirstBinding
import com.example.pametni_paketnik.databinding.FragmentPictureBinding
import com.example.pametni_paketnik.databinding.FragmentSecondBinding
import java.io.File
import android.content.ContentValues

import android.net.Uri




private const val FILE_NAME = "photo"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PictureFragment : Fragment() {
    private var _binding: FragmentPictureBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            if (allPermissionsGranted()) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(FILE_NAME)

               val fileProvider = FileProvider.getUriForFile(this.requireContext(), "com.example.android.fileprovider", photoFile)
               takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

                if (takePictureIntent.resolveActivity(this.requireContext().packageManager) != null) {
                  requireActivity().startActivityForResult(takePictureIntent, REQUEST_CODE)
                    val bundle = bundleOf("image" to takePictureIntent)
                    findNavController().navigate(R.id.action_PictureFragment_to_FirstFragment)
                }
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }
/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            //   val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
          //  binding.imageView.setImageBitmap(takenImage)
            val bundle = bundleOf("image" to takenImage)
            findNavController().navigate(R.id.action_PictureFragment_to_FirstFragment, bundle)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
*/
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = this.requireContext()?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg",storageDirectory)
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in CameraHelper.REQUIRED_PERMISSIONS) {
            if (context?.let {
                    ContextCompat.checkSelfPermission(
                        it, permission
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}