package com.fusion.kim.journalapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mEntriesRef;

    private RecyclerView mMainRecycler;

    private GoogleSignInClient mClient;

    private TextView mMainHeaderTv;

    private String mKey, mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();
                }

            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser user = mAuth.getCurrentUser();
        mCurrentUserId = user.getUid().toString();

        mMainRecycler = findViewById(R.id.rv_main);
        mMainRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMainRecycler.setHasFixedSize(true);

        mEntriesRef = FirebaseDatabase.getInstance().getReference().child("Entries").child(mCurrentUserId);

        FirebaseDatabase.getInstance().getReference().child("Entries")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mCurrentUserId)){

                } else {

                    startActivity(new Intent(MainActivity.this, NewEntryActivity.class));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewEntryActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        final Query query = mEntriesRef.limitToLast(50).orderByChild("date");

        FirebaseRecyclerOptions<Entry> options =
                new FirebaseRecyclerOptions.Builder<Entry>()
                        .setQuery(query, Entry.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Entry, EntryViewHolder> adapter = new FirebaseRecyclerAdapter<Entry, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder holder, int position, @NonNull final Entry model) {

                holder.mTitleTv.setText(model.getTitle());
                holder.mContentTv.setText(model.getContent());
                holder.mDateTv.setText(toDate(model.getDate()) + ", " + getTime(model.getDate()));


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent contentIntent = new Intent(MainActivity.this, ViewEntryContentActivity.class);
                        contentIntent.putExtra("title", model.getTitle());
                        contentIntent.putExtra("content", model.getContent());
                        contentIntent.putExtra("date", model.getDate());
                        startActivity(contentIntent);

                    }
                });

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new EntryViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_item, parent, false));
            }
        };
        adapter.startListening();
        mMainRecycler.setAdapter(adapter);
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
        if (id == R.id.action_logout) {

            mClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mAuth.signOut();

                            }

                        }
                    });


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class EntryViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mTitleTv;
        private TextView mContentTv;
        private TextView mDateTv;

        private View mSeparator;

        public EntryViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTitleTv = itemView.findViewById(R.id.tv_item_title);
            mContentTv = itemView.findViewById(R.id.tv_item_content);
            mDateTv = itemView.findViewById(R.id.tv_item_date);
            mSeparator = itemView.findViewById(R.id.item_separator);

        }
    }

    private String getTime(long mills){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        String time = null;

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String modHour, modMinute;

        if ( hour < 10){

            modHour = "0"+hour;

        }else {
            modHour = ""+hour;
        }

        if (minute < 10){

            modMinute = "0"+minute;

        }else {

            modMinute = ""+minute;

        }

        return time = modHour + ":" + modMinute;

    }

    private String toDate(long timestamp) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }


}
