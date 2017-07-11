package martin.noaahurricanetracks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Spinner element
        final Spinner basinSpinner = (Spinner) findViewById(R.id.basinSpinner);
        // Spinner click listener
        basinSpinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> basinList = new ArrayList<String>();
        basinList.add("North Atlantic");
        basinList.add("South Atlantic");
        basinList.add("East Pacific");
        basinList.add("West Pacific");
        basinList.add("South Pacific");
        basinList.add("North Indian");
        basinList.add("South Indian");
        // reating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, basinList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        basinSpinner.setAdapter(dataAdapter);

        //season selection
        final EditText seasonText = (EditText) findViewById(R.id.seasonEditText);

        //search button
        Button searchButton = (Button) findViewById(R.id.searchButton);
        // Capture button clicks
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                myIntent.putExtra("basin", basinSpinner.getSelectedItem().toString());
                String integerRegex = "([0-9]{0,9})";
                if (seasonText.getText().toString().isEmpty() || !Pattern.matches(integerRegex, seasonText.getText().toString())) {
                    Toast.makeText(arg0.getContext(), "Please enter valid number between 1848-2015", Toast.LENGTH_LONG).show();
                }
                else{
                    int season = Integer.parseInt(seasonText.getText().toString());
                    if(season >= 1848 && season <= 2015) {
                        myIntent.putExtra("season", season);
                        startActivity(myIntent);
                    }
                    else{
                        Toast.makeText(arg0.getContext(), "Please enter valid number between 1848-2015", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
