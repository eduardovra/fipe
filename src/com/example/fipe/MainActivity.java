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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.george.fipe.client.FipeProvider;
//import com.george.fipe.uol.service.FipeRestService;
//import com.george.fipe.uol.jaxb.Marca;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends Activity {

	private static final String LOGS = "logs";
	private Spinner mCategory, mMarca, mModel, mYear;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCategory = (Spinner) findViewById(R.id.spinner1);
		List<String> list = new ArrayList<String>();
		list.add("Carros");
		list.add("Motos");
		list.add("Caminhões");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategory.setAdapter(dataAdapter);
		
		new DownloadFilesTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... trash) {
			
			HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListMarcas.do?category=1");
			uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListMarcas.do");

			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;
			try {
				resp = client.execute(uri);
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
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Document doc = null;
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
			
			Log.v(LOGS, "Root element :" + doc.getDocumentElement().getNodeName());
			
			NodeList nList = doc.getElementsByTagName("marca");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				Node nNode = nList.item(temp);
				//Element line = (Element) nNode;
		 
				Log.v(LOGS, "\nCurrent Element :" + nNode.getNodeName());
				
				Node child = nNode.getFirstChild();
				CharacterData cd = (CharacterData) child;
				
				Log.v(LOGS, "CDATA: " + cd.getData());
			}
			
			return null;
		}
		
		protected void onPostExecute(Void trash) {
			//finish();
		}

	}
}
