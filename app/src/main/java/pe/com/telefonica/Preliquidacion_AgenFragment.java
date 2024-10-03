package pe.com.telefonica.soyuz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class Preliquidacion_AgenFragment extends Fragment {
    static Spinner spinner = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preliquidacion_agencia, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        spinner = (Spinner) view.findViewById(R.id.spinner_preAgen);
        final List<Spinner_model> model = new ArrayList<>();
        Spinner_model planet_2 = new Spinner_model("1", "SELECCIONAR", "");
        model.add(planet_2);
        Spinner_model planet = new Spinner_model("1", "PERU BUS", "01");
        model.add(planet);
        Spinner_model planet1 = new Spinner_model("2", "SOYUZ", "02");
        model.add(planet1);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, model);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Spinner_model st = (Spinner_model)spinner.getSelectedItem();

               if (st.abbrev.equals("01")){
                   FuncionesAuxiliares.guardarDataMemoria("Empresa","01",getContext());
                    BoleteroPreLiquidacionFragment tarifarioViajeFragment = new BoleteroPreLiquidacionFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.preliquidaAgenFragment, tarifarioViajeFragment);
                    ft.commit();
                    /* ----------------------------------------- */

                }else if (st.abbrev.equals("02")){
                   FuncionesAuxiliares.guardarDataMemoria("Empresa","02",getContext());
                   BoleteroPreLiquidacionFragment tarifarioCargaFragment = new BoleteroPreLiquidacionFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.preliquidaAgenFragment, tarifarioCargaFragment);
                    ft.commit();
                    /* ----------------------------------------- */
                }
                /* ----------------------------------------- */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }


}
