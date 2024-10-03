package pe.com.telefonica.soyuz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
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
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;


public class AppSideBarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private String RespuestaWS="";
    private String RespuestaWS_Session="";
    private int UserCierra=0;
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseBoletos ventaBlt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_side_bar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        /* Set del nombre y puesto del usuario que ha iniciado sesión en la barra lateral */
        TextView empleado = headerView.findViewById(R.id.empleado);
        empleado.setText(sharedPreferences.getString("nombreEmpleado", ""));

        TextView puesto = headerView.findViewById(R.id.puesto);
        puesto.setText(sharedPreferences.getString("puestoUsuario", ""));

        TextView CodigoBus = headerView.findViewById(R.id.busHeader);
        CodigoBus.setVisibility(View.INVISIBLE);
        /* ----------------------------------------- */

        navigationView.setNavigationItemSelectedListener(this);
        if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")||sharedPreferences.getString("Modulo", "nada").equals("ANDROID_INSP")){
            Menu menu = navigationView.getMenu();
            MenuItem item3 = menu.findItem(R.id.nav_ReporteControlador);
            item3.setVisible(false);
        }else{
            Menu menu = navigationView.getMenu();
            MenuItem item3 = menu.findItem(R.id.nav_ReporteControlador);
            item3.setVisible(false);
        }

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("ANFITRION ESTANDAR")||
                sharedPreferences.getString("puestoUsuario", "nada").equals("CONDUCTOR ESTANDAR")){

            if(sharedPreferences.getString("anf_codigoEmpresa","NoData").equals("01"))
            {
                ImageView imgView = headerView.findViewById(R.id.imageView);
                imgView.setImageResource(R.drawable.peru_bus);
            }
//            CodigoBus.setText("BUS : " + String.valueOf(Integer.valueOf(sharedPreferences.getString("anf_codigoVehiculo", ""))));
            CodigoBus.setVisibility(View.VISIBLE);

            Menu menu = navigationView.getMenu();


            MenuItem item = menu.findItem(R.id.nav_list);
            item.setIcon(R.drawable.ic_seat);
            item.setTitle("Venta Pasaje");
            item.setVisible(true);

//            MenuItem item3 = menu.findItem(R.id.nav_tarifario);
//            item3.setVisible(false);
//
            MenuItem item13 = menu.findItem(R.id.nav_repoCantinsp);
            item13.setVisible(false);

            MenuItem item100 = menu.findItem(R.id.vent_carg_enco);
            item100.setVisible(false);
//
            MenuItem item53 = menu.findItem(R.id.sel_caja);
            item53.setVisible(false);
//
            MenuItem item23 = menu.findItem(R.id.nav_enco_carg);
            item23.setVisible(false);
//
//            MenuItem item33 = menu.findItem(R.id.nav_Manifiesto);
//            item33.setVisible(false);
//
            MenuItem item43 = menu.findItem(R.id.nav_MataBoleto);
            item43.setVisible(false);
//
            MenuItem AsignaAsiento = menu.findItem(R.id.nav_asignarAsientos);
            AsignaAsiento.setVisible(false);
//
            MenuItem Vip = menu.findItem(R.id.nav_servicio_especial);
            Vip.setVisible(false);
//
            MenuItem Trasbordo = menu.findItem(R.id.nav_trasbordo);
            Trasbordo.setVisible(false);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

//            ft.replace(R.id.fragment_base, new CargaExtraFragment());
            ft.replace(R.id.fragment_base, new VentaBoletosFragment());
            ft.commit();
            navigationView.setCheckedItem(R.id.nav_list);

        }
        else if(sharedPreferences.getString("puestoUsuario", "nada").equals("AUXILIAR DE ENCOMIENDA")||
                sharedPreferences.getString("puestoUsuario", "nada").equals("SUPERVISOR DE CARGA Y ENCOMIENDA") ){

            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.nav_liquidacion);
            item.setVisible(true);

            MenuItem itemVenta = menu.findItem(R.id.nav_list);
            itemVenta.setVisible(false);

            MenuItem Vip = menu.findItem(R.id.nav_servicio_especial);
            Vip.setVisible(false);

            MenuItem item100 = menu.findItem(R.id.vent_carg_enco);
            item100.setVisible(false);

            MenuItem item1 = menu.findItem(R.id.nav_tarifario);
            item1.setVisible(false);

            MenuItem item2 = menu.findItem(R.id.nav_carga);
            item2.setVisible(false);

            MenuItem item20 = menu.findItem(R.id.nav_camara);
            item20.setVisible(false);

            MenuItem item21 = menu.findItem(R.id.sel_caja);
            item21.setVisible(false);

            MenuItem item22 = menu.findItem(R.id.nav_enco_carg);
            item22.setVisible(false);

            MenuItem item23 = menu.findItem(R.id.nav_repoCantinsp);
            item23.setVisible(false);

            MenuItem item3 = menu.findItem(R.id.nav_Manifiesto);
            item3.setVisible(false);

            MenuItem item4 = menu.findItem(R.id.nav_MataBoleto);
            item4.setVisible(false);

            MenuItem item5 = menu.findItem(R.id.nav_trasbordo);
            item5.setVisible(false);

            MenuItem item6 = menu.findItem(R.id.nav_asignarAsientos);
            item6.setVisible(false);


        }
        else if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_INSP") ||
                sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS") ||
                sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")){

            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.nav_liquidacion);

            if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
                item.setVisible(true);

                MenuItem itemVenta = menu.findItem(R.id.nav_list);
                itemVenta.setIcon(R.drawable.ic_seat);
                itemVenta.setTitle("Venta Boletos");

            }else if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")){

                item.setVisible(false);

                MenuItem itemVenta = menu.findItem(R.id.nav_list);
                itemVenta.setIcon(R.drawable.ic_bus);
                itemVenta.setTitle("Embarque");

            }else{
                item.setVisible(false);
            }
            if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_INSP") ||
                    sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")){
                MenuItem item2 = menu.findItem(R.id.nav_carga);
                item2.setVisible(false);

                MenuItem Vip = menu.findItem(R.id.nav_servicio_especial);
                Vip.setVisible(false);
            }

            if(sharedPreferences.getString("VentaVip","NoData").equals("InfinityDev"))
            {
                MenuItem Vip = menu.findItem(R.id.nav_servicio_especial);
                Vip.setVisible(true);
            }

            MenuItem itemVenta = menu.findItem(R.id.nav_enco_carg);
            itemVenta.setVisible(false);

            MenuItem item3 = menu.findItem(R.id.nav_tarifario);
            item3.setVisible(false);

            MenuItem item4 = menu.findItem(R.id.nav_trasbordo);
            item4.setVisible(false);

            MenuItem item6 = menu.findItem(R.id.nav_asignarAsientos);
            item6.setVisible(false);

            MenuItem item7 = menu.findItem(R.id.nav_MataBoleto);
            item7.setVisible(false);

            MenuItem item8 = menu.findItem(R.id.nav_Manifiesto);
            item8.setVisible(false);

            if(sharedPreferences.getString("flagInspeccionVenta", "NoData").equals("true")){

                MenuItem item5 = menu.findItem(R.id.nav_list);
                item5.setEnabled(true);
                item5.setTitle("Inspección");
                /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new InspeccionVentaFragment());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);*/

                /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new InspeccionVentaFragment());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);*/

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new mataBoleto());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);


                MenuItem itemInsp = menu.findItem(R.id.nav_Manifiesto);
                itemInsp.setEnabled(true);
                itemInsp.setVisible(true);

                /*FragmentTransaction ftInsp = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new BoletosInspeccion());
                ftInsp.commit();
                navigationView.setCheckedItem(R.id.nav_list);*/
            }else if (sharedPreferences.getString("Modulo", "nada").equals("ANDROID_INSP")){

                /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new ItinerarioFragment());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);*/
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new ItinerarioInspectorRuta());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);

            }else if (sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")){

                /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new EmbarqueLeerBoletos());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);*/
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new ItinerarioFragment());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);

            }else {
                //System.out.println("app bar");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_base, new BoleteroViajeFragment());
                ft.commit();
                navigationView.setCheckedItem(R.id.nav_list);
            }

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = item.getItemId();
        String puestoUsuario = sharedPreferences.getString("Modulo", "nada");
        if (id == R.id.nav_list) {
            if(puestoUsuario.equals("ANFITRION ESTANDAR")|| puestoUsuario.equals("CONDUCTOR ESTANDAR")
                    ){
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new CargaExtraFragment()).commit();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new VentaBoletosFragment()).commit();

            }else if(puestoUsuario.equals("ANDROID_INSP")){
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new ItinerarioFragment()).commit();
                if(sharedPreferences.getString("flagInspeccionVenta", "NoData").equals("true")) {
                    //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new InspeccionVentaFragment()).commit();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new mataBoleto()).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new ItinerarioInspectorRuta()).commit();
                }

            }else if(puestoUsuario.equals("ANDROID_VENTAS")){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new BoleteroViajeFragment()).commit();

            }else if(puestoUsuario.equals("ANDROID_CONTROL")){
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new EmbarqueLeerBoletos()).commit();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new ItinerarioFragment()).commit();
            }
        }else if (id == R.id.nav_tarifario) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new TarifarioFragment()).commit();

        }else if (id == R.id.nav_enco_carg) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new carga_encomie()).commit();

        }else if (id == R.id.sel_caja) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new SelecionCajaCarga()).commit();

        }else if (id == R.id.sel_agen) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new SelecAgen()).commit();

        }else if (id == R.id.vent_carg_enco) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new Venta_Carga_enco()).commit();

        }else if (id == R.id.vent_carg_enco2) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new venta_carga_enco2()).commit();

        }else if (id == R.id.nav_liquidacion) {

            if(puestoUsuario.equals("ANDROID_VENTAS")){
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new BoleteroPreLiquidacionFragment()).commit();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new Preliquidacion_AgenFragment()).commit();
            }else{
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new PreLiquidacionFragment()).commit();

            }

        }else if (id == R.id.nav_carga) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new CargaExtraFragment()).commit();

        }else if (id == R.id.nav_camara) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new camaratest()).commit();

        }else if (id == R.id.nav_trasbordo){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new ItinerarioFragment()).commit();

        }else if (id == R.id.nav_asignarAsientos){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new AsignarAsientoFragment()).commit();

        }else if (id == R.id.nav_ReporteControlador){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base,new ReporteControladorFragment()).commit();
        }else if (id == R.id.nav_MataBoleto){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base,new mataBoleto()).commit();
        }else if (id == R.id.nav_Manifiesto){
            if(sharedPreferences.getString("flagInspeccionVenta","NoData").equals("true"))
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new BoletosInspeccion()).commit();
            }else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new ManifiestoPasajeros()).commit();
            }
        }else if (id == R.id.nav_repoCantinsp) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base, new Reporte_inspeccion()).commit();

        }else if(id==R.id.nav_servicio_especial)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base,new ServicioExpressFragment()).commit();
        }else if (id == R.id.nav_close) {
            if(puestoUsuario.equals("ANFITRION ESTANDAR") ||
                    puestoUsuario.equals("ANDROID_VENTAS") ||
                    puestoUsuario.equals("CONDUCTOR ESTANDAR")){

                if(puestoUsuario.equals("ANFITRION ESTANDAR") ||
                        puestoUsuario.equals("CONDUCTOR ESTANDAR")){
                    ventaBlt = new DatabaseBoletos(getApplicationContext());
                    //modi_jairo
                    sqLiteDatabase = ventaBlt.getWritableDatabase();
                    sqLiteDatabase.execSQL("DELETE FROM Manifiesto");
                    ActualizacionAsientosService.actualizarAsientos(getApplicationContext(), false);

                }
//                stopBoletoService();
                JSONObject jsonObject = generarJSON();
                Log.d("json_cerrar", jsonObject.toString());

                final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String ws_postVenta = getString(R.string.ws_ruta) + "CierreItinerario";

                MyJSONArrayRequest jsonArrayRequestActualizarCorrelativos = new MyJSONArrayRequest(Request.Method.PUT, ws_postVenta, jsonObject,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length() > 0) {

                                    JSONObject info;
                                    try {

                                        info = response.getJSONObject(0);
                                        //String respuesta = info.getString("Respuesta");
                                        //Log.d("respuestaWScorrelativo", respuesta);
                                        //Log.d("respuestaWScorrelativo", respuesta);
                                        //respuestaWS = info.getString("Respuesta");
                                        RespuestaWS = info.getString("Respuesta");
                                        UserCierra = 1;
                                        breakTime();
                                        LiberaToken();
                                        breakTime();
                                        logout();
                                        Intent intent = new Intent(AppSideBarActivity.this, LoginActivity.class);
                                        finish();
                                        startActivity(intent);
                                        //Log.d("Request", RespuestaWS);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error en la ws ActualizarCorrelativo. Actualizar los correlativos de forma manual.", Toast.LENGTH_LONG).show();
                        //errorWS(queue, error);
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
                jsonArrayRequestActualizarCorrelativos.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonArrayRequestActualizarCorrelativos);
                breakTime();
            }
            else{
                if(puestoUsuario.equals("ANDROID_CONTROL")){
                    ventaBlt = new DatabaseBoletos(getApplicationContext());
                    sqLiteDatabase = ventaBlt.getWritableDatabase();
                    sqLiteDatabase.execSQL("DELETE FROM Asignacion");
                }
                breakTime();
                LiberaToken();
                breakTime();
                logout();
                stopBoletoService();
                Intent intent = new Intent(AppSideBarActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public  void logout(){
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }


    public JSONObject generarJSON(){

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONObject correlativoViajeBlt = new JSONObject();
        JSONObject correlativoViajeFac = new JSONObject();
        JSONObject correlativoCargaBlt = new JSONObject();
        JSONObject correlativoCargaFac = new JSONObject();
        JSONObject correlativoViajeBlt02 = new JSONObject();
        JSONObject correlativoViajeFac02 = new JSONObject();
        JSONObject correlativoCargaBlt02 = new JSONObject();
        JSONObject correlativoCargaFac02 = new JSONObject();
        JSONObject itinerario = new JSONObject();
        JSONArray correlativos =  new JSONArray();

        JSONObject actualizarCorrelativos = new JSONObject();

        try {
            if(sharedpreferences.getString("Modulo", "nada").equals("ANFITRION ESTANDAR") ||
            sharedpreferences.getString("Modulo", "nada").equals("CONDUCTOR ESTANDAR")
            ){
                final Cursor c1 = UltimaVentaPOS(sharedpreferences.getString("anf_codigoEmpresa", "NoData"),sharedpreferences.getString("anf_numSerieBltViaje", "NoData"),"viaje");
                if (c1.getCount() > 0) {
                    while(c1.moveToNext()){
                        correlativoViajeBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                        correlativoViajeBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBltViaje", "NoData"));
                        correlativoViajeBlt.put("Serie", sharedpreferences.getString("anf_numSerieBltViaje", "NoData"));
                        correlativoViajeBlt.put("UltimoCorrelativo", c1.getString(c1.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                    correlativoViajeBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBltViaje", "NoData"));
                    correlativoViajeBlt.put("Serie", sharedpreferences.getString("anf_numSerieBltViaje", "NoData"));
                    correlativoViajeBlt.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoBltViaje", "NoData"));
                }

                /*correlativoViajeBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                correlativoViajeBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBltViaje", "NoData"));
                correlativoViajeBlt.put("Serie", sharedpreferences.getString("anf_numSerieBltViaje", "NoData"));
                correlativoViajeBlt.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoBltViaje", "NoData"));*/

                final Cursor c2 = UltimaVentaPOS(sharedpreferences.getString("anf_codigoEmpresa", "NoData"),sharedpreferences.getString("anf_numSerieFacViaje", "NoData"),"viaje");
                if (c2.getCount() > 0) {
                    while(c2.moveToNext()){
                        correlativoViajeFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                        correlativoViajeFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBltViaje", "NoData"));
                        correlativoViajeFac.put("Serie", sharedpreferences.getString("anf_numSerieFacViaje", "NoData"));
                        correlativoViajeFac.put("UltimoCorrelativo", c2.getString(c2.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                    correlativoViajeFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoFacViaje", "NoData"));
                    correlativoViajeFac.put("Serie", sharedpreferences.getString("anf_numSerieFacViaje", "NoData"));
                    correlativoViajeFac.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoFacViaje", "NoData"));
                }
/*
                correlativoViajeFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                correlativoViajeFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoFacViaje", "NoData"));
                correlativoViajeFac.put("Serie", sharedpreferences.getString("anf_numSerieFacViaje", "NoData"));
                correlativoViajeFac.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoFacViaje", "NoData"));*/
                final Cursor c3 = UltimaVentaPOS(sharedpreferences.getString("anf_codigoEmpresa", "NoData"),sharedpreferences.getString("anf_numSerieBolCarga", "NoData"),"carga");
                if (c3.getCount() > 0) {
                    while(c3.moveToNext()){
                        correlativoCargaBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                        correlativoCargaBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBolCarga", "NoData"));
                        correlativoCargaBlt.put("Serie", sharedpreferences.getString("anf_numSerieBolCarga", "NoData"));
                        correlativoCargaBlt.put("UltimoCorrelativo", c3.getString(c3.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoCargaBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                    correlativoCargaBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBolCarga", "NoData"));
                    correlativoCargaBlt.put("Serie", sharedpreferences.getString("anf_numSerieBolCarga", "NoData"));
                    correlativoCargaBlt.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoBolCarga", "NoData"));
                }

                /*correlativoCargaBlt.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                correlativoCargaBlt.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoBolCarga", "NoData"));
                correlativoCargaBlt.put("Serie", sharedpreferences.getString("anf_numSerieBolCarga", "NoData"));
                correlativoCargaBlt.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoBolCarga", "NoData"));*/

                final Cursor c4 = UltimaVentaPOS(sharedpreferences.getString("anf_codigoEmpresa", "NoData"),sharedpreferences.getString("anf_numSerieFacCarga", "NoData"),"carga");
                if (c4.getCount() > 0) {
                    while(c4.moveToNext()){
                        correlativoCargaFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                        correlativoCargaFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoFacCarga", "NoData"));
                        correlativoCargaFac.put("Serie", sharedpreferences.getString("anf_numSerieFacCarga", "NoData"));
                        correlativoCargaFac.put("UltimoCorrelativo", c4.getString(c4.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoCargaFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                    correlativoCargaFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoFacCarga", "NoData"));
                    correlativoCargaFac.put("Serie", sharedpreferences.getString("anf_numSerieFacCarga", "NoData"));
                    correlativoCargaFac.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoFacCarga", "NoData"));
                }
                /*correlativoCargaFac.put("Empresa", sharedpreferences.getString("anf_codigoEmpresa", "NoData"));
                correlativoCargaFac.put("TipoDocumento", sharedpreferences.getString("anf_tipoDocumentoFacCarga", "NoData"));
                correlativoCargaFac.put("Serie", sharedpreferences.getString("anf_numSerieFacCarga", "NoData"));
                correlativoCargaFac.put("UltimoCorrelativo", sharedpreferences.getString("anf_correlativoFacCarga", "NoData"));*/

                correlativos.put(correlativoViajeBlt);
                correlativos.put(correlativoViajeFac);
                correlativos.put(correlativoCargaBlt);
                correlativos.put(correlativoCargaFac);

                itinerario.put("FechaViaje", sharedpreferences.getString("anf_fechaProgramacion", "NoData"));
                itinerario.put("NuSecu", sharedpreferences.getString("anf_secuencia", "NoData"));
                itinerario.put("Rumbo", sharedpreferences.getString("anf_rumbo", "NoData"));

                actualizarCorrelativos.put("Correlativo", correlativos);
                actualizarCorrelativos.put("Itinerario", itinerario);

            } else if(sharedpreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
                /*-----------------------------------------------------------------------*/

                final Cursor c1 = UltimaVentaPOS("01",sharedpreferences.getString("guardar_serieViajeBLT01", "NoData"),"viaje");
                if (c1.getCount() > 0) {
                    while(c1.moveToNext()){
                        correlativoViajeBlt.put("Empresa", "01");
                        correlativoViajeBlt.put("TipoDocumento", "BLT");
                        correlativoViajeBlt.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT01", ""));
                        correlativoViajeBlt.put("UltimoCorrelativo", c1.getString(c1.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeBlt.put("Empresa", "01");
                    correlativoViajeBlt.put("TipoDocumento", "BLT");
                    correlativoViajeBlt.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT01", ""));
                    correlativoViajeBlt.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeBLT01", ""));
                }
                Log.d("CORRELATIVO_1",correlativoViajeBlt.toString());


                /*correlativoViajeBlt.put("Empresa", "01");
                correlativoViajeBlt.put("TipoDocumento", "BLT");
                correlativoViajeBlt.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT01", ""));
                correlativoViajeBlt.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeBLT01", ""));*/

                final Cursor c2 = UltimaVentaPOS("01",sharedpreferences.getString("guardar_serieViajeFAC01", "NoData"),"viaje");
                if (c2.getCount() > 0) {
                    while(c2.moveToNext()){
                        correlativoViajeFac.put("Empresa", "01");
                        correlativoViajeFac.put("TipoDocumento", "BLT");
                        correlativoViajeFac.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC01", ""));
                        correlativoViajeFac.put("UltimoCorrelativo", c2.getString(c2.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeFac.put("Empresa", "01");
                    correlativoViajeFac.put("TipoDocumento", "BLT");
                    correlativoViajeFac.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC01", ""));
                    correlativoViajeFac.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeFAC01", ""));

                }
                Log.d("CORRELATIVO_2",correlativoViajeFac.toString());
                /*correlativoViajeFac.put("Empresa", "01");
                correlativoViajeFac.put("TipoDocumento", "BLT");
                correlativoViajeFac.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC01", ""));
                correlativoViajeFac.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeFAC01", ""));*/

                final Cursor c3 = UltimaVentaPOS("01",sharedpreferences.getString("guardar_serieCargaBOL01", "NoData"),"carga");
                if (c3.getCount() > 0) {
                    while(c3.moveToNext()){
                        correlativoCargaBlt.put("Empresa", "01");
                        correlativoCargaBlt.put("TipoDocumento", "BOL");
                        correlativoCargaBlt.put("Serie", sharedpreferences.getString("guardar_serieCargaBOL01", ""));
                        correlativoCargaBlt.put("UltimoCorrelativo", c3.getString(c3.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoCargaBlt.put("Empresa", "01");
                    correlativoCargaBlt.put("TipoDocumento", "BOL");
                    correlativoCargaBlt.put("Serie", sharedpreferences.getString("guardar_serieCargaBOL01", ""));
                    correlativoCargaBlt.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaBOL01", ""));
                }
                Log.d("CORRELATIVO_3",correlativoCargaBlt.toString());
                /*correlativoCargaBlt.put("Empresa", "01");
                correlativoCargaBlt.put("TipoDocumento", "BOL");
                correlativoCargaBlt.put("Serie", sharedpreferences.getString("guardar_serieCargaBOL01", ""));
                correlativoCargaBlt.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaBOL01", ""));*/

                final Cursor c4 = UltimaVentaPOS("01",sharedpreferences.getString("guardar_serieCargaFAC01", "NoData"),"carga");
                if (c4.getCount() > 0) {
                    while(c4.moveToNext()){
                        correlativoCargaFac.put("Empresa", "01");
                        correlativoCargaFac.put("TipoDocumento", "FAC");
                        correlativoCargaFac.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC01", ""));
                        correlativoCargaFac.put("UltimoCorrelativo", c4.getString(c4.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoCargaFac.put("Empresa", "01");
                    correlativoCargaFac.put("TipoDocumento", "FAC");
                    correlativoCargaFac.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC01", ""));
                    correlativoCargaFac.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaFAC01", ""));
                }
                Log.d("serie",sharedpreferences.getString("guardar_serieCargaFAC01", "NoData"));
                Log.d("CORRELATIVO_4",correlativoCargaFac.toString());
               /* correlativoCargaFac.put("Empresa", "01");
                correlativoCargaFac.put("TipoDocumento", "FAC");
                correlativoCargaFac.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC01", ""));
                correlativoCargaFac.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaFAC01", ""));*/
                final Cursor c5 = UltimaVentaPOS("02",sharedpreferences.getString("guardar_serieViajeBLT02", "NoData"),"viaje");
                if (c5.getCount() > 0) {
                    while(c5.moveToNext()){
                        correlativoViajeBlt02.put("Empresa", "02");
                        correlativoViajeBlt02.put("TipoDocumento", "BLT");
                        correlativoViajeBlt02.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT02", "NoData"));
                        correlativoViajeBlt02.put("UltimoCorrelativo", c5.getString(c5.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeBlt02.put("Empresa", "02");
                    correlativoViajeBlt02.put("TipoDocumento", "BLT");
                    correlativoViajeBlt02.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT02", ""));
                    correlativoViajeBlt02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeBLT02", ""));
                }
                Log.d("SERIE_02",sharedpreferences.getString("guardar_serieViajeBLT02", "NoData"));
                Log.d("EMPRESA","02");
                Log.d("CORRELATIVO_5",correlativoViajeBlt02.toString());
               /* correlativoViajeBlt02.put("Empresa", "02");
                correlativoViajeBlt02.put("TipoDocumento", "BLT");
                correlativoViajeBlt02.put("Serie", sharedpreferences.getString("guardar_serieViajeBLT02", ""));
                correlativoViajeBlt02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeBLT02", ""));*/
                final Cursor c6 = UltimaVentaPOS("02",sharedpreferences.getString("guardar_serieViajeFAC02", "NoData"),"viaje");
                if (c6.getCount() > 0) {
                    while(c6.moveToNext()){
                        correlativoViajeFac02.put("Empresa", "02");
                        correlativoViajeFac02.put("TipoDocumento", "BLT");
                        correlativoViajeFac02.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC02", ""));
                        correlativoViajeFac02.put("UltimoCorrelativo", c6.getString(c6.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoViajeFac02.put("Empresa", "02");
                    correlativoViajeFac02.put("TipoDocumento", "BLT");
                    correlativoViajeFac02.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC02", ""));
                    correlativoViajeFac02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeFAC02", ""));
                }
                Log.d("SERIE_FAC_VIAJE",sharedpreferences.getString("guardar_correlativoViajeFAC02", "NoData"));
                Log.d("CORRELATIVO_6",correlativoViajeFac02.toString());
                /*correlativoViajeFac02.put("Empresa", "02");
                correlativoViajeFac02.put("TipoDocumento", "BLT");
                correlativoViajeFac02.put("Serie", sharedpreferences.getString("guardar_serieViajeFAC02", ""));
                correlativoViajeFac02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoViajeFAC02", ""));*/

                final Cursor c7 = UltimaVentaPOS("02",sharedpreferences.getString("guardar_serieCargaBOL02", "NoData"),"carga");
                if (c7.getCount() > 0) {
                    while(c7.moveToNext()){
                        correlativoCargaBlt02.put("Empresa", "02");
                        correlativoCargaBlt02.put("TipoDocumento", "BOL");
                        correlativoCargaBlt02.put("Serie", sharedpreferences.getString("guardar_serieCargaBOL02", ""));
                        correlativoCargaBlt02.put("UltimoCorrelativo", c7.getString(c7.getColumnIndex("NU_ULTI_VENT")));
                    }
                }
                Log.d("carga_02", sharedpreferences.getString("guardar_serieCargaBOL02", ""));
                Log.d("CORRELATIVO_7",correlativoCargaBlt02.toString());
                /*correlativoCargaBlt02.put("Empresa", "02");
                correlativoCargaBlt02.put("TipoDocumento", "BOL");
                correlativoCargaBlt02.put("Serie", sharedpreferences.getString("guardar_serieCargaBOL02", ""));
                correlativoCargaBlt02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaBOL02", ""));*/

                final Cursor c8 = UltimaVentaPOS("02",sharedpreferences.getString("guardar_serieCargaFAC02", "NoData"),"carga");
                if (c8.getCount() > 0) {
                    while(c8.moveToNext()){
                        correlativoCargaFac02.put("Empresa", "02");
                        correlativoCargaFac02.put("TipoDocumento", "FAC");
                        correlativoCargaFac02.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC02", ""));
                        correlativoCargaFac02.put("UltimoCorrelativo", c8.getString(c8.getColumnIndex("NU_ULTI_VENT")));
                    }
                }else{
                    correlativoCargaFac02.put("Empresa", "02");
                    correlativoCargaFac02.put("TipoDocumento", "FAC");
                    correlativoCargaFac02.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC02", ""));
                    correlativoCargaFac02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaFAC02", ""));
                }
                Log.d("CORRELATIVO_8",sharedpreferences.getString("guardar_serieCargaFAC02", ""));
                Log.d("CORRELATIVO_8",correlativoCargaFac02.toString());
                /*correlativoCargaFac02.put("Empresa", "02");
                correlativoCargaFac02.put("TipoDocumento", "FAC");
                correlativoCargaFac02.put("Serie", sharedpreferences.getString("guardar_serieCargaFAC02", ""));
                correlativoCargaFac02.put("UltimoCorrelativo", sharedpreferences.getString("guardar_correlativoCargaFAC02", ""));*/

                correlativos.put(correlativoViajeBlt);
                correlativos.put(correlativoViajeFac);
                correlativos.put(correlativoCargaBlt);
                correlativos.put(correlativoCargaFac);
                correlativos.put(correlativoViajeBlt02);
                correlativos.put(correlativoViajeFac02);
                correlativos.put(correlativoCargaBlt02);
                correlativos.put(correlativoCargaFac02);

                itinerario.put("FechaViaje", "");
                itinerario.put("NuSecu", "");
                itinerario.put("Rumbo", sharedpreferences.getString("guardar_rumbo", "NoData"));

                actualizarCorrelativos.put("Correlativo", correlativos);
                actualizarCorrelativos.put("Itinerario", itinerario);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return actualizarCorrelativos;
    }
    public JSONObject GeneraCierre(){

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONObject CierreToken = new JSONObject();
        try {
            CierreToken.put("Key", sharedpreferences.getString("KEY", "NoData"));
            //Log.d("keyAliberar",CierreToken.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return CierreToken;
    }

    public String LiberaToken()
    {
        JSONObject JsonToken = new JSONObject();
        JsonToken = GeneraCierre();
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String ws_postVenta = getString(R.string.ws_ruta) + "LiberaKey";

        MyJSONArrayRequest jsonArrayCierreSession = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, JsonToken,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {

                            JSONObject info;
                            try {
                                info = response.getJSONObject(0);
                                RespuestaWS_Session = info.getString("Respuesta");
                                Log.d("LIBERA_TOKEN", RespuestaWS_Session);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error en la ws ActualizarCorrelativo. Actualizar los correlativos de forma manual.", Toast.LENGTH_LONG).show();
                //errorWS(queue, error);
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
        return RespuestaWS_Session;
    }

    /**
     * Mapea el error del WS y muestra una vista de error.
     * @param queue Contiene el queue del request.
     * @param error Determina el tipo de error de la Web Service.
     */
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

        //Intent intent = new Intent(AppSideBarActivity.this, ErrorActivity.class);
        //AppSideBarActivity.this.finish();
        //startActivity(intent);

    }

    public Cursor UltimaVentaPOS(final String Empresa,final String Serie,final String Tipo)
    {
            ventaBlt = new DatabaseBoletos(getApplicationContext());
            sqLiteDatabase = ventaBlt.getWritableDatabase();
            final Cursor cursor = sqLiteDatabase.rawQuery("SELECT CO_EMPR,substr(nu_docu,1,4) SERIE,MAX(CAST(substr(nu_docu,6,length(nu_docu)) AS INTEGER)) NU_ULTI_VENT from VentaBoletos where substr(nu_docu,1,4) in ('"+Serie+"') AND CO_EMPR in ('"+Empresa+"') AND tipo='"+Tipo+"' GROUP BY CO_EMPR,substr(nu_docu,1,4)",new String[]{});
            return cursor;

    }
    public void stopBoletoService() {
        Log.d("ServicioStop","si");
        BoletoService.startService(getApplicationContext(), false);
    }

}
