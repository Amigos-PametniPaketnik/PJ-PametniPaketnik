package com.example.pametni_paketnik

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.models.ParcelLocker

class ParcelLockersAdapter(private val data: MutableList<ParcelLocker>, private val onClickObject: ParcelLockersAdapter.MyOnClick): RecyclerView.Adapter<ParcelLockersAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val tvBoxID: TextView = ItemView.findViewById(R.id.item_boxid)
        val tvDatetimeOpened: TextView = ItemView.findViewById(R.id.item_datetimeopened)
        val tvOpened: TextView = ItemView.findViewById(R.id.item_opened)
        val line: CardView = ItemView.findViewById(R.id.card_view)
    }

    interface MyOnClick {
        fun onClick(p0: View?, position:Int)
        fun onLongClick(p0: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = data[position]
        holder.line.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                onClickObject.onClick(p0, holder.adapterPosition)
            }
        })
        holder.line.setOnLongClickListener(object:View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                onClickObject.onLongClick(p0, holder.adapterPosition)
                return true
            }
        })
        holder.tvBoxID.text = ItemsViewModel?.numberParcelLocker
        holder.tvDatetimeOpened.text = ItemsViewModel?.description


    }

    override fun getItemCount(): Int {
        return data.size
    }
}