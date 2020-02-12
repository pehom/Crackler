package com.android.pehom.cracklechat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<MessageModel> {
    private List<MessageModel> messages;
    private Activity activity;

    public MessageAdapter(Activity context, int resource, List<MessageModel> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.activity = context;
    }


    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        MessageModel messageModel = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if (viewType == 0) {
            layoutResource = R.layout.left_message_item;
        } else {
            layoutResource = R.layout.right_message_item;
        }

        if (convertView != null) {
            viewHolder = (ViewHolder)convertView.getTag();

        } else {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        boolean isText = messageModel.getImageUrl() == null;
        if (isText) {
            viewHolder.userNameTextView.setText(messageModel.getName() + " said");
            viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.photoImageView.setClickable(false);
            viewHolder.messageTextView.setText(messageModel.getText());
        } else {
            viewHolder.userNameTextView.setText(messageModel.getName() + " said");
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.messageTextView.setClickable(false);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.photoImageView.getContext()).load(messageModel.getImageUrl()).into(viewHolder.photoImageView);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        MessageModel messageModel = messages.get(position);
        if (messageModel.getIsMine()) flag = 0;
        else flag = 1;
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder {
        private TextView userNameTextView;
        private TextView messageTextView;
        private ImageView photoImageView;

        public ViewHolder(View view) {
            userNameTextView = view.findViewById(R.id.userNameTextView);
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextTextView);
        }
    }
}
