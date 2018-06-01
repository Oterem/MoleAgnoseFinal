package com.example.oterem.demo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class ListViewFragment extends Fragment {

    public static ListViewFragment newInstance() {
        return new ListViewFragment();
    }

    private ListView listView;
    private Animator spruceAnimator;
    private List<ExampleData> placeHolderList;
    private ArrayList<String> names;
    private ArrayList<String> urls;


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


        // Create the animator after the list view has finished laying out
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initSpruce();
            }
        });

        placeHolderList = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            placeHolderList.add(new ExampleData(names.get(i),urls.get(i)));
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
            public void onClick(View v) {
                int a = getCount();
                String s = String.valueOf(a);
                Utils.makeToast(getContext(),s);
            }
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
            TextView t = v.findViewById(R.id.text_omri);
            TextView a = v.findViewById(R.id.text_terem);
            a.setText(placeholderList.get(position).getFriend());
            Linkify.addLinks(a,Linkify.WEB_URLS);
            a.setMovementMethod(LinkMovementMethod.getInstance());
            t.setText(placeholderList.get(position).getName());

            return v;
        }
    }

}
