package pe.com.telefonica.soyuz;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class BuscarBoleto extends Fragment {
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private String idEmpresa="";
    private ListView listView;
    private Boolean existe = false;
    ProgressDialog progressDialog;
    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup parent,Bundle savedInstanceState) {
        gson = new Gson();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.buscar_boleto_controlador,parent,false );
    }
    @Override
    public void onViewCreated(View view ,Bundle  savedInstanceState)
    {
        final Context  context_BuscarBoleto  = view.getContext();
        final Spinner spinner_empresa = view.findViewById(R.id.spinner_empresa);
        final EditText txtserie = view.findViewById(R.id.txtSerie);
        final EditText txtCorrelativo = view.findViewById(R.id.txtCorrelativo);
        final Button btnBuscar = view.findViewById(R.id.btnBuscar);
        final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
        final ArrayList<String> lista_codEmpresa = new ArrayList<>();
        final ArrayList<String> lista_nombreEmpresa = new ArrayList<>();
        AgregaBoletos();
        for (int i = 0; i < lista_empresas.size(); i++) {
            String[] dataEmpresa = lista_empresas.get(i).split("-");
            lista_codEmpresa.add(dataEmpresa[0]);
            lista_nombreEmpresa.add(dataEmpresa[1]);
        }
        ArrayAdapter adapter = new ArrayAdapter<>(context_BuscarBoleto, android.R.layout.simple_spinner_item, lista_nombreEmpresa);
        spinner_empresa.setAdapter(adapter);
        spinner_empresa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                idEmpresa = lista_codEmpresa.get(position);
                guardarDataMemoria("guardar_idEmpresa", idEmpresa, getActivity());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                btnBuscar.setEnabled(false);
                if (txtserie.getText().toString().length()<0 ){
                    Toast.makeText(getActivity(),"INGRESE UNA SERIE",Toast.LENGTH_LONG).show();
                    btnBuscar.setEnabled(true);
                    return;
                }else if(txtCorrelativo.getText().toString().length()<0){
                    Toast.makeText(getActivity(),"INGRESE CORRELATIVO",Toast.LENGTH_LONG).show();
                    btnBuscar.setEnabled(true);
                    return;
                }
                final RequestQueue queue = Volley.newRequestQueue(getContext());
                final String CompletaCeroCorr = completarCorrelativo(Integer.valueOf(txtCorrelativo.getText().toString()));
                String CorrelativoCompleto = txtserie.getText().toString()+"-"+CompletaCeroCorr;
                final String TI_DOCU;
                if ( txtserie.getText().toString().substring(0,1).equals("B"))
                {
                    TI_DOCU  = "BLT";
                }
                else{
                    TI_DOCU  = "FAC";
                }
                final String ws_buscarDocumento = getString(R.string.ws_ruta) + "TCDOCU_CLIE/"+CorrelativoCompleto+"/"+sharedPreferences.getString("guardar_idEmpresa", "NoData");
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Espere");
                progressDialog.setMessage("Buscando Documento");
                progressDialog.setCancelable(false);
                Thread hilo = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyJSONArrayRequest RequestBuscaBol = new MyJSONArrayRequest(Request.Method.GET, ws_buscarDocumento, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length()>0)
                                        {
                                            JSONObject json;
                                            try{
                                                json = response.getJSONObject(0);
                                                final String FechaEmision = json.getString("FE_DOCU");
                                                //DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH);
                                                //Date FechaVenta = format.parse(FechaEmision);
                                                //final String Femision = FechaVenta.toString();
                                                final String Femision = FechaEmision.substring(0,10);

                                                if(lista_boletosLeidos.size() == 0){
                                                    //lista_boletosLeidos.add(sharedPreferences.getString("guardar_idEmpresa","NoData")+"."+txtserie.getText().toString()+"."+CompletaCeroCorr+"."+TI_DOCU+"."+Femision);
                                                    lista_boletosLeidos.add(sharedPreferences.getString("guardar_idEmpresa","NoData")+"ƒ"+txtserie.getText().toString()+"ƒ"+CompletaCeroCorr+"ƒ"+TI_DOCU+"ƒ"+Femision);
                                                    guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                                                    //Log.d("tamano", lista_boletosLeidos.toString());

                                                    /* Los boletos leídos se muestran en la tabla */
                                                   /* listView = view.findViewById(R.id.listView_boletosLeidos);
                                                    TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, context_BuscarBoleto);
                                                    listView.setAdapter(adapterBoletosLeidos);*/
                                                    /* ----------------------------------------- */

                                                    btnBuscar.setEnabled(true);


                                                }else{


                                                    for (int i = 0; i < lista_boletosLeidos.size(); i++) {

                                                        if(lista_boletosLeidos.get(i).contains(CompletaCeroCorr)){

                                                            existe = true;
                                                            Toast.makeText(getActivity(),"El boleto ya fue agregado", Toast.LENGTH_LONG).show();
                                                            break;

                                                        }
                                                    }

                                                    if(!existe){
                                                        lista_boletosLeidos.add(sharedPreferences.getString("guardar_idEmpresa","NoData")+"ƒ"+txtserie.getText().toString()+"ƒ"+CompletaCeroCorr+"ƒ"+TI_DOCU+"ƒ"+Femision);
                                                        guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                                                        //Log.d("tamano", Integer.toString(lista_boletosLeidos.size()));
                                                    }

                                                }
                                                /*listView = view.findViewById(R.id.listView_boletosLeidos);
                                                TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
                                                listView.setAdapter(adapterBoletosLeidos);*/

                                                mataBoleto Embarque = new mataBoleto();
                                                //BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                                                FragmentManager fragmentManager = getFragmentManager();
                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                fragmentTransaction.replace(R.id.fragment_base, Embarque).commit();
                                            }catch (Exception e)
                                            {
                                                Log.d("error",e.getMessage());
                                            }

                                        }

                                        Log.d("data",response.toString());
                                        progressDialog.dismiss();

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
                        RequestBuscaBol.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(RequestBuscaBol);
                    }
                });
                hilo.start();
                progressDialog.show();
                btnBuscar.setEnabled(true);
            }
        });

    }
    void AgregaBoletos()
    {
        String DataArreglo = sharedPreferences.getString("lista_boletosLeidos","NoData");
        if (DataArreglo.equals("NoData")==false)
        {
            String Arr =  DataArreglo.substring(1,(DataArreglo.length())-1);
            ArrayList<String> Data = new ArrayList<>();
            List<String> items = Arrays.asList(Arr.split("\\,"));

            final int size = items.size();
            for (int i = 0; i < size; i++)
            {
                //object = items.get(i);
                lista_boletosLeidos.add(items.get(i));
                //do something with i
            }
            guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
            /*listView = view.findViewById(R.id.listView_boletosLeidos);
            TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
            listView.setAdapter(adapterBoletosLeidos);*/

            //List<String> list = new ArrayList<String>(Arrays.asList(string.split(" , ")));
        }


    }

}
