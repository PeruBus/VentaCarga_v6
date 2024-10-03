package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;


public class BuscarBoletoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buscar_boleto, parent, false);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final Context contextBuscarBoleto = view.getContext();

        final EditText editText_numDocumento = view.findViewById(R.id.editText_numDocumento);

        final Button btn_buscar = view.findViewById(R.id.btn_buscar);


        final ArrayList<String> lista_asientosVendidos = getArray(sharedPreferences, gson, "insp_jsonReporteVenta");
        final ArrayList<String> lista_destinos = getArray(sharedPreferences,gson,"json_destinos");

        btn_buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tramaBusqueda = "";

                if (!lista_asientosVendidos.isEmpty() && !lista_asientosVendidos.get(0).equals("NoData")) {

                    String[] dataReporteVenta = lista_asientosVendidos.get(0).split("/");

                    for (int i = 0; i < dataReporteVenta.length; i++) {

                        String[] dataAsientosVendidos = dataReporteVenta[i].split("-");
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
                        //CARGA:
                        // dataAsientosVendidos[12] = TI_PROD
                        // dataAsientosVendidos[13] = CA_DOCU


                        if(editText_numDocumento.getText().toString().equals(dataAsientosVendidos[5])){

                           if(dataAsientosVendidos[10].equals("")){
                               dataAsientosVendidos[10] = "NO";
                           }


                            tramaBusqueda += dataAsientosVendidos[1]+"-"+dataAsientosVendidos[2]+"-"+dataAsientosVendidos[5]+"-"+
                                   dataAsientosVendidos[0]+"-"+dataAsientosVendidos[3]+"-"+dataAsientosVendidos[4]+"-"+dataAsientosVendidos[10]+"/";
                            /* ----------------------------------------- */
                        }
                        /* ----------------------------------------- */
                    }
                    /* ----------------------------------------- */

                    /* Validación en caso la trama no esté vacía */
                    if(!tramaBusqueda.equals("")){

                        tramaBusqueda = tramaBusqueda.substring(0, tramaBusqueda.length() - 1);
                        final String tramaListener = tramaBusqueda;

                        ListView listView = view.findViewById(R.id.listView_busquedaBoletos);
                        TablaBusquedaBoletosAdapter adapterBusquedaBoletos = new TablaBusquedaBoletosAdapter(tramaBusqueda, getActivity());
                        listView.setAdapter(adapterBusquedaBoletos);

                       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*
                                String[] dataReporteBusqueda = tramaListener.split("/");

                                String[] dataBusquedaBoletos = dataReporteBusqueda[position].split("-");



                                String numBoletoEncontrado = dataBusquedaBoletos[0] + "-" + dataBusquedaBoletos[1];
                                guardarDataMemoria("insp_numBoletoLeido", numBoletoEncontrado, contextBuscarBoleto);
                                /* ----------------------------------------- */

                                /* Validación en caso la lista de asiento vendidos no esté vacía
                                if (!lista_asientosVendidos.get(0).equals("NoData")) {

                                    String[] dataReporteVenta = lista_asientosVendidos.get(0).split("/");

                                    /* Iteración en función a la lista de asientos vendidos
                                    for (int i = 0; i < dataReporteVenta.length; i++) {

                                        String[] dataAsientosVendidos = dataReporteVenta[i].split("-");

                                        String numCorrelativoBLTCompleto = completarCorrelativo(Integer.valueOf(dataAsientosVendidos[2]));
                                        String numBoletoVendido = dataAsientosVendidos[1] + "-" + numCorrelativoBLTCompleto;

                                        if (numBoletoEncontrado.equals(numBoletoVendido)) {


                                            guardarDataMemoria("insp_numBoleto", numBoletoVendido, contextBuscarBoleto);
                                            guardarDataMemoria("insp_tipoDocumento", dataAsientosVendidos[8], contextBuscarBoleto);
                                            /* ----------------------------------------- */

                                            /* Se obtiene toda la data que se va a mostrar en el formulario del inspección
                                            for (int j = 0; j < lista_destinos.size(); j++) {
                                                String[] dataDestinos = lista_destinos.get(j).split("-");

                                                if (dataAsientosVendidos[3].equals(dataDestinos[0])) {
                                                    guardarDataMemoria("insp_origenID", dataDestinos[0], contextBuscarBoleto);
                                                    guardarDataMemoria("insp_origenNombre", dataDestinos[1], contextBuscarBoleto);
                                                }
                                            }

                                            for (int j = 0; j < lista_destinos.size(); j++) {
                                                String[] dataDestinos = lista_destinos.get(j).split("-");

                                                if (dataAsientosVendidos[4].equals(dataDestinos[0])) {
                                                    guardarDataMemoria("insp_destinoID", dataDestinos[0], contextBuscarBoleto);
                                                    guardarDataMemoria("insp_destinoNombre", dataDestinos[1], contextBuscarBoleto);
                                                }
                                            }
                                            guardarDataMemoria("insp_tarifa", dataAsientosVendidos[6], contextBuscarBoleto);
                                            guardarDataMemoria("insp_dni", dataAsientosVendidos[5], contextBuscarBoleto);
                                            guardarDataMemoria("insp_asiento", dataAsientosVendidos[0], contextBuscarBoleto);
                                            guardarDataMemoria("insp_inspeccionBoleto", "NO", contextBuscarBoleto);
                                            guardarDataMemoria("insp_liberado", dataAsientosVendidos[9], contextBuscarBoleto);
                                            guardarDataMemoria("insp_carga", dataAsientosVendidos[10], contextBuscarBoleto);
                                            guardarDataMemoria("insp_servicioEmpresa", dataAsientosVendidos[11], contextBuscarBoleto);

                                            if (dataAsientosVendidos[11].equals("CARGA")) {

                                                guardarDataMemoria("insp_tipoProducto", dataAsientosVendidos[12], contextBuscarBoleto);
                                                guardarDataMemoria("insp_cantidad", dataAsientosVendidos[13], contextBuscarBoleto);

                                            }

                                            final ArrayList<String> lista_reporteInspeccion = getArray(sharedPreferences, gson, "json_reporteInspeccion");

                                            if (lista_reporteInspeccion.size() != 0) {


                                                for (int j = 0; j < lista_reporteInspeccion.size(); j++) {

                                                    String[] dataReporteInspeccion = lista_reporteInspeccion.get(j).split("-");
                                                    // dataReporteInspeccion[0] = NUM_SERIE
                                                    // dataReporteInspeccion[1] = CORRELATIVO
                                                    // dataReporteInspeccion[2] = ID_EVENT
                                                    // dataReporteInspeccion[3] = SEMAFORO
                                                    // dataReporteInspeccion[4] = DE_TIPO_EVENT


                                                    String numBoletoReporte = dataReporteInspeccion[0] + "-" + dataReporteInspeccion[1];



                                                    if (numBoletoEncontrado.equals(numBoletoReporte)) {

                                                        guardarDataMemoria("insp_idEvento", dataReporteInspeccion[2], contextBuscarBoleto);
                                                        guardarDataMemoria("insp_semaforo", dataReporteInspeccion[3], contextBuscarBoleto);
                                                        guardarDataMemoria("insp_tipoInspeccion", dataReporteInspeccion[4], contextBuscarBoleto);

                                                    }
                                                }
                                            }


                                            InspectorFormularioFragment inspectorFormularioFragment = new InspectorFormularioFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, inspectorFormularioFragment).commitAllowingStateLoss();
                                            /* -----------------------------------------
                                        }
                                        /* -----------------------------------------
                                    }
                                    /* -----------------------------------------
                                }
                                /* ----------------------------------------- */
                                InspeccionVentaFragment inspectorFormularioFragment = new InspeccionVentaFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, inspectorFormularioFragment).commitAllowingStateLoss();

                               /* Fragment inspectorFormularioFragment = new InspeccionVentaFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, inspectorFormularioFragment).commitAllowingStateLoss();*/
                            }
                        });
                        /* ----------------------------------------- */
                    }
                    /* ----------------------------------------- */
                }else{
                    Toast toast = Toast.makeText(getContext(),"NO AHI VENTA EN ESTE ITINERARIO", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 50, 50);
                    toast.show();
                    InspeccionVentaFragment inspectorFormularioFragment = new InspeccionVentaFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, inspectorFormularioFragment).commitAllowingStateLoss();
                }
                /* ----------------------------------------- */
            }
        });
        /* ----------------------------------------- */
    }
}
