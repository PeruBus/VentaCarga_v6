package pe.com.telefonica.soyuz;

import android.content.ContentValues;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;


public class InspeccionVentaFragment extends Fragment {


    private DatabaseBoletos ventaBlt;
    static Spinner spinner = null;

    private SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inspeccion_venta, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Inicialización de la base de datos */
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        /* ----------------------------------------- */

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final Gson gson = new Gson();

        final Context contextInspeccionVenta = view.getContext();



        //final Spinner spinner_tramoInicio = view.findViewById(R.id.spinner_tramoInicio);
        //final Spinner spinner_tramoFin = view.findViewById(R.id.spinner_tramoFin);

       /* final TextView textView_jefe = view.findViewById(R.id.textView_jefeArea);
        final TextView textView_inspector = view.findViewById(R.id.textView_inspector);
        final TextView textView_conductor = view.findViewById(R.id.textView_conductor);
        final TextView textView_bus = view.findViewById(R.id.textView_bus);
        final TextView textView_fechaInspeccion = view.findViewById(R.id.textView_fechaInspeccion);
        final TextView textView_anfitrion = view.findViewById(R.id.textView_anfitrion);*/

        final Button btn_buscarBoleto = view.findViewById(R.id.btn_buscarBoleto);
        final Button btn_leerQR = view.findViewById(R.id.btn_leerQR);
        final Button btn_cerrarInspeccion = view.findViewById(R.id.btn_cerrarInspeccion);

        /* Se muestra la información del bus que se está inspeccionando en la pantalla */
       /* textView_jefe.setText("Jefe del área legal");
        textView_inspector.setText(sharedPreferences.getString("codigoUsuario", "NoData"));
        textView_conductor.setText(sharedPreferences.getString("insp_codigoConductor", "NoData"));
        textView_bus.setText(sharedPreferences.getString("insp_codigoVehiculo", "NoData"));
        textView_anfitrion.setText(sharedPreferences.getString("insp_codigoAnfitrion", "NoData"));*/

        //String fecha = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String fecha = sharedPreferences.getString("fechaInspeccion","NoData");
       /* textView_fechaInspeccion.setText(fecha);
        /* ----------------------------------------- */

        /* Arreglo */
        // lista_asientosVendidos: arreglo que coniene los asientos vendidos
        /*final ArrayList<String> lista_asientosVendidos = getArray(sharedPreferences, gson, "insp_jsonReporteVenta");
        ListView listView = view.findViewById(R.id.listView_inspecciones);
        TablaAdapter adapterInspecciones = new TablaAdapter(lista_asientosVendidos, getActivity());
        listView.setAdapter(adapterInspecciones);*/
        /* ----------------------------------------- */

        /* Arreglos */
        // lista_destinos: arreglo que contiene todos los destinos
        // lista_tramoInspeccion: arreglo que contiene todos los tramos de inspección
        // lista_idTramos: arreglo que contiene los IDs de cada tramo
        // lista_nombreTramos: arreglo que contiene los nombres de cada tramo
        final ArrayList<String> lista_destinos = getArray(sharedPreferences,gson,"json_destinos");
        final ArrayList<String> ListaZona= getArray(sharedPreferences,gson,"jsonZona");
       // final ArrayList<String> lista_tramoInspeccion = getArray(sharedPreferences,gson,"json_tramoInspeccion");
        //final ArrayList<String> lista_idTramos = new ArrayList<>();
        //final ArrayList<String> lista_nombreTramos = new ArrayList<>();
        /* ----------------------------------------- */

        /* Se itera en función de los tramos de inspección y se agrega data a los arreglos */
        /*for (int i = 0; i < lista_tramoInspeccion.size(); i++){

            String[] dataTramoInspeccion = lista_tramoInspeccion.get(i).split("-");
            // dataTramoInspeccion[0] = CO_TRAM
            // dataTramoInspeccion[1] = DE_TRAM
            // dataTramoInspeccion[2] = CO_RUMB

            if (sharedPreferences.getString("insp_rumbo", "NoData").equals(dataTramoInspeccion[2])) {
                lista_idTramos.add(dataTramoInspeccion[0]);
                lista_nombreTramos.add(dataTramoInspeccion[1]);
            }
        }*/
        /* ----------------------------------------- */

        /* Se agrega la lista de nombre de tramos a los spinner de tramo de inicio y fin */
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(contextInspeccionVenta, android.R.layout.simple_spinner_item, lista_nombreTramos);
        spinner_tramoInicio.setAdapter(adapter);
        spinner_tramoFin.setAdapter(adapter);
        /* ----------------------------------------- */
        Log.d("zona",ListaZona.toString());
        spinner = (Spinner) view.findViewById(R.id.spinner_tramoInicio);
        final List<Spinner_model> model = new ArrayList<>();
        for(int i =0;i< ListaZona.size();i++)
        {
            String[] arr = ListaZona.get(i).split("/");
            Spinner_model Spinner_zona = new Spinner_model(arr[0], arr[1], arr[2]);
            model.add(Spinner_zona);
        }
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, model);
        spinner.setAdapter(spinnerArrayAdapter);
        btn_buscarBoleto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Cambia a la vista de buscar boleto */
                BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, buscarBoletoFragment).commit();
                /* ----------------------------------------- */
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Spinner_model st = (Spinner_model)spinner.getSelectedItem();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btn_leerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Cambia a la vista de buscar boleto */
                mataBoleto mataBoleto_Insp  = new mataBoleto();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, mataBoleto_Insp).commit();
                /* ----------------------------------------- */
            }
        });


    }


    public void startReporteInspeccionService() {
        BoletoService.startService(getActivity(), true);
    }


    public void stopReporteInspeccionService() {
        BoletoService.startService(getActivity(), false);
    }


    private void errorWS(RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        }else if (error instanceof TimeoutError) {
            Toast.makeText(getActivity(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getActivity(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getActivity(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        }else if (error instanceof ParseError) {
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        Intent intent = new Intent(getActivity(), ErrorActivity.class);
        startActivity(intent);
    }

}