package asdevs.expensebook.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import asdevs.expensebook.R;
import asdevs.expensebook.database.DataBaseClient;
import asdevs.expensebook.model.Expense;

public class AddExpenseFragment extends DialogFragment {

    public static String TAG = "FullScreenDialog";
    private TextInputEditText nameText, dateText, amountText, typeText;
    private TextInputLayout dateLayout;
    private Button createExpense;
    private Expense exp;
    Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        if(getArguments() != null) {
            exp = (Expense) getArguments().getSerializable("expense");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.add_expense_dialog, container, false);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Text Input IDs
        nameText = view.findViewById(R.id.nameText);
        dateText = view.findViewById(R.id.dateText);
        dateLayout = view.findViewById(R.id.dateLayout);
        amountText = view.findViewById(R.id.amountText);
        typeText = view.findViewById(R.id.typeText);
        createExpense = view.findViewById(R.id.create);

        // Check if create or update
        if(exp == null) {
            toolbar.setTitle("Create Expense");
        } else{
            toolbar.setTitle("Update Expense");
            nameText.setText(exp.getName());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            dateText.setText(sdf.format(exp.getDate()));
            amountText.setText(Double.toString(exp.getAmount()));
            typeText.setText(exp.getType());
            createExpense.setText("Update Expense");
        }

        // Default date set
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        dateText.setText(day + "-" + month + "-" + calendar.get(Calendar.YEAR));

        // Date Picker
        final DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String month = String.format("%02d", monthOfYear + 1);
                String day = String.format("%02d", dayOfMonth);
                dateText.setText(day + "-" + month + "-" + year);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        createExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exp == null) {
                    CreateExpense(v);
                }
                else{
                    UpdateExpense(v);
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    private void CreateExpense(final View view){
        if(nameText.getText() == null || dateText.getText() == null || amountText.getText() == null || typeText.getText() == null){
            Toast.makeText(view.getContext(), "Fill in the form Correctly!", Toast.LENGTH_LONG).show();
        }
        else{
            String name = nameText.getText().toString();
            Double amount = Double.parseDouble(amountText.getText().toString());
            String type = typeText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            Date date = null;
            try {
                date = sdf.parse(dateText.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final Expense expense = new Expense(name, amount, type, date);

            class SaveExpense extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids){
                    //adding to database
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .createExpense(expense);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Toast.makeText(view.getContext(), "Expense Successfully Created!", Toast.LENGTH_LONG).show();
                    ExpenseFragment ef = (ExpenseFragment)getParentFragment();
                    ef.getAllExpenses();
                    dismiss();
                }
            }

            SaveExpense saveExpense = new SaveExpense();
            saveExpense.execute();
        }
    }

    private void UpdateExpense(final View view){
        if(nameText.getText() == null || dateText.getText() == null || amountText.getText() == null || typeText.getText() == null){
            Toast.makeText(view.getContext(), "Fill in the form Correctly!", Toast.LENGTH_LONG).show();
        }
        else{
            String name = nameText.getText().toString();
            Double amount = Double.parseDouble(amountText.getText().toString());
            String type = typeText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            Date date = null;
            try {
                date = sdf.parse(dateText.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final Expense expense = new Expense(name, amount, type, date);
            expense.setId(exp.getId());

            class UpdateExpense extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids){
                    //adding to database
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .updateExpense(expense);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Toast.makeText(view.getContext(), "Expense Successfully Updated!", Toast.LENGTH_LONG).show();
                    ExpenseFragment ef = (ExpenseFragment)getParentFragment();
                    ef.getAllExpenses();
                    dismiss();
                }
            }

            UpdateExpense updateExpense = new UpdateExpense();
            updateExpense.execute();
        }
    }
}
