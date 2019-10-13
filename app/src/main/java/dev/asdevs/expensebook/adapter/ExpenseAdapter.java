package dev.asdevs.expensebook.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dev.asdevs.expensebook.R;
import dev.asdevs.expensebook.fragment.AddExpenseFragment;
import dev.asdevs.expensebook.fragment.ExpenseFragment;
import dev.asdevs.expensebook.model.Expense;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;
    private ExpenseFragment context;

    public ExpenseAdapter(List<Expense> expenses, ExpenseFragment context) {
        this.expenses = expenses;
        this.context = context;
    }

    @NonNull
    @Override
    public ExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.expense_list_row, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Expense expense = expenses.get(i);
        viewHolder.setDetails(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, type, amount, date;
        private View view;
        //private RelativeLayout viewForeground;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            //viewForeground = view.findViewById(R.id.view_foreground);
        }

        void setDetails(final Expense expense) {
            name.setText(expense.getName());
            type.setText("Type: " + expense.getType());
            amount.setText("Rs. " + expense.getAmount());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
            cal.setTime(expense.getDate());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            String strDate = cal.get(Calendar.DAY_OF_MONTH) + " " + simpleDateFormat.format(expense.getDate()) + ", " + cal.get(Calendar.YEAR);
            date.setText(strDate);

            // Attaching Click Listener
            view.setOnClickListener(v -> {
                Bundle b = new Bundle();
                b.putSerializable("expense", expense);
                AddExpenseFragment expenseFragment = new AddExpenseFragment();
                expenseFragment.setArguments(b);
                FragmentTransaction ft = context.getChildFragmentManager().beginTransaction();
                expenseFragment.show(ft, "UpdateExpense");
            });
        }
    }
}
