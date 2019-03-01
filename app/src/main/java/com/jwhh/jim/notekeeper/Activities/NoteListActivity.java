package com.jwhh.jim.notekeeper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jwhh.jim.notekeeper.Adapters.NoteRecyclerAdapter;
import com.jwhh.jim.notekeeper.DataManager;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.R;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {
    NoteRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {
        //Set up the recycler view
        final RecyclerView recyclerNote = (RecyclerView) findViewById(R.id.recycler_list_notes);
        LinearLayoutManager noteLayoutManager = new LinearLayoutManager(this);
        recyclerNote.setLayoutManager(noteLayoutManager);
//Retrieve the notes to be displayed
        mAdapter = new NoteRecyclerAdapter(this, null);
        recyclerNote.setAdapter(mAdapter);

    }

}
