package pe.com.telefonica.soyuz;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

//import com.zcs.sdk.print.PrnTextFont;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getCodigoUsuario;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarIntegerMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Button btn_login;
    EditText editText_user;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Boolean ventaDone = false;

    Gson gson;

    RequestQueue queue;

    Boolean getEmpresasDone     = false;
    Boolean getDestinosDone     = false;
    Boolean getTipoProductoDone = false;
    Boolean getUsuarioDone      = false;

    Boolean getCorreltativoDone = false;
    Boolean getServicioDone01   = false;
    Boolean getServicioDone02   = false;
    Boolean getTramosDone       = false;
    Boolean getTarifaDone01     = false;
    Boolean getTarifaDone02     = false;
    private IDAL dal;
    private IPrinter printer;



    int counter    = 0;
    int debug      = 0;

    String puestoUsuario;
    String KeySession;
    String codigoUsuario;
    String CO_GRUP;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getSupportActionBar().hide();

        final Context context = getApplicationContext();
        FuncionesAuxiliares.setLocale(Locale.US, getResources(), context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        gson = new Gson();
        queue = Volley.newRequestQueue(this);

        editText_user = findViewById(R.id.user);
        btn_login = findViewById(R.id.btn_login);

        final TextView txt_version = findViewById(R.id.txt_version);
//        txt_version.setText(String.format("v%s", BuildConfig.VERSION_NAME));

        txt_version.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                debug++;
                if (debug==5){
                    try{
                        Intent intent = new Intent(LoginActivity.this, DebugActivity.class);
                        LoginActivity.this.finish();
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                return false;
            }


        });

        try {
            dal = NeptuneLiteUser.getInstance().getDal(context);
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
        } catch (Exception e) {

        }
        final String MenuModulo = sharedPreferences.getString("MenuPerfil","NoData");
        final String Modulo = sharedPreferences.getString("Modulo","NoData");
        final String Ruta = sharedPreferences.getString("puestoUsuario","NoData");
         if (Modulo.equals("ANDROID_CONTROL")||Modulo.equals("ANDROID_INSP")|| Modulo.equals("ANDROID_VENTAS"))
         {
             Intent intent = new Intent(LoginActivity.this,AppSideBarActivity.class);
             LoginActivity.this.finish();
             startActivity(intent);
         }else if(MenuModulo.equals("S")){
             Intent intent = new Intent(LoginActivity.this,CriterioUsuario.class);
             LoginActivity.this.finish();
             startActivity(intent);
         }else if(Ruta.equals("ANFITRION ESTANDAR")||Ruta.equals("CONDUCTOR ESTANDAR")){
             Intent intent = new Intent(LoginActivity.this,AppSideBarActivity.class);
             LoginActivity.this.finish();
             startActivity(intent);
         }
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editText_user.length()>0) {
                    codigoUsuario = getCodigoUsuario(editText_user, editor);
                    btn_login.setEnabled(false);
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("Espere por favor");
                    progressDialog.setMessage("Iniciando sesión...");
                    progressDialog.setCancelable(false);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CRTL_VERS(gson, codigoUsuario, queue, sharedPreferences, editor, btn_login);
                        }
                    });
                    thread.start();
                    progressDialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "DEBE INGRESAR CODIGO DE USUARIO", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void inicioSesionAnfitrion(String puestoUsuarioURL) {

        String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/" + codigoUsuario + "/Puesto/" + puestoUsuarioURL;
        Log.d("ws_getItinerario",ws_getItinerario);
        JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "El Conductor no tiene ningún itinerario asignado.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() > 1) {
                            Toast.makeText(getApplicationContext(), "El Conductor tiene más de un itinerario asignado.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() == 1) {

                            try {
                                JSONObject info = response.getJSONObject(0);
                                String fecha = info.getString("FECHA_PROGRAMACION");
                                fecha = fecha.substring(0, 10);
                                guardarDataMemoria("anf_fechaProgramacion", fecha, getApplicationContext());
                                guardarDataMemoria("anf_codigoEmpresa", info.getString("CODIGO_EMPRESA"), getApplicationContext());
                                guardarDataMemoria("anf_codigoVehiculo", info.getString("CODIGO_VEHICULO"), getApplicationContext());
                                guardarDataMemoria("anf_horaSalida", info.getString("HORA_SALIDA"), getApplicationContext());
                                guardarDataMemoria("anf_servicio", info.getString("SERVICIO"), getApplicationContext());
                                guardarDataMemoria("anf_origen", info.getString("ORIGEN"), getApplicationContext());
                                guardarDataMemoria("anf_destino", info.getString("DESTINO"), getApplicationContext());
                                guardarDataMemoria("anf_secuencia", info.getString("SECUENCIA"), getApplicationContext());
                                guardarDataMemoria("anf_tipoBus", info.getString("TIPO_BUS"), getApplicationContext());
                                guardarDataMemoria("anf_rumbo", info.getString("RUMBO"), getApplicationContext());
                                guardarDataMemoria("anf_nombre", info.getString("ANFITRION"), getApplicationContext());
                                guardarDataMemoria("con_nombre",info.getString("CONDUCTOR"),getApplicationContext());
                                guardarDataMemoria("pro_fech",info.getString("FE_PROG"),getApplicationContext());
                                guardarDataMemoria("pro_hora",info.getString("HO_SALI"),getApplicationContext());
                                guardarDataMemoria("pro_orig",info.getString("DE_ORIG"),getApplicationContext());
                                guardarDataMemoria("pro_fina",info.getString("DE_FINA"),getApplicationContext());

                                GetAsientos(info.getString("RUMBO"),info.getString("TIPO_BUS"));
                                breakTime();
                                GetTramoVenta(info.getString("RUMBO"),info.getString("ORIGEN"));
                                breakTime();
                                ObtenerUltimaVenta(info.getString("CODIGO_VEHICULO").trim(),"ANFITRION%20ESTANDAR",info.getString("CODIGO_EMPRESA"));
                                breakTime();
                                GetTarifaRuta(info.getString("RUMBO"),info.getString("CODIGO_EMPRESA"),info.getString("SECUENCIA"),info.getString("FECHA_PROGRAMACION").substring(0, 10));
                                breakTime();
                                GetCorrelativoRuta(info.getString("CODIGO_EMPRESA"), info.getString("CODIGO_VEHICULO"),info.getString("RUMBO"));
                                breakTime();
                                GetAsientosVendidos(info.getString("CODIGO_EMPRESA"),info.getString("SECUENCIA"),info.getString("RUMBO"),fecha);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestItinerario);
        breakTime();

    }

    private void inicioSesionCarga(String puestoUsuarioURL) {

        Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
        LoginActivity.this.finish();
        startActivity(intent);

//        String ws_getItinerario = getString(R.string.ws_ruta) + "GetItinerario/CodigoPersonal/" + codigoUsuario + "/Puesto/" + puestoUsuarioURL;
//        Log.d("ws_getItinerario",ws_getItinerario);
//        JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_getItinerario, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        if (response.length() == 0) {
//                            Toast.makeText(getApplicationContext(), "El usuario no tiene ningún itinerario asignado.", Toast.LENGTH_LONG).show();
//                            errorView();
//                        } else if (response.length() > 1) {
//                            Toast.makeText(getApplicationContext(), "El usuario tiene más de un itinerario asignado.", Toast.LENGTH_LONG).show();
//                            errorView();
//                        } else if (response.length() == 1) {
//
//                            try {
//                                JSONObject info = response.getJSONObject(0);
//                                String fecha = info.getString("FECHA_PROGRAMACION");
//                                fecha = fecha.substring(0, 10);
//                                guardarDataMemoria("anf_fechaProgramacion", fecha, getApplicationContext());
//                                guardarDataMemoria("anf_codigoEmpresa", info.getString("CODIGO_EMPRESA"), getApplicationContext());
//                                guardarDataMemoria("anf_codigoVehiculo", info.getString("CODIGO_VEHICULO"), getApplicationContext());
//                                guardarDataMemoria("anf_horaSalida", info.getString("HORA_SALIDA"), getApplicationContext());
//                                guardarDataMemoria("anf_servicio", info.getString("SERVICIO"), getApplicationContext());
//                                guardarDataMemoria("anf_origen", info.getString("ORIGEN"), getApplicationContext());
//                                guardarDataMemoria("anf_destino", info.getString("DESTINO"), getApplicationContext());
//                                guardarDataMemoria("anf_secuencia", info.getString("SECUENCIA"), getApplicationContext());
//                                guardarDataMemoria("anf_tipoBus", info.getString("TIPO_BUS"), getApplicationContext());
//                                guardarDataMemoria("anf_rumbo", info.getString("RUMBO"), getApplicationContext());
//                                guardarDataMemoria("anf_nombre", info.getString("ANFITRION"), getApplicationContext());
//                                guardarDataMemoria("CO_VEHI_anfi",info.getString("CODIGO_VEHICULO"),getApplicationContext());
//
//                                GetAsientos(info.getString("RUMBO"),info.getString("TIPO_BUS"));
//                                breakTime();
//                                GetTramoVenta(info.getString("RUMBO"),info.getString("ORIGEN"));
//                                breakTime();
//                                ObtenerUltimaVenta(info.getString("CODIGO_VEHICULO").trim(),"ANFITRION%20ESTANDAR",info.getString("CODIGO_EMPRESA"));
//                                breakTime();
//                                GetTarifaRuta(info.getString("RUMBO"),info.getString("CODIGO_EMPRESA"),info.getString("SECUENCIA"),info.getString("FECHA_PROGRAMACION").substring(0, 10));
//                                breakTime();
//                                GetCorrelativoRuta(info.getString("CODIGO_EMPRESA"), info.getString("CODIGO_VEHICULO"),info.getString("RUMBO"));
//                                breakTime();
//                                GetAsientosVendidos(info.getString("CODIGO_EMPRESA"),info.getString("SECUENCIA"),info.getString("RUMBO"),fecha);
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Toast.makeText(getApplicationContext(), "Error en la ws getItinerario.", Toast.LENGTH_LONG).show();
//                errorWS(editor, queue, error);
//                /* ----------------------------------------- */
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", auth);
//                return headers;
//            }
//        };
//        jsonArrayRequestItinerario.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        queue.add(jsonArrayRequestItinerario);
//        breakTime();

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
    public void errorView() {

        btn_login.setEnabled(true);
        progressDialog.dismiss();

        Intent intent = new Intent(LoginActivity.this, ErrorActivity.class);
        LoginActivity.this.finish();
        startActivity(intent);
    }



    public void CRTL_VERS(final Gson gson, final String codigoUsuario, final RequestQueue queue, final SharedPreferences sharedPreferences, final SharedPreferences.Editor editor, final Button btn_login)
    {

        String wsCTRL_VERS = getString(R.string.ws_ruta)+ "CTRL_VERS/OFIVENT/ANDROID";
        JsonArrayRequest RequestUltimaVenta = new JsonArrayRequest(Request.Method.GET, wsCTRL_VERS, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()>0) {
                            JSONObject json;
//                            try   {
//                                    json = response.getJSONObject(0);
//                                    if (BuildConfig.VERSION_NAME.trim().equals(json.getString("NU_VERS")))
//                                    {
                                        ObtenerUsuario();
//                                    }
//                                    else
//                                    {
//                                        Toast.makeText(getApplicationContext(),json.getString("MSJ") , Toast.LENGTH_LONG).show();
//                                        btn_login.setEnabled(true);
//                                        progressDialog.dismiss();
//                                    }
//                            } catch (JSONException e) {
//
//                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws de Version", Toast.LENGTH_LONG).show();
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
        Log.d("Token",ws_GeneraToken);
        Log.d("TokenCuerpo",JsonToken.toString());
        MyJSONArrayRequest jsonArrayCierreSession = new MyJSONArrayRequest(Request.Method.POST, ws_GeneraToken, JsonToken,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            JSONObject info;
                            try {
                                info = response.getJSONObject(0);
                                KeySession = info.getString("TOKEN");
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
        progressDialog.dismiss();
        errorView();
    }

    static {
        System.loadLibrary("DeviceConfig");
    }

    private void ObtenerUsuario()
    {
        String ws_getUsuario = getString(R.string.ws_ruta) + "GetUsuario/" + codigoUsuario;
        Log.d("usuario",ws_getUsuario);
        JsonArrayRequest jsonArrayRequestPuesto = new JsonArrayRequest(Request.Method.GET, ws_getUsuario, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Usuario no Existe o Tiene session Activa", Toast.LENGTH_LONG).show();
                            errorView();
                        }else if (response.length() > 1) {
                            Toast.makeText(getApplicationContext(), "Se encontraron más de un usuario con el mismo código.", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            btn_login.setEnabled(true);
                            errorView();
                        } else if (response.length() == 1) {
                            try{
                                JSONObject info = response.getJSONObject(0);
                                puestoUsuario = info.getString("PUESTO");
                                final String ID_POS = info.getString("ID_POS");
                                CO_GRUP = info.getString("CO_GRUP");
                                final String puestoUsuarioURL = puestoUsuario.replace(" ", "%20");
                                guardarDataMemoria("CodUsuario",codigoUsuario,getApplicationContext());
                                guardarDataMemoria("puestoUsuario", info.getString("PUESTO"), getApplicationContext());
                                guardarDataMemoria("nombreEmpleado", info.getString("EMPLEADO"), getApplicationContext());
                                guardarDataMemoria("puestoUsuarioCompleto", puestoUsuarioURL, getApplicationContext());
                                guardarDataMemoria("CO_GRUP",CO_GRUP,getApplicationContext());
                                if(CO_GRUP.equals("null") && puestoUsuario.equals("CONDUCTOR ESTANDAR")||
                                        puestoUsuario.equals("ANFITRION ESTANDAR"))
                                {
                                    if(puestoUsuario.equals("CONDUCTOR ESTANDAR")||
                                            puestoUsuario.equals("ANFITRION ESTANDAR")){
                                        guardarDataMemoria("Modulo",puestoUsuario,getApplicationContext());
                                        GetEmpresa();
                                        breakTime();
                                        GetDestinos();
                                        breakTime();
                                        GetProductosCarga();
                                        breakTime();
                                        inicioSesionAnfitrion(puestoUsuarioURL);
                                       /* Toast Te = Toast.makeText(getApplicationContext(), puestoUsuario, Toast.LENGTH_LONG);
                                        Te.setGravity(Gravity.CENTER, 50, 50);
                                        Te.show();*/
                                        //progressDialog.dismiss();
                                    }else{
                                        Toast Te = Toast.makeText(getApplicationContext(),"PUESTO : "+ puestoUsuario+" NO PERMITIDO", Toast.LENGTH_LONG);
                                        Te.setGravity(Gravity.CENTER, 50, 50);
                                        Te.show();
                                        btn_login.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                }else if(puestoUsuario.equals("INSPECTOR DE RUTA")
                                        || puestoUsuario.equals("SUPERVISOR DE RUTA")
                                        || puestoUsuario.equals("SUPERVISOR DE ZONA")) {
                                    final String Serial = Build.SERIAL;
                                    if (ID_POS.trim().equals(Serial.trim())) {
                                        guardarDataMemoria("Modulo", "ANDROID_INSP", getApplicationContext());
                                        GetEmpresa();
                                        breakTime();
                                        GetDestinos();
                                        breakTime();
                                        GET_ZONA(codigoUsuario);
                                        breakTime();
                                        GetPuntosControl(codigoUsuario);
                                        GetReporteCantInsp(codigoUsuario);
                                        GetItinerario("ANDROID_INSP",sharedPreferences);
                                       /* progressDialog.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
                                        LoginActivity.this.finish();
                                        startActivity(intent);*/
                                    }else if(!ID_POS.trim().equals(Serial.trim())){
                                        Toast Te = Toast.makeText(getApplicationContext(),"POS PERTENECE A OTRO USUARIO", Toast.LENGTH_LONG);
                                        Te.setGravity(Gravity.CENTER, 50, 50);
                                        Te.show();
                                        btn_login.setEnabled(true);
                                        progressDialog.dismiss();
                                    }else{
                                        Toast Te = Toast.makeText(getApplicationContext(),"INSPECTOR NO CUENTA CON ASIGNACION DE POS", Toast.LENGTH_LONG);
                                        Te.setGravity(Gravity.CENTER, 50, 50);
                                        Te.show();
                                        btn_login.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                }else if(puestoUsuario.equals("AUXILIAR DE ENCOMIENDA")
                                        || puestoUsuario.equals("SUPERVISOR DE CARGA Y ENCOMIENDA")){
                                    guardarDataMemoria("Modulo",puestoUsuario,getApplicationContext());
                                    GetEmpresa();
                                    breakTime();
                                    GetProductosCarga();
                                    breakTime();
                                    inicioSesionCarga(puestoUsuarioURL);
                                }else if(!CO_GRUP.equals("null") && ID_POS.equals("null")){
                                    ObtenerMenu(CO_GRUP);
                                }else{
                                    Toast Te = Toast.makeText(getApplicationContext(),"PUESTO : "+ puestoUsuario+" NO PERMITIDO", Toast.LENGTH_LONG);
                                    Te.setGravity(Gravity.CENTER, 50, 50);
                                    Te.show();
                                    btn_login.setEnabled(true);
                                    progressDialog.dismiss();
                                }
                            }catch (JSONException e){
                                Toast.makeText(getApplicationContext(), "Se recibió datos erróneos de la webservice getUsuario.", Toast.LENGTH_LONG).show();
                                btn_login.setEnabled(true);
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws UsuarioxGrupo.", Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
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
        queue.add(jsonArrayRequestPuesto);
        breakTime();

    }
    private void ObtenerMenu(String CO_GRUP)
    {
        String ws_getMenu = getString(R.string.ws_ruta) + "TRMENU_GRUP/" + CO_GRUP;
        Log.d("ws_grupo",ws_getMenu);
        JsonArrayRequest jsonArrayRequestGrupo = new JsonArrayRequest(Request.Method.GET, ws_getMenu, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "GRUPO NO PERMITIDO", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() > 1) {
                            try{
                                ArrayList<String> ListaMenuXGrupo = new ArrayList<>();
                                final String jsonMenu;
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject info = response.getJSONObject(i);
                                    ListaMenuXGrupo.add(info.getString("CO_MENU")+"ƒ"+info.getString("NO_MENU"));
                                }
                                jsonMenu = gson.toJson(ListaMenuXGrupo);
                                guardarDataMemoria("MenuSoyuz",jsonMenu,getApplicationContext());
                                //Log.d("jsonGrupo",jsonMenu);
                                progressDialog.dismiss();
                                guardarDataMemoria("MenuPerfil","S",getApplicationContext());
                                Intent intent = new Intent(LoginActivity.this, CriterioUsuario.class);
                                LoginActivity.this.finish();
                                startActivity(intent);
                            }catch (JSONException e){
                                Toast.makeText(getApplicationContext(), "Se recibió datos erróneos de la webservice TRMENU_GRUP.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws TRMENU_GRUP.", Toast.LENGTH_LONG).show();
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
        queue.add(jsonArrayRequestGrupo);
        breakTime();

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

    }
    void GetTarifaRuta(String Rumbo, final String Empresa, String Secuencia, String FechaProgramacion)
    {
        String ws_getTarifa = getString(R.string.ws_ruta) + "GetTarifa/" + Rumbo + "/" + Empresa + "/" +
                Secuencia + "/" + FechaProgramacion;
        /* ----------------------------------------- */
        Log.d("urlWS_TARIFA", ws_getTarifa);
        /* Request que obtiene todas las tarifas de viaje y carga */
        JsonArrayRequest jsonArrayRequestTarifa = new JsonArrayRequest(Request.Method.GET, ws_getTarifa, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            /* En caso el usuario no tenga itinerario asignados no puede iniciar sesión */
                            Toast.makeText(getApplicationContext(), "No hay tarifas para este itinerario.", Toast.LENGTH_LONG).show();
                            errorView();
                            /* ----------------------------------------- */

                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_tarifasViaje = new ArrayList<>();
                                //ArrayList<String> lista_tarifasCarga = new ArrayList<>();
                                //final String NU_TARI =  info = response.getJSONObject(0);
                                info = response.getJSONObject(0);
                                guardarDataMemoria("NU_TARI",info.getString("NU_TARI"),getApplicationContext());
                                guardarDataMemoria("CO_TIPO",info.getString("CO_TIPO"),getApplicationContext());

                                /* Se generan dos tramas, una con las tarifas de viaje y otra de carga, y ambas se guardan en memoria */
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);

                                    /*if (info.getString("TI_TARI").equals("TARIFA VIAJE")) {*/

                                        lista_tarifasViaje.add(info.getString("CO_DEST_ORIG") + "-"
                                                + info.getString("CO_DEST_FINA") + "-" + Integer.toString(info.getInt("PR_BASE")));

                                                //Integer.toString(info.getInt("PR_BASE")));

                                    /*}
                                    /*else if (info.getString("TI_TARI").equals("TARIFA CARGA")) {

                                        lista_tarifasCarga.add(info.getString("PRODUCTO") + "-"
                                                + info.getString("ORIGEN") + "-"
                                                + info.getString("DESTINO") + "-"
                                                + Integer.toString(info.getInt("IMPORTE")));
                                    }*/
                                }

                                String jsonTarifasViaje = gson.toJson(lista_tarifasViaje);
                                guardarDataMemoria("anf_jsonTarifasViaje", jsonTarifasViaje, getApplicationContext());

                                ImprimeTarifa(printer,lista_tarifasViaje,Empresa);

                                /*String jsonTarifasCarga = gson.toJson(lista_tarifasCarga);
                                guardarDataMemoria("anf_jsonTarifasCarga", jsonTarifasCarga, getApplicationContext());
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
                Toast.makeText(getApplicationContext(), "Error en la ws getTarifas.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestTarifa.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestTarifa);
    }
    void ImprimeTarifa(IPrinter printerx, ArrayList<String> Tarifario,String CO_EMPR)
    {
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("--------------------------------\n");
            Ticket.append("   NU_SECU  : "+ sharedPreferences.getString("anf_secuencia","Nodata")+"\n");
            Ticket.append("   COD_BUS  : "+ sharedPreferences.getString("anf_codigoVehiculo","Nodata")+"\n");
            Ticket.append("   TARIFARIO: "+sharedPreferences.getString("NU_TARI","NoData")+"\n");
            Ticket.append("   EMPRESA: "+CO_EMPR+"\n");
            Ticket.append("   FECHA: "+FechaImpresion+"\n");
            Ticket.append("   RUMBO: "+sharedPreferences.getString("anf_rumbo","NoData")+"\n");
            Ticket.append("--------------------------------\n");
            int Elementro=1;
            for (int i=0;i<Tarifario.size();i++)
            {

                String[] Tari = Tarifario.get(i).split("-");
                Ticket.append("|"+Tari[0]+"-"+Tari[1]+"->"+Tari[2]+"|");
                if(Elementro==3){
                    Ticket.append("\n");
                    Ticket.append("--------------------------------\n");
                    Elementro=0;
                }
                Elementro=Elementro+1;
            }

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
//                                GeneraToken(sharedPreferences);
                                String jsonItinerario = gson.toJson(lista_itinerario);
                                guardarDataMemoria("jsonItinerario", jsonItinerario, getApplicationContext());
                                //guardarDataMemoria("jsonItinerario", lista_itinerario.toString(), getApplicationContext());
                                progressDialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
                                LoginActivity.this.finish();
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
    void GET_ZONA(String CO_TRAB)
    {
            String ws_zona = getString(R.string.ws_ruta) + "TTINSP_TRAM/" + CO_TRAB;
            Log.d("getItinerario",ws_zona);
            JsonArrayRequest jsonArrayRequestItinerario = new JsonArrayRequest(Request.Method.GET, ws_zona, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length() == 0) {
                                Toast.makeText(getApplicationContext(), "El usuario no tiene ZONA ASIGNADA", Toast.LENGTH_LONG).show();
                                errorView();
                            } else if (response.length() > 1) {
                                JSONObject info;
                                ArrayList<String> lista_zona = new ArrayList<>();
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        info = response.getJSONObject(i);
                                        lista_zona.add(info.getString("CO_TRAM") + "/" + info.getString("DE_TRAM") +
                                                "/" + info.getString("CO_ZONA")+"/"+info.getString("NU_ORDE_SUR")+"/"+info.getString("NU_ORDE_NOR"));
                                    }
                                    String JSON_ZONA = gson.toJson(lista_zona);
                                    guardarDataMemoria("jsonZona", JSON_ZONA, getApplicationContext());
                                } catch (JSONException e) {
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error en la ws zona.", Toast.LENGTH_LONG).show();
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

    void GetAsientos(final String Rumbo,final String Tipo_Bus)
    {
        String ws_getDistribucionAsientos = getString(R.string.ws_ruta) + "DistribucionAsiento/Rumbo/" + Rumbo + "/TipoBus/" + Tipo_Bus;
        Log.d("DistribucionAsiento",ws_getDistribucionAsientos);
        JsonArrayRequest jsonArrayRequestDistribucionAsientos = new JsonArrayRequest(Request.Method.GET, ws_getDistribucionAsientos, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0) {
                    Toast.makeText(getApplicationContext(), "No hay distribución de asientos para el bus de este itinerario.", Toast.LENGTH_LONG).show();
                    errorView();
                }else if (response.length() > 0) {
                    try {
                        int num_asientos = response.length();
                        String num_col = "0";

                        /* Se obtiene el número de asientos y columnas, y se guarda en memoria */
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject info = response.getJSONObject(i);
                            String num_col_temp = info.getString("NU_COLU");

                            if (Integer.parseInt(num_col_temp) > Integer.parseInt(num_col)) {
                                num_col = num_col_temp;
                            }
                        }

                        guardarIntegerMemoria("anf_numAsientos", num_asientos, getApplicationContext());
                        guardarIntegerMemoria("anf_numCol", Integer.valueOf(num_col), getApplicationContext());
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
                Toast.makeText(getApplicationContext(), "Error en la ws getDistribucionAsientos.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestDistribucionAsientos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestDistribucionAsientos);
    }
    void GetTramoVenta(final String Rumbo,final String Origen)
    {
        String ws_getTramos = getString(R.string.ws_ruta) + "GetTramo/" + Rumbo +"/"+ Origen;
        Log.d("Tramos",ws_getTramos);
        JsonArrayRequest jsonArrayRequestTramos = new JsonArrayRequest(Request.Method.GET, ws_getTramos, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "No hay tramos para este itinerario.", Toast.LENGTH_LONG).show();
                            errorView();
                        }else if (response.length() > 0) {

                            JSONObject info;
                            ArrayList<String> lista_tramos = new ArrayList<>();

                            try {
                                /* Se genera una trama con los tramos de viaje y se guarda en memoria */
                                for(int i = 0; i < response.length(); i++){

                                    info = response.getJSONObject(i);
                                    String data = info.getString("CO_DEST_FINA")+"-"+
                                            info.getString("NU_KILO_VIAJ");

                                    if(!lista_tramos.contains(data)){
                                        lista_tramos.add(data);
                                    }
                                }

                                String jsonTramos = gson.toJson(lista_tramos);
                                guardarDataMemoria("anf_jsonTramos", jsonTramos, getApplicationContext());
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
        jsonArrayRequestTramos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestTramos);
        breakTime();
    }
    void GetCorrelativoRuta(final String CO_EMPR, final String CO_VEHI,final String CO_RUMB)
    {
        String ws_getCorrelativo = getString(R.string.ws_ruta) + "GetUltimoCorrelativo/" + CO_EMPR + "/" + CO_VEHI + "/" + CO_RUMB + "/~ ";
        Log.d("correlativo", ws_getCorrelativo);
        JsonArrayRequest jsonArrayRequestCorrelativo = new JsonArrayRequest(Request.Method.GET, ws_getCorrelativo, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "No hay correlativos para este itinerario.", Toast.LENGTH_LONG).show();
                            errorView();
                        } else if (response.length() > 0) {
                            try {

                                JSONObject info;
                                String viaje_correlativoBlt = "";
                                String viaje_serieBlt = "";
                                String viaje_correlativoFac = "";
                                String viaje_serieFac = "";

                                String carga_correlativoBol = "";
                                String carga_serieBol = "";
                                String carga_correlativoFac = "";
                                String carga_serieFac = "";

                                /* Se obtiene las series y correlativos de viaje y carga, y se guarda en memoria */
                                for (int i = 0; i < response.length(); i++) {

                                    info = response.getJSONObject(i);
                                    if (info.getString("DE_GSER").equals("PASAJES RUTA")) {

                                        if (info.getString("TI_DOCU").equals("BLT")) {

                                            viaje_serieBlt = info.getString("NUMERO_SERIE");
                                            viaje_correlativoBlt = info.getString("ULTIMO_CORRELATIVO");
                                            guardarDataMemoria("anf_tipoDocumentoBltViaje", info.getString("TIPO_DOCUMENTO"), getApplicationContext());

                                        } else if (info.getString("TI_DOCU").equals("FAC")) {

                                            viaje_serieFac = info.getString("NUMERO_SERIE");
                                            viaje_correlativoFac = info.getString("ULTIMO_CORRELATIVO");
                                            guardarDataMemoria("anf_tipoDocumentoFacViaje", info.getString("TIPO_DOCUMENTO"), getApplicationContext());
                                        }
                                    } else if (info.getString("DE_GSER").equals("CARGA")) {
                                        if (info.getString("TI_DOCU").equals("BOL")) {
                                            carga_serieBol = info.getString("NUMERO_SERIE");
                                            carga_correlativoBol = info.getString("ULTIMO_CORRELATIVO");
                                            guardarDataMemoria("anf_tipoDocumentoBolCarga", info.getString("TIPO_DOCUMENTO"), getApplicationContext());
                                        } else if (info.getString("TI_DOCU").equals("FAC")) {
                                            carga_serieFac = info.getString("NUMERO_SERIE");
                                            carga_correlativoFac = info.getString("ULTIMO_CORRELATIVO");
                                            guardarDataMemoria("anf_tipoDocumentoFacCarga", info.getString("TIPO_DOCUMENTO"), getApplicationContext());
                                        }
                                    }
                                }

                                guardarDataMemoria("anf_numSerieBltViaje", viaje_serieBlt, getApplicationContext());
                                //Log.d("viaje_serieBlt", viaje_serieBlt );
                                guardarDataMemoria("anf_correlativoBltViaje", viaje_correlativoBlt, getApplicationContext());
                                //Log.d("viaje_correlativoBlt", viaje_correlativoBlt );
                                guardarDataMemoria("anf_numSerieFacViaje", viaje_serieFac, getApplicationContext());
                                //Log.d("viaje_serieFac", viaje_serieFac );
                                guardarDataMemoria("anf_correlativoFacViaje", viaje_correlativoFac, getApplicationContext());
                                //Log.d("viaje_correlativoFac", viaje_correlativoFac );

                                guardarDataMemoria("anf_numSerieBolCarga", carga_serieBol, getApplicationContext());
                                //Log.d("carga_serieBol", carga_serieBol );
                                guardarDataMemoria("anf_correlativoBolCarga", carga_correlativoBol, getApplicationContext());
                                //Log.d("carga_correlativoBol", carga_correlativoBol );
                                guardarDataMemoria("anf_numSerieFacCarga", carga_serieFac, getApplicationContext());
                                //Log.d("carga_serieFac", carga_serieFac );
                                guardarDataMemoria("anf_correlativoFacCarga", carga_correlativoFac, getApplicationContext());
                                //Log.d("carga_correlativoFac", carga_correlativoFac );
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
        jsonArrayRequestCorrelativo.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestCorrelativo);
        breakTime();
    }
    void GetAsientosVendidos(final String CO_EMPR,final String NU_SECU,final String CO_RUMB,final String FE_VIAJ)
    {
        String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + CO_EMPR + "/" + NU_SECU + "/" + CO_RUMB + "/" + FE_VIAJ;
        Log.d("asientos vendidos", ws_getAsientosVendidos);
        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        String asientosVend = "";
                        guardarDataMemoria("cantBoletos",String.valueOf(response.length()),getApplicationContext());
                        if (response.length() == 0) {
                            progressDialog.dismiss();
//                            Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
//                            LoginActivity.this.finish();
                            Intent intent = new Intent(LoginActivity.this, iniAnfi.class);
                            LoginActivity.this.finish();
                            startActivity(intent);
                        } else if (response.length() > 0) {
                            try {

                                JSONObject info;
                                ArrayList<String> lista_reporteVenta = new ArrayList<>();
                                ArrayList<String> lista_reporteVentaGPS = new ArrayList<>();

                                /* Se genera una trama con los asientos vendidos y se obtiene el número del asientos, ambos se guardan en memoria */
                                for (int i = 0; i < response.length(); i++) {

                                    info = response.getJSONObject(i);
                                    if(info.getString("ServicioEmpresa").equals("VIAJE") && info.getString("LIBERADO").equals("NO")){

                                        lista_reporteVenta.add(info.getString("NU_ASIE"));

                                        asientosVend += info.getString("NU_ASIE") + "-" +
                                                info.getString("NU_DOCU") + "-" +
                                                info.getString("CO_DEST_ORIG") + "-" +
                                                info.getString("CO_DEST_FINA") + "-" +
                                                info.getString("CO_CLIE") + "-" +
                                                info.getString("IM_TOTA") + "-" +
                                                info.getString("CO_EMPR") + "-" +
                                                info.getString("TI_DOCU") + "-" +
                                                info.getString("LIBERADO") + "-" +
                                                info.getString("CARGA") + "-" +
                                                info.getString("ServicioEmpresa") +"/";

                                        ventaBlt = new DatabaseBoletos(getApplicationContext());
                                        sqLiteDatabase = ventaBlt.getWritableDatabase();
                                        try   {
                                            ContentValues sqlQuery = new ContentValues();
                                            sqlQuery.put("CO_EMPR", sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
                                            sqlQuery.put("TI_DOCU", "BLT");
                                            String Nu_docu= info.getString("NU_DOCU");
                                            sqlQuery.put("NU_DOCU",Nu_docu.substring(0,4)+"-"+ String.valueOf(Integer.valueOf(Nu_docu.substring(4,Nu_docu.length()))));
                                            sqlQuery.put("NU_SECU", info.getString("NU_SECU"));
                                            sqlQuery.put("FE_VIAJ",info.getString("FE_VIAJ"));
                                            sqlQuery.put("HO_VIAJ",info.getString("HO_VIAJ"));
                                            sqlQuery.put("CO_VEHI",sharedPreferences.getString("anf_codigoVehiculo","NoData"));
                                            sqlQuery.put("NO_CLIE",info.getString("NO_CLIE"));
                                            sqlQuery.put("CO_DEST_ORIG",info.getString("CO_DEST_ORIG"));
                                            sqlQuery.put("CO_DEST_FINA",info.getString("CO_DEST_FINA"));
                                            sqlQuery.put("DOCU_IDEN",info.getString("NU_DNIS"));
                                            sqlQuery.put("NU_ASIE",info.getString("NU_ASIE"));
                                            sqlQuery.put("IM_TOTA",info.getString("IM_TOTA"));
                                            sqlQuery.put("TIPO","1");
                                            sqLiteDatabase.insert("Manifiesto",null,sqlQuery);

                                        } catch (Exception e) {
                                            String error = e.getMessage();

                                        }
                                    }
                                }
                                //guardarDataMemoria("cantBoletos",String.valueOf(response.length()),getApplicationContext());
                                //Log.d("loginCant",String.valueOf(response.length()));
                                asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                lista_reporteVentaGPS.add(asientosVend);

                                String json_reporteVentaGPS = gson.toJson(lista_reporteVentaGPS);
                                guardarDataMemoria("anf_jsonReporteVentaGPS", json_reporteVentaGPS, getApplicationContext());

                                ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                                String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getApplicationContext());

                                String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                                guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getApplicationContext());
                                /* ----------------------------------------- */
                                //GeneraToken(sharedPreferences);
                                progressDialog.dismiss();
//                                Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
//                                LoginActivity.this.finish();
                                Intent intent = new Intent(LoginActivity.this, iniAnfi.class);
                                LoginActivity.this.finish();
                                startActivity(intent);
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
                Toast.makeText(getApplicationContext(), "Error en la ws getReporteVenta.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestAsientosVendidos);
        breakTime();
    }

    void GetPuntosControl(final String CO_TRAB)
    {

        String ws_CTRL_INSP_POS = getString(R.string.ws_ruta) + "CTRL_INSP_POS/" + CO_TRAB;
        Log.d("Control", ws_CTRL_INSP_POS);
        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_CTRL_INSP_POS, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {

                            Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
                            LoginActivity.this.finish();
                            startActivity(intent);
                        } else if (response.length() > 0) {
                            try {

                                JSONObject info;
                                final ArrayList<String> Lista_control = new ArrayList<>();
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    final String CONTROL_INSP = info.getString("ID_TMCTRL_INSP") + "ƒ" + info.getString("CO_TRAM") + "ƒ" + info.getString("CO_RUMB") + "ƒ" + info.getString("CO_DEST_FINA");
                                    Lista_control.add(CONTROL_INSP);
                                }
                                guardarDataMemoria("CTRL_INSP_POS", gson.toJson(Lista_control), getApplicationContext());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getReporteVenta.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestAsientosVendidos);
        breakTime();

    }

    void GetReporteCantInsp(final String CO_TRAB)
    {

        String ws_ReporCantInsp = getString(R.string.ws_ruta) + "ReporteCantInsp/" + CO_TRAB;
        final ArrayList<String> lista_reporteCantInsp = new ArrayList<>();
        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_ReporCantInsp, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        String cantidinsp = "";
                        if (response.length() > 0) {
                            try {
                                JSONObject info;
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);
                                    cantidinsp += info.getString("FE_PROG") + "-" + info.getString("IN_INSP") + "-" +
                                            info.getString("FI_INSP") + "-" + info.getString("CO_VEHI") + "-" +
                                            info.getString("CA_INSP") +"/";
                                }
                                cantidinsp = cantidinsp.substring(0, cantidinsp.length() - 1);
                                lista_reporteCantInsp.add(cantidinsp);
                                String json_reporteVenta = gson.toJson(lista_reporteCantInsp);
                                guardarDataMemoria("insp_jsonReporteCantInsp", json_reporteVenta, getApplicationContext());
                                progressDialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws getReporteVenta.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestAsientosVendidos);
        breakTime();

    }



}