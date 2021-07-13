package com.example.lab5kulbaka;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * @author Kulbaka Nataly
 * @date 07.05.2021
 */
public class PopupClass {

    public void showPopupWindow(final View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new android.widget.PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button btn = popupView.findViewById(R.id.cancel_button);
        btn.setOnClickListener(v -> {
            if (((CheckBox) popupView.findViewById(R.id.checkbox)).isChecked()) {
                SharedPreferences settings = view.getContext().getSharedPreferences("JOURNALS_APP", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("NEED_MESSAGE", false);
                editor.apply();
            }
            popupWindow.dismiss();
        });
    }

}
