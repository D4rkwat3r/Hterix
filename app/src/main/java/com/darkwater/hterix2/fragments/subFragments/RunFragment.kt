package com.darkwater.hterix2.fragments.subFragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.darkwater.hterix2.R
import com.darkwater.hterix2.WorkThread
import com.darkwater.hterix2.activity.AuthActivity
import com.darkwater.hterix2.adapters.CommunityListAdapter
import com.darkwater.hterix2.fragments.MainFragment
import com.darkwater.hterix2.http.Telemetry
import com.darkwater.hterix2.objects.User
import com.darkwater.hterix2.utils.MailingCallback

class RunFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_run, container, false)
        onReady(view)
        return view
    }

    private fun onReady(view: View) {
        val submitButton = view.findViewById<Button>(R.id.startButton)
        val userCountHint = view.findViewById<TextView>(R.id.doneUsersCounter)
        val isReadOnlyCheckbox = view.findViewById<CheckBox>(R.id.isReadOnlyCheckbox)
        val logoutButton = view.findViewById<ImageView>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireActivity())
            alert.setTitle("Выход")
            alert.setMessage("Выйти из аккаунта?")
            alert.setPositiveButton("Да") { dialog, _ ->
                requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE).edit {
                    clear()
                }
                startActivity(Intent(requireActivity(), AuthActivity::class.java))
                dialog.dismiss()
                requireActivity().finish()
            }
            alert.setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()
        }
        isReadOnlyCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isReadOnlyRequired = isChecked
        }
        val threads = mutableListOf<WorkThread>()
        var isStarted = false
        submitButton.setOnClickListener {
            if (!isStarted) {
                if (!isConfigured) {
                    Toast.makeText(requireContext(), "Настройки не установлены", Toast.LENGTH_SHORT).show()
                    val parent = parentFragment as MainFragment
                    val pager = parent.content.findViewById<ViewPager2>(R.id.viewPager)
                    pager.currentItem = 1
                } else if (CommunityListAdapter.selectedCommunities.isEmpty()) {
                    Toast.makeText(requireContext(), "Сообщества не выбраны", Toast.LENGTH_SHORT).show()
                } else {
                    if (type == "comments" && isReadOnlyCheckbox.isChecked) {
                        Toast.makeText(requireContext(), "Настройкйа не применима к выбранному типу рассылки", Toast.LENGTH_SHORT).show()
                        isReadOnlyRequired = false
                        isReadOnlyCheckbox.isChecked = false
                    }
                    var total = 0
                    CommunityListAdapter.selectedCommunities.forEach { total += it.onlineMembersCount!! }
                    userCountHint.text = "0/$total"
                    CommunityListAdapter.selectedCommunities.forEach {
                        threads.add(
                            WorkThread(
                                requireActivity(),
                                it.ndcId,
                                isReadOnlyRequired,
                                area,
                                type,
                                userCount,
                                text,
                                object: MailingCallback {
                                    override fun onPrepared() {
                                        Log.d("Debug", "Thread prepared.")
                                    }

                                    override fun onMessageSent(chatId: String) {
                                        Log.d("Debug", "Message to $chatId sent.")
                                    }

                                    override fun onReadOnlyEnabled(chatId: String) {
                                        Log.d("Debug", "Enabled read-only mode in $chatId enabled.")
                                    }

                                    override fun onPartPassed(invitedCount: Int) {
                                        Log.d("Debug", "Invited $invitedCount users.")
                                        val splitted = userCountHint.text.split("/")
                                        val done = splitted[0].toInt()
                                        var limit = splitted[1].toInt()
                                        val newValue = done + invitedCount
                                        if (newValue > limit)
                                            limit = newValue
                                        userCountHint.text = "$newValue/$limit"
                                    }
                                }
                            )
                        )
                    }
                    threads.forEach {
                        it.start()
                    }
                    submitButton.text = "Остановить"
                    Telemetry.notifyStarted(
                        isReadOnlyRequired,
                        userCount,
                        text,
                        CommunityListAdapter.selectedCommunities
                    )
                    isStarted = true
                }
            }
            else {
                threads.forEach {
                    it.kill()
                }
                threads.clear()
                submitButton.text = "Запустить"
                userCountHint.text = "0/?"
                isStarted = false
            }
        }
    }
    
    companion object {
        var userCount: Int = 0
        lateinit var text: String
        lateinit var area: String
        lateinit var type: String
        var isReadOnlyRequired = false
        var isConfigured = false
        fun configure(
            specifiedText: String,
            count: Int,
            adverArea: String,
            adverType: String
        ) {
            text = specifiedText
            userCount = count
            area = adverArea
            isConfigured = true
            type = adverType
        }
    }
    
}