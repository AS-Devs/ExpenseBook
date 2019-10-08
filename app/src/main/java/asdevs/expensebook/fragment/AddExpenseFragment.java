package asdevs.expensebook.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import asdevs.expensebook.R;
import asdevs.expensebook.database.DataBaseClient;
import asdevs.expensebook.model.Expense;
import asdevs.expensebook.model.User;

public class AddExpenseFragment extends DialogFragment {

    public static String TAG = "AddExpenseDialog";
    private TextInputEditText nameText, dateText, amountText, typeText;
    private TextInputLayout nameLayout, dateLayout;
    private Expense exp;
    Calendar calendar = Calendar.getInstance();
    private View view;
    private List<User> users = new ArrayList<>();
    private User user = new User();
    private String[] userNames;
    private AlertDialog.Builder userDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        if(getArguments() != null) {
            exp = (Expense) getArguments().getSerializable("expense");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.add_expense_dialog, container, false);

        // Get All Users
        getAllUsers();

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        // Text Input IDs
        nameText = view.findViewById(R.id.nameText);
        dateText = view.findViewById(R.id.dateText);
        nameLayout = view.findViewById(R.id.nameLayout);
        dateLayout = view.findViewById(R.id.dateLayout);
        amountText = view.findViewById(R.id.amountText);
        typeText = view.findViewById(R.id.typeText);
        Button createExpense = view.findViewById(R.id.create);

        nameLayout.setEnabled(false);
        nameLayout.setError("No Users Found");

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

        dateLayout.setOnClickListener(view -> datePickerDialog.show());
        dateText.setOnClickListener(view -> datePickerDialog.show());

        nameLayout.setOnClickListener(v -> {
            AlertDialog mDialog = userDialog.create();
            mDialog.show();
        });

        nameText.setOnClickListener(v -> {
            AlertDialog mDialog = userDialog.create();
            mDialog.show();
        });

        createExpense.setOnClickListener(v -> {
            if(exp == null) {
                CreateExpense(v);
            }
            else{
                UpdateExpense(v);
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
        if(nameText.getText().toString().trim().length() == 0 || dateText.getText().toString().trim().length() == 0 || amountText.getText().toString().trim().length() == 0 || typeText.getText().toString().trim().length() == 0){
            Toast.makeText(view.getContext(), "Add The Expense Correctly!", Toast.LENGTH_LONG).show();
        }
        else{
            String name = nameText.getText().toString();
            double amount = Double.parseDouble(amountText.getText().toString());
            String type = typeText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            Date date = null;
            try {
                date = sdf.parse(dateText.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final Expense expense = new Expense(name, amount, type, date);
            for(User usr : users){
                if(usr.getName().equals(name)){
                    usr.setAmount(usr.getAmount() + amount);
                    user = usr;
                    break;
                }
            }

            class SaveExpense extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids){
                    //adding to database
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .createExpense(expense);
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .updateUser(user);
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
            double amount = Double.parseDouble(amountText.getText().toString());
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
            for(User usr : users){
                if(usr.getName().equals(name)){
                    usr.setAmount(usr.getAmount() + amount - exp.getAmount());
                    user = usr;
                    break;
                }
            }

            class UpdateExpense extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids){
                    //adding to database
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .updateExpense(expense);
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .updateUser(user);
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

    private void createUserDialog(){
        nameLayout.setEnabled(true);
        nameLayout.setError(null);
        userDialog = new AlertDialog.Builder(view.getContext());
        userDialog.setTitle("Select an User");
        userDialog.setSingleChoiceItems(userNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nameText.setText(userNames[i]);
                dialogInterface.dismiss();
            }
        });
        userDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
    }

    public void getAllUsers() {

        class GetAllUsers extends AsyncTask<Void, Void, List<User>>{

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
                    nameLayout.setError("No Users Found");
                } else {
                    users = usrs;
                    userNames = new String[users.size()];
                    for(int i = 0; i<users.size(); i++){
                        userNames[i] = users.get(i).getName();
                    }
                    createUserDialog();
                }
            }
        }

        GetAllUsers gau = new GetAllUsers();
        gau.execute();
    }
}
