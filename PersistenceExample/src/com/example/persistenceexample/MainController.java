package com.example.persistenceexample;

import java.util.ArrayList;
import java.util.List;

import data.DAO;
import data.IdataAcces;

import Common.AppConst;
import Common.Friend;
import Common.OnDataSourceChangeListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MainController {
	// The data model, act as a cache 
	private List<Friend> friends;
	private Context context;
	private IdataAcces dao;
	private String user;
	
	//observers list.
	private List<OnDataSourceChangeListener> dataSourceChangedListenrs = new ArrayList<OnDataSourceChangeListener>();

	public MainController(Context context) {
		this.context = context;
		dao = DAO.getInstatnce(context.getApplicationContext());
	}

	public String getUserName() {
		if (user != null)
			return user;
		SharedPreferences settings = context.getSharedPreferences(
				AppConst.SharedPrefsName, 0);
		if (settings == null)
			return "";
		user = settings.getString(AppConst.SharedPrefs_UserName, "");
		return user;
	}

	public List<Friend> getAllFriends() {
		try {
			if (friends != null)
				return friends;
			dao.open();
			List<Friend> fl = dao.getAllFriends();
			dao.close();
			friends = fl;
			return fl;
		} catch (Exception e) {
			// in case of error, return empty list.
			return new ArrayList<Friend>();
		}
	}

	public void refreshData() {
		friends = null;
		getAllFriends();
	}

	public void addFriend(Friend f) {
		try {
			dao.open();
			Friend retFriend = dao.addFriend(f);
			dao.close();
			friends.add(retFriend);
			invokeDataSourceChanged();
		} catch (Exception e) {
			Log.e("MainController",e.getMessage());
		}
	}

	public void removeFriend(Friend f) {
		dao.open();
		dao.removeFriend(f);
		removeFronCache(f);
		dao.close();
		invokeDataSourceChanged();
	}
	
	public void removeFronCache(Friend f)
	{
		int i =0;
		for(;i<friends.size();i++)
		{
			if(friends.get(i).getId() == f.getId()) break;
		}
		friends.remove(i);
	}
	public void registerOnDataSourceChanged(OnDataSourceChangeListener listener)
	{
		if(listener!=null)
			dataSourceChangedListenrs.add(listener);
	}
	public void unRegisterOnDataSourceChanged(OnDataSourceChangeListener listener)
	{
		if(listener!=null)
			dataSourceChangedListenrs.remove(listener);
	}
	public void invokeDataSourceChanged()
	{
		for (OnDataSourceChangeListener listener : dataSourceChangedListenrs) {
			listener.DataSourceChanged();
		}
	}
}
