package dev.asdevs.expensebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dev.asdevs.expensebook.fragment.ExpenseFragment;
import dev.asdevs.expensebook.fragment.UserFragment;

import static dev.asdevs.expensebook.database.DataBaseClient.DATABASE_NAME;

public class HomeActivity extends AppCompatActivity {

    View container;
    FragmentManager fragmentManager;
    ExpenseFragment expenseFragment;
    UserFragment userFragment;
    public static final int REQUEST_WRITE_STORAGE = 112;
    public static final int REQUEST_READ_STORAGE = 113;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_expense:
                loadFragment(expenseFragment, "Expense");
                //changeFragment(0);
                return true;
            case R.id.navigation_user:
                //changeFragment(1);
                loadFragment(userFragment, "User");
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        container = findViewById(R.id.frame_container);
        // Creating Fragments
        expenseFragment = new ExpenseFragment();
        userFragment = new UserFragment();
        // Get Fragment Manager
        fragmentManager = getSupportFragmentManager();
        // Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_expense);
        // Load default fragment
        //loadFragment(new ExpenseFragment(), "Expense");
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment visibleFragment = fragmentManager.getPrimaryNavigationFragment();
        if (visibleFragment != null) {
            transaction.detach(visibleFragment);
        }
        Fragment frag = fragmentManager.findFragmentByTag(tag);
        if (frag == null) {
            transaction.add(R.id.frame_container, fragment, tag);
        } else {
            //fragmentManager.popBackStack();
            transaction.attach(fragment);
        }
        transaction.setPrimaryNavigationFragment(fragment);
        transaction.setReorderingAllowed(true);
        transaction.commitNowAllowingStateLoss();
    }

    public void exportDatabaseToStorage() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            // You are allowed to write external storage:
            //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/new_folder";
            //File storageDir = new File(path);
            //if (!storageDir.exists() && !storageDir.mkdirs()) {
            // This should never happen - log handled exception!
            //}
            if (exportDb()) {
                Toast.makeText(this, "Export Successful", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void importDatabaseFromStorage() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        } else {
            if (importDb()) {
                reStartActivity();
                Toast.makeText(this, "Import Successful", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Something went wrong! Please make sure that your backup is in your Internal Storage!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (exportDb()) {
                        Toast.makeText(this, "Export Successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot export data to storage. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (importDb()) {
                        reStartActivity();
                        Toast.makeText(this, "Import Successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot import data to storage. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean exportDb() {
        try {
            File dbFile = new File(this.getDatabasePath(DATABASE_NAME).getAbsolutePath());
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATABASE_NAME + ".db";

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            //FileOutputStream output = openFileOutput(DATABASE_NAME + ".db", Context.MODE_PRIVATE);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            return true;
        } catch (IOException ex) {
            Log.e("Export_DB: ", ex.getMessage());
            return false;
        }
    }

    private boolean importDb() {
        try {
            // Get the Database backup file from internal storage
            String dbBackupFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATABASE_NAME + ".db";
            File dbBackupFile = new File(dbBackupFilePath);
            FileInputStream fis = new FileInputStream(dbBackupFile);

            // Open the empty db as the output stream
            FileOutputStream fos = new FileOutputStream(this.getDatabasePath(DATABASE_NAME).getAbsolutePath());

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            // Close the streams
            fos.flush();
            fos.close();
            fis.close();

            return true;
        } catch (IOException ex) {
            Log.e("Import_DB_IO: ", ex.getMessage());
            return false;
        } catch (SQLiteException ex) {
            Log.e("Import_DB_SQLITE: ", ex.getMessage());
            return false;
        }
    }

    private void reStartActivity() {
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        startActivity(intent); // start same activity
        finish(); // destroy older activity
        overridePendingTransition(0, 0);
    }

}
