package com.example.androidfirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {

    private List<Services> serviceList;
    private final OnServicesClickListener listener;
    private OnItemClickListener itemClickListener; // Новый слушатель для выбора элемента

    public interface OnServicesClickListener {
        void onEditClick(Services service);
        void onDeleteClick(Services service);
    }

    public interface OnItemClickListener {
        void onItemClick(Services service);
    }

    public ServicesAdapter(List<Services> serviceList, OnServicesClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Services service = serviceList.get(position);
        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription() + " - $" + service.getCost());

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(service);
            }
        });

        holder.editButton.setVisibility(listener != null ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(listener != null ? View.VISIBLE : View.GONE);

        if (listener != null) {
            holder.editButton.setOnClickListener(v -> listener.onEditClick(service));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(service));
        }
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void addService(Services service) {
        serviceList.add(service);
        notifyItemInserted(serviceList.size() - 1);
    }

    public void updateService(Services updatedService) {
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).getName().equals(updatedService.getName())) {
                serviceList.set(i, updatedService);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeService(Services service) {
        int position = serviceList.indexOf(service);
        if (position != -1) {
            serviceList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        Button editButton;
        Button deleteButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.service_name);
            descriptionTextView = itemView.findViewById(R.id.service_description);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}