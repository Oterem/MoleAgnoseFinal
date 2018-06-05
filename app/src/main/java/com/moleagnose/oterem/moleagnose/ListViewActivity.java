
package com.moleagnose.oterem.moleagnose;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ListViewActivity extends AppCompatActivity {

    private String helpUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtra = getIntent();
        Bundle b = intentExtra.getExtras();
        helpUrl = b.getString("helpUrl");

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        }


        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {


            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                if (id == R.id.action_about) {
                    Intent i = new Intent(ListViewActivity.this,About.class);
                    startActivity(i);
                    return true;
                }
                if (id == R.id.action_help) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(helpUrl));
                    startActivity(i);
                    return true;
                }
                return true;
            }


        });
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
