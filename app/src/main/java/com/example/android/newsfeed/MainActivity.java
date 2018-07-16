package com.example.android.newsfeed;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    /**
     * URL for news data from THE GUARDIAN dataset
     */
    private static final String THEGUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search";
    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;
    /**
     * Text view that is displayed when the list is empty
     */
    @BindView(R.id.empty_textview)
    TextView mEmptyStateTextView;
    /**
     * Progress bar view that is displayed while the data is being fetched
     */
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    /**
     * List view that contains the news
     */
    @BindView(R.id.list)
    ListView newsListView;
    /**
     * Adapter for the list of news
     */
    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind the view using butterknife
        ButterKnife.bind(this);

        // Create a new {@link ArrayAdapter} of news
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News selectedNews = (News) adapterView.getItemAtPosition(i);
                // Get the URL that will lead to the corresponding story
                String url = selectedNews.getUrl();
                // Use an intent to open the story in the browser; build the intent
                Uri webpage = Uri.parse(url);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                // Verify it resolves
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
                boolean isIntentSafe = activities.size() > 0;
                // Start an activity if it's safe
                if (isIntentSafe) {
                    startActivity(webIntent);
                }
            }
        });

        // set the empty view on the list
        newsListView.setEmptyView(mEmptyStateTextView);

        // Keep a reference of the device's active network status in the form of a NetworkInfo object
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        // if the device is connected to the internet, attempt to fetch the data from the internet
        // if it is not connected, tell the user that the device has no internet connection
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.mipmap.no_internet), null, null);
            mEmptyStateTextView.setText(R.string.no_internet);
        }

    }

    @Override
    // This method instantiates and returns a new Loader for the given ID
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // obtain a string value from the preferences
        // the second parameter is the default value for this preference
        String category = sharedPrefs.getString(
                getString(R.string.settings_category_key),
                getString(R.string.settings_category_default)
        );
        Log.i(LOG_TAG, category);

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        // break apart the URI string
        Uri baseUri = Uri.parse(THEGUARDIAN_REQUEST_URL);

        // prepare the baseUri to be able to add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // append query parameters and values
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("page-size", "20");
        uriBuilder.appendQueryParameter("api-key", "e9e45a10-fcc2-4865-aaae-a8cf2d439107");
        // if the user selects category "All", the query parameter "section" will not be appended
        if (!category.equals(getString(R.string.settings_category_all_value))) {
            uriBuilder.appendQueryParameter("section", category);
        }

        // Create a new loader for the completed URL
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }

        // assign the text to be displayed in case of an empty state list
        mEmptyStateTextView.setText(R.string.no_news);

        // hide the progress bar when the info has been retrieved
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    /**
     * This method inflates the Options Menu specified in the XML
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * In this method we setup the action that occurs when any of the items in the Options menu is
     * selected
     *
     * @param item represents the item that the user selected
     * @return true or false, whether the click should be consumed here or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
