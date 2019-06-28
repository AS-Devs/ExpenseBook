package asdevs.expensebook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import asdevs.expensebook.adapter.ExpenseAdapter;
import asdevs.expensebook.database.DataBaseClient;
import asdevs.expensebook.model.Expense;

public class MainActivity extends AppCompatActivity {

    private List<Expense> expenses = new ArrayList<>();
    private RecyclerView recyclerView;
    //private ExpenseAdapter mAdapter;
    private int lastDeletedItemPosition;
    private Expense lastDeletedItem;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //mAdapter = new ExpenseAdapter(expenses);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Recycler view swipe gesture
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //awesome code when user grabs recycler card to reorder
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                //awesome code to run when user drops card and completes reorder
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //awesome code when swiping right to remove recycler card and delete SQLite data
                if (direction == ItemTouchHelper.RIGHT) {
                    lastDeletedItemPosition = viewHolder.getAdapterPosition();
                    lastDeletedItem = expenses.get(lastDeletedItemPosition);
                    expenses.remove(lastDeletedItemPosition);
                    recyclerView.setAdapter(new ExpenseAdapter(expenses, MainActivity.this));
                    deleteExpense(lastDeletedItem);
                    //undoSnackBar();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //prepareDummyData();
        getAllExpenses();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialogFragment();
            }
        });
    }

    private void undoSnackBar() {
        view = findViewById(R.id.parentViewGroup);
        Snackbar snackbar = Snackbar.make(view, "1 Item Removed!",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenses.add(lastDeletedItemPosition, lastDeletedItem);
                recyclerView.setAdapter(new ExpenseAdapter(expenses, MainActivity.this));
                reAddExpense(lastDeletedItem);
            }
        });
        snackbar.show();
    }

    private void buildDialogFragment() {

        class DialogFragment extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                AddExpenseFragment dialog = new AddExpenseFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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
                return DataBaseClient.getInstance(getApplicationContext()).getDataBase()
                        .iDataBase()
                        .getAllExpenses();
            }

            @Override
            protected void onPostExecute(List<Expense> exps) {
                super.onPostExecute(exps);
                if (exps == null || exps.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No Data Found!", Toast.LENGTH_LONG).show();
                } else {
                    expenses = exps;
                    recyclerView.setAdapter(new ExpenseAdapter(expenses, MainActivity.this));
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
                DataBaseClient.getInstance(getApplicationContext()).getDataBase()
                        .iDataBase()
                        .deleteExpense(expense);
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

    private void reAddExpense(final Expense expense) {

        class ReAddExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DataBaseClient.getInstance(getApplicationContext()).getDataBase()
                        .iDataBase()
                        .createExpense(expense);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Snackbar undoSnackbar = Snackbar.make(view, "Item Restored",
                        Snackbar.LENGTH_LONG);
                undoSnackbar.show();
            }
        }
        ReAddExpense rae = new ReAddExpense();
        rae.execute();
    }
}
