package com.example.android.newsfeed;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param news    A List of News objects to display in a list
     */
    public NewsAdapter(Activity context, List<News> news) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for four TextViews, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, news);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position in the list of data that should be displayed in the
     *                    list item view.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the {@link News} object located at this position in the list
        News currentNews = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID section
        TextView sectionTextView = convertView.findViewById(R.id.section);
        // Get the section from the current News object
        if (currentNews != null) {
            sectionTextView.setText(currentNews.getSection());
        }

        // Find the TextView in the list_item.xml layout with the ID date
        TextView dateTextView = convertView.findViewById(R.id.date);
        // Get the date from the current News object and set it on the date textView
        if (currentNews != null) {
            String date = convertView.getResources().getString(R.string.date).toUpperCase() + ": "
                    + formatDate(currentNews.getDate());
            dateTextView.setText(date);
        }

        // Find the TextView in the list_item.xml layout with the ID title
        TextView titleTextView = convertView.findViewById(R.id.title);
        // Get the title from the current News object
        if (currentNews != null) {
            titleTextView.setText(currentNews.getTitle());
        }

        // Find the TextView in the list_item.xml layout with the ID author
        TextView authorTextView = convertView.findViewById(R.id.author);
        // Get the author from the current News object
        if (currentNews != null) {
            String author = convertView.getResources().getString(R.string.author).toUpperCase() + ": ";
            if (currentNews.getAuthor() == null) {
                author += convertView.getResources().getString(R.string.not_specified);
            } else
                author += currentNews.getAuthor();
            authorTextView.setText(author);
        }

        // Return the whole list item layout (containing 4 TextViews)
        // so that it can be shown in the ListView
        return convertView;
    }

    private String formatDate(String date) {
        String[] parts = date.split("T");
        return parts[0];
    }
}
