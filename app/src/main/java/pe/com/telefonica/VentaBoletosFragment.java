package pe.com.telefonica.soyuz;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.com.telefonica.soyuz.gps.*;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.generarCodigoQR;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarIntegerMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;
import static pe.com.telefonica.soyuz.gps.Utils.getDistanceText;


public class VentaBoletosFragment extends Fragment {
    int FlagValidaButton=0;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    public static final String SOLICITA_TED = "ted";
    public static final String PATH_XML = "xml";
    public static final String COLUMN_NAME_XML64 = "xml64";
    public static final String COLUMN_NAME_TED = "ted";
    public static final String COLUMN_NAME_TED64 = "ted64";
    private static final String TAG = "GPS";
    Context context;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private MyReceiver myReceiver;
    private LocationUpdatesService mService = null;
    private boolean mBound = false;
    private boolean flag = false;
    private IDAL dal;
    private IPrinter printer;
    Dialog boleto_dialog;
    Context context_boleto;
    TextView TextView_RasonSocial;
    TextView TextView_RUC;
    EditText editText_tarifa;
    EditText editText_nombreCliente;
    EditText editText_dni;
    EditText editText_RazonSocial;
    EditText editText_RUC;
    Spinner spinner_origen;
    Spinner spinner_destino;
    static Spinner spinner_TipoDocumento;
    Button button_imprimirBoleto;
    Button button_cancelarVentaboleto;
    Dialog carga_dialog;
    Context context_boletoCarga;
    EditText editText_origen;
    EditText editText_destino;
    EditText editText_dniCarga;
    EditText editText_tarifaBase;
    EditText editText_tarifaCarga;
    EditText editText_cantidad;
    Button button_CancelaCarga;
    Spinner spinner_tipoProducto;
    Button button_imprimirBoletoCarga;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson;
    String empresa_seleccionada = "";
    int numCorrelativoSeleccionado = 0;
    String numSerieSeleccionado = "";
    ArrayList<String> arrayPosicionAsientos;
    ArrayList<String> lista_asientosVendidos;
    ArrayList<String> lista_asientosVendidosRuta;
    GridView gridviewAsientos;
    int position;
    ArrayList<String> lista_productos;
    int numCorrelativoCargaSeleccionado = 0;
    String numSerieCargaSeleccionado = "";
    ProgressDialog progressDialog;
    ProgressDialog progressDialog2;
    static Spinner spinner = null;
    EditText editText_Tarifa_Adicional_SE;
    TextView TextView_adicional_Vip;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            //startLocationUpdate();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();
        editor = sharedPreferences.edit();
        return inflater.inflate(R.layout.venta_boletos, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        context = getContext();
        try {
            dal = NeptuneLiteUser.getInstance().getDal(context);
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
        } catch (Exception e) {
            //Log.e("IMPRESORA", "No se puede inicializar la impresora");
        }
        context.bindService(new Intent(context, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        myReceiver = new MyReceiver();
        if (Utils.requestingLocationUpdates(context)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Gson gson = new Gson();
        boleto_dialog = new Dialog(getActivity());
        boleto_dialog.setContentView(R.layout.boleto_dialog);
        context_boleto = boleto_dialog.getContext();
        editText_tarifa = boleto_dialog.findViewById(R.id.editText_tarifa);
        editText_nombreCliente = boleto_dialog.findViewById(R.id.editText_nombreCliente);
        editText_dni = boleto_dialog.findViewById(R.id.editText_dni);
        spinner_origen = boleto_dialog.findViewById(R.id.spinner_origen);
        spinner_destino = boleto_dialog.findViewById(R.id.spinner_destino);
        button_imprimirBoleto = boleto_dialog.findViewById(R.id.button_imprimirBoleto);
        button_cancelarVentaboleto = boleto_dialog.findViewById(R.id.button_cancelarVentaboleto);
        TextView_RasonSocial=boleto_dialog.findViewById(R.id.textView7);
        TextView_RUC=boleto_dialog.findViewById(R.id.textView8);
        spinner_TipoDocumento = boleto_dialog.findViewById(R.id.spinner_tipoDocu);
        editText_RazonSocial = boleto_dialog.findViewById(R.id.editText_RazonSocial);
        editText_RUC = boleto_dialog.findViewById(R.id.editText_RUC);
        TextView_RasonSocial.setVisibility(View.INVISIBLE);
        TextView_RUC.setVisibility(View.INVISIBLE);
        editText_RazonSocial.setVisibility(View.INVISIBLE);
        editText_RUC.setVisibility(View.INVISIBLE);
        carga_dialog = new Dialog(getActivity());
        carga_dialog.setContentView(R.layout.carga_dialog);
        context_boletoCarga = carga_dialog.getContext();
        editText_origen = carga_dialog.findViewById(R.id.editText_origenCarga);
        editText_destino = carga_dialog.findViewById(R.id.editText_destinoCarga);
        editText_dniCarga = carga_dialog.findViewById(R.id.editText_dniCarga);
        editText_tarifaBase = carga_dialog.findViewById(R.id.editText_tarifaBase);
        editText_tarifaCarga = carga_dialog.findViewById(R.id.editText_tarifaCarga);
        editText_cantidad = carga_dialog.findViewById(R.id.editText_cantidad);
        button_CancelaCarga = carga_dialog.findViewById(R.id.button_cancelarVentaCarga);
        spinner_tipoProducto = carga_dialog.findViewById(R.id.spinner_tipoProducto);
        button_imprimirBoletoCarga = carga_dialog.findViewById(R.id.button_imprimirBoletoCarga);
        TextView_adicional_Vip = boleto_dialog.findViewById(R.id.TextView_adicional_Vip);
        editText_Tarifa_Adicional_SE= boleto_dialog.findViewById(R.id.editText_tarifa_adicional_vip);
        TextView_adicional_Vip.setVisibility(View.GONE);
        editText_Tarifa_Adicional_SE.setVisibility(View.GONE);
        gridviewAsientos = view.findViewById(R.id.gridview);
        gridviewAsientos.setNumColumns(sharedPreferences.getInt("anf_numCol", 1) - 1);
        float numCol = sharedPreferences.getInt("anf_numCol", 1) - 1;
        float numAsientos = sharedPreferences.getInt("anf_numAsientos", 1);
        float numFilasAprox = numAsientos / numCol;
        int numFilas = (int) numFilasAprox;
        int numFilasEntero = (int) numFilasAprox;
        float parteDecimal = numFilasAprox - numFilasEntero;
        if (parteDecimal > 0) {
            numFilasEntero = numFilasEntero + 1;
        }
        arrayPosicionAsientos = new ArrayList<>();
        int counter = 0;
        int posicion = 1;
        int col3 = 3;
        int col4 = 4;
        while (counter < numFilasEntero) {
            for (int i = 1; i <= numCol; i++) {
                if (posicion == (col3 + 4 * counter)) {
                    arrayPosicionAsientos.add(Integer.toString(posicion + 1));
                } else if (posicion == (col4 + 4 * counter)) {
                    arrayPosicionAsientos.add(Integer.toString(posicion - 1));
                } else {
                    arrayPosicionAsientos.add(Integer.toString(posicion));
                }
                posicion++;
            }
            counter++;
        }
        lista_asientosVendidos = getArray(sharedPreferences, gson, "anf_jsonReporteVenta_");
        lista_asientosVendidosRuta = getArray(sharedPreferences, gson, "anf_jsonReporteVentaRuta_");
        final ArrayList<String> lista_nombreDestino = new ArrayList<>();
        final ArrayList<String> lista_idDestino = new ArrayList<>();
        gridviewAsientos.setAdapter(new ImageAdapter(getActivity(), sharedPreferences.getInt("anf_numAsientos", 0),
                lista_asientosVendidosRuta, lista_asientosVendidos, sharedPreferences.getString("puestoUsuario", "NoData"), arrayPosicionAsientos, numFilas, numCol));
        startBoletoService();






//        gridviewAsientog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v,
//                                    final int posicion, long id) {
//
//            });
//





















        gridviewAsientos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    final int posicion, long id) {





                if(sharedPreferences.getString("ValidaProgramacion","NoData").equals("NoData")) {
                    final ProgressDialog progressDialog_valida = new ProgressDialog(getActivity());
                    progressDialog_valida.setMessage("Loading...");
                    progressDialog_valida.show();
                    progressDialog_valida.setCancelable(false);
                    progressDialog_valida.setCanceledOnTouchOutside(false);
//                    final RequestQueue queue = Volley.newRequestQueue(getContext());
//                    String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/" + sharedPreferences.getString("CodUsuario", "NoData") + "/Puesto/RELOGIN";
//                    Log.d("ValidaVenta", ws_getItinerario);
//                    JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
//                            new Response.Listener<JSONArray>() {
//                                @Override
//                                public void onResponse(JSONArray response) {
//                                    if (response.length() == 1) {
//                                        try {
//                                            guardarDataMemoria("ValidaProgramacion", "true", getActivity());
//                                            JSONObject RequestJson = response.getJSONObject(0);
//                                            String fecha = RequestJson.getString("FECHA_PROGRAMACION");
//                                            fecha = fecha.substring(0, 10);
//                                            if (sharedPreferences.getString("anf_fechaProgramacion", "NoData").equals(fecha) &&
//                                                    sharedPreferences.getString("anf_codigoEmpresa", "NoData").equals(RequestJson.getString("CODIGO_EMPRESA")) &&
//                                                    sharedPreferences.getString("anf_codigoVehiculo", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO")) &&
//                                                    sharedPreferences.getString("anf_horaSalida", "NoData").equals(RequestJson.getString("HORA_SALIDA")) &&
//                                                    sharedPreferences.getString("anf_servicio", "NoData").equals(RequestJson.getString("SERVICIO")) &&
//                                                    sharedPreferences.getString("anf_origen", "NoData").equals(RequestJson.getString("ORIGEN")) &&
//                                                    sharedPreferences.getString("anf_destino", "NoData").equals(RequestJson.getString("DESTINO")) &&
//                                                    sharedPreferences.getString("anf_secuencia", "NoData").equals(RequestJson.getString("SECUENCIA")) &&
//                                                    sharedPreferences.getString("anf_tipoBus", "NoData").equals(RequestJson.getString("TIPO_BUS")) &&
//                                                    sharedPreferences.getString("anf_rumbo", "NoData").equals(RequestJson.getString("RUMBO")) &&
//                                                    sharedPreferences.getString("anf_nombre", "NoData").equals(RequestJson.getString("ANFITRION")) &&
//                                                    sharedPreferences.getString("CO_VEHI_anfi", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO"))
//
//                                            ) {
                                                progressDialog_valida.dismiss();
                                                position = posicion;
                                                flag = true;
                                                editText_nombreCliente.getText().clear();
                                                editText_dni.getText().clear();
                                                editText_tarifaCarga.getText().clear();
                                                final ArrayList<String> Tarifario = getArray(sharedPreferences,gson,"anf_jsonTarifasViaje");
                                                final ArrayList<String> lista_destinos = getArray(sharedPreferences,gson, "json_destinos");
                                                CargaDestinos(sharedPreferences,gson,Tarifario,lista_destinos);
                                                CargaTipoDocumento();
                                                spinner_TipoDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Spinner_model st = (Spinner_model)spinner_TipoDocumento.getSelectedItem();
                                                        editText_RazonSocial.setText("");
                                                        editText_RUC.setText("");
                                                        editText_dni.setText("");
                                                        editText_nombreCliente.setText("");
                                                        if (st.id=="3")
                                                        {
                                                            TextView_RasonSocial.setVisibility(View.VISIBLE);
                                                            TextView_RUC.setVisibility(View.VISIBLE);
                                                            editText_RazonSocial.setVisibility(View.VISIBLE);
                                                            editText_RUC.setVisibility(View.VISIBLE);
                                                            editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                                                        }else if(st.id=="4" || st.id=="5"){
                                                            TextView_RasonSocial.setVisibility(View.INVISIBLE);
                                                            TextView_RUC.setVisibility(View.INVISIBLE);
                                                            editText_RazonSocial.setVisibility(View.INVISIBLE);
                                                            editText_RUC.setVisibility(View.INVISIBLE);
                                                            editText_dni.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                                                        }

                                                        else{
                                                            TextView_RasonSocial.setVisibility(View.INVISIBLE);
                                                            TextView_RUC.setVisibility(View.INVISIBLE);
                                                            editText_RazonSocial.setVisibility(View.INVISIBLE);
                                                            editText_RUC.setVisibility(View.INVISIBLE);
                                                            editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                                                        }

                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {}
                                                });
                                                spinner_origen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Spinner_model st = (Spinner_model)spinner_origen.getSelectedItem();
                                                        final List<Spinner_model> Destino = new ArrayList<>();
                                                        Spinner_model CO_DEST_FINA = new Spinner_model("999", "SELECCIONAR", "");
                                                        Destino.add(CO_DEST_FINA);
                                                        final ArrayList<String>  DEST_TRAMO_DEST = new ArrayList<>();
                                                        for (int i=0;i < Tarifario.size();i++)
                                                        {
                                                            String[] DESTINO_ID = Tarifario.get(i).toString().split("-");
                                                            if(DESTINO_ID[0].equals(String.valueOf(st.id)))
                                                            {
                                                                DEST_TRAMO_DEST.add(DESTINO_ID[1]);
                                                            }
                                                        }
                                                        final ArrayList<String> DestinosSpinner_DEST  =  FuncionesAuxiliares.removeDuplicates(DEST_TRAMO_DEST);
                                                        for (int j=0;j<DestinosSpinner_DEST.size();j++)
                                                        {
                                                            for(int h = 0;h<lista_destinos.size();h++)
                                                            {
                                                                String[] TTDEST = lista_destinos.get(h).split("-");
                                                                if (DestinosSpinner_DEST.get(j).toString().equals(TTDEST[0].toString()))
                                                                {
                                                                    Spinner_model CO_DEST_FINA_ = new Spinner_model(TTDEST[0], TTDEST[0] + "-" + TTDEST[1], "");
                                                                    Destino.add(CO_DEST_FINA_);
                                                                }
                                                            }
                                                        }
                                                        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                                                                android.R.layout.simple_spinner_item, Destino);
                                                        spinner_destino.setAdapter(spinnerArrayAdapter);
                                                        Spinner_model stdest = (Spinner_model)spinner_destino.getSelectedItem();
                                                        calculaTarifa(String.valueOf(st.id),String.valueOf(stdest.id), editText_tarifa, button_imprimirBoleto);
                                                    }
                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                    }
                                                });
                                                spinner_destino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Spinner_model CO_DEST_ORIG = (Spinner_model)spinner_origen.getSelectedItem();
                                                        Spinner_model CO_DEST_FINA = (Spinner_model)spinner_destino.getSelectedItem();
                                                        calculaTarifa(String.valueOf(CO_DEST_ORIG.id),String.valueOf(CO_DEST_FINA.id), editText_tarifa, button_imprimirBoleto);
                                                    }
                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                    }
                                                });
                                                button_imprimirBoleto.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //RealizaVenta();
