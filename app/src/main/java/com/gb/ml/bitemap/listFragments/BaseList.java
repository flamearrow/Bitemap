package com.gb.ml.bitemap.listFragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * abstract fragment to display a list of customizable items from DB
 */
public abstract class BaseList extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();

        listView.setOnItemClickListener(getItemClickListener());
        setListAdapter(createListAdapter());
    }

    /**
     * Create DB adaptor for the list, decides how the list is displayed
     *
     * @return The list adaptor created from DB, should have it's own formatted defined
     */
    abstract ListAdapter createListAdapter();

    /**
     * Call back when list item is clicked
     *
     * @return ItemClickListener fro list items
     */
    AdapterView.OnItemClickListener getItemClickListener() {

        return new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "you're clicking item " + position,
                        Toast.LENGTH_LONG).show();
            }
        };
    }


}
