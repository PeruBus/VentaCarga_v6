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
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
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
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.pax.dal.IDAL;
import com.pax.dal.IFingerprintReader;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.com.telefonica.soyuz.gps.LocationUpdatesService;
import pe.com.telefonica.soyuz.gps.Utils;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;
import static pe.com.telefonica.soyuz.gps.Utils.getDistanceText;

public class VentaVipExpress extends Fragment {
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

    private boolean mBound = false;

    private boolean flag = false;

    private IDAL dal;
    private IPrinter printer;

    Dialog boleto_dialog;
    Context context_boleto;

    TextView TextView_adicional_Vip;

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

    EditText editText_Tarifa_Adicional_SE;

    Button button_CancelaCarga;

    Spinner spinner_tipoProducto;

    Button button_imprimirBoletoCarga;

    SharedPreferences sharedPreferences;
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
    ProgressDialog progressDialog;
    static Spinner spinner = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
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

        TextView_adicional_Vip = boleto_dialog.findViewById(R.id.TextView_adicional_Vip);
        editText_Tarifa_Adicional_SE= boleto_dialog.findViewById(R.id.editText_tarifa_adicional_vip);

        TextView_RasonSocial=boleto_dialog.findViewById(R.id.textView7);
        TextView_RUC=boleto_dialog.findViewById(R.id.textView8);
        spinner_TipoDocumento = boleto_dialog.findViewById(R.id.spinner_tipoDocu);
        editText_RazonSocial = boleto_dialog.findViewById(R.id.editText_RazonSocial);
        editText_RUC = boleto_dialog.findViewById(R.id.editText_RUC);

        //para ruta
        /*TextView_adicional_Vip.setVisibility(View.GONE);
        editText_Tarifa_Adicional_SE.setVisibility(View.GONE);*/