//                                                        if(sharedPreferences.getString("ValidaProgramacion_2","NoData").equals("NoData")) {
//                                                            final ProgressDialog progressDialog_valida = new ProgressDialog(getActivity());
//                                                            progressDialog_valida.setMessage("Loading...");
//                                                            progressDialog_valida.show();
//                                                            progressDialog_valida.setCancelable(false);
//                                                            progressDialog_valida.setCanceledOnTouchOutside(false);
//                                                            final RequestQueue queue = Volley.newRequestQueue(getContext());
//                                                            String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/" + sharedPreferences.getString("CodUsuario", "NoData") + "/Puesto/RELOGIN";
//                                                            Log.d("ValidaVenta", ws_getItinerario);
//                                                            JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
//                                                                    new Response.Listener<JSONArray>() {
//                                                                        @Override
//                                                                        public void onResponse(JSONArray response) {
//                                                                            if (response.length() == 1) {
//                                                                                try {
//                                                                                    guardarDataMemoria("ValidaProgramacion_2", "true", getActivity());
//                                                                                    JSONObject RequestJson = response.getJSONObject(0);
//                                                                                    String fecha = RequestJson.getString("FECHA_PROGRAMACION");
//                                                                                    fecha = fecha.substring(0, 10);
//                                                                                    if (sharedPreferences.getString("anf_fechaProgramacion", "NoData").equals(fecha) &&
//                                                                                            sharedPreferences.getString("anf_codigoEmpresa", "NoData").equals(RequestJson.getString("CODIGO_EMPRESA")) &&
//                                                                                            sharedPreferences.getString("anf_codigoVehiculo", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO")) &&
//                                                                                            sharedPreferences.getString("anf_horaSalida", "NoData").equals(RequestJson.getString("HORA_SALIDA")) &&
//                                                                                            sharedPreferences.getString("anf_servicio", "NoData").equals(RequestJson.getString("SERVICIO")) &&
//                                                                                            sharedPreferences.getString("anf_origen", "NoData").equals(RequestJson.getString("ORIGEN")) &&
//                                                                                            sharedPreferences.getString("anf_destino", "NoData").equals(RequestJson.getString("DESTINO")) &&
//                                                                                            sharedPreferences.getString("anf_secuencia", "NoData").equals(RequestJson.getString("SECUENCIA")) &&
//                                                                                            sharedPreferences.getString("anf_tipoBus", "NoData").equals(RequestJson.getString("TIPO_BUS")) &&
//                                                                                            sharedPreferences.getString("anf_rumbo", "NoData").equals(RequestJson.getString("RUMBO")) &&
//                                                                                            sharedPreferences.getString("anf_nombre", "NoData").equals(RequestJson.getString("ANFITRION")) &&
//                                                                                            sharedPreferences.getString("CO_VEHI_anfi", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO"))
//
//                                                                                    ) {
//                                                                                        progressDialog_valida.dismiss();
//                                                                                        RealizaVenta();
//                                                                                    }
////                                                                                    else {
////                                                                                        progressDialog_valida.dismiss();
////                                                                                        Toast toast = Toast.makeText(getContext(),"HUBO UN CAMBIO EN LA PROGRAMACION LOGUEARSE NUEVAMENTE CON CODIGO DE CONDUCTOR", Toast.LENGTH_LONG);
////                                                                                        toast.setGravity(Gravity.CENTER, 50, 50);
////                                                                                        toast.show();
////                                                                                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
////                                                                                        SharedPreferences.Editor editor = sharedpreferences.edit();
////                                                                                        editor.clear();
////                                                                                        editor.commit();
////                                                                                        Intent intent = new Intent(getActivity(), ErrorActivity.class);
////                                                                                        startActivity(intent);
////                                                                                    }
//
//                                                                                } catch (Exception ex) {
//                                                                                    ex.printStackTrace();
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }, new Response.ErrorListener() {
//                                                                @Override
//                                                                public void onErrorResponse(VolleyError error) {
//                                                                    error.printStackTrace();
//                                                                    Toast.makeText(getActivity(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
//                                                                }
//                                                            }) {
//                                                                @Override
//                                                                public Map<String, String> getHeaders() throws AuthFailureError {
//                                                                    Map<String, String> headers = new HashMap<>();
//                                                                    String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                                                                    String auth = "Basic "
//                                                                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                                                                    headers.put("Content-Type", "application/json");
//                                                                    headers.put("Authorization", auth);
//                                                                    return headers;
//                                                                }
//                                                            };
//                                                            jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                                                            queue.add(jsonArrayRequestItinerario);
//
//                                                        }else{
                                                            RealizaVenta();
//                                                        }

                                                    }
                                                });
                                                button_cancelarVentaboleto.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        button_imprimirBoleto.setEnabled(true);
                                                        button_imprimirBoletoCarga.setEnabled(true);
                                                        flag = false;
                                                        boleto_dialog.dismiss();
                                                    }
                                                });
                                                boleto_dialog.show();
                                                boleto_dialog.setCancelable(false);

//                                            }
//                                            else {
//                                                progressDialog_valida.dismiss();
//                                                Toast toast = Toast.makeText(getContext(),"HUBO UN CAMBIO EN LA PROGRAMACION LOGUEARSE NUEVAMENTE CON CODIGO DE CONDUCTOR", Toast.LENGTH_LONG);
//                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                toast.show();
//                                                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                                                SharedPreferences.Editor editor = sharedpreferences.edit();
//                                                editor.clear();
//                                                editor.commit();
//                                                Intent intent = new Intent(getActivity(), ErrorActivity.class);
//                                                startActivity(intent);
//                                            }

