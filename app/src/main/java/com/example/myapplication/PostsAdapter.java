package com.example.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends ArrayAdapter<Post> {
    List<Post> list = new ArrayList<>();
    private Context context;

    public PostsAdapter(List<Post> postList, Context context) {
        super(context, R.layout.list_view1, postList);

        this.list = postList;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view1, null);
        }

        Post post = list.get(position);
        TextView userId = convertView.findViewById(R.id.user_id);
        userId.setText(Integer.toString(post.getUserId()));

        TextView id = convertView.findViewById(R.id.id);
        id.setText(Integer.toString(post.getId()));

        TextView title = convertView.findViewById(R.id.title);
        title.setText(post.getTitle());

        TextView body = convertView.findViewById(R.id.body);
        body.setText(post.getBody());

        return convertView;

    }
}
