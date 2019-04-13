package com.example.lenovo.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactsFragment extends Fragment {
    List<String> groupName = new ArrayList<String>();
    List<List<String>> friendsName = new ArrayList<>();
    List<List<Bitmap>> avators = new ArrayList<>();
    TextView textView;
    ExpandableListView expandableListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        expandableListView = view.findViewById(R.id.contacts_expand);
        getAllName();
        for(int i = 0; i < friendsName.size(); i++)
        {
            for(int j = 0; j < friendsName.get(i).size(); j++)
            {
                Log.v("ContactsFragment", friendsName.get(i).get(j));
            }
        }
        expandableListView.setAdapter(new MyAdapter(getActivity()));
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("friendName", friendsName.get(groupPosition).get(childPosition));
                bundle.putString("loginUser", MessageUi.loginName);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
        return view;
    }
    //获取组名和好友名称
    public void getAllName()
    {
        if(XmppUtil.getConnection() == null)
        {
            Log.v("ContactsFragment","信息获取为空");
            return;
        }
        Roster roster = Roster.getInstanceFor(XmppUtil.getConnection());
        Collection<RosterGroup> collection = roster.getGroups();
        for(RosterGroup rosterGroup : collection)
        {
            groupName.add(rosterGroup.getName());
            List<String> stringList = new ArrayList<>();
            final List<Bitmap> tempAvator = new ArrayList<>();
            for(final RosterEntry entry : rosterGroup.getEntries())
            {
                stringList.add(entry.getName());
                Bitmap bitmap = setAvatorFromCache(entry.getName());
                if(bitmap != null)
                {
                    tempAvator.add(bitmap);
                }
                else
                {
                    new Thread()
                    {
                        @Override
                        public void run() {
                            tempAvator.add(setAvatorFromNet(entry.getName()));
                            super.run();
                        }
                    }.start();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            avators.add(tempAvator);
            friendsName.add(stringList);
        }
    }
    class MyAdapter extends BaseExpandableListAdapter{
        private Context context;
        public MyAdapter(Context context)
        {
            this.context = context;
        }
        @Override
        public int getGroupCount() {
            return groupName.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return friendsName.get(i).size();
        }

        @Override
        public Object getGroup(int i) {
            return groupName.get(i);
        }

        @Override
        public Object getChild(int i, int i1) {
            return friendsName.get(i).get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            GroupHolder groupHolder = null;
            if(view == null)
            {
                view = (View)getActivity().getLayoutInflater().from(context).inflate(R.layout.contact_list_parent_item, null);
                groupHolder = new GroupHolder();
                groupHolder.group_textView = (TextView)view.findViewById(R.id.contacts_parent_title);
                view.setTag(groupHolder);
            }
            else
            {
                groupHolder = (GroupHolder)view.getTag();
            }
            groupHolder.group_textView.setText(groupName.get(i));
            return view;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            ChildHolder childHolder = null;
            if(view == null)
            {
                view = (View)getActivity().getLayoutInflater().inflate(R.layout.contact_list_child_item, null);
                childHolder = new ChildHolder();
                childHolder.child_textView = (TextView)view.findViewById(R.id.friend_name);
                childHolder.imageView = (com.example.lenovo.myapplication.CircleImageView)view.findViewById(R.id.contacts_list_image);
                view.setTag(childHolder);
            }
            else
            {
                childHolder = (ChildHolder)view.getTag();
            }
            childHolder.child_textView.setText(friendsName.get(i).get(i1));
            childHolder.imageView.setImageBitmap(avators.get(i).get(i1));
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
        class GroupHolder{
            public TextView group_textView;
        }
        class ChildHolder{
            public TextView child_textView;
            public com.example.lenovo.myapplication.CircleImageView imageView;
        }
    }
    //如果内存中有对应的头像信息则从内存中获取相应的位图信息
    public Bitmap setAvatorFromCache(String name)
    {
        Bitmap bitmap = null;
        String path = getActivity().getExternalCacheDir().getPath();
        File file = new File(path);
        File[] files = file.listFiles();
        if(files.length != 0)
        {
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].getName().equals(name + "crop.jpg"))
                {
                    bitmap = BitmapFactory.decodeFile(files[i].getPath());
                }
            }
        }
        return bitmap;
    }
    //如果内存中尚未存放相应的位图，则从服务器中获取位图
    public Bitmap setAvatorFromNet(final String name)
    {
        byte[] data = NetUtil.getAvator("http://192.168.43.208:8080/IM/InfoServlet?username=" + name);
        if(data != null)
        {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        else
        {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.tempavator);
            return bmp;
        }
    }
}
