package com.darkwater.hterix2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darkwater.hterix2.R
import com.darkwater.hterix2.adapters.CommunityListAdapter
import com.darkwater.hterix2.http.AminoRequest
import com.darkwater.hterix2.utils.Serialization
import com.google.gson.JsonParser

class CommunitiesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_communities, container, false)
        onReady(view)
        return view
    }

    private fun onReady(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.communitiesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = null
        AminoRequest.initRequest("GET", "/g/s/community/joined?v=1&start=0&size=1000", requireActivity())
            .async()
            .send {
                val communities = Serialization.extractCommunityList(it)
                val adapter = CommunityListAdapter(requireActivity(), communities)
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener {
                    adapter.resolveClick(it)
                }
            }
    }

}