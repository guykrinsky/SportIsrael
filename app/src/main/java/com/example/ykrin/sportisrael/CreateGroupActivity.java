package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateGroupActivity extends AppCompatActivity {

    ImageButton back_button;
    EditText name_field;
    EditText description_field;
    LinearLayout sport_chips;
    LinearLayout skill_chips;
    SwitchCompat private_switch;
    Button submit_button;
    ProgressBar progress;

    SportType selected_sport = null;
    SkillLevel selected_skill = SkillLevel.ANY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        back_button = findViewById(R.id.create_back);
        name_field = findViewById(R.id.create_name);
        description_field = findViewById(R.id.create_description);
        sport_chips = findViewById(R.id.create_sport_chips);
        skill_chips = findViewById(R.id.create_skill_chips);
        private_switch = findViewById(R.id.create_private_switch);
        submit_button = findViewById(R.id.create_submit);
        progress = findViewById(R.id.create_progress);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        for (SportType sport : SportType.values()) {
            sport_chips.addView(makeSportChip(sport));
        }
        for (SkillLevel level : SkillLevel.values()) {
            skill_chips.addView(makeSkillChip(level));
        }
        updateChipSelection(sport_chips, selected_sport);
        updateChipSelection(skill_chips, selected_skill);

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private TextView makeChip(String label, Object tag) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextSize(14);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color));
        chip.setBackgroundResource(R.drawable.chip_background);
        int padH = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        int padV = getResources().getDimensionPixelSize(R.dimen.spacing_s);
        chip.setPadding(padH, padV, padH, padV);
        chip.setTag(tag);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.spacing_s);
        chip.setLayoutParams(params);
        return chip;
    }

    private TextView makeSportChip(final SportType sport) {
        TextView chip = makeChip(sport.getDisplayName(), sport);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_sport = sport;
                updateChipSelection(sport_chips, selected_sport);
            }
        });
        return chip;
    }

    private TextView makeSkillChip(final SkillLevel level) {
        TextView chip = makeChip(level.getDisplayName(), level);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_skill = level;
                updateChipSelection(skill_chips, selected_skill);
            }
        });
        return chip;
    }

    private void updateChipSelection(LinearLayout container, Object selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View chip = container.getChildAt(i);
            chip.setSelected(chip.getTag() == selected);
        }
    }

    private void submit() {
        String name = name_field.getText().toString().trim();
        if (name.length() < 3) {
            name_field.setError(getString(R.string.groups_name_required));
            return;
        }
        if (selected_sport == null) {
            Toast.makeText(this, R.string.groups_sport_required, Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.user_null, Toast.LENGTH_SHORT).show();
            return;
        }

        String description = description_field.getText().toString().trim();
        Group group = new Group(name, description, selected_sport.getValue(),
                private_switch.isChecked(), user.getUid(), user.getDisplayName(),
                selected_skill.getValue());

        submit_button.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        final DocumentReference new_doc =
                FirebaseFirestore.getInstance().collection(GroupActions.GROUPS).document();
        new_doc.set(group).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progress.setVisibility(View.GONE);
                submit_button.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(CreateGroupActivity.this,
                            R.string.groups_created, Toast.LENGTH_SHORT).show();
                    // Land the creator directly in their new group.
                    Intent intent = new Intent(CreateGroupActivity.this, GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, new_doc.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateGroupActivity.this,
                            getString(R.string.error_prefix)
                                    + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
