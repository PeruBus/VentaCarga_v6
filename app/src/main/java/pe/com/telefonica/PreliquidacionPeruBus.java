package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class PreliquidacionPeruBus extends Fragment {
    ProgressDialog progressDialog;
    RequestQueue queue;
    int FlagValidaButton = 0;
    /**
     * Base de datos interna.
     */
    private DatabaseBoletos ventaBlt;
    /**
     * Instancia de SQLiteDatabase.
     */
    private SQLiteDatabase sqLiteDatabase;
    /**
     * Constante para la interaccion con el APK de DigiFlow.
     */
    JSONArray getListaBoletos = null;
    /**
     * Instancia para guardar datos en memoria.
     */
    private SharedPreferences sharedPreferences;
    /**
     * Valor del monto total de preliquidacion.
     */
    float montoTotal = 0;
    /**
     * Valor de la cantidad de boletos de preliquidacion.
     */
    int cantBoletos = 0;
    Cursor cursor;

    TableLayout tableLayout;
    TextView textView_montoTotal;
    TextView textView_cantBoletos;

    Button button_imprimir;
    Button button_liberarLiquidacion;
    StringBuilder listaBoletos;


    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preliquidacion_agen_viaje, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Inicialización de la instancia para guardar datos en memoria */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /* ----------------------------------------- */

        tableLayout = view.findViewById(R.id.table_layout_1);
        textView_montoTotal = view.findViewById(R.id.textView_montoTotal_1);
        textView_cantBoletos = view.findViewById(R.id.textView_cantBoletos_1);

        button_imprimir = view.findViewById(R.id.button_imprimir_1);
        button_liberarLiquidacion = view.findViewById(R.id.button_liberarLiquidacion_1);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        queue = Volley.newRequestQueue(getActivity());

        listaBoletos = new StringBuilder();

        /* Inicialización de la base de datos */
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        /* ----------------------------------------- */
        getListaBoletosWS(queue, sharedPreferences);




    }

    public void getResponse(RequestQueue queue, String url, final VolleyCallbackInterface volleyCallbackInterface) {


        /* Request a la ws */
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    //TODO: Cuando hay respuesta del Servidor
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
                return;
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
        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestVenta);

    }

    /**
     * Obtiene la lista de boletos de preliquidacion.
     *
     * @param queue
     * @param sharedPreferences
     */
    public void getListaBoletosWS(RequestQueue queue, final SharedPreferences sharedPreferences) {

        /* Fecha y hora de la venta del boleto */
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        /* ----------------------------------------- */

        /* Ruta de la Web service */
        String ws_preLiquidacion = getString(R.string.ws_ruta) + "PreLiquidacionBoletero/" + sharedPreferences.getString("codigoUsuario", "NoData") + "/" + fechaVenta;
        Log.d("asientos vendidos", ws_preLiquidacion);
        /* ----------------------------------------- */
        getResponse(queue, ws_preLiquidacion,
                new VolleyCallbackInterface() {
                    @Override
                    public void onSuccessResponse(String result, Boolean flag) {
                        guardarDataMemoria("listaBoletos", result, getActivity());
                        try {
                            getListaBoletos = new JSONArray(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.d("prueba", result);

                        /* query a la tabla VentaBoletos */
                        //cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\"", null, null, null, null);
                        cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\" and CO_EMPR=\"01\"", null, null,null,null);
                        /* ----------------------------------------- */

                        /* Se obtiene la cantidad de boletos, se muestra en pantalla y se guarda en memoria */
                        textView_cantBoletos.setText(Integer.toString(cantBoletos));
                        editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
                        editor.commit();
                        /* ----------------------------------------- */

                        /* Iteración en función a la cantidad filas obtenidas en el query */
                        while (cursor.moveToNext()) {

                            /* Se obtiene el JSON (string) y el tipo (viaje/carga) */
                            String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                            String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                            /* ----------------------------------------- */

                            try {

                                /* Se genera un JSON a partir de un string */
                                JSONObject jsonObject = new JSONObject(data);
                                /* ----------------------------------------- */

                                /* Se crea la fila y se configura sus propiedades */
                                TableRow tableRow = new TableRow(getActivity());
                                tableRow.setId(0);

                                tableRow.setLayoutParams(new TableRow.LayoutParams(
                                        TableRow.LayoutParams.MATCH_PARENT,
                                        TableRow.LayoutParams.WRAP_CONTENT));
                                tableRow.setPadding(10, 10, 10, 10);
                                tableRow.setClickable(true);

                                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                TableRow.LayoutParams params1 = new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                params.setMargins(0, 0, 5, 0);
                                /* ----------------------------------------- */

                                /* Se obtienen los valores de número de documento, origen, destino y monto, y se a cada TextView */
                                TextView numDocumento = new TextView(getContext());
                                numDocumento.setLayoutParams(params);
                                numDocumento.setTextColor(Color.parseColor("#000000"));
                                numDocumento.setGravity(Gravity.CENTER);

                                TextView origen = new TextView(getContext());
                                origen.setLayoutParams(params);
                                origen.setTextColor(Color.parseColor("#000000"));
                                origen.setGravity(Gravity.CENTER);

                                TextView destino = new TextView(getContext());
                                destino.setLayoutParams(params);
                                destino.setTextColor(Color.parseColor("#000000"));
                                destino.setGravity(Gravity.CENTER);

                                TextView empresa = new TextView(getContext());
                                empresa.setLayoutParams(params);
                                empresa.setTextColor(Color.parseColor("#000000"));
                                empresa.setGravity(Gravity.CENTER);

                                TextView monto = new TextView(getContext());
                                monto.setLayoutParams(params);
                                monto.setTextColor(Color.parseColor("#000000"));
                                monto.setGravity(Gravity.CENTER);
                                /* ----------------------------------------- */


                                /* Se obtiene la lista de boletos de la ws y se compara con la lista de boletos de la BD interna */
                                for (int i = 0; i < getListaBoletos.length(); i++) {

                                    try {

                                        JSONObject info = getListaBoletos.getJSONObject(i);

                                        if (tipo.equals("viaje") &&
                                                jsonObject.getString("NumeroDocumento").equals(info.getString("NU_DOCU")) &&
                                                jsonObject.getString("Empresa").equals(info.getString("CO_EMPR")) &&
                                                jsonObject.getString("FechaDocumento").equals(info.getString("FE_DOCU")) &&
                                                info.getString("CO_ESTA_DOCU").equals("ACT") &&
                                                info.getString("ST_LIQI").equals("N")) {

                                            /*Log.d("boleto", info.getString("NU_DOCU"));
                                            Log.d("estado", info.getString("CO_ESTA_DOCU"));
                                            Log.d("empresa", info.getString("CO_EMPR"));
                                            Log.d("entro", "si");*/
                                            numDocumento.setText(jsonObject.getString("NumeroDocumento"));
                                            origen.setText(jsonObject.getString("OrigenBoleto"));
                                            destino.setText(jsonObject.getString("DestinoBoleto"));
                                            empresa.setText(jsonObject.getString("Empresa"));
                                            monto.setText(String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))));

                                            montoTotal += Float.valueOf(jsonObject.getString("Precio"));

                                            cantBoletos++;
                                            textView_cantBoletos.setText(Integer.toString(cantBoletos));
                                            editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
                                            editor.commit();

                                            listaBoletos.append("" + jsonObject.getString("NumeroDocumento") + "  " + jsonObject.getString("OrigenBoleto") + "  " + jsonObject.getString("DestinoBoleto") + " " + jsonObject.getString("Empresa") + " " + String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))) + "\n");

                                            /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                                            tableRow.addView(numDocumento);
                                            tableRow.addView(origen);
                                            tableRow.addView(destino);
                                            tableRow.addView(empresa);
                                            tableRow.addView(monto);

                                            tableLayout.addView(tableRow);
                                            /* ----------------------------------------- */

                                            break;

                                        } else if (tipo.equals("carga") &&
                                                jsonObject.getString("SerieCorrelativo").equals(info.getString("NU_DOCU")) &&
                                                jsonObject.getString("CodigoEmpresa").equals(info.getString("CO_EMPR")) &&
                                                jsonObject.getString("FechaDocumento").equals(info.getString("FE_DOCU")) &&
                                                info.getString("CO_ESTA_DOCU").equals("ACT") &&
                                                info.getString("ST_LIQI").equals("N")) {

                                            //Log.d("entro carga", "si");
                                            numDocumento.setText(jsonObject.getString("SerieCorrelativo"));
                                            origen.setText(jsonObject.getString("Origen"));
                                            destino.setText(jsonObject.getString("Destino"));
                                            empresa.setText(jsonObject.getString("CodigoEmpresa"));
                                            monto.setText(String.format("%.2f", Float.valueOf(jsonObject.getString("ImporteTotal"))));

                                            montoTotal += Float.valueOf(jsonObject.getString("ImporteTotal"));

                                            cantBoletos++;
                                            textView_cantBoletos.setText(Integer.toString(cantBoletos));
                                            editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
                                            editor.commit();

                                            listaBoletos.append("" + jsonObject.getString("SerieCorrelativo") + "  " + jsonObject.getString("Origen") + "  " + jsonObject.getString("Destino") + " " + jsonObject.getString("CodigoEmpresa") + " " + String.format("%.2f", Float.valueOf(jsonObject.getString("ImporteTotal"))) + "\n");

                                            /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                                            tableRow.addView(numDocumento);
                                            tableRow.addView(origen);
                                            tableRow.addView(destino);
                                            tableRow.addView(empresa);
                                            tableRow.addView(monto);

                                            tableLayout.addView(tableRow);
                                            /* ----------------------------------------- */

                                            break;

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                /* ----------------------------------------- */

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        /* ----------------------------------------- */

                        /* Se muestra el monto acumulado en pantalla y se guarda en memoria */
                        textView_montoTotal.setText(Float.toString(Float.valueOf(montoTotal)));
                        editor.putString("guardar_montoTotal", Float.toString(Float.valueOf(montoTotal)));
                        editor.commit();

                    }

                });
    }

    /**
     * Mapea el error del WS y muestra una vista de error.
     *
     * @param queue Contiene el queue del request.
     * @param error Determina el tipo de error de la Web Service.
     */
    private void errorWS(RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        } else if (error instanceof TimeoutError) {
            Toast.makeText(getActivity(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getActivity(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getActivity(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ParseError) {
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        // Intent intent = new Intent(getActivity(), ErrorActivity.class);
        // startActivity(intent);
    }

    public void MostrarDataOffline() {
        try {
            cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\"", null, null, null, null);
            textView_cantBoletos.setText(Integer.toString(cantBoletos));
            editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
            editor.commit();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    TableRow tableRow = new TableRow(getActivity());
                    tableRow.setId(0);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    tableRow.setPadding(10, 10, 10, 10);
                    tableRow.setClickable(true);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
                    TableRow.LayoutParams params1 = new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT, 1);
                    params.setMargins(0, 0, 5, 0);
                    TextView numDocumento = new TextView(getContext());
                    numDocumento.setLayoutParams(params);
                    numDocumento.setTextColor(Color.parseColor("#000000"));
                    numDocumento.setGravity(Gravity.CENTER);
                    TextView origen = new TextView(getContext());
                    origen.setLayoutParams(params);
                    origen.setTextColor(Color.parseColor("#000000"));
                    origen.setGravity(Gravity.CENTER);
                    TextView destino = new TextView(getContext());
                    destino.setLayoutParams(params);
                    destino.setTextColor(Color.parseColor("#000000"));
                    destino.setGravity(Gravity.CENTER);
                    TextView empresa = new TextView(getContext());
                    empresa.setLayoutParams(params);
                    empresa.setTextColor(Color.parseColor("#000000"));
                    empresa.setGravity(Gravity.CENTER);
                    TextView monto = new TextView(getContext());
                    monto.setLayoutParams(params);
                    monto.setTextColor(Color.parseColor("#000000"));
                    monto.setGravity(Gravity.CENTER);
                    try {

                        numDocumento.setText(jsonObject.getString("NumeroDocumento"));
                        origen.setText(jsonObject.getString("OrigenBoleto"));
                        destino.setText(jsonObject.getString("DestinoBoleto"));
                        empresa.setText(jsonObject.getString("Empresa"));
                        monto.setText(String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))));

                        montoTotal += Float.valueOf(jsonObject.getString("Precio"));

                        cantBoletos++;
                        textView_cantBoletos.setText(Integer.toString(cantBoletos));
                        editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
                        editor.commit();

                        listaBoletos.append("" + jsonObject.getString("NumeroDocumento") + "  " + jsonObject.getString("OrigenBoleto") + "  " + jsonObject.getString("DestinoBoleto") + " " + jsonObject.getString("Empresa") + " " + String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))) + "\n");

                        /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                        tableRow.addView(numDocumento);
                        tableRow.addView(origen);
                        tableRow.addView(destino);
                        tableRow.addView(empresa);
                        tableRow.addView(monto);

                        tableLayout.addView(tableRow);
                        /* ----------------------------------------- */

                        //break;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            textView_montoTotal.setText(Float.toString(Float.valueOf(montoTotal)));
            editor.putString("guardar_montoTotal", Float.toString(Float.valueOf(montoTotal)));
            editor.commit();


        } catch (Exception ex) {
            Log.d("MostrarDataOffline", ex.getMessage());
        }
    }
}




