package pe.com.telefonica.soyuz;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import android.util.Base64;
import android.util.Log;
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

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;



public class ActualizacionAsientosService extends IntentService {

    public ActualizacionAsientosService(String name) {
        super(name);
    }

    public ActualizacionAsientosService(){
        super("Actualización Asientos Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Gson gson = new Gson();
        final RequestQueue queue = Volley.newRequestQueue(this);
        String ws_getFlagActualizacion = getString(R.string.ws_ruta) + "ValidadorAsiento/"+sharedPreferences.getString("anf_fechaProgramacion", "NoData")+
                "/"+ sharedPreferences.getString("anf_secuencia", "NoData") +"/"+sharedPreferences.getString("anf_rumbo", "NoData");
        //Log.d("servicioActu", ws_getFlagActualizacion);
        JsonArrayRequest jsonArrayRequestActualizarAsientos = new JsonArrayRequest(Request.Method.GET, ws_getFlagActualizacion, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 1) {
                            try {
                                    JSONObject info = response.getJSONObject(0);
                                    final  int CantBolOld = Integer.parseInt(sharedPreferences.getString("cantBoletos", "NoData"));
                                    Log.d("OldBol",sharedPreferences.getString("cantBoletos", "NoData"));
                                    final int CantBolNew = Integer.parseInt(info.getString("Respuesta"));
                                    Log.d("newbol",info.getString("Respuesta"));
                                //if (info.getString("Respuesta").equals("SI")) {
                                if ((CantBolNew-CantBolOld)>0) {
                                    String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" +
                                            sharedPreferences.getString("anf_secuencia", "NoData") + "/" + sharedPreferences.getString("anf_rumbo", "NoData") + "/" +
                                            sharedPreferences.getString("anf_fechaProgramacion", "NoData");
                                    JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() > 0) {
                                                        try {
                                                            ArrayList<String> lista_reporteVenta = new ArrayList<>();
                                                            ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                                                            JSONObject info;
                                                            for (int i = 0; i < response.length(); i++) {

                                                                info = response.getJSONObject(i);
                                                                if(info.getString("ServicioEmpresa").equals("VIAJE")){
                                                                    lista_reporteVenta.add(info.getString("NU_ASIE"));
                                                                }
                                                            }
                                                            String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                                            guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getApplicationContext());
                                                            String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                                                            guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getApplicationContext());
                                                            guardarDataMemoria("cantBoletos",String.valueOf(CantBolNew),getApplicationContext());
                                                            Intent dialogIntent = new Intent(ActualizacionAsientosService.this, AppSideBarActivity.class);
                                                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(dialogIntent);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getReporteVenta al momento de actualizar los asientos.", Toast.LENGTH_LONG).show();
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
                                    jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(jsonArrayRequestAsientosVendidos);
                                    breakTime();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), "Se obtuvo más de un valor en la respuesta.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws ActualizarAsientos. No se puedo actualizar los asientos.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestActualizarAsientos.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestActualizarAsientos);
        breakTime();
    }
    public static Intent newIntent(Context context) {
        return new Intent(context, ActualizacionAsientosService.class);
    }

    public static void actualizarAsientos(Context context, boolean isOn) {

        Intent i = ActualizacionAsientosService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 15000, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }


    private void errorWS(RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {

            Toast.makeText(getApplicationContext(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        }else if (error instanceof TimeoutError) {
            Toast.makeText(getApplicationContext(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getApplicationContext(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getApplicationContext(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getApplicationContext(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        }else if (error instanceof ParseError) {
            Toast.makeText(getApplicationContext(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        Intent intent = new Intent(ActualizacionAsientosService.this, ErrorActivity.class);
        startActivity(intent);
    }
}
