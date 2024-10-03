package pe.com.telefonica.soyuz;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.util.Printer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;


public class CriterioUsuario  extends AppCompatActivity  implements RecyclercriterioAdapter.ItemClickListener{
    RecyclercriterioAdapter adapter;
    ArrayList<ModuloSistema> ListaModulos = new ArrayList<ModuloSistema>();
    SharedPreferences sharedpreferences;
    ProgressDialog ProgresBar;
    RequestQueue queue;
    final Gson gson = new Gson();
    SharedPreferences.Editor editor;
    String codigoUsuario;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    private IDAL dal;
    private IPrinter printer;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.reporte_contralor_trafico);
        queue = Volley.newRequestQueue(this);
        final TextView Titulo = findViewById(R.id.text_Titulo);
        final Button btnSalir = findViewById(R.id.button_imprimir_controlador);
        sharedpreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        btnSalir.setText("SALIR DEL SISTEMA");
        Titulo.setText("SELECCIONAR MODULO");
        CargaModulo();
        String Prueba  = sharedpreferences.getString("MenuSoyuz","NoData");
        Log.d("prueba",Prueba);
        codigoUsuario =  sharedpreferences.getString("CodUsuario","NoData");
  //      final SharedPreferences.Editor editor = sharedpreferences.edit();
        try {
            dal = NeptuneLiteUser.getInstance().getDal(getApplicationContext());
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
        } catch (Exception e) {
            //Log.e("IMPRESORA", "No se puede inicializar la impresora");
        }


        RecyclerView recyclerView = findViewById(R.id.rvAsignacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new RecyclercriterioAdapter(getApplicationContext(), ListaModulos);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FuncionesAuxiliares.logout(getApplicationContext());
                Intent intent = new Intent(CriterioUsuario.this,LoginActivity.class);
                CriterioUsuario.this.finish();
                startActivity(intent);
            }
        });
    }
    @Override
    public void onItemClick(View view,final int position) {
        ProgresBar = new ProgressDialog(CriterioUsuario.this);
        ProgresBar.setTitle("Espere por favor");
        ProgresBar.setMessage("Iniciando Modulo");
        ProgresBar.setCancelable(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SeleccionaModulo(adapter.getTextList(position));
            }
        });
        thread.start();
        ProgresBar.show();

    }
    void CargaModulo()
    {
        ArrayList<String> ListaMenu = getArray(sharedpreferences, gson, "MenuSoyuz");
        //Log.d("lista", ListaMenu.toString());
        for(int i =  0 ; i< ListaMenu.size();i++)
        {
            String arr[] = ListaMenu.get(i).split("ƒ");
            ModuloSistema Mod = new ModuloSistema(arr[0],arr[1]);
            ListaModulos.add(Mod);
        }

    }

    void SeleccionaModulo(String CO_MENU)
    {
        guardarDataMemoria("Modulo",CO_MENU,getApplicationContext());
        if(CO_MENU.equals("ANDROID_CONTROL"))
        {
            ModuloControlador();
        }
        else if(CO_MENU.equals("ANDROID_INSP")){
            //ModuloInspeccion();
            ModuloControlador();
        }else if(CO_MENU.equals("ANDROID_VENTAS")){
            ModuloVentas();
        }
    }
    void CambioPantalla()
    {
        Intent intent = new Intent(CriterioUsuario.this,AppSideBarActivity.class);
        CriterioUsuario.this.finish();
        startActivity(intent);
    }
    void ModuloVentas()
    {
        GetEmpresa();
        breakTime();
        GetDestinos();
        breakTime();
        GetProductosCarga();
        breakTime();
        AndroidVentas();
    }
    void ModuloInspeccion()
    {


    }
    void ModuloControlador()
    {
        GetEmpresa();
        GetDestinos();
        GetItinerario("ANDROID_CONTROL",sharedpreferences);
        GeneraToken(sharedpreferences);
    }
    void GetEmpresa()
    {
        String ws_getEmpresas = getString(R.string.ws_ruta) + "GetEmpresa";
        Log.d("EmpresaWS",ws_getEmpresas);
        JsonArrayRequest jsonArrayRequestEmpresas = new JsonArrayRequest(Request.Method.GET, ws_getEmpresas, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0){
                            Toast.makeText(getApplicationContext(), "No se recibió datos de la webservice getEmpresa.", Toast.LENGTH_LONG).show();
                            errorView();
                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_empresas = new ArrayList<String>();
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    lista_empresas.add(
                                            info.getString("CODIGO_EMPRESA") + "-" +
                                                    info.getString("EMPRESA") + "-" +
                                                    info.getString("DIRECCION") + "-" +
                                                    info.getString("DEPARTAMENTO") + "-" +
                                                    info.getString("PROVINCIA") + "-" +
                                                    info.getString("RUC") + "-" +
                                                    info.getString("RAZON_SOCIAL")
                                    );
                                }
                                String json_empresas = gson.toJson(lista_empresas);
                                guardarDataMemoria("json_empresas", json_empresas, getApplicationContext());
                                //getEmpresasDone=true;
                            } catch (JSONException e) {

                                Toast.makeText(getApplicationContext(), "Se recibió datos erróneos de la webservice getEmpresa.", Toast.LENGTH_LONG).show();
                                //Log.d("error", "format response");
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getEmpresa.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
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
        jsonArrayRequestEmpresas.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestEmpresas);

    }
    void GetDestinos()
    {
        String ws_getDestinos = getString(R.string.ws_ruta) + "GetDestino";
        Log.d("Destino",ws_getDestinos);
        JsonArrayRequest jsonArrayRequestDestinos = new JsonArrayRequest(Request.Method.GET, ws_getDestinos, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0){
                            Toast.makeText(getApplicationContext(), "No se recibió datos de la webservice getDestino.", Toast.LENGTH_LONG).show();
                            errorView();
                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_destinos = new ArrayList<>();
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    lista_destinos.add(info.getString("CO_DEST") + "-" +
                                            info.getString("DE_DEST") + "-" +
                                            info.getString("ORDEN")
                                    );
                                }
                                String json_destinos = gson.toJson(lista_destinos);
                                guardarDataMemoria("json_destinos", json_destinos, getApplicationContext());
                                //getDestinosDone=true;


                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Se recibió datos erróneos de la webservice getDestino.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getDestino.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
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
        jsonArrayRequestDestinos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestDestinos);
        //breakTime();
    }
    void GetProductosCarga()
    {
        String ws_getProductos = getString(R.string.ws_ruta) + "ProductosCarga";
        JsonArrayRequest jsonArrayRequestProductos = new JsonArrayRequest(Request.Method.GET, ws_getProductos, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0){
                            Toast.makeText(getApplicationContext(), "No se recibió datos de la webservice getProductosCarga.", Toast.LENGTH_LONG).show();
                            errorView();

                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_productos = new ArrayList<>();
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    lista_productos.add(info.getString("TI_PROD") + "-" +
                                            info.getString("DE_TIPO_PROD")
                                    );
                                }
                                String json_productos = gson.toJson(lista_productos);
                                guardarDataMemoria("json_productos", json_productos, getApplicationContext());
                                //getTipoProductoDone=true;
                                //cargarDatosSegunRolUsuario();

                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Se recibió datos erróneos de la webservice getProductosCarga.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getProductosCarga.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
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
        jsonArrayRequestProductos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestProductos);
        breakTime();
    }
    void AndroidVentas()
    {
        String ws_getCorrelativo = getString(R.string.ws_ruta) + "GetUltimoCorrelativo/~/~/~/" + codigoUsuario;
        Log.d("empresa", ws_getCorrelativo);
        JsonArrayRequest jsonArrayRequestCorrelativo = new JsonArrayRequest(Request.Method.GET, ws_getCorrelativo, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "El rol boletero no tiene ningún correlativo asignado.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() > 0) {
                            try {

                                JSONObject info;
                                ArrayList<String> lista_agencias = new ArrayList<>();

                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    final String TRAMA_Agencias = info.getString("CO_RUMB")+"ƒ"+info.getString("CO_AGEN")+"ƒ"+info.getString("CO_UNID")+"ƒ"+info.getString("CO_DEST");
                                    if (!lista_agencias.contains(TRAMA_Agencias)){
                                        lista_agencias.add(TRAMA_Agencias);
                                    }
                                    /*if (lista_agencias.contains(info.getString("CO_AGEN"))){
                                        lista_agencias.add(info.getString("CO_AGEN"));
                                    }*/
                                }
                                if (lista_agencias.size() > 1){
                                    Toast.makeText(getApplicationContext(), "Se detectó más de una agencia asignada a su usuario. Verificar con Administración", Toast.LENGTH_LONG).show();
                                    errorView();
                                } else{
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            if (response.getJSONObject(i).getString("EMPRESA").equals("01") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("PASAJES RUTA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("BLT")){

                                                guardarDataMemoria("guardar_serieViajeBLT01", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoViajeBLT01", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("01") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("PASAJES RUTA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("FAC")){
                                                guardarDataMemoria("guardar_serieViajeFAC01", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoViajeFAC01", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("01") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("CARGA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("BOL")){
                                                guardarDataMemoria("guardar_serieCargaBOL01", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoCargaBOL01", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("01") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("CARGA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("FAC")){
                                                guardarDataMemoria("guardar_serieCargaFAC01", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoCargaFAC01", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("02") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("PASAJES RUTA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("BLT")){
                                                guardarDataMemoria("guardar_serieViajeBLT02", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoViajeBLT02", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("02") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("PASAJES RUTA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("FAC")){
                                                guardarDataMemoria("guardar_serieViajeFAC02", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoViajeFAC02", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("02") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("CARGA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("BOL")){
                                                guardarDataMemoria("guardar_serieCargaBOL02", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoCargaBOL02", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            } else if (response.getJSONObject(i).getString("EMPRESA").equals("02") &&
                                                    response.getJSONObject(i).getString("DE_GSER").equals("CARGA") &&
                                                    response.getJSONObject(i).getString("TI_DOCU").equals("FAC")){
                                                guardarDataMemoria("guardar_serieCargaFAC02", response.getJSONObject(i).getString("NUMERO_SERIE"), getApplicationContext());
                                                guardarDataMemoria("guardar_correlativoCargaFAC02", response.getJSONObject(i).getString("ULTIMO_CORRELATIVO"), getApplicationContext());
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    guardarDataMemoria("bol_getCorrelativo", response.toString(), getApplicationContext());
                                    String[] arr = lista_agencias.get(0).split("ƒ");
                                    guardarDataMemoria("Dest_fina_se",arr[3],getApplicationContext());
                                    guardarDataMemoria("CO_RUMB_SE_GET",arr[0],getApplicationContext());
                                    Log.d("dest",sharedpreferences.getString("Dest_fina_se","NoData"));
                                    TDPROG_ESAG_GET("01",arr[2],arr[1],arr[0],arr[3]);
                                    breakTime();
                                    String ws_getServicioBoletero01 = getString(R.string.ws_ruta) + "ServicioBoletero/01/"+
                                            response.getJSONObject(0).getString("CO_UNID")+"/"+
                                            response.getJSONObject(0).getString("CO_AGEN");
                                    Log.d("empresa", ws_getServicioBoletero01);
                                    JsonArrayRequest jsonArrayRequestServicio01 = new JsonArrayRequest(Request.Method.GET, ws_getServicioBoletero01, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() == 0) {
                                                        Toast.makeText(getApplicationContext(), "El rol boletero no tiene servicios asignados para la empresa 01.", Toast.LENGTH_LONG).show();
                                                        errorView();
                                                    } else if (response.length() > 0) {
                                                        guardarDataMemoria("bol_getServicio01", response.toString(), getApplicationContext());
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getServicioEmpresa01.", Toast.LENGTH_LONG).show();
                                            errorWS(editor, queue, error);
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
                                    queue.add(jsonArrayRequestServicio01);
                                    breakTime();
                                    breakTime();
                                    String ws_getServicioBoletero02 = getString(R.string.ws_ruta) + "ServicioBoletero/02/"+
                                            response.getJSONObject(0).getString("CO_UNID")+"/"+
                                            response.getJSONObject(0).getString("CO_AGEN");
                                    JsonArrayRequest jsonArrayRequestServicio02 = new JsonArrayRequest(Request.Method.GET, ws_getServicioBoletero02, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() == 0) {
                                                        Toast.makeText(getApplicationContext(), "El rol boletero no tiene servicios asignados para la empresa 02.", Toast.LENGTH_LONG).show();
                                                        errorView();
                                                    } else if (response.length() > 0) {
                                                        guardarDataMemoria("bol_getServicio02", response.toString(), getApplicationContext());
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getServicioEmpresa02.", Toast.LENGTH_LONG).show();
                                            errorWS(editor, queue, error);
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
                                    queue.add(jsonArrayRequestServicio02);
                                    breakTime();
                                    String ws_getTarifa01 = getString(R.string.ws_ruta) + "TarifaAgencia/01/"+
                                            response.getJSONObject(0).getString("CO_UNID")+"/"+
                                            response.getJSONObject(0).getString("CO_AGEN");
                                    Log.d("tarifa", ws_getTarifa01);
                                    JsonArrayRequest jsonArrayRequestTarifa01 = new JsonArrayRequest(Request.Method.GET, ws_getTarifa01, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() == 0) {
                                                        Toast.makeText(getApplicationContext(), "El rol boletero no tiene tarifas asignadas para la empresa 01.", Toast.LENGTH_LONG).show();
                                                        errorView();
                                                    }else if (response.length() > 0) {
                                                        guardarDataMemoria("bol_getTarifa01", response.toString(), getApplicationContext());
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getTarifaEmpresa01.", Toast.LENGTH_LONG).show();
                                            errorWS(editor, queue, error);
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
                                    queue.add(jsonArrayRequestTarifa01);
                                    breakTime();

                                    /* ============================================= GET TARIFA EMPRESA 02 ==========================================================================*/

                                    /* Ruta de la Web service */
                                    String ws_getTarifa02 = getString(R.string.ws_ruta) + "TarifaAgencia/02/"+response.getJSONObject(0).getString("CO_UNID")
                                            +"/"+response.getJSONObject(0).getString("CO_AGEN");
                                    Log.d("tarifa", ws_getTarifa02);
                                    /* ----------------------------------------- */
                                    /* Request que obtiene los servicios de las empresas */
                                    JsonArrayRequest jsonArrayRequestTarifa02 = new JsonArrayRequest(Request.Method.GET, ws_getTarifa02, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() == 0) {
                                                        /* En caso el usuario no tenga tarifas asignadas no puede iniciar sesión */
                                                        Toast.makeText(getApplicationContext(), "El rol boletero no tiene tarifas asignadas para la empresa 02.", Toast.LENGTH_LONG).show();
                                                        errorView();
                                                        /* ----------------------------------------- */

                                                    }else if (response.length() > 0) {

                                                        /* Se guarda el JSON de los servicios */
                                                        guardarDataMemoria("bol_getTarifa02", response.toString(), getApplicationContext());
                                                        /* ----------------------------------------- */

                                                        //getTarifaDone02 = true;
                                                        //Log.d("tarifa", "true");
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getTarifaEmpresa02.", Toast.LENGTH_LONG).show();
                                            errorWS(editor, queue, error);
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
                                    queue.add(jsonArrayRequestTarifa02);
                                    breakTime();

                                    //getCorreltativoDone = true;
                                    //Log.d("correlativo", "true");
                                    breakTime();
                                    //ObtenerUltimaVenta(codigoUsuario,puestoUsuario,"~");
                                    final String ModuloVentas = sharedpreferences.getString("Modulo","NoData");
                                    ObtenerUltimaVenta(codigoUsuario,ModuloVentas,"~");
                                    breakTime();
                                    breakTime();
                                    breakTime();
                                    breakTime();
                                    /* ============================================= GET TRAMOS ==========================================================================*/

                                    /* Ruta de la Web service */
                                    String ws_getTramos = getString(R.string.ws_ruta) + "TTRAM_BOL";
                                    //Log.d("tramo", ws_getTramos);
                                    /* ----------------------------------------- */

                                    /* Request que obtiene las series y correlativos de viaje y carga */
                                    JsonArrayRequest jsonArrayRequestTramos = new JsonArrayRequest(Request.Method.GET, ws_getTramos, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length() == 0) {
                                                        /* En caso el usuario no tenga tramos asignados no puede iniciar sesión */
                                                        Toast.makeText(getApplicationContext(), "El rol boletero no tiene tramos asignados.", Toast.LENGTH_LONG).show();
                                                        errorView();
                                                        /* ----------------------------------------- */

                                                    }else if (response.length() > 0) {

                                                        /* Se guarda el JSON de los servicios */
                                                        guardarDataMemoria("bol_getTramos", response.toString(), getApplicationContext());
                                                        //Log.d("tramo", response.toString());
                                                        /* ----------------------------------------- */

                                                        //getTramosDone = true;
                                                        //Log.d("tramos", "true");
                                                        breakTime();
                                                        GeneraToken(sharedpreferences);
                                                        try {
                                                            Thread.sleep(5000);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        ImpresionPrueba(printer);
                                                        ProgresBar.dismiss();
                                                        Intent intent = new Intent(CriterioUsuario.this, AppSideBarActivity.class);
                                                        CriterioUsuario.this.finish();
                                                        startActivity(intent);
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Error en la ws getTramos.", Toast.LENGTH_LONG).show();
                                            errorWS(editor, queue, error);
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
                                    queue.add(jsonArrayRequestTramos);
                                    breakTime();
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
                Toast.makeText(getApplicationContext(), "Error en la ws getCorrelativos.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
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
        queue.add(jsonArrayRequestCorrelativo);
        breakTime();

    }
    private void ObtenerUltimaVenta(String  pcodigoUsuario,String ppuestoUsuario,String co_empr) {
        String wsUltimaVenta = getString(R.string.ws_ruta) + "UltimaVenta/" + pcodigoUsuario + "/" + ppuestoUsuario+"/"+co_empr;
        Log.d("ultimaVenta",wsUltimaVenta);
        JsonArrayRequest RequestUltimaVenta = new JsonArrayRequest(Request.Method.GET, wsUltimaVenta, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>0) {
                            JSONObject json;
                            ventaBlt = new DatabaseBoletos(getApplicationContext());
                            sqLiteDatabase = ventaBlt.getWritableDatabase();
                            sqLiteDatabase.execSQL("DELETE FROM UltimaVenta");
                            try   {
                                for (int i = 0; i < response.length(); i++) {
                                    json = response.getJSONObject(i);
                                    ContentValues sqlQuery = new ContentValues();
                                    sqlQuery.put("CO_EMPR",json.getString("CO_EMPR"));
                                    sqlQuery.put("TI_DOCU",json.getString("TI_DOCU"));
                                    sqlQuery.put("NU_DOCU",json.getString("NU_DOCU"));
                                    //sqLiteDatabase = ventaBlt.getWritableDatabase();
                                    sqLiteDatabase.insert("UltimaVenta",null,sqlQuery);
                                    //Log.d("Res", response.toString());
                                    //Log.d("se ingreso", "se ingreso la informacion");
                                    //ventaDone = true;
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);

                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        RequestUltimaVenta.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(RequestUltimaVenta);
    }
    public void GeneraToken(final SharedPreferences sharedPreferences)
    {
        JSONObject JsonToken = new JSONObject();
        JsonToken = JsonToken(sharedPreferences);
        String ws_GeneraToken = getString(R.string.ws_ruta) + "GeneraToken";
        MyJSONArrayRequest jsonArrayCierreSession = new MyJSONArrayRequest(Request.Method.POST, ws_GeneraToken, JsonToken,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            JSONObject info;
                            try {
                                info = response.getJSONObject(0);
                                 final String KeySession = info.getString("TOKEN");
                                guardarDataMemoria("KEY", KeySession, getApplicationContext());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Genera Token incorrecto", Toast.LENGTH_LONG).show();
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
        jsonArrayCierreSession.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayCierreSession);
        breakTime();
    }
    public JSONObject JsonToken(final SharedPreferences sharedPreferences){

        JSONObject TokenJson = new JSONObject();
        try {
            TokenJson.put("Usuario", codigoUsuario);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TokenJson;
    }

    public void errorView() {
        ProgresBar.dismiss();
        Intent intent = new Intent(CriterioUsuario.this, ErrorActivity.class);
        CriterioUsuario.this.finish();
        startActivity(intent);
    }
    private void errorWS(SharedPreferences.Editor editor, RequestQueue queue, VolleyError error) {

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
        ProgresBar.dismiss();
        errorView();
    }
    private void GetItinerario(String Modulo,final SharedPreferences sharedPreferences)
    {
        String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/InfinityDev/Puesto/" + Modulo;
        Log.d("getItinerario",ws_getItinerario);
        JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "El usuario no tiene ningún itinerario asignado.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() == 1) {
                            Toast.makeText(getApplicationContext(), "El usuario solo tiene un itinerario asignado.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() > 1) {
                            JSONObject info;
                            ArrayList<String> lista_itinerario = new ArrayList<>();
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
                                GeneraToken(sharedpreferences);
                                String jsonItinerario = gson.toJson(lista_itinerario);
                                guardarDataMemoria("jsonItinerario", jsonItinerario, getApplicationContext());
                                ProgresBar.dismiss();
                                Intent intent = new Intent(CriterioUsuario.this, AppSideBarActivity.class);
                                CriterioUsuario.this.finish();
                                startActivity(intent);

                            } catch (JSONException e) {
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
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
        jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestItinerario);
        breakTime();
    }
    void ImpresionPrueba(IPrinter printerx)
    {
        try {
            ValidaImpresion();
            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("--------------------------------\n");
            Ticket.append("   IMPRESION DE PRUEBA       1  \n");
            Ticket.append("   IMPRESION DE PRUEBA       2  \n");
            Ticket.append("   IMPRESION DE PRUEBA       3  \n");
            Ticket.append("   IMPRESION DE PRUEBA       4  \n");
            Ticket.append("--------------------------------\n");
            Ticket.append("\n\n\n\n\n");
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
    public void ValidaImpresion()
    {
        try{
            //printer.init();
            String error = String.valueOf(printer.getStatus());
            if(error.equals("1"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA OCUPADA", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //        return;
            }else if(error.equals("2"))
            {
                //Toast.makeText(getActivity(),"IMPRESORA SIN PAPEL", Toast.LENGTH_SHORT).show();
                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA SIN PAPEL", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //    return;
            }else if(error.equals("3"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"El formato del error del paquete de datos de impresión", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //     return;
            }
            else if(error.equals("4"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"MAL FUNCIONAMIENTO DE LA IMPRESORA", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //   return;
            }else if(error.equals("8"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA SOBRE CALOR", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //    return;
            }else if(error.equals("9"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"EL VOLTAJE DE LA IMPRESORA ES DEMASIADO BAJO", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //       return;
            }else if(error.equals("-16"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"La impresión no está terminada", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //return;
            }else if(error.equals("-6"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"error de corte de atasco", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //   return;
            }else if(error.equals("-5"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"error de apertura de la cubierta", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //    return;
            }else if(error.equals("-4"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"La impresora no ha instalado la biblioteca de fuentes", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //  return;
            }else if(error.equals("-2"))
            {
                Toast toast = Toast.makeText(getApplicationContext(),"El paquete de datos es demasiado largo", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 50, 50);
                toast.show();
                //  return;
            }

        }catch (Exception ex)
        {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void TDPROG_ESAG_GET(final String CO_EMPR,final String CO_UNID,final String CO_AGEN,final String CO_RUMB,final String CO_DEST_FINA)
    {
        try{
            String wsTDPROG_ESAG = getString(R.string.ws_ruta) + "TDPROG_ESAG/" + CO_EMPR + "/" + CO_UNID+"/"+CO_AGEN+"/"+CO_RUMB;
            Log.d("PermisoVenta",wsTDPROG_ESAG);
            JsonArrayRequest TDPROG_ESAG_GET = new JsonArrayRequest(Request.Method.GET, wsTDPROG_ESAG, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length()>0) {
                                guardarDataMemoria("VentaVip","InfinityDev",getApplicationContext());
                                JSONObject info;
                                ArrayList<String> TDPROG_ESAG = new ArrayList<>();
                                ArrayList<String> NU_SECUS = new ArrayList<>();
                                ArrayList<String> PERMISO_VENTA = new ArrayList<>();
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        info = response.getJSONObject(i);
                                        final String TramaRequest =info.getString("CO_RUMB") +"ƒ"+info.getString("FE_PROG").substring(0,10) +"ƒ"+info.getString("NU_SECU")
                                                +"ƒ"+info.getString("CO_DEST_ORIG")+"ƒ"+info.getString("CO_DEST_FINA")+"ƒ"+info.getString("TI_SERV");
                                        final String TRAMA_NU_SECUS = info.getString("NU_SECU");
                                        final String TRAMA_PERMISO_VENTA= info.getString("CO_DEST_ORIG") +"ƒ"+info.getString("CO_DEST_FINA")+"ƒ"+info.getString("TI_SERV");
                                        if (!TDPROG_ESAG.contains(TramaRequest)) {
                                            TDPROG_ESAG.add(TramaRequest);
                                        }
                                        if(!NU_SECUS.contains(TRAMA_NU_SECUS))
                                        {
                                            NU_SECUS.add(TRAMA_NU_SECUS);
                                        }
                                        if(!PERMISO_VENTA.contains(TRAMA_PERMISO_VENTA))
                                        {
                                            PERMISO_VENTA.add(TRAMA_PERMISO_VENTA);
                                        }
                                    }
                                    guardarDataMemoria("NU_SECU_ESAG",NU_SECUS.toString().substring(1,NU_SECUS.toString().length()-1),getApplicationContext());
                                    //TDPROG_ITIN_SE(CO_DEST_FINA,NU_SECUS.toString().substring(1,NU_SECUS.toString().length()-1),CO_UNID,CO_AGEN,CO_RUMB);
                                }catch (Exception ex)
                                {

                                }
                                guardarDataMemoria("TDPORG_ESAG_VIP",gson.toJson(PERMISO_VENTA),getApplicationContext());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
                    errorWS(editor, queue, error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                    String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            TDPROG_ESAG_GET.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(TDPROG_ESAG_GET);


        }catch (Exception ex)
        {

        }

    }
    public void TDPROG_ITIN_SE(final String CO_DEST_FINA,final String NU_SECUS,final String CO_UNID,final String CO_AGEN,final String CO_RUMB)
    {
        try{
            String ws_ItinerarioSE = getString(R.string.ws_ruta) + "TDPROG_ITIN_SE/" + NU_SECUS.replace(" ","") + "/" + CO_RUMB+"/01/"+CO_UNID+"/"+CO_AGEN+"/"+CO_DEST_FINA;
            Log.d("Itin_SE",ws_ItinerarioSE);
            JsonArrayRequest TDPROG_ITIN_SE = new JsonArrayRequest(Request.Method.GET, ws_ItinerarioSE, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length()>0) {
                                JSONObject info;
                                ArrayList<String> TDPROG_ITIN_SE = new ArrayList<>();
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        info = response.getJSONObject(i);
                                        final String TramaRequest =info.getString("CANT_ASIE") +"ƒ"+info.getString("HO_SALI") +"ƒ"+info.getString("CO_EMPR")
                                                +"ƒ"+info.getString("CO_VEHI")+"ƒ"+info.getString("FE_PROG").substring(0,10)+"ƒ"+info.getString("ST_TIPO_SERV")+"ƒ"+info.getString("CO_DEST_ORIG")
                                                +"ƒ"+info.getString("CO_DEST_FINA")+"ƒ"+info.getString("NU_SECU")+"ƒ"+info.getString("CO_TIPO_BUSS")+"ƒ"+info.getString("CO_RUMB")
                                                +"ƒ"+info.getString("CANT_VENT")+"ƒ"+info.getString("ANFITRION")+"ƒ"+info.getString("CONDUCTOR")+"ƒ"+info.getString("DE_TIPO_BUSS");
                                        if (!TDPROG_ITIN_SE.contains(TramaRequest)) {
                                            TDPROG_ITIN_SE.add(TramaRequest);
                                        }
                                    }
                                }catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                                final String Itin_SE = gson.toJson(TDPROG_ITIN_SE);
                                Log.d("VentaSP",Itin_SE);
                                guardarDataMemoria("TDPORG_ITIN_SE",Itin_SE,getApplicationContext());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
                    errorWS(editor, queue, error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                    String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            TDPROG_ITIN_SE.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(TDPROG_ITIN_SE);

        }catch(Exception ex)
        {

        }
    }

}
