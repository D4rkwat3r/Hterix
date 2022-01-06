package com.darkwater.hterix2.fragments.subFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.darkwater.hterix2.R
import com.darkwater.hterix2.fragments.MainFragment
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText

class AdvertisementSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_advertisement_settings, container, false)
        onReady(view)
        return view
    }

    private fun onReady(view: View) {
        var usersInPrivate = 1
        var adverArea = "global"
        var adverType = "messages"
        val userCountHint = view.findViewById<TextView>(R.id.sliderCounterText)
        val slider = view.findViewById<Slider>(R.id.usersInPrivateCount)
        val areaGroup = view.findViewById<RadioGroup>(R.id.areaGroup)
        val globalAreaRadio = view.findViewById<RadioButton>(R.id.globalArea)
        val localAreaRadio = view.findViewById<RadioButton>(R.id.localArea)
        val typeGroup = view.findViewById<RadioGroup>(R.id.mailingTypeGroup)
        val messagesTypeRadio = view.findViewById<RadioButton>(R.id.messagesType)
        val commentsTypeRadio = view.findViewById<RadioButton>(R.id.commentsType)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val messageInput = view.findViewById<TextInputEditText>(R.id.adverMessageEditText)
        val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        slider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                val choicedCount = slider.value.toString().split(".")[0].toInt()
                userCountHint.text = "Количество пользователей в одном чате: $choicedCount"
                usersInPrivate = choicedCount
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val choicedCount = slider.value.toString().split(".")[0].toInt()
                userCountHint.text = "Количество пользователей в одном чате: $choicedCount"
                usersInPrivate = choicedCount
            }
        })
        areaGroup.setOnCheckedChangeListener { _, radioId ->
            val checkedRadio = view.findViewById<RadioButton>(radioId)
            when (checkedRadio.text.toString()) {
                "Глобальная область" -> adverArea = "global"
                "Локальная область" -> adverArea = "local"
            }
        }
        typeGroup.setOnCheckedChangeListener { _, radioId ->
            val checkedRadio = view.findViewById<RadioButton>(radioId)
            when (checkedRadio.text.toString()) {
                "Сообщения" -> adverType = "messages"
                "Комментарии" -> {
                    adverType = "comments"
                    usersInPrivate = 1
                    userCountHint.text = "Количество пользователей в одном чате: $usersInPrivate"
                    slider.value = 1F
                }
            }
        }
        saveButton.setOnClickListener {
            if (messageInput.text!!.isEmpty()) {
                messageInput.error = "Введите сообщение"
            }
            else {
                prefs.edit {
                    putBoolean("configured", true)
                    putString("message", messageInput.text.toString())
                    putInt("usersInPrivate", usersInPrivate)
                    putString("adverArea", adverArea)
                    putString("adverType", adverType)
                }
                RunFragment.configure(
                    messageInput.text.toString(),
                    usersInPrivate,
                    adverArea,
                    adverType
                )
                val parent = parentFragment as MainFragment
                val pager = parent.content.findViewById<ViewPager2>(R.id.viewPager)
                pager.currentItem = 0
            }
        }
        if (prefs.getBoolean("configured", false)) {
            adverArea = prefs.getString("adverArea", null)!!
            adverType = prefs.getString("adverType", null)!!
            messageInput.setText(prefs.getString("message", null)!!)
            usersInPrivate = prefs.getInt("usersInPrivate", 1)
            userCountHint.text = "Количество пользователей в одном чате: $usersInPrivate"
            slider.value = prefs.getInt("usersInPrivate", usersInPrivate).toFloat()
            when (adverArea) {
                "global" -> {
                    globalAreaRadio.isChecked = true
                    localAreaRadio.isChecked = false
                }
                "local" -> {
                    globalAreaRadio.isChecked = false
                    localAreaRadio.isChecked = true
                }
            }
            when (adverType) {
                "messages" -> {
                    messagesTypeRadio.isChecked = true
                    commentsTypeRadio.isChecked = false
                }
                "comments" -> {
                    messagesTypeRadio.isChecked = false
                    commentsTypeRadio.isChecked = true
                }
            }
        }
    }

}