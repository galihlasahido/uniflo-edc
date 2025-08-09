package id.uniflo.uniedc.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.ui.SettingsActivity;

public class SettingsFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        // Setup click listener to open full settings
        CardView cardSettings = view.findViewById(R.id.card_edc_settings);
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            });
        }
        
        return view;
    }
}