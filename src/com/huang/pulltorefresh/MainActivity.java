package com.huang.pulltorefresh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.huang.widget.PullToRefreshListView;
import com.huang.widget.PullToRefreshListView.OnRefreshListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity
{
    private String[] mStrings = {
            "Abbaye de Belloc0", "Abbaye du Mont des Cats1", "Abertam2",
            "Abondanc3e", "Ackawi4", "Acorn5", "Adelost6", "Affidelice au Chablis7",
            "Afuega'l Pitu8", "Airag9", "Airedale10", "Aisy Cendre11",
            "Allgauer Emmentaler12","huang13","lizhe14","hou15","rui16"};
	List<String> list ;
    PullToRefreshListView listView;
	ArrayAdapter<String> adapter ;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		list= new ArrayList<String>();
		list.addAll(Arrays.asList(mStrings));
		
		listView = (PullToRefreshListView)findViewById(R.id.listView);
		 adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		listView.setOnRefreshListener(new OnRefreshListener()
		{
			
			@Override
			public void onRefresh()
			{
				  new GetDataTask().execute();
		
				
			}
		});
		
		
	}
	

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                ;
            }
            return mStrings;
        }

        @Override
        protected void onPostExecute(String[] result) {
        	//mStrings.addFirst("Added after refresh...");
        	list.add("huanglizhe");
        	adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
    		listView.onRefreshComplete();
    	
            super.onPostExecute(result);
        }
    }
}
