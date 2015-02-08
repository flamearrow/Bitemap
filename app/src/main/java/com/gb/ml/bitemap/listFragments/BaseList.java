package com.gb.ml.bitemap.listFragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

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
     * Create DB adaptor for the list
     * @return The list adaptor created from DB, should have it's own formatted defined
     */
    abstract ListAdapter createListAdapter();

    /**
     * Call back when list item is clicked
     * @return ItemClickListener fro list items
     */
    abstract AdapterView.OnItemClickListener getItemClickListener();

}
