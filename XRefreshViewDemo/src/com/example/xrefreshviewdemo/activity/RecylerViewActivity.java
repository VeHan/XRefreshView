package com.example.xrefreshviewdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.SimpleXRefreshListener;
import com.andview.refreshview.listener.OnBottomLoadMoreTime;
import com.example.xrefreshviewdemo.R;
import com.example.xrefreshviewdemo.recylerview.Person;
import com.example.xrefreshviewdemo.recylerview.PersonAdapter;

public class RecylerViewActivity extends Activity implements
		PersonAdapter.OnBottomListener {
	RecyclerView recyclerView;
	PersonAdapter adapter;
	List<Person> personList = new ArrayList<Person>();
	XRefreshView xRefreshView;
	int lastVisibleItem = 0;
	LinearLayoutManager layoutManager;
	private boolean isBottom = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recylerview);
		xRefreshView = (XRefreshView) findViewById(R.id.xrefreshview);
		xRefreshView.setPullLoadEnable(true);
		recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test_rv);
		recyclerView.setHasFixedSize(true);

		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		initData();
		adapter = new PersonAdapter(personList);
		// adapter.setOnRecyclerViewListener(this);
		adapter.setOnBottomListener(this);
		recyclerView.setAdapter(adapter);
		
		xRefreshView.setXRefreshViewListener(new SimpleXRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						xRefreshView.stopRefresh();
					}
				}, 2000);
			}

			@Override
			public void onLoadMore() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						xRefreshView.stopLoadMore();
					}
				}, 2000);
			}
		});
		recyclerView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrolled(int dx, int dy) {
				int lastPosition = layoutManager.findLastVisibleItemPosition();
				isBottom=adapter.getItemCount()-1==lastPosition;
			}

			@Override
			public void onScrollStateChanged(int newState) {
			}
		});
		 xRefreshView.setOnBottomLoadMoreTime(new OnBottomLoadMoreTime() {
		
		 @Override
		 public boolean isBottom() {
		 return isBottom;
		 }
		 });
	}

	private void initData() {
		for (int i = 0; i < 20; i++) {
			Person person = new Person("name" + i, "" + i);
			personList.add(person);
		}
	}

	@Override
	public void isOnBottom(boolean isBottom) {
		this.isBottom = isBottom;
	}

}
