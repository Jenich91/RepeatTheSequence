package edu.school.sfleta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class MenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        view.findViewById<Button>(R.id.btn_new_game).setOnClickListener {

            val action = MenuFragmentDirections.actionMenuFragmentToGameFragment("default_mode")
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.btn_free_mode).setOnClickListener {
            val action = MenuFragmentDirections.actionMenuFragmentToGameFragment("free_mode")
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_settingsFragment)
        }

        view.findViewById<Button>(R.id.btn_about).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_aboutFragment)
        }

        return view
    }
}
