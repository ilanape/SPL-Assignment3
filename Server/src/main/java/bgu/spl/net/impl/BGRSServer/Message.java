package bgu.spl.net.impl.BGRSServer;

public class Message {
    private short opcode;
    private String[] parameters;

    public Message(short opcode, String[] parameters){
        this.opcode=opcode;
        this.parameters=parameters;
    }

    public short getOpcode() {
        return opcode;
    }

    public String[] getParameters() {
        return parameters;
    }
}
