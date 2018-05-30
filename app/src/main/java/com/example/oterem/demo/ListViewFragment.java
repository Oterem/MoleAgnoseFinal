package com.example.oterem.demo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.oterem.demo.dummy.DummyContent;
import com.example.oterem.demo.dummy.DummyContent.DummyItem;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = (ListView) container.findViewById(R.id.list_view);

        // Create the animator after the list view has finished laying out
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initSpruce();
            }
        });

        // Mock data objects
        List<ExampleData> placeHolderList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            placeHolderList.add(new ExampleData());
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
                //initSpruce();
                Utils.makeToast(getContext(),"shai ata gay");
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

            View vi = convertView;
            ViewHolder vh;

            if (convertView == null) {
                vi = inflater.inflate(R.layout.view_placeholder, null);
                vh = new ViewHolder((RelativeLayout) vi);
                vi.setTag(vh);
            }

            return vi;
        }
    }

}
