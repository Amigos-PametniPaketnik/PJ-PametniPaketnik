package com.example.pametni_paketnik

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.models.Unlocked
import java.text.SimpleDateFormat

class UnlockAdapter(private val data: MutableList<Unlocked>, private val onClickObject: UnlockAdapter.MyOnClick): RecyclerView.Adapter<UnlockAdapter.ViewHolder>() {

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_card_lockers, parent, false)
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
        holder.tvBoxID.text = ItemsViewModel?.idParcelLocker
        val formatDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        holder.tvDatetimeOpened.text = formatDate.format(ItemsViewModel?.dateTime).toString()
        if (ItemsViewModel?.opened!!) {
            holder.tvOpened.text = "Uspešen"
            holder.tvOpened.setTextColor(R.color.teal_700)
        }
        else {
            holder.tvOpened.text = "Neuspešen"
            holder.tvOpened.setTextColor(R.color.design_default_color_error)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
}