        /*TextView_adicional_Vip.setVisibility(View.INVISIBLE);
        editText_Tarifa_Adicional_SE.setVisibility(View.INVISIBLE);*/
        editText_Tarifa_Adicional_SE.setText("0.00");
        editText_Tarifa_Adicional_SE.setEnabled(true);

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
        lista_asientosVendidos = getArray(sharedPreferences, gson, "anf_jsonReporteVenta");
        lista_asientosVendidosRuta = getArray(sharedPreferences, gson, "anf_jsonReporteVentaVIP");
        gridviewAsientos.setAdapter(new ImageAdapter(getActivity(), sharedPreferences.getInt("anf_numAsientos", 0),
                lista_asientosVendidosRuta, lista_asientosVendidos, sharedPreferences.getString("Modulo", "NoData"), arrayPosicionAsientos, numFilas, numCol));
        startBoletoService();
        gridviewAsientos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    final int posicion, long id) {
                final RequestQueue queue = Volley.newRequestQueue(getContext());

                Toast.makeText(getActivity(), arrayPosicionAsientos.get(posicion), Toast.LENGTH_SHORT).show();
                final String HO_SALI = sharedPreferences.getString("HO_SALI_SE","NoData").substring(11,13)+sharedPreferences.getString("HO_SALI_SE","NoData").substring(14,16);

                String ws_TWDOCU_BLOQ = getString(R.string.ws_ruta) + "TWDOCU_BLOQ/01/" + sharedPreferences.getString("CO_RUMB_SE","NoData") + "/"
                        + sharedPreferences.getString("FE_PROG_SE","NoData") + "/" + sharedPreferences.getString("NU_SECU_SE","NoData")+"/"
                        +arrayPosicionAsientos.get(posicion).toString()+"/"+sharedPreferences.getString("guardar_unidad","NoData")+"/"+sharedPreferences.getString("guardar_agencia","NoData")+"/"
                        +HO_SALI+"/"+sharedPreferences.getString("CO_TIPO_BUSS_SE","NoData")+"/"+sharedPreferences.getString("CO_VEHI_SE","NoData")+"/"
                        +sharedPreferences.getString("guardar_caja","NoData")+"/"+sharedPreferences.getString("CodUsuario","NoData")+"/"+"V";
                Log.d("asientos vendidos", ws_TWDOCU_BLOQ);
                JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_TWDOCU_BLOQ, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length() == 0) {
                                    Toast.makeText(getActivity(), "No Ahi Respuesta", Toast.LENGTH_LONG).show();

                                } else if (response.length() > 0) {
                                    try {
                                        JSONObject info;
                                        info = response.getJSONObject(0);
                                        if (info.getString("Respuesta").equals("false"))
                                        {
                                            Toast.makeText(getActivity(), info.getString("Mensaje"), Toast.LENGTH_LONG).show();
                                        }
                                        else if (info.getString("Respuesta").equals("true")) {
                                            position = posicion;
                                            flag = true;
                                            editText_nombreCliente.getText().clear();
                                            editText_dni.getText().clear();
                                            editText_tarifaCarga.getText().clear();
                                            final String CO_RUMB_SE_EXPRESS = sharedPreferences.getString("CO_RUMB_SE","NoData");
                                            final String ST_TIPO_SERV_EXPRESS = sharedPreferences.getString("ST_TIPO_SERV","NoData");
                                            final ArrayList<String> lista_destinos = getArray(sharedPreferences,gson, "json_destinos");
                                            final ArrayList<String> TDPROG_ESAG_VIP = getArray(sharedPreferences,gson,"TDPORG_ESAG_VIP");
                                            CargaDestinos(sharedPreferences,gson,lista_destinos,TDPROG_ESAG_VIP,sharedPreferences.getString("Dest_fina_se","NoData"));
                                            //CargaTipoDocumento();
                                            FuncionesAuxiliares.CargaTipoDocumento(spinner_TipoDocumento,getActivity());
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
                                                    for(int i=0;i< TDPROG_ESAG_VIP.size();i++)
                                                    {
                                                        String[] ST_TIPO_SERV =TDPROG_ESAG_VIP.get(i).split("ƒ");
                                                        if (ST_TIPO_SERV[2].equals(ST_TIPO_SERV_EXPRESS) && ST_TIPO_SERV[0].equals(String.valueOf(st.id)))
                                                        {
                                                            for(int j=0;j<lista_destinos.size();j++)
                                                            {
                                                                String[] CO_DEST_FINA_AGEN_VIP = lista_destinos.get(j).split("-");
                                                                if(CO_DEST_FINA_AGEN_VIP[0].equals(ST_TIPO_SERV[1]))
                                                                {
                                                                    Spinner_model CO_DEST_ORIG_ = new Spinner_model(CO_DEST_FINA_AGEN_VIP[0], CO_DEST_FINA_AGEN_VIP[0] + "-" + CO_DEST_FINA_AGEN_VIP[1], "");
                                                                    Destino.add(CO_DEST_ORIG_);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                                                            android.R.layout.simple_spinner_item, Destino);
                                                    spinner_destino.setAdapter(spinnerArrayAdapter);
                                                    Spinner_model stdest = (Spinner_model)spinner_destino.getSelectedItem();
                                                    calculaTarifa(String.valueOf(st.id),String.valueOf(stdest.id), editText_tarifa, button_imprimirBoleto,ST_TIPO_SERV_EXPRESS,CO_RUMB_SE_EXPRESS);
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
                                                    calculaTarifa(String.valueOf(CO_DEST_ORIG.id),String.valueOf(CO_DEST_FINA.id), editText_tarifa, button_imprimirBoleto,ST_TIPO_SERV_EXPRESS,CO_RUMB_SE_EXPRESS);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {
                                                }
                                            });
                                            button_imprimirBoleto.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (!ValidaStadoImpresora())
                                                    {
                                                        return;
                                                    }
                                                    button_imprimirBoleto.setEnabled(false);
                                                    if (FlagValidaButton == 0) {
                                                        FlagValidaButton =1;
                                                        final Spinner_model st = (Spinner_model)spinner_TipoDocumento.getSelectedItem();
                                                        final Spinner_model so = (Spinner_model)spinner_origen.getSelectedItem();
                                                        final Spinner_model sd = (Spinner_model)spinner_destino.getSelectedItem();
                                                        if(so.id == "999")
                                                        {
                                                            FlagValidaButton = 0;
                                                            button_imprimirBoleto.setEnabled(true);
                                                            Toast.makeText(getActivity(), "Seleccione Origen", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }else if( sd.id =="999")
                                                        {
                                                            FlagValidaButton = 0;
                                                            button_imprimirBoleto.setEnabled(true);
                                                            Toast.makeText(getActivity(), "Seleccione Destino", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }else if(!ValidacionDocumento(st.id)){
                                                            return;
                                                        }else {
                                                            progressDialog = new ProgressDialog(getActivity());
                                                            progressDialog.setTitle("Espere por favor");
                                                            progressDialog.setMessage("Imprimiendo");
                                                            progressDialog.setCancelable(false);
                                                            Thread thread = new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        String CorrelativoTIPO = "";
                                                                        if(st.id != "3"){
                                                                            guardarDataMemoria("TipoVenta","BOLETA",getContext());
                                                                            CorrelativoTIPO="guardar_correlativoViajeBLT01";
                                                                            numCorrelativoSeleccionado = Integer.valueOf(sharedPreferences.getString(CorrelativoTIPO, "0"));
                                                                            numCorrelativoSeleccionado = numCorrelativoSeleccionado + 1;
                                                                            numSerieSeleccionado = sharedPreferences.getString("guardar_serieViajeBLT01", "NoData");
                                                                            editor.putString("guardar_tipoDocumentoViaje", "BLT");
                                                                            editor.commit();
                                                                        } else {
                                                                            guardarDataMemoria("TipoVenta","FACTURA",getContext());
                                                                            CorrelativoTIPO="guardar_correlativoViajeFAC01";
                                                                            numCorrelativoSeleccionado = Integer.valueOf(sharedPreferences.getString(CorrelativoTIPO, "0"));
                                                                            numCorrelativoSeleccionado = numCorrelativoSeleccionado + 1;
                                                                            numSerieSeleccionado = sharedPreferences.getString("guardar_serieViajeFAC01", "NoData");
                                                                            editor.putString("guardar_tipoDocumentoViaje", "BLT");
                                                                            editor.commit();
                                                                        }
                                                                        String numCorrelativoViajeCompleto = getNumCorrelativo(numCorrelativoSeleccionado);
                                                                        editor.putString("guardar_correlativoViajeCompleto", numCorrelativoViajeCompleto);
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

                                                                        editor.putString("Origen_Texto", so.name.trim().substring(2,so.name.length()));
                                                                        editor.commit();

                                                                        editor.putString("Destino_Texto", sd.name.trim().substring(2,sd.name.length()));
                                                                        editor.commit();



                                                                        final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                                                                        final String codigo_empresa = "01";
                                                                        for (int i = 0; i < lista_empresas.size(); i++) {
                                                                            String[] dataEmpresa = lista_empresas.get(i).split("-");
                                                                            if (codigo_empresa.equals(dataEmpresa[0])) {
                                                                                empresa_seleccionada = lista_empresas.get(i);
                                                                            }
                                                                        }
                                                                        if (ValidaExiteCorrelativo(numSerieSeleccionado+"-"+numCorrelativoViajeCompleto,codigo_empresa) == true)
                                                                        {
                                                                            numCorrelativoViajeCompleto = completarCorrelativo(Integer.valueOf(numCorrelativoViajeCompleto)+1);
                                                                            guardarDataMemoria("guardar_correlativoViajeCompleto", numCorrelativoViajeCompleto, getActivity());
                                                                            guardarDataMemoria(CorrelativoTIPO, String.valueOf(Integer.valueOf(numCorrelativoSeleccionado)), getActivity());
                                                                        }
                                                                        final String trama = generarTramaBoleto(sharedPreferences, numSerieSeleccionado, numCorrelativoViajeCompleto, empresa_seleccionada);
                                                                        Log.d("trama",trama);
                                                                        String[] dataEncriptada = generarCodigoQR(trama);
                                                                        Date date = new Date();
                                                                        String ho_bol = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                                                        final String TedQR =  dataEncriptada[2]+"|01|"
                                                                                +CO_RUMB_SE_EXPRESS+"|"
                                                                                +sharedPreferences.getString("guardar_origen", "NoData")+"|"
                                                                                +sharedPreferences.getString("guardar_destino", "NoData")+"|"
                                                                                +sharedPreferences.getString("guardar_numeroDocumento", "NoData")+"|"
                                                                                +sharedPreferences.getString("guardar_nombreCliente", "NoData")+"|"
                                                                                +sharedPreferences.getString("anf_secuencia", "NoData")+"|"
                                                                                +sharedPreferences.getString("anf_fechaProgramacion", "NoData")+"|"
                                                                                +sharedPreferences.getString("guardar_numAsientoVendido","NoData")+"|"
                                                                                +"VIAJE|"+ho_bol;
                                                                        /* ----------------------------------------- */

                                                                        Boolean[] respuesta = guardarCompraViaje(dataEncriptada[0], dataEncriptada[1],
                                                                                numCorrelativoViajeCompleto,CO_RUMB_SE_EXPRESS);
                                                                        imprimir_boletas(spinner_origen, spinner_destino, TedQR, empresa_seleccionada, sharedPreferences, "Viaje");
                                                                        FlagValidaButton=0;
                                                                        int newVenta = Integer.parseInt(sharedPreferences.getString("cantBoletos", "NoData"));
                                                                        newVenta = newVenta+1;
                                                                        guardarDataMemoria("cantBoletos",String.valueOf(newVenta),getActivity());
                                                                        guardarDataMemoria(CorrelativoTIPO, String.valueOf(Integer.valueOf(numCorrelativoViajeCompleto)), getActivity());
                                                                        String numAsientoVendido = arrayPosicionAsientos.get(position);
                                                                        lista_asientosVendidosRuta.add(numAsientoVendido);
                                                                        String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                                                        guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getActivity());

                                                                        //gridviewAsientos.invalidateViews();


                                                                        final RequestQueue queue1 = Volley.newRequestQueue(getContext());
                                                                        final String HO_SALI = sharedPreferences.getString("HO_SALI_SE","NoData").substring(11,13)+sharedPreferences.getString("HO_SALI_SE","NoData").substring(14,16);
                                                                        String ws_TWDOCU_BLOQ = getString(R.string.ws_ruta) + "TWDOCU_BLOQ/01/" + sharedPreferences.getString("CO_RUMB_SE","NoData") + "/"
                                                                                + sharedPreferences.getString("FE_PROG_SE","NoData") + "/" + sharedPreferences.getString("NU_SECU_SE","NoData")+"/"
                                                                                +arrayPosicionAsientos.get(posicion).toString()+"/"+sharedPreferences.getString("guardar_unidad","NoData")+"/"+sharedPreferences.getString("guardar_agencia","NoData")+"/"
                                                                                +HO_SALI+"/"+sharedPreferences.getString("CO_TIPO_BUSS_SE","NoData")+"/"+sharedPreferences.getString("CO_VEHI_SE","NoData")+"/"
                                                                                +sharedPreferences.getString("guardar_caja","NoData")+"/"+sharedPreferences.getString("CodUsuario","NoData")+"/"+"D";
                                                                        Log.d("asientos vendidos", ws_TWDOCU_BLOQ);
                                                                        JsonArrayRequest jsonArrayRequestLiberaAsiento = new JsonArrayRequest(Request.Method.GET, ws_TWDOCU_BLOQ, null,
                                                                                new Response.Listener<JSONArray>() {
                                                                                    @Override
                                                                                    public void onResponse(JSONArray response) {
                                                                                        if (response.length() == 0) {
                                                                                            Toast.makeText(getActivity(), "No Ahi Respuesta", Toast.LENGTH_LONG).show();

                                                                                        } else if (response.length() > 0) {
                                                                                            try{
                                                                                                final JSONObject info;
                                                                                                info = response.getJSONObject(0);
                                                                                                if (info.getString("Respuesta").equals("true"))
                                                                                                {
                                                                                                    Toast.makeText(getActivity(), info.getString("Mensaje"), Toast.LENGTH_LONG).show();
                                                                                                    button_imprimirBoleto.setEnabled(true);
                                                                                                    boleto_dialog.hide();
                                                                                                    progressDialog.dismiss();
                                                                                                    gridviewAsientos.invalidateViews();
                                                                                                }else{
                                                                                                    Toast.makeText(getActivity(), "No se puede liberar valide conexión", Toast.LENGTH_LONG).show();
                                                                                                }
                                                                                            }catch (Exception ex)
                                                                                            {

                                                                                            }
                                                                                        }
                                                                                    }

                                                                                }, new Response.ErrorListener() {
                                                                            @Override
                                                                            public void onErrorResponse(VolleyError error) {
                                                                                error.printStackTrace();
                                                                                Toast.makeText(getActivity(), "Error en la ws TWDOCU_BLOQ.", Toast.LENGTH_LONG).show();

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
                                                                        jsonArrayRequestLiberaAsiento.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                                        queue1.add(jsonArrayRequestLiberaAsiento);
                                                                    } catch (Exception ex) {
                                                                        progressDialog.show();
                                                                    }
                                                                }
                                                            });
                                                            thread.start();
                                                            progressDialog.show();
                                                        }
                                                    }
                                                }
                                            });
                                            button_cancelarVentaboleto.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    final RequestQueue queue1 = Volley.newRequestQueue(getContext());
                                                    final String HO_SALI = sharedPreferences.getString("HO_SALI_SE","NoData").substring(11,13)+sharedPreferences.getString("HO_SALI_SE","NoData").substring(14,16);
                                                    String ws_TWDOCU_BLOQ = getString(R.string.ws_ruta) + "TWDOCU_BLOQ/01/" + sharedPreferences.getString("CO_RUMB_SE","NoData") + "/"
                                                            + sharedPreferences.getString("FE_PROG_SE","NoData") + "/" + sharedPreferences.getString("NU_SECU_SE","NoData")+"/"
                                                            +arrayPosicionAsientos.get(posicion).toString()+"/"+sharedPreferences.getString("guardar_unidad","NoData")+"/"+sharedPreferences.getString("guardar_agencia","NoData")+"/"
                                                            +HO_SALI+"/"+sharedPreferences.getString("CO_TIPO_BUSS_SE","NoData")+"/"+sharedPreferences.getString("CO_VEHI_SE","NoData")+"/"
                                                            +sharedPreferences.getString("guardar_caja","NoData")+"/"+sharedPreferences.getString("CodUsuario","NoData")+"/"+"D";
                                                    Log.d("asientos vendidos", ws_TWDOCU_BLOQ);
                                                    JsonArrayRequest jsonArrayRequestLiberaAsiento = new JsonArrayRequest(Request.Method.GET, ws_TWDOCU_BLOQ, null,
                                                            new Response.Listener<JSONArray>() {
                                                                @Override
                                                                public void onResponse(JSONArray response) {
                                                                    if (response.length() == 0) {
                                                                        Toast.makeText(getActivity(), "No Ahi Respuesta", Toast.LENGTH_LONG).show();

                                                                    } else if (response.length() > 0) {
                                                                        try{
                                                                            final JSONObject info;
                                                                            info = response.getJSONObject(0);
                                                                            if (info.getString("Respuesta").equals("true"))
                                                                            {
                                                                                Toast.makeText(getActivity(), info.getString("Mensaje"), Toast.LENGTH_LONG).show();
                                                                                button_imprimirBoleto.setEnabled(true);
                                                                                button_imprimirBoletoCarga.setEnabled(true);
                                                                                flag = false;
                                                                                boleto_dialog.dismiss();
                                                                            }else{
                                                                                Toast.makeText(getActivity(), "No se puede liberar valide conexión", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }catch (Exception ex)
                                                                        {

                                                                        }
                                                                    }
                                                                }

                                                            }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            error.printStackTrace();
                                                            Toast.makeText(getActivity(), "Error en la ws TWDOCU_BLOQ.", Toast.LENGTH_LONG).show();

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
                                                    jsonArrayRequestLiberaAsiento.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                    queue1.add(jsonArrayRequestLiberaAsiento);
                                                }
                                            });
                                            /* ----------------------------------------- */
                                            boleto_dialog.show();
                                            boleto_dialog.setCancelable(false);

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
                        Toast.makeText(getActivity(), "Error en la ws TWDOCU_BLOQ.", Toast.LENGTH_LONG).show();

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
                jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonArrayRequestAsientosVendidos);
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
    public void CargaDestinos(final SharedPreferences sp, final Gson gs, final ArrayList<String> lista_destinos, final ArrayList<String> PermisoVip, final String CO_DEST_ORIG_AGEN)
    {
        try{

            final List<Spinner_model> Origen = new ArrayList<>();
            /*Spinner_model CO_DEST_ORIG = new Spinner_model("999", "SELECCIONAR", "");
            Origen.add(CO_DEST_ORIG);*/


            for(int i=0;i<lista_destinos.size();i++)
            {
                String[] CO_DEST_ORIG_AGEN_VIP = lista_destinos.get(i).split("-");
                if(CO_DEST_ORIG_AGEN_VIP[0].equals(CO_DEST_ORIG_AGEN))
                {
                    Spinner_model CO_DEST_ORIG_ = new Spinner_model(CO_DEST_ORIG_AGEN_VIP[0], CO_DEST_ORIG_AGEN_VIP[0] + "-" + CO_DEST_ORIG_AGEN_VIP[1], "");
                    Origen.add(CO_DEST_ORIG_);
                }

            }
            ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,Origen);
            spinner_origen.setAdapter(spinnerArray);
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


    public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey) {

        String json = sharedPreferences.getString(jsonKey, "NoData");
        //Log.d("json buscado", json);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        ArrayList<String> lista = new ArrayList<>();

        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }

        return lista;
    }

    public String getNumCorrelativo(int numCorrelativoBLT) {

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



    public void calculaTarifa(final String origen, final String destino, final EditText editText_tarifa, final Button button_imprimir,final String TI_SERV,final String CO_RUMB_SE) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        JSONArray getTarifa_express = null;
        guardarDataMemoria("guardar_origen",origen,getActivity());
        guardarDataMemoria("guardar_destino",destino,getActivity());
        try{
            getTarifa_express = new JSONArray(sharedPreferences.getString("bol_getTarifa01", "NoData"));
            for(int i = 0;i< getTarifa_express.length();i++){
                if (getTarifa_express.getJSONObject(i).getString("CO_DEST_ORIG").equals(origen) &&
                        getTarifa_express.getJSONObject(i).getString("CO_DEST_FINA").equals(destino) &&
                        getTarifa_express.getJSONObject(i).getString("TI_SERV").equals(TI_SERV) &&
                        getTarifa_express.getJSONObject(i).getString("CO_RUMB").equals(CO_RUMB_SE))
                {
                    editor.putString("guardar_tarifa", getTarifa_express.getJSONObject(i).getString("PR_BASE_ACTU").toString());
                    editor.commit();
                    editText_tarifa.setText(getTarifa_express.getJSONObject(i).getString("PR_BASE_ACTU").toString()+".00");
                    button_imprimir.setEnabled(true);
                    break;
                }else{
                    editText_tarifa.setText("0.00");
                    editor.putString("guardar_tarifa", "0");
                    editor.commit();
                    button_imprimir.setEnabled(true);
                }
            }
        }catch(Exception ex)
        {

        }
    }

    public boolean ValidaStadoImpresora()
    {
        try{
            //printer.init();
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
    public void imprimir_boletas(Spinner spinner_origen, Spinner spinner_destino, String ted, final String empresa_seleccionada, SharedPreferences sharedPreferences_, String tipoBoleta) {
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
        boleta.setEmpesa_imp("01");
        boleta.SetPrueba(getString(R.string.ws_ticket));
        boleta.SetRUC(sharedPreferences.getString("guardar_RUC","NoData"));
        boleta.SetRazonSocial(sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData"));
        boleta.SetDocuElectronico(sharedPreferences.getString("TipoVenta","NoData"));
        try {
            printer.init();
            printer.printStr(boleta.getVoucher(), null);
            printer.printBitmap(boleta.getQRBitmap(ted));
            printer.printStr(boleta.margenFinal(), null);
            printer.printStr("\n\n\n\n\n", null);
            int iRetError = printer.start();
            if (iRetError != 0x00) {
            }
            //printer.cutPaper(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();
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
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    public void stopBoletoService() {
        BoletoService.startService(getActivity(), false);
    }
    public Boolean[] guardarCompraViaje(String xml64, String ted64,
                                        String numCorrelativoViajeCompleto,final String CO_RUMB_SE_EXPRESS) {

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
        final JSONObject jsonObject = generarJSONViaje(sharedPreferences, fechaVenta, horaVenta, xml64, ted64, numSerieSeleccionado, numCorrelativoViajeCompleto, Integer.toString(numCorrelativoSeleccionado),CO_RUMB_SE_EXPRESS);
        //Log.d("respuesta", jsonObject.toString());
        /* ----------------------------------------- */
        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "viaje");
        cv.put("liberado", "No");
        cv.put("nu_docu",numSerieSeleccionado + "-" + numCorrelativoViajeCompleto);
        cv.put("ti_docu","BLT");
        cv.put("co_empr","01");
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
            cv.put("puesto", "boletero");
        }else{
            cv.put("puesto", "anfitrion");
        }

        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
        String ws_postVenta = getString(R.string.ws_ruta) + "SetVentaRuta";
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
                                     if (info.getString("Respuesta").equals("GUARDADO")) {
                                        respuesta[0] = true;
                                    } else {
                                        Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
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
        //breakTime();
        return respuesta;
    }




    public JSONObject generarJSONViaje(SharedPreferences sharedPreferences_, String fechaVenta, String horaVenta, String xml64, String ted64,
                                       String numSerieSeleccionado, String numCorrelativoViajeCompleto, String numCorrelativoSeleccionado,final String CO_RUMB_SE_EXPRESS) {

        JSONObject jsonObject = new JSONObject();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            // Log.d("jsonViahje",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            jsonObject.put("Empresa", "01");
            jsonObject.put("tipoDocumento", "BLT");
            jsonObject.put("NumeroDocumento", numSerieSeleccionado + "-" + numCorrelativoViajeCompleto);
            jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad","NoData"));
            jsonObject.put("Agencia",  sharedPreferences.getString("guardar_agencia","NoData"));
            jsonObject.put("CondicionPago", "CCE");
            jsonObject.put("MonedaTipo", "SOL");
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("RumboItinerario", CO_RUMB_SE_EXPRESS);
            jsonObject.put("OrigenBoleto", sharedPreferences.getString("guardar_origen", "NoData"));
            jsonObject.put("DestinoBoleto", sharedPreferences.getString("guardar_destino", "NoData"));
            jsonObject.put("SecuenciaItin", sharedPreferences.getString("NU_SECU_SE", "NoData"));

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
            jsonObject.put("TipoServicioItin", sharedPreferences.getString("ST_TIPO_SERV", "NoData"));
            jsonObject.put("Asiento", sharedPreferences.getString("guardar_numAsientoVendido", "NoData"));
            jsonObject.put("FechaViajeItin", sharedPreferences.getString("FE_PROG_SE", "NoData"));
            jsonObject.put("horaViajeItin", sharedPreferences.getString("HO_SALI_SE","NoData").substring(11,13)+sharedPreferences.getString("HO_SALI_SE","NoData").substring(14,16));
            jsonObject.put("Precio", sharedPreferences.getString("guardar_tarifa", "NoData"));
            jsonObject.put("UsuarioRegistro", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("Correlativo", numCorrelativoSeleccionado);
            jsonObject.put("Caja",sharedPreferences.getString("guardar_caja","NoData"));
            jsonObject.put("TipoVenta", "Boletero");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para venta de boleto de viaje.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

        return jsonObject;
    }






    public String generarTramaBoleto(SharedPreferences sharedPreferences, String numSerieBLT, String numCorrelativoViajeCompleto, String empresaTrama) {

        numCorrelativoViajeCompleto = numCorrelativoViajeCompleto.substring(2);
        String[] empresaSeleccionada = empresaTrama.split("-");
        String tipoDocumento = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }
        String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }
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
        String direccionCliente = "";
        if (sharedPreferences.getString("guardar_direccionCliente", "NoData").equals("")) {
            direccionCliente = "-";
        } else {
            direccionCliente = sharedPreferences.getString("guardar_direccionCliente", "NoData");
        }
        Date date = new Date();
        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";
        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);
        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        String numeroFloat = String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")));
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        String tramaBoleto ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + numSerieBLT + "\n" +
                "A;Correlativo;;" + numCorrelativoViajeCompleto + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                "A;RUTRecep;;" + DocuDeclara + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;-\n" +
                "A;DirRecepUrbaniza;;NoData"+"\n"+
                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;0.00\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "A;MntTotal;;" + String.format("%.2f",Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData"))) +"\n" +
                "A;MntTotalIgv;;0.00\n" +
                "A;TipoOperacion;;0101\n" +
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
                "E;DescripcionAdicSunat;31;www.perubus.com.pe/ Telf: 2052370\n"+
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







}
