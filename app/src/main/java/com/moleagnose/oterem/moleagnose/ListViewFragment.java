package com.moleagnose.oterem.moleagnose;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import java.util.ArrayList;
import java.util.List;


public class ListViewFragment extends Fragment {

    public static ListViewFragment newInstance() {
        return new ListViewFragment();
    }

    private ListView listView;
    private Animator spruceAnimator;
    private List<ExampleData> placeHolderList;
    private ArrayList<String> names;
    private ArrayList<String> urls;
    private ArrayList<String> imageUrls;



    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ListView) container.findViewById(R.id.list_view);
        Bundle b = getArguments();
        names = b.getStringArrayList("names");
        urls = b.getStringArrayList("urls");
        imageUrls = b.getStringArrayList("imageUrls");


        // Create the animator after the list view has finished laying out
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initSpruce();
            }

        });

        placeHolderList = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            placeHolderList.add(new ExampleData(names.get(i),urls.get(i),imageUrls.get(i)));
        }

        // Remove default dividers and set adapter
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(new ListViewAdapter(placeHolderList));



        return inflater.inflate(R.layout.list_view_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (spruceAnimator != null) {
            spruceAnimator.start();
        }
    }

    private void initSpruce() {
        spruceAnimator = new Spruce.SpruceBuilder(listView)
                .sortWith(new DefaultSort(100))
                .animateWith(DefaultAnimations.shrinkAnimator(listView, 800),
                        ObjectAnimator.ofFloat(listView, "translationX", -listView.getWidth(), 0f).setDuration(800))
                .start();
    }

    private class ListViewAdapter extends BaseAdapter {


        private List<ExampleData> placeholderList;
        private LayoutInflater inflater;

        ListViewAdapter(List<ExampleData> placeholderList) {
            this.placeholderList = placeholderList;
            this.inflater = LayoutInflater.from(getContext());
        }

        class ViewHolder implements View.OnClickListener{


            private RelativeLayout parent;

            ViewHolder(RelativeLayout parent) {
                this.parent = parent;
                this.parent.setOnClickListener(this);
            }



            @Override
            public void onClick(View v) {}
        }





        @Override
        public int getCount() {
            return placeholderList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.view_placeholder,null);
            TextView name = v.findViewById(R.id.links_text_name);
            TextView url = v.findViewById(R.id.links_text_url);
            ImageView image = v.findViewById(R.id.links_image);
            name.setText(placeholderList.get(position).getName());
            url.setText(placeholderList.get(position).getUrl());
            Linkify.addLinks(url,Linkify.WEB_URLS);
            url.setMovementMethod(LinkMovementMethod.getInstance());
            if(placeholderList.get(position).getImageUrl() != "null" ){
                Log.i(null,placeholderList.get(position).getImageUrl()+position);
                Glide.with(getContext()).load(placeholderList.get(position).getImageUrl()).into(image);
            }
            else {
                Uri uri = Uri.parse("android.resource://com.example.oterem.demo/drawable/logo.png");
               image.setImageURI(uri);
               //Glide.with(getContext()).load(R.drawable.logo1).into(image);
            }

            return v;
        }
    }

}
