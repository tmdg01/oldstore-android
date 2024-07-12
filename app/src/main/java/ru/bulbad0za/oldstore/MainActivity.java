package ru.bulbad0za.oldstore;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
    private List<App> appList = new ArrayList<App>();
    private List<App> searchResults = new ArrayList<App>();
    private AppAdapter adapter;
    private AppAdapter searchAdapter;
    private ListView homeListView, gamesListView, appsListView, searchListView;
    private EditText searchEditText;
    private Button searchButton;
    private Button settingsButton;
    private TextView searchInstructionText;
    private String serverUrl;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabHost = getTabHost();

        TabSpec spec1 = tabHost.newTabSpec("Главная");
        spec1.setIndicator("Главная", getResources().getDrawable(android.R.drawable.ic_menu_compass));
        spec1.setContent(R.id.tab_home);

        TabSpec spec2 = tabHost.newTabSpec("Игры");
        spec2.setIndicator("Игры", getResources().getDrawable(android.R.drawable.ic_menu_save));
        spec2.setContent(R.id.tab_games);

        TabSpec spec3 = tabHost.newTabSpec("Приложения");
        spec3.setIndicator("Приложения", getResources().getDrawable(android.R.drawable.ic_menu_today));
        spec3.setContent(R.id.tab_apps);

        TabSpec spec4 = tabHost.newTabSpec("Поиск");
        spec4.setIndicator("Поиск", getResources().getDrawable(android.R.drawable.ic_menu_search));
        spec4.setContent(R.id.tab_search);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);

        homeListView = (ListView) findViewById(R.id.list_view_home);
        gamesListView = (ListView) findViewById(R.id.list_view_games);
        appsListView = (ListView) findViewById(R.id.list_view_apps);
        searchListView = (ListView) findViewById(R.id.list_view_search);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchButton = (Button) findViewById(R.id.search_button);
        settingsButton = (Button) findViewById(R.id.settings_button);
        searchInstructionText = (TextView) findViewById(R.id.search_instruction_text);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        serverUrl = prefs.getString("server_url", "http://old-apps.xyz");

        adapter = new AppAdapter(this, appList);
        searchAdapter = new AppAdapter(this, searchResults);

        homeListView.setAdapter(adapter);
        gamesListView.setAdapter(adapter);
        appsListView.setAdapter(adapter);
        searchListView.setAdapter(searchAdapter);

        loadApps("all"); // Load all apps on home tab initially

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App selectedApp;
                if (parent == searchListView) {
                    selectedApp = searchResults.get(position);
                } else {
                    selectedApp = appList.get(position);
                }
                Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                intent.putExtra("app", selectedApp);
                startActivity(intent);
            }
        };

        homeListView.setOnItemClickListener(itemClickListener);
        gamesListView.setOnItemClickListener(itemClickListener);
        appsListView.setOnItemClickListener(itemClickListener);
        searchListView.setOnItemClickListener(itemClickListener);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    searchApps(query);
                }
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("Главная")) {
                    loadApps("all");
                } else if (tabId.equals("Игры")) {
                    loadApps("game");
                } else if (tabId.equals("Приложения")) {
                    loadApps("app");
                }
            }
        });
    }

    private void loadApps(String category) {
        if (isNetworkAvailable()) {
            new FetchAppsTask().execute(serverUrl + "/get_apps.php?category=" + category);
        } else {
            Toast.makeText(this, "Отсутствует интернет-соединение или выбрана неверная инстанция.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchApps(String query) {
        if (isNetworkAvailable()) {
            new SearchAppsTask().execute(serverUrl + "/search_apps.php?query=" + query);
        } else {
            Toast.makeText(this, "Отсутствует интернет-соединение или выбрана неверная инстанция.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class FetchAppsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error fetching apps", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.trim().isEmpty()) {
                try {
                    Log.d("MainActivity", "Received JSON: " + result);
                    JSONArray appsArray = new JSONArray(result);
                    appList.clear();

                    for (int i = 0; i < appsArray.length(); i++) {
                        JSONObject appJson = appsArray.getJSONObject(i);
                        Log.d("MainActivity", "Parsing app: " + appJson.toString());

                        try {
                            String id = appJson.getString("id");
                            Log.d("MainActivity", "Parsed id: " + id);
                            String name = appJson.getString("name");
                            Log.d("MainActivity", "Parsed name: " + name);
                            String shortDescription = appJson.getString("short_description");
                            String description = appJson.getString("description");
                            String icon = appJson.getString("icon");
                            String file = appJson.getString("file");
                            String category = appJson.optString("category", "");
                            String minAndroidVersion = appJson.optString("min_android_version", "");
                            String applicationVersion = appJson.optString("application_version", "");

                            JSONArray architecturesArray = appJson.getJSONArray("architectures");
                            List<String> architectures = new ArrayList<>();
                            for (int j = 0; j < architecturesArray.length(); j++) {
                                architectures.add(architecturesArray.getString(j));
                            }

                            JSONArray sourcesArray = appJson.getJSONArray("sources");
                            List<Source> sources = new ArrayList<>();
                            for (int j = 0; j < sourcesArray.length(); j++) {
                                JSONObject sourceJson = sourcesArray.getJSONObject(j);
                                String sourceName = sourceJson.getString("name");
                                String sourceLink = sourceJson.getString("link");
                                sources.add(new Source(sourceName, sourceLink));
                            }

                            App app = new App(
                                    id,
                                    name,
                                    shortDescription,
                                    description,
                                    icon,
                                    file,
                                    category,
                                    minAndroidVersion,
                                    applicationVersion,
                                    architectures,
                                    sources
                            );
                            appList.add(app);

                        } catch (JSONException e) {
                            Log.e("MainActivity", "Error parsing app JSON object", e);
                        }
                    }

                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e("MainActivity", "Error parsing JSON", e);
                    Log.e("MainActivity", "JSON content: " + result);
                    Toast.makeText(MainActivity.this, "Error loading apps", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("MainActivity", "Received empty or null result");
                Toast.makeText(MainActivity.this, "No data received from server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SearchAppsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error searching apps", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.trim().isEmpty()) {
                try {
                    Log.d("MainActivity", "Received JSON: " + result);
                    JSONArray appsArray = new JSONArray(result);
                    searchResults.clear();

                    for (int i = 0; i < appsArray.length(); i++) {
                        JSONObject appJson = appsArray.getJSONObject(i);
                        Log.d("MainActivity", "Parsing app: " + appJson.toString());

                        try {
                            String id = appJson.getString("id");
                            Log.d("MainActivity", "Parsed id: " + id);
                            String name = appJson.getString("name");
                            Log.d("MainActivity", "Parsed name: " + name);
                            String shortDescription = appJson.getString("short_description");
                            String description = appJson.getString("description");
                            String icon = appJson.getString("icon");
                            String file = appJson.getString("file");
                            String category = appJson.optString("category", "");
                            String minAndroidVersion = appJson.optString("min_android_version", "");
                            String applicationVersion = appJson.optString("application_version", "");

                            JSONArray architecturesArray = appJson.getJSONArray("architectures");
                            List<String> architectures = new ArrayList<>();
                            for (int j = 0; j < architecturesArray.length(); j++) {
                                architectures.add(architecturesArray.getString(j));
                            }

                            JSONArray sourcesArray = appJson.getJSONArray("sources");
                            List<Source> sources = new ArrayList<>();
                            for (int j = 0; j < sourcesArray.length(); j++) {
                                JSONObject sourceJson = sourcesArray.getJSONObject(j);
                                String sourceName = sourceJson.getString("name");
                                String sourceLink = sourceJson.getString("link");
                                sources.add(new Source(sourceName, sourceLink));
                            }

                            App app = new App(
                                    id,
                                    name,
                                    shortDescription,
                                    description,
                                    icon,
                                    file,
                                    category,
                                    minAndroidVersion,
                                    applicationVersion,
                                    architectures,
                                    sources
                            );
                            searchResults.add(app);

                        } catch (JSONException e) {
                            Log.e("MainActivity", "Error parsing app JSON object", e);
                        }
                    }

                    searchAdapter.notifyDataSetChanged();

                    if (searchResults.isEmpty()) {
                        searchInstructionText.setText("No results found");
                        searchInstructionText.setVisibility(View.VISIBLE);
                        searchListView.setVisibility(View.GONE);
                    } else {
                        searchInstructionText.setVisibility(View.GONE);
                        searchListView.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    Log.e("MainActivity", "Error parsing JSON", e);
                    Log.e("MainActivity", "JSON content: " + result);
                    Toast.makeText(MainActivity.this, "Error searching apps", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("MainActivity", "Received empty or null result");
                Toast.makeText(MainActivity.this, "No data received from server", Toast.LENGTH_SHORT).show();
            }
        }

    }

    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        serverUrl = prefs.getString("server_url", "http://old-apps.xyz");
        loadApps("all");
    }

}