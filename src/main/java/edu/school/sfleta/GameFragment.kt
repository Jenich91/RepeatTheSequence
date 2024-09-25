package edu.school.sfleta

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import kotlinx.coroutines.*

class GameFragment : Fragment(), GameButtonsHandlers {
    enum class SoundsName {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    private lateinit var soundsNameAndButtons: Map<SoundsName, Button>
    private lateinit var nextTurn: Button
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button

    private lateinit var guessedRhythms: Array<List<Pair<Button, Double>>>
    private lateinit var levelTextView: TextView
    private lateinit var bestResultTextView: TextView
    private lateinit var gameResults: SharedPreferences
    private lateinit var menuSettings: SharedPreferences

    private lateinit var _view: View
    private lateinit var _context: Context
    private val activity: Activity
        get() = _context as Activity
    private lateinit var gameMode: String

    private var delayAmount: Double = 500.0
    private var soundOn: Boolean = true
    private var soundTheme: String = "drums"
    private var hardMode: Boolean = false

    private var currentNoteCounter = 0
    private var levelCounter = 0
    private var maxLevelCounter = 10
    private var inGame: Boolean = false
    private var isHearingCompleted: Boolean = false

    private var toast: Toast? = null
    private var mp: MediaPlayer? = null

    companion object {
        var bestResult: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _view = inflater.inflate(R.layout.fragment_game, container, false)
        _context = requireActivity()

        levelTextView = _view.findViewById(R.id.LevelValue)
        initButtons()
        initRhythms()
        initGameResult()
        initGameMode()
        initMenuSettings()

        return _view
    }

    private fun initMenuSettings() {
        menuSettings = _context.getSharedPreferences("menuSettings", Context.MODE_PRIVATE)
        soundOn = menuSettings.getBoolean("soundOn", soundOn)
        hardMode = menuSettings.getBoolean("hardMode", hardMode)
        delayAmount = menuSettings.getInt("delayAmount", 500).toDouble()
        soundTheme = menuSettings.getString("soundTheme", "drums").toString()
    }

    private fun initGameMode() {
        gameMode = GameFragmentArgs.fromBundle(requireArguments()).gameMode
        if (gameMode == "free_mode") {
            _view.findViewById<Button>(R.id.btn_start_game).visibility = View.GONE
            _view.findViewById<TextView>(R.id.LevelKey).visibility = View.GONE
            _view.findViewById<TextView>(R.id.LevelValue).visibility = View.GONE
            _view.findViewById<TextView>(R.id.BestResultValue).visibility = View.GONE
            _view.findViewById<TextView>(R.id.BestResultKey).visibility = View.GONE
        }
    }

    private fun initGameResult() {
        gameResults = _context.getSharedPreferences("gameResults", Context.MODE_PRIVATE)
        bestResultTextView = _view.findViewById(R.id.BestResultValue)
        bestResultTextView.text = getBestGameResult().toString()
    }

    private fun initButtons() {
        soundsNameAndButtons = mapOf(
            SoundsName.ONE to _view.findViewById(R.id.btn_one),
            SoundsName.TWO to _view.findViewById(R.id.btn_two),
            SoundsName.THREE to _view.findViewById(R.id.btn_three),
            SoundsName.FOUR to _view.findViewById(R.id.btn_four)
        )

        button1 = soundsNameAndButtons[SoundsName.ONE]!!
        button2 = soundsNameAndButtons[SoundsName.TWO]!!
        button3 = soundsNameAndButtons[SoundsName.THREE]!!
        button4 = soundsNameAndButtons[SoundsName.FOUR]!!
        nextTurn = _view.findViewById(R.id.btn_start_game)

        turnOffButtonClickSound()
    }

    private fun turnOffButtonClickSound() {
        soundsNameAndButtons.values.forEach { it.isSoundEffectsEnabled = false }
        nextTurn.isSoundEffectsEnabled = false
    }

    private fun initRhythms() {
        guessedRhythms = arrayOf(listOf(getRandNote() to delayAmount))
    }

    private fun getRandNote(): Button {
        return when ((1..4).shuffled().first()) {
            1 -> soundsNameAndButtons[SoundsName.ONE]!!
            2 -> soundsNameAndButtons[SoundsName.TWO]!!
            3 -> soundsNameAndButtons[SoundsName.THREE]!!
            4 -> soundsNameAndButtons[SoundsName.FOUR]!!
            else -> throw IllegalArgumentException("Unknown note index")
        }
    }

    private fun blockSoundButtons() {
        soundsNameAndButtons.values.forEach { button -> button.isClickable = false }
        nextTurn.isClickable = false
    }

    private fun unblockSoundButtonsAfterTime(time: Double, callback: () -> Unit) {
        lifecycleScope.launch {
            delay(time.toLong())
            soundsNameAndButtons.values.forEach { button -> button.isClickable = true }
            nextTurn.isClickable = true
            callback()
        }
    }

    private fun showMsg(msg: String) {
        toast?.cancel()
        toast = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT)
        toast?.show()
    }

    private fun extendListOfNotes() {
        val newPairList = listOf(getRandNote() to delayAmount)
        guessedRhythms += (guessedRhythms.last() + newPairList)
    }

    override fun nextTurn(btn: View) {
        showMsg("Listen and remember...")
        blockSoundButtons()

        if (levelCounter >= 1) {
            extendListOfNotes()
        }

        playSoundAsync(guessedRhythms[levelCounter]) {
            isHearingCompleted = false
            inGame = true
        }
        var blockPeriod = ((guessedRhythms[levelCounter].size) * delayAmount) + delayAmount
        if (delayAmount < 500) blockPeriod *= 3

        unblockSoundButtonsAfterTime(blockPeriod) {
            activity.runOnUiThread {
                showMsg("Go...")
            }
            isHearingCompleted = true
        }
    }

