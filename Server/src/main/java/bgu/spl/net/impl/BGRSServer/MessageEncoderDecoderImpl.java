package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {
    private Short opcode = null;
    private ByteBuffer opcodeBuffer = ByteBuffer.allocate(2);
    private ByteBuffer otherBuffer = ByteBuffer.allocate(2);
    private int zeroCounter = 0;
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private ByteArrayOutputStream output;

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (opcodeBuffer.hasRemaining()) {
            opcodeBuffer.put(nextByte);
            if (opcodeBuffer.hasRemaining()) return null;
            else {//opcode is ready
                opcodeBuffer.flip();
                opcode = opcodeBuffer.getShort();
            }
            if (opcode == 4 | opcode == 11) { //only two bytes
                Short copyOpcode = opcode;
                reset();
                return new Message(copyOpcode, null);
            }
            return null;
        }
        if (opcode >= 1 & opcode <= 3) return zerosType(nextByte, 2);
        else if ((opcode >= 5 & opcode <= 7) | (opcode >= 9 & opcode <= 10)) return twoByteType(nextByte);
        else return zerosType(nextByte, 1); //opcode==8
    }

    private Message twoByteType(byte nextByte) {
        otherBuffer.put(nextByte);
        if (otherBuffer.hasRemaining()) return null;
        //two bytes are ready
        otherBuffer.flip();
        String[] parameters = new String[1];
        parameters[0] = String.valueOf(otherBuffer.getShort());
        Short copyOpcode = opcode;
        reset();
        return new Message(copyOpcode, parameters);
    }

    private Message zerosType(byte nextByte, int zeros) {
        if (nextByte == '\0') zeroCounter++;
        if (zeroCounter != zeros) { //byte array filling
            pushByte(nextByte);
            return null;
        }
        pushByte(nextByte);//we want the last '\0'
        //byte array is ready
        Short copyOpcode = opcode;
        String[] parameters = splitByZero(Arrays.copyOfRange(bytes, 0, len), zeros);
        reset();
        return new Message(copyOpcode, parameters);
    }

    private String[] splitByZero(byte[] bytes, int zeros) {
        String[] toReturn = new String[zeros];
        int counter = 0;
        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\0') {
                toReturn[counter] = new String(bytes, j, i - j, StandardCharsets.UTF_8);
                j = i + 1;
                counter++;
            }
        }
        return toReturn;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(Message message) {
        opcode = message.getOpcode();
        output = new ByteArrayOutputStream();

        if (opcode == 12) return ackReply(message);
        else {  //(opcode == 13)
            output.write(shortToBytes(opcode), 0, 2);
            output.write(shortToBytes(Short.parseShort(message.getParameters()[0])), 0, 2);
            return output.toByteArray();
        }
    }

    private byte[] ackReply(Message message) {
        try {
            //opcode
            output.write(shortToBytes(opcode), 0, 2);
            //ack to which action
            short replyTo = Short.parseShort(message.getParameters()[0]);
            output.write(shortToBytes(replyTo), 0, 2);
            if (message.getParameters().length == 2) //has more parameters
                output.write(message.getParameters()[1].getBytes(StandardCharsets.UTF_8));

            output.write(new byte[]{(byte) '\0'});
            return output.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private void reset() {
        opcode = null;
        opcodeBuffer.clear();
        otherBuffer.clear();
        zeroCounter = 0;
        bytes = new byte[1 << 10]; //start with 1k
        len = 0;
    }

}


