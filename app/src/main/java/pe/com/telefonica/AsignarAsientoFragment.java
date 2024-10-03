package pe.com.telefonica.soyuz;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;


 
public class AsignarAsientoFragment extends Fragment {

    ProgressDialog progressDialog;

    private DatabaseBoletos ventaBlt;

    private SQLiteDatabase sqLiteDatabase;

    private JSONArray getReporteVenta;

    private Boolean asientoUsado = false;

    Boolean getReporteVentaDone = false;

    EditText editText_selecAsiento;

    Button button_guardarAsiento;

    Button btn_asignarAsiento;

    ListView listView;

    Dialog selecAsiento_dialog;

    final ArrayList<String> lista_boletosAsignarAsientos = new ArrayList<>();

    final ArrayList<String> lista_numAsientos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.asignar_asiento, parent, false);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {


        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        ActualizacionAsientosService.actualizarAsientos(getActivity(), false);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        selecAsiento_dialog = new Dialog(getActivity());
        selecAsiento_dialog.setContentView(R.layout.seleccionar_asiento_dialog);

        editText_selecAsiento = selecAsiento_dialog.findViewById(R.id.editText_selecAsiento);
        button_guardarAsiento = selecAsiento_dialog.findViewById(R.id.button_guardarAsiento);

        btn_asignarAsiento = view.findViewById(R.id.btn_asignarAsiento);

        listView = view.findViewById(R.id.listView_asignarAsientos);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getReporteVentaWS(queue, sharedPreferences);

    }


    public JSONObject generarJSON(ArrayList<String> lista_boletosAsignarAsientos){

        JSONArray arrayBoletos =  new JSONArray();
        //JSONObject boleto = new JSONObject();
        JSONObject documento = new JSONObject();

        try {
            for(int i = 0; i<lista_boletosAsignarAsientos.size(); i++){

                String[] dataBoleto = lista_boletosAsignarAsientos.get(i).split("/");
                JSONObject boleto = new JSONObject();
                boleto.put("Empresa", dataBoleto[0]);
                boleto.put("TipoDocumento", dataBoleto[1]);
                boleto.put("NumeroDocumento", dataBoleto[2]);
                boleto.put("Asiento", dataBoleto[3]);
                arrayBoletos.put(boleto);
            }

            //arrayBoletos.put(boleto);
            documento.put("Documento",arrayBoletos);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return documento;
    }


    public void getResponse(final RequestQueue queue, String url, final VolleyCallbackInterface volleyCallbackInterface) {
        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0) {
                            volleyCallbackInterface.onSuccessResponse(response.toString(), true);

                        } else if (response.length() > 0) {
                            volleyCallbackInterface.onSuccessResponse(response.toString(), true);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getActivity(), "Error en la ws getReporteVenta.", Toast.LENGTH_LONG).show();
               // errorWS(queue, error);
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
        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestAsientosVendidos);

    }


    public void getReporteVentaWS(RequestQueue queue, final SharedPreferences sharedPreferences) {
        String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" +
                sharedPreferences.getString("anf_secuencia", "NoData") + "/" +
                sharedPreferences.getString("anf_rumbo", "NoData") + "/" +
                sharedPreferences.getString("anf_fechaProgramacion", "NoData");
        Log.d("asientos vendidos", ws_getAsientosVendidos);
        getResponse(queue, ws_getAsientosVendidos,
                new VolleyCallbackInterface() {
                    @Override
                    public void onSuccessResponse(String result, Boolean flag) {
                        guardarDataMemoria("getReporteVenta", result, getActivity());
                        Log.d("venta", result);
                        Log.d("asientos venta", getReporteVentaDone.toString());
                        getReporteVentaDone = flag;
                        try {
                            getReporteVenta = new JSONArray(sharedPreferences.getString("getReporteVenta", "[{'CO_EMPR':'0'}]"));
                            if(getReporteVenta.length() != 0){
                                for(int i = 0; i < getReporteVenta.length(); i++){
                                    if(!lista_numAsientos.contains(getReporteVenta.getJSONObject(i).getString("NU_ASIE")) &&
                                            !getReporteVenta.getJSONObject(i).getString("NU_ASIE").equals("0") &&
                                            getReporteVenta.getJSONObject(i).getString("ServicioEmpresa").equals("VIAJE")){
                                        lista_numAsientos.add(getReporteVenta.getJSONObject(i).getString("NU_ASIE"));
                                    }
                                    String tramaBoleto = getReporteVenta.getJSONObject(i).getString("CO_EMPR")+"/"+
                                            getReporteVenta.getJSONObject(i).getString("TI_DOCU")+"/"+
                                            getReporteVenta.getJSONObject(i).getString("NU_DOCU")+"/"+
                                            getReporteVenta.getJSONObject(i).getString("NU_ASIE");
                                    if(!lista_boletosAsignarAsientos.contains(tramaBoleto) &&
                                            getReporteVenta.getJSONObject(i).getString("NU_ASIE").equals("0") &&
                                            getReporteVenta.getJSONObject(i).getString("ServicioEmpresa").equals("VIAJE")){
                                        lista_boletosAsignarAsientos.add(tramaBoleto);
                                    }
                                }
                                final TablaAsignarAsientosAdapter asientosAdapter = new TablaAsignarAsientosAdapter(lista_boletosAsignarAsientos, getActivity());
                                listView.setAdapter(asientosAdapter);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                button_guardarAsiento.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                String numAsiento = editText_selecAsiento.getText().toString();
                                                Log.d("asiento",numAsiento);
                                                int numAsientosTotales = sharedPreferences.getInt("anf_numAsientos", 0);

                                                if(Integer.valueOf(numAsiento) > numAsientosTotales){
                                                    Toast.makeText(getActivity(), "El asiento seleccionado excede el número total de asientos en el bus.", Toast.LENGTH_SHORT).show();

                                                } else if(numAsiento.equals("")){
                                                    Toast.makeText(getActivity(), "Debe seleccionar un asiento.", Toast.LENGTH_SHORT).show();

                                                }else if(lista_numAsientos.contains(numAsiento)){
                                                    Toast.makeText(getActivity(), "El asiento seleccionado ya está ocupado.", Toast.LENGTH_SHORT).show();

                                                } else {

                                                    for(int i = 0; i < lista_boletosAsignarAsientos.size(); i++){

                                                        String[] dataBoleto = lista_boletosAsignarAsientos.get(i).split("/");

                                                        if(dataBoleto[3].equals(numAsiento)){

                                                            asientoUsado = true;
                                                            Toast.makeText(getActivity(), "El asiento ya ha sido seleccionado.", Toast.LENGTH_SHORT).show();
                                                            break;

                                                        }
                                                    }

                                                    if(!asientoUsado){

                                                        String[] dataBoleto = lista_boletosAsignarAsientos.get(position).split("/");
                                                        dataBoleto[3] = numAsiento;
                                                        String boletoActualizado = dataBoleto[0]+"/"+dataBoleto[1]+"/"+dataBoleto[2]+"/"+dataBoleto[3];
                                                        lista_boletosAsignarAsientos.set(position, boletoActualizado);
                                                    }
                                                }
                                                /* ----------------------------------------- */
                                                asientosAdapter.notifyDataSetChanged();
                                                selecAsiento_dialog.hide();
                                            }
                                        });
                                        selecAsiento_dialog.show();
                                    }
                                });
                                btn_asignarAsiento.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        progressDialog = new ProgressDialog(getActivity());
                                        progressDialog.setTitle("Espere por favor");
                                        progressDialog.setMessage("Imprimiendo");
                                        progressDialog.setCancelable(false);
                                        Thread thread = new Thread(new Runnable() {
                                            //getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                              try {
                                                 final RequestQueue queue = Volley.newRequestQueue(getActivity());
                                                  final JSONObject jsonObject = generarJSON(lista_boletosAsignarAsientos);
                                                  Log.d("jsonAsie", jsonObject.toString());
                                                  String ws_asignarAsientos = getString(R.string.ws_ruta) + "AsignaAsiento";
                                                  Log.d("wsAsie",ws_asignarAsientos);
                                                  MyJSONArrayRequest jsonArrayRequestAsignarAsiento = new MyJSONArrayRequest(Request.Method.PUT, ws_asignarAsientos, jsonObject,
                                                          new Response.Listener<JSONArray>() {
                                                              @Override
                                                              public void onResponse(JSONArray response) {
                                                                  if (response.length() > 0) {

                                                                      JSONObject info;
                                                                      try {

                                                                          info = response.getJSONObject(0);

                                                                          String respuesta = info.getString("Respuesta");
                                                                          Log.d("respuesta", respuesta);
                                                                          progressDialog.dismiss();
                                                                          VentaBoletosFragment VentaBoletosFragment_ = new VentaBoletosFragment();
                                                                          FragmentManager fragmentManager = getFragmentManager();
                                                                          FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                          fragmentTransaction.replace(R.id.fragment_base, VentaBoletosFragment_).commit();
                                                                          //ActualizacionAsientosService.actualizarAsientos(getActivity(), true);

                                                                      } catch (JSONException e) {
                                                                          e.printStackTrace();
                                                                      }
                                                                  }
                                                                  //* ----------------------------------------- *//*
                                                              }
                                                          }, new Response.ErrorListener() {
                                                      @Override
                                                      public void onErrorResponse(VolleyError error) {
                                                          error.printStackTrace();
                                                          Toast.makeText(getActivity(), "Error en la ws de asignarAsientos. No se pudo asignar los asientos.", Toast.LENGTH_SHORT).show();
                                                          //errorWS(queue, error);
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
                                                      //* ----------------------------------------- *//*
                                                  };
                                                  //* ----------------------------------------- *//*
                                                  jsonArrayRequestAsignarAsiento.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                  queue.add(jsonArrayRequestAsignarAsiento);
                                            }catch (Exception e)
                                            {
                                                Log.d("error",e.toString());
                                                //button_imprimirBoleto.setEnabled(true);
                                                progressDialog.dismiss();
                                            }

                                        }
                                    });
                                    thread.start();
                                    progressDialog.show();



                                    }
                                });
                                //* ----------------------------------------- *//*

                            }else{
                                btn_asignarAsiento.setEnabled(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });

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
