package asdevs.expensebook.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import asdevs.expensebook.adapter.ExpenseAdapter;
import asdevs.expensebook.database.DataBaseClient;
import asdevs.expensebook.model.Expense;
import asdevs.expensebook.model.User;

public class ExpenseFragment extends Fragment {

    private List<Expense> expenses = new ArrayList<>();
    private RecyclerView recyclerView;
    private int lastDeletedItemPosition;
    private Expense lastDeletedItem;
    private View view;
    private List<User> users;
    private User deletedExpenseUser;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSubtitle("Manage Expenses");
        setHasOptionsMenu(true);

        // Set Up Recycler View
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Recycler view swipe gesture
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //awesome code when swiping right to remove recycler card and delete SQLite data
                if (direction == ItemTouchHelper.RIGHT) {
                    lastDeletedItemPosition = viewHolder.getAdapterPosition();
                    lastDeletedItem = expenses.get(lastDeletedItemPosition);
                    expenses.remove(lastDeletedItemPosition);
                    recyclerView.setAdapter(new ExpenseAdapter(expenses, ExpenseFragment.this));
                    deleteExpense(lastDeletedItem);
                    undoSnackBar();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        getAllExpenses();
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
                AddExpenseFragment dialog = new AddExpenseFragment();
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                dialog.show(ft, AddExpenseFragment.TAG);
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

    public void getAllExpenses() {

        class GetAllExpense extends AsyncTask<Void, Void, List<Expense>> {

            @Override
            protected List<Expense> doInBackground(Void... voids) {
                //getting from database
                return DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .getAllExpenses();
            }

            @Override
            protected void onPostExecute(List<Expense> exps) {
                super.onPostExecute(exps);
                if (exps == null || exps.size() == 0) {
                    Toast.makeText(view.getContext(), "No Data Found!", Toast.LENGTH_LONG).show();
                } else {
                    expenses = exps;
                    recyclerView.setAdapter(new ExpenseAdapter(expenses, ExpenseFragment.this));
                }
            }
        }

        GetAllExpense gu = new GetAllExpense();
        gu.execute();
    }

    public void deleteExpense(final Expense expense) {

        class DeleteExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .deleteExpense(expense);

                deletedExpenseUser = new User();
                for (User u : users) {
                    if (expense.getName().equals(u.getName())) {
                        deletedExpenseUser = u;
                        u.setAmount(u.getAmount() - expense.getAmount());
                        DataBaseClient.getInstance(view.getContext()).getDataBase()
                                .iDataBase()
                                .updateUser(u);
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                undoSnackBar();
            }
        }
        DeleteExpense de = new DeleteExpense();
        de.execute();
    }

    private void undoSnackBar() {
        //view = view.findViewById(R.id.parentViewGroup);
        Snackbar snackbar = Snackbar.make(view, "1 Item Removed!",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenses.add(lastDeletedItemPosition, lastDeletedItem);
                recyclerView.setAdapter(new ExpenseAdapter(expenses, ExpenseFragment.this));
                reAddExpense(lastDeletedItem);
            }
        });
        snackbar.show();
    }

    private void reAddExpense(final Expense expense) {

        class ReAddExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .createExpense(expense);
                deletedExpenseUser.setAmount(deletedExpenseUser.getAmount() + expense.getAmount());
                DataBaseClient.getInstance(view.getContext()).getDataBase()
                        .iDataBase()
                        .updateUser(deletedExpenseUser);
                deletedExpenseUser = null;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Snackbar undoSnackBar = Snackbar.make(view, "Item Restored",
                        Snackbar.LENGTH_LONG);
                undoSnackBar.show();
            }
        }
        ReAddExpense rae = new ReAddExpense();
        rae.execute();
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
                users = new ArrayList<>();
                users = usrs;
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
