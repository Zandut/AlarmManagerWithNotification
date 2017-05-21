package com.example.zandut.customcalenderview;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "zandutmobile@gmail.com";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView textBulan;
    private CompactCalendarView calender;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM yyyy");
    private List<com.google.api.services.calendar.model.Event> arrayEvent = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = new ProgressDialog(this);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();

        textBulan = (TextView) findViewById(R.id.textView);
        calender = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        calender.setUseThreeLetterAbbreviation(true);

        long waktu = SystemClock.elapsedRealtime() + 10 * 1000;
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            waktu = extras.getLong("waktu");
        }

//        Date date1 = new Date(2017)
//
//        Calendar cur_cal = Calendar.getInstance();
//        cur_cal.setTimeInMillis(System.currentTimeMillis());
//
//        cur_cal.set(Calendar.HOUR_OF_DAY, 12);
//        cur_cal.set(Calendar.MINUTE, 23);
//        cur_cal.set(Calendar.SECOND, 15);
//
//        Calendar cur_call = Calendar.getInstance();
//        cur_call.setTimeInMillis(System.currentTimeMillis());
//
//        cur_call.set(Calendar.HOUR_OF_DAY, 12);
//        cur_call.set(Calendar.MINUTE, 23);
//        cur_call.set(Calendar.SECOND,20);
//
//        Intent intent1 = new Intent(this, MyService.class);
//        intent1.putExtra("title", "Alarm 1");
//        intent1.putExtra("id", 1);
//
//
//
//
//
//        PendingIntent pi = PendingIntent.getService(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarm_manager.set(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis() , pi);
//
//        intent1.putExtra("title", "Alarm 2");
//        intent1.putExtra("id", 2);
//        PendingIntent p1i = PendingIntent.getService(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        alarm_manager.set(AlarmManager.RTC_WAKEUP, cur_call.getTimeInMillis() , p1i);


//        calender.setCurrentDate(new Date(2015, 5, 7));


        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");

        textBulan.setText(simpleDateFormat.format(calender.getFirstDayOfCurrentMonth()));


    }

    private void getResultsFromApi()
    {
        if (!isGooglePlayServicesAvailable())
        {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null)
        {
            chooseAccount();
        } else if (!isDeviceOnline())
        {
//            mOutputText.setText("No network connection available.");
        } else
        {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount()
    {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS))
        {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null)
            {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else
            {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else
        {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
////                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                } else
                {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null)
                {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list)
    {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list)
    {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class Mahasiswa
    {
        private int id;
        private String nama;

        public Mahasiswa(int id, String nama)
        {
            this.id = id;
            this.nama = nama;
        }

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getNama()
        {
            return nama;
        }

        public void setNama(String nama)
        {
            this.nama = nama;
        }
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>>
    {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params)
        {
            try
            {
                return getDataFromApi();
            } catch (Exception e)
            {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException
        {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            List<EventReminder> arrayReminder = new ArrayList<>();
            EventReminder eventReminder = new EventReminder();

            eventReminder.setMethod("popup");
            eventReminder.setMinutes(0);


            arrayReminder.add(eventReminder);
            Events events = mService.events().list("zandutmobile@gmail.com")

                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            events.setDefaultReminders(arrayReminder);

            arrayEvent = events.getItems();


            return eventStrings;
        }


        @Override
        protected void onPreExecute()
        {
//            mOutputText.setText("");
            mProgress.show();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(List<String> output)
        {
            mProgress.hide();
//            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
//            } else {
//                output.add(0, "Data retrieved using the Google Calendar API:");
//                mOutputText.setText(TextUtils.join("\n", output));
//            }


            Date date = null;
            int i = 0;
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            for (com.google.api.services.calendar.model.Event event : arrayEvent)
            {

//                event.getReminders().setUseDefault(false);
                try
                {
                    date = dateFormat2.parse(event.getStart().getDateTime().toString());
                    calender.addEvent(new Event(Color.RED, date.getTime(), event));
                    long selisih = date.getTime() - System.currentTimeMillis();

                    Date now = new Date(System.currentTimeMillis());

                    Toast.makeText(MainActivity.this, date.getHours()+":"+date.getMinutes(), Toast.LENGTH_SHORT).show();
                    if (System.currentTimeMillis() < date.getTime())
                    {

                        i++;
                        Toast.makeText(MainActivity.this, "Jml "+i, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        intent.putExtra("title", event.getSummary());
                        intent.putExtra("content", dateFormat.format(date));
                        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, event.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        am.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
                    }





                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }



//

            calender.setListener(new CompactCalendarView.CompactCalendarViewListener()
            {
                @Override
                public void onDayClick(Date dateClicked)
                {
                    List<Event> event1 = calender.getEvents(dateClicked);
                    if (event1.size() > 0)
                    {
                        com.google.api.services.calendar.model.Event evt = (com.google.api.services.calendar.model.Event) event1.get(0).getData();
                        Toast.makeText(MainActivity.this, evt.getSummary(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth)
                {
                    textBulan.setText(simpleDateFormat.format(firstDayOfNewMonth));
                }


            });
        }

        @Override
        protected void onCancelled()
        {
            mProgress.hide();
            if (mLastError != null)
            {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException)
                {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else
                {
                    Toast.makeText(MainActivity.this, mLastError.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            } else
            {
//                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
