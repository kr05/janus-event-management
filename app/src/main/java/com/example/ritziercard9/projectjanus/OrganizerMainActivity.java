package com.example.ritziercard9.projectjanus;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class OrganizerMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SellersFragment.OnSellersFragmentInteractionListener, EventsFragment.OnEventsFragmentInteractionListener {

    private String selectedNav;
    private static final int NEW_EVENT_REQUEST = 10;
    private static final int NEW_SELLER_REQUEST = 20;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        android.support.v4.app.FragmentTransaction initTransaction = getSupportFragmentManager().beginTransaction();
        if (getIntent().getStringExtra("selected") != null) {
            String selected = getIntent().getStringExtra("selected");

            switch (selected) {
                case "events":
                    selectedNav = selected;
                    getSupportActionBar().setTitle("Eventos");
                    navigationView.setCheckedItem(R.id.nav_events);
                    EventsFragment eventsFragment = EventsFragment.newInstance();
                    initTransaction.replace(R.id.organizer_fragment_container, eventsFragment);
                    initTransaction.commit();
                    break;
                case "sellers":
                    getSupportActionBar().setTitle("Vendedores");
                    selectedNav = selected;
                    navigationView.setCheckedItem(R.id.nav_sellers);
                    SellersFragment sellersFragment = SellersFragment.newInstance();
                    initTransaction.replace(R.id.organizer_fragment_container, sellersFragment);
                    initTransaction.commit();
                    break;
//                case "access":
//                    getSupportActionBar().setTitle("Control de acceso");
//                    selectedNav = selected;
//                    navigationView.setCheckedItem(R.id.nav_access);
//                    Intent accessIntent= new Intent(this, ScannerEventsListActivity.class);
//                    startActivity(accessIntent);
//                    break;
//                case "tickets":
//                    getSupportActionBar().setTitle("Venta de boletos");
//                    selectedNav = selected;
//                    navigationView.setCheckedItem(R.id.nav_tickets);
//                    Intent ticketsIntent = new Intent(this, EventsListActivity.class);
//                    startActivity(ticketsIntent);
//                    break;
                default:
                    break;
            }
        } else {
            selectedNav = "sellers";
            getSupportActionBar().setTitle("Vendedores");
            navigationView.setCheckedItem(R.id.nav_sellers);
            SellersFragment sellersFragment = SellersFragment.newInstance();
            initTransaction.replace(R.id.organizer_fragment_container, sellersFragment);
            initTransaction.commit();
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedNav.equals("sellers")) {
                    Intent intent = new Intent(getApplicationContext(), NewSellerActivity.class);
                    startActivityForResult(intent, NEW_SELLER_REQUEST);
                } else if (selectedNav.equals("events")) {
                    Intent intent = new Intent(getApplicationContext(), NewEventActivity.class);
                    startActivityForResult(intent, NEW_EVENT_REQUEST);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.organizer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_sellers) {
            selectedNav = "sellers";
            getSupportActionBar().setTitle("Vendedores");
            SellersFragment sellersFragment = SellersFragment.newInstance();
            fragmentTransaction.replace(R.id.organizer_fragment_container, sellersFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_events) {
            selectedNav = "events";
            getSupportActionBar().setTitle("Eventos");
            EventsFragment eventsFragment = EventsFragment.newInstance();
            fragmentTransaction.replace(R.id.organizer_fragment_container, eventsFragment);
            fragmentTransaction.commit();
        }
//        else if (id == R.id.nav_access) {
//            selectedNav = "events";
//            getSupportActionBar().setTitle("Control de acceso");
//            Intent accessIntent= new Intent(this, ScannerEventsListActivity.class);
//            startActivity(accessIntent);
//        } else if (id == R.id.nav_tickets) {
//            selectedNav = "events";
//            getSupportActionBar().setTitle("Venta de boletos");
//            Intent ticketsIntent = new Intent(this, EventsListActivity.class);
//            startActivity(ticketsIntent);
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSellersFragmentInteraction(Uri uri) {

    }

    @Override
    public void onEventsFragmentInteraction(Uri uri) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("title");
                Snackbar.make(findViewById(R.id.organizer_fragment_container), "El evento para " + name + " a sido creado.", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
