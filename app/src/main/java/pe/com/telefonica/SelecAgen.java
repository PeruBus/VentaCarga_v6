package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.SharedPreferences;
import android.widget.Spinner;
import android.widget.Toast;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class SelecAgen extends Fragment {
    private Gson gson;

    RequestQueue queue;
    SharedPreferences sharedPreferences;

    Button btn_conanf;
    Spinner sp_caja;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        gson = new Gson();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.selec_agen,parent,false );
    }

    @Override
    public void onViewCreated(View view ,Bundle  savedInstanceState)
    {
        final Button btn_conanf = view.findViewById(R.id.btn_conanf);
        sp_caja = view.findViewById(R.id.sp_caja);
        SelectCaja();


        sp_caja.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                String coAge = item.toString().substring(0,3);
//
//                Toast.makeText(getActivity(),"INGRESE UNA AGENCIA "+coAge,Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_conanf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String CajaSelec = sp_caja.getSelectedItem().toString();
                if (CajaSelec.equals("")) {
                    Toast.makeText(getActivity(),"INGRESE UNA AGENCIA",Toast.LENGTH_LONG).show();
                }
                else if(!CajaSelec.equals("")){
                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                    Toast.makeText(getActivity(),"SELECCIONASTE LA AGENCIA "+CajaSelec,Toast.LENGTH_LONG).show();
                    guardarDataMemoria("CodAgencia",CajaSelec.substring(0,3),getActivity());
                    guardarDataMemoria("DesAgencia",CajaSelec,getActivity());
                    SelecionCajaCarga selecioncajacarga = new SelecionCajaCarga();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, selecioncajacarga).commit();
                }
            }
        });

    }

    void SelectCaja(){
        final RequestQueue queue = Volley.newRequestQueue(getContext());

//        final Spinner_model so = (Spinner_model)spin_serie.getSelectedItem();
        String ws_seriebus = getString(R.string.ws_ruta) + "TRUSUA_AGEN/" + sharedPreferences.getString("CodUsuario", "nada");
        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_seriebus, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>=0)
                        {
                            final ArrayList<String> lista_cajas = new ArrayList<>();
                            JSONObject json;
                            try{
                                for (int i = 0; i < response.length(); i++){
                                    json = response.getJSONObject(i);
                                    lista_cajas.add(json.getString("CO_AGEN"));
                                }
//                                String JSON_SERIE = gson.toJson(lista_series);
//                                guardarDataMemoria("jsonSeries", JSON_SERIE, getApplicationContext());
                            }catch (Exception e)
                            {
                                Log.d("error",e.getMessage());
                            }
                            ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),R.layout.spinner_series_bus,lista_cajas);
                            sp_caja.setAdapter(spinnerArray);
//                            String JSON_SERIES = gson.toJson(lista_series);
//                            guardarDataMemoria("jsonCarga", JSON_SERIES, getContext());

//                            spin_serie.setAdapter((SpinnerAdapter) lista_series);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(RequestBuscaBol1);
    }
}