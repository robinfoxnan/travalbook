package com.bird2fish.travelbook.ui.home

import android.content.Context
import android.graphics.*
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
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.FragmentHomeBinding

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

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        this.btnHike = binding.btsportHike
        this.btnRun = binding.btsportRun
        this.btnBike = binding.btsportBike
        this.btnMotor = binding.btsportMotor
        this.btnCar = binding.btsportCar
        this.btnLasy = binding.btsportLasy

        initToolBarImageButton()
        initTopbarEvent()

        // 根据当前状态来初始化
        changeMode()
        return root
    }

    // 将所有的图片复位
    private fun initToolBarImageButton(){
//        var btnHike = binding.btsportHike
//        var btnRun = binding.btsportRun
//        var btnBike = binding.btsportBike
//
//        var btnMotor = binding.btsportMotor
//        var btnCar = binding.btsportCar
//        var btnLasy = binding.btsportLasy

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

    // 切换卫星信号采集频率
    private fun changeMode(){
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
//            else -> {
//
//            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}