    override fun btnClickHandler(btn: View) {
        if (btn.isClickable || !isHearingCompleted) {
            if (soundOn) playSound(btn)
            if (!hardMode) (btn as Button).startAnimation(
                AnimationUtils.loadAnimation(
                    _context,
                    R.anim.alpha_animation
                )
            )
        }

        if (btn != nextTurn && inGame && isHearingCompleted && guessedRhythms.size > levelCounter) {
            val currentRhythm: List<Pair<Button, Double>> = guessedRhythms[levelCounter]
            val userInputBtn = btn as Button

            if (userInputBtn.text != currentRhythm[currentNoteCounter].first.text) {
                showMsg("No...")
                updateBestGameResultText()
                endGame()
            } else {
                showMsg("Yes!")

                currentNoteCounter++
                if (currentNoteCounter == currentRhythm.size) {
                    showMsg("Ready to next level")
                    levelCounter += 1
                    resetCurrentLevelState()
                }

                if (levelCounter == maxLevelCounter) {
                    showMsg("You win!")
                    endGame()
                }
            }
        }
    }

    private fun endGame() {
        inGame = false

        if (levelCounter > getBestGameResult()) addCurrentGameResult()

        showResultDialog { resetCurrentLevelState() }

        levelCounter = 0
        updateLevelText()

        guessedRhythms = arrayOf(listOf(getRandNote() to delayAmount))
    }

    private fun addCurrentGameResult() {
        gameResults.edit {
            putInt(bestResultTextView.toString(), levelCounter)
        }
    }

    private fun getBestGameResult(): Int {
        val gameResultsTmp: Map<String, *> = gameResults.all
        val resultMap = gameResultsTmp.entries.sortedBy { it.value as Int }.associate { it.toPair() }

        if (resultMap.isEmpty()) return 0
        val result = (resultMap.entries.last().value as Int)
        bestResult = result
        return result
    }

    private fun resetCurrentLevelState() {
        currentNoteCounter = 0
        updateLevelText()
    }

    private fun updateBestGameResultText() {
        if (getBestGameResult() > levelCounter) {
            bestResultTextView.text = getBestGameResult().toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLevelText() {
        levelTextView.text = (levelCounter + 1).toString()
    }

    private fun backToMenu() {
        val navController = Navigation.findNavController(activity, R.id.nav_host_fragment)
        navController.navigate(R.id.menuFragment)
    }

    private fun showResultDialog(callback: () -> Unit) {
        val dialogBuilder = AlertDialog.Builder(_context)
        dialogBuilder.setMessage("You complete $levelCounter levels.")
        dialogBuilder.setCancelable(true)

        dialogBuilder.setPositiveButton(
            "Restart game"
        ) { dialog, _ ->
            dialog.cancel()
            callback()
        }

        dialogBuilder.setNegativeButton(
            "Return to menu"
        ) { dialog, _ ->
            dialog.cancel()
            backToMenu()
        }

        val alert11: AlertDialog = dialogBuilder.create()
        alert11.show()
    }

    private fun playSound(v: View) {
        try {
            mp?.release()
            mp = null

            mp = when (soundTheme) {
                "drums" -> when (v) {
                    soundsNameAndButtons[SoundsName.ONE] -> MediaPlayer.create(_context, R.raw.kick_01)
                    soundsNameAndButtons[SoundsName.TWO] -> MediaPlayer.create(_context, R.raw.snare_01)
                    soundsNameAndButtons[SoundsName.THREE] -> MediaPlayer.create(_context, R.raw.snare_roll_01)
                    soundsNameAndButtons[SoundsName.FOUR] -> MediaPlayer.create(_context, R.raw.open_hihat_01)
                    else -> throw RuntimeException("Cannot find sound file")
                }
                "animals" -> when (v) {
                    soundsNameAndButtons[SoundsName.ONE] -> MediaPlayer.create(_context, R.raw.animals_angrykitty_02)
                    soundsNameAndButtons[SoundsName.TWO] -> MediaPlayer.create(_context, R.raw.animals_crow_02)
                    soundsNameAndButtons[SoundsName.THREE] -> MediaPlayer.create(_context, R.raw.animals_goat_02)
                    soundsNameAndButtons[SoundsName.FOUR] -> MediaPlayer.create(_context, R.raw.animals_pig_02)
                    else -> throw RuntimeException("Cannot find sound file")
                }
                "notes" -> when (v) {
                    soundsNameAndButtons[SoundsName.ONE] -> MediaPlayer.create(_context, R.raw.piano_c_03)
                    soundsNameAndButtons[SoundsName.TWO] -> MediaPlayer.create(_context, R.raw.piano_d_sharp_04)
                    soundsNameAndButtons[SoundsName.THREE] -> MediaPlayer.create(_context, R.raw.piano_f_sharp_03)
                    soundsNameAndButtons[SoundsName.FOUR] -> MediaPlayer.create(_context, R.raw.piano_g_03)
                    else -> throw RuntimeException("Cannot find sound file")
                }
                else -> throw RuntimeException("Cannot find theme")
            }

            mp?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSoundAsync(soundsAndDelays: List<Pair<Button, Double>>, callback: () -> Unit) {
        lifecycleScope.launch {
            callback()
            for ((btn, note_delay) in soundsAndDelays) {
                withContext(Dispatchers.Main) {
                    btn.performClick()
                }
                delay(note_delay.toLong())
            }
        }
    }
}

