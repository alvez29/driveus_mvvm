package com.example.driveus_mvvm.ui.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.driveus_mvvm.R

object HelpMenu {

    fun displayHelpMenu(context: Context, textList: List<Int>) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_help_fragment, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)
            .setTitle("Ayuda")

        var pointer = 0

        var auxText = "${pointer+1}/${textList.size}"
        mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(textList[0])
        mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).text = auxText

        val mAlertDialog = mBuilder.show()
        mDialogView.findViewById<View>(R.id.dialog_help__button__accept).setOnClickListener {
            mAlertDialog.dismiss()
        }
        mDialogView.findViewById<ImageButton>(R.id.dialog_help__image__arrow_left).setOnClickListener {
            if (pointer == 0) {
                pointer = textList.size - 1
            } else {
                pointer -= 1
            }

            auxText = "${pointer+1}/${textList.size}"
            mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(textList[pointer])
            mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).text = auxText
        }
        mDialogView.findViewById<ImageButton>(R.id.dialog_help__image__arrow_right).setOnClickListener {
            if (pointer == textList.size - 1) {
                pointer = 0
            } else {
                pointer += 1
            }

            auxText = "${pointer+1}/${textList.size}"
            mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(textList[pointer])
            mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).text = auxText
        }
    }
}