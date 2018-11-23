package com.ptg.gdrivebackup.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ptg.gdrivebackup.R;
import com.ptg.gdrivebackup.model.User;
import com.ptg.gdrivebackup.utility.DatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserAddActivity extends AppCompatActivity {

    @BindView(R.id.clUsersRoot)
    ConstraintLayout clUsersRoot;
    @BindView(R.id.etFullName)
    TextInputEditText etFullName;
    @BindView(R.id.etEmailId)
    TextInputEditText etEmailId;
    @BindView(R.id.etMobileNo)
    TextInputEditText etMobileNo;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_add);
        ButterKnife.bind(this);
        setupToolbar();
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btSave)
    protected void onSaveClick() {
        String fullName = etFullName.getText().toString();
        String email = etEmailId.getText().toString();
        String phone = etMobileNo.getText().toString();
        if (fullName.equals("")) {
            showMessage(getString(R.string.msg_fullname));
        } else if (phone.equals("")) {
            showMessage(getString(R.string.msg_phone));
        } else if (email.equals("")) {
            showMessage(getString(R.string.msg_email));
        } else {
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            databaseHelper.saveUser(user);
            finish();
        }
    }

    private void showMessage(String message) {
        Snackbar.make(clUsersRoot, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }


}
