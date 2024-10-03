package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class Venta_Carga_enco extends Fragment {
    private IDAL dal;
    private IPrinter printer;

    /**
     * Constantes para la interaccion con el APK de DigiFlow.
     */
    public static final String SOLICITA_TED = "ted";
    public static final String PATH_XML = "xml";
    public static final String COLUMN_NAME_XML64 = "xml64";
    public static final String COLUMN_NAME_TED = "ted";
    public static final String COLUMN_NAME_TED64 = "ted64";
    ListView listView;
    private Boolean existe = false;
    private SharedPreferences sharedPreferences;
    String numSerieCargaSeleccionado = "";
    ProgressDialog progressDialog;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    Gson gson;
    Boolean boletoEncontrado = false;
    int numCorrelativoCargaSeleccionado = 0;
    String empresaSeleccionada = "";


    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.venta_carga_enco, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Spinner spinner_tipoProducto = view.findViewById(R.id.spinner_tipoProducto);
        final Button btn_vali = view.findViewById(R.id.btn_vali);
        final Button btn_escaner = view.findViewById(R.id.btn_escaner);
        final Button btn_impri = view.findViewById(R.id.btn_impri);
        final EditText etex_seri = view.findViewById(R.id.etex_seri);
        final EditText etex_corr = view.findViewById(R.id.etex_corr);
        final EditText etex_cocli = view.findViewById(R.id.etex_cocli);
        final EditText etex_TARIFA = view.findViewById(R.id.etex_TARIFA);
        final EditText etex_CANTID = view.findViewById(R.id.etex_CANTID);
        final TextView TXT_CLIE = view.findViewById(R.id.TXT_CLIE);
        final TextView TXT_DOCUME = view.findViewById(R.id.TXT_DOCUME);



        try {
            dal = NeptuneLiteUser.getInstance().getDal(getContext());
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
        } catch (Exception e) {

        }


        gson = new Gson();

        final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
        final ArrayList<String> lista_idProductos = new ArrayList<>();
        final ArrayList<String> lista_nombreProductos = new ArrayList<>();
        /* ----------------------------------------- */

        /* Se itera en función a la lista de productos y se agrega data a los arreglos de IDs y nombre */
        for (int i = 0; i < lista_productos.size(); i++) {
            String[] dataProductos = lista_productos.get(i).split("-");
            // dataProductos[0] = TI_PROD
            // dataProductos[1]= DE_TIPO_PROD

            lista_idProductos.add(dataProductos[0]);
            lista_nombreProductos.add(dataProductos[1]);

        }


        /* ----------------------------------------- */


        btn_escaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try{
                    final IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = dal.getScanner(EScannerType.REAR);
                    if (iScanner.open()) {
                        iScanner.start(new IScanner.IScanListener() {
                            @Override
                            public void onRead(String codigoQR) {
                                final String[] dataCodigoQR = codigoQR.split("\\|");
                                Log.d("TramaQR",dataCodigoQR.toString());
                                String Evento="";
                                Log.d("trama",codigoQR);
                                if(codigoQR.length()<=8){
                                    Toast.makeText(getActivity(),"BUSCANDO DNI "+dataCodigoQR[0], Toast.LENGTH_LONG).show();
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String CodigoCliente = dataCodigoQR[0];
                                    final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota4/"+CodigoCliente+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Espere");
                                    progressDialog.setMessage("Buscando Documento");
                                    progressDialog.setCancelable(false);
                                    MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);
                                                            final String NuDocu = json.getString("NU_DOCU");
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoTipo = json.getString("TIPO");
                                                            final String CoEsta = json.getString("ESTADO");
                                                            final String FeEmis = json.getString("D_EMISION");
                                                            final String HoEmis = json.getString("H_EMISION");
                                                            final String DeAgen = json.getString("DE_AGEN");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String NuSecu = json.getString("NU_SECU");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String ImTota = json.getString("IM_TOTA");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String CoTrab = json.getString("VENDEDOR");
                                                            final String NuDoCa = json.getString("NU_DOCU_CARG");


                                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            guardarDataMemoria("extra_empresa", "01");
                                                            //CAMBIAR ESTO!!!
                                                            guardarDataMemoria("extra_tipoDocumentoBoleto", "BOL");
                                                            guardarDataMemoria("extra_numDocumentoBoleto", NuDocu);
                                                            guardarDataMemoria("extra_numDocumSerie", NuDocu.substring(0,4));
                                                            guardarDataMemoria("extra_numDocumCorre", NuDocu.substring(6,15));
                                                            //guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaDocumento"));
                                                            guardarDataMemoria("extra_fechaProgramacion", FeViaj);
                                                            guardarDataMemoria("extra_RUC",CoClie);
                                                            guardarDataMemoria("extra_RazonSocial",NoClie);
                                                            guardarDataMemoria("extra_rumbo", CoRumb);
                                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                                            guardarDataMemoria("extra_asiento", NuAsie);
                                                            guardarDataMemoria("extra_servicio", "N");
                                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                                            guardarDataMemoria("extra_origen", CoOrig);
                                                            guardarDataMemoria("extra_destino", CoDest);
                                                            guardarDataMemoria("extra_deorigen", DeOrig);
                                                            guardarDataMemoria("extra_dedestino", DeDest);
                                                            guardarDataMemoria("extra_nombreCliente", NoPasa);
                                                            guardarDataMemoria("extra_horaViajeItin", "0000");
                                                            guardarDataMemoria("extra_serieDocu", NuDoCa.substring(0,4));
                                                            guardarDataMemoria("extra_correDocu", NuDoCa.substring(6,15));
                                                            guardarDataMemoria("extra_idProducto", lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                                                            guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition()));

                                                            etex_seri.setText(NuDocu.substring(0,4));
                                                            etex_corr.setText(NuDocu.substring(6,15));
                                                            etex_cocli.setText(CoClie);
                                                            TXT_CLIE.setText(NoClie);
                                                            TXT_DOCUME.setText(NuDoCa);
                                                            etex_TARIFA.setText(String.format("%.2f", Float.valueOf("2.00")));

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
                                    RequestBuscaBol2.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(RequestBuscaBol2);
                                }
                                else if(dataCodigoQR[2].length()==4 && dataCodigoQR[3].length()==8){
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String CorrelativoCompleto = dataCodigoQR[2]+"-00"+dataCodigoQR[3];
                                    Toast.makeText(getActivity(),"BUSCANDO BOLETO "+CorrelativoCompleto, Toast.LENGTH_LONG).show();
                                    final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota3/"+CorrelativoCompleto+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Espere");
                                    progressDialog.setMessage("Buscando Documento");
                                    progressDialog.setCancelable(false);
                                    MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);
                                                            final String NuDocu = json.getString("NU_DOCU");
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoTipo = json.getString("TIPO");
                                                            final String CoEsta = json.getString("ESTADO");
                                                            final String FeEmis = json.getString("D_EMISION");
                                                            final String HoEmis = json.getString("H_EMISION");
                                                            final String DeAgen = json.getString("DE_AGEN");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String NuSecu = json.getString("NU_SECU");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String ImTota = json.getString("IM_TOTA");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String CoTrab = json.getString("VENDEDOR");
                                                            final String NuDoCa = json.getString("NU_DOCU_CARG");


                                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            guardarDataMemoria("extra_empresa", "01");
                                                            //CAMBIAR ESTO!!!
                                                            guardarDataMemoria("extra_tipoDocumentoBoleto", "BOL");
                                                            guardarDataMemoria("extra_numDocumentoBoleto", NuDocu);
                                                            guardarDataMemoria("extra_numDocumSerie", NuDocu.substring(0,4));
                                                            guardarDataMemoria("extra_numDocumCorre", NuDocu.substring(6,15));
                                                            //guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaDocumento"));
                                                            guardarDataMemoria("extra_fechaProgramacion", FeViaj);
                                                            guardarDataMemoria("extra_RUC",CoClie);
                                                            guardarDataMemoria("extra_RazonSocial",NoClie);
                                                            guardarDataMemoria("extra_rumbo", CoRumb);
                                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                                            guardarDataMemoria("extra_asiento", NuAsie);
                                                            guardarDataMemoria("extra_servicio", "N");
                                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                                            guardarDataMemoria("extra_origen", CoOrig);
                                                            guardarDataMemoria("extra_destino", CoDest);
                                                            guardarDataMemoria("extra_deorigen", DeOrig);
                                                            guardarDataMemoria("extra_dedestino", DeDest);
                                                            guardarDataMemoria("extra_nombreCliente", NoPasa);
                                                            guardarDataMemoria("extra_horaViajeItin", "0000");
                                                            guardarDataMemoria("extra_serieDocu", NuDoCa.substring(0,4));
                                                            guardarDataMemoria("extra_correDocu", NuDoCa.substring(6,15));
                                                            guardarDataMemoria("extra_idProducto", lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                                                            guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition()));

                                                            etex_seri.setText(NuDocu.substring(0,4));
                                                            etex_corr.setText(NuDocu.substring(6,15));
                                                            etex_cocli.setText(CoClie);
                                                            TXT_CLIE.setText(NoClie);
                                                            TXT_DOCUME.setText(NuDoCa);
                                                            etex_TARIFA.setText(String.format("%.2f", Float.valueOf("2.00")));

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
                                    RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(RequestBuscaBol1);
                                }

                            }
                            @Override
                            public void onFinish() {}
                            @Override
                            public void onCancel() {}
                        });
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al scanear el boleto.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                }
            }
        });

        btn_vali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (etex_cocli.getText().toString().equals("") ){
                    if (etex_seri.getText().toString().equals("") ){
                        Toast.makeText(getActivity(),"INGRESE UNA SERIE",Toast.LENGTH_LONG).show();
                        btn_vali.setEnabled(true);
                        return;
                    }else if(etex_corr.getText().toString().equals("")){
                        Toast.makeText(getActivity(),"INGRESE CORRELATIVO",Toast.LENGTH_LONG).show();
                        btn_vali.setEnabled(true);
                        return;
                    }
                    else if (etex_seri.getText().toString().length()>0 && etex_corr.getText().toString().length()>0){

                        Toast.makeText(getActivity(),"tipo producto "+lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()),Toast.LENGTH_LONG).show();
                        final RequestQueue queue = Volley.newRequestQueue(getContext());
                        final String CompletaCeroCorr = completarCorrelativo(Integer.valueOf(etex_corr.getText().toString()));
                        final String CorrelativoCompleto = etex_seri.getText().toString()+"-"+CompletaCeroCorr;
                        final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota3/"+CorrelativoCompleto+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Espere");
                        progressDialog.setMessage("Buscando Documento");
                        progressDialog.setCancelable(false);
                        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length()>0)
                                        {
                                            JSONObject json;
                                            try{
                                                json = response.getJSONObject(0);
                                                final String NuDocu = json.getString("NU_DOCU");
                                                final String CoClie = json.getString("CO_CLIE");
                                                final String NoClie = json.getString("NO_CLIE");
                                                final String NuDnis = json.getString("NU_DNIS");
                                                final String NoPasa = json.getString("NO_PASA");
                                                final String CoVehi = json.getString("BUS");
                                                final String CoTipo = json.getString("TIPO");
                                                final String CoEsta = json.getString("ESTADO");
                                                final String FeEmis = json.getString("D_EMISION");
                                                final String HoEmis = json.getString("H_EMISION");
                                                final String DeAgen = json.getString("DE_AGEN");
                                                final String CoOrig = json.getString("CO_DEST_ORIG");
                                                final String DeOrig = json.getString("ORIGEN");
                                                final String CoDest = json.getString("CO_DEST_FINA");
                                                final String DeDest = json.getString("DESTINO");
                                                final String FeViaj = json.getString("FE_VIAJ");
                                                final String HoViaj = json.getString("HO_VIAJ");
                                                final String NuSecu = json.getString("NU_SECU");
                                                final String CoRumb = json.getString("CO_RUMB");
                                                final String ImTota = json.getString("IM_TOTA");
                                                final String NuAsie = json.getString("NU_ASIE");
                                                final String CoTrab = json.getString("VENDEDOR");
                                                final String NuDoCa = json.getString("NU_DOCU_CARG");


                                                guardarDataMemoria("extra_documentoCliente", CoClie);
                                                guardarDataMemoria("extra_empresa", "01");
                                                //CAMBIAR ESTO!!!
                                                guardarDataMemoria("extra_tipoDocumentoBoleto", "BOL");
                                                guardarDataMemoria("extra_numDocumentoBoleto", NuDocu);
                                                guardarDataMemoria("extra_numDocumSerie", NuDocu.substring(0,4));
                                                guardarDataMemoria("extra_numDocumCorre", NuDocu.substring(6,15));
                                                //guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaDocumento"));
                                                guardarDataMemoria("extra_fechaProgramacion", FeViaj);
                                                guardarDataMemoria("extra_RUC",CoClie);
                                                guardarDataMemoria("extra_RazonSocial",NoClie);
                                                guardarDataMemoria("extra_rumbo", CoRumb);
                                                guardarDataMemoria("extra_secuencia", NuSecu);
                                                guardarDataMemoria("extra_asiento", NuAsie);
                                                guardarDataMemoria("extra_servicio", "N");
                                                guardarDataMemoria("extra_secuencia", NuSecu);
                                                guardarDataMemoria("extra_origen", CoOrig);
                                                guardarDataMemoria("extra_destino", CoDest);
                                                guardarDataMemoria("extra_deorigen", DeOrig);
                                                guardarDataMemoria("extra_dedestino", DeDest);
                                                guardarDataMemoria("extra_nombreCliente", NoPasa);
                                                guardarDataMemoria("extra_horaViajeItin", "0000");
                                                guardarDataMemoria("extra_serieDocu", NuDoCa.substring(0,4));
                                                guardarDataMemoria("extra_correDocu", NuDoCa.substring(6,15));
                                                guardarDataMemoria("extra_idProducto", lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                                                guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition()));

                                                etex_seri.setText(NuDocu.substring(0,4));
                                                etex_corr.setText(NuDocu.substring(6,15));
                                                etex_cocli.setText(CoClie);
                                                TXT_CLIE.setText(NoClie);
                                                TXT_DOCUME.setText(NuDoCa);
                                                etex_TARIFA.setText(String.format("%.2f", Float.valueOf("2.00")));



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
                        RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(RequestBuscaBol1);
                    }

                }
                else if (etex_cocli.getText().toString().length()>0){
                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                    final String CodigoCliente = etex_cocli.getText().toString();
                    Toast.makeText(getActivity(),"BUSCANDO CLIENTE "+CodigoCliente, Toast.LENGTH_LONG).show();
                    final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota4/"+CodigoCliente+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Espere");
                    progressDialog.setMessage("Buscando Documento");
                    progressDialog.setCancelable(false);
                    MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.length()>0)
                                    {
                                        JSONObject json;
                                        try{
                                            json = response.getJSONObject(0);
                                            final String NuDocu = json.getString("NU_DOCU");
                                            final String CoClie = json.getString("CO_CLIE");
                                            final String NoClie = json.getString("NO_CLIE");
                                            final String NuDnis = json.getString("NU_DNIS");
                                            final String NoPasa = json.getString("NO_PASA");
                                            final String CoVehi = json.getString("BUS");
                                            final String CoTipo = json.getString("TIPO");
                                            final String CoEsta = json.getString("ESTADO");
                                            final String FeEmis = json.getString("D_EMISION");
                                            final String HoEmis = json.getString("H_EMISION");
                                            final String DeAgen = json.getString("DE_AGEN");
                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                            final String DeOrig = json.getString("ORIGEN");
                                            final String CoDest = json.getString("CO_DEST_FINA");
                                            final String DeDest = json.getString("DESTINO");
                                            final String FeViaj = json.getString("FE_VIAJ");
                                            final String HoViaj = json.getString("HO_VIAJ");
                                            final String NuSecu = json.getString("NU_SECU");
                                            final String CoRumb = json.getString("CO_RUMB");
                                            final String ImTota = json.getString("IM_TOTA");
                                            final String NuAsie = json.getString("NU_ASIE");
                                            final String CoTrab = json.getString("VENDEDOR");
                                            final String NuDoCa = json.getString("NU_DOCU_CARG");


                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                            guardarDataMemoria("extra_empresa", "01");
                                            //CAMBIAR ESTO!!!
                                            guardarDataMemoria("extra_tipoDocumentoBoleto", "BOL");
                                            guardarDataMemoria("extra_numDocumentoBoleto", NuDocu);
                                            guardarDataMemoria("extra_numDocumSerie", NuDocu.substring(0,4));
                                            guardarDataMemoria("extra_numPriSer", NuDocu.substring(0,4));
                                            guardarDataMemoria("extra_numDocumCorre", NuDocu.substring(6,15));
                                            //guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaDocumento"));
                                            guardarDataMemoria("extra_fechaProgramacion", FeViaj);
                                            guardarDataMemoria("extra_RUC",CoClie);
                                            guardarDataMemoria("extra_RazonSocial",NoClie);
                                            guardarDataMemoria("extra_rumbo", CoRumb);
                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                            guardarDataMemoria("extra_asiento", NuAsie);
                                            guardarDataMemoria("extra_servicio", "N");
                                            guardarDataMemoria("extra_secuencia", NuSecu);
                                            guardarDataMemoria("extra_origen", CoOrig);
                                            guardarDataMemoria("extra_destino", CoDest);
                                            guardarDataMemoria("extra_deorigen", DeOrig);
                                            guardarDataMemoria("extra_dedestino", DeDest);
                                            guardarDataMemoria("extra_nombreCliente", NoPasa);
                                            guardarDataMemoria("extra_horaViajeItin", "0000");
                                            guardarDataMemoria("extra_serieDocu", NuDoCa.substring(0,4));
                                            guardarDataMemoria("extra_correDocu", NuDoCa.substring(6,15));
                                            guardarDataMemoria("extra_idProducto", lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                                            guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition()));

                                            etex_seri.setText(NuDocu.substring(0,4));
                                            etex_corr.setText(NuDocu.substring(6,15));
                                            etex_cocli.setText(CoClie);
                                            TXT_CLIE.setText(NoClie);
                                            TXT_DOCUME.setText(NuDoCa);
                                            etex_TARIFA.setText(String.format("%.2f", Float.valueOf("2.00")));

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
                    RequestBuscaBol2.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(RequestBuscaBol2);

                }
            }
        });




        btn_impri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImprimeTarifa(printer);




                if (etex_TARIFA.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), "La tarifa adicional no puede estar vacía.", Toast.LENGTH_SHORT).show();

                }else if (etex_CANTID.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), "El campo cantidad no puede estar vacío", Toast.LENGTH_SHORT).show();

                }  else if (etex_CANTID.getText().toString().split("\\.").length > 1) {

                    Toast.makeText(getActivity(), "La cantidad debe ser un número.", Toast.LENGTH_SHORT).show();

                } else if (Integer.valueOf(etex_CANTID.getText().toString()) == 0) {

                    Toast.makeText(getActivity(), "La cantidad no puede ser menor a 1.", Toast.LENGTH_SHORT).show();

                } else if (Integer.valueOf(etex_CANTID.getText().toString()) > 20) {

                    Toast.makeText(getActivity(), "La cantidad no puede ser mayor a 20.", Toast.LENGTH_SHORT).show();

                } else {
                    guardarDataMemoria("extra_idProducto", lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                    guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition()));
                    guardarDataMemoria("extra_importe", etex_TARIFA.getText().toString());
                    guardarDataMemoria("extra_cantidad", etex_CANTID.getText().toString());


