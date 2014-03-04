package com.example.fipe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {

	private static final String LOGS = "logs";
	private Spinner mCategory, mMarca, mModel;
	String category = null;
	String marca = null, modelo = null;
	List<String> anos_celula, anos_label;
	Map <String, String> prizes;
	List<String> brands, models;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		anos_celula = new ArrayList<String>();
		anos_label = new ArrayList<String>();
		prizes = new HashMap<String, String>();
		brands = new ArrayList<String>();
		models = new ArrayList<String>();
		
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
		mModel.setOnItemSelectedListener(new ModelOnItemSelectedListener());
		
		Button btnSubmit = (Button) findViewById(R.id.button1);

		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					MainActivity.this.FetchYears();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void FetchBrands () throws UnsupportedEncodingException {
		HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListMarcas.do");
		uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListMarcas.do");

		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("category", category));
		uri.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		if (category != null)
			new DownloadFilesTask().execute(uri);
	}

	private void FetchModels () throws UnsupportedEncodingException {
		HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListModels.do");
		uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListModels.do");

		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("category", category));
		params.add(new BasicNameValuePair("marca", marca));
		uri.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		if (marca != null)
			new DownloadFilesTask().execute(uri);
	}
	
	private void FetchYears() throws UnsupportedEncodingException {
		HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListYear.do");
		uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListYear.do");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("category", category));
		params.add(new BasicNameValuePair("modelo", modelo));
		uri.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		if (modelo != null)
			new DownloadFilesTask().execute(uri);
	}
	
	private void FetchPrices () throws UnsupportedEncodingException {
		if (modelo != null && anos_celula != null) {
			HttpPost uris[] = new HttpPost[anos_celula.size()];
			
			for (int i = 0; i < anos_celula.size(); i++) {
				String celula = anos_celula.get(i);
				HttpPost uri = new HttpPost("http://tabela.carros.uol.com.br/app/client/pgListPrize.do");
				uri.setHeader("Referer", "http://tabela.carros.uol.com.br/app/client/pgListPrize.do");
				
				List<NameValuePair> params = new ArrayList<NameValuePair>(2);
				params.add(new BasicNameValuePair("category", category));
				params.add(new BasicNameValuePair("modelo", modelo));
				params.add(new BasicNameValuePair("celula", celula));
				uri.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				
				uris[i] = uri;
			}
			
			new DownloadFilesTask().execute(uris);
		}
	}
	
	private class CategoryOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			category = "" + pos + 1;
			try {
				MainActivity.this.FetchBrands();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class MarcaOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			marca = parent.getItemAtPosition(pos).toString();
			try {
				MainActivity.this.FetchModels();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class ModelOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			modelo = parent.getItemAtPosition(pos).toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class DownloadFilesTask extends AsyncTask<HttpPost, Void, String> {
		protected String doInBackground(HttpPost... uri) {
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = null;
			DocumentBuilder builder = null;
			String name = null;
			
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 0; i < uri.length; i++) {
				
				try {
					resp = client.execute(uri[i]);
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
				
				name = doc.getDocumentElement().getNodeName();

				if (name.equals("marcas")) {
					processBrand(doc);
				}
				else if (name.equals("modelos")) {
					processModel(doc);
				}
				else if (name.equals("ano")) {
					processYear(doc);
					try {
						MainActivity.this.FetchPrices();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (name.equals("fipe")) {
					processPrize(doc);
					// TODO lançar intent da activity dos graficos, depois de ter recebido todos os preços
					if (anos_label.size() == prizes.size()) {
//						for (int j = 0; j < prizes.size(); j++) {
							Log.v(LOGS, "key: " + prizes.toString());
//						}
					}
				}
			}
			
			return name;
		}

		protected void onPostExecute (String result) {
			// Update UI
			
			List<String> list = null;
			Spinner spinner = null;

			if (result.equals("marcas")) {
				list = brands;
				spinner = mMarca;
			}
			else if (result.equals("modelos")) {
				list = models;
				spinner = mModel;
			}
			else {
				return;
			}
			
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_spinner_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			spinner.setAdapter(dataAdapter);
		}
		
		private void processBrand (Document doc) {
			NodeList nList = doc.getElementsByTagName("marca");
			List<String> list = new ArrayList<String>();

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				Node child = nNode.getFirstChild();
				CharacterData cd = (CharacterData) child;
				list.add(cd.getData());
				Log.v(LOGS, cd.getData());
			}

			brands = list;
		}
		
		private void processModel (Document doc) {
			NodeList nList = doc.getElementsByTagName("modelo");
			List<String> list = new ArrayList<String>();

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				Node child = nNode.getFirstChild();
				CharacterData cd = (CharacterData) child;
				list.add(cd.getData());
			}

			models = list;
		}
		
		private void processYear (Document doc) {
			NodeList nList = doc.getElementsByTagName("label_ano");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				anos_label.add(nNode.getTextContent());
			}
			
			nList = doc.getElementsByTagName("celula_ano");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				anos_celula.add(nNode.getTextContent());
			}
			
			Log.v(LOGS, "anos_label size: " + anos_label.size());
		}
		
		private void processPrize(Document doc) {
			String year = null, prize = null;
			NodeList nList = doc.getElementsByTagName("preco");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				prize = nNode.getTextContent();
			}
			
			nList = doc.getElementsByTagName("label_ano");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				year = nNode.getTextContent();
			}
			
			prizes.put(year, prize);
		}
	}
}
