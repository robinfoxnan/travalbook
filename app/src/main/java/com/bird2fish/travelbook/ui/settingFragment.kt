package com.bird2fish.travelbook.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentSettingBinding
import com.bird2fish.travelbook.helper.PreferencesHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [settingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class settingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_setting, container, false)

        loadConfig()
        binding.btnSave.setOnClickListener{
            saveConfig()
        }

        return binding.root
    }

    private fun loadConfig(){
        val hike = PreferencesHelper.getModeHikePosInterval() / 1000
        binding.tvHike.setText(hike.toString())

        val run = PreferencesHelper.getModeRunPosInterval() / 1000
        binding.tvRun.setText(run.toString())

        val bike = PreferencesHelper.getModeBikePosInterval() / 1000
        binding.tvBike.setText(bike.toString())

        val motor = PreferencesHelper.getModeMotorPosInterval() / 1000
        binding.tvMotor.setText(motor.toString())

        val car = PreferencesHelper.getModeCarPosInterval() / 1000
        binding.tvCar.setText(car.toString())

        val lasy = PreferencesHelper.getModeLasyPosInterval() / 1000
        binding.tvLasy.setText(lasy.toString())


        val refresh = PreferencesHelper.getRefreshInterval() / 1000
        binding.tvRefresh.setText(refresh.toString())
    }

    private fun saveConfig(){
        try {
            // 刷新周期
            var deltaRefresh = binding.tvRefresh.text.toString().toLong()
            if (deltaRefresh < 2){
                deltaRefresh = 2000
            }else{
                deltaRefresh = deltaRefresh * 1000
            }
            GlobalData.intervalOfRefresh =  deltaRefresh
            PreferencesHelper.setRefreshInterval(deltaRefresh)

            // 运动模式周期
            var deltaHike= binding.tvHike.text.toString().toLong()
            var deltaRun= binding.tvRun.text.toString().toLong()
            var deltaBike= binding.tvBike.text.toString().toLong()
            var deltaMotor= binding.tvMotor.text.toString().toLong()
            var deltaCar= binding.tvCar.text.toString().toLong()
            var deltaLasy= binding.tvLasy.text.toString().toLong()
            if (deltaHike < 1){
                deltaHike = 1000
            }else {
                deltaHike *=  1000
            }

            if (deltaRun < 1){
                deltaRun = 1000
            }else {
                deltaRun *= 1000
            }

            if (deltaBike < 1){
                deltaBike = 1000
            }else {
                deltaBike *=  1000
            }

            if (deltaMotor < 1){
                deltaMotor = 1000
            }else {
                deltaMotor *=  1000
            }

            if (deltaCar < 1){
                deltaCar = 1000
            }else {
                deltaCar *= 1000
            }

            if (deltaLasy < 1){
                deltaLasy = 1000
            }else {
                deltaLasy *= 1000
            }

            PreferencesHelper.setModeHikePosInterval(deltaHike)
            PreferencesHelper.setModeRunPosLongerval(deltaRun)
            PreferencesHelper.setModeBikePosInterval(deltaBike)
            PreferencesHelper.setModeMotorPosInterval(deltaMotor)
            PreferencesHelper.setModeCarPosInterval(deltaCar)
            PreferencesHelper.setModeLasyPosInterval(deltaLasy)
            UiHelper.showCenterMessage(requireActivity(), "保存完毕")

        }catch (e: NumberFormatException) {
            UiHelper.showCenterMessage(requireActivity(), e.toString())
        }


    }



}