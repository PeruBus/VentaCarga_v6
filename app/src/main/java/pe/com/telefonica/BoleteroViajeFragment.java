package pe.com.telefonica.soyuz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.Printer;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.generarCodigoQR;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class BoleteroViajeFragment extends Fragment {
    String KeyTTDOSE_CORR="";
    String CorrelativoValida="";
    int FlagValidaButton=0;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    JSONArray getCorrelativo = null;
    JSONArray getServicio = null;
    JSONArray getTramos = null;
    JSONArray getTarifa = null;
    String empresaSeleccionada = "";
    String idEmpresa = "";
    String idDestino = "";
    String idServicio = "";
    String rumbo = "";
    String idOrigen = "";
    IDAL dal;
    IPrinter printer;
    Boolean ventaDone = false;
    String correlativoSeleccionado = "";
    String serieSeleccionado = "";
    Button button_imprimirBoleto;
    EditText editText_dni;
    ProgressDialog progressDialog;
    Spinner spinner_destino;
    ArrayList<String> lista_empresas;
    EditText editText_tarifa;
    EditText editText_tarifaAdicional;
    Spinner Spinner_TTTIPO_DOCU;
    EditText ET_RUC_CLIENTE;
    EditText ET_RASON_SOCIAL;
    TextView TV_textRUC;
    TextView TV_textRasonSocial;
    EditText editText_noClie_NoRuc;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();
        parent.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        View inflate = inflater.inflate(R.layout.boletero_venta_viaje, parent, false);
        return inflate;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        startBoletoService();
        final Context context_boleto = view.getContext();
        FuncionesAuxiliares.setLocale(Locale.US, getResources(), context_boleto);
        try {
            dal = NeptuneLiteUser.getInstance().getDal(context_boleto);
            printer = dal.getPrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();
        }
        final Spinner spinner_empresa = view.findViewById(R.id.spinner_empresa);
        spinner_destino = view.findViewById(R.id.spinner_destino);
        final Spinner spinner_servicio = view.findViewById(R.id.spinner_servicio);
        final Spinner spinner_rumbo = view.findViewById(R.id.spinner_rumbo);
        Spinner_TTTIPO_DOCU  = view.findViewById(R.id.spinner_TipoDocu_bol);
        editText_tarifa = view.findViewById(R.id.editText_tarifa);
        editText_tarifaAdicional = view.findViewById(R.id.editText_tarifaAdicional_bol);
        editText_noClie_NoRuc = view.findViewById(R.id.editText_noClie_NoRuc);
        editText_dni = view.findViewById(R.id.editText_dni);
        ET_RUC_CLIENTE = view.findViewById(R.id.editText_RUC);
        ET_RUC_CLIENTE.setVisibility(View.INVISIBLE);
        ET_RASON_SOCIAL =  view.findViewById(R.id.editText_RasonSocial);
        ET_RASON_SOCIAL.setVisibility(View.INVISIBLE);
        TV_textRUC = view.findViewById(R.id.textRUC);
        TV_textRUC.setVisibility(View.INVISIBLE);
        TV_textRasonSocial = view.findViewById(R.id.textRasonSocial);
        TV_textRasonSocial.setVisibility(View.INVISIBLE);
        button_imprimirBoleto = view.findViewById(R.id.button_imprimirBoleto);
        editText_tarifaAdicional.getText().clear();
        editText_dni.getText().clear();
        FuncionesAuxiliares.CargaTipoDocumento(Spinner_TTTIPO_DOCU,getActivity());
        try {
            getTramos = new JSONArray(sharedPreferences.getString("bol_getTramos", "NoData"));
            getCorrelativo = new JSONArray(sharedPreferences.getString("bol_getCorrelativo", "NoData"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Tramos y Correlativos.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }
        final ArrayList<String> lista_destinos = getArray(sharedPreferences, gson, "json_destinos");
        String agencia = "";
        String unidad = "";
        String caja = "";
        try {
            agencia = getCorrelativo.getJSONObject(0).getString("CO_AGEN");
            unidad = getCorrelativo.getJSONObject(0).getString("CO_UNID");
            caja = getCorrelativo.getJSONObject(0).getString("CO_CAJA");
            guardarDataMemoria("guardar_agencia", agencia, getActivity());
            guardarDataMemoria("guardar_unidad", unidad, getActivity());
            guardarDataMemoria("guardar_caja", caja, getActivity());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Correlativos.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }
        lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
        final List<Spinner_model> TMEMPR_LIST = new ArrayList<>();
        for (int i = 0; i < lista_empresas.size(); i++) {
            String[] dataEmpresa = lista_empresas.get(i).split("-");
            Spinner_model TMEMPR = new Spinner_model(dataEmpresa[0], dataEmpresa[1], "");
            TMEMPR_LIST.add(TMEMPR);
        }
        ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,TMEMPR_LIST);
        spinner_empresa.setAdapter(spinnerArray);
        spinner_empresa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner_model st = (Spinner_model)spinner_empresa.getSelectedItem();
                idEmpresa = st.id;
                guardarDataMemoria("guardar_idEmpresa", st.id, getActivity());
                guardarDataMemoria("guardar_nombreEmpresa", st.name, getActivity());
                try {
                    if(st.id.equals("01")){
                        getServicio = new JSONArray(sharedPreferences.getString("bol_getServicio01", "NoData"));
                        getTarifa = new JSONArray(sharedPreferences.getString("bol_getTarifa01", "NoData"));
                    } else{
                        getServicio = new JSONArray(sharedPreferences.getString("bol_getServicio02", "NoData"));
                        getTarifa = new JSONArray(sharedPreferences.getString("bol_getTarifa02", "NoData"));
                    }
                    final List<Spinner_model> ST_TIPO_SERV = new ArrayList<>();
                    for (int i = 0; i < getServicio.length(); i++) {
                        Spinner_model TI_SERV = new Spinner_model(getServicio.getJSONObject(i).getString("TI_SERV"), getServicio.getJSONObject(i).getString("DE_TIPO_SERV"), "");
                        ST_TIPO_SERV.add(TI_SERV);
                    }
                    ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,ST_TIPO_SERV);
                    spinner_servicio.setAdapter(spinnerArray);
                    final List<Spinner_model> TTRUMB_SPINNER = new ArrayList<>();
                    for (int i = 0; i < getTarifa.length(); i++) {
                        Spinner_model TARI_RUMB = new Spinner_model(getTarifa.getJSONObject(i).getString("CO_RUMB"), getTarifa.getJSONObject(i).getString("CO_RUMB"), "");
                        TTRUMB_SPINNER.add(TARI_RUMB);
                    }
                    ModeloDistinct(TTRUMB_SPINNER);
                    ArrayAdapter adapterRumbo = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,TTRUMB_SPINNER);
                    spinner_rumbo.setAdapter(adapterRumbo);
                    calcularTarifa(editText_tarifa);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Correlativos.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_rumbo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner_model st = (Spinner_model)spinner_rumbo.getSelectedItem();
                rumbo=st.id.trim();
                guardarDataMemoria("guardar_rumbo", rumbo, getActivity());
                try {
                    for (int i = 0; i < getTarifa.length(); i++) {
                        if (getTarifa.getJSONObject(i).getString("CO_RUMB").equals(rumbo)) {
                            for (int j = 0; j < lista_destinos.size(); j++) {
                                String[] dataDestinos = lista_destinos.get(j).split("-");
                                if (dataDestinos[0].equals(getTarifa.getJSONObject(i).getString("CO_DEST_ORIG"))) {
                                    idOrigen = getTarifa.getJSONObject(i).getString("CO_DEST_ORIG");
                                    guardarDataMemoria("guardar_idOrigen", dataDestinos[0], getActivity());
                                    guardarDataMemoria("Origen_Texto", dataDestinos[1], getActivity());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    final List<Spinner_model> LIST_CO_DEST_FINA = new ArrayList<>();
                    for (int i = 0; i < getTarifa.length(); i++) {
                        if (getTarifa.getJSONObject(i).getString("CO_RUMB").equals(rumbo) && getTarifa.getJSONObject(i).getString("TI_SERV").equals(st.id.trim())){
                            for (int j = 0; j < lista_destinos.size(); j++) {
                                String[] dataDestinos = lista_destinos.get(j).split("-");
                                if (dataDestinos[0].equals(getTarifa.getJSONObject(i).getString("CO_DEST_FINA"))) {
                                    Spinner_model CO_DEST_FINA = new Spinner_model(dataDestinos[0], dataDestinos[1], "");
                                    LIST_CO_DEST_FINA.add(CO_DEST_FINA);
                                }
                            }
                        }
                    }
                    ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,LIST_CO_DEST_FINA);
                    spinner_destino.setAdapter(spinnerArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Tarifa.", Toast.LENGTH_LONG).show();
                }
                calcularTarifa(editText_tarifa);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_destino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner_model st = (Spinner_model)spinner_destino.getSelectedItem();
                idDestino = st.id.trim();
                guardarDataMemoria("guardar_idDestino", idDestino, getActivity());
                guardarDataMemoria("Destino_Texto", st.name.trim(), getActivity());
                calcularTarifa(editText_tarifa);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_servicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner_model st = (Spinner_model)spinner_servicio.getSelectedItem();
                idServicio = st.id.trim();
                guardarDataMemoria("guardar_tipoServicio", idServicio, getActivity());
                try {
                    for (int i = 0; i < getTarifa.length(); i++) {
                        if (getTarifa.getJSONObject(i).getString("CO_RUMB").equals(rumbo)) {
                            for (int j = 0; j < lista_destinos.size(); j++) {
                                String[] dataDestinos = lista_destinos.get(j).split("-");
                                if (dataDestinos[0].equals(getTarifa.getJSONObject(i).getString("CO_DEST_ORIG"))) {
                                    idOrigen = getTarifa.getJSONObject(i).getString("CO_DEST_ORIG");
                                    guardarDataMemoria("guardar_idOrigen", dataDestinos[0], getActivity());
                                    guardarDataMemoria("guardar_nombreOrigen", dataDestinos[1], getActivity());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    final List<Spinner_model> LIST_CO_DEST_FINA = new ArrayList<>();
                    for (int i = 0; i < getTarifa.length(); i++) {
                        if (getTarifa.getJSONObject(i).getString("CO_RUMB").equals(rumbo) && getTarifa.getJSONObject(i).getString("TI_SERV").equals(st.id.trim())){
                            for (int j = 0; j < lista_destinos.size(); j++) {
                                String[] dataDestinos = lista_destinos.get(j).split("-");
                                if (dataDestinos[0].equals(getTarifa.getJSONObject(i).getString("CO_DEST_FINA"))) {
                                    Spinner_model CO_DEST_FINA = new Spinner_model(dataDestinos[0], dataDestinos[1], "");
                                    LIST_CO_DEST_FINA.add(CO_DEST_FINA);
                                }
                            }
                        }
                    }
                    ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,LIST_CO_DEST_FINA);
                    spinner_destino.setAdapter(spinnerArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Tarifa.", Toast.LENGTH_LONG).show();
                }
                calcularTarifa(editText_tarifa);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Spinner_TTTIPO_DOCU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner_model st = (Spinner_model)Spinner_TTTIPO_DOCU.getSelectedItem();
                ET_RASON_SOCIAL.setText("");
                ET_RUC_CLIENTE.setText("");
                editText_dni.setText("");
                Log.d("spinner_docu",st.id);
                editText_noClie_NoRuc.setText("");
                if (st.id=="3")
                {
                    ET_RASON_SOCIAL.setVisibility(View.VISIBLE);
                    ET_RUC_CLIENTE.setVisibility(View.VISIBLE);
                    TV_textRasonSocial .setVisibility(View.VISIBLE);
                    TV_textRUC.setVisibility(View.VISIBLE);
                    editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                }else if(st.id=="4" || st.id=="5"){
                    ET_RASON_SOCIAL.setVisibility(View.INVISIBLE);
                    ET_RUC_CLIENTE.setVisibility(View.INVISIBLE);
                    TV_textRasonSocial .setVisibility(View.INVISIBLE);
                    TV_textRUC.setVisibility(View.INVISIBLE);
                    editText_dni.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                }
                else{
                    ET_RASON_SOCIAL.setVisibility(View.INVISIBLE);
                    ET_RUC_CLIENTE.setVisibility(View.INVISIBLE);
                    TV_textRasonSocial .setVisibility(View.INVISIBLE);
                    TV_textRUC.setVisibility(View.INVISIBLE);
                    editText_dni.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        button_imprimirBoleto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (!FuncionesAuxiliares.ValidaStadoImpresora(getActivity(), printer)) {
                return;
            }
            if(FlagValidaButton == 0)
            {
               FlagValidaButton=1;
               button_imprimirBoleto.setEnabled(false);
                final Spinner_model st = (Spinner_model)Spinner_TTTIPO_DOCU.getSelectedItem();
                if(!FuncionesAuxiliares.ValidacionDocumento(st.id,editText_noClie_NoRuc,
                        editText_dni,ET_RUC_CLIENTE,ET_RASON_SOCIAL,button_imprimirBoleto,getActivity())) {
                    FlagValidaButton=0;
                    return;
                }else if(ValidaCorrelativoExite())
                {
                    FlagValidaButton = 0;
                    button_imprimirBoleto.setEnabled(true);
                    return;
                }else if(editText_tarifa.getText().toString().equals("0")) {
                    Toast.makeText(getActivity(), "TARIFA NO PUEDE SER 0", Toast.LENGTH_SHORT).show();
                    FlagValidaButton=0;
                    button_imprimirBoleto.setEnabled(true);
                    return;
                }else if(editText_tarifaAdicional.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), "AGREGAR TARIFA ADICIONAL", Toast.LENGTH_SHORT).show();
                    FlagValidaButton=0;
                    button_imprimirBoleto.setEnabled(true);
                    return;
                }else if(editText_tarifaAdicional.getText().toString().length() >= 2) {
                    Toast.makeText(getActivity(), "TARIFA ADICIONAL VALOR NO PERMITIDO", Toast.LENGTH_SHORT).show();
                    FlagValidaButton=0;
                    button_imprimirBoleto.setEnabled(true);
                    return;
                }
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(st.id);
            }
            }
        });
    }
    public boolean ValidaCorrelativoExite()
    {
        for (int i = 0; i < getCorrelativo.length(); i++) {
            try{
                JSONObject info;
                info = getCorrelativo.getJSONObject(i);
                if(editText_dni.getText().toString().length() == 8 &&
                        sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                        info.getString("DE_GSER").equals("PASAJES RUTA") &&
                        info.getString("TI_DOCU").equals("BLT")) {
                    serieSeleccionado = info.getString("NUMERO_SERIE");
                    KeyTTDOSE_CORR = "guardar_correlativoViajeBLT"+idEmpresa;
                    CorrelativoValida = sharedPreferences.getString(KeyTTDOSE_CORR, "NoData");
                    correlativoSeleccionado = Integer.toString(Integer.valueOf(CorrelativoValida)+1);
                    String correlativoCompleto = completarCorrelativo(Integer.valueOf(correlativoSeleccionado));
                    if(ValidaDuplicidad(serieSeleccionado+"-"+correlativoCompleto,sharedPreferences.getString("guardar_idEmpresa", "NoData")) == true)
                    {
                        Toast.makeText(getActivity(), "CORRELATIVO YA EXISTE COMUNICARSE CON SISTEMAS,PARA SU ACTUALIZACION", Toast.LENGTH_SHORT).show();
                        FlagValidaButton=0;
                        button_imprimirBoleto.setEnabled(true);
                        return true;
                    }
                } else if (editText_dni.getText().toString().length() == 11 &&
                        sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                        info.getString("DE_GSER").equals("PASAJES RUTA") &&
                        info.getString("TI_DOCU").equals("FAC")) {
                    serieSeleccionado = info.getString("NUMERO_SERIE");
                    KeyTTDOSE_CORR = "guardar_correlativoViajeFAC"+idEmpresa;
                    CorrelativoValida = sharedPreferences.getString(KeyTTDOSE_CORR, "NoData");
                    correlativoSeleccionado = Integer.toString(Integer.valueOf(CorrelativoValida)+1);
                    String correlativoCompleto = completarCorrelativo(Integer.valueOf(correlativoSeleccionado));
                    if(ValidaDuplicidad(serieSeleccionado+"-"+correlativoCompleto,sharedPreferences.getString("guardar_idEmpresa", "NoData")) == true)
                    {
                        Toast.makeText(getActivity(), "CORRELATIVO YA EXISTE COMUNICARSE CON SISTEMAS,PARA SU ACTUALIZACION", Toast.LENGTH_SHORT).show();
                        FlagValidaButton=0;
                        button_imprimirBoleto.setEnabled(true);
                        return true;
                    }
                }
            } catch (Exception ex)
            {
                Toast.makeText(getContext(),"Error ValidarCorrelativo"+ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
    public void calcularTarifa(EditText editText_tarifa){
        for (int i = 0; i < getTarifa.length(); i++) {
            try {
                if (getTarifa.getJSONObject(i).getString("CO_DEST_FINA").equals(idDestino) &&
                        getTarifa.getJSONObject(i).getString("TI_SERV").equals(idServicio) &&
                        getTarifa.getJSONObject(i).getString("CO_RUMB").equals(rumbo)){
                    editText_tarifa.getText().clear();
                    editText_tarifa.setText(getTarifa.getJSONObject(i).getString("PR_BASE_ACTU"));
                    break;

                }else{
                    editText_tarifa.getText().clear();
                    editText_tarifa.setText("0");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor. Revisar la ws de Tarifas.", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void imprimir_boletas(String ted, String tipoBoleta) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boleta boleta = new Boleta(tipoBoleta);
        boleta.setOrigen(sharedPreferences.getString("Origen_Texto", "NoData"));
        boleta.setDestino(sharedPreferences.getString("Destino_Texto","NoData"));
        boleta.setTarifa(sharedPreferences.getString("guardar_tarifa", "NoData"));
        boleta.setDNI(sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
        boleta.setSeriePasaje(sharedPreferences.getString("guardar_serieViaje", "NoData"));
        boleta.setCorrelativoPasaje(sharedPreferences.getString("guardar_correlativoViajeCompleto","NoData"));
        boleta.setEmpresa(empresaSeleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("guardar_fechaVentaViaje", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("guardar_horaVentaViaje", "NoData"));
        boleta.setNombreCliente(sharedPreferences.getString("guardar_nombreCliente", "-"));
        boleta.setNumAsiento(sharedPreferences.getString("guardar_numAsientoVendido", "-"));
        boleta.setEmpesa_imp(sharedPreferences.getString("guardar_idEmpresa","NoData"));
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();
        }
    }
    public void guardarCompraViaje(String xml64, String ted64,
                                   final String correlativoCompleto, final Spinner spinner_destino) {
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        guardarDataMemoria("guardar_fechaVentaViaje", fechaVenta, getActivity());
        final String horaVenta = new SimpleDateFormat("hh:mm a").format(date);
        guardarDataMemoria("guardar_horaVentaViaje", horaVenta, getActivity());
        final JSONObject jsonObject = generarJSONViaje(fechaVenta, xml64, ted64, serieSeleccionado, correlativoCompleto, correlativoSeleccionado);
        String ws_postVenta = getString(R.string.ws_ruta) + "SetVentaRuta";
        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "viaje");
        cv.put("liberado", "No");
        cv.put("nu_docu",serieSeleccionado + "-" + correlativoCompleto);
        cv.put("ti_docu","BLT");
        cv.put("co_empr",sharedPreferences.getString("guardar_idEmpresa", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));
        if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
            cv.put("puesto", "boletero");
        }else{
            cv.put("puesto", "anfitrion");
        }
        sqLiteDatabase.insert("VentaBoletos", null, cv);
        ventaDone = true;
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(!response.toString().equals("null") && !response.toString().equals("[]"))
                        {
                            if (response.length() > 0) {
                               JSONObject info;
                                try {
                                    info = response.getJSONObject(0);
                                    if (info.getString("Respuesta").equals("GUARDADO")) {

                                   } else {
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
                Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();
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
        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestVenta);
    }
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    public void stopBoletoService() {
        BoletoService.startService(getActivity(), false);
    }
    public JSONObject generarJSONViaje(String fechaVenta, String xml64, String ted64,
                                       String serieSeleccionado, String correlativoCompleto, String correlativoSeleccionado) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Empresa", sharedPreferences.getString("guardar_idEmpresa", "NoData"));
            jsonObject.put("tipoDocumento", sharedPreferences.getString("guardar_tipoDocumentoViaje", "NoData"));
            jsonObject.put("NumeroDocumento", serieSeleccionado + "-" + correlativoCompleto);
            jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad", "NoData"));
            jsonObject.put("Agencia", sharedPreferences.getString("guardar_agencia", "NoData"));
            jsonObject.put("CondicionPago", "CCE");
            jsonObject.put("MonedaTipo", "SOL");
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("RumboItinerario", sharedPreferences.getString("guardar_rumbo", "NoData"));
            jsonObject.put("OrigenBoleto", sharedPreferences.getString("guardar_idOrigen", "NoData"));
            jsonObject.put("DestinoBoleto", sharedPreferences.getString("guardar_idDestino", "NoData"));
            jsonObject.put("SecuenciaItin", "");
            if (sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
                jsonObject.put("CodigoCliente", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
                jsonObject.put("RUC", sharedPreferences.getString("guardar_RUC", "NoData"));
                jsonObject.put("RazonSocial",sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData"));
            } else {
                jsonObject.put("CodigoCliente", sharedPreferences.getString("guardar_numeroDocumento", "NoData"));
                jsonObject.put("RUC", "");
                jsonObject.put("RazonSocial","");
            }
            jsonObject.put("NombreCliente", sharedPreferences.getString("guardar_nombreCliente", "NoData"));
            jsonObject.put("TipoServicioItin", sharedPreferences.getString("guardar_tipoServicio", "NoData"));
            jsonObject.put("Asiento", "");
            jsonObject.put("FechaViajeItin", "");
            jsonObject.put("horaViajeItin", "");
            jsonObject.put("Precio", sharedPreferences.getString("guardar_tarifa", "NoData"));
            jsonObject.put("UsuarioRegistro", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("Correlativo", correlativoSeleccionado);
            jsonObject.put("Caja", sharedPreferences.getString("guardar_caja", "NoData"));
            jsonObject.put("TipoVenta", "Boletero");
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
        } catch (JSONException e) {
            e.printStackTrace();
            guardarDataMemoria("ErrorVenta",e.getMessage(),getActivity());
        }
        return jsonObject;
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
                        return true;
                    }
                }
            }
            return  false;
        }
        catch (Exception ex)
        {
            return  false;
        }
    }
    public boolean ValidaDuplicidad(String NU_DOCU,String CO_EMPR)
    {
        try{
            sqLiteDatabase = ventaBlt.getWritableDatabase();
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
    public static void ModeloDistinct(List<? extends Object> al) {
        for(int i = 0; i < al.size(); i++) {
            for(int j = i + 1; j < al.size(); j++) {
                if(al.get(i).equals(al.get(j))){
                    al.remove(j);
                    j--;
                }
            }
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;
        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping...");
            try {
                if (false) {
                } else {
                            JSONObject info;
                            for (int i = 0; i < getCorrelativo.length(); i++) {
                                try {
                                    info = getCorrelativo.getJSONObject(i);
                                    if (!params[0].equals("3") &&
                                            sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                                            info.getString("DE_GSER").equals("PASAJES RUTA") &&
                                            info.getString("TI_DOCU").equals("BLT")) {
                                        guardarDataMemoria("TipoVenta","BOLETA",getContext());
                                        serieSeleccionado = info.getString("NUMERO_SERIE");
                                        KeyTTDOSE_CORR = "guardar_correlativoViajeBLT" + idEmpresa;
                                        guardarDataMemoria("SerieSeleccionadoKey", KeyTTDOSE_CORR, getActivity());
                                        CorrelativoValida = sharedPreferences.getString(KeyTTDOSE_CORR, "NoData");
                                        correlativoSeleccionado = Integer.toString(Integer.valueOf(CorrelativoValida) + 1);
                                        guardarDataMemoria("guardar_tipoDocumentoViaje", info.getString("TIPO_DOCUMENTO"), getActivity());
                                    } else if (params[0].equals("3")  &&sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                                            info.getString("DE_GSER").equals("PASAJES RUTA") &&
                                            info.getString("TI_DOCU").equals("FAC")) {
                                        guardarDataMemoria("TipoVenta","FACTURA",getContext());
                                        serieSeleccionado = info.getString("NUMERO_SERIE");
                                        KeyTTDOSE_CORR = "guardar_correlativoViajeFAC" + idEmpresa;
                                        guardarDataMemoria("SerieSeleccionadoKey", KeyTTDOSE_CORR, getActivity());
                                        CorrelativoValida = sharedPreferences.getString(KeyTTDOSE_CORR, "NoData");
                                        correlativoSeleccionado = Integer.toString(Integer.valueOf(CorrelativoValida) + 1);
                                        guardarDataMemoria("guardar_tipoDocumentoViaje", info.getString("TIPO_DOCUMENTO"), getActivity());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            String correlativoCompleto = completarCorrelativo(Integer.valueOf(correlativoSeleccionado));
                            guardarDataMemoria("guardar_correlativoViajeCompleto",correlativoCompleto,getActivity());
                            guardarDataMemoria("guardar_serieViaje", serieSeleccionado, getActivity());
                            guardarDataMemoria("guardar_correlativoViaje", correlativoSeleccionado, getActivity());
                            guardarDataMemoria("guardar_correlativoViajeCompleto", correlativoCompleto, getActivity());
                            Integer tarifa_total = Integer.valueOf(editText_tarifa.getText().toString()) + Integer.valueOf(editText_tarifaAdicional.getText().toString());
                            guardarDataMemoria("guardar_tarifa", Integer.toString(tarifa_total), getActivity());
                            guardarDataMemoria("guardar_numeroDocumento", editText_dni.getText().toString(), getActivity());
                            guardarDataMemoria("guardar_nombreCliente",editText_noClie_NoRuc.getText().toString(),getActivity());
                            guardarDataMemoria("guardar_RUC",ET_RUC_CLIENTE.getText().toString(),getActivity());
                            guardarDataMemoria("guardar_RAZON_SOCIAL",ET_RASON_SOCIAL.getText().toString(),getActivity());
                            for (int i = 0; i < lista_empresas.size(); i++) {
                                String idEmpresa = lista_empresas.get(i).split("-")[0];
                                if (idEmpresa.equals(sharedPreferences.getString("guardar_idEmpresa", "NoData"))) {
                                    empresaSeleccionada = lista_empresas.get(i);
                                    break;
                                }
                            }
                            if (ValidaExiteCorrelativo(serieSeleccionado + "-" + correlativoCompleto, sharedPreferences.getString("guardar_idEmpresa", "NoData")) == true) {
                                correlativoCompleto = completarCorrelativo(Integer.valueOf(correlativoSeleccionado) + 1);
                                guardarDataMemoria("guardar_correlativoViajeCompleto", correlativoCompleto, getActivity());
                                guardarDataMemoria(KeyTTDOSE_CORR, correlativoSeleccionado, getActivity());
                            }
                            final String trama = FuncionesAuxiliares.generarTramaBoleto(sharedPreferences, serieSeleccionado, correlativoCompleto,empresaSeleccionada);
                            String[] dataEncriptada = generarCodigoQR(trama, BoleteroViajeFragment.this);
                            guardarCompraViaje(dataEncriptada[0], dataEncriptada[1],
                                    correlativoCompleto, spinner_destino);
                            Date date = new Date();
                            String ho_bol = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                            final String TedQR = dataEncriptada[2] + "|" + sharedPreferences.getString("guardar_idEmpresa", "NoData") + "|"
                                    + sharedPreferences.getString("guardar_rumbo", "NoData") + "|"
                                    + sharedPreferences.getString("guardar_idOrigen", "NoData") + "|"
                                    + sharedPreferences.getString("guardar_idDestino", "NoData") + "|"
                                    + sharedPreferences.getString("guardar_numeroDocumento", "NoData") + "|"
                                    + sharedPreferences.getString("guardar_nombreCliente", "NoData") + "|"
                                    + sharedPreferences.getString("NU_SECU", "NoData") + "|"
                                    + sharedPreferences.getString("FE_VIAJ", "NoData") + "|0|"
                                    + "VIAJE|" + ho_bol;
                            imprimir_boletas(TedQR, "Viaje");
                            guardarDataMemoria(KeyTTDOSE_CORR, correlativoSeleccionado, getActivity());
                        }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("¿El pasajero viaja con carga?").setTitle("Carga");
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    BoleteroCargaFragment boleteroCargaFragment  = new BoleteroCargaFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, boleteroCargaFragment).commit();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
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
        }
    }
}
