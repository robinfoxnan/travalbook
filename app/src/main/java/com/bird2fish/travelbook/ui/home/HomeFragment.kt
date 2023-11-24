package com.bird2fish.travelbook.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.Keys
import com.bird2fish.travelbook.core.TencentLocService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentHomeBinding
import com.bird2fish.travelbook.helper.PreferencesHelper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var  btnHike : ImageView
    lateinit var  btnRun : ImageView
    lateinit var  btnBike : ImageView
    lateinit var  btnMotor : ImageView
    lateinit var  btnCar : ImageView
    lateinit var  btnLasy : ImageView



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        this.btnHike = binding.btsportHike
        this.btnRun = binding.btsportRun
        this.btnBike = binding.btsportBike
        this.btnMotor = binding.btsportMotor
        this.btnCar = binding.btsportCar
        this.btnLasy = binding.btsportLasy

        initToolBarImageButton()
        initTopbarEvent()

        // 根据当前状态来初始化
        initSetMode()
        return root
    }
    /////////////////////////////////////////////////////////////////////////////

    // 将所有的图片复位
    private fun initToolBarImageButton(){
        btnHike.setImageResource(R.drawable.hike)
        btnHike.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_hike)

        btnRun.setImageResource(R.drawable.run)
        btnRun.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_run)

        btnBike.setImageResource(R.drawable.bike)
        btnBike.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_bike)


        btnMotor.setImageResource(R.drawable.motorbike)
        btnMotor.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_motor)

        btnCar.setImageResource(R.drawable.car)
        btnCar.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_car)

        btnLasy.setImageResource(R.drawable.lasy)
        btnLasy.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_lasy)
    }

    private fun onClickHike(){
        initToolBarImageButton()

        btnHike.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_hike1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.hike,
            replacementColorResId = R.color.back_hike,
            imageView = btnHike
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.hike)
        binding.btnStart.btnText.setText(R.string.start_to_hike)

    }

    private fun onClickRun(){
        initToolBarImageButton()

        btnRun.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_run1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.run,
            replacementColorResId = R.color.back_run,
            imageView = btnRun
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.run)
        binding.btnStart.btnText.setText(R.string.start_to_run)

    }


    private fun onClickBike(){
        initToolBarImageButton()

        btnBike.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_bike1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.bike,
            replacementColorResId = R.color.back_bike,
            imageView = btnBike
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.bike)
        binding.btnStart.btnText.setText(R.string.start_to_bike)

    }

    private fun onClickMotor(){
        initToolBarImageButton()

        btnMotor.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_motor1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.motorbike,
            replacementColorResId = R.color.back_motor,
            imageView = btnMotor
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.motorbike)
        binding.btnStart.btnText.setText(R.string.start_to_motor)

    }

    private fun onClickCar(){
        initToolBarImageButton()

        btnCar.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_car1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.car,
            replacementColorResId = R.color.back_car,
            imageView = btnCar
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.car)
        binding.btnStart.btnText.setText(R.string.start_to_car)


    }

    private fun onClickLasy(){
        initToolBarImageButton()

        btnLasy.background = UiHelper.idToDrawable(requireActivity(), R.drawable.back_lasy1)
        UiHelper.replaceOpaqueWithColor(
            context = requireActivity().applicationContext,
            resourceId = R.drawable.lasy,
            replacementColorResId = R.color.back_lasy,
            imageView = btnLasy
        )
        binding.btnStart.btnImage.setImageResource(R.drawable.lasy)
        binding.btnStart.btnText.setText(R.string.start_to_lasy)

    }

    // 初始化顶部工具条
    private fun initTopbarEvent(){
        btnHike.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_HIKE

            changeMode()
        }

        btnRun.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_RUN
            changeMode()
        }

        btnBike.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_BIKE
            changeMode()
        }

        btnMotor.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_MOTOR
            changeMode()
        }

        btnCar.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_CAR
            changeMode()
        }

        btnLasy.setOnClickListener{
            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_LAZY
            changeMode()
        }

    }

    // 初始化时候设置
    private fun initSetMode()
    {
//        try {
//            GlobalData.sportMode =  GlobalData.SportModeEnum.valueOf(PreferencesHelper.getSportMode())
//        }catch (e: Exception){
//            GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_HIKE
//        }

        // 仅仅设置图标
        when (GlobalData.sportMode) {
            GlobalData.SportModeEnum.SPORT_MODE_HIKE->{
                onClickHike()
            }

            GlobalData.SportModeEnum.SPORT_MODE_RUN->{
                onClickRun()
            }

            GlobalData.SportModeEnum.SPORT_MODE_BIKE->{
                onClickBike()
            }
            GlobalData.SportModeEnum.SPORT_MODE_MOTOR->{
                onClickMotor()
            }
            GlobalData.SportModeEnum.SPORT_MODE_CAR->{
                onClickCar()
            }
            GlobalData.SportModeEnum.SPORT_MODE_LAZY->{
                onClickLasy()
            }

        }

    }

    // 切换卫星信号采集频率
    private fun changeMode(){

        // 运动模式
        PreferencesHelper.setSportMode(GlobalData.sportMode.name)

        when (GlobalData.sportMode) {

            GlobalData.SportModeEnum.SPORT_MODE_HIKE->{
                GlobalData.intervalOfLocation = PreferencesHelper.getModeHikePosInterval()
                onClickHike()
            }

            GlobalData.SportModeEnum.SPORT_MODE_RUN->{
                GlobalData.intervalOfLocation = PreferencesHelper.getModeRunPosInterval()
                onClickRun()
            }

            GlobalData.SportModeEnum.SPORT_MODE_BIKE->{
                GlobalData.intervalOfLocation= PreferencesHelper.getModeBikePosInterval()
                onClickBike()
            }
            GlobalData.SportModeEnum.SPORT_MODE_MOTOR->{
                GlobalData.intervalOfLocation = PreferencesHelper.getModeMotorPosInterval()
                onClickMotor()
            }
            GlobalData.SportModeEnum.SPORT_MODE_CAR->{
                GlobalData.intervalOfLocation = PreferencesHelper.getModeCarPosInterval()
                onClickCar()
            }
            GlobalData.SportModeEnum.SPORT_MODE_LAZY->{
                GlobalData.intervalOfLocation = PreferencesHelper.getModeLasyPosInterval()
                onClickLasy()
            }

        }

        PreferencesHelper.setCurrentPosInterval(GlobalData.intervalOfLocation)

//        if (TencentLocService.instance != null){
//            TencentLocService.instance!!.restartLocationService()
//        }

        val intent = Intent(requireActivity(), TencentLocService::class.java)
        //intent.putExtra("command", "start"); // 通过Intent传递命令
        intent.action = Keys.ACTION_INIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(intent);
        } else {
            requireActivity().startService(intent);
        }
        val detail = String.format("设置GPS采集间隔为 %d 秒", GlobalData.intervalOfLocation / 1000)
        UiHelper.showCenterMessage(requireActivity(), detail)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}