package com.kotlindersleri.seftali4

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotlindersleri.seftali4.databinding.FragmentHomeBinding
import com.kotlindersleri.seftali4.databinding.FragmentIletisimBinding


class iletisimFragment : Fragment() {
    private lateinit var binding:FragmentIletisimBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIletisimBinding.inflate(inflater, container, false)

        return binding.root
    }}

