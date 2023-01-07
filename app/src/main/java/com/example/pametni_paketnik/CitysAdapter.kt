package com.example.pametni_paketnik

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.matrixTSP.location

class CitysAdapter(private val data: MutableList<location>, private val onClickObject: MyOnClick): RecyclerView.Adapter<CitysAdapter.ViewHolder>() {

    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val tvLocationID: TextView = ItemView.findViewById(R.id.id_lokacije)
        val tvPostName: TextView = ItemView.findViewById(R.id.posta)
        val tvPostNumber: TextView = ItemView.findViewById(R.id.posta)
        val tvAddress: TextView = ItemView.findViewById(R.id.naslov)
        val tvParcelLockerNumber: TextView = ItemView.findViewById(R.id.st_paketnikov)
        val line: CardView = ItemView.findViewById(R.id.card_view)
    }

    interface MyOnClick {
        fun onClick(p0: View?, position:Int)
        fun onLongClick(p0: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_card_city, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

     //   val ItemsViewModel = data.value?.get(position)
        val ItemsViewModel = data[position]
       // holder.setIsRecyclable(false)
        holder.line.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
              //  holder.line.setCardBackgroundColor( Color.CYAN)
                onClickObject.onClick(p0, holder.adapterPosition)

            }
        })
        holder.line.setOnLongClickListener(object:View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
             //   holder.line.setCardBackgroundColor( Color.WHITE)
                onClickObject.onLongClick(p0, holder.adapterPosition)

                return true
            }
        })
        holder.tvLocationID.text = ItemsViewModel?.index.toString()
        holder.tvPostName.text =  ItemsViewModel?.postOffice +" "+ ItemsViewModel?.postNumber
        holder.tvAddress.text =  ItemsViewModel?.address
        holder.tvParcelLockerNumber.text =  ItemsViewModel?.parcelLockerCount

    }

    override fun getItemCount(): Int {


       return data.size
    }



}

