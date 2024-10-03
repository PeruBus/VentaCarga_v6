package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

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
public class BoleteroPreLiquidacionFragment extends Fragment {
    ProgressDialog progressDialog;
    RequestQueue queue;
    int FlagValidaButton=0;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    JSONArray getListaBoletos = null;
    private SharedPreferences sharedPreferences;
    float montoTotal = 0;
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
        return inflater.inflate(R.layout.preliquidacion, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tableLayout = view.findViewById(R.id.table_layout);
        textView_montoTotal = view.findViewById(R.id.textView_montoTotal);
        textView_cantBoletos = view.findViewById(R.id.textView_cantBoletos);
        button_imprimir = view.findViewById(R.id.button_imprimir);
        button_liberarLiquidacion = view.findViewById(R.id.button_liberarLiquidacion);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();
        queue = Volley.newRequestQueue(getActivity());
        listaBoletos = new StringBuilder();
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        ConnectivityManager cm =(ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            getListaBoletosWS(queue, sharedPreferences);
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MostrarDataOffline();
                }
            });
        }
        button_imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FlagValidaButton==0)
                {
                    FlagValidaButton=1;
                        if(!textView_cantBoletos.getText().toString().equals("0") && !textView_montoTotal.getText().toString().equals("0.0")){
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setTitle("Espere por favor");
                            progressDialog.setMessage("Imprimiendo");
                            progressDialog.setCancelable(false);
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        imprimir_preLiquidacion(listaBoletos, sharedPreferences);
                                        button_imprimir.setEnabled(true);
                                        FlagValidaButton=0;
                                        progressDialog.dismiss();
                                    }catch (Exception e)
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                            thread.start();
                            progressDialog.show();
                        }else{
                            FlagValidaButton=0;
                            Toast.makeText(getActivity(), "No hay boletos para imprimir", Toast.LENGTH_SHORT).show();
                        }
                    }
                 }
        });
        button_liberarLiquidacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ws_getLiberarLiquidacion;
                if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
                    Date date = new Date();
                    String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(date);
                    ws_getLiberarLiquidacion = getString(R.string.ws_ruta) + "ValidaLiquidacion/" + fechaActual + "/~/~/~/" +
                            sharedPreferences.getString("Modulo", "NoData") + "/" + sharedPreferences.getString("codigoUsuario", "NoData");
                }
                else {
                    ws_getLiberarLiquidacion = getString(R.string.ws_ruta) + "ValidaLiquidacion/" + sharedPreferences.getString("anf_fechaProgramacion", "NoData") + "/" +
                            sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" + sharedPreferences.getString("anf_secuencia", "NoData") + "/" +
                            sharedPreferences.getString("anf_rumbo", "NoData") + "/" + sharedPreferences.getString("puestoUsuarioCompleto", "NoData") + "/" +
                            sharedPreferences.getString("codigoUsuario", "NoData");
                }
                final RequestQueue queue = Volley.newRequestQueue(getActivity());
                JsonArrayRequest jsonArrayLiberarLiquidacion = new JsonArrayRequest(Request.Method.GET, ws_getLiberarLiquidacion, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length() == 1) {
                                    try {
                                        JSONObject info;
                                        info = response.getJSONObject(0);
                                        if(info.getString("Respuesta").equals("true")){
                                            Toast.makeText(getActivity(), "Se recibe confirmación de la ws. Se procede a liberar la BD.", Toast.LENGTH_LONG).show();
                                            ventaBlt = new DatabaseBoletos(getActivity());
                                            sqLiteDatabase = ventaBlt.getWritableDatabase();
                                            sqLiteDatabase.delete("VentaBoletos", null, null);
                                            BoleteroPreLiquidacionFragment preLiquidacionFragment = new BoleteroPreLiquidacionFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, preLiquidacionFragment).commit();
                                        } else {
                                            Toast.makeText(getActivity(), "Aún no se puede liberar la BD.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity(), "Error en la ws getLiberarPreliquidacion.", Toast.LENGTH_LONG).show();
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
                jsonArrayLiberarLiquidacion.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonArrayLiberarLiquidacion);
            }
        });
    }
    public void imprimir_preLiquidacion(StringBuilder listaBoletos, SharedPreferences sharedPreferences) {
        PreLiquidacion preLiquidacion = new PreLiquidacion(listaBoletos);
        preLiquidacion.setCantBoletos(sharedPreferences.getString("guardar_cantBoletos", "NoData"));
        preLiquidacion.setMontoTotal(sharedPreferences.getString("guardar_montoTotal", "NoData"));
        preLiquidacion.setNombreAnfitrion(sharedPreferences.getString("nombreEmpleado","NoData"));
        try {
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
            IPrinter printer = dal.getPrinter();
            printer.init();
            String[] boletos = preLiquidacion.getVoucher().toString().split("\n");
            for (int i = 0; i < boletos.length; i++) {
                printer.printStr(boletos[i]+"\n", null);
                if (i%100 == 0) {
                    int iRetError = printer.start();
                    if (iRetError != 0x00) {
                    }
                    printer.init();
                }
            }
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            printer.printStr(preLiquidacion.margenFinal(), null);
            int iRetError = printer.start();
            if (iRetError != 0x00) {
                if (iRetError == 0x02) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getResponse(RequestQueue queue, String url, final VolleyCallbackInterface volleyCallbackInterface) {
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            volleyCallbackInterface.onSuccessResponse(response.toString(), true);
                        }else if(response.length() > 0){
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
        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestVenta);
    }
    public void getListaBoletosWS(final RequestQueue queue, final SharedPreferences sharedPreferences) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Espere por favor");
        progressDialog.setMessage("Cargando Pre Liquidacion");
        progressDialog.setCancelable(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
                String ws_preLiquidacion = getString(R.string.ws_ruta) + "PreLiquidacionBoletero/" + sharedPreferences.getString("codigoUsuario", "NoData") + "/" + fechaVenta;
                Log.d("asientos vendidos", ws_preLiquidacion);
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
                                String Tipo = sharedPreferences.getString("Empresa","0");
                                cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\" and co_empr=\""+Tipo +"\"", null, null,null,null);
                                textView_cantBoletos.setText(Integer.toString(cantBoletos));
                                editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
                                editor.commit();
                                while(cursor.moveToNext()){
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
                                        for (int i = 0; i < getListaBoletos.length(); i++) {
                                            try {
                                                JSONObject info = getListaBoletos.getJSONObject(i);
                                                if (tipo.equals("viaje") &&
                                                        jsonObject.getString("NumeroDocumento").equals(info.getString("NU_DOCU")) &&
                                                        jsonObject.getString("Empresa").equals(info.getString("CO_EMPR")) &&
                                                        jsonObject.getString("FechaDocumento").equals(info.getString("FE_DOCU")) &&
                                                        info.getString("CO_ESTA_DOCU").equals("ACT") &&
                                                        info.getString("ST_LIQI").equals("N")) {
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
                                                    listaBoletos.append("" + jsonObject.getString("NumeroDocumento") + "  " + jsonObject.getString("OrigenBoleto") + "  " + jsonObject.getString("DestinoBoleto") +" "+ jsonObject.getString("Empresa") + " " + String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))) + "\n");
                                                    tableRow.addView(numDocumento);
                                                    tableRow.addView(origen);
                                                    tableRow.addView(destino);
                                                    tableRow.addView(empresa);
                                                    tableRow.addView(monto);
                                                    tableLayout.addView(tableRow);
                                                    break;
                                                } else if (tipo.equals("carga") &&
                                                        jsonObject.getString("SerieCorrelativo").equals(info.getString("NU_DOCU")) &&
                                                        jsonObject.getString("CodigoEmpresa").equals(info.getString("CO_EMPR")) &&
                                                        jsonObject.getString("FechaDocumento").equals(info.getString("FE_DOCU")) &&
                                                        info.getString("CO_ESTA_DOCU").equals("ACT") &&
                                                        info.getString("ST_LIQI").equals("N")) {
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
                                                    tableRow.addView(numDocumento);
                                                    tableRow.addView(origen);
                                                    tableRow.addView(destino);
                                                    tableRow.addView(empresa);
                                                    tableRow.addView(monto);
                                                    tableLayout.addView(tableRow);
                                                    break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                textView_montoTotal.setText(Float.toString(Float.valueOf(montoTotal)));
                                editor.putString("guardar_montoTotal", Float.toString(Float.valueOf(montoTotal)));
                                editor.commit();
                                progressDialog.dismiss();
                            }

                        });
            }
        });
        thread.start();
        progressDialog.show();
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
    }
    public void MostrarDataOffline()
    {
        try {
            cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\"", null, null,null,null);
            textView_cantBoletos.setText(Integer.toString(cantBoletos));
            editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
            editor.commit();
            while(cursor.moveToNext()){
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
                                listaBoletos.append("" + jsonObject.getString("NumeroDocumento") + "  " + jsonObject.getString("OrigenBoleto") + "  " + jsonObject.getString("DestinoBoleto") +" "+ jsonObject.getString("Empresa") + " " + String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))) + "\n");
                                tableRow.addView(numDocumento);
                                tableRow.addView(origen);
                                tableRow.addView(destino);
                                tableRow.addView(empresa);
                                tableRow.addView(monto);
                                tableLayout.addView(tableRow);
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
        }catch (Exception ex){
            Log.d("MostrarDataOffline", ex.getMessage());
        }
    }
}