//                                        }
//                                        catch (Exception ex) {
//                                            ex.printStackTrace();
//                                        }
//                                    }
//
//                                }
//                            }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            error.printStackTrace();
//                            Toast.makeText(getActivity(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
//                        }
//                    }) {
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            Map<String, String> headers = new HashMap<>();
//                            String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                            String auth = "Basic "
//                                    + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                            headers.put("Content-Type", "application/json");
//                            headers.put("Authorization", auth);
//                            return headers;
//                        }
//                    };
//                    jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                    queue.add(jsonArrayRequestItinerario);
                }else{
                    position = posicion;
                    flag = true;
                    editText_nombreCliente.getText().clear();
                    editText_dni.getText().clear();
                    editText_tarifaCarga.getText().clear();
                    final ArrayList<String> Tarifario = getArray(sharedPreferences,gson,"anf_jsonTarifasViaje");
                    final ArrayList<String> lista_destinos = getArray(sharedPreferences,gson, "json_destinos");
                    CargaDestinos(sharedPreferences,gson,Tarifario,lista_destinos);
                    CargaTipoDocumento();
                    spinner_TipoDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Spinner_model st = (Spinner_model)spinner_TipoDocumento.getSelectedItem();
                            editText_RazonSocial.setText("");
                            editText_RUC.setText("");
                            editText_dni.setText("");
                            editText_nombreCliente.setText("");
                            if (st.id=="3")
                            {
                                TextView_RasonSocial.setVisibility(View.VISIBLE);
                                TextView_RUC.setVisibility(View.VISIBLE);
                                editText_RazonSocial.setVisibility(View.VISIBLE);
                                editText_RUC.setVisibility(View.VISIBLE);
                                editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }else if(st.id=="4" || st.id=="5"){
                                TextView_RasonSocial.setVisibility(View.INVISIBLE);
                                TextView_RUC.setVisibility(View.INVISIBLE);
                                editText_RazonSocial.setVisibility(View.INVISIBLE);
                                editText_RUC.setVisibility(View.INVISIBLE);
                                editText_dni.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                            }

                            else{
                                TextView_RasonSocial.setVisibility(View.INVISIBLE);
                                TextView_RUC.setVisibility(View.INVISIBLE);
                                editText_RazonSocial.setVisibility(View.INVISIBLE);
                                editText_RUC.setVisibility(View.INVISIBLE);
                                editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                    spinner_origen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Spinner_model st = (Spinner_model)spinner_origen.getSelectedItem();
                            final List<Spinner_model> Destino = new ArrayList<>();
                            Spinner_model CO_DEST_FINA = new Spinner_model("999", "SELECCIONAR", "");
                            Destino.add(CO_DEST_FINA);
                            final ArrayList<String>  DEST_TRAMO_DEST = new ArrayList<>();
                            for (int i=0;i < Tarifario.size();i++)
                            {
                                String[] DESTINO_ID = Tarifario.get(i).toString().split("-");
                                if(DESTINO_ID[0].equals(String.valueOf(st.id)))
                                {
                                    DEST_TRAMO_DEST.add(DESTINO_ID[1]);
                                }
                            }
                            final ArrayList<String> DestinosSpinner_DEST  =  FuncionesAuxiliares.removeDuplicates(DEST_TRAMO_DEST);
                            for (int j=0;j<DestinosSpinner_DEST.size();j++)
                            {
                                for(int h = 0;h<lista_destinos.size();h++)
                                {
                                    String[] TTDEST = lista_destinos.get(h).split("-");
                                    if (DestinosSpinner_DEST.get(j).toString().equals(TTDEST[0].toString()))
                                    {
                                        Spinner_model CO_DEST_FINA_ = new Spinner_model(TTDEST[0], TTDEST[0] + "-" + TTDEST[1], "");
                                        Destino.add(CO_DEST_FINA_);
                                    }
                                }
                            }
                            ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                                    android.R.layout.simple_spinner_item, Destino);
                            spinner_destino.setAdapter(spinnerArrayAdapter);
                            Spinner_model stdest = (Spinner_model)spinner_destino.getSelectedItem();
                            calculaTarifa(String.valueOf(st.id),String.valueOf(stdest.id), editText_tarifa, button_imprimirBoleto);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    spinner_destino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Spinner_model CO_DEST_ORIG = (Spinner_model)spinner_origen.getSelectedItem();
                            Spinner_model CO_DEST_FINA = (Spinner_model)spinner_destino.getSelectedItem();
                            calculaTarifa(String.valueOf(CO_DEST_ORIG.id),String.valueOf(CO_DEST_FINA.id), editText_tarifa, button_imprimirBoleto);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    button_imprimirBoleto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(sharedPreferences.getString("ValidaProgramacion_2","NoData").equals("NoData")) {
                                    final ProgressDialog progressDialog_valida = new ProgressDialog(getActivity());
                                    progressDialog_valida.setMessage("Loading...");
                                    progressDialog_valida.show();
                                    progressDialog_valida.setCancelable(false);
                                    progressDialog_valida.setCanceledOnTouchOutside(false);
//                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
//                                    String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/" + sharedPreferences.getString("CodUsuario", "NoData") + "/Puesto/RELOGIN";
//                                    Log.d("ValidaVenta", ws_getItinerario);
//                                    JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
//                                            new Response.Listener<JSONArray>() {
//                                                @Override
//                                                public void onResponse(JSONArray response) {
//                                                    if (response.length() == 1) {
//                                                        try {
//                                                            guardarDataMemoria("ValidaProgramacion_2", "true", getActivity());
//                                                            JSONObject RequestJson = response.getJSONObject(0);
//                                                            String fecha = RequestJson.getString("FECHA_PROGRAMACION");
//                                                            fecha = fecha.substring(0, 10);
//                                                            if (sharedPreferences.getString("anf_fechaProgramacion", "NoData").equals(fecha) &&
//                                                                    sharedPreferences.getString("anf_codigoEmpresa", "NoData").equals(RequestJson.getString("CODIGO_EMPRESA")) &&
//                                                                    sharedPreferences.getString("anf_codigoVehiculo", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO")) &&
//                                                                    sharedPreferences.getString("anf_horaSalida", "NoData").equals(RequestJson.getString("HORA_SALIDA")) &&
//                                                                    sharedPreferences.getString("anf_servicio", "NoData").equals(RequestJson.getString("SERVICIO")) &&
//                                                                    sharedPreferences.getString("anf_origen", "NoData").equals(RequestJson.getString("ORIGEN")) &&
//                                                                    sharedPreferences.getString("anf_destino", "NoData").equals(RequestJson.getString("DESTINO")) &&
//                                                                    sharedPreferences.getString("anf_secuencia", "NoData").equals(RequestJson.getString("SECUENCIA")) &&
//                                                                    sharedPreferences.getString("anf_tipoBus", "NoData").equals(RequestJson.getString("TIPO_BUS")) &&
//                                                                    sharedPreferences.getString("anf_rumbo", "NoData").equals(RequestJson.getString("RUMBO")) &&
//                                                                    sharedPreferences.getString("anf_nombre", "NoData").equals(RequestJson.getString("ANFITRION")) &&
//                                                                    sharedPreferences.getString("CO_VEHI_anfi", "NoData").equals(RequestJson.getString("CODIGO_VEHICULO"))
//
//                                                            ) {
                                                                progressDialog_valida.dismiss();
                                                                RealizaVenta();

//                                                            }else {
//                                                                progressDialog_valida.dismiss();
//                                                                Toast toast = Toast.makeText(getContext(),"HUBO UN CAMBIO EN LA PROGRAMACION LOGUEARSE NUEVAMENTE CON CODIGO DE CONDUCTOR", Toast.LENGTH_LONG);
//                                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                                toast.show();
//                                                                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                                                                SharedPreferences.Editor editor = sharedpreferences.edit();
//                                                                editor.clear();
//                                                                editor.commit();
//                                                                Intent intent = new Intent(getActivity(), ErrorActivity.class);
//                                                                startActivity(intent);
//                                                            }

//                                                        } catch (Exception ex) {
//                                                            ex.printStackTrace();
//                                                        }
//                                                    }
//                                                }
//                                            }, new Response.ErrorListener() {
//                                        @Override
//                                        public void onErrorResponse(VolleyError error) {
//                                            error.printStackTrace();
//                                            Toast.makeText(getActivity(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
//                                        }
//                                    }) {
//                                        @Override
//                                        public Map<String, String> getHeaders() throws AuthFailureError {
//                                            Map<String, String> headers = new HashMap<>();
//                                            String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                                            String auth = "Basic "
//                                                    + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                                            headers.put("Content-Type", "application/json");
//                                            headers.put("Authorization", auth);
//                                            return headers;
//                                        }
//                                    };
//                                    jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                                    queue.add(jsonArrayRequestItinerario);

                            }else{
                                RealizaVenta();
                            }
                               //RealizaVenta();
                        }
                    });
                    button_cancelarVentaboleto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            button_imprimirBoleto.setEnabled(true);
                            button_imprimirBoletoCarga.setEnabled(true);
                            flag = false;
                            boleto_dialog.dismiss();
                        }
                    });
                    boleto_dialog.show();
                    boleto_dialog.setCancelable(false);

                }

            }
        });
    }
	public boolean ValidacionDocumento(String idTipoDocu)
    {
        if(idTipoDocu=="1")
        {
            FlagValidaButton = 0;
            button_imprimirBoleto.setEnabled(true);
            Toast.makeText(getActivity(), "SELECCIONAR TIPO DOCUMENTO", Toast.LENGTH_SHORT).show();
            return false;
        }else if(idTipoDocu=="2")
        {
            if (editText_dni.getText().toString().length() != 8) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingrese un DNI", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (editText_nombreCliente.getText().toString().equals(" ") || editText_nombreCliente.getText().toString().equals("")) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="3")
        {
            if (editText_dni.getText().toString().length() != 8) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingrese un DNI", Toast.LENGTH_SHORT).show();
                return false;
            }
            /*if (editText_RUC.getText().toString().length() != 11) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingrese un RUC", Toast.LENGTH_SHORT).show();
                return false;
            }else if(editText_RUC.getText().toString().length()==11 &&
                        (!editText_RUC.getText().toString().substring(0,2).equals("20"))&&
                        (!editText_RUC.getText().toString().substring(0,2).equals("10"))&&
                        (!editText_RUC.getText().toString().substring(0,2).equals("15"))){
                    Toast.makeText(getActivity(), "Ingrese RUC Valido", Toast.LENGTH_SHORT).show();
                    FlagValidaButton=0;
                    button_imprimirBoleto.setEnabled(true);
                    return false;
            }*/
            String Respuesta = FuncionesAuxiliares.ValidaRuc(editText_RUC.getText().toString().trim());
            if(!Respuesta.equals("INFINITY_DEV"))
            {
                Toast.makeText(getActivity(), Respuesta, Toast.LENGTH_SHORT).show();
                FlagValidaButton=0;
                button_imprimirBoleto.setEnabled(true);
                return false;
            }
            if (editText_RazonSocial.getText().toString().equals(" ") || editText_RazonSocial.getText().toString().equals("")) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingresar Razon Social", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (editText_nombreCliente.getText().toString().equals(" ") || editText_nombreCliente.getText().toString().equals("")) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="4")
        {
            if (editText_dni.getText().toString().length() != 12) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingrese Carnet de Extranjeria", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (editText_nombreCliente.getText().toString().equals(" ") || editText_nombreCliente.getText().toString().equals("")) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="5")
        {
            if (editText_dni.getText().toString().length() != 12) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingrese Pasaporte", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (editText_nombreCliente.getText().toString().equals(" ") || editText_nombreCliente.getText().toString().equals("")) {
                FlagValidaButton = 0;
                button_imprimirBoleto.setEnabled(true);
                Toast.makeText(getActivity(), "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else {
            return false;
        }
    }
    private void startLocationUpdate() {
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            mService.requestLocationUpdates();
        }
    }
    public void CargaDestinos(final SharedPreferences sp,final Gson gs,final ArrayList<String> Tarifario,final ArrayList<String> lista_destinos)
    {
        try{
            final ArrayList<String> lista_idOrigen = new ArrayList<>();
            final ArrayList<String> lista_nombreOrigen = new ArrayList<>();
            final ArrayList<String> lista_nombreOrigenSpinner = new ArrayList<>();
            final ArrayList<String>  DEST_TRAMO = new ArrayList<>();
            final int anf_origen = Integer.valueOf(sp.getString("anf_origen", "NoData"));
            final int anf_destino = Integer.valueOf(sp.getString("anf_destino", "NoData"));
            for (int i=0;i < Tarifario.size();i++)
            {
                String[] Origen_Id = Tarifario.get(i).toString().split("-");
                DEST_TRAMO.add(Origen_Id[0]);
            }
            final List<Spinner_model> Origen = new ArrayList<>();
            Spinner_model CO_DEST_ORIG = new Spinner_model("999", "SELECCIONAR", "");
            Origen.add(CO_DEST_ORIG);
            final ArrayList<String> DestinosSpinner  =  FuncionesAuxiliares.removeDuplicates(DEST_TRAMO);
            for (int j=0;j<DestinosSpinner.size();j++)
            {
                for(int h = 0;h<lista_destinos.size();h++)
                {
                    String[] TTDEST = lista_destinos.get(h).split("-");
                    if (DestinosSpinner.get(j).toString().equals(TTDEST[0].toString()))
                    {
                        Spinner_model CO_DEST_ORIG_ = new Spinner_model(TTDEST[0], TTDEST[0] + "-" + TTDEST[1], "");
                        Origen.add(CO_DEST_ORIG_);
                    }
                }
            }
            ArrayAdapter spinnerArrayAdapter1 = new ArrayAdapter(getContext(),
                    android.R.layout.simple_spinner_item, Origen);
            spinner_origen.setAdapter(spinnerArrayAdapter1);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void CargaTipoDocumento()
    {
        final List<Spinner_model> model = new ArrayList<>();
        Spinner_model TipoDocumento = new Spinner_model("1", "SELECCIONAR", "");
        model.add(TipoDocumento);
        Spinner_model TipoDocumento1 = new Spinner_model("2", "DNI", "");
        model.add(TipoDocumento1);
        Spinner_model TipoDocumento2 = new Spinner_model("3", "RUC", "");
        model.add(TipoDocumento2);
        Spinner_model TipoDocumento3 = new Spinner_model("4", "CARNET DE EXTRANJERIA", "");
        model.add(TipoDocumento3);
        Spinner_model TipoDocumento4 = new Spinner_model("5", "PASAPORTE", "");
        model.add(TipoDocumento4);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, model);
        spinner_TipoDocumento.setAdapter(spinnerArrayAdapter);
    }
    public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey)
    {
        String json = sharedPreferences.getString(jsonKey, "NoData");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> lista = new ArrayList<>();
        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }
        return lista;
    }
    public String getNumCorrelativo(int numCorrelativoBLT)
    {
        String sec_zeros = "";
        if (Integer.toString(numCorrelativoBLT).length() < 10) {
            int num_zeros = 10 - Integer.toString(numCorrelativoBLT).length();
            for (int i = 0; i < num_zeros; i++) {
                sec_zeros = sec_zeros + "0";
            }
        }
        String numCorrelativoCompleto = sec_zeros + numCorrelativoBLT;
        return numCorrelativoCompleto;
    }

    public static Comparator<String> compareIDAsc = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int id1 = Integer.valueOf(o1);
            int id2 = Integer.valueOf(o2);
            return id1 - id2;
        }
    };

	public static Comparator<String> compareIDDesc = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int id1 = Integer.valueOf(o1);
            int id2 = Integer.valueOf(o2);
            return id2 - id1;
        }
    };
    public void calculaTarifa(final String origen, final String destino, final EditText editText_tarifa, final Button button_imprimir)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        final ArrayList<String> lista_tarifasViaje = getArray(sharedPreferences, gson, "anf_jsonTarifasViaje");
        editor.putString("guardar_origen", origen);
        editor.commit();
        editor.putString("guardar_destino", destino);
        editor.commit();
        for (int i = 0; i < lista_tarifasViaje.size(); i++) {
            String[] dataTarifa = lista_tarifasViaje.get(i).split("-");
            if ((dataTarifa[0].equals(origen)) && (dataTarifa[1].equals(destino))) {
                editor.putString("guardar_tarifa", dataTarifa[2]);
                editor.commit();
                editText_tarifa.setText(dataTarifa[2]+".00");
                button_imprimir.setEnabled(true);
                break;
            } else {
                editText_tarifa.setText("0.00");
                editor.putString("guardar_tarifa", "0");
                editor.commit();
                button_imprimir.setEnabled(true);
            }
        }
    }
    public boolean ValidaStadoImpresora()
    {
        try{
            String error = String.valueOf(printer.getStatus());
            if(error.equals("1"))
            {
                Toast.makeText(getActivity(),"IMPRESORA OCUPADA", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("2"))
            {
                Toast.makeText(getActivity(),"IMPRESORA SIN PAPEL", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("3"))
            {
                Toast.makeText(getActivity(),"El formato del error del paquete de datos de impresión", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if(error.equals("4"))
            {
                Toast.makeText(getActivity(),"MAL FUNCIONAMIENTO DE LA IMPRESORA", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("8"))
            {
                Toast.makeText(getActivity(),"IMPRESORA SOBRE CALOR", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("9"))
            {
                Toast.makeText(getActivity(),"EL VOLTAJE DE LA IMPRESORA ES DEMASIADO BAJO", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-16"))
            {
                Toast.makeText(getActivity(),"La impresión no está terminada", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-6"))
            {
                Toast.makeText(getActivity(),"error de corte de atasco", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-5"))
            {
                Toast.makeText(getActivity(),"error de apertura de la cubierta", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-4"))
            {
                Toast.makeText(getActivity(),"La impresora no ha instalado la biblioteca de fuentes", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-2"))
            {
                Toast.makeText(getActivity(),"El paquete de datos es demasiado largo", Toast.LENGTH_SHORT).show();
                return false;
            }
            else{
                return true;
            }
        }catch (Exception ex)
        {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return  false;
        }
    }
    public void imprimir_boletas(Spinner spinner_origen, Spinner spinner_destino, String ted, String empresa_seleccionada, SharedPreferences sharedPreferences_, String tipoBoleta) {

        /* Se pasan todos los valores necesarios para generar la estructura de la boleta que se va a imprimir */
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boleta boleta = new Boleta(tipoBoleta);
        boleta.setOrigen(sharedPreferences.getString("Origen_Texto","NoData"));
        boleta.setDestino(sharedPreferences.getString("Destino_Texto","NoData"));
        boleta.setTarifa(sharedPreferences.getString("guardar_tarifa", "NoData"));
        boleta.setDNI(sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
        boleta.setSeriePasaje(sharedPreferences.getString("guardar_serieViaje", "NoData"));
        boleta.setCorrelativoPasaje(sharedPreferences.getString("guardar_correlativoViajeCompleto", "NoData"));
        boleta.setEmpresa(empresa_seleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("guardar_fechaVentaViaje", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("guardar_horaVentaViaje", "NoData"));
        boleta.setNombreAnfitrion(sharedPreferences.getString("anf_nombre", "NoData"));
        boleta.setNombreCliente(sharedPreferences.getString("guardar_nombreCliente", "NoData"));
        boleta.setNumAsiento(sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
        boleta.setCO_VEHI(sharedPreferences.getString("CO_VEHI_anfi","NoData"));
        boleta.setEmpesa_imp(sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        boleta.SetPrueba(getString(R.string.ws_ticket));
        boleta.SetRUC(sharedPreferences.getString("guardar_RUC","NoData"));
        boleta.SetRazonSocial(sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData"));
        boleta.SetDocuElectronico(sharedPreferences.getString("TipoVenta","NoData"));
        /* ----------------------------------------- */

        try {

            printer.init();

            /* TEXTO */
            printer.printStr(boleta.getVoucher(), null);
            /* ----------------------------------------- */

            /* QR */
            printer.printBitmap(boleta.getQRBitmap(ted));
            /* ----------------------------------------- */
//            printer.printBitmap(boleta.getQRBitmap(ted));
            /* ----------------------------------------- */

            /* Margen final */
            printer.printStr(boleta.margenFinal(), null);
            /* ----------------------------------------- */
            printer.printStr("\n\n\n\n\n", null);
            int iRetError = printer.start();

            if (iRetError != 0x00) {
                //Log.d("Impresora", "ERROR:"+iRetError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            /*Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);*/
        }
    }


    public void imprimir_boletasCarga(Spinner spinner_origen, Spinner spinner_destino, String ted, String empresa_seleccionada, String tipoProducto, SharedPreferences sharedPreferences, String tipoBoleta) {

        /* Se pasan todos los valores necesarios para generar la estructura de la boleta que se va a imprimir */
        Boleta boleta = new Boleta(tipoBoleta);
        boleta.setOrigen(spinner_origen.getSelectedItem().toString());
        boleta.setDestino(spinner_destino.getSelectedItem().toString());
        boleta.setTarifa(sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
        boleta.setDNI(sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
        boleta.setSeriePasaje(sharedPreferences.getString("guardar_serieViaje", "NoData"));
        boleta.setCorrelativoPasaje(sharedPreferences.getString("guardar_correlativoViajeCompleto", "NoData"));
        boleta.setSerieCarga(sharedPreferences.getString("guardar_serieCarga", "NoData"));
        boleta.setCorrelativoCarga(sharedPreferences.getString("guardar_correlativoCargaCompleto", "NoData"));
        boleta.setEmpresa(empresa_seleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("guardar_fechaVentaCarga", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("guardar_horaVentaCarga", "NoData"));
        boleta.setTipoProducto(tipoProducto);
        boleta.setCantidad(sharedPreferences.getString("guardar_cantidad", "NoData"));
        boleta.setNombreAnfitrion(sharedPreferences.getString("anf_nombre", "NoData"));
        boleta.setNumAsiento(sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
        boleta.setNombreCliente(sharedPreferences.getString("guardar_nombreCliente", "NoData"));
        boleta.setEmpesa_imp(sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        boleta.SetRUC(sharedPreferences.getString("guardar_RUC","NoData"));
        boleta.SetRazonSocial(sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData"));
        boleta.SetDocuElectronico(sharedPreferences.getString("TipoVenta","NoData"));
        boleta.SetPrueba(getString(R.string.ws_ticket));
        /* ----------------------------------------- */

        try {
            printer.init();

            /* TEXTO */
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            printer.printStr(boleta.getVoucher(), null);
            /* ----------------------------------------- */

            /* QR */
            printer.printBitmap(boleta.getQRBitmap(ted));
            /* ----------------------------------------- */

            /* Margen final */
            printer.printStr(boleta.margenFinal(), null);
            /* ----------------------------------------- */

            int iRetError = printer.start();

            if (iRetError != 0x00) {
                //Log.d("Impresora", "ERROR:"+iRetError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }


    public String[] generarCodigoQR(String trama) {
        String xml64 = null;
        String ted = null;
        String ted64 = null;
        try{

        /* URI del proveedor del servicio */
        Uri CONTENT_URI = Uri.parse("content://org.pe.dgf.provider").buildUpon().appendPath(PATH_XML).build();
        /* ----------------------------------------- */

        /* Se generar el query para obtener la data encriptada */
        Cursor results = getActivity().getContentResolver().query(CONTENT_URI, null, trama, null, SOLICITA_TED);
        /* ----------------------------------------- */

        /*String xml64 = null;
        String ted = null;
        String ted64 = null;*/

        /* Se obtiene la data encriptada */
        if (results != null) {
            if (results.moveToNext()) {

                /* Se obtiene el valor de cada columna */
                xml64 = results.getString(results.getColumnIndex(COLUMN_NAME_XML64));
                ted = results.getString(results.getColumnIndex(COLUMN_NAME_TED));//para generar el QR
                ted64 = results.getString(results.getColumnIndex(COLUMN_NAME_TED64));
                /* ----------------------------------------- */
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */
        }catch (Exception ex)
        {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        String[] valores = {xml64, ted64, ted};

        return valores;
    }


    public void startActualizacionAsientosService() {
        ActualizacionAsientosService.actualizarAsientos(getActivity(), true);
    }


    public void stoptActualizacionAsientosService() {
        ActualizacionAsientosService.actualizarAsientos(getActivity(), false);
    }


    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }


    public void stopBoletoService() {
        BoletoService.startService(getActivity(), false);
    }


    public Boolean[] guardarCompraViaje(String xml64, String ted64,
                                        String numCorrelativoViajeCompleto) {

        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //SharedPreferences.Editor editor = sharedPreferences.edit();*/

        final Boolean[] respuesta = new Boolean[1];
        respuesta[0] = true;

        /* Fecha y hora de la venta del boleto */
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        guardarDataMemoria("guardar_fechaVentaViaje",fechaVenta,getContext());
        //editor.putString("guardar_fechaVentaViaje", fechaVenta);
        //editor.commit();

        final String horaVenta = new SimpleDateFormat("hh:mm a").format(date);
        guardarDataMemoria("guardar_horaVentaViaje",horaVenta,getContext());
        //editor.putString("guardar_horaVentaViaje", horaVenta);
        //editor.commit();
        /* ----------------------------------------- */

        /* Se obtiene el JSON generado */
        final JSONObject jsonObject = generarJSONViaje(sharedPreferences, fechaVenta, horaVenta, xml64, ted64, numSerieSeleccionado, numCorrelativoViajeCompleto, Integer.toString(numCorrelativoSeleccionado));
        //Log.d("respuesta", jsonObject.toString());
        /* ----------------------------------------- */
        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "viaje");
        cv.put("liberado", "No");
        cv.put("nu_docu",numSerieSeleccionado + "-" + numCorrelativoViajeCompleto);
        cv.put("ti_docu","BLT");
        cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
            cv.put("puesto", "boletero");
        }else{
            cv.put("puesto", "anfitrion");
        }

        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
        /* ----------------------------------------- */

        /* Se detiene el servicio */
        //stopBoletoService();
        /* ----------------------------------------- */

        //respuesta[0] = true;

        /* Ruta de la Web service */
        String ws_postVenta = getString(R.string.ws_ruta) + "SetVentaRuta";
        //Log.d("respuesta", ws_postVenta);
        /* ----------------------------------------- */

        /* Request que envía el boleto de viaje vendido */
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(!response.toString().equals("null")&& !response.toString().equals("[]"))
                        {
                            if (response.length() > 0) {

                                JSONObject info;
                                try {

                                    info = response.getJSONObject(0);
                                    //Log.d("respuesta", info.toString());

                                    /* Se obtiene la respuesta del servidor y en caso de ser "guardado" se guarda el boleto en la BD */
                                    if (info.getString("Respuesta").equals("GUARDADO")) {

                                      /*  ContentValues cv = new ContentValues();
                                        cv.put("data_boleto", jsonObject.toString());
                                        cv.put("estado", "guardado");
                                        cv.put("tipo", "viaje");
                                        cv.put("liberado", "No");

                                        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
                                            cv.put("puesto", "boletero");
                                        }else{
                                            cv.put("puesto", "anfitrion");
                                        }

                                        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
                                        /* ----------------------------------------- */

                                        /* Se detiene el servicio */
                                        //stopBoletoService();
                                        /* ----------------------------------------- */

                                        respuesta[0] = true;

                                    } else {
                                        //Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
                                        boleto_dialog.hide();
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

                        Toast.makeText(getActivity(), "No hay conectividad. Entrando en modo Offline.", Toast.LENGTH_LONG).show();

                        /* Se guarda el boleto de viaje en la BD con estado "pendiente" */
                      /*  ContentValues cv = new ContentValues();
                        cv.put("data_boleto", jsonObject.toString());
                        cv.put("estado", "pendiente");
                        cv.put("tipo", "viaje");
                        cv.put("liberado", "No");

                        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
                            cv.put("puesto", "boletero");
                        }else{
                            cv.put("puesto", "anfitrion");
                        }
                        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
                        /* ----------------------------------------- */

                        /* Se detiene el servicio */
//                        startBoletoService();
                        /* ----------------------------------------- */

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
        /* ----------------------------------------- */
        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestVenta);
        breakTime();
        return respuesta;
    }



    public Boolean[] guardarCompraCarga(String xml64, String ted64,
                                        String numCorrelativoCargaCompleto,
                                        final Button button_imprimirBoletoCarga) {

        final RequestQueue queue = Volley.newRequestQueue(getContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        final Boolean[] respuesta = new Boolean[1];
        respuesta[0] = true;

        /* Fecha, hora y día de la venta del boleto */
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        editor.putString("guardar_fechaVentaCarga", fechaVenta);
        editor.commit();

        final String horaVenta = new SimpleDateFormat("hh:mm").format(date);
        editor.putString("guardar_horaVentaCarga", horaVenta);
        editor.commit();

        String strDiaFormat = "dd";
        DateFormat diaFormat = new SimpleDateFormat(strDiaFormat);
        String diaSemana = diaFormat.format(date);
        /* ----------------------------------------- */

        /* Se obtiene el JSON generado */
        final JSONObject jsonObject = generarJSONCarga(sharedPreferences, fechaVenta, horaVenta, diaSemana, xml64, ted64,
                numSerieCargaSeleccionado, numCorrelativoCargaCompleto, Integer.toString(numCorrelativoCargaSeleccionado));
        //Log.d("respuesta", jsonObject.toString());
        /* ----------------------------------------- */
        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "carga");
        cv.put("liberado", "No");
        cv.put("nu_docu",numSerieCargaSeleccionado + "-" + numCorrelativoCargaCompleto);
        cv.put("ti_docu","BLT");
        cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
            cv.put("puesto", "boletero");

        }else{
            cv.put("puesto", "anfitrion");
        }

        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);

        /* Ruta de la Web service */
        String ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";
        Log.d("respuesta", jsonObject.toString());
        /* ----------------------------------------- */

        /* Request que envía el boleto de carga vendido */
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {

                            JSONObject info;
                            try {

                                info = response.getJSONObject(0);
                                //Log.d("respuesta", info.toString());

                                /* Se obtiene la respuesta del servidor y en caso de ser "guardado" se guarda el boleto en la BD */
                                if (info.getString("Respuesta").equals("GUARDADO")) {

                                    /*ContentValues cv = new ContentValues();
                                    cv.put("data_boleto", jsonObject.toString());
                                    cv.put("estado", "guardado");
                                    cv.put("tipo", "carga");
                                    cv.put("liberado", "No");

                                    if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
                                        cv.put("puesto", "boletero");

                                    }else{
                                        cv.put("puesto", "anfitrion");

                                    }

                                    Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);*/
                                    /* ----------------------------------------- */

                                    /* Se detiene el servicio */
                                    //stopBoletoService();
                                    /* ----------------------------------------- */

                                    respuesta[0] = true;

                                    /* Se activa el servicio de sincronización de asientos vendidos con boletero */
                                    //startActualizacionAsientosService();
                                    /* ----------------------------------------- */

                                    button_imprimirBoletoCarga.setEnabled(true);





                                }else{
                                  /*  Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
                                    carga_dialog.hide();

                                    /* Selección de serie y correlativo dependiendo si es DNI o RUC */
                                   /* if(editText_dni.getText().toString().length() == 8){

                                        numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado - 1;

                                        /* Se actualiza el correlativo de viaje */
                                   //     editor.putString("anf_correlativoBolCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                     //   editor.commit();
                                        /* ----------------------------------------- */

                                    //} else if (editText_dni.getText().toString().length() == 11) {

                                        /*numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado - 1;

                                        /* Se actualiza el correlativo de Viaje */
                                       // editor.putString("anf_correlativoFacCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                       // editor.commit();
                                        /* ----------------------------------------- */

                                   // }
                                    /* ----------------------------------------- */
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

                        /* Se guarda el boleto de carga con estado "pendiente" en la BD */
                    /*    ContentValues cv = new ContentValues();
                        cv.put("data_boleto", jsonObject.toString());
                        cv.put("estado", "pendiente");
                        cv.put("tipo", "carga");
                        cv.put("liberado", "No");
                        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
                            cv.put("puesto", "boletero");
                        }else{
                            cv.put("puesto", "anfitrion");
                        }

                        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
                        /* ----------------------------------------- */

                        /* Se detiene el servicio */
                       //startBoletoService();
                        /* ----------------------------------------- */

                        /* Se activa el servicio de sincronización de asientos vendidos con boletero */
                        //startActualizacionAsientosService();
                        /* ----------------------------------------- */

                        button_imprimirBoletoCarga.setEnabled(true);

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
        breakTime();

        return respuesta;
    }


    public JSONObject generarJSONViaje(SharedPreferences sharedPreferences_, String fechaVenta, String horaVenta, String xml64, String ted64,
                                       String numSerieSeleccionado, String numCorrelativoViajeCompleto, String numCorrelativoSeleccionado) {

        JSONObject jsonObject = new JSONObject();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
           // Log.d("jsonViahje",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            jsonObject.put("Empresa", sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            jsonObject.put("tipoDocumento", sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData"));
            jsonObject.put("NumeroDocumento", numSerieSeleccionado + "-" + numCorrelativoViajeCompleto);
            jsonObject.put("Unidad", "");
            jsonObject.put("Agencia", "");
            jsonObject.put("CondicionPago", "CCE");
            jsonObject.put("MonedaTipo", "SOL");
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("RumboItinerario", sharedPreferences.getString("anf_rumbo", "NoData"));
            jsonObject.put("OrigenBoleto", sharedPreferences.getString("guardar_origen", "NoData"));
            jsonObject.put("DestinoBoleto", sharedPreferences.getString("guardar_destino", "NoData"));
            jsonObject.put("SecuenciaItin", sharedPreferences.getString("anf_secuencia", "NoData"));

            //if (sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData").equals("FAC")) {
            if (sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
                jsonObject.put("CodigoCliente", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
                jsonObject.put("RUC", sharedPreferences.getString("guardar_RUC", "NoData"));
                jsonObject.put("RazonSocial",sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData"));
            } else {
                jsonObject.put("CodigoCliente", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
                jsonObject.put("RUC", "");
                jsonObject.put("RazonSocial","");
            }

            //jsonObject.put("CodigoCliente", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
            jsonObject.put("NombreCliente", sharedPreferences.getString("guardar_nombreCliente", "NoData"));
            jsonObject.put("TipoServicioItin", sharedPreferences.getString("anf_servicio", "NoData"));
            jsonObject.put("Asiento", sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
            jsonObject.put("FechaViajeItin", sharedPreferences.getString("anf_fechaProgramacion", "NoData"));
            jsonObject.put("horaViajeItin", sharedPreferences.getString("anf_horaSalida", "NoData"));
            jsonObject.put("Precio", sharedPreferences.getString("guardar_tarifa", "NoData"));
            jsonObject.put("UsuarioRegistro", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("Correlativo", numCorrelativoSeleccionado);
            jsonObject.put("Caja", "");
            jsonObject.put("TipoVenta", "Anfitrion");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);

            ventaBlt = new DatabaseBoletos(getContext());
            sqLiteDatabase = ventaBlt.getWritableDatabase();
            try   {
                ContentValues sqlQuery = new ContentValues();
                sqlQuery.put("CO_EMPR", sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
                sqlQuery.put("TI_DOCU", "BLT");
                sqlQuery.put("NU_DOCU", numSerieSeleccionado + "-" + String.valueOf(Integer.valueOf(numCorrelativoViajeCompleto)));
                sqlQuery.put("NU_SECU", sharedPreferences.getString("anf_secuencia", "NoData"));
                sqlQuery.put("FE_VIAJ",sharedPreferences.getString("anf_fechaProgramacion", "NoData"));
                sqlQuery.put("HO_VIAJ",sharedPreferences.getString("anf_horaSalida", "NoData"));
                sqlQuery.put("CO_VEHI",sharedPreferences.getString("anf_codigoVehiculo","NoData"));
                sqlQuery.put("NO_CLIE",sharedPreferences.getString("guardar_nombreCliente", "NoData"));
                sqlQuery.put("CO_DEST_ORIG",sharedPreferences.getString("guardar_origen", "NoData"));
                sqlQuery.put("CO_DEST_FINA",sharedPreferences.getString("guardar_destino", "NoData"));
                sqlQuery.put("DOCU_IDEN",sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
                sqlQuery.put("NU_ASIE",sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
                sqlQuery.put("IM_TOTA",sharedPreferences.getString("guardar_tarifa", "NoData"));
                sqlQuery.put("TIPO","2");
                sqLiteDatabase.insert("Manifiesto",null,sqlQuery);

            } catch (Exception e) {
                String error = e.getMessage();

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para venta de boleto de viaje.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

        return jsonObject;
    }



    public JSONObject generarJSONCarga(SharedPreferences sharedPreferences, String fechaVenta, String horaVenta, String diaSemana, String xml64,
                                       String ted64, String numSerieCargaSeleccionado, String numCorrelativoCargaCompleto, String numCorrelativoCargaSeleccionado) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            jsonObject.put("Unidad", "");
            jsonObject.put("Agencia", "");
            jsonObject.put("TipoDocuCarga", sharedPreferences.getString("guardar_tipoDocumentoCarga", "NoData"));
            jsonObject.put("SerieCorrelativo", numSerieCargaSeleccionado + "-" + numCorrelativoCargaCompleto);
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("Rumbo", sharedPreferences.getString("anf_rumbo", "NoData"));
            jsonObject.put("Origen", sharedPreferences.getString("guardar_origen", "NoData"));
            jsonObject.put("Destino", sharedPreferences.getString("guardar_destino", "NoData"));
            jsonObject.put("NuSecu", sharedPreferences.getString("anf_secuencia", "NoData"));
            jsonObject.put("NumeroDia", diaSemana);
            jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
            jsonObject.put("RUC", sharedPreferences.getString("guardar_RUC", ""));

            //jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
            jsonObject.put("NombreCliente", sharedPreferences.getString("guardar_nombreCliente", "NoData"));
            jsonObject.put("TipoServicio", sharedPreferences.getString("anf_servicio", "NoData"));
            jsonObject.put("NumeroAsiento", sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
            jsonObject.put("FechaViajeItinerario", sharedPreferences.getString("anf_fechaProgramacion", "NoData"));
            jsonObject.put("HoraViaje", sharedPreferences.getString("anf_horaSalida", "NoData"));
            jsonObject.put("ImporteTotal", sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
            jsonObject.put("Observacion", "");
            jsonObject.put("CodigoUsuario", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("NuDocuBoletoViaje", sharedPreferences
                    .getString("guardar_serieViaje", "NoData") + "-" + sharedPreferences.getString("guardar_correlativoViajeCompleto", "NoData"));
            jsonObject.put("TipoDocumentoBoletoViaje", sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData"));
            jsonObject.put("Correlativo", numCorrelativoCargaSeleccionado);
            jsonObject.put("Producto", sharedPreferences.getString("guardar_idProducto", "NoData"));
            jsonObject.put("Cantidad", sharedPreferences.getString("guardar_cantidad", "NoData"));
            jsonObject.put("Caja", "");
            jsonObject.put("TipoVenta", "Anfitrion");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para venta de boleto de carga.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

        return jsonObject;
    }


    public String generarTramaBoleto(SharedPreferences sharedPreferences, String numSerieBLT, String numCorrelativoViajeCompleto, String empresaTrama) {

        numCorrelativoViajeCompleto = numCorrelativoViajeCompleto.substring(2);
        String[] empresaSeleccionada = empresaTrama.split("-");
        // empresaSeleccionada[0] = CODIGO_EMPRESA
        // empresaSeleccionada[1] = EMPRESA
        // empresaSeleccionada[2] = DIRECCION
        // empresaSeleccionada[3] = LIMA
        // empresaSeleccionada[4] = DISTRITO
        // empresaSeleccionada[5] = DEPARTAMENTO
        // empresaSeleccionada[6] = PROVINCIA
        // empresaSeleccionada[7] = RUC
        // empresaSeleccionada[8] = RAZON_SOCIAL

        String tipoDocumento = "";
        String normativaSunat = "";
        //if (sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData").equals("FAC")) {
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }

        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            normativaSunat = "A;FormaPago;;Contado\n";
        }else{
            normativaSunat = "";
        }


        /*if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 11) {
            tipoDocumento = "01";
        } else {
            tipoDocumento = "03";
        }*/

        String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }


        /*if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 11) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }*/

        String documentoCliente = "";

        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").equals("")) {
            documentoCliente = "-";
        } else {
            documentoCliente = sharedPreferences.getString("guardar_numeroDocumento", "NoData");
        }
        String DocuDeclara  = "";
        String nombreCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            nombreCliente = sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData");
            DocuDeclara = sharedPreferences.getString("guardar_RUC","NoData");
        }else{
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
            DocuDeclara = sharedPreferences.getString("guardar_numeroDocumento","NoData");
        }
        /*if (sharedPreferences.getString("guardar_nombreCliente", "NoData").equals("")) {
            nombreCliente = "-";
        } else {
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
        }*/

        String direccionCliente = "";
        if (sharedPreferences.getString("guardar_direccionCliente", "NoData").equals("")) {
            direccionCliente = "-";
        } else {
            direccionCliente = sharedPreferences.getString("guardar_direccionCliente", "NoData");
        }

        /* Fecha y hora del cuando se genera la trama del boleto de viaje */
        Date date = new Date();

        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";

        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);

        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        /* ----------------------------------------- */

        /* Convertir monto a letras */
        String numeroFloat = String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")));
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        /* ----------------------------------------- */
        String tramaBoleto ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + numSerieBLT + "\n" +
                "A;Correlativo;;" + numCorrelativoViajeCompleto + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                //"A;TipoRucEmis;;6\n"+
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                //"A;RUTRecep;;" + documentoCliente + "\n" +
                "A;RUTRecep;;" + DocuDeclara + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                //"A;DirRecep;;" + direccionCliente + "\n" +
                "A;DirRecep;;-\n" +
//                "A;DirRecepUrbaniza;;NoData"+"\n"+
//                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;0.00\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "A;MntTotal;;" + String.format("%.2f",Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData"))) +"\n" +
                "A;MntTotalIgv;;0.00\n" +
                "A;TipoOperacion;;0101\n" + normativaSunat +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                "B;CodigoProductoSunat;1;78111802\n" +
                "B;PrcItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;PrcItemSinIgv;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;IndExe;1;20\n" +
                "B;CodigoTipoIgv;1;9997\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;0.00\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;"+sharedPreferences.getString("guardar_nombreCliente", "NoData")+"\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                //"E;DescripcionAdicSunat;4;WWW.PERUBUS.COM.PE\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n"+
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n"+
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;-\n"+
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n"+
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;5\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena +"\n"+
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;-\n"+
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;\"SOAT: COMPAÑIA DE SEGUROS LA POSITIVA.\" VENTA NORMAL\n"+
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.SOYUZONLINE.COM.PE\n"+
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("Destino_Texto","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n"+
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n"+
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n"+
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+numSerieBLT+"/F-VTS-42\n"+
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19;-\n"+
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;-\n"+
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;-\n"+
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n"+
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;"+empresaSeleccionada[1]+"\n"+
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;0320050000133\n"+
                "E;TipoAdicSunat;25;01\n"+
                "E;NmrLineasDetalle;25;25\n"+
                "E;NmrLineasAdicSunat;25;25\n"+
                "E;DescripcionAdicSunat;25;S/\n"+
                "E;TipoAdicSunat;26;01\n"+
                "E;NmrLineasDetalle;26;26\n"+
                "E;NmrLineasAdicSunat;26;26\n"+
                "E;DescripcionAdicSunat;26;-\n"+
                "E;TipoAdicSunat;27;01\n"+
                "E;NmrLineasDetalle;27;27\n"+
                "E;NmrLineasAdicSunat;27;27\n"+
                "E;DescripcionAdicSunat;27;-\n"+
                "E;TipoAdicSunat;28;01\n"+
                "E;NmrLineasDetalle;28;28\n"+
                "E;NmrLineasAdicSunat;28;28\n"+
                "E;DescripcionAdicSunat;28;FE. VIAJE: "+fechaVenta+" HORA VIAJE:"+ horaVenta+"\n"+
                "E;TipoAdicSunat;29;01\n"+
                "E;NmrLineasDetalle;29;29\n"+
                "E;NmrLineasAdicSunat;29;29\n"+
                "E;DescripcionAdicSunat;29;ASIENTO:     0         CLASE: ESTANDAR PEGASO\n"+
                "E;TipoAdicSunat;30;01\n"+
                "E;NmrLineasDetalle;30;30\n"+
                "E;NmrLineasAdicSunat;30;30\n"+
                "E;DescripcionAdicSunat;30;-\n"+
                "E;TipoAdicSunat;31;01\n"+
                "E;NmrLineasDetalle;31;31\n"+
                "E;NmrLineasAdicSunat;31;31\n"+
                "E;DescripcionAdicSunat;31;www.soyuzonline.com.pe Telf: 2052370\n"+
                "E;TipoAdicSunat;32;01\n"+
                "E;NmrLineasDetalle;32;32\n"+
                "E;NmrLineasAdicSunat;32;32\n"+
                "E;DescripcionAdicSunat;32;NRO\n"+
                "E;TipoAdicSunat;33;02\n"+
                "E;NmrLineasDetalle;33;1\n"+
                "E;NmrLineasAdicSunat;33;11\n"+
                "E;DescripcionAdicSunat;33;0\n"+
                "E;TipoAdicSunat;34;02\n"+
                "E;NmrLineasDetalle;34;1\n"+
                "E;NmrLineasAdicSunat;34;12\n"+
                "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim()+"  -  "+ sharedPreferences.getString("Destino_Texto","NoData").toString().trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";
      /*  String tramaBoleto = "A;Serie;;" + numSerieBLT + "\n" +
                "A;Correlativo;;" + numCorrelativoViajeCompleto + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + " - " + empresaSeleccionada[3] + " - " + empresaSeleccionada[4] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;TipoOperacion;;0101\n" +
                "A;TipoRutReceptor;;" + tipoDocumentoCliente + "\n" +
                "A;RUTRecep;;" + documentoCliente + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;" + direccionCliente + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;MntNeto;;0.00\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "A;MntTotalIgv;;0.00\n" +
                "A;MntTotal;;" + String.format("%.2f",Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData"))) +"\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                "B;CodigoProductoSunat;1;78111802\n" +
                "B;PrcItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;PrcItemSinIgv;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;IndExe;1;20\n" +
                "B;CodigoTipoIgv;1;9997\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;0.00\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;-\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n" +
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n" +
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;-\n" +
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n" +
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;9\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena+"\n" +
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;-\n" +
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;-\n" +
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.PERUBUS.COM.PE\n" +
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;ICA\n" +
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;STA. CRUZ\n" +
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n" +
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n" +
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n" +
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+numSerieBLT+"/F-VTS-42\n" +
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19; Sucursal ICA RUTA\n" +
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;AV. MATIAS MANZANILLA 130\n" +
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;ICA ICA\n" +
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n" +
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;E.T. PERU BUS S.A\n" +
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;0180050002160\n" +
                "E;TipoAdicSunat;25;01\n" +
                "E;NmrLineasDetalle;25;25\n" +
                "E;NmrLineasAdicSunat;25;25\n" +
                "E;DescripcionAdicSunat;25;S/\n" +
                "E;TipoAdicSunat;26;01\n" +
                "E;NmrLineasDetalle;26;26\n" +
                "E;NmrLineasAdicSunat;26;26\n" +
                "E;DescripcionAdicSunat;26;-\n" +
                "E;TipoAdicSunat;27;01\n" +
                "E;NmrLineasDetalle;27;27\n" +
                "E;NmrLineasAdicSunat;27;27\n" +
                "E;DescripcionAdicSunat;27;-\n" +
                "E;TipoAdicSunat;28;01\n" +
                "E;NmrLineasDetalle;28;28\n" +
                "E;NmrLineasAdicSunat;28;28\n" +
                "E;DescripcionAdicSunat;28;FE. VIAJE: 12/03/19 HORA VIAJE: 04:30\n" +
                "E;TipoAdicSunat;29;01\n" +
                "E;NmrLineasDetalle;29;29\n" +
                "E;NmrLineasAdicSunat;29;29\n" +
                "E;DescripcionAdicSunat;29;ASIENTO:     0         CLASE: ESTANDAR RUTERO\n" +
                "E;TipoAdicSunat;30;01\n" +
                "E;NmrLineasDetalle;30;30\n" +
                "E;NmrLineasAdicSunat;30;30\n" +
                "E;DescripcionAdicSunat;30;NRro\n" +
                "E;TipoAdicSunat;31;01\n" +
                "E;NmrLineasDetalle;31;31\n" +
                "E;NmrLineasAdicSunat;31;31\n" +
                "E;DescripcionAdicSunat;31;www.perubus.com.pe Telf: 205230\n" +
                "E;TipoAdicSunat;32;01\n" +
                "E;NmrLineasDetalle;32;32\n" +
                "E;NmrLineasAdicSunat;32;32\n" +
                "E;DescripcionAdicSunat;32;NRO\n" +
                "E;TipoAdicSunat;33;02\n" +
                "E;NmrLineasDetalle;33;1\n" +
                "E;NmrLineasAdicSunat;33;11\n" +
                "E;DescripcionAdicSunat;33;0\n" +
                "E;TipoAdicSunat;34;02\n" +
                "E;NmrLineasDetalle;34;1\n" +
                "E;NmrLineasAdicSunat;34;12\n" +
                "E;DescripcionAdicSunat;34;ICA  -  STA. CRUZ\n" +
                "E;TipoAdicSunat;35;02\n" +
                "E;NmrLineasDetalle;35;1\n" +
                "E;NmrLineasAdicSunat;35;13\n" +
                "E;DescripcionAdicSunat;35;Reserva\n";*/

        return tramaBoleto;
    }
    @Override
    public void onDestroy() {
        boleto_dialog.dismiss();
        carga_dialog.dismiss();
        super.onDestroy();

    }


    public boolean ValidaExiteCorrelativo(String NU_DOCU,String CO_EMPR)
    {
        try
        {
            sqLiteDatabase = ventaBlt.getWritableDatabase();
            final Cursor cursor = sqLiteDatabase.query("VentaBoletos", null, "nu_docu=\""+NU_DOCU +"\" and ti_docu='BLT' and co_empr=\""+CO_EMPR +"\"", null, null,null,null);
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()){
                    String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                    final JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getString("NumeroDocumento").equals(NU_DOCU) && jsonObject.getString("Empresa").equals(CO_EMPR))
                    {
                        //Log.d("NU_DOCU_EXISTE",NU_DOCU);
                        return true;
                    }
                }
            }

            return  false;
        }
        catch (Exception ex)
        {
            //Log.d("ErrorValidacion",ex.getMessage());
            return  false;
        }
    }
    public boolean ValidaDuplicidad(String NU_DOCU,String CO_EMPR)
    {
        try{
            sqLiteDatabase = ventaBlt.getWritableDatabase();
            //final Cursor cursor = sqLiteDatabase.query("UltimaVenta", null, "nu_docu>=\""+NU_DOCU +"\" and ti_docu='BLT' and co_empr=\""+CO_EMPR +"\"", null, null,null,null);
            final Cursor cursor = sqLiteDatabase.query("UltimaVenta", null, "nu_docu>=\""+NU_DOCU +"\" and ti_docu='BLT' and co_empr=\""+CO_EMPR +"\" and  substr(nu_docu,1,1)=\""+NU_DOCU.substring(0,1) +"\"" , null, null,null,null);
            if (cursor.getCount()>0)
            {
                return  true;
            }
            else
            {
                return false;
            }

        }catch (Exception ex)
        {
            return false;
        }
    }
    public String generarTramaCarga(SharedPreferences sharedPreferences, String numSerieCarga, String numCorrelativoCargaCompleto, String empresaTrama) {

        String[] empresaSeleccionada = empresaTrama.split("-");
        //empresaSeleccionada[0] = CODIGO_EMPRESA
        //empresaSeleccionada[1] = EMPRESA
        //empresaSeleccionada[2] = DIRECCION
        //empresaSeleccionada[3] = LIMA
        //empresaSeleccionada[4] = DISTRITO
        //empresaSeleccionada[5] = DEPARTAMENTO
        //empresaSeleccionada[6] = PROVINCIA
        //empresaSeleccionada[7] = RUC
        //empresaSeleccionada[8] = RAZON_SOCIAL

        numCorrelativoCargaCompleto = numCorrelativoCargaCompleto.substring(2);

        String tipoDocumento = "";
        /*if (sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData").equals("FAC")) {
            tipoDocumento = "01";
        } else {
            tipoDocumento = "03";
        }*/
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }

        String normativaSunat = "";
        //if (sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData").equals("FAC")) {

        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            normativaSunat = "A;FormaPago;;Contado\n";
        }else{
            normativaSunat = "";
        }


        /*String tipoDocumentoCliente = "";
        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 11) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }*/
        String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }





       /* String documentoCliente = "";
        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").equals("")) {
            documentoCliente = "-";
        } else {
            documentoCliente = sharedPreferences.getString("guardar_numeroDocumento", "NoData");
        }*/

       String documentoCliente = "";

        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").equals("")) {
            documentoCliente = "-";
        } else {
            documentoCliente = sharedPreferences.getString("guardar_numeroDocumento", "NoData");
        }


        /*String nombreCliente = "";
        if (sharedPreferences.getString("guardar_nombreCliente", "NoData").equals("")) {
            nombreCliente = "-";
        } else {
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
        }*/

        String DocuDeclara  = "";
        String nombreCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            nombreCliente = sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData");
            DocuDeclara = sharedPreferences.getString("guardar_RUC","NoData");
        }else{
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
            DocuDeclara = sharedPreferences.getString("guardar_numeroDocumento","NoData");
        }

        String direccionCliente = "";
        if (sharedPreferences.getString("guardar_direccionCliente", "NoData").equals("NoData")) {
            direccionCliente = "-";
        } else {
            direccionCliente = sharedPreferences.getString("guardar_direccionCliente", "NoData");
        }

        String codEmpresaTrama = "";
        if(empresaSeleccionada[0].equals("01")){
            codEmpresaTrama = "0180050002083";

        }else if(empresaSeleccionada[0].equals("02")){
            codEmpresaTrama = "0320050000128";

        }else {
            codEmpresaTrama = "-";

        }

        /* Fecha y hora del cuando se genera la trama del boleto de carga */
        Date date = new Date();

        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";

        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);

        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        /* ----------------------------------------- */

        /* Se obtiene el monto neto y el monto de IGV */
        double montoTotal = Float.valueOf(sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
        double montoSinIGV = (montoTotal * 100) / 118;
        double montoSinIGVRedondeado = Math.rint(montoSinIGV * 100) / 100;

        double montoIGV = montoTotal - montoSinIGVRedondeado;
        /* ----------------------------------------- */

        /* Convertir monto a letras */
        String numeroFloat = String.format("%.2f",montoTotal);
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        /* ----------------------------------------- */

        String tramaCarga =
                "A;Serie;;" + numSerieCarga + "\n" +
                        "A;Correlativo;;" + numCorrelativoCargaCompleto + "\n" +
                        "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                        "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                        "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                        "A;ComuEmis;;150115\n" +
                        "A;CodigoLocalAnexo;;0000\n" +
                        "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                        "A;TipoOperacion;;0101\n"  +
                        "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                        "A;RUTRecep;;" + DocuDeclara + "\n" +
                        "A;RznSocRecep;;" + nombreCliente + "\n" +
                        "A;DirRecep;;-\n" +
                        "A;TipoMoneda;;PEN\n" +
                        "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                        "A;MntExe;;0.00\n" +
                        "A;MntExo;;0.00\n" +
                        "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" + normativaSunat +
                        "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoRucEmis;;6\n"+
                "A;UrbanizaEmis;;LIMA\n" +
                "A;ProviEmis;;LIMA\n" +
//                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
//                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                //"A;RUTRecep;;" + documentoCliente + "\n" +
//                "A;DirRecepUrbaniza;;NoData"+"\n"+
//                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;"+String.format("%.2f", montoIGV)+"\n" +
                "A2;TasaImpuesto;1;18\n"+
                "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoSinIGV)+"\n"+
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                //"B;CodigoProductoSunat;1;78111802\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;" + montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;IndExe;1;10\n" +
                //"B;CodigoTipoIgv;1;9997\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;" + String.format("%.2f", montoIGV) + "\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;"+sharedPreferences.getString("anf_nombre", "NoData")+"\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                //"E;DescripcionAdicSunat;4;WWW.PERUBUS.COM.PE\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n"+
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n"+
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;"+sharedPreferences.getString("guardar_nombreCliente", "NoData")+"\n" +
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n"+
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;5\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena +"\n"+
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;-\n"+
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;\"SOAT: COMPAÑIA DE SEGUROS LA POSITIVA.\" VENTA NORMAL\n"+
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                        "E;DescripcionAdicSunat;12;WWW.PERUBUS.COM.PE\n"+
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("Destino_Texto","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n"+
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n"+
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n"+
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+numSerieCarga+"/F-VTS-42\n"+
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19;-\n"+
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;-\n"+
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;-\n"+
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n"+
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;"+empresaSeleccionada[1]+"\n"+
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;0320050000133\n"+
                "E;TipoAdicSunat;25;01\n"+
                "E;NmrLineasDetalle;25;25\n"+
                "E;NmrLineasAdicSunat;25;25\n"+
                "E;DescripcionAdicSunat;25;S/\n"+
                "E;TipoAdicSunat;26;01\n"+
                "E;NmrLineasDetalle;26;26\n"+
                "E;NmrLineasAdicSunat;26;26\n"+
                "E;DescripcionAdicSunat;26;-\n"+
                "E;TipoAdicSunat;27;01\n"+
                "E;NmrLineasDetalle;27;27\n"+
                "E;NmrLineasAdicSunat;27;27\n"+
                "E;DescripcionAdicSunat;27;-\n"+
                "E;TipoAdicSunat;28;01\n"+
                "E;NmrLineasDetalle;28;28\n"+
                "E;NmrLineasAdicSunat;28;28\n"+
                "E;DescripcionAdicSunat;28;FE. VIAJE: "+fechaVenta+" HORA VIAJE:"+ horaVenta+"\n"+
                "E;TipoAdicSunat;29;01\n"+
                "E;NmrLineasDetalle;29;29\n"+
                "E;NmrLineasAdicSunat;29;29\n"+
                "E;DescripcionAdicSunat;29;ASIENTO:     0         CLASE: ESTANDAR PEGASO\n"+
                "E;TipoAdicSunat;30;01\n"+
                "E;NmrLineasDetalle;30;30\n"+
                "E;NmrLineasAdicSunat;30;30\n"+
                "E;DescripcionAdicSunat;30;-\n"+
                "E;TipoAdicSunat;31;01\n"+
                "E;NmrLineasDetalle;31;31\n"+
                "E;NmrLineasAdicSunat;31;31\n"+
                "E;DescripcionAdicSunat;31;http://www.perubus.com.pe Telf: 2052370\n"+
                "E;TipoAdicSunat;32;01\n"+
                "E;NmrLineasDetalle;32;32\n"+
                "E;NmrLineasAdicSunat;32;32\n"+
                "E;DescripcionAdicSunat;32;NRO\n"+
                "E;TipoAdicSunat;33;02\n"+
                "E;NmrLineasDetalle;33;1\n"+
                "E;NmrLineasAdicSunat;33;11\n"+
                "E;DescripcionAdicSunat;33;0\n"+
                "E;TipoAdicSunat;34;02\n"+
                "E;NmrLineasDetalle;34;1\n"+
                "E;NmrLineasAdicSunat;34;12\n"+
                "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim()+"  -  "+ sharedPreferences.getString("Destino_Texto","NoData").toString().trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";
                /*"E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;ORIGEN\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;DESTINO\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;ASIENTO:     0         CLASE: ESTANDAR RUTERO\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;WWW.PERUBUS.COM.PE\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;0180050002160\n";*/

      /*  String tramaCarga = "A;Serie;;" + numSerieCarga + "\n" +
                "A;Correlativo;;" + numCorrelativoCargaCompleto + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + " - " + empresaSeleccionada[3] + " - " + empresaSeleccionada[4] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;TipoOperacion;;0101\n" +
                "A;TipoRutReceptor;;" + tipoDocumentoCliente + "\n" +
                "A;RUTRecep;;" + documentoCliente + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;" + direccionCliente + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;MntNeto;;" + montoSinIGVRedondeado+ "\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;" + String.format("%.2f", montoIGV) + "\n" +
                "A2;TasaImpuesto;1;18\n" +
                "A2;MontoImpuestoBase;1;" + String.format("%.2f", montoTotal) + "\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;" + sharedPreferences.getString("guardar_cantidad", "NoData") + "\n" +
                "B;UnmdItem;1;MTQ\n" +
                "B;VlrCodigo;1;" + sharedPreferences.getString("guardar_idProducto", "NoData") + "\n" +
                "B;NmbItem;1;" + sharedPreferences.getString("guardar_nombreProducto", "NoData") + "\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;" + String.format("%.2f", montoTotal) + "\n" +
                "B;PrcItemSinIgv;1;" + montoSinIGVRedondeado + "\n" +
                "B;MontoItem;1;" + String.format("%.2f", montoTotal) + "\n" +
                "B;IndExe;1;10\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;" + String.format("%.2f", montoIGV) + "\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;-\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n" +
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n" +
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;-\n" +
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n" +
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;9\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena+"\n" +
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;F-VTS-43\n" +
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;-\n" +
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.SOYUZONLINE.COM.PE\n" +
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;LIMA\n" +
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;ICA\n" +
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n" +
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n" +
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n" +
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+numSerieCarga+"/F-VTS-43\n" +
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19;Sucursal EN RUTA\n" +
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;EN RUTA\n" +
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;LA VICTORIA LIMA\n" +
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n" +
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;SOYUZ S.A\n" +
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;"+codEmpresaTrama+"\n" +
                "E;TipoAdicSunat;25;01\n" +
                "E;NmrLineasDetalle;25;25\n" +
                "E;NmrLineasAdicSunat;25;25\n" +
                "E;DescripcionAdicSunat;25;S/\n" +
                "E;TipoAdicSunat;26;01\n" +
                "E;NmrLineasDetalle;26;26\n" +
                "E;NmrLineasAdicSunat;26;26\n" +
                "E;DescripcionAdicSunat;26;-\n" +
                "E;TipoAdicSunat;27;01\n" +
                "E;NmrLineasDetalle;27;27\n" +
                "E;NmrLineasAdicSunat;27;27\n" +
                "E;DescripcionAdicSunat;27;-\n" +
                "E;TipoAdicSunat;28;01\n" +
                "E;NmrLineasDetalle;28;28\n" +
                "E;NmrLineasAdicSunat;28;28\n" +
                "E;DescripcionAdicSunat;28;-\n" +
                "E;TipoAdicSunat;29;01\n" +
                "E;NmrLineasDetalle;29;29\n" +
                "E;NmrLineasAdicSunat;29;29\n" +
                "E;DescripcionAdicSunat;29;-\n" +
                "E;TipoAdicSunat;30;01\n" +
                "E;NmrLineasDetalle;30;30\n" +
                "E;NmrLineasAdicSunat;30;30\n" +
                "E;DescripcionAdicSunat;30;-\n" +
                "E;TipoAdicSunat;31;01\n" +
                "E;NmrLineasDetalle;31;31\n" +
                "E;NmrLineasAdicSunat;31;31\n" +
                "E;DescripcionAdicSunat;31;http://www.soyuzonline.com.pe \n" +
                "E;TipoAdicSunat;32;01\n" +
                "E;NmrLineasDetalle;32;32\n" +
                "E;NmrLineasAdicSunat;32;32\n" +
                "E;DescripcionAdicSunat;32;PESO\n" +
                "E;TipoAdicSunat;33;02\n" +
                "E;NmrLineasDetalle;33;1\n" +
                "E;NmrLineasAdicSunat;33;11\n" +
                "E;DescripcionAdicSunat;33;0.00\n" +
                "E;TipoAdicSunat;34;02\n" +
                "E;NmrLineasDetalle;34;1\n" +
                "E;NmrLineasAdicSunat;34;12\n" +
                "E;DescripcionAdicSunat;34;-\n" +
                "E;TipoAdicSunat;35;02\n" +
                "E;NmrLineasDetalle;35;1\n" +
                "E;NmrLineasAdicSunat;35;13\n" +
                "E;DescripcionAdicSunat;35;-\n";*/

        return tramaCarga;
    }


    public void breakTime() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Métodos y clases adicionales para el servicio de GPS */
    /* Fuente: https://github.com/googlesamples/android-play-location/tree/master/LocationUpdatesForegroundService/app/src/main/java/com/google/android/gms/location/sample/locationupdatesforegroundservice */

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            context.unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }


    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    getView().findViewById(R.id.venta_boletos_layout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                Snackbar.make(
                        getView().findViewById(R.id.venta_boletos_layout),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    private class MyReceiver extends BroadcastReceiver {
        float oldDistance=0;
        final int MINIMUN_PROCESS_DISTANCE = context.getResources().getInteger(R.integer.MINIMUN_PROCESS_DISTANCE);
        @Override
        public void onReceive(Context context, Intent intent) {

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            final RequestQueue queue = Volley.newRequestQueue(getActivity());

            final Gson gson = new Gson();

            /* Objetos JSON */
            JSONObject liberacionAsientos = new JSONObject();
            JSONArray documentos =  new JSONArray();
            /* ----------------------------------------- */

            /* Se obtiene la distacia recorrida y se trunca a dos decimales */
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            float distance = intent.getFloatExtra(LocationUpdatesService.EXTRA_DISTANCE,0);
            String distancia = String.format("%.2f", distance/1000);
            /* ----------------------------------------- */

            /* Arreglos */
            // lista_reporteVentaGPS: arreglo que guarda los boletos vendidos
            // lista_tramos: arreglo que contiene la lista de tramos de viaje con el kilomatraje acumulado
            final ArrayList<String> lista_reporteVentaGPS = getArray(sharedPreferences,gson,"anf_jsonReporteVentaGPS");
            final ArrayList<String> lista_tramos = getArray(sharedPreferences,gson,"anf_jsonTramos");
            /* ----------------------------------------- */

            final Date date = new Date();

            /* Distancia mínima para procesar */
            boolean vale_la_pena_procesar = (distance-oldDistance)>MINIMUN_PROCESS_DISTANCE;
            /* ----------------------------------------- */

            /* Validación en caso location sea diferente de null, el arrelgo de asiento no esté vacío, la diastancia sea mayor a la mímina y no se esté realizando una venta de boleto */
            if (location != null && !lista_reporteVentaGPS.isEmpty() && !lista_tramos.isEmpty() && vale_la_pena_procesar && !flag) {

                oldDistance = distance;

                Toast.makeText(getContext(), getDistanceText(context, distance),
                        Toast.LENGTH_SHORT).show();

                String[] dataReporteGPS = lista_reporteVentaGPS.get(0).split("/");

                /* Se itera en función a los boletos vendidos */
                for(int i = 0; i < dataReporteGPS.length; i++){

                    String[] dataAsientosVendidos = dataReporteGPS[i].split("-");
                    // dataAsientosVendidos[0] = NUM_ASIENT
                    // dataAsientosVendidos[1] = SERIE
                    // dataAsientosVendidos[2] = CORRELATIVO
                    // dataAsientosVendidos[3] = CO_DEST_ORIG
                    // dataAsientosVendidos[4] = CO_DEST_FINA
                    // dataAsientosVendidos[5] = CO_CLIE
                    // dataAsientosVendidos[6] = IM_TOTA
                    // dataAsientosVendidos[7] = CO_EMPR
                    // dataAsientosVendidos[8] = TI_DOCU
                    // dataAsientosVendidos[9] = LIBERADO
                    // dataAsientosVendidos[10] = CARGA
                    // dataAsientosVendidos[11] = ServicioEmpresa

                    /* Se itera en función a la lista de tramos */
                    for(int j = 0; j < lista_tramos.size(); j++){

                        String[] dataTramo = lista_tramos.get(j).split("-");
                        // dataTramo[0] = CO_DEST_FINA
                        // dataTramo[1] = NU_KILO_VIAJ

                        /* Validación que determina si un boleto que no ha sido liberado y que es de tipo viaje ha superado su destino */
                        if(dataTramo[0].equals(dataAsientosVendidos[3]) && Float.valueOf(distancia) >= Float.valueOf(dataTramo[1])
                                && dataAsientosVendidos[9].equals("NO") && dataAsientosVendidos[11].equals("VIAJE")){

                            /* Se genera el JSON del boleto que va a ser liberado y se agrega en el array JSON "documentos" */
                            JSONObject boletosLiberados = new JSONObject();

                            String numCorrelativoBLTCompleto = completarCorrelativo(Integer.valueOf(dataAsientosVendidos[2]));
                            String numBoleto = dataAsientosVendidos[1] + "-" + numCorrelativoBLTCompleto;

                            String fechaConsulta = new SimpleDateFormat("yyyy-MM-dd").format(date);
                            String horaConsulta = new SimpleDateFormat("hh:mm:ss").format(date);

                            try {
                                boletosLiberados.put("Empresa", dataAsientosVendidos[7]);
                                boletosLiberados.put("TipoDocumento", dataAsientosVendidos[8]);
                                boletosLiberados.put("NumeroDocumento", numBoleto);
                                boletosLiberados.put("FechaDocumento", fechaConsulta+"T"+horaConsulta);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Error al generar el json para liberar asientos.", Toast.LENGTH_LONG).show();

                                intent = new Intent(getActivity(), ErrorActivity.class);
                                startActivity(intent);

                            }
                            documentos.put(boletosLiberados);
                            /* ----------------------------------------- */
                        }
                        /* ----------------------------------------- */
                    }
                    /* ----------------------------------------- */
                }
                /* ----------------------------------------- */

                /* Se obtienen todos los boletos */
                Cursor cursor = sqLiteDatabase.query("VentaBoletos", null, null, null, null,null,null);
                /* ----------------------------------------- */

                /* Validación si encuentra conincidencias con el query */
                if(cursor.getCount() != 0){

                    /* Iteración en función a la cantidad filas obtenidas en el query */
                    while(cursor.moveToNext()){

                        /* Se obtiene el ID, el JSON (string), el tipo (viaje/carga) y si esta liberado */
                        final String id = cursor.getString(cursor.getColumnIndex("id"));
                        String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                        String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                        String liberado = cursor.getString(cursor.getColumnIndex("liberado"));
                        /* ----------------------------------------- */

                        try {

                            /* Se genera un JSON a partir de un string */
                            JSONObject jsonObject = new JSONObject(data);
                            /* ----------------------------------------- */

                            /* Iteración en función a la lista de tramos */
                            for(int j = 0; j < lista_tramos.size(); j++){

                                String[] dataTramo = lista_tramos.get(j).split("-");
                                // dataTramo[0] = CO_DEST_FINA
                                // dataTramo[1] = NU_KILO_VIAJ

                                /* Validación que determina si un boleto que no ha sido liberado y que es de tipo viaje ha superado su destino */
                                if(tipo.equals("viaje") && liberado.equals("No") &&
                                        dataTramo[0].equals(jsonObject.get("DestinoBoleto")) && Float.valueOf(distancia) >= Float.valueOf(dataTramo[1])){

                                    /* Se genera el JSON del boleto que va a ser liberado y se agrega en el array JSON "documentos" */
                                    String fechaConsulta = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                    String horaConsulta = new SimpleDateFormat("hh:mm:ss").format(date);

                                    JSONObject boletosLiberados = new JSONObject();

                                    boletosLiberados.put("Empresa", jsonObject.get("Empresa"));
                                    boletosLiberados.put("TipoDocumento", jsonObject.get("tipoDocumento"));
                                    boletosLiberados.put("NumeroDocumento", jsonObject.get("NumeroDocumento"));
                                    boletosLiberados.put("FechaDocumento", fechaConsulta+"T"+horaConsulta);

                                    documentos.put(boletosLiberados);
                                    /* ----------------------------------------- */

                                    /* Se actuliza el estado de liberado del boleto en la BD */
                                    ContentValues cv = new ContentValues();
                                    cv.put("data_boleto", jsonObject.toString());
                                    cv.put("liberado", "Si");
                                    int value = sqLiteDatabase.update("VentaBoletos",cv, "id="+id, null);
                                    /* ----------------------------------------- */
                                }
                                /* ----------------------------------------- */
                            }
                            /* ----------------------------------------- */
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Error al generar el json para liberar asientos.", Toast.LENGTH_LONG).show();

                            intent = new Intent(getActivity(), ErrorActivity.class);
                            startActivity(intent);
                        }
                    }
                    /* ----------------------------------------- */
                }
                /* ----------------------------------------- */

                try {

                    /* Se genera el JSON que se va a enviar con el array JSON que contine todos los boletos que van a ser liberados */
                    liberacionAsientos.put("Documento",documentos);
                    /* ----------------------------------------- */

                    /* Ruta de la Web service */
                    String ws_putLiberacionAsientos = getString(R.string.ws_ruta) + "LiberaAsiento";
                    /* ----------------------------------------- */

                    /* Validación en caso el array JSON no esté vacío */
                    if(documentos.length() != 0){

                        /* Request que envía los boletos que van a ser liberados */
                        MyJSONArrayRequest jsonArrayRequestLiberacionAsientos = new MyJSONArrayRequest(Request.Method.PUT, ws_putLiberacionAsientos, liberacionAsientos,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length() > 0) {

                                            JSONObject info;
                                            try {
                                                info = response.getJSONObject(0);

                                                String respuesta = info.getString("Respuesta");

                                                /* Validación de respuesta positiva del Servidor */
                                                if (respuesta.equals("LIBRE")) {

                                                    /* Ruta de la Web service */
                                                    String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" +
                                                            sharedPreferences.getString("anf_secuencia", "NoData") + "/" + sharedPreferences.getString("anf_rumbo", "NoData") + "/" +
                                                            sharedPreferences.getString("anf_fechaProgramacion", "NoData");
                                                    /* ----------------------------------------- */

                                                    /* Request que obtiene los asientos vendidos y actualiza la data en memoria */
                                                    JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                                            new Response.Listener<JSONArray>() {

                                                                @Override
                                                                public void onResponse(JSONArray response) {

                                                                    String asientosVend = "";

                                                                    if (response.length() > 0) {
                                                                        try {

                                                                            JSONObject info;
                                                                            /* Arreglos */
                                                                            // lista_reporteVenta: arreglo que contien los número de asientos vendidos
                                                                            // lista_reporteVentaGPS: arreglo que contiene los boletos vendidos
                                                                            ArrayList<String> lista_reporteVenta = new ArrayList<>();
                                                                            ArrayList<String> lista_reporteVentaGPS = new ArrayList<>();
                                                                            /* ----------------------------------------- */

                                                                            /* Se itera en función a la respuesta y se guarda el número de asiento y la data del boleto completo */
                                                                            for (int i = 0; i < response.length(); i++) {

                                                                                info = response.getJSONObject(i);
                                                                                if(info.getString("ServicioEmpresa").equals("VIAJE") && info.getString("LIBERADO").equals("NO")){

                                                                                    lista_reporteVenta.add(info.getString("NU_ASIE"));

                                                                                    asientosVend += info.getString("NU_ASIE") + "-" + info.getString("NU_DOCU") + "-" +
                                                                                            info.getString("CO_DEST_ORIG") + "-" + info.getString("CO_DEST_FINA") + "-" +
                                                                                            info.getString("CO_CLIE") + "-" + info.getString("IM_TOTA") + "-" +
                                                                                            info.getString("CO_EMPR") + "-" + info.getString("TI_DOCU") + "-" +
                                                                                            info.getString("LIBERADO") + "-" + info.getString("CARGA") + "-" +
                                                                                            info.getString("ServicioEmpresa") +"/";
                                                                                }
                                                                            }
                                                                            /* ----------------------------------------- */

                                                                            /* Se elimina el último "/" de la trama, se agrega al arreglo y se guarda en memoria */
                                                                            asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                                                            lista_reporteVentaGPS.add(asientosVend);
                                                                            String json_reporteVentaGPS = gson.toJson(lista_reporteVentaGPS);
                                                                            guardarDataMemoria("anf_jsonReporteVentaGPS", json_reporteVentaGPS, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se actualiza el arrelgo de asientos vendidos en ruta y se guarda en memoria */
                                                                            ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                                                                            String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                                                            guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se guarda el arreglo de asientos vendidos en memoria */
                                                                            String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                                                                            guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se actualiza la vista de venta de boletos */
                                                                            VentaBoletosFragment ventaBoletosFragment = new VentaBoletosFragment();
                                                                            FragmentManager fragmentManager = getFragmentManager();
                                                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                            fragmentTransaction.replace(R.id.fragment_base, ventaBoletosFragment).commit();
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
                                                            Toast.makeText(getActivity(), " asientos vendidos Ha ocurrido un problema durante la ejecución de la aplicación." +
                                                                    "" +
                                                                    "Por favor, salir y volver a intentar", Toast.LENGTH_SHORT).show();
                                                            editor.clear();
                                                            editor.commit();
                                                            queue.getCache().clear();
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
                                                    queue.add(jsonArrayRequestAsientosVendidos);
                                                    breakTime();
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
                        queue.add(jsonArrayRequestLiberacionAsientos);

                    }else{
                        Toast.makeText(getActivity(), "No hay asientos para liberar.", Toast.LENGTH_SHORT);
                    }
                    /* ----------------------------------------- */
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /* ----------------------------------------- */
        }
    }


    void RealizaVenta()
    {
        if (!ValidaStadoImpresora())
        {
            return;
        }else{
            button_imprimirBoleto.setEnabled(false);
            if (FlagValidaButton == 0) {
                FlagValidaButton = 1;
                final Spinner_model st = (Spinner_model) spinner_TipoDocumento.getSelectedItem();
                final Spinner_model so = (Spinner_model) spinner_origen.getSelectedItem();
                final Spinner_model sd = (Spinner_model) spinner_destino.getSelectedItem();
                if (so.id == "999") {
                    FlagValidaButton = 0;
                    button_imprimirBoleto.setEnabled(true);
                    Toast.makeText(getActivity(), "Seleccione Origen", Toast.LENGTH_SHORT).show();
                    return;
                } else if (sd.id == "999") {
                    FlagValidaButton = 0;
                    button_imprimirBoleto.setEnabled(true);
                    Toast.makeText(getActivity(), "Seleccione Destino", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!ValidacionDocumento(st.id)) {
                    return;
                } else {
                    try {
                        VentaAsyncrona runner = new VentaAsyncrona();
                        runner.execute(st.id,so.name.trim(),sd.name.trim());
                    } catch (Exception ex) {
                        progressDialog.show();
                    }
                }
            }
        }
    }




    private class VentaAsyncrona extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                String CorrelativoTIPO = "";
                //int numCorrelativoSeleccionado;
                //String numSerieSeleccionado;
                if (params[0] != "3") {
                    guardarDataMemoria("TipoVenta", "BOLETA", getContext());
                    //if (editText_dni.getText().toString().length() == 8) {


                    //Log.d("a", sharedPreferences.getString("anf_correlativoBltViaje", "0"));
                    CorrelativoTIPO = "anf_correlativoBltViaje";
                    numCorrelativoSeleccionado = Integer.valueOf(sharedPreferences.getString(CorrelativoTIPO, "0"));
                    numCorrelativoSeleccionado = numCorrelativoSeleccionado + 1;

                    /* Se actualiza el correlativo de viaje */
                    // editor.putString("anf_correlativoBltViaje", Integer.toString(numCorrelativoSeleccionado));
                    // editor.commit();
                    /* ----------------------------------------- */

                    numSerieSeleccionado = sharedPreferences.getString("anf_numSerieBltViaje", "NoData");

                    /*editor.putString("guardar_tipoDocumentoViaje", sharedPreferences.getString("anf_tipoDocumentoBltViaje", "NoData"));
                    editor.commit();*/
                    guardarDataMemoria("guardar_tipoDocumentoViaje",sharedPreferences.getString("anf_tipoDocumentoBltViaje","NoData"), getActivity());

                    //} else if (editText_dni.getText().toString().length() == 11) {
                } else {
                    guardarDataMemoria("TipoVenta", "FACTURA", getContext());
                    CorrelativoTIPO = "anf_correlativoFacViaje";
                    numCorrelativoSeleccionado = Integer.valueOf(sharedPreferences.getString(CorrelativoTIPO, "0"));
                    numCorrelativoSeleccionado = numCorrelativoSeleccionado + 1;

                    /* Se actualiza el correlativo de viaje */
                    // editor.putString("anf_correlativoFacViaje", Integer.toString(numCorrelativoSeleccionado));
                    // editor.commit();
                    /* ----------------------------------------- */

                    numSerieSeleccionado = sharedPreferences.getString("anf_numSerieFacViaje", "NoData");

                    /*editor.putString("guardar_tipoDocumentoViaje", sharedPreferences.getString("anf_tipoDocumentoFacViaje", "NoData"));
                    editor.commit();*/
                    guardarDataMemoria("guardar_tipoDocumentoViaje",sharedPreferences.getString("anf_tipoDocumentoFacViaje", "NoData"),getActivity());
                }
                /* ----------------------------------------- */

                                           /* editor.putString("guardar_correlativoViaje", Integer.toString(numCorrelativoSeleccionado));
                                            editor.commit();*/

                String numCorrelativoViajeCompleto = getNumCorrelativo(numCorrelativoSeleccionado);
                guardarDataMemoria("guardar_correlativoViajeCompleto",numCorrelativoViajeCompleto,getActivity());
                guardarDataMemoria("guardar_serieViaje",numSerieSeleccionado,getActivity());
                guardarDataMemoria("guardar_nombreCliente",editText_nombreCliente.getText().toString(),getActivity());
                guardarDataMemoria("guardar_numeroDocumento",editText_dni.getText().toString(),getActivity());
                guardarDataMemoria("guardar_numAsientoVendido",arrayPosicionAsientos.get(position),getActivity());
                guardarDataMemoria("guardar_RUC",editText_RUC.getText().toString().trim(),getActivity());
                guardarDataMemoria("guardar_RAZON_SOCIAL",editText_RazonSocial.getText().toString().trim(),getActivity());
                guardarDataMemoria("Origen_Texto",params[1].substring(2, params[1].length()),getActivity());
                guardarDataMemoria("Destino_Texto",params[2].substring(2, params[2].length()),getActivity());



                /*editor.putString("guardar_correlativoViajeCompleto", numCorrelativoViajeCompleto);
                editor.commit();

                editor.putString("guardar_serieViaje", numSerieSeleccionado);
                editor.commit();

                editor.putString("guardar_nombreCliente", editText_nombreCliente.getText().toString());
                editor.commit();

                editor.putString("guardar_numeroDocumento", editText_dni.getText().toString());
                editor.commit();

                editor.putString("guardar_numAsientoVendido", arrayPosicionAsientos.get(position));
                editor.commit();

                editor.putString("guardar_RUC", editText_RUC.getText().toString().trim());
                editor.commit();

                editor.putString("guardar_RAZON_SOCIAL", editText_RazonSocial.getText().toString().trim());
                editor.commit();

                editor.putString("Origen_Texto", params[1].substring(2, params[1].length()));
                editor.commit();

                editor.putString("Destino_Texto", params[2].substring(2, params[2].length()));
                editor.commit();*/


                final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                for (int i = 0; i < lista_empresas.size(); i++) {

                    String[] dataEmpresa = lista_empresas.get(i).split("-");
                    String codigo_empresa = sharedPreferences.getString("anf_codigoEmpresa", "NoData");

                    if (codigo_empresa.equals(dataEmpresa[0])) {
                        empresa_seleccionada = lista_empresas.get(i);
                    }
                }
                if (ValidaExiteCorrelativo(numSerieSeleccionado + "-" + numCorrelativoViajeCompleto, sharedPreferences.getString("anf_codigoEmpresa", "NoData")) == true) {
                    numCorrelativoViajeCompleto = completarCorrelativo(Integer.valueOf(numCorrelativoViajeCompleto) + 1);
                    guardarDataMemoria("guardar_correlativoViajeCompleto", numCorrelativoViajeCompleto, getActivity());
                    guardarDataMemoria(CorrelativoTIPO, String.valueOf(Integer.valueOf(numCorrelativoSeleccionado)), getActivity());
                }
                /* ----------------------------------------- */

                /* Se genera la trama del boleto de viaje y se obtiene la data encriptada */
                final String trama = generarTramaBoleto(sharedPreferences, numSerieSeleccionado, numCorrelativoViajeCompleto, empresa_seleccionada);
                //Log.d("trama",trama);
                String[] dataEncriptada = generarCodigoQR(trama);
                // dataEncriptada[0] = xml64
                // dataEncriptada[1] = ted64
                // dataEncriptada[2] = ted
                Date date = new Date();
                String ho_bol = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                final String TedQR = dataEncriptada[2] + "|" + sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "|"
                        + sharedPreferences.getString("anf_rumbo", "NoData") + "|"
                        + sharedPreferences.getString("guardar_origen", "NoData") + "|"
                        + sharedPreferences.getString("guardar_destino", "NoData") + "|"
                        + sharedPreferences.getString("guardar_numeroDocumento", "NoData") + "|"
                        + sharedPreferences.getString("guardar_nombreCliente", "NoData") + "|"
                        + sharedPreferences.getString("anf_secuencia", "NoData") + "|"
                        + sharedPreferences.getString("anf_fechaProgramacion", "NoData") + "|"
                        + sharedPreferences.getString("guardar_numAsientoVendido", "NoData") + "|"
                        + "VIAJE|" + ho_bol;
                /* ----------------------------------------- */

                Boolean[] respuesta = guardarCompraViaje(dataEncriptada[0], dataEncriptada[1],
                        numCorrelativoViajeCompleto);


                imprimir_boletas(spinner_origen, spinner_destino, TedQR, empresa_seleccionada, sharedPreferences, "Viaje");
                /* ----------------------------------------- */
                //FlagValidaButton = 0;
                //guardarDataMemoria("cantBoletos",String.valueOf(response.length()),getApplicationContext());
                int newVenta = Integer.parseInt(sharedPreferences.getString("cantBoletos", "NoData"));
                newVenta = newVenta + 1;
                guardarDataMemoria("cantBoletos", String.valueOf(newVenta), getActivity());
                guardarDataMemoria(CorrelativoTIPO, String.valueOf(Integer.valueOf(numCorrelativoViajeCompleto)), getActivity());



            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            button_imprimirBoleto.setEnabled(true);
            FlagValidaButton = 0;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("¿El pasajero viaja con carga?").setTitle("Carga");
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    boleto_dialog.hide();
                    editText_origen.setText(spinner_origen.getSelectedItem().toString());
                    editText_destino.setText(spinner_destino.getSelectedItem().toString());
                    editText_dniCarga.setText(editText_dni.getText().toString());
                    String numAsientoVendido = arrayPosicionAsientos.get(position);
                    lista_productos = getArray(sharedPreferences, gson, "json_productos");
                    final ArrayList<String> lista_idProductos = new ArrayList<>();
                    final ArrayList<String> lista_nombreProductos = new ArrayList<>();
                    for (int i = 0; i < lista_productos.size(); i++) {
                        String[] dataProductos = lista_productos.get(i).split("-");
                        lista_idProductos.add(dataProductos[0]);
                        lista_nombreProductos.add(dataProductos[1]);
                    }
                    ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(context_boletoCarga, android.R.layout.simple_spinner_item, lista_nombreProductos);
                    spinner_tipoProducto.setAdapter(adapter_spinner);
                    spinner_tipoProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String idProducto = lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                            String nombreProducto = lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                            guardarDataMemoria("guardar_idProducto",idProducto,getActivity());
                            guardarDataMemoria("guardar_nombreProducto",nombreProducto,getActivity());
                            /*editor.putString("guardar_idProducto", idProducto);
                            editor.putString("guardar_nombreProducto", nombreProducto);
                            editor.commit();*/
                            editText_tarifaBase.setText(String.format("%.2f", Float.valueOf("2.00")));
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    button_CancelaCarga.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //startActualizacionAsientosService();
                            button_imprimirBoleto.setEnabled(true);
                            button_imprimirBoletoCarga.setEnabled(true);
                            flag = false;
                            carga_dialog.dismiss();
                        }
                    });
                    button_imprimirBoletoCarga.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!ValidaStadoImpresora())
                            {
                                return;
                            }else {

                                button_imprimirBoletoCarga.setEnabled(false);
                                progressDialog2 = new ProgressDialog(getActivity());
                                progressDialog2.setTitle("Espere por favor");
                                progressDialog2.setMessage("Imprimiendo");
                                progressDialog2.setCancelable(false);
                                /* Hilo secundario que ejecuta todos los requests mientras está activado el cuadro de espera */
                                //getActivity().runOnUiThread();
                                Thread thread1 = new Thread(new Runnable() {
                                    //getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            /* Selección de serie y correlativo dependiendo si es DNI o RUC */
                                            if(sharedPreferences.getString("TipoVenta","NoData").equals("BOLETA"))
                                            {
                                                //if (editText_dni.getText().toString().length() == 8) {

                                                numCorrelativoCargaSeleccionado = Integer.valueOf(sharedPreferences.getString("anf_correlativoBolCarga", "0"));
                                                numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado + 1;
                                                editor.putString("anf_correlativoBolCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                                editor.commit();
                                                /* ----------------------------------------- */

                                                numSerieCargaSeleccionado = sharedPreferences.getString("anf_numSerieBolCarga", "NoData");

                                                editor.putString("guardar_tipoDocumentoCarga", sharedPreferences.getString("anf_tipoDocumentoBolCarga", "NoData"));
                                                editor.commit();


                                                //} else if (editText_dni.getText().toString().length() == 11) {
                                            } else if (sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {

                                                numCorrelativoCargaSeleccionado = Integer.valueOf(sharedPreferences.getString("anf_correlativoFacCarga", "0"));
                                                numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado + 1;

                                                /* Se actualiza el correlativo de Viaje */
                                                editor.putString("anf_correlativoFacCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                                editor.commit();
                                                /* ----------------------------------------- */

                                                numSerieCargaSeleccionado = sharedPreferences.getString("anf_numSerieFacCarga", "NoData");

                                                editor.putString("guardar_tipoDocumentoCarga", sharedPreferences.getString("anf_tipoDocumentoFacCarga", "NoData"));
                                                editor.commit();
                                            }
                                            /* ----------------------------------------- */

                                            editor.putString("guardar_correlativoCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                            editor.commit();

                                            editor.putString("guardar_serieCarga", numSerieCargaSeleccionado);
                                            editor.commit();

                                            String numCorrelativoCargaCompleto = getNumCorrelativo(numCorrelativoCargaSeleccionado);
                                            editor.putString("guardar_correlativoCargaCompleto", numCorrelativoCargaCompleto);
                                            editor.commit();

                                            /* Arreglo */
                                            // lista_empresas: arreglo que contiene los datos de ambas empresas
                                            final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                                            /* ----------------------------------------- */

                                            /* Selección de la empresa según itinerario */
                                            String empresa_seleccionada = "";
                                            String empresaTramaCarga = "";
                                            for (int i = 0; i < lista_empresas.size(); i++) {

                                                String[] data = lista_empresas.get(i).split("-");
                                                // data[0] = CODIGO_EMPRESA
                                                // data[1] = EMPRESA
                                                // data[2] = DIRECCION
                                                // data[3] = DEPARTAMENTO
                                                // data[4] = PROVINCIA
                                                // data[5] = RUC
                                                // data[6] = RAZON_SOCIAL
                                                String codigo_empresa = sharedPreferences.getString("anf_codigoEmpresa", "NoData");

                                                if (codigo_empresa.equals(data[0])) {
                                                    empresaTramaCarga = lista_empresas.get(i);
                                                    empresa_seleccionada = lista_empresas.get(i);
                                                }
                                            }
                                            /* ----------------------------------------- */

                                            /* Se calcula la tarifa total y se guarda en memoria */
                                            //float tarifaTotal = Integer.valueOf(editText_cantidad.getText().toString()) * (Float.valueOf(editText_tarifaCarga.getText().toString()));
                                            float tarifaTotal =  (Float.valueOf(editText_tarifaCarga.getText().toString()));
                                            editor.putString("guardar_tarifaTotal", Float.toString(tarifaTotal));
                                            editor.commit();
                                            /* ----------------------------------------- */

                                            editor.putString("guardar_cantidad", editText_cantidad.getText().toString());
                                            editor.commit();

                                            /* Se genera la trama del boleto de carga y se obtiene la data encriptada */
                                            final String trama = generarTramaCarga(sharedPreferences, numSerieCargaSeleccionado, numCorrelativoCargaCompleto, empresaTramaCarga);
                                            //Log.d("trama",trama);
                                            String[] dataEncriptada = generarCodigoQR(trama);
                                            // dataEncriptada[0]=xml64
                                            // dataEncriptada[1]=ted64
                                            // dataEncriptada[2]=ted
                                            /* ----------------------------------------- */
                                            Date date = new Date();
                                            String ho_bol = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                            //Log.d("hora_bol",ho_bol);
                                            final String TedQR =  dataEncriptada[2]+"|"+sharedPreferences.getString("anf_codigoEmpresa", "NoData")+"|"
                                                    +sharedPreferences.getString("anf_rumbo", "NoData")+"|"
                                                    +sharedPreferences.getString("guardar_origen", "NoData")+"|"
                                                    +sharedPreferences.getString("guardar_destino", "NoData")+"|"
                                                    +sharedPreferences.getString("guardar_numeroDocumento", "NoData")+"|"
                                                    +sharedPreferences.getString("guardar_nombreCliente", "NoData")+"|"
                                                    +sharedPreferences.getString("anf_secuencia", "NoData")+"|"
                                                    +sharedPreferences.getString("anf_fechaProgramacion", "NoData")+"|"
                                                    +sharedPreferences.getString("guardar_numAsientoVendido","NoData")+"|"
                                                    +"CARGA|"+ho_bol;
                                            //Log.d("QR_CARGAANF",TedQR);

                                            Boolean[] respuesta = guardarCompraCarga(dataEncriptada[0], dataEncriptada[1], numCorrelativoCargaCompleto,
                                                    button_imprimirBoletoCarga);

                                            /* Se obtiene el tipo de producto */
                                            String tipoProducto = "";
                                            for (int i = 0; i < lista_productos.size(); i++) {
                                                String[] dataProductos = lista_productos.get(i).split("-");
                                                // dataProductos[0] = TI_PROD
                                                // dataProductos[1]= DE_TIPO_PROD

                                                if (dataProductos[0].equals(sharedPreferences.getString("guardar_idProducto", "NoData"))) {
                                                    tipoProducto = dataProductos[1];
                                                    break;
                                                }
                                            }
                                            /* ----------------------------------------- */

                                            /* Impresión del boleto de carga y control */
                                            imprimir_boletasCarga(spinner_origen, spinner_destino, TedQR, empresa_seleccionada, tipoProducto, sharedPreferences, "Carga");
                                            //imprimir_boletasCarga(spinner_origen, spinner_destino, dataEncriptada[2], empresa_seleccionada, tipoProducto, sharedPreferences, "Control");
                                            /* ----------------------------------------- */

                                            /* Flag que activa el servicio de GPS luego que finalizó la venta */
                                            flag = false;
                                            /* ----------------------------------------- */
                                            //carga_dialog.hide();
                                            button_imprimirBoleto.setEnabled(true);
                                            button_imprimirBoletoCarga.setEnabled(true);
                                            progressDialog2.dismiss();

                                        } catch (Exception ex) {
                                            progressDialog2.dismiss();
                                            button_imprimirBoleto.setEnabled(true);
                                            progressDialog2.show();
                                        }
                                    }
                                });
                                thread1.start();
                                progressDialog2.show();
                                carga_dialog.hide();

                            }
                            /* ----------------------------------------- */
                        }
                    });
                    /* ----------------------------------------- */
                    carga_dialog.setCancelable(false);
                    carga_dialog.show();

                }
            });
            /* Botón carga "NO" */
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    boleto_dialog.hide();

                    /* Se agrega el asiento al arreglo de asientos vendidos en ruta y se guarda en memoria */

                                        /*String numAsientoVendido = arrayPosicionAsientos.get(position);
                                        lista_asientosVendidosRuta.add(numAsientoVendido);
                                        String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                        guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getActivity());*/

                    /* ----------------------------------------- */

                    /* Flag que activa el servicio de GPS luego que finalizó la venta */
                    flag = false;
                    /* ----------------------------------------- */

                    gridviewAsientos.invalidateViews();

                    button_imprimirBoleto.setEnabled(true);
                    //startActualizacionAsientosService();
                }
            });
            /* ----------------------------------------- */

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();

        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    "Imprimiendo Boleto",
                    "Espere...");
        }


        @Override
        protected void onProgressUpdate(String... text) {
            //inalResult.setText(text[0]);

        }
    }

}