package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.IotDevicesDialogContentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IotDevicesDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: IotDevicesDialogContentBinding
    lateinit var audioManager:AudioManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=IotDevicesDialogContentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        audioManager= requireContext().applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.sbVolume.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    audioManager.setStreamVolume(3,progress,0)
                }
                changeVolumeIcon(progress==0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBar?.run {
                    thumb=ContextCompat.getDrawable(requireContext(), R.drawable.h8h)
                    thumbOffset= (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15.5F,resources.displayMetrics)+0.5F).toInt()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.run {
                    thumb=ContextCompat.getDrawable(requireContext(), R.drawable.h8g)
                    thumbOffset= (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,12F,resources.displayMetrics)+0.5F).toInt()
                }
            }

        })
    }
    private fun changeVolumeIcon(silent:Boolean){
        binding.ivVolume.run {
            if(silent){
                setImageResource(R.drawable.bow)
            }
            else{
                setImageResource(R.drawable.bov)
            }

        }
    }
    companion object {
        const val TAG = "IotDevicesDialogFragment"
    }
}