package com.example.pametni_paketnik

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentFirstBinding
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

private const val REQUEST_CODE = 42
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

        if (arguments?.containsKey("scan") == true)
            binding.textViewParcelLocker.text = "Scaned parcel locker: ${arguments?.getString("scan")}"
      //  if (arguments?.containsKey("image") == true)
        //    binding.imageView.setImageBitmap(arguments?.get("image") as Bitmap)
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_PictureFragment)
        }

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}