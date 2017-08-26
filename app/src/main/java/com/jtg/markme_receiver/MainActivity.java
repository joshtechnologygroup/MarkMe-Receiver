package com.jtg.markme_receiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import io.chirp.sdk.ChirpSDK;
import io.chirp.sdk.ChirpSDKListener;
import io.chirp.sdk.ChirpSDKStatusListener;
import io.chirp.sdk.model.Chirp;
import io.chirp.sdk.model.ChirpError;
import io.chirp.sdk.model.ChirpProtocolName;
import io.chirp.sdk.result.ChirpProtocolResult;
import utils.MarkMeDB;
import utils.User;

public class MainActivity extends AppCompatActivity {

    private ChirpSDK chirpSDK;
    private AudioManager audioManager;
    private MarkMeDB markMeDB;
    private Button usersButton;

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
                    // Mark entry
                    ArrayList<String> userData = User.getUser(markMeDB.getReadableDatabase(), secretCode);
                    if(!userData.isEmpty()){
                        System.out.println("Matched User: " + userData.get(0));
                        sendChirp(chirpData);
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
//        this.createFakeUserData(9);
    }

    private void createFakeUserData(int n){
//        User.deleteUsersWithIN(this.markMeDB.getWritableDatabase(), "", new ArrayList<String>());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.usersButton = (Button)this.findViewById(R.id.usersButton);

        this.usersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent secretCodePage = new Intent(MainActivity.this, UserListActivity.class);
                startActivity(secretCodePage);
            }
        });

        this.initChirp();
        this.initReceiver();

        this.chirpSDK.start();
    }
}
