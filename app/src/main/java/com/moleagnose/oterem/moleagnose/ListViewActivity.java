
package com.moleagnose.oterem.moleagnose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ListViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtra = getIntent();
        Bundle b = intentExtra.getExtras();
        setContentView(R.layout.list_view_fragment);
        FragmentManager fm = getSupportFragmentManager();

        Fragment listViewFragment = fm.findFragmentById(R.id.list_view_fragment);
        if (listViewFragment == null) {
            listViewFragment = ListViewFragment.newInstance();
            listViewFragment.setArguments(b);
            fm.beginTransaction()
                    .replace(R.id.list_view_fragment, listViewFragment)
                    .commit();
        }

        Toolbar toolBar = (Toolbar) findViewById(R.id.list_view_tool_bar);
        setSupportActionBar(toolBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.list_view_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
