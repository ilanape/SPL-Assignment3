package bgu.spl.net.impl.BGRSServer;

import java.util.LinkedList;
import java.util.List;

public class User {
    private String authorization;
    private String userName;
    private String password;
    private List<Short> registedCourses;

    public User(String authorization, String userName, String password){
        this.authorization=authorization;
        this.userName=userName;
        this.password=password;
        registedCourses= new LinkedList<>();
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public List<Short> getRegisteredCourses(){
        return registedCourses;
    }

    public String getAuthorization() {
        return authorization;
    }
}
