package models;

public enum EEtatPartie
{
    SELECTION_ROLE("SELECTION_ROLE"),
    DEVINER("DEVINER"),
    CHOISIR_INDICE("CHOISIR_INDICE"),
    FIN("FIN");

    private String etat;

    private EEtatPartie(String etat)
    {
        this.etat = etat;
    }

    public String getEtat()
    {
        return this.etat;
    }
}
