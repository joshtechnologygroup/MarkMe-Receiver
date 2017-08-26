package com.jtg.markme_receiver;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.util.ArrayList;

import utils.MarkMeDB;
import utils.User;

public class UserListActivity extends AppCompatActivity {

    private TextView userListText;
    private MarkMeDB markMeDB;

    private void populateUserListView(SQLiteDatabase db){
        ArrayList<ArrayList<String>> userRows = User.getAllUsers(db);
        if(userRows.isEmpty()){
            this.userListText.setText(R.string.no_user_text);
        }
        else{
            this.userListText.setText("");
            for(ArrayList<String> row: userRows){
                this.userListText.append("- " + android.text.TextUtils.join(", ", row) + "\n");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        this.markMeDB = new MarkMeDB(this.getApplicationContext());
        this.userListText = (TextView)this.findViewById(R.id.userListText);
        this.userListText.setMovementMethod(new ScrollingMovementMethod());
        this.populateUserListView(this.markMeDB.getReadableDatabase());

    }
}
