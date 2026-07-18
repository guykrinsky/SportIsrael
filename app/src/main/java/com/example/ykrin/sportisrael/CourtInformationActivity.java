package com.example.ykrin.sportisrael;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourtInformationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "sportIsrael";

    ImageButton back_button;
    TextView info_title;
    TextView court_description;
    TextView sport_badge;
    View status_dot;
    TextView status_label;
    LinearLayout detail_surface_row;
    TextView detail_surface_value;
    LinearLayout detail_lighting_row;
    TextView detail_lighting_value;
    LinearLayout detail_access_row;
    TextView detail_access_value;
    Button info_full_button;
    Button info_empty_button;
    Button info_players_button;

    Court court;
    FirebaseFirestore mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_information);

        back_button = findViewById(R.id.court_info_back);
        info_title = findViewById(R.id.info_court_name);
        court_description = findViewById(R.id.info_court_description);
        sport_badge = findViewById(R.id.court_sport_badge);
        status_dot = findViewById(R.id.court_status_dot);
        status_label = findViewById(R.id.court_status_label);
        detail_surface_row = findViewById(R.id.detail_surface_row);
        detail_surface_value = findViewById(R.id.detail_surface_value);
        detail_lighting_row = findViewById(R.id.detail_lighting_row);
        detail_lighting_value = findViewById(R.id.detail_lighting_value);
        detail_access_row = findViewById(R.id.detail_access_row);
        detail_access_value = findViewById(R.id.detail_access_value);
        info_empty_button = findViewById(R.id.court_is_empty);
        info_full_button = findViewById(R.id.court_is_full);
        info_players_button = findViewById(R.id.court_is_searching_for_players);

        mDB = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();
        final String court_title = extras == null ? null : extras.getString("court_title");
        if (court_title == null) {
            Log.e(TAG, "CourtInformationActivity didn't receive court_title");
            finish();
            return;
        }

        DocumentReference selected_document = mDB.collection("courts").document(court_title);
        selected_document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    court = task.getResult().toObject(Court.class);
                    show_court_information(court);
                } else {
                    Log.e(TAG, "There isn't a court with clicked marker title: " + court_title);
                    Toast.makeText(CourtInformationActivity.this,
                            "Selected court has no information", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        back_button.setOnClickListener(this);
        info_empty_button.setOnClickListener(this);
        info_full_button.setOnClickListener(this);
        info_players_button.setOnClickListener(this);
    }

    public void show_court_information(Court court) {
        info_title.setText(court.getTitle());
        court_description.setText(court.getDescription());

        // Sport badge tinted with the sport's identity color.
        SportType sport = SportType.fromValue(court.getSport());
        sport_badge.setText(sport.getDisplayName());
        GradientDrawable badge = (GradientDrawable) sport_badge.getBackground().mutate();
        badge.setColor(ContextCompat.getColor(this, sport.getColorRes()));

        updateStatusUI(CourtState.fromValue(court.getState()));
        showParsedDetails(court.getDescription());
    }

    /**
     * OSM-imported courts store details in the description as
     * "key: value" pairs separated by " · ". Surface structured
     * rows for the ones worth calling out.
     */
    private void showParsedDetails(String description) {
        if (description == null) {
            return;
        }
        for (String part : description.split("·")) {
            String piece = part.trim();
            String lower = piece.toLowerCase();
            if (lower.startsWith("surface:")) {
                detail_surface_value.setText(capitalize(piece.substring("surface:".length()).trim()));
                detail_surface_row.setVisibility(View.VISIBLE);
            } else if (lower.contains("lit at night")) {
                detail_lighting_value.setText("Lit at night");
                detail_lighting_row.setVisibility(View.VISIBLE);
            } else if (lower.startsWith("access:")) {
                detail_access_value.setText(capitalize(piece.substring("access:".length()).trim()));
                detail_access_row.setVisibility(View.VISIBLE);
            }
        }
    }

    private static String capitalize(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private void updateStatusUI(CourtState state) {
        int color;
        String label;
        switch (state) {
            case EMPTY:
                color = R.color.success;
                label = "Available";
                break;
            case FULL:
                color = R.color.danger;
                label = "Full";
                break;
            case SEARCHING:
                color = R.color.info;
                label = "Looking for players";
                break;
            default:
                color = R.color.gray;
                label = "Status unknown";
                break;
        }
        GradientDrawable dot = (GradientDrawable) status_dot.getBackground().mutate();
        dot.setColor(ContextCompat.getColor(this, color));
        status_label.setText(label);
    }

    @Override
    public void onClick(View button) {
        if (button == back_button) {
            finish();
            return;
        }

        CourtState new_state = null;
        if (button == info_empty_button) {
            new_state = CourtState.EMPTY;
        } else if (button == info_full_button) {
            new_state = CourtState.FULL;
        } else if (button == info_players_button) {
            new_state = CourtState.SEARCHING;
        }

        if (new_state != null && court != null) {
            persistState(new_state);
        }
    }

    /**
     * Writes the new state to Firestore (the single source of truth).
     * The UI updates optimistically but reverts if the write fails, so
     * it never permanently shows a state that wasn't persisted.
     */
    private void persistState(final CourtState new_state) {
        final CourtState previous_state = CourtState.fromValue(court.getState());
        if (new_state == previous_state) {
            return;
        }

        court.setState(new_state.getValue());
        updateStatusUI(new_state);
        setStatusButtonsEnabled(false);

        mDB.collection("courts").document(court.getTitle())
                .update("state", new_state.getValue())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setStatusButtonsEnabled(true);
                        Toast.makeText(CourtInformationActivity.this,
                                getString(R.string.court_state_changed, new_state.getValue()),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to persist court state", e);
                        court.setState(previous_state.getValue());
                        updateStatusUI(previous_state);
                        setStatusButtonsEnabled(true);
                        Toast.makeText(CourtInformationActivity.this,
                                "Couldn't update status - check your connection",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setStatusButtonsEnabled(boolean enabled) {
        info_empty_button.setEnabled(enabled);
        info_full_button.setEnabled(enabled);
        info_players_button.setEnabled(enabled);
    }
}
