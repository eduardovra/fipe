package com.example.fipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String LOGS = "logs";
	private Spinner mCategory, mMarca, mModel;
	private int category = 0;
	String marca = null, modelo = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCategory = (Spinner) findViewById(R.id.spinner1);
		mMarca = (Spinner) findViewById(R.id.spinner2);
		mModel = (Spinner) findViewById(R.id.spinner3);

		List<String> list = new ArrayList<String>();
		list.add("Carros");
		list.add("Motos");
		list.add("Caminhões");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategory.setAdapter(dataAdapter);
		
		mCategory.setOnItemSelectedListener(new CategoryOnItemSelectedListener());
		mMarca.setOnItemSelectedListener(new MarcaOnItemSelectedListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void FetchMarcas () {
		HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListMarcas.do?category=" + category);
		uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListMarcas.do");

		if (category != 0)
			new DownloadFilesTask().execute(uri);
	}

	private void FetchModels () {
		HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListModels.do?category=" + category + "&marca=" + marca);
		uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListModels.do");

		if (marca != null)
			new DownloadFilesTask().execute(uri);
	}
	
	private class CategoryOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Toast.makeText(parent.getContext(), 
				"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
				Toast.LENGTH_SHORT).show();
			category = pos + 1;
			MainActivity.this.FetchMarcas();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class MarcaOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Toast.makeText(parent.getContext(), 
				"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
				Toast.LENGTH_SHORT).show();
			marca = parent.getItemAtPosition(pos).toString();
			MainActivity.this.FetchModels();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class DownloadFilesTask extends AsyncTask<HttpPost, Void, Document> {
		protected Document doInBackground(HttpPost... uri) {
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;
			try {
				resp = client.execute(uri[0]);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			StatusLine status = resp.getStatusLine();
			if (status.getStatusCode() != 200) {
				Log.v(LOGS, "HTTP error, invalid server status code: " + resp.getStatusLine());
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = null;
			DocumentBuilder builder = null;
			
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				doc = builder.parse(resp.getEntity().getContent());
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return doc;
		}
		
		protected void onPostExecute(Document doc) {
			
			NodeList nList = null;
			String name;
			Spinner spinner = null;

			name = doc.getDocumentElement().getNodeName();
			Log.v(LOGS, "Root element: " + name);
			
			if (name.equals("marcas")) {
				nList = doc.getElementsByTagName("marca");
				spinner = mMarca;
			}
			else if (name.equals("modelos")) {
				nList = doc.getElementsByTagName("modelo");
				spinner = mModel;
			}
			else {
				return;
			}
			
			List<String> list = new ArrayList<String>();

			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				Node nNode = nList.item(temp);

				Log.v(LOGS, "\nCurrent Element: " + nNode.getNodeName());
				
				Node child = nNode.getFirstChild();
				CharacterData cd = (CharacterData) child;
				
				Log.v(LOGS, "CDATA: " + cd.getData());
				
				list.add(cd.getData());
			}
			
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_spinner_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(dataAdapter);
		}
	}
}
