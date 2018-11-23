package com.ptg.gdrivebackup.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ptg.gdrivebackup.R;
import com.ptg.gdrivebackup.model.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private Context _context;
    private ArrayList<User> dataList;

    public UserAdapter(Context context, ArrayList<User> dataList) {
        this._context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(_context).inflate(R.layout.item_user, viewGroup, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder userHolder, int i) {
        User user = dataList.get(i);
        userHolder.tvEmailId.setText(user.getEmail());
        userHolder.tvFullName.setText(user.getFullName());
        userHolder.tvMobileNo.setText(user.getPhone());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFullName)
        protected TextView tvFullName;
        @BindView(R.id.tvEmailId)
        protected TextView tvEmailId;
        @BindView(R.id.tvMobileNo)
        protected TextView tvMobileNo;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
