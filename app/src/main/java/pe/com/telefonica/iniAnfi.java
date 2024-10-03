package pe.com.telefonica.soyuz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class iniAnfi extends AppCompatActivity {

    Button btn_vollog;
    Button btn_conanf;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ini_anfi);

        btn_vollog = findViewById(R.id.btn_vollog);
        btn_conanf = findViewById(R.id.btn_conanf);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        TextView txtCONDUCTOR = findViewById(R.id.txtCONDUCTOR);
        TextView txtCODIGO_VEHICULO = findViewById(R.id.txtCODIGO_VEHICULO);
        TextView txtFE_PROG = findViewById(R.id.txtFE_PROG);
        TextView txtHO_SALI = findViewById(R.id.txtHO_SALI);
        TextView txtDE_ORIG = findViewById(R.id.txtDE_ORIG);
        TextView txtDE_FINA = findViewById(R.id.txtDE_FINA);
        TextView txtANFITRION = findViewById(R.id.txtANFITRION);

        txtANFITRION.setText(sharedPreferences.getString("anf_nombre", ""));
        txtCONDUCTOR.setText(sharedPreferences.getString("con_nombre", ""));
        txtCODIGO_VEHICULO.setText(sharedPreferences.getString("anf_codigoVehiculo", ""));
        txtFE_PROG.setText(sharedPreferences.getString("pro_fech", ""));
        txtHO_SALI.setText(sharedPreferences.getString("pro_hora", ""));
        txtDE_ORIG.setText(sharedPreferences.getString("pro_orig", ""));
        txtDE_FINA.setText(sharedPreferences.getString("pro_fina", ""));

        btn_vollog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ventaBlt = new DatabaseBoletos(getApplicationContext());
                sqLiteDatabase = ventaBlt.getWritableDatabase();
                sqLiteDatabase.execSQL("DELETE FROM Manifiesto");
                stopBoletoService();
                logout();
                Intent intent = new Intent(iniAnfi.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btn_conanf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(iniAnfi.this, AppSideBarActivity.class);
                iniAnfi.this.finish();
                startActivity(intent);
            }
        });


    }

    public void stopBoletoService() {
        Log.d("ServicioStop","si");
        BoletoService.startService(getApplicationContext(), false);
    }

    public  void logout(){
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
}