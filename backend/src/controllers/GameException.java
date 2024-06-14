package controllers;

public class GameException extends Exception
{
    public enum Type
    {
        MAX_PLAYER,
        CODE_INVALID,
        PLAYER_INVALID,
        STATE_INVALID,
        UNKNOW,
    }

    private Type type;

    
    public GameException(String message) {
        super(message);
        this.type=Type.UNKNOW;
    }

    public GameException(String message, Type type) {
        super(message);
        this.type=type;
    }

    public Type getType()
    {
        return type;
    }
}