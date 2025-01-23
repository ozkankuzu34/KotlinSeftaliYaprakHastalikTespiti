package com.kotlindersleri.seftali4

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotlindersleri.seftali4.databinding.FragmentAboutBinding
import com.kotlindersleri.seftali4.databinding.FragmentIletisimBinding


class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentAboutBinding.inflate(inflater,container,false)

        return binding.root
    }


}