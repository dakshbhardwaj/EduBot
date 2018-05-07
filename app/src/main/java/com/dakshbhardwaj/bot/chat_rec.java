package com.dakshbhardwaj.bot;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

/**
 * Created by daksh on 5/1/18.
 */

public class chat_rec extends ViewHolder {

    TextView leftText,rightText;

    public chat_rec(View itemView) {
        super(itemView);

        leftText = (TextView)itemView.findViewById(R.id.leftText);
        rightText = (TextView)itemView.findViewById(R.id.rightText);
    }
}
