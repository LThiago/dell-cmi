package dellcmi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CurlWrapper {
    
    public String executeCurlBuilder(String optionsParams) throws Exception{
        //-v -L -X \"GET\" https://cowc-dev.dell.com/cs/login/login.htm -H \"Content-Type: text/html\"
        ProcessBuilder pb = new ProcessBuilder("C:\\Windows\\SysWOW64\\curl", "-v", "-L",
                                "-X", "GET", "https://cowc-dev.dell.com/cs/login/login.htm", "-H",
                                "\"Content-Type: text/html\"");
        
                                Process proc = pb.start();
        InputStream ins = proc.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(ins));
        StringBuilder sb = new StringBuilder();
        read
        .lines()
        .forEach(line -> {
            sb.append(line);
        });
        read.close();
        //proc.waitFor();

        proc.destroy();

        String output = sb.toString();
        
        System.out.println("%%%%% RESULT: " + output);

        return output;
    }

    public String executeCurl(String optionsParams){
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String output;
        try {
            process =runtime.exec(optionsParams);
            //process.waitFor();
            
            //int errorCode = process.waitFor();
            // BufferedReader reader = process.errorReader(StandardCharsets.UTF_8);
            // output = String.format("Program execution failed (code %d): %s",0,
            //                reader.lines().collect(Collectors.joining()));
            String line = "";
		    StringBuilder msg = new StringBuilder();
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            while ((line = reader.readLine()) != null ) {
                msg.append(line + "\n");
            }
            output = msg.toString();
           
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not invoke program.", e);
        } //catch (InterruptedException e) {
         //   e.printStackTrace();
         //   throw new RuntimeException("Could not wait for process exit.", e);
        //}
        
        
        System.out.println("%%%%% RESULT: " + output);

        return output;
    }
}
