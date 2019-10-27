package dev.asdevs.expensebook.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.asdevs.expensebook.HomeActivity;
import dev.asdevs.expensebook.R;
import dev.asdevs.expensebook.adapter.UserAdapter;
import dev.asdevs.expensebook.database.DataBaseClient;
import dev.asdevs.expensebook.model.Expense;
import dev.asdevs.expensebook.model.User;

public class UserFragment extends Fragment {

    private View view;
    private List<User> users = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyView;
    private UserAdapter adapter;
    //private int lastDeletedItemPosition;

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
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        toolbar.setSubtitle("Manage Users");
        setHasOptionsMenu(true);

        emptyView = view.findViewById(R.id.empty_view);
        // Set Up Recycler View
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getAllUsers();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view -> buildDialogFragment());
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
                    //Toast.makeText(view.getContext(), "No Data Found!", Toast.LENGTH_LONG).show();
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    users = usrs;
                    adapter = new UserAdapter(users, UserFragment.this);
                    recyclerView.setAdapter(adapter);
                }
            }
        }

        GetAllUsers gau = new GetAllUsers();
        gau.execute();
    }

    public void resetUser(final User user) {

        class ResetUserExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                List<Expense> expenses = DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .getAllExpenses();
                for (Expense ex : expenses) {
                    if (user.getName().equals(ex.getName())) {
                        DataBaseClient.getInstance(view.getContext()).getDataBase()
                                .iDataBase()
                                .deleteExpense(ex);
                    }
                }
                user.setAmount(0);
                DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .updateUser(user);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getContext(), "User Reset Successful", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }
        }

        ResetUserExpense rue = new ResetUserExpense();
        rue.execute();
    }

    public void deleteUser(final User user) {

        class DeleteUser extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                List<Expense> expenses = DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .getAllExpenses();
                for (Expense ex : expenses) {
                    if (user.getName().equals(ex.getName())) {
                        DataBaseClient.getInstance(view.getContext()).getDataBase()
                                .iDataBase()
                                .deleteExpense(ex);
                    }
                }
                DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .deleteUser(user);
                users.remove(user);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(users == null || users.size() == 0){
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else{
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(view.getContext(), "User Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        }

        DeleteUser du = new DeleteUser();
        du.execute();
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
                try {
                    ((HomeActivity) Objects.requireNonNull(getActivity())).importDatabaseFromStorage();
                } catch (NullPointerException ex) {
                    Log.e("Import DB: ", ex.getMessage());
                    Toast.makeText(view.getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
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
