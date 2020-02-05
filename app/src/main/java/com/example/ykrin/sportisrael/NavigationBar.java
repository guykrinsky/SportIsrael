package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class NavigationBar implements BottomNavigationView.OnNavigationItemSelectedListener
{
    private AppCompatActivity calling_activity;

    public NavigationBar(AppCompatActivity calling_activity)
    {
        this.calling_activity = calling_activity;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId())
        {
            case R.id.action_groups:
                Intent intent_groups = new Intent(this.calling_activity, GroupsActivity.class);
                this.calling_activity.startActivity(intent_groups);
                break;
            case R.id.action_map:
                Intent intent_map = new Intent(this.calling_activity, MapActivity.class);
                this.calling_activity.startActivity(intent_map);
                break;
            case R.id.action_profile:
                Intent intent_profile = new Intent(this.calling_activity, ProfileActivity.class);
                this.calling_activity.startActivity(intent_profile);
                break;
            default:
                return false;
        }
        return true;
    }
}
