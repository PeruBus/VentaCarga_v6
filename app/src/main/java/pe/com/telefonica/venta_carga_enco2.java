package pe.com.telefonica.soyuz;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Printer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.*;
import com.zcs.sdk.print.*;

public class venta_carga_enco2 extends Fragment {
    private IDAL dal;
    private IPrinter printer;
    String empresaSeleccionada = "";
    String empresaTramaCarga = "";

    private DriverManager mDriverManager;
    private Printer mPrinter;

    int FlagValidaButton=0;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    public static final String SOLICITA_TED = "ted";
    public static final String PATH_XML = "xml";
    private SQLiteDatabase sqLiteDatabase;
    public static final String COLUMN_NAME_XML64 = "xml64";
    public static final String COLUMN_NAME_TED = "ted";
    public static final String COLUMN_NAME_TED64 = "ted64";
    EditText RA_DE_SERI;
    LinearLayout RA_CA_SERI;
    TextView RA_CA_CORR;
    EditText RA_DE_CORR;
    TextView RA_CA_RUC;
    TextView RA_DE_RUC;
    TextView RA_CA_RZSC;
    TextView RA_DE_RZSC;
    TextView RA_CA_DOCU;
    EditText RA_DE_DOCU;
    TextView RA_CA_CLNT;
    TextView RA_DE_CLNT;
    TextView RD_CA_TPDC;
    TextView RD_CO_EMPR;
    Spinner  RD_DE_TPDC;
    Spinner  RD_DE_EMPR;
    LinearLayout RD_CA_DOCU;
    EditText RD_DE_DOCU;
    TextView RD_CA_CLNT;
    EditText RD_DE_CLNT;
    TextView RD_CA_RUC;
    EditText RD_DE_RUC;
    TextView RD_CA_RZSC;
    EditText RD_DE_RZSC;
    TextView RA_CA_TPPR;
    Spinner  RA_DE_TPPR;
    TextView RA_CA_TPPR2;
    EditText RA_DE_TPPR2;
    TextView RA_CA_TARI;
    EditText RA_DE_TARI;
    TextView RA_CA_CANT;
    EditText RA_DE_CANT;
    TextView RA_CA_BIMP;
    Button   RA_DE_BIMP;
    TextView RA_CA_BIMP2;
    Button   RA_DE_BIMP2;
    Button   RA_BTN_BUSC;
    Button   RA_BTN_DOCU;
    TextView TT_NU_DOCU;
    TextView RA_DETA_DOCU;
    TextView RD_CA_CEPS;
    EditText RD_DE_CEPS;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        gson = new Gson();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.venta_carga_enco2,parent,false );
    }

    @Override
    public void onViewCreated(View view ,Bundle  savedInstanceState)
    {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Button btn_dr = view.findViewById(R.id.BTN_DR);
        final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RA_CA_SERI = view.findViewById(R.id.RA_CA_SERI);
        RA_DE_SERI = view.findViewById(R.id.RA_DE_SERI);
        RA_CA_CORR = view.findViewById(R.id.RA_CA_CORR);
        RA_DE_CORR = view.findViewById(R.id.RA_DE_CORR);
        RA_CA_RUC = view.findViewById(R.id.RA_CA_RUC);
        RA_DE_RUC = view.findViewById(R.id.RA_DE_RUC);
        RA_CA_RZSC = view.findViewById(R.id.RA_CA_RZSC);
        RA_DE_RZSC = view.findViewById(R.id.RA_DE_RZSC);
        RA_CA_DOCU = view.findViewById(R.id.RA_CA_DOCU);
        RA_DE_DOCU = view.findViewById(R.id.RA_DE_DOCU);
        RA_CA_CLNT = view.findViewById(R.id.RA_CA_CLNT);
        RA_DE_CLNT = view.findViewById(R.id.RA_DE_CLNT);
        RD_CA_TPDC = view.findViewById(R.id.RD_CA_TPDC);
        RD_CO_EMPR = view.findViewById(R.id.RD_CO_EMPR);
        RD_DE_TPDC = view.findViewById(R.id.RD_DE_TPDC);
        RD_DE_EMPR = view.findViewById(R.id.RD_DE_EMPR);
        RD_CA_DOCU = view.findViewById(R.id.RD_CA_DOCU);
        RD_DE_DOCU = view.findViewById(R.id.RD_DE_DOCU);
        RD_CA_CLNT = view.findViewById(R.id.RD_CA_CLNT);
        RD_DE_CLNT = view.findViewById(R.id.RD_DE_CLNT);
        RD_CA_RUC = view.findViewById(R.id.RD_CA_RUC);
        RD_DE_RUC = view.findViewById(R.id.RD_DE_RUC);
        RD_CA_RZSC = view.findViewById(R.id.RD_CA_RZSC);
        RD_DE_RZSC = view.findViewById(R.id.RD_DE_RZSC);
        RA_CA_TPPR = view.findViewById(R.id.RA_CA_TPPR);
        RA_DE_TPPR = view.findViewById(R.id.RA_DE_TPPR);
        RA_CA_TPPR2 = view.findViewById(R.id.RA_CA_TPPR2);
        RA_DE_TPPR2 = view.findViewById(R.id.RA_DE_TPPR2);
        RA_CA_TARI = view.findViewById(R.id.RA_CA_TARI);
        RA_DE_TARI = view.findViewById(R.id.RA_DE_TARI);
        RA_CA_CANT = view.findViewById(R.id.RA_CA_CANT);
        RA_DE_CANT = view.findViewById(R.id.RA_DE_CANT);
        RA_CA_BIMP = view.findViewById(R.id.RA_CA_BIMP);
        RA_DE_BIMP = view.findViewById(R.id.RA_DE_BIMP);
        RA_CA_BIMP2 = view.findViewById(R.id.RA_CA_BIMP2);
        RA_DE_BIMP2 = view.findViewById(R.id.RA_DE_BIMP2);
        RA_BTN_BUSC = view.findViewById(R.id.RA_BTN_BUSC);
        RA_BTN_DOCU = view.findViewById(R.id.RA_BTN_DOCU);
        TT_NU_DOCU = view.findViewById(R.id.TT_NU_DOCU);
        RA_DETA_DOCU = view.findViewById(R.id.RA_DETA_DOCU);
        RD_CA_CEPS = view.findViewById(R.id.RD_CA_CEPS);
        RD_DE_CEPS = view.findViewById(R.id.RD_DE_CEPS);

        try {
            dal = NeptuneLiteUser.getInstance().getDal(getContext());
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
//            mDriverManager= DriverManager.getInstance();
//            mPrinter = mDriverManager.getPrinter();
        } catch (Exception e) {}

        gson = new Gson();
        final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
        final ArrayList<String> lista_idProductos = new ArrayList<>();
        final ArrayList<String> lista_nombreProductos = new ArrayList<>();

        lista_idProductos.add("998");
        lista_nombreProductos.add("SELECCIONAR");
        /* ----------------------------------------- */

        /* Se itera en función a la lista de productos y se agrega data a los arreglos de IDs y nombre */
        for (int i = 0; i < lista_productos.size(); i++) {
            String[] dataProductos = lista_productos.get(i).split("-");
            // dataProductos[0] = TI_PROD
            // dataProductos[1]= DE_TIPO_PROD

            lista_idProductos.add(dataProductos[0]);
            lista_nombreProductos.add(dataProductos[1]);

        }

        lista_idProductos.add("999");
        lista_nombreProductos.add("OTRO");

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, lista_nombreProductos);
        RA_DE_TPPR.setAdapter(adapter_spinner);

        CL_DR_AT();

        btn_dr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_dr.getText().toString().equals("APAGADO")) {
                    CargaTipoDocumento();
                    CargaEmpresa();
                    CL_DR_DS();
                } else {
                    CL_DR_AT();
                }
            }
        });

        RA_DE_CORR.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean procesado = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    GET_MANU_NUDO();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    procesado = true;
                }
                return procesado;
            }
        });

        RA_DE_DOCU.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean procesado = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    GET_MANU_DOCU();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    procesado = true;
                }
                return procesado;
            }
        });

        RA_BTN_BUSC.setOnClickListener(new View.OnClickListener() {
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
                                    Toast.makeText(getActivity(),"BUSCANDO DNI "+dataCodigoQR[0], Toast.LENGTH_SHORT).show();
                                    final String CodigoCliente = dataCodigoQR[0];
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota4/"+CodigoCliente+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                                    Toast.makeText(getActivity(),""+ws_buscarTCDOCU_TOTA1, Toast.LENGTH_SHORT).show();
                                    MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);
                                                            final String CoEmpr = json.getString("CO_EMPR");
                                                            final String NuDocu = json.getString("NU_DOCU");
                                                            String PrSeri = NuDocu.substring(0,1);
                                                            String NuSeri = NuDocu.substring(0,4);
                                                            int NuCorr = Integer.parseInt(NuDocu.substring(6,15));
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String TIPDOC = json.getString("TIPO_DOCU");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String NuDoCa = json.getString("NU_DOCU_CARG");
                                                            Toast.makeText(getActivity(),"BOLETA "+NuDocu, Toast.LENGTH_SHORT).show();

                                                            if(CoEmpr.equals("00")){
                                                                Toast.makeText(getActivity(),"NO SE ENCONTRO EL DOCUMENTO, VERFICAR LOS DATOS ", Toast.LENGTH_SHORT).show();
                                                                CL_DR_AT();
                                                            }
                                                            else if(!CoEmpr.equals("00") && PrSeri.equals("B")){
                                                                Toast.makeText(getActivity(),"BOLETA ", Toast.LENGTH_SHORT).show();
                                                                RA_CA_CLNT.setVisibility(View.VISIBLE);
                                                                RA_CA_CLNT.setLayoutParams(activo);
                                                                RA_DE_CLNT.setVisibility(View.VISIBLE);
                                                                RA_DE_CLNT.setLayoutParams(activo);
                                                                RA_CA_TPPR.setVisibility(View.VISIBLE);
                                                                RA_CA_TPPR.setLayoutParams(activo);
                                                                RA_DE_TPPR.setVisibility(View.VISIBLE);
                                                                RA_DE_TPPR.setLayoutParams(activo);
                                                                RA_CA_RUC.setVisibility(View.INVISIBLE);
                                                                RA_CA_RUC.setLayoutParams(desact);
                                                                RA_DE_RUC.setVisibility(View.INVISIBLE);
                                                                RA_DE_RUC.setLayoutParams(desact);
                                                                RA_CA_RZSC.setVisibility(View.INVISIBLE);
                                                                RA_CA_RZSC.setLayoutParams(desact);
                                                                RA_DE_RZSC.setVisibility(View.INVISIBLE);
                                                                RA_DE_RZSC.setLayoutParams(desact);
                                                                RA_DE_SERI.setEnabled(false);
                                                                RA_DE_CORR.setEnabled(false);
                                                                RA_DE_DOCU.setEnabled(false);
                                                                RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                                                RA_BTN_BUSC.setLayoutParams(desact);
                                                                RA_DE_DOCU.setText(NuDnis);
                                                                RA_DE_CLNT.setText(NoPasa);
                                                                TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                                                RA_DE_SERI.setText(NuSeri);
                                                                RA_DE_CORR.setText(""+NuCorr);
                                                                guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                                                guardarDataMemoria("GD_PRISERI", PrSeri);
                                                                guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                                                guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                                                guardarDataMemoria("GD_NuDocu", NuDocu);
                                                                guardarDataMemoria("GD_TPDOC", TIPDOC);
                                                                guardarDataMemoria("GD_CoClie", CoClie);
                                                                guardarDataMemoria("GD_NoClie", NoClie);
                                                                guardarDataMemoria("GD_NuDnis", NuDnis);
                                                                guardarDataMemoria("GD_NoPasa", NoPasa);
                                                                guardarDataMemoria("GD_CoVehi", CoVehi);
                                                                guardarDataMemoria("GD_CoOrig", CoOrig);
                                                                guardarDataMemoria("GD_DeOrig", DeOrig);
                                                                guardarDataMemoria("GD_CoDest", CoDest);
                                                                guardarDataMemoria("GD_DeDest", DeDest);
                                                                guardarDataMemoria("GD_FeViaj", FeViaj);
                                                                guardarDataMemoria("GD_HoViaj", HoViaj);
                                                                guardarDataMemoria("GD_CoRumb", CoRumb);
                                                                guardarDataMemoria("GD_NuAsie", NuAsie);
                                                                guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                                                guardarDataMemoria("GD_TipDTE", "03");
                                                                guardarDataMemoria("extra_RazonSocial", NoClie);
                                                                guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            }
                                                            else if(!CoEmpr.equals("00") && PrSeri.equals("F")){
                                                                RA_CA_CLNT.setVisibility(View.VISIBLE);
                                                                RA_CA_CLNT.setLayoutParams(activo);
                                                                RA_DE_CLNT.setVisibility(View.VISIBLE);
                                                                RA_DE_CLNT.setLayoutParams(activo);
                                                                RA_CA_TPPR.setVisibility(View.VISIBLE);
                                                                RA_CA_TPPR.setLayoutParams(activo);
                                                                RA_DE_TPPR.setVisibility(View.VISIBLE);
                                                                RA_DE_TPPR.setLayoutParams(activo);
                                                                RA_CA_RUC.setVisibility(View.VISIBLE);
                                                                RA_CA_RUC.setLayoutParams(activo);
                                                                RA_DE_RUC.setVisibility(View.VISIBLE);
                                                                RA_DE_RUC.setLayoutParams(activo);
                                                                RA_CA_RZSC.setVisibility(View.VISIBLE);
                                                                RA_CA_RZSC.setLayoutParams(activo);
                                                                RA_DE_RZSC.setVisibility(View.VISIBLE);
                                                                RA_DE_RZSC.setLayoutParams(activo);
                                                                RA_DE_SERI.setEnabled(false);
                                                                RA_DE_CORR.setEnabled(false);
                                                                RA_DE_DOCU.setEnabled(false);
                                                                RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                                                RA_BTN_BUSC.setLayoutParams(desact);
                                                                RA_DE_RUC.setText(CoClie);
                                                                RA_DE_RZSC.setText(NoClie);
                                                                RA_DE_DOCU.setText(NuDnis);
                                                                RA_DE_CLNT.setText(NoPasa);
                                                                TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                                                RA_DE_SERI.setText(NuSeri);
                                                                RA_DE_CORR.setText(""+NuCorr);
                                                                guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                                                guardarDataMemoria("GD_PRISERI", PrSeri);
                                                                guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                                                guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                                                guardarDataMemoria("GD_NuDocu", NuDocu);
                                                                guardarDataMemoria("GD_TPDOC", TIPDOC);
                                                                guardarDataMemoria("GD_CoClie", CoClie);
                                                                guardarDataMemoria("GD_NoClie", NoClie);
                                                                guardarDataMemoria("GD_NuDnis", NuDnis);
                                                                guardarDataMemoria("GD_NoPasa", NoPasa);
                                                                guardarDataMemoria("GD_CoVehi", CoVehi);
                                                                guardarDataMemoria("GD_CoOrig", CoOrig);
                                                                guardarDataMemoria("GD_DeOrig", DeOrig);
                                                                guardarDataMemoria("GD_CoDest", CoDest);
                                                                guardarDataMemoria("GD_DeDest", DeDest);
                                                                guardarDataMemoria("GD_FeViaj", FeViaj);
                                                                guardarDataMemoria("GD_HoViaj", HoViaj);
                                                                guardarDataMemoria("GD_CoRumb", CoRumb);
                                                                guardarDataMemoria("GD_NuAsie", NuAsie);
                                                                guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                                                guardarDataMemoria("GD_TipDTE", "01");
                                                                guardarDataMemoria("extra_RazonSocial", NoClie);
                                                                guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            }
                                                        }catch (Exception e)
                                                        {
                                                            Log.d("error",e.getMessage());
                                                        }
                                                    }
                                                    Log.d("data",response.toString());
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getActivity(),"VERIFICA TU CONEXION A INTERNET Y VUELVE A INTENTARLO", Toast.LENGTH_SHORT).show();
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
                                else if(dataCodigoQR[2].length()==4 && dataCodigoQR[3].length()==8){
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String CorrelativoCompleto = dataCodigoQR[2]+"-00"+dataCodigoQR[3];
                                    Toast.makeText(getActivity(),"BUSCANDO BOLETO "+CorrelativoCompleto, Toast.LENGTH_LONG).show();
                                    final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota3/"+CorrelativoCompleto+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                                    MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);

                                                            final String CoEmpr = json.getString("CO_EMPR");
                                                            final String TIPDOC = json.getString("TIPO_DOCU");
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String NuDoCa = json.getString("NU_DOCU_CARG");

                                                            if(CoEmpr.equals("00")){
                                                                Toast.makeText(getActivity(),"NO SE ENCONTRO EL DOCUMENTO, VERFICAR LOS DATOS", Toast.LENGTH_SHORT).show();
                                                                CL_DR_AT();
                                                            }
                                                            else if(!CoEmpr.equals("00") && CorrelativoCompleto.substring(0,1).equals("B")){
//                                            Toast.makeText(getActivity(),"PRIMERA LETRA "+CorrelativoCompleto.substring(0,1), Toast.LENGTH_SHORT).show();
                                                                RA_CA_CLNT.setVisibility(View.VISIBLE);
                                                                RA_CA_CLNT.setLayoutParams(activo);
                                                                RA_DE_CLNT.setVisibility(View.VISIBLE);
                                                                RA_DE_CLNT.setLayoutParams(activo);
                                                                RA_CA_TPPR.setVisibility(View.VISIBLE);
                                                                RA_CA_TPPR.setLayoutParams(activo);
                                                                RA_DE_TPPR.setVisibility(View.VISIBLE);
                                                                RA_DE_TPPR.setLayoutParams(activo);
                                                                RA_CA_RUC.setVisibility(View.INVISIBLE);
                                                                RA_CA_RUC.setLayoutParams(desact);
                                                                RA_DE_RUC.setVisibility(View.INVISIBLE);
                                                                RA_DE_RUC.setLayoutParams(desact);
                                                                RA_CA_RZSC.setVisibility(View.INVISIBLE);
                                                                RA_CA_RZSC.setLayoutParams(desact);
                                                                RA_DE_RZSC.setVisibility(View.INVISIBLE);
                                                                RA_DE_RZSC.setLayoutParams(desact);
                                                                RA_DE_DOCU.setEnabled(false);
                                                                RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                                                RA_BTN_BUSC.setLayoutParams(desact);
                                                                RA_DE_DOCU.setText(NuDnis);
                                                                RA_DE_CLNT.setText(NoPasa);
                                                                TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                                                RA_DE_SERI.setText(dataCodigoQR[2]);
                                                                RA_DE_CORR.setText(dataCodigoQR[3]);
                                                                RA_DE_SERI.setEnabled(false);
                                                                RA_DE_CORR.setEnabled(false);
                                                                guardarDataMemoria("GD_PRISERI", CorrelativoCompleto.substring(0,1));
                                                                guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                                                guardarDataMemoria("GD_CoClie", CoClie);
                                                                guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                                                guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                                                guardarDataMemoria("GD_TPDOC", TIPDOC);
                                                                guardarDataMemoria("GD_NoClie", NoClie);
                                                                guardarDataMemoria("GD_NuDnis", NuDnis);
                                                                guardarDataMemoria("GD_NoPasa", NoPasa);
                                                                guardarDataMemoria("GD_CoVehi", CoVehi);
                                                                guardarDataMemoria("GD_CoOrig", CoOrig);
                                                                guardarDataMemoria("GD_DeOrig", DeOrig);
                                                                guardarDataMemoria("GD_CoDest", CoDest);
                                                                guardarDataMemoria("GD_DeDest", DeDest);
                                                                guardarDataMemoria("GD_FeViaj", FeViaj);
                                                                guardarDataMemoria("GD_HoViaj", HoViaj);
                                                                guardarDataMemoria("GD_CoRumb", CoRumb);
                                                                guardarDataMemoria("GD_NuAsie", NuAsie);
                                                                guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                                                guardarDataMemoria("GD_TipDTE", "03");
                                                                guardarDataMemoria("extra_RazonSocial", NoClie);
                                                                guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            }
                                                            else if(!CoEmpr.equals("00") && CorrelativoCompleto.substring(0,1).equals("F")){
//                                            Toast.makeText(getActivity(),"PRIMERA LETRA "+CorrelativoCompleto.substring(0,1), Toast.LENGTH_SHORT).show();
                                                                RA_CA_CLNT.setVisibility(View.VISIBLE);
                                                                RA_CA_CLNT.setLayoutParams(activo);
                                                                RA_DE_CLNT.setVisibility(View.VISIBLE);
                                                                RA_DE_CLNT.setLayoutParams(activo);
                                                                RA_CA_TPPR.setVisibility(View.VISIBLE);
                                                                RA_CA_TPPR.setLayoutParams(activo);
                                                                RA_DE_TPPR.setVisibility(View.VISIBLE);
                                                                RA_DE_TPPR.setLayoutParams(activo);
                                                                RA_CA_RUC.setVisibility(View.VISIBLE);
                                                                RA_CA_RUC.setLayoutParams(activo);
                                                                RA_DE_RUC.setVisibility(View.VISIBLE);
                                                                RA_DE_RUC.setLayoutParams(activo);
                                                                RA_CA_RZSC.setVisibility(View.VISIBLE);
                                                                RA_CA_RZSC.setLayoutParams(activo);
                                                                RA_DE_RZSC.setVisibility(View.VISIBLE);
                                                                RA_DE_RZSC.setLayoutParams(activo);
                                                                RA_DE_DOCU.setEnabled(false);
                                                                RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                                                RA_BTN_BUSC.setLayoutParams(desact);
                                                                RA_DE_RUC.setText(CoClie);
                                                                RA_DE_RZSC.setText(NoClie);
                                                                RA_DE_DOCU.setText(NuDnis);
                                                                RA_DE_CLNT.setText(NoPasa);
                                                                TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                                                RA_DE_SERI.setText(dataCodigoQR[2]);
                                                                RA_DE_CORR.setText(dataCodigoQR[3]);
                                                                RA_DE_SERI.setEnabled(false);
                                                                RA_DE_CORR.setEnabled(false);
                                                                guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                                                guardarDataMemoria("GD_PRISERI", CorrelativoCompleto.substring(0,1));
                                                                guardarDataMemoria("GD_CoClie", CoClie);
                                                                guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                                                guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                                                guardarDataMemoria("GD_TPDOC", TIPDOC);
                                                                guardarDataMemoria("GD_NoClie", NoClie);
                                                                guardarDataMemoria("GD_NuDnis", NuDnis);
                                                                guardarDataMemoria("GD_NoPasa", NoPasa);
                                                                guardarDataMemoria("GD_CoVehi", CoVehi);
                                                                guardarDataMemoria("GD_CoOrig", CoOrig);
                                                                guardarDataMemoria("GD_DeOrig", DeOrig);
                                                                guardarDataMemoria("GD_CoDest", CoDest);
                                                                guardarDataMemoria("GD_DeDest", DeDest);
                                                                guardarDataMemoria("GD_FeViaj", FeViaj);
                                                                guardarDataMemoria("GD_HoViaj", HoViaj);
                                                                guardarDataMemoria("GD_CoRumb", CoRumb);
                                                                guardarDataMemoria("GD_NuAsie", NuAsie);
                                                                guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                                                guardarDataMemoria("GD_TipDTE", "03");
                                                                guardarDataMemoria("extra_RazonSocial", NoClie);
                                                                guardarDataMemoria("extra_documentoCliente", CoClie);
                                                            }
                                                        }catch (Exception e)
                                                        {
                                                            Log.d("error",e.getMessage());
                                                        }
                                                    }
                                                    Log.d("data",response.toString());
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getActivity(),"VERIFICA TU CONEXION A INTERNET Y VUELVE A INTENTARLO", Toast.LENGTH_SHORT).show();
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

        RA_BTN_DOCU.setOnClickListener(new View.OnClickListener() {
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
                                final String CodigoCliente = dataCodigoQR[0];
                                RD_DE_DOCU.setText(CodigoCliente);
                                GET_CLIE_DNI(CodigoCliente);
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

        RD_DE_TPDC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner_model st = (Spinner_model)RD_DE_TPDC.getSelectedItem();
                Spinner_model2 st2 = (Spinner_model2)RD_DE_EMPR.getSelectedItem();
                if (st2.id=="1"){
                }else if (st.id=="2"){
                    CL_DR_DS();
                    docu_ndni();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "1");
                    guardarDataMemoria("GD_TipDTE", "03");
                }else if (st.id=="3"){
                    CL_DR_DS();
                    docu_nruc();
                    GET_SERI_CARG("F",st2.id);
                    guardarDataMemoria("GD_TPDOC", "6");
                    guardarDataMemoria("GD_TipDTE", "01");
                }else if (st.id=="1"){
                    CL_DR_DS();
                }else if (st.id=="4"){
                    CL_DR_DS();
                    docu_otro();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "4");
                    guardarDataMemoria("GD_TipDTE", "03");
                }else {
                    CL_DR_DS();
                    docu_otro();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "7");
                    guardarDataMemoria("GD_TipDTE", "03");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RD_DE_EMPR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner_model st = (Spinner_model)RD_DE_TPDC.getSelectedItem();
                Spinner_model2 st2 = (Spinner_model2)RD_DE_EMPR.getSelectedItem();
                if (st2.id=="1"){
                }else if (st.id=="2"){
                    CL_DR_DS();
                    docu_ndni();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "1");
                    guardarDataMemoria("GD_TipDTE", "03");
                }else if (st.id=="3"){
                    CL_DR_DS();
                    docu_nruc();
                    GET_SERI_CARG("F",st2.id);
                    guardarDataMemoria("GD_TPDOC", "6");
                    guardarDataMemoria("GD_TipDTE", "01");
                }else if (st.id=="1"){
                    CL_DR_DS();
                }else if (st.id=="4"){
                    CL_DR_DS();
                    docu_otro();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "4");
                    guardarDataMemoria("GD_TipDTE", "03");
                }else {
                    CL_DR_DS();
                    docu_otro();
                    GET_SERI_CARG("B",st2.id);
                    guardarDataMemoria("GD_TPDOC", "7");
                    guardarDataMemoria("GD_TipDTE", "03");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RD_DE_DOCU.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean procesado = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String CodigoCliente = RD_DE_DOCU.getText().toString();
                    GET_CLIE_DNI(CodigoCliente);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    procesado = true;
                }
                return procesado;
            }
        });

        RD_DE_CEPS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean procesado = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String CodigoCliente = RD_DE_CEPS.getText().toString().replace(" ","").replace("'","");
                    RD_DE_CEPS.setText(RD_DE_CEPS.getText().toString().replace(" ","").replace("'",""));
                    GET_CLIE_DNI(CodigoCliente);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    procesado = true;
                }
                return procesado;
            }
        });

        RD_DE_RUC.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean procesado = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String CodigoCliente = RD_DE_RUC.getText().toString();
                    GET_CLIE_RUC(CodigoCliente);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    procesado = true;
                }
                return procesado;
            }
        });

        RA_DE_TPPR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Spinner_model st = (Spinner_model)RA_DE_TPPR.getSelectedItem();
                if (lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999"){
                    RA_CA_TPPR2.setVisibility(View.VISIBLE);
                    RA_CA_TPPR2.setLayoutParams(activo);
                    RA_DE_TPPR2.setVisibility(View.VISIBLE);
                    RA_DE_TPPR2.setLayoutParams(activo);
                    GET_TARI_CANT();
                }
                else{
                    RA_CA_TPPR2.setVisibility(View.INVISIBLE);
                    RA_CA_TPPR2.setLayoutParams(desact);
                    RA_DE_TPPR2.setVisibility(View.INVISIBLE);
                    RA_DE_TPPR2.setLayoutParams(desact);
                    GET_TARI_CANT();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RA_DE_BIMP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resp;
//                ProgressDialog progressDialog;
//                progressDialog = ProgressDialog.show(getActivity(),
//                        "Imprimiendo Boleto",
//                        "Espere...");
                String CABECE = TT_NU_DOCU.getText().toString().substring(0,3);
                String CO_CAJA = sharedPreferences.getString("CodCaja","NoData");
                String PRISERI = sharedPreferences.getString("GD_PRISERI","NoData");
                if(CO_CAJA.equals("NoData")){
                    Toast.makeText(getActivity(),"ASIGNA UNA CAJA", Toast.LENGTH_SHORT).show();
                }else if (RA_DE_TARI.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "La tarifa no puede estar vacía.", Toast.LENGTH_SHORT).show();
                }else if (RA_DE_CANT.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "La cantidad no puede estar vacía.", Toast.LENGTH_SHORT).show();
                }else if (Integer.valueOf(RA_DE_TARI.getText().toString())<2) {
                    Toast.makeText(getActivity(), "La tarifa no puede ser menor a 2 soles.", Toast.LENGTH_SHORT).show();
                }else if (Integer.valueOf(RA_DE_CANT.getText().toString())==0) {
                    Toast.makeText(getActivity(), "La cantidad no puede ser cero.", Toast.LENGTH_SHORT).show();
                }else if (Integer.valueOf(RA_DE_CANT.getText().toString())>20) {
                    Toast.makeText(getActivity(), "La cantidad no puede ser mayor a 20.", Toast.LENGTH_SHORT).show();
                }else if (lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999" && RA_DE_TPPR2.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Especificar que Otro tipo de producto se esta registrando.", Toast.LENGTH_SHORT).show();
                }else if(RA_DE_SERI.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "La serie ha sido modificada, ingresar serie y volver a buscar", Toast.LENGTH_SHORT).show();
                }else if(RA_DE_CORR.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "El correlativo ha sido modificado, ingresar correlativo, y volver a buscar", Toast.LENGTH_SHORT).show();
                }else if(!CABECE.equals("COM")){
                    Toast.makeText(getActivity(), "No existe serie de carga, no se puede imprimir el documento, verifica los campos y vuelve a intentar", Toast.LENGTH_SHORT).show();
                }else{
                    try{
                        Date date = new Date();
                        String FeEmisCarg_impr = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        String HoEmisCarg_impr = new SimpleDateFormat("hh:mm a").format(date);
                        String PrLetrCarg_impr = sharedPreferences.getString("GD_PRISERI","NE");
                        String NuDocuCarg_impr = sharedPreferences.getString("GD_NuDoCa","NE");
                        String CoEmprCarg_impr = sharedPreferences.getString("GD_CoEmpr","NE");
                        String NuSeriCarg_impr = NuDocuCarg_impr.substring(0,4);
                        String NuCorrCarg_impr = NuDocuCarg_impr.substring(7,15);
                        String NuDocuViaj_impr = sharedPreferences.getString("GD_NuDocu","NE");
                        String NuAsieViaj_impr = sharedPreferences.getString("GD_NuAsie","NE");
                        String CoOrigViaj_imp1 = sharedPreferences.getString("GD_CoOrig","NE");
                        String DeOrigViaj_imp1 = sharedPreferences.getString("GD_DeOrig","NE");
                        String DeOrigViaj_impr = sharedPreferences.getString("GD_DeOrig","NE");
                        String CoDestViaj_imp1 = sharedPreferences.getString("GD_CoDest","NE");
                        String DeDestViaj_imp1 = sharedPreferences.getString("GD_DeDest","NE");
                        String DeDestViaj_impr = sharedPreferences.getString("GD_DeDest","NE");
                        String CoClieCarg_impr = sharedPreferences.getString("GD_CoClie","NE");
                        String NoClieCarg_impr = sharedPreferences.getString("GD_NoClie","NE");
                        String NuDnisCarg_impr = sharedPreferences.getString("GD_NuDnis","NE");
                        String NoPasaCarg_impr = sharedPreferences.getString("GD_NoPasa","NE");
                        String CoVehiCarg_impr = sharedPreferences.getString("GD_CoVehi","NE");
                        String CoRumbCarg_impr = sharedPreferences.getString("GD_CoRumb","NoData");
                        String FeViajCarg_impr = sharedPreferences.getString("GD_FeViaj","NoData");
                        String HoViajCarg_impr = sharedPreferences.getString("GD_HoViaj","NE");
                        String NoVendCarg_impr = sharedPreferences.getString("nombreEmpleado","NE");
                        String DeAgenCarg_impr = sharedPreferences.getString("DesAgencia","NE");
                        String CaProdCarg_impr = RA_DE_CANT.getText().toString();
                        String ImTotaCarg_impr = RA_DE_TARI.getText().toString();
                        String IdProdCarg_impr ="";
                        String DeProdCarg_impr ="";
                        String DeProdCarg_Tram ="";
                        String TiDocuCarg_Tram ="";
                        String Ted_QR_Carg="";

                        if(PrLetrCarg_impr.equals("B")){
                            TiDocuCarg_Tram="BOL";
                        }else{
                            TiDocuCarg_Tram="FAC";
                        }

                        if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999"){
                            IdProdCarg_impr="054";
                            DeProdCarg_impr=RA_DE_TPPR2.getText().toString();
                            DeProdCarg_Tram="BULTO";
                        }else if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())!="999"){
                            IdProdCarg_impr=lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                            DeProdCarg_impr=lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                            DeProdCarg_Tram=lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                        }

                        Ted_QR_Carg =  "20106076635|03"
                                +"|"+NuSeriCarg_impr
                                +"|"+NuCorrCarg_impr
                                +"|0.00|"+CaProdCarg_impr+"|"
                                +FeEmisCarg_impr+"|1|"
                                +CoClieCarg_impr+"|TED|"
                                +CoEmprCarg_impr+"|"
                                +CoRumbCarg_impr+"|"
                                +CoOrigViaj_imp1+"|"
                                +CoDestViaj_imp1+"|"
                                +NuDocuViaj_impr+"|"
                                +NoClieCarg_impr+"|B"
                                +CoVehiCarg_impr+"|"
                                +FeViajCarg_impr+"|0|CARGA";

                        guardarDataMemoria("extra_idProducto", lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
                        guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
                        guardarDataMemoria("extra_importe", RA_DE_TARI.getText().toString());
                        guardarDataMemoria("extra_cantidad", RA_DE_CANT.getText().toString());
                        String numCorrelativoCargaCompleto = "0"+sharedPreferences.getString("extra_correDocu", "NoData");
                        guardarDataMemoria("guardar_correlativoCargaCompleto", numCorrelativoCargaCompleto);



                        guardarDataMemoria("FeEmisCarg_impr", FeEmisCarg_impr);
                        guardarDataMemoria("HoEmisCarg_impr", HoEmisCarg_impr);
                        guardarDataMemoria("PrLetrCarg_impr", PrLetrCarg_impr);
                        guardarDataMemoria("NuDocuCarg_impr", NuDocuCarg_impr);
                        guardarDataMemoria("NuSeriCarg_impr", NuSeriCarg_impr);
                        guardarDataMemoria("NuCorrCarg_impr", NuCorrCarg_impr);
                        guardarDataMemoria("NuDocuViaj_impr", NuDocuViaj_impr);
                        guardarDataMemoria("NuAsieViaj_impr", NuAsieViaj_impr);
                        guardarDataMemoria("CoOrigViaj_imp1", CoOrigViaj_imp1);
                        guardarDataMemoria("DeOrigViaj_imp1", DeOrigViaj_imp1);
                        guardarDataMemoria("DeOrigViaj_impr", DeOrigViaj_impr);
                        guardarDataMemoria("CoDestViaj_imp1", CoDestViaj_imp1);
                        guardarDataMemoria("DeDestViaj_imp1", DeDestViaj_imp1);
                        guardarDataMemoria("DeDestViaj_impr", DeDestViaj_impr);
                        guardarDataMemoria("CoClieCarg_impr", CoClieCarg_impr);
                        guardarDataMemoria("NoClieCarg_impr", NoClieCarg_impr);
                        guardarDataMemoria("NuDnisCarg_impr", NuDnisCarg_impr);
                        guardarDataMemoria("NoPasaCarg_impr", NoPasaCarg_impr);
                        guardarDataMemoria("CoVehiCarg_impr", CoVehiCarg_impr);
                        guardarDataMemoria("CoRumbCarg_impr", CoRumbCarg_impr);
                        guardarDataMemoria("FeViajCarg_impr", FeViajCarg_impr);
                        guardarDataMemoria("HoViajCarg_impr", HoViajCarg_impr);
                        guardarDataMemoria("NoVendCarg_impr", NoVendCarg_impr);
                        guardarDataMemoria("DeAgenCarg_impr", DeAgenCarg_impr);
                        guardarDataMemoria("CaProdCarg_impr", CaProdCarg_impr);
                        guardarDataMemoria("ImTotaCarg_impr", ImTotaCarg_impr);
                        guardarDataMemoria("IdProdCarg_impr", IdProdCarg_impr);
                        guardarDataMemoria("DeProdCarg_impr", DeProdCarg_impr);
                        guardarDataMemoria("DeProdCarg_Tram", DeProdCarg_Tram);
                        guardarDataMemoria("TiDocuCarg_Tram", TiDocuCarg_Tram);

                        RealizaVenta(CoEmprCarg_impr,DeProdCarg_impr,"S",sharedPreferences,mPrinter,PrLetrCarg_impr,NuDocuCarg_impr,NuSeriCarg_impr,NuCorrCarg_impr,NuDocuViaj_impr,NuAsieViaj_impr,DeOrigViaj_impr,DeDestViaj_impr,CoClieCarg_impr,NoClieCarg_impr,NuDnisCarg_impr,NoPasaCarg_impr,FeViajCarg_impr,HoViajCarg_impr,NoVendCarg_impr,DeAgenCarg_impr,CaProdCarg_impr,ImTotaCarg_impr,TiDocuCarg_Tram,IdProdCarg_impr,DeProdCarg_impr,DeProdCarg_Tram,Ted_QR_Carg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        RA_DE_BIMP2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String CABECE = TT_NU_DOCU.getText().toString().substring(0,3);
                Spinner_model st = (Spinner_model)RD_DE_TPDC.getSelectedItem();
                String Respuesta = FuncionesAuxiliares.ValidaRuc(RD_DE_RUC.getText().toString().trim());
                String CO_CAJA = sharedPreferences.getString("CodCaja","NoData");
                String PRISERI = sharedPreferences.getString("GD_PRISERI","NoData");
                guardarDataMemoria("GD_NoClie", RD_DE_CLNT.getText().toString());
                guardarDataMemoria("GD_DeOrig", "-");
                guardarDataMemoria("GD_DeDest", "-");
                if(CO_CAJA.equals("NoData")){
                    Toast.makeText(getActivity(),"ASIGNA UNA CAJA", Toast.LENGTH_SHORT).show();
                }else if(RA_DE_TARI.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "La tarifa no puede estar vacía.", Toast.LENGTH_SHORT).show();
                }else if(RA_DE_CANT.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "La cantidad no puede estar vacía.", Toast.LENGTH_SHORT).show();
                }else if(Integer.valueOf(RA_DE_TARI.getText().toString())<2) {
                    Toast.makeText(getActivity(), "La tarifa no puede ser menor a 2 soles.", Toast.LENGTH_SHORT).show();
                }else if(Integer.valueOf(RA_DE_CANT.getText().toString())==0) {
                    Toast.makeText(getActivity(), "La cantidad no puede ser cero.", Toast.LENGTH_SHORT).show();
                }else if(Integer.valueOf(RA_DE_CANT.getText().toString())>20) {
                    Toast.makeText(getActivity(), "La cantidad no puede ser mayor a 20.", Toast.LENGTH_SHORT).show();
                }else if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999" && RA_DE_TPPR2.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Especificar que Otro tipo de producto se esta registrando.", Toast.LENGTH_SHORT).show();
                }else if(st.id=="3" && RD_DE_RZSC.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese la Razon Social", Toast.LENGTH_SHORT).show();
                }else if(st.id=="3" && RD_DE_RUC.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese el Numero de RUC", Toast.LENGTH_SHORT).show();
                }else if(st.id=="3" && !Respuesta.equals("INFINITY_DEV")){
                    Toast.makeText(getActivity(), Respuesta, Toast.LENGTH_SHORT).show();
                }else if(RD_DE_CLNT.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese el Nombre del Cliente", Toast.LENGTH_SHORT).show();
                }else if(st.id=="3" && RD_DE_DOCU.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese el Numero de DNI", Toast.LENGTH_SHORT).show();
                }else if(st.id=="2" && RD_DE_DOCU.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese el Numero de Documento", Toast.LENGTH_SHORT).show();
                }else if(st.id=="4" && RD_DE_CEPS.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Ingrese el Numero de Documento", Toast.LENGTH_SHORT).show();
                }else if(!CABECE.equals("COM")){
                    Toast.makeText(getActivity(), "No existe serie de carga, no se puede imprimir el documento, verifica los campos y vuelve a intentar", Toast.LENGTH_SHORT).show();
                }else{
                    try{
                        Date date = new Date();
                        String co_clie="";
                        if(st.id=="4" || st.id=="5"){
                            co_clie=RD_DE_CEPS.getText().toString();
                        }
                        else if(st.id=="2" || st.id=="3"){
                            co_clie=RD_DE_DOCU.getText().toString();
                        }
                        String FeEmisCarg_impr = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        String HoEmisCarg_impr = new SimpleDateFormat("hh:mm a").format(date);
                        String PrLetrCarg_imp0 = sharedPreferences.getString("GD_NuDoCa","NoData");
                        String PrLetrCarg_impr = PrLetrCarg_imp0.substring(0,1);
                        String NuDocuCarg_impr = sharedPreferences.getString("GD_NuDoCa","NE");
                        String NuSeriCarg_impr = NuDocuCarg_impr.substring(0,4);
                        String NuCorrCarg_impr = NuDocuCarg_impr.substring(7,15);
                        String CoEmprCarg_impr = sharedPreferences.getString("GD_CoEmpr","NE");
                        String NuDocuViaj_impr = "";
                        String NuAsieViaj_impr = "";
                        String CoOrigViaj_imp1 = "";
                        String DeOrigViaj_imp1 = "";
                        String DeOrigViaj_impr = "0";
                        String CoDestViaj_imp1 = "";
                        String DeDestViaj_imp1 = "";
                        String DeDestViaj_impr = "0";
                        String CoClieCarg_impr = "";
                        String NoClieCarg_impr = "";
                        String NuDnisCarg_impr = "";
                        String NoPasaCarg_impr = "";
                        String CoVehiCarg_impr = "";
                        String CoRumbCarg_impr = "";
                        String FeViajCarg_impr = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        String HoViajCarg_impr = new SimpleDateFormat("HH:mm").format(date);
                        String NoVendCarg_impr = sharedPreferences.getString("nombreEmpleado","NE");
                        String DeAgenCarg_impr = sharedPreferences.getString("DesAgencia","NE");
                        String CaProdCarg_impr = RA_DE_CANT.getText().toString();
                        String ImTotaCarg_impr = RA_DE_TARI.getText().toString();
                        String IdProdCarg_impr ="";
                        String DeProdCarg_impr ="";
                        String DeProdCarg_Tram ="";
                        String TiDocuCarg_Tram ="";
                        String Ted_QR_Carg="";

                        if(PrLetrCarg_impr.equals("B")){
                            TiDocuCarg_Tram="BOL";
                            CoClieCarg_impr=co_clie;
                            NoClieCarg_impr=RD_DE_CLNT.getText().toString();
                            NuDnisCarg_impr=co_clie;
                            NoPasaCarg_impr=RD_DE_CLNT.getText().toString();
                        }else{
                            TiDocuCarg_Tram="FAC";
                            CoClieCarg_impr=RD_DE_RUC.getText().toString();
                            NoClieCarg_impr=RD_DE_RZSC.getText().toString();
                            NuDnisCarg_impr=co_clie;
                            NoPasaCarg_impr=RD_DE_CLNT.getText().toString();
                        }

                        if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999"){
                            IdProdCarg_impr="054";
                            DeProdCarg_impr=RA_DE_TPPR2.getText().toString();
                            DeProdCarg_Tram="BULTO";
                        }else if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())!="999"){
                            IdProdCarg_impr=lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                            DeProdCarg_impr=lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                            DeProdCarg_Tram=lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition());
                        }

                        Ted_QR_Carg =  "20106076635|03"
                                +"|"+NuSeriCarg_impr
                                +"|"+NuCorrCarg_impr
                                +"|0.00|"+CaProdCarg_impr+"|"
                                +FeEmisCarg_impr+"|1|"
                                +CoClieCarg_impr+"|TED|"
                                +CoEmprCarg_impr+"|"
                                +CoRumbCarg_impr+"|"
                                +CoOrigViaj_imp1+"|"
                                +CoDestViaj_imp1+"|"
                                +NuDocuViaj_impr+"|"
                                +NoClieCarg_impr+"|B"
                                +CoVehiCarg_impr+"|"
                                +FeViajCarg_impr+"|0|CARGA";

                        guardarDataMemoria("extra_idProducto", lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
                        guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
                        guardarDataMemoria("extra_importe", RA_DE_TARI.getText().toString());
                        guardarDataMemoria("extra_cantidad", RA_DE_CANT.getText().toString());
                        String numCorrelativoCargaCompleto = "0"+sharedPreferences.getString("extra_correDocu", "NoData");
                        guardarDataMemoria("guardar_correlativoCargaCompleto", numCorrelativoCargaCompleto);



                        guardarDataMemoria("GD_CoEmpr", CoEmprCarg_impr);
                        guardarDataMemoria("FeEmisCarg_impr", FeEmisCarg_impr);
                        guardarDataMemoria("HoEmisCarg_impr", HoEmisCarg_impr);
                        guardarDataMemoria("PrLetrCarg_impr", PrLetrCarg_impr);
                        guardarDataMemoria("NuDocuCarg_impr", NuDocuCarg_impr);
                        guardarDataMemoria("NuSeriCarg_impr", NuSeriCarg_impr);
                        guardarDataMemoria("NuCorrCarg_impr", NuCorrCarg_impr);
                        guardarDataMemoria("NuDocuViaj_impr", NuDocuViaj_impr);
                        guardarDataMemoria("NuAsieViaj_impr", NuAsieViaj_impr);
                        guardarDataMemoria("CoOrigViaj_imp1", CoOrigViaj_imp1);
                        guardarDataMemoria("DeOrigViaj_imp1", DeOrigViaj_imp1);
                        guardarDataMemoria("DeOrigViaj_impr", DeOrigViaj_impr);
                        guardarDataMemoria("CoDestViaj_imp1", CoDestViaj_imp1);
                        guardarDataMemoria("DeDestViaj_imp1", DeDestViaj_imp1);
                        guardarDataMemoria("DeDestViaj_impr", DeDestViaj_impr);
                        guardarDataMemoria("CoClieCarg_impr", CoClieCarg_impr);
                        guardarDataMemoria("NoClieCarg_impr", NoClieCarg_impr);
                        guardarDataMemoria("NuDnisCarg_impr", NuDnisCarg_impr);
                        guardarDataMemoria("NoPasaCarg_impr", NoPasaCarg_impr);
                        guardarDataMemoria("CoVehiCarg_impr", CoVehiCarg_impr);
                        guardarDataMemoria("CoRumbCarg_impr", CoRumbCarg_impr);
                        guardarDataMemoria("FeViajCarg_impr", FeViajCarg_impr);
                        guardarDataMemoria("HoViajCarg_impr", HoViajCarg_impr);
                        guardarDataMemoria("NoVendCarg_impr", NoVendCarg_impr);
                        guardarDataMemoria("DeAgenCarg_impr", DeAgenCarg_impr);
                        guardarDataMemoria("CaProdCarg_impr", CaProdCarg_impr);
                        guardarDataMemoria("ImTotaCarg_impr", ImTotaCarg_impr);
                        guardarDataMemoria("IdProdCarg_impr", IdProdCarg_impr);
                        guardarDataMemoria("DeProdCarg_impr", DeProdCarg_impr);
                        guardarDataMemoria("DeProdCarg_Tram", DeProdCarg_Tram);
                        guardarDataMemoria("TiDocuCarg_Tram", TiDocuCarg_Tram);

                        RealizaVenta(CoEmprCarg_impr,DeProdCarg_impr,"N",sharedPreferences,mPrinter,PrLetrCarg_impr,NuDocuCarg_impr,NuSeriCarg_impr,NuCorrCarg_impr,NuDocuViaj_impr,NuAsieViaj_impr,DeOrigViaj_impr,DeDestViaj_impr,CoClieCarg_impr,NoClieCarg_impr,NuDnisCarg_impr,NoPasaCarg_impr,FeViajCarg_impr,HoViajCarg_impr,NoVendCarg_impr,DeAgenCarg_impr,CaProdCarg_impr,ImTotaCarg_impr,TiDocuCarg_Tram,IdProdCarg_impr,DeProdCarg_impr,DeProdCarg_Tram,Ted_QR_Carg);














//                        String cliente="";
//                        String co_clie="";
//                        String cabcomp="";
//                        String info0="";
//                        String info1="";
//                        String info2="";
//                        String info3="";
//                        Date date = new Date();
//                        String fecha = new SimpleDateFormat("yyyy-MM-dd").format(date);
//                        String NuDoCa = sharedPreferences.getString("GD_NuDoCa","NoData");
//                        String NoEmpl = sharedPreferences.getString("nombreEmpleado","NoData");
//                        String DeAgen = sharedPreferences.getString("DesAgencia","NoData");
//                        if(st.id=="4" || st.id=="5"){
//                            co_clie=RD_DE_CEPS.getText().toString();
//                        }
//                        else if(st.id=="2" || st.id=="3"){
//                            co_clie=RD_DE_DOCU.getText().toString();
//                        }
//                        if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())=="999"){
//                            guardarDataMemoria("GD_IDPROD", "054");
//                            guardarDataMemoria("GD_DEPDIM", RA_DE_TPPR2.getText().toString());
//                            guardarDataMemoria("GD_DEPDTD", "BULTO");
//                        }else if(lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition())!="999"){
//                            guardarDataMemoria("GD_IDPROD", lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
//                            guardarDataMemoria("GD_DEPDIM", lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
//                            guardarDataMemoria("GD_DEPDTD", lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
//                        }
//                        guardarDataMemoria("GD_TARI", RA_DE_TARI.getText().toString());
//                        guardarDataMemoria("GD_CANT", RA_DE_CANT.getText().toString());
//                        if(PRISERI.equals("B")){
//                            guardarDataMemoria("extra_RazonSocial", RD_DE_CLNT.getText().toString());
//                            guardarDataMemoria("extra_documentoCliente", co_clie);
//                            guardarDataMemoria("GD_CoClie", co_clie);
//                            guardarDataMemoria("GD_NoClie", RD_DE_CLNT.getText().toString());
//                            guardarDataMemoria("GD_NuDnis", co_clie);
//                            guardarDataMemoria("GD_NoPasa", RD_DE_CLNT.getText().toString());
//                            guardarDataMemoria("GD_TIDOCU", "BOL");
//                            guardarDataMemoria("GD_CABDOC", "|  BOLETA DE VENTA ELECTRONICA |\n");
//                            cabcomp="|  BOLETA DE VENTA ELECTRONICA |\n";
//                            cliente="CLIENTE:\n"+
//                                    "   "+RD_DE_CLNT.getText().toString()+"\n" +
//                                    "DOCUMENTO:\n" +
//                                    "   "+co_clie+"\n";
//                            guardarDataMemoria("GD_CABDO2", cliente);
//                        }else if(PRISERI.equals("F")){
//                            guardarDataMemoria("extra_RazonSocial", RD_DE_RZSC.getText().toString());
//                            guardarDataMemoria("extra_documentoCliente", RD_DE_RUC.getText().toString());
//                            guardarDataMemoria("GD_CoClie", RD_DE_RUC.getText().toString());
//                            guardarDataMemoria("GD_NoClie", RD_DE_RZSC.getText().toString());
//                            guardarDataMemoria("GD_NuDnis", co_clie);
//                            guardarDataMemoria("GD_NoPasa", RD_DE_CLNT.getText().toString());
//                            guardarDataMemoria("GD_TIDOCU", "FAC");
//                            guardarDataMemoria("GD_CABDOC", "| FACTURA DE VENTA ELECTRONICA |\n");
//                            cabcomp="| FACTURA DE VENTA ELECTRONICA |\n";
//                            cliente="SEÑORES:\n" +
//                                    "   "+RD_DE_RZSC.getText().toString()+"\n" +
//                                    "RUC:\n" +
//                                    "   "+RD_DE_RUC.getText().toString()+"\n" +
//                                    "CLIENTE:\n"+
//                                    "   "+RD_DE_CLNT.getText().toString()+"\n" +
//                                    "DNI:\n" +
//                                    "   "+co_clie+"\n";
//                            guardarDataMemoria("GD_CABDO2", cliente);
//                        }
//                        info0="N°COMPROBANTE: "+NuDoCa+"\n";
//                        info1="" +
//                                "VEHICULO:   0000\n" +
//                                "TIPO:       ALMACENAMIENTO\n";
//                        info2="" +
//                                "ORIGEN:     -\n" +
//                                "DESTINO:    -\n";
//                        info3="" +
//                                "AGENCIA:    "+DeAgen+"\n" +
//                                "VENDEDOR:   "+NoEmpl+"\n";
//                        String TedQR="";
//                        TedQR =  "20106076635|03"
//                                +"|"+NuDoCa.substring(0,4)
//                                +"|"+NuDoCa.substring(7,15)
//                                +"|0.00|"
//                                +RA_DE_TARI.getText().toString()+"|"
//                                +fecha+"|1|"
//                                +co_clie+"|TED|01|"
//                                +sharedPreferences.getString("extra_rumbo", "SRB")+"|"
//                                +sharedPreferences.getString("extra_origen", "99")+"|"
//                                +sharedPreferences.getString("extra_destino", "99")+"|"
//                                +sharedPreferences.getString("extra_serieDocu", "NoData") + "-" + sharedPreferences.getString("guardar_correlativoCargaCompleto", "NoData")+"|"
//                                +RD_DE_CLNT.getText().toString()+"|"
//                                +sharedPreferences.getString("extra_secuencia", "SRB")+"|"
//                                +fecha+"|0|CARGA";
//                        guardarDataMemoria("extra_idProducto", lista_idProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
//                        guardarDataMemoria("guardar_nombreProducto", lista_nombreProductos.get(RA_DE_TPPR.getSelectedItemPosition()));
//                        guardarDataMemoria("extra_importe", RA_DE_TARI.getText().toString());
//                        guardarDataMemoria("extra_cantidad", RA_DE_CANT.getText().toString());
//                        String numCorrelativoCargaCompleto = "0"+sharedPreferences.getString("extra_correDocu", "NoData");
//                        guardarDataMemoria("guardar_correlativoCargaCompleto", numCorrelativoCargaCompleto);
//
//                        guardarDataMemoria("cabcomp", cabcomp);
//                        guardarDataMemoria("info1", info1);
//                        guardarDataMemoria("info1", info1);
//                        guardarDataMemoria("info0", info0);
//                        guardarDataMemoria("cliente", cliente);
//                        guardarDataMemoria("info1", info1);
//                        guardarDataMemoria("info2", info2);
//                        guardarDataMemoria("info3", info3);
//                        guardarDataMemoria("TedQR", TedQR);
//
//                        new VentaAsyncrona().execute(NuDoCa);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
//    public boolean ValidaStadoImpresora()
//    {
//        try{
//            String error = String.valueOf(printer.getStatus());
//            if(error.equals("1"))
//            {
//                Toast.makeText(getActivity(),"IMPRESORA OCUPADA", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("2"))
//            {
//                Toast.makeText(getActivity(),"IMPRESORA SIN PAPEL", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("3"))
//            {
//                Toast.makeText(getActivity(),"El formato del error del paquete de datos de impresión", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//            else if(error.equals("4"))
//            {
//                Toast.makeText(getActivity(),"MAL FUNCIONAMIENTO DE LA IMPRESORA", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("8"))
//            {
//                Toast.makeText(getActivity(),"IMPRESORA SOBRE CALOR", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("9"))
//            {
//                Toast.makeText(getActivity(),"EL VOLTAJE DE LA IMPRESORA ES DEMASIADO BAJO", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("-16"))
//            {
//                Toast.makeText(getActivity(),"La impresión no está terminada", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("-6"))
//            {
//                Toast.makeText(getActivity(),"error de corte de atasco", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("-5"))
//            {
//                Toast.makeText(getActivity(),"error de apertura de la cubierta", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("-4"))
//            {
//                Toast.makeText(getActivity(),"La impresora no ha instalado la biblioteca de fuentes", Toast.LENGTH_SHORT).show();
//                return false;
//            }else if(error.equals("-2"))
//            {
//                Toast.makeText(getActivity(),"El paquete de datos es demasiado largo", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//            else{
//                return true;
//            }
//        }catch (Exception ex)
//        {
//            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
//            return  false;
//        }
//    }

    void RealizaVenta(String CoEmpr,String DeProd,String TipoRegi,SharedPreferences sharedPreferences, Printer mPrinter,String PrLetr,String NuDocu,String NuSeri,String NuCorr,String NuDoVi,String NuAsie,String DeOrig,String DeDest,String CoClie,String NoClie,String NuDnis,String NoPasa,String FeViaj,String HoViaj,String NoVend,String DeAgen, String CaProd, String ImTota, String TiDocu, String IdProd, String DePrIm, String DePrTr, String Ted_QR){
        try{
//            ProgressDialog progressDialog;
//            progressDialog = ProgressDialog.show(getActivity(),
//                    "Imprimiendo Boleto",
//                    "Espere...");
//            if(!ValidaStadoImpresora()){
//                return;
//            }else{

                final String trama = GeneTram(CoEmpr,PrLetr,NuSeri,NuCorr,CoClie,NoClie,NuDnis,NoPasa,ImTota,NoVend,DeOrig,DeDest,CaProd,DePrIm);

                String[] dataEncriptada = generarCodigoQR(trama);

                Boolean[] respuesta = guardarCompraCarga(dataEncriptada[0], dataEncriptada[1]);

                imprBoleto01(CaProd,DeProd,TipoRegi,"1",sharedPreferences,mPrinter,PrLetr,NuDocu,NuSeri,NuCorr,NuDoVi,NuAsie,DeOrig,DeDest,CoClie,NoClie,NuDnis,NoPasa,FeViaj,HoViaj,NoVend,DeAgen,ImTota,Ted_QR);
                Thread.sleep(500);
                imprBoleto01(CaProd,DeProd,TipoRegi,"2",sharedPreferences,mPrinter,PrLetr,NuDocu,NuSeri,NuCorr,NuDoVi,NuAsie,DeOrig,DeDest,CoClie,NoClie,NuDnis,NoPasa,FeViaj,HoViaj,NoVend,DeAgen,ImTota,Ted_QR);

                venta_carga_enco2 ventacargaenco = new venta_carga_enco2();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, ventacargaenco).commit();

//            }
//            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String GeneTram(String CoEmpr,String PrLetr, String NuSeri, String NuCorr, String CoClie, String NoClie, String NuDnis, String NoPasa, String ImTota, String NoVend, String DeOrig, String DeDest, String CaProd, String DeProd){
        String ti_docu = "";
        String ti_docu_clie = "";
        String no_suna = "";
        String nu_ruc = "";
        String no_razo = "";
        String no_COME = "";
        Date date = new Date();

        if(CoEmpr.equals("01")){
            no_razo="EMPRESA DE TRANSPORTES PERU BUS S.A";
            nu_ruc="20106076635";
            no_COME="E.T. PERU BUS S.A";
        }else{
            no_razo="EXPRESO SUR BUS S.A.";
            nu_ruc="20608231588";
            no_COME="EXP. SURBUS S.A.";
        }

        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";
        DateFormat FeFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat HoFormat = new SimpleDateFormat(strHoraFormat);
        final String fechaVenta = FeFormat.format(date);
        final String horaVenta = HoFormat.format(date);

        if(PrLetr.equals("F")){
            ti_docu = "01";
            no_suna = "A;FormaPago;;Contado\n";
        }else if(PrLetr.equals("B")){
            ti_docu = "03";
            no_suna = "";
        }

        if(PrLetr.equals("F")){
            ti_docu_clie = "6";
        }else if(CoClie.length() == 8){
            ti_docu_clie = "1";
        }else{
            ti_docu_clie = "7";
        }

        /* Se obtiene el monto neto y el monto de IGV */
        double montoTotal = Float.valueOf(ImTota);
        double montoSinIGV = (montoTotal * 100) / 118;
        double montoSinIGVRedondeado = Math.rint(montoSinIGV * 100) / 100;

        double montoIGV = montoTotal - montoSinIGVRedondeado;

        /* Convertir monto a letras */
        String numeroFloat = String.format("%.2f",montoTotal);
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";

        String tramaCarga ="A;Serie;;" + NuSeri + "\n" +
                "A;Correlativo;;" + NuCorr + "\n" +
                "A;RznSocEmis;;"+no_razo+"\n" +
                "A;CODI_EMPR;;1\n" +
                "A;RUTEmis;;"+nu_ruc+"\n" +
                "A;DirEmis;;AV. MEXICO NRO 333 LA VICTORIA - LIMA - LIMA\n" +
                "A;ComuEmis;;150115\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;NomComer;;"+no_COME+"\n" +
                "A;TipoDTE;;" + ti_docu + "\n" +
                "A;TipoOperacion;;0101\n"  +
                "A;TipoRutReceptor;;"+ti_docu_clie+"\n" +
                "A;RUTRecep;;" + CoClie + "\n" +
                "A;RznSocRecep;;" + NoClie + "\n" +
                "A;DirRecep;;-\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoRucEmis;;6\n"+
                "A;UrbanizaEmis;;LIMA\n" +
                "A;ProviEmis;;LIMA\n" +
                "A;CodigoAutorizacion;;000000"+"\n"+ no_suna +
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;"+String.format("%.2f", montoIGV)+"\n" +
                "A2;TasaImpuesto;1;18\n"+
                "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoSinIGV)+"\n"+
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP CARGA POR "+CaProd+" "+DeProd+"\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;" + montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", montoSinIGV)+"\n" +
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
                "E;DescripcionAdicSunat;3;"+NoVend+"\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;CONTADO\n" +
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
                "E;DescripcionAdicSunat;7;"+NoPasa+"\n" +
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
                "E;DescripcionAdicSunat;11;\"SOAT: COMPAÑIA DE SEGUROS RIMAC SEGUROS.\" VENTA NORMAL\n"+
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.PERUBUS.COM.PE\n"+
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;"+DeOrig.trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+DeDest.trim() +"\n"+
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
                "E;DescripcionAdicSunat;18;"+NuSeri+"/F-VTS-42\n"+
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
                "E;DescripcionAdicSunat;23;"+no_COME+"\n"+
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
                "E;DescripcionAdicSunat;29;-\n"+
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
                "E;DescripcionAdicSunat;34;"+DeOrig.trim()+" - "+ DeDest.trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";

        return tramaCarga;
    }

    void imprBoleto01(String CaProd,String DeProd,String TipoRegi,String VezRegi,SharedPreferences sharedPreferences, Printer mPrinter,String PrLetr,String NuDocu,String NuSeri,String NuCorr,String NuDoVi,String NuAsie,String DeOrig,String DeDest,String CoClie,String NoClie,String NuDnis,String NoPasa,String FeViaj,String HoViaj,String NoVend,String DeAgen,String ImTota, String TedQR){
        try{
            Boleta boleta = new Boleta("Carga");
            String co_empr = sharedPreferences.getString("GD_CoEmpr","NE");
            String FeEmisCarg_impr = sharedPreferences.getString("FeEmisCarg_impr","NE");
            String HoEmisCarg_impr = sharedPreferences.getString("HoEmisCarg_impr","NE");

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap1 = barcodeEncoder.encodeBitmap(NuSeri+"|00"+NuCorr, BarcodeFormat.DATA_MATRIX, 250,90);

            Bitmap bitmap2 = Bitmap.createBitmap(bitmap1, 0, 17, 250, 56);
            Matrix matrix = new Matrix();
            matrix.postRotate(0);

            Bitmap bitmap3 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(),
                    matrix, true);

            StringBuilder Ticket= new StringBuilder();
            StringBuilder Ticket2= new StringBuilder();
            String RaSoci;
            String NuRuc;

            Bitmap bitmap_perubus = BitmapFactory.decodeResource(getResources(), R.drawable.inic_perubus);
            Bitmap bitmap_surbus = BitmapFactory.decodeResource(getResources(), R.drawable.inic_subus);

            if(co_empr.equals("01")){
                RaSoci="EMPR. DE TRANSPORTES PERUBUS S.A";
                NuRuc="20106076635";
            }else {
                RaSoci="EXPRESO SURBUS S.A.";
                NuRuc="20608231588";
            }


//            int printStatus = mPrinter.getPrinterStatus();
//            if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
//            }
//            else {
                PrnStrFormat format = new PrnStrFormat();
                PrnStrFormat format2 = new PrnStrFormat();
                format.setTextSize(30);
                format.setAli(Layout.Alignment.ALIGN_CENTER);
                format.setStyle(PrnTextStyle.BOLD);
                format.setFont(PrnTextFont.CUSTOM);
//                        format.setPath(Environment.getExternalStorageDirectory() +
//                                "/fonts/simsun.ttf");
                format.setTextSize(25);
//                format.setStyle(PrnTextStyle.NORMAL);
//                format.setAli(Layout.Alignment.ALIGN_NORMAL);
                Ticket.append("\n"+RaSoci+"\n");
                Ticket.append("AV. MEXICO NRO 333 P. J MATUTE\n");
                Ticket.append("RUC  "+NuRuc+"\n");
                Ticket.append("N°COMPROBANTE: "+NuDocu+"\n");
                Ticket.append("FECHA EMISION: "+FeEmisCarg_impr+"\n");
                Ticket.append("HORA EMISION: "+HoEmisCarg_impr+"\n");
                Ticket.append("________________________________\n");
                if(PrLetr.equals("B")){
                    Ticket.append("BOLETA DE VENTA ELECTRONICA\n");
                }else if(!PrLetr.equals("B")){
                    Ticket.append("FACTURA DE VENTA ELECTRONICA\n");
                }

                if(VezRegi.equals("1")){
                    Ticket.append("_________CARGA USUARIO__________\n");
                }else if(!VezRegi.equals("1")){
                    Ticket.append("______CARGA TRANSPORTISTA_______\n");
                }

                if(TipoRegi.equals("S")){
                    Ticket.append("DOCU.ASOCIADO: "+NuDoVi+"\n");
                    Ticket.append("FECHA EMBARQUE: "+FeViaj+"\n");
                    Ticket.append("HORA EMBARQUE: "+HoViaj+"\n");
                    Ticket.append("ASIENTO: "+NuAsie+"\n");
                    Ticket.append("RUMBO: "+DeOrig+" - "+DeDest+"\n");
                }else{
                    Ticket.append("PRODUCTO: "+DeProd+"\n");
                }

                if(PrLetr.equals("F")){
                    Ticket.append("CLIENTE: "+NoPasa+"\n");
                    Ticket.append("DOC.IDENTIDAD: "+NuDnis+"\n");
                    Ticket.append("RAZON SOCIAL: "+NoClie+"\n");
                    Ticket.append("RUC: "+CoClie+"\n");
                }else if(PrLetr.equals("B")){
                    Ticket.append("CLIENTE: "+NoClie+"\n");
                    Ticket.append("DOC.IDENTIDAD: "+CoClie+"\n");
                }
                Ticket.append("TIPO DE PAGO: CONTADO\n");
                Ticket.append("AGENCIA: "+DeAgen+"\n");
                Ticket.append("VENDEDOR: "+NoVend+"\n");
                Ticket.append("________________________________\n");
                Ticket.append("CANT. DESCRP            IMPORTE\n");
                Ticket.append(" "+CaProd+"    BULTO            "+Float.valueOf(ImTota)+"\n");
                Ticket.append("________________________________\n");

                double gravada = 0;
                double igv = 0;
                String gravada2 = "";
                String igv2 = "";
                gravada = (double) (Float.valueOf(ImTota) / 1.18);
                igv = Float.valueOf(ImTota) - gravada;
                gravada = Double.parseDouble(new DecimalFormat("#.##").format(gravada));
                igv = Double.parseDouble(new DecimalFormat("#.##").format(igv));
                gravada2 = gravada+"";
                igv2 = igv+"";
                Ticket.append("OP.GRAVADAS         S/. "+gravada2+"\n");
                Ticket.append("OP.I.G.V.           S/. "+igv2+"\n");
                Ticket.append("IMPORTE TOTAL       S/. "+Float.valueOf(ImTota)+"\n");

                Ticket2.append("Al recibir el presente documento" +"\n");
                Ticket2.append("acepto todos  los terminos y las" +"\n");
                Ticket2.append("condiciones   del   contrato  de" +"\n");
                Ticket2.append("servicio de transporte descritas" +"\n");
                Ticket2.append("y publicadas en letreros, banner" +"\n");
                Ticket2.append("y   paneles   ubicados   en  los" +"\n");
                Ticket2.append("Terminales     Terrestres    y/o" +"\n");
                Ticket2.append("oficinas de  venta y en  nuestra" +"\n");
                Ticket2.append("pagina  web   WWW.PERUBUS.COM.PE" +"\n");
                Ticket2.append("Autorizado  mediante resolucion:" +"\n");
                Ticket2.append("N° 0180050002160" +"\n");
                printer.init();
                printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//                mPrinter.setPrintAppendBitmap(bitmap3,Layout.Alignment.ALIGN_CENTER);
//                mPrinter.setPrintAppendString(Ticket.toString(), format);
//                mPrinter.setPrintAppendBitmap(boleta.getQRBitmap(TedQR),Layout.Alignment.ALIGN_CENTER);
//                mPrinter.setPrintAppendString(Ticket2.toString(), format);
//                mPrinter.setPrintAppendString("\n\n", format);

                if(co_empr.equals("01")){
                    printer.printBitmap(bitmap_perubus);
                }else if(!co_empr.equals("01")){
                    printer.printBitmap(bitmap_surbus);
                }

                printer.printStr("\n", null);
                printer.printStr(Ticket.toString(), null);
                printer.printBitmap(boleta.getQRBitmap(TedQR));
                printer.printStr(Ticket2.toString(), null);
                printer.printBitmap(bitmap3);
                printer.printStr("\n\n\n\n\n\n", null);
                int iRetError = printer.start();

                if (iRetError != 0x00) {
                    if (iRetError == 0x02) {

                    }
                }


//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
//                printStatus = mPrinter.setPrintStart();
//            }


















//            printer.init();
//            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//            Date date = new Date();
//
//
//            Bitmap bitmap_perubus = BitmapFactory.decodeResource(getResources(), R.drawable.inic_perubus);
//            Bitmap bitmap_surbus = BitmapFactory.decodeResource(getResources(), R.drawable.inic_subus);
//
//            if(co_empr.equals("01")){
//                RaSoci="EMPR. DE TRANSPORTES PERUBUS S.A";
//                NuRuc="20106076635";
//            }else {
//                RaSoci="EXPRESO SURBUS S.A.";
//                NuRuc="20608231588";
//            }
//
//
//
//            StringBuilder Ticket= new StringBuilder();
//            StringBuilder Ticket2= new StringBuilder();
//            Ticket.append("EMPR. DE TRANSPORTES PERUBUS S.A\n");
//            Ticket.append("AV. MEXICO NRO 333 P. J MATUTE\n");
//            Ticket.append("RUC  20106076635\n");
//            Ticket.append("N°COMPROBANTE: "+NuDocu+"\n");
//            Ticket.append("FECHA EMISION: "+FeEmisCarg_impr+"\n");
//            Ticket.append("HORA EMISION: "+HoEmisCarg_impr+"\n");
//            Ticket.append("________________________________\n");
//            if(PrLetr.equals("B")){
//                Ticket.append("BOLETA DE VENTA ELECTRONICA\n");
//            }else if(!PrLetr.equals("B")){
//                Ticket.append("FACTURA DE VENTA ELECTRONICA\n");
//            }
//
//            if(VezRegi.equals("1")){
//                Ticket.append("_________CARGA USUARIO__________\n");
//            }else if(!VezRegi.equals("1")){
//                Ticket.append("______CARGA TRANSPORTISTA_______\n");
//            }
//
//            if(TipoRegi.equals("S")){
//                Ticket.append("DOCU.ASOCIADO: "+NuDoVi+"\n");
//                Ticket.append("FECHA EMBARQUE: "+FeViaj+"\n");
//                Ticket.append("HORA EMBARQUE: "+HoViaj+"\n");
//                Ticket.append("ASIENTO: "+NuAsie+"\n");
//                Ticket.append("RUMBO: "+DeOrig+" - "+DeDest+"\n");
//            }else{
//                Ticket.append("PRODUCTO: "+DeProd+"\n");
//            }
//
//            if(PrLetr.equals("F")){
//                Ticket.append("CLIENTE: "+NoPasa+"\n");
//                Ticket.append("DOC.IDENTIDAD: "+NuDnis+"\n");
//                Ticket.append("RAZON SOCIAL: "+NoClie+"\n");
//                Ticket.append("RUC: "+CoClie+"\n");
//            }else if(PrLetr.equals("B")){
//                Ticket.append("CLIENTE: "+NoClie+"\n");
//                Ticket.append("DOC.IDENTIDAD: "+CoClie+"\n");
//            }
//            Ticket.append("TIPO DE PAGO: CONTADO\n");
//            Ticket.append("AGENCIA: "+DeAgen+"\n");
//            Ticket.append("VENDEDOR: "+NoVend+"\n");
//            Ticket.append("________________________________\n");
//            Ticket.append("CANT. DESCRP            IMPORTE\n");
//            Ticket.append(" "+CaProd+"    BULTO            "+Float.valueOf(ImTota)+"\n");
//            Ticket.append("________________________________\n");
//
//            double gravada = 0;
//            double igv = 0;
//            String gravada2 = "";
//            String igv2 = "";
//            gravada = (double) (Float.valueOf(ImTota) / 1.18);
//            igv = Float.valueOf(ImTota) - gravada;
//            gravada = Double.parseDouble(new DecimalFormat("#.##").format(gravada));
//            igv = Double.parseDouble(new DecimalFormat("#.##").format(igv));
//            gravada2 = gravada+"";
//            igv2 = igv+"";
//            Ticket.append("OP.GRAVADAS         S/. "+gravada2+"\n");
//            Ticket.append("OP.I.G.V.           S/. "+igv2+"\n");
//            Ticket.append("IMPORTE TOTAL       S/. "+Float.valueOf(ImTota)+"\n");
//
//            Ticket2.append("Al recibir el presente documento" +"\n");
//            Ticket2.append("acepto todos  los terminos y las" +"\n");
//            Ticket2.append("condiciones   del   contrato  de" +"\n");
//            Ticket2.append("servicio de transporte descritas" +"\n");
//            Ticket2.append("y publicadas en letreros, banner" +"\n");
//            Ticket2.append("y   paneles   ubicados   en  los" +"\n");
//            Ticket2.append("Terminales     Terrestres    y/o" +"\n");
//            Ticket2.append("oficinas de  venta y en  nuestra" +"\n");
//            Ticket2.append("pagina  web   WWW.PERUBUS.COM.PE" +"\n");
//            Ticket2.append("Autorizado  mediante resolucion:" +"\n");
//            Ticket2.append("N° 0180050002160" +"\n");
//            Ticket2.append("\n");
//            Ticket2.append("\n");
//            Ticket2.append("\n");
//            Ticket2.append("\n");
//
//            printerx.printBitmap(bitmap3);
//            printerx.printStr("\n", null);
//            printerx.printStr(Ticket.toString(), null);
//            printerx.printBitmap(boleta.getQRBitmap(TedQR));
//            printerx.printStr(Ticket2.toString(), null);
//            printerx.printStr("\n\n", null);
//            int iRetError = printerx.start();
//
//            if (iRetError != 0x00) {
//                if (iRetError == 0x02) {
//
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Imprcab1( Printer mPrinter,String cabcomp,String info0,String cliente){
        try{
//            String NuSeriCarg = sharedPreferences.getString("NU_SERI_CARG","NoData");
//            String NuCorrCarg = sharedPreferences.getString("NU_CORR_CARG","NoData");
//
//            printerx.init();
//            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//            Date date = new Date();
//
//            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//            Bitmap bitmap1 = barcodeEncoder.encodeBitmap(NuSeriCarg+"|"+NuCorrCarg, BarcodeFormat.CODE_128, 2050,250);
//
//
//            StringBuilder Ticket= new StringBuilder();
//            Ticket.append("EMPR. DE TRANSPORTES PERUBUS S.A\n");
//            Ticket.append("AV. MEXICO NRO 333 P. J MATUTE\n");
//            Ticket.append("RUC  20106076635\n");
//            Ticket.append(info0);
//            Ticket.append("       LA VICTORIA - LIMA\n");
//            Ticket.append("________________________________\n");
//            Ticket.append(cabcomp);
//            Ticket.append("|            (CARGA)           |\n");
//            Ticket.append("|            USUARIO           |\n");
//            Ticket.append("________________________________\n");
//            Ticket.append(cliente);
//            printerx.printBitmap(bitmap1);
//            printerx.printStr("\n", null);
//            printerx.printStr(Ticket.toString(), null);
//            int iRetError = printerx.start();
//
//            if (iRetError != 0x00) {
//                if (iRetError == 0x02) {
//
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void Imprcab2( Printer mPrinter,String cabcomp,String info0,String cliente){
        try{
//            printerx.init();
//            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//            Date date = new Date();
//            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);
//            StringBuilder Ticket= new StringBuilder();
//            Ticket.append("EMPR. DE TRANSPORTES PERUBUS S.A\n");
//            Ticket.append("        RUC  20106076635\n");
//            Ticket.append(" AV. MEXICO NRO 333 P. J MATUTE\n");
//            Ticket.append("       LA VICTORIA - LIMA\n");
//            Ticket.append("________________________________\n");
//            Ticket.append(cabcomp);
//            Ticket.append("|            (CARGA)           |\n");
//            Ticket.append(info0);
//            Ticket.append("|         TRANSPORTISTA        |\n");
//            Ticket.append("________________________________\n");
//            Ticket.append(cliente);
//            printerx.printStr(Ticket.toString(), null);
//            int iRetError = printerx.start();
//
//            if (iRetError != 0x00) {
//                if (iRetError == 0x02) {
//
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void Imprinfo( Printer mPrinter,String info1,String info2,String info3){
        try{
//            printerx.init();
//            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//            Date date = new Date();
//            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);
//            StringBuilder Ticket= new StringBuilder();
//            int total = Integer.parseInt(sharedPreferences.getString("GD_TARI", "NoData"));
//            double gravada = 0;
//            double igv = 0;
//            String gravada2 = "";
//            String igv2 = "";
//            gravada = (double) (total / 1.18);
//            igv = total - gravada;
//            gravada = Double.parseDouble(new DecimalFormat("#.##").format(gravada));
//            igv = Double.parseDouble(new DecimalFormat("#.##").format(igv));
//            gravada2 = gravada+"";
//            igv2 = igv+"";
//            Ticket.append(info1);
//            Ticket.append(info2);
//            Ticket.append("EMISION:    "+FechaImpresion+"\n");
//            Ticket.append(info3);
//            Ticket.append("PRODUCTO:   "+sharedPreferences.getString("GD_DEPDIM","NoData")+"\n");
//            Ticket.append("________________________________\n");
//            Ticket.append("CANT. DESCRP            IMPORTE\n");
//            Ticket.append(" "+sharedPreferences.getString("GD_CANT", "NoData")+"    CARGA         S/. "+sharedPreferences.getString("GD_TARI", "NoData")+".00\n");
//            Ticket.append("--------------------------------\n");
//            Ticket.append("OP.GRAVADAS         S/. "+gravada2+"\n");
//            Ticket.append("OP.I.G.V.           S/. "+igv2+"\n");
//            Ticket.append("IMPORTE TOTAL       S/. "+total+".00\n");
//            printerx.printStr(Ticket.toString(), null);
//            int iRetError = printerx.start();
//
//            if (iRetError != 0x00) {
//                if (iRetError == 0x02) {
//
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void ImprQR(String ted){
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

    void ImprPie( Printer mPrinter){
        try{
//            printerx.init();
//            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_32_16);
//            Date date = new Date();
//            StringBuilder Ticket= new StringBuilder();
//            Ticket.append("Al recibir el presente documento\n");
//            Ticket.append("acepto todos  los terminos y las\n");
//            Ticket.append("condiciones   del   contrato  de\n");
//            Ticket.append("servicio de transporte descritas\n");
//            Ticket.append("y publicadas en letreros, banner\n");
//            Ticket.append("y   paneles   ubicados   en  los\n");
//            Ticket.append("Terminales     Terrestres    y/o\n");
//            Ticket.append("oficinas de  venta y en  nuestra\n");
//            Ticket.append("pagina  web   WWW.PERUBUS.COM.PE\n");
//            Ticket.append("Autorizado  mediante resolucion:\n");
//            Ticket.append("N° 0180050002160\n");
//            Ticket.append("\n\n\n\n\n");
//            printerx.printStr(Ticket.toString(), null);
//            int iRetError = printerx.start();
//            if (iRetError != 0x00) {
//                if (iRetError == 0x02) {
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void GET_SERI_CARG(String CodigoCliente,final String co_empr){
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        guardarDataMemoria("GD_CoEmpr", co_empr);
        final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota4/"+co_empr+CodigoCliente+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
        MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>0)
                        {
                            JSONObject json;
                            try{
                                json = response.getJSONObject(0);
                                final String NuDoCa = json.getString("NU_DOCU_CARG");
                                TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                guardarDataMemoria("GD_PRISERI", NuDoCa.substring(0,1));
                                guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                guardarDataMemoria("GD_NuDoCa", NuDoCa);
                            }catch (Exception e)
                            {
                                Log.d("error",e.getMessage());
                            }
                        }
                        Log.d("data",response.toString());
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

    void GET_CLIE_RUC(String CodigoCliente){
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        String ws_buscarDNI = getString(R.string.ws_ruta) + "TMCLIE_Q02/"+CodigoCliente;
        RA_CA_TPPR.setVisibility(View.VISIBLE);
        RA_CA_TPPR.setLayoutParams(activo);
        RA_DE_TPPR.setVisibility(View.VISIBLE);
        RA_DE_TPPR.setLayoutParams(activo);
        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarDNI, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>0)
                        {
                            JSONObject json;
                            try{
                                json = response.getJSONObject(0);
                                final String NuDocu = json.getString("NO_CLIE");
                                RD_DE_RZSC.setText(NuDocu);
                            }catch (Exception e)
                            {
                                Log.d("error",e.getMessage());
//                                Toast.makeText(getContext(),"Ingrese los datos manualmente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getContext(),"Ingrese los datos manualmente", Toast.LENGTH_SHORT).show();
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

    void GET_CLIE_DNI(String CodigoCliente){
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        String ws_buscarDNI = getString(R.string.ws_ruta) + "TMCLIE_Q01/"+CodigoCliente;
        RA_CA_TPPR.setVisibility(View.VISIBLE);
        RA_CA_TPPR.setLayoutParams(activo);
        RA_DE_TPPR.setVisibility(View.VISIBLE);
        RA_DE_TPPR.setLayoutParams(activo);
        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarDNI, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>0)
                        {
                            JSONObject json;
                            try{
                                json = response.getJSONObject(0);
                                final String NuDocu = json.getString("NO_CLIE");
                                RD_DE_CLNT.setText(NuDocu);
                            }catch (Exception e)
                            {
                                Log.d("error",e.getMessage());
//                                Toast.makeText(getContext(),"Ingrese los datos manualmente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getContext(),"Ingrese los datos manualmente", Toast.LENGTH_SHORT).show();
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

    void GET_TARI_CANT(){
//        final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        if(!RA_DE_CLNT.getText().toString().equals("")){
            RA_CA_BIMP.setVisibility(View.VISIBLE);
            RA_CA_BIMP.setLayoutParams(activo);
            RA_DE_BIMP.setVisibility(View.VISIBLE);
            RA_DE_BIMP.setLayoutParams(activo);
            RA_CA_TARI.setVisibility(View.VISIBLE);
            RA_CA_TARI.setLayoutParams(activo);
            RA_DE_TARI.setVisibility(View.VISIBLE);
            RA_DE_TARI.setLayoutParams(activo);
            RA_CA_CANT.setVisibility(View.VISIBLE);
            RA_CA_CANT.setLayoutParams(activo);
            RA_DE_CANT.setVisibility(View.VISIBLE);
            RA_DE_CANT.setLayoutParams(activo);
        }else if(!RD_DE_CLNT.getText().toString().equals("")){
            RA_CA_BIMP2.setVisibility(View.VISIBLE);
            RA_CA_BIMP2.setLayoutParams(activo);
            RA_DE_BIMP2.setVisibility(View.VISIBLE);
            RA_DE_BIMP2.setLayoutParams(activo);
            RA_CA_TARI.setVisibility(View.VISIBLE);
            RA_CA_TARI.setLayoutParams(activo);
            RA_DE_TARI.setVisibility(View.VISIBLE);
            RA_DE_TARI.setLayoutParams(activo);
            RA_CA_CANT.setVisibility(View.VISIBLE);
            RA_CA_CANT.setLayoutParams(activo);
            RA_DE_CANT.setVisibility(View.VISIBLE);
            RA_DE_CANT.setLayoutParams(activo);
        }
    }

    void docu_ndni(){
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RD_DE_DOCU.setVisibility(View.VISIBLE);
        RD_DE_DOCU.setLayoutParams(activo);
        RD_CA_DOCU.setVisibility(View.VISIBLE);
        RD_CA_DOCU.setLayoutParams(activo);
        RD_DE_CLNT.setVisibility(View.VISIBLE);
        RD_DE_CLNT.setLayoutParams(activo);
        RD_CA_CLNT.setVisibility(View.VISIBLE);
        RD_CA_CLNT.setLayoutParams(activo);
    }

    void docu_nruc(){
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RD_DE_DOCU.setVisibility(View.VISIBLE);
        RD_DE_DOCU.setLayoutParams(activo);
        RD_CA_DOCU.setVisibility(View.VISIBLE);
        RD_CA_DOCU.setLayoutParams(activo);
        RD_DE_CLNT.setVisibility(View.VISIBLE);
        RD_DE_CLNT.setLayoutParams(activo);
        RD_CA_CLNT.setVisibility(View.VISIBLE);
        RD_CA_CLNT.setLayoutParams(activo);
        RD_DE_RUC.setVisibility(View.VISIBLE);
        RD_DE_RUC.setLayoutParams(activo);
        RD_CA_RUC.setVisibility(View.VISIBLE);
        RD_CA_RUC.setLayoutParams(activo);
        RD_DE_RZSC.setVisibility(View.VISIBLE);
        RD_DE_RZSC.setLayoutParams(activo);
        RD_CA_RZSC.setVisibility(View.VISIBLE);
        RD_CA_RZSC.setLayoutParams(activo);
        RA_DETA_DOCU.setText("       D.N.I:");
    }

    void docu_otro(){
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RD_DE_CEPS.setVisibility(View.VISIBLE);
        RD_DE_CEPS.setLayoutParams(activo);
        RD_CA_CEPS.setVisibility(View.VISIBLE);
        RD_CA_CEPS.setLayoutParams(activo);
        RD_DE_CLNT.setVisibility(View.VISIBLE);
        RD_DE_CLNT.setLayoutParams(activo);
        RD_CA_CLNT.setVisibility(View.VISIBLE);
        RD_CA_CLNT.setLayoutParams(activo);
        RD_DE_DOCU.setText("");
        RD_DE_CLNT.setText("");
    }

    public void CargaTipoDocumento() {
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
        RD_DE_TPDC.setAdapter(spinnerArrayAdapter);
    }

    public void CargaEmpresa() {
        final List<Spinner_model2> model = new ArrayList<>();
        Spinner_model2 TipoDocumento = new Spinner_model2("1", "SELECCIONAR", "");
        model.add(TipoDocumento);
        Spinner_model2 TipoDocumento1 = new Spinner_model2("01", "TRANSP. PERUBUS", "");
        model.add(TipoDocumento1);
        Spinner_model2 TipoDocumento2 = new Spinner_model2("39", "EXPRESO SURBUS", "");
        model.add(TipoDocumento2);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, model);
        RD_DE_EMPR.setAdapter(spinnerArrayAdapter);
    }

    void GET_MANU_DOCU(){
        try{
            String DE_DOCU = RA_DE_DOCU.getText().toString();
            String CO_CAJA = sharedPreferences.getString("CodCaja","NoData");
            final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);

            if(CO_CAJA.equals("NoData")){
                Toast.makeText(getActivity(),"ASIGNA UNA CAJA", Toast.LENGTH_SHORT).show();
            }
            else if(DE_DOCU.length()==0){
                Toast.makeText(getActivity(),"INGRESA UN DNI O RUC", Toast.LENGTH_SHORT).show();
            }
            else if(DE_DOCU.length()>0){
                final RequestQueue queue = Volley.newRequestQueue(getContext());
                final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota4/"+DE_DOCU+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length()>0)
                                {
                                    JSONObject json;
                                    try{
                                        json = response.getJSONObject(0);
                                        final String CoEmpr = json.getString("CO_EMPR");
                                        final String NuDocu = json.getString("NU_DOCU");
                                        String PrSeri = NuDocu.substring(0,1);
                                        String NuSeri = NuDocu.substring(0,4);
                                        int NuCorr = Integer.parseInt(NuDocu.substring(6,15));
                                        final String TIPDOC = json.getString("TIPO_DOCU");
                                        final String CoClie = json.getString("CO_CLIE");
                                        final String NoClie = json.getString("NO_CLIE");
                                        final String NuDnis = json.getString("NU_DNIS");
                                        final String NoPasa = json.getString("NO_PASA");
                                        final String CoVehi = json.getString("BUS");
                                        final String CoOrig = json.getString("CO_DEST_ORIG");
                                        final String DeOrig = json.getString("ORIGEN");
                                        final String CoDest = json.getString("CO_DEST_FINA");
                                        final String DeDest = json.getString("DESTINO");
                                        final String FeViaj = json.getString("FE_VIAJ");
                                        final String HoViaj = json.getString("HO_VIAJ");
                                        final String CoRumb = json.getString("CO_RUMB");
                                        final String NuAsie = json.getString("NU_ASIE");
                                        final String NuDoCa = json.getString("NU_DOCU_CARG");

                                        if(CoEmpr.equals("00")){
                                            Toast.makeText(getActivity(),"NO SE ENCONTRO EL DOCUMENTO, VERFICAR LOS DATOS ", Toast.LENGTH_SHORT).show();
                                            CL_DR_AT();
                                        }
                                        else if(!CoEmpr.equals("00") && PrSeri.equals("B")){
                                            RA_CA_CLNT.setVisibility(View.VISIBLE);
                                            RA_CA_CLNT.setLayoutParams(activo);
                                            RA_DE_CLNT.setVisibility(View.VISIBLE);
                                            RA_DE_CLNT.setLayoutParams(activo);
                                            RA_CA_TPPR.setVisibility(View.VISIBLE);
                                            RA_CA_TPPR.setLayoutParams(activo);
                                            RA_DE_TPPR.setVisibility(View.VISIBLE);
                                            RA_DE_TPPR.setLayoutParams(activo);
                                            RA_CA_RUC.setVisibility(View.INVISIBLE);
                                            RA_CA_RUC.setLayoutParams(desact);
                                            RA_DE_RUC.setVisibility(View.INVISIBLE);
                                            RA_DE_RUC.setLayoutParams(desact);
                                            RA_CA_RZSC.setVisibility(View.INVISIBLE);
                                            RA_CA_RZSC.setLayoutParams(desact);
                                            RA_DE_RZSC.setVisibility(View.INVISIBLE);
                                            RA_DE_RZSC.setLayoutParams(desact);
                                            RA_DE_SERI.setEnabled(false);
                                            RA_DE_CORR.setEnabled(false);
                                            RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                            RA_BTN_BUSC.setLayoutParams(desact);
                                            RA_DE_DOCU.setText(NuDnis);
                                            RA_DE_CLNT.setText(NoPasa);
                                            TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                            RA_DE_SERI.setText(NuSeri);
                                            RA_DE_CORR.setText(""+NuCorr);
                                            guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                            guardarDataMemoria("GD_PRISERI", PrSeri);
                                            guardarDataMemoria("GD_NuDocu", NuDocu);
                                            guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                            guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                            guardarDataMemoria("GD_TPDOC", TIPDOC);
                                            guardarDataMemoria("GD_CoClie", CoClie);
                                            guardarDataMemoria("GD_NoClie", NoClie);
                                            guardarDataMemoria("GD_NuDnis", NuDnis);
                                            guardarDataMemoria("GD_NoPasa", NoPasa);
                                            guardarDataMemoria("GD_CoVehi", CoVehi);
                                            guardarDataMemoria("GD_CoOrig", CoOrig);
                                            guardarDataMemoria("GD_DeOrig", DeOrig);
                                            guardarDataMemoria("GD_CoDest", CoDest);
                                            guardarDataMemoria("GD_DeDest", DeDest);
                                            guardarDataMemoria("GD_FeViaj", FeViaj);
                                            guardarDataMemoria("GD_HoViaj", HoViaj);
                                            guardarDataMemoria("GD_CoRumb", CoRumb);
                                            guardarDataMemoria("GD_NuAsie", NuAsie);
                                            guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                            guardarDataMemoria("GD_TipDTE", "03");
                                            guardarDataMemoria("extra_RazonSocial", NoClie);
                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                        }
                                        else if(!CoEmpr.equals("00") && PrSeri.equals("F")){
                                            RA_CA_CLNT.setVisibility(View.VISIBLE);
                                            RA_CA_CLNT.setLayoutParams(activo);
                                            RA_DE_CLNT.setVisibility(View.VISIBLE);
                                            RA_DE_CLNT.setLayoutParams(activo);
                                            RA_CA_TPPR.setVisibility(View.VISIBLE);
                                            RA_CA_TPPR.setLayoutParams(activo);
                                            RA_DE_TPPR.setVisibility(View.VISIBLE);
                                            RA_DE_TPPR.setLayoutParams(activo);
                                            RA_CA_RUC.setVisibility(View.VISIBLE);
                                            RA_CA_RUC.setLayoutParams(activo);
                                            RA_DE_RUC.setVisibility(View.VISIBLE);
                                            RA_DE_RUC.setLayoutParams(activo);
                                            RA_CA_RZSC.setVisibility(View.VISIBLE);
                                            RA_CA_RZSC.setLayoutParams(activo);
                                            RA_DE_RZSC.setVisibility(View.VISIBLE);
                                            RA_DE_RZSC.setLayoutParams(activo);
                                            RA_DE_SERI.setEnabled(false);
                                            RA_DE_CORR.setEnabled(false);
                                            RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                            RA_BTN_BUSC.setLayoutParams(desact);
                                            RA_DE_RUC.setText(CoClie);
                                            RA_DE_RZSC.setText(NoClie);
                                            RA_DE_DOCU.setText(NuDnis);
                                            RA_DE_CLNT.setText(NoPasa);
                                            TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                            RA_DE_SERI.setText(NuSeri);
                                            RA_CA_DOCU.setText("       D.N.I:");
                                            RA_DE_CORR.setText(""+NuCorr);
                                            guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                            guardarDataMemoria("GD_PRISERI", PrSeri);
                                            guardarDataMemoria("GD_NuDocu", NuDocu);
                                            guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                            guardarDataMemoria("GD_CORR", NuDoCa.substring(7,15));
                                            guardarDataMemoria("GD_TPDOC", TIPDOC);
                                            guardarDataMemoria("GD_CoClie", CoClie);
                                            guardarDataMemoria("GD_NoClie", NoClie);
                                            guardarDataMemoria("GD_NuDnis", NuDnis);
                                            guardarDataMemoria("GD_NoPasa", NoPasa);
                                            guardarDataMemoria("GD_CoVehi", CoVehi);
                                            guardarDataMemoria("GD_CoOrig", CoOrig);
                                            guardarDataMemoria("GD_DeOrig", DeOrig);
                                            guardarDataMemoria("GD_CoDest", CoDest);
                                            guardarDataMemoria("GD_DeDest", DeDest);
                                            guardarDataMemoria("GD_FeViaj", FeViaj);
                                            guardarDataMemoria("GD_HoViaj", HoViaj);
                                            guardarDataMemoria("GD_CoRumb", CoRumb);
                                            guardarDataMemoria("GD_NuAsie", NuAsie);
                                            guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                            guardarDataMemoria("GD_TipDTE", "01");
                                            guardarDataMemoria("extra_RazonSocial", NoClie);
                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                        }
                                    }catch (Exception e)
                                    {
                                        Log.d("error",e.getMessage());
                                    }
                                }
                                Log.d("data",response.toString());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void GET_MANU_NUDO(){
        try{

            String DE_SERI = RA_DE_SERI.getText().toString();
            String DE_CORR = RA_DE_CORR.getText().toString();
            String CO_CAJA = sharedPreferences.getString("CodCaja","NoData");
            final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);

            if(CO_CAJA.equals("NoData")){
                Toast.makeText(getActivity(),"ASIGNA UNA CAJA", Toast.LENGTH_SHORT).show();
            }
            else if(DE_SERI.length()!=4){
                Toast.makeText(getActivity(),"INGRESA CORRECTAMENTE LA SERIE", Toast.LENGTH_SHORT).show();
            }
            else if(DE_CORR.length()==0){
                Toast.makeText(getActivity(),"INGRESA EL CORRELATIVO", Toast.LENGTH_SHORT).show();
            }
            else if(DE_SERI.length()==4 && DE_CORR.length()!=0){
                final RequestQueue queue = Volley.newRequestQueue(getContext());
                final String CompletaCeroCorr = completarCorrelativo(Integer.valueOf(DE_CORR));
                final String CorrelativoCompleto = DE_SERI+"-"+CompletaCeroCorr;
                Toast.makeText(getActivity(),"BUSCANDO DOCUMENTO "+CorrelativoCompleto, Toast.LENGTH_SHORT).show();
                final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota3/"+CorrelativoCompleto+"/"+sharedPreferences.getString("CodUsuario", "NoData")+"/"+sharedPreferences.getString("CodCaja", "NoData");
                MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length()>0)
                                {
                                    JSONObject json;
                                    try{
                                        json = response.getJSONObject(0);

                                        final String CoEmpr = json.getString("CO_EMPR");
                                        final String NuDocu = json.getString("NU_DOCU");
                                        final String TIPDOC = json.getString("TIPO_DOCU");
                                        final String CoClie = json.getString("CO_CLIE");
                                        final String NoClie = json.getString("NO_CLIE");
                                        final String NuDnis = json.getString("NU_DNIS");
                                        final String NoPasa = json.getString("NO_PASA");
                                        final String CoVehi = json.getString("BUS");
                                        final String CoOrig = json.getString("CO_DEST_ORIG");
                                        final String DeOrig = json.getString("ORIGEN");
                                        final String CoDest = json.getString("CO_DEST_FINA");
                                        final String DeDest = json.getString("DESTINO");
                                        final String FeViaj = json.getString("FE_VIAJ");
                                        final String HoViaj = json.getString("HO_VIAJ");
                                        final String CoRumb = json.getString("CO_RUMB");
                                        final String NuAsie = json.getString("NU_ASIE");
                                        final String NuDoCa = json.getString("NU_DOCU_CARG");

                                        if(CoEmpr.equals("00")){
                                            Toast.makeText(getActivity(),"NO SE ENCONTRO EL DOCUMENTO, VERFICAR LOS DATOS", Toast.LENGTH_SHORT).show();
                                            CL_DR_AT();
                                        }
                                        else if(!CoEmpr.equals("00") && CorrelativoCompleto.substring(0,1).equals("B")){
//                                            Toast.makeText(getActivity(),"PRIMERA LETRA "+CorrelativoCompleto.substring(0,1), Toast.LENGTH_SHORT).show();
                                            RA_CA_CLNT.setVisibility(View.VISIBLE);
                                            RA_CA_CLNT.setLayoutParams(activo);
                                            RA_DE_CLNT.setVisibility(View.VISIBLE);
                                            RA_DE_CLNT.setLayoutParams(activo);
                                            RA_CA_TPPR.setVisibility(View.VISIBLE);
                                            RA_CA_TPPR.setLayoutParams(activo);
                                            RA_DE_TPPR.setVisibility(View.VISIBLE);
                                            RA_DE_TPPR.setLayoutParams(activo);
                                            RA_CA_RUC.setVisibility(View.INVISIBLE);
                                            RA_CA_RUC.setLayoutParams(desact);
                                            RA_DE_RUC.setVisibility(View.INVISIBLE);
                                            RA_DE_RUC.setLayoutParams(desact);
                                            RA_CA_RZSC.setVisibility(View.INVISIBLE);
                                            RA_CA_RZSC.setLayoutParams(desact);
                                            RA_DE_RZSC.setVisibility(View.INVISIBLE);
                                            RA_DE_RZSC.setLayoutParams(desact);
                                            RA_DE_DOCU.setEnabled(false);
                                            RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                            RA_BTN_BUSC.setLayoutParams(desact);
                                            RA_DE_DOCU.setText(NuDnis);
                                            RA_DE_CLNT.setText(NoPasa);
                                            TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                            guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                            guardarDataMemoria("GD_PRISERI", CorrelativoCompleto.substring(0,1));
                                            guardarDataMemoria("GD_NuDocu", NuDocu);
                                            guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                            guardarDataMemoria("GD_CORR", NuDoCa.substring(5,15));
                                            guardarDataMemoria("GD_TPDOC", TIPDOC);
                                            guardarDataMemoria("GD_CoClie", CoClie);
                                            guardarDataMemoria("GD_NoClie", NoClie);
                                            guardarDataMemoria("GD_NuDnis", NuDnis);
                                            guardarDataMemoria("GD_NoPasa", NoPasa);
                                            guardarDataMemoria("GD_CoVehi", CoVehi);
                                            guardarDataMemoria("GD_CoOrig", CoOrig);
                                            guardarDataMemoria("GD_DeOrig", DeOrig);
                                            guardarDataMemoria("GD_CoDest", CoDest);
                                            guardarDataMemoria("GD_DeDest", DeDest);
                                            guardarDataMemoria("GD_FeViaj", FeViaj);
                                            guardarDataMemoria("GD_HoViaj", HoViaj);
                                            guardarDataMemoria("GD_CoRumb", CoRumb);
                                            guardarDataMemoria("GD_NuAsie", NuAsie);
                                            guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                            guardarDataMemoria("GD_TipDTE", "03");
                                            guardarDataMemoria("extra_RazonSocial", NoClie);
                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                        }
                                        else if(!CoEmpr.equals("00") && CorrelativoCompleto.substring(0,1).equals("F")){
//                                            Toast.makeText(getActivity(),"PRIMERA LETRA "+CorrelativoCompleto.substring(0,1), Toast.LENGTH_SHORT).show();
                                            RA_CA_CLNT.setVisibility(View.VISIBLE);
                                            RA_CA_CLNT.setLayoutParams(activo);
                                            RA_DE_CLNT.setVisibility(View.VISIBLE);
                                            RA_DE_CLNT.setLayoutParams(activo);
                                            RA_CA_TPPR.setVisibility(View.VISIBLE);
                                            RA_CA_TPPR.setLayoutParams(activo);
                                            RA_DE_TPPR.setVisibility(View.VISIBLE);
                                            RA_DE_TPPR.setLayoutParams(activo);
                                            RA_CA_RUC.setVisibility(View.VISIBLE);
                                            RA_CA_RUC.setLayoutParams(activo);
                                            RA_DE_RUC.setVisibility(View.VISIBLE);
                                            RA_DE_RUC.setLayoutParams(activo);
                                            RA_CA_RZSC.setVisibility(View.VISIBLE);
                                            RA_CA_RZSC.setLayoutParams(activo);
                                            RA_DE_RZSC.setVisibility(View.VISIBLE);
                                            RA_DE_RZSC.setLayoutParams(activo);
                                            RA_DE_DOCU.setEnabled(false);
                                            RA_BTN_BUSC.setVisibility(View.INVISIBLE);
                                            RA_BTN_BUSC.setLayoutParams(desact);
                                            RA_DE_RUC.setText(CoClie);
                                            RA_DE_RZSC.setText(NoClie);
                                            RA_DE_DOCU.setText(NuDnis);
                                            RA_DE_CLNT.setText(NoPasa);
                                            TT_NU_DOCU.setText("COMPROBANTE: "+NuDoCa);
                                            guardarDataMemoria("GD_CoEmpr", CoEmpr);
                                            guardarDataMemoria("GD_PRISERI", CorrelativoCompleto.substring(0,1));
                                            guardarDataMemoria("GD_NuDocu", NuDocu);
                                            guardarDataMemoria("GD_SERI", NuDoCa.substring(0,4));
                                            guardarDataMemoria("GD_CORR", NuDoCa.substring(5,15));
                                            guardarDataMemoria("GD_TPDOC", TIPDOC);
                                            guardarDataMemoria("GD_CoClie", CoClie);
                                            guardarDataMemoria("GD_NoClie", NoClie);
                                            guardarDataMemoria("GD_NuDnis", NuDnis);
                                            guardarDataMemoria("GD_NoPasa", NoPasa);
                                            guardarDataMemoria("GD_CoVehi", CoVehi);
                                            guardarDataMemoria("GD_CoOrig", CoOrig);
                                            guardarDataMemoria("GD_DeOrig", DeOrig);
                                            guardarDataMemoria("GD_CoDest", CoDest);
                                            guardarDataMemoria("GD_DeDest", DeDest);
                                            guardarDataMemoria("GD_FeViaj", FeViaj);
                                            guardarDataMemoria("GD_HoViaj", HoViaj);
                                            guardarDataMemoria("GD_CoRumb", CoRumb);
                                            guardarDataMemoria("GD_NuAsie", NuAsie);
                                            guardarDataMemoria("GD_NuDoCa", NuDoCa);
                                            guardarDataMemoria("GD_TipDTE", "01");
                                            guardarDataMemoria("extra_RazonSocial", NoClie);
                                            guardarDataMemoria("extra_documentoCliente", CoClie);
                                        }
                                    }catch (Exception e)
                                    {
                                        Log.d("error",e.getMessage());
                                    }
                                }
                                Log.d("data",response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),"VERIFICA TU CONEXION A INTERNET Y VUELVE A INTENTARLO", Toast.LENGTH_SHORT).show();
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

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"ALGO SALIO MAL, CIERRA SESION Y VUELVE A INTENTARLO", Toast.LENGTH_SHORT).show();
        }
    }

    void CL_DR_AT(){
        TT_NU_DOCU.setText("VENTA DE CARGA");
        final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RA_DE_SERI.setVisibility(View.VISIBLE);
        RA_DE_SERI.setLayoutParams(activo);
        RA_CA_SERI.setVisibility(View.VISIBLE);
        RA_CA_SERI.setLayoutParams(activo);
        RA_DE_CORR.setVisibility(View.VISIBLE);
        RA_DE_CORR.setLayoutParams(activo);
        RA_CA_CORR.setVisibility(View.VISIBLE);
        RA_CA_CORR.setLayoutParams(activo);
        RA_DE_RUC.setVisibility(View.INVISIBLE);
        RA_DE_RUC.setLayoutParams(desact);
        RA_CA_RUC.setVisibility(View.INVISIBLE);
        RA_CA_RUC.setLayoutParams(desact);
        RA_DE_RZSC.setVisibility(View.INVISIBLE);
        RA_DE_RZSC.setLayoutParams(desact);
        RA_CA_RZSC.setVisibility(View.INVISIBLE);
        RA_CA_RZSC.setLayoutParams(desact);
        RA_DE_DOCU.setVisibility(View.VISIBLE);
        RA_DE_DOCU.setLayoutParams(activo);
        RA_CA_DOCU.setVisibility(View.VISIBLE);
        RA_CA_DOCU.setLayoutParams(activo);
        RA_DE_CLNT.setVisibility(View.INVISIBLE);
        RA_DE_CLNT.setLayoutParams(desact);
        RA_CA_CLNT.setVisibility(View.INVISIBLE);
        RA_CA_CLNT.setLayoutParams(desact);
        RD_DE_TPDC.setVisibility(View.INVISIBLE);
        RD_DE_TPDC.setLayoutParams(desact);
        RD_DE_EMPR.setVisibility(View.INVISIBLE);
        RD_DE_EMPR.setLayoutParams(desact);
        RD_CA_TPDC.setVisibility(View.INVISIBLE);
        RD_CA_TPDC.setLayoutParams(desact);
        RD_CO_EMPR.setVisibility(View.INVISIBLE);
        RD_CO_EMPR.setLayoutParams(desact);
        RD_DE_DOCU.setVisibility(View.INVISIBLE);
        RD_DE_DOCU.setLayoutParams(desact);
        RD_CA_DOCU.setVisibility(View.INVISIBLE);
        RD_CA_DOCU.setLayoutParams(desact);
        RD_DE_CLNT.setVisibility(View.INVISIBLE);
        RD_DE_CLNT.setLayoutParams(desact);
        RD_CA_CLNT.setVisibility(View.INVISIBLE);
        RD_CA_CLNT.setLayoutParams(desact);
        RD_DE_RUC.setVisibility(View.INVISIBLE);
        RD_DE_RUC.setLayoutParams(desact);
        RD_CA_RUC.setVisibility(View.INVISIBLE);
        RD_CA_RUC.setLayoutParams(desact);
        RD_DE_RZSC.setVisibility(View.INVISIBLE);
        RD_DE_RZSC.setLayoutParams(desact);
        RD_CA_RZSC.setVisibility(View.INVISIBLE);
        RD_CA_RZSC.setLayoutParams(desact);
        RA_CA_TPPR.setVisibility(View.INVISIBLE);
        RA_CA_TPPR.setLayoutParams(desact);
        RA_DE_TPPR.setVisibility(View.INVISIBLE);
        RA_DE_TPPR.setLayoutParams(desact);
        RA_CA_TPPR2.setVisibility(View.INVISIBLE);
        RA_CA_TPPR2.setLayoutParams(desact);
        RA_DE_TPPR2.setVisibility(View.INVISIBLE);
        RA_DE_TPPR2.setLayoutParams(desact);
        RA_CA_TARI.setVisibility(View.INVISIBLE);
        RA_CA_TARI.setLayoutParams(desact);
        RA_DE_TARI.setVisibility(View.INVISIBLE);
        RA_DE_TARI.setLayoutParams(desact);
        RA_CA_CANT.setVisibility(View.INVISIBLE);
        RA_CA_CANT.setLayoutParams(desact);
        RA_DE_CANT.setVisibility(View.INVISIBLE);
        RA_DE_CANT.setLayoutParams(desact);
        RA_CA_BIMP.setVisibility(View.INVISIBLE);
        RA_CA_BIMP.setLayoutParams(desact);
        RA_DE_BIMP.setVisibility(View.INVISIBLE);
        RA_DE_BIMP.setLayoutParams(desact);
        RA_CA_BIMP2.setVisibility(View.INVISIBLE);
        RA_CA_BIMP2.setLayoutParams(desact);
        RA_DE_BIMP2.setVisibility(View.INVISIBLE);
        RA_DE_BIMP2.setLayoutParams(desact);
        RD_CA_CEPS.setVisibility(View.INVISIBLE);
        RD_CA_CEPS.setLayoutParams(desact);
        RD_DE_CEPS.setVisibility(View.INVISIBLE);
        RD_DE_CEPS.setLayoutParams(desact);
        RA_DE_DOCU.setEnabled(true);
        RA_DE_SERI.setEnabled(true);
        RA_DE_CORR.setEnabled(true);
        RA_DE_SERI.setText("");
        RA_DE_CORR.setText("");
        RA_DE_DOCU.setText("");
        RA_BTN_BUSC.setVisibility(View.VISIBLE);
        RA_BTN_BUSC.setLayoutParams(activo);
        RD_DE_DOCU.setText("");
        RD_DE_CLNT.setText("");
        RA_DE_CLNT.setText("");
        RD_DE_CEPS.setText("");
        RD_DE_RUC.setText("");
        RD_DE_RZSC.setText("");
    }

    void CL_DR_DS(){
        TT_NU_DOCU.setText("VENTA DE CARGA");
        final LinearLayout.LayoutParams desact = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        final LinearLayout.LayoutParams activo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        RA_DE_SERI.setVisibility(View.INVISIBLE);
        RA_DE_SERI.setLayoutParams(desact);
        RA_CA_SERI.setVisibility(View.INVISIBLE);
        RA_CA_SERI.setLayoutParams(desact);
        RA_DE_CORR.setVisibility(View.INVISIBLE);
        RA_DE_CORR.setLayoutParams(desact);
        RA_CA_CORR.setVisibility(View.INVISIBLE);
        RA_CA_CORR.setLayoutParams(desact);
        RA_DE_RUC.setVisibility(View.INVISIBLE);
        RA_DE_RUC.setLayoutParams(desact);
        RA_CA_RUC.setVisibility(View.INVISIBLE);
        RA_CA_RUC.setLayoutParams(desact);
        RA_DE_RZSC.setVisibility(View.INVISIBLE);
        RA_DE_RZSC.setLayoutParams(desact);
        RA_CA_RZSC.setVisibility(View.INVISIBLE);
        RA_CA_RZSC.setLayoutParams(desact);
        RA_DE_DOCU.setVisibility(View.INVISIBLE);
        RA_DE_DOCU.setLayoutParams(desact);
        RA_CA_DOCU.setVisibility(View.INVISIBLE);
        RA_CA_DOCU.setLayoutParams(desact);
        RA_DE_CLNT.setVisibility(View.INVISIBLE);
        RA_DE_CLNT.setLayoutParams(desact);
        RA_CA_CLNT.setVisibility(View.INVISIBLE);
        RA_CA_CLNT.setLayoutParams(desact);
        RD_DE_TPDC.setVisibility(View.VISIBLE);
        RD_DE_TPDC.setLayoutParams(activo);
        RD_DE_EMPR.setVisibility(View.VISIBLE);
        RD_DE_EMPR.setLayoutParams(activo);
        RD_CA_TPDC.setVisibility(View.VISIBLE);
        RD_CA_TPDC.setLayoutParams(activo);
        RD_CO_EMPR.setVisibility(View.VISIBLE);
        RD_CO_EMPR.setLayoutParams(activo);
        RD_DE_DOCU.setVisibility(View.INVISIBLE);
        RD_DE_DOCU.setLayoutParams(desact);
        RD_CA_DOCU.setVisibility(View.INVISIBLE);
        RD_CA_DOCU.setLayoutParams(desact);
        RD_DE_CLNT.setVisibility(View.INVISIBLE);
        RD_DE_CLNT.setLayoutParams(desact);
        RD_CA_CLNT.setVisibility(View.INVISIBLE);
        RD_CA_CLNT.setLayoutParams(desact);
        RD_DE_RUC.setVisibility(View.INVISIBLE);
        RD_DE_RUC.setLayoutParams(desact);
        RD_CA_RUC.setVisibility(View.INVISIBLE);
        RD_CA_RUC.setLayoutParams(desact);
        RD_DE_RZSC.setVisibility(View.INVISIBLE);
        RD_DE_RZSC.setLayoutParams(desact);
        RD_CA_RZSC.setVisibility(View.INVISIBLE);
        RD_CA_RZSC.setLayoutParams(desact);
        RD_CA_CEPS.setVisibility(View.INVISIBLE);
        RD_CA_CEPS.setLayoutParams(desact);
        RD_DE_CEPS.setVisibility(View.INVISIBLE);
        RD_DE_CEPS.setLayoutParams(desact);
        RA_CA_TPPR.setVisibility(View.INVISIBLE);
        RA_CA_TPPR.setLayoutParams(desact);
        RA_DE_TPPR.setVisibility(View.INVISIBLE);
        RA_DE_TPPR.setLayoutParams(desact);
        RA_CA_TPPR2.setVisibility(View.INVISIBLE);
        RA_CA_TPPR2.setLayoutParams(desact);
        RA_DE_TPPR2.setVisibility(View.INVISIBLE);
        RA_DE_TPPR2.setLayoutParams(desact);
        RA_CA_TARI.setVisibility(View.INVISIBLE);
        RA_CA_TARI.setLayoutParams(desact);
        RA_DE_TARI.setVisibility(View.INVISIBLE);
        RA_DE_TARI.setLayoutParams(desact);
        RA_CA_CANT.setVisibility(View.INVISIBLE);
        RA_CA_CANT.setLayoutParams(desact);
        RA_DE_CANT.setVisibility(View.INVISIBLE);
        RA_DE_CANT.setLayoutParams(desact);
        RA_CA_BIMP.setVisibility(View.INVISIBLE);
        RA_CA_BIMP.setLayoutParams(desact);
        RA_DE_BIMP.setVisibility(View.INVISIBLE);
        RA_DE_BIMP.setLayoutParams(desact);
        RA_CA_BIMP2.setVisibility(View.INVISIBLE);
        RA_CA_BIMP2.setLayoutParams(desact);
        RA_DE_BIMP2.setVisibility(View.INVISIBLE);
        RA_DE_BIMP2.setLayoutParams(desact);
//        RD_DE_DOCU.setText("");
        RD_DE_CEPS.setText("");
        RD_DE_RUC.setText("");
        RD_DE_RZSC.setText("");
        RA_DETA_DOCU.setText("   DOCUMENTO:");
        RA_DE_CLNT.setText("");
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

    public void guardarDataMemoria(String key, String value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public String generarTramaCarga(SharedPreferences sharedPreferences, String numSerieCarga, String numCorrelativoCargaCompleto, String empresaTrama) {

        String SERIE = sharedPreferences.getString("GD_SERI", "NoData");
        String CORRE = sharedPreferences.getString("GD_CORR", "NoData");

        String[] empresaSeleccionada = empresaTrama.split("-");

        numCorrelativoCargaCompleto = numCorrelativoCargaCompleto.substring(2);

        String tipoDocumento = "";
        String normativaSunat = "";

        String priSeri = sharedPreferences.getString("GD_PRISERI", "NoData");
        if(priSeri.equals("F")){
            normativaSunat = "A;FormaPago;;Contado\n";
        }else if(priSeri.equals("B")){
            normativaSunat = "";
        }

        String priSeri0 = sharedPreferences.getString("GD_PRISERI", "NoData");
        if(priSeri0.equals("F")){
            tipoDocumento = "01";
        }else if(priSeri0.equals("B")){
            tipoDocumento = "03";
        }

        String tipoDocumentoCliente = "";

        String priSeri2 = sharedPreferences.getString("GD_PRISERI", "NoData");
        if(priSeri2.equals("F")){
            tipoDocumentoCliente = "6";
        }else if(priSeri2.equals("B")){
            tipoDocumentoCliente = "1";
        }

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
        if(sharedPreferences.getString("GD_PRISERI","NoData").equals("F")) {
            nombreCliente = sharedPreferences.getString("extra_RazonSocial","NoData");
            DocuDeclara = sharedPreferences.getString("extra_documentoCliente","NoData");
        }else{
            nombreCliente = sharedPreferences.getString("extra_RazonSocial", "NoData");
            DocuDeclara = sharedPreferences.getString("extra_documentoCliente","NoData");
        }

//        String direccionCliente = "";
//        if (sharedPreferences.getString("guardar_direccionCliente", "NoData").equals("NoData")) {
//            direccionCliente = "-";
//        } else {
//            direccionCliente = sharedPreferences.getString("guardar_direccionCliente", "NoData");
//        }

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
                "A;Serie;;" + SERIE + "\n" +
                        "A;Correlativo;;" + CORRE + "\n" +
                        "A;RznSocEmis;;EMPRESA DE TRANSPORTES PERU BUS S.A\n" +
                        "A;CODI_EMPR;;1\n" +
                        "A;RUTEmis;;20106076635\n" +
                        "A;DirEmis;;AV. MEXICO NRO 333 LA VICTORIA - LIMA - LIMA\n" +
                        "A;ComuEmis;;150115\n" +
                        "A;CodigoLocalAnexo;;0011\n" +
                        "A;NomComer;;E.T. PERU BUS S.A\n" +
                        "A;TipoDTE;;" + tipoDocumento + "\n" +
                        "A;TipoOperacion;;0101\n" +
                        "A;TipoRutReceptor;;"+sharedPreferences.getString("GD_TPDOC", "NoData")+"\n" +
                        "A;RUTRecep;;" + DocuDeclara + "\n" +
                        "A;RznSocRecep;;" + nombreCliente + "\n" +
                        "A;DirRecep;;-\n" +
                        "A;TipoMoneda;;PEN\n" +
                        "A;MntNeto;;"+String.format("%.2f", montoTotal-montoIGV)+"\n" +
                        "A;MntExe;;0.00\n" +
                        "A;MntExo;;0.00\n" +
                        "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                        "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n"+
                        "A;FchEmis;;" + fechaVenta + "\n" +
                        "A;HoraEmision;;" + horaVenta + "\n" +normativaSunat +
//                "A;TipoRucEmis;;6\n"+
//                "A;UrbanizaEmis;;LIMA\n" +
//                "A;ProviEmis;;LIMA\n" +
//                //"A;RUTRecep;;" + documentoCliente + "\n" +
//                "A;CodigoAutorizacion;;000000"+"\n"+
//                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                        "A2;CodigoImpuesto;1;1000\n" +
                        "A2;MontoImpuesto;1;"+String.format("%.2f", montoIGV)+"\n" +
                        "A2;TasaImpuesto;1;18\n"+
                        "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoTotal)+"\n"+
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
                        "E;DescripcionAdicSunat;3;"+sharedPreferences.getString("nombreEmpleado", "NoData")+"\n" +
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
                        "E;DescripcionAdicSunat;7;"+sharedPreferences.getString("GD_NoClie", "NoData")+"\n"+
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
                        "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("GD_DeOrig","NoData").toString().trim() +"\n"+
                        "E;TipoAdicSunat;14;01\n" +
                        "E;NmrLineasDetalle;14;14\n" +
                        "E;NmrLineasAdicSunat;14;14\n" +
                        "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("GD_DeDest","NoData").toString().trim() +"\n"+
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
                        "E;DescripcionAdicSunat;18;"+SERIE+"/F-VTS-42\n"+
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
                        "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("GD_DeOrig","NoData").toString().trim()+" - "+ sharedPreferences.getString("GD_DeDest","NoData").toString().trim()+"\n"+
                        "E;TipoAdicSunat;35;02\n"+
                        "E;NmrLineasDetalle;35;1\n"+
                        "E;NmrLineasAdicSunat;35;13\n"+
                        "E;DescripcionAdicSunat;35;-\n";
        return tramaCarga;
    }

    public String[] generarCodigoQR(String trama) {
//        ProgressDialog progressDialog;
//        progressDialog = ProgressDialog.show(getActivity(),
//                "Imprimiendo Boleto",
//                "Espere...");

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

//        progressDialog.dismiss();

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
        cv.put("nu_docu",sharedPreferences.getString("GD_NuDoCa", "NoData"));
        cv.put("ti_docu",sharedPreferences.getString("GD_TIDOCU", "NoData"));
        cv.put("co_empr",sharedPreferences.getString("GD_CoEmpr", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

//        cv.put("data_boleto", jsonObject.toString());
//        cv.put("estado", "pendiente");
//        cv.put("tipo", "carga");
//        cv.put("liberado", "No");
//        cv.put("nu_docu",sharedPreferences.getString("NuDocuCarg_impr", "NoData"));
//        cv.put("ti_docu",sharedPreferences.getString("TiDocuCarg_Tram", "NoData"));
//        cv.put("co_empr","01");
//        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
            cv.put("puesto", "boletero");
            sharedPreferences.getString("extra_empresa", "NoData");
        }else{
            cv.put("puesto", "anfitrion");
            //cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            sharedPreferences.getString("extra_empresa", "NoData");
        }

//        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);


        //Log.d("json object", jsonObject.toString());
        /* ----------------------------------------- */

        /* Ruta de la Web service */
        String ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga2";
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
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("GD_CoEmpr", "01"));
            if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")){
                jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad", "NoData"));
                jsonObject.put("Agencia", sharedPreferences.getString("guardar_agencia", "NoData"));

            }else if (sharedPreferences.getString("Modulo", "NoData").equals("CONDUCTOR ESTANDAR")){
                jsonObject.put("Unidad", "");
                jsonObject.put("Agencia", "");
            }

            jsonObject.put("TipoDocuCarga", sharedPreferences.getString("TiDocuCarg_Tram", "NoData"));
            jsonObject.put("SerieCorrelativo", sharedPreferences.getString("NuDocuCarg_impr", "NoData"));
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("Rumbo", sharedPreferences.getString("CoRumbCarg_impr", ""));
            jsonObject.put("Origen", sharedPreferences.getString("CoOrigViaj_imp1", ""));
            jsonObject.put("Destino", sharedPreferences.getString("CoDestViaj_imp1", ""));
            jsonObject.put("NuSecu", sharedPreferences.getString("extra_secuencia", "0"));
            jsonObject.put("NumeroDia", diaSemana);
            jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("NuDnisCarg_impr", "NoData"));
            jsonObject.put("RUC", sharedPreferences.getString("GD_CoClie", "NoData"));
            jsonObject.put("NombreCliente", sharedPreferences.getString("GD_NoClie", "NoData"));
            jsonObject.put("TipoServicio", sharedPreferences.getString("extra_servicio", ""));
            jsonObject.put("NumeroAsiento", sharedPreferences.getString("GD_NuAsie", "NoData"));
            jsonObject.put("FechaViajeItinerario", sharedPreferences.getString("GD_FeViaj", "NoData"));
            jsonObject.put("HoraViaje", sharedPreferences.getString("GD_HoViaj", "NoData"));
            jsonObject.put("ImporteTotal", sharedPreferences.getString("ImTotaCarg_impr", "NoData"));
            jsonObject.put("Observacion",sharedPreferences.getString("CaProdCarg_impr", "NoData")+" "+sharedPreferences.getString("DeProdCarg_impr", "NoData"));
            jsonObject.put("CodigoUsuario", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("NuDocuBoletoViaje", sharedPreferences.getString("GD_NuDoCa", "NoData"));
            jsonObject.put("TipoDocumentoBoletoViaje", "BLT");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
            jsonObject.put("Correlativo", sharedPreferences.getString("GD_CORR", "NoData"));
            jsonObject.put("Producto", sharedPreferences.getString("IdProdCarg_impr", "NoData"));
            jsonObject.put("Cantidad", "1");
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

    private class VentaAsyncrona extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;
        @Override

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    "Imprimiendo Boleto",
                    "Espere...");
        }
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }


        @Override
        protected void onProgressUpdate(String... text) {
            //inalResult.setText(text[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            publishProgress("Sleeping...");
            try{
                final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                /* ----------------------------------------- */

                String GD_SERI = sharedPreferences.getString("GD_SERI", "NoData");
                String GD_CORR = sharedPreferences.getString("GD_CORR", "NoData");
                /* Se obtiene la empresa que genera la venta */
                for (int i = 0; i < lista_empresas.size(); i++) {

                    String[] data = lista_empresas.get(i).split("-");
                    // data[0] = CODIGO_EMPRESA
                    // data[1] = EMPRESA
                    // data[2] = DIRECCION
                    // data[3] = DEPARTAMENTO
                    // data[4] = PROVINCIA
                    // data[5] = RUC

                    String codigo_empresa = "01";

                    if (codigo_empresa.equals(data[0])) {
                        empresaTramaCarga = lista_empresas.get(i);
                        empresaSeleccionada = lista_empresas.get(i);
                    }
                }
                float tarifaTotal = Integer.valueOf(RA_DE_TARI.getText().toString());
                editor.putString("guardar_tarifaTotal", Float.toString(tarifaTotal));
                editor.commit();
                /* ----------------------------------------- */

                editor.putString("guardar_cantidad", RA_DE_CANT.getText().toString());
                editor.commit();

                final String trama = generarTramaCarga(sharedPreferences,GD_SERI, GD_CORR, empresaTramaCarga);
                String cabcomp = sharedPreferences.getString("cabcomp", "NoData");
                String info0 = sharedPreferences.getString("info0", "NoData");
                String cliente = sharedPreferences.getString("cliente", "NoData");
                String info1 = sharedPreferences.getString("info1", "NoData");
                String info2 = sharedPreferences.getString("info2", "NoData");
                String info3 = sharedPreferences.getString("info3", "NoData");
                String TedQR = sharedPreferences.getString("TedQR", "NoData");
               //Log.d("trama", trama);
                String[] dataEncriptada = generarCodigoQR(trama);
                Boolean[] respuesta = guardarCompraCarga(dataEncriptada[0], dataEncriptada[1]);

                Imprcab1(mPrinter,cabcomp,info0,cliente);
                Imprinfo(mPrinter,info1,info2,info3);
                ImprQR(TedQR);
                ImprPie(mPrinter);
                Imprcab2(mPrinter,cabcomp,info0,cliente);
                Imprinfo(mPrinter,info1,info2,info3);
                ImprQR(TedQR);
                ImprPie(mPrinter);
                venta_carga_enco2 ventacargaenco = new venta_carga_enco2();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, ventacargaenco).commit();
            }catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

    }


}