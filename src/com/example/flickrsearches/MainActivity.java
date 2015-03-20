package com.example.flickrsearches;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ListActivity
{
	private static final String SITES = "sites";
	private EditText queryEditText;
	private EditText tagEditText;
	private SharedPreferences savedSites;
	private ArrayList<String> tags;
	private ArrayAdapter<String> adapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		queryEditText = (EditText) findViewById(R.id.queryEditText);
		tagEditText = (EditText) findViewById(R.id.tagEditText);
		
		savedSites = getSharedPreferences(SITES, MODE_PRIVATE);
		
		tags = new ArrayList<String>(savedSites.getAll().keySet());
		Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
		
		adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
		setListAdapter(adapter);
		
		ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListener);
		getListView().setOnItemClickListener(itemClickListener);
		getListView().setOnItemLongClickListener(itemLongClickListener);
	}
	public OnClickListener saveButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0)
			{
				addTaggedSites(queryEditText.getText().toString(), tagEditText.getText().toString());
				queryEditText.setText("");
				tagEditText.setText("");
				
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(),  0);
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				builder.setMessage(R.string.missingMessage);
				builder.setPositiveButton(R.string.OK, null);
				
				AlertDialog errorDialog = builder.create();
				errorDialog.show();
		
			}
		}
	};
	private void addTaggedSites(String query, String tag)
	{
		SharedPreferences.Editor preferencesEditor = savedSites.edit();
		preferencesEditor.putString(tag, query);
		preferencesEditor.apply();
		if (!tags.contains(tag))
		{
			tags.add(tag);
			Collections.sort(tags,String.CASE_INSENSITIVE_ORDER);
			adapter.notifyDataSetChanged();
		}
	}
	OnItemClickListener itemClickListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String tag = ((TextView) view).getText().toString();
			String urlString = getString(R.string.searchURL) + Uri.encode(savedSites.getString(tag, ""), "UTF-8");
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
			startActivity(webIntent);
		}
	};
	
	OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			final String tag = ((TextView) view).getText().toString();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(getString(R.string.shareEditDeleteTitle, tag));
			
			builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
						case 0:
							shareSite(tag);
							break;
						case 1:
							tagEditText.setText(tag);
							queryEditText.setText(savedSites.getString(tag, ""));
							break;
						case 2:
							deleteSite(tag);
							break;
						}
					}
				}
			);
			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			}
			);
			builder.create().show();
			return true;
		}
	};
			
			private void shareSite(String tag)
			{
				String urlString = Uri.encode(savedSites.getString(tag, ""), "UTF-8");
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT,  getString(R.string.shareSubject));
				shareIntent.putExtra(Intent.EXTRA_TEXT,  getString(R.string.shareMessage, urlString));
				shareIntent.setType("text/plain");
				
				startActivity(Intent.createChooser(shareIntent, getString(R.string.shareSearch)));
			}
			private void deleteSite(final String tag)
			{
				AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
				
				confirmBuilder.setMessage(getString(R.string.confirmMessage, tag));
				
				confirmBuilder.setNegativeButton(getString(R.string.cancel),  new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.cancel();
					}
				}
				);
				confirmBuilder.setPositiveButton(getString(R.string.delete),  new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						tags.remove(tag);
						SharedPreferences.Editor preferencesEditor = savedSites.edit();
						preferencesEditor.remove(tag);
						preferencesEditor.apply();
						
						adapter.notifyDataSetChanged();
					}
				}
				);
				confirmBuilder.create().show();
			}
}