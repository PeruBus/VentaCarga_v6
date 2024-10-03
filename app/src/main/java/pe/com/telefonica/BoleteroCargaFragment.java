package pe.com.telefonica.soyuz;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.generarCodigoQR;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class BoleteroCargaFragment extends Fragment {


    private DatabaseBoletos ventaBlt;


    private SQLiteDatabase sqLiteDatabase;


    private SharedPreferences sharedPreferences;


    private Gson gson;


    JSONArray getCorrelativo = null;


    String empresaSeleccionada = "";


    Boolean ventaDone = false;


    String correlativoSeleccionado = "";


    String serieSeleccionado = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        gson = new Gson();


        parent.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        View inflate = inflater.inflate(R.layout.boletero_venta_carga, parent, false);
        return inflate;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Elementos para venta de boletos de viaje */
        final Context context_boleto = view.getContext();

        final EditText editText_empresa = view.findViewById(R.id.editText_empresa);
        final EditText editText_rumbo = view.findViewById(R.id.editText_rumbo);
        final EditText editText_origenCarga = view.findViewById(R.id.editText_origenCarga);
        final EditText editText_destinoCarga = view.findViewById(R.id.editText_destinoCarga);
        final EditText editText_dniCarga = view.findViewById(R.id.editText_dniCarga);
        final EditText editText_tarifaBase = view.findViewById(R.id.editText_tarifaBase);
        final EditText editText_tarifaCarga = view.findViewById(R.id.editText_tarifaCarga);
        final EditText editText_cantidad = view.findViewById(R.id.editText_cantidad);

        final Spinner spinner_tipoProducto = view.findViewById(R.id.spinner_tipoProducto);

        final Button button_imprimirBoletoCarga = view.findViewById(R.id.button_imprimirBoletoCarga);

        editText_empresa.setText(sharedPreferences.getString("guardar_nombreEmpresa","NoData"));
        editText_rumbo.setText(sharedPreferences.getString("guardar_rumbo","NoData"));
        editText_origenCarga.setText(sharedPreferences.getString("guardar_nombreOrigen","NoData"));
        editText_destinoCarga.setText(sharedPreferences.getString("guardar_nombreDestino","NoData"));
        editText_dniCarga.setText(sharedPreferences.getString("guardar_numeroDocumento","NoData"));
        editText_tarifaBase.setText("2.00");



        try {
            getCorrelativo = new JSONArray(sharedPreferences.getString("bol_getCorrelativo", "NoData"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Correlativos.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }


        final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
        final ArrayList<String> lista_idProductos = new ArrayList<>();
        final ArrayList<String> lista_nombreProductos = new ArrayList<>();
        /* ----------------------------------------- */


        for (int i = 0; i < lista_productos.size(); i++) {

            String[] dataProductos = lista_productos.get(i).split("-");
            lista_idProductos.add(dataProductos[0]);
            lista_nombreProductos.add(dataProductos[1]);
        }

        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(context_boleto, android.R.layout.simple_spinner_item, lista_nombreProductos);
        spinner_tipoProducto.setAdapter(adapter_spinner);

        spinner_tipoProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                String idProducto = lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                String nombreProducto = lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                guardarDataMemoria("guardar_idProducto", idProducto, getActivity());
                guardarDataMemoria("guardar_nombreProducto", nombreProducto, getActivity());


                if(sharedPreferences.getString("puestoUsuario", "nada").equals("ANFITRION ESTANDAR")){


                    final ArrayList<String> lista_tarifasCarga = getArray(sharedPreferences, gson, "anf_jsonTarifasCarga");

                    for (int i = 0; i < lista_tarifasCarga.size(); i++) {
                        String[] data_tarifaCarga = lista_tarifasCarga.get(i).split("-");

                        if (data_tarifaCarga[0].equals(idProducto) &&
                                data_tarifaCarga[1].equals(sharedPreferences.getString("guardar_origen", "NoData")) &&
                                data_tarifaCarga[2].equals(sharedPreferences.getString("guardar_destino", "NoData"))) {

                            editText_tarifaBase.setText(String.format("%.2f", Float.valueOf(data_tarifaCarga[3])));
                            break;
                        } else {
                            editText_tarifaBase.setText("0.00");
                        }
                        /* ----------------------------------------- */
                    }
                    /* ----------------------------------------- */
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /* ----------------------------------------- */


        button_imprimirBoletoCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText_tarifaCarga.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), "El campo de tarifa adicional no puede estar vacío.", Toast.LENGTH_SHORT).show();

                } else if (Float.valueOf(editText_tarifaCarga.getText().toString()) < Float.valueOf(editText_tarifaBase.getText().toString())) {

                    Toast.makeText(getActivity(), "La tarifa adicional no puede ser menor que la tarifa base.", Toast.LENGTH_SHORT).show();

                }else if (editText_cantidad.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), "El campo cantidad no puede estar vacío.", Toast.LENGTH_SHORT).show();

                } else if (editText_cantidad.getText().toString().split("\\.").length > 1) {

                    Toast.makeText(getActivity(), "La cantidad debe ser un número.", Toast.LENGTH_SHORT).show();

                } else if (Integer.valueOf(editText_cantidad.getText().toString()) == 0) {

                    Toast.makeText(getActivity(), "La cantidad no puede ser menor a 1.", Toast.LENGTH_SHORT).show();

                } else if (Integer.valueOf(editText_cantidad.getText().toString()) > 20) {

                    Toast.makeText(getActivity(), "La cantidad no puede ser mayor a 20.", Toast.LENGTH_SHORT).show();

                } else {

                    button_imprimirBoletoCarga.setEnabled(false);

                    JSONObject info;
                    for (int i = 0; i < getCorrelativo.length(); i++) {
                        try {
                            info = getCorrelativo.getJSONObject(i);
                            if(sharedPreferences.getString("guardar_numeroDocumento","NoData").length() == 8 &&
                                    sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                                    info.getString("DE_GSER").equals("CARGA") &&
                                    info.getString("TI_DOCU").equals("BOL")){
                                serieSeleccionado = info.getString("NUMERO_SERIE");
                                correlativoSeleccionado = Integer.toString(Integer.valueOf(sharedPreferences.getString("guardar_correlativoCargaBOL"+sharedPreferences.getString("guardar_idEmpresa", ""), "NoData")) + 1);
                                guardarDataMemoria("guardar_tipoDocumentoCarga", info.getString("TIPO_DOCUMENTO"), getActivity());
                                guardarDataMemoria("guardar_correlativoCargaBOL"+sharedPreferences.getString("guardar_idEmpresa", ""), correlativoSeleccionado, getActivity());
                            } else if (sharedPreferences.getString("guardar_numeroDocumento","NoData").length() == 11 &&
                                    sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                                    info.getString("DE_GSER").equals("CARGA") &&
                                    info.getString("TI_DOCU").equals("FAC")) {
                                serieSeleccionado = info.getString("NUMERO_SERIE");
                                correlativoSeleccionado = Integer.toString(Integer.valueOf(sharedPreferences.getString("guardar_correlativoCargaFAC"+sharedPreferences.getString("guardar_idEmpresa", ""), "NoData")) + 1);
                                guardarDataMemoria("guardar_tipoDocumentoCarga", info.getString("TIPO_DOCUMENTO"), getActivity());
                                guardarDataMemoria("guardar_correlativoCargaFAC"+sharedPreferences.getString("guardar_idEmpresa", ""), correlativoSeleccionado, getActivity());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Correlativos.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getActivity(), ErrorActivity.class);
                            startActivity(intent);
                        }
                    }
                    String correlativoCompleto = completarCorrelativo(Integer.valueOf(correlativoSeleccionado));
                    guardarDataMemoria("guardar_serie", serieSeleccionado, getActivity());
                    guardarDataMemoria("guardar_correlativo", correlativoSeleccionado, getActivity());
                    final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                    for (int i = 0; i < lista_empresas.size(); i++) {
                        String idEmpresa = lista_empresas.get(i).split("-")[0];
                        if(idEmpresa.equals(sharedPreferences.getString("guardar_idEmpresa", "NoData"))){
                            empresaSeleccionada = lista_empresas.get(i);
                            break;
                        }
                    }
                    float tarifaTotal = Integer.valueOf(editText_cantidad.getText().toString()) * (Float.valueOf(editText_tarifaCarga.getText().toString()));
                    guardarDataMemoria("guardar_tarifaTotal", Float.toString(tarifaTotal), getActivity());
                    guardarDataMemoria("guardar_cantidad", editText_cantidad.getText().toString(), getActivity());
                    final String trama = generarTramaCarga(sharedPreferences, serieSeleccionado, correlativoCompleto, empresaSeleccionada);
                    String[] dataEncriptada = generarCodigoQR(trama, BoleteroCargaFragment.this);
                    guardarCompraCarga(dataEncriptada[0], dataEncriptada[1], correlativoCompleto,
                            button_imprimirBoletoCarga);
                    imprimir_boletasCarga(correlativoCompleto, sharedPreferences.getString("guardar_nombreOrigen", "NoData"),
                            sharedPreferences.getString("guardar_nombreDestino", "NoData"), dataEncriptada[2],
                            sharedPreferences.getString("guardar_nombreProducto", "NoData"), "Carga");
                    BoleteroViajeFragment boleteroViajeFragment = new BoleteroViajeFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, boleteroViajeFragment).commit();
                }
            }
        });
    }


    public void imprimir_boletasCarga(String correlativoCompleto, String nombreOrigen, String  nombreDestino, String ted, String tipoProducto, String tipoBoleta) {
        Boleta boleta = new Boleta(tipoBoleta);
        boleta.setOrigen(nombreOrigen);
        boleta.setDestino(nombreDestino);
        boleta.setTarifa(sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
        boleta.setDNI(sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
        boleta.setSeriePasaje(sharedPreferences.getString("guardar_serieViaje", "NoData"));
        boleta.setCorrelativoPasaje(sharedPreferences.getString("guardar_correlativoViajeCompleto", "NoData"));
        boleta.setSerieCarga(sharedPreferences.getString("guardar_serie", "NoData"));
        boleta.setCorrelativoCarga(correlativoCompleto);
        boleta.setEmpresa(empresaSeleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("guardar_fechaVentaCarga", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("guardar_horaVentaCarga", "NoData"));
        boleta.setTipoProducto(tipoProducto);
        boleta.setCantidad(sharedPreferences.getString("guardar_cantidad", "NoData"));
        boleta.setNombreAnfitrion(sharedPreferences.getString("nombreEmpleado", "NoData"));
        boleta.setNumAsiento(sharedPreferences.getString("guardar_numAsientoVendido", "-"));
        boleta.setNombreCliente(sharedPreferences.getString("guardar_nombreCliente", "-"));
        boleta.SetPrueba(getString(R.string.ws_ticket));
        try {
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getContext());
            IPrinter printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            printer.printStr(boleta.getVoucher(), null);
            printer.printBitmap(boleta.getQRBitmap(ted));
            printer.printStr(boleta.margenFinal(), null);
            int iRetError = printer.start();
            if (iRetError != 0x00) {
                if (iRetError == 0x02) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }
    }
    public Boolean[] guardarCompraCarga(String xml64, String ted64, final String correlativoCompleto,
                                        final Button button_imprimirBoletoCarga) {
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Boolean[] respuesta = new Boolean[1];
        respuesta[0] = true;
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        guardarDataMemoria("guardar_fechaVentaCarga", fechaVenta, getActivity());
        final String horaVenta = new SimpleDateFormat("hh:mm").format(date);
        guardarDataMemoria("guardar_horaVentaCarga", horaVenta, getActivity());
        String strDiaFormat = "dd";
        DateFormat diaFormat = new SimpleDateFormat(strDiaFormat);
        String diaSemana = diaFormat.format(date);
        final JSONObject jsonObject = generarJSONCarga(fechaVenta, diaSemana, xml64, ted64, serieSeleccionado, correlativoCompleto, correlativoSeleccionado);
        String ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            JSONObject info;
                            try {
                                info = response.getJSONObject(0);
                                if (info.getString("Respuesta").equals("GUARDADO")) {
                                    ContentValues cv = new ContentValues();
                                    cv.put("data_boleto", jsonObject.toString());
                                    cv.put("estado", "guardado");
                                    cv.put("tipo", "carga");
                                    cv.put("liberado", "No");
                                    if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
                                        cv.put("puesto", "boletero");
                                    }else{
                                        cv.put("puesto", "anfitrion");
                                    }
                                    Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
                                    respuesta[0] = true;
                                    button_imprimirBoletoCarga.setEnabled(true);
                                }else{
                                    Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "Error en la ws SetBoletoCarga. No se pudo guardar el boleto de carga en la ws.", Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();
                ContentValues cv = new ContentValues();
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
                startBoletoService();
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
        return respuesta;
    }
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    public JSONObject generarJSONCarga(String fechaVenta, String diaSemana, String xml64,
                                       String ted64, String serieSeleccionado, String correlativoCompleto, String correlativoSeleccionado) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("guardar_idEmpresa", "NoData"));
            jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad", "NoData"));
            jsonObject.put("Agencia", sharedPreferences.getString("guardar_agencia", "NoData"));
            jsonObject.put("TipoDocuCarga", sharedPreferences.getString("guardar_tipoDocumentoCarga", "NoData"));
            jsonObject.put("SerieCorrelativo", serieSeleccionado + "-" + correlativoCompleto);
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("Rumbo", sharedPreferences.getString("guardar_rumbo", "NoData"));
            jsonObject.put("Origen", sharedPreferences.getString("guardar_idOrigen", "NoData"));
            jsonObject.put("Destino", sharedPreferences.getString("guardar_idDestino", "NoData"));
            jsonObject.put("NuSecu", "");
            jsonObject.put("NumeroDia", diaSemana);
            jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
            jsonObject.put("RUC", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
            jsonObject.put("NombreCliente", "");
            jsonObject.put("TipoServicio", sharedPreferences.getString("guardar_tipoServicio", "NoData"));
            jsonObject.put("NumeroAsiento", "");
            jsonObject.put("FechaViajeItinerario", "");
            jsonObject.put("HoraViaje", "");
            jsonObject.put("ImporteTotal", sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
            jsonObject.put("Observacion", "");
            jsonObject.put("CodigoUsuario", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("NuDocuBoletoViaje", sharedPreferences.getString("guardar_serieViaje", "NoData") + "-" + sharedPreferences.getString("guardar_correlativoViajeCompleto", "NoData"));
            jsonObject.put("TipoDocumentoBoletoViaje", sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData"));
            jsonObject.put("Correlativo", correlativoSeleccionado);
            jsonObject.put("Producto", sharedPreferences.getString("guardar_idProducto", "NoData"));
            jsonObject.put("Cantidad", sharedPreferences.getString("guardar_cantidad", "NoData"));
            jsonObject.put("Caja", sharedPreferences.getString("guardar_caja", "NoData"));
            jsonObject.put("TipoVenta", "Boletero");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para boleto de carga.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }
        return jsonObject;
    }
    public String generarTramaCarga(SharedPreferences sharedPreferences, String numSerieCarga, String numCorrelativoCargaCompleto, String empresaTrama) {
        String[] empresaSeleccionada = empresaTrama.split("-");
        numCorrelativoCargaCompleto = numCorrelativoCargaCompleto.substring(2);
        String tipoDocumento = "";
        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 11) {
            tipoDocumento = "01";
        } else {
            tipoDocumento = "03";
        }
        String tipoDocumentoCliente = "";
        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 11) {
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
        String nombreCliente = "";
        if (sharedPreferences.getString("guardar_nombreCliente", "NoData").equals("")) {
            nombreCliente = "-";
        } else {
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
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
        Date date = new Date();
        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";
        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);
        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        double montoTotal = Float.valueOf(sharedPreferences.getString("guardar_tarifaTotal", "NoData"));
        double montoSinIGV = (montoTotal * 100) / 118;
        double montoSinIGVRedondeado = Math.rint(montoSinIGV * 100) / 100;
        double montoIGV = montoTotal - montoSinIGVRedondeado;
        String numeroFloat = String.format("%.2f",montoTotal);
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        String tramaCarga = "A;Serie;;" + numSerieCarga + "\n" +
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
                "E;DescripcionAdicSunat;35;-\n";
        return tramaCarga;
    }
}