//                    final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
//                    /* ----------------------------------------- */
//
//                    /* Se obtiene la empresa que genera la venta */
//                    String empresaTramaCarga = "dfgdfgdfgdfgdfgdfgdfgdfgdfggdfgdf";
//                    for (int i = 0; i < lista_empresas.size(); i++) {
//
//                        String[] data = lista_empresas.get(i).split("-");
//                        // data[0] = CODIGO_EMPRESA
//                        // data[1] = EMPRESA
//                        // data[2] = DIRECCION
//                        // data[3] = DEPARTAMENTO
//                        // data[4] = PROVINCIA
//                        // data[5] = RUC
//
//                        String codigo_empresa = sharedPreferences.getString("extra_empresa", "NoData");
//
//                        if (codigo_empresa.equals(data[0])) {
//                            empresaTramaCarga = lista_empresas.get(i);
//                            empresaSeleccionada = lista_empresas.get(i);
//                        }
//                    }
//                    float tarifaTotal = Integer.valueOf(etex_TARIFA.getText().toString());
//                    editor.putString("extra_tarifaTotal", Float.toString(tarifaTotal));
//                    editor.commit();
//                    /* ----------------------------------------- */
//
//                    editor.putString("extra_cantidad", etex_CANTID.getText().toString());
//                    editor.commit();

