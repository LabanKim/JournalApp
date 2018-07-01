package com.fusion.kim.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ViewEntryContentActivity extends AppCompatActivity {

    private TextView mTitleTv, mContentTv, mDateTv;

    private String mTitle, mContent, mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View Entry");

        mTitleTv = findViewById(R.id.tv_entry_title);
        mContentTv = findViewById(R.id.tv_entry_content);
        mDateTv = findViewById(R.id.tv_entry_date);

        Bundle data = getIntent().getExtras();
        mTitle = data.getString("title");
        mContent = data.getString("content");

        mTitleTv.setText(mTitle);
        mContentTv.setText(mContent);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editIntent = new Intent(ViewEntryContentActivity.this, ModifyActivity.class);
                editIntent.putExtra("title", mTitle);
                editIntent.putExtra("content", mContent);
                editIntent.putExtra("date", mDate);
                editIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(editIntent);
            }
        });
    }

}
