package com.bird2fish.travelbook.ui.tracks

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bird2fish.travelbook.R

class maptrack : Fragment() {

    companion object {
        fun newInstance() = maptrack()
    }

    private lateinit var viewModel: MaptrackViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maptrack, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MaptrackViewModel::class.java)
        // TODO: Use the ViewModel
    }

}