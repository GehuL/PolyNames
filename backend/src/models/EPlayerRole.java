package models;

public enum EPlayerRole
{
    MAITRE_INTUITION,
    MAITRE_MOT;

    public EPlayerRole inverse()
    {
        return this == MAITRE_INTUITION ? MAITRE_MOT : MAITRE_INTUITION;
    }   
}
