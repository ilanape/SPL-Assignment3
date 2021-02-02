package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.srv.Server;


public class ReactorMain {

    public static void main(String[] args) {
        Database DB = Database.getInstance();
        DB.initialize("./Courses.txt");
        Server.reactor(
                Integer.parseInt(args[1]),
                Integer.parseInt(args[0]), //port
                () -> new MessagingProtocolImpl(), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();


    }
}
