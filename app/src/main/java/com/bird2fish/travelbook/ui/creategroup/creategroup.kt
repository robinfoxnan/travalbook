package com.bird2fish.travelbook.ui.creategroup

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bird2fish.travelbook.R

class creategroup : Fragment() {

    companion object {
        fun newInstance() = creategroup()
    }

    private lateinit var viewModel: CreategroupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creategroup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreategroupViewModel::class.java)
        // TODO: Use the ViewModel
    }

}