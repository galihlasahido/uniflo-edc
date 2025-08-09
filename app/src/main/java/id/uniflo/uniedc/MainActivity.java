package id.uniflo.uniedc;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import id.uniflo.uniedc.fragments.DashboardFragment;
import id.uniflo.uniedc.fragments.HistoryFragment;
import id.uniflo.uniedc.fragments.ReportsFragment;
import id.uniflo.uniedc.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    // private FloatingActionButton fabCenter;
    private Fragment currentFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_modern);
        
        initViews();
        setupBottomNavigation();
        setupFAB();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }
    
    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        // FAB removed from layout
        // fabCenter = findViewById(R.id.fab_center);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.nav_beranda:
                    fragment = new DashboardFragment();
                    break;
                case R.id.nav_transaksi:
                    fragment = new HistoryFragment();
                    break;
                case R.id.nav_notifikasi:
                    fragment = new ReportsFragment();
                    break;
                case R.id.nav_profil:
                    fragment = new SettingsFragment();
                    break;
                case R.id.nav_placeholder:
                    // Do nothing - this is placeholder for FAB
                    return false;
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });
        
        // Remove shifting mode for consistent icon sizes
        bottomNavigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
    }
    
    private void setupFAB() {
        // FAB removed from layout
        /*
        // Add bounce animation on load
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        fabCenter.startAnimation(bounceAnimation);
        
        fabCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add ripple animation
                Animation pulseAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.pulse);
                fabCenter.startAnimation(pulseAnimation);
                
                // Navigate to payment activity
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
        */
    }
    
    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    @Override
    public void onBackPressed() {
        if (currentFragment instanceof DashboardFragment) {
            super.onBackPressed();
        } else {
            // Navigate back to home
            bottomNavigation.setSelectedItemId(R.id.nav_beranda);
        }
    }
}