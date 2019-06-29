package asdevs.expensebook;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import asdevs.expensebook.fragment.ExpenseFragment;
import asdevs.expensebook.fragment.UserFragment;

public class HomeActivity extends AppCompatActivity {

    View container;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_expense:
                    loadFragment(new ExpenseFragment());
                    return true;
                case R.id.navigation_user:
                    loadFragment(new UserFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        container = findViewById(R.id.frame_container);
        // Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_expense);
        // Load default fragment
        loadFragment(new ExpenseFragment());
    }

    private void loadFragment(Fragment fragment){
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().popBackStack();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }
}
