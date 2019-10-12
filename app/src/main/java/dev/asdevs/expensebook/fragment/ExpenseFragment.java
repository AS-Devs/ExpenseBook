package dev.asdevs.expensebook.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.asdevs.expensebook.HomeActivity;
import dev.asdevs.expensebook.R;
import dev.asdevs.expensebook.adapter.ExpenseAdapter;
import dev.asdevs.expensebook.database.DataBaseClient;
import dev.asdevs.expensebook.model.Expense;
import dev.asdevs.expensebook.model.User;

public class ExpenseFragment extends Fragment {

    private List<Expense> expenses = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ExpenseAdapter adapter;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        toolbar.setSubtitle("Manage Expenses");
        setHasOptionsMenu(true);

        emptyView = view.findViewById(R.id.empty_view);
        // Set Up Recycler View
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Recycler view swipe gesture
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                final ColorDrawable background = new ColorDrawable(Color.RED);
                background.setBounds(0, itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                background.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    lastDeletedItemPosition = viewHolder.getAdapterPosition();
                    lastDeletedItem = expenses.get(lastDeletedItemPosition);
                    expenses.remove(lastDeletedItemPosition);
                    adapter.notifyDataSetChanged();
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
        fab.setOnClickListener(view -> buildDialogFragment());
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
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    //Toast.makeText(view.getContext(), "No Data Found!", Toast.LENGTH_LONG).show();
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    expenses.clear();
                    expenses.addAll(exps);
                    adapter = new ExpenseAdapter(expenses, ExpenseFragment.this);
                    recyclerView.setAdapter(adapter);
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
                if (expenses == null || expenses.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
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
        snackbar.setAction("UNDO", v -> {
            expenses.add(lastDeletedItemPosition, lastDeletedItem);
            adapter.notifyDataSetChanged();
            reAddExpense(lastDeletedItem);
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
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
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
