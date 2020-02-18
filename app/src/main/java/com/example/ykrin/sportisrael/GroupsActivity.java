package com.example.ykrin.sportisrael;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

import com.ms.square.android.expandabletextview.ExpandableTextView;

public class GroupsActivity extends AppCompatActivity {

    BottomNavigationView menu_bar_view;
    ExpandableTextView expandable_text;
    String baketball_groups = " basketball groups \n"  +
            "all basketball groups:";
    String soccer_groups = " soccer \n"  +
            "all soccer groups:";
    String my_groups = " my groups \n"  +
            "all the groups you are member in:";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);


        expandable_text = (ExpandableTextView)findViewById(R.id.expand_text_view);
        expandable_text.setText(baketball_groups);

        expandable_text = (ExpandableTextView)findViewById(R.id.expand_text_soccer);
        expandable_text.setText(soccer_groups);

        expandable_text = (ExpandableTextView)findViewById(R.id.expand_text_my_groups);
        expandable_text.setText(my_groups);



        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);
    }
}
