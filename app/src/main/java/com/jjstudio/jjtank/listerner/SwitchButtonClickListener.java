package com.jjstudio.jjtank.listerner;

import android.view.View;
import android.widget.ImageButton;

import com.jjstudio.jjtank.R;
import com.jjstudio.jjtank.model.TankControlData;

public class SwitchButtonClickListener implements View.OnClickListener{

    private ImageButton imageButton;
    private boolean switchOn;

    public SwitchButtonClickListener(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    @Override
    public void onClick(View view) {
//            ImageButton btn = (ImageButton)view;
//            if (!switchOn){
//                btn.setImageResource(R.drawable.swithfieldon);
//                sendValue = TankControlData.SWT_1_ON;
//                switch1On=true;
//            }else{
//                btn.setImageResource(R.drawable.swithfieldoff);
//                sendValue = TankControlData.SWT_1_OFF;
//                switch1On=false;
//            }
    }
}
