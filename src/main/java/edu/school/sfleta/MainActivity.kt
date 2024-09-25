package edu.school.sfleta

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController

interface GameButtonsHandlers {
    fun nextTurn(btn: View)
    fun btnClickHandler(btn: View)
}

class MainActivity : AppCompatActivity(), GameButtonsHandlers {
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getGameFragmentForFuncCall(): GameFragment? {
        return navHostFragment.childFragmentManager.fragments.firstOrNull { it is GameFragment } as GameFragment?
    }

    override fun btnClickHandler(btn: View) {
        getGameFragmentForFuncCall()?.btnClickHandler(btn)
    }

    override fun nextTurn(btn: View) {
        getGameFragmentForFuncCall()?.nextTurn(btn)
    }
}
