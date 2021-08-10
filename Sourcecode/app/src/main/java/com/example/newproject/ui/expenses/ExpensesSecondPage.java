package com.example.newproject.ui.expenses;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentExpensesSecondPageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import utilty.ApiLogin;


public class ExpensesSecondPage extends Fragment {

    private ExpensesViewModel expensesViewModel;
    private FragmentExpensesSecondPageBinding binding;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference dbRoot = db.getReference().child("Users");

    private DatePickerDialog datepickerdialog;
    private Button dateButton;

    private EditText mName, mAmount;
    private Button button;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String currentUserId;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        expensesViewModel =
                new ViewModelProvider(this).get(ExpensesViewModel.class);

        binding = FragmentExpensesSecondPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
        }

        dbRoot = db.getReference().child("Users").child(currentUserId);

        radioGroup = root.findViewById(R.id.itemRadioGroup);
        mName = root.findViewById(R.id.itemName);
        mAmount = root.findViewById(R.id.itemAmount);
        button = root.findViewById(R.id.saveButton);


        inItDatePicker();
        dateButton = root.findViewById(R.id.datePickerButton);
        dateButton.setText(getTodaysDate());

        //Todo: xml onclick was causing issues, had to define onclick listener
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datepickerdialog.show();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int radioID = radioGroup.getCheckedRadioButtonId();
                radioButton = root.findViewById(radioID);

                if ( !TextUtils.isEmpty(mName.getText().toString())
                        && !TextUtils.isEmpty(mAmount.getText().toString()) )
                {
                    String name = mName.getText().toString();
                    String amount = mAmount.getText().toString();
                    String date = dateButton.getText().toString();
                    String method = radioButton.getText().toString();
                    String id = dbRoot.push().getKey();

                    HashMap<String, String> userMap = new HashMap<>();

                    userMap.put("itemName", name);
                    userMap.put("itemAmount", amount);
                    userMap.put("itemDate", date);
                    userMap.put("itemMethod", method);
                    userMap.put("itemID", id);

                    dbRoot.child(id).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                    openMainActivity();
                }

                else
                    Toast.makeText(getActivity(), "Empty fields not allowed!!", Toast.LENGTH_SHORT).show();
            }
        });





        return root;

    }


    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void inItDatePicker() {

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {

                month = month + 1;
                String date = makeDateString(day, month, year);
                dateButton.setText(date);

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datepickerdialog = new DatePickerDialog(getActivity(), style, dateSetListener, year, month, day);


    }

    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month)
    {
        if (month == 1)
            return "JAN";
        if (month == 2)
            return "FEB";
        if (month == 3)
            return "MAR";
        if (month == 4)
            return "APR";
        if (month == 5)
            return "MAY";
        if (month == 6)
            return "JUN";
        if (month == 7)
            return "JUL";
        if (month == 8)
            return "AUG";
        if (month == 9)
            return "SEP";
        if (month == 10)
            return "OCT";
        if (month == 11)
            return "NOV";
        if (month == 12)
            return "DEC";

        return "JAN";
    }

   /* public void openDataPicker(View view)
    {
        datepickerdialog.show();
    }*/

    //Todo: May be an issue with radio buttons, crashing on click (this was caused by onclick defined in xml file)
    public void checkButton(View view)
    {
        int radioID = radioGroup.getCheckedRadioButtonId();


        radioButton = binding.getRoot().findViewById(radioID);

        Toast.makeText(getActivity(), "" + radioButton.getText(), Toast.LENGTH_SHORT).show();
    }

    //Todo: Errors here, I think the way the fragment
    // navigates or the setup of the fragment is causing a crash
    public void openMainActivity() {
        //Travel to first fragment
        //Todo: This seems to work
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_profile, ExpensesFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack("name") // name can be null
                .commit();

        //Todo: Replacing a fragment (this failed)
        //getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_profile, new ExpensesFragment()).commit();

        //Todo: Using intents for activity change (this will not work for fragments)
        //Intent intent = new Intent (this, MainActivity.class);
        //startActivity(intent);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}//End Fragment







