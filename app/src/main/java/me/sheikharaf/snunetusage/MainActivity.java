package me.sheikharaf.snunetusage;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    String NetId, pass;
    Document document, usage_page;
    Connection.Response loginForm = null;
    Connection.Response usage_login = null;
    EasyTracker easyTracker = EasyTracker.getInstance(this);
    MenuItem menu_item_refresh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRefreshActionButtonState(menu_item_refresh, true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            NetId = extras.getString("username");
            pass = extras.getString("password");
            new FetchPage().execute();
        }

        TextView tvCredit = (TextView) findViewById(R.id.textView2);
        tvCredit.setText(Html.fromHtml(getString(R.string.credit)));
        tvCredit.setMovementMethod(LinkMovementMethod.getInstance());
        tvCredit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            easyTracker.send(MapBuilder
                                                            .createEvent("ui_action",     // Event category (required)
                                                                    "button_press",  // Event action (required)
                                                                    "website_textview",   // Event label
                                                                    null)            // Event value
                                                            .build()
                                            );
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse("http://sheikharaf.me/about"));
                                            startActivity(i);
                                        }
                                    }
        );

        Calendar rightNow = Calendar.getInstance();
        if (rightNow.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
            // Send notification about user's rank every Wednesday
            SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            Boolean notificationTriggered = sharedPreferences.getBoolean("notificationTriggered", true);
            if (!notificationTriggered) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("notificationTriggered", true);
                editor.apply();
                new fetchRankAndNotify().execute();
            }
        }
        else {
            SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notificationTriggered", false);
            editor.apply();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_forget) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("username");
                            editor.remove("password");
                            editor.apply();
                            Intent backToLogin = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(backToLogin);
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

            easyTracker.send(MapBuilder
                    .createEvent("ui_action",     // Event category (required)
                            "forget_option",  // Event action (required)
                            "forget_clicked",   // Event label
                            null)            // Event value
                    .build());
            return true;
        }

        if (id == R.id.action_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Hey! This app shows me my data usage in one tap. Check it out: https://play.google.com/store/apps/details?id=me.sheikharaf.snunetusage";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this app");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            easyTracker.send(MapBuilder
                    .createEvent("ui_action",     // Event category (required)
                            "share_option",  // Event action (required)
                            "share_clicked",   // Event label
                            null)            // Event value
                    .build());
        }

        if (id == R.id.action_help) {
            Intent showHelp = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(showHelp);
        }

        if (id == R.id.action_refresh) {
            menu_item_refresh = item;
            setRefreshActionButtonState(menu_item_refresh, true);
            new FetchPage().execute();
            easyTracker.send(MapBuilder
                    .createEvent("ui_action",     // Event category (required)
                            "refresh_menu",  // Event action (required)
                            "refresh_clicked",   // Event label
                            null)            // Event value
                    .build());
        }

        if (id == R.id.action_stats) {
            Intent startStatsAct = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(startStatsAct);
        }

        return super.onOptionsItemSelected(item);
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

    private class FetchPage extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Set progressdialog title
            //mProgressDialog.setTitle("Android Jsoup ListView Tutorial");
            // Set progressdialog message
            //mProgressDialog.setMessage("Loading...");
            //mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            //mProgressDialog.show();
            setRefreshActionButtonState(menu_item_refresh, true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                loginForm = Jsoup.connect("http://myaccount.snu.edu.in/login.php")
                        .method(Connection.Method.GET)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                document = Jsoup.connect("http://myaccount.snu.edu.in/loginSubmit.php")
                        .data("cookieexists", "false")
                        .data("snuNetId", NetId)
                        .data("password", pass)
                        .data("submit", "Login")
                        .cookies(loginForm.cookies())
                        .post();
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new FetchResult().execute();
        }
    }

    private class FetchResult extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                usage_login = Jsoup.connect("http://myaccount.snu.edu.in/myAccountInfo.php")
                        .method(Connection.Method.GET)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Calculate the date of previous Wednesday
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Calendar date = Calendar.getInstance();
            String today = df.format(date.getTime());
            int diff = (date.get(Calendar.DAY_OF_WEEK) - Calendar.WEDNESDAY) % 7;
            if (!(diff >= 0)) {
                diff += 7;
            }
            date.add(Calendar.DAY_OF_MONTH, (-diff));
            String last_wednesday = df.format(date.getTime());


            try {
                usage_page = Jsoup.connect("http://myaccount.snu.edu.in/myAccountInfo.php")
                        .data("startDate", last_wednesday)
                        .data("endDate", today)
                        .data("submit", "Submit")
                        .cookies(loginForm.cookies())
                        .post();
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                Element elementsByTag = usage_page.getElementsByTag("tfoot").get(0);
                final String usage = elementsByTag.getElementsByTag("th").last().text().substring(13);
                double usage_double = Double.valueOf(usage);
                int percentage = (int) (usage_double * 100) / 3;
                if (percentage == 0) percentage = 1; //For the progress bar

                final TextView tvResult = (TextView) findViewById(R.id.tvResult);

                final CircularProgressBar c1 = (CircularProgressBar) findViewById(R.id.circularprogressbar1);

                c1.animateProgressTo(0, percentage, new CircularProgressBar.ProgressAnimationListener() {

                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationProgress(int progress) {
                        c1.setTitle(progress + "%");
                    }

                    @Override
                    public void onAnimationFinish() {
                        c1.setSubTitle("used until now");
                        tvResult.setText(usage + " GB");
                    }
                });
                setRefreshActionButtonState(menu_item_refresh, false);

            } catch (IndexOutOfBoundsException e) {
                SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("username");
                editor.remove("password");
                editor.apply();

                Toast.makeText(getApplicationContext(), "Network communication issue. Try again later", Toast.LENGTH_LONG).show();
                setRefreshActionButtonState(menu_item_refresh, false);
                Intent backToLogin = new Intent(getApplicationContext(), LoginActivity.class);
                backToLogin.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(backToLogin);
            } catch (NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Network communication issue. Try again later", Toast.LENGTH_LONG).show();
                setRefreshActionButtonState(menu_item_refresh, false);
            }
        }
    }

    public class fetchRankAndNotify extends AsyncTask<Void, Void, Void> {
        String rank;

        @Override
        protected Void doInBackground(Void... params) {
            Document document = null;
            try {
                String url_str = "http://arafsheikh.pythonanywhere.com/getrank?snuid=" + NetId;
                URL url = new URL(url_str);
                document = Jsoup.parse(url, 5000);
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
            Context context = getApplication();
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Weekly rank")
                    .setContentText("Your data usage rank for last week was "+rank+"!")
                    .build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    notificationManager.notify(1000, notification);
                }
            }, 5000);
        }
    }

    public void setRefreshActionButtonState(MenuItem refreshItem, final boolean refreshing) {
        //final MenuItem refreshItem = findViewById(R.id.action_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }
}
