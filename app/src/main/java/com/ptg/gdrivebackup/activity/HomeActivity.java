package com.ptg.gdrivebackup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ptg.gdrivebackup.R;
import com.ptg.gdrivebackup.adapter.UserAdapter;
import com.ptg.gdrivebackup.model.User;
import com.ptg.gdrivebackup.utility.DatabaseHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity  extends AppCompatActivity {
    @BindView(R.id.rvListItem)protected RecyclerView rvListItem;
    @BindView(R.id.fab)protected FloatingActionButton fab;
    private DatabaseHelper databaseHelper;
    private UserAdapter data_adapter;
    private ArrayList<User> userArrayList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        databaseHelper=new DatabaseHelper(this);
        userArrayList=new ArrayList<>();
        data_adapter=new UserAdapter(this,userArrayList);
        LinearLayoutManager mLayoutMgr = new LinearLayoutManager(this);
        rvListItem.setLayoutManager(mLayoutMgr);
        rvListItem.setItemAnimator(new DefaultItemAnimator());
        rvListItem.setAdapter(data_adapter);
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDataOnList();
    }

    private void setDataOnList() {
        userArrayList.clear();
        userArrayList.addAll(databaseHelper.getAllUserList());
        data_adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.fab)
    protected void onAddIconClick(){
        Intent intent=new Intent(this,UserAddActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_backup:
                Intent intent=new Intent(this,BackupActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_remove_db:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
