package ru.bulbad0za.oldstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.preference.PreferenceManager;


import java.util.List;

public class AppAdapter extends ArrayAdapter<App> {
    private Context context;
    private List<App> apps;
    private String serverUrl;

    public AppAdapter(Context context, List<App> apps) {
        super(context, 0, apps);
        this.context = context;
        this.apps = apps;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.serverUrl = preferences.getString("server_url", "http://old-apps.xyz");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final App app = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_app, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.app_name);
        TextView descriptionTextView = (TextView) convertView.findViewById(R.id.app_short_description);
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.app_icon);

        nameTextView.setText(app.getName());
        descriptionTextView.setText(app.getShortDescription());
        new ImageLoader.LoadImageTask(iconImageView).execute(serverUrl + "/images/" + app.getIcon());

        return convertView;
    }
}