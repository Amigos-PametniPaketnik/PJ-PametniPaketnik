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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pametni_paketnik.data.model.LoggedInUser
import com.example.pametni_paketnik.ui.login.LoggedInUserView


private const val FILE_NAME = "photo"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BiometricLoginFragment : Fragment() {
    private var _binding: FragmentPictureBinding? = null
    private lateinit var biometricLoginViewModel: BiometricLoginViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var app: MyApplication

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
        biometricLoginViewModel = ViewModelProvider(this).get(BiometricLoginViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        app = requireActivity().application as MyApplication

        biometricLoginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                binding.progressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })

            if (allPermissionsGranted()) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(FILE_NAME)

               val fileProvider = FileProvider.getUriForFile(this.requireContext(), "com.example.android.fileprovider", photoFile)
               takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider) //comment this and result line for lower quality image

                if (takePictureIntent.resolveActivity(this.requireContext().packageManager) != null) {
                    resultLauncher.launch(takePictureIntent)
                  //  findNavController().navigate(R.id.action_PictureFragment_to_FirstFragment)
                }
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
          //val data: Bitmap = result.data?.extras?.get("data") as Bitmap
            val data = BitmapFactory.decodeFile(photoFile.absolutePath) //comment this and use above line for lower quality image
            biometricLoginViewModel.authenticateWithPhoto(photoFile)
            Toast.makeText(requireContext(), "Poteka indentifikacija na podlagi slike...", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.VISIBLE
            //binding.imageView2.setImageBitmap(data)
        }
        else{
            findNavController().navigate(R.id.action_PictureFragment_to_FirstFragment)
        }
    }

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

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + " " + model.name
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return

        val loggedInUser = LoggedInUser(model.id, model.username, model.accesstoken, model.name, model.lastname, model.email)
        app.saveAccessToken(model.accesstoken)
        app.saveUserID(model.id)
        userViewModel.userLoggedIn(loggedInUser)
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_PictureFragment_to_menuFragment)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_PictureFragment_to_loginFragment)
    }

}