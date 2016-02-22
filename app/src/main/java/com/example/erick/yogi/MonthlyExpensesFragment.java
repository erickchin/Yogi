package com.example.erick.yogi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MonthlyExpensesFragment extends Fragment {

    MonthlyExpensesDH data;
    private ArrayAdapter<String> monthlyExpensesAdapter;
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> expenseList = new ArrayList<String>();
    String value;
    int positionRow;


    public MonthlyExpensesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        data = new MonthlyExpensesDH(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monthly_expenses, container, false);

        monthlyExpensesAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_monthly, // The name of the layout ID.
                        R.id.list_item_monthly_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        // Setup FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddMonthlyExpenseActivity.class);
                startActivity(intent);
            }
        });

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_expenses);
        listView.setAdapter(monthlyExpensesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                positionRow = position;

                LayoutInflater li = LayoutInflater.from(getContext());
                View promptsView = li.inflate(R.layout.edit_expenses, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextNumber);

                // set dialog message
                alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                value = userInput.getText().toString();
                                data.changeValue(nameList.get(positionRow), value);
                                getMonthlyExpenses();
                            }
                        })
                 .setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                data.deleteData(nameList.get(position), expenseList.get(position));
                Toast.makeText(getContext(), nameList.get(position) + " evaluation deleted. ", Toast.LENGTH_LONG).show();
                getMonthlyExpenses();
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMonthlyExpenses();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getMonthlyExpenses();
    }

    // TODO Get monthly expenses
    private void getMonthlyExpenses() {
        // Reset all the lists
        monthlyExpensesAdapter.clear();
        nameList.clear();
        expenseList.clear();
        SQLiteDatabase db = data.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + data.COL_2 + ", " + data.COL_3 + " FROM " + data.TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(data.COL_2));
                    String expense = cursor.getString(cursor.getColumnIndex(data.COL_3));
                    nameList.add(name);
                    expenseList.add(expense);
                    monthlyExpensesAdapter.add(name + "\r\nMonthly: " + expense);
                } while (cursor.moveToNext());
            }
        }

        double totalExpense = 0;
        TextView text = (TextView) getActivity().findViewById(R.id.textview_summary);

        for (int i = 0; i < expenseList.size(); i++) {
            totalExpense += Double.parseDouble(expenseList.get(i));
        }
        if (totalExpense > 0) {

            text.setText("Monthly Expenses " + Double.toString(totalExpense));
        }
        else {
            text.setText("Enter monthly expenses");
        }
        cursor.close();
    }
}