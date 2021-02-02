package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;

import java.util.List;

public class MessagingProtocolImpl implements MessagingProtocol<Message> {
    private User thisUser = null;
    private boolean shouldTerminate = false;
    private Database DB = Database.getInstance();

    @Override
    public Message process(Message msg) {
        short opcode=msg.getOpcode();
        String[] firstParameter = new String[1];
        firstParameter[0] = String.valueOf(opcode);

        if (opcode == 1 | opcode == 2) {
            if (thisUser == null) {
                if (DB.USERREG(opcode, msg.getParameters()[0], msg.getParameters()[1]))
                    return new Message((short) 12, firstParameter);
            }
        } else if (opcode == 3) {
            if (thisUser == null) {
                if (DB.LOGIN(msg.getParameters()[0], msg.getParameters()[1])) {
                    thisUser = DB.getUser(msg.getParameters()[0]);
                    return new Message((short) 12, firstParameter);
                }
            }
        } else if (opcode == 4) {
            if (thisUser != null) {
                if (DB.LOGOUT(thisUser.getUserName())) {
                    shouldTerminate = true;
                    return new Message((short) 12, firstParameter);
                }
            }
        } else if (opcode == 5) {
            if (thisUser != null && thisUser.getAuthorization().equals("student")) {
                if (DB.COURSEREG(thisUser.getUserName(), Short.parseShort(msg.getParameters()[0])))
                    return new Message((short) 12, firstParameter);
            }
        } else if (opcode == 6) {
            if (thisUser != null) {
                if (DB.KDAMCHECK(Short.parseShort(msg.getParameters()[0])) != null) {
                    List<Short> kdamList = DB.KDAMCHECK(Short.parseShort(msg.getParameters()[0]));
                    String[] parameters = new String[2];
                    parameters[0] = firstParameter[0];
                    parameters[1] = kdamList.toString();
                    return new Message((short) 12, parameters);
                }
            }
        } else if (opcode == 7) {
            if (thisUser != null && thisUser.getAuthorization().equals("admin")) {
                if (DB.COURSESTAT(Short.parseShort(msg.getParameters()[0])) != null) {
                    List<String> courseStat = DB.COURSESTAT(Short.parseShort(msg.getParameters()[0]));
                    String[] parameters = new String[2];
                    parameters[0] = firstParameter[0];
                    parameters[1] = courseStat.toString();
                    return new Message((short) 12, parameters);
                }
            }
        } else if (opcode == 8) {
            if (thisUser != null && thisUser.getAuthorization().equals("admin")) {
                List<String> studentStat = DB.STUDENTSTAT(msg.getParameters()[0]);
                if (studentStat != null) {
                    String[] parameters = new String[2];
                    parameters[0] = firstParameter[0];
                    parameters[1] = studentStat.toString();
                    return new Message((short) 12, parameters);
                }
            }
        } else if (opcode == 9) {
            String[] parameters = new String[2];
            parameters[0] = firstParameter[0];
            if (thisUser != null && thisUser.getAuthorization().equals("student")) {
                if (thisUser.getRegisteredCourses().contains(Short.parseShort(msg.getParameters()[0])))
                    parameters[1] = "REGISTERED";
                else parameters[1] = "NOT REGISTERED";
                return new Message((short) 12, parameters);
            }
        } else if (opcode == 10) {
            if (thisUser != null && thisUser.getAuthorization().equals("student")) {
                if (DB.UNREGISTER(thisUser.getUserName(), Short.parseShort(msg.getParameters()[0])))
                    return new Message((short) 12, firstParameter);
            }
        } else if(opcode==11) {
            if (thisUser != null && thisUser.getAuthorization().equals("student")) {
                String[] parameters = new String[2];
                parameters[0] = firstParameter[0];
                List<Short> myCourses = thisUser.getRegisteredCourses();
                parameters[1] = myCourses.toString().replace(", ",",");
                return new Message((short) 12, parameters);
            }
        }
        return new Message((short) 13, firstParameter);
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
