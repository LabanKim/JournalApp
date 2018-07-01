package com.fusion.kim.journalapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class ModifyActivity extends AppCompatActivity {

    private String mTitle, mContent;

    private EditText mTitleInput, mContentInput;

    private DatabaseReference mEntriesRef;

    private ProgressDialog progressDialog;

    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        Bundle data = getIntent().getExtras();
        mTitle = data.getString("title");
        mContent = data.getString("content");

        mTitleInput = findViewById(R.id.et_entry_title);
        mContentInput = findViewById(R.id.et_entry_content);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = user.getUid().toString();

        mTitleInput.setText(mTitle);
        mContentInput.setText(mContent);

        mEntriesRef = FirebaseDatabase.getInstance().getReference().child("Entries").child(mCurrentUserId);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Entry...");
        progressDialog.setIndeterminate(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {


            String title = mTitleInput.getText().toString().trim();
            String content = mContentInput.getText().toString().trim();

            if (TextUtils.isEmpty(title)){

                mTitleInput.setError("Title Cannot Be Empty");

            }

            if (TextUtils.isEmpty(content)){

                mContentInput.setError("Entry Cannot Be Empty");

            }

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)){

                progressDialog.show();

                Map entryMap = new HashMap();
                entryMap.put("title", title);
                entryMap.put("content", content);
                entryMap.put("date", ServerValue.TIMESTAMP);

                mEntriesRef.push().setValue(entryMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            progressDialog.dismiss();

                            Toast.makeText(ModifyActivity.this, "Entry Updated", Toast.LENGTH_LONG).show();

                        } else {

                            Toast.makeText(ModifyActivity.this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
