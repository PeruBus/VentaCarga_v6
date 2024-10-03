package pe.com.telefonica.soyuz;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public  class BoletoService extends IntentService {
   private DatabaseBoletos ventaBlt;
   Gson gson;
   private SQLiteDatabase sqLiteDatabase;
    Boolean listaBoletosDone = false;
    RequestQueue queue;
    public BoletoService(String name) {
        super(name);
    }
    public BoletoService() {
        super("Boleto Service");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Gson gson = new Gson();
        if (sharedPreferences.getString("puestoUsuario", "NoData").equals("ANFITRION ESTANDAR") ||
                sharedPreferences.getString("puestoUsuario", "NoData").equals("CONDUCTOR ESTANDAR")) {
            ventaBoletos();
        } else if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_INSP")) {
//            Toast toast = Toast.makeText(getApplicationContext(),"ACTUALIZANDO ITINERARIO , RECUERDE QUE SE REALIZA CADA 5 MIN.", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
            ActualizaControlador("ANDROID_INSP");
            InspeccionWS();
        } else if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")) {
            ventaBoletos();
        } else if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_CONTROL")) {
//            Toast toast = Toast.makeText(getApplicationContext(),"ACTUALIZANDO ITINERARIO , RECUERDE QUE SE REALIZA CADA 5 MIN.", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
            ActualizaControlador("ANDROID_CONTROL");
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, BoletoService.class);
    }


    public static void startService(Context context, boolean isOn) {

        Intent i = BoletoService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isOn) {
            if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_CONTROL")||
                    sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_INSP")) {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 300000, 300000, pi);
            }else{
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 1000, 1000, pi);
            }
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }


    public void ventaBoletos() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        ventaBlt = new DatabaseBoletos(this);
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        final Cursor cursor = sqLiteDatabase.query("VentaBoletos", null, "estado=\"pendiente\"", null, null, null, null);
        if (cursor.getCount() >= 1) {
            Log.d("cantidad", Integer.toString(cursor.getCount()));
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnected();
            if (isConnected) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                    String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                    final String id = cursor.getString(cursor.getColumnIndex("id"));
                    try {
                        final JSONObject jsonObject = new JSONObject(data);
                        String ws_postVenta = "";
                        if (tipo.equals("viaje")) {
                            ws_postVenta = getString(R.string.ws_ruta) + "SetVentaRuta";
                        } else if (tipo.equals("carga")) {
                            ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";
                        }
                        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if(!response.toString().equals("null") && !response.toString().equals("[]"))
                                        {
                                            if (response.length() > 0) {
                                                JSONObject info;
                                                try {
                                                    info = response.getJSONObject(0);
                                                    String respuesta = info.getString("Respuesta");
                                                    if (respuesta.equals("EXISTE")) {
                                                        ContentValues cv = new ContentValues();
                                                        cv.put("data_boleto", jsonObject.toString());
                                                        cv.put("estado", "guardado");
                                                        int value = sqLiteDatabase.update("VentaBoletos", cv, "id=" + id, null);
                                                    } else {
                                                        ContentValues cv = new ContentValues();
                                                        cv.put("data_boleto", jsonObject.toString());
                                                        cv.put("estado", "pendiente");
                                                        int value = sqLiteDatabase.update("VentaBoletos", cv, "id=" + id, null);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
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
                        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonArrayRequestVenta);
                        breakTime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            } else {
                Toast.makeText(getApplicationContext(), "No hay conectividad.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void ActualizaControlador(final String RequestTRAMA)
    {
        queue = Volley.newRequestQueue(getApplicationContext());
        gson = new Gson();
        String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/InfinityDev/Puesto/"+RequestTRAMA;
        JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "El usuario no tiene ningún itinerario asignado.", Toast.LENGTH_LONG).show();
                        } else if (response.length() == 1) {
                            Toast.makeText(getApplicationContext(), "El usuario solo tiene un itinerario asignado.", Toast.LENGTH_LONG).show();
                        } else if (response.length() > 1) {
                            JSONObject info;
                            ArrayList<String> lista_itinerario = new ArrayList<>();
                            Log.d("itin_", response.toString());
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    String fecha = info.getString("FECHA_PROGRAMACION");
                                    fecha = fecha.substring(0, 10);
                                    lista_itinerario.add(info.getString("CONDUCTOR") + "/" + info.getString("CODIGO_CONDUCTOR") +
                                            "/" + info.getString("ANFITRION") + "/" + info.getString("CODIGO_ANFITIRON") +
                                            "/" + info.getString("CODIGO_VEHICULO") + "/" + info.getString("CODIGO_EMPRESA") +
                                            "/" + info.getString("ORIGEN") + "/" + info.getString("DESTINO") +
                                            "/" + info.getString("RUMBO") + "/" + fecha + "/" + info.getString("SECUENCIA") +
                                            "/" + info.getString("TIPO_BUS") + "/" + info.getString("HORA_SALIDA") + "/" + info.getString("SERVICIO"));
                                }
                                String jsonItinerario = gson.toJson(lista_itinerario);
                                guardarDataMemoria("jsonItinerario", jsonItinerario, getApplicationContext());
                            } catch (JSONException e) {
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
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
        queue.add(jsonArrayRequestItinerario);
        breakTime();
    }

    public void trasbordoBoletos() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        ventaBlt = new DatabaseBoletos(this);
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        final Cursor cursor = sqLiteDatabase.query("TransbordoBoletos", null, "estado=\"pendiente\"", null, null,null,null);
        if (cursor.getCount() >= 1){
            ConnectivityManager cm =
                    (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnected();
            if (isConnected){
                while(cursor.moveToNext()){
                    String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                    final String id = cursor.getString(cursor.getColumnIndex("id"));
                    try {
                        final JSONObject jsonObject = new JSONObject(data);
                        String ws_trasbordo = getString(R.string.ws_ruta) + "Transbordo";
                        MyJSONArrayRequest jsonArrayRequestTrasbordo = new MyJSONArrayRequest(Request.Method.POST, ws_trasbordo, jsonObject,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length() > 0) {
                                            JSONObject info;
                                            try {
                                                info = response.getJSONObject(0);
                                                String respuesta = info.getString("Respuesta");
                                                if (respuesta.equals("GUARDADO")) {
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("data_boleto", jsonObject.toString());
                                                    cv.put("estado", "guardado");
                                                    int value = sqLiteDatabase.update("TransbordoBoletos",cv, "id="+id, null);
                                                }else {
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("data_boleto", jsonObject.toString());
                                                    cv.put("estado", "guardado");
                                                    int value = sqLiteDatabase.update("TransbordoBoletos",cv, "id="+id, null);
                                                }
                                             } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error en la ws Trasbordo. No se pudo realizar el trasbordo.", Toast.LENGTH_SHORT).show();
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
                        jsonArrayRequestTrasbordo.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonArrayRequestTrasbordo);
                        breakTime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }else{
                Toast.makeText(getApplicationContext(), "No hay conectividad.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void reporteInspeccion() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        ventaBlt = new DatabaseBoletos(this);
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        final Cursor cursor = sqLiteDatabase.query("ReporteInspeccion", null, "estado=\"pendiente\"", null, null,null,null);
        if (cursor.getCount() >= 1){
            ConnectivityManager cm =
                    (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnected();
            if (isConnected){
                while(cursor.moveToNext()){
                    String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                    final String id = cursor.getString(cursor.getColumnIndex("id"));
                    try {
                        final JSONObject jsonObject = new JSONObject(data);
                        String ws_postGuardarReporte = getString(R.string.ws_ruta) + "InspeccionDocumentoRuta";
                        MyJSONArrayRequest jsonArrayRequestReporte = new MyJSONArrayRequest(Request.Method.POST, ws_postGuardarReporte, jsonObject,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length() > 0) {
                                            JSONObject info;
                                            try {
                                                info = response.getJSONObject(0);
                                                String respuesta = info.getString("Respuesta");
                                                if (respuesta.equals("EXISTE")) {
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("data_boleto", jsonObject.toString());
                                                    cv.put("estado", "guardado");
                                                    int value = sqLiteDatabase.update("ReporteInspeccion",cv, "id="+id, null);
                                                } else {
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("data_boleto", jsonObject.toString());
                                                    cv.put("estado", "pendiente");
                                                    int value = sqLiteDatabase.update("ReporteInspeccion",cv, "id="+id, null);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error en la ws GuardarReporte. No se pudo guardar el reporte de inspección.", Toast.LENGTH_SHORT).show();
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
                        jsonArrayRequestReporte.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonArrayRequestReporte);
                        breakTime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }else{
                Toast.makeText(getApplicationContext(), "No hay conectividad", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void  GetTarifaRuta(){
        final RequestQueue queue = Volley.newRequestQueue(this);
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            SharedPreferences sharedPreferences;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String ws_Valida_Tarifa = getString(R.string.ws_ruta) + "TMVALIDA_TARIFA/" + sharedPreferences.getString("anf_rumbo", "NoData") + "/" + sharedPreferences.getString("anf_fechaProgramacion", "NoData")
                    + "/" + sharedPreferences.getString("anf_secuencia", "NoData") + "/" + sharedPreferences.getString("NU_TARI", "NoData") + "/" + sharedPreferences.getString("CO_TIPO", "NoData");
            final String RUMBO = sharedPreferences.getString("anf_rumbo","NoData");
            final String FE_PROG_ITIN = sharedPreferences.getString("anf_fechaProgramacion","NoData");
            final String NU_SECU = sharedPreferences.getString("anf_secuencia","NoData");
            final String NU_TARI = sharedPreferences.getString("NU_TARI","NoData");
            final String CO_TIPO = sharedPreferences.getString("CO_TIPO","NoData");
            final String CO_EMPR = sharedPreferences.getString("anf_codigoEmpresa","NoData");
            Log.d("urlWS_TARIFA", ws_Valida_Tarifa);
            JsonArrayRequest jsonArrayRequestTarifa = new JsonArrayRequest(Request.Method.GET, ws_Valida_Tarifa, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No hay tarifas para este itinerario.", Toast.LENGTH_LONG).show();
                            } else if (response.length() > 0) {
                                try {
                                    JSONObject info;
                                    info = response.getJSONObject(0);
                                    String Resultado = info.getString("Respuesta");
                                    Log.d("pruebaSincroniza", Resultado.toString());
                                    if (Resultado.equals("1")) {
                                        Log.d("pruebaSincroniza", "1");
                                        Tarifario(RUMBO,CO_EMPR,NU_SECU,FE_PROG_ITIN);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error en la ws getTarifas.", Toast.LENGTH_LONG).show();
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
            queue.add(jsonArrayRequestTarifa);
        }
    }
    void Tarifario(String Rumbo, final String Empresa, String Secuencia, String FechaProgramacion)
    {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String ws_getTarifa = getString(R.string.ws_ruta) + "GetTarifa/" + Rumbo + "/" + Empresa + "/" +
                Secuencia + "/" + FechaProgramacion;
        JsonArrayRequest jsonArrayRequestTarifa = new JsonArrayRequest(Request.Method.GET, ws_getTarifa, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "No hay tarifas para este itinerario.", Toast.LENGTH_LONG).show();
                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_tarifasViaje = new ArrayList<>();
                                info = response.getJSONObject(0);
                                guardarDataMemoria("NU_TARI",info.getString("NU_TARI"),getApplicationContext());
                                guardarDataMemoria("CO_TIPO",info.getString("CO_TIPO"),getApplicationContext());
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    lista_tarifasViaje.add(info.getString("CO_DEST_ORIG") + "-"
                                            + info.getString("CO_DEST_FINA") + "-" + Integer.toString(info.getInt("PR_BASE")));
                                }
                                String jsonTarifasViaje = gson.toJson(lista_tarifasViaje);
                                guardarDataMemoria("anf_jsonTarifasViaje", jsonTarifasViaje, getApplicationContext());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getTarifas.", Toast.LENGTH_LONG).show();
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
        queue.add(jsonArrayRequestTarifa);
    }
    public void breakTime(){
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        Intent intent = new Intent(BoletoService.this, ErrorActivity.class);
        startActivity(intent);
    }
    public void InspeccionWS() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        ventaBlt = new DatabaseBoletos(this);
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        final Cursor cursor = sqLiteDatabase.query("VentaBoletos", null, "estado=\"pendiente\" and tipo=\"inspeccion\"" , null, null, null, null);
        if (cursor.getCount() >= 1) {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnected();
            if (isConnected) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                    String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                    final String id = cursor.getString(cursor.getColumnIndex("id"));
                    try {
                        final JSONObject jsonObject = new JSONObject(data);
                        String ws_postVenta = "";
                        if (tipo.equals("viaje")) {
                            ws_postVenta = getString(R.string.ws_ruta) + "SetVentaRuta";

                        } else if (tipo.equals("carga")) {
                            ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";

                        } else if (tipo.equals("inspeccion")) {
                            ws_postVenta = getString(R.string.ws_ruta) + "TCDOCU_INSP_RTA";
                        }
                        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if(!response.toString().equals("null") && !response.toString().equals("[]"))
                                        {
                                            if (response.length() > 0) {
                                                JSONObject info;
                                                try {
                                                    info = response.getJSONObject(0);
                                                    String respuesta = info.getString("Respuesta");
                                                    if (respuesta.equals("EXISTE")) {
                                                        ContentValues cv = new ContentValues();
                                                        cv.put("data_boleto", jsonObject.toString());
                                                        cv.put("estado", "guardado");
                                                        int value = sqLiteDatabase.update("VentaBoletos", cv, "id=" + id, null);
                                                    } else {
                                                        ContentValues cv = new ContentValues();
                                                        cv.put("data_boleto", jsonObject.toString());
                                                        cv.put("estado", "pendiente");
                                                        int value = sqLiteDatabase.update("VentaBoletos", cv, "id=" + id, null);
                                                    }
                                                 } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
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
                        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonArrayRequestVenta);
                        breakTime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            } else {
                Toast.makeText(getApplicationContext(), "No hay conectividad.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}