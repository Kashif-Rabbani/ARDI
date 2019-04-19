package org.genesis.service.resources;


import com.google.gson.Gson;
import net.minidev.json.JSONObject;

import net.minidev.json.JSONValue;
import org.visualdataweb.vowl.owl2vowl.Owl2Vowl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


@Path("owl2vowl")
public class Owl2VowlResource {
    @POST
    @Path("json/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response GET_dataSource(String body) {
        System.out.println("[GET /getVowlJson/]" + body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        JSONObject output = oWl2vowl(objBody.getAsString("rdfsFilePath"), objBody.getAsString("vowlJsonFileOutputPath"));
        return Response.ok(new Gson().toJson(output)).build();
        //return Response.status(201).entity(output).build();
    }


    public static JSONObject oWl2vowl(String rdfsFilePath, String vowl_output_path) {
        JSONObject vowlData = new JSONObject();
        String jsonFilePath = "";
        try {
            File temp = new File(rdfsFilePath);
            String vowlFileName = temp.getName().replaceAll(".ttl", "-vowl.json");
            //InputStream in = new FileInputStream(rdfsFilePath);
            Owl2Vowl owl2Vowl = new Owl2Vowl(new FileInputStream(rdfsFilePath));
            //System.out.println(owl2Vowl.getJsonAsString());
            File jsonVowlFile = new File(vowl_output_path + vowlFileName);
            owl2Vowl.writeToFile(jsonVowlFile);
            jsonFilePath = jsonVowlFile.getAbsolutePath();

            vowlData.put("vowlJsonFileName", vowlFileName.replaceAll(".json", ""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        vowlData.put("vowlJsonFilePath", jsonFilePath);
        //System.out.println(jsonFilePath);
        return vowlData;
    }
}
