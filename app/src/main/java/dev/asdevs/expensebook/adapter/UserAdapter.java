package dev.asdevs.expensebook.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dev.asdevs.expensebook.R;
import dev.asdevs.expensebook.fragment.UserFragment;
import dev.asdevs.expensebook.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private UserFragment context;

    public UserAdapter(List<User> users, UserFragment context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_list_row, viewGroup, false);
        return new UserAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        User user = users.get(i);
        viewHolder.setDetails(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name, amount;
        private View view;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
        }

        void setDetails(final User user) {
            name.setText(user.getName());
            amount.setText("Rs. " + user.getAmount());

            // Click handler
            view.setOnClickListener(v -> {
                AlertDialog alert = new AlertDialog.Builder(v.getContext())
                        .setTitle("Reset or Delete the User!")
                        .setMessage("Either Reset the User details or Delete the User Altogether.")
                        .setNeutralButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                context.resetUser(user);
                            }
                        })
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                context.deleteUser(user);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.ic_user)
                        .show();
            });
        }
    }
}
