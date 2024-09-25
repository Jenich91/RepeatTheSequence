package edu.school.sfleta

import android.os.Bundle
import androidx.navigation.NavArgs

class GameFragmentArgs private constructor(val gameMode: String) : NavArgs {
    companion object {
        fun fromBundle(bundle: Bundle): GameFragmentArgs {
            bundle.classLoader = GameFragmentArgs::class.java.classLoader
            val gameMode = bundle.getString("gameMode") ?: ""
            return GameFragmentArgs(gameMode)
        }
    }
}
