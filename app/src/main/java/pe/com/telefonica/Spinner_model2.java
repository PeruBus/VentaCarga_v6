package pe.com.telefonica.soyuz;

public class Spinner_model2 {
    public String id = "0";
    public String name = "";
    public String abbrev = "";
    public Spinner_model2(String _id, String _name, String _abbrev )
    {
        id = _id;
        name = _name;
        abbrev = _abbrev;
    }
    /*public String toString()
    {
        return( name + " (" + abbrev + ")" );
    }*/
    public String toString()
    {
        return(name);
    }
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Spinner_model2)) {
            return false;
        }
        Spinner_model2 Model = (Spinner_model2) o;
        return Model.id.equals(id) &&
                Model.abbrev.equals(abbrev) &&
                Model.name.equals(name);
    }

}