//                    final String trama = generarTramaCarga(sharedPreferences, empresaTramaCarga);
                    //Log.d("trama", trama);
                    String[] dataEncriptada = generarCodigoQR("trama");
                    Boolean[] respuesta = guardarCompraCarga(dataEncriptada[0], dataEncriptada[1]);
                    String TedQR="";
                    TedQR =  dataEncriptada[2]+"|"+sharedPreferences.getString("extra_empresa", "NoData")+"|"
                            +sharedPreferences.getString("extra_rumbo", "NoData")+"|"
                            +sharedPreferences.getString("extra_origen", "NoData")+"|"
                            +sharedPreferences.getString("extra_destino", "NoData")+"|"
                            +sharedPreferences.getString("extra_serieDocu", "NoData") + "-" + sharedPreferences.getString("extra_correDocu", "NoData")+"|"
//                            +sharedPreferences.getString("Senior", "NoData")+"|"
                            +sharedPreferences.getString("extra_secuencia", "NoData")+"|"
                            +sharedPreferences.getString("extra_fechaProgramacion", "NoData")+"|0|"
                            +"CARGA";

                    Imprimecab(printer);
                    Imprimeqr(TedQR);
                    Imprimepie(printer);
                    Imprimecab2(printer);
                    Imprimeqr(TedQR);
                    Imprimepie(printer);
