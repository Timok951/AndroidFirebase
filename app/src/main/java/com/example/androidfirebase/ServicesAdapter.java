package com.example.androidfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.androidfirebase.Services;

import java.util.List;

public class ServicesAdapter extends ArrayAdapter<Services> {

    private final Context context;
    private final List<Services> services;

    public ServicesAdapter(Context context, List<Services> services) {
        super(context, 0, services);
        this.context = context;
        this.services = services;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        // Получаем текущий сервис
        Services service = services.get(position);

        // Находим и заполняем текстовые элементы в списке
        TextView nameTextView = convertView.findViewById(android.R.id.text1);
        TextView descriptionTextView = convertView.findViewById(android.R.id.text2);

        nameTextView.setText(service.getName());
        descriptionTextView.setText(service.getDescription() + " - $" + service.getPrice());

        return convertView;
    }
}
