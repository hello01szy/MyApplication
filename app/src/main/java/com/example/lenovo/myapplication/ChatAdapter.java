package com.example.lenovo.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private List<Msg> listMsg;
    private LayoutInflater mInflater;
    private Context context;
    public ChatAdapter (List<Msg> listMsg, Context context)
    {
        super();
        this.context = context;
        this.listMsg = listMsg;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        if(listMsg.size() == 0)
            return 0;
        return listMsg.size();
    }

    @Override
    public Object getItem(int i) {
        return listMsg.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
            if(listMsg.get(i).fromOrTo.equals("FROM"))
            {
                ViewHolder holder = new ViewHolder();
                view = mInflater.inflate(R.layout.chat_left_item, null);
                holder.username_tv = (TextView)view.findViewById(R.id.chat_item_from);
                holder.data_tv = (TextView)view.findViewById(R.id.chat_item_date);
                holder.msg_tv = (TextView)view.findViewById(R.id.chat_message);
                view.setTag(holder);
            }
            else
            {
                ViewHolder holder = new ViewHolder();
                view = mInflater.inflate(R.layout.chat_right_item, null);
                holder.username_tv = (TextView)view.findViewById(R.id.chat_login_user);
                holder.data_tv = (TextView)view.findViewById(R.id.chat_login_date);
                holder.msg_tv = (TextView)view.findViewById(R.id.login_message);
                view.setTag(holder);
            }
            ViewHolder holder = (ViewHolder)view.getTag();
            String username = listMsg.get(i).username;
            String date = listMsg.get(i).date;
            String msg = listMsg.get(i).msgbody;
            if(username != null)
            {
                holder.username_tv.setText(username);
            }
            else
            {
                holder.username_tv.setText("");
            }
            if(date != null)
            {
                holder.data_tv.setText(date);
            }
            else
            {
                holder.data_tv.setText("");
            }
            if(msg != null)
            {
                holder.msg_tv.setText(msg);
            }
            else
            {
                holder.msg_tv.setText("");
            }
        return view;
    }
    private class ViewHolder{
        private TextView username_tv;
        private TextView data_tv;
        private TextView msg_tv;
    }
}
