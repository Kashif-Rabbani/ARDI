package org.genesis.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Modified by Kashif Rabbani 18/042019
 */
public class ApacheMain {

    public static String configPath;

    public static void main(String[] args) throws Exception {
        int port = 9090;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        ResourceConfig config = new ResourceConfig();
        config.packages("org.genesis.service");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        try {
            server.start();
            System.out.println(
                    "Server " + server.getState() + " at Port " + port + "\n " +
                            "You can access OWL2VOWL Service at http://localhost:" + port + "/owl2vowl/json/" +
                            "\n Request type: POST " +
                            "\n Parameters of post request: 'rdfsFilePath' and 'vowlJsonFileOutputPath' as JSONObject elements." +
                            "\n Return: JSONObject of containing two elements 'vowlJsonFileName' and 'vowlJsonFilePath'");
            server.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
