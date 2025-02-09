package com.garlicbread.foodbridge.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.foodbridge.CommunityCentre
import com.garlicbread.foodbridge.R
import com.garlicbread.foodbridge.dto.CommunityCentreRequests


class CommunityCentreItem(
    private val dataList: List<CommunityCentreRequests>,
    private val context: Context
) : RecyclerView.Adapter<CommunityCentreItem.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var quantity: TextView = itemView.findViewById(R.id.quantity)
        var distance: TextView = itemView.findViewById(R.id.distance)
        var view: ConstraintLayout = itemView.findViewById(R.id.view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_centre, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataList[position].title
        holder.quantity.text = "${dataList[position].quantity} person(s)"
        holder.distance.text = "${dataList[position].distance} miles away"

        holder.view.setOnClickListener {
            val newIntent = Intent(context, CommunityCentre::class.java)
            newIntent.putExtra("CommunityCentreId", dataList[position].id)
            context.startActivity(newIntent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
