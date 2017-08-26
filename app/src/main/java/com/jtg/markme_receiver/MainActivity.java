package com.jtg.markme_receiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.chirp.sdk.ChirpSDK;
import io.chirp.sdk.ChirpSDKListener;
import io.chirp.sdk.ChirpSDKStatusListener;
import io.chirp.sdk.model.Chirp;
import io.chirp.sdk.model.ChirpError;
import io.chirp.sdk.model.ChirpProtocolName;
import io.chirp.sdk.result.ChirpProtocolResult;
import utils.MarkMeDB;
import utils.User;
import utils.UserAttendance;

public class MainActivity extends AppCompatActivity {

    private ChirpSDK chirpSDK;
    private AudioManager audioManager;
    private MarkMeDB markMeDB;
    private Button usersButton, syncPageButton;
    private TextView liveFeedText;

    public final String ENTRY_CODE = "01";
    public final String EXIT_CODE = "02";

    public final String ACCEPT_MESSAGE = "%s Marked. :)";
    public final String REJECT_MESSAGE = "Request Failed. Try Again.";

    public ChirpSDKListener sdkListener = new ChirpSDKListener() {

        @Override
        public void onChirpHeard(final Chirp chirp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chirpData = chirp.getChirpData().getIdentifier();
                    String secretCode = chirpData.substring(0, 6);
                    String entryType = chirpData.substring(6, 8);
                    System.out.println("Heard: " + secretCode + ", type: "+ entryType + "\n");
                    ArrayList<String> userData = User.getUser(markMeDB.getReadableDatabase(), secretCode);
                    if(!userData.isEmpty()){
                        String inTime = "";
                        String outTime = "";
                        String timeStamp = new SimpleDateFormat("dd MMM HH:mm").format(
                            Calendar.getInstance().getTime()
                        );
                        System.out.println("- "+ entryType + "  - "+ ENTRY_CODE );
                        if(entryType.contentEquals(ENTRY_CODE)){
                            inTime = timeStamp;
                        }
                        else {
                            outTime = timeStamp;
                        }
                        ArrayList<String> latestUserAttendanceData = UserAttendance.getLatestUserAttendance(
                            markMeDB.getReadableDatabase(), userData.get(0)
                        );
                        if(latestUserAttendanceData.isEmpty()){
                            if(!inTime.isEmpty()) {
                                UserAttendance.insertOrUpdateUserAttendance(
                                        markMeDB.getWritableDatabase(), userData.get(0), inTime, outTime, true
                                );
                                System.out.println(String.format("%20s %30s %30s\n", userData.get(1), inTime, outTime));
                                updateLiveView();
                                sendChirp(chirpData);
                            }
                        }
                        else{
                            String objInTime = latestUserAttendanceData.get(2);
                            String objOutTime = latestUserAttendanceData.get(2);

                            if(objOutTime.isEmpty()){
                                if(!outTime.isEmpty()){
                                    UserAttendance.insertOrUpdateUserAttendance(
                                            markMeDB.getWritableDatabase(), userData.get(0), inTime, outTime, false
                                    );
                                    System.out.println(String.format("%20s %30s %30s\n", userData.get(1), inTime, outTime));
                                    updateLiveView();
                                    sendChirp(chirpData);
                                }
                            }
                            else{
                                if(!inTime.isEmpty()){
                                    UserAttendance.insertOrUpdateUserAttendance(
                                            markMeDB.getWritableDatabase(), userData.get(0), inTime, outTime, true
                                    );
                                    System.out.println(String.format("%20s %30s %30s\n", userData.get(1), inTime, outTime));
                                    updateLiveView();
                                    sendChirp(chirpData);
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onChirpHearStarted() {
            System.out.println("Starting hearing");
        }

        @Override
        public void onChirpHearFailed() {
            System.out.println("hearing failed");
        }

        @Override
        public void onChirpError(ChirpError chirpError) {
            System.out.println("hearing error "+ chirpError.getMessage());
        }
    };

    private void initReceiver(){
        SharedPreferences sharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_name),
            MODE_PRIVATE
        );
        String last_sync = sharedPreferences.getString(getString(R.string.last_sync_key_name), "");
        if(last_sync.isEmpty()){
            Intent syncPage = new Intent(MainActivity.this, SyncActivity.class);
            startActivity(syncPage);
        }
        this.markMeDB = new MarkMeDB(this.getApplicationContext());
        this.audioManager = (AudioManager) getApplicationContext().getSystemService(
            this.getApplicationContext().AUDIO_SERVICE
        );
        this.updateLiveView();
//        this.createFakeUserData(9);
    }

    private void createFakeUserData(int n){
        User.deleteUsersWithIN(this.markMeDB.getWritableDatabase(), "", new ArrayList<String>());
        for(int i=1; i <= n; i++)
            User.insertOrUpdateUser(
                this.markMeDB.getWritableDatabase(), "EMP-"+i, "User-"+i, "HR-26-"+i, "aabb0"+i
            );
//        User.insertOrUpdateUser(
//            this.markMeDB.getWritableDatabase(), "EMP-3", "User-11", "HR-26-11", "aabbcc11"
//        );
    }

    protected void initChirp() {
        String API_KEY = "xhYw3G5PKSHzzy1rIBoOrMSbZ";
        String API_SECRET = "tou2ncHSbP6uE2qihwdhHItO4AqGm0abOmWRq1KVxvzSIuMxew";

        this.chirpSDK = new ChirpSDK(this.getApplicationContext(), API_KEY, API_SECRET, new ChirpSDKStatusListener() {

            @Override
            public void onAuthenticationSuccess() {
                System.out.println("Auth - ok");
            }

            @Override
            public void onChirpError(ChirpError chirpError) {
                System.out.println("init " + chirpError.getMessage());
            }
        });

        ChirpProtocolResult chirpProtocolResult = chirpSDK.setProtocolNamed(ChirpProtocolName.ChirpProtocolNameUltrasonic);

        if (chirpProtocolResult != ChirpProtocolResult.OK) {
            System.out.println("Error setting ultrasonic mode: " + chirpProtocolResult.getDescription());
        }

        chirpSDK.setListener(sdkListener);
    }

    protected void sendChirp(String chirpData) {
        int origVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        System.out.println(origVolume + " , " + maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        System.out.println("1 -- " + chirpData);
        chirpSDK.chirp(new Chirp(chirpData));
//            TODO: Reset volume to original after sending data.
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, origVolume, 0);
    }

    private void updateLiveView(){
        String liveFeedHeader = String.format(
                "%20s %20s %20s\n%60s\n",
                "Employee", "In-Time", "Out-Time",
                "----------------------------------------------------------------------------------------------------"
        );
        this.liveFeedText.setText(liveFeedHeader);
        ArrayList<ArrayList<String>> userAttendnces = UserAttendance.getAllUserAttendnces(markMeDB.getReadableDatabase());
        for(ArrayList<String> userAttendnceData: userAttendnces){
            System.out.println(userAttendnceData);
            liveFeedText.append(String.format("%20s %30s %30s\n",
                    userAttendnceData.get(0),
                    userAttendnceData.get(1),
                    userAttendnceData.get(2)));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.usersButton = (Button)this.findViewById(R.id.usersButton);
        this.syncPageButton = (Button)this.findViewById(R.id.syncPageButton);
        this.liveFeedText = (TextView) this.findViewById(R.id.liveFeedText);

        this.liveFeedText.setMovementMethod(new ScrollingMovementMethod());

        this.usersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent usersPage = new Intent(MainActivity.this, UserListActivity.class);
                startActivity(usersPage);
            }
        });

        this.syncPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent syncPage = new Intent(MainActivity.this, SyncActivity.class);
                startActivity(syncPage);
            }
        });

        this.initChirp();
        this.initReceiver();

        this.chirpSDK.start();
    }
}
