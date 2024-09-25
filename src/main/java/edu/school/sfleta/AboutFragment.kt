package edu.school.sfleta

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        val textViewLink = view.findViewById<TextView>(R.id.githubLinkValue)
        textViewLink.movementMethod = LinkMovementMethod.getInstance()

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable._sfleta_icon)

        val bestResultValue = view.findViewById<TextView>(R.id.BestResultValue)
        bestResultValue.text = GameFragment.bestResult.toString()

        return view
    }
}