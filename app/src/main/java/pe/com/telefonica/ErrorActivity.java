package pe.com.telefonica.soyuz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.error);

        final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedpreferences.edit();

        Button btn_cerrar =  findViewById(R.id.btn_cerrar);
        editor.clear();
        editor.commit();
        /* TODO: CLICK LISTENER PARA EL BOTÓN CERRAR */
        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Elimina las variables del sistema y regresa a la pantalla de logueo */
                editor.clear();
                editor.commit();
                Intent intent = new Intent(ErrorActivity.this, LoginActivity.class);
                ErrorActivity.this.finish();
                startActivity(intent);
              //  String Origen =sharedpreferences.getString("ErrorVenta","NoData");
                //if(!Origen.equals("NoData")) {
                  //  Toast toast = Toast.makeText(getApplicationContext(), sharedpreferences.getString("ErrorVenta", "NoData"), Toast.LENGTH_LONG);
                    //toast.setGravity(Gravity.CENTER, 50, 50);
                    //toast.show();
                    //Intent intent = new Intent(ErrorActivity.this, AppSideBarActivity.class);
                  //  ErrorActivity.this.finish();
                   // startActivity(intent);
                //}else{

                //}
                /* ----------------------------------------- */

            }
        });
        /* ----------------------------------------- */


    }



}
