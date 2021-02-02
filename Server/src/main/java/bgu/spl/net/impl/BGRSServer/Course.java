package bgu.spl.net.impl.BGRSServer;

import java.util.List;

public class Course {
    private short courseNum;
    private String courseName;
    private List<Short> KdamCourseList;
    private int numOfMaxStudents;
    private int showNum;

    public Course(short courseNum, String courseName, List<Short> KdamCourseList, int numOfMaxStudents, int showNum){
        this.courseNum=courseNum;
        this.courseName=courseName;
        this.KdamCourseList=KdamCourseList;
        this.numOfMaxStudents=numOfMaxStudents;
        this.showNum=showNum;
    }

    public int getNumOfMaxStudents(){
        return numOfMaxStudents;
    }

    public short getCourseNum(){ return courseNum;}

    public List<Short> getKdamCourseList() {
        return KdamCourseList;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getShowNum(){ return showNum;}
}
