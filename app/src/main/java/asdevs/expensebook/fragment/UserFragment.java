package asdevs.expensebook.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import asdevs.expensebook.HomeActivity;
import asdevs.expensebook.R;
import asdevs.expensebook.adapter.UserAdapter;
import asdevs.expensebook.database.DataBaseClient;
import asdevs.expensebook.model.User;

public class UserFragment extends Fragment {

    private View view;
    private List<User> users = new ArrayList<>();
    private RecyclerView recyclerView;
    private int lastDeletedItemPosition;

    public UserFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSubtitle("Manage Users");
        setHasOptionsMenu(true);

        // Set Up Recycler View
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getAllUsers();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialogFragment();
            }
        });
        return view;
    }

    private void buildDialogFragment() {

        class DialogFragment extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                AddUserFragment dialog = new AddUserFragment();
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                dialog.show(ft, AddUserFragment.TAG);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }
        DialogFragment df = new DialogFragment();
        df.execute();
    }

    public void getAllUsers() {

        class GetAllUsers extends AsyncTask<Void, Void, List<User>> {

            @Override
            protected List<User> doInBackground(Void... voids) {
                return DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .getAllUsers();
            }

            @Override
            protected void onPostExecute(List<User> usrs) {
                super.onPostExecute(usrs);
                if (usrs == null || usrs.size() == 0) {
                    Toast.makeText(view.getContext(), "No Data Found!", Toast.LENGTH_LONG).show();
                } else {
                    users = usrs;
                    recyclerView.setAdapter(new UserAdapter(users, UserFragment.this));
                }
            }
        }

        GetAllUsers gau = new GetAllUsers();
        gau.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_items, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                Toast.makeText(view.getContext(), "Import", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_export:
                try {
                    ((HomeActivity) Objects.requireNonNull(getActivity())).exportDatabaseToStorage();
                } catch (NullPointerException ex) {
                    Log.e("Export DB: ", ex.getMessage());
                    Toast.makeText(view.getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
