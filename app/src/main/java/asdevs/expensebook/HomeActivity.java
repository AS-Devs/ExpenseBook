package asdevs.expensebook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import asdevs.expensebook.fragment.ExpenseFragment;
import asdevs.expensebook.fragment.UserFragment;

import static asdevs.expensebook.database.DataBaseClient.DATABASE_NAME;

public class HomeActivity extends AppCompatActivity {

    View container;
    public static final int REQUEST_WRITE_STORAGE = 112;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_expense:
                loadFragment(new ExpenseFragment());
                return true;
            case R.id.navigation_user:
                loadFragment(new UserFragment());
                return true;
        }
        return false;
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

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().popBackStack();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
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
}
