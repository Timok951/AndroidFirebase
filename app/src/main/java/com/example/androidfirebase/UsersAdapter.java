package com.example.androidfirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> userList;
    private final OnUsersClickListener listener;

    public interface OnUsersClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public UsersAdapter(List<User> userList, OnUsersClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.emailTextView.setText(user.getEmail());
        holder.nameTextView.setText(user.getDisplayName());

        holder.editButton.setOnClickListener(v -> listener.onEditClick(user));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    public void updateUser(User updatedUser) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUid().equals(updatedUser.getUid())) {
                userList.set(i, updatedUser);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeUser(User user) {
        int position = userList.indexOf(user);
        if (position != -1) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        TextView nameTextView;
        Button editButton;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.user_email);
            nameTextView = itemView.findViewById(R.id.user_name);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}