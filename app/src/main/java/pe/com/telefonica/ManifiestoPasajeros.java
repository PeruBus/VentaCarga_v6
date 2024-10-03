package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;

public class ManifiestoPasajeros extends Fragment {
    private SharedPreferences sharedPreferences;
    final ArrayList<String> lista_Manifiesto = new ArrayList<>();
    private ListView listView;
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseBoletos ventaBlt;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.manifiesto_clientes, parent, false);
    }
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        //ventaBlt = new DatabaseBoletos(getActivity());
        //sqLiteDatabase = ventaBlt.getWritableDatabase();
        CargaData();
        listView = view.findViewById(R.id.listView_Manifiesto);
        TablaManifiestoAdapter adapterBoletosLeidos = new TablaManifiestoAdapter(lista_Manifiesto, getActivity());
        listView.setAdapter(adapterBoletosLeidos);
    }
    void CargaData()
    {
        try {
            //ventaBlt = new DatabaseBoletos(getActivity());
            //sqLiteDatabase = ventaBlt.getWritableDatabase();
            final Cursor cursor = sqLiteDatabase.rawQuery("SELECT NO_CLIE,NU_DOCU,CO_DEST_ORIG,CO_DEST_FINA,DOCU_IDEN FROM Manifiesto",new String[]{});
            guardarDataMemoria("Cant_BoletosAsignados", Integer.toString(cursor.getCount()), getContext());
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()){
                    lista_Manifiesto.add(cursor.getString(cursor.getColumnIndex("NO_CLIE"))+"."
                            +cursor.getString(cursor.getColumnIndex("NU_DOCU"))+"."
                            +cursor.getString(cursor.getColumnIndex("CO_DEST_ORIG")).trim()+"."
                            +cursor.getString(cursor.getColumnIndex("CO_DEST_FINA")).trim()+"."
                            +cursor.getString(cursor.getColumnIndex("DOCU_IDEN")));
                }
            }

        }catch (Exception ex)
        {
            Toast.makeText(getContext(),ex.getMessage(),Toast.LENGTH_LONG);
        }

    }
}
