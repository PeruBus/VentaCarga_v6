package pe.com.telefonica.soyuz;

public class ModeloZonaInspeccion {
    String CO_TRAM="";
    String DE_TRAM="";
    String CO_ZONA="";
    String NU_ORDE_SUR="";
    String NU_ORDE_NOR="";

    public ModeloZonaInspeccion(String pCO_TRAM,String pDE_TRAM,String pCO_ZONA,String pNU_ORDE_SUR,String pNU_ORDE_NOR)
    {
        this.CO_TRAM=pCO_TRAM;
        this.DE_TRAM=pDE_TRAM;
        this.CO_ZONA=pCO_ZONA;
        this.NU_ORDE_SUR=pNU_ORDE_SUR;
        this.NU_ORDE_NOR=pNU_ORDE_NOR;
    }
    public String getCO_TRAM() {
        return CO_TRAM;
    }

    public String getDE_TRAM() {
        return DE_TRAM;
    }

    public String getCO_ZONA() {
        return CO_ZONA;
    }

    public String getNU_ORDE_SUR() {
        return NU_ORDE_SUR;
    }

    public String getNU_ORDE_NOR() {
        return NU_ORDE_NOR;
    }
    public String ToString(){
        return getCO_TRAM()+"ƒ"+getNU_ORDE_SUR();
    }




}
