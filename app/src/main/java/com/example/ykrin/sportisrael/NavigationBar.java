package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Bottom navigation between the three tab screens.
 *
 * Tab switches reuse the existing Activity instance when one is alive
 * (FLAG_ACTIVITY_REORDER_TO_FRONT), so each tab keeps its scroll
 * position, loaded data and view state instead of being recreated.
 * Reselecting the current tab is a no-op.
 */
public class NavigationBar implements BottomNavigationView.OnNavigationItemSelectedListener
{
    private final AppCompatActivity calling_activity;
    private final int current_tab_id;

    private NavigationBar(AppCompatActivity calling_activity, int current_tab_id)
    {
        this.calling_activity = calling_activity;
        this.current_tab_id = current_tab_id;
    }

    /**
     * Wires the bottom bar for an activity: highlights the activity's own
     * tab (if it is one of the tabs) and installs the switch listener.
     */
    public static void attach(AppCompatActivity activity, BottomNavigationView bar)
    {
        int current_tab_id = tabIdFor(activity);
        if (current_tab_id != 0)
        {
            // Highlight before installing the listener so it doesn't fire.
            bar.getMenu().findItem(current_tab_id).setChecked(true);
        }
        bar.setOnNavigationItemSelectedListener(new NavigationBar(activity, current_tab_id));
    }

    private static int tabIdFor(AppCompatActivity activity)
    {
        if (activity instanceof GroupsActivity)
            return R.id.action_groups;
        if (activity instanceof MapActivity)
            return R.id.action_map;
        if (activity instanceof ProfileActivity)
            return R.id.action_profile;
        return 0; // Not a tab screen (e.g. MainActivity).
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == current_tab_id)
        {
            // Already on this tab - don't reload the screen.
            return true;
        }

        Class<?> destination;
        switch(menuItem.getItemId())
        {
            case R.id.action_groups:
                destination = GroupsActivity.class;
                break;
            case R.id.action_map:
                destination = MapActivity.class;
                break;
            case R.id.action_profile:
                destination = ProfileActivity.class;
                break;
            default:
                return false;
        }

        Intent intent = new Intent(this.calling_activity, destination);
        // Reuse a live instance of the destination instead of creating a
        // new one, preserving its state (scroll, loaded data, inputs).
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.calling_activity.startActivity(intent);
        // Instant switch, no slide animation between tabs.
        this.calling_activity.overridePendingTransition(0, 0);
        return true;
    }
}