//                    Imprimeqr("HOLA MUNDO");
//                    String tipoProducto = "";
//                    tipoProducto = sharedPreferences.getString("extra_idProducto", "NoData");
//                    imprimir_boletasCarga(sharedPreferences, TedQR, "01", tipoProducto, "Carga");
                }
                /* ----------------------------------------- */
            }
        });














        /* Se agrega la lista de nombres de productos al spinner de tipo de producto */
        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, lista_nombreProductos);
        spinner_tipoProducto.setAdapter(adapter_spinner);

    }


    public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey) {

        String json = sharedPreferences.getString(jsonKey, "NoData");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        ArrayList<String> lista = new ArrayList<>();

        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }

        return lista;
    }



    public String getNumCorrelativo(int  numCorrelativoBLT){


        String sec_zeros = "";
        if (Integer.toString(numCorrelativoBLT).length() < 10) {
            int num_zeros = 10 - Integer.toString(numCorrelativoBLT).length();

            for (int i = 0; i < num_zeros; i++) {
                sec_zeros = sec_zeros + "0";
            }
        }
        String numCorrelativoCompleto = sec_zeros + Integer.toString(numCorrelativoBLT);

        return numCorrelativoCompleto;

    }


    public void guardarDataMemoria(String key, String value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public void Imprimeqr(String ted)
    {
        Boleta boleta = new Boleta("Carga");
        try{
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getContext());
            IPrinter printer = dal.getPrinter();
            printer.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);

            printer.printBitmap(boleta.getQRBitmap(ted));

            int iRetError = printer.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void Imprimecab(IPrinter printerx)
    {
        Boleta boleta = new Boleta("Carga");
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("EMPR. DE TRANSPORTES PERUBUS S.A\n");
            Ticket.append("        RUC  20106076635\n");
            Ticket.append(" AV. MEXICO NRO 333 P. J MATUTE\n");
            Ticket.append("       LA VICTORIA - LIMA\n");
            Ticket.append("________________________________\n");
            Ticket.append("|  BOLETA DE VENTA ELECTRONICA |\n");
            Ticket.append("|            (CARGA)           |\n");
            Ticket.append("|            USUARIO           |\n");
            Ticket.append("________________________________\n");
            Ticket.append("N. DOCUMENTO:    "+sharedPreferences.getString("extra_serieDocu", "NoData")+"-"+sharedPreferences.getString("extra_correDocu", "NoData")+"\n");
            Ticket.append("________________________________\n");
            Ticket.append("________________________________\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("        SERIE: "+sharedPreferences.getString("extra_serieDocu", "NoData")+"\n");
            Ticket.append("  CORRELATIVO: "+sharedPreferences.getString("extra_correDocu", "NoData")+"\n");
            Ticket.append("DOC. ASOCI: "+sharedPreferences.getString("extra_numDocumentoBoleto", "NoData")+"\n");
            Ticket.append("FECHA EMISION: "+FechaImpresion+"\n");
            Ticket.append("HORA VENTA:    \n");
            Ticket.append("SEÑORES: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("DOC.IDENTIDAD: "+sharedPreferences.getString("extra_documentoCliente", "NoData")+"\n");
            Ticket.append("ORIGEN: "+sharedPreferences.getString("extra_origen", "NoData")+"-"+sharedPreferences.getString("extra_deorigen", "NoData")+"\n");
            Ticket.append("DESTINO: "+sharedPreferences.getString("extra_destino", "NoData")+"-"+sharedPreferences.getString("extra_dedestino", "NoData")+"\n");
            Ticket.append("PRODUCTO: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("CANT. DESCRP         IMPORTE S/\n");
            Ticket.append("--------------------------------\n");
            Ticket.append(" "+sharedPreferences.getString("extra_cantidad", "NoData")+"    CARGA         "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("OP.EXONERADAS        "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("OP.INAFECTAS            0.00\n");
            Ticket.append("OP.GRAVADAS             0.00\n");
            Ticket.append("OP.I.G.V.               0.00\n");
            Ticket.append("IMPORTE TOTAL     S/. "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");

//            printer.printBitmap(boleta.getQRBitmap(ted));
//            Ticket.append("--------------------------------\n");
//            Ticket.append("Al recibir el presente documento\n");
//            Ticket.append("acepto todos los terminos y  las\n");
//            Ticket.append("condiciones   del   contrato  de\n");
//            Ticket.append("servicio de transporte descritas\n");
//            Ticket.append("y publicadas en letreros, banner\n");
//            Ticket.append("y   paneles   ubicados   en  los\n");
//            Ticket.append("Terminales    Terrestres     y/o\n");
//            Ticket.append("oficinas de  venta y en  nuestra\n");
//            Ticket.append("pagina  web   WWW.PERUBUS.COM.PE\n");
//            Ticket.append("Autorizado  mediante resolucion:\n");
//            Ticket.append("N° 0180050002160\n");



//            Ticket.append("\n\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }
    public void Imprimecab2(IPrinter printerx)
    {
//        Boleta boleta = new Boleta("Carga");
        int total = Integer.parseInt(sharedPreferences.getString("extra_importe", "NoData"));
        float gravada = (float) (total/1.18);
        float igv = total - gravada;

        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("      E. T. PERUBUS S.A.\n");
            Ticket.append(" AV. MEXICO NRO 333 P. J MATUTE\n");
            Ticket.append("      LA VICTORIA - LIMA\n");
            Ticket.append("        SERIE: "+sharedPreferences.getString("extra_serieDocu", "NoData")+"\n");
            Ticket.append("  CORRELATIVO: "+sharedPreferences.getString("extra_correDocu", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("   BOLETA DE VENTA ELECTRONICA\n");
            Ticket.append("             (CARGA)\n");
            Ticket.append("          TRANSPORTISTA\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("DOC. ASOCI: "+sharedPreferences.getString("extra_numDocumentoBoleto", "NoData")+"\n");
            Ticket.append("FECHA EMISION: "+FechaImpresion+"\n");
            Ticket.append("HORA VENTA:    \n");
            Ticket.append("SEÑORES: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("DOC.IDENTIDAD: "+sharedPreferences.getString("extra_documentoCliente", "NoData")+"\n");
            Ticket.append("ORIGEN: "+sharedPreferences.getString("extra_origen", "NoData")+"-"+sharedPreferences.getString("extra_deorigen", "NoData")+"\n");
            Ticket.append("DESTINO: "+sharedPreferences.getString("extra_destino", "NoData")+"-"+sharedPreferences.getString("extra_dedestino", "NoData")+"\n");
            Ticket.append("PRODUCTO: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("CANT. DESCRP         IMPORTE S/\n");
            Ticket.append("--------------------------------\n");
            Ticket.append(sharedPreferences.getString("extra_cantidad", "NoData")+"     CARGA         "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("OP.EXONERADAS         "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("OP.INAFECTAS          "+total+"\n");
            Ticket.append("OP.GRAVADAS           0.00\n");
            Ticket.append("OP.I.G.V.             0.00\n");
            Ticket.append("IMPORTE TOTAL     S/. "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");

//            printer.printBitmap(boleta.getQRBitmap(ted));
//            Ticket.append("--------------------------------\n");
//            Ticket.append("Al recibir el presente documento\n");
//            Ticket.append("acepto todos los terminos y  las\n");
//            Ticket.append("condiciones   del   contrato  de\n");
//            Ticket.append("servicio de transporte descritas\n");
//            Ticket.append("y publicadas en letreros, banner\n");
//            Ticket.append("y   paneles   ubicados   en  los\n");
//            Ticket.append("Terminales    Terrestres     y/o\n");
//            Ticket.append("oficinas de  venta y en  nuestra\n");
//            Ticket.append("pagina  web   WWW.PERUBUS.COM.PE\n");
//            Ticket.append("Autorizado  mediante resolucion:\n");
//            Ticket.append("N° 0180050002160\n");



//            Ticket.append("\n\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }


    public void Imprimepie(IPrinter printerx)
    {
        Boleta boleta = new Boleta("Carga");
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
//            Ticket.append("      E. T. PERUBUS S.A.\n");
//            Ticket.append(" AV. MEXICO NRO 333 P. J MATUTE\n");
//            Ticket.append("      LA VICTORIA - LIMA\n");
//            Ticket.append("        SERIE: "+sharedPreferences.getString("extra_serieDocu", "NoData")+"\n");
//            Ticket.append("  CORRELATIVO: "+sharedPreferences.getString("extra_correDocu", "NoData")+"\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append("   BOLETA DE VENTA ELECTRONICA\n");
//            Ticket.append("             (CARGA)\n");
//            Ticket.append("             USUARIO\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append("DOC. ASOCI: "+sharedPreferences.getString("extra_numDocumentoBoleto", "NoData")+"\n");
//            Ticket.append("FECHA EMISION: "+FechaImpresion+"\n");
//            Ticket.append("HORA VENTA:    \n");
//            Ticket.append("SEÑORES: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
//            Ticket.append("DOC.IDENTIDAD: "+sharedPreferences.getString("extra_documentoCliente", "NoData")+"\n");
//            Ticket.append("ORIGEN: "+sharedPreferences.getString("extra_origen", "NoData")+"-"+sharedPreferences.getString("extra_deorigen", "NoData")+"\n");
//            Ticket.append("DESTINO: "+sharedPreferences.getString("extra_destino", "NoData")+"-"+sharedPreferences.getString("extra_dedestino", "NoData")+"\n");
//            Ticket.append("PRODUCTO: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append("CANT. DESCRP         IMPORTE S/\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append(sharedPreferences.getString("extra_cantidad", "NoData")+"     CARGA         "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append("OP.EXONERADAS        "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
//            Ticket.append("OP.INAFECTAS          0.00\n");
//            Ticket.append("OP.GRAVADAS           0.00\n");
//            Ticket.append("OP.I.G.V.             0.00\n");
//            Ticket.append("IMPORTE TOTAL     S/. "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
//            Ticket.append("--------------------------------\n");

//            printer.printBitmap(boleta.getQRBitmap(ted));
            Ticket.append("--------------------------------\n");
            Ticket.append("Al recibir el presente documento\n");
            Ticket.append("acepto todos  los terminos y las\n");
            Ticket.append("condiciones   del   contrato  de\n");
            Ticket.append("servicio de transporte descritas\n");
            Ticket.append("y publicadas en letreros, banner\n");
            Ticket.append("y   paneles   ubicados   en  los\n");
            Ticket.append("Terminales     Terrestres    y/o\n");
            Ticket.append("oficinas de  venta y en  nuestra\n");
            Ticket.append("pagina  web   WWW.PERUBUS.COM.PE\n");
            Ticket.append("Autorizado  mediante resolucion:\n");
            Ticket.append("N° 0180050002160\n");



            Ticket.append("\n\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }

    public void ImprimeBol(String ted,IPrinter printerx)
    {
        Boleta boleta = new Boleta("Carga");
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("      E. T. PERUBUS S.A.\n");
            Ticket.append(" AV. MEXICO NRO 333 P. J MATUTE\n");
            Ticket.append("      LA VICTORIA - LIMA\n");
            Ticket.append("        SERIE: "+sharedPreferences.getString("extra_serieDocu", "NoData")+"\n");
            Ticket.append("  CORRELATIVO: "+sharedPreferences.getString("extra_correDocu", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("   BOLETA DE VENTA ELECTRONICA\n");
            Ticket.append("             (CARGA)\n");
            Ticket.append("             USUARIO\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("DOC. ASOCI: "+sharedPreferences.getString("extra_numDocumentoBoleto", "NoData")+"\n");
            Ticket.append("FECHA EMISION: "+FechaImpresion+"\n");
            Ticket.append("HORA VENTA:    \n");
            Ticket.append("SEÑORES: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("DOC.IDENTIDAD: "+sharedPreferences.getString("extra_documentoCliente", "NoData")+"\n");
            Ticket.append("ORIGEN: "+sharedPreferences.getString("extra_origen", "NoData")+"-"+sharedPreferences.getString("extra_deorigen", "NoData")+"\n");
            Ticket.append("DESTINO: "+sharedPreferences.getString("extra_destino", "NoData")+"-"+sharedPreferences.getString("extra_dedestino", "NoData")+"\n");
            Ticket.append("PRODUCTO: "+sharedPreferences.getString("extra_RazonSocial", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("CANT. DESCRP         IMPORTE S/\n");
            Ticket.append("--------------------------------\n");
            Ticket.append(sharedPreferences.getString("extra_cantidad", "NoData")+"     CARGA         "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("OP.EXONERADAS        "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("OP.INAFECTAS          0.00\n");
            Ticket.append("OP.GRAVADAS           0.00\n");
            Ticket.append("OP.I.G.V.             0.00\n");
            Ticket.append("IMPORTE TOTAL     S/. "+sharedPreferences.getString("extra_importe", "NoData")+"\n");
            Ticket.append("--------------------------------\n");

            printer.printBitmap(boleta.getQRBitmap(ted));
            Ticket.append("--------------------------------\n");
            Ticket.append("Al recibir el presente documento\n");
            Ticket.append("acepto todos los terminos y  las\n");
            Ticket.append("condiciones   del   contrato  de\n");
            Ticket.append("servicio de transporte descritas\n");
            Ticket.append("y publicadas en letreros, banner\n");
            Ticket.append("y   paneles   ubicados   en  los\n");
            Ticket.append("Terminales    Terrestres     y/o\n");
            Ticket.append("oficinas de  venta y en  nuestra\n");
            Ticket.append("pagina  web   WWW.PERUBUS.COM.PE\n");
            Ticket.append("Autorizado  mediante resolucion:\n");
            Ticket.append("N° 0180050002160\n");



            Ticket.append("\n\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }

    public void imprimir_boletasCarga(JSONObject jsonObject, SharedPreferences sharedPreferences, String ted, String empresaSeleccionada, String tipoProducto, String tipoBoleta){

        Gson gson = new Gson();

        /* Arreglo */
        // lista_destinos: arreglo que contiene todos los desinos
        final ArrayList<String> lista_destinos = getArray(sharedPreferences, gson,"json_destinos"); //trama: CO_DEST - DE_DEST
        /* ----------------------------------------- */

        String origen = "";
        String destino  = "";

        /* Se pasan todos los valores necesarios para generar la estructura de la boleta que se va a imprimir */
        Boleta boleta = new Boleta(tipoBoleta);
        try {

            for(int i = 0; i < lista_destinos.size(); i++){

                String[] dataDestino = lista_destinos.get(i).split("-");

                if(dataDestino[0].equals(jsonObject.getString("OrigenBoleto"))){
                    origen = dataDestino[0]+"-"+dataDestino[1];
                }
            }

            for(int i = 0; i < lista_destinos.size(); i++){

                String[] dataDestino = lista_destinos.get(i).split("-");

                if(dataDestino[0].equals(jsonObject.getString("DestinoBoleto"))){
                    destino = dataDestino[0]+"-"+dataDestino[1];
                }
            }

            boleta.setOrigen(origen);
            boleta.setDestino(destino);
            boleta.setDNI(jsonObject.getString("CodigoCliente"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boleta.setTarifa(sharedPreferences.getString("extra_tarifaTotal", "NoData"));
        boleta.setSerieCarga(sharedPreferences.getString("extra_serieCarga",  "NoData"));
        boleta.setCorrelativoCarga(sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData"));
        boleta.setEmpresa(empresaSeleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("extra_fechaVentaCarga", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("extra_horaVentaCarga", "NoData"));
        boleta.setTipoProducto(tipoProducto);
        boleta.setCantidad(sharedPreferences.getString("extra_cantidad", "NoData"));
        boleta.setNombreAnfitrion(sharedPreferences.getString("nombreEmpleado", "NoData"));
        boleta.setNumAsiento(sharedPreferences.getString("extra_asiento", "NoData"));
        String [] dataBoletoViaje = sharedPreferences.getString("extra_numDocumentoBoleto", "NoData").split("-");
        boleta.setSeriePasaje(dataBoletoViaje[0]);
        boleta.setCorrelativoPasaje(dataBoletoViaje[1]);
        boleta.setNombreCliente(sharedPreferences.getString("extra_nombreCliente", ""));
        boleta.setEmpesa_imp(sharedPreferences.getString("extra_empresa", "NoData"));
        boleta.SetPrueba(getString(R.string.ws_ticket));
        boleta.SetRUC(sharedPreferences.getString("extra_RUC","NoData"));
        boleta.SetRazonSocial(sharedPreferences.getString("extra_RazonSocial","NoData"));
        boleta.SetDocuElectronico(sharedPreferences.getString("TipoVenta_carga","NoData"));
        /* ----------------------------------------- */

        try {

            /* Se inicializa la impresora del equipo */
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getContext());
            IPrinter printer = dal.getPrinter();
            printer.init();
            /* ----------------------------------------- */

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
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String[] generarCodigoQR(String trama) {

        /* URI del proveedor del servicio */
        Uri CONTENT_URI = Uri.parse("content://org.pe.dgf.provider").buildUpon().appendPath(PATH_XML).build();
        /* ----------------------------------------- */

        /* Se generar el query para obtener la data encriptada */
        Cursor results = getActivity().getContentResolver().query(CONTENT_URI, null, trama, null, SOLICITA_TED);
        /* ----------------------------------------- */

        String xml64 = null;
        String ted = null;
        String ted64 = null;

        /* Se obtiene la data encriptada */
        if (results != null) {
            if (results.moveToNext()) {

                /* Se obtiene el valor de cada columna */
                xml64 = results.getString(results.getColumnIndex(COLUMN_NAME_XML64));
                ted = results.getString(results.getColumnIndex(COLUMN_NAME_TED));
                ted64 = results.getString(results.getColumnIndex(COLUMN_NAME_TED64));
                /* ----------------------------------------- */
            }
        }
        /* ----------------------------------------- */

        String[] valores = {xml64, ted64, ted};

        return valores;
    }


    public Boolean[] guardarCompraCarga(String xml64, String ted64) {

        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final Boolean[] respuesta = new Boolean[1];
        respuesta[0] = true;

        /* Fecha, hora y día de la venta del boleto */
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        editor.putString("extra_fechaVentaCarga", fechaVenta);
        editor.commit();

        final String horaVenta = new SimpleDateFormat("hh:mm").format(date);
        editor.putString("extra_horaVentaCarga", horaVenta);
        editor.commit();

        String strDiaFormat = "dd";
        DateFormat diaFormat = new SimpleDateFormat(strDiaFormat);
        String diaSemana = diaFormat.format(date);
        /* ----------------------------------------- */

        /* Se obtiene el JSON generado */
        final JSONObject jsonObject = generarJSONCarga(sharedPreferences, fechaVenta, horaVenta, diaSemana, xml64, ted64);

        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "carga");
        cv.put("liberado", "No");
        cv.put("nu_docu",sharedPreferences.getString("extra_serieDocu", "NoData") + "-" + sharedPreferences.getString("extra_correDocu", "NoData"));
        cv.put("ti_docu","BLT");
        //cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
            cv.put("puesto", "boletero");
            sharedPreferences.getString("extra_empresa", "NoData");
        }else{
            cv.put("puesto", "anfitrion");
            //cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            sharedPreferences.getString("extra_empresa", "NoData");
        }

        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
        //Log.d("json object", jsonObject.toString());
        /* ----------------------------------------- */

        /* Ruta de la Web service */
        String ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";
        //Log.d("url",ws_postVenta);
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
                                //Log.d("respuesta",info.toString());

                                /* Se obtiene la respuesta del servidor y en caso de ser "guardado" se guarda el boleto en la BD */
                                if (info.getString("Respuesta").equals("GUARDADO")) {

                                  /*  ContentValues cv = new ContentValues();
                                    cv.put("data_boleto", jsonObject.toString());
                                    cv.put("estado", "guardado");
                                    cv.put("tipo", "carga");
                                    cv.put("liberado", "No");
                                    if(sharedPreferences.getString("puestoUsuario", "NoData").equals("BOLETERO")){
                                        cv.put("puesto", "boletero");
                                    }else {
                                        cv.put("puesto", "anfitrion");
                                    }

                                    Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);*/
                                    /* ----------------------------------------- */

                                    respuesta[0] = true;

                                } else {
                                    Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
//                                    button_imprimirBoletoCarga.setEnabled(true);
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

                Toast.makeText(getActivity(), "Error en la ws SetBoletoCarga. No se pudo guardar el boleto de carga en la ws.", Toast.LENGTH_LONG).show();

                Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();

                /* Se guarda el boleto de carga con estado "pendiente" en la BD */
               /* ContentValues cv = new ContentValues();
                cv.put("data_boleto", jsonObject.toString());
                cv.put("estado", "pendiente");
                cv.put("tipo", "carga");
                cv.put("liberado", "No");
                if(sharedPreferences.getString("puestoUsuario", "NoData").equals("BOLETERO")){
                    cv.put("puesto", "boletero");
                }else {
                    cv.put("puesto", "anfitrion");
                }

                Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);*/
                /* ----------------------------------------- */

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

        return respuesta;
    }


    public JSONObject generarJSONCarga(SharedPreferences sharedPreferences, String fechaVenta, String horaVenta, String diaSemana, String xml64, String ted64) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("extra_empresa", "NoData"));
            if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")){
                jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad", "NoData"));
                jsonObject.put("Agencia", sharedPreferences.getString("guardar_agencia", "NoData"));

            }else if (sharedPreferences.getString("Modulo", "NoData").equals("CONDUCTOR ESTANDAR")){
                jsonObject.put("Unidad", "");
                jsonObject.put("Agencia", "");
            }

            jsonObject.put("TipoDocuCarga", sharedPreferences.getString("extra_tipoDocumentoCarga", "NoData"));
            jsonObject.put("SerieCorrelativo", sharedPreferences.getString("extra_serieDocu", "NoData") + "-" + sharedPreferences.getString("extra_correDocu", "NoData"));
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("Rumbo", sharedPreferences.getString("extra_rumbo", "NoData"));
            jsonObject.put("Origen", sharedPreferences.getString("extra_origen", "NoData"));
            jsonObject.put("Destino", sharedPreferences.getString("extra_destino", "NoData"));
            jsonObject.put("NuSecu", sharedPreferences.getString("extra_secuencia", "NoData"));
            jsonObject.put("NumeroDia", diaSemana);
            jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("extra_documentoCliente", "NoData"));
            jsonObject.put("RUC", sharedPreferences.getString("extra_RUC", "NoData"));
            jsonObject.put("NombreCliente", sharedPreferences.getString("extra_nombreCliente", "NoData"));
            jsonObject.put("TipoServicio", sharedPreferences.getString("extra_servicio", "NoData"));
            jsonObject.put("NumeroAsiento", sharedPreferences.getString("extra_asiento", "NoData"));
            jsonObject.put("FechaViajeItinerario", sharedPreferences.getString("extra_fechaProgramacion", "NoData"));
            jsonObject.put("HoraViaje", sharedPreferences.getString("extra_horaViajeItin", "NoData"));
            jsonObject.put("ImporteTotal", sharedPreferences.getString("extra_tarifaTotal", "NoData"));
            jsonObject.put("Observacion", "");
            jsonObject.put("CodigoUsuario", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("NuDocuBoletoViaje", sharedPreferences.getString("extra_numDocumentoBoleto", "NoData"));
            jsonObject.put("TipoDocumentoBoletoViaje", sharedPreferences.getString("extra_tipoDocumentoBoleto", "NoData"));
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
            jsonObject.put("Correlativo", sharedPreferences.getString("extra_correlativoCarga", "NoData"));
            jsonObject.put("Producto", sharedPreferences.getString("extra_idProducto", "NoData"));
            jsonObject.put("Cantidad", sharedPreferences.getString("extra_cantidad", "NoData"));
            if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")){
                jsonObject.put("TipoVenta", "Boletero");
            }else if (sharedPreferences.getString("Modulo", "NoData").equals("CONDUCTOR ESTANDAR")){
                jsonObject.put("TipoVenta", "Anfitrion");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }



    void ImprimeTarifa(IPrinter printerx)
    {
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");
            Ticket.append("--------------------------------\n");

            Ticket.append("\n\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }


    public String generarTramaCarga(SharedPreferences sharedPreferences, String empresaTrama){

        String[] empresaSeleccionada = empresaTrama.split("-");
        // data[0] = CODIGO_EMPRESA
        // data[1] = EMPRESA
        // data[2] = DIRECCION
        // data[3] = DEPARTAMENTO
        // data[4] = PROVINCIA
        // data[5] = RUC
        // data[6] = RAZON_SOCIAL

        /*String tipoDocumento = "";
        if (sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 11) {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }*/
        String tipoDocumento = "";
        if(sharedPreferences.getString("TipoVenta_carga","NoData").equals("FACTURA")) {
            tipoDocumento = "01";
        }else if (sharedPreferences.getString("TipoVenta_carga", "NoData").equals("BOLETA")) {
            tipoDocumento = "03";
        }

        /*String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 11){
            tipoDocumentoCliente = "6";
        }else if(sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 8){
            tipoDocumentoCliente = "1";
        }else{
            tipoDocumentoCliente = "7";
        }*/
        String tipoDocumentoCliente = "";
        String DocuDeclara  = "";
        if(sharedPreferences.getString("TipoVenta_carga","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
            DocuDeclara= sharedPreferences.getString("extra_RUC","NoData");
        } else if (sharedPreferences.getString("TipoVenta_carga", "NoData").equals("BOLETA")) {
            tipoDocumentoCliente = "1";
            DocuDeclara= sharedPreferences.getString("extra_documentoCliente","NoData");
        } else {
            tipoDocumentoCliente = "7";
            DocuDeclara= sharedPreferences.getString("extra_documentoCliente","NoData");
        }



        String documentoCliente = "";
        if(sharedPreferences.getString("extra_documentoCliente", "NoData").equals("")){
            documentoCliente = "-";
        }else{
            documentoCliente = sharedPreferences.getString("extra_documentoCliente", "NoData");
        }

        String nombreCliente = "";
        if(sharedPreferences.getString("extra_nombreCliente", "NoData").equals("")){
            nombreCliente = "-";
        }else{
            nombreCliente = sharedPreferences.getString("extra_nombreCliente", "NoData");
        }

        String direccionCliente = "";
        if(sharedPreferences.getString("extra_direccionCliente", "NoData").equals("NoData")){
            direccionCliente = "-";
        }else{
            direccionCliente = sharedPreferences.getString("extra_direccionCliente", "NoData");
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
        double montoTotal = Float.valueOf(sharedPreferences.getString("extra_tarifaTotal", "NoData"));
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
        String tramaCarga ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + sharedPreferences.getString("extra_serieDocu", "NoData") + "\n" +
                "A;Correlativo;;" + sharedPreferences.getString("extra_correDocu", "NoData").substring(2) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;TipoRucEmis;;6\n"+
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
                "A;DirRecep;;" + direccionCliente + "\n" +
                "A;DirRecepUrbaniza;;NoData"+"\n"+
                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                "A;TipoOperacion;;0101\n" +
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
                "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("extra_origen","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("extra_destino","NoData").toString().trim() +"\n"+
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
                "E;DescripcionAdicSunat;18;"+sharedPreferences.getString("extra_serieDocu", "NoData")+"/F-VTS-42\n"+
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
                "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("extra_origen","NoData").toString().trim()+"  -  "+ sharedPreferences.getString("extra_destino","NoData").toString().trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";
       /* String tramaCarga ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + sharedPreferences.getString("extra_serieCarga", "NoData") + "\n" +
                "A;Correlativo;;" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData").substring(2) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;TipoRucEmis;;6\n"+
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;TipoRutReceptor;;"+tipoDocumento+"\n" +
                "A;RUTRecep;;" + documentoCliente + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;" + direccionCliente + "\n" +
                "A;DirRecepUrbaniza;;NoData"+"\n"+
                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                "A;TipoOperacion;;0101\n" +
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
                "B;IndExe;1;20\n" +
                //"B;CodigoTipoIgv;1;9997\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;" + String.format("%.2f", montoIGV) + "\n" +
                "E;TipoAdicSunat;1;01\n" +
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
                "E;DescripcionAdicSunat;5;0180050002160\n";
       /* String tramaCarga = "A;Serie;;"+sharedPreferences.getString("extra_serieCarga", "NoData")+"\n" +
                "A;Correlativo;;"+sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData").substring(2)+"\n" +
                "A;RznSocEmis;;"+empresaSeleccionada[8]+"\n" +
                "A;CODI_EMPR;;"+empresaSeleccionada[0].substring(1)+"\n" +
                "A;RUTEmis;;"+empresaSeleccionada[7]+"\n" +
                "A;DirEmis;;"+empresaSeleccionada[2] + " - " + empresaSeleccionada[3] + " - " + empresaSeleccionada[4]+"\n" +
                "A;ComuEmis;;150115\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;NomComer;;"+empresaSeleccionada[1]+"\n" +
                "A;TipoDTE;;"+tipoDocumento+"\n" +
                "A;TipoOperacion;;0101\n" +
                "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                "A;RUTRecep;;"+documentoCliente+"\n" +
                "A;RznSocRecep;;"+nombreCliente+"\n" +
                "A;DirRecep;;"+direccionCliente+"\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;"+String.format("%.2f", montoIGV)+"\n" +
                "A;MntTotal;;"+String.format("%.2f", montoTotal)+"\n" +
                "A;FchEmis;;"+fechaVenta+"\n" +
                "A;HoraEmision;;"+horaVenta+"\n" +
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;"+ String.format("%.2f", montoIGV)+"\n" +
                "A2;TasaImpuesto;1;18\n" +
                "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;"+sharedPreferences.getString("extra_cantidad", "NoData")+"\n" +
                "B;UnmdItem;1;MTQ\n" +
                "B;VlrCodigo;1;"+sharedPreferences.getString("extra_idProducto", "NoData")+"\n" +
                "B;NmbItem;1;"+sharedPreferences.getString("guardar_nombreProducto", "NoData")+"\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;"+montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;IndExe;1;1010\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;"+String.format("%.2f", montoIGV)+"\n"+
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
                "E;DescripcionAdicSunat;18;"+sharedPreferences.getString("extra_serieCarga", "NoData")+"/F-VTS-43\n" +
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

    public Bitmap getQRBitmap(String QRtext) {

        /* FUENTE:
         / https://stackoverflow.com/questions/28232116/android-using-zxing-generate-qr-code
         */

        BitMatrix result;
        //int WIDTH=500;
        int HEIGHT=700;
        int WIDTH=700;
        try {
            result = new MultiFormatWriter().encode(QRtext,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        } catch (WriterException we) {
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);

//        bitmap.setShadowLayer( 4.0f, 0.0f, 2.0f, Color.BLACK);


        return bitmap;
    }

}