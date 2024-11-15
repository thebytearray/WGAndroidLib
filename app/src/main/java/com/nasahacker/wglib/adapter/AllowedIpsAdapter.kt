package com.nasahacker.wglib.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nasahacker.wglib.R

class AllowedIpsAdapter(private val allowedIps: MutableList<String>) :
    RecyclerView.Adapter<AllowedIpsAdapter.AllowedIpsViewHolder>() {

    class AllowedIpsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ipTextView: TextView = itemView.findViewById(R.id.ipTextView)
        val removeButton: MaterialButton = itemView.findViewById(R.id.removeIpButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowedIpsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allowed_ip, parent, false)
        return AllowedIpsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllowedIpsViewHolder, position: Int) {
        val ip = allowedIps[position]
        holder.ipTextView.text = ip
        holder.removeButton.setOnClickListener {
            allowedIps.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = allowedIps.size

    fun addAllowedIp(ip: String) {
        allowedIps.add(ip)
        notifyItemInserted(allowedIps.size - 1)
    }

    fun getAllowedIps(): List<String> = allowedIps
}
