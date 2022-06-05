package com.example.pametni_paketnik

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.databinding.FragmentFirstBinding
import com.example.pametni_paketnik.databinding.FragmentUnlocksBinding

class UnlocksFragment : Fragment() {

    private var _binding: FragmentUnlocksBinding? = null
    private lateinit var viewModel: UnlocksViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: RecyclerView.Adapter<UnlockAdapter.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUnlocksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(UnlocksViewModel::class.java)
        if (arguments != null) {
            if (requireArguments().containsKey("idParcelLocker") && requireArguments().containsKey("numberParcelLocker")) {
                viewModel.loadUnlocksForBox(requireArguments().getString("idParcelLocker")!!)
                viewModel.unlocks.observe(viewLifecycleOwner, Observer { unlocks ->
                    adapter = UnlockAdapter(unlocks!!, object: UnlockAdapter.MyOnClick {
                        override fun onClick(p0: View?, position: Int) {
                            Toast.makeText(requireContext(), "Short Click!", Toast.LENGTH_SHORT).show()
                        }

                        override fun onLongClick(p0: View?, position: Int) {
                            Toast.makeText(requireContext(), "Long click!", Toast.LENGTH_SHORT).show()
                        }
                    })
                    binding.recyclerView.adapter = adapter
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

                })

            }
        }
        else
            findNavController().navigate(R.id.action_unlocksFragment_to_profileFragment)
    }

}