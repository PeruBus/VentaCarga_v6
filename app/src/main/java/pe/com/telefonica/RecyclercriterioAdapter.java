package pe.com.telefonica.soyuz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclercriterioAdapter extends RecyclerView.Adapter<RecyclercriterioAdapter.ViewHolder>  {
    private List<ModuloSistema> mData;
    private LayoutInflater mInflater;
    private RecyclercriterioAdapter.ItemClickListener mClickListener;

    RecyclercriterioAdapter(Context context, List<ModuloSistema> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    @Override
    public RecyclercriterioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.seleccion_modulo, parent, false);
        return new RecyclercriterioAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclercriterioAdapter.ViewHolder holder, int position) {
        ModuloSistema Mod = mData.get(position);
        holder.text_criterio.setText(Mod.getpNombreModulo());
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text_criterio;

        ViewHolder(View itemView) {
            super(itemView);
            text_criterio = itemView.findViewById(R.id.text_criterio);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    ModuloSistema getItem(int id) {
        return mData.get(id);
    }
    public String getTextList(int id)
    {
        return  mData.get(id).getpModulo();
    }
    void setClickListener(RecyclercriterioAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
