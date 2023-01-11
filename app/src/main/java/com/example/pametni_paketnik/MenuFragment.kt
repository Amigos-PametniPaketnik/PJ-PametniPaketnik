package com.example.pametni_paketnik

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pametni_paketnik.databinding.FragmentMenuBinding
import com.example.pametni_paketnik.databinding.FragmentProfileBinding


class MenuFragment : Fragment() {

    companion object {
        fun newInstance() = MenuFragment()
    }

    private lateinit var userViewModel: UserViewModel
    private var _binding: FragmentMenuBinding? = null
    private lateinit var app: MyApplication

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireActivity().application as MyApplication
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)



        binding.buttonMap.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_ParcelLockerMapFragment)
        }
        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_profileFragment)
        }
        binding.buttonScan.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_FirstFragment)
        }
        binding.buttonDistance.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_CitysFragment)
        }
    }

}