package edu.school.sfleta

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    private lateinit var settingsView: View
    private lateinit var menuSettings: SharedPreferences
    private lateinit var soundToggleButton: ToggleButton
    private lateinit var hardModeToggleButton: ToggleButton
    private lateinit var delayAmountSeekBar: SeekBar
    private lateinit var soundThemeSpinner: Spinner

    private var delayAmount: Int = 500
    private var soundOn: Boolean = false
    private var hardMode: Boolean = false
    private var soundTheme: String = "drums"
    private var soundThemeSpinnerPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsView = inflater.inflate(R.layout.fragment_settings, container, false)
        menuSettings = requireActivity().getSharedPreferences("menuSettings", Context.MODE_PRIVATE)

        soundToggleButton = settingsView.findViewById(R.id.sound_toggleButton)
        hardModeToggleButton = settingsView.findViewById(R.id.hardModeSetting_toggleButton)
        delayAmountSeekBar = settingsView.findViewById(R.id.seekBar)
        soundThemeSpinner = settingsView.findViewById(R.id.themesSpinner)

        setupSoundToggleButton()
        setupHardModeToggleButton()
        setupDelayAmountSeekBar()
        setupSoundThemeSpinner()

        loadFromSharedPreferences()
        return settingsView
    }

    private fun setupSoundToggleButton() {
        soundToggleButton.setOnCheckedChangeListener { _, isChecked ->
            soundOn = isChecked
            saveToSharedPreferences()
        }
    }

    private fun setupHardModeToggleButton() {
        hardModeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            hardMode = isChecked
            saveToSharedPreferences()
        }
    }

    private fun setupDelayAmountSeekBar() {
        delayAmountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                delayAmount = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveToSharedPreferences()
            }
        })
    }

    private fun setupSoundThemeSpinner() {
        val items = arrayOf("drums", "animals", "notes")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        soundThemeSpinner.adapter = adapter

        soundThemeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                soundTheme = items[position]
                soundThemeSpinnerPosition = position
                saveToSharedPreferences()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveToSharedPreferences() {
        menuSettings.edit {
            putBoolean("soundOn", soundOn)
            putBoolean("hardMode", hardMode)
            putInt("delayAmount", roundToNearestHundred(delayAmount))
            putString("soundTheme", soundTheme)
            putInt("soundThemeSpinnerPosition", soundThemeSpinnerPosition)
        }
    }

    private fun roundToNearestHundred(number: Int): Int {
        return (number / 100) * 100
    }

    private fun loadFromSharedPreferences() {
        soundOn = menuSettings.getBoolean("soundOn", soundOn)
        delayAmount = menuSettings.getInt("delayAmount", delayAmount)
        hardMode = menuSettings.getBoolean("hardMode", hardMode)
        soundTheme = menuSettings.getString("soundTheme", soundTheme) ?: "drums"
        soundThemeSpinnerPosition = menuSettings.getInt("soundThemeSpinnerPosition", soundThemeSpinnerPosition)

        soundToggleButton.isChecked = soundOn
        hardModeToggleButton.isChecked = hardMode
        delayAmountSeekBar.progress = delayAmount
        soundThemeSpinner.setSelection(soundThemeSpinnerPosition)
    }
}
