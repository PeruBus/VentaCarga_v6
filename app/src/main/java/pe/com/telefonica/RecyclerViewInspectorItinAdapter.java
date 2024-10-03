package pe.com.telefonica.soyuz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewInspectorItinAdapter extends RecyclerView.Adapter<RecyclerViewInspectorItinAdapter.ViewHolder>{
    private List<ItinerarioModel> mData;
    private LayoutInflater mInflater;
    private RecyclerViewInspectorItinAdapter.ItemClickListener mClickListener;

    RecyclerViewInspectorItinAdapter(Context context, List<ItinerarioModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    @Override
    public RecyclerViewInspectorItinAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_inspector_itin_row, parent, false);
        return new RecyclerViewInspectorItinAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclerViewInspectorItinAdapter.ViewHolder holder, int position) {
        ItinerarioModel Mod = mData.get(position);
        holder.text_Bus.setText("BUS: "+Mod.getCO_VEHI());
        holder.text_Empresa.setText("EMPRESA: "+Mod.getCO_EMPR());
        holder.text_NuSecu.setText("SALIDA: "+Mod.getNU_SECU());
        holder.txt_FE_PROG.setText("PROGRAMACION: "+Mod.getFE_PROG());
        holder.txt_Rumbo.setText("RUMBO: "+Mod.getCO_RUMB());
        holder.txt_HO_SALI.setText("HORA: "+Mod.getHO_SALI());
        holder.txt_Anfitrion.setText("ANFITRION: "+Mod.getNO_AYUD());
        holder.txt_Conductor.setText("CONDUCTOR: "+Mod.getNO_COND());
        holder.txt_CO_DEST_ORIG.setText("ORIGEN: "+Mod.getCO_DEST_ORIG());
        holder.txt_CO_DEST_FINA.setText("DESTINO: "+Mod.getCO_DEST_FINA());
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text_Empresa;
        TextView text_Bus;
        TextView text_NuSecu;
        TextView txt_Rumbo;
        TextView txt_Conductor;
        TextView txt_Anfitrion;
        TextView txt_FE_PROG;
        TextView txt_CO_DEST_ORIG;
        TextView txt_CO_DEST_FINA;
        TextView txt_HO_SALI;
        ViewHolder(View itemView) {
            super(itemView);
            text_Empresa = itemView.findViewById(R.id.text_Empresa);
            text_Bus = itemView.findViewById(R.id.text_Bus);
            text_NuSecu = itemView.findViewById(R.id.text_NuSecu);
            txt_Rumbo = itemView.findViewById(R.id.txt_Rumbo);
            txt_Conductor = itemView.findViewById(R.id.txt_Conductor);
            txt_Anfitrion = itemView.findViewById(R.id.txt_Anfitrion);
            txt_FE_PROG = itemView.findViewById(R.id.txt_FE_PROG);
            txt_CO_DEST_ORIG = itemView.findViewById(R.id.txt_CO_DEST_ORIG);
            txt_CO_DEST_FINA = itemView.findViewById(R.id.txt_CO_DEST_FINA);
            txt_HO_SALI = itemView.findViewById(R.id.txt_HO_SALI);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    ItinerarioModel getItem(int id) {
        return mData.get(id);
    }
    public String getTextList(int id)
    {
        return  mData.get(id).toString();
    }
    void setClickListener(RecyclerViewInspectorItinAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

