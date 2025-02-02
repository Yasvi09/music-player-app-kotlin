package com.example.musicplayerapp.ui.theme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.musicplayerapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlaylistFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        val addPlaylistBtn = view.findViewById<FloatingActionButton>(R.id.add_playlist)

        addPlaylistBtn.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)

            bottomSheetDialog.setContentView(bottomSheetView)

            val btnCancel = bottomSheetView.findViewById<Button>(R.id.btn_cancel)
            btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        return view
    }

}