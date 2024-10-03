package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;


public class EmbarqueBuscarBus extends Fragment {
    private SQLiteDatabase sqLiteDatabase;
    Boolean ventaDone = false;

    /**
     *Constante para almacenar la secuencia del itinerario seleccionado.
     */
    private String secuenciaItin;
    /**
     * Constante para almacenar la fecha del itinerario seleccionado.
     */
    private String fechaItin;
    /**
     * Constante para almacenar la hora del itinerario seleccionado.
     */
    private String horaItin;
    /**
     * Constante para almacenar la empresa del itinerario seleccionado.
     */
    private String empresaItin;
    /**
     * Lista que almacena el itinerario encontrado.
     */
    private ListView listView;
    /**
     * Lista que almacena el itinerario encontrado.
     */
    private String busSeleccionado;

    ProgressDialog progressDialog;
    private DatabaseBoletos ventaBlt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        return inflater.inflate(R.layout.embarque_busqueda_bus, parent, false);
    }

    /**
     * Implementación de la lógica para buscar y mostrar una lista con todos los buses en ruta del día.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final EditText editText_codBus = view.findViewById(R.id.editText_codBus);

        Button btn_buscar = view.findViewById(R.id.btn_buscar);
        final Button btn_asignarItin = view.findViewById(R.id.btn_asignarItin);

        btn_asignarItin.setEnabled(false);

        /* TODO: CLICK LISTENER PARA EL BOTÓN BUSCAR BUS */
        btn_buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> lista_itinerario = getArray(sharedPreferences, gson, "jsonItinerario");
                Log.d("lista", lista_itinerario.toString());
                final ArrayList<String> lista_busesEncontrados = new ArrayList<>();
                for (int i = 0; i < lista_itinerario.size(); i++) {

                    String[] itinerario = lista_itinerario.get(i).split("/");
                    // itinerario[0] = CONDUCTOR
                    // itinerario[1] = CODIGO_CONDUCTOR
                    // itinerario[2] = ANFITRION
                    // itinerario[3] = CODIGO_ANFITIRON
                    // itinerario[4] = CODIGO_VEHICULO
                    // itinerario[5] = CODIGO_EMPRESA
                    // itinerario[6] = ORIGEN
                    // itinerario[7] = DESTINO
                    // itinerario[8] = RUMBO
                    // itinerario[9] = FECHA PROGRAMACION
                    // itinerario[10] = SECUENCIA
                    // itinerario[11] = TIPO_BUS
                    // itinerario[12] = HORA_SALIDA
                    // itinerario[13] = SERVICIO

                    /* Validación en caso se encuentre el código de bus buscado */
                    if (editText_codBus.getText().toString().equals(itinerario[4])) {

                        lista_busesEncontrados.add(lista_itinerario.get(i));

                        //break;
                    }
                }

                /* Los boletos encontrados se muestran en la tabla */
                listView = view.findViewById(R.id.listView_busEncontrado);
                TablaBusquedaBusAdapter adapterBusesEncontrados = new TablaBusquedaBusAdapter(lista_busesEncontrados, getActivity());
                listView.setAdapter(adapterBusesEncontrados);
                /* ----------------------------------------- */
                if(listView.getAdapter().getCount() != 0){
                    btn_asignarItin.setEnabled(true);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String[] busSeleccionado = lista_busesEncontrados.get(position).split("/");
                        secuenciaItin = busSeleccionado[10];
                        fechaItin = busSeleccionado[9];
                        horaItin = busSeleccionado[12];
                        empresaItin = busSeleccionado[5];
                        guardarDataMemoria("co_vehi_asig",busSeleccionado[4].toString(),getActivity());
                        Toast.makeText(getActivity(), "Se seleccionó el bus "+busSeleccionado[4].toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        /* TODO: CLICK LISTENER PARA EL BOTÓN BUSCAR BUS */
        btn_asignarItin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listView.getAdapter().getCount() != 0){

                    final RequestQueue queue = Volley.newRequestQueue(getActivity());

                    final JSONObject jsonObject = generarJSON();
                    //Log.d("json", jsonObject.toString());

                    //Log.d("tamaño", Integer.toString(jsonObject.length()));

                    /* Ruta de la Web service */
                    final String ws_getEmbarque = getString(R.string.ws_ruta) + "Embarque";
                    //Log.d("ws embarque", ws_getEmbarque);
                    /* ----------------------------------------- */
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Espere");
                    progressDialog.setMessage("Asignando");
                    progressDialog.setCancelable(false);
                    Thread hilo = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MyJSONArrayRequest jsonArrayRequestEmbarque = new MyJSONArrayRequest(Request.Method.PUT, ws_getEmbarque, jsonObject,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray response) {
                                            if (response.length() > 0) {

                                                JSONObject info;
                                                try {

                                                    info = response.getJSONObject(0);
                                                    String respuesta = info.getString("Respuesta");
                                                   //Log.d("respuesta", respuesta);

                                                    SharedPreferences.Editor Elimina = sharedPreferences.edit();
                                                    Elimina.remove("lista_boletosLeidos");
                                                    Elimina.commit();
                                                    progressDialog.dismiss();
                                                    /* Cambia a la vista de buscar boleto */
                                                    EmbarqueLeerBoletos embarqueLeerBoletos = new EmbarqueLeerBoletos();
                                                    FragmentManager fragmentManager = getFragmentManager();
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.fragment_base, embarqueLeerBoletos).commit();
                                                    /* ----------------------------------------- */

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    Toast.makeText(getActivity(), "Error en la ws Embarque. No se pudo realizar el embarque.", Toast.LENGTH_LONG).show();
                                    errorWS(queue, error);
                                }
                            }) {
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
                            /* ----------------------------------------- */
                            jsonArrayRequestEmbarque.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            queue.add(jsonArrayRequestEmbarque);
                        }
                    });
                    hilo.start();
                    progressDialog.show();
                    /* Request que actualiza los correlativos de Anfitrión */


                }else {
                    Toast.makeText(getActivity(), "No se ha buscado un itinerario.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Genera un JSON con los correlativos de viaje y carga tanto para boleta como para factura.
     * @return Objeto JSON que contiene toda la data de los correlativos.
     */
    public JSONObject generarJSON(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Gson gson = new Gson();

        /* Arreglo para los boletos leidos */
        final ArrayList<String> lista_boletosLeidos = getArray(sharedPreferences, gson, "lista_boletosLeidos");
        /* ----------------------------------------- */

        JSONArray arrayBoletos =  new JSONArray();
        //JSONObject boleto = new JSONObject();
        JSONObject embarque = new JSONObject();


        try {
            for(int i = 0; i<lista_boletosLeidos.size(); i++){

                String[] boletoLeído = lista_boletosLeidos.get(i).split("\\.");

                if(boletoLeído[0].equals(empresaItin)){
                    JSONObject boleto = new JSONObject();
                    boleto.put("Empresa", boletoLeído[0]);
                    boleto.put("TipoDocumento", boletoLeído[3]);
                    boleto.put("NumeroDocumento", boletoLeído[1]+"-"+boletoLeído[2]);
                    boleto.put("SecuenciaItin", secuenciaItin);
                    boleto.put("FechaViajeItin", fechaItin);
                    boleto.put("HoraViajeItin", horaItin);
                    arrayBoletos.put(boleto);
                    ventaBlt = new DatabaseBoletos(getContext());
                    sqLiteDatabase = ventaBlt.getWritableDatabase();
                    try   {
                            ContentValues sqlQuery = new ContentValues();
                            sqlQuery.put("CO_EMPR", boletoLeído[0].toString());
                            sqlQuery.put("TI_DOCU", boletoLeído[3].toString());
                            sqlQuery.put("NU_DOCU", boletoLeído[1].toString()+"-"+boletoLeído[2].toString());
                            sqlQuery.put("NU_SECU", secuenciaItin.toString());
                            sqlQuery.put("FE_VIAJ",fechaItin.toString());
                            sqlQuery.put("HO_VIAJ",horaItin.toString());
                            sqlQuery.put("CO_VEHI",sharedPreferences.getString("co_vehi_asig","NoData"));
                            sqLiteDatabase.insert("Asignacion",null,sqlQuery);

                    } catch (Exception e) {
                        String error = e.getMessage();

                    }
                }else {

                    Toast.makeText(getActivity(), "La empresa del boleto leído "+boletoLeído[1]+"-"+boletoLeído[2]+" es diferente al de itinerario.", Toast.LENGTH_LONG).show();
                }

            }

            //arrayBoletos.put(boleto);
            embarque.put("Embarque",arrayBoletos);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return embarque;
    }
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    public void stopBoletoService() {
        BoletoService.startService(getActivity(), false);
    }
    /**
     * Mapea el error del WS y muestra una vista de error.
     * @param queue Contiene el queue del request.
     * @param error Determina el tipo de error de la Web Service.
     */
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

