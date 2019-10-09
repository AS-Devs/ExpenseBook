package dev.asdevs.expensebook.fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import dev.asdevs.expensebook.R;
import dev.asdevs.expensebook.database.DataBaseClient;
import dev.asdevs.expensebook.model.User;

public class AddUserFragment extends DialogFragment {

    public static String TAG = "AddUserDialog";
    private TextInputEditText nameText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.add_user_dialog, container, false);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Create User");

        // Text Input IDs
        nameText = view.findViewById(R.id.nameText);
        Button createUser = view.findViewById(R.id.create);

        createUser.setOnClickListener(this::CreateUser);

        return view;
    }

    private void CreateUser(final View view) {
        if (nameText.getText().toString().trim().length() == 0) {
            Toast.makeText(view.getContext(), "Add a User First!", Toast.LENGTH_LONG).show();

        } else {
            String name = nameText.getText().toString();
            final User user = new User();
            user.setName(name);

            class SaveUser extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids){
                    //adding to database
                    DataBaseClient.getInstance(view.getContext()).getDataBase()
                            .iDataBase()
                            .createUser(user);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Toast.makeText(view.getContext(), "User Successfully Created!", Toast.LENGTH_LONG).show();
                    UserFragment ef = (UserFragment)getParentFragment();
                    ef.getAllUsers();
                    dismiss();
                }
            }
            SaveUser saveExpense = new SaveUser();
            saveExpense.execute();
        }
    }

}
