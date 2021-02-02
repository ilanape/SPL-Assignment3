package bgu.spl.net.impl.BGRSServer;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
    private ConcurrentHashMap<Course, List<User>> Courses;
    private ConcurrentHashMap<User, Boolean> Users;
    private HashMap<Course, LinkedList<Short>> kdamsOrdered;
    private Course[] arr;

    private static class DatabaseHolder {
        private static Database instance = new Database();
    }

    //to prevent user from creating new Database
    private Database() {
        Courses = new ConcurrentHashMap<>();
        Users = new ConcurrentHashMap<>();
        kdamsOrdered = new HashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return DatabaseHolder.instance;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    boolean initialize(String coursesFilePath) {
        try {
            File coursesFile = new File(coursesFilePath);
            Scanner myReader = new Scanner(coursesFile);
            int counter = 0;
            while (myReader.hasNextLine()) {
                String thisCourse = myReader.nextLine();
                buildCourse(thisCourse, counter);
                counter++;
            }
            arr = new Course[counter];
            orderCourses();
            kdamOrder();
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void kdamOrder() {
        for (int i = 0; i < arr.length; i = i + 1) {
            for (int j = 0; j < arr.length; j = j + 1) {
                if (arr[i].getKdamCourseList().contains(arr[j].getCourseNum())) {
                    kdamsOrdered.get(arr[i]).addLast(arr[j].getCourseNum());
                }
            }
        }
    }

    private void orderCourses() {
        for (int i = 0; i < arr.length; i = i + 1) {
            for (Course c : Courses.keySet()) {
                if (c.getShowNum() == i)
                    arr[i] = c;
            }
        }
        //arr is ready
    }

    /**
     * creates new course in Courses DS from given string
     *
     * @param course
     */
    private void buildCourse(String course, int counter) {
        String[] str = course.split("\\|");
        short courseNum = Short.parseShort(str[0]);
        String courseName = str[1];
        String[] kdamParse = str[2].substring(1, str[2].length() - 1).split(",");
        List<Short> KdamCourses = new LinkedList<>();
        for (String s : kdamParse) {
            if (!s.isEmpty()) KdamCourses.add(Short.valueOf(s));
        }
        int numOfMaxStudents = Integer.parseInt(str[3]);

        Course thisCourse = new Course(courseNum, courseName, KdamCourses, numOfMaxStudents, counter);
        Courses.put(thisCourse, new LinkedList<>());
        kdamsOrdered.put(thisCourse, new LinkedList<Short>());
    }

    public User getUser(String userName) {
        synchronized (Users.keySet()) {
            for (User u : Users.keySet()) {
                if (u.getUserName().equals(userName)) return u;
            }
            return null; //user does not exist
        }
    }


    public boolean USERREG(short authorization, String userName, String password) {
        if (getUser(userName) != null) return false; //user already registered
        synchronized (Users.keySet()) { //so no one would register the same one at the same time
            String kind;
            if (authorization == 1) kind = "admin";
            else kind = "student";
            User toAdd = new User(kind, userName, password);
            Boolean succeed = Users.put(toAdd, false);
            return succeed == null;
        }
    }


    public boolean LOGIN(String userName, String password) {
        User u = getUser(userName);
        if (u == null || Users.get(u)) return false; //user does not exist or already logged
        else {//isn't logged
            if (!u.getPassword().equals(password)) return false; //wrong password
            synchronized (Users.get(u)) {
                if (!Users.get(u)) { //no one logged in between
                    Users.replace(u, false, true);
                    return true;
                }
                return false;
            }
        }
    }

    public boolean LOGOUT(String userName) {
        //u is not null since we assume the func is called by server if the user is logged in
        User u = getUser(userName);
        if (!Users.get(u)) return false; //already logged out
        //is logged
        //only one client can be logged to this user at once - no thread-safety problems
        Users.replace(u, true, false);
        return true;
    }

    public boolean COURSEREG(String userName, short courseNum) {
        //when this func is called:
        // the protocol already checked if the user is logged
        // the protocol checked the user is not admin
        User u = getUser(userName);
        if (u.getRegisteredCourses().contains(courseNum)) return false; //already registered to course
        //is not registered to this course
        for (Course c : Courses.keySet()) {
            if (c.getCourseNum() == courseNum) { //course exists in lists
                synchronized (Courses.get(c)) {
                    if (c.getNumOfMaxStudents() - Courses.get(c).size() <= 0) return false; //course is full
                    List<Short> kdamCourses = c.getKdamCourseList();
                    for (short kdam : kdamCourses) {
                        if (!u.getRegisteredCourses().contains(kdam)) return false; //doesnt have kdams
                    }
                    //has all kdams
                    Courses.get(c).add(u);
                    u.getRegisteredCourses().add(c.getCourseNum());
                    return true;
                }
            }
        }
        return false;
    }

    public List<Short> KDAMCHECK(short courseNum) {
        for (Course c : kdamsOrdered.keySet()) {
            if (c.getCourseNum() == courseNum) { //course exists in lists
                return kdamsOrdered.get(c);
            }
        }
        return null; //course does not exist
    }


    public boolean UNREGISTER(String userName, short courseNum) {
        // the protocol already checked if the user is logged
        User u = getUser(userName);
        if (!u.getRegisteredCourses().contains(courseNum)) return false; //does no registered to this course
        //is registered to course
        for (Course c : Courses.keySet()) {
            if (c.getCourseNum() == courseNum) {
                synchronized (Courses.get(c)) {
                    Courses.get(c).remove(u);
                    int index = u.getRegisteredCourses().indexOf(courseNum);
                    u.getRegisteredCourses().remove(index);
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> COURSESTAT(short courseNum) {
        List<String> list = new LinkedList<>();
        for (Course c : Courses.keySet()) {
            if (c.getCourseNum() == courseNum) {
                list.add("Course: (" + courseNum + ") " + c.getCourseName());
                list.add("Seats Available: " + (c.getNumOfMaxStudents() - Courses.get(c).size()) + "/" + c.getNumOfMaxStudents());
                List<String> userNames = new LinkedList<>();
                for (User u : Courses.get(c)) {
                    userNames.add(u.getUserName());
                }
                Collections.sort(userNames); //alphabetical
                StringBuilder toAdd = new StringBuilder("Students Registered: [");
                for (String name : userNames) {
                    toAdd.append(name).append(",");
                }
                if (!toAdd.toString().equals("Students Registered: ["))
                    toAdd = new StringBuilder(toAdd.substring(0, toAdd.length() - 1));

                toAdd.append("]");
                list.add(toAdd.toString());
                return list;
            }
        }
        return null;
    }

    public List<String> STUDENTSTAT(String userName) {
        List<String> list = new LinkedList<>();
        User u = getUser(userName);
        if (u == null) return null; //student does not exist
        list.add("Student: " + u.getUserName());
        StringBuilder toAdd = new StringBuilder("Courses: [");
        for (int i = 0; i < arr.length; i = i + 1) {
            if (u.getRegisteredCourses().contains(arr[i].getCourseNum()))
                toAdd.append(arr[i].getCourseNum()).append(",");
        }
        if (!toAdd.toString().equals("Courses: ["))
            toAdd.deleteCharAt(toAdd.length() - 1); //remove the last ','
        toAdd.append("]");
        list.add(toAdd.toString());
        return list;
    }


}
