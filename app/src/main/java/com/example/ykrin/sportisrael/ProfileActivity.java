package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener
{
    static final String BIBI_USERNAME = "Bejamin";
    static final String BIBI_DESCRIPTION = "The prime minister of Israel";

    BottomNavigationView menu_bar_view;
    ImageView profile_icon;
    Button friend_request_button;
    Button invite_button;
    TextView username;
    TextView profile_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        menu_bar_view = (BottomNavigationView)findViewById(R.id.navigation_bar);
        profile_icon =(ImageView) findViewById(R.id.ivProfile);
        friend_request_button = (Button) findViewById(R.id.btnFriendRequest);
        invite_button = (Button) findViewById(R.id.btnMessage);
        username = (TextView)findViewById(R.id.tvName);
        profile_description = (TextView)findViewById(R.id.tvDescription);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bibi_sport_israel);

        //changing icon to circle
        RoundedBitmapDrawable mDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        mDrawable.setCircular(true);
        profile_icon.setImageDrawable(mDrawable);


        NavigationBar navigation_bar = new NavigationBar(this);
        // Set actions for pressing menu bar options.
        menu_bar_view.setOnNavigationItemSelectedListener(navigation_bar);

        invite_button.setOnClickListener(this);
        friend_request_button.setOnClickListener(this);

        updateUserDetails();
    }

    public void updateUserDetails()
    {
        username.setText(BIBI_USERNAME);
        profile_description.setText(BIBI_DESCRIPTION);
    }
    @Override
    public void onClick(View button_pressed)
    {
        if (button_pressed == friend_request_button)
        {
            if (friend_request_button.getText().toString().equalsIgnoreCase("friend request"))
            {
                friend_request_button.setText("Send");
            }
            else
            {
                    friend_request_button.setText("friend request");
            }
        }
        if (button_pressed == invite_button)
        {
            // If pressed on invite button, move to game map activity.
            Toast.makeText(this, "invitation sent", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "select a court to play on", Toast.LENGTH_LONG).show();
            Intent intent_map = new Intent(this, MapActivity.class);
            this.startActivity(intent_map);
        }

    }

}
