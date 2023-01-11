package com.example.pametni_paketnik

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.databinding.FragmentCitysBinding
import org.osmdroid.config.Configuration
import timber.log.Timber
import java.io.InputStream
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pametni_paketnik.matrixTSP.GA
import com.example.pametni_paketnik.matrixTSP.RandomUtils
import com.example.pametni_paketnik.matrixTSP.TSP
import com.example.pametni_paketnik.matrixTSP.TSP.Tour
import com.example.pametni_paketnik.matrixTSP.location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.io.InputStreamReader

import java.io.BufferedReader
import java.util.ArrayList


class CitysFragment : Fragment() {

    private var _binding: FragmentCitysBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CitysViewModel
    private lateinit var adapter: RecyclerView.Adapter<CitysAdapter.ViewHolder>
    private lateinit var navController: NavController
    private lateinit var citysJson: String
    private lateinit var app: MyApplication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCitysBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CitysViewModel::class.java)
        navController = Navigation.findNavController(view)
        app = requireActivity().application as MyApplication

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Configuration.getInstance()
            .load(requireActivity().applicationContext, requireActivity().getPreferences(Context.MODE_PRIVATE))
       val binding = FragmentCitysBinding.inflate(layoutInflater) //ADD THIS LINE


        requireActivity().setContentView(binding.root)

      //  citysJson
        var inputStream: InputStream? = null

        var data = ""
        inputStream = activity!!.assets.open("locationCoordinates.json")

        val buf = StringBuilder()
        val `in` = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        var str: String?
        while (`in`.readLine().also { str = it } != null) {
            buf.append(str)
        }
        `in`.close()
        data = buf.toString()


        app.citysIndexList.clear()
        viewModel.loadCitys(data)


        viewModel.citys.observe(viewLifecycleOwner, Observer { citys ->
            adapter = CitysAdapter(citys!!, object: CitysAdapter.MyOnClick {
                override fun onClick(p0: View?, position: Int) {
                 //   Toast.makeText(requireContext(), "Short Click!", Toast.LENGTH_SHORT).show()

                    app.citysIndexList.add( citys[position].index)
                    citys.remove( viewModel.citysAll.value?.get(position))

                    adapter.notifyDataSetChanged()
                }

                override fun onLongClick(p0: View?, position: Int) {

                    app.citysIndexList.clear()
                }
            })

            binding.recyclerViewParcelLockers.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewParcelLockers.adapter = adapter
        })




        binding.button.setOnClickListener {

            RandomUtils.setSeedFromTime() // nastavi novo seme ob vsakem zagonu main metode (vsak zagon bo drugačen)


            // primer zagona za problem eil101.tsp

            val gson = Gson()

//        System.out.println("knedl "+x)
            val locations = gson.fromJson<MutableList<location>>(
                data,
                object : TypeToken<MutableList<location?>?>() {}.type
            )


            var locationsSelected: List<location>
            locationsSelected=ArrayList<location>()

            if(app.citysIndexList.size!=0) {

                if (!(app.citysIndexList.size == 1 && app.citysIndexList[0] == 0)) {



                    for (i in app.citysIndexList) {
                        locationsSelected.add(locations[i])
                    }

                    var citysTemp = ArrayList<TSP.City>()
                    for (i in locationsSelected) {
                        val tspTemp = TSP()
                        var z = tspTemp.City()
                        z.index = i.index
                        z.x = i.latitude.toDouble()
                        z.y = i.longitude.toDouble()
                        citysTemp.add(z)
                    }


                    var inputStream2: InputStream? = null

                    var data2 = ""
                    inputStream2 = activity!!.assets.open("matrixTime.json")

                    val buf2 = StringBuilder()
                    val in2 = BufferedReader(InputStreamReader(inputStream2, "UTF-8"))
                    var str2: String?
                    while (in2.readLine().also { str2 = it } != null) {
                        buf2.append(str2)
                    }
                    in2.close()
                    data2 = buf2.toString()

                    System.out.println("lel")
                    for (i in app.citysIndexList) {
                        System.out.println(i)
                    }


                    val matrika: Array<DoubleArray> =
                        gson.fromJson(data2, Array<DoubleArray>::class.java)

                    val tours = ArrayList<Tour>()
                    for (i in 0..29) {
                        val eilTsp = TSP("eil101.tsp", 10000, citysTemp, matrika)

                        val ga = GA(100, 0.8, 0.1)
                        val bestPath = ga.execute(eilTsp)
                        tours.add(bestPath)

                    }


                    var min = 999999999.0;
                    var poz = 0;
                    var counter = 0;
                    for (i in tours) {
                        if (i.distance < min) {
                            min = i.distance
                            poz = counter
                        }
                        counter++;

                    }

                    var tempLocations: ArrayList<location> = ArrayList<location>(tours[poz].path.size)


                    for (i in tours[poz].path) {
                        tempLocations.add(locations[i.index])
                    }




                    app.citysList = tempLocations
                }
                else{
                    locationsSelected.add(locations[0])
                    app.citysList=locationsSelected
                }
            }
            else{
                locationsSelected.add(locations[0])
                app.citysList=locationsSelected

            }



            app.matrixTime=true
            findNavController().navigate(R.id.action_CitysFragment_to_CitysMapFragment)
        }
        binding.button2.setOnClickListener {

            RandomUtils.setSeedFromTime() // nastavi novo seme ob vsakem zagonu main metode (vsak zagon bo drugačen)


            // primer zagona za problem eil101.tsp

            val gson = Gson()

//        System.out.println("knedl "+x)
            val locations = gson.fromJson<MutableList<location>>(
                data,
                object : TypeToken<MutableList<location?>?>() {}.type
            )


            var locationsSelected2: List<location>
            locationsSelected2=ArrayList<location>()

            if(app.citysIndexList.size!=0) {

                if (!(app.citysIndexList.size == 1 && app.citysIndexList[0] == 0)) {



                for (i in app.citysIndexList) {
                    locationsSelected2.add(locations[i])
                }

                var citysTemp = ArrayList<TSP.City>()
                for (i in locationsSelected2) {
                    val tspTemp = TSP()
                    var z = tspTemp.City()
                    z.index = i.index
                    z.x = i.latitude.toDouble()
                    z.y = i.longitude.toDouble()
                    citysTemp.add(z)
                }


                var inputStream2: InputStream? = null

                var data2 = ""
                inputStream2 = activity!!.assets.open("matrixDistance.json")

                val buf2 = StringBuilder()
                val in2 = BufferedReader(InputStreamReader(inputStream2, "UTF-8"))
                var str2: String?
                while (in2.readLine().also { str2 = it } != null) {
                    buf2.append(str2)
                }
                in2.close()
                data2 = buf2.toString()

                System.out.println("lel")
                for (i in app.citysIndexList) {
                    System.out.println(i)
                }


                val matrika: Array<DoubleArray> =
                    gson.fromJson(data2, Array<DoubleArray>::class.java)

                val tours = ArrayList<Tour>()
                for (i in 0..29) {
                    val eilTsp = TSP("eil101.tsp", 10000, citysTemp, matrika)

                    val ga = GA(100, 0.8, 0.1)
                    val bestPath = ga.execute(eilTsp)
                    tours.add(bestPath)

                }


                var min = 999999999.0;
                var poz = 0;
                var counter = 0;
                for (i in tours) {
                    if (i.distance < min) {
                        min = i.distance
                        poz = counter
                    }
                    counter++;

                }

                var tempLocations: ArrayList<location> = ArrayList<location>(tours[poz].path.size)


                for (i in tours[poz].path) {
                    tempLocations.add(locations[i.index])
                }




                app.citysList = tempLocations
            }
                else{
                    locationsSelected2.add(locations[0])
                    app.citysList=locationsSelected2
                }
            }
            else{
                locationsSelected2.add(locations[0])
                app.citysList=locationsSelected2

            }



            app.matrixTime=false
            findNavController().navigate(R.id.action_CitysFragment_to_CitysMapFragment)
        }
    }




}