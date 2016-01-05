package me.sheikharaf.snunetusage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class StatisticsActivity extends AppCompatActivity {
    String snuid;
    EasyTracker easyTracker = EasyTracker.getInstance(this);
    TextView tvRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.loading);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences pref = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        snuid = pref.getString("username", null);
        new fetchStats().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_opt_out:
                Intent form_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/forms/3phx3AqTWr"));
                startActivity(form_intent);
                easyTracker.send(MapBuilder
                        .createEvent("ui_action",     // Event category (required)
                                "forget_option",  // Event action (required)
                                "forget_clicked",   // Event label
                                null)            // Event value
                        .build());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class fetchStats extends AsyncTask<Void, Void, Void> {
        Iterator<Element> ite = null;
        ProgressDialog dialog = new ProgressDialog(StatisticsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Fetching data...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Document document = null;
            try {
                URL url = new URL("http://arafsheikh.pythonanywhere.com/stats.html");
                document = Jsoup.parse(url, 5000);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(document != null) {
                Element table = document.select("table").first();
                ite = table.select("td").iterator();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setContentView(R.layout.activity_statistics);
            TextView total = (TextView) findViewById(R.id.textView6);
            TextView name1 = (TextView) findViewById(R.id.textView11);
            TextView name2 = (TextView) findViewById(R.id.textView21);
            TextView name3 = (TextView) findViewById(R.id.textView31);
            TextView name4 = (TextView) findViewById(R.id.textView41);
            TextView name5 = (TextView) findViewById(R.id.textView51);
            TextView usage1 = (TextView) findViewById(R.id.textView12);
            TextView usage2 = (TextView) findViewById(R.id.textView22);
            TextView usage3 = (TextView) findViewById(R.id.textView32);
            TextView usage4 = (TextView) findViewById(R.id.textView42);
            TextView usage5 = (TextView) findViewById(R.id.textView52);
            TextView timestamp = (TextView) findViewById(R.id.textView7);

            if (ite != null) {
                name1.setText(ite.next().text());
                usage1.setText(ite.next().text());
                ite.next();
                name2.setText(ite.next().text());
                usage2.setText(ite.next().text());
                ite.next();
                name3.setText(ite.next().text());
                usage3.setText(ite.next().text());
                ite.next();
                name4.setText(ite.next().text());
                usage4.setText(ite.next().text());
                ite.next();
                name5.setText(ite.next().text());
                usage5.setText(ite.next().text());
                ite.next();

                total.setText(ite.next().text());
                ite.next();
                timestamp.setText(ite.next().text());
            }

            tvRank = (TextView) findViewById(R.id.tvMyRank);
            if (snuid != null) {
                new fetchRank().execute();
            }
            dialog.dismiss();
        }
    }

    private class fetchRank extends AsyncTask<Void, Void, Void> {
        String rank;

        @Override
        protected void onPreExecute() {
            tvRank.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Document document = null;
            try {
                String url_str = "http://arafsheikh.pythonanywhere.com/getrank?snuid=" + snuid;
                URL url = new URL(url_str);
                document = Jsoup.parse(url, 5000);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (document != null) {
                rank = document.body().getElementsByTag("h1").text();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            tvRank.setText("Your rank: "+
                    rank);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
