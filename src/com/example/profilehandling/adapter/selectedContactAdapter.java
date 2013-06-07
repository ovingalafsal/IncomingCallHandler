package com.example.profilehandling.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.profilehandling.R;
import com.example.profilehandling.data.Contact;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class selectedContactAdapter extends ArrayAdapter<Contact> {
	
	public ArrayList<Contact> contact = new ArrayList<Contact>();
	Context context;
	
	public selectedContactAdapter(Context context, int textViewResourceId,ArrayList<Contact> data) {
		super(context, textViewResourceId, data);
		this.contact = data;
		this.context = context;
	}
	
	static class ViewHolder{
		private TextView Name;
		private CheckBox check;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder ;
		if(v == null) {
			LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(R.layout.single_contactxml, null);
			holder = new ViewHolder();
			holder.Name = (TextView)v.findViewById(R.id.listtext);
			holder.check = (CheckBox)v.findViewById(R.id.checkBox);
			holder.check.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v ;  
					Contact contactNew = (Contact) cb.getTag();
					contactNew.setSelected(cb.isChecked());
				}
			});
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		Contact contactNew = contact.get(position);
		holder.Name.setText(contact.get(position).getName());
		holder.check.setChecked(contactNew.isSelected());
		holder.check.setTag(contactNew);
		return v;
	}
	
	
}
