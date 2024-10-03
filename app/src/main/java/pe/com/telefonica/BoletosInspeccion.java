package pe.com.telefonica.soyuz;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.gson.Gson;
import java.util.ArrayList;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;

public class BoletosInspeccion extends Fragment {
    SharedPreferences sharedPreferences;
    Gson gson;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();
        return inflater.inflate(R.layout.boletos_inspeccion, parent, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final ArrayList<String> lista_asientosVendidos = getArray(sharedPreferences, gson, "insp_jsonReporteVenta");
        ListView listView = view.findViewById(R.id.listView_inspecciones);
        TablaAdapter adapterInspecciones = new TablaAdapter(lista_asientosVendidos, getActivity());
        listView.setAdapter(adapterInspecciones);
    }


}
