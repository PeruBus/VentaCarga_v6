package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import android.widget.AdapterView;
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

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;



public class PreLiquidacionFragment extends Fragment {

    ProgressDialog progressDialog;
    int FlagValidaButton=0;
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
    Button button_imprimir;
    /**
     * Instancia para guardar datos en memoria.
     */
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preliquidacion, parent, false);
    }

	/**
	* Implementación de la lógica que muestra los boletos vendidos, calcula la pre liquidación y la imprime.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Inicialización de la instancia para guardar datos en memoria */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /* ----------------------------------------- */


        TableLayout tableLayout = view.findViewById(R.id.table_layout);
        final TextView textView_montoTotal = view.findViewById(R.id.textView_montoTotal);
        final TextView textView_cantBoletos = view.findViewById(R.id.textView_cantBoletos);

        //Button button_imprimir = view.findViewById(R.id.button_imprimir);
        button_imprimir = view.findViewById(R.id.button_imprimir);
        Button button_liberarLiquidacion = view.findViewById(R.id.button_liberarLiquidacion);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final RequestQueue queue = Volley.newRequestQueue(getActivity());

        final StringBuilder listaBoletos = new StringBuilder();

        float montoTotal = 0;
        int cantBoletos = 0;


        /* Inicialización de la base de datos */
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        /* ----------------------------------------- */

        /* Se obtienen los boletos según el rol */
        Cursor cursor;
        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){

            /* Se obtiene el JSON Array de los tramos */
            try {

                getListaBoletos = new JSONArray(sharedPreferences.getString("listaBoletos", "NoData"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            /* ----------------------------------------- */

            cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"boletero\"", null, null,null,null);

        }else{
            cursor = sqLiteDatabase.query("VentaBoletos", null, "puesto=\"anfitrion\"", null, null,null,null);

        }
        /* ----------------------------------------- */

        /* Se obtiene la cantidad de boletos, se muestra en pantalla y se guarda en memoria */
        cantBoletos = cursor.getCount();
        textView_cantBoletos.setText(Integer.toString(cantBoletos));
        editor.putString("guardar_cantBoletos", Integer.toString(cantBoletos));
        editor.commit();
        /* ----------------------------------------- */

        /* Iteración en función a la cantidad filas obtenidas en el query */
        while(cursor.moveToNext()){

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
                params.setMargins(0, 0, 10, 0);
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

                TextView monto = new TextView(getContext());

                monto.setLayoutParams(params);
                monto.setTextColor(Color.parseColor("#000000"));
                monto.setGravity(Gravity.CENTER);

                if (tipo.equals("viaje")){

                    numDocumento.setText(jsonObject.getString("NumeroDocumento"));
                    origen.setText(jsonObject.getString("OrigenBoleto"));
                    destino.setText(jsonObject.getString("DestinoBoleto"));
                    monto.setText(String.format("%.2f", Float.valueOf(jsonObject.getString("Precio"))));

                    montoTotal += Float.valueOf(jsonObject.getString("Precio"));

                    listaBoletos.append(""+jsonObject.getString("NumeroDocumento")+"    "+jsonObject.getString("OrigenBoleto")+"   "+jsonObject.getString("DestinoBoleto")+"  "+String.format("%.2f", Float.valueOf(jsonObject.getString("Precio")))+"\n");

                } else if (tipo.equals("carga")){

                    numDocumento.setText(jsonObject.getString("SerieCorrelativo"));
                    origen.setText(jsonObject.getString("Origen"));
                    destino.setText(jsonObject.getString("Destino"));
                    monto.setText(String.format("%.2f", Float.valueOf(jsonObject.getString("ImporteTotal"))));

                    montoTotal += Float.valueOf(jsonObject.getString("ImporteTotal"));

                    listaBoletos.append(""+jsonObject.getString("SerieCorrelativo")+"    "+jsonObject.getString("Origen")+"   "+jsonObject.getString("Destino")+"  "+String.format("%.2f", Float.valueOf(jsonObject.getString("ImporteTotal")))+"\n");

                }
                /* ----------------------------------------- */


                /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                tableRow.addView(numDocumento);
                tableRow.addView(origen);
                tableRow.addView(destino);
                tableRow.addView(monto);

                tableLayout.addView(tableRow);
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
        /* ----------------------------------------- */

        /* TODO: CLICK LISTENER DEL BOTÓN IMPRIMIR PRE LIQUIDACIÓN */
        button_imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FlagValidaButton==0)
                {
                    FlagValidaButton=1;
                /* Validación en caso no haya ningún boleto en pre liquidación */
                if(!textView_cantBoletos.getText().toString().equals("0") && !textView_montoTotal.getText().toString().equals("0.0")){
                    //imprimir_preLiquidacion(listaBoletos, sharedPreferences);
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Espere por favor");
                    progressDialog.setMessage("Imprimiendo");
                    progressDialog.setCancelable(false);
                    /* Hilo secundario que ejecuta todos los requests mientras está activado el cuadro de espera */
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                imprimir_preLiquidacion(listaBoletos, sharedPreferences);
                                button_imprimir.setEnabled(true);
                                FlagValidaButton=0;
                                progressDialog.dismiss();
                                //Log.d("PRELIUIDACION_IMPRIME", "IMPRIMIENO");

                            }catch (Exception e)
                            {
                                //Log.d("error",e.toString());
                                //button_imprimir.setEnabled(true);
                                progressDialog.dismiss();
                            }
                        }
                    });
                    /* ----------------------------------------- */
                    thread.start();
                    progressDialog.show();

                }else{
                    FlagValidaButton=0;
                    Toast.makeText(getActivity(), "No hay boletos para imprimir", Toast.LENGTH_SHORT).show();
                }
                /* ----------------------------------------- */
            }
            }
        });
        /* ----------------------------------------- */

        /* TODO: CLICK LISTENER DEL BOTÓN LIBERAR PRE LIQUIDACIÓN */
        button_liberarLiquidacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Ruta de la Web service dependiendo si es Anfitrión o Boletero */
                String ws_getLiberarLiquidacion;
                if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){

                    Date date = new Date();
                    String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(date);
                    ws_getLiberarLiquidacion = getString(R.string.ws_ruta) + "ValidaLiquidacion/" + fechaActual + "/~/~/~/" +
                            sharedPreferences.getString("puestoUsuario", "NoData") + "/" + sharedPreferences.getString("codigoUsuario", "NoData");
                    Log.d("Liqui_bol",ws_getLiberarLiquidacion);

                }else {

                    ws_getLiberarLiquidacion = getString(R.string.ws_ruta) + "ValidaLiquidacion/" + sharedPreferences.getString("anf_fechaProgramacion", "NoData") + "/" +
                            sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" + sharedPreferences.getString("anf_secuencia", "NoData") + "/" +
                            sharedPreferences.getString("anf_rumbo", "NoData") + "/" + sharedPreferences.getString("puestoUsuarioCompleto", "NoData") + "/" +
                            sharedPreferences.getString("codigoUsuario", "NoData");

                    Log.d("Liqui_anfi",ws_getLiberarLiquidacion);
                }
                /* ----------------------------------------- */

                /* Request que obtiene la respuesta para liberar la BD */
                JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getLiberarLiquidacion, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length() == 1) {
                                    try {
                                        JSONObject info;
                                        info = response.getJSONObject(0);

                                        /* Validación de la respuesta en caso de ser true */
                                        if(info.getString("Respuesta").equals("true")){

                                            /* Se inicilaiza la BD y se elimina la data de la tabla */
                                            ventaBlt = new DatabaseBoletos(getActivity());
                                            sqLiteDatabase = ventaBlt.getWritableDatabase();
                                            sqLiteDatabase.delete("VentaBoletos", null, null);
                                            /* ----------------------------------------- */

                                            /* Se actualiza la vista de pre liquidación */
                                            PreLiquidacionFragment preLiquidacionFragment = new PreLiquidacionFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, preLiquidacionFragment).commit();
                                            /* ----------------------------------------- */

                                        } else {
                                            Toast.makeText(getActivity(), "Aún no se puede liberar la BD.", Toast.LENGTH_SHORT).show();
                                        }
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
                        Toast.makeText(getActivity(), "Error en la ws getLiberarPreliquidacion.", Toast.LENGTH_SHORT).show();
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
            }
        });
        /* ----------------------------------------- */
    }

	/**
	* Imprime el voucher de pre liquidación.
	* @param listaBoletos Lista de boletos vendidos por el Anfitrión o Boletero.
	* @param sharedPreferences
	*/
    public void imprimir_preLiquidacion(StringBuilder listaBoletos, SharedPreferences sharedPreferences) {

        Date date = new Date();
        String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

        /* Se pasan todos los valores necesarios para generar la estructura de la boleta que se va a imprimir */
        PreLiquidacion preLiquidacion = new PreLiquidacion(listaBoletos);
        preLiquidacion.setCantBoletos(sharedPreferences.getString("guardar_cantBoletos", "NoData"));
        preLiquidacion.setMontoTotal(sharedPreferences.getString("guardar_montoTotal", "NoData"));
        preLiquidacion.setNombreAnfitrion(sharedPreferences.getString("nombreEmpleado","NoData"));
        preLiquidacion.setCodigoVehiculo(sharedPreferences.getString("anf_codigoVehiculo","NoData"));
        preLiquidacion.setRumbo(sharedPreferences.getString("anf_rumbo","NoData"));
        preLiquidacion.setFecha(FechaImpresion);
        /* ----------------------------------------- */

        try {

            /* Se inicializa la impresora del equipo
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
            IPrinter printer = dal.getPrinter();
            printer.init();


            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            printer.printStr(preLiquidacion.getVoucher(), null);



            printer.printStr(preLiquidacion.margenFinal(), null);


            int iRetError = printer.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {
                }
            }*/
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
            IPrinter printer = dal.getPrinter();
            printer.init();
            /* ----------------------------------------- */

            String[] boletos = preLiquidacion.getVoucher().toString().split("\n");
            //Log.d("tamano", Integer.toString(boletos.length));

            for (int i = 0; i < boletos.length; i++) {
                printer.printStr(boletos[i]+"\n", null);

                if (i%100 == 0) {
                    int iRetError = printer.start();
                    if (iRetError != 0x00) {
                        //Log.d("Impresora", "ERROR:"+iRetError);
                    }
                    printer.init();
                }
            }

            /* TEXTO */
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            /* ----------------------------------------- */

            /* Margen final */
            printer.printStr(preLiquidacion.margenFinal(), null);
            /* ----------------------------------------- */

            int iRetError = printer.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {
                    //TODO mensaje de falta de papel
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al imprimir el voucher de pre liquidación.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }
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
