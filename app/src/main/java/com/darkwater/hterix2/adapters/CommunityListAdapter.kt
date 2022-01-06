package com.darkwater.hterix2.adapters

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.darkwater.hterix2.R
import com.darkwater.hterix2.http.AminoRequest
import com.darkwater.hterix2.objects.Community
import com.google.gson.JsonParser
import kotlin.Exception

class CommunityListAdapter(private val activity: Activity, private var communities: MutableList<Community>): RecyclerView.Adapter<CommunityListAdapter.ViewHolder>() {

    private var clickListener: ((Int) -> Unit)? = null

    init {
        selectedCommunities.clear()
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val communityIcon = itemView.findViewById<ImageView>(R.id.communityIcon)
        val communityName = itemView.findViewById<TextView>(R.id.communityName)
        val communityOnline = itemView.findViewById<TextView>(R.id.communityOnline)
        init {
            itemView.setBackgroundResource(R.drawable.community_card_style)
            if (clickListener != null) itemView.setOnClickListener { clickListener!!.invoke(adapterPosition) }
        }
        fun loadIcon(url: String) {
            Glide.with(itemView.context)
                .load(communities[adapterPosition].iconUrl)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(communityIcon)
        }
        fun selectCommunity() {
            itemView.setBackgroundResource(R.drawable.choosen_community_card_style)
        }
        fun unselectCommunity() {
            itemView.setBackgroundResource(R.drawable.community_card_style)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.community_card, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return communities.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.communityName.text = communities[position].name
        holder.loadIcon(communities[position].iconUrl)
        if (communities[position] in selectedCommunities) {
            holder.selectCommunity()
        } else {
            holder.unselectCommunity()
        }
        if (communities[position].onlineMembersCount != null)
            holder.communityOnline.text = "Онлайн: ${communities[position].onlineMembersCount}"
        else {
            AminoRequest.initRequest("GET", "/x${communities[position].ndcId}/s/live-layer?topic=ndtopic:x${communities[position].ndcId}:online-members&start=0&size=1", activity)
                .async()
                .onError { communities.remove(communities[position]) }
                .send {
                    communities[position].onlineMembersCount = JsonParser.parseString(it).asJsonObject["userProfileCount"].asInt
                    notifyItemChanged(position)
                }
        }
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        clickListener = listener
    }

    fun getIndex(community: Community): Int {
        return communities.indexOf(community)
    }

    fun resolveClick(index: Int) {
        if (selectedCommunities.size > 1 && !selectedCommunities.contains(communities[index])) {
            val alert = AlertDialog.Builder(activity)
            alert.setTitle("Ограничено")
            alert.setMessage("Нельзя выбрать больше сообществ в бесплатной версии.")
            alert.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()
            return
        }
        if (communities[index] in selectedCommunities) {
            selectedCommunities.remove(communities[index])
        } else {
            selectedCommunities.add(communities[index])
        }
        notifyItemChanged(index)
    }

    companion object {
        val selectedCommunities = mutableListOf<Community>()
    }

}