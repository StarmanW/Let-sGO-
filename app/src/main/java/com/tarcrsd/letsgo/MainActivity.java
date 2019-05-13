package com.tarcrsd.letsgo;

import android.content.Intent;
import android.graphics.Color;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tarcrsd.letsgo.Adapters.PagerAdapter;

public class MainActivity extends AppCompatActivity {
    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * On Create method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseApp, Firestore and FirebaseAuth
        FirebaseApp.initializeApp(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
        }

        // Initialize tab fragments & event recycle view
        initTabFragments();
    }

    /**
     * Initialize tab fragments
     */
    private void initTabFragments() {
        /*
         * Create an instance of the tab layout from the view.
         * Set the text for each tab.
         */
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_upcomingEvents));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_attending));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_profile));
        tabLayout.setTabTextColors(Color.WHITE, Color.WHITE);

        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Use PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Create menu option at top
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // MenuInflater class is used to instantiate menu XML files into Menu objects.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createNewEvent:
                Intent createNewEventIntent = new Intent(getApplicationContext() ,CreateEventActivity.class);
                startActivity(createNewEventIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
