package com.example.pametni_paketnik

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ScanQRCodeFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private lateinit var cameraHelper: CameraHelper

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraHelper = CameraHelper(
            owner = this.activity as AppCompatActivity,
            context = this.requireContext(),
            viewFinder = binding.cameraView,
            onResult = ::onResult
        )

        cameraHelper.start()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun onResult(result: String) {
        Log.d(TAG, "Result is $result")
        binding.textResult.text = result
        Toast.makeText(this.requireContext(), "Scaned barcode is: $result", Toast.LENGTH_SHORT).show()
        cameraHelper.stop()
        val bundle = bundleOf("scan" to result)
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment, bundle)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        cameraHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val TAG = "CameraXDemo"
    }
}