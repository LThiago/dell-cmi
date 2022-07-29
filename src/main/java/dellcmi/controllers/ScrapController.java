package dellcmi.controllers;

import dellcmi.services.ScrapService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapController {

    @Autowired
    private ScrapService service  = new ScrapService();

    @GetMapping("/status")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<String>("Service is running on door :8080. With safe Connection.", HttpStatus.OK);
    }

    @GetMapping("/mainPage")
    @ResponseBody
    public ResponseEntity<?> getMainPage(@RequestParam String username, @RequestParam String password){
        try {
            return new ResponseEntity<>(service.getLoginPageCurl(username, password), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/init")
    @ResponseBody
    public ResponseEntity<?> getResponse(@RequestParam String username, @RequestParam String password){
        try {
            return new ResponseEntity<>(service.initConnection(username, password), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/userInfo")
    public ResponseEntity<?> getResponse(){
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
            .body(service.getUserInfo());
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/sslTrust")
    public ResponseEntity<?> trustSSL(){
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(service.getPageWithTrustSSL());
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}
