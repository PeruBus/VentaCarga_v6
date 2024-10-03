package pe.com.telefonica.soyuz;

public class ModuloSistema {
    private String pModulo;
    private String pNombreModulo;
    public ModuloSistema(String Modulo,String NombreModulo)
    {
        this.pModulo = Modulo;
        this.pNombreModulo = NombreModulo;
    }

    public String getpModulo() {
        return pModulo;
    }

    public String getpNombreModulo() {
        return pNombreModulo;
    }
}
