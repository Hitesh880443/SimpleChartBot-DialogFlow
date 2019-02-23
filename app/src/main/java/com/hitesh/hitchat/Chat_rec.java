package com.hitesh.hitchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class Chat_rec extends RecyclerView.ViewHolder {

    TextView bot_ans, user_msg;
    public Chat_rec(@NonNull View itemView) {
        super(itemView);
        bot_ans = (TextView)itemView.findViewById(R.id.tv_ans);
        user_msg = (TextView)itemView.findViewById(R.id.tv_msg);
    }
}
