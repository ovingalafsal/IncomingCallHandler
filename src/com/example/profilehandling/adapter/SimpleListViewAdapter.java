package com.example.profilehandling.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.profilehandling.R;
import com.example.profilehandling.data.Contact;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SimpleListViewAdapter extends ArrayAdapter<Contact> {
	
	ArrayList<Contact> contact = new ArrayList<Contact>();
	Context context;
	
	public SimpleListViewAdapter(Context context, int textViewResourceId,ArrayList<Contact> data) {
		super(context, textViewResourceId, data);
		this.contact = data;
		this.context = context;
	}
	
	static class ViewHolder{
		private TextView songName;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder ;
		if(v == null) {
			LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(R.layout.simple_list_item, null);
			holder = new ViewHolder();
			holder.songName = (TextView)v.findViewById(R.id.listtext);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		holder.songName.setText(contact.get(position).getName());
		return v;
	}
	
	